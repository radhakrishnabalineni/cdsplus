package com.hp.cdspDestination;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.ParameterParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.Log;

public abstract class HttpMethod {
  

  protected final static String EMPTY_STRING = "";

  public final String GET_METHOD_TEXT = "GET";
  public final String PUT_METHOD_TEXT = "PUT";
  public final String DELETE_METHOD_TEXT = "DELETE";
  public final String POST_METHOD_TEXT = "POST";

  protected final String URL_PREFIX = "http://";
  protected final String SSL_URL_PREFIX = "https://";
  protected final String CONTENT_LENGTH_HEADER = "Content-Length";
  protected final String CONTENT_TYPE_HEADER = "Content-Type";

  protected int lastResponseCode;
  protected Map<?, ?> lastHeaders;
  protected String baseUrl;
  
  public HttpMethod( final String baseUrl ) {
    this.baseUrl = baseUrl;
    if ( !this.baseUrl.equals( EMPTY_STRING )
        && !this.baseUrl.startsWith( URL_PREFIX ) 
        && !this.baseUrl.startsWith( SSL_URL_PREFIX ) ) {
      this.baseUrl = URL_PREFIX + this.baseUrl;
    }
  }
  
  public abstract byte[] resolve( final String path ) throws IOException, ProcessingException;

  public abstract byte[] resolve( final String path, final String mimeType,
      byte[] payload ) throws IOException, ProcessingException;

  final public Map<?, ?> getLastHeaders() {
    return lastHeaders;
  }

  final public int getLastResponseCode() {
    return lastResponseCode;
  }

  final public String getLastMimeType() {
    if ( lastHeaders == null ) return null;
    List<?> values = (List<?>)lastHeaders.get( CONTENT_TYPE_HEADER );
    if ( values == null || values.size() == 0 ) return null;
    return (String)values.get( 0 );
  }

  final public boolean isError() {
    final int error_code = 400;

    return lastResponseCode >= error_code;
  }

  final protected byte[] getResponse( HttpURLConnection hUrlCon ) throws IOException, ProcessingException {
    BufferedInputStream bis = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      bis = new BufferedInputStream( this.getStreamResponse( hUrlCon ) );
      int r = -1;
      while ( (r = bis.read()) != -1 )
        baos.write( r );
    } catch ( IOException e ) {
      handleError( hUrlCon );
    } finally {
      try {
        if ( bis != null ) bis.close();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    return baos.toByteArray();
  }
  
  /***
   * Calls an HTTP put request, passing as the request body a file content
   * 
   * @param completeUrl URL for the HTTP request
   * @param fileName Name of the file to be passed as the request content
   * @param mimeType Mime Type for the request
   * @return Byte array with the request response
   * @throws IOException
   * @throws ProcessingException 
   */
  private byte[] putInputStream(String completeUrl, String fileName, String mimeType, String updateType, Integer priority) throws IOException, ProcessingException {

	  HttpClient requestClient = new HttpClient();
	  File contentFile = new File(fileName);
	  FileInputStream fileStream = new FileInputStream(contentFile);

	  PutMethod	methodPut = new PutMethod(completeUrl);

	  if ( mimeType != null && !mimeType.equals("") && fileStream != null ) {

		  methodPut.setRequestHeader(CONTENT_TYPE_HEADER, mimeType);
		  methodPut.setRequestHeader(CONTENT_LENGTH_HEADER, Long.toString(contentFile.length()));
		  methodPut.setRequestEntity(new FileRequestEntity(contentFile, mimeType));
	  } else {
		  methodPut.setRequestHeader(CONTENT_LENGTH_HEADER, "0");
	  }
	  if (priority != null) {
			methodPut.setRequestHeader("priority", String.valueOf(priority));
		}
		if (updateType != null && !updateType.equals("")) {
			methodPut.setRequestHeader("updateType", updateType);
		}
	  int rc = requestClient.executeMethod(methodPut);
	  checkResponse(rc);
	  return methodPut.getResponseBody();
  }
  
  /***
   * Calls an HTTP put request, passing as its content a byte array
   * 
   * @param completeUrl URL for the HTTP request
   * @param payload
   * @param mimeType Mime Type for the request
   * @return Byte array with the request response
   * @throws IOException
   * @throws ProcessingException  
   */
  protected byte[] putEntity(String completeUrl, byte[] payload, String mimeType, String updateType, Integer priority) throws IOException, ProcessingException {
	  
	  HttpClient requestClient = new HttpClient();
	  PutMethod	methodPut = new PutMethod(completeUrl);

	  if ( mimeType != null && !mimeType.equals("") && payload != null ){
		  methodPut.setRequestHeader(CONTENT_TYPE_HEADER, mimeType);
		  methodPut.setRequestHeader(CONTENT_LENGTH_HEADER, Integer.toString(payload.length));
		  methodPut.setRequestEntity(new ByteArrayRequestEntity(payload));
	  }else{
		  methodPut.setRequestHeader(CONTENT_LENGTH_HEADER, "0");
	  }
	  
	  if (priority!= null) {

			methodPut.setRequestHeader("priority", String.valueOf(priority));
		}
		if (updateType != null && !updateType.equals("")) {
			methodPut.setRequestHeader("updateType", updateType);
		}
		
	  int rc = requestClient.executeMethod(methodPut);
	  checkResponse(rc);
	  return  methodPut.getResponseBody();
  }
  
  /***
   * Executes an HTTP get request
   * 
   * @param completeUrl Url to get
   * @param mimeType Mime Type for the request
   * @return byte[] with the server response
   * @throws IOException
   * @throws ProcessingException  
   */
  private byte[] getFromUrl(String completeUrl, String mimeType) throws IOException, ProcessingException {
  	HttpClient requestClient = new HttpClient();
  	GetMethod getMethod = new GetMethod(completeUrl);
  	if ( mimeType != null && !mimeType.equals("")){
  		getMethod.setRequestHeader(CONTENT_TYPE_HEADER,mimeType);
  	}
  	getMethod.setRequestHeader(CONTENT_LENGTH_HEADER,"0");
  	int rc = requestClient.executeMethod(getMethod);
	checkResponse(rc);
  	return getMethod.getResponseBody();
  }
  
  /***
   * Executes an HTTP delete request 
   * 
   * @param completeUrl URL to be deleted
   * @return Byte array with the request response
   * @throws IOException
   * @throws ProcessingException  
   */
  private byte[] deleteUrl(String completeUrl, String updateType, Integer priority ) throws IOException, ProcessingException {
	  HttpClient requestClient = new HttpClient();
	  DeleteMethod methodDel = new DeleteMethod(completeUrl);
	  if (priority!= null) {
		  methodDel.setRequestHeader("priority", String.valueOf(priority));
		}
		if (updateType != null && !updateType.equals("")) {
			methodDel.setRequestHeader("updateType", updateType);
		}
	  int rc = requestClient.executeMethod(methodDel);
	  checkResponse(rc);
	  return methodDel.getResponseBody();
  }
  
  /***
   * Executes a HTTP post request 
   * @param completeUrl Url where the post will be made
   * @param payload Byte array with the set of pair values to be posted
   * @return Byte array with the post request response
   * @throws IOException
 * @throws ProcessingException 
   */
  private byte[] postEntity(String completeUrl, byte[] payload, String updateType, Integer priority ) throws IOException, ProcessingException {
  	List<?> parsedValues = new ParameterParser().parse(new String(payload), '=');
  	NameValuePair[] a = (NameValuePair[])parsedValues.toArray(new NameValuePair[parsedValues.size()]);
  	
  	HttpClient requestClient = new HttpClient();
  	PostMethod postMethod = new PostMethod(completeUrl);
  	postMethod.setRequestHeader(CONTENT_LENGTH_HEADER, Integer.toString(payload.length));
  	postMethod.setRequestBody(a);
  	
  	if (priority!= null) {
  		postMethod.setRequestHeader("priority", String.valueOf(priority));
	}
	if (updateType != null && !updateType.equals("")) {
		postMethod.setRequestHeader("updateType", updateType);
	}
	
  	int rc = requestClient.executeMethod(postMethod);
	checkResponse(rc);
  	return postMethod.getResponseBody();
  }
  
  final protected byte[] request( final String path, final String mimeType, 
  		String fileName, final String method, String updateType, Integer priority) throws IOException, ProcessingException {
  	String completeUrl = baseUrl + path;
  	Log.debug("baseUrl in HTTP MEthod-->"+baseUrl);
  	Log.debug("path in HTTP MEthod-->"+path);
  	Log.debug("mimeType in HTTP MEthod-->"+mimeType);
  	Log.debug("fileName in HTTP MEthod-->"+fileName);
  	Log.debug("method in HTTP MEthod-->"+method);
  	Log.debug("updateType in HTTP MEthod-->"+updateType);
  	Log.debug("priority in HTTP MEthod-->"+priority);
  	Log.debug("completeUrl in HTTP MEthod-->"+completeUrl);
  		
	  
	    if (method.equals( PUT_METHOD_TEXT )){ 
	    	return putInputStream(completeUrl, fileName, mimeType, updateType, priority);
	    }else if(	method.equals( GET_METHOD_TEXT )){
	    	return getFromUrl(completeUrl, mimeType);
	    }else if(method.equals( DELETE_METHOD_TEXT )){
	    	return deleteUrl(completeUrl, updateType, priority);
	    }else if(method.equals( POST_METHOD_TEXT )) {
	    	return postEntity(completeUrl, null, updateType, priority);
	    }
	    else{
	      throw new HttpException( "HttpMethod.request: the http method passed '"
	          + method + "' is not supported or invalid" );
	    }
  }

  final protected byte[] request(final String path, final String mimeType, 
  		byte[] payload, final String method , String updateType, Integer priority)throws IOException, ProcessingException {
    String completeUrl = baseUrl + path;
    
    Log.debug("baseUrl in HTTP MEthod payload-->"+baseUrl);
    Log.debug("path in HTTP MEthod payload-->"+path);
    Log.debug("mimeType in HTTP MEthod payload-->"+mimeType);
    Log.debug("method in HTTP MEthod payload-->"+method);
    Log.debug("updateType in HTTP MEthod payload-->"+updateType);
    Log.debug("priority in HTTP MEthod payload-->"+priority);
    Log.debug("completeUrl in HTTP MEthod payload-->"+completeUrl);
	 	String xmlString= new String(payload);
	  	  //Log.debug("^^^^^^ from httpmethod...Conversion of byte array to xmlString-->"+xmlString);
    
  	if (method.equals( PUT_METHOD_TEXT )){ 
  		return putEntity(completeUrl, payload, mimeType, updateType, priority );
  	}else if(	method.equals( GET_METHOD_TEXT )){
  		return getFromUrl(completeUrl,mimeType);
  	}else if(method.equals( DELETE_METHOD_TEXT )){
  		return deleteUrl(completeUrl,updateType, priority);
  	}else if(method.equals( POST_METHOD_TEXT )) {
  		return postEntity(completeUrl, payload, updateType, priority);
  	}
  	else{
  		throw new HttpException( "HttpMethod.request: the http method passed '"
  				+ method + "' is not supported or invalid" );
  	}
	    
  }

  final private InputStream getStreamResponse( HttpURLConnection hUrlCon )
      throws IOException {

    lastHeaders = hUrlCon.getHeaderFields();
    lastResponseCode = hUrlCon.getResponseCode();
    if ( this.isError() ) {
    	// let the handleError to configure the response message
    	handleError( hUrlCon );
    }
    return hUrlCon.getInputStream();
  }

  final private void handleError( HttpURLConnection hUrlCon ) throws IOException {
    BufferedInputStream bes = null;
    //String errorS = "";
    try {
    	bes = new BufferedInputStream( hUrlCon.getErrorStream() );
    	if ( bes != null ) {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		int r = -1;
    		while ( (r = bes.read()) > 0 ) {
    			baos.write( r );
    		}
    		//errorS = new String( baos.toByteArray() );
    	}
    } catch ( IOException e ) {
    	//errorS = new StringBuffer( "The error stream failed to be read [" ).append(
    	// e.getMessage() ).append( "]" ).toString();
    } finally {
    	try {
    		if ( bes != null ) bes.close();
    	} catch ( IOException e ) {
    		/*
        		errorS = new StringBuffer(
            	"The error stream failed to be read and couldn't be closed! [" ).append(
            	e.getMessage() ).append( "]" ).toString();
    		 */
    		e.printStackTrace();
    	}
    }
    StringBuffer sb = new StringBuffer( "Failed: " ).append(
    		hUrlCon.getURL().toString());
    /*
    	sb.append( " with code of '" );
    	sb.append( hUrlCon.getResponseCode() ).append( "' with the message [" ).append(
        errorS ).append( "]" );
     */
    throw new IOException( sb.toString() );
  } 
  
  /**
   * We want textual information with each response code. An enum let's us do that 
   * with out a big switch statement. 
   */
  enum HttpResponseCode {

	  /**
	   * This is not used right now, but is here for a reference
	   */
	  HTTP_OK_200_RESPONSE(HttpURLConnection.HTTP_OK) {
		  public String toString() {
			  return "OK - Unspecified";		  
		  }
	  },
	  
	  /**
	   * This is not used right now, but is here for a reference
	   */
	  HTTP_OK_202_RESPONSE(HttpURLConnection.HTTP_ACCEPTED) {
		  public String toString() {
			  return "OK - Unspecified";		  
		  }
	  },
	  /**
	   * HTTP_PARTIAL, 206,  is the maximum value in the 200 range.
	   */
	  HTTP_MAX_OK_RESPONSE(HttpURLConnection.HTTP_PARTIAL) {
		  public String toString() {
			  return "OK - Partial content";		  
		  }
	  },

	  /*
	   * Client side failures 
	   */
	  
	  //400
	  HTTP_BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST) {
		  public String toString() {
			  return "Client failure - Unspecified bad request";		  
		  }
	  },
	  
	  
	  //401
	  HTTP_UNAUTHORIZED(HttpURLConnection.HTTP_UNAUTHORIZED) {
		  public String toString() {
			  return "Client failure - Not authorized";		  
		  }
	  },
	  
	  //402 is payment required
	  
	  //403
	  HTTP_FORBIDDEN(HttpURLConnection.HTTP_FORBIDDEN) {
		  public String toString() {
			  return "Client failure - Forbidden";		  
		  }
	  },
	  
	  //404
	  HTTP_NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND) {
		  public String toString() {
			  return "Client failure - Not Found";		  
		  }
	  },
	  
	  //405
	  HTTP_BAD_METHOD(HttpURLConnection.HTTP_BAD_METHOD) {
		  public String toString() {
			  return "Client failure - Method not allowed";		  
		  }
	  },
	  
	  //406
	  HTTP_NOT_ACCEPTABLE(HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
		  public String toString() {
			  return "Client failure - Not Acceptable";		  
		  }
	  },
	  
	  //407
	  HTTP_PROXY_AUTH(HttpURLConnection.HTTP_PROXY_AUTH) {
		  public String toString() {
			  return "Client failure - Must authenticate through a proxy";		  
		  }
	  },
	  
	  //408
	  HTTP_CLIENT_TIMEOUT(HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
		  public String toString() {
			  return "Client failure - timeout";		  
		  }
	  },
	   
	  //409
	  HTTP_CONFLICT(HttpURLConnection.HTTP_CONFLICT) {
		  public String toString() {
			  return "Client failure - Conflict, rule violation";		  
		  }
	  },
	  
	  //413
	  HTTP_ENTITY_TOO_LARGE(HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
		  public String toString() {
			  return "Client failure - Entity too large";		  
		  }
	  },
	  
	  //415
	  HTTP_UNSUPPORTED_TYPE(HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
		  public String toString() {
			  return "Client failure - Unsupported mime type";		  
		  }
	  },
	  
	  /*
	   * Server side failures
	   */
	  
	  /**
	   * Anything equal to or larger than this value indicates a problem at the server
	   */ 
	  //500
	  HTTP_SERVER_ERROR_RESPONSE(HttpURLConnection.HTTP_INTERNAL_ERROR) {
		  public String toString() {
			  return "Server failure - Internal Server Error";		  
		  }
	  },
	  
	  //501
	  HTTP_NOT_IMPLEMENTED(HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
		  public String toString() {
			  return "Server failure - Method not implemented";		  
		  }
	  },
	  
	  //502
	  HTTP_BAD_GATEWAY(HttpURLConnection.HTTP_BAD_GATEWAY) {
		  public String toString() {
			  return "Server failure - Invalid response from upstream gateway";		  
		  }
	  },
	  
	  //503
	  HTTP_UNAVAILABLE(HttpURLConnection.HTTP_UNAVAILABLE) {
		  public String toString() {
			  return "Server failure - Temporarily unavailable";		  
		  }
	  },
	  
	  //504
	  HTTP_GATEWAY_TIMEOUT(HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
		  public String toString() {
			  return "Server failure - No timely response from upstream gateway";		  
		  }
	  },
	  
	  //505
	  HTTP_SERVER_FAIL3(HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
		  public String toString() {
			  return "Server failure - HTTP protocol version speciefied by the client is unsupported";		  
		  }
	  },

	  ;

	  private int responseCode;
	  
	  HttpResponseCode(int rc) {
		  responseCode = rc;
	  }
	  
	  public int getResponseCode() {
		   return responseCode;
	  }
	  
	  public static int getMaxOkResponse() {
		  return HTTP_MAX_OK_RESPONSE.responseCode;
	  }
	  
	  public static int getServerFailureResponse() {
		  return HTTP_SERVER_ERROR_RESPONSE.responseCode;
	  }

	  public static String getMessage(int rc) {
		 HttpResponseCode[] httpRespCodes = values();
		 for (HttpResponseCode httpRespCode: httpRespCodes ) {
			 if (httpRespCode.responseCode == rc) {
				 return httpRespCode.toString();
			 }
		 }
		return "No message for response code: "+rc;
	  } 
  }
  
  /**
   * Check the range of the response code and throw the correct ProcessingException 
   * if the response code is not OK.
   * <p>
   * Any response less than 206 (java.net.HttpURLConnection.HTTP_PARTIAL), including the most common, 200,
   * (java.net.HttpURLConnection.HTTP_OK) passes through this method without effect.
   * </p>
   * <p>
   * Any response greater than 206 but less than 500 indicates an error on the client side so our application 
   * has a problem and we should NOT retry.
   * </p>
   * <p>
   * Any response equal to or greater than 500 (java.net.HttpURLConnection.HTTP_INTERNAL_ERROR) indicates an error on 
   * the server side and we should retry it.
   * </p>
   *	
   * @param responseCode the integer value representing the response code to be checked.
   * @throws ProcessingException if the code is not OK.
   */
  private void checkResponse(int responseCode) throws ProcessingException {

	
	  
	  //Is the number greater than or equal to 500?
	  if (responseCode>=HttpResponseCode.getServerFailureResponse()) {
		  //server failures mean retry 
		  throw new ProcessingException(("post failed with response code: "+ responseCode + " msg: " + HttpResponseCode.getMessage(responseCode)), true);

	  }
	  //How about 206?
	  if (responseCode>HttpResponseCode.getMaxOkResponse()){
		  //client failures mean do not retry
		  throw new ProcessingException(("post failed with response code: "+ responseCode + " msg: " + HttpResponseCode.getMessage(responseCode)), false);

	  } 
  }
  
  // #################################################
  // Command Line common methods
  // #################################################

  final protected static void outputFile( final String file, byte[] content )
      throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(
        file ) );
    try {
      for ( int i = 0; i < content.length; i++ )
        bos.write( content[ i ] );
    } finally {
      bos.close();
    }
  }

  final protected static byte[] inputFile( String file ) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferedInputStream bis = new BufferedInputStream( new FileInputStream(
        file ) );
    int i = -1;
    try {
      while ( (i = bis.read()) != -1 )
        baos.write( i );
    } finally {
      bis.close();
    }
    return baos.toByteArray();
  }

  public static void main( String[] args ) throws IOException,
      FactoryConfigurationError, ParserConfigurationException, SAXException, ProcessingException {
    final String usage_message = "java HttpMethod <xml file> <base url>";
    final String url_attr = "url";
    final String output_file_attr = "output_file";
    final String mime_type_attr = "mime_type";
    final String input_file_attr = "input_file";

    if ( args.length != 2 ) {
      System.out.println( "Incorrect number of parameters: " + args.length );
      System.out.println( usage_message );
      System.exit( 1 );
    }
    File xmlFile = new File( args[ 0 ] );
    if ( !xmlFile.isFile() ) {
      System.out.println( "The file passed does not exist "
          + xmlFile.getAbsolutePath() );
      System.out.println( usage_message );
      System.exit( 1 );
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware( true );
    dbf.setValidating( false );
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document reflectDoc = db.parse( xmlFile );
    Element rootEle = reflectDoc.getDocumentElement();
    String baseUrl = args[ 1 ];
    NodeList nl = rootEle.getChildNodes();
    for ( int i = 0; i < nl.getLength(); i++ ) {
      if ( nl.item( i ) instanceof Element ) {
        Element e = (Element)nl.item( i );
        String[] toPass = null;
        // <put mime_type="text/xsl"
        // input_file="../../src/xsl/views/apdz_support_205.xsl"
        // url="support/stylesheet/apdz_support_205"/>
        if ( e.getNodeName().equals( "put" ) ) {
          // two options for put; url OR url, mime_type, and input_file.
          String url = e.getAttribute( url_attr );
          String file = e.getAttribute( input_file_attr );
          String mimeType = e.getAttribute( mime_type_attr );
          if ( !mimeType.equals( "" ) && !file.equals( "" ) ) {
            toPass = new String[ 3 ];
            toPass[ 1 ] = file;
            toPass[ 2 ] = mimeType;
          } else if ( !mimeType.equals( "" ) || !file.equals( "" ) ) {
            throw new IllegalArgumentException(
                "The 'put' requires either both a mime_type and input_file or neither of them" );
          }
          if ( url.equals( "" ) ) throw new IllegalArgumentException(
              "The 'put' did not contain the required 'url' attribute" );
          if ( toPass == null ) toPass = new String[ 1 ];
          toPass[ 0 ] = baseUrl + url;
          Put.main( toPass );
          System.out.println( "Put success: " + toPass[ 0 ] );
          // <delete url="support/stylesheet/apdz_support_205"/>
        } else if ( e.getNodeName().equals( "delete" ) ) {
          String url = e.getAttribute( url_attr );
          if ( url.equals( "" ) ) throw new IllegalArgumentException(
              "The 'delete' did not contain the required 'url' attribute" );
          toPass = new String[ 1 ];
          toPass[ 0 ] = baseUrl + url;
          Delete.main( toPass );
          System.out.println( "Delete success: " + toPass[ 0 ] );
          // <get url="support/stylesheet/apdz_support_205"/>
        } else if ( e.getNodeName().equals( "get" ) ) {
          String url = e.getAttribute( url_attr );
          String file = e.getAttribute( output_file_attr );
          if ( url.equals( "" ) ) throw new IllegalArgumentException(
              "The 'get' did not contain the required 'url' attribute" );
          if ( !file.equals( "" ) ) {
            toPass = new String[ 2 ];
            toPass[ 1 ] = file;
          } else {
            toPass = new String[ 1 ];
          }
          toPass[ 0 ] = baseUrl + url;
          Get.main( toPass );
          System.out.println( "Get success: " + toPass[ 0 ] );
        }
      }
    }
  }
}
