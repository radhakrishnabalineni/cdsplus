package com.hp.cdsplus.mongo.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.Hash;

public class DBLogger {
	private static final String DB_NAME= "diagnostics";
	private static final String COLLECTION_NAME="transactions";
	static DB db = null;
	static DBCollection transCollection = null;
	static HashMap<String, DBObject> buffer;
	private static boolean isEnabled = false;
	
	
	static int maxCount = 1000;
	public static final void init() throws MongoUtilsException{
		db = ConfigurationManager.getInstance().getMongoDBAuthenticated(DB_NAME);
		transCollection = db.getCollection(COLLECTION_NAME);
		buffer = new HashMap<String, DBObject>();
	}
	
	public static final void setEnabled(boolean enable){
		isEnabled = enable;
	}
	
	public static final void log(String transaction_id, String key, Object value){
		DBObject record = new BasicDBObject();
		record.put(key, value);
		log(transaction_id,record);
	}
	
	public static final void log(String transaction_id, DBObject record){
		if(isEnabled && transaction_id != null){
			transCollection.update(new BasicDBObject("_id",transaction_id), new BasicDBObject("$set",record),true, false);
		}		
	}

}
