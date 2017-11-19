package com.hp.cdsplus.processor;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class ReloadEvents {

	public ReloadEvents() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		System.setProperty("mongo.configuration", "config/mongo.properties");
		DB db;
		try {
			db = ConfigurationManager.getInstance().getMongoDBAuthenticated("configDB");
			DBCollection collection = db.getCollection("status");
			//"1379047983081"
			DBObject query = new BasicDBObject("_id","soar");
			DBObject update = new BasicDBObject("lastModified",0L);
			collection.update(query, update,true, false);
			
			/*db = ConfigurationManager.getInstance().getMongoDBAuthenticated("soar");
			db.getCollection("metadata_cache").remove(new BasicDBObject());
			
			db = ConfigurationManager.getInstance().getMongoDBAuthenticated("library");
			db.getCollection("metadata_cache").remove(new BasicDBObject());
			
			db = ConfigurationManager.getInstance().getMongoDBAuthenticated("marketingstandard");
			db.getCollection("metadata_cache").remove(new BasicDBObject());
			*/
			query = new BasicDBObject("_id","library");
			update = new BasicDBObject("lastModified",0L);
			collection.update(query, update,true, false);
			
			query = new BasicDBObject("_id","marketingstandard");
			update = new BasicDBObject("lastModified",0L);
			collection.update(query, update,true, false);
			
			
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
