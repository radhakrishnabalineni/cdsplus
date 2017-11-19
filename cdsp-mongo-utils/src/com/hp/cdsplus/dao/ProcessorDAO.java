package com.hp.cdsplus.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class ProcessorDAO {

    public void handleUpdateEvent(Options options) throws MongoUtilsException {
	DBCursor records = null;
	long cache_timeStamp=0l;
	try {
	    String limit = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "HISTORY_VERSION_LIMIT");

	    String historyCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_HISTORY_COLLECTION);
	    String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_TEMP_COLLECTION);
	    String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
	    String cacheCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_CACHE_COLLECTION);
	    	   
	    int history_version_limit = 5;
	    if (limit != null && !"".equals(limit)) {
		history_version_limit = Integer.parseInt(limit);
	    }

	    // at this point all the metadata should have reached both history
	    // and live tables
	    DBObject filter = new BasicDBObject();

	    if (options.getQuery() != null) {
		filter = options.getQuery();
	    }

	    DB contentDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
	    DBCollection cacheCollection = contentDB.getCollection(cacheCollectionName);

	    DBCollection metadataTempCollection = contentDB.getCollection(tempCollectionName);
	    DBCollection metadataLiveCollection = contentDB.getCollection(liveCollectionName);
	    DBCollection metadataHistoryCollection = contentDB.getCollection(historyCollectionName);

	    records = metadataTempCollection.find(filter).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

	    while (records.hasNext()) {
		DBObject record = records.next();
		// if(record != null){ // commented as per Recommendation from
		// DBA and Mongo Support
		
		long currentTime=System.currentTimeMillis();
		long stagingTime=(Long)record.get("lastModified");
		
		record.removeField("processStatus");
		record.put("processEndTime", currentTime);
		String id = record.get("_id").toString();
		
		DBObject cache_record = getCacheRecord(cacheCollection, id);
		//Handling Timestamp to make sure the document sent to live is latest always
		if (cache_record != null) {
			cache_timeStamp = (Long) cache_record.get("lastModified");
			if(stagingTime > cache_timeStamp){
				record.put("lastModified",currentTime);
			}else
				record.put("lastModified",cache_timeStamp);
		}else{
			record.put("lastModified",currentTime);
		}
		metadataLiveCollection.update(new BasicDBObject("_id", record.get("_id")), record, true, false);
		
		record.removeField("_id");
		record.put("documentId", id);

		// metadataHistoryCollection.insert(record);
		DBObject historyQuery = new BasicDBObject("documentId", id).append("lastModified", record.get("lastModified"));
		metadataHistoryCollection.update(historyQuery, record, true, false);

		long count = metadataHistoryCollection.count(new BasicDBObject("documentId", id));

		if (count > history_version_limit) {
		    DBObject query = new BasicDBObject("documentId", id);
		    DBObject sort = new BasicDBObject("lastModified", -1);
		    options.getMongoUtils().deleteMeta(options.getContentType(), historyCollectionName, query, sort, history_version_limit);
		    
		}

		DBObject wItemRecord = new BasicDBObject();

		BasicDBList subscriptions = (BasicDBList) record.get("subscriptions");
		BasicDBList tempSubList = new BasicDBList();

		if (subscriptions != null) {
			// By default we need to add fastxml as one subscription to
		    // all MS & Support documents.
		    if (options.getContentType().equalsIgnoreCase("marketingstandard") || options.getContentType().equalsIgnoreCase("support"))
			subscriptions.add("fastxml");

		    // wItemRecord.put("subscriptions",subscriptions);
		    tempSubList = subscriptions;
		}
		else {
		    subscriptions = new BasicDBList();
		}
		Long temp_date = (Long) record.get("lastModified");
		wItemRecord.put("subscriptions", subscriptions);
		wItemRecord.put("lastModified", temp_date);
		wItemRecord.put("priority", record.get("priority"));
		wItemRecord.put("status", record.get("eventType"));
		wItemRecord.put("eventType", record.get("eventType"));
		if (record.get("hasAttachments") == null)
		    wItemRecord.put("hasAttachments", false);
		else
		    wItemRecord.put("hasAttachments", record.get("hasAttachments"));
		    //Deleted subscription feature change : saving deleteSubs in metadata_cache
		   	BasicDBList changeMapElements = getChangeMapElements(tempSubList, stagingTime, cache_record, id);
			wItemRecord.put("deleteSubs", changeMapElements);
		   

		DBObject update = new BasicDBObject("$set", wItemRecord);
		cacheCollection.update(new BasicDBObject("_id", id), update, true, false);

		metadataTempCollection.remove(new BasicDBObject("_id", id));
	    }

	    // }
	}
	finally {
	    if (records != null)
		records.close();
	}
    }
    
    /**Delete subscription feature change : method for getting the record
     * @param collection
     * @param id
     * @return Cache collection DB record. null if record not found
     */
    private static DBObject getCacheRecord(DBCollection collection, String id) {
	DBObject query = new BasicDBObject();
	query.put("_id", id);
	DBObject record = collection.findOne(query);
	return record;
    }
    
    //Delete subscription feature change : method for getting the deleted subscription list
    private static BasicDBList getChangeMapElements(BasicDBList tempSubList, Long temp_date, DBObject record, String id) {
	BasicDBList cacheSubList = new BasicDBList();
	BasicDBList changeMapElements = null;
	if (record != null) {

	    BasicDBList deleteSubs = (BasicDBList) record.get("deleteSubs");
	    cacheSubList = (BasicDBList) record.get("subscriptions");
	    Long date_cache = (Long) record.get("lastModified");
	    deleteSubs = deleteSubs == null ? new BasicDBList() : deleteSubs;
	    cacheSubList = cacheSubList == null ? new BasicDBList() : cacheSubList;
	    if (!cacheSubList.isEmpty() && (!tempSubList.isEmpty()))
		cacheSubList.removeAll(tempSubList);

	    Date limit = DateUtils.addHours(new Date(date_cache), 24);
	    if (new Date(temp_date).after(limit))
		changeMapElements = cacheSubList;
	    else {
		deleteSubs.removeAll(tempSubList);
		changeMapElements = deleteSubs;
		changeMapElements.addAll(cacheSubList);
	    }

	}
	else
	    changeMapElements = new BasicDBList();
	return changeMapElements;

    }

    public void handleDeleteEvent(Options options) throws MongoUtilsException {
	DBCursor recordList = null;
	try {
	    String contentDB = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "mongoDB");

	    String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_TEMP_COLLECTION);
	    String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
	    String historyCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_HISTORY_COLLECTION);
	    String cacheCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_CACHE_COLLECTION);

	    DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentDB);

	    DBCollection histCollection = db.getCollection(historyCollectionName);
	    DBCollection tempCollection = db.getCollection(tempCollectionName);
	    DBCollection liveCollection = db.getCollection(liveCollectionName);
	    DBCollection cacheCollection = db.getCollection(cacheCollectionName);

		DBObject deleteQuery = new BasicDBObject("eventType","delete");
		
		recordList = tempCollection.find(deleteQuery).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		//System.out.println(recordList.count());
		for(DBObject record : recordList){
			long currentTime=System.currentTimeMillis();
			long stagingTime=(Long)record.get("lastModified");
			
			String id = (String) record.get("_id");
			
			record.put("status","DELETE");
			record.put("hasAttachments", false);
			DBObject query = new BasicDBObject("_id",id);
			
			
			
			//DBCursor cur=cacheCollection.find(new BasicDBObject("_id", id));
			DBObject cacheRecord =  getCacheRecord(cacheCollection, id);
			
			//Handling Timestamp to make sure the document sent to live is latest always
			if (cacheRecord != null) {
				long cache_timeStamp = (Long) cacheRecord.get("lastModified");
				if(stagingTime > cache_timeStamp){
					record.put("lastModified",currentTime);
				}else
					record.put("lastModified",cache_timeStamp);
			}else{
				record.put("lastModified",currentTime);
			}
			liveCollection.update(query, record, true, false);
			
			if(cacheRecord!=null){
				record.removeField("_id");
				DBObject update = new BasicDBObject("$set",record);
				cacheCollection.update(query, update, true, false);
			}else{
				cacheCollection.update(query, record, true, false);
			}
			
			record.removeField("_id");
			handleHistory(id, record, histCollection);
			
			// Remove ODS Support Documents from GridFS
			if(options.getContentType().equalsIgnoreCase("support")){
				GridFS gfs = new GridFS(db, "ods_content");
				List<GridFSDBFile> gridFSDBFileList = gfs.find(new BasicDBObject("filename",id));
				if (gridFSDBFileList.size() > 0){
					gfs.remove(new BasicDBObject("filename",id));
				}
			}
			tempCollection.remove(query);
		}
		}finally{
			if(recordList!=null)
				recordList.close();
		}		
	}
	
	private void handleHistory(String id , DBObject record, DBCollection histCollection){

		DBObject histQuery = new BasicDBObject("documentId" , id);
		DBObject histFields = new BasicDBObject("documentId",1).append("lastModified", 1).append("_id",1).append("eventType", 1);
		DBObject histSort = new BasicDBObject("lastModified",-1);
		DBCursor histCursor = histCollection.find(histQuery,histFields).sort(histSort).limit(1);
		
		record.put("documentId", id);
		
		if(histCursor == null || histCursor.count() == 0){
			histCollection.insert(record);
			return;
		}
		DBObject rec = histCursor.next();
		//System.out.println(rec);
		if(rec != null){
			String eventType = (String) rec.get("eventType");
			if(eventType != null && !"".equals(eventType) && eventType.equalsIgnoreCase("delete")){
					DBObject update = new BasicDBObject("$set",record);
					//System.out.println(update);
					histCollection.update(rec, update, false, false);
					return;
			}
			histCollection.insert(record);
		}
	}
	
	public static void main(String[] args) throws MongoUtilsException{
		System.setProperty("mongo.configuration", "config/mongo.properties");
		String id = "col62969";
		String contentType = "soar";
		Options options = new Options();
		options.setContentType(contentType);
		ProcessorDAO procDAO = new ProcessorDAO();
		DBObject object = new BasicDBObject();
		
		object.put("_id", id);
		object.put("eventType", "delete");
		object.put("lastModified", System.currentTimeMillis());
		object.put("status", "delete");
		object.put("hasAttachments", true);
		object.put("priority",1);
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		DBCollection tempCollection = db.getCollection("metadata_temp");
		tempCollection.insert(object);
		procDAO.handleDeleteEvent(options);

	}
}

