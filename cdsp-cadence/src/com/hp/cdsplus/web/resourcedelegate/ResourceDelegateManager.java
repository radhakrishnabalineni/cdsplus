package com.hp.cdsplus.web.resourcedelegate;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.hp.cdsplus.web.delegate.ServiceDelegateManager;
import com.hp.cdsplus.web.delegate.impl.ServiceDelegateManagerImpl;
import com.hp.cdsplus.web.model.ServiceDelegateBO;
import com.hp.cdsplus.web.util.MimeTypes;
import com.hp.cdsplus.web.util.ServiceConstants;


/**
 * @author reddypm
 *
 */

public class ResourceDelegateManager {

	
	
	public Response resourceDelegate(ServiceDelegateBO serviceDelegateBO) {
		
		switch (serviceDelegateBO.getWebOptions().getRequestMethod()){
		case GET:
			return doGET(serviceDelegateBO);
		case PUT:
			return doPUT(serviceDelegateBO);
		case POST:
		default:
			return Response.status(Status.FORBIDDEN).build();
		}
	}

	private Response doPUT(ServiceDelegateBO serviceDelegateBO) {
		ServiceDelegateManager serviceDelegateManager=new ServiceDelegateManagerImpl();
		switch (serviceDelegateBO.getWebOptions().getMaxLevel()) {
		case 0:
		case 1:
		case 2:
		case 3:
			return Response.status(Status.FORBIDDEN).build();
		case 4:
			return Response.ok(serviceDelegateManager.putDocumentMetadata(serviceDelegateBO.getOptions())).build();
		case 5:
			return Response.ok(serviceDelegateManager.putDocumentContent(serviceDelegateBO.getOptions())).build();
		}
		return null;
	}

	private Response doGET(ServiceDelegateBO serviceDelegateBO) {
		
		ServiceDelegateManager serviceDelegateManager=new ServiceDelegateManagerImpl();
		CacheControl defaultCache = new CacheControl();
		defaultCache.setMaxAge(120);
		
		switch (serviceDelegateBO.getWebOptions().getMaxLevel()) {
		case 0:
			CacheControl appCache0 = new CacheControl();
			appCache0.setMaxAge(60);
			return Response.ok(serviceDelegateManager.getContentList(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(appCache0).build();
		case 1:
			CacheControl appCache1 = new CacheControl();
			appCache1.setMaxAge(60);
			
			return Response.ok(serviceDelegateManager.getContentList(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(appCache1).build();
		
			
		case 2:
			return Response.ok(serviceDelegateManager.getSubcriptionList(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
			// get document list
	
		case 3:		
			if(serviceDelegateBO.getWebOptions().getWildCard()==null)
			{
				serviceDelegateBO.getOptions().setLimit(20);
			}
			/*25th June 2014, Reverted due to issue reported by idf_201
			if(serviceDelegateBO.getOptions().getLimit()==0){
				String subscription = serviceDelegateBO.getOptions().getSubscription()==null?"content":serviceDelegateBO.getOptions().getSubscription();
				String message = ServiceConstants.errorMsg_The_URI+" "+
						serviceDelegateBO.getOptions().getBaseUri()+serviceDelegateBO.getOptions().getContentType()+"/"
						+subscription+"/*limit=0"+" "+ServiceConstants.errorMsg_isInvalid;
				
				return Response.status(Status.NO_CONTENT).entity(message)
						.type(MimeTypes.getMimeTypes("c")).build();
			}*/
			if (serviceDelegateBO.getOptions().getExpand() != null)
			{
	             if(serviceDelegateBO.getOptions().getExpand().equalsIgnoreCase("true")){
	                    return Response.ok(serviceDelegateManager.getExpandedDocumentList(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
	             }

				 return Response.ok(serviceDelegateManager.getExpandDetails(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
			}
			return Response.ok(serviceDelegateManager.getDocumentList(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
			
		
		case 4:
			
			if (serviceDelegateBO.getOptions().getExpand() != null)
				
				 return Response.ok(serviceDelegateManager.getExpandDetails(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
	
			else if (serviceDelegateBO.getOptions().getTask() != null)

				return Response.ok(serviceDelegateManager.taskSubcheck(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();

			 else if(serviceDelegateBO.getOptions().getVersions()!=null)
				 
				 return Response.ok(serviceDelegateManager.getVersions(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
             
			 else if(serviceDelegateBO.getOptions().isIncludeDeletes() == true)
				 
				 return Response.ok(serviceDelegateManager.getGenericExpandDetails(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
			
			/* else if( serviceDelegateBO.getOptions().getSubscription()!=null && serviceDelegateBO.getOptions().getSubscription().equals("fastxml"))
				 
				 return Response.ok(serviceDelegateManager.getFASTXMLDocument(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
*/
			 else  if(serviceDelegateBO.getOptions().getSubscription() !=null &&
					 (serviceDelegateBO.getOptions().getSubscription().equalsIgnoreCase("bschtml") ||
					  serviceDelegateBO.getOptions().getSubscription().equalsIgnoreCase("blossom1") ||
					  serviceDelegateBO.getOptions().getSubscription().equalsIgnoreCase("html")))
				 
				 return Response.ok(serviceDelegateManager.getDocumentMetaData(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("html")).cacheControl(defaultCache).build();
			
			 else
				return Response.ok(serviceDelegateManager.getDocumentMetaData(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();

			// for document attachments access
		case 5:
			if (serviceDelegateBO.getWebOptions().getWildCard() != null)

				return Response.ok(serviceDelegateManager.getDocumentAttachments(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("xml")).cacheControl(defaultCache).build();
			else{
			String attachmentName = 	serviceDelegateBO.getOptions().getAttachmentName();
			String fileExt=attachmentName.substring(attachmentName.lastIndexOf('.')+1);
			
			if(MimeTypes.getMimeTypes(fileExt)!=null)
				return Response.ok(serviceDelegateManager.getDocumentAttachment(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes(fileExt)).cacheControl(defaultCache).build();
			else
				return Response.ok(serviceDelegateManager.getDocumentAttachment(serviceDelegateBO.getOptions())).type(MimeTypes.getMimeTypes("wildcard")).cacheControl(defaultCache).build();
			}
		default :
			
			return Response.status(Status.NOT_FOUND).entity(ServiceConstants.errorMsg_The_Entry+" "+ServiceConstants.errorMsg_doesnt_exist)
					.type(MimeTypes.getMimeTypes("c")).build();
		}
	}

	
}
