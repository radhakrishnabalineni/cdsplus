package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.vfs.FileSystemException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Element;

import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.utils.SendMail;
import com.hp.soar.priorityLoader.utils.LoaderLog;

public class ICMWSDestination extends Destination {
	private static final String ICMWSDESTINATION = "icmDestination";
	private static final String ICMCLASSNAME = "class";
	private static final String BASEHTTPSURL = "icm_base_https_url";
	private static final String SOARICMINTEGRATION = "soar_icm_integration_on";
	private static final String SERVERNAME = "server";
	private static final String USERNAME = "username";
	private static final String USERPASSWORD = "password";
	private static final String SERVEROID = "serverOid";
	private static final String PREFIX = "prefix";
	private static final String EMAILSMTPHOST = "email_smtp_host";
	private static final String EMAILSMTPPORT = "email_smtp_port";
	private static final String EMAILTOADDR = "email_to_addr";
	private static final String EMAILFROMADDR = "email_from_addr";

	private String icmBaseHttpsUrl;
	private String soarIcmIntegrationOn;
	private String ftpServer;
	private String icmUsername;
	private String icmPassword;
	private final String uploadRequest = "/upload";
	private final String deleteRequest = "/delete";
	// ID of the server to send software to (ICM = )
	private String icmServerOid;
	private String icmPrefix;
	private String emailSmtpHost;
	private String emailSmtpPort;
	private String emailToAddr;
	private String emailFromAddr;
	
	private String icmRuleId;
	private static final int BUFFER_SIZE = 1024;

	public ICMWSDestination (ConfigurationReader config, DestinationType destType) throws FileSystemException {
		super(config, destType);
	}
	
	public void initDestination(ConfigurationReader config) throws FileSystemException {
		Element rdElement = config.getElement(ICMWSDESTINATION);
		if (rdElement == null) {
			throw new IllegalArgumentException(ICMWSDESTINATION+" not specified in config file.");
		}

		String className = rdElement.getAttribute(ICMCLASSNAME);

		if (className == null || !className.equals(ICMWSDestination.class.getName())) {
			throw new IllegalArgumentException(className +" is not specified in config file or is not supported");
		}
		icmBaseHttpsUrl = checkRequiredEntry(BASEHTTPSURL, rdElement.getAttribute(BASEHTTPSURL));
		soarIcmIntegrationOn = checkRequiredEntry(SOARICMINTEGRATION, rdElement.getAttribute(SOARICMINTEGRATION));
		ftpServer = checkRequiredEntry(SERVERNAME, rdElement.getAttribute(SERVERNAME));
		icmUsername = checkRequiredEntry(USERNAME, rdElement.getAttribute(USERNAME));
		icmPassword = checkRequiredEntry(USERPASSWORD, rdElement.getAttribute(USERPASSWORD));
		icmServerOid = checkRequiredEntry(SERVEROID, rdElement.getAttribute(SERVEROID));
		icmPrefix = checkRequiredEntry(PREFIX, rdElement.getAttribute(PREFIX));
		emailSmtpHost = checkRequiredEntry(EMAILSMTPHOST, rdElement.getAttribute(EMAILSMTPHOST));
		emailSmtpPort = checkRequiredEntry(EMAILSMTPPORT, rdElement.getAttribute(EMAILSMTPPORT));
		emailToAddr = checkRequiredEntry(EMAILTOADDR, rdElement.getAttribute(EMAILTOADDR));
		emailFromAddr = checkRequiredEntry(EMAILFROMADDR, rdElement.getAttribute(EMAILFROMADDR));
	}

//	public static void main(String[] args) {
//		DestinationFactory.buildDestination(config, destType)
//	}
	private void sendEmailNotification (StringBuffer subject, StringBuffer msgBody, String icmFileName,
			String icmServerDir, String localFileAbsolutePath, String exceptionMsg) {
		String[] emailToList = emailToAddr.split(";");
		if (subject==null){
			if(localFileAbsolutePath == null)
				localFileAbsolutePath = "NA for delete";
			subject = new StringBuffer(); msgBody = new StringBuffer();
			subject.append("ICM software delivery or delete failed with exception for file ").append(icmFileName);
			msgBody.append(subject).append("\n")
					.append("\nException details :: ").append(exceptionMsg)
					.append("\nLocal File Path :: ")
					.append(localFileAbsolutePath)
					.append("\nServer File Path :: ").append(icmServerDir);
		}
		try {
			SendMail.setupSendMail(emailSmtpHost, emailSmtpPort, emailFromAddr, emailToList);
			SendMail.sendMessage(subject.toString(), msgBody.toString());
		} catch (AddressException e) {
			LoaderLog.error("Bad email address: " + e.getClass() + " - " + e.getMessage());
		} catch (MessagingException e) {
			LoaderLog.error("Failed to deliver email: " + e.getClass() + " - " + e.getMessage());
		}
	}
	
	public void executeUpdate(String remoteFile, InputStream is,
			String icmFileName, String icmServerDir,
			String localFileAbsolutePath) {
//		executeSecureFileUploadRequest(icmFileName, icmServerDir, localFileAbsolutePath);
		LoaderLog.info("inside execute update method in ICMWS");
		executeSecureFileUploadAsStream(icmFileName, icmServerDir, localFileAbsolutePath);
	}
	
	/* 
     * Perform HTTP POST upload request against secure upload service  
     */
	@SuppressWarnings("deprecation")
	private void executeSecureFileUploadRequest(String icmFileName,
			String icmServerDir, String localFileAbsolutePath) {
		// Debug
//		System.out.println("localFileAbsolutePath : " + localFileAbsolutePath);
		long startTime = 0, statusCode = 0;
		String reasonPhrase = "";
		/*if (!icmServerDir.startsWith("/")) {
			icmServerDir = "/" + icmServerDir;
		}
		icmServerDir = icmPrefix + icmServerDir;*/
        LoaderLog.info("icmServerDir:"+icmServerDir);
		// (1) - Create a Http Client 
		CloseableHttpClient httpClient = HttpClients.createDefault(); 
        
        // (2) - Build the request url
		StringBuilder uri = new StringBuilder(); 
        uri.append(icmBaseHttpsUrl);
        uri.append(uploadRequest);
        uri.append("?server=").append(ftpServer);
        uri.append("&directory=").append(icmServerDir);
        uri.append("&fileName=").append(icmFileName);
        uri.append("&userId=").append(icmUsername);
        try {
			uri.append("&ruleId=").append(URLEncoder.encode(icmRuleId, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			uri.append("&ruleId=").append(icmRuleId);
			LoaderLog.error("URL encoding for ruleId String FAILED for icmRuleId : "
					+ icmRuleId + " : " + e.getClass() + " - "
					+ e.getMessage());
		}
        
        // (3) - Create a Http POST
        HttpPost httpPost = new HttpPost(uri.toString());
        
        // (4) - Add username/password authentication to POST header   
		httpPost.addHeader(BasicScheme.authenticate(
				new UsernamePasswordCredentials(icmUsername, icmPassword),
				"UTF-8", false));
        
        try {
        	// (5) - Build a multipart entity and add the file to it 
            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create(); 
            multipartEntity.addPart(icmFileName, new FileBody(new File(localFileAbsolutePath)));
            
            // (6) - Set the POST (multipart) entity 
            httpPost.setEntity(multipartEntity.build());
            
            
            // (7) - Execute the POST request 
            LoaderLog.info(" \n***** Execute HTTP Post Request for UPLOAD***** :: " + uri.toString());
            startTime = System.currentTimeMillis();
            
//            HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(3, false);
//            httpPost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
            HttpResponse response = httpClient.execute(httpPost);
            
            // (8) - Sending failure email and logging the response code and reason
            statusCode = response.getStatusLine().getStatusCode();
            reasonPhrase = response.getStatusLine().getReasonPhrase();
            if (statusCode != HttpStatus.SC_OK) {
            	StringBuffer subject = new StringBuffer(), body = new StringBuffer();
            	subject.append("ICM software delivery failed for file " + icmFileName + ": " + reasonPhrase);
            	LoaderLog.error(subject.toString());
				body.append(subject).append("\n").append("\nLocal File Path :: ").append(localFileAbsolutePath)
						.append("\nServer File Path :: ").append(icmServerDir)
						.append("\nResponse Code :: ")
						.append(statusCode).append("\nResponse Reason :: ")
						.append(reasonPhrase).append("\nUrl :: ").append(uri);
            	sendEmailNotification(subject, body, null, null, null, null);
            }
            
            LoaderLog.info("\nResponse Code :: " + statusCode);
            LoaderLog.info("\nResponse Reason :: " + response.getStatusLine().getReasonPhrase());
            LoaderLog.info("\n***** Upload request processing complete *****: " + icmFileName + "  <"
    				+ (System.currentTimeMillis() - startTime) + ">");
        } catch(HttpException he) { 
			LoaderLog.error("Fatal protocol violation; FAILED ICM Upload of :"
					+ icmFileName + " : " + he.getClass() + " - "
					+ he.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, localFileAbsolutePath, he.getClass() + " - "
					+ he.getMessage());
        } catch (IOException ie) {
			LoaderLog.error("Fatal transport error; FAILED ICM Upload of :"
					+ icmFileName + " : " + ie.getClass() + " - "
					+ ie.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, localFileAbsolutePath, ie.getClass() + " - "
					+ ie.getMessage());
        } finally {
        	// Release the connection
            httpPost.releaseConnection(); 
        }
	}
	/* 
     * Perform HTTP Conn upload request against secure upload service  
     */
	@SuppressWarnings("resource")
	private void executeSecureFileUploadAsStream (String icmFileName,
			String icmServerDir, String localFileAbsolutePath) {
		long startTime = 0, statusCode = 0;
		String reasonPhrase = "";
		/*if (!icmServerDir.startsWith("/")) {
			icmServerDir = "/" + icmServerDir;
		}
		icmServerDir = icmPrefix + icmServerDir;*/
		LoaderLog.info("icmServerDir:"+icmServerDir);
		
        // (1) - Build the request url
		StringBuilder uri = new StringBuilder(); 
        uri.append(icmBaseHttpsUrl);
        uri.append(uploadRequest);
        uri.append("?server=").append(ftpServer);
        uri.append("&directory=").append(icmServerDir);
        uri.append("&fileName=").append(icmFileName);
        uri.append("&userId=").append(icmUsername);
        try {
			uri.append("&ruleId=").append(URLEncoder.encode(icmRuleId, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			uri.append("&ruleId=").append(icmRuleId);
			LoaderLog.error("URL encoding for ruleId String FAILED for icmRuleId : "
					+ icmRuleId + " : " + e.getClass() + " - "
					+ e.getMessage());
		}
        // (2) - Encode username/password for authentication in POST header 
     	String encodedUserPassword = Base64.encodeBase64String((icmUsername + ":" + icmPassword).getBytes());
     	
        try {
        	// (3) - Open a url connection
            URL url = new URL(uri.toString());
            LoaderLog.info("before httpurlconnection");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            
        	// (4) - Url connection settings  
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Authorization", "Basic " + encodedUserPassword);
            httpConn.setRequestProperty("Content-Type", "UTF-8 ");
            
            // (5) - Open output stream of the HTTP connection for writing data
            OutputStream outputStream = httpConn.getOutputStream();
        
	        // (6) - Open input stream of the file to upload
	        FileInputStream inputStream = new FileInputStream(localFileAbsolutePath);
        
	        // (7) - Write data to the outputstream
	        LoaderLog.info(" \n***** Execute HTTP Conn Request for UPLOAD***** :: " + uri.toString());
	        startTime = System.currentTimeMillis();
	        byte[] buffer = new byte[BUFFER_SIZE];
	        int bytesRead = -1;
	         
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	              outputStream.write(buffer, 0, bytesRead);
	        }
	        outputStream.close();
	        
	        // (8) - Sending failure email and logging the response code and reason
	        statusCode = httpConn.getResponseCode();
	        reasonPhrase = httpConn.getResponseMessage();
	        if (statusCode != HttpStatus.SC_OK) {
	        	StringBuffer subject = new StringBuffer(), body = new StringBuffer();
	        	subject.append("ICM software delivery failed for file " + icmFileName + ": " + reasonPhrase);
	        	LoaderLog.error(subject.toString());
				body.append(subject).append("\n").append("\nLocal File Path :: ").append(localFileAbsolutePath)
						.append("\nServer File Path :: ").append(icmServerDir)
						.append("\nResponse Code :: ")
						.append(statusCode).append("\nResponse Reason :: ")
						.append(reasonPhrase).append("\nUrl :: ").append(uri);
	        	sendEmailNotification(subject, body, null, null, null, null);
	        }
	        
	        LoaderLog.info("\nResponse Code :: " + statusCode);
	        LoaderLog.info("\nResponse Reason :: " + reasonPhrase);
	        LoaderLog.info("\n***** Stream Upload request processing complete *****: " + icmFileName + "  <"
					+ (System.currentTimeMillis() - startTime) + ">");
            httpConn.disconnect();
        } catch(HttpException he) { 
			LoaderLog.error("Fatal protocol violation; FAILED ICM Upload of :"
					+ icmFileName + " : " + he.getClass() + " - "
					+ he.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, localFileAbsolutePath, he.getClass() + " - "
					+ he.getMessage());
        } catch (IOException ie) {
			LoaderLog.error("Fatal transport error; FAILED ICM Upload of :"
					+ icmFileName + " : " + ie.getClass() + " - "
					+ ie.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, localFileAbsolutePath, ie.getClass() + " - "
					+ ie.getMessage());
        }
	}
	
	public void executeDelete(String remoteFile, String icmFileName,
			String icmServerDir) {
		executeSecureFileDeleteRequest (icmFileName, icmServerDir);
	}
	
	/* 
     * Perform HTTP POST delete request against secure upload service for delete 
     */
	@SuppressWarnings("deprecation")
	private void executeSecureFileDeleteRequest(String icmFileName,
			String icmServerDir) {
		long startTime = 0, statusCode = 0;
		String reasonPhrase = "";
		/* if (!icmServerDir.startsWith("/")) {
			icmServerDir = "/" + icmServerDir;
		}
		icmServerDir = icmPrefix + icmServerDir; */
		// (1) - Create a Http Client 
		CloseableHttpClient httpClient = HttpClients.createDefault(); 
        
        // (2) - Build the request url
		StringBuilder uri = new StringBuilder(); 
        uri.append(icmBaseHttpsUrl);
        uri.append(deleteRequest);
        uri.append("?server=").append(ftpServer);
        uri.append("&directory=").append(icmServerDir);
        uri.append("&fileName=").append(icmFileName);
        
        // (3) - Create a Http POST 
        HttpPost httpPost = new HttpPost(uri.toString());
        
        // (4) - Add username/password authentication to POST header   
		httpPost.addHeader(BasicScheme.authenticate(
				new UsernamePasswordCredentials(icmUsername, icmPassword),
				"UTF-8", false));
        
        try {           
            // (5) - Execute the DELETE request 
            LoaderLog.info(" \n***** Execute HTTP Post Request for DELETE***** :: " + uri.toString());
            startTime = System.currentTimeMillis();
            HttpResponse response = httpClient.execute(httpPost);
            
            // (8) - Sending failure email and logging the response code and reason
            statusCode = response.getStatusLine().getStatusCode();
            reasonPhrase = response.getStatusLine().getReasonPhrase();
            if (statusCode != HttpStatus.SC_OK) {
            	StringBuffer subject = new StringBuffer(), body = new StringBuffer();
            	subject.append("ICM software delete failed for file " + icmFileName + ": " + reasonPhrase);
            	LoaderLog.error(subject.toString());
				body.append(subject).append("\n").append("\nServer File Path :: ").append(icmServerDir)
						.append("\nResponse Code :: ")
						.append(statusCode).append("\nResponse Reason :: ")
						.append(reasonPhrase).append("\nUrl :: ").append(uri);
            	sendEmailNotification(subject, body, null, null, null, null);
            }
            
            LoaderLog.info("\nResponse Code :: " + response.getStatusLine().getStatusCode());
            LoaderLog.info("\nResponse Reason :: " + response.getStatusLine().getReasonPhrase());
            
        } catch(HttpException he) { 
			LoaderLog.error("Fatal protocol violation; FAILED ICM delete of :"
					+ icmFileName + " : " + he.getClass() + " - "
					+ he.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, null, he.getClass() + " - "
					+ he.getMessage());
        } catch (IOException ie) {
			LoaderLog.error("Fatal transport error; FAILED ICM delete of :"
					+ icmFileName + " : " + ie.getClass() + " - "
					+ ie.getMessage());
			sendEmailNotification(null, null, icmFileName, icmServerDir, null, ie.getClass() + " - "
					+ ie.getMessage());
        } finally {
    	// Release the connection
          	httpPost.releaseConnection(); 
        }
		LoaderLog.info("\n***** Delete request processing complete *****: " + icmFileName + "  <"
				+ (System.currentTimeMillis() - startTime) + ">");
	}
	
	public static String checkRequiredEntry(String field, String x) {
		if (x == null || x.equals("")) {
			throw new IllegalArgumentException(" config file field: <" +field + "> must have a value");
		}
		return x;
	}

	@Override
	public boolean exists(String remoteFile) throws FileSystemException {
		return false;
	}
	
	@Override
	public String getSoarIcmIntegrationOn() {
		return soarIcmIntegrationOn;
	}

	@Override
	public void setIcmRule(String icmRuleId) {
		this.icmRuleId = icmRuleId;
	}

	@Override
	public String getServerOid() {
		return this.icmServerOid;
	}
}
