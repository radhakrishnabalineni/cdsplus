package com.hp.cdsplus.web.contentservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.cdsplus.bindings.output.schema.expattachment.Attachments;
import com.hp.cdsplus.bindings.output.schema.expversions.Versions;
import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.ConfigDAO;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.RefDataDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.cdsplus.web.util.StylesheetSubscriptionUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import javax.ws.rs.WebApplicationException;


/**
 * @author reddypm These Generic abstract class is mainly used for common
 *         methods like retrieve DAO object
 *         ,getDocumentList,getSubscriptionList,ParsingDocument for any content
 *         service impl.
 * 
 * 
 */
/**
 * @author pamujulv
 *
 */
public abstract class AbstractGenericService implements ContentService {

	/**
	 * These method is used to hit the DB layer and get Object /List of Objects
	 * 
	 * @return list of string
	 * 
	 */
	protected ContentDAO contentDAO = new ContentDAO();
	protected ConfigDAO configDAO = new ConfigDAO();
	protected ConversionUtils conversion = new ConversionUtils();
	protected MongoAPIUtils mongoUtils = new MongoAPIUtils();
	protected RefDataDAO refDataDAO = new RefDataDAO();
	protected StylesheetSubscriptionUtil stylesheetUtil = new StylesheetSubscriptionUtil();
	/* (non-Javadoc)
	 * @see com.hp.cdsplus.web.contentservice.ContentService#getContentList(com.hp.cdsplus.dao.Options)
	 */
	@Override
	public Object getContentList(Options options) throws WebApplicationException {

		com.hp.cdsplus.bindings.output.schema.cadence.Request request = new com.hp.cdsplus.bindings.output.schema.cadence.Request();
		try{
			List<DBObject> daoObjectList = contentDAO.getContentTypeList(options);
			request.setBase(options.getBaseUri() == null ? "null" : options.getBaseUri());
			List<com.hp.cdsplus.bindings.output.schema.cadence.Ref> refs = new ArrayList<com.hp.cdsplus.bindings.output.schema.cadence.Ref>();
			com.hp.cdsplus.bindings.output.schema.cadence.Ref ref = null;

			if (daoObjectList != null && !daoObjectList.isEmpty()) {
				request.setCount(String.valueOf(daoObjectList.size()));
				for (DBObject refDet : daoObjectList) {
					ref = new com.hp.cdsplus.bindings.output.schema.cadence.Ref();
					ref.setHref(refDet.get("_id").toString());
					ref.setType(ServiceConstants.xmlElementType);
					ref.setSize(String.valueOf(refDet.get("count")));
					refs.add(ref);
				}
				request.getRef().addAll(refs);
			} else {
				request.setCount("0");
			}
		}
		catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
		return request;
	}




	/**
	 * These is method is get subscription list for any content type i.e.
	 * library ,soar, etc.
	 * 
	 * @param options
	 * @return Response Object
	 */
	@Override
	public Object getSubcriptionList(Options options)
			throws ApplicationException, WebApplicationException {
		List<Ref> refs = new ArrayList<Ref>();
		TreeSet<String> subscriptionList = null;
		Result result = new Result();
		String hrefLink  = options.getContentType() + "/";
		result.setBase(options.getBaseUri() == null ? "null" : options.getBaseUri());
		try {
			
			Ref ref = null;
			if(options.getSubscription() != null && options.getSubscription().equals("stylesheet")){
				hrefLink = options.getContentType() + "/" + "stylesheet" + "/";
			}
			//SMO: User Story#<8094> : else condition has been commented out to remove content from subscription classes.
			subscriptionList = contentDAO.getSubscriptionList(options);
			if(subscriptionList==null)
			    subscriptionList = new TreeSet<String>();
			if(!ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
			{
			    subscriptionList.add("content");
			}
			
			if (subscriptionList != null && !subscriptionList.isEmpty()) {
				for (String refDet : subscriptionList) {
					ref = new Ref();
					ref.setHref(hrefLink + refDet);
					ref.setType(ServiceConstants.xmlElementType);
					refs.add(ref);
				}
			}
			
			//Add fastxml for MS and support content types
			//commenting this lines as fastxml is added as a subscription to support, MS subscriptions.
			/*if(options.getContentType().equalsIgnoreCase("marketingstandard") || options.getContentType().equalsIgnoreCase("support")){
				ref = new Ref();
				ref.setHref(options.getContentType() + "/fastxml");
				ref.setType(ServiceConstants.xmlElementType);
				refs.add(ref);
				
			}*/
			//adding default 'content' folder 
			result.getRef().addAll(refs);
			result.setCount(String.valueOf(subscriptionList.size()));
			return result;
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}
	}

	/**
	 * These method is used get document for any subscription type.. based
	 * following details
	 * 
	 * @param serviceDelegateBO
	 * @return response object.
	 */

	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException{
		
		if(options.getSubscription() != null && options.getSubscription().equals("stylesheet")){
			return this.getSubcriptionList(options);
		}
		
		DBCursor docList = null;          
		Ref ref = null;
		ArrayList<Ref> refs = new ArrayList<Ref>();
		Result result = new Result();
		try {
			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			String urlLink = options.getContentType() + "/" + subscription
					+ "/";

			if (!("content".equals(subscription)) && !validateSubcsription(options, subscription) && !validateFastxmlSubscription(options, subscription)){

				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			}else{
				docList = contentDAO.getLiveDocumentList(options);
			}

			validateDocumentCount(docList,options);
			if(options.isReverse()){
				docList.sort(new BasicDBObject("lastModified",1));
			}

			result.setBase(options.getBaseUri());

			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new Ref();
					ref.setEventType(docObject.get(ServiceConstants.eventType)==null ? "update":docObject.get(ServiceConstants.eventType).toString());
					ref.setHasAttachments(docObject.get(ServiceConstants.hasAttachments)==null ? null:docObject.get(ServiceConstants.hasAttachments).toString());
					ref.setLastModified(docObject.get(ServiceConstants.lastModified)==null ? null:docObject.get(ServiceConstants.lastModified).toString());
					if (docObject.get(ServiceConstants.deleteSubs) != null
							&& ((BasicDBList) docObject
									.get(ServiceConstants.deleteSubs))
									.contains(options.getSubscription())) {
						ref.setEventType("delete");

					} else {
						ref.setEventType(docObject
								.get(ServiceConstants.eventType) == null ? "update"
								: docObject.get(ServiceConstants.eventType)
										.toString());
					}
					ref.setHasAttachments(docObject
							.get(ServiceConstants.hasAttachments) == null ? null
							: docObject.get(ServiceConstants.hasAttachments)
									.toString());
					ref.setLastModified(docObject
							.get(ServiceConstants.lastModified) == null ? null
							: docObject.get(ServiceConstants.lastModified)
									.toString());
					ref.setPriority(docObject.get(ServiceConstants.priority) == null ? "4"
							: docObject.get(ServiceConstants.priority)
									.toString());
					if (ref.getEventType() != null) {
						if (ref.getEventType().equalsIgnoreCase("delete"))
							ref.setStatus(ref.getEventType());
					}
					ref.setType(ServiceConstants.xmlElementType);
					ref.setHref(urlLink + docObject.get(ServiceConstants.id));
					refs.add(ref);
				}
				result.getRef().addAll(refs);
				result.setCount(String.valueOf(docList.size()));				
			}else
				result.setCount("0");
			
			result.setConsidered("0");
			
			return result;
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		} 
	}

	protected  Ref iterateSubscriptionResult(Ref ref, DBObject docObject,
			String urlLink, boolean flag, String filter, String filterValue) {

		if (flag) {

			if (docObject.get(filter) != null) {
				if (docObject.get(filter).toString()
						.equalsIgnoreCase(filterValue)) {
					ref = new Ref();

					if (docObject.get(ServiceConstants.eventType) != null)
						ref.setEventType(docObject.get(
								ServiceConstants.eventType).toString());
					if(ref.getEventType().equals("delete")){
						ref.setStatus(ref.getEventType());
					}
					if (docObject.get(ServiceConstants.priority) != null)
						ref.setPriority(docObject
								.get(ServiceConstants.priority).toString());
					if (docObject.get(ServiceConstants.id) != null)
						ref.setHref(urlLink
								+ docObject.get(ServiceConstants.id));
					if (docObject.get(ServiceConstants.hasAttachments) != null)
						ref.setHasAttachments(docObject.get(
								ServiceConstants.hasAttachments).toString());
					if (docObject.get(ServiceConstants.lastModified) != null)
						ref.setLastModified(docObject.get(
								ServiceConstants.lastModified).toString());
					ref.setType(ServiceConstants.xmlElementType);

				}
			}

		} else {

			ref = new Ref();
			if (docObject.get(ServiceConstants.eventType) != null)
				ref.setEventType(docObject.get(ServiceConstants.eventType)
						.toString());
			if(ref.getEventType().equals("delete")){
				ref.setStatus(ref.getEventType());
			}
			if (docObject.get(ServiceConstants.priority) != null)
				ref.setPriority(docObject.get(ServiceConstants.priority)
						.toString());
			if (docObject.get(ServiceConstants.id) != null)
				ref.setHref(urlLink + docObject.get(ServiceConstants.id));

			if (docObject.get(ServiceConstants.hasAttachments) != null)
				ref.setHasAttachments(docObject.get(
						ServiceConstants.hasAttachments).toString());
			if (docObject.get(ServiceConstants.lastModified) != null)
				ref.setLastModified(docObject
						.get(ServiceConstants.lastModified).toString());
			ref.setType(ServiceConstants.xmlElementType);


		}

		return ref;

	}

	/**
	 * These method is parse the single document using JAX-B conversion
	 * 
	 * @param options
	 * @return response
	 * @throws ConversionUtilsException 
	 */
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

		DBObject document;
		Object result =null;
		try {
			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			if (!validateSubcsription(options, subscription))
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			
			if(options.getSubscription() == null || "".equals(options.getSubscription())){
				DBObject displayFields = options.getDisplayFields();
				if(displayFields == null){
					displayFields = new BasicDBObject();
				}
				if(options.getContentType().equalsIgnoreCase("soar")){
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_types",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_support_categories",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_support_subcategories",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_marketing_categories",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_marketing_subcategories",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_big_series",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_series",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_names",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_lines",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.product_number_names",0);
					displayFields.put("soar-software-feed.collection.software-items.software-item.products-supported.support_name_oids",0);
					displayFields.put(
							"soar-software-feed.collection.company_info", 0);
				}else{
					displayFields.put("document.product_types", 0);
					displayFields.put("document.product_support_categories", 0);
					displayFields.put("document.product_support_subcategories", 0);
					displayFields.put("document.product_marketing_categories" ,0);
					displayFields.put("document.product_marketing_subcategories",0 );
					displayFields.put("document.product_big_series", 0);
					displayFields.put("document.product_series",0);
					displayFields.put("document.product_names", 0);
					displayFields.put("document.product_numbers",0);
					
					displayFields.put("document.product_lines", 0);
					displayFields.put("document.product_number_names" ,0);
					displayFields.put("document.support_name_oids", 0 );
					displayFields.put("document.company_info", 0);
				}
				options.setDisplayFields(displayFields);
			}
			
			if(options.getLastModified() > 0L){
				document = contentDAO.getHistoryMetadata(options);
			}else {
				document = contentDAO.getLiveMetadata(options);	
			}
			
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
			
			if(!subscription.equals("content")){
				//System.out.println(" i am in side content check");
				BasicDBObject documentdbo=(BasicDBObject)document.get("document");
				if(documentdbo!=null && !documentdbo.equals("") ){
					BasicDBObject file_nameDbo= (BasicDBObject)documentdbo.get("file_name");
					if(file_nameDbo!=null && !file_nameDbo.equals("")){
						BasicDBObject projectrefDbo = (BasicDBObject)file_nameDbo.get("projectref");
						String xlinkhref= (String)projectrefDbo.get("@xlink/href");
						if(!subscription.equals("content")){
							String smartfolder= configDAO.getSubscriptionSmartfolder(options.getContentType(), subscription);
							//System.out.println(" smartfolder -->"+smartfolder);
							if(smartfolder!=null && !"content".equals(smartfolder)){
								//System.out.println(" smartfolder not null and i am replacing!!!");
								String [] xlinkhreflink= xlinkhref.split("/");
								if(xlinkhreflink.length==3){
									projectrefDbo.append("@xlink/href", xlinkhreflink[0]+"/"+smartfolder+"/"+xlinkhreflink[2]);
								}
							}
						}
					}
				}
			}

			DBObject dbObject=null;
			if(document.get("document")!=null){
				dbObject=new BasicDBObject("document",document.get("document"));
			}
			else if(document.get("soar-software-feed")!=null){
				dbObject=new BasicDBObject("soar-software-feed",document.get("soar-software-feed"));
			}
			else if(document.get("region")!=null){
				dbObject=new BasicDBObject("region",document.get("region"));
			}
			else{
				throw new ApplicationException(options.getDocid()+" "+ServiceConstants.errorMsg_doesnt_exist);
			}
			
			Boolean isPartitioned = (document.get("isPartitioned")!=null && Boolean.valueOf(document.get("isPartitioned").toString()));
			
			if(options.getSubscription() != null || !"".equals(options.getSubscription())){
				handleHierarchyExpansions(options , dbObject , isPartitioned);
			}    

			mergeLargeData(document, dbObject);
			
			String dbName = options.getContentType();

			String bindingClassName = ConfigurationManager.getInstance()
					.getMappingValue(dbName, "javaPackageName")
					+ "."
					+ ConfigurationManager.getInstance().getMappingValue(
							dbName, "javaClassName");

			result = conversion.convertJSONtoObject(dbObject.toString(), bindingClassName);

		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}catch(ConversionUtilsException e){
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return result;

	}
	
	public InputStream getDocumentContentMetaData(Options options) throws ApplicationException, WebApplicationException {

		try {
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
			GridFS gridFS = new GridFS(db, "content");
			DBObject query = new BasicDBObject("metadata.docid",options.getDocid()).append("metadata.fileName", options.getDocid()+".xml");

			GridFSDBFile gridFSDBFileMetadata = gridFS.findOne(query);
			if (gridFSDBFileMetadata == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ options.getDocid() + " "
								+ ServiceConstants.errorMsg_isInvalid);
			}
			return gridFSDBFileMetadata.getInputStream();

		} catch (MongoUtilsException mue) {			
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);

		}
	}
	
	public InputStream getFASTXMLDocument(Options options) throws ApplicationException, WebApplicationException {
		
		List<GridFSDBFile> fastXMLFileList;
		GridFSDBFile gridFSDBFile;
		
		try {
		
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		
		if (!validateFastxmlSubscription(options, subscription))
			throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry +" "+ subscription
					+ "  "+ServiceConstants.errorMsg_doesnt_exist);
		
			fastXMLFileList = contentDAO.getFASTXMLFile(options);

			if (fastXMLFileList.isEmpty()) {
				throw new ApplicationException(options.getDocid()
						+ " does not exists in "+ options.getContentType()+"'s "+options.getSubscription());
			}
			gridFSDBFile = (GridFSDBFile) fastXMLFileList.iterator().next();

		} catch (MongoUtilsException e) {			
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		}catch(MongoException e){
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

		return gridFSDBFile.getInputStream();
	}



	private void handleHierarchyExpansions(Options options, DBObject record , Boolean isPartitioned) {
		if(  options.getSubscription() != null && 
			(options.getSubscription().equalsIgnoreCase("astro2") || options.getSubscription().equals("astro2_201") || options.getSubscription().equals("astro2_"))  &&
			!options.getContentType().equalsIgnoreCase("soar") && !options.getContentType().equalsIgnoreCase("contentfeedback") ){
			
			handleProductHierarchyForAstroSub(options,record,isPartitioned);
			
		}else{
			if(options.getContentType().equalsIgnoreCase("soar"))
				handleSoarProductHierarchy(options,record,isPartitioned);
			else handleProductHierarchy(options,record,isPartitioned);
		}

	}
	
	private void handleProductHierarchyForAstroSub(Options options,DBObject record,Boolean isPartitioned) {
		
		DBObject document = (DBObject) record.removeField(CONC_DOC_ROOT);
		//addProductTag(document); // commented out this method call as it should be called after merging Product Info from GridFS
		if(isPartitioned){
			DBObject partition_object = getPartitionObject(options);
			mergeProductMetadata(options,document,  partition_object);
		}
		addProductTag(document); // calling after merging Product Metadata
		Set<String> listOfHier = getListOfHier();
		BasicDBObject data = new BasicDBObject();
		if(document != null){
			Set<String> keySet = document.keySet();
			if(keySet != null){
				for(String key : keySet){
					if(listOfHier.contains(key)){
						if(data.isEmpty())
						    data.put(key,document.get(key));	
						else
							data.append(key,document.get(key));
					}
				}
			}
		}		
		document.put("products", data);
		record.put(CONC_DOC_ROOT, document);
	}

	//instead of <product_types>18964,18972</product_types>
	//Display like - <product_types><product>18964</product><product>18972</product></product_types>
	private void addProductTag(DBObject document) {
		convertStringToList(document,SUB_DOC_PRODUCT_TYPE);
		convertStringToList(document,SUB_DOC_SUPPORT_CATEGORY);
		convertStringToList(document,SUB_DOC_SUPPORT_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_CATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_PRODUCT_BIGSERIES);
		convertStringToList(document,SUB_DOC_PRODUCT_SERIES);
		convertStringToList(document,SUB_DOC_PRODUCT_NAME);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER_NAME);           
		convertStringToList(document,SUB_DOC_SUPPORT_NAME);
		// ADDED for CR 584
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_TYPE);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_CATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_MARKETING_CATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_MARKETING_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_BIGSERIES);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_SERIES);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NAME);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NUMBER);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NUMBER_NAME);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_NAME_OIDS);
	}

	private Set<String> getListOfHier(){
		Set<String> set = new HashSet<String>();
		set.add(SUB_DOC_PRODUCT_NAME);
		set.add(SUB_DOC_PRODUCT_NUMBER);
		set.add(SUB_DOC_PRODUCT_NUMBER_NAME); 
		set.add(SUB_DOC_PRODUCT_LINE_CODE);
		set.add(SUB_DOC_PRODUCT_TYPE);
		set.add(SUB_DOC_SUPPORT_CATEGORY);
		set.add(SUB_DOC_SUPPORT_SUBCATEGORY);
		set.add(SUB_DOC_MARKETING_CATEGORY);
		set.add(SUB_DOC_MARKETING_SUBCATEGORY);
		set.add(SUB_DOC_PRODUCT_BIGSERIES);
		set.add(SUB_DOC_PRODUCT_SERIES);		          
		set.add(SUB_DOC_SUPPORT_NAME);
		return set;
	}

	protected void handleProductHierarchy(Options options,DBObject record,Boolean isPartitioned) {
		DBObject document = (DBObject) record.removeField(CONC_DOC_ROOT);
		if(isPartitioned){
			DBObject partition_object = getPartitionObject(options);
			mergeProductMetadata(options,document,  partition_object);
		}             
		convertStringToList(document,SUB_DOC_PRODUCT_TYPE);
		convertStringToList(document,SUB_DOC_SUPPORT_CATEGORY);
		convertStringToList(document,SUB_DOC_SUPPORT_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_CATEGORY);
		convertStringToList(document,SUB_DOC_MARKETING_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_PRODUCT_BIGSERIES);
		convertStringToList(document,SUB_DOC_PRODUCT_SERIES);
		convertStringToList(document,SUB_DOC_PRODUCT_NAME);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER);
		convertStringToList(document,SUB_DOC_PRODUCT_NUMBER_NAME);           
		convertStringToList(document,SUB_DOC_SUPPORT_NAME);
		
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_TYPE);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_CATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_MARKETING_CATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_MARKETING_SUBCATEGORY);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_BIGSERIES);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_SERIES);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NAME);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NUMBER);
		convertStringToList(document,SUB_DOC_FAQ_PRODUCT_NUMBER_NAME);
		convertStringToList(document,SUB_DOC_FAQ_SUPPORT_NAME_OIDS);
		
		
		record.put(CONC_DOC_ROOT, document);
	}
	//SMO Change : CR-10349 HierarchyExpansinon DB call to populate hierExpObj.
	private DBObject populateHierarchyExpansionObject(Options options) 
	{
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
		}
		return hierExpObj;
	}
	
	private void handleSoarProductHierarchy(Options options,DBObject record,Boolean isPartitioned) {
		
		//SMO Change : CR-10349 Method call to populate HierarchyExpansion Object, to be used in mergeSoarProductMetadata and insertProductNumberList 
		 DBObject hierExpObj = populateHierarchyExpansionObject(options);
		if(hierExpObj == null)
			return;
		
		BasicDBList new_soar_items = new BasicDBList();
		
		DBObject soar_software_feed_doc = getObject(record, SOAR_SOFTWARE_FEED_DOC);
		
		if(soar_software_feed_doc == null)
			return;

		DBObject soar_collection =getObject(soar_software_feed_doc, SOAR_DOC_COLLECTION);
		
		if(soar_collection == null)
			return;
		//CR 9943 : Changes to get company info from collection
		String company_info =(String)soar_collection.get(COMPANY_INFO);
		if(StringUtils.isEmpty(company_info))
			company_info=COMPANY_INFO_IS_BLANK;

		DBObject soar_items = getObject(soar_collection, SOAR_SOFTWARE_ITEMS_DOC);
		if(soar_items== null){
			return;
		}
		BasicDBList soar_item_list = getList(soar_items.get(SOAR_SOFTWARE_ITEM_DOC));


		if(soar_item_list == null || soar_item_list.size() == 0)
			return;
		
		Iterator<?> itr = soar_item_list.iterator();
// get product content
		
		DBObject partitioned_object = null;
		
		if(isPartitioned){
			partitioned_object = getPartitionObject(options);
		}
		
		
		while(itr.hasNext()){
			DBObject soar_item = (DBObject) itr.next();
		
			if(soar_item == null) 
				continue;
		
			DBObject products_supported = getObject(soar_item,SOAR_PRODUCTS_SUPPORTED_DOC);

			if(products_supported == null) 
				continue;
			
			String item_id = (String) soar_item.get("@item-ID");

			if(isPartitioned && partitioned_object != null && partitioned_object.containsField(item_id)){
				mergeSoarProductMetadata(options,products_supported, getObject(partitioned_object, item_id),hierExpObj);
			}      

			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_TYPES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_MARKETING_CATEGORIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_MARKETING_SUBCATEGORIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_SUPPORT_CATEGORIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_SUPPORT_SUBCATEGORIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_BIG_SERIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_SERIES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_NAMES);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_NUMBERS);
			convertStringToList(products_supported, SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES);
			convertStringToList(products_supported, SOAR_SUB_DOC_SUPPORT_NAME_OID);
			
			// now introducte the product number list in place of product array 
			try {
				// SMO CR 9943 : Changes to add company_info in method call
				insertProductNumberList(products_supported,options,company_info,hierExpObj);
			} catch (MongoUtilsException e) {
				e.printStackTrace();
			}
			soar_item.put(SOAR_PRODUCTS_SUPPORTED_DOC, products_supported);
			new_soar_items.add(soar_item);
		}
		if(new_soar_items.size()>0){
			soar_items.put(SOAR_SOFTWARE_ITEM_DOC, new_soar_items);
			soar_collection.put(SOAR_SOFTWARE_ITEMS_DOC, soar_items);
			
			//SMO change - CR 10349 : removing company_info from document based on company_info flag
			String CIFlag = hierExpObj.get("soar-software-feed.collection.company_info").toString();
			if(StringUtils.isEmpty(CIFlag))
				CIFlag = "";
			if(!Boolean.valueOf(CIFlag))
				soar_collection.removeField(COMPANY_INFO);
			//change ends
			soar_software_feed_doc.put(SOAR_DOC_COLLECTION, soar_collection);

			record.put(SOAR_SOFTWARE_FEED_DOC, soar_software_feed_doc);
		}
	}
	// SMO CR 9943 : Changes to add company_info in method signature
	// SMO Change : CR 10349 - Signature changed to add hierarchy Expansion Object as an argument instead of making a db call
	private void insertProductNumberList(DBObject products_supported, Options options, String company_info , DBObject hierExpObj) throws MongoUtilsException {
		if(options.getSubscription() == null || "".equals(options.getSubscription())){
			return;
		}
	
		BasicDBList product_number_list = new BasicDBList();
		if(hierExpObj == null){
			//System.out.println("hierarchy expansions filter query returned null");
			return;
		}
		Object prod_num_list_val = hierExpObj.get("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list");
		if(prod_num_list_val == null){
			//System.out.println("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list = null");
			return;
		}
		Boolean isProdNumListEnabled = (Boolean) prod_num_list_val;
		if(!isProdNumListEnabled){
			//System.out.println("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list = false");
			return;
		}
		/*
		 *  "#" : "310381-B21", "@deleted-product" : "No","@projectoid" : "productmaster/content/225028", "@type" : "PMN"
		 */
		//List<?> product_list =  (List<?>) ((DBObject)products_supported.removeField(SOAR_SUB_DOC_PRODUCT_NUMBERS)).get("product");
		DBObject product_numbers = (DBObject)products_supported.removeField(SOAR_SUB_DOC_PRODUCT_NUMBERS);
		List<?> product_list = null;
		if(product_numbers==null) 
			return ;
		product_list =  (List<?>) product_numbers.get("product");
		// SMO CR 9943 : Changes to add company_info in method signature	
		HashMap<String, String> productTextMap = getProductNameTag(product_list, options,company_info);
		if(product_list.size() > 0){
			for(int index = 0; index< product_list.size(); index++){
				String productOid = (String) product_list.get(index);
				if(productOid != null){
					String productText = productTextMap.get(productOid);
					// SMO CHANGE : CR-10349
					BasicDBObject productObj = new BasicDBObject("@deleted-product" , "No")
					.append("@projectoid" , "productmaster/"+company_info.toLowerCase()+"content/"+productOid)
					.append("@type", "PMN");
					if (productText!=null){
						productObj.append("#", productText);
					}
					product_number_list.add(productObj);
				}
				
			}
			products_supported.put("product", product_number_list); 
		}
	}



	
	private HashMap<String, String> getProductNameTag(List<?> product_list, Options options, String company_info) throws WebApplicationException {
		HashMap<String, String> productNameMap = new HashMap<String, String>();
		try{			
			//PRODUCT_NUMBER_NAME
			String dbName = ConfigurationManager.getInstance().getMappingValue("productmaster", "mongoDB");
			if(dbName == null){
				dbName = options.getContentType();
			}
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			// SMO CR 9943 
			String hierarchyCollectionName = ConfigurationManager.getInstance().getMappingValue("productmaster",company_info.toLowerCase()+"hierarchyCollection");
			DBCollection hierarchyCollection = db.getCollection(hierarchyCollectionName);
			DBObject query = new BasicDBObject("_id",new BasicDBObject("$in",product_list));
			DBObject fields = new BasicDBObject("PRODUCT_NUMBER_NAME",1);
			DBCursor cursor = hierarchyCollection.find(query,fields);
			for(DBObject record : cursor){
				String prod_number_name = (String) record.get("PRODUCT_NUMBER_NAME");
				String prod_number = (String) record.get("_id");
				if(prod_number != null && prod_number_name != null && !"".equals(prod_number) && ! "".equals(prod_number_name)){
					productNameMap.put(prod_number, prod_number_name);
				} 
			}
		}catch (MongoUtilsException e) {
			throw new WebApplicationException(500);
		}
		return productNameMap;
	}





	protected DBObject getPartitionObject(Options options) {
		GridFSDBFile gridFSDBFile = null;
		InputStream inputStream = null;
		StringWriter writer = new StringWriter();
		String gridFSData = null;
		DBObject dbo = null;

		try {
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
			GridFS gfs = new GridFS(db, "product_content");

			List<GridFSDBFile> gridFSDBFileList = gfs.find(new BasicDBObject().append("filename",options.getDocid()));
			if(gridFSDBFileList.iterator().hasNext()){
				gridFSDBFile = gridFSDBFileList.iterator().next();
				inputStream = gridFSDBFile.getInputStream(); 
				IOUtils.copy(inputStream, writer, "UTF-8");
				gridFSData = writer.toString();
				dbo = (DBObject) JSON.parse(gridFSData);
			}
		}catch (MongoUtilsException e) {
			e.printStackTrace();
			return null;
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
		return dbo;
	}




	public BasicDBList getList(Object object){
		BasicDBList returnList = new BasicDBList();
		if(object instanceof List){
			return (BasicDBList) object;
		}else if(object instanceof DBObject){
			returnList.add(object);
		}
		return returnList;
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
		}
		
		DBObject product = getObject(partition_obj, SUB_DOC_PRODUCTS);
		if(product!=null)
		document.put(SUB_DOC_PRODUCTS, product);
	
		product = getObject(partition_obj, SUB_DOC_PRODUCT_NUMBER);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NUMBER)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NUMBER)!=false)
		document.put(SUB_DOC_PRODUCT_NUMBER, product);

		product = getObject(partition_obj, SUB_DOC_PRODUCT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NAME)!=false)
		document.put(SUB_DOC_PRODUCT_NAME, product);
		
		product =  getObject(partition_obj, SUB_DOC_PRODUCT_NUMBER_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NUMBER_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_NUMBER_NAME)!=false)
		document.put(SUB_DOC_PRODUCT_NUMBER_NAME, product);

		product = getObject(partition_obj, SUB_DOC_SUPPORT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_NAME)!=false)
		document.put(SUB_DOC_SUPPORT_NAME, product);
		
		product = getObject(partition_obj, SUB_DOC_PRODUCT_SERIES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_SERIES)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_SERIES)!=false)
		document.put(SUB_DOC_PRODUCT_SERIES, product); 
		
		product = getObject(partition_obj, SUB_DOC_PRODUCT_BIGSERIES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_BIGSERIES)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_BIGSERIES)!=false)
		document.put(SUB_DOC_PRODUCT_BIGSERIES, product);
		
		product = getObject(partition_obj, SUB_DOC_SUPPORT_CATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_CATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_CATEGORY)!=false)
		document.put(SUB_DOC_SUPPORT_CATEGORY, product);
		
		product = getObject(partition_obj,SUB_DOC_SUPPORT_SUBCATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_SUBCATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_SUPPORT_SUBCATEGORY)!=false)
		document.put(SUB_DOC_SUPPORT_SUBCATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_MARKETING_CATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_MARKETING_CATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_MARKETING_CATEGORY)!=false)
		document.put(SUB_DOC_MARKETING_CATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_MARKETING_SUBCATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_MARKETING_SUBCATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_MARKETING_SUBCATEGORY)!=false)
		document.put(SUB_DOC_MARKETING_SUBCATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_PRODUCT_TYPE);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_TYPE)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_PRODUCT_TYPE)!=false)
		document.put(SUB_DOC_PRODUCT_TYPE, product);
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCTS);
		if(product!=null)
		document.put(SUB_DOC_FAQ_PRODUCTS, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NAME)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_NAME, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_SERIES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_SERIES)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_SERIES)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_SERIES, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_BIGSERIES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_BIGSERIES)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_BIGSERIES)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_BIGSERIES, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_TYPE);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_TYPE)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_TYPE)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_TYPE, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NAME)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_NAME, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_NUMBER);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NUMBER)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NUMBER)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_NUMBER, product); 	
		
		product = getObject(partition_obj, SUB_DOC_FAQ_PRODUCT_NUMBER_NAME);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NUMBER_NAME)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_PRODUCT_NUMBER_NAME)!=false)
		document.put(SUB_DOC_FAQ_PRODUCT_NUMBER_NAME, product); 
		
		product = getObject(partition_obj, SUB_DOC_FAQ_SUPPORT_CATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_CATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_CATEGORY)!=false)
		document.put(SUB_DOC_FAQ_SUPPORT_CATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_FAQ_SUPPORT_SUBCATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_SUBCATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_SUBCATEGORY)!=false)
		document.put(SUB_DOC_FAQ_SUPPORT_SUBCATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_FAQ_MARKETING_CATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_MARKETING_CATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_MARKETING_CATEGORY)!=false)
		document.put(SUB_DOC_FAQ_MARKETING_CATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_FAQ_MARKETING_SUBCATEGORY);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_MARKETING_SUBCATEGORY)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_MARKETING_SUBCATEGORY)!=false)
		document.put(SUB_DOC_FAQ_MARKETING_SUBCATEGORY, product);
		
		product = getObject(partition_obj, SUB_DOC_FAQ_SUPPORT_NAME_OIDS);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_NAME_OIDS)!=null && (Boolean)hierExpObj.get(SUB_DOC_PREFIX+SUB_DOC_FAQ_SUPPORT_NAME_OIDS)!=false)
		document.put(SUB_DOC_FAQ_SUPPORT_NAME_OIDS, product);
		
	}
	//SMO Change : CR 10349 - Signature changed to add hierarchy Expansion Object as an argument instead of making a db call
	protected void mergeSoarProductMetadata(Options options,DBObject document, DBObject partition_obj, DBObject hierExpObj ) {	
		
		
		DBObject product = getObject(partition_obj, SOAR_SUB_DOC_PRODUCT_NUMBERS);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NUMBERS)!=null && (Boolean)hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NUMBERS)!=false)
		document.put(SOAR_SUB_DOC_PRODUCT_NUMBERS, product);

		product = getObject(partition_obj, SOAR_SUB_DOC_PRODUCT_NAMES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NAMES)!=null && (Boolean)hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NAMES)!=false)
		document.put(SOAR_SUB_DOC_PRODUCT_NAMES, product);
		
		product =  getObject(partition_obj, SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES)!=null && (Boolean)hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES)!=false)
		document.put(SOAR_SUB_DOC_PRODUCT_NUMBER_NAMES, product);

		product = getObject(partition_obj, SOAR_SUB_DOC_SUPPORT_NAME_OID);
		if(product!=null && hierExpObj!=null && hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_SUPPORT_NAME_OID)!=null && (Boolean)hierExpObj.get(SOAR_SUB_DOC_PREFIX+SOAR_SUB_DOC_SUPPORT_NAME_OID)!=false)
		document.put(SOAR_SUB_DOC_SUPPORT_NAME_OID, product);			
	}
	
	protected void convertStringToList(DBObject document, String field) {
		if(document == null || !document.containsField(field))
			return;

		DBObject obj = (DBObject) document.get(field);
		if(obj == null)
			return;
		Object product = obj.get("product");

		if(product == null){
			return;
		}

		if(product instanceof List)
			return;

		if (product instanceof String){
			String str = (String) product;
			if(str !=null && !"".equals(str) && str.contains(",")){
				obj.put("product",  Arrays.asList(str.split(",")));

			}
			else {
				obj.put("product",  Arrays.asList(new String[] {str}));
			}
			document.put(field, obj);
		}
	}

	/**
	 * These method is get the attachment like JPG,PNG,etc..
	 * 
	 * @param options
	 * @return response
	 */
	@Override
	public InputStream getDocumentAttachment(Options options) throws ApplicationException, WebApplicationException{
		/*
		 * 1. call DAO getAttachment returns List<GridFSDBFile> 2. return the
		 * attachment to browser.
		 */
		List<GridFSDBFile> attachmentList;
		GridFSDBFile gridFSDBFile;

		try {
			attachmentList = contentDAO.getAttachment(options);

			if (attachmentList.isEmpty()) {
				throw new ApplicationException(options.getAttachmentName()
						+ " does not exists/ associated with the document-"
						+ options.getDocid());
			}
			gridFSDBFile = (GridFSDBFile) attachmentList.iterator().next();

		} catch (MongoUtilsException e) {			
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		}catch(MongoException e){
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}


		return gridFSDBFile.getInputStream();

	}

	/**
	 * These method is get the attachmentlist like JPG,PNG,etc..
	 * 
	 * @param options
	 * @return response
	 */
	@Override
	public Object getDocumentAttachments(Options options) {

		/*
		 * 1. call DAO getAttachmentlist returns List<DBobject> 2. return the
		 * attachmentlist to browser as xml.
		 */
		List<GridFSDBFile> attachmentList;
		Result result = new Result();

		List<Ref> refs = new ArrayList<Ref>();
		Ref ref = null;
		result.setBase(options.getBaseUri() == null ? "null" : options
				.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {
			attachmentList = contentDAO.getAllAttachments(options);

			if (attachmentList.isEmpty()) {
				result.setCount("0");
			}

			if (attachmentList != null && !attachmentList.isEmpty()) {
				//result.setCount(String.valueOf(attachmentList.size()));
				int count = 0;
				for (GridFSDBFile gridFSDBFile : attachmentList) {
					if(gridFSDBFile != null && gridFSDBFile.getMetaData() != null && gridFSDBFile.getMetaData().get("fileName") != null && gridFSDBFile.getMetaData().get("docid") != null){
						
						String fileName = gridFSDBFile.getMetaData().get("fileName").toString();
						String docId = gridFSDBFile.getMetaData().get("docid").toString();
						//Fixed for 607
						//If attached xml file name is same as docID, then it is not considered as a attachment.
						if(docId != null && fileName != null &&  !((docId+".xml").equalsIgnoreCase(fileName))){
					
							ref = new Ref();
							ref.setHref(options.getContentType()
									+ "/"
									+ subscription
									+ "/"
									+ gridFSDBFile.getMetaData().get("docid")
									.toString()
									+ "/"
									+ gridFSDBFile.getMetaData().get("fileName")
									.toString());
							ref.setLastModified(gridFSDBFile.getMetaData()
									.get("lastModified").toString());
							ref.setType(ServiceConstants.xmlElementType);
							refs.add(ref);
							
							count++;
						}
					}
				}
				result.setCount(String.valueOf(count));
				result.getRef().addAll(refs);
			} else {
				result.setCount("0");
			}

		}

		catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}

		return result;

	}

	/**
	 * These method is get previous versions of the document..
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	public Object getExpandVersions(Options serviceDelegateBO) {

		com.hp.cdsplus.bindings.output.schema.expversions.Result result = new com.hp.cdsplus.bindings.output.schema.expversions.Result();
		com.hp.cdsplus.bindings.output.schema.expversions.Ref ref = null;
		result.setBase(serviceDelegateBO.getBaseUri());
		String subscription = serviceDelegateBO.getSubscription() == null ? "content"
				: serviceDelegateBO.getSubscription();
		String urlLink = serviceDelegateBO.getContentType() + "/"
				+ subscription + "/";

		try {
		/*	DBObject document = contentDAO.getLiveMetadata(serviceDelegateBO);

			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ serviceDelegateBO.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}
*/
			/*if (serviceDelegateBO.getDocid().equalsIgnoreCase(
					document.get(ServiceConstants.id).toString()))

			{*/
				List<DBObject> metadataList = new ArrayList<DBObject>();
				List<com.hp.cdsplus.bindings.output.schema.expversions.Ref> refLists = new ArrayList<com.hp.cdsplus.bindings.output.schema.expversions.Ref>();
				com.hp.cdsplus.bindings.output.schema.expversions.Ref refVersions = null;
				Versions versions = new Versions();
				// adding data from the revsions
				metadataList = contentDAO
						.getMetadataRevisionList(serviceDelegateBO);
		

						if (metadataList != null && !metadataList.isEmpty()) {
							DBObject document = metadataList.get(0);
							ref = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
							if (document.get(ServiceConstants.eventType) != null)
								ref.setEventType(document.get(ServiceConstants.eventType)
										.toString());
							if (document.get(ServiceConstants.priority) != null)
								ref.setPriority(new BigInteger(document.get(
										ServiceConstants.priority).toString()));
							if (document.get(ServiceConstants.documentId) != null)
								ref.setHref(urlLink + document.get(ServiceConstants.documentId));
							if (document.get(ServiceConstants.hasAttachments) != null)
								ref.setHasAttachments(Boolean.valueOf(document.get(
										ServiceConstants.hasAttachments).toString()));

							if (document.get(ServiceConstants.lastModified) != null)
								ref.setLastModified(new BigInteger(document.get(
										ServiceConstants.lastModified).toString()));
							ref.setType(ServiceConstants.xmlElementType);
							result.setCount(new BigInteger("1"));
							result.setConsidered(new BigInteger("0"));

						//CR 446 : Commented to exclude latest version from revisions	
						/*	for (DBObject metadataDbobjectList : metadataList) {*/
							
							for (int index = 1; index < metadataList.size(); index++) {
								
								DBObject metadataDbobjectList = metadataList.get(index);
								// adding for main version of the document
																
								refVersions = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
								if(metadataDbobjectList
										.get(ServiceConstants.eventType) == null ||metadataDbobjectList.get(
												ServiceConstants.lastModified)==null)
									continue;
								if (metadataDbobjectList
										.get(ServiceConstants.eventType) != null)
									refVersions.setEventType(metadataDbobjectList.get(
											ServiceConstants.eventType).toString());
								if (metadataDbobjectList.get(ServiceConstants.priority) != null)
									refVersions.setPriority(new BigInteger(
											metadataDbobjectList.get(
													ServiceConstants.priority)
													.toString()));
								if (metadataDbobjectList.get(ServiceConstants.documentId) != null)
									refVersions.setHref(urlLink
											+ metadataDbobjectList.get(ServiceConstants.documentId)
											+ "?"
											+ ServiceConstants.lastModified
											+ "="
													+ metadataDbobjectList.get(
															ServiceConstants.lastModified)
															.toString());
								if (metadataDbobjectList
										.get(ServiceConstants.hasAttachments) != null)
									refVersions.setHasAttachments(Boolean
											.valueOf(metadataDbobjectList.get(
													ServiceConstants.hasAttachments)
													.toString()));
								if (metadataDbobjectList
										.get(ServiceConstants.lastModified) != null)
									refVersions.setLastModified(new BigInteger(
											metadataDbobjectList.get(
													ServiceConstants.lastModified)
													.toString()));
								refVersions.setType(ServiceConstants.xmlElementType);
								refLists.add(refVersions);
				}
							versions.getRef().addAll(refLists);
							ref.setVersions(versions);
							result.setRef(ref);
						return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {			
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

	}

	/**
	 * These method is get document attachments ..
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	public Object getExpandAttachments(Options serviceDelegateBO) {

		com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
		result.setBase(serviceDelegateBO.getBaseUri());
		com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = null;
		String subscription = serviceDelegateBO.getSubscription() == null ? "content"
				: serviceDelegateBO.getSubscription();
		String urlLink = serviceDelegateBO.getContentType() + "/"
				+ subscription + "/";

		try {
			DBObject document = contentDAO.getLiveMetadata(serviceDelegateBO);
			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ serviceDelegateBO.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}
			if (serviceDelegateBO.getDocid().equalsIgnoreCase(
					document.get(ServiceConstants.id).toString()))

			{
				List<GridFSDBFile> attachmentList = new ArrayList<GridFSDBFile>();
				Attachments attachments = new Attachments();
				List<com.hp.cdsplus.bindings.output.schema.expattachment.Ref> refLists = new ArrayList<com.hp.cdsplus.bindings.output.schema.expattachment.Ref>();
				com.hp.cdsplus.bindings.output.schema.expattachment.Ref refAttachments = null;

				// adding for main version of the document
				ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
						if (document.get(ServiceConstants.eventType) != null)
							ref.setEventType(document.get(ServiceConstants.eventType)
									.toString());
						if (document.get(ServiceConstants.priority) != null)
							ref.setPriority(new BigInteger(document.get(
									ServiceConstants.priority).toString()));
						if (document.get(ServiceConstants.id) != null)
							ref.setHref(urlLink + document.get(ServiceConstants.id));
						if (document.get(ServiceConstants.hasAttachments) != null)
							ref.setHasAttachments(Boolean.valueOf(document.get(
									ServiceConstants.hasAttachments).toString()));
						if (document.get(ServiceConstants.lastModified) != null)
							ref.setLastModified(new BigInteger(document.get(
									ServiceConstants.lastModified).toString()));
						ref.setType(ServiceConstants.xmlElementType);

						// adding data from the attachments revsions
						attachmentList = contentDAO
								.getAllAttachments(serviceDelegateBO);

						if (attachmentList != null && !attachmentList.isEmpty()) {

							for (GridFSDBFile gridFSDBFile : attachmentList) {

								refAttachments = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
								if (gridFSDBFile.getMetaData().get(
										ServiceConstants.eventType) != null)

									refAttachments
									.setEventType(gridFSDBFile.getMetaData()
											.get(ServiceConstants.eventType)
											.toString());
								if (gridFSDBFile.getMetaData().get(
										ServiceConstants.priority) != null)

									refAttachments.setPriority(new BigInteger(
											gridFSDBFile.getMetaData()
											.get(ServiceConstants.priority)
											.toString()));
								if (gridFSDBFile.getMetaData().get("docid") != null)

									refAttachments.setHref(urlLink
											+ document.get(ServiceConstants.id)
											+ "/"
											+ gridFSDBFile.getMetaData()
											.get("fileName").toString());

								if (gridFSDBFile.getMetaData().get(
										ServiceConstants.hasAttachments) != null)
									refAttachments
									.setHasAttachments(Boolean
											.valueOf(gridFSDBFile
													.getMetaData()
													.get(ServiceConstants.hasAttachments)
													.toString()));
								if (gridFSDBFile.getMetaData().get(
										ServiceConstants.lastModified) != null)
									refAttachments.setLastModified(new BigInteger(
											gridFSDBFile.getMetaData()
											.get(ServiceConstants.lastModified)
											.toString()));
								refAttachments.setType(ServiceConstants.xmlElementType);
								refLists.add(refAttachments);

							}
							attachments.getRef().addAll(refLists);							
						}
						ref.setAttachments(attachments);
						result.setCount(new BigInteger("1"));
						result.setConsidered(new BigInteger("0"));
						result.setRef(ref);
						return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

	}

	@Override
	public Object getVersions(Options options) {
		return getGenericExpand(options);
	}

	/**
	 * These method is get generic expand details like
	 * expand=[version,attachment,false],versions=[true,false]
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	protected Object getGenericExpand(Options serviceDelegateBO) throws ApplicationException, WebApplicationException{

		com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
		com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = null;
		result.setBase(serviceDelegateBO.getBaseUri() == null ? "null"
				: serviceDelegateBO.getBaseUri());
		String subscription = serviceDelegateBO.getSubscription() == null ? "content"
				: serviceDelegateBO.getSubscription();
		String urlLink = serviceDelegateBO.getContentType() + "/"
				+ subscription + "/";

		try {
			DBObject document = contentDAO.getCacheMetadata(serviceDelegateBO);
			if (document == null || !serviceDelegateBO.getDocid().equalsIgnoreCase(
					document.get(ServiceConstants.id).toString())) { 
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
			else{
					ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
					if (document.get(ServiceConstants.eventType) != null)
						ref.setEventType(document.get(ServiceConstants.eventType)
								.toString());
					if (document.get(ServiceConstants.priority) != null)
						ref.setPriority(new BigInteger(document.get(
								ServiceConstants.priority).toString()));
					if (document.get(ServiceConstants.id) != null)
						ref.setHref(urlLink + document.get(ServiceConstants.id));
					if (document.get(ServiceConstants.hasAttachments) != null)
						ref.setHasAttachments(Boolean.valueOf(document.get(
								ServiceConstants.hasAttachments).toString()));
					if (document.get(ServiceConstants.lastModified) != null)
						ref.setLastModified(new BigInteger(document.get(
								ServiceConstants.lastModified).toString()));
					ref.setType(ServiceConstants.xmlElementType);
					result.setCount(new BigInteger("1"));
					result.setConsidered(new BigInteger("0"));
					result.setRef(ref);
					return result;				
			}
			
		} catch (OptionsException oe) {			
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {			
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {			
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

	}

	/**
	 * These method is validate weather the document is related to subscription
	 * 
	 * @param options
	 * @return response
	 */
	@Override
	public Object getTaskSubcheck(Options options) throws ApplicationException, WebApplicationException {
		com.hp.cdsplus.bindings.output.schema.subcheck.Result result = new com.hp.cdsplus.bindings.output.schema.subcheck.Result();
		result.setBase(options.getBaseUri() == null ? "null" : options.getBaseUri());
		DBObject document;

		if (options.getTask().equals(ServiceConstants.subCheck)) {
			try {	
				String subscription = options.getSubscription() == null ? "content": options.getSubscription();

				if(subscription.equalsIgnoreCase("content")){
					return getGenericExpand(options);
				}else{
					String contentType = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
					DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
					
					String collectionName =ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_CACHE_COLLECTION);
					DBCollection cacheCollection = db.getCollection(collectionName);
					DBObject query = new BasicDBObject("_id",options.getDocid());
					ArrayList<String> subscriptions = new ArrayList<String>();
					subscriptions.add(options.getSubscription());
					query.put("subscriptions",new BasicDBObject("$in",subscriptions));
					document = cacheCollection.findOne(query);
					if (document == null || document.get("eventType").toString().equalsIgnoreCase("delete")) {
						result.setSubCheck(false);
					}else{
						result.setSubCheck(true);
					}
					return result;
						
				}
				
			} catch (MongoUtilsException mue) {
				//throw new ApplicationException(mue.getMessage());
				throw new WebApplicationException(500);
			} catch (Exception e) {
				throw new ApplicationException(e.getMessage());
			}

		} else {

			return getGenericExpand(options);
		}

		
	}

	/**
	 * These method is get document query param like
	 * expand=[versions,attachments,false],versions=[true,false]
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	public Object getDocumentExpandDetails(Options serviceDelegateBO) {

		Object response = null;

		if (serviceDelegateBO.getExpand().equalsIgnoreCase(
				ServiceConstants.expand_versions)) {

			response = getExpandVersions(serviceDelegateBO);
		}

		else if (serviceDelegateBO.getExpand().equalsIgnoreCase(
				ServiceConstants.expand_attachments))
		{
			if(serviceDelegateBO.getDocid() == null)
				response = getExpandAttachmentsList(serviceDelegateBO);
			else
				response = getExpandAttachments(serviceDelegateBO);
			
		} else {
			response = getGenericExpand(serviceDelegateBO);
		}

		return response;
	}

	/**
	 * These method is get document query param like expand=true
	 * 
	 * @param serviceDelegateBO
	 * @return Object
	 */
	public Object getGenericExpandTrue(Options serviceDelegateBO) {
		Object obj = null;
		try {
			DBObject document = contentDAO.getLiveMetadata(serviceDelegateBO);

			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ serviceDelegateBO.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}
			/*document.removeField("_id");
                     document.removeField("eventType");
                     document.removeField("lastModified");
                     document.removeField("mime");
                     document.removeField("priority");
                     document.removeField("subscriptions");
                     document.removeField("hasAttachments");
                     document.removeField(ServiceConstants.processStatus);*/
			DBObject dbObject=null;
			if(document.get("document")!=null){
				dbObject=new BasicDBObject("document",document.get("document"));
			}
			else if(document.get("soar-software-feed")!=null){
				dbObject=new BasicDBObject("soar-software-feed",document.get("soar-software-feed"));
			}
			else if(document.get("region")!=null){
				dbObject=new BasicDBObject("region",document.get("region"));
			}
			else{
				throw new ApplicationException(serviceDelegateBO.getDocid()+" "+ServiceConstants.errorMsg_doesnt_exist);
			}
			Boolean isPartitioned = (document.get("isPartitioned")!=null && Boolean.valueOf(document.get("isPartitioned").toString()));
			
			handleHierarchyExpansions(serviceDelegateBO , dbObject, isPartitioned);
			
			String dbName = serviceDelegateBO.getContentType();

			String bindingClassName = ConfigurationManager.getInstance()
					.getMappingValue(dbName, "javaPackageName")
					+ "."
					+ ConfigurationManager.getInstance().getMappingValue(
							dbName, "javaClassName");

			obj = conversion.convertJSONtoObject(dbObject.toString(),
					bindingClassName);


		} catch (Exception exception) {
			throw new ApplicationException(exception.getMessage());
		}

		return obj;
	}


	public DBObject getDocument(Options serviceDelegateBO){

		DBObject document=null;
		try {
		    if(serviceDelegateBO.getSubscription() == null || "".equals(serviceDelegateBO.getSubscription()))
		    {
			DBObject displayFields = serviceDelegateBO.getDisplayFields();
			if(displayFields == null)
				displayFields = new BasicDBObject();
			if(serviceDelegateBO.getContentType().equalsIgnoreCase("soar"))
				displayFields.put("soar-software-feed.collection.company_info", 0);
			else
				displayFields.put("document.company_info", 0);
			serviceDelegateBO.setDisplayFields(displayFields);
		    }
			document = contentDAO.getLiveMetadata(serviceDelegateBO);
			
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
		return document;
	}

	/* (non-Javadoc)
	 * @see com.hp.cdsplus.web.contentservice.ContentService#getExpandDetails(com.hp.cdsplus.dao.Options)
	 */
	@Override
	public Object getExpandDetails(Options options) throws ApplicationException, WebApplicationException {

		return getDocumentExpandDetails(options);
	}


	/**
	 * @param options
	 * @param subscription
	 * @return
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 */
	protected boolean validateSubcsription(Options options,String subscription) throws MongoUtilsException, OptionsException{
		if("content".equals(subscription) || ConfigurationManager.getInstance().isValidSubscription(options.getContentType(), subscription)){
			return true;
		}      
		return false;
	}
	
	/**
	 * @param options
	 * @param subscription
	 * @return
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 */
	protected boolean validateFastxmlSubscription(Options options,String subscription) throws MongoUtilsException, OptionsException{
		if("fastxml".equals(subscription) && (options.getContentType().equalsIgnoreCase("marketingstandard") || options.getContentType().equalsIgnoreCase("support"))){
			return true;
		}      
		return false;
	}



	public String convertObjectToXml(Options options,Object obj){
		String documentXMl=null;
		try {
			String dbName = options.getContentType();

			String bindingClassName = ConfigurationManager.getInstance()
					.getMappingValue(dbName, "javaPackageName")
					+ "."
					+ ConfigurationManager.getInstance().getMappingValue(
							dbName, "javaClassName");

			documentXMl = conversion.convertObjecttoXML(obj, bindingClassName);
		} catch (ConversionUtilsException e) {

			throw new ApplicationException(e.getMessage());

		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}
		return documentXMl;
	}

	public String convertObjectToXml(String className,Object obj,Options options){
		String documentXMl=null;
		try {
			String dbName = options.getContentType();

			String bindingClassName = ConfigurationManager.getInstance()
					.getMappingValue(dbName, "javaPackageName")
					+ "."
					+ className;

			documentXMl = conversion.convertObjecttoXML(obj, bindingClassName);
		} catch (ConversionUtilsException e) {

			throw new ApplicationException(e.getMessage());

		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}
		return documentXMl;
	}

	public void validateDocumentCount(DBCursor cursor,Options options){  
		try {
			String recordCount=ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "maxRecordLimit");
			if(recordCount==null){
				recordCount="10000";
			}
			if(cursor.size()>Integer.parseInt(recordCount)){
				throw new ApplicationException("Max Records limit was exceded.Please Provide combination with any query paramters");
			}

		} catch (MongoUtilsException e) {
			throw new WebApplicationException(500);
		}
	}

	/**
	 * @param options
	 * @return
	 * @throws ApplicationException
	 */
	public Object getGenericExpandDetails(Options options) throws ApplicationException, WebApplicationException{
		return getGenericExpand(options);

	}

	/**
	 * Mehtod added to fix CR -431
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
	
	public String putDocumentMetadata(Options options) throws ApplicationException, WebApplicationException {
		return null;
	}
	
	public String putDocumentContent(Options options) throws ApplicationException, WebApplicationException {
		return null;
	}
	
	/**
	 * These method is get document attachments ..
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	public Object getExpandAttachmentsList(Options options) {

		com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result result = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result();
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content": options.getSubscription();
		String urlLink = options.getContentType() + "/"+ subscription + "/";
		DBCursor listOfDBObjects = null;
		try {
			
			if(options.getLimit() == 0) options.setLimit(20);
			else if(options.getLimit()>100)  options.setLimit(100);    	                     
			
			if(options.getContentType().endsWith("content")){
				BasicDBList docidList = refDataDAO.getDistinctDocumentList(options);				
				Iterable<DBObject> resultFileList = refDataDAO.getMapReduceWithHasAttachments(options, docidList);
				return getContentExpandAttachmentDetailsList(options, result,urlLink,resultFileList);
			}
			
			listOfDBObjects = contentDAO.getLiveDocumentList(options);
			validateDocumentCount(listOfDBObjects,options);
			if(options.isReverse()){
				listOfDBObjects.sort(new BasicDBObject("lastModified",1));
            }
			return getExpandAttachmentDetailsList(options, result,urlLink,listOfDBObjects);
		
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			throw new WebApplicationException(500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}
	}

	private Object getExpandAttachmentDetailsList(Options options,
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result result,
			String urlLink, DBCursor listOfDBObjects) throws MongoUtilsException,
			OptionsException {
		
		if (listOfDBObjects != null && !(listOfDBObjects.count() == 0)) {
						
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments attachments = null;
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref refAttachments = null;
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref ref = null;
			List<GridFSDBFile> attachmentList = new ArrayList<GridFSDBFile>();
			List<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref> prepareRefList = new ArrayList<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref>();
			List<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref> prepareRefAttacchmentList = null;
			
			
			for(DBObject document : listOfDBObjects){
								    
					options.setDocid(document.get("_id").toString());
					
					// adding for main version of the document
					ref = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref();
					
					if (document.get(ServiceConstants.eventType) != null)
						ref.setEventType(document.get(ServiceConstants.eventType).toString());
					
			    	if (document.get(ServiceConstants.priority) != null)
						ref.setPriority(new BigInteger(document.get(ServiceConstants.priority).toString()));
				
					if (document.get(ServiceConstants.id) != null)
						ref.setHref(urlLink + document.get(ServiceConstants.id));
					
					if (document.get(ServiceConstants.hasAttachments) != null)
						ref.setHasAttachments(Boolean.valueOf(document.get(ServiceConstants.hasAttachments).toString()));
					
					if (document.get(ServiceConstants.lastModified) != null)
						ref.setLastModified(new BigInteger(document.get(ServiceConstants.lastModified).toString()));
					
					ref.setType(ServiceConstants.xmlElementType);

					// adding data from the attachments revsions
					attachmentList = contentDAO.getAllAttachments(options);

					attachments = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments();
					if (attachmentList != null && !attachmentList.isEmpty()) {

						prepareRefAttacchmentList = new ArrayList<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref>();
						
						for (GridFSDBFile gridFSDBFile : attachmentList) {

							refAttachments = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref();
							/*	
							if (gridFSDBFile.getMetaData().get(ServiceConstants.eventType) != null)
								refAttachments.setEventType(gridFSDBFile.getMetaData().get(ServiceConstants.eventType).toString());
							
							if (gridFSDBFile.getMetaData().get(ServiceConstants.priority) != null)
								refAttachments.setPriority(new BigInteger(gridFSDBFile.getMetaData().get(ServiceConstants.priority).toString()));
							*/
							if (gridFSDBFile.getMetaData().get("docid") != null)
								refAttachments.setHref(urlLink+ document.get(ServiceConstants.id)+ "/"+ gridFSDBFile.getMetaData().get("fileName").toString());

							if (gridFSDBFile.getMetaData().get(ServiceConstants.hasAttachments) != null)
								refAttachments.setHasAttachments(Boolean.valueOf(gridFSDBFile.getMetaData().get(ServiceConstants.hasAttachments).toString()));
							
							if (gridFSDBFile.getMetaData().get(ServiceConstants.lastModified) != null)
								refAttachments.setLastModified(new BigInteger(gridFSDBFile.getMetaData().get(ServiceConstants.lastModified).toString()));
							
							refAttachments.setType(ServiceConstants.xmlElementType);
							prepareRefAttacchmentList.add(refAttachments);

						}
						attachments.getRef().addAll(prepareRefAttacchmentList);						
					}
					ref.setAttachments(attachments);
					prepareRefList.add(ref);					
				}
				result.setCount(new BigInteger(String.valueOf(prepareRefList.size())));
				result.getRef().addAll(prepareRefList);
				return result;
				
			}else {
				result.setCount(new BigInteger("0"));
				return result;
			}
	}

	private Object getContentExpandAttachmentDetailsList(Options options,
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result result,
			String urlLink, Iterable<DBObject> resultFileList) throws MongoUtilsException,
			OptionsException {
		
		Iterator<DBObject> iterator = resultFileList.iterator();
		
		if (resultFileList != null  && iterator!=null) {
						
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments attachments = null;
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref refAttachments = null;
			com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref ref = null;
			List<GridFSDBFile> attachmentList = new ArrayList<GridFSDBFile>();
			List<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref> prepareRefList = new ArrayList<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref>();
			List<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref> prepareRefAttacchmentList = null;
			
			BasicDBObject document = null;
			
			while (iterator.hasNext()){
				
				    document =  (BasicDBObject)iterator.next().get("value");
								    
					options.setDocid(((BasicDBObject)document.get("metadata")).getString("docid").toString());
					
					// adding for main version of the document
					ref = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref();
					
					ref.setEventType(((BasicDBObject)document.get("metadata")).getString("eventType"));
				
		    		ref.setPriority(new BigInteger(((BasicDBObject)document.get("metadata")).getString("priority")));
			
					ref.setHref(urlLink + ((BasicDBObject)document.get("metadata")).getString("docid"));
					
					ref.setHasAttachments(Boolean.valueOf(document.get("hasAttachment").toString()));
					
					ref.setLastModified(new BigInteger(((BasicDBObject)document.get("metadata")).getString("lastModified")));
					
					ref.setType(ServiceConstants.xmlElementType);

					// adding data from the attachments revsions
					attachmentList = contentDAO.getAllAttachments(options);

					attachments = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments();
					if (attachmentList != null && !attachmentList.isEmpty()) {

						prepareRefAttacchmentList = new ArrayList<com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref>();
						
						for (GridFSDBFile gridFSDBFile : attachmentList) {

							refAttachments = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref();
							/*	
							if (gridFSDBFile.getMetaData().get(ServiceConstants.eventType) != null)
								refAttachments.setEventType(gridFSDBFile.getMetaData().get(ServiceConstants.eventType).toString());
							
							if (gridFSDBFile.getMetaData().get(ServiceConstants.priority) != null)
								refAttachments.setPriority(new BigInteger(gridFSDBFile.getMetaData().get(ServiceConstants.priority).toString()));
							*/
							if (gridFSDBFile.getMetaData().get("docid") != null)
								refAttachments.setHref(urlLink+ document.get(ServiceConstants.id)+ "/"+ gridFSDBFile.getMetaData().get("fileName").toString());

							if (gridFSDBFile.getMetaData().get(ServiceConstants.hasAttachments) != null)
								refAttachments.setHasAttachments(Boolean.valueOf(gridFSDBFile.getMetaData().get(ServiceConstants.hasAttachments).toString()));
							
							if (gridFSDBFile.getMetaData().get(ServiceConstants.lastModified) != null)
								refAttachments.setLastModified(new BigInteger(gridFSDBFile.getMetaData().get(ServiceConstants.lastModified).toString()));
							
							refAttachments.setType(ServiceConstants.xmlElementType);
							prepareRefAttacchmentList.add(refAttachments);

						}
						attachments.getRef().addAll(prepareRefAttacchmentList);						
					}
					ref.setAttachments(attachments);
					prepareRefList.add(ref);					
				}
				result.setCount(new BigInteger(String.valueOf(prepareRefList.size())));
				result.setConsidered(new BigInteger("0"));
				result.getRef().addAll(prepareRefList);
				return result;
				
			}else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
	}

	
    @Override
    public Object getExpandedDocumentList(Options options) {
           return null;
    }
    /**
     * This method removes Extra Indentation introduced due to JAXB formatting.
     * It should be called after convertObjectToXml() method to remove extra spaces 
     * @param document
     * @return document without indentation
     */
    protected String removeIndentation(String doc){

    	doc = doc.replaceAll("<([^>]*)></\\1>", "<$1/>");
		doc = doc.replaceAll("<([^>]*)> *\r*\n *</\\1>", "<$1/>");
		//doc = doc.replaceAll(" *xsi:nil=\"true\" *xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" */>", "/>");
		//doc = doc.replaceAll(" *xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" *xsi:nil=\"true\" */>", "/>");
		doc = doc.replaceAll(" *xsi:nil=\"true\" */>", "/>");
		doc = doc.replaceAll(">\r*\n* *<", ">\n<");
		doc = doc.replaceAll("> *\r*\n* *<proj:", "><proj:");
		doc = doc.replaceAll("<([^/][^?>]*)> *\r*\n* *<", "<$1><");
		doc = doc.replaceAll("(<[^/>]*/><[^<>]*>) *\r*\n* *(<[^<>]*>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*/>) *\r*\n *(<[^/<>]*/>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*>) *\r*\n* *(<[^/<>]*>) *\r*\n* *(<[^/<>]*>) *\r*\n* *([^/<>]*</[^/<>]*>)","$1$2$3$4");
		doc = doc.replaceAll("(<[^/<>]*/>) *\r*\n* *(<[^/<>]*>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*>) *\r*\n *(<[^/<>]*>)", "$1$2");
		//SMO : Workaround for HPQ - to be removed after data segregation
		//doc = doc.replaceAll("HPQ", "");
		try {
		    if(!ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
		       doc= doc.replaceAll("(hp[i|e]content)", "content");
		}
		catch (MongoUtilsException e) {
			throw new WebApplicationException(500);
		   //throw new ApplicationException();
		}
    	return doc;

    }
    
    
    
    protected org.w3c.dom.Document updateExtraPropertyView(String doc, String docId) {
		org.w3c.dom.Document document = null;
		if(doc == null) {
			throw new ApplicationException(ServiceConstants.errorMsg_The_Entry +" "+ docId  + "  "+ServiceConstants.errorMsg_doesnt_exist);
		}
		document = getDocumentObject(doc);
		changeExtraPropertiesView(document);
		return document;
		
	}
    
    private org.w3c.dom.Document changeExtraPropertiesView(org.w3c.dom.Document document) {
		
    	
    	if(document != null) {
			
			NodeList nodes = document.getElementsByTagName("extra_property");
 			Node extraPropertiesNode = document.getElementsByTagName("extra_properties").item(0);
 			if(nodes!=null && extraPropertiesNode!=null){
 				
 				List<String> keyList = new ArrayList<String>();
 				List<String> valuesList = new ArrayList<String>();
 				
 				for(int i=0; i<nodes.getLength();i++){
 						Element extraPropertyElement = (Element) nodes.item(i);
 						String nodeName =extraPropertyElement.getElementsByTagName("extra_property_name").item(0).getTextContent();
 						String nodeValue = extraPropertyElement.getElementsByTagName("extra_property_values").item(0).getChildNodes().item(1).getTextContent();
 					    keyList.add(nodeName);
 					    valuesList.add(nodeValue);
 				}
 				
 				removeExtraNodeAndValue(extraPropertiesNode,"extra_property");
				if(keyList!= null && keyList.size() != 0)
					createNodeNameAndValue(document,keyList,valuesList);
			}
		}else{
			System.out.println(" dom document is null");
		}
		return document;
	}
    
    
    protected static void removeExtraNodeAndValue(Node parent, String filter) {
	
		NodeList children = parent.getChildNodes();
		parent.setTextContent("");
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(filter)) {
					parent.removeChild(child);
				} else {
					removeExtraNodeAndValue(child, filter);
				}
			}
		}
	}
    
    protected static void removeExtraNodeAndValueRetainChilds(Node parent, String filter) {
    	
    	NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(filter)) {
					parent.removeChild(child);
				} 
			}
		}
		
	}
	
    private static void createNodeNameAndValue(org.w3c.dom.Document document,List<String> keyList,List<String> valuesList){
		Element elem = (Element)document.getElementsByTagName("extra_properties").item(0);
 		for(int i=0;i<keyList.size();i++){
 				Element createElement = document.createElement(keyList.get(i));
 	 			elem.appendChild(createElement);
 	 			if(valuesList!=null)
 	 				createElement.setTextContent(valuesList.get(i)); 
 	 			else
 	 				createElement.setTextContent(""); 
  		}
 	}
	
	private org.w3c.dom.Document getDocumentObject(String doc) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document document = null;
		try {	
				DocumentBuilder docBuilder;
				docBuilder = docFactory.newDocumentBuilder();
				document =  docBuilder.parse(new InputSource(new StringReader(doc)));
				document.getDocumentElement().normalize();
				//System.out.println("dom document in getDocumentObject-->"+document);
				
				
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e.getMessage());
		}catch (SAXException e) {
			throw new ApplicationException(e.getMessage());
		} catch (IOException e) {
			throw new ApplicationException(e.getMessage());
		}
		return document;
	}
	
	protected String changeDOMObjectToXml(org.w3c.dom.Document doc) {

		StringWriter sw = null;
		StreamResult result=null;

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			sw = new StringWriter();
			result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		if(sw != null)
			return sw.toString();

		return null;
	}
	/**
	 * These method is get previous versions of the document..
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	public Object getExpandEmptyVersions(Options serviceDelegateBO) {

		com.hp.cdsplus.bindings.output.schema.expversions.Result result = new com.hp.cdsplus.bindings.output.schema.expversions.Result();
		com.hp.cdsplus.bindings.output.schema.expversions.Ref ref = null;
		result.setBase(serviceDelegateBO.getBaseUri());
		String subscription = serviceDelegateBO.getSubscription() == null ? "content"
				: serviceDelegateBO.getSubscription();
		String urlLink = serviceDelegateBO.getContentType() + "/"
				+ subscription + "/";

		try {
			Versions versions = new Versions();
			DBObject document = contentDAO.getLiveMetadata(serviceDelegateBO);

			if (document != null) {
							
							ref = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
							if (document.get(ServiceConstants.eventType) != null)
								ref.setEventType(document.get(ServiceConstants.eventType)
										.toString());
							if (document.get(ServiceConstants.priority) != null)
								ref.setPriority(new BigInteger(document.get(
										ServiceConstants.priority).toString()));
							if (document.get("_id") != null)
								ref.setHref(urlLink + document.get("_id"));
							if (document.get(ServiceConstants.hasAttachments) != null)
								ref.setHasAttachments(Boolean.valueOf(document.get(
										ServiceConstants.hasAttachments).toString()));

							if (document.get(ServiceConstants.lastModified) != null)
								ref.setLastModified(new BigInteger(document.get(
										ServiceConstants.lastModified).toString()));
							ref.setType(ServiceConstants.xmlElementType);
							result.setCount(new BigInteger("1"));
							result.setConsidered(new BigInteger("0"));

							ref.setVersions(versions);
							result.setRef(ref);
						return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			throw new WebApplicationException(500);
			//throw new ApplicationException(mue.getMessage());
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

	}

	/**
	 * This method is overridden by SoarServiceImpl 
	 * to handle large metadata documents
	 * @param the parent DBObject used for fetching file data
	 * @param dbObject the fetched data is put in this object
	 * @param key
	 * @return
	 */
	public void mergeLargeData(DBObject parent, DBObject dbObject) throws MongoInternalException, MongoUtilsException, 
																			IOException, ApplicationException {
		
		try{
			
		}
		
		
		catch(MongoInternalException ex){
			throw ex;
		}
		
		
		catch(ApplicationException ex){
			throw ex;
		}
		
		return ;
	}
}
