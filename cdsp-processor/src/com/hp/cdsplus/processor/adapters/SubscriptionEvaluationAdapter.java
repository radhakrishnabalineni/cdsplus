package com.hp.cdsplus.processor.adapters;

import java.util.Map;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class SubscriptionEvaluationAdapter implements Adapter{

	private DB db;
	private Map<String, DBObject> subscriptionMap;
	/*
	 * 1. Get Map of subscriptions for provided content type
	 * 3. for each subscription evaluate on temp collection(apply filter).
	 * 4. write the subscription name against each matching document in temp collection.
	 */

	public void evaluate(String contentType, String subscriptionCollectionName, String targetCollectionName) throws MongoUtilsException, AdapterException, OptionsException {
		
		if(isNull(contentType)){
			throw new AdapterException("Content Type parameter cannot be null/blank");
		}
		
		if(isNull(subscriptionCollectionName)){
			throw new AdapterException("Subscription collection cannot be null/blank");
		}
		
		if(isNull(targetCollectionName)){
			throw new AdapterException("Metadata Target collection cannot be null/blank");
		}
		
		getDBHandle(contentType);
		
		prepareSubscriptionsMap(contentType);
		
		evaluateAndUpdateDocuments(targetCollectionName);
		
	}

	public void prepareSubscriptionsMap(String contentType) throws OptionsException, MongoUtilsException{
		Options options = new Options();
		options.setContentType(contentType);
		this.subscriptionMap = new ContentDAO().getAllSubscriptions(options);
	}


	public void evaluateAndUpdateDocuments(String targetCollectionName) throws OptionsException, MongoUtilsException{
		DBCollection targetCollection  = db.getCollection(targetCollectionName);
		DBCursor cursor;
		DBObject dbobject;
		for (Map.Entry<String, DBObject> subscription : subscriptionMap.entrySet()){
			dbobject= subscription.getValue();
			// changes to make sure only the documents in temp table with SUB_EVAL_STARTED status will be picked up for evaluation
			dbobject.put("$and", new BasicDBObject("processStatus",ProcessStatus.SUB_EVAL_STARTED));
			
			
			cursor = targetCollection.find(dbobject);
				for(DBObject record : cursor){
					updateDocument(subscription.getKey(), record, targetCollection);
				}

		}
	}

	public void updateDocument(String subscriptionName, DBObject dbObject, DBCollection targetCollection) throws MongoUtilsException, OptionsException{
		BasicDBList subscriptionsList = (BasicDBList) dbObject.get("subscriptions");
		if(subscriptionsList!=null){
			if(!subscriptionsList.contains(subscriptionName)){
				subscriptionsList.add(subscriptionName);
			}
		}else{
			subscriptionsList = new BasicDBList();
			subscriptionsList.add(subscriptionName);
		}
		dbObject.put("subscriptions", subscriptionsList);
		targetCollection.save(dbObject);
		
	}

	public void getDBHandle(String contentType) throws MongoUtilsException {
		db=ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
	}
	
	private boolean isNull(String string){
		if(string == null || "".equals(string)){
			return true;
		}
		return false;
	}
	
	

	@Override
	public void evaluate(Item item) throws ProcessException, OptionsException,
			MongoUtilsException {
		ContentDAO contentDao = new ContentDAO();
		
		Options options = new Options();
		options.setContentType(item.getContentType());
		
		this.subscriptionMap = contentDao.getAllSubscriptions(options);
		
		for (Map.Entry<String, DBObject> subscription : subscriptionMap.entrySet()){
			
			DBObject update = new BasicDBObject("$addToSet",new BasicDBObject("subscriptions",subscription.getKey()));
			QueryBuilder qBuilder = QueryBuilder.start("processStatus").is(ProcessStatus.SUB_EVAL_STARTED.toString());
			qBuilder.and(subscription.getValue());
			
			options = new Options();
			options.setContentType(item.getContentType());
			options.setMetadataDocument(update);
			options.setQuery(qBuilder.get());
			
			contentDao.updateAllMetadataInTemp(options);
		}
	}
}