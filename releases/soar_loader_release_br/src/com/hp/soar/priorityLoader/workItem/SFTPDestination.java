package com.hp.soar.priorityLoader.workItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileObject;
import org.apache.commons.vfs.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.TrustEveryoneUserInfo;

import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.vfs.util.FSConnectionPool;
import com.hp.soar.priorityLoader.vfs.util.RemoteConfigUtil;
import com.hp.soar.priorityLoader.vfs.util.RemoteConfiguration;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

@SuppressWarnings({"unused","unchecked"})
public class SFTPDestination extends Destination {

	public static final String REMOTEDESTINATION = "remoteDestination";
	private static final String BLANK = "";
	private static final int BUFFSIZE = 32 * 1024; // set buffsize to 32K for
													// upload

	// ID of the server to send software to (ftp.hp.com = 1)
	private String serverOid;

	private  FileSystemOptions opts;
	private  String username;
	private  String password;
	private  String server; // "sg-dev-app-cdsplus.boise.itc.hp.com";
	private  String prefix; // "/mnt/cdsplus/home/cdspwww/wds/";

	private boolean zeroByteDelete = false;

	private static RemoteConfiguration remoteConfig = null;

	public SFTPDestination(ConfigurationReader config, DestinationType destType) throws FileSystemException {
		super(config, destType);
	}

	public void initDestination(ConfigurationReader config)
			throws FileSystemException {

		if (remoteConfig == null) {
			remoteConfig = RemoteConfigUtil.praseConfig(config);
		}

		String className = remoteConfig.getClassName();
		// This is where different destinations can be configured when required.
		// for now only sftpDestination is supported
		if (className == null
				|| !className.equals(SFTPDestination.class.getName())) {
			throw new IllegalArgumentException(className
					+ " is not specified ic config file or is not supported");
		}

		prefix = remoteConfig.getPrefix();
		serverOid = remoteConfig.getServerOid();

		String zbd = remoteConfig.getZbd();
		zeroByteDelete = zbd != null && zbd.equals("true");

		username = remoteConfig.getUserName();

		String serverUrl = remoteConfig.getServerUrl();

		String temp[] = serverUrl.split("//");
		if (!serverUrl.startsWith("sftp") && temp.length != 2) {
			throw new IllegalArgumentException("Server name " + serverUrl
					+ " is not an sftp server");
		} else {
			// Appending username to Server URL for Key pair Authentication
			server = temp[0] + "//" + username + ":"
					+ remoteConfig.getPassword() + "@" + temp[1];
		}
		opts = new FileSystemOptions();

		password = remoteConfig.getPassword();
		String sshKey = remoteConfig.getSshKey();
		String passphrase = remoteConfig.getPassphrase();

		// BR667134 : If password exists in config.xml, Username/Password
		// authentication will be used
		if (password != null && !password.equals("")) {
			StaticUserAuthenticator auth = new StaticUserAuthenticator(null,
					username, password);
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
					opts, auth);

			// TODO remove while deploying, added for testing purpose
			// SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
			// opts, "no");
		}
		// BR667134 : If password doesn't exists in config.xml, Key pair
		// authentication will be used
		else if (sshKey != null && !sshKey.equals("")) {
			File keyFiles[] = { new File(sshKey) };
			if (!keyFiles[0].exists()) {
				throw new IllegalArgumentException(" no such private key "
						+ keyFiles[0]);
			}
			LoaderLog.info("SETTING sshKey " + keyFiles[0]);
			SftpFileSystemConfigBuilder.getInstance().setIdentities(opts,
					keyFiles);
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					opts, "no");

			// SftpFileSystemConfigBuilder.getInstance().setPassiveMode(opts,
			// true);

			if (passphrase != null && !passphrase.equals("")) {

				SimpleUserInfo sni = new SimpleUserInfo(passphrase);
				SftpFileSystemConfigBuilder.getInstance()
						.setUserInfo(opts, sni);
				LoaderLog.info("SETTING PASSPHRASE " + passphrase);
			}
		}

		// BR667134 : If password and SSH keys doesn't exists in config.xml,
		// throw Exception
		else {
			throw new IllegalArgumentException(
					"No password or SSH private key provided for Authentication. "
							+ "Please provide a valid private key or password.");
		}
		// End code changes by <048174> Niharika on 14-Mar-2013 for BR667134

	}

	public static String checkRequiredEntry(String field, String x) {
		if (x == null || x.equals("")) {
			throw new IllegalArgumentException(" config file field: <" + field
					+ "> must have a value");
		}
		return x;
	}

	private FileObject getFileObject(String primary, FileSystemManager fsManager)
			throws FileSystemException {
		FileObject file = null;
		while (file == null) {
			try {
				if (LoaderLog.isDebugEnabled()) {
					LoaderLog.debug( "getFileObject: " + primary );
				}
				file = fsManager.resolveFile(primary, opts);
			} catch (FileSystemException e) {
				//if ( file != null )  // commented by venkat as we are not using reset connection
					//    		  resetConnection( file );
				LoaderLog.error( LoaderLog.getExceptionMsgForLog("getFileObject ", e));
				System.exit(1);  //abnormal termination  when FTP session failure (BR686204--13.2 Release)
			        
				
			}
		}
		return file;
	}

	/**
	 * get the server oid for the sftp destination server
	 * 
	 * @return
	 */
	public String getServerOid() {
		return serverOid;
	}

	/**
	 * exists check to see if a location is on the remote server
	 * 
	 * @param location
	 * @return
	 * @throws FileSystemException
	 */
	public boolean exists(String location) throws FileSystemException {

		FileSystemManager fsManager = null;
		boolean flag = false;

		if (!location.startsWith("/")) {
			location = "/" + location;
		}

		String fullPathPrimary = server + "/" + prefix + location;
		FileObject file = null;

		try {
			fsManager = FSConnectionPool.getInstance(remoteConfig)
					.getFSManager();
			file = getFileObject(fullPathPrimary, fsManager);
			if (file == null) {
				return false;
			}

			if (file.getType() == FileType.FOLDER) {
				// return false; //commented by venkat, declared flag variable
				return flag;
			}

			FileObject dir = file.getParent();
			if (!dir.exists() || dir.getType() != FileType.FOLDER) {
				// return false; //commented by venkat, declared flag variable
				return flag;
			}
			flag = file.exists();
		} catch (FileSystemException e) {
			LoaderLog
					.error(LoaderLog
							.getExceptionMsgForLog(
									"FileSystemException occured while checking file exists or not",
									e));
			System.exit(1);  //abnormal termination  when FTP session failure (BR686204--13.2 Release)
		} finally {
			FSConnectionPool.getInstance(remoteConfig).returnSFSManager(
					fsManager);
		}

		return flag;
		
	}

	// ICM changes
	public void executeUpdate (String remoteFile, InputStream is, String icmFileName, String icmServerDir, String localFileAbsolutePath) throws IOException {
		put(remoteFile, is);
	}
	public void put(String location, InputStream is) throws IOException {
		// indicate that this thread is intering the VFS system

		FileSystemManager fsManager = null;

		if (!location.startsWith("/")) {
			location = "/" + location;
		}

		String fullPathPrimary = server + "/" + prefix + location;
		BufferedOutputStream bos = null;

		try {
			fsManager = FSConnectionPool.getInstance(remoteConfig)
					.getFSManager();
			// short circuit put for testing for now
			// if (false) {
			// LoaderLog.info("SFTP - put to "+fullPathPrimary+" Skipped");
			// return;
			// }

			FileObject file = getFileObject(fullPathPrimary, fsManager);

			// fsManager.resolveFile(fullPathPrimary, getFileSystemOptions());

			if (file.getType() == FileType.FOLDER) {
				LoaderLog.error("Can't write file over directory " + prefix
						+ location);
				return;
			}

			FileObject dir = file.getParent();
			if (!dir.exists() || dir.getType() != FileType.FOLDER) {
				LoaderLog.warn(dir.getName() + " doesn't exist creating "
						+ dir.getName());
				if (!hackToCreateDirectoriesWithProperPermissions(dir, 7, 7, 5)) {
					LoaderLog
							.error("Could not create folder for "
									+ prefix
									+ location
									+ " most likely because there is a file in the way");
					return;
				}
			}
			if (!file.exists()) {
				file.createFile();
			}

			if (file.exists()) {
				hackToSetPermissions(file, 7, 7, 5);
				FileContent fc = file.getContent();
				bos = new BufferedOutputStream(fc.getOutputStream());
				BufferedInputStream bis = new BufferedInputStream(is);
				byte bytes[] = new byte[BUFFSIZE];
				int bytesRead = 0;
				while ((bytesRead = bis.read(bytes)) != -1) {
					bos.write(bytes, 0, bytesRead);
				}
				bos.flush();
			}
		} catch (IOException e) {
			// There was a problem, reset the connection
			LoaderLog.error(LoaderLog.getExceptionMsgForLog(
					"put IOException: ", e));
			LoaderLog.logStackTrace(e);
			System.exit(1);
			throw new IOException(e.toString());
		} finally {
			try {
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				LoaderLog.error(LoaderLog.getExceptionMsgForLog(
						"put failed to close bos: ", e));
				LoaderLog.logStackTrace(e);
			}
			FSConnectionPool.getInstance(remoteConfig).returnSFSManager(
					fsManager);
		}

	}
	// ICM changes
	public void executeDelete(String remoteFile, String icmFileName, String serverDir) throws IOException {
		remove(remoteFile);
	}
	/**
	 * 
	 */
	public void remove(String location) throws IOException {

		FileSystemManager fsManager = null;

		if (!location.startsWith("/")) {
			location = "/" + location;
		}

		if (zeroByteDelete) {

			String fullPath = server + "/" + prefix + location;
			FileObject file = null;

			try {
				// using VFS system
				fsManager = FSConnectionPool.getInstance(remoteConfig)
						.getFSManager();

				file = fsManager.resolveFile(fullPath, getFileSystemOptions());

				if (file.exists() && file.getType() == FileType.FOLDER) {
					LoaderLog.error("Can't write file over directory " + prefix
							+ location);
					return;
				}
				if ( !hackToCreateDirectoriesWithProperPermissions( file.getParent(), 7,7,5 ) ) {
					LoaderLog.error( "Could not create folder for" + location + " most likely because there is a file in the way"); 
					return;
				}
				FileContent fc = file.getContent();
				OutputStream os = fc.getOutputStream();
				os.flush();
				os.close();
				hackToSetPermissions( file, 7, 7, 5 );
			} catch (FileSystemException e) {
				//        if ( file != null)
				//          resetConnection( file );
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("zeroByteDelete FileSystemException: ", e));
				System.exit(1);  //(BR686204--13.2 Release)
				throw new IOException(e.toString());
			} finally {
				FSConnectionPool.getInstance(remoteConfig).returnSFSManager(
						fsManager);
			}
		} else {
			String fullPath = server + "/" + prefix + location;
			FileObject file = null;
			try {
				fsManager = FSConnectionPool.getInstance(remoteConfig)
						.getFSManager();

				file = fsManager.resolveFile(fullPath, getFileSystemOptions());
				if (file.exists()) {
					file.delete();
				}

				deleteEmptyParents(file.getParent());

			} catch (FileSystemException e) {
				//        if ( file != null)
				//          resetConnection( file );
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("delete FileSystemException", e));
				// ICM CR #513:: Do not exit loader if the exception is due to file not existing on sftp server location
				if(!e.getCause().toString().equals("2: No such file"))
				System.exit(1); //abnormal termination  when FTP session failure (BR686204--13.2 Release)
				throw new IOException(e.toString());
			} finally {
				FSConnectionPool.getInstance(remoteConfig).returnSFSManager(
						fsManager);
			}
		}
	}

	/**
	 * deleteEmptyParents removes all of the directories from this file up that are empty
	 * @param file
	 * @throws FileSystemException
	 */
	private void deleteEmptyParents(FileObject dir) throws FileSystemException {
		if (dir.exists()) {
			FileObject[] children = dir.getChildren();
			if (children != null && children.length > 0) {
				return;
			}
			try {
				dir.delete();
			} catch (FileSystemException e) {
				// if the dir can't be deleted probably because of permissions so just return
				LoaderLog.error("Exception deleting dir: "+dir.toString()+" skipping delete. ");
				return;
			}
		}
		deleteEmptyParents(dir.getParent());
	}
	public void finish() {
	}


	/**
	 * Hi, if you are looking at this method you probably stop. 
	 * basically it creates all the directories for that object
	 * see the hackToSetPermissions method to see just why it 
	 * sucks 
	 * @param fo
	 * @param me
	 * @param group
	 * @param everyone
	 * @throws FileSystemException
	 */
	public boolean hackToCreateDirectoriesWithProperPermissions(FileObject fo, int me, int group, int everyone)
			throws FileSystemException {
		Stack stack = new Stack();
		FileObject f = fo;
		while (!f.exists()) {
			LoaderLog.info( "Creating dir " + f.toString() );
			stack.push(f);
			f = f.getParent();
		}
		if ( f.exists() && f.getType() != FileType.FOLDER ) {
			return false; 
		}
		while (!stack.empty()) {
			f = (FileObject) stack.pop();
			f.createFolder();
			hackToSetPermissions(f, me, group, everyone);
		}
		return true;
	}

	/**
	 * This is bad bad bad method... never ever do what you see here
	 * infact don't even look at this method... run away from it
	 * It breaks the encapsulation model for java... and just cuz you
	 * can do it doesn't mean you should, unless you have to which is 
	 * whey I wrote this.. I am quite sure I will end up in some hell
	 * for java programmers for this one... 
	 * @param fo
	 * @param me
	 * @param group
	 * @param everyone
	 * @throws FileSystemException
	 */
	public void hackToSetPermissions(FileObject fo, int me, int group,
			int everyone) throws FileSystemException {
		try {
			SftpFileSystem sfs = (SftpFileSystem) fo.getFileSystem();
			Field field = SftpFileObject.class.getDeclaredField( "relPath" );
			field.setAccessible(true);
			String relPath = (String)field.get( fo );
			Method getC = SftpFileSystem.class.getDeclaredMethod("getChannel",
					new Class[] {});
			Method retC = SftpFileSystem.class.getDeclaredMethod("putChannel",
					new Class[] { ChannelSftp.class });
			getC.setAccessible(true);
			retC.setAccessible(true);
			ChannelSftp csf = null;
			try {
				csf = (ChannelSftp) getC.invoke(sfs, null);
				csf.chmod(me << 6 | group << 3 | everyone, relPath);
			} catch( SftpException se ) {
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("hackToSetPermissions",se));
				throw new FileSystemException( se.toString() ); 
			} finally {
				if (csf != null) retC.invoke(sfs, new Object[] { csf });
			}
		} catch (InvocationTargetException  e) {
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("invocationTargetException ",e));
			throw new FileSystemException(e.toString());
		} catch (IllegalAccessException e) {
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("illegalAccessException ", e));
			throw new FileSystemException(e.toString());
		} catch (NoSuchFieldException e) {
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("noSuchFieldException ", e));
			throw new FileSystemException(e.toString());
		} catch (NoSuchMethodException e) {
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("noSuchMethodException ", e));
			throw new FileSystemException(e.toString());
		}
	}

	public static String nullCheck(String field, String x) {
		if (x == null || x.equals(""))
			LoaderLog.error(field + " must have a value");
		if (x == null || x.equals("")) throw new NullPointerException(field
				+ " must have a value");
		else return x;
	}

	public static class SimpleUserInfo extends TrustEveryoneUserInfo {

		String passphrase = null;

		public SimpleUserInfo(String passphrase) {
			this.passphrase = passphrase;
		}

		public boolean promptPassphrase(String msg) {
			return true;
		}

		public String getPassphrase() {
			return passphrase;
		}
	}

	static private long encode(String s) {
		byte[] bytes = s.getBytes();
		int numInts = bytes.length / 8 + (bytes.length % 8 != 0 ? 1 : 0);
		long longVal = 0;
		for(int i=bytes.length-1; i >= 0; i--) {
//			int intLoc = i / 8;
//			int byteLoc = i % 8;
			longVal = ((longVal << 8) | bytes[i]);
		}
		return longVal;
		
	}
	
	static private String decode(String val) {
		byte[] bytes = new byte[8];
		long v = Long.parseLong(val);
		for(int i=0; i<8; i++) {
			bytes[i] = (byte)(v & 0xFF);
			v = v >>> 8;
		}
		return new String(bytes);
	}
	
	public static void main( String a[] ) throws Exception{
		if (a[0].equals("-e")) {
			if (a[1].length() != 8) {
				System.err.println("8 character pwd required");
			}
			long longVal = encode(a[1]);
			System.out.println(longVal);
			return;
		}
		
		if (a[0].equals("-d")) {
			String val = decode(a[1]);
			System.out.println(val);
			return;
		}

		String server = a[ 0 ];
		String username = a[ 1 ];
		String password = a[ 2 ];
		String prefix = a[ 3 ];
		String cert = a.length>4?a[4]:null;
		String passphrase = a.length>5?a[5]:null;
		FileSystemOptions opts = new FileSystemOptions();
		if (server.startsWith("sftp://")) {
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts,
					"no");
			if (cert != null && !cert.equals("")) {
				File keyFiles[] = { new File( cert ) };
				if (!keyFiles[0].exists()) {
					throw new NullPointerException(" no such private key " + keyFiles[0]);
				}
				SftpFileSystemConfigBuilder.getInstance().setIdentities(opts, keyFiles);
				LoaderLog.info("SETTING cert " + keyFiles[0]);
			}
			if (passphrase != null && !passphrase.equals("")) {
				SimpleUserInfo sni = new SimpleUserInfo(passphrase);
				SftpFileSystemConfigBuilder.getInstance().setUserInfo(opts, sni);
				LoaderLog.info("SETTING PASSPHRASE " + passphrase);
			}
		}
		FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
		// String dirString = nullCheck("dir", config.getAttribute("dir"));
		StaticUserAuthenticator auth = new StaticUserAuthenticator(null, username,        password);
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts,
				auth);
		FileSystemManager fsManager = VFS.getManager(); 
		String fullPathPrimary = server + "/" + prefix;
		FileObject fo = fsManager.resolveFile( fullPathPrimary );
		traverse( fo, true );
	}

	public static void traverse( FileObject fo, boolean keepGoing ) throws Exception{
		FileType ft = fo.getType();
		LoaderLog.info(""+ fo ); 
		if ( ft == FileType.FILE ) {
		} else if ( ft == FileType.FOLDER ) {
			FileObject files[] = fo.getChildren();
			for ( int i = 0; i < files.length; i++ ) {
				if ( keepGoing )
					traverse( files[ i ], false );
			}
		}
	}

	@Override
	public String getSoarIcmIntegrationOn() {
		return BLANK;
	}

	@Override
	public void setIcmRule(String icmRule) {
		// Do nothing
	}

	/**
	 * Method to setup get SFTP config
	 * 
	 * @return the FileSystemOptions object containing the specified
	 *         configuration options
	 */
	private FileSystemOptions getFileSystemOptions() {

		return this.opts;
	}

}