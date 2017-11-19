/**
 * 
 */
package com.hp.concentra.extractor.workItem;

import java.io.IOException;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.core.session.SessionException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.documents.SupportCommMergerException;
import com.hp.concentra.extractor.utils.ExtractorSessionPool;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.workItem.WorkItem;


/**
 * @author dahlm
 *
 */
public class ConcentraWorkItem extends WorkItem {



	private ConcentraExtractDocument extractDocument;

	private ConcentraAdminSession session;
	private String tag;

	//  Only used for testing Priority Loader logic:  private boolean test = true;

	/**
	 * @param token
	 * @param priority
	 * @param objectName TODO
	 * @param chronicleId TODO
	 * @param objectId TODO
	 * @param isDeleted indicates the document is archived (deleted)
	 */
	public ConcentraWorkItem(Long token, Integer priority, String eventType, String objectName, 
			String chronicleId, String objectId, String objectType, boolean isDeleted) {
		super(token, priority, eventType);

		// event is valid if it could be looked up, is the right type of object, and it is not an update/touch on a deleted document
		valid = (priority != -1 && ConcentraExtractElement.isValidObjectType(objectType)) && !(isDeleted && (eventType != EVENT_DELETE));
		
		extractDocument = new ConcentraExtractDocument(token.toString(), eventType, objectName, chronicleId, objectId, objectType);

		tag = token+" - "+objectName+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	public ConcentraWorkItem(Iterator<String> vIt) throws ProcessingException {
		super(vIt);

		extractDocument = new ConcentraExtractDocument(token.toString(), eventType, vIt);
		valid = (priority != -1 && ConcentraExtractElement.isValidObjectType(extractDocument.getObjectType()));

		tag = token+" - "+extractDocument.getObjectName()+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	/**
	 * cleanup is called by external controller
	 * @throws IOException 
	 */
	public void cleanup() {
		// TODO: Only used to test PriorityLoader logic
		//    if (test) {
		//      return;
		//    }
		// clean up the extraction directory
		try {
			extractDocument.cleanup();
		} catch (IOException e) {
			LoaderLog.info("WorkItem "+getTag()+" : "+e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getFullTag()
	 */
	public String getFullTag() {
		return tag +" "+ extractDocument.getObjectType();
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return extractDocument.getObjectName();
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getPackageType()
	 */
	@Override
	public String getPackageType() {
		return "Document";
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getTag()
	 */
	@Override
	public String getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#load()
	 */
	@Override
	public void load() throws ProcessingException {
		if (!valid) {
			LoaderLog.info("WorkItem "+getTag()+" is no longer valid, not loading");
			return;
		}

		// TODO: Used to test PriorityLoader logic
		//    if (test) {
		//      Double sleepTime = (Math.random() * 15) % 15;
		//      int multiplier = (eventType == EVENT_UPDATE) ? 100 : 1;
		//      try {
		//        Thread.sleep(sleepTime.longValue() * multiplier);
		//      } catch (InterruptedException ie) {}
		//      return;
		//    }
		//    
		// get a docbase session to work with
		try {
			session = ExtractorSessionPool.getDocbaseSession();
			try {
				boolean loadValid = extractDocument.load(session, priority, eventType); 
				synchronized(this) {
					setValid(loadValid);
				}
				
			} catch (DfException e) {
				throw new ProcessingException("DocumentExtraction Failed docbase communication. ", e, false);
			} catch (IOException e) {
				throw new ProcessingException("DocumentExtraction Failed IO. ", e, false);
			} catch (DmRepositoryException e) {
				throw new ProcessingException("DocumentExtraction Failed DmRepository. ", e, false);
			} catch (SupportCommMergerException e) {
				throw new ProcessingException("DocumentExtraction Failed SupportCommMerger. ", e, false);
			} catch (DocumentException e) {
				throw new ProcessingException("DocumentExtraction Failed DocumentException. ", e, false);
			} catch (SAXException e) {
				throw new ProcessingException("DocumentExtraction Failed SAXException. ", e, false);
			}
		} catch (SessionException e) {
			// couldn't get a session, tell it to retry
			throw new ProcessingException("Failed to get docbase session for load. ",e,true);
		} finally {
			ExtractorSessionPool.releaseDocbaseSession(session);
		}
		
	}

	//	/**
	//	 * loadItems creates the list of projectContent items for
	//	 * @param session
	//	 * @throws ProcessingException
	//	 * @throws DfException 
	//	 * @throws IOException 
	//	 */
	//	private void loadItems() throws ProcessingException, DfException, IOException {
	//		SoarExtractorService extractorService = new SoarExtractorService(this);
	//		extractedItems = extractorService.extractDocument();
	//	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#logStatus(java.lang.String)
	 */
	@Override
	public void logStatus(String status) throws ProcessingException {
		// Used to test PriorityLoader logic
		//    if (test) {
		//      return;
		//    }

		// get a docbase session to work with
		try {
			extractDocument.logStatus(status, null);
		} catch (DfException e) {
			throw new ProcessingException("Log status failed DfException. ", e, false);
		} catch (DmRepositoryException e) {
			throw new ProcessingException("Log status failed DmRepositoryException. ", e, false);
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#setPriority(java.lang.Integer)
	 */
	@Override
	public void setPriority(Integer priority) {
		super.setPriority(priority);
		// reset the tag to be on the new priority
		tag = token+" - "+extractDocument.getObjectName()+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#store()
	 */
	@Override
	public boolean store() throws ProcessingException {
		// Used to test PriorityLoader logic
		//    if (test) {
		//      Double sleepTime = (Math.random() * 15) % 15;
		//      int multiplier = (eventType == EVENT_UPDATE) ? 100 : 10;
		//      try {
		//        Thread.sleep(sleepTime.longValue() * multiplier);
		//      } catch (InterruptedException ie) {}
		//      return;
		//    }
		extractDocument.store(getPriority());
		// Concentra store always stores, no zip files
		
		return true;
	}


	@Override
	public void logStatus(String status, Exception e) throws ProcessingException {

		try {
			extractDocument.logStatus(status, e);
		} catch (DfException dfe) {
			throw new ProcessingException("Log status failed DfException. ", dfe, false);
		} catch (DmRepositoryException dmre) {
			throw new ProcessingException("Log status failed DmRepositoryException. ", dmre, false);
		}

	}


	@Override
	public boolean syncStore() {
		//Concentra sync's to CDS+ which can all be done in parallel
		return false;
	}
	
	public void save(StringBuffer sb)  {
		super.save(sb);
		extractDocument.save(sb);
	}

}
