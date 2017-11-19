/**
 * 
 */
package com.hp.cdsplus.web.delegate.impl;

import java.io.InputStream;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.web.contentservice.ContentService;
import com.hp.cdsplus.web.contentservice.impl.AppServiceImpl;
import com.hp.cdsplus.web.delegate.ServiceDelegateManager;
import com.hp.cdsplus.web.delegate.factory.ObjectFactory;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 * 
 */
public class ServiceDelegateManagerImpl implements ServiceDelegateManager {

	
	private  ContentService getServiceInstance(Options options) {

		try {
			String contentType = getContentType(options);
			if (null != contentType) {
				ObjectFactory serviceFactory = new ObjectFactory();
				ContentService serviceImplInstance = serviceFactory
						.getServiceInstance(contentType);

				if (null != serviceImplInstance)
					return serviceImplInstance;
				else
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_Entry + " "
									+ contentType + " "
									+ ServiceConstants.errorMsg_NotFound);
			} else {

				throw new ApplicationException("Options should not be null");
			}
		} catch (ApplicationException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	private String getContentType(Options delegatorBO) {

		String contentType = null;
		if (delegatorBO != null) {

			if (delegatorBO != null
					|| delegatorBO != null) {

				if (delegatorBO.getContentType() != null) {
					contentType = delegatorBO.getContentType();

				} 

			}

		}

		return contentType;
	}
	
	
	@Override
	public Object getContentList(Options options) throws ApplicationException, WebApplicationException {
	
		AppServiceImpl serviceImpl=new AppServiceImpl();
		
		return serviceImpl.getContentList(options);
		
	} 
	
	@Override
	public Object getSubcriptionList(Options options) throws ApplicationException, WebApplicationException {
		ContentService serviceImpl=getServiceInstance(options);
		
		return serviceImpl.getSubcriptionList(options);
	}

	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		
		return serviceImpl.getDocumentList(options);
	}
	
	@Override
    public Object getExpandedDocumentList(Options options) throws ApplicationException{
        ContentService serviceImpl=getServiceInstance(options);       
           
        return serviceImpl.getExpandedDocumentList(options);
    }


	@Override
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		
		return serviceImpl.getDocumentMetaData(options);
	}

	@Override
	public Object getDocumentAttachments(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		
		return serviceImpl.getDocumentAttachments(options);
	}


	@Override
	public Object taskSubcheck(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		return serviceImpl.getTaskSubcheck(options);
	}

	@Override
	public Object getExpandDetails(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		return serviceImpl.getExpandDetails(options);
	}

	@Override
	public InputStream getDocumentAttachment(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		return serviceImpl.getDocumentAttachment(options);
	}
	
	@Override
	public Object getVersions(Options options) throws ApplicationException, WebApplicationException{
		ContentService serviceImpl=getServiceInstance(options);
		return serviceImpl.getVersions(options);
	}
	
	public Object getGenericExpandDetails(Options options)
			throws ApplicationException {
		ContentService serviceImpl=getServiceInstance(options);
		return serviceImpl.getGenericExpandDetails(options);
	}

	@Override
	public Object putDocumentMetadata(Options options) {
		ContentService serviceImpl = getServiceInstance(options);
		return serviceImpl.putDocumentMetadata(options);
	}
	
	@Override
	public Object putDocumentContent(Options options) {
		ContentService serviceImpl = getServiceInstance(options);
		return serviceImpl.putDocumentContent(options);
	}

}
