package com.hp.cdsplus.wds.destination;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.transform.Templates;

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

import com.hp.cdsplus.util.logger.LoggerUtil;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;
import com.hp.cdsplus.wds.exception.RetryException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class SFTPDestination implements IDestination {

  private FileSystemManager fsManager;
  private FileSystemOptions opts;
  private String username;
  private String password;
  private String server; 	
  private String fallback; 		
  private String prefix; 	
  private boolean zeroByteDelete = false;

  private static File resolveCertFile( File base, String name ) {
    if ( name.startsWith( "/") ) {
      return new File( name ); 
    } else {
      return new File( base, name ); 
    }
  }
  
  public List<?> getChildElements(Element element, String name) {
	  ArrayList<Element> list = new ArrayList<Element>();
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

  public FileObject getFileObject(String primary, String secondary) {
    FileObject file = null;
    while (file == null) {
      try {
    	  LoggerUtil.info( "TRYING primary: " + primary );
    	  file = fsManager.resolveFile(primary, opts);
      } catch (FileSystemException e) {
    	  if ( file != null )
    		  resetConnection( file );
    	  
        if (secondary != null) {
          LoggerUtil.info( "TRYING secondary: " + secondary );
          try {
            file = fsManager.resolveFile(secondary, opts);
          } catch (FileSystemException ee) {
            if ( file != null )
              resetConnection( file );
            
          }
        }
      }
      if (file == null) {
    	  try {
			Thread.currentThread();
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
    }
    return file;
  }

  private void resetConnection( FileObject fo ) {
    SftpFileSystem sfs = (SftpFileSystem) fo.getFileSystem();
    LoggerUtil.info( "Connection open " + sfs.isOpen() );
    if (! sfs.isOpen() ) {
      fsManager.closeFileSystem( sfs );
      fsManager.getFilesCache().clear( sfs );
      sfs.closeCommunicationLink();
      sfs.close();
    }
  }
  
  public boolean put(String location, byte[] bytes) throws DestinationException {
    if (!location.startsWith("/")) {
      location = "/" + location;
    }
    String fullPathPrimary = server + "/" + prefix + location;
    String fullPathSecondary = fallback == null ? null : fallback + "/" + prefix + location;
    BufferedOutputStream bos = null;
    FileObject file = null;
    try {
      file = getFileObject(fullPathPrimary, fullPathSecondary);
      if ( file.getType() == FileType.FOLDER ) {
    	  throw new NonRetryException("Can't write file over directory " + prefix + location,new Throwable().fillInStackTrace());
      }
      
      FileObject dir = file.getParent();
      if (!dir.exists() || dir.getType() != FileType.FOLDER) {
        LoggerUtil.warn(dir.getName() + " doesn't exist creating " + dir.getName());
        if ( !hackToCreateDirectoriesWithProperPermissions( dir, 7,7,5 ) ) {
        	throw new NonRetryException("Could not create folder for " + prefix + location + " most likely because there is a file in the way",new Throwable().fillInStackTrace());
        }
      }
        if (! file.exists() ) {
	   		file.createFile();
	    }
      
      if (file.exists() ) {
    	hackToSetPermissions( file, 7,7,5 );
    	FileContent fc = file.getContent();
    	bos = new BufferedOutputStream(fc.getOutputStream());
    	bos.write(bytes);
    	bos.flush();
      }
      return true;
    } catch (IOException e) {
      if ( file != null)
        resetConnection( file );
      throw new NonRetryException(e);
    } finally {
      try {
        if (bos != null) bos.close();
      } catch (IOException e) {
    	  
    	  LoggerUtil.logStackTrace(e);
      }
    }
    
  }

  /**
 * @throws RecoverableException 
 * @throws NonRecoverableException 
   * 
   */
  public boolean remove(String location) throws DestinationException{
    if (!location.startsWith("/")) {
      location = "/" + location;
    }
    if (zeroByteDelete) {
      String fullPath = server + "/" + prefix + location;
      FileObject file = null; 
      try {
        file = fsManager.resolveFile(fullPath, opts);
        if ( file.exists() && file.getType() == FileType.FOLDER ) {
        	throw new NonRetryException("Can't write file over directory " + prefix + location,new Throwable().fillInStackTrace());
        }
        if ( !hackToCreateDirectoriesWithProperPermissions( file.getParent(), 7,7,5 ) ) {
        	throw new NonRetryException("Could not create folder for " + prefix + location + " most likely because there is a file in the way",new Throwable().fillInStackTrace());
        }
        FileContent fc = file.getContent();
        OutputStream os = fc.getOutputStream();
        os.flush();
        os.close();
        hackToSetPermissions( file, 7, 7, 5 );
      } catch (FileSystemException e) {
        if ( file != null)
          resetConnection( file ); 
        throw new RetryException(e);
      } catch (IOException e) {
    	  throw new NonRetryException(e);
	}
    } else {
      String fullPath = server + "/" + prefix + location;
      FileObject file = null; 
      try {
        file = fsManager.resolveFile(fullPath, opts);
        if (file.exists()) {
          file.delete();
        }
      } catch (FileSystemException e) {
        if ( file != null)
          resetConnection( file );
        throw new RetryException(e);
      }
    }
    return true;
  }

  public boolean finalizeDest(boolean force) {
	  return true;
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
    Stack<FileObject> stack = new Stack<FileObject>();
    FileObject f = fo;
    while (!f.exists()) {
      LoggerUtil.info( "Creating dir " + f.toString() );
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
    	  throw new FileSystemException( se.toString() ); 
      } finally {
        if (csf != null) retC.invoke(sfs, new Object[] { csf });
      }
    } catch (InvocationTargetException  e) {
    	
    	throw new FileSystemException(e.toString());
    } catch (IllegalAccessException e) {
    	
      	throw new FileSystemException(e.toString());
    } catch (NoSuchFieldException e) {
    	
    	throw new FileSystemException(e.toString());
    } catch (NoSuchMethodException e) {
    	
    	throw new FileSystemException(e.toString());
    }
  }

  public static String nullCheck(String field, String x) {
    if (x == null || x.equals(""))
      LoggerUtil.error(field + " must have a value");
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
  
  
  
  public static void traverse( FileObject fo, boolean keepGoing ) throws Exception{
    FileType ft = fo.getType();
    //LoggerUtil.info(""+ fo ); 
    if ( ft == FileType.FILE ) {
    } else if ( ft == FileType.FOLDER ) {
      FileObject files[] = fo.getChildren();
      for ( int i = 0; i < files.length; i++ ) {
        if ( keepGoing )
          traverse( files[ i ], false );
      }
    }
  }

  public void init(Element root, File workingDir) throws NonRetryException {
	  prefix = nullCheck("prefix", root.getAttribute("prefix"));
	    fallback = root.getAttribute("fallBack");
	    if (fallback != null && fallback.equals("")) {
	      fallback = null;
	    }
	    String zbd = root.getAttribute("zeroByteDelete");
	    zeroByteDelete = zbd != null && zbd.equals("true");
	    server = nullCheck("server", root.getAttribute("server"));
	    username = nullCheck("username", root.getAttribute("username"));
	    password = nullCheck("password", root.getAttribute("password"));
	    opts = new FileSystemOptions();
	    if (server.startsWith("sftp://")) {
	      try {
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts,
			      "no");
		} catch (FileSystemException e) {
			throw new NonRetryException(e);
		}
	      String cert = root.getAttribute("cert");
	      if (cert != null && !cert.equals("")) {
	        File keyFiles[] = { resolveCertFile(workingDir, cert) };
	        if (!keyFiles[0].exists()) {
	          throw new NullPointerException(" no such private key " + keyFiles[0]);
	        }
	        try {
				SftpFileSystemConfigBuilder.getInstance().setIdentities(opts, keyFiles);
			} catch (FileSystemException e) {
				throw new NonRetryException(e);
			}
	        LoggerUtil.info("SETTING cert " + keyFiles[0]);
	      }
	      String passphrase = root.getAttribute("passphrase");
	      if (passphrase != null && !passphrase.equals("")) {
	        SimpleUserInfo sni = new SimpleUserInfo(passphrase);
	        SftpFileSystemConfigBuilder.getInstance().setUserInfo(opts, sni);
	        LoggerUtil.info("SETTING PASSPHRASE " + passphrase);
	      }
	    }
	    FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
	    StaticUserAuthenticator auth = new StaticUserAuthenticator(null, username,
	        password);
	    try {
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts,
			    auth);
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			fsManager = VFS.getManager();
		} catch (FileSystemException e) {
			throw new NonRetryException(e);
		}
	  
  }

  public boolean needStoreSync() {
	  return false;
  }

public boolean remove(String location, Templates sTemplates)
		throws DestinationException {
	// TODO Auto-generated method stub
	return false;
}
  
}