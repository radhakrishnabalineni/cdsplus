package com.hp.cdsplus.processor.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DataExport {
	public static void main(String[] args) throws MongoUtilsException, IOException{
		System.setProperty("mongo.configuration", "config/mongo.properties");
		Set<String> contentList = ConfigurationManager.getInstance().keySet();
		File dataFolder = new File("data");
		dataFolder.mkdir();
		
		MongoClient localMongoClient = new MongoClient(new MongoClientURI("mongodb://cdspdbrw:WelcomeRW_11243@localhost/admin"));
		for(String contentType : contentList){
			if(contentType.equals("soar")){
				continue;
			}
			System.out.println("starting - "+contentType);
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
			DBCollection stagingCollection = db.getCollection("metadata_staging");
			if(stagingCollection == null){
				System.out.println("staging collection returned null. skipping content type - "+contentType);
				continue;
			}
			DB localDB = localMongoClient.getDB(contentType);
			DBCollection localStagingCollection = localDB.getCollection("metadata_staging");
			DBCursor cursor = stagingCollection.find();
			if(cursor == null || cursor.count() == 0){
				System.out.println("cursor returned null/zero size. skipping content type - "+contentType);
				continue;
			}
			cursor.limit(1000);
			System.out.println("starting load");
			for(DBObject record : cursor){
			localStagingCollection.insert(record);
			}
			System.out.println("finished - "+contentType);
		}
	}
}
