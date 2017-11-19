package com.hp.cdsplus.pmloader.single;

import java.sql.SQLException;
import java.util.Date;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.pmloader.single.Level;
import com.hp.cdsplus.pmloader.single.PMDataValidator;
import com.hp.cdsplus.pmloader.single.PMLoaderConstants;
import com.hp.cdsplus.pmloader.single.LoaderException;
import com.hp.cdsplus.pmloader.single.WorkItem;
import com.hp.cdsplus.pmloader.single.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 */
public class ExtractorThread implements Runnable,PMLoaderConstants {

	private boolean isExit = false;
	private boolean isComplete = false;
	private boolean isDeltaMode = true;
	private boolean isRepublishMode = false;
	private boolean isTempReload = false;

	private QueueManager queueMgr;
	private Level level;
	private ObjectId currentLoadId;
	private Date[] dateRange;
	private boolean isFatal;

	private static final Logger logger = Logger.getLogger(ExtractorThread.class);
	
	/**
	 * Constructor for ExtractorThread.
	 * @param queueManager QueueManager
	 * @param level Level
	 */
	public ExtractorThread(QueueManager queueManager, Level level, boolean isTempReload){
		this.isDeltaMode = false;
		this.queueMgr = queueManager;
		this.level = level;
		this.isTempReload=isTempReload;
		Thread it = new Thread(this,level.name()+"_Thread");
		it.setDaemon(true);
		it.start();
	}
	/**
	 * Constructor for ExtractorThread.
	 * @param queueManager QueueManager
	 * @param level Level
	 * @param currentLoadId ObjectId
	 * @param dateRange Date[]
	 * @param isDeltaMode boolean
	 */
	public ExtractorThread(QueueManager queueManager, Level level, ObjectId currentLoadId, Date[] dateRange, boolean isDeltaMode, boolean isRepublishMode, boolean isTempReload){
		this.isDeltaMode = isDeltaMode;
		this.isRepublishMode = isRepublishMode;
		this.queueMgr = queueManager;
		this.currentLoadId = currentLoadId;
		this.dateRange = dateRange;
		this.level = level;
		this.isTempReload= isTempReload;
		/*Thread it = new Thread(this,level.name()+"_Thread");
		it.setDaemon(true);
		it.start();*/
		
		this.run();
	}

	/**
	 * Method run.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
			
		if(this.isTempReload){
		try {
			logger.info("loading queue with the items left over from last run");
			this.reloadPendingItemsToQueue();
			}
		 catch (MongoUtilsException e) {
			this.isFatal = true;
			logger.fatal("Failed to extract oids from temp for "+level);
			logger.error(e);
			e.printStackTrace();
		 }
		}
	
		else if(isRepublishMode){
			logger.info("PMLoader configured to run in republish mode");
			PMDataValidator validator = new PMDataValidator();
			try {
				/*logger.info("retrieving republish oid list.");
				TreeSet<String> republishOids = validator.getRepublishOids(this.level);
				logger.info("republish oids - "+ republishOids);
				for(String oid : republishOids){
					this.queueMgr.push(new WorkItem(oid, level));
				}
				logger.info("completed retrieving republish oid list "+republishOids);
				
				
				// oids which have been deleted in pmaster db but still available in CDS+
				TreeSet<String> deletedOids = validator.getPMDeletedOids(level);
				for(String oid : deletedOids){
					this.queueMgr.push(new WorkItem(oid, level, true));
				}
				logger.info("completed retrieving deleted oid list. no of items inserted to queue are - "+ deletedOids.size());
				
				*/
				//oids which is available in pmaster db but not available in CDS+
				logger.info("retrieving missing oid list for level : "+level.toString());
				TreeSet<String> missingOids = validator.getPMMissingOids(level);
				for(String oid : missingOids){
					this.queueMgr.push(new WorkItem(oid, level, false));
				}
				logger.info("completed retrieving missing oid list. no of items inserted to the queue are are  - "+ missingOids.size());
				
			} catch (MongoUtilsException e) {
				this.isFatal = true;
				logger.fatal("Failed to extract republish/missing/deleted oids for "+level);
				logger.error(e);
				e.printStackTrace();
			} catch (SQLException e) {
				this.isFatal = true;
				logger.fatal("Failed to extract  republish/missing/deleted oids for "+level);
				logger.error(e);
				e.printStackTrace();
			}
		} else if(isDeltaMode ){
			// if set to delta mode and has valid date range defined, we do a delta extract, by default its going to be full extract
			if(dateRange != null && dateRange.length > 0){
				logger.info("Date range for delta mode. StartTime - "+dateRange[0]+" EndTime - "+dateRange[1]);
				try {
						level.process(this.queueMgr,dateRange);
				} catch (SQLException e) {
					this.isFatal = true;
					logger.fatal("Failed to extract delta change for "+level);
					logger.error(e);
					e.printStackTrace();
					//logExtractorStatus("Failed - extraction of "+level+" delta failed");
				} catch (LoaderException e) {
					this.isFatal = true;
					logger.fatal("Failed to extract delta change for "+level);
					logger.error(e);
					e.printStackTrace();
					//logExtractorStatus("Failed - extraction of "+level+" delta failed");
				}
			}
		}else{
			try {
				level.process(queueMgr);
			} catch (SQLException e) {
				this.isFatal = true;
				logger.fatal("Failed to perform full extract for - "+level);
				logger.error(e);
				//logExtractorStatus("Failed - full extraction of "+level+" failed");
			} catch (LoaderException e) {
				this.isFatal = true;
				logger.fatal("Failed to perform full extract for - "+level);
				logger.error(e);
				//logExtractorStatus("Failed - full extraction of "+level+" failed");
			}
		}
		this.isComplete = true;
		//logger.info("Complete : "+Thread.currentThread()+this.isComplete);
	}
	/**
	 * Method logLoaderStatus.
	 * @param status String
	 * @throws OptionsException 
	 * @throws MongoUtilsException 

	 * @throws LoaderException  */
	void logExtractorStatus(String status) throws MongoUtilsException, OptionsException{
		DBObject query = new BasicDBObject("_id", currentLoadId);
		DBObject update = new BasicDBObject("status", status);
		
		Options options = new Options();
		options.setContentType("productmaster");
		options.setQuery(query);
		options.setMetadataDocument(update);
		
		ProductMasterDAO pmDAO = new ProductMasterDAO();
		pmDAO.updateLogEntry(options);
	}

	/**
	 * Method isCompleted.
	 * @return boolean
	 */
	public boolean isCompleted() {
		return this.isComplete;
	}

	/**
	 * Method setExit.
	 * @param b boolean
	 */
	public void setExit(boolean b) {
		this.isExit = true;
		this.queueMgr.setExit(true);
	}
	
	/**
	 * Method isExit.
	 * @return boolean
	 */
	public boolean isExit(){
		return this.isExit;
	}


	/**
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	
	 * @return the isDeltaMode */
	public boolean isDeltaMode() {
		return isDeltaMode;
	}

	/**
	 * @param isDeltaMode the isDeltaMode to set
	 */
	public void setDeltaMode(boolean isDeltaMode) {
		this.isDeltaMode = isDeltaMode;
	}

	/**
	
	 * @return the queueMgr */
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
	
	 * @return the level */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	
	 * @return the currentLoadId */
	public Object getCurrentLoadId() {
		return currentLoadId;
	}

	/**
	 * @param currentLoadId the currentLoadId to set
	 */
	public void setCurrentLoadId(ObjectId currentLoadId) {
		this.currentLoadId = currentLoadId;
	}

	/**
	
	 * @return the dateRange */
	public Date[] getDateRange() {
		return dateRange;
	}

	/**
	 * @param dateRange the dateRange to set
	 */
	public void setDateRange(Date[] dateRange) {
		this.dateRange = dateRange;
	}
	public boolean isFatal() {
		// TODO Auto-generated method stub
		return isFatal;
	}
	
	public void reloadPendingItemsToQueue() throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("productmaster");
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue("productmaster", PMLoaderConstants.TEMP_COLLECTION);
		if(StringUtils.isEmpty(tempCollectionName))
		    tempCollectionName = "pm_temp";
		DBCollection collection = db.getCollection(tempCollectionName);
		DBCursor cursor = collection.find();
		while(cursor.hasNext()){
			DBObject obj = cursor.next();
			String _id = obj.get("_id").toString();
			String hierarchy_level = (String) obj.get("level");
			if(_id !=null && hierarchy_level !=null)
			this.queueMgr.push(new WorkItem(_id,Level.valueOf(hierarchy_level)));
		}
	}

}
