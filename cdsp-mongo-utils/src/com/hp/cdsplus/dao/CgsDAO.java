package com.hp.cdsplus.dao;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class CgsDAO {
	public static final String DB_NAME_KEY = "mongoDB";
	private static final String LAST_MODIFIED_TS_FIELD="lastModified";
	private static final String COMPANY_INFO="company_info";
	public DBCursor getDocumentList(Options options) throws MongoUtilsException{
		System.out.println(options.getContentType());
		
		String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(),DB_NAME_KEY);
		System.out.println(dbName);
		DB cgsDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
		DBCollection liveCollection = cgsDB.getCollection(collectionName);
		
		QueryBuilder builder = QueryBuilder.start();
		
		if(options.getAfter() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).greaterThanEquals(options.getAfter());
		}

		if(options.getBefore() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).lessThan(options.getBefore());
		}
		if(!StringUtils.isEmpty(options.getCompany())){
			builder.and(COMPANY_INFO).is(options.getCompany().toUpperCase());
		}
		if(options.getQuery() != null){
			builder.and(options.getQuery());
		}
		
		DBCursor cursor = liveCollection.find(builder.get());
		if(options.getLimit() > 0){
			cursor.limit(options.getLimit());
		}
		return cursor;
	}
	
	public DBObject getDocumentMetadata(Options options) throws MongoUtilsException, OptionsException{
		if(options.getContentType() == null || "".equals(options.getContentType())){
			throw new OptionsException("Content Type cannot be null");
		}
		String dbName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(),DB_NAME_KEY);
		DB cgsDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
		DBCollection liveCollection = cgsDB.getCollection(collectionName);
		
		if(options.getDocid() == null || "".equals(options.getDocid())){
			throw new OptionsException("Doc id cannot be null");
		}
		return liveCollection.findOne(new BasicDBObject("_id",options.getDocid()));
	}
	
	public static void main(String[] args){
		System.setProperty("mongo.configuration", "config/mongo.properties");
		Options options = new Options();
		options.setContentType("cgs");
		options.setDocid("c00650017");
		
		

		try {
			DBObject record = new CgsDAO().getDocumentMetadata(options);
			DBObject groups = (DBObject)record.get("groups");
			ArrayList list  = (ArrayList) groups.get("group");
			for(int i =0 ; i < list.size(); i++){
				System.out.println(list.get(i));
			}
			/*DBCursor cursor = new CgsDAO().getDocumentList(options);
			while(cursor.hasNext()){
				System.out.println(cursor.next());
			}*/
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
