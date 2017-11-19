package com.hp.cdsplus.web.util;



public interface ServiceConstants {

	
	//Url Filter Varibles
	
	public final static String contextUrl = "base";

	public final static String contentType = "contentType";

	public static final String subscriptionType = "subscriptionType";
	public static final String docId = "docId";
	public static final String attachment = "attachments";
	public static final String versions = "versions";
	public static final String after = "after";
	public static final String limit = "limit";
	public static final String before = "before";
	public static final String includeDeletes = "includeDeletes";
	public static final String reverse = "reverse";
	public static final String wildcard ="wildcard";
	public static final String maxLevel ="maxLevel";
	public static final String expand_versions ="versions";
	public static final String expand_attachments ="attachments";
	public static final String expand ="expand";
	public static final String subCheck="subCheck";
	public static final String deleteSubs = "deleteSubs";
	
	
	
	
	
	//Error Messages
	
	public static final String errorMsg_NotFound = "Not Found";
	public static final String errorMsg_The_URI = "The URI";
	public static final String errorMsg_The_PARAM = "The PARAM";
	public static final String errorMsg_isInvalid = "is Invalid";
	public static final String errorMsg_The_Entry = "The Entry";
	public static final String errorMsg_doesnt_exist = "doesn't exist";
	
	//Namepace Url 
	
	public final static String base_uri ="/cadence/app/";
	public final static String projNameSpace="http://www.hp.com/cdsplus";
	public final static String xlinkNameSpace="http://www.w3.org/1999/xlink";
	public final static String xmlElementType="simple";
	
	
	//NameSpace Prefex
	public final static String basePrefex ="base";
	public final static String projPrefex="proj";
	public final static String xlinkPrefex="xlink";
	
	//DBObject Attributes
	
	public final static String id ="_id";
	public final static String documentId ="documentId";
	public final static String eventType ="eventType";
	public final static String priority ="priority";
	public final static String hasAttachments ="hasAttachments";
	public final static String lastModified ="lastModified";
	public final static String last_Modified ="last_modified";
	public final static String processStatus ="processStatus";
	public static final String subscriptions = "subscriptions";
	public static final String mime="mime";
	public static final String task="task";
	
	
	//CR 284
	public final static String METADATA_LIVE_COLLECTION ="metadata_live";
	public final static String PRODUCTMASTER_COLLECTION="productmaster";
	public final static String HIERARCHY_COLLECTION="hierarchyCollection";
	public final static String MONGO_DB="mongoDB";
	public final static String HIERARCHY_LEVEL="hierarchy_level";
	
	//for admin tool
	public final static String METADATA_CACHE_COLLECTION ="metadata_cache";
	
	//CR 605
	public static final String docid = "docid";
	public static final String stylesheetSub = "stylesheet";
	
	//For Groomer Subscriptions
	//public static final String GROOMER_REGEX = "regex";
	//public static final String GROOMER_REPLACEMENT = "replacement"
	//public static final String GROOMER_SEARCH_PATTERN = "searchpattern";
	//For General Subscriptions
	public static final String GENERAL_FILTER= "filter";
	public static final String GENERAL_HIERARCHY_EXPANSIONS = "hierarchyExpansions";
	public static final String GENERAL_SMART_FOLDER = "smartfolder";
	public static final String STYLESHEET_ERROR_MSG ="Stylesheet data is not visible for "; 

}
