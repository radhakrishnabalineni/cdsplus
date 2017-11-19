package com.hp.cdsplus.processor.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.CGSContentItem;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.ReferenceItem;
import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
;

/**
 */
public class ProcessorThreadManager {
	
	private static final Logger logger = LogManager.getLogger(com.hp.cdsplus.processor.threads.ProcessorThread.class);
	private ArrayList<ProcessorThread> processorThreadList;
	private QueueManager queueMgr;
	
	private volatile boolean isExit = false;
	private volatile boolean isComplete = false;
	private volatile boolean isInitialized = false;
	private volatile boolean isFatal = false;
	private int threadCount = 4;
	private final ContentDAO contentDAO;
	
	/**
	 * Constructor for ProcessorThreadManager.
	 * @param queueMgr QueueManager
	 */
	public ProcessorThreadManager(QueueManager queueMgr) {
		this.setQueueMgr(queueMgr);
		this.processorThreadList = new ArrayList<ProcessorThread>();
		contentDAO = new ContentDAO();
	}

	/**
	 *  start worker threads
	 */
	public void start() { 
		String threadCntStr = System.getProperty("PROCESSOR_THREADS");
		if(threadCntStr == null || "".equals(threadCntStr)){
			logger.warn("Thread count not specified in the properties file. Default value will be 4");
		}
		this.threadCount = Integer.parseInt(threadCntStr);
		
		logger.info("Starting processor threads");
		for(int index = 0 ; index <this.threadCount; index++){
			ProcessorThread pThread = new ProcessorThread(this.queueMgr);
			Thread workerThread = new Thread(pThread,"PThread - "+index);
			workerThread.setDaemon(true);
			workerThread.start();
			processorThreadList.add(pThread);
			logger.info("PThread - "+index+" Started");
		}
		this.setInitialized(true);
	}
	
	/**
	 * @throws MongoUtilsException 
	 * @throws ProcessException 
	 * @throws OptionsException 
	 * 
	 */
	public void loadContentItems() throws MongoUtilsException, ProcessException, OptionsException {	
		Options options = new Options();
		Set<String> contentTypeList = ConfigurationManager.getInstance().keySet();
		
		String statusCollectionName = System.getProperty("STATUS_COLLECTION_NAME");
		
		String instance_name = System.getProperty("instance_name");
		DBObject filter= new BasicDBObject("instance_name",instance_name);
		
		HashMap<String, Long> deltaContent = null;
		
		if(statusCollectionName != null && !"".equals(statusCollectionName)){
			deltaContent = contentDAO.getLastProcessedTokens(options, statusCollectionName, filter);
		}else{
			deltaContent = contentDAO.getLastProcessedTokens(options, filter);
		}
			
		
		for(Entry<String,Long> content : deltaContent.entrySet()){
			if(!contentTypeList.contains(content.getKey())){
				logger.info("Content Type not defined - "+content.getKey());
				continue;
			}
			String type = ConfigurationManager.getInstance().getMappingValue(content.getKey(), "type");
			if(type == null){
				logger.error("No TYPE defined for the given content type["+content.getKey()+"] in processor_mappings. Valid values are [ CONTENT_TYPE, REFERENCE_TYPE, CGS_TYPE]");
				continue;
			}
			if(type.equals("CONTENT_TYPE")){
				logger.info(content.getKey() +" - "+type+" - "+content.getValue());
				this.queueMgr.push(new ContentItem(content.getKey(),content.getValue(), this.queueMgr));
			}else if(type.equals("REFERENCE_TYPE")){
				logger.info(content.getKey() +" - "+type+" - "+content.getValue());
				this.queueMgr.push(new ReferenceItem(content.getKey(),content.getValue(), this.queueMgr));
			}else if(type.equals("CGS_TYPE")){
				logger.info(content.getKey() +" - "+type);
				this.queueMgr.push(new CGSContentItem(this.queueMgr));
			}
		}
	}
	
	/**
	 * @throws MongoUtilsException
	 * @throws ProcessException
	 * @throws OptionsException
	 */
	public void loadTempItems() throws MongoUtilsException, ProcessException, OptionsException{

		DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		String statusCollectionName = System.getProperty("STATUS_COLLECTION_NAME");
		DBCollection statusCollection = null;
		
		if(statusCollectionName != null && !"".equals(statusCollectionName)){
			statusCollection = configdb.getCollection(statusCollectionName);
		}else{
			statusCollection = configdb.getCollection("status");
		}
		
		String instance_name = System.getProperty("instance_name");
		DBObject filter= new BasicDBObject("instance_name",instance_name);
		
		DBCursor contentListCursor = statusCollection.find(filter);
		DBObject dispFields = new BasicDBObject();
		
		while(contentListCursor.hasNext()){
			DBObject record = contentListCursor.next();
			String contentType = record.get("_id").toString();
			String type = ConfigurationManager.getInstance().getMappingValue(contentType, "type");
			if(type==null || type.equals("REFERENCE_TYPE")){
				continue;
			}
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
			String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(contentType, ConfigurationManager.METADATA_TEMP_COLLECTION);
			String stagingCollectionName = ConfigurationManager.getInstance().getMappingValue(contentType, ConfigurationManager.METADATA_STAGING_COLLECTION);
			
			logger.info("reloading items from temp for - "+contentType);
			if(tempCollectionName == null){
				continue;
			}
			if(stagingCollectionName == null){
				continue;
			}
			DBCollection tempCollection = db.getCollection(tempCollectionName);
			
			DBCollection stagingCollection = db.getCollection(stagingCollectionName);
			dispFields = new BasicDBObject();
			dispFields.put("_id", 1);
			
			DBCursor results = tempCollection.find(new BasicDBObject("processStatus", new BasicDBObject("$ne", ProcessStatus.METADATA_TRANS_COMPLETE.toString())),dispFields);
			
			if(results == null){
				continue;
			}
			for(DBObject tempResult : results){
				DBObject result = null;
				dispFields = new BasicDBObject();
				dispFields.put("_id", 1);
				dispFields.put("lastModified", 1);
				dispFields.put("priority", 1);
				dispFields.put("status", 1);
				dispFields.put("eventType", 1);
				if(tempResult!=null){
					result = stagingCollection.findOne(tempResult,dispFields);
					logger.debug("reloading from temp ("+contentType+") - "+result);
				}
					
				try{
					if(result!=null){
						tempCollection.remove(new BasicDBObject("_id",result.get("_id").toString()));
						this.queueMgr.push(new WorkItem(contentType,result,this.queueMgr));
					}
						
				}catch(ProcessException e){
					logger.error(e.getMessage() + "record found in metadata_temp collection is as follows : "+result, e);
					e.printStackTrace();
					continue;
				}
				
			}
			results.close();
		}
	}
	

	/**
	 * stop worker threads
	 */
	public void stop() {
		this.setExit(true);
		if(processorThreadList !=null && processorThreadList.size() > 0){
			for(ProcessorThread pThread : processorThreadList){
				pThread.stop();
				while(!pThread.isComplete()){
					Thread.currentThread();
					try {
						Thread.sleep((long)(Math.random()*5)*1000);
					} catch (InterruptedException e) {}
				}
			}
		}
		this.setComplete(true);
	}
	
	/**
	 * Method isActive.
	 * @return boolean
	 */
	public boolean isActive(){
		for(ProcessorThread pThread : processorThreadList){
			if(pThread.isFatal()){
				this.stop();	
			}
		}
		return isInitialized() && !isComplete() && !isExit();
		
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
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	
	 * @return the isExit */
	public boolean isExit() {
		return isExit;
	}

	/**
	 * @param isExit the isExit to set
	 */
	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}

	/**
	
	 * @return the isComplete */
	public boolean isComplete() {
		return isComplete;
	}
	
	/**
	 * Method getProcessorThreadListSize.
	 * @return int
	 */
	public int getProcessorThreadListSize(){
		return this.processorThreadList.size();
	}

	/**
	
	 * @return the isInitialized */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * @param isInitialized the isInitialized to set
	 */
	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	/**
	
	 * @return the isFatal */
	public boolean isFatal() {
		for(ProcessorThread pThread : processorThreadList){
			if (pThread.isFatal())
				this.setFatal(true);
		}
		return isFatal;
	}

	/**
	 * @param isFatal the isFatal to set
	 */
	public void setFatal(boolean isFatal) {
		this.isFatal = isFatal;
	}

	public static void main(String[] args){

		System.setProperty("mongo.configuration", "config/mongo.properties");
		try {
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("marketingstandard");
			DBCollection tempCollection = db.getCollection("metadata_temp");
			DBCollection stagingCollection = db.getCollection("metadata_staging");
			DBCursor results = tempCollection.find(new BasicDBObject(), new BasicDBObject("_id",1));
			results.limit(10);
			for(DBObject tempResult : results){
				System.out.println(stagingCollection.findOne(tempResult));
			}
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
