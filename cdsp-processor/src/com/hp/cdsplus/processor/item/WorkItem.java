package com.hp.cdsplus.processor.item;


import java.io.IOException;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.service.WorkItemServiceHandler;
import com.mongodb.DBObject;

/**
 */
public class WorkItem extends Item {
	
	private String id;
	private String eventType;
	private int priority;
	private Long lastModified;

	private boolean isProcessed = false;
	private QueueManager queueMgr;
	
	private DBObject document;
	
	public WorkItem(){
		this.logMessage(this.getContentType() + "/"+this.getId());
	}
	
	public WorkItem(String contentType, DBObject item, QueueManager queueManager) throws ProcessException, MongoUtilsException, OptionsException{
		if(isNull(contentType)){
			throw new ProcessException("Content Type cannot be null/blank");
		}
		if(item ==null){
			throw new ProcessException("DBObject cannot be null/blank");
		}
		this.setContentType(contentType);
		
		String temp = (String) item.get("_id");
		if(isNull(temp)){
			throw new ProcessException("Document ID cannot be null/blank - "+item);
		}
		this.setId(temp);
		
		temp = (String) item.get("eventType");
		if(isNull(temp)){
			throw new ProcessException("Event Type cannot be null/blank"+item);
		}
		this.setEventType(temp);
		
		Integer priority = (Integer) item.get("priority");
		if(isNull(temp)){
			throw new ProcessException("priority cannot be null/blank"+item);
		}
		this.setPriority(priority.intValue());
		
		Long lastModified = (Long) item.get("lastModified");
		if(isNull(temp)){
			throw new ProcessException("lastModified time stamp cannot be null/blank"+item);
		}
		this.setLastModified(lastModified.longValue());
		this.queueMgr = queueManager;
		this.setStatus(ProcessStatus.NEW_ITEM);
	}
	
	/**
	 * @throws MongoUtilsException 
	 * given the content type and id, load method will get the document from db
	 * @throws ProcessException 
	 * @throws OptionsException 
	 * @throws  
	 */
	public void load() throws ProcessException, OptionsException, MongoUtilsException{
		/*
		 * get the metadata from the staging collection and save it in work item
		 *
		 */
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			System.out.println(" Work Item:load:This work item does not belong to this content type hence return.");
			return;
		}

		if(isNull(this.getContentType())){
			throw new ProcessException("Content Type is not set. Please provide a valid content type");
		}
		
		if(isNull(this.getId())){
			throw new ProcessException("Id is the primary identifier for a work item and hence cannot be null");
		}
		
		ContentDAO contentDAO = new ContentDAO();
		Options options = new Options();
		options.setContentType(this.getContentType());
		options.setDocid(this.getId());
		
		// get the document from staging table
		this.document = contentDAO.getStagedMetadata(options);
		if(this.document == null){
			throw new ProcessException("Cannot find Record for given document id - "+this.getId()+" under content type - "+this.getContentType());
		}
		
		// set status of document
		this.setProcessed(false);
		options.setMetadataDocument(this.document);
		// write the document from staging db to temp db
		contentDAO.writeMetadataToTemp(options);
		this.setStatus(ProcessStatus.LOAD_COMPLETE);
	}
	
	/**
	 * given the document, process method puts the document through the process flow
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 * @throws ProcessException 
	 * @throws IOException 
	 */
	public void service() throws MongoUtilsException, OptionsException, ProcessException, IOException{
		/*
		 * call the service handler to take the metadata object through work flow
		 */
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			System.out.println(" Work Item:service:This work item does not belong to this content type hence return.");
			return;
		}
		WorkItemServiceHandler serviceHandler = new WorkItemServiceHandler();
		serviceHandler.doService(this);
		this.setStatus(ProcessStatus.SERVICE_COMPLETE);
	}
	
	/**
	 * given the document, save method now saves it back to history and live collections in DB
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public void save() throws MongoUtilsException, OptionsException{
		/*not doing anything as of now. since the subscription eval, 
		 * history handling and move to live for a document has been moved to contentItem.preProcess()
		 * 
		 */
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			System.out.println(" Work Item:save:This work item does not belong to this content type hence return.");
			return;
		}
		this.isProcessed = true;
		this.setStatus(ProcessStatus.SAVE_COMPLETE);
	}
	
	private boolean isNull(String string){
		if(string == null || "".equals(string)){
			return true;
		}
		return false;
	}
	/**
	
	 * @return the id */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	
	 * @return the document */
	public DBObject getDocument() {
		return document;
	}
	/**
	 * @param document the document to set
	 */
	public void setDocument(DBObject document) {
		this.document = document;
	}

	/**
	
	 * @return the isProcessed */
	public boolean isProcessed() {
		return isProcessed;
	}

	/**
	 * @param isProcessed the isProcessed to set
	 */
	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	/**
	 * @return the lastModified
	 */
	public Long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/*public static void load(String contentType, String id) throws MongoUtilsException, OptionsException{
		ContentDAO contentDAO = new ContentDAO();
		Options options = new Options();
		
		options.setContentType(contentType);
		options.setDocid(id);
		
		DBObject document = contentDAO.getStagedMetadata(options);
		if(document == null){
			logger.error("Cannot find the document - "+contentType+"/"+id);
		}
		
		options.setMetadataDocument(document);
		contentDAO.writeMetadataToTemp(options);
	}

	
	public static void main(String[] args){
		
		System.setProperty("CONFIG_LOCATION","config/ITG/processor.properties");
		
		System.setProperty("log.file.location", "logs" );
		System.setProperty("root.logger.level", "INFO" );
		System.setProperty("thread.logger.level", "INFO" );
		System.setProperty("extractor.logger.level", "INFO" );
		Processor.initAppProperties();
		System.setProperty("LOG4J_LOCATION","config/ITG/log4j.xml");
		
		String contentType = "support";
		String id = "c00260694";
		try {
			load(contentType, id);
		} catch (MongoUtilsException e) {
			e.printStackTrace();
		} catch (OptionsException e) {
			e.printStackTrace();
		}
		DBObject record = new BasicDBObject();
		record.put("_id" , "c00260694");
		record.put("eventType" , "update");
		record.put("priority" , 4);
		record.put("lastModified" , 1384269924999L);
		try {
			WorkItem wItem = new WorkItem(contentType, record);
			wItem.process();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
}