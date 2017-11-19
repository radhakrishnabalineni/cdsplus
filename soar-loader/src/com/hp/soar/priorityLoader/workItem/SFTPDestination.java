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
import java.util.ArrayList;
import java.util.List;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

@SuppressWarnings({"unused","unchecked"})
public class SFTPDestination {

	public static final String REMOTEDESTINATION = "remoteDestination";
	private static final String RDCLASSNAME = "class";
	private static final String SSHKEYFILE = "sshkey";
	private static final String SERVER= "server";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String ZEROBYTEDELETE = "zeroByteDelete";
	private static final String PASSPHRASE= "passphrase";
	private static final String SERVEROID = "serverOid";
	private static final String PREFIX = "prefix";

	private static final int BUFFSIZE = 32 * 1024; // set buffsize to 32K for upload

	// ID of the server to send software to (ftp.hp.com = 1)

	private String serverOid;

	// object for synchronizing reset connection on
	private Object resetConnectionObj = new Object();
	
	private final FileSystemManager fsManager;
	private final FileSystemOptions opts;
	private final String username;
	private final String password;
	private final String server; 	// "sg-dev-app-cdsplus.boise.itc.hp.com";
	private final String prefix; 	// "/mnt/cdsplus/home/cdspwww/wds/";
	
	// flag indicating that VFS needs to be reset
	private boolean vfsResetPending = false;
	private FileObject vfsResetFileObject = null;
	
	// # of threads that are actively using VFS
	private int 	usingVFS = 0;
	
	private boolean zeroByteDelete = false;

	public SFTPDestination(ConfigurationReader config) throws FileSystemException {
		super();

		Element rdElement = config.getElement(REMOTEDESTINATION);
		if (rdElement == null) {
			throw new IllegalArgumentException(REMOTEDESTINATION+" not specified in config file.");
		}

		String className = rdElement.getAttribute(RDCLASSNAME);
		// This is where different destinations can be configured when required. 
		// for now only sftpDestination is supported
		if (className == null || !className.equals(SFTPDestination.class.getName())) {
			throw new IllegalArgumentException(className +" is not specified ic config file or is not supported");
		}

		prefix = checkRequiredEntry(PREFIX, rdElement.getAttribute(PREFIX));
		serverOid = checkRequiredEntry(SERVEROID, rdElement.getAttribute(SERVEROID));

		String zbd = rdElement.getAttribute(ZEROBYTEDELETE);
		zeroByteDelete = zbd != null && zbd.equals("true");
		
		// Start code changes by <048174> Niharika on 14-03-2013 for BR667134

		// server = checkRequiredEntry(SERVER, rdElement.getAttribute(SERVER));

		username = checkRequiredEntry(USERNAME,
				rdElement.getAttribute(USERNAME));
		// password = checkRequiredEntry(PASSWORD,
		// rdElement.getAttribute(PASSWORD)); //BR667134:Commented as this
		// attribute can be blank in case of Key Authentication

		String serverUrl = checkRequiredEntry(SERVER,
				rdElement.getAttribute(SERVER));
		String temp[] = serverUrl.split("//");
		if (!serverUrl.startsWith("sftp") && temp.length != 2) {
			throw new IllegalArgumentException("Server name " + serverUrl
					+ " is not an sftp server");
		} else {
			// Appending username to Server URL for Key pair Authentication
			server = temp[0] + "//" + username + "@" + temp[1];
		}
		opts = new FileSystemOptions();

		password = rdElement.getAttribute(PASSWORD);
		String sshKey = rdElement.getAttribute(SSHKEYFILE);
		String passphrase = rdElement.getAttribute(PASSPHRASE);

		// BR667134 : If password exists in config.xml, Username/Password
		// authentication will be used
		if (password != null && !password.equals("")) {
			StaticUserAuthenticator auth = new StaticUserAuthenticator(null,
					username, password);
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
					opts, auth);

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

		fsManager = VFS.getManager();
	}

	public static String checkRequiredEntry(String field, String x) {
		if (x == null || x.equals("")) {
			throw new IllegalArgumentException(" config file field: <" +field + "> must have a value");
		}
		return x;
	}



	public List getChildElements(Element element, String name) {
		List list = new ArrayList();
		for (Node child = element.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element ele = (Element) child;
				if (ele.getNodeName().equals(name)) {
					list.add(ele);
				}
			}
		}
		return list;
	}

	public FileObject getFileObject(String primary) throws FileSystemException {
		FileObject file = null;
		while (file == null) {
			try {
				if (LoaderLog.isDebugEnabled()) {
					LoaderLog.debug( "getFileObject: " + primary );
				}
				file = fsManager.resolveFile(primary, opts);
			} catch (FileSystemException e) {
				resetConnection( file );
				throw e;
			}
		}
		return file;
	}

	/**
	 * get the server oid for the sftp destination server
	 * @return
	 */
	public String returnServerOid() {
		return serverOid;
	}

	/**
	 * EnterVFS should be called right before any VFS transaction is started
	 * It checks to see if vfs needs to be reset and if so waits until it is
	 */
	private void enterVFS() {
		synchronized (fsManager) {
			if (vfsResetPending) {
				// Wait for notify that the reset is done
				try {
					LoaderLog.debug("EnterVFS waiting for reset");
					fsManager.wait();
				} catch (InterruptedException ie){
				}
			} 
			// indicate that this thread is using the VFS
			++usingVFS;
			LoaderLog.debug("EnterVFS " + usingVFS);
		}
	}
	
	/**
	 * exitVFS indicates that this thread is no longer in the VFS filesystem.  
	 * If a reset is pending, it will initiate the reset, otherwise it will wait until the reset is completed.
	 */
	private void exitVFS() {
		// Control is handled by the fsManager object
		synchronized (fsManager) {
			// decrement the number of threads using the VFS
			--usingVFS;
			LoaderLog.debug("ExitVFS: "+usingVFS);
			if (vfsResetPending) {
				// a reset is pending
				if (usingVFS == 0) {
					// no other thread is using the VFS so reset the connection
					LoaderLog.debug("ExitVFS doing VFS reset");
					resetVFSConnection();
					
					// tell the other threads the connection is OK to use
					fsManager.notifyAll();
				} else {
					// there is still a thread using the VFS so wait for that thread to handle the reset
					try {
						LoaderLog.debug("ExitVFS waiting for reset");
						fsManager.wait();
					} catch (InterruptedException ie) {
						LoaderLog.debug("ExitVFS Resuming after reset");
					}
				}
			}
		}
	}
	
	/**
	 * resetConnection adds this destination to the collected ones  If all connections have been captured, it resets the
	 * vfs system and allows all of the threads to continue by notifying them.
	 * @throws FileSystemException 
	 */
	private void resetConnection( FileObject fo ) {
		synchronized (fsManager) {
			LoaderLog.debug("ResetConnection requested");
			vfsResetPending = true;
			if (fo != null) {
				vfsResetFileObject = fo;
			}
		}
	}
	
	/**
	 * resetConnection adds this destination to the collected ones  If all connections have been captured, it resets the
	 * vfs system and allows all of the threads to continue by notifying them.
	 * @throws FileSystemException 
	 */
	private void resetVFSConnection( ) {
		// add this thread to the waiting ones
		if (vfsResetFileObject != null) {
			SftpFileSystem sfs = (SftpFileSystem) vfsResetFileObject.getFileSystem();
			LoaderLog.info( "ResetVFSConnection open " + sfs.isOpen() );
			if (! sfs.isOpen() ) {
				fsManager.closeFileSystem( sfs );
				fsManager.getFilesCache().clear( sfs );
				sfs.closeCommunicationLink();
				sfs.close();
			}
			// reset the File Object
			vfsResetFileObject = null;
			// reset the pending signal
			vfsResetPending = false;
			
		} else {
			// Didn't get a fileObject to reset.  The loader will hang so exit
			LoaderLog.error("Resetting VFS fileSystem failed.  No FileObject provided.");
			System.exit(1);
		}
	}
	
	/**
	 * exists check to see if a location is on the remote server
	 * @param location
	 * @return
	 * @throws FileSystemException
	 */
	public boolean exists(String location) throws FileSystemException {
		boolean flag = false;
		if (!location.startsWith("/")) {
			location = "/" + location;
		}
		String fullPathPrimary = server + "/" + prefix + location;
		FileObject file = null;
		try{
			// enter VFS use
			enterVFS();
			file = getFileObject(fullPathPrimary);
			if (file == null) {
				return false;
			}
			
			if ( file.getType() == FileType.FOLDER ) {
				//return false;  //commented by venkat, declared flag variable
				return flag;
			}

			FileObject dir = file.getParent();
			if (!dir.exists() || dir.getType() != FileType.FOLDER) {
				//return false; //commented by venkat, declared flag variable
				return flag;
			}
			flag =  file.exists();
		}catch(FileSystemException e){
			resetConnection(file);
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("FileSystemException occured while checking file exists or not", e));
			throw e;
		} finally {
			// exit VFS use
			exitVFS();
		}
		
		return flag;

	}

	public void put(String location, InputStream is) throws IOException {
		// indicate that this thread is intering the VFS system
		enterVFS();
		
		if (!location.startsWith("/")) {
			location = "/" + location;
		}
		String fullPathPrimary = server + "/" + prefix + location;
		BufferedOutputStream bos = null;
		FileObject file = null;
		try {
			// short circuit put for testing for now
//			if (false) {
//				LoaderLog.info("SFTP - put to "+fullPathPrimary+" Skipped");
//				return;
//			}
			
			file = getFileObject(fullPathPrimary);
			if ( file.getType() == FileType.FOLDER ) {
				LoaderLog.error( "Can't write file over directory " + prefix + location ); 
				return;
			}

			FileObject dir = file.getParent();
			if (!dir.exists() || dir.getType() != FileType.FOLDER) {
				LoaderLog.warn(dir.getName() + " doesn't exist creating " + dir.getName());
				if ( !hackToCreateDirectoriesWithProperPermissions( dir, 7,7,5 ) ) {
					LoaderLog.error( "Could not create folder for " + prefix + location + " most likely because there is a file in the way"); 
					return;
				}
			}
			if (! file.exists() ) {
				file.createFile();
			}

			if (file.exists() ) {
				hackToSetPermissions( file, 7,7,5 );
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
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("put IOException: ", e));
			LoaderLog.logStackTrace(e);
			resetConnection( file );
			throw new IOException(e.toString());
		} finally {
			try {
				if (bos != null) bos.close();
			} catch (IOException e) {
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("put failed to close bos: ",e));
				LoaderLog.logStackTrace(e);
			}
			// indicate that this thread is done with the VFS system
			exitVFS();
		}

	}

	
	/**
	 * 
	 */
	public void remove(String location) throws IOException {
		if (!location.startsWith("/")) {
			location = "/" + location;
		}
		if (zeroByteDelete) {
			String fullPath = server + "/" + prefix + location;
			FileObject file = null; 
			try {
				// using VFS system
				enterVFS();
				
				file = fsManager.resolveFile(fullPath, opts);
				if ( file.exists() && file.getType() == FileType.FOLDER ) {
					LoaderLog.error( "Can't write file over directory " + prefix + location ); 
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
				resetConnection( file );
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("zeroByteDelete FileSystemException: ", e));
				throw new IOException(e.toString());
			} finally {
				// done using vfs
				exitVFS();
			}
		} else {
			String fullPath = server + "/" + prefix + location;
			FileObject file = null; 
			try {
				if (false) {
					LoaderLog.info("SFTP - delete : "+fullPath+" Skipped");
					return;
				}
				
				// using vfs
				enterVFS();
				
				file = fsManager.resolveFile(fullPath, opts);
				if (file.exists()) {
					file.delete();
				}
				deleteEmptyParents(file.getParent());

			} catch (FileSystemException e) {
				resetConnection( file );
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("delete FileSystemException", e));
				throw new IOException(e.toString());
			} finally {
				// done using vfs
				exitVFS();
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

}