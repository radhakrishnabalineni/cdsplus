package com.hp.concentra.extractor.utils;

/**
 * Constants used in several classes around the extractor project
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class ExtractorConstants {
    // Attribute and property name for priority
    public static final String ELEMENT_PRIORITY = "priority";

    // Special priority values
    public static final int PRIORITY_ZERO = 0;
    public static final int NO_PRIORITY = -1;

    // Extractor events constants
    public static final String EVENT_UPDATE = "update";
    public static final String EVENT_DELETE = "delete";
    public static final String EVENT_TOUCH = "touch";
    public static final String EVENT_FAILED = "failed";

    public static final String ELEMENT_AUTHOR = "author";
    public static final String ELEMENT_AUTHORS = "authors";
    public static final String ELEMENT_CONTENT_UPDATE_DATE = "content_update_date";
    public static final String ELEMENT_CONTENT_UPDATE_USER = "content_update_user";
    public static final String ELEMENT_CONTENT_TYPE = "content_type";
    public static final String ELEMENT_DOCUMENT_TYPE = "document_type";
    public static final String ELEMENT_HAS_VALID_PRODUCTS = "has_valid_products";
    public static final String ELEMENT_LANGUAGE_CODE = "language_code";
    public static final String NAMESPACE_PREFIX = "ens";
    public static final String ELEMENT_NAMESPACE_VALUE = "http://concentra.boi.itc.hp.com/extractor";
    public static final String ELEMENT_OBJECT_NAME = "object_name";
    public static final String ELEMENT_PRODUCT = "product";
    public static final String PRODUCT = "product";
    public static final String JPC_ID = "jpc_id";
    public static final String ELEMENT_REGION = "region";
    public static final String ELEMENT_REGIONS = "regions";
    public static final String ELEMENT_VERSION_LABEL = "version_label";
    public static final String EMPTY_STRING = "";
    public static final String FALSE = "false";
    public static final String FUNCTION_GET_WORKORDER_FOR = "getWorkOrderFor";
    public static final String FUNCTION_GET_WORKORDER_SINCE = "getWorkOrderSince";
    public static final String FUNCTION_GET_DOCUMENT_RETRIEVAL = "getDocumentRetrieval";
    public static final String HHO_MARKETING_DOC = "c_hho_marketing_doc";
    public static final String INDENTATION_SIZE = "    ";
    public static final String MANUAL_DOC = "c_manual_doc";
    public static final String R_OBJECT_ID = "r_object_id";
    public static final String R_OBJECT_TYPE = "r_object_type";
    public static final String SUPPORT_DOC = "c_support_doc";
    public static final String TRUE = "true";
    public static final String UTF_8 = "UTF-8";
    public static final String SPACE = " ";
    public static final String DOT_EXTENSION = ".";
    public static final String RIGHT_SQUARE_BRACKET = "]";
    public static final String LEFT_SQUARE_BRACKET = "[";
    //SMO : US #7469 - inclusion of company flag
    public static final String ELEMENT_COMPANY = "company_info";
    public static final String COMPANY_NAME = "company_name";
}
