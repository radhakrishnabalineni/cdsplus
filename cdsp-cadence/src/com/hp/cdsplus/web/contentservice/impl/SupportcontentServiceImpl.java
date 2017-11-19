package com.hp.cdsplus.web.contentservice.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Marshaller;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.hp.cdsplus.bindings.output.schema.expattachment.Attachments;
import com.hp.cdsplus.bindings.output.schema.expversions.Versions;
import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.conversion.JAXBContextManager;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.RefDataDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.LinkGroomer;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import javax.ws.rs.WebApplicationException;

public class SupportcontentServiceImpl extends AbstractGenericService {
	
	LinkGroomer linkGroomer= new LinkGroomer("supportcontent");
	
	@Override
	public Object getDocumentList(Options options){
		
		if(options.getSubscription() != null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
			throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
			
		Ref ref = null;
		Result result = new Result();
		RefDataDAO refDataDAO = null;
		List<Ref> refs = new ArrayList<Ref>();
	
		try {
			result.setBase(options.getBaseUri() == null ? "null" : options.getBaseUri());
			String subscription = options.getSubscription() == null ? "content": options.getSubscription();
			
			//SMO : 
			if ("hpicontent".equals(subscription)) {
				options.setCompany("HPI");
			} else if ("hpecontent".equals(subscription)) {
				options.setCompany("HPE");
			}else if(!validateSubcsription(options, subscription)){
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			}
						
			refDataDAO = getRefDataDAO();
			BasicDBList docidList = refDataDAO.getDistinctDocumentList(options);
						
			Iterable<DBObject> resultFileList = refDataDAO.getMapReduceWithHasAttachments(options, docidList);
			Iterator<DBObject> iterator = resultFileList.iterator();
			
			if (resultFileList != null  && iterator!=null) {
				while (iterator.hasNext()){
					ref = getRefObject(options, subscription, (BasicDBObject)iterator.next().get("value"));
					refs.add(ref);	
				}
				if(options.isReverse())
					Collections.sort(refs,Collections.reverseOrder(new com.hp.cdsplus.web.util.LastModifiedComparator()));
				else
					Collections.sort(refs,new com.hp.cdsplus.web.util.LastModifiedComparator());
				result.getRef().addAll(refs);
				result.setCount(String.valueOf(docidList.size()));
			}else
				result.setCount("0");
			
			result.setConsidered("0");			
			result.setBase(options.getBaseUri());
			
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
		return result;

	}
	
	@Override
	public Object getExpandedDocumentList(Options options){
	
		if(options.getLimit()>10000)
	    	 options.setLimit(10000); 
	      else if(options.getLimit()==0)
            options.setLimit(20);
		
		try{
		Marshaller marshaller= JAXBContextManager.getInstance().getJAXBContext("com.hp.cdsplus.bindings.output.schema.subscription.Result").createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Writer writer = new OutputStreamWriter(baos);
		
		marshaller.marshal(getDocumentList(options), writer);
		
		String convertedXML = baos.toString();
		
		Document document = DocumentHelper.parseText(convertedXML);
		List<Node> nodesList = document.selectNodes("/result/proj:ref");
		Iterator<Node> nodesIterator = nodesList.iterator();
		Document metadata = null;
		while(nodesIterator.hasNext()){
			Element element = (Element)nodesIterator.next();
			String[] hrefParts= element.attributeValue("href").split("/");
			options.setDocid(hrefParts[2]);
			InputStream is = new ByteArrayInputStream(getDocumentMetaData(options).toString().getBytes("UTF-8"));
			SAXReader reader = new SAXReader();
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			metadata = reader.read(is);
			Element contentElement = element.addElement("content");
			contentElement.add(metadata.getRootElement());
		}
		
			return document.asXML();
		
		}catch(Exception e){
			throw new ApplicationException(e.getMessage());
		}
	}
	private Ref getRefObject(Options options, String subscription,BasicDBObject dbObject) {
		
		Ref ref = null;
		if(dbObject != null){
			ref = new Ref();
			
			ref.setEventType(((BasicDBObject)dbObject.get("metadata")).getString("eventType"));
			ref.setLastModified(((BasicDBObject)dbObject.get("metadata")).getString("lastModified"));
			ref.setPriority(((BasicDBObject)dbObject.get("metadata")).getString("priority"));
			ref.setType(ServiceConstants.xmlElementType);
			ref.setHasAttachments(dbObject.get("hasAttachment").toString());
					
			
			//com.hp.cdsplus.bindings.output.schema.subscription.Result result = (com.hp.cdsplus.bindings.output.schema.subscription.Result)super.getDocumentAttachments(docIdOption);
			//if(result != null && Integer.valueOf(result.getCount()) > 0)
				//	ref.setHasAttachments("true");
			//else ref.setHasAttachments("false");
//			
//			try {
//				List<GridFSDBFile> attachmentList = contentDAO.getAllAttachments(docIdOption);
//				if(!attachmentList.isEmpty())
//					ref.setHasAttachments("true");
//				else
//					ref.setHasAttachments("false");
//			} catch (MongoUtilsException e) {
//				e.printStackTrace();
//			} catch (OptionsException e) {
//				e.printStackTrace();
//			}
			
			ref.setHref(options.getContentType()
					+ "/" + subscription + "/"
					+ ((BasicDBObject)dbObject.get("metadata")).getString("docid"));
			
			//if(ref.getEventType().equals("delete"))	ref.setStatus(ref.getEventType());
		}
		return ref;
	}
	
	private RefDataDAO getRefDataDAO(){
		
		RefDataDAO refDataDAO = null;
		
		if(refDataDAO == null) 
			refDataDAO = new RefDataDAO();
		
		return refDataDAO;
	}
	
	@Override
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException {
		
		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
			throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
				
		InputStream ipstream = super.getDocumentContentMetaData(options);
		//SMO :
		if(options.getSubscription()==null || "".equals(options.getSubscription()) || "hpicontent".equals(options.getSubscription()) || "hpecontent".equals(options.getSubscription()))
			return getDocumentObject(ipstream,options,null);
		
		try {
			String subscriptionCollectionName = ConfigurationManager.getInstance().getMappingValue("supportcontent", ContentDAO.SUBSCRIPTION_COLLECTION_NAME);
			DBCursor cursor = mongoUtils.readMeta("supportcontent", subscriptionCollectionName,new BasicDBObject("_id",options.getSubscription()),options.getDisplayFields());
			
			DBObject dbObject = null;
			
			if(cursor.hasNext()){
				dbObject = cursor.next();
				
				DBObject displayFields = options.getDisplayFields();
				  if(dbObject.get("hierarchyExpansions")!=null){
						displayFields= new BasicDBObject();
						
						DBObject hierarchyExpansions = (DBObject)dbObject.get("hierarchyExpansions");
						
						for(String key : hierarchyExpansions.keySet()){
							
							String value = hierarchyExpansions.get(key).toString();
							if(value == null || "".equals(value)){
								continue;
							}

							Boolean val = Boolean.parseBoolean(value);
							if(!val) displayFields.put(key, 0);		
						}
						options.setDisplayFields(displayFields);
					}
					else{
						options.setDisplayFields(null);
					}
					
					if(dbObject.get("isGroomer")!=null && (Boolean)dbObject.get("isGroomer")==true){
						return linkGroomer.applyGroomsToAttributes(ipstream,options,getDocumentProperties(options));
					}
					else{
						return getDocumentObject(ipstream,options,getDocumentProperties(options));
					}					
			}
			
		} catch (MongoUtilsException e) {			
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		} catch (SAXException e) {
			throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " "
							+ options.getDocid() + " "
							+ ServiceConstants.errorMsg_doesnt_exist);		
		} catch (DocumentException e) {
			throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " "
							+ options.getDocid() + " "
							+ ServiceConstants.errorMsg_doesnt_exist);		
		}
		
		throw new ApplicationException(
				ServiceConstants.errorMsg_The_Entry + " "
						+ options.getDocid() + " "
						+ ServiceConstants.errorMsg_doesnt_exist);		
	}
	
	private String getDocumentObject(InputStream is,Options options,Document documentProperties) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
		    	String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			document = reader.read(is);
			document.getRootElement().addNamespace("proj", "http://www.hp.com/cdsplus");
			document.getRootElement().addNamespace("xlink", "http://www.w3.org/1999/xlink");
			if(document.processingInstruction("dctm")!=null)
				document.remove(document.processingInstruction("dctm"));
			document.setDocType(null);
			if(options.getBaseUri()!=null)				
				document.getRootElement().addAttribute("xml:base", options.getBaseUri());				
			List<Node> nodesList = document.selectNodes("//image[@src] | //attachment[@src]");
			Iterator<Node> nodesIterator = nodesList.iterator();
			while(nodesIterator.hasNext()){
				Element element = (Element)nodesIterator.next();
				//SMO : 
				element.addElement("proj:ref").addAttribute("xlink:type", "simple").addAttribute("xlink:href", "supportcontent/"+subscription+"/"+options.getDocid()+"/"+element.attributeValue("src"));
			}
			if(documentProperties!=null){
				List content = document.getRootElement().content();
				content.add(0,documentProperties.getRootElement());
			}
			return document.asXML().replaceAll("[<][?]xm[-][^>]*[?][>]","").replaceAll("\r*\n*<[?]xml[-]stylesheet[^?>]*[?]>", "");			
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		throw new ApplicationException(
				ServiceConstants.errorMsg_The_Entry + " "
						+ options.getDocid() + " "
						+ ServiceConstants.errorMsg_doesnt_exist);		
		
	}
	
	public Document getDocumentProperties(Options options) throws MongoUtilsException{
		
		if(options.getDisplayFields()==null){
			return null;
		}
		
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue("support", ContentDAO.METADATA_LIVE_COLLECTION_NAME);
		DBCursor cursor = mongoUtils.readMeta("support", metadataCollectionName,new BasicDBObject("_id",options.getDocid()),options.getDisplayFields());
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		if(cursor.hasNext()){
			BasicDBObject record = (BasicDBObject) cursor.next();
			if(record != null){
				
				options.setContentType("support");
				options.setSubscription(null);
				
				handleProductHierarchy(options,record,false);
				
				Document metadataDocument = DocumentHelper.createDocument();
				Element properties = metadataDocument.addElement("properties");
				DBObject displayFields = options.getDisplayFields();
				
				BasicDBObject docDBObject = (BasicDBObject)record.get("document");
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_TYPE);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_SUPPORT_CATEGORY);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_SUPPORT_SUBCATEGORY);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_MARKETING_CATEGORY);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_MARKETING_SUBCATEGORY);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_BIGSERIES);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_SERIES);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_NAME);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_NUMBER);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_PRODUCT_NUMBER_NAME);
				prepareXMLProductInformation(docDBObject,displayFields,properties,SUB_DOC_SUPPORT_NAME);
				
				if(record.get("document")!=null){
					
					SupportServiceImpl supportServiceImpl = new SupportServiceImpl();
					SAXReader reader = new SAXReader();  
				    Document supportMetadataDocument = null;
					try {
						//Regular expression is added here for moving new xml tags to next line as vice-versa is done is support 
						//side-effect of which is causing only first xml tag to be consireded in each line by following code
						supportMetadataDocument = reader.read(new StringReader(supportServiceImpl.getDocumentMetaData(options).toString().
							replaceAll("(<[^</>]*/>)<", "$1\n<").replaceAll("(</[^</>]*>)<", "$1\n<")));
					} catch (ApplicationException e) {
						e.printStackTrace();
					} catch (DocumentException e) {
						e.printStackTrace();
					}  
					if(supportMetadataDocument!=null){
						List<Node> documentNode =  supportMetadataDocument.getRootElement().content();
						Iterator<Node> listIterator = documentNode.iterator();
						while(listIterator.hasNext()){
							Node node = listIterator.next();
							if(node.getName()!=null){
								properties.add(node.detach());
							}
						}						
					}					
				}
				return metadataDocument;		
			}				
		}
		cursor.close();	
		return null;
	}
	
	
	private void prepareXMLProductInformation(DBObject docDBObject,DBObject displayFields,Element properties,String field){
		String qualifiedFieldName = "document."+field;
		if(docDBObject.get(field)!=null && (displayFields.get(qualifiedFieldName)==null)||(displayFields.get(qualifiedFieldName)!=null && (Integer)displayFields.get(qualifiedFieldName)!=0)){
			DBObject dbObject = (DBObject)docDBObject.get(field);
			JSONArray productList;
			try {
				productList = new JSONArray(dbObject.get("product").toString());
				Element products = properties.addElement(field);
				for(int i=0;i<productList.length();i++){
					products.addElement("product").setText(productList.get(i).toString());
				}
			} catch (JSONException e) {				
				e.printStackTrace();
			}			
		}
	}
	
	@Override
    public Object getExpandVersions(Options serviceDelegateBO) {

		   com.hp.cdsplus.bindings.output.schema.expversions.Result result = new com.hp.cdsplus.bindings.output.schema.expversions.Result();
           com.hp.cdsplus.bindings.output.schema.expversions.Ref ref = null;
           result.setBase(serviceDelegateBO.getBaseUri());
           String subscription = serviceDelegateBO.getSubscription() == null ? "content"
                        : serviceDelegateBO.getSubscription();
           String urlLink = serviceDelegateBO.getContentType() + "/"
                        + subscription + "/";
           
           try {
           //SMO : 
        	   if(!validateSubcsription(serviceDelegateBO, subscription)) {
   				throw new ApplicationException(
   						ServiceConstants.errorMsg_The_Entry +" "+ subscription
   						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
   			}
                        RefDataDAO refDataDAO = new RefDataDAO();
                        String docid=serviceDelegateBO.getDocid();
                        
                        QueryBuilder builder = QueryBuilder.start();
        				builder.put("metadata.docid").is(docid);
        				DBObject subQuery1 = builder.get();
        				
        				builder = QueryBuilder.start();
        				builder.put("metadata.fileType").is("xml");
        				builder.exists(true);
        				DBObject subQuery2 = builder.get();
        				
        				builder = QueryBuilder.start();
        				builder.and(subQuery1,subQuery2);
                        
                        List<GridFSDBFile> metadataList = refDataDAO.getRefData(serviceDelegateBO.getContentType(), builder.get());
                        
                        if (metadataList != null && !metadataList.isEmpty()) {
                                             DBObject document = metadataList.get(0).getMetaData();
                                             ref = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
                                             if (document.get(ServiceConstants.eventType) != null)
                                                    ref.setEventType(document.get(ServiceConstants.eventType)
                                                                  .toString());
                                             if (document.get(ServiceConstants.priority) != null)
                                                    ref.setPriority(new BigInteger(document.get(
                                                                  ServiceConstants.priority).toString()));
                                             if (document.get(ServiceConstants.docid) != null)
                                                    ref.setHref(urlLink + document.get(ServiceConstants.docid));

                                            com.hp.cdsplus.bindings.output.schema.subscription.Result queryresult = (com.hp.cdsplus.bindings.output.schema.subscription.Result)super.getDocumentAttachments(serviceDelegateBO);
                             				if(queryresult != null && Integer.valueOf(queryresult.getCount()) > 0)
                             						ref.setHasAttachments(true);
                             				else ref.setHasAttachments(false);		

                                             if (document.get(ServiceConstants.lastModified) != null)
                                                    ref.setLastModified(new BigInteger(document.get(
                                                                  ServiceConstants.lastModified).toString()));
                                             ref.setType(ServiceConstants.xmlElementType);
                                             result.setCount(new BigInteger("1"));
                                             
                                             Versions versions = new Versions();
                                             ref.setVersions(versions);
                                             result.setRef(ref);        
                                             result.setConsidered(new BigInteger("0"));
                                      return result;
                  } else {
                        result.setCount(new BigInteger("0"));
                        result.setConsidered(new BigInteger("0"));
                        return result;
                  }
           } catch (MongoException e) {
                //throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } catch (MongoUtilsException e) {
                //throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } catch (OptionsException e) {
        	   throw new ApplicationException(e.getMessage());
		}

    }
	
	@Override
    public Object getExpandAttachments(Options serviceDelegateBO) {

		   com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
           com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = null;
           com.hp.cdsplus.bindings.output.schema.expattachment.Ref refAttachment =null;
           result.setBase(serviceDelegateBO.getBaseUri());
           String subscription = serviceDelegateBO.getSubscription() == null ? "content"
                        : serviceDelegateBO.getSubscription();
           String urlLink = serviceDelegateBO.getContentType() + "/"
                        + subscription + "/";

           try {
			        	 //SMO : 
			        	   if(!validateSubcsription(serviceDelegateBO, subscription)) {
			   				throw new ApplicationException(
			   						ServiceConstants.errorMsg_The_Entry +" "+ subscription
			   						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			   			}
           
                        RefDataDAO refDataDAO = new RefDataDAO();
                        String docid=serviceDelegateBO.getDocid();
                        
                        QueryBuilder builder = QueryBuilder.start();
        				builder.put("metadata.docid").is(docid);
        				DBObject subQuery1 = builder.get();
        				
        				builder = QueryBuilder.start();
        				builder.put("metadata.fileType").is("xml");
        				builder.exists(true);
        				DBObject subQuery2 = builder.get();
        				
        				builder = QueryBuilder.start();
        				builder.and(subQuery1,subQuery2);
                        
                        List<GridFSDBFile> metadataList = refDataDAO.getRefData(serviceDelegateBO.getContentType(), builder.get());
                        
                        if (metadataList != null && !metadataList.isEmpty()) {
                                             DBObject document = metadataList.get(0).getMetaData();
                                             ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
                                             if (document.get(ServiceConstants.eventType) != null)
                                                    ref.setEventType(document.get(ServiceConstants.eventType)
                                                                  .toString());
                                             if (document.get(ServiceConstants.priority) != null)
                                                    ref.setPriority(new BigInteger(document.get(
                                                                  ServiceConstants.priority).toString()));
                                             if (document.get(ServiceConstants.docid) != null)
                                                    ref.setHref(urlLink + document.get(ServiceConstants.docid));

                                            com.hp.cdsplus.bindings.output.schema.subscription.Result queryresult = (com.hp.cdsplus.bindings.output.schema.subscription.Result)super.getDocumentAttachments(serviceDelegateBO);
                                        	Attachments attachments = new Attachments();
                                            
                                            if(queryresult != null && Integer.valueOf(queryresult.getCount()) > 0){
                             						ref.setHasAttachments(true);
                             					    for(Ref tempRef :queryresult.getRef()){
                                                    refAttachment = new  com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
                                                    refAttachment.setHref(tempRef.getHref());
                                                    refAttachment.setType(tempRef.getType());
                                                    refAttachment.setLastModified(new BigInteger(tempRef.getLastModified()));
                                                    attachments.getRef().add(refAttachment);
                                                    }
                                            }
                             				else ref.setHasAttachments(false);		

                                             if (document.get(ServiceConstants.lastModified) != null)
                                                    ref.setLastModified(new BigInteger(document.get(
                                                                  ServiceConstants.lastModified).toString()));
                                             ref.setType(ServiceConstants.xmlElementType);
                                             ref.setAttachments(attachments);
                              				 result.setCount(new BigInteger("1"));
                                             result.setRef(ref);        
                                             result.setConsidered(new BigInteger("0"));
                                      return result;
                  } else {
                        result.setCount(new BigInteger("0"));
                        result.setConsidered(new BigInteger("0"));
                        return result;
                  }
           } catch (MongoException e) {
                //throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } catch (MongoUtilsException e) {
                //throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } catch (OptionsException e) {
        	   throw new ApplicationException(e.getMessage());
		}

    }
	/**
	 * These method is get generic expand details like
	 * expand=[version,attachment,false],versions=[true,false]
	 * 
	 * @param serviceDelegateBO
	 * @return response
	 */
	@Override
	public Object getVersions(Options serviceDelegateBO) throws ApplicationException, WebApplicationException {

		com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
		com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = null;
		result.setBase(serviceDelegateBO.getBaseUri() == null ? "null"
				: serviceDelegateBO.getBaseUri());
		String subscription = serviceDelegateBO.getSubscription() == null ? "content"
				: serviceDelegateBO.getSubscription();
		String urlLink = serviceDelegateBO.getContentType() + "/"
				+ subscription + "/";

		try {
			
			 //SMO : 
     	   if(!validateSubcsription(serviceDelegateBO, subscription)) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry +" "+ subscription
						+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			}
     	   
			RefDataDAO refDataDAO = new RefDataDAO();
            String docid=serviceDelegateBO.getDocid();
                                     
            QueryBuilder builder = QueryBuilder.start();
			builder.put("metadata.docid").is(docid);
			DBObject subQuery1 = builder.get();
			
			builder = QueryBuilder.start();
			builder.put("metadata.fileType").is("xml");
			builder.exists(true);
			DBObject subQuery2 = builder.get();
			
			builder = QueryBuilder.start();
			builder.and(subQuery1,subQuery2);
            
            List<GridFSDBFile> metadataList = refDataDAO.getRefData(serviceDelegateBO.getContentType(), builder.get());
            
            if (metadataList != null && !metadataList.isEmpty()) {
            	DBObject document = metadataList.get(0).getMetaData();
                
            	ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
                 
    			if (document.get(ServiceConstants.eventType) != null)
					ref.setEventType(document.get(ServiceConstants.eventType)
							.toString());
				if (document.get(ServiceConstants.priority) != null)
					ref.setPriority(new BigInteger(document.get(
							ServiceConstants.priority).toString()));
				if (document.get(ServiceConstants.docid) != null)
					ref.setHref(urlLink + document.get(ServiceConstants.docid));
				
				com.hp.cdsplus.bindings.output.schema.subscription.Result queryresult = (com.hp.cdsplus.bindings.output.schema.subscription.Result)super.getDocumentAttachments(serviceDelegateBO);
				if(queryresult != null && Integer.valueOf(queryresult.getCount()) > 0)
						ref.setHasAttachments(true);
				else ref.setHasAttachments(false);				
				
				if (document.get(ServiceConstants.lastModified) != null)
					ref.setLastModified(new BigInteger(document.get(
							ServiceConstants.lastModified).toString()));
				ref.setType(ServiceConstants.xmlElementType);
				result.setCount(new BigInteger("1"));
				result.setConsidered(new BigInteger("0"));
				result.setRef(ref);
				return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}           		

		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		}

	}
}