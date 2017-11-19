package com.hp.cdsplus.web.contentservice.impl;

import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;

public class RichmediaServiceImpl extends AbstractGenericService {


	@Override
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException {
		/*
		 * 1. call DAO getLiveMetadata returns DBObject 2. get document from
		 * dbobject [it only gives the document bind to output bindigs] 3. pass
		 * this json to conversion ( get the javaPackageName, javaClassName for
		 * output bindigns
		 * ConfigurationManager.getInstance().getMappingValue(dbName,
		 * javaPackageName);
		 * ConfigurationManager.getInstance().getMappingValue(dbName,
		 * javaClassName);
		 * 
		 * replace projectref-->proj:ref(if not done at conversion utils side) *
		 * 4. return the parsed xml to browser.
		 */
		
		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub)){
			 return stylesheetUtil.getStylesheetXMLDocument(options);
		}
		
		DBObject document;
		Object result;
		String doc;
		try {
			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			if (!validateSubcsription(options, subscription))
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			if(subscription.equalsIgnoreCase("content"))
			{
			    DBObject displayFields = options.getDisplayFields();
			    if(displayFields == null)
			    {
				displayFields = new BasicDBObject();
			    }
			    displayFields.put("asset_rendition.company_info", 0);
			    options.setDisplayFields(displayFields);
			}
			if(options.getLastModified() > 0L){
				document = contentDAO.getHistoryMetadata(options);
			}else {
				document = contentDAO.getLiveMetadata(options);
			}

			//System.out.println(document);
			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
						+ options.getDocid() + " "
						+ ServiceConstants.errorMsg_doesnt_exist);
			}

			else if(document.get("eventType")!=null && document.get("eventType").toString()!=null)
			{
				if(document.get("eventType").toString().equalsIgnoreCase("delete")){
					throw new ApplicationException(options.getDocid()+" "+ServiceConstants.errorMsg_doesnt_exist);      
				}
			}

			DBObject dbObject=null;
			if(document.get("asset_rendition")!=null){
				dbObject=new BasicDBObject("asset_rendition",document.get("asset_rendition"));
			}else{
				throw new ApplicationException(options.getDocid()+" "+ServiceConstants.errorMsg_doesnt_exist);
			}
			
			Boolean isPartitioned = (document.get("isPartitioned")!=null && Boolean.valueOf(document.get("isPartitioned").toString()));

			if(options.getSubscription() != null || !"".equals(options.getSubscription())){
				handleProductHierarchy(options , dbObject , isPartitioned);
			}   
			
			String dbName = options.getContentType();

			String bindingClassName = ConfigurationManager.getInstance()
			.getMappingValue(dbName, "javaPackageName")
			+ "."
			+ ConfigurationManager.getInstance().getMappingValue(
					dbName, "javaClassName");

			result = conversion.convertJSONtoObject(dbObject.toString(), bindingClassName);
			doc=removeIndentation(convertObjectToXml(options, result));
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}catch(ConversionUtilsException e){
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		}

		return doc;
	}

	protected void handleProductHierarchy(Options options,DBObject record,Boolean isPartitioned) {
		DBObject rootdocument = getObject(record, MSC_DOC_ROOT);
		DBObject document = getObject(rootdocument, SUB_DOC_PRODUCTS);
		if(isPartitioned){
			DBObject partition_object = getPartitionObject(options);
			mergeProductMetadata(options,document,  partition_object);
		}             
		convertStringToList(document,SUB_DOC_PRODUCT_NAME);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER_NAME);           
		convertStringToList(document,SUB_DOC_SUPPORT_NAME);
		
		convertStringToList(document,SUB_DOC_PRODUCT_TYPE);
		convertStringToList(document,SUB_DOC_SUPPORT_CATEGORY);
		convertStringToList(document,SUB_DOC_SUPPORT_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_CATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_PRODUCT_BIGSERIES);
		convertStringToList(document,SUB_DOC_PRODUCT_SERIES);
		
		rootdocument.put(SUB_DOC_PRODUCTS, document);
		record.put(MSC_DOC_ROOT, rootdocument);
	}

	protected void mergeProductMetadata(Options options,DBObject document, DBObject partition_obj) {	

		DBObject hierExpObj = null;
		try {
			if(options.getSubscription()!=null && !options.getSubscription().equals("content")){
				String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
				if(dbName == null){
					dbName = options.getContentType();
				}
				DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
				String subscriptionCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "subscriptionCollection");
				DBCollection subscriptionCollection = db.getCollection(subscriptionCollectionName);
				DBObject query = new BasicDBObject("_id", options.getSubscription());
				DBObject fields = new BasicDBObject("hierarchyExpansions",1);
				DBObject result = subscriptionCollection.findOne(query,fields);
				hierExpObj = (DBObject) result.get("hierarchyExpansions");		
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(500);
		}

		DBObject product = getObject(partition_obj, SUB_DOC_PRODUCT_NUMBER);
		if(product!=null && hierExpObj!=null && hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NUMBER)!=null && (Boolean)hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NUMBER)!=false)
			document.put(SUB_DOC_PRODUCT_NUMBER, product);
		
		if(options.getSubscription()==null || options.getSubscription().equals("content"))
			document.put(SUB_DOC_PRODUCT_NUMBER, product);

		product = getObject(partition_obj, SUB_DOC_PRODUCT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NAME)!=null && (Boolean)hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NAME)!=false)
			document.put(SUB_DOC_PRODUCT_NAME, product);
		
		if(options.getSubscription()==null || options.getSubscription().equals("content"))
			document.put(SUB_DOC_PRODUCT_NAME, product);

		product =  getObject(partition_obj, SUB_DOC_PRODUCT_NUMBER_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NUMBER_NAME)!=null && (Boolean)hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_PRODUCT_NUMBER_NAME)!=false)
			document.put(SUB_DOC_PRODUCT_NUMBER_NAME, product);
		
		if(options.getSubscription()==null || options.getSubscription().equals("content"))
			document.put(SUB_DOC_PRODUCT_NUMBER_NAME, product);

		product = getObject(partition_obj, SUB_DOC_SUPPORT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_SUPPORT_NAME)!=null && (Boolean)hierExpObj.get(MSC_DOC_ROOT+DOT+SUB_DOC_PRODUCTS+DOT+SUB_DOC_SUPPORT_NAME)!=false)
			document.put(SUB_DOC_SUPPORT_NAME, product);
		
		if(options.getSubscription()==null || options.getSubscription().equals("content"))
			document.put(SUB_DOC_SUPPORT_NAME, product);

	}

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

}
