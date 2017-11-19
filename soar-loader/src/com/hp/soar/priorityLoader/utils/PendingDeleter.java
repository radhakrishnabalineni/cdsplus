package com.hp.soar.priorityLoader.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.loader.utils.ConfigurationReader;


/**
 * PendingDeleter is a thread that handles all of the pending deletes for the binary files in Soar
 * 
 * @author dahlm
 *
 */
public class PendingDeleter implements Runnable {

	private static PendingDeleter deleter;
	
	private LinkedList<PendingDelete> deletes = new LinkedList<PendingDelete>();
	
	private Thread t;
	
	private PendingDeleter() {
		// get the persisted deletes back into the queue
		IDfCollection results = null;
		try {
			LoaderLog.debug("Loading persisted delay deletes");
			IDfSession session = ConnectionPool.getDocbaseSession();
			StringBuffer sb = new StringBuffer("select * from dm_dbo.soar_pending_sw_deletes order by execute_time asc");
			try {
				results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.DF_EXEC_QUERY, "GetPendingDeletes");
				if (results != null) {
					while(results.next()) {
						PendingDelete pd = new PendingDelete(results);
						deletes.add(pd);
					}
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
		} catch (DfException e) {
			LoaderLog.error("Failed to get docbase session for getting pending deletes: "+e.getMessage());
		}
		
		// setup the thread and start it
		t = new Thread(this,"DEL");
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * add a new delete to the system
	 * @param pd
	 * @throws DfException 
	 */
	private void addSwDelete(PendingDelete pd) {
		try {
			pd.persist();
		} catch (DfException e) {
			LoaderLog.error(LoaderLog.getExceptionMsgForLog("Failed to persist "+pd, e));
		}
		
		synchronized (deletes) {
			deletes.add(pd);
			// make sure the thread is ready to process it.
			deletes.notifyAll();
		}
	}
	
	/**
	 * remove a scheduled delete
	 * @param pd
	 * @throws DfException 
	 */
	private void removeSwDelete(String swPath) {
		if (swPath == null) {
			return;
		}
		synchronized (deletes) {
			Iterator<PendingDelete> it = deletes.iterator();
			while(it.hasNext()) {
				PendingDelete pd = it.next();
				if (swPath.equals(pd.getSwPath())) {
					LoaderLog.info("Removing "+swPath+" from deletion queue.");
					try {
						pd.unPersist();
					} catch (DfException e) {
						LoaderLog.error(LoaderLog.getExceptionMsgForLog("Failed to remove pending delete "+pd, e));
					}
					it.remove();
					return;
				}
			}
		}
	}
	
	public void run() {
		while(true) {
			try {
				synchronized(deletes) {
					if (deletes.size() == 0) {
						// nothing to do so just wait until something is given to me
						try {
							// wait until notified that something is to be deleted
							deletes.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				
				// get the first pd in the queue
				PendingDelete pd = null;
				synchronized(deletes) {
					pd = deletes.getFirst();
				}
				long now = System.currentTimeMillis();
				if (now >= pd.getDeleteTime().getTime()) {
					// This pending delete is to be executed
					try {
						pd.delete();
					} catch (IOException e) {
						LoaderLog.error(LoaderLog.getExceptionMsgForLog("Failed to remove "+pd+" ", e));
						throw e;
					}
					
					// This delete is done, take it out of the queue
					synchronized(deletes) {
						deletes.removeFirst();
					}
				} else {
					long sleepTime = pd.getDeleteTime().getTime() - now;
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException ie) {
					}
				}
			} catch (Exception e) {
				// Don't want this thread to exit on an exception
				try {
					long sleepAmount = new Double((Math.random()* 15) % 15).longValue(); 
					LoaderLog.debug("Sleep "+sleepAmount+" seconds");
					Thread.sleep(sleepAmount*1000);
				} catch (InterruptedException ie) {
				}
			}
		}
	}

	/**
	 * Queue up a sw_file delete
	 * @param swPath
	 * @param collectionId
	 * @param itemId
	 * @param swFileOid
	 */
	public static void addSwFileToDelete(String swPath, String collectionId, String itemId, String swFileOid) {
		if (swPath == null) {
			// There is no software path for this item.
			return;
		}
		LoaderLog.info("Queuing "+swPath+" for deletion.");
		PendingDelete pd = new PendingDelete(swPath, collectionId, itemId, swFileOid);
		deleter.addSwDelete(pd);
	}
	
	/**
	 * Queue up a sw_file delete
	 * @param swPath
	 * @param collectionId
	 * @param itemId
	 */
	public static void removeSwFile(String swPath) {
		deleter.removeSwDelete(swPath);
	}
	
	/**
	 * create the PendingDeleter thread and start it running
	 */
	public static void start(ConfigurationReader config) {
		// setup the PendingDelete statics
		PendingDelete.start(config);
		// start the thread
		if (deleter == null) {
			deleter = new PendingDeleter();
		}
	}
}
