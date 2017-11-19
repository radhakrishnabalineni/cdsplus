package com.hp.cdsplus.processor.adapters;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BasicBSONEncoder;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.utils.Constants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 */
public class HierarchyExpansionAdapter implements Adapter{
	private ContentDAO contentDao;

	private static final Logger logger = LogManager.getLogger(HierarchyExpansionAdapter.class);
	
	public static final String TEMP_COLLECTION_NAME = "metadataTempCollection";
	
	public static final String HIERARCHY_COLLECTION_NAME = "hierarchyCollection";
	//SMO : 7130
	public static final String HPI="HPI";
	
	public static final String PRODUCT_MASTER_DB_NAME = "productmaster";
	public static final String SOAR_DB_NAME = "soar";
	
	private static final String ID_FIELD = "_id";
	private static final String HIERARCHY_LEVEL="hierarchy_level";
	private static final String DOCUMENT_DOC = "document";
	private static final String PRODUCTS_DOC = "products";
	private static final String PRODUCT_DOC = "product";
	//SMO : 7130
	private static final String COMPANY_INFO = "company_info";
	// for soar
	private static final String SOAR_SOFTWARE_FEED_DOC = "soar-software-feed";
	private static final String SOAR_DOC_COLLECTION = "collection";
	private static final String SOAR_SOFTWARE_ITEMS_DOC = "software-items";
	private static final String SOAR_SOFTWARE_ITEM_DOC = "software-item";
	
	//These strings are mongo hierarchy collection id's
	private static final String PRODUCT_SERIES_OID_FIELD ="PRODUCT_SERIES_OID";
	private static final String PRODUCT_BIGSERIES_OID_FIELD = "PRODUCT_BIGSERIES_OID";
	private static final String PRODUCT_NUMBER_NAME_FIELD = "PRODUCT_NUMBER_NAME";
	private static final String PRODUCT_LINE_CODE_FIELD = "PRODUCT_LINE_CODE";
	private static final String PRODUCT_NAME_OID_FIELD ="PRODUCT_NAME_OID";
	private static final String SUPPORT_CATEGORY_OID_FIELD = "SUPPORT_CATEGORY_OID";
	private static final String SUPPORT_SUBCATEGORY_OID_FIELD="SUPPORT_SUBCATEGORY_OID";
	private static final String MARKETING_CATEGORY_OID_FIELD="MARKETING_CATEGORY_OID";
	private static final String MARKETING_SUBCATEGORY_OID_FIELD="MARKETING_SUBCATEGORY_OID";
	//Fix CR 250
	private static final String PRODUCT_TYPE_OID_FIELD = "PRODUCT_TYPE_OID";
	private static final String PRODUCT_NUMBER_OID_FIELD = "PRODUCT_NUMBER_OID";
	private static final String SUPPORT_NAME_OID_FIELD = "SUPPORT_NAME_OID";
	
	//These transformed to document pmaster expansions
	private static final String SUB_DOC_PRODUCTS="document.products";
	private static final String SUB_DOC_PRODUCT_SERIES ="document.product_series";
	private static final String SUB_DOC_PRODUCT_BIGSERIES = "document.product_big_series";
	private static final String SUB_DOC_PRODUCT_NUMBER_NAME = "document.product_number_names";
	private static final String SUB_DOC_PRODUCT_LINE_CODE = "document.product_lines";
	private static final String SUB_DOC_PRODUCT_NAME ="document.product_names";
	private static final String SUB_DOC_SUPPORT_CATEGORY = "document.product_support_categories";
	private static final String SUB_DOC_SUPPORT_SUBCATEGORY="document.product_support_subcategories";
	private static final String SUB_DOC_MARKETING_CATEGORY="document.product_marketing_categories";
	
	private static final String SUB_DOC_MARKETING_SUBCATEGORY="document.product_marketing_subcategories";
	//Fix CR 250
	private static final String SUB_DOC_PRODUCT_TYPE="document.product_types";
	private static final String SUB_DOC_PRODUCT_NUMBER="document.product_numbers";
	private static final String SUB_DOC_SUPPORT_NAME="document.support_name_oids";
	
	//These transformed to document faq_products pmaster expansions
	private static final String FAQ_PRODUCTS_DOC = "faq_products";
	public static final String SUPPORT_DB_NAME = "support";
	public static final String MANUAL_DB_NAME = "manual";
	//private static final String SUB_DOC_FAQ_PRODUCTS="document.faq_products";
	private static final String SUB_DOC_FAQ_PRODUCT_SERIES ="faq_product_series";
	private static final String SUB_DOC_FAQ_PRODUCT_BIGSERIES = "faq_product_big_series";
	private static final String SUB_DOC_FAQ_PRODUCT_NUMBER_NAME = "faq_product_number_names";
	private static final String SUB_DOC_FAQ_PRODUCT_LINE_CODE = "faq_product_lines";
	private static final String SUB_DOC_FAQ_PRODUCT_NAME ="faq_product_names";
	private static final String SUB_DOC_FAQ_SUPPORT_CATEGORY = "faq_product_support_categories";
	private static final String SUB_DOC_FAQ_SUPPORT_SUBCATEGORY="faq_product_support_subcategories";
	private static final String SUB_DOC_FAQ_MARKETING_CATEGORY="faq_product_marketing_categories";
	private static final String SUB_DOC_FAQ_MARKETING_SUBCATEGORY="faq_product_marketing_subcategories";
	private static final String SUB_DOC_FAQ_PRODUCT_TYPE="faq_product_types";
	private static final String SUB_DOC_FAQ_PRODUCT_NUMBER="faq_product_numbers";
	private static final String SUB_DOC_FAQ_SUPPORT_NAME_OIDS="faq_support_name_oids";
	// Soar pmaster expansions
	//private static final String SUB_DOC_SOAR_SUPPORT_SUBCATEGORY ="support_subcategory";
	//private static final String SUB_DOC_SOAR_PRODUCT_NAME = "product_name";
	//private static final String SUB_DOC_SOAR_PRODUCT_TYPE = "product_type";
	private static final String DOC_PRODUCT_TYPES = "product_types";
	private static final String DOC_PRODUCT_SUPPORT_SUBCATEGORIES = "product_support_subcategories";
	private static final String DOC_PRODUCT_SERIES = "product_series";
	private static final String DOC_PRODUCT_NAMES = "product_names";
	private static final String DOC_PRODUCT_NUMBERS = "product_numbers";
	private static final String DOC_PRODUCT_LINES = "product_lines";
	private static final String DOC_PRODUCT_SUPPORT_CATEGORIES = "product_support_categories";
	private static final String DOC_PRODUCT_BIG_SERIES = "product_big_series";
	private static final String DOC_PRODUCT_MARKETING_CATEGORIES = "product_marketing_categories";
	private static final String DOC_PRODUCT_MARKETING_SUBCATEGORIES = "product_marketing_subcategories";
	private static final String DOC_PRODUCT_NUMBER_NAMES = "product_number_names";
	private static final String DOC_SUPPORT_NAME_OID="support_name_oids";
		
	// Constants for SOAR
	private static final String SOAR_COLLECTION_DOC = "collection";
	private static final String SOAR_PRODUCTS_SUPPORTED_DOC = "products-supported";
	private static final String SOAR_OID_ATTR = "@oid";
	private static final String SOAR_PROJECTOID_ATTR = "@projectoid";
	private static final String DOT = ".";
	private static final String SOAR_ITEM_ID="@item-ID";
	
	private boolean isPartitioned = false;
	private int tempDocumentSize = 0;
	private boolean supportdocument = false;
	
	private static final ThreadLocal<String> company_info = new ThreadLocal<String>();
	
	//SMO 7130 Changes : methods added for setting and getting companyinfo in a threadlocal variable.
	
	private static void setCompany_info(String info)
	{
		company_info.set(info);
	}
	
		
	private static String getCompany_info()
	{
		return company_info.get();
	}
	
	/**
	 * Since MongoDB has a limitation of storing a document, in a collection, of maximum size 16 MB,
	 * Max Size allowed for hierarchy document is 12 MB only and 4 MB is kept for rest of the document.
	 * If Hierarchy document exceeds 12 MB, it will be saved to GridFS
	 * 
	 * */
	private int maxHierarchyDocSize= 16252928;
		
	/**
	 * Method evaluate.
	 * @param item Item
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 * @see com.hp.cdsplus.processor.adapters.Adapter#evaluate(Item)
	 */
	@Override
	public void evaluate(Item item) throws AdapterException, OptionsException, MongoUtilsException{
			WorkItem wItem = (WorkItem) item;
			contentDao = new ContentDAO();
		DBObject update = null;
		isPartitioned=  false;
		supportdocument = false;
		String maxSize = System.getProperty("maxHierarchyDocSize");
		
		if(maxSize!=null && maxSize!=""){
			maxHierarchyDocSize = Integer.parseInt(maxSize);
		}
		
		DBObject gridFSUpdateDocument = new BasicDBObject();
		
		if(item.getContentType().equals(SOAR_DB_NAME)) {
			update = getSoarProductList(item,gridFSUpdateDocument);
		}else if(item.getContentType().equalsIgnoreCase(Constants.MSC_DB_NAME)){
			TreeSet<String> productList = getProductList(item, Constants.MSC_ROOT_ELEMENT,PRODUCTS_DOC);
			if(productList == null){
				return;
			}
			update = expandMSCProductHierarchy(productList,gridFSUpdateDocument);
		}else{
			TreeSet<String> productList = getProductList(item, DOCUMENT_DOC,PRODUCTS_DOC);
			TreeSet<String> faqproductList =null;
			if(wItem.getContentType().equalsIgnoreCase(SUPPORT_DB_NAME)){
				this.supportdocument=true;
				faqproductList = getProductList(item,DOCUMENT_DOC, FAQ_PRODUCTS_DOC);
			}
			if(productList==null){
				if(!supportdocument){
					return;
					}else if(faqproductList==null){
					return;
				}
			}
			update = expandProductHierarchy(productList,gridFSUpdateDocument,faqproductList, wItem);
		}

		if(this.isPartitioned){
			logger.info(wItem.getId()+ " IsPartitioned : "+isPartitioned+" Total Doc Size : "+this.tempDocumentSize);
			
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(item.getContentType());
			GridFS gfs = new GridFS(db, "product_content");
		
			GridFSInputFile gfsFile = gfs.createFile(gridFSUpdateDocument.toString().getBytes());
			gfsFile.setFilename(wItem.getId());
			gfsFile.setContentType(wItem.getContentType());
			//gfsFile.save();	
			
			List<GridFSDBFile> fileList=gfs.find(new BasicDBObject().append("filename",wItem.getId()));
			if(fileList.size()==0){
				gfsFile.save();
			}else if (fileList.size()>0){
				gfs.remove(new BasicDBObject().append("filename",wItem.getId()));
				gfsFile.save();	
			}
		}
		if (update != null) {
			Options options = new Options();
			options.setContentType(item.getContentType());
			options.setDocid(wItem.getId());
			
			options.setMetadataDocument(update);
			
			contentDao.writeMetadataToTemp(options);
		}
	
	}
	
	
	/**
	 * Method to get the hierarchy information for MSC documents from PRODUCT_MASTER_DB_NAME 
	 * and form the update document for Item
	 * @param product_list
	 * @return
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public DBObject expandMSCProductHierarchy(TreeSet<String> product_list, DBObject gridFSUpdateDocument) throws OptionsException, MongoUtilsException {
		
		TreeSet product_series = new TreeSet();
		TreeSet product_big_series = new TreeSet();
		TreeSet product_number_names = new TreeSet();
		TreeSet product_lines = new TreeSet();
		TreeSet product_names = new TreeSet();
		TreeSet product_support_categories = new TreeSet();
		TreeSet product_support_subcategories = new TreeSet();
		TreeSet product_marketing_categories = new TreeSet();
		TreeSet product_marketing_subcategories = new TreeSet();
		TreeSet product_types = new TreeSet();
		TreeSet product_numbers = new TreeSet();
		TreeSet support_name_oids = new TreeSet();
		
		
		DBObject query = new BasicDBObject();
		query.put(ID_FIELD, new BasicDBObject("$in", product_list));
		
		Options options = new Options();
		options.setContentType(PRODUCT_MASTER_DB_NAME);
		options.setQuery(query);
		String hierarchyCollection = decideHierarchyCollection();
		//String hierarchyCollection = ConfigurationManager.getInstance().getMappingValue(PRODUCT_MASTER_DB_NAME, HIERARCHY_COLLECTION_NAME);
		ArrayList<DBObject> products = contentDao.getAllMetadata(hierarchyCollection, options);
		
		DBObject updateDocument = null;
				
			
		for (DBObject product : products) {
			product_series.addAll(getList(product,PRODUCT_SERIES_OID_FIELD));
			product_big_series.addAll(getList(product,PRODUCT_BIGSERIES_OID_FIELD));
			product_number_names.addAll(getList(product,PRODUCT_NUMBER_NAME_FIELD));
			product_lines.addAll(getList(product,PRODUCT_LINE_CODE_FIELD));
			product_names.addAll(getList(product,PRODUCT_NAME_OID_FIELD));
			product_support_categories.addAll(getList(product,SUPPORT_CATEGORY_OID_FIELD));
			product_support_subcategories.addAll(getList(product,SUPPORT_SUBCATEGORY_OID_FIELD));
			product_marketing_categories.addAll(getList(product,MARKETING_CATEGORY_OID_FIELD));
			product_marketing_subcategories.addAll(getList(product,MARKETING_SUBCATEGORY_OID_FIELD));
			product_types.addAll(getList(product,PRODUCT_TYPE_OID_FIELD));
			product_numbers.addAll(getList(product,PRODUCT_NUMBER_OID_FIELD));
			support_name_oids.addAll(getList(product,SUPPORT_NAME_OID_FIELD));
			
		}
	
		DBObject updateQuery =  new BasicDBObject();
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.PRODUCT,product_list);
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_SERIES+Constants.DOT+Constants.PRODUCT,getString(product_series));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_BIGSERIES+Constants.DOT+Constants.PRODUCT, getString(product_big_series));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NAMES+Constants.DOT+Constants.PRODUCT,getString(product_names));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_LINES+Constants.DOT+Constants.PRODUCT,product_lines);
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NUMBER_NAMES+Constants.DOT+Constants.PRODUCT,getString(product_number_names));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_SUPPORT_CATEGORIES+Constants.DOT+Constants.PRODUCT,getString(product_support_categories));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_SUPPORT_SUBCATEGORIES+Constants.DOT+Constants.PRODUCT,getString(product_support_subcategories));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_MARKETING_CATEGORIES+Constants.DOT+Constants.PRODUCT,getString(product_marketing_categories));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_MARKETING_SUBCATEGORIES+Constants.DOT+Constants.PRODUCT,getString(product_marketing_subcategories));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_TYPES+Constants.DOT+Constants.PRODUCT,getString(product_types));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NUMBERS+Constants.DOT+Constants.PRODUCT,getString(product_numbers));
		updateQuery.put(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_SUPPORT_NAME_OIDS+Constants.DOT+Constants.PRODUCT,getString(support_name_oids));
		
		this.tempDocumentSize += new BasicBSONEncoder().encode(updateQuery).length;
		
		if(this.tempDocumentSize > maxHierarchyDocSize){
			isPartitioned=  true;
			updateQuery.removeField(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NUMBER_NAMES+Constants.DOT+Constants.PRODUCT);
			updateQuery.removeField(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NUMBERS+Constants.DOT+Constants.PRODUCT);
			updateQuery.removeField(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_PRODUCT_NAMES+Constants.DOT+Constants.PRODUCT);
			updateQuery.removeField(Constants.MSC_ROOT_ELEMENT+Constants.DOT+Constants.MSC_PRODUCTS+Constants.DOT+Constants.MSC_SUPPORT_NAME_OIDS+Constants.DOT+Constants.PRODUCT);
			updateQuery.put("isPartitioned", true);
			
			gridFSUpdateDocument.put(Constants.MSC_PRODUCT_NUMBER_NAMES, new BasicDBObject(PRODUCT_DOC,getString(product_number_names)));
			gridFSUpdateDocument.put(Constants.MSC_PRODUCT_NUMBERS, new BasicDBObject(PRODUCT_DOC,getString(product_numbers)));
			gridFSUpdateDocument.put(Constants.MSC_PRODUCT_NAMES, new BasicDBObject(PRODUCT_DOC,getString(product_names)));
			gridFSUpdateDocument.put(Constants.MSC_SUPPORT_NAME_OIDS, new BasicDBObject(PRODUCT_DOC,getString(support_name_oids)));
			
		}
		updateDocument = new BasicDBObject("$set", updateQuery);
		return updateDocument;
	}
	
	
	/**
	 * Method to get the hierarchy information from PRODUCT_MASTER_DB_NAME 
	 * and form the update document for Item
	 * @param product_list
	 * @return
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public DBObject expandProductHierarchy(TreeSet<String> product_list, DBObject gridFSUpdateDocument, TreeSet<String> faq_product_list, WorkItem wItem) throws OptionsException, MongoUtilsException {
		
		TreeSet product_series = new TreeSet();
		TreeSet product_big_series = new TreeSet();
		TreeSet product_number_names = new TreeSet();
		TreeSet product_lines = new TreeSet();
		TreeSet product_names = new TreeSet();
		TreeSet product_support_categories = new TreeSet();
		TreeSet product_support_subcategories = new TreeSet();
		TreeSet product_marketing_categories = new TreeSet();
		TreeSet product_marketing_subcategories = new TreeSet();
		//Fix CR 250
		TreeSet product_types = new TreeSet();
		TreeSet product_numbers = new TreeSet();
		TreeSet support_name_oids = new TreeSet();

		TreeSet faq_product_series = new TreeSet();
		TreeSet faq_product_big_series = new TreeSet();
		TreeSet faq_product_number_names = new TreeSet();
		TreeSet faq_product_lines = new TreeSet();
		TreeSet faq_product_names = new TreeSet();
		TreeSet faq_product_support_categories = new TreeSet();
		TreeSet faq_product_support_subcategories = new TreeSet();
		TreeSet faq_product_marketing_categories = new TreeSet();
		TreeSet faq_product_marketing_subcategories = new TreeSet();
		TreeSet faq_product_types = new TreeSet();
		TreeSet faq_product_numbers = new TreeSet();
		TreeSet faq_support_name_oids = new TreeSet();
		
		HashMap<String, String> ods_prodmapping = new HashMap<String, String>();
		HashMap<String, String> ods_faq_prodmapping = new HashMap<String, String>();

		DBObject updateDocument = null;
		BasicDBObject updateQuery =  new BasicDBObject();
		String hierarchyCollection = decideHierarchyCollection();
		
		if(product_list!=null){
			DBObject query = new BasicDBObject();
			query.put(ID_FIELD, new BasicDBObject("$in", product_list));

			Options options = new Options();
			options.setContentType(PRODUCT_MASTER_DB_NAME);
			options.setQuery(query);
			//logger.debug("query - "+query);
			
//			String hierarchyCollection = ConfigurationManager.getInstance().getMappingValue(PRODUCT_MASTER_DB_NAME, HIERARCHY_COLLECTION_NAME);
			
			
			ArrayList<DBObject> products = contentDao.getAllMetadata(hierarchyCollection, options);
			

		
			for (DBObject product : products) {
				product_series.addAll(getList(product,PRODUCT_SERIES_OID_FIELD));
				product_big_series.addAll(getList(product,PRODUCT_BIGSERIES_OID_FIELD));
				product_number_names.addAll(getList(product,PRODUCT_NUMBER_NAME_FIELD));
				product_lines.addAll(getList(product,PRODUCT_LINE_CODE_FIELD));
				product_names.addAll(getList(product,PRODUCT_NAME_OID_FIELD));
				product_support_categories.addAll(getList(product,SUPPORT_CATEGORY_OID_FIELD));
				product_support_subcategories.addAll(getList(product,SUPPORT_SUBCATEGORY_OID_FIELD));
				product_marketing_categories.addAll(getList(product,MARKETING_CATEGORY_OID_FIELD));
				product_marketing_subcategories.addAll(getList(product,MARKETING_SUBCATEGORY_OID_FIELD));
				//Fix CR 250
				product_types.addAll(getList(product,PRODUCT_TYPE_OID_FIELD));
				product_numbers.addAll(getList(product,PRODUCT_NUMBER_OID_FIELD));
				support_name_oids.addAll(getList(product,SUPPORT_NAME_OID_FIELD));
				
				//Write the code to prepare ods transformations
				//if(this.supportdocument){ // Since same is required for manual as well, so removing this check
					ods_prodmapping.put(product.get(ID_FIELD).toString(), product.get(HIERARCHY_LEVEL).toString().toLowerCase());
				//}

			}
			
			//write ods expansions to gridfs bucket name ods_products

			updateQuery.
			append(SUB_DOC_PRODUCTS, new BasicDBObject(PRODUCT_DOC,product_list)).
			append(SUB_DOC_PRODUCT_SERIES, new BasicDBObject(PRODUCT_DOC,getString(product_series))).
			append(SUB_DOC_PRODUCT_BIGSERIES, new BasicDBObject(PRODUCT_DOC,getString(product_big_series))).
			append(SUB_DOC_PRODUCT_NAME, new BasicDBObject(PRODUCT_DOC,getString(product_names))).
			append(SUB_DOC_PRODUCT_LINE_CODE, new BasicDBObject(PRODUCT_DOC,product_lines)).
			append(SUB_DOC_PRODUCT_NUMBER_NAME, new BasicDBObject(PRODUCT_DOC,getString(product_number_names))).
			append(SUB_DOC_SUPPORT_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(product_support_categories))).
			append(SUB_DOC_SUPPORT_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(product_support_subcategories))).
			append(SUB_DOC_MARKETING_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_categories))).
			append(SUB_DOC_MARKETING_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_subcategories))).
			append(SUB_DOC_PRODUCT_TYPE, new BasicDBObject(PRODUCT_DOC,getString(product_types))).
			append(SUB_DOC_PRODUCT_NUMBER, new BasicDBObject(PRODUCT_DOC,getString(product_numbers))).
			append(SUB_DOC_SUPPORT_NAME, new BasicDBObject(PRODUCT_DOC,getString(support_name_oids)));
		}

		//preparing faq prodcuts hieararchy if the document is of support type.
		if(this.supportdocument){
			if(faq_product_list!=null){

			DBObject query = new BasicDBObject();
			query.put(ID_FIELD, new BasicDBObject("$in", faq_product_list));

			Options options = new Options();
			options.setContentType(PRODUCT_MASTER_DB_NAME);
			options.setQuery(query);
			//logger.debug("query - "+query);

			
			ArrayList<DBObject> products = contentDao.getAllMetadata(hierarchyCollection, options);

			//logger.debug(products.size());

			// Loop through all products to do expansion from Hierarchy

			for (DBObject product : products) {
				faq_product_series.addAll(getList(product,PRODUCT_SERIES_OID_FIELD));
				faq_product_big_series.addAll(getList(product,PRODUCT_BIGSERIES_OID_FIELD));
				faq_product_number_names.addAll(getList(product,PRODUCT_NUMBER_NAME_FIELD));
				faq_product_lines.addAll(getList(product,PRODUCT_LINE_CODE_FIELD));
				faq_product_names.addAll(getList(product,PRODUCT_NAME_OID_FIELD));
				faq_product_support_categories.addAll(getList(product,SUPPORT_CATEGORY_OID_FIELD));
				faq_product_support_subcategories.addAll(getList(product,SUPPORT_SUBCATEGORY_OID_FIELD));
				faq_product_marketing_categories.addAll(getList(product,MARKETING_CATEGORY_OID_FIELD));
				faq_product_marketing_subcategories.addAll(getList(product,MARKETING_SUBCATEGORY_OID_FIELD));
				faq_product_types.addAll(getList(product,PRODUCT_TYPE_OID_FIELD));
				faq_product_numbers.addAll(getList(product,PRODUCT_NUMBER_OID_FIELD));
				faq_support_name_oids.addAll(getList(product,SUPPORT_NAME_OID_FIELD));
				
				ods_faq_prodmapping.put(product.get(ID_FIELD).toString(), product.get(HIERARCHY_LEVEL).toString().toLowerCase());
				
			}

				updateQuery.
				append(DOCUMENT_DOC+DOT+FAQ_PRODUCTS_DOC, new BasicDBObject(PRODUCT_DOC,faq_product_list)).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_SERIES, new BasicDBObject(PRODUCT_DOC,getString(faq_product_series))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_BIGSERIES, new BasicDBObject(PRODUCT_DOC,getString(faq_product_big_series))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NAME, new BasicDBObject(PRODUCT_DOC,getString(faq_product_names))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_LINE_CODE, new BasicDBObject(PRODUCT_DOC,faq_product_lines)).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER_NAME, new BasicDBObject(PRODUCT_DOC,getString(faq_product_number_names))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_support_categories))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_support_subcategories))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_marketing_categories))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_marketing_subcategories))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_TYPE, new BasicDBObject(PRODUCT_DOC,getString(faq_product_types))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER, new BasicDBObject(PRODUCT_DOC,getString(faq_product_numbers))).
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_NAME_OIDS, new BasicDBObject(PRODUCT_DOC,getString(faq_support_name_oids)));
				}
			else{
				updateQuery.
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_SERIES+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_BIGSERIES+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NAME+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_LINE_CODE+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER_NAME+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_CATEGORY+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_SUBCATEGORY+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_CATEGORY+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_SUBCATEGORY+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_TYPE+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER+DOT+PRODUCT_DOC,"").
				append(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_NAME_OIDS+DOT+PRODUCT_DOC,"");
				}
		}
		this.tempDocumentSize += new BasicBSONEncoder().encode(updateQuery).length;
		//System.out.println(tempDocumentSize);
		if(this.tempDocumentSize > maxHierarchyDocSize){
			isPartitioned=  true;

			updateQuery.removeField(SUB_DOC_PRODUCT_SERIES);
			updateQuery.removeField(SUB_DOC_PRODUCT_BIGSERIES);
			updateQuery.removeField(SUB_DOC_SUPPORT_CATEGORY);
			updateQuery.removeField(SUB_DOC_SUPPORT_SUBCATEGORY);
			updateQuery.removeField(SUB_DOC_MARKETING_CATEGORY);
			updateQuery.removeField(SUB_DOC_MARKETING_SUBCATEGORY);
			updateQuery.removeField(SUB_DOC_PRODUCT_TYPE);
			updateQuery.removeField(SUB_DOC_PRODUCT_NUMBER);
			updateQuery.removeField(SUB_DOC_PRODUCT_NUMBER_NAME);
			updateQuery.removeField(SUB_DOC_PRODUCT_NAME);
			updateQuery.removeField(SUB_DOC_SUPPORT_NAME);
			updateQuery.put("isPartitioned", true);

			//gridFSUpdateDocument = new BasicDBObject(); //Commented as this object is already initialised 
			gridFSUpdateDocument.put(DOC_PRODUCT_NUMBER_NAMES, new BasicDBObject(PRODUCT_DOC,getString(product_number_names)));
			gridFSUpdateDocument.put(DOC_PRODUCT_NUMBERS, new BasicDBObject(PRODUCT_DOC,getString(product_numbers)));
			gridFSUpdateDocument.put(DOC_PRODUCT_NAMES, new BasicDBObject(PRODUCT_DOC,getString(product_names)));
			gridFSUpdateDocument.put(DOC_SUPPORT_NAME_OID, new BasicDBObject(PRODUCT_DOC,getString(support_name_oids)));
			gridFSUpdateDocument.put(DOC_PRODUCT_SERIES, new BasicDBObject(PRODUCT_DOC,getString(product_series)));
			gridFSUpdateDocument.put(DOC_PRODUCT_BIG_SERIES, new BasicDBObject(PRODUCT_DOC,getString(product_big_series)));
			gridFSUpdateDocument.put(DOC_PRODUCT_SUPPORT_CATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_support_categories)));
			gridFSUpdateDocument.put(DOC_PRODUCT_SUPPORT_SUBCATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_support_subcategories)));
			gridFSUpdateDocument.put(DOC_PRODUCT_MARKETING_CATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_categories)));
			gridFSUpdateDocument.put(DOC_PRODUCT_MARKETING_SUBCATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_subcategories)));
			gridFSUpdateDocument.put(DOC_PRODUCT_TYPES, new BasicDBObject(PRODUCT_DOC,getString(product_types)));

			if(this.supportdocument && faq_product_list!=null ){
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER_NAME);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NUMBER);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_NAME);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_SERIES);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_BIGSERIES);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_PRODUCT_TYPE);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_CATEGORY);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_MARKETING_SUBCATEGORY);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_CATEGORY);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_SUBCATEGORY);
				updateQuery.removeField(DOCUMENT_DOC+DOT+SUB_DOC_FAQ_SUPPORT_NAME_OIDS);
				
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_NUMBER_NAME, new BasicDBObject(PRODUCT_DOC,getString(faq_product_number_names)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_NUMBER, new BasicDBObject(PRODUCT_DOC,getString(faq_product_numbers)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_NAME, new BasicDBObject(PRODUCT_DOC,getString(faq_product_names)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_SERIES, new BasicDBObject(PRODUCT_DOC,getString(faq_product_series)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_BIGSERIES, new BasicDBObject(PRODUCT_DOC,getString(faq_product_big_series)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_PRODUCT_TYPE, new BasicDBObject(PRODUCT_DOC,getString(faq_product_types)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_MARKETING_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_marketing_categories)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_MARKETING_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_marketing_subcategories)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_SUPPORT_CATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_support_categories)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_SUPPORT_SUBCATEGORY, new BasicDBObject(PRODUCT_DOC,getString(faq_product_support_subcategories)));
				gridFSUpdateDocument.put(SUB_DOC_FAQ_SUPPORT_NAME_OIDS, new BasicDBObject(PRODUCT_DOC,getString(faq_support_name_oids)));
				
			}
		}
		
		if(SUPPORT_DB_NAME.equalsIgnoreCase(wItem.getContentType()) || MANUAL_DB_NAME.equalsIgnoreCase(wItem.getContentType())){
			
			BasicDBObject gridFSodsProducts = new BasicDBObject(); 
			gridFSodsProducts.append("products", ods_prodmapping );
			
			// FAQ Products are in support only. Not required for manual
			if(this.supportdocument){
				gridFSodsProducts.append("faq_products", ods_faq_prodmapping );
			}
			
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(wItem.getContentType());
			GridFS gfs = new GridFS(db, "ods_products");
			GridFSInputFile gfsFile = gfs.createFile(gridFSodsProducts.toString().getBytes());
			gfsFile.setFilename(wItem.getId());
	
			List<GridFSDBFile> gridFSDBFileList=gfs.find(new BasicDBObject().append("filename",wItem.getId()));
			if(gridFSDBFileList.size()==0){
				gfsFile.save();
			}else if (gridFSDBFileList.size()>=1){
				gfs.remove(new BasicDBObject().append("filename",wItem.getId()));
				gfsFile.save();
			}
		}
		//logger.debug("Update Document  : "+updateQuery);
		//logger.debug("gridFSUpdateDocument  : "+gridFSUpdateDocument);
		updateDocument = new BasicDBObject("$set", updateQuery);
		return updateDocument;
	}
	
	//SMO : UserStory#7130 
	//Description: new method added for deciding the hierarchy collection (HPI/HPE) based on company info field in document
	private String decideHierarchyCollection() throws MongoUtilsException {
		String hierarchyCollection = null;

		String companyType = getCompany_info();
		hierarchyCollection = ConfigurationManager.getInstance().getMappingValue(PRODUCT_MASTER_DB_NAME, companyType.toLowerCase()+HIERARCHY_COLLECTION_NAME);
		
		if(StringUtils.isEmpty(hierarchyCollection))
		{
			logger.debug("Hierarchy collection name  "+ hierarchyCollection +" is found to be invalid");
			return null;
		}

		return hierarchyCollection;
	}

	
	/**
	 * Method to get the hierarchy information from PRODUCT_MASTER_DB_NAME and form the
	 * update document for Soar Item
	 * 
	 * @param returnList
	 * @return
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DBObject expandSoarProductHierarchy(TreeSet<String> productOidList, BasicDBList products_list, DBObject gridFSUpdateDocument, String item_id)
			throws OptionsException, MongoUtilsException {
		TreeSet product_series = new TreeSet();
		TreeSet product_big_series = new TreeSet();
		TreeSet product_number_names = new TreeSet();
		TreeSet product_lines = new TreeSet();
		TreeSet product_names = new TreeSet();
		TreeSet product_support_categories = new TreeSet();
		TreeSet product_support_subcategories = new TreeSet();
		TreeSet product_marketing_categories = new TreeSet();
		TreeSet product_marketing_subcategories = new TreeSet();
		// Fix CR 250
		TreeSet product_types = new TreeSet();
		TreeSet product_numbers = new TreeSet();
		TreeSet support_name_oids = new TreeSet();

		// logger.debug("Product List : " + returnList);
		DBObject query = new BasicDBObject();
		query.put(ID_FIELD, new BasicDBObject("$in", productOidList));

		Options options = new Options();
		options.setContentType(PRODUCT_MASTER_DB_NAME);
		options.setQuery(query);
		// logger.debug("query - "+query);
		//SMO : UserStory#7130 
		String hierarchyCollection = decideHierarchyCollection();
//		String hierarchyCollection = ConfigurationManager.getInstance().getMappingValue(PRODUCT_MASTER_DB_NAME, HIERARCHY_COLLECTION_NAME);
		ArrayList<DBObject> products = contentDao.getAllMetadata(
				hierarchyCollection, options);

		// logger.debug(products.size());

		DBObject updateDocument = null;
		DBObject updateDocument1 = null;
		
		// Loop through all products to do expansion from Hierarchy

		for (DBObject product : products) {
			product_series.addAll(getList(product, PRODUCT_SERIES_OID_FIELD));
			product_big_series.addAll(getList(product,
					PRODUCT_BIGSERIES_OID_FIELD));
			product_number_names.addAll(getList(product,
					PRODUCT_NUMBER_NAME_FIELD));
			product_lines.addAll(getList(product, PRODUCT_LINE_CODE_FIELD));
			product_names.addAll(getList(product, PRODUCT_NAME_OID_FIELD));
			product_support_categories.addAll(getList(product,
					SUPPORT_CATEGORY_OID_FIELD));
			product_support_subcategories.addAll(getList(product,
					SUPPORT_SUBCATEGORY_OID_FIELD));
			product_marketing_categories.addAll(getList(product,
					MARKETING_CATEGORY_OID_FIELD));
			product_marketing_subcategories.addAll(getList(product,
					MARKETING_SUBCATEGORY_OID_FIELD));
			// Fix CR 250
			product_types.addAll(getList(product, PRODUCT_TYPE_OID_FIELD));
			product_numbers.addAll(getList(product, PRODUCT_NUMBER_OID_FIELD));
			support_name_oids.addAll(getList(product, SUPPORT_NAME_OID_FIELD));
		}
		updateDocument = new BasicDBObject();
		updateDocument1 = new BasicDBObject();
		
		
				updateDocument.put(DOC_PRODUCT_SERIES,	new BasicDBObject(PRODUCT_DOC, getString(product_series)));
				updateDocument.put(DOC_PRODUCT_BIG_SERIES, new BasicDBObject(PRODUCT_DOC, getString(product_big_series)));
				updateDocument.put(DOC_PRODUCT_LINES, new BasicDBObject(PRODUCT_DOC, product_lines));
				updateDocument.put(DOC_PRODUCT_SUPPORT_CATEGORIES,	new BasicDBObject(PRODUCT_DOC,getString(product_support_categories)));
				updateDocument.put(DOC_PRODUCT_SUPPORT_SUBCATEGORIES, new BasicDBObject(PRODUCT_DOC, getString(product_support_subcategories)));
				updateDocument.put(DOC_PRODUCT_MARKETING_CATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_categories)));
				updateDocument.put(DOC_PRODUCT_MARKETING_SUBCATEGORIES, new BasicDBObject(PRODUCT_DOC,getString(product_marketing_subcategories)));
				updateDocument.put(DOC_PRODUCT_TYPES,new BasicDBObject(PRODUCT_DOC, getString(product_types)));
				updateDocument.put(PRODUCT_DOC,products_list);
				updateDocument.put(DOC_PRODUCT_NAMES, new BasicDBObject(PRODUCT_DOC, getString(product_names)));
				updateDocument.put(DOC_PRODUCT_NUMBERS, new BasicDBObject(PRODUCT_DOC, getString(product_numbers)));
				updateDocument.put(DOC_PRODUCT_NUMBER_NAMES, new BasicDBObject(PRODUCT_DOC, getString(product_number_names)));
				updateDocument.put(DOC_SUPPORT_NAME_OID, new BasicDBObject(PRODUCT_DOC, getString(support_name_oids)));
				
				updateDocument1.put(DOC_PRODUCT_NAMES, new BasicDBObject(PRODUCT_DOC, getString(product_names)));
				updateDocument1.put(DOC_PRODUCT_NUMBERS, new BasicDBObject(PRODUCT_DOC, getString(product_numbers)));
				updateDocument1.put(DOC_PRODUCT_NUMBER_NAMES, new BasicDBObject(PRODUCT_DOC, getString(product_number_names)));
				updateDocument1.put(DOC_SUPPORT_NAME_OID, new BasicDBObject(PRODUCT_DOC, getString(support_name_oids)));
				
		gridFSUpdateDocument.put(item_id, updateDocument1);		
		
		return updateDocument;
		
	}


	private String getString(TreeSet<String> oidSet) {
		StringBuffer returnStr = new StringBuffer();
		for(String oid : oidSet){
			if(returnStr.length() == 0)
				returnStr.append(oid);
			else
				returnStr.append(",").append(oid);
		}
		return returnStr.toString();
	}



	/**
	 * @param dbObject
	 * @param key
	 * @return list of DB Object
	 */
	private BasicDBList getList(DBObject dbObject, String key) {
		Object object = dbObject.get(key);
		if (object == null || object=="") {
			return new BasicDBList();
		}
		
		BasicDBList returnList = new BasicDBList();

		if (object instanceof BasicDBList) {
			return (BasicDBList) object;
		} else if (object instanceof DBObject) {
			returnList.add((DBObject) object);
		} else if (object instanceof String) {
			String str = (String) object;
			if ((key.equalsIgnoreCase(PRODUCT_SERIES_OID_FIELD)
					|| key.equalsIgnoreCase(PRODUCT_BIGSERIES_OID_FIELD)
					|| key.equalsIgnoreCase(PRODUCT_NUMBER_NAME_FIELD)
					|| key.equalsIgnoreCase(PRODUCT_NAME_OID_FIELD)
					|| key.equalsIgnoreCase(SUPPORT_CATEGORY_OID_FIELD)
					|| key.equalsIgnoreCase(SUPPORT_SUBCATEGORY_OID_FIELD)
					|| key.equalsIgnoreCase(MARKETING_CATEGORY_OID_FIELD)
					|| key.equalsIgnoreCase(MARKETING_SUBCATEGORY_OID_FIELD)
					|| key.equalsIgnoreCase(PRODUCT_TYPE_OID_FIELD)
					|| key.equalsIgnoreCase(PRODUCT_NUMBER_OID_FIELD)
					|| key.equalsIgnoreCase(SUPPORT_NAME_OID_FIELD) )
					|| key.equalsIgnoreCase(PRODUCT_LINE_CODE_FIELD)
					&& str.contains(","))
				returnList.addAll(Arrays.asList(str.split(",")));
			else
				returnList.add(str);

		}else{
			returnList.add(object);
		}
		return returnList;
	}
	/**
	 * Method to retrieve productList for the Item
	 * @param item
	 * @return
	 * @throws MongoUtilsException
	 * @throws AdapterException
	 * @throws OptionsException
	 */
	public TreeSet<String> getProductList(Item item, String root_element,String sub_element) throws MongoUtilsException, AdapterException, OptionsException{
		TreeSet<String> returnList = new TreeSet<String>();
		WorkItem workItem = (WorkItem) item;
		
		Options options = new Options();
		options.setContentType(item.getContentType());
		options.setDocid(workItem.getId());

		DBObject result = contentDao.getTempMetadata(options);
		
		if(result ==null){
			logger.error("Can't find Document object. Skipping processing of document id - " + workItem.getId());
			return null;
		}
		
		this.tempDocumentSize += new BasicBSONEncoder().encode(result).length;
		
		DBObject document = (DBObject) result.get(root_element);
		if(document==null){
			logger.debug("Sub ducument: document for "+workItem.getId()+"is null");
			return null;
		}
//SMO : UserStory#7130 
		String companyType = (String)document.get(COMPANY_INFO);
		if(StringUtils.isEmpty(companyType))
		{
			logger.debug("Company_info "+ companyType +" is an invalid value");
			companyType="";
		}
		
			setCompany_info(companyType);
		Object prdObj = document.get(sub_element);
		DBObject products = null;
		
		if(prdObj instanceof DBObject)
			products = (DBObject) document.get(sub_element);
		
		if(products==null){
			logger.debug(workItem.getId()+" document.products is null");
			return null;
		}
		
		//logger.debug( products.get(PRODUCT_DOC));
		BasicDBList productList = getList(products, PRODUCT_DOC);	
		
		Iterator<?> itr = productList.iterator();
		while(itr.hasNext()){
			String oid = itr.next().toString();
			if(oid.contains("_")){
				oid = oid.substring(oid.indexOf("_")+1);
			}
			if(oid != null && !"".equals(oid))
				returnList.add(oid);
		}
		return returnList;
	}
	/**
	 * Method to retrieve productList for the Item
	 * 
	 * @param item
	 * @return
	 * @throws MongoUtilsException
	 * @throws AdapterException
	 * @throws OptionsException
	 */
	@SuppressWarnings("unchecked")
	public BasicDBObject getSoarProductList(Item item, DBObject gridFSUpdateDocument)
			throws MongoUtilsException, AdapterException, OptionsException {
		TreeSet productOidList =null;
		WorkItem workItem = (WorkItem) item;
		BasicDBObject updateQuery =new BasicDBObject();
		BasicDBObject updateDocument = new BasicDBObject();
		
		Options options = new Options();
		options.setContentType(item.getContentType());
		options.setDocid(workItem.getId());

		DBObject result = contentDao.getTempMetadata(options);
		
		if (result == null) {
			/*throw new AdapterException("Can't find Document "
					+ workItem.getId());*/
			logger.error("Can't find Document "
					+ workItem.getId());
			return null;
		}
		this.tempDocumentSize += new BasicBSONEncoder().encode(result).length;
		
		DBObject soarsoftwarefeed = getObject(result, SOAR_SOFTWARE_FEED_DOC);
		if (soarsoftwarefeed == null) {
			logger.debug("Sub ducument: document for " + workItem.getId()
					+ "is null");
			return null;
		}
		else {
			DBObject collection = getObject(soarsoftwarefeed,SOAR_DOC_COLLECTION);
				if (collection != null) {
					DBObject software_items = getObject(collection, SOAR_SOFTWARE_ITEMS_DOC);
					//SMO : UserStory#7130 
					String companyType = (String)collection.get(COMPANY_INFO);
					if(StringUtils.isEmpty(companyType))
					{
						logger.debug("Company_info "+ companyType +" is an invalid value");
						companyType="";
					}
					
						setCompany_info(companyType);
					if (software_items != null) {
						
						BasicDBList software_item_list = getList(software_items,
								SOAR_SOFTWARE_ITEM_DOC);
						int itemIndex = 0;
						Iterator<?> itr = software_item_list.iterator();
						
						//gridFSUpdateDocument = new BasicDBObject();//Commented as this object is already initialised 
						
						while (itr.hasNext()) {
							productOidList= new TreeSet();						
							DBObject software_item = (DBObject) itr.next();
							String item_id= (String)software_item.get(SOAR_ITEM_ID);
							DBObject products_supported = getObject(software_item,
									SOAR_PRODUCTS_SUPPORTED_DOC);
							if (products_supported != null) {
								BasicDBList product_list = getList(products_supported,
										PRODUCT_DOC);
								// Create a new product list to store product with @projectoid
								BasicDBList product_list_projectOid = new BasicDBList();
								//int productArrayIndex = 0;
								Iterator<?> product_itr = product_list.iterator();
								while (product_itr.hasNext()) {
									
									DBObject product = (DBObject) product_itr.next();
									
									String projectOid = (String)product.get(SOAR_OID_ATTR);
									if(projectOid!=null && projectOid!=""){
										
										productOidList.add(projectOid);
										product.removeField(SOAR_OID_ATTR);
										product.put(SOAR_PROJECTOID_ATTR, projectOid);
										product_list_projectOid.add(product);
									}
								}
								StringBuilder buildQuery = new StringBuilder();
								
								buildQuery.append(SOAR_SOFTWARE_FEED_DOC + DOT
										+ SOAR_COLLECTION_DOC + DOT
										+ SOAR_SOFTWARE_ITEMS_DOC + DOT
										+ SOAR_SOFTWARE_ITEM_DOC + DOT);
								// for each software-item
								if (software_item_list.size() > 1) {
									buildQuery.append(itemIndex + DOT);
								}
								buildQuery.append(SOAR_PRODUCTS_SUPPORTED_DOC);
								
								updateQuery.append(buildQuery.toString(),expandSoarProductHierarchy(productOidList,product_list_projectOid,gridFSUpdateDocument,item_id));
								
						}
				itemIndex++;		
				}
						this.tempDocumentSize += new BasicBSONEncoder().encode(updateQuery).length;
						
						if(this.tempDocumentSize > maxHierarchyDocSize){
							this.isPartitioned=  true;
							
							for (int i = 0; i < software_item_list.size(); i++) {
								DBObject prodSupportObj = (DBObject)updateQuery.remove(SOAR_SOFTWARE_FEED_DOC + DOT
										+ SOAR_COLLECTION_DOC + DOT
										+ SOAR_SOFTWARE_ITEMS_DOC + DOT
										+ SOAR_SOFTWARE_ITEM_DOC + DOT+i+DOT+SOAR_PRODUCTS_SUPPORTED_DOC);
								prodSupportObj.removeField(DOC_PRODUCT_NUMBER_NAMES);
								prodSupportObj.removeField(DOC_PRODUCT_NUMBERS);
								prodSupportObj.removeField(DOC_PRODUCT_NAMES);
								prodSupportObj.removeField(DOC_SUPPORT_NAME_OID);
								updateQuery.put(SOAR_SOFTWARE_FEED_DOC + DOT
										+ SOAR_COLLECTION_DOC + DOT
										+ SOAR_SOFTWARE_ITEMS_DOC + DOT
										+ SOAR_SOFTWARE_ITEM_DOC + DOT+i+DOT+SOAR_PRODUCTS_SUPPORTED_DOC,prodSupportObj);
								
							}
							updateQuery.put("isPartitioned", true);
						}							
			}
		}
		}
		updateDocument.append("$set", updateQuery);
		return updateDocument;
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return
	 */
	private DBObject getObject(DBObject dbObject, String key) {
		DBObject returnObj = new BasicDBObject();
		Object obj = dbObject.get(key);
		if (obj == null || obj == "") {
			return null;
		} else if (obj instanceof DBObject) {
			returnObj = (DBObject) obj;

		} else if (obj instanceof String) {
			returnObj = (DBObject) obj;

		}
		return returnObj;
	}

	/**
	 * @return the contentDao
	 */
	public ContentDAO getContentDao() {
		return contentDao;
	}
	/**
	 * @param contentDao the contentDao to set
	 */
	public void setContentDao(ContentDAO contentDao) {
		this.contentDao = contentDao;
	}

	public static void main(String[] args) throws ProcessException, MongoUtilsException, OptionsException, UnknownHostException{
		System.setProperty("mongo.configuration","config/mongo.properties");
		//Mongo mongo = new Mongo("g2t1888c.austin.hp.com");
		
	//	DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("support");
	//	DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("library");
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("soar");
		DBCollection metadata_temp = db.getCollection("metadata_temp");
		DBObject obj = new BasicDBObject();
		//"eventType" : "update" , "priority" : 1 , "lastModified" : 1374487506735}
//		obj.put("_id", "c02605330");
//		obj.put("priority", 1);
//		obj.put("eventType", "update");
//		obj.put("lastModified" , 1374487506735L);
		obj.put("_id", "col9944");
		obj.put("priority", 1);
		obj.put("eventType", "update");
		obj.put("lastModified" , 1425545302178L);
//		obj.put("_id", "c00060911");
//		obj.put("priority", 3);
//		obj.put("eventType", "update");
//		obj.put("lastModified" , 1382578657586L);
		
		QueueManager queMgr= new QueueManager();
//		queMgr.push(new ContentItem("support",0L, queMgr));
//	WorkItem item = new WorkItem("support", obj, queMgr);
		queMgr.push(new ContentItem("soar",0L, queMgr));
		WorkItem item = new WorkItem("soar", obj, queMgr);	
//		queMgr.push(new ContentItem("library",0L, queMgr));
//		WorkItem item = new WorkItem("library", obj, queMgr);
	//item.load();
	
	HierarchyExpansionAdapter hierarchyAdapter = new HierarchyExpansionAdapter();
	hierarchyAdapter.evaluate(item);
	System.out.println(metadata_temp.findOne(new BasicDBObject("_id","col9944")));
//	System.out.println(metadata_temp.findOne(new BasicDBObject("_id","c00060911")));
	}
}
	