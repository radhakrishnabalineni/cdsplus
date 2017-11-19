package com.hp.soar.priorityLoader.workItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.utils.LoaderLog;

public class ICMDestination  {
	private static final String ICMDESTINATION = "icmDestination";
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
	private static final String uploadRequest = "/upload";
	private static final String deleteRequest = "/delete";
	
	private String icmBaseHttpsUrl;
	private boolean isICMOn;
	private boolean isBoth;
	private String ftpServer;
	private String icmUsername;
	private String icmPassword;
	
	// ID of the server to send software to (ICM = )
	private String icmServerOid;
	private String icmPrefix;
	
	
	private static final int BUFFER_SIZE = 1024*8;
	private static final int TWENTYMEG = 1024*1024*20;

	public ICMDestination (ConfigurationReader config) throws FileSystemException {
		Element rdElement = config.getElement(ICMDESTINATION);
		if (rdElement == null) {
			throw new IllegalArgumentException(ICMDESTINATION+" not specified in config file.");
		}

		String className = rdElement.getAttribute(ICMCLASSNAME);

		if (className == null || !className.equals(ICMDestination.class.getName())) {
			throw new IllegalArgumentException(className +" is not specified in config file or is not supported");
		}
		icmBaseHttpsUrl = checkRequiredEntry(BASEHTTPSURL, rdElement.getAttribute(BASEHTTPSURL));
		String isOn = checkRequiredEntry(SOARICMINTEGRATION, rdElement.getAttribute(SOARICMINTEGRATION)); 
		isICMOn = isOn != null && !isOn.equalsIgnoreCase("n"); 
		isBoth = isOn != null && isOn.equalsIgnoreCase("b"); 
		ftpServer = checkRequiredEntry(SERVERNAME, rdElement.getAttribute(SERVERNAME));
		icmUsername = checkRequiredEntry(USERNAME, rdElement.getAttribute(USERNAME));
		icmPassword = checkRequiredEntry(USERPASSWORD, rdElement.getAttribute(USERPASSWORD));
		icmServerOid = checkRequiredEntry(SERVEROID, rdElement.getAttribute(SERVEROID));
		icmPrefix = checkRequiredEntry(PREFIX, rdElement.getAttribute(PREFIX));
		String emailSmtpHost = checkRequiredEntry(EMAILSMTPHOST, rdElement.getAttribute(EMAILSMTPHOST));
		String emailSmtpPort = checkRequiredEntry(EMAILSMTPPORT, rdElement.getAttribute(EMAILSMTPPORT));
		String emailToAddr = checkRequiredEntry(EMAILTOADDR, rdElement.getAttribute(EMAILTOADDR));
		String emailFromAddr = checkRequiredEntry(EMAILFROMADDR, rdElement.getAttribute(EMAILFROMADDR));

		ICMAdapter.initEmailSettings(emailSmtpHost, emailSmtpPort, emailToAddr, emailFromAddr);
	}

	public static void main(String[] args) {
		String fileName = args[0];
		ConfigurationReader configReader;
		try {
			configReader = new ConfigurationReader("soarConfig.xml");
			// create an ICMDestination instance
			ICMDestination destination = new ICMDestination(configReader);
			String icmFileName = "bl8x0ci2_fw_bndl_lin.tar.gz";
			String icmServerDir = "pub/softlib2/software1/supportpack-linux/p688055204/v100171";
			String icmRule="SWD_Ruleset";
			
			// remove the file first
			destination.remove(icmFileName, icmServerDir);
			
			FileInputStream inStream = new FileInputStream(fileName);
			
			// now add it back
			destination.put(icmFileName, icmServerDir, inStream, icmRule);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ICMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	

	
	/* 
     * Perform HTTP Conn upload request against secure upload service  
     */
	/**
	 * put sends a file to the ICM server
	 * @param icmFileName
	 * @param icmServerDir
	 * @param localFileAbsolutePath
	 * @param icmRule TODO
	 * @throws ICMException 
	 * @throws IOException 

	public void put (String icmFileName, String icmServerDir, InputStream inStream, String icmRule) throws ICMException, IOException {
		long startTime = 0, statusCode = 0;
		String reasonPhrase = "";
		
		HttpPost postMethod = new HttpPost()
		HttpClient httpClient = new HttpClient();
		
//		if (!icmServerDir.startsWith("/")) {
//			icmServerDir = "/" + icmServerDir;
//		}
//		icmServerDir = icmPrefix + icmServerDir;
		LoaderLog.debug("icmServerDir:"+icmServerDir);
		
        // (1) - Build the request url
		StringBuilder uri = new StringBuilder(); 
        uri.append(icmBaseHttpsUrl);
        uri.append(uploadRequest);
        uri.append("?server=").append(ftpServer);
        uri.append("&directory=").append(icmServerDir);
        uri.append("&fileName=").append(icmFileName);
        uri.append("&userId=").append(icmUsername);
        try {
			uri.append("&ruleId=").append(URLEncoder.encode(icmRule, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			uri.append("&ruleId=").append(icmRule);
			LoaderLog.error("URL encoding for ruleId String FAILED for icmRuleId : "
					+ icmRule + " : " + e.getClass() + " - "
					+ e.getMessage());
		}
        // (2) - Encode username/password for authentication in POST header 
     	String encodedUserPassword = Base64.encodeBase64String((icmUsername + ":" + icmPassword).getBytes());
     	
     	// (3) - Open a url connection
     	URL url = new URL(uri.toString());
     	LoaderLog.debug("before httpurlconnection");
     	HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

     	// (4) - Url connection settings  
     	httpConn.setDoOutput(true);
     	httpConn.setRequestMethod("POST");
     	httpConn.setRequestProperty("Authorization", "Basic " + encodedUserPassword);
     	httpConn.setRequestProperty("Content-Type", "UTF-8 ");

     	// (5) - Open output stream of the HTTP connection for writing data
     	OutputStream outputStream = httpConn.getOutputStream();

     	// (7) - Write data to the outputstream
     	LoaderLog.debug("ICMDestination put: " + uri.toString());
     	startTime = System.currentTimeMillis();
     	byte[] buffer = new byte[BUFFER_SIZE];
     	int bytesRead = -1;
     	long totalBytes = 0;

     	while ((bytesRead = inStream.read(buffer)) != -1) {
     		totalBytes += bytesRead;
     		outputStream.write(buffer, 0, bytesRead);
     		if (totalBytes % TWENTYMEG == 0) {
     	     	LoaderLog.debug("ICMDestination put: " + totalBytes);
     		}
     	}
     	outputStream.close();

     	// (8) - Sending failure email and logging the response code and reason
     	statusCode = httpConn.getResponseCode();
     	reasonPhrase = httpConn.getResponseMessage();
     	
     	LoaderLog.debug("ICMDestination Response Code :: " + statusCode);
     	LoaderLog.debug("ICMDestination Response Reason :: " + reasonPhrase);

     	httpConn.disconnect();
     	if (statusCode != HttpStatus.SC_OK) {
     		throw new ICMException(statusCode, reasonPhrase);
     	}


	}
	*/
	
	/**
	 * put a file to the ICM server
	 * @param icmFileName
	 * @param icmServerDir
	 * @param inStream
	 * @param icmRule
	 * @throws ICMException
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public void put (String icmFileName, String icmServerDir, InputStream inStream, String icmRule) throws ICMException, IOException {

        // (1) - Create a Http Client
		CloseableHttpClient  httpClient = HttpClients.createDefault();
		
        // (2) - Build the request url
//        StringBuilder urlStr = new StringBuilder();
//        urlStr.append(icmBaseHttpsUrl + uploadRequestMapping);
//        urlStr.append("?server=" + serverName);
//        urlStr.append("&directory=" + directoryName);
//        urlStr.append("&fileName=" + fileName);
//        urlStr.append("&userId=" + userName);
//        urlStr.append("&ruleId=" + ruleSet);
       try { 
    	   StringBuilder urlStr = new StringBuilder(); 
    	   urlStr.append(icmBaseHttpsUrl);
    	   urlStr.append(uploadRequest);
    	   urlStr.append("?server=").append(ftpServer);
    	   urlStr.append("&directory=").append(icmServerDir);
    	   urlStr.append("&fileName=").append(icmFileName);
    	   urlStr.append("&userId=").append(icmUsername);
    	   try {
    		   urlStr.append("&ruleId=").append(URLEncoder.encode(icmRule, "UTF-8"));
    	   } catch (UnsupportedEncodingException e) {
    		   urlStr.append("&ruleId=").append(icmRule);
    		   LoaderLog.error("URL encoding for ruleId String FAILED for icmRuleId : "
    				   + icmRule + " : " + e.getClass() + " - "
    				   + e.getMessage());
    	   }
    	   // (2) - Encode username/password for authentication in POST header 
    	   //     	String encodedUserPassword = Base64.encodeBase64String((icmUsername + ":" + icmPassword).getBytes());

    	   LoaderLog.debug(" \n***** Execute HTTP Request *****");
    	   LoaderLog.debug("HTTP POST                   = " + urlStr.toString());

    	   // (3) - Create a Http POST
    	   HttpPost httpPost = new HttpPost(urlStr.toString());   

    	   // (4) - Add username/password authentication to POST header
    	   httpPost.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(icmUsername, icmPassword), "UTF-8", false));

    	   long beginTime = System.currentTimeMillis();
    	   StreamEntity entity = new StreamEntity(inStream);

    	   // (5) - Execute the POST request
    	   httpPost.setEntity(entity);
    	   HttpResponse actualResponse = httpClient.execute(httpPost);

    	   // (6) - Output the response code
    	   int responseCode = actualResponse.getStatusLine().getStatusCode();
    	   LoaderLog.debug("Response Code                     = " + responseCode);
    	   LoaderLog.debug("Duration (ms)                    = " + (System.currentTimeMillis() - beginTime));
    	   LoaderLog.debug("***** Upload complete *****");
       } finally {
    	   httpClient.close();
       }
	}

	
	/* 
     * Perform HTTP POST delete request against secure upload service for delete 
     */
	@SuppressWarnings("deprecation")
	public void remove(String icmFileName,
			String icmServerDir) throws ICMException, ClientProtocolException, IOException {
		long statusCode = 0;
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

        try {
        	// (4) - Add username/password authentication to POST header   
        	httpPost.addHeader(BasicScheme.authenticate(
        			new UsernamePasswordCredentials(icmUsername, icmPassword),
        			"UTF-8", false));
        	
        	// (5) - Execute the DELETE request 
        	long startTime = System.currentTimeMillis();
        	LoaderLog.debug("ICMDestination Delete : "+uri.toString());
        	HttpResponse response = httpClient.execute(httpPost);
        	LoaderLog.debug("ICMDestination Delete finished <"+(System.currentTimeMillis() - startTime)+">");
        	
        	// (8) - Sending failure email and logging the response code and reason
        	statusCode = response.getStatusLine().getStatusCode();
        	reasonPhrase = response.getStatusLine().getReasonPhrase();
        	if (statusCode != HttpStatus.SC_OK) {
        		throw new ICMException(statusCode, reasonPhrase);
        	}
            
        } finally {
    	// Release the connection
          	httpPost.releaseConnection(); 
        }
	}
	
	public boolean isICMOn() {
		return isICMOn;
	}

	public boolean isBoth() {
		return isBoth;
	}

	public static String checkRequiredEntry(String field, String x) {
		if (x == null || x.equals("")) {
			throw new IllegalArgumentException(" config file field: <" +field + "> must have a value");
		}
		return x;
	}

	private class StreamEntity extends AbstractHttpEntity {
		InputStream inStream;
		
        public StreamEntity(InputStream inStream) {
			super();
			this.inStream = inStream;
		}

		public boolean isRepeatable() {
            return false;
        }

        public long getContentLength() {
            return -1;
        }

        public boolean isStreaming() {
            return false;
        }

        public InputStream getContent() throws IOException {
            // Should be implemented as well but is irrelevant for this case
            throw new UnsupportedOperationException();
        }

        public void writeTo(final OutputStream outputStream) throws IOException {
             try {
           	  LoaderLog.debug("Start writing data...");
           	  byte[] buffer = new byte[BUFFER_SIZE];
           	  int bytesRead = -1;
           	  
           	  long i = 0;
           	  while ((bytesRead = inStream.read(buffer)) != -1) {
           		  outputStream.write(buffer, 0, bytesRead);
           		  i += bytesRead;
           		  if((i % TWENTYMEG) == 0) {
           			  LoaderLog.debug(" ***** Sent = " + i);
           		  }
           	  }
           	  LoaderLog.debug(" ***** Fini = "+i);
             } finally {
            	 inStream.close();
             }
        }
    };     

}
