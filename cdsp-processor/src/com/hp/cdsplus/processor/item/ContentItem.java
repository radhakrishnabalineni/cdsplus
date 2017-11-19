package com.hp.cdsplus.processor.item;

import java.io.IOException;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.Manager;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.adapters.DeleteEventHandlerAdapter;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.service.ContentItemServiceHandler;
import com.hp.cdsplus.processor.service.ServiceHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 */
public class ContentItem extends Item {
	
	
	private Long lastModified;
	
	private ContentDAO contentDao = new ContentDAO();
	private QueueManager queueMgr;
	private Long latestTS = 0L;
	/**
	 * @param contentType 		- content class that the item represents
	 * @param lastModified TODO
	 * @param lastModified		- token beyond which the documents need to be loaded to queue
	 * @param queueManager TODO
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public ContentItem(String contentType, Long lastModified, QueueManager queueManager)throws ProcessException, MongoUtilsException, OptionsException{
		if(contentType == null || "".equals(contentType)){
			throw new ProcessException("Content Type parameter cannot be null/blank while initializing ContentItem object");
		}
		this.setContentType(contentType);
		if(lastModified > 0L)
			this.lastModified = lastModified;
		else 
			this.lastModified = 0L;
		this.setStatus(ProcessStatus.NEW_ITEM);
		this.queueMgr = queueManager;
		this.latestTS = this.lastModified;
		this.logMessage(this.getContentType() + "token - "+this.lastModified);
	}


	/**
	 * @throws AdapterException 
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 * @throws ProcessException 
	 * @throws IOException 
	 */
	private void preProcess() throws MongoUtilsException, OptionsException, ProcessException, IOException {
		// apply subscription evaluation on processed documents in temp	
		ServiceHandler serviceHandler = new ContentItemServiceHandler();
		serviceHandler.doService(this);
		
	}
	
	/* (non-Javadoc)
	 * @see com.hp.cdsplus.processor.item.Item#load()
	 */
	public void load() throws MongoUtilsException, OptionsException, ProcessException, IOException{
		
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		// first do the preprocessing section
		this.preProcess();
		

		// now load documents to be processed
		DB contentDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(this.getContentType());
		
		String stagingCollectionName = ConfigurationManager.getInstance().getMappingValue(this.getContentType(), ConfigurationManager.METADATA_STAGING_COLLECTION);
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(this.getContentType(), ConfigurationManager.METADATA_TEMP_COLLECTION);
		
		DBCollection stagingCollection = contentDB.getCollection(stagingCollectionName);
		DBCollection tempCollection = contentDB.getCollection(tempCollectionName);
		
		String processorDelay = System.getProperty("processorDelay");
		Long latestTime=System.currentTimeMillis();
		if(processorDelay !=null && !"".equalsIgnoreCase(processorDelay)){	
			latestTime = System.currentTimeMillis() - Long.parseLong(processorDelay);
		}
		DBObject query = new BasicDBObject();
		query.put("lastModified", new BasicDBObject("$gt",this.lastModified).append("$lt", latestTime));
		
		DBObject wItemRecord = new BasicDBObject();
		
		
		DBCursor results = stagingCollection.find(query);
		results.sort(new BasicDBObject("lastModified",-1));
		//results.limit(1000);
		while(results.hasNext()){
			if(Manager.reloadQueue){
				break;
			}
			DBObject record = results.next();
			if(record != null){
				Long lastModified_ts = (Long) record.get("lastModified");
				if(latestTS < lastModified_ts.longValue()){
					latestTS = lastModified_ts.longValue();
				}
				
				wItemRecord.put("_id", record.get("_id"));
				wItemRecord.put("lastModified",record.get("lastModified"));
				wItemRecord.put("priority",record.get("priority"));
				wItemRecord.put("status",record.get("status"));
				wItemRecord.put("eventType", record.get("eventType"));
				wItemRecord.put("processStartTime", System.currentTimeMillis());
				
				tempCollection.update(new BasicDBObject("_id",record.get("_id")),wItemRecord, true, false);
				
				this.queueMgr.push(new WorkItem(this.getContentType(),record,this.queueMgr));
				
				
			}
			
		}

		this.setStatus(ProcessStatus.LOAD_COMPLETE);
		
	}
	
	/* (non-Javadoc)
	 * @see com.hp.cdsplus.processor.item.Item#service()
	 */
	public void service() throws MongoUtilsException, OptionsException, ProcessException{
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		DeleteEventHandlerAdapter delEventAdapter = new DeleteEventHandlerAdapter();
		delEventAdapter.evaluate(this);
		this.setStatus(ProcessStatus.SERVICE_COMPLETE);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.cdsplus.processor.item.Item#save()
	 */
	public void save() throws OptionsException, MongoUtilsException, ProcessException{
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		String statusCollectionName = System.getProperty("STATUS_COLLECTION_NAME");
		
		// change the status collection entry to hold the latest lastModified time stamp
		Options options = new Options();
		options.setContentType(this.getContentType());
		if(statusCollectionName != null && !"".equals(statusCollectionName)){
			contentDao.updateStatusCollection(options,this.latestTS,statusCollectionName);
		}else{
			contentDao.updateStatusCollection(options,this.latestTS);
		}
		//System.out.println(" Content Item:save:Clearing the content type from the quemanager contenttype set as this is served");
		this.queueMgr.cleanUpContentItem(this);
	}


	/**
	 * @return the lastModified
	 */
	public Long getLastModified() {
		return lastModified;
	}
}
