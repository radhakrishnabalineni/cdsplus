package com.hp.cdsplus.web.contentservice.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.concentra.bindings.output.schema.marketingstandard.Content;
import com.hp.concentra.bindings.output.schema.marketingstandard.Document;
import com.hp.concentra.bindings.output.schema.marketingstandard.Ref;
import com.hp.concentra.bindings.output.schema.marketingstandard.Result;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;

public class MarketingstandardServiceImpl extends AbstractGenericService {

	ContentDAO contentDAO = new ContentDAO();

	@Override
	public Object getExpandDetails(Options options) {
		// TODO Auto-generated method stub
		DBObject document=null;
		if (options.getExpand() != null) {
			if (options.getExpand().equalsIgnoreCase("true")) {
				Result result = new Result();
				String subscription=options.getSubscription()==null?"content":options.getSubscription();
				Ref ref = new Ref();
				Content content = new Content();
				document=getDocument(options);
				if(document == null){
					throw new ApplicationException(ServiceConstants.errorMsg_The_Entry +" "+ options.getDocid()
                            + "  "+ServiceConstants.errorMsg_doesnt_exist);
				}
				Long lastModified=(Long)document.get("lastModified");
				Integer priority=(Integer) document.get("priority");
				String href= options.getContentType()
						+ "/"+subscription+"/"+document.get("_id");
				if((String) document.get("eventType")!=null)
				ref.setEventType((String) document.get("eventType"));
				if((Boolean) document.get("hasAttachments")!=null)
				ref.setHasAttachments((Boolean) document.get("hasAttachments"));
				if(lastModified!=null)
				ref.setLastModified(BigInteger.valueOf(lastModified));
				if(priority!=null)
				ref.setPriority(BigInteger.valueOf(priority));
				if(href!=null)
				ref.setHref(href);
				ref.setType(ServiceConstants.xmlElementType);
				Object Obj = getGenericExpandTrue(options);
				content.setDocument((Document) Obj);
				ref.setContent(content);
				result.setBase(options.getBaseUri());
				result.setConsidered(new BigInteger("0"));
				result.setCount(new BigInteger("1"));
				result.getRef().add(ref);
				String str=convertObjectToXml("Result",result,options);
				str = removeIndentation(str);
				return str;

			}

		}

		return getDocumentExpandDetails(options);
	}

	@Override
	public Object getDocumentMetaData(Options options)
			throws ApplicationException, WebApplicationException {
		
		 if( options.getSubscription()!=null && options.getSubscription().equals("fastxml")){
			 return this.getDocumentObject(getFASTXMLDocument(options));
		 }
		 if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub)){
			 return stylesheetUtil.getStylesheetXMLDocument(options);
		 }
		String doc = null;
		Document documentXml = (Document) super.getDocumentMetaData(options);
		documentXml.setBase(options.getBaseUri());
		doc=convertObjectToXml(options, documentXml);
		doc= removeIndentation(doc);
		return doc;

		
//		if(options.getSubscription()!=null && !("content").equals(options.getSubscription())){
//			org.w3c.dom.Document domDocument = updateExtraPropertyView(doc,options.getDocid());
//			doc = changeDOMObjectToXml(domDocument);
//		}	
		
	}
	
	@Override
	public InputStream getFASTXMLDocument(Options options) {
		return super.getFASTXMLDocument(options);		
	}

	@Override
    public Object getExpandedDocumentList(Options options) {
           DBCursor docList = null;          
           Ref ref = null;
           ArrayList<Ref> refs = new ArrayList<Ref>();
           Result result = new Result();
           try {
        	      if(options.getLimit()>10000)
        	    	 options.setLimit(10000); 
        	      else if(options.getLimit()==0)
                     options.setLimit(20);
                  
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
                               ref.setHasAttachments(docObject.get(ServiceConstants.hasAttachments)==null ? null:Boolean.valueOf(docObject.get(ServiceConstants.hasAttachments).toString()));
                               ref.setLastModified(docObject.get(ServiceConstants.lastModified)==null ? null:BigInteger.valueOf(Long.valueOf(docObject.get(ServiceConstants.lastModified).toString())));
                               ref.setPriority(docObject.get(ServiceConstants.priority)==null ? BigInteger.valueOf(4l):BigInteger.valueOf(Long.valueOf(docObject.get(ServiceConstants.priority).toString())));
                               /*
                               if(ref.getEventType()!=null){
                                      if(ref.getEventType().equalsIgnoreCase("delete"))
                                             ref.setStatus(docObject.get(ServiceConstants.eventType).toString());
                               }*/
                               ref.setType(ServiceConstants.xmlElementType);
                               ref.setHref(urlLink + docObject.get(ServiceConstants.id));
                               Content content = new Content();
                               options.setDocid(docObject.get(ServiceConstants.id).toString());
                               content.setDocument((Document) super.getDocumentMetaData(options));
                               ref.setContent(content);
                               refs.add(ref);
                        }
                        result.getRef().addAll(refs);
                        result.setCount(BigInteger.valueOf(Long.valueOf(String.valueOf(docList.size()))));
                  }else
                	  result.setCount(new BigInteger("0"));
                  
                  result.setConsidered(new BigInteger("0"));
                  
                  options.setDocid(null);
                  String str=convertObjectToXml("Result",result,options);          		
          		  return str;
           } catch (OptionsException oe) {
                  throw new ApplicationException(oe.getMessage());
           } catch (MongoUtilsException mue) {
                //throw new ApplicationException(mue.getMessage());
      			throw new WebApplicationException(mue, 500);
           } catch (MongoException e) {
                //  throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } 
    }

	/**
	 *SMO : Method added to convert Input Stream to String 
	 * @param is Input Stream
	 * @return
	 */
	private String getDocumentObject(InputStream is) {
		
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		//InputStream is = gridFSDBFile.getInputStream();
		try{
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}catch (IOException e) {
				e.printStackTrace();
		}finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		}
		return sb.toString();
	}
}
