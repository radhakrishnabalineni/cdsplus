package com.hp.cdsplus.dao;

import java.util.Set;
import java.util.TreeSet;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ConfigDAO {
	MongoAPIUtils mongoUtils;
	public ConfigDAO() {
		mongoUtils = new MongoAPIUtils();
	}
	
	public Set<String> getSubscriptionList(String contentType) throws MongoUtilsException{
		Set<String> subscriptionSet = new TreeSet<String>();
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		DBCollection collection = db.getCollection("subscriptions");
		DBCursor cursor = collection.find();
		while(cursor.hasNext()){
			DBObject rec = cursor.next();
			if(rec != null){
				subscriptionSet.add(rec.get("_id").toString());
			}
		}
		return subscriptionSet;
		
	}
	
	public String getSubscription(String contentType, String subscriptionName) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		DBCollection collection = db.getCollection("subscriptions");
		DBObject subscription = collection.findOne(new BasicDBObject("_id",subscriptionName));
		return subscription.toString();
	}
	

	public String getSubscriptionSmartfolder(String contentType, String subscriptionName) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		DBCollection collection = db.getCollection("subscriptions");
		DBObject subscription = collection.findOne(new BasicDBObject("_id",subscriptionName));
		String smartfolder=(String)subscription.get("smartfolder");
		return smartfolder;
	}
	
	 public void deleteSubscriptionEntry(String contentType, String subscription) throws MongoUtilsException {
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		// get subscription filter
		BasicDBList subscriptionsList = new BasicDBList();
		subscriptionsList.add(subscription);
		DBObject query = new BasicDBObject("subscriptions", new BasicDBObject("$in", subscriptionsList));
		// get the metadata_cache collection
		DBCollection metadataCacheCollection = db.getCollection("metadata_cache");
		//commenting delete sub change for faster revaluation on 2/8/2015
		//DBObject updateCacheObj = new BasicDBObject("$addToSet", new BasicDBObject("deleteSubs", subscription));
		//metadataCacheCollection.update(query, updateCacheObj, false, true);
		DBObject deleteCacheObj = new BasicDBObject("$pull", new BasicDBObject("subscriptions", subscription));
		metadataCacheCollection.update(query, deleteCacheObj, false, true);
		DBCollection metadataLiveCollection = db.getCollection("metadata_live");
		DBObject deleteObj = new BasicDBObject("$pull", new BasicDBObject("subscriptions", subscription));
		metadataLiveCollection.update(query, deleteObj, false, true);
	    }
	
	public long evalSubscription(String contentType, String subscription) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		//get subscription filter
		DBCollection subscriptionCollection = db.getCollection("subscriptions");
		DBObject subscriptionObj = subscriptionCollection.findOne(new BasicDBObject("_id",subscription));
		
		Set<String> subscriptionSet = new TreeSet<String>();
		subscriptionSet.add(subscriptionObj.get("_id").toString());
		
		//get the metadata_live collection
		DBCollection metadataLiveCollection = db.getCollection("metadata_live");
		DBObject updateQuery = (DBObject) subscriptionObj.get("filter");
		
		updateQuery.put("$isolated", 1);
		
		DBObject updateObj = new BasicDBObject("$addToSet",new BasicDBObject("subscriptions",subscription));
		metadataLiveCollection.update(updateQuery, updateObj, false, true);
		
		long rowCount = metadataLiveCollection.count(new BasicDBObject("subscriptions", new BasicDBObject("$in",subscriptionSet)));
		
		return rowCount;
	}
}
