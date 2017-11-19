package com.hp.cdsplus.web.contentservice.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import javax.ws.rs.WebApplicationException;

public class ReportsServiceImpl extends AbstractGenericService {

	private static final String ID_FIELD = "_id";
	private static final String EVENT_TYPE_FIELD = "eventType";
	private static final String PRIORITY_FIELD="priority";
	private static final String LAST_MODIFIED_TS_FIELD="lastModified";
	private static final String SUBSCRIPTIONS_FIELD = "subscriptions";
	private static final String HASATTACHMENTS_FIELD = "hasAttachments";
	private static final String MEATDATA_DOCID_FIELD = "metadata.docid";

	@Override
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException {
		
		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub)){
			 return stylesheetUtil.getStylesheetXMLDocument(options);
		}
		
		String dbName;
		try {
			dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
			DBCollection metadataLiveCollection = db.getCollection(liveCollectionName);
			
			DBObject record = metadataLiveCollection.findOne(new BasicDBObject("_id",options.getDocid()));
			
			if(record == null){
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ options.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("document");
			rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			rootElement.setAttribute("xmlns:proj", "http://www.hp.com/cdsplus");
			rootElement.setAttribute("xml:base", options.getBaseUri());		

			options.getBaseUri();
			doc.appendChild(rootElement);
			//SMO User story ## 8076 Changes for displaying company_info on screen
			
			String ciText=(String)record.get("company_info");
			if(!StringUtils.isEmpty(ciText) && ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
			{
				Element companyInfoElement = doc.createElement("company_info");
				companyInfoElement.setTextContent(ciText);
				rootElement.appendChild(companyInfoElement);
			}
			
			//change ends
			String workGroupName = (String) record.get("work_group_name");
			if(workGroupName != null && !"".equals(workGroupName)){
				Element workGroupNameElement = doc.createElement("work_group_name");
				workGroupNameElement.setTextContent(workGroupName);
				rootElement.appendChild(workGroupNameElement);
			}
			
			String file_name = (String) record.get("file_name");
			if(file_name != null){
				
				if(file_name.startsWith("\n")){
					file_name = file_name.substring(1,file_name.length());
				}
				if(file_name.endsWith("\n")){
					file_name = file_name.substring(0,file_name.length()-1);
				}
				
				Element fileNameElement = doc.createElement("file_name");
				fileNameElement.setTextContent(file_name);
				
				Element projref = doc.createElement("proj:ref");
				Attr xlink = doc.createAttribute("xlink:type");
				xlink.setValue("simple");
				projref.setAttributeNode(xlink);
				Attr href = doc.createAttribute("xlink:href");
				//SMO User story ## 8076 Change for using company info for making href string
				String subscription = options.getSubscription() == null ? "content" : options.getSubscription();
				if(!validateSubcsription(options, subscription))
				{

					  throw new ApplicationException(
					  ServiceConstants.errorMsg_The_Entry +" "+ subscription +
					  "  "+ServiceConstants.errorMsg_doesnt_exist); 
					  
				}
				String hrefString = "";
				hrefString = "reports/"+subscription.toLowerCase()+"/"+options.getDocid() + "/"+file_name;
				href.setValue(hrefString);
				projref.setAttributeNode(href);
				fileNameElement.appendChild(projref);
				rootElement.appendChild(fileNameElement);
				
			}
			return doc;
			
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e);
			throw new WebApplicationException(500);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		} catch (OptionsException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException {
		
		if(options.getSubscription() != null && options.getSubscription().equals("stylesheet")){
			return super.getSubcriptionList(options);
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

			try {
				docList = getDocumentListCursor(options);
			} catch (MongoUtilsException e) {
				throw new ApplicationException(e);
			}

			validateDocumentCount(docList,options);
			
			if(docList != null && options.isReverse()){
				docList.sort(new BasicDBObject("lastModified",1));
			}

			result.setBase(options.getBaseUri());

			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new Ref();
					ref.setEventType(docObject.get(ServiceConstants.eventType)==null ? "update":docObject.get(ServiceConstants.eventType).toString());
					ref.setHasAttachments(docObject.get(ServiceConstants.hasAttachments)==null ? null:docObject.get(ServiceConstants.hasAttachments).toString());
					ref.setLastModified(docObject.get(ServiceConstants.lastModified)==null ? null:docObject.get(ServiceConstants.lastModified).toString());
					ref.setPriority(docObject.get(ServiceConstants.priority)==null ? "4":docObject.get(ServiceConstants.priority).toString());
					if(ref.getEventType()!=null){
						if(ref.getEventType().equalsIgnoreCase("delete"))
							ref.setStatus(docObject.get(ServiceConstants.eventType).toString());
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
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		} 
	}
	
	@Override
	public InputStream getDocumentAttachment(Options options)
			throws ApplicationException, WebApplicationException {
		GridFSDBFile gridFSDBFile;
		
		try {
			String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			
			String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
			
			GridFS gridFS = new GridFS(db,contentBucketName);
			
			DBObject query = new BasicDBObject("filename",options.getAttachmentName());
			query.put("metadata.docid", options.getDocid());
			
			
			gridFSDBFile = gridFS.findOne(query);

			if (gridFSDBFile == null) {
				throw new ApplicationException(options.getAttachmentName()
						+ " does not exists/ associated with the document-"
						+ options.getDocid());
			}

		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e);
			throw new WebApplicationException(e, 500);
		} catch(MongoException e){
			//throw new ApplicationException(e);
			throw new WebApplicationException(e, 500);
		}


		return gridFSDBFile.getInputStream();

	}

	@Override
	public Object getDocumentAttachments(Options options) {
		//System.out.println("calling ReportsServiceImpl getDocumentAttachments()");
		List<GridFSDBFile> attachmentList;
		Result result = new Result();

		List<Ref> refs = new ArrayList<Ref>();
		Ref ref = null;
		result.setBase(options.getBaseUri() == null ? "null" : options
				.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {
			String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			
			String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
			
			GridFS gridFS = new GridFS(db,contentBucketName);
			DBObject query = new BasicDBObject("metadata.docid", options.getDocid());
			//System.out.println(query);
			attachmentList = gridFS.find(query);
			//System.out.println("count : "+attachmentList.size());
			if (attachmentList != null && !attachmentList.isEmpty()) {
				result.setCount(String.valueOf(attachmentList.size()));

				for (GridFSDBFile gridFSDBFile : attachmentList) {
					ref = new Ref();
					ref.setHref(options.getContentType()
							+ "/"
							+ subscription
							+ "/"
							+ gridFSDBFile.getMetaData().get("docid")
							.toString()
							+ "/"
							+ gridFSDBFile.getFilename());
					Object lastModified = gridFSDBFile.getMetaData()
							.get("lastModified");
					if(lastModified != null)
						ref.setLastModified(lastModified.toString());
					ref.setType(ServiceConstants.xmlElementType);
					refs.add(ref);
				}

				result.getRef().addAll(refs);
			} else {
				result.setCount("0");
			}
			return result;
		}

		catch (MongoUtilsException e) {
			//throw new ApplicationException();
			throw new WebApplicationException(500);
		} catch (Exception e) {
			throw new ApplicationException();
		}
	}

	private DBCursor getDocumentListCursor(Options options) throws MongoUtilsException {
		QueryBuilder builder = QueryBuilder.start();
		DBObject query = new BasicDBObject();
		// if its subscription specific
		if(!isNull(options.getSubscription())){
			query = getSubscriptionFilter(options);
			if(options.isIncludeDeletes()){
				builder.or(query, new BasicDBObject("eventType","delete"));
			}
		}

		// last_modified > after and last_modified < before
		if(options.getAfter() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).greaterThanEquals(options.getAfter());
		}

		if(options.getBefore() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).lessThan(options.getBefore());
		}

		if(!options.isIncludeDeletes()){
			builder.and(query);
			builder.and("eventType").notEquals("delete");
		}

		if(options.getQuery() != null){
			builder.and(options.getQuery());
		}

		if(options.getHasAttachments()!=null){
			builder.and(HASATTACHMENTS_FIELD).is(Boolean.valueOf(options.getHasAttachments()));
		}

		DBObject fields=  options.getDisplayFields();

		if(fields == null){
			fields = new BasicDBObject(ID_FIELD,true)
			.append(LAST_MODIFIED_TS_FIELD, true)
			.append(PRIORITY_FIELD, true)
			.append(SUBSCRIPTIONS_FIELD, true)
			.append(EVENT_TYPE_FIELD, true)
			.append(HASATTACHMENTS_FIELD, true);
		}

		DBCursor cursor = mongoUtils.readMeta(options.getContentType(), "metadata_live", builder.get(),fields);
		// reverse the order of the results
		cursor.sort(new BasicDBObject("lastModified",-1));

		//System.out.println("Record Count - "+cursor.count());

		//set limit if requested
		if(options.getLimit() > 0)
			cursor.limit(options.getLimit());
		//get the documents from collection and store it in memory to be returned to the calling method.

		return cursor;
	}

	private DBObject getSubscriptionFilter(Options options) throws MongoUtilsException{

		String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		String subCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.SUBSCRIPTION_COLLECTION);
		DBCollection subscriptionCollection = db.getCollection(subCollectionName);
		
		DBObject query = null;
		
		
		if(options.getSubscription()!= null && !"".equals(options.getSubscription()) && !"content".equals(options.getSubscription())){
			DBObject subFilter = subscriptionCollection.findOne(new BasicDBObject("_id",options.getSubscription()), new BasicDBObject("filter",1));
			if(subFilter != null){
				Object filterObj = subFilter.get("filter");
				if( filterObj != null && filterObj instanceof DBObject){
					query = (DBObject) filterObj;
				}else{
					query = new BasicDBObject();
				}
			}else{
				throw new ApplicationException("The subscription "+options.getSubscription()+" "+ServiceConstants.errorMsg_doesnt_exist);
			}
		}else {
			query = new BasicDBObject();
		}
		
		return query;
	}

	//Adding putDocumentMetadata, putDocumentContent methods to deal with OPS Reports.
	//Introduction of these methods will enforce just a cdsplus url change in concentra side to push Ops Reports to new cdsplus.
	
	@Override
	public String putDocumentMetadata(Options options) throws ApplicationException, WebApplicationException {
		String response = null;
		if(options.getDocid() == null){
			throw new ApplicationException("Invalid Request : Document cannot be saved without id");
		}
		if(options.getInputStream() == null){
			throw new ApplicationException("Invalid Request : Document cannot be saved without a valid input stream");
		}
		
		try {
			String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
			DBCollection metadataLiveCollection = db.getCollection(liveCollectionName);
			
			DBObject record = parse(options.getInputStream());
			if(!(record.containsField("file_name") || record.containsField("work_group_name"))){
				throw new ApplicationException("work_group_name or file_name field is missing in the metadata. Cannot be saved to mongodb",new Throwable().fillInStackTrace());
			}
			record.put("_id", options.getDocid());
			record.put("lastModified", System.currentTimeMillis());
			record.put("priority", 1);
			record.put("eventType", "update");
			if(record.containsField("file_name")){
				record.put("hasAttachments", "true");
			}
			
			metadataLiveCollection.update(new BasicDBObject("_id", options.getDocid()), record, true, false, WriteConcern.ACKNOWLEDGED);
						
			
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			//throw new ApplicationException(e);
			throw new WebApplicationException(500);
		}
		return response;
	}
	
	private DBObject parse(InputStream bytes) throws ApplicationException {
		Document doc = null;
		DBObject record = new BasicDBObject();
		try {
			doc = XMLUtils.parse(bytes);
			Element file_name_element = XMLUtils.getElement(doc, "file_name");
			if(file_name_element !=null){
				String file_name = file_name_element.getTextContent();
				if(file_name.startsWith("\n")){
					file_name = file_name.substring(1,file_name.length());
				}
				if(file_name.endsWith("\n")){
					file_name = file_name.substring(0,file_name.length()-1);
				}
				record.put("file_name", file_name);
			}
			Element workGroupName = XMLUtils.getElement(doc, "work_group_name");
			if(workGroupName !=null){
				record.put("work_group_name", workGroupName.getTextContent());
			}
			//SMO User story ## 8076 Changes for parsing company_info from data coming from concentra
			Element companyInfo = XMLUtils.getElement(doc, "company_info");
			if(companyInfo !=null){
				record.put("company_info", companyInfo.getTextContent());
			}
			//Change ends here
			
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		}
		return record;
	}
	
	
	@Override
	public String putDocumentContent(Options options) throws ApplicationException, WebApplicationException {
		String response = null;
		if(options.getDocid() == null){
			throw new ApplicationException("Invalid Request : Document cannot be saved without id");
		}
		if(options.getInputStream() == null){
			throw new ApplicationException("Invalid Request : Document cannot be saved without a valid input stream");
		}		
		try {
			String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
			GridFS gridFS = new GridFS(db, contentBucketName);
			gridFS.remove(new BasicDBObject("filename",options.getAttachmentName()));

			GridFSInputFile inputFile = gridFS.createFile(options.getInputStream());

			DBObject metadata = new BasicDBObject("docid", options.getDocid());
			metadata.put("fileName", options.getAttachmentName());
			metadata.put("lastModified", System.currentTimeMillis());
			metadata.put("priority", 1);
			metadata.put("eventType", "update");
			inputFile.setMetaData(metadata);
			inputFile.setFilename(options.getAttachmentName());
			inputFile.save();

			response = "successful";

		} catch (MongoUtilsException e) {
			e.printStackTrace();
			//throw new ApplicationException(e);
			throw new WebApplicationException(500);
		}
		//System.out.println(response);
		return response;
	}
	
	
	
    
	public boolean isNull(String value) {
		if(value == null || "".equals(value))
			return true;
		return false;
	}
	
}
