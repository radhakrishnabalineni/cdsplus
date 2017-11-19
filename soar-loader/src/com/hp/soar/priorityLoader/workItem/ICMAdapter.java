package com.hp.soar.priorityLoader.workItem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.vfs.FileSystemException;

import com.hp.loader.utils.SendMail;
import com.hp.soar.priorityLoader.utils.LoaderLog;

public class ICMAdapter extends DestinationAdapter {
	private static String emailSmtpHost;
	private static String emailSmtpPort;
	private static String[] emailToList;
	private static String emailFromAddr;
	
	private ICMDestination icmDestination;
	private SFTPDestination sftpDestination;
	private String	icmRule;
	
	public ICMAdapter(String icmRule, ICMDestination icmDestination, SFTPDestination sftpDestination) {
		super();
		this.icmRule = icmRule;
		this.icmDestination = icmDestination;
		this.sftpDestination = sftpDestination;
	}

	@Override
	public void remove(String remoteFile) throws IOException {
		// Take the remoteFile and split it into the directory and filename
		int splitIdx = remoteFile.lastIndexOf(SoarExtractElement.file_separator);
		String serverDir = remoteFile.substring(0, splitIdx);
		String fileName = remoteFile.substring(splitIdx+1);
		
		// This is where the control logic needs to be to handle the actual decision of where to put the file
		boolean icmTarget = icmRule != null && icmRule.length()> 0; 
		long startTime = System.currentTimeMillis();

		if (icmTarget) {
			LoaderLog.info("ICM DELETE swFile: " + remoteFile);
			try {
				icmDestination.remove(fileName, serverDir);
			} catch (IOException ie) {
				String subject = "ICM ERROR loading file";
				String msg = "ICM ERROR IOException removing "+remoteFile+" : "+ie.getMessage();
				LoaderLog.error(msg);
				sendEmailNotification(subject, msg);
				throw ie;
			} catch (ICMException icme) {
				String subject = "ICM load failure";
				String msg = "ICM ERROR removing "+remoteFile+"\nResponseCode: "+icme.getStatusCode()+"\nResponse: "+icme.getResponse();
	        	LoaderLog.error(msg);
	        	sendEmailNotification(subject, msg);
	        	throw new IOException(icme);
	        }
			LoaderLog.info("ICM DELETE finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");			
		}
		if (!icmTarget || icmDestination.isBoth()) {
			// supposed to publish to both destinations so remove SFTP
			try {
				LoaderLog.info("ICM SFTP DELETE "+(!icmTarget ? "not protected":"both")+" swFile: " + remoteFile);
				startTime = System.currentTimeMillis();
				sftpDestination.remove(remoteFile);
				LoaderLog.info("ICM SFTP DELETE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
			} catch (FileNotFoundException fe) {
				LoaderLog.error("FAILED SFTP DELETE of: "+remoteFile+" : "+fe.getClass()+" - "+ fe.getMessage());
				throw fe;
			} catch (IOException ie) {
				LoaderLog.error("FAILED SFTP DELETE of: "+remoteFile+" : "+ie.getClass()+" - "+ ie.getMessage());
				throw ie;
			}
		}
	}

	@Override
	public boolean exists(String remoteFile) throws FileSystemException {
		// Take the remoteFile and split it into the directory and filename
		int splitIdx = remoteFile.lastIndexOf(SoarExtractElement.file_separator);
		String serverDir = remoteFile.substring(0, splitIdx);
		String fileName = remoteFile.substring(splitIdx+1);

		// not checking for existence as of yet so always say it isn't there
		return false;
	}
	
	@Override
	public void putZip(String remoteFile, Source src, int count,
			int numFiles, String republish) throws IOException {
		
		LoaderLog.info("ICM UPDATE ZIP ["+ count + " of " + numFiles + "] "+republish +" swFile : " + src.getName() + " to " + remoteFile);
		long startTime = System.currentTimeMillis();
		put(remoteFile, src);
		LoaderLog.info("ICM UPDATE ZIP finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
	}
	
	@Override
	public void putFile(String remoteFile, Source src) throws IOException {
		long startTime = System.currentTimeMillis();
		if (icmDestination.isICMOn()) {
			LoaderLog.info("ICM UPDATE swFile from: " + src.getName() + " to " + remoteFile);
		}
		put(remoteFile, src);
		if (icmDestination.isICMOn()) {
			LoaderLog.info("ICM UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
		}
	}
	
	private void put(String remoteFile, Source src) throws IOException {
		// Take the remoteFile and split it into the directory and filename
		int splitIdx = remoteFile.lastIndexOf(SoarExtractElement.file_separator);
		String serverDir = remoteFile.substring(0, splitIdx);
		String fileName = remoteFile.substring(splitIdx+1);
		
		// This is where the control logic needs to be to handle the actual decision of where to put the file
		boolean icmTarget = ((icmRule != null && icmRule.length()> 0) && icmDestination.isICMOn()); 

		if (icmTarget) {
			InputStream is = src.getInputStream();
			try {
				LoaderLog.debug("ICM Target "+remoteFile);
				icmDestination.put(fileName, serverDir, is, icmRule);
			} catch (IOException ie) {
				String subject = "ICM ERROR loading file";
				String msg = "ICM ERROR IOException loading "+src.getName()+" to "+remoteFile+" : "+ie.getMessage();
				LoaderLog.error(msg);
				sendEmailNotification(subject, msg);
				throw ie;
			} catch (ICMException icme) {
				String subject = "ICM load failure";
				String msg = "ICM ERROR loading "+src.getName()+" to "+remoteFile+"\nResponseCode: "+icme.getStatusCode()+"\nResponse: "+icme.getResponse();
	        	LoaderLog.error(msg);
	        	sendEmailNotification(subject, msg);
	        	throw new IOException(icme);
	        } finally {
	        	is.close();
	        }
		}
		if (!icmTarget || icmDestination.isBoth()) {
			// supposed to publish to both destinations so publish to SFTP
			// Already read the file from the stream above.  Need to reopen the input file for the sftp read
			InputStream is = src.getInputStream();
			try {
				LoaderLog.info("ICM SFTP UPDATE swFile"+(!icmTarget ? (icmDestination.isICMOn() ? " not protected " : " not on ") : " both " )
						+" from: " + src.getName() + " to " + remoteFile);
				long startTime = System.currentTimeMillis();
				sftpDestination.put(remoteFile, is);
				LoaderLog.info("ICM SFTP UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
			} catch (FileNotFoundException fe) {
				LoaderLog.error("ICM FAILED SFTP UPDATE of: "+remoteFile+" : "+fe.getClass()+" - "+ fe.getMessage());
				throw fe;
			} catch (IOException ie) {
				LoaderLog.error("ICM FAILED SFTP UPDATE of: "+remoteFile+" : "+ie.getClass()+" - "+ ie.getMessage());
				throw ie;
			} finally {
				is.close();
			}
		}
		// see if you need to remove the "other" entry
		if (!icmDestination.isBoth() && icmDestination.isICMOn()) {
			// delete it from the opposite of what it was
			if (icmTarget) {
				// delete it from the SFTP server
				long startTime = System.currentTimeMillis();
				LoaderLog.info("ICM SFTP DELETE swFile: "+ remoteFile);
				sftpDestination.remove(remoteFile);
				LoaderLog.info("ICM SFTP DELETE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
			} else {				
				long startTime = System.currentTimeMillis();
				LoaderLog.info("ICM DELETE swFile: "+ remoteFile);
				try {
					icmDestination.remove(serverDir, fileName);
				} catch (ICMException e) {
					throw new IOException(e);
				}
				LoaderLog.info("ICM DELETE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
			}
		}
	}

	/**
	 * sendEmailNotification of a failure
	 * @param subject
	 * @param msgBody
	 * @param icmFileName
	 * @param icmServerDir
	 * @param localFileAbsolutePath
	 * @param exceptionMsg
	 */
	protected void sendEmailNotification (String subject, String msgBody) {
		try {
			SendMail.setupSendMail(emailSmtpHost, emailSmtpPort, emailFromAddr, emailToList);
			SendMail.sendMessage(subject, msgBody);
		} catch (AddressException e) {
			LoaderLog.error("Bad email address: " + e.getClass() + " - " + e.getMessage());
		} catch (MessagingException e) {
			LoaderLog.error("Failed to deliver email: " + e.getClass() + " - " + e.getMessage());
		}
	}
	
	/**
	 * initialize the email settings for sending notification
	 * @param smtpHost
	 * @param smtpPort
	 * @param toAddr
	 * @param fromAddr
	 */
	public static void initEmailSettings (String smtpHost, String smtpPort, String toAddr, String fromAddr ) {
		emailSmtpHost = smtpHost;
		emailSmtpPort = smtpPort;
		emailToList = toAddr.split(";");
		emailFromAddr = fromAddr;
	}
}
