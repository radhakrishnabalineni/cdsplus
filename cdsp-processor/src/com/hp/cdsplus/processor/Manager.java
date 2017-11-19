/**
 * 
 */
package com.hp.cdsplus.processor;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *  Manager class which runs as a daemon thread..
 *  
 *  Manager keeps checking status collection every 1 sec for any changes
 *  
 *  If a change is found in status collection, trigger queue manager cleaning
 *  
 *  It uses a flag to indicate that queue manager is getting empty
 *  
 *  It should not allow loadContentItems() to happen until the queue is getting cleared
 *  
 *  Once queue is cleared, start loading items from temp afresh
 *  
 * @author srivasni
 *
 */
public class Manager extends Thread{
	private static final Logger logger = LogManager.getLogger(com.hp.cdsplus.processor.Manager.class);
	private QueueManager queueMgr ;
	public static boolean reloadQueue = false;

	private boolean isExit = false;

	ArrayList<String> newassignedContentTypes;
	ArrayList<String> existingassignedContentTypes;

	public ArrayList<String> getExistingassignedContentTypes() {
		return existingassignedContentTypes;
	}

	public Manager(QueueManager queueManager) {
		this.setQueueMgr(queueManager);
		newassignedContentTypes = new ArrayList<String>();
		existingassignedContentTypes = new ArrayList<String>();
	}

	public void run(){

		while(!isExit()){
			try {
				reloadQueue = false;
				queueMgr.setCurrentContentTypeSet(existingassignedContentTypes);
				if(checkStatusUpdate()){
					synchronized (queueMgr) {
						try {
							reloadQueue = true;
							lockContentTypes();
							queueMgr.cleanUp();
							//this.wait();
							while(Manager.reloadQueue && !Processor.reloadedTemp && existingassignedContentTypes.size()>0){
								System.out.println("Manager thread waiting to be notified by processor.");
							}
							logger.info("Manager thread resumed.");
							Processor.reloadedTemp=false;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (MongoUtilsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.currentThread();
			try {
				Thread.sleep((long) ( 30*1000));
			} catch (InterruptedException e) {}
		}

	}

	private void lockContentTypes() throws MongoUtilsException {
		// set assigned to true for the loaded/assigned content types.
		DBCollection statusCollection = getStatusCollection();
		for (String contentType : existingassignedContentTypes) {
			DBObject setObject = new BasicDBObject("$set",new BasicDBObject("assigned","true"));
			statusCollection.update(new BasicDBObject("_id",contentType), setObject, true, false);
		}

	}

	private DBCollection getStatusCollection() throws MongoUtilsException {
		DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		String statusCollectionName = System.getProperty("STATUS_COLLECTION_NAME");
		DBCollection statusCollection = null;

		if(statusCollectionName != null && !"".equals(statusCollectionName)){
			statusCollection = configdb.getCollection(statusCollectionName);
		}else{
			statusCollection = configdb.getCollection("status");
		}
		return statusCollection;
	}

	public boolean checkStatusUpdate() throws MongoUtilsException{
		DBCollection statusCollection = getStatusCollection();
		String instance_name = System.getProperty("instance_name");
		DBCursor contentListCursor = statusCollection.find(new BasicDBObject("instance_name",instance_name));
		DBObject statusObject ;
		while(contentListCursor.hasNext()){
			statusObject = contentListCursor.next();
			newassignedContentTypes.add(statusObject.get("_id").toString());
		}
		if(existingassignedContentTypes.isEmpty()){
			existingassignedContentTypes.addAll(newassignedContentTypes);
			lockContentTypes();
			return true;
		}
		if(!existingassignedContentTypes.containsAll(newassignedContentTypes)){
			logger.info(" There is a change (Added)..............................");
			existingassignedContentTypes.clear();
			existingassignedContentTypes.addAll(newassignedContentTypes);
			return true;
		}

		if(!newassignedContentTypes.containsAll(existingassignedContentTypes)){
			logger.info(" There is a change (removed)..............................");
			unAssignContentType(statusCollection);
			return true;
		}
		logger.info("No change............. ");
		newassignedContentTypes.clear();
		return false;
	}

	private void unAssignContentType(DBCollection statusCollection) {
		ArrayList<String> tobeunassignedContentTypes = new ArrayList<String>();
		tobeunassignedContentTypes.addAll(existingassignedContentTypes);
		tobeunassignedContentTypes.removeAll(newassignedContentTypes);
		for (String contentType : tobeunassignedContentTypes) {
			DBObject unsetObject = new BasicDBObject("$unset",new BasicDBObject("assigned",1));
			statusCollection.update(new BasicDBObject("_id",contentType), unsetObject, true, false);
		}
		existingassignedContentTypes.clear();
		existingassignedContentTypes.addAll(newassignedContentTypes);
	}

	/**
	 * @return the queueMgr
	 */
	public QueueManager getQueueMgr() {
		return queueMgr;
	}

	/**
	 * @param queueMgr the queueMgr to set
	 */
	public void setQueueMgr(QueueManager queueMgr) {
		this.queueMgr = queueMgr;
	}

	/**
	 * @return the isExit
	 */
	public boolean isExit() {
		return isExit;
	}

	/**
	 * @param isExit the isExit to set
	 */
	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}
}
