package com.hp.cdsplus.wds.constants;

public interface IConstants 
{
	public static final String POST_SERVER_PATH 			= "/cadence/app/";
	public static final String SUBSCRIPTION_CONFIG_ELE_NAME = "sub";
	public static final String SUBSCRIPTION_ATTR_NAME 		= "subscription";	
	public static final String SELECTION_CONFIG_ELE_NAME 	= "selection";
	public static final String DEST_ELE_NAME				= "destination";
	public static final String NAMED_DEST_ELE_NAME			= "namedDestination";
	public static final String NAME_ATTRIBUTE				= "name";
	public static final String DEST_CLASS_ATTR_NAME 		= "class";
	
	public static final String SLASH						= "/";
	//3.1 Patch : includeDeletes Configurable start
	public static final String PARAM_ELEMENT				= "/*?";
	public static final String INCLUDE_DELETES				= "includeDeletes=true";
	public static final String AND_ELEMENT					= "&";
	public static final String REVERSE_LIST					= "reverse=true";
	//3.1 Patch : includeDeletes Configurable end
	
	public static final String AFTER						= "&after=";
	public static final String RESULT_LIMIT					= "&limit=";
	public static final String GETFILEINFO					= "?expand=versions";
	
	
	
	public static final String HREF_ATTRIBUTE				= "xlink:href";
	public static final String LAST_MODIFIED				= "lastModified";
	public static final String HAS_ATTACHMENTS				= "hasAttachments";
	public static final String PRIORITY						= "priority";
	public static final String EVENT_TYPE					= "eventType";
	
	public static final String ALL_ELEMENTS					= "*";
	public static final String ROOT_ELEMENT					= "xml:base";
	public static final String PROJ_NS_KEY					= "proj";
	public static final String SD_NS_KEY					= "sd";
	public static final String XLINK_NS_KEY					= "xlink";
	public static final String SUB_NS_KEY					= "sub";
	public static final String PROJ_NS_VALUE				= "xmlns:proj";
	public static final String SD_NS_VALUE					= "xmlns:sd";
	public static final String XLINK_NS_VALUE				= "xmlns:xlink";
	public static final String SUB_NS_VALUE					= "xmlns:sub";
	public static final String PROJ_REV_ELEMENT				= "proj:ref";

	public static final String WDS_CLIENT="wdsClient";
	public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String SEL_NS_KEY					= "sel";
	public static final String SEL_NS_VALUE					= "xmlns:sel";
	
	public static final String INCLUDE_DELETED_DOC = "includeDeletes";	
}

