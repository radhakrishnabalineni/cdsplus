/**
 * 
 */
package com.hp.cdsplus.web.delegate;

import java.io.InputStream;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.web.exception.ApplicationException;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 *
 */
public interface ServiceDelegateManager {
	
	
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getContentList(Options options) throws ApplicationException, WebApplicationException;
	
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getSubcriptionList(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException;	
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getExpandedDocumentList(Options options) throws ApplicationException;	
	/**
	 * @param options
	 * @return String
	 * @throws ApplicationException
	 */
	public Object getDocumentMetaData(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getDocumentAttachments(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return InputStream
	 * @throws ApplicationException
	 */
	
	public InputStream getDocumentAttachment(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object taskSubcheck(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getExpandDetails(Options options) throws ApplicationException, WebApplicationException;
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getVersions(Options options) throws ApplicationException, WebApplicationException;
	
	/**
	 * @param options
	 * @return Object
	 * @throws ApplicationException
	 */
	public Object getGenericExpandDetails(Options options) throws ApplicationException, WebApplicationException;

	public Object putDocumentMetadata(Options options)throws ApplicationException, WebApplicationException;

	public Object putDocumentContent(Options options)throws ApplicationException, WebApplicationException;
	
	}
