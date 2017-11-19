package com.hp.cdsplus.web.contentservice.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.dao.CgsDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.RefDataDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import javax.ws.rs.WebApplicationException;

public class CgsServiceImpl extends AbstractGenericService {
	

	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException{
		
		if(options.getSubscription() != null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
			throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
		
		Result result = new Result();
		Ref ref = null;
		ArrayList<Ref> refs = new ArrayList<Ref>();
		//SMO:apply chnages in list
		//com.hp.cdsplus.bindings.output.schema.subscription.Ref ref = null;
		//ArrayList<com.hp.cdsplus.bindings.output.schema.subscription.Ref> refs = new ArrayList<com.hp.cdsplus.bindings.output.schema.subscription.Ref>();
	//	com.hp.cdsplus.bindings.output.schema.subscription.Result result = new com.hp.cdsplus.bindings.output.schema.subscription.Result();

		RefDataDAO refDataDAO = new RefDataDAO();
		DBCursor docList = null;
		//SMO:apply changes to get content
		try {
			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			//SMO : 
			if ("hpicontent".equals(subscription)) 
			{
				options.setCompany("HPI");
			} 
			else if ("hpecontent".equals(subscription)) 
			{
				options.setCompany("HPE");
			}
			else if(!validateSubcsription(options, subscription) && !"system".equalsIgnoreCase(subscription)){
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			}
			  
			 if(!"system".equalsIgnoreCase(subscription))
			 {
			     	docList =  refDataDAO.getRefDataListFromLive(options);
			 	result.setBase(options.getBaseUri());
			 }	
			 
			String urlLink = options.getContentType() + "/" + subscription
					+ "/";

			System.out.println(subscription);
			
			 
		
			DBCursor cursor = new CgsDAO().getDocumentList(options);
			validateDocumentCount(cursor,options);

			if(options.isReverse()){
				cursor.sort(new BasicDBObject("lastModified",1));
			}
			result.setBase(options.getBaseUri());
			
			//String urlLink = options.getContentType() + "/content/";
            //SMO:apply changes
			if (docList != null && !(docList.size() == 0)) {
			for (DBObject docObject : cursor) {
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
			result.setCount(String.valueOf(cursor.size()));	
			}
			else
			
				result.setCount("0");
				result.setConsidered("0");
				return result;
		}
		catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
			}
			 catch (ApplicationException e) {
					throw new ApplicationException(e.getMessage());
			 }
			catch (Exception e1) {
				e1.printStackTrace();
			
		}
		return result;

	
		
	}

	@Override
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException {
		
		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
			throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
				
		String subscription = "";
		//SMO: validate the subscription

		try {
		    subscription = options.getSubscription() == null ? "content" : options.getSubscription();
			if(!validateSubcsription(options, subscription) && !"system".equalsIgnoreCase(subscription))
			{
			  
			  throw new ApplicationException(
			  ServiceConstants.errorMsg_The_Entry +" "+ subscription +
			  "  "+ServiceConstants.errorMsg_doesnt_exist); 
			  }
		} catch (MongoUtilsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OptionsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String docid = options.getDocid();
		
		if(subscription != null && !"".equals(subscription) && subscription.equalsIgnoreCase("system")){
			if(docid != null && !"".equals(docid)){
				String dbName;
				try {
					dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
					String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
					
					DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
					GridFS gridFS = new GridFS(db, contentBucketName);
					GridFSDBFile file = gridFS.findOne(new BasicDBObject("filename",options.getDocid()));
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					if(file != null){
						file.writeTo(outputStream);
					}
					return new String(outputStream.toByteArray());
				} catch (MongoUtilsException e) {
					//throw new ApplicationException(e);
					throw new WebApplicationException(500);
				} catch (IOException e) {
					throw new ApplicationException(e);
				}
				
			}
		}

		try {
			DBObject record = new CgsDAO().getDocumentMetadata(options);
			if(record ==null){
				throw new ApplicationException("The Entry "+options.getDocid()+" not found");
			}
			String id = (String) record.get("_id");
			String contentType = (String) record.get("contentType");
			
			DBObject groups = (DBObject) record.get("groups");
			ArrayList<String> list  = (ArrayList<String>) groups.get("group");
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("groups");
			rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			rootElement.setAttribute("xmlns:proj", "http://www.hp.com/cdsplus");
			rootElement.setAttribute("xml:base", options.getBaseUri());		
			//rootElement.setIdAttributeNS("http://www.w3.org/1999/xlink", "xmlns:xlink", true);
			//rootElement.setIdAttributeNS("http://www.hp.com/cdsplus", "xmlns:proj", true);
			//rootElement.setIdAttributeNS(options.getBaseUri(), "xml:base", true);
			options.getBaseUri();
			doc.appendChild(rootElement);

			for(int i =0 ; i < list.size(); i++){
				Element group = doc.createElement("group");
				group.appendChild(doc.createTextNode(list.get(i)));
				rootElement.appendChild(group);
			}
			
			Element projref = doc.createElement("proj:ref");
			Attr xlink = doc.createAttribute("xlink:type");
			xlink.setValue("simple");
			projref.setAttributeNode(xlink);
			
			Attr href = doc.createAttribute("xlink:href");
			//SMO: apply changes to get subscription
			href.setValue(contentType+"/"+subscription+"/"+id);
			projref.setAttributeNode(href);
			rootElement.appendChild(projref);
			return doc;
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e);
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public String putDocumentMetadata(Options options) throws ApplicationException, WebApplicationException {
		String response = null;
		String sub = options.getSubscription();
		if(options.getDocid() == null){
			//System.out.println("docid is null");
			throw new ApplicationException("Invalid Request : Document cannot be saved without id");
		}
		if(options.getInputStream() == null){
			//System.out.println("inputstream is null");
			throw new ApplicationException("Invalid Request : Document cannot be saved without a valid input stream");
		}
		
		if(sub == null || "".equals(sub) || !sub.equalsIgnoreCase("system")){
			//System.out.println("Subscription != system");
			throw new ApplicationException("Invalid Request : Write operation cannot be performed");
		}
		
		try {
			String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
			String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
			
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
			
			if(sub != null && !"".equals(sub) && sub.equalsIgnoreCase("system")){
				GridFS gridFS = new GridFS(db, contentBucketName);
				gridFS.remove(new BasicDBObject("filename",options.getDocid()));
				
				GridFSInputFile inputFile = gridFS.createFile(options.getInputStream());
				inputFile.setMetaData(new BasicDBObject("isModified", true).append("lastModified", System.currentTimeMillis()));
				inputFile.setFilename(options.getDocid());
				inputFile.save();
				response = "successful";
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			//throw new ApplicationException(e);
			throw new WebApplicationException(500);
		}
		//System.out.println(response);
		return response;
	}
	@Override
	public Object getDocumentExpandDetails(Options serviceDelegateBO) {

		Object response = null;

		if (serviceDelegateBO.getExpand().equalsIgnoreCase(
				ServiceConstants.expand_versions)) {

			response = getExpandEmptyVersions(serviceDelegateBO);
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
	public static void main(String[] args) throws MongoUtilsException, IOException{
		System.setProperty("mongo.configuration","config/mongo.properties");
		Options options = new Options();
		options.setContentType("cgs");
		options.setSubscription("system");
		options.setDocid("CGSRules");
		options.setInputStream(new FileInputStream("config/CGSRules.xml"));
		
		//String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");
		//String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.CONTENT_BUCKET_NAME);
		
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("soar");
		/*GridFS gridFS = new GridFS(db, contentBucketName);
		GridFSDBFile file = gridFS.findOne(new BasicDBObject("filename",options.getDocid()));
		if(file != null){
			file.writeTo("config/CGSRules.xml");
		}
		//new CgsServiceImpl().putDocumentMetadata(options);
		 */
		DBCollection coll = db.getCollection("metadata_cache");
		ArrayList<String> subs = new ArrayList<String>();
		subs.add("gnew_201");
		DBCursor cursor = coll.find(new BasicDBObject("subscriptions",new BasicDBObject("$in",subs)));
		File file = new File("config/soar_gnew_coll_list.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for(DBObject rec : cursor){
			if(rec.get("_id")!=null)
			writer.write(rec.get("_id").toString()+"\n");
		}
		writer.flush();
		writer.close();
	}
	
}
