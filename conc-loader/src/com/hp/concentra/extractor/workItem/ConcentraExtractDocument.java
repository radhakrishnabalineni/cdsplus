/**
 * 
 */
package com.hp.concentra.extractor.workItem;

import java.io.IOException;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;


import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.hp.cdspDestination.ProjContent;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.DocbaseUtils;
import com.hp.concentra.extractor.documents.ConcentraDoc;
import com.hp.concentra.extractor.documents.ConcentraDocFactory;
import com.hp.concentra.extractor.documents.SupportCommMergerException;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.MappingHandler;

import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.workItem.WorkItem;

/**
 * @author dahlm
 *
 */
public class ConcentraExtractDocument extends ConcentraExtractElement {

  /**
   * 
   * @param eventId
   * @param eventType
   * @param objectName
   * @param chronicleId
   * @param objectId
   * @param objectType
   */
  public ConcentraExtractDocument(String eventId, String eventType, String objectName, String chronicleId, String objectId, String contentType) {
    super(eventId, eventType, objectName, chronicleId, objectId, contentType);
  }

  public ConcentraExtractDocument(String eventId, String eventType, Iterator<String> vIt){
	    super(eventId, eventType, vIt.next(), vIt.next(), vIt.next(), vIt.next());
  }

  /**
   * 
   * @param session
   * @param priority 
   * @param eventType update/delete/touch event
   * @throws DfException
   * @throws ProcessingException
   * @throws IOException
   * @throws JDOMException 
   * @throws SupportCommMergerException 
   * @throws DmRepositoryException 
   * @throws SAXException 
   * @throws DocumentException 
   */
  public boolean load(ConcentraAdminSession session, Integer priority, String eventType) throws DfException, ProcessingException, IOException, DmRepositoryException, SupportCommMergerException, DocumentException, SAXException {
    // touch and delete events require no load
    if ( eventType == WorkItem.EVENT_TOUCH ) {
      // touch event
      return loadPostContent(session);
    } else if ( eventType == WorkItem.EVENT_UPDATE ) {
      // update event
      return loadPutContent(session, priority);
    } else if ( eventType == WorkItem.EVENT_DELETE ) {
      // delete event
      return loadDeleteContent(session);
    } else {
      throw new ProcessingException( " The event type of '" + eventType +"' is not a valid option\n", false );
    }    
  }
  
  /**
   * getExtension returns the dos extension of the file type for the document
   * @param session
   * @param sb
   * @return
   * @throws DmRepositoryException
   * @throws DfException
   */
  private boolean isXML(ConcentraAdminSession session, StringBuffer sb) throws DmRepositoryException, DfException {
    // get the document extension to determine type.
    sb.setLength(0);
    sb.append("select a_content_type from c_base_object where r_object_id='");
    sb.append(objectId).append("'");
    IDfCollection results = null;

    try {
      results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.EXEC_QUERY, "getContentType");
      if (results != null && results.next()) {
        String extension = results.getString("a_content_type");
        if (extension.equals("xml")) {
          return true;
        }
      }
    } finally {
      DocbaseUtils.closeCollection(results);
    }
    return false;
  }
  
  /**
   * loadDeleteContent sets up for a delete update (assumes XML delete as well)
   * @throws ProcessingException 
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  private boolean loadDeleteContent(ConcentraAdminSession session) throws ProcessingException, DmRepositoryException, DfException {
    
    // build the property location
    ProjContent pc = new ProjContent(getCDSPlusContentType(), objectName + ConcentraExtractElement.XML_EXTENSION, null);

    // add Property location for delete
    // clear the buffer
    StringBuffer sb = new StringBuffer(getCDSPlusContentType());
    sb.append(ConcentraExtractElement.LOADER);
    sb.append(ProjContent.normalizeContentNameForUrl(pc.getExtentionlessContentName()));
    pc.setPath(sb.toString());
    addProjContent(pc);      
    
    // see if content needs to deleted as it is in content directory when xml
    if (isXML(session, sb)) {

      // add Content location for delete (doing this in a blanket way.  it may not exist)
      pc = new ProjContent(getCDSPlusContentType(), objectName+ConcentraExtractElement.XML_EXTENSION, null);
    
      sb.setLength(0);
      sb.append(getCDSPlusContentType());
      sb.append(ConcentraExtractElement.CONTENT);
      sb.append(ProjContent.normalizeContentNameForUrl(pc.getExtentionlessContentName()));
      pc.setPath(sb.toString());
      addProjContent(pc);      
    }
    return true;
  }

  /**
   * loadPostContent sets up for a touch update
   * @throws ProcessingException 
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  private boolean loadPostContent(ConcentraAdminSession session) throws ProcessingException, DmRepositoryException, DfException {
    //Declares the touch attribute as true, just for update the last date modified
    String content = "touch=true";
    byte[] propBytes = content.getBytes();
    
    // build the property location
    ProjContent pc = new ProjContent(getCDSPlusContentType(), objectName + ConcentraExtractElement.XML_EXTENSION, propBytes);
    
    // add Property location for touch
    StringBuffer sb = new StringBuffer(getCDSPlusContentType());
    sb.append(ConcentraExtractElement.LOADER);
    sb.append(ProjContent.normalizeContentNameForUrl(pc.getExtentionlessContentName()));
    pc.setPath(sb.toString());
    addProjContent(pc);      
    
    // see if content needs to touched as it is in content directory when xml
    if (isXML(session, sb)) {
      // add Content location for touch as content is in content directory
      pc = new ProjContent(getCDSPlusContentType(), objectName+ConcentraExtractElement.XML_EXTENSION, propBytes);
    
      sb.setLength(0);
      sb.append(getCDSPlusContentType());
      sb.append(ConcentraExtractElement.CONTENT);
      sb.append(ProjContent.normalizeContentNameForUrl(pc.getExtentionlessContentName()));
      pc.setPath(sb.toString());
      addProjContent(pc);      
    }
    
    return true;
  }

  /**
   * 
   * @param session
   * @param priority
   * @param eventType
   * @return
   * @throws DfException
   * @throws ProcessingException
   * @throws IOException
   * @throws DmRepositoryException
   * @throws SupportCommMergerException
   * @throws SAXException 
   * @throws DocumentException 
   */
  private boolean loadPutContent(ConcentraAdminSession session, Integer priority) throws DfException, ProcessingException, IOException, DmRepositoryException, SupportCommMergerException, DocumentException, SAXException {
    // get the specific document that is supposed to be loaded.
    StringBuffer dql = new StringBuffer();
    String colId = null;
    String activeFlag = "true";
    
    dql.append("select c.r_object_id as col_id, c.active_flag as active_flag from c_base_object (all) b, c_base_col c where b.r_object_id='");
    dql.append(objectId).append("' and any b.r_version_label='FINAL' and c.r_object_id=b.col_id and b.archived_flag=0");
    
    IDfCollection results = null;
    try {
      results = DocbaseUtils.executeQuery(session, dql.toString(), IDfQuery.EXEC_QUERY, "checkDocIsNotStale");
      if (results == null || !results.next()) {
        // This document is stale so 
        LoaderLog.info("Token "+eventId+" is stale or archived.  Not Extracting");
        return false;
      }
      // get the collection ID
      colId = results.getString("col_id");
      String tmpActiveFlag = results.getString("active_flag");
      activeFlag = ("1".equals(tmpActiveFlag) ? "true" : "false");
    } catch (DmRepositoryException e) {
      // Can't check the status of this doc, are we hosed, or just disconnected
      throw new ProcessingException ("Failed to get base info for doc "+getIdentifier(), true);
    } finally {
      DocbaseUtils.closeCollection(results);
    }
    
    // now call getPropertyFile and getContent for that document
    ConcentraDoc doc = ConcentraDocFactory.getDocumentInstance(contentType, objectId, chronicleId, eventId, colId, priority, activeFlag);
    if (doc != null) {
      doc.setSession(session);
      return doc.extract(this);
    } else {
      throw new ProcessingException("Invalid content_type "+contentType, false);
    }
  }

  /* (non-Javadoc)
   * @see com.hp.soar.priorityLoader.workItem.ConcentraExtractElement#getIdentifier()
   */
  @Override
  protected String getIdentifier() {
    return objectName;
  }

  /**
   * logStatus puts entries into the docbase for the results of an extraction
   * @param status
   * @param e Exception that was thrown if there was a problem
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  public void logStatus(String status, Exception e) throws DfException, DmRepositoryException  {
    // update the extractor_events table that this event was extracted if success
    StringBuffer dql = new StringBuffer();

    // update the c_event_log for this collection
    logConcentraEvent(dql, status);

  }
  
	public void save(StringBuffer sb)  {
		sb.append(objectName).append(" ").append(chronicleId).append(" ").append(objectId).append(" ").append(contentType).append(" ");
	}

}
