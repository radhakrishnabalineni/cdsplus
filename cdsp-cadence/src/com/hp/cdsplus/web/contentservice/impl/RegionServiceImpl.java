package com.hp.cdsplus.web.contentservice.impl;

import java.math.BigInteger;
import java.util.ArrayList;

import com.hp.cdsplus.bindings.output.schema.regions.Content;
import com.hp.cdsplus.bindings.output.schema.regions.Ref;
import com.hp.cdsplus.bindings.output.schema.regions.Region;
import com.hp.cdsplus.bindings.output.schema.regions.Result;
import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.RefDataDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;


public class RegionServiceImpl extends AbstractGenericService {

	ContentDAO contentDAO = new ContentDAO();
	ConversionUtils conversion = new ConversionUtils();
	@Override
	public Object getExpandDetails(Options options) {
		DBObject region = null;
		if (options.getExpand() != null) {
			if (options.getExpand().equalsIgnoreCase("true")) {
				Result result = new Result();
				Ref ref = new Ref();
				Content content = new Content();
				region = getDocument(options);
				if (region == null) {
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_Entry + " "
									+ options.getDocid() + "  "
									+ ServiceConstants.errorMsg_doesnt_exist);
				}
				Long lastModified = (Long) region.get("lastModified");
				Integer priority = (Integer) region.get("priority");
				String subscription = options.getSubscription() == null ? "content"
						: options.getSubscription();
				String href = options.getContentType() + "/" + subscription
						+ "/" + region.get("_id");
				if ((String) region.get("eventType") != null)
					ref.setEventType((String) region.get("eventType"));
				if ((String) region.get("hasAttachments") != null)
					ref.setHasAttachments((Boolean) region
							.get("hasAttachments"));
				if (lastModified != null)
					ref.setLastModified(BigInteger.valueOf(lastModified));
				if (priority != null)
					ref.setPriority(BigInteger.valueOf(priority));
				if (href != null)
					ref.setHref(href);
				ref.setType(ServiceConstants.xmlElementType);
				Object Obj = getGenericExpandTrue(options);
				content.setRegion((Region) Obj);
				ref.setContent(content);
				result.setRef(ref);
				result.setCount(new BigInteger("1"));
				result.setConsidered(new BigInteger("0"));
				result.setBase(options.getBaseUri());
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

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());
		String doc = null;
		 
		//SMO : code changes to allow only hpicontent and hpecontent...story no is-8077
		try {
			String subscription1 = options.getSubscription() == null ? "content"
					: options.getSubscription();
			String urlLink = options.getContentType() + "/" + subscription1
					+ "/";
			
			if(!validateSubcsription(options, subscription1)){
			  
			  throw new ApplicationException(
			  ServiceConstants.errorMsg_The_Entry +" "+ subscription1 +
			  "  "+ServiceConstants.errorMsg_doesnt_exist); 
			  }
			else{
			options.setSubscription(null);
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Region documentXml = (Region) super.getDocumentMetaData(options);
		documentXml.setBase(options.getBaseUri());
		doc = convertObjectToXml(options, documentXml);
		return doc;
	}
	
	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException {

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		RefDataDAO refDataDAO = new RefDataDAO();
		DBCursor docList = null;
		com.hp.cdsplus.bindings.output.schema.subscription.Ref ref = null;
		ArrayList<com.hp.cdsplus.bindings.output.schema.subscription.Ref> refs = new ArrayList<com.hp.cdsplus.bindings.output.schema.subscription.Ref>();
		com.hp.cdsplus.bindings.output.schema.subscription.Result result = new com.hp.cdsplus.bindings.output.schema.subscription.Result();
		try {
			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			String urlLink = options.getContentType() + "/" + subscription
					+ "/";

			System.out.println(subscription);
			
			  if(!validateSubcsription(options, subscription)){
			  
			  throw new ApplicationException(
			  ServiceConstants.errorMsg_The_Entry +" "+ subscription +
			  "  "+ServiceConstants.errorMsg_doesnt_exist); 
			  } else{ 
				  docList =  refDataDAO.getRefDataListFromLive(options);
				  result.setBase(options.getBaseUri());
			  }
			 
			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new com.hp.cdsplus.bindings.output.schema.subscription.Ref();
					ref.setEventType(docObject.get(ServiceConstants.eventType) == null ? null
							: docObject.get(ServiceConstants.eventType)
									.toString());
					ref.setHasAttachments("false");
					ref.setLastModified(docObject
							.get(ServiceConstants.lastModified) == null ? null
							: docObject.get(ServiceConstants.lastModified)
									.toString());
					ref.setPriority(docObject.get(ServiceConstants.priority) == null ? null
							: docObject.get(ServiceConstants.priority)
									.toString());
					if (ref.getEventType().equalsIgnoreCase("delete"))
						ref.setStatus(docObject.get(ServiceConstants.eventType)
								.toString());
					ref.setType(ServiceConstants.xmlElementType);
					ref.setHref(urlLink + docObject.get(ServiceConstants.id));
					refs.add(ref);
				}
				result.getRef().addAll(refs);
				result.setCount(String.valueOf(docList.size()));
			} else
				result.setCount("0");
			result.setConsidered("0");
			return result;
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		} 
		 catch (ApplicationException e) {
				throw new ApplicationException(e.getMessage());
		 }
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
