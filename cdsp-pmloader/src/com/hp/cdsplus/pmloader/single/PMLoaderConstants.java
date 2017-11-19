package com.hp.cdsplus.pmloader.single;

import org.xml.sax.helpers.AttributesImpl;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public interface PMLoaderConstants {
	
	public static final String VENDOR_PARSER_CLASS = "org.apache.xerces.parsers.SAXParser";
	public static final String DATA_NAME_ATTR = "name";
	public static final String PROJ_CONTENT_URL_PATH = "/content/";
	public static final String SERVER = "pm_server";
	public static final String PROJECT_SERVER = "project_server";
	public static final String TYPE = "type";
	public static final String CACHE_TYPE = "cache_type";
	public static final String OID = "oid";
	public static final String DELTA_INCREMENT = "delta_increment";
	public static final String OVERWRITE = "overwrite";
	public static final String PM_DATA = "pm_data";
	public static final String FILE_NAME_REGEX = "[\\._]";
	public static final String OLDEST_ONE_NAME = "top";
	public static final String OLDEST_ONE_DESC = "old_one";
	public static final int DELTA_INCREMENT_DEFAULT_VALUE = 24;
	public static final int DIGEST_OUTPUT_RATE = 2000;
	public static final boolean OVERWRITE_FLAG_DEFAULT_VALUE = false;

	public static final int STARTDATEIDX = 0;
	public static final int ENDDATEIDX = 1;


	// PRODUCT SKU QUERY STRING CONSTANTS 
	public static final String SKU_ALL="SKU_ALL";
	public static final String SKU_DELTA="SKU_DELTA";
	public static final String SKU_HIERARCHY="SKU_HIERARCHY";
	public static final String SKU_CONTENT="SKU_CONTENT";
	public static final String SKU_PARENTS="SKU_PARENTS";
	public static final String SKU_CHILDREN="SKU_CHILDREN";
	
	// PRODUCT MODEL QUERY STRING CONSTANTS 
	public static final String PROD_MODEL_ALL="PROD_MODEL_ALL";
	public static final String PROD_MODEL_DELTA="PROD_MODEL_DELTA";
	public static final String PROD_MODEL_HIERARCHY="PROD_MODEL_HIERARCHY";
	public static final String PROD_MODEL_CONTENT="PROD_MODEL_CONTENT";
	public static final String PROD_MODEL_PARENTS="PROD_MODEL_PARENTS";
	public static final String PROD_MODEL_CHILDREN="PROD_MODEL_CHILDREN";
	
	// PRODUCT SERIES QUERY STRING CONSTANTS 
	public static final String PROD_SERIES_ALL="PROD_SERIES_ALL";
	public static final String PROD_SERIES_DELTA="PROD_SERIES_DELTA";
	public static final String PROD_SERIES_HIERARCHY="PROD_SERIES_HIERARCHY";
	public static final String PROD_SERIES_CONTENT="PROD_SERIES_CONTENT";
	public static final String PROD_SERIES_PARENTS="PROD_SERIES_PARENTS";
	public static final String PROD_SERIES_CHILDREN="PROD_SERIES_CHILDREN";
	
	// PRODUCT BIG SERIES QUERY STRING CONSTANTS 
	public static final String BIG_SERIES_ALL="BIG_SERIES_ALL";
	public static final String BIG_SERIES_DELTA="BIG_SERIES_DELTA";
	public static final String BIG_SERIES_HIERARCHY="BIG_SERIES_HIERARCHY";
	public static final String BIG_SERIES_CONTENT="BIG_SERIES_CONTENT";
	public static final String BIG_SERIES_PARENTS="BIG_SERIES_PARENTS";
	public static final String BIG_SERIES_CHILDREN="BIG_SERIES_CHILDREN";
	
	// PRODUCT SUPPORT SUB-CATEGORY QUERY STRING CONSTANTS 
	public static final String SUP_SUB_CAT_ALL="SUP_SUB_CAT_ALL";
	public static final String SUP_SUB_CAT_DELTA="SUP_SUB_CAT_DELTA";
	public static final String SUP_SUB_CAT_HIERARCHY="SUP_SUB_CAT_HIERARCHY";
	public static final String SUP_SUB_CAT_CONTENT="SUP_SUB_CAT_CONTENT";
	public static final String SUP_SUB_CAT_PARENTS="SUP_SUB_PARENTS";
	public static final String SUP_SUB_CAT_CHILDREN="SUP_SUB_CHILDREN";
	
	// PRODUCT MARKETING SUB-CATEGORY QUERY STRING CONSTANTS 
	public static final String MARKETING_SUB_CAT_ALL ="MARKETING_SUB_CAT_ALL";
	public static final String MARKETING_SUB_CAT_DELTA="MARKETING_SUB_CAT_DELTA";
	public static final String MARKETING_SUB_CAT_HIERARCHY="MARKETING_SUB_CAT_HIERARCHY";
	public static final String MARKETING_SUB_CAT_CONTENT="MARKETING_SUB_CAT_CONTENT";
	public static final String MARKETING_SUB_CAT_PARENTS="MARKETING_SUB_PARENTS";
	public static final String MARKETING_SUB_CAT_CHILDREN="MARKETING_SUB_CHILDREN";
	
	// PRODUCT SUPPORT CATEGORY QUERY STRING CONSTANTS 
	public static final String SUPPORT_CAT_ALL="SUPPORT_CAT_ALL";
	public static final String SUPPORT_CAT_DELTA="SUPPORT_CAT_DELTA";
	public static final String SUPPORT_CAT_HIERARCHY="SUPPORT_CAT_HIERARCHY";
	public static final String SUPPORT_CAT_CONTENT="SUPPORT_CAT_CONTENT";
	public static final String SUPPORT_CAT_PARENTS="SUPPORT_CAT_PARENTS";
	public static final String SUPPORT_CAT_CHILDREN="SUPPORT_CAT_CHILDREN";
	
	// PRODUCT MARKETING CATEGORY QUERY STRING CONSTANTS 
	public static final String MARKETING_CAT_ALL="MARKETING_CAT_ALL";
	public static final String MARKETING_CAT_DELTA="MARKETING_CAT_DELTA";
	public static final String MARKETING_CAT_HIERARCHY="MARKETING_CAT_HIERARCHY";
	public static final String MARKETING_CAT_CONTENT="MARKETING_CAT_CONTENT";
	public static final String MARKETING_CAT_PARENTS="MARKETING_CAT_PARENTS";
	public static final String MARKETING_CAT_CHILDREN="MARKETING_CAT_CHILDREN";
	
	// PRODUCT TYPE QUERY STRING CONSTANTS 
	public static final String PRODUCT_TYPE_ALL="PRODUCT_TYPE_ALL";
	public static final String PRODUCT_TYPE_DELTA="PRODUCT_TYPE_DELTA";
	public static final String PRODUCT_TYPE_HIERARCHY="PRODUCT_TYPE_HIERARCHY";
	public static final String PRODUCT_TYPE_CONTENT="PRODUCT_TYPE_CONTENT";
	public static final String PRODUCT_TYPE_PARENTS="PRODUCT_TYPE_PARENTS";
	public static final String PRODUCT_TYPE_CHILDREN="PRODUCT_TYPE_CHILDREN";
	
	
	public static final String DELTATYPE = "Deltatype";
	public static final String FILE_NAME_MATCH = ".";
	public static final String APPEND_XML = ".xml";

	public static final String DB_LINK = "dblink";
	public static final String PMNODE = "pmnode";
	public static final String PMVIEW = "pmview";
	public static final String PRODUCT_NAME = "product_name";
	public static final String PRODUCT_NUMBER = "product_number";

	public static final String PMNODE_PROJ_NAME = "proj";
	public static final String PMNODE_PRODUCT_LINE_NAME = "product_line";
	public static final String PMNODE_PROJ_NAMESPACE = "http://www.hp.com/cdsplus/";
	public static final String PMNODE_PROJ_ID_NAME = "id";
	public static final String PMNODE_ROOT_NAME = "node";
	public static final String PMNODE_NAME_NAME = "name";
	public static final String PMNODE_LONG_NAME_NAME = "long_name";
	public static final String PMNODE_LANG_NAME = "language";
	public static final String PMNODE_COUNTRY_NAME = "country";
	public static final String PMNODE_LEVEL_NAME = "heirarchy_level";
	public static final String PMNODE_TREE_TYPE_NAME = "node_type";
	public static final String PMNODE_PARENTS_NAME = "parents";
	public static final String PMNODE_PARENT_NAME = "parent";
	public static final String PMNODE_CHILDREN_NAME = "children";
	public static final String PMNODE_CHILD_NAME = "child";
	public static final String PMNODE_VIEW_NAME = "view";
	public static final String PMNODE_ATTRIBUTE_TYPE = "CDATA";
	public static final String PMNODE_CHILD_QNAME = new StringBuffer(
			PMNODE_PROJ_NAME).append(":").append(PMNODE_CHILD_NAME).toString();
	public static final String PMNODE_PARENT_QNAME = new StringBuffer(
			PMNODE_PROJ_NAME).append(":").append(PMNODE_PARENT_NAME).toString();
	public static final String PMNODE_VIEW_QNAME = new StringBuffer(
			PMNODE_PROJ_NAME).append(":").append(PMNODE_VIEW_NAME).toString();

	public static final AttributesImpl PMNODE_BLANK_ATTR = new AttributesImpl();

	public static final String PMVIEW_ROOT_NAME = "view";
	public static final String PMVIEW_ROOT_ATTR_NAME = "oid";
	public static final String PMVIEW_ATTRIBUTE_TYPE = "CDATA";
	public static final String PMVIEW_PRODUCT_LINES_NAME = "product_lines";
	public static final String PMVIEW_SUPPORT_NAME_OID_NAME = "support_name_oid";
	public static final String PMVIEW_SUPPORT_NAME_OIDS_NAME = "support_name_oids";
	public static final String PMVIEW_PRODUCT_NUMBERS_NAME = "product_numbers";
	public static final String PMVIEW_SOURCE_NAME = "source";
	public static final String PMVIEW_SOURCE_QNAME = new StringBuffer(
			PMLoaderConstants.PMNODE_PROJ_NAME).append(":")
			.append(PMVIEW_SOURCE_NAME).toString();

	public static final String TYPE_URL_STRING = "productmaster/content/";
	public static final String CACHE_TYPE_URL_STRING = "productmastercache/content/";
	public static final String PMATER = null;
	public static final String EMPTY_STRING = "";
	public static final String LINE_SEPARATOR = "line.separator";
	public final String FILE_SEPARATOR = "file.separator";
	
	// concentra db sync constants
	public static final String UPDATES = "_UPDATE";
	public static final String DELETES = "_DELETE";
	public static final String NEW = "_NEW";
	
	// mongo db collection names
	public static final String HIERARCHY_COLLECTION = "hierarchy";
	public static final String HIERARCHY_HISTORY_COLLECTION = "hierarchy_delta";
	
	public static final String CONTENT_COLLECTION="content";
	public static final String CONTENT_HISTORY_COLLECTION = "content_delta";
	
	public static final String EXTRACTION_LOGS_COLLECTION="pm_extraction_logs";
	public static final String  TEMP_COLLECTION = PMasterLoader.company_info.toLowerCase()+"tempCollectionName";

}
