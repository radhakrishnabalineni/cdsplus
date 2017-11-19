package com.hp.cdsplus.processor.item;

import java.io.IOException;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.adapters.CGSRulesAdapter;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.exception.ServiceHandlerException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class CGSContentItem extends Item {

	private QueueManager queueMgr;

	private boolean isRulesModified;
	
	public CGSContentItem(QueueManager queueManager) throws ProcessException, MongoUtilsException, OptionsException {
		this.queueMgr = queueManager;
		this.setContentType("cgs");
		this.setStatus(ProcessStatus.NEW_ITEM);
	}
	
	@Override
	protected void load() throws ProcessException, OptionsException,
			MongoUtilsException, ServiceHandlerException, AdapterException {
		
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		
		try {
			this.isRulesModified=  CGSRulesAdapter.getInstance().isRulesModified();
		} catch (IOException e) {
			throw new ProcessException(e);
		}
		if (this.isRulesModified){
			logger.info("CGS Rules Modified");
			// if cgs rules has been modified, the 'isRulesModified' method would have reloaded the CGSRules.xml already.
			// We will reset the cursor back to 0L so that we can revaluate all the items under support/manual content type for cgs generation
			loadItems(System.currentTimeMillis(), "support");
			loadItems(System.currentTimeMillis(), "manual");
			this.setStatus(ProcessStatus.LOAD_COMPLETE);
		}
		
	}
	
	
	private void loadItems(Long latestTimeStamp, String contentType) throws MongoUtilsException, OptionsException{
		Options options = new Options();
		options.setContentType(contentType);
		QueryBuilder builder = QueryBuilder.start();
		builder.put("lastModified").greaterThan(0l);
		builder.put("lastModified").lessThan(latestTimeStamp);

		DBObject query = builder.get();
		DBObject displayFields = new BasicDBObject("_id",1).append("lastModified", 1).append("isDelete",1);
		
		String contentDB = ConfigurationManager.getInstance().getMappingValue(contentType, CGSRulesAdapter.DB_NAME_KEY);
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentDB);
		
		String collectionName = ConfigurationManager.getInstance().getMappingValue(contentType, ConfigurationManager.METADATA_STAGING_COLLECTION);
		
		DBCollection metadataCollection = db.getCollection(collectionName);
		DBCursor cursor = metadataCollection.find(query, displayFields);
		
		while(cursor.hasNext()){
			
			DBObject rec = cursor.next();
			
			String isDelStr = (String) rec.get("isDelete");
			if(isDelStr ==null && "".equals(isDelStr)){
				continue;
			}
			Boolean isDelete = Boolean.parseBoolean(isDelStr);
			String id = (String) rec.get("_id");
			if(id !=null){
				this.queueMgr.push(new CGSWorkItem(contentType, id, isDelete));
			}
				
		}
	}
	

	@Override
	public void service() throws ProcessException, ServiceHandlerException,
			MongoUtilsException, OptionsException {
		//dont do anything
	}

	@Override
	public void save() throws ProcessException, MongoUtilsException,
			OptionsException {
		if(!(this.queueMgr.getCurrentContentTypeSet().contains(this.getContentType()))){
			return;
		}
		this.setStatus(ProcessStatus.SAVE_COMPLETE);
		this.queueMgr.cleanUpContentItem(this);
		
	}


	public static void main(String[] args){
		System.setProperty("mongo.configuration", "config/mongo.properties");
		try {
			
			QueueManager queueManager = new QueueManager();
			CGSContentItem cgsContentItem = new CGSContentItem(queueManager);
			
			cgsContentItem.load();
			System.out.println("Total Items loaded - "+queueManager.size());
			while(!queueManager.isEmpty()){
				CGSWorkItem item = (CGSWorkItem) queueManager.pop();
				System.out.println(item.getContentType() + " : "+item.getId());
			}
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
