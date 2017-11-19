/**
 * 
 */
package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.workItem.WorkItem;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.ConnectionPool;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.VirusScanException;
import com.hp.soar.priorityLoader.utils.VirusScanner;


/**
 * @author dahlm
 *
 */
public class SoarWorkItem extends WorkItem {


	private static final String[] EMPTY_STRING_ARRAY = new String[0];


	private SoarExtractCollection extractCollection;

	private IDfSession session;
	private String extractEvent;
	private String tag;

	private String chronicleId;
	private String collectionId;
	private String objectId;
	private String colEvent;

	/**
	 * @param token
	 * @param priority
	 * @param collectionId TODO
	 * @param chronicleId TODO
	 * @param objectId TODO
	 */
	public SoarWorkItem(Long token, Integer priority, String eventType, String collectionId, 
			String chronicleId, String objectId) {
		super(token, priority, eventType);
		// extract event holds WorkItem event types (UPDATE,DELETE,TOUCH)
		extractEvent = eventType;

		colEvent = SoarExtractElement.ITEM_MODIFIED;
		if (eventType == EVENT_DELETE) {
			colEvent = SoarExtractElement.ITEM_DELETED;
		} 

		this.chronicleId = chronicleId;
		this.collectionId = collectionId;
		this.objectId = objectId;


		if (priority == -1) {
			// This event isn't mapped to anything so set it to invalid
			setValid(false);
		}

		tag = token+" - "+collectionId+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	/**
	 * @param token
	 * @param priority
	 * @param collectionId TODO
	 * @param chronicleId TODO
	 * @param objectId TODO
	 * @throws ProcessingException 
	 */
	public SoarWorkItem(Iterator<String> vIt) throws ProcessingException {
		super(vIt);
		// extract event holds WorkItem event types (UPDATE,DELETE,TOUCH)
		extractEvent = eventType;

		colEvent = SoarExtractElement.ITEM_MODIFIED;
		if (eventType == EVENT_DELETE) {
			colEvent = SoarExtractElement.ITEM_DELETED;
		} 

		this.collectionId = vIt.next();
		this.chronicleId = vIt.next();
		this.objectId = vIt.next();


		if (priority == -1) {
			// This event isn't mapped to anything so set it to invalid
			setValid(false);
		}

		tag = token+" - "+collectionId+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	/**
	 * cleanupDirectory 
	 * @param dir
	 */
	private void cleanupDirectory(File dir) {
		if (!dir.exists()) {
			// directory is not there to delete
			return;
		}
		// list files
		File[] files = dir.listFiles();
		for(File f : files) {
			if (f.isDirectory()) {
				cleanupDirectory(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	/**
	 * cleanup cleans up the extraction directory
	 */
	public void cleanup() {
		// clean up the extraction directory
		StringBuffer objPath = new StringBuffer(SoarExtractElement.getContentPath().getAbsolutePath());

		objPath.append(SoarExtractElement.FILE_SEP).append(token.toString());

		cleanupDirectory(new File(objPath.toString()));
	}

	@Override
	public void exit() {
		// Tell the VirusScanners to exit
		VirusScanner.exit();
		super.exit();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getFullTag()
	 */
	public String getFullTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getIdentifier()
	 */
	public String getIdentifier() {
		return collectionId;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getPackageType()
	 */
	public String getPackageType() {
		return "Collection";
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#getTag()
	 */
	public String getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#load()
	 */
	public void load() throws ProcessingException {
		if (!valid) {
			// this collection isn't supposed to load
			LoaderLog.info("WorkItem "+getTag()+" is no longer valid, not loading");      
			return;
		}
		// get a docbase session to work with
		try {
			session = ConnectionPool.getDocbaseSession();
		    // setup the dbservice and get the last time this collection was successfully extracted
			SoarExtractionDBService dbService = new SoarExtractionDBService(session);
			
			try {
				int refListToken = ReferenceLists.getRefTableToken();
				
				// we are valid so construct the DB objects for this extract
				extractCollection = new SoarExtractCollection(dbService, token.toString(), objectId, chronicleId, collectionId, colEvent);

				boolean loadValid = extractCollection.load();
				synchronized(this) {
					setValid(loadValid);
				}
				// finished loading, see if the reference lists have changed and we need to load again
				if (refListToken != ReferenceLists.getRefTableToken()) {
					// The reference lists changed while loading, so need to cause a new load
					throw new ProcessingException("Reference Lists Changed", null, true);
				}
			} catch (DfException e) {
				throw new ProcessingException("Collection extraction Failed: ", e, (++failedCount <= 3));
			} catch (FileNotFoundException e) {
				throw new ProcessingException("Collection extraction Failed: ", e, (++failedCount <= 3));
			} catch (IOException e) {
				throw new ProcessingException("Collection extraction Failed: ", e, (++failedCount <= 3));
			} catch (VirusScanException e) {
				throw new ProcessingException("Virus detected in Collection: ", e, false);
			} catch (InterruptedException e) {
				throw new ProcessingException("Load Interrupted: ", e, (++failedCount <= 3));
			}
		} catch (DfException e) {
			// couldn't get a session, tell it to retry
			throw new ProcessingException("Failed to get docbase session for load. ",e,true);
		} finally {
			try {
				ConnectionPool.releaseDocbaseSession(session);
			} catch (DfException e) {
				throw new ProcessingException("Failed to release docbase session for load. ", e, false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#logStatus(java.lang.String)
	 */
	@Override
	public void logStatus(String status) throws ProcessingException {
		logStatus(status, null);
	}
	
	public void logStatus(String status, Exception exc) throws ProcessingException {
		// get a docbase session to work with
		try {
			session = ConnectionPool.getDocbaseSession();
			try {
				extractCollection.logStatus(session, status, token, exc);
			} catch (DfException e) {
				throw new ProcessingException("Log status failed docbase communication. ", e, false);
			}
		} catch (DfException e) {
			// couldn't get a session, tell it to retry
			throw new ProcessingException("Failed to get docbase session for logging status. ",e,false);
		} finally {
			try {
				ConnectionPool.releaseDocbaseSession(session);
			} catch (DfException e) {
				throw new ProcessingException("Failed to release docbase session for logging. ", e, false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#setPriority(java.lang.Integer)
	 */
	@Override
	public void setPriority(Integer priority) {
		super.setPriority(priority);
		// reset the tag to be on the new priority
		tag = token+" - "+collectionId+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#store()
	 */
	public boolean store() throws ProcessingException {
		extractCollection.store(priority);
		// this loader always stores every workItem so don't need to check for store request
		
		return true;
	}


	public boolean syncStore() {
		// Soar syncs are to cds+ and ftp.  They can all be done in parallel.
		return false;
	}


	public void save(StringBuffer sb)  {
		super.save(sb);
		sb.append(collectionId).append(" ").append(chronicleId).append(" ").append(objectId).append(" ");
	}


}
