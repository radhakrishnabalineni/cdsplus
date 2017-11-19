package com.hp.cdsplus.processor.item;

import java.io.IOException;
import java.util.ArrayList;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.exception.ServiceHandlerException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.service.ReferenceItemServiceHandler;
import com.hp.cdsplus.processor.service.ServiceHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ReferenceItem extends Item {
	private ArrayList<DBObject> recordList;
	private QueueManager queueMgr;
	private Long lastModified;

	
	//private QueueManager queueMgr;
	public ReferenceItem(String contentType, Long lastModified, QueueManager queueMgr) throws MongoUtilsException, OptionsException {
		this.setContentType(contentType);
		if(lastModified > 0L){
			this.lastModified = lastModified;
		}else{
			this.lastModified = 0L;
		}
		this.setStatus(ProcessStatus.NEW_ITEM);
		this.queueMgr = queueMgr;
		
		this.logMessage(this.getContentType() + "token - "+this.lastModified);
	}

	@Override
	protected void load() throws ProcessException, OptionsException,
			MongoUtilsException, ServiceHandlerException, AdapterException {
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		// get modified records from staging
		Options options = new Options();
		options.setContentType(this.getContentType());
		options.setAfter(this.lastModified);
		
		String collectionName = ConfigurationManager.getInstance().getMappingValue(this.getContentType(), ContentDAO.METADATA_STAGING_COLLECTION_NAME);
		this.recordList = contentDao.getAllMetadata(collectionName, options);
		this.setStatus(ProcessStatus.LOAD_COMPLETE);

	}

	@Override
	public void service() throws ProcessException, ServiceHandlerException,
			MongoUtilsException, OptionsException, IOException {
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		// place holder for any future enhancements to service the static data
		ServiceHandler refServiceHandler = new ReferenceItemServiceHandler();
		refServiceHandler.doService(this);
		this.setStatus(ProcessStatus.SERVICE_COMPLETE);
	}

	@Override
	public void save() throws ProcessException, MongoUtilsException,
			OptionsException {
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		// save it to live. Also make not of lastModified 
		Long temp = 0L;
		for(DBObject record : this.recordList){
			Long lastModified_ts = (Long) record.get("lastModified");
			if(temp < lastModified_ts.longValue()){
				temp = lastModified_ts.longValue();
			}
			Options options = new Options();
			options.setContentType(this.getContentType());
			options.setMetadataDocument(record);
			options.setDocid(record.get("_id").toString());
			options.setQuery(new BasicDBObject("_id",record.get("_id").toString()));
			contentDao.writeMetadataToLive(options);
		}
		Options options = new Options();
		options.setContentType(this.getContentType());
		contentDao.updateStatusCollection(options,temp);
	}
}
