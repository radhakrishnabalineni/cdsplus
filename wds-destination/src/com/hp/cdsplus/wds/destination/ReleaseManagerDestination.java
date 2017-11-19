package com.hp.cdsplus.wds.destination;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Templates;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.w3c.dom.Element;

import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;
import com.hp.cdsplus.wds.exception.RetryException;
import com.hp.cdsplus.wds.utils.IDestinationConstants;
import com.hp.loader.priorityLoader.NonRecoverableException;
import com.hp.loader.priorityLoader.RecoverableException;
import com.hp.loader.utils.ThreadLog;

public class ReleaseManagerDestination implements IDestination {

	
	private FileSystemOptions opts;
	private String server; 
	private String prefix;
	private String job;
	private String username;
	private String contentArea;
	private String password;
	private String dest_path;
	private File stage;
	private File zipStage;
	private long max_zip_size;
	private long max_zip_limit;
	private long stageTotalSize = 0;
	private File fakeDest = null;
	private boolean is_fake_dest = false;

	private static long lastTime = 0;

	private volatile ConcurrentHashMap<String, byte[]> stageMap = new ConcurrentHashMap<String, byte[]>();
	private volatile StringBuffer webList = new StringBuffer();


	public void init(Element root, File workingDir) {
		//first check if its fake Destination. being run from local system
		String fakeString = XMLUtils.getAttributeValue(root,"fakeDest");

		if (fakeString != null && !fakeString.equals("")) {
			fakeDest = new File(fakeString);
			if (!fakeDest.exists())
				fakeDest.mkdirs();
			is_fake_dest = true;
			//System.out.println("Fake Destination created - "+fakeDest.getAbsolutePath());
		}else
			is_fake_dest = false;
		server 			= XMLUtils.nullCheck("server", XMLUtils.getAttributeValue(root,"server"));
		job 			= XMLUtils.nullCheck("job must be either UPDATE PROD or UPDATE STAGE",XMLUtils.getAttributeValue(root,"job"));
		username 		= XMLUtils.nullCheck("username", XMLUtils.getAttributeValue(root,"username"));
		password 		= XMLUtils.nullCheck("password", XMLUtils.getAttributeValue(root,"password"));

		prefix 			= XMLUtils.nullCheck("prefix", XMLUtils.getAttributeValue(root,"prefix"));
		contentArea 	= XMLUtils.nullCheck("contentArea", XMLUtils.getAttributeValue(root,"contentArea"));
		dest_path		= XMLUtils.nullCheck("path on destination side", XMLUtils.getAttributeValue(root, "dest_path"));
		String max_size = XMLUtils.getAttributeValue(root, "maxPackageSize");
		if (max_size != null){
			max_zip_size	= Long.parseLong(max_size);
			max_zip_size	= (max_zip_size == 0)?(1024*1024):max_zip_size*(1024*1024);
		}else {
			max_zip_size	= 1024*1024;
		}
		String max_count = null;
		max_count = XMLUtils.getAttributeValue(root, "maxPackageCount");
		if(max_count != null){
			max_zip_limit	= Long.parseLong(max_count);
			max_zip_limit	= (max_zip_limit <= 0) ? 100 : max_zip_limit;		
		} 
		createDirs(workingDir, XMLUtils.nullCheck("dir", XMLUtils.getAttributeValue(root,"dir")));
		webList.append(IDestinationConstants.WEBLIST_HEADER);
	}

	public FileSystemManager getSFTPConn() throws FileSystemException{
		StaticUserAuthenticator auth = new StaticUserAuthenticator(null,username, password);
		opts = new FileSystemOptions();
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts,auth);
		if (server.startsWith("sftp://")) 
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		else
			FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
		return VFS.getManager();
	}

	/**
	 * @param workingDir
	 * @param dirString
	 */
	private void createDirs(File workingDir, String dirString) {

		File dir = new File(workingDir, dirString);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		zipStage = new File(dir, "zipStage");
		if (!zipStage.exists()) {
			zipStage.mkdirs();
		}

		stage = new File(dir, "stage");
		if (!stage.exists()) {
			stage.mkdirs();
		}		
	}

	/**
	 * 
	 */
	public boolean  put(String location, byte[] bytes) throws DestinationException{
		synchronized(webList){
			// hold the lock until the full write has been considered.
			
			webList.append("ASIS\t./" + prefix + location + "\n");
			stageTotalSize += bytes.length;
			stageMap.put(prefix + location, bytes);
			// return true if the destination should be finalized
			return (stageMap.size() >= max_zip_limit ||  stageTotalSize > max_zip_size );
		}
	}

	public boolean finalizeDest(boolean force) throws DestinationException{
		// changes for CR 177
		// check for max size or limit
		// either of them is crossed we send the package.
		if ((force && (!stageMap.isEmpty() || webList.length() > IDestinationConstants.WEBLIST_HEADER.length())) || 
				(stageMap.size() >= max_zip_limit ||  stageTotalSize > max_zip_size )){
			// has either reached the last event (force==true) and there is something to be sent or reached either of the zip file size limits
			return finishTar();
		} else {
			// return true if force and there isn't anything to save, otherwise false
			return force ? true : false;
		}
	}

	public synchronized boolean finishTar() throws DestinationException{
		String weblist = null;
		synchronized(webList){
			weblist = webList.toString();
			webList.setLength(0);
			webList.append(IDestinationConstants.WEBLIST_HEADER);
		}
		byte header[] = null;
		long tempName = getLastTime();
		File zipFile = new File(zipStage, contentArea + "." + tempName + ".pkg");
		TarOutputStream zos;
		TarEntry ze = null;
		TarEntry headerEntry = null;
		Iterator<String> itr = null;
		try {
			header = weblist.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NonRetryException(e);
		}
		try {
			zos = new TarOutputStream(new FileOutputStream(zipFile));
			headerEntry = new TarEntry("Weblist");
			headerEntry.setSize(header.length);
			zos.putNextEntry(headerEntry);
			zos.write(header);
			zos.closeEntry();
			zos.flush();
			itr = stageMap.keySet().iterator();
			while(itr.hasNext()){
				String name = itr.next();
				byte[] content 	= stageMap.remove(name);
				ze = new TarEntry(name);
				ze.setSize(content.length);
				zos.putNextEntry(ze);
				zos.write(content);
				zos.closeEntry();
			}

			zos.finish();
			zos.close();
			
			// reset the size of the file, we just wrote out the last set.
			stageTotalSize = 0;
			
			String tix = buildTix(contentArea + "." + tempName,Long.toString(zipFile.length()), job);
			File tixFile = new File(zipStage, contentArea + "." + tempName + ".tix");
			FileOutputStream fos = new FileOutputStream(new File(zipStage,contentArea + "." + tempName + ".tix"));
			fos.write(tix.getBytes("UTF-8"));
			fos.flush();
			fos.close();

			if (is_fake_dest) {
				zipFile.renameTo(new File(fakeDest, zipFile.getName()));
				tixFile.renameTo(new File(fakeDest, tixFile.getName()));
			} else {
				sendToFtp(zipFile, zipFile.getName());
				sendToFtp(tixFile, tixFile.getName());
				if (!zipFile.delete())
					ThreadLog.error("file transfer successful, however couldn't delete - "+zipFile+" from local file system.");
				if (!tixFile.delete())
					ThreadLog.error("file transfer successful, however couldn't delete - "+zipFile+" from local file system.");
			}
			return true;
		} catch (FileNotFoundException e) {
			throw new NonRetryException(e);
		} catch (IOException e) {
			throw new NonRetryException(e);
		}
	}

	private void sendToFtp(File file, String fileName) throws DestinationException{
		BufferedOutputStream bos = null;
		BufferedInputStream is = null;
		FileObject remoteFile = null;
		FileSystemManager fsManager = null;
		try{
			fsManager = getSFTPConn();
			remoteFile = fsManager.resolveFile(server + dest_path + fileName,opts);

			is = new BufferedInputStream(new FileInputStream(file));
			FileContent fc = remoteFile.getContent();
			bos = new BufferedOutputStream(fc.getOutputStream());
			BufferedInputStream bis = new BufferedInputStream(is);
			byte bytes[] = new byte[16 * 1024];
			int bytesRead = 0;
			while ((bytesRead = bis.read(bytes)) != -1) {
				bos.write(bytes, 0, bytesRead);
			}
			bos.flush();
		} catch (FileSystemException e) {
			throw new RetryException("Cannot connect to the remote machine - "+server, e);
		} catch (FileNotFoundException e) {
			throw new RetryException("Cannot find file - "+fileName, e);
		} catch (IOException e) {
			throw new RetryException(e);
		}  finally {
			try {
				if (bos != null)
					bos.close();
				if (is != null)
					is.close();
				closeFtpConnection(fsManager, remoteFile);
			} catch (IOException e) {
			}catch (Exception e) {
			}
		}
	}
	
	/* 
	 * method added for closing the sftp connection
	 */
	private void closeFtpConnection(FileSystemManager fsManager, FileObject remoteFile){
		//remoteFile.close();
		FileSystem fs = remoteFile.getFileSystem();
        fsManager.closeFileSystem(fs);
	}

	public String buildTix(String whatever, String checksum, String job) {
		StringBuffer sb = new StringBuffer();
		sb.append("PUBLISH_KEY\t:" + whatever + "\n");
		sb.append("CHECKSUM\t:" + checksum + "\n");
		sb.append("JOB\t\t:" + job + "\n");
		sb.append("ARCHIVE\t\t:tar\n");
		sb.append("WEBLIST\t\t:Weblist\n");
		sb.append("INFORM_EXTRA\t:\n");
		sb.append("REL_VERSION\t:accessHP1.5\n");
		return sb.toString();
	}

	/**
	 * @throws RecoverableException 
	 * @throws NonRecoverableException 
	 * 
	 */
	public boolean remove(String location) throws DestinationException {	
		synchronized (webList){
			webList.append("OBS\t./" + prefix + location + "\n");
		}
		return false;
	}


	/**
	 * @return
	 */
	public synchronized static long getLastTime() {
		long x = System.currentTimeMillis();
		if (x <= lastTime) {
			x = lastTime + 1;
		}
		lastTime = x;
		return lastTime;
	}

	public boolean needStoreSync() {
		// This destination creates a zip file which should have full items put in it.  Not items mixed.
		return true;
	}

	public boolean remove(String location, Templates sTemplates)
			throws DestinationException {
		return false;
	}
}
