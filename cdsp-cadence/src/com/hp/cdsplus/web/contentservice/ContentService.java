package com.hp.cdsplus.web.contentservice;

import java.io.InputStream;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.web.exception.ApplicationException;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 *
 */
public interface ContentService {
	
	public static final String MSC_DOC_ROOT = "asset_rendition";
	
	public static final String CONC_DOC_ROOT = "document";
	public static final String SOAR_SOFTWARE_FEED_DOC = "soar-software-feed";
	public static final String SOAR_DOC_COLLECTION = "collection";
	public static final String SOAR_SOFTWARE_ITEMS_DOC = "software-items";
	public static final String SOAR_SOFTWARE_ITEM_DOC = "software-item";
	public static final String SOAR_PRODUCTS_SUPPORTED_DOC = "products-supported";
	
	public static final String SUB_DOC_PRODUCT_SERIES ="product_series";
	public static final String SUB_DOC_PRODUCT_BIGSERIES = "product_big_series";
	public static final String SUB_DOC_PRODUCT_NUMBER_NAME = "product_number_names";
	public static final String SUB_DOC_PRODUCT_LINE_CODE = "product_lines";
	public static final String SUB_DOC_PRODUCT_NAME ="product_names";
	public static final String SUB_DOC_SUPPORT_CATEGORY = "product_support_categories";
	public static final String SUB_DOC_SUPPORT_SUBCATEGORY="product_support_subcategories";
	public static final String SUB_DOC_MARKETING_CATEGORY="product_marketing_categories";
	
	public static final String SUB_DOC_MARKETING_SUBCATEGORY="product_marketing_subcategories";
	//Fix CR 250
	public static final String SUB_DOC_PRODUCT_TYPE="product_types";
	public static final String SUB_DOC_PRODUCT_NUMBER="product_numbers";
	public static final String SUB_DOC_SUPPORT_NAME="support_name_oids";

	// Soar pmaster expansions

	public static final String SOAR_SUB_DOC_PRODUCT_TYPES = "product_types";
	public static final String SOAR_SUB_DOC_PRODUCT_SUPPORT_SUBCATEGORIES = "product_support_subcategories";
	public static final String SOAR_SUB_DOC_PRODUCT_SERIES = "product_series";
	public static final String SOAR_SUB_DOC_PRODUCT_NAMES = "product_names";
	public static final String SOAR_SUB_DOC_PRODUCT_NUMBERS = "product_numbers";
	public static final String SOAR_SUB_DOC_PRODUCT_LINES = "product_lines";
	public static final String SOAR_SUB_DOC_PRODUCT_SUPPORT_CATEGORIES = "product_support_categories";
	public static final String SOAR_SUB_DOC_PRODUCT_BIG_SERIES = "product_big_series";
	public static final String SOAR_SUB_DOC_PRODUCT_MARKETING_CATEGORIES = "product_marketing_categories";
	public static final String SOAR_SUB_DOC_PRODUCT_MARKETING_SUBCATEGORIES = "product_marketing_subcategories";
	public static final String SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES = "product_number_names";
	public static final String SOAR_SUB_DOC_SUPPORT_NAME_OID="support_name_oids";
	
	public static final String SUB_DOC_FAQ_PRODUCT_SERIES ="faq_product_series";
	public static final String SUB_DOC_FAQ_PRODUCT_BIGSERIES = "faq_product_big_series";
	public static final String SUB_DOC_FAQ_PRODUCT_NUMBER_NAME = "faq_product_number_names";
	public static final String SUB_DOC_FAQ_PRODUCT_LINE_CODE = "faq_product_lines";
	public static final String SUB_DOC_FAQ_PRODUCT_NAME ="faq_product_names";
	public static final String SUB_DOC_FAQ_SUPPORT_CATEGORY = "faq_product_support_categories";
	public static final String SUB_DOC_FAQ_SUPPORT_SUBCATEGORY="faq_product_support_subcategories";
	public static final String SUB_DOC_FAQ_MARKETING_CATEGORY="faq_product_marketing_categories";
	public static final String SUB_DOC_FAQ_MARKETING_SUBCATEGORY="faq_product_marketing_subcategories";
	public static final String SUB_DOC_FAQ_PRODUCT_TYPE="faq_product_types";
	public static final String SUB_DOC_FAQ_PRODUCT_NUMBER="faq_product_numbers";
	public static final String SUB_DOC_FAQ_SUPPORT_NAME_OIDS="faq_support_name_oids";
	
	public static final String SUB_DOC_PRODUCTS="products";
	public static final String SOAR_SUB_DOC_PRODUCT="product";
	public static final String SUB_DOC_FAQ_PRODUCTS="faq_products";
	
	public static final String SOAR_SUB_DOC_PREFIX="soar-software-feed.collection.software-items.software-item.products-supported.";
	public static final String SUB_DOC_PREFIX="document.";
	
	public static final String DOT = ".";
	public static final String PRODUCT="product";
	
	//For SMO Changes
	public static final String COMPANY_INFO="company_info";
	public static final String COMPANY_INFO_IS_BLANK="";
	
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getContentList(Options options) throws WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getSubcriptionList(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getDocumentList(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return String
	 * @throws ApplicationException
	 */	
	public Object getExpandedDocumentList(Options options)throws ApplicationException;
	/**
	 * @param options
	 * @return String
	 * @throws ApplicationException
	 */	
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getDocumentAttachments(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return InputStream
	 * @throws ApplicationException
	 */
	public InputStream getDocumentAttachment(Options options)throws ApplicationException, WebApplicationException;
	
	/**
	 * @param options
	 * @return object
	 * @throws ApplicationException
	 */
	
	public Object getTaskSubcheck(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return object
	 * @throws ApplicationException
	 */
	public Object getExpandDetails(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return object
	 * @throws ApplicationException
	 */
	public Object getVersions(Options options)throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return
	 * @throws ApplicationException
	 */
	
	public Object getGenericExpandDetails(Options options) throws ApplicationException, WebApplicationException;

	/**
	 * @param options
	 * @return
	 */
	public Object putDocumentMetadata(Options options) throws ApplicationException, WebApplicationException;
	
	public Object putDocumentContent(Options options) throws ApplicationException, WebApplicationException;
	
	
}
