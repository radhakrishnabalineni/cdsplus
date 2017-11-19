package com.hp.loader.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 * <p>Title: SendMail</p>
 * <p>Description: This is basically a notification service sends
 *                  mail to the "ToAddress" with the message body</p>
 *                  This was taken from the Concentra code and modified to work as a utility in
 *                  the loader code.  This is a singleton object that is given the From/To addresses
 *                  as part of instantiation.
 *                  
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Hewlett-Packard</p>
 */

public class SendMail  {
	//String to set the Content Type
	private static final String CONTENTTYPE = "text/plain";
	
	// singleton reference
	private static SendMail sendMail = null;

	//to set the sender's mail id
	private InternetAddress fromAddr = null;
	//address array to store the mail ids of the To recipients
	private InternetAddress[] toAddrs = null;

	//instantiate a Properties object
	Properties objProperties = new Properties();
	//set a mail session with settings
	Session session = null;
	
	private SendMail(String smtpServerHost, String smtpPort, String fromAddr, String[]toAddr) throws AddressException {
		if (toAddr != null) {
			toAddrs = new InternetAddress[toAddr.length];
			int i=0;
			for(String addr : toAddr) {
				toAddrs[i++] = new InternetAddress(addr); 
			}
		}

		if (fromAddr != null) {
			this.fromAddr = new InternetAddress(fromAddr);
		}
		
		//to put the mail host
		objProperties.put("mail.smtp.host",smtpServerHost);
		//to put the mail port
		objProperties.put("mail.smtp.port",smtpPort);
		//checks the user credentials
		objProperties.put("mail.smtp.auth", "false");
		//set a mail session
		session = Session.getInstance(objProperties,null);
	}


	/**
	 * lSendMessage constructs and sends the mail
	 * @param subject
	 * @param msg
	 */
	private void lSendMessage(String subject, String msg) throws MessagingException {
		
		Message objMessage= new MimeMessage(session);
		
		//to set the TO recipients
		objMessage.setRecipients(Message.RecipientType.TO,toAddrs);
		
		//to set the sender's mail id
		objMessage.setFrom(fromAddr);
		
		//to set the subject of the mail
		objMessage.setSubject(subject);
		
		//to set the content of the mail
		objMessage.setContent(msg, CONTENTTYPE);
		
		//to transport a message to the given address
		Transport transport = session.getTransport(toAddrs[0]);
		//to create the connection of the transport object
		transport.connect();
		//to send the message to the respective addresses
		transport.sendMessage(objMessage,toAddrs);
	}

	/**
	 * sendMessage uses the singleton to send a message
	 * @param subject
	 * @param msg
	 * @throws MessagingException
	 */
	public static void sendMessage(String subject, String msg) throws MessagingException {
		if (sendMail == null) {
			throw new MessagingException("SendMail not set up");
		}
		sendMail.lSendMessage(subject, msg);
	}

	/**
	 * setupSendMail gets the initial information needed to enable notifications.  These
	 * are the same throughout the entire program.
	 * @param smtpServer
	 * @param port
	 * @param fromAddr
	 * @param toAddrs
	 * @throws AddressException 
	 */
	public static void setupSendMail(String smtpServer, String port, String fromAddr, String[] toAddrs) throws AddressException {
		sendMail = new SendMail(smtpServer, port, fromAddr, toAddrs);
	}
}    //end of SendMail
