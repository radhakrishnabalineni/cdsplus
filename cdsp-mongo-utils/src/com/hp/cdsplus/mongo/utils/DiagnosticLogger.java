package com.hp.cdsplus.mongo.utils;

import java.util.HashMap;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


public class DiagnosticLogger {
	public static final String DB_NAME= "diagnostics";
	public static final String COLLECTION_NAME="transactions";
	static DB db = null;
	static DBCollection transCollection = null;
	private static boolean isEnabled = false;
	
	static DiagnosticLogger logger = null;
	
	private DiagnosticLogger(){
	}
	
	public static synchronized DiagnosticLogger getInstance(){
		if(logger == null){
			logger = new DiagnosticLogger();
		}
		
		return logger;
		
	}
	public static final void setEnabled(boolean enable){
		isEnabled = enable;
	}
	
	public static final void log(String transaction_id, String key, Object value) throws MongoUtilsException{
		HashMap<String, Object> valuesMap = new HashMap<String, Object>();
		valuesMap.put(key, value);
		log(transaction_id, valuesMap);
	}
	
	public static final void log(String transaction_id, HashMap<String, Object> valuesMap) throws MongoUtilsException{
		DBObject record = new BasicDBObject();
		record.putAll(valuesMap);
		log(transaction_id, record);
	}
	
	public static final void log(String transaction_id, DBObject record) throws MongoUtilsException{
		getInstance().logToDB(transaction_id,record);
	}
	
	private final void logToDB(String transaction_id, DBObject record) throws MongoUtilsException{
		if(isEnabled && transaction_id != null){
			db = ConfigurationManager.getInstance().getMongoDBAuthenticated(DB_NAME);
			transCollection = db.getCollection(COLLECTION_NAME);
			transCollection.update(new BasicDBObject("_id",transaction_id), new BasicDBObject("$set",record),true, false);
		}		
	}

}
