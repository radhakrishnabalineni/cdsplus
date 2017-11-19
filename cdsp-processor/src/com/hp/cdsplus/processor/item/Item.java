package com.hp.cdsplus.processor.item;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.exception.ServiceHandlerException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 */
public abstract class Item {
	
	protected static final Logger logger = LogManager.getLogger(com.hp.cdsplus.processor.threads.ProcessorThread.class);
	private String contentType;

	public ContentDAO contentDao = new ContentDAO();
	private ProcessStatus currStatus;
	private ProcessStatus prevStatus;
	
	private StringBuffer messageBuffer = new StringBuffer();
	
	long start_time;
	long end_time;
	
	long load_time;
	long service_time;
	long save_time;
	public final void process() throws ProcessException, MongoUtilsException, ServiceHandlerException, OptionsException, AdapterException, IOException{
		if(this instanceof WorkItem){
			messageBuffer.append(this.getContentType() + "/"+((WorkItem)this).getId()).append("  ");
		}else if(this instanceof CGSWorkItem){
			messageBuffer.append(this.getContentType() + "/"+((CGSWorkItem)this).getId()).append("  ");;
		}else{
			messageBuffer.append(this.getContentType()).append("  ");
		}
		start_time = System.currentTimeMillis();
		if(this.currStatus == null)
			this.currStatus = ProcessStatus.NEW_ITEM;
		switch(this.currStatus){
		case NEW_ITEM:
			this.load();  // Loading the document from staging bd to temp db
			this.load_time = System.currentTimeMillis();
		case SAVE_COMPLETE:
			this.service();
			this.service_time = System.currentTimeMillis();
		case SERVICE_COMPLETE:
			this.save();
			this.save_time = System.currentTimeMillis();
		default:
			break;
		}
		this.end_time = System.currentTimeMillis();
	}
	
	protected abstract void load() throws ProcessException, OptionsException, MongoUtilsException, ServiceHandlerException, AdapterException, IOException;
	public abstract void service() throws ProcessException, ServiceHandlerException, MongoUtilsException, OptionsException, IOException;
	public abstract void save() throws ProcessException, MongoUtilsException, OptionsException;
	
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return this.contentType;
	}
	
	/**
	 * @return the status
	 */
	public ProcessStatus getStatus() {
		return this.currStatus;
	}
	
	/**
	 * @param status the status to set
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public void setStatus(ProcessStatus status) throws MongoUtilsException, OptionsException {
		this.setStatus(status, false);
	}
	
	public void setStatus(ProcessStatus prevStatus,ProcessStatus currStatus,  boolean dbUpdate) throws MongoUtilsException, OptionsException{
		this.prevStatus = prevStatus;
		this.currStatus = currStatus;
		this.logStatus();
		if(dbUpdate){
			this.updateStatusInDB(this.prevStatus, this.currStatus);
		}
	}
	
	public void setStatus(ProcessStatus status, boolean dbUpdate) throws MongoUtilsException, OptionsException{
		// first save the existing status before setting the new status
		this.prevStatus = this.currStatus;
		// now set the new status
		this.currStatus = status;
		this.logStatus();
		if(dbUpdate){
			this.updateStatusInDB(null, this.currStatus);
		}
	}
	
	public void logStatus(){
		if(this instanceof WorkItem){
			logger.debug(this.getContentType() +" "+((WorkItem)this).getId()+" "+this.prevStatus + " "+this.currStatus);
		}else if(this instanceof CGSWorkItem){
			logger.debug("(cgs)"+this.getContentType() +" "+((CGSWorkItem)this).getId()+" "+this.prevStatus + " "+this.currStatus);
			return;
		}else
			logger.debug(this.getContentType() +" "+this.prevStatus + " "+this.currStatus);
		
	}
	
	public void updateStatusInDB(ProcessStatus prevStatus, ProcessStatus currentStatus) throws OptionsException, MongoUtilsException{
		Options options = new Options();
		options.setContentType(this.getContentType());
		DBObject query = new BasicDBObject();
		
		if(prevStatus != null){
			query.put("processStatus",prevStatus.toString());
		}
		if(this instanceof WorkItem){
			WorkItem wItem = (WorkItem) this;
			query.put("_id", wItem.getId());
		}
			
		DBObject update = new BasicDBObject("$set",new BasicDBObject("processStatus", currentStatus.toString()));
		options.setMetadataDocument(update);
		options.setQuery(query);	
		new ContentDAO().updateAllMetadataInTemp(options);
	}


	public void logMessage(String message){
		messageBuffer.append(message).append(" ");
	}
	public String getLogMessage(){
		return messageBuffer.toString();
	}
	
	public void logStats(){
		messageBuffer.append("L:"+(this.load_time-this.start_time)).append(" ");
		messageBuffer.append("P:"+(this.service_time-this.load_time)).append(" ");
		messageBuffer.append("S:"+(this.save_time-this.service_time)).append(" ");
		messageBuffer.append("T:"+(this.end_time-this.start_time)).append(" ");
		logger.info("Completed "+messageBuffer.toString());
	}
}