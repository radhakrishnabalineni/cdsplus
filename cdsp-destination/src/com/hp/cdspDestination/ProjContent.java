package com.hp.cdspDestination;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.Log;

public class ProjContent {

	public static final String XML_CONTENT_EXTENTION = ".xml";
	public static final String XML_CONTENT_MIME = "text/xml";
	public static final String BINARY_XML_CONTENT_MIME = "application/rdf+xml";

	public static final String EXTENTION_SEPERATOR = ".";

	private static HashMap<String, String> EXTENTION_MIME_TYPES = new HashMap<String, String>();
	static {
		EXTENTION_MIME_TYPES.put( XML_CONTENT_EXTENTION, XML_CONTENT_MIME );
		EXTENTION_MIME_TYPES.put( ".ttf", "system/noidea" );
		EXTENTION_MIME_TYPES.put( ".wmv", "video/x-ms-wmv" );
		EXTENTION_MIME_TYPES.put( ".mpg", "video/mpeg" );
		EXTENTION_MIME_TYPES.put( ".mp3", "audio/mp3" );
		EXTENTION_MIME_TYPES.put( ".wav", "audio/wav" );
		EXTENTION_MIME_TYPES.put( ".m4a", "audio/x-m4a" );
		EXTENTION_MIME_TYPES.put( ".asf", "application/asx" );
		EXTENTION_MIME_TYPES.put( ".swf", "application/x-shockwave-flash" );
		EXTENTION_MIME_TYPES.put( ".mht", "message/rfc822" );
		EXTENTION_MIME_TYPES.put( ".avi", "video/x-msvideo" );
		EXTENTION_MIME_TYPES.put( ".htm", "text/html" );
		EXTENTION_MIME_TYPES.put( ".html", "text/html" );
		EXTENTION_MIME_TYPES.put( ".txt", "text/plain" );
		EXTENTION_MIME_TYPES.put( ".hqx", "application/binhex" );
		EXTENTION_MIME_TYPES.put( ".doc", "application/msword" );
		EXTENTION_MIME_TYPES.put( ".ppt", "application/ms-powerpoint" );
		EXTENTION_MIME_TYPES.put( ".bin", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".dll", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".exe", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".sea", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".chm", "application/octet-stream" ); //no idea
		EXTENTION_MIME_TYPES.put( ".fla", "application/octet-stream" ); //no idea
		EXTENTION_MIME_TYPES.put( ".iso", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".pdf", "application/pdf" );
		EXTENTION_MIME_TYPES.put( ".xls", "application/ms-excel" );
		EXTENTION_MIME_TYPES.put( ".sit", "application/stuffit" );
		EXTENTION_MIME_TYPES.put( ".vsd", "application/visio" );
		EXTENTION_MIME_TYPES.put( ".swf", "application/x-shockwave-flash" );
		EXTENTION_MIME_TYPES.put( ".zip", "application/zip" );
		EXTENTION_MIME_TYPES.put( ".jpg", "image/jpeg" );
		EXTENTION_MIME_TYPES.put( ".png", "image/x-png" );
		EXTENTION_MIME_TYPES.put( ".bmp", "image/x-ms-bmp" );
		EXTENTION_MIME_TYPES.put( ".gif", "image/gif" );
		EXTENTION_MIME_TYPES.put( ".tif", "image/tif" );
		EXTENTION_MIME_TYPES.put( ".dmg", "image/dmg" );
		EXTENTION_MIME_TYPES.put( ".csv", "application/ms-excel" );
		EXTENTION_MIME_TYPES.put( ".xpr", "application/is-xpr" );
		EXTENTION_MIME_TYPES.put( ".mpp", "application/vnd.ms-project" );
		EXTENTION_MIME_TYPES.put( ".wma", "audio/x-ms-wma" );
		EXTENTION_MIME_TYPES.put( ".mov", "video/quicktime" );
		EXTENTION_MIME_TYPES.put( ".gz", "application/gzip" );
		EXTENTION_MIME_TYPES.put( ".cab", "application/gzip" );
		EXTENTION_MIME_TYPES.put( ".flv", "video/x-flv" );
		EXTENTION_MIME_TYPES.put( ".msg", "application/vnd.ms-outlook");
		EXTENTION_MIME_TYPES.put( ".pot", "application/vnd.ms-powerpoint" );  
		EXTENTION_MIME_TYPES.put( ".rtf", "application/rtf");
		EXTENTION_MIME_TYPES.put( ".xml",  "text/xml");
		EXTENTION_MIME_TYPES.put( ".docx", "application/msword");
		EXTENTION_MIME_TYPES.put( ".pptm", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".pptx", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".docm", "application/vnd.ms-word");
		EXTENTION_MIME_TYPES.put( ".dotm", "application/vnd.ms-word");
		EXTENTION_MIME_TYPES.put( ".dotx", "application/vnd.ms-word");
		EXTENTION_MIME_TYPES.put( ".potm", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".potx", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".ppam", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".ppsm", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".ppsx", "application/vnd.ms-powerpoint");
		EXTENTION_MIME_TYPES.put( ".xlam", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".xlsb", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".xlsm", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".xlsx", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".xltm", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".xltx", "application/vnd.ms-excel");
		EXTENTION_MIME_TYPES.put( ".ai", "application/postscript" );
		EXTENTION_MIME_TYPES.put( ".eps", "application/postscript" );
		EXTENTION_MIME_TYPES.put( ".dlf", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".dxf", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".indt", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".indd", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".qxd", "application/x-quark-express" );
		EXTENTION_MIME_TYPES.put( ".qxp", "application/x-quark-express" );
		EXTENTION_MIME_TYPES.put( ".hpd", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".xps", "application/vnd.ms-xpsdocument" );
		EXTENTION_MIME_TYPES.put( ".xmp", "application/rdf+xml" );
		EXTENTION_MIME_TYPES.put( ".pub", "application/x-mspublisher" );
		EXTENTION_MIME_TYPES.put( ".ps", "application/postscript" );
		EXTENTION_MIME_TYPES.put( ".psd", "application/x-photoshop" );
		EXTENTION_MIME_TYPES.put( ".jmf", "application/octet-stream" );
		EXTENTION_MIME_TYPES.put( ".ppml", "application/vnd.podi-ppml+xml" );
		EXTENTION_MIME_TYPES.put( ".jdf", "application/vnd.cip4-jdf+xml" );
		EXTENTION_MIME_TYPES.put( ".inx", "application/octet-stream" );
		//BR685331 Request to add ePub file type in Concentra
	//added by kumar
    EXTENTION_MIME_TYPES.put( ".epub", "application/epub+zip" );
	}

	private String mimeType;
	private String contentType;
	private String contentName;
	private String contentPath;
	private String path;
	private byte[] content;
	private String extentionlessContentName;
	//SMO : 
	private String company;
	public ProjContent( String contentType, String contentName )
	throws ProcessingException {
		this( contentType, contentName, new byte[0] );
		this.contentPath = "./";
	}

	public ProjContent( String contentType, String contentName, byte[] content )
	throws ProcessingException {
		this(contentType, contentName, content, false);
	}
	
	//CDS+ 10.3 Release -- Added to support SoarLoader Binary XML uploads 
	public ProjContent( String contentType, String contentName, byte[] content, boolean isBinary ) throws ProcessingException {
		if ( contentType != null ) {
			this.contentType = contentType;
		} else {
			throw new ProcessingException( "The content type of '" + contentType +
			"' is not valid" );
		}
		this.content = content;
		path = this.contentType;
		this.contentName = contentName.toLowerCase();
		int i = this.contentName.lastIndexOf( ProjContent.EXTENTION_SEPERATOR );
		if ( i == -1 ) {
			throw new ProcessingException( "The content name of '" + contentName +
			"' has no extention" );
		}
		String extention = this.contentName.substring( i, this.contentName.length() );
		this.mimeType = (String)ProjContent.EXTENTION_MIME_TYPES.get( extention );

		if ( this.mimeType == null ) {
			throw new ProcessingException( "The content name of '" + this.contentName +
			"' has no valid extention" );
		}
		if (mimeType.equals( ProjContent.XML_CONTENT_MIME ))  {
			if (isBinary) {
				// this is being put in the binary location so reset the mimeType from text/xml to application/rdf+xml
				mimeType = BINARY_XML_CONTENT_MIME;
			} else {
				// strip the .xml from the filename
				this.contentName = this.contentName.substring( 0, i );
			}
		}
	}

	/**
	 * @param contentName the contentName to set
	 */
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	public void setContentPath(String path){
		this.contentPath = path;
	}

	public String getContentType() {
		return contentType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getContentName() {
		return contentName;
	}

	public String getExtentionlessContentName() {
		/* Nick: No idea why it is only XML content with an extention left on
    String s = contentName;
    if ( !mimeType.equals( XML_CONTENT_MIME ) ) {
      int i = contentName.indexOf( EXTENTION_SEPERATOR );
      s = contentName.substring( 0, i );
    }
    return s;
		 */
		if ( extentionlessContentName == null ) {
			int i = contentName.indexOf( EXTENTION_SEPERATOR );
			if ( i != -1 ) extentionlessContentName = contentName.substring( 0, i );
			else extentionlessContentName = contentName;
		}
		return extentionlessContentName;
	}

	public byte[] getContent() {
		return content;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String p ) {
		path = p;
	}

	public String put( Put put, String eventType, Integer priority ) throws IOException, ProcessingException {
		return put(put, null, eventType, priority );
	}


	/**
	 * directPut is used to bypass the retry mechanism
	 * @param put
	 * @param pMimeType
	 * @return
	 * @throws IOException
	 * @throws ProcessingException 
	 */
	public String put( Put put, String pMimeType, String eventType, Integer priority ) throws IOException, ProcessingException {
		if(pMimeType != null && !pMimeType.trim().equals("")){
			mimeType = pMimeType;  
		}
		Log.debug("mimeType in PC put-->"+mimeType);
		Log.debug("eventType in PC put-->"+eventType);
		Log.debug("priority in PC put-->"+priority);

		if ( mimeType == null || mimeType.equals( "" ) ||
				path == null || path == ( "" )) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for putting" );
		}
		if(content == null){
			StringBuffer filePath = new StringBuffer(contentPath);
			if (!contentPath.endsWith(File.separator)) {
				filePath.append(File.separator);
			}
			filePath.append(contentName);
			Log.debug("contentPath in PC put-->"+contentPath);
			Log.debug("contentName in PC put-->"+contentName);
			Log.debug("content in PC put-->"+content);
			Log.debug("path in PC put-->"+path);
			Log.debug("filePath.toString() in PC put-->"+filePath.toString());
			File tempFile = new File(filePath.toString());
			Log.debug("Size of the file being pushed to cdsplus legacy:"+tempFile.length());

			return new String( put.resolve( path, mimeType, filePath.toString(), eventType, priority));
		}else{
			Log.debug("contentPath in PC put else-->"+contentPath);
			Log.debug("contentName in PC put else-->"+contentName);
			Log.debug("content in PC put else-->"+content);
			Log.debug("content in PC put else length-->"+content.length );

			Log.debug("path in PC put else-->"+path);

			return new String( put.resolve( path, mimeType, content, eventType, priority ) );
		}
	}
	
	public String processUpdate(MongoDestination mongoDest, String updateType, Integer priority, boolean convertToJSON) throws MongoUtilsException {
		return processUpdate(mongoDest,null,updateType,priority,convertToJSON);
	}
/*
	 * check if the the failue is due to huge Meta size ??*/
	public boolean isGiantMetaUpdate() {
		Log.debug("reached to Process huge Meta Content of " + content.length);
		if(content.length/(1024*1024) > 16)
			return true;
		else
			return false;
	}
	public String processUpdate(MongoDestination mongoDest, String pMimeType, String updateType, Integer priority, boolean convertToJSON) throws MongoUtilsException {
		Log.debug("reached to ProjContent processUpdate." + System.currentTimeMillis());
		Log.debug("path in ProjContent:"+path);
		if(pMimeType != null && !pMimeType.trim().equals("")){
			mimeType = pMimeType;  
		}
		if ( mimeType == null || mimeType.equals( "" ) ||
				path == null || path == ( "" )) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for putting" );
		}
		if(content == null){
			Log.debug("inside content null ... so its a bin file ---"+this.getCompany());
			StringBuffer filePath = new StringBuffer(contentPath);
			if (!contentPath.endsWith(File.separator)) {
				filePath.append(File.separator);
			}
			filePath.append(contentName);
			//SMO : Adding company_info for attachments
			return new String( mongoDest.processUpdate( path, mimeType, filePath.toString(), updateType, priority, false, this.getCompany()));
		}else{
			String xmlString= new String(content);
			Log.debug("ProjContent Conversion of byte array to xmlString"+xmlString);
			//SMO : 
			return new String( mongoDest.processUpdate( path, mimeType, content, updateType, priority, convertToJSON,this.getCompany()) );
		}
	}
	
	public String processDelete(MongoDestination mongoDest, String updateType, Integer priority) throws MongoUtilsException {
		if ( path == null || path == ( "" ) ) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for deleteing" );
		}
		return new String( mongoDest.processDelete( path, updateType, priority ) );
	}
	
	public String processTouch(MongoDestination mongoDest, String updateType, Integer priority) throws MongoUtilsException {
		if ( mimeType == null || mimeType.equals( "" ) ||
				path == null || path == ( "" ) || content == null ) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for posting" );
		}
		return new String( mongoDest.processTouch( path, mimeType, content, updateType, priority  ) );
	}

	public String del( Delete del, String eventType, Integer priority  ) throws IOException, ProcessingException {
		if ( path == null || path == ( "" ) ) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for deleteing" );
		}
		return new String( del.resolve( path, eventType, priority ) );
	}

	//Execute the Post call, sending the specified content
	public String post(Post post, String eventType, Integer priority ) throws IOException, ProcessingException {
		if ( mimeType == null || mimeType.equals( "" ) ||
				path == null || path == ( "" ) || content == null ) {
			throw new IllegalArgumentException( "ProjContent: The content piece '" + contentName + "' has not been configured correctly for posting" );
		}
		return new String( post.resolve( path, mimeType, content, eventType, priority  ) );
	}

	public static String normalizeContentNameForUrl( String cName ) {
		final char universal_char = '_';
		//chars that are bad in a url line or xml attributes
		final char space = ' ';
		final char slash = '/';
		final char amp = '&';
		final char sngl_quot = '\'';
		final char quot = '\"';
		final char gt = '>';
		final char lt = '<';
		final char quest = '?';
		final char equal = '=';

		String toReturn = cName;
		if ( toReturn.indexOf( space ) != -1 )
			toReturn = toReturn.replace( space, universal_char );
		if ( toReturn.indexOf( slash ) != -1 )
			toReturn = toReturn.replace( slash, universal_char );
		if ( toReturn.indexOf( amp ) != -1 )
			toReturn = toReturn.replace( amp, universal_char );
		if ( toReturn.indexOf( sngl_quot ) != -1 )
			toReturn = toReturn.replace( sngl_quot, universal_char );
		if ( toReturn.indexOf( quot ) != -1 )
			toReturn = toReturn.replace( quot, universal_char );
		if ( toReturn.indexOf( gt ) != -1 )
			toReturn = toReturn.replace( gt, universal_char );
		if ( toReturn.indexOf( lt ) != -1 )
			toReturn = toReturn.replace( lt, universal_char );
		if ( toReturn.indexOf( quest ) != -1 )
			toReturn = toReturn.replace( quest, universal_char );
		if ( toReturn.indexOf( equal ) != -1 )
			toReturn = toReturn.replace( equal, universal_char );
		return toReturn.toLowerCase();
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}


