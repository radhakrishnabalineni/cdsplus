package com.hp.cdsplus.web.contentservice.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.hp.cdsplus.conversion.JAXBContextManager;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.RefDataDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.cdsplus.bindings.output.schema.expversions.Versions;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import javax.ws.rs.WebApplicationException;


public class Soar_ref_dataServiceImpl extends AbstractGenericService{

@Override
	public Object getDocumentList(Options options){
	
	if(options.getSubscription() != null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
		throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
	
	Result result = new Result();
	Ref ref = null;
		
			RefDataDAO refDataDao=new RefDataDAO();	
			DBObject metadata=null;
			DBCursor docList = null;
			
			
			//SMO:apply changes to get content
			try {
				String subscription = options.getSubscription() == null ? "content"
						: options.getSubscription();
				String urlLink = options.getContentType() + "/" + subscription
						+ "/";

				
				
				  if(!validateSubcsription(options, subscription)){
				  
				  throw new ApplicationException(
				  ServiceConstants.errorMsg_The_Entry +" "+ subscription +
				  "  "+ServiceConstants.errorMsg_doesnt_exist); 
				  } else{ 
					  docList =  refDataDAO.getRefDataListFromLive(options);
					  result.setBase(options.getBaseUri());
				  }
			
				DBCursor cursor = refDataDao.getRefDataList(options);
				if(options.isReverse()){
					cursor.sort(new BasicDBObject("metadata.lastModified",1));
				}
				if (cursor != null  && !(cursor.size() == 0)) {
				validateDocumentCount(cursor,options);
				while(cursor.hasNext()){
					ref = new Ref();
					metadata=(DBObject) cursor.next().get("metadata");
					if(null!=metadata.get(ServiceConstants.eventType) || !(metadata.get(ServiceConstants.eventType).equals(""))){
					ref.setEventType((String) metadata.get("eventType"));
					}
					ref.setHasAttachments("false");
					if(null!=metadata.get(ServiceConstants.lastModified) || !(metadata.get(ServiceConstants.lastModified).equals(""))){
//					long lastModified=(long) metadata.get(ServiceConstants.lastModified);
					ref.setLastModified(String.valueOf(metadata.get(ServiceConstants.lastModified)));
					}
					if(null!=metadata.get(ServiceConstants.priority) || !(metadata.get(ServiceConstants.priority).equals("")) ){
					//int priority=(int) metadata.get(ServiceConstants.priority);
					ref.setPriority(String.valueOf(metadata.get(ServiceConstants.priority)));
					}
					if(null!=metadata.get("docid") || !(metadata.get("docid").equals(""))){
					ref.setHref(options.getContentType()
							+"/"
							+subscription									
							+"/"
							+metadata.get("docid"));
					}
					ref.setType(ServiceConstants.xmlElementType);
					result.getRef().add(ref);
				}
				result.setCount(String.valueOf(cursor.size()));	
				}else
		    		result.setCount("0");	
				result.setBase(options.getBaseUri());
				result.setConsidered("0");
			} 
			catch (OptionsException oe) {
				throw new ApplicationException(oe.getMessage());
			} catch (MongoUtilsException mue) {
				//throw new ApplicationException(mue.getMessage());
				throw new WebApplicationException(mue, 500);
			} 
			catch (MongoException e) {
				//throw new ApplicationException(e.getMessage());
				throw new WebApplicationException(e, 500);
				}
				 catch (ApplicationException e) {
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
	
	org.dom4j.Document document = DocumentHelper.parseText(convertedXML);
	List<org.dom4j.Node> nodesList = document.selectNodes("/result/proj:ref");
	Iterator<org.dom4j.Node> nodesIterator = nodesList.iterator();
	org.dom4j.Document metadata = null;
	while(nodesIterator.hasNext()){
		org.dom4j.Element element = (org.dom4j.Element)nodesIterator.next();
		String[] hrefParts= element.attributeValue("href").split("/");
		options.setDocid(hrefParts[2]);
		InputStream is = new ByteArrayInputStream(getDocumentMetaDataString(options).trim().getBytes("UTF-8"));
		SAXReader reader = new SAXReader();
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		metadata = reader.read(is);
		org.dom4j.Element contentElement = element.addElement("content");
		contentElement.add(metadata.getRootElement());
	}
	
		return document.asXML();
	
	}catch(Exception e){
		e.printStackTrace();
	}
	return null;

}

public String getDocumentMetaDataString(Options options) throws ApplicationException, WebApplicationException {
	
	BufferedReader br = null;
	StringBuilder sb = new StringBuilder();
	String line;
	String xml=null;
	try {
		GridFSDBFile file  = new RefDataDAO().getRefData(options.getContentType(), options.getDocid());
			if (file == null) {
			throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " "
							+ options.getDocid() + " "
							+ ServiceConstants.errorMsg_doesnt_exist);
		}
		InputStream is = file.getInputStream();
		try{
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);			
				}
			xml=sb.toString();			
		}catch (IOException e) {
			throw new ApplicationException(e.getMessage());
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage());
					}
				}
			}
		} catch (MongoUtilsException mue) {
		//throw new ApplicationException(mue.getMessage());
		throw new WebApplicationException(mue, 500);
	} catch (MongoException e) {
		//throw new ApplicationException(e.getMessage());
		throw new WebApplicationException(e, 500);
	
	}

	return xml;

}

@Override
public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException {
	
	if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub))
		throw new ApplicationException(ServiceConstants.STYLESHEET_ERROR_MSG + options.getContentType());
	
	BufferedReader br = null;
	StringBuilder sb = new StringBuilder();
	String line;
	String xml=null;
	Document doc=null;
	try {
		GridFSDBFile file  = new RefDataDAO().getRefData(options.getContentType(), options.getDocid());
			if (file == null) {
			throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " "
							+ options.getDocid() + " "
							+ ServiceConstants.errorMsg_doesnt_exist);
		}
		InputStream is = file.getInputStream();
		try{
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);			
				}
			xml=sb.toString();
			DocumentBuilderFactory factory = 
					   DocumentBuilderFactory.newInstance();
			 doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			 Element root = doc.getDocumentElement();
			 root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			 root.setAttribute("xmlns:proj", "http://www.hp.com/cdsplus");
			 root.setAttribute("xml:base", options.getBaseUri());		
		}catch (IOException e) {
			throw new ApplicationException(e.getMessage());
			} catch (SAXException e) {
			// TODO Auto-generated catch block
				throw new ApplicationException(e.getMessage());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			throw new ApplicationException(e.getMessage());
		}finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage());
					}
				}
			}
		} catch (MongoUtilsException mue) {
		//throw new ApplicationException(mue.getMessage());
		throw new WebApplicationException(mue, 500);
	} catch (MongoException e) {
		//throw new ApplicationException(e.getMessage());
		throw new WebApplicationException(e, 500);
	
	}

	return doc;

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

                                         ref.setHasAttachments(false);		

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
            	    result.setConsidered(new BigInteger("0"));
                    result.setCount(new BigInteger("0"));
                    return result;
              }
       } catch (MongoException e) {
            //throw new ApplicationException(e.getMessage());
  			throw new WebApplicationException(e, 500);
       } catch (MongoUtilsException e) {
            //throw new ApplicationException(e.getMessage());
      		throw new WebApplicationException(e, 500);
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
public Object getVersions(Options serviceDelegateBO) throws ApplicationException, WebApplicationException{

	com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
	com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = null;
	result.setBase(serviceDelegateBO.getBaseUri() == null ? "null"
			: serviceDelegateBO.getBaseUri());
	String subscription = serviceDelegateBO.getSubscription() == null ? "content"
			: serviceDelegateBO.getSubscription();
	String urlLink = serviceDelegateBO.getContentType() + "/"
			+ "content" + "/";

	try {
		
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
			
			ref.setHasAttachments(false);				
			
			if (document.get(ServiceConstants.lastModified) != null)
				ref.setLastModified(new BigInteger(document.get(
						ServiceConstants.lastModified).toString()));
			ref.setType(ServiceConstants.xmlElementType);
			result.setCount(new BigInteger("1"));
			result.setConsidered(new BigInteger("0"));
			result.setRef(ref);
			return result;
		} else {
			result.setConsidered(new BigInteger("0"));
			result.setCount(new BigInteger("0"));
			return result;
		}           		

	} catch (MongoUtilsException mue) {
		//throw new ApplicationException(mue.getMessage());
		throw new WebApplicationException(mue, 500);
	} catch (MongoException e) {
		//throw new ApplicationException(e.getMessage());
		throw new WebApplicationException(e, 500);
	}

}
}
