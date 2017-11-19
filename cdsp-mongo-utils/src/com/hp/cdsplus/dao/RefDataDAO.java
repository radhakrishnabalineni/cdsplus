package com.hp.cdsplus.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class RefDataDAO {
	MongoAPIUtils mongoUtils ;
	private static final String LAST_MODIFIED_TS_FIELD="lastModified";
	private static final String METADATA="metadata";
	private static final String DOT=".";
	private static final String EVENT_TYPE="eventType";
	private static final String IS_ATTACHMENT="isAttachment";
	//SMO : Adding company_info for querying
	private static final String COMPANY_INFO="company_info";
	public RefDataDAO() {
		mongoUtils = new MongoAPIUtils();
	}
	
	public DBCursor getRefDataList(Options options) throws MongoUtilsException, OptionsException{

		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not been set. Set the content type in options class to retrieve the docuement list");
		}
		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type not defined");
		}
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
		DBCollection collection= db.getCollection("content.files");
		//GridFS gridFS = new GridFS(db,"content");
		//DBCursor cursor = gridFS.getFileList();
	
		/*DBObject query = new BasicDBObject();
		// last_modified > after and last_modified < before
		if(options.getAfter() > 0){
			query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD, new BasicDBObject("$gte", (options.getAfter())));
		}

		if(options.getBefore() > 0){
			query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD, new BasicDBObject("$lt", (options.getBefore())));
		}

		if(!options.isIncludeDeletes()){
			query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD+EVENT_TYPE, new BasicDBObject("$ne","delete"));
		}

		if(options.getQuery() != null){
			query.putAll(options.getQuery());
		}*/
		 QueryBuilder builder = QueryBuilder.start();

		  	//builder.and(METADATA+DOT+IS_ATTACHMENT).is(false);

			// last_modified > after and last_modified < before
			if(options.getAfter() > 0){
				builder.and(METADATA+DOT+LAST_MODIFIED_TS_FIELD).greaterThanEquals(options.getAfter());
			}

			if(options.getBefore() > 0){
				builder.and(METADATA+DOT+LAST_MODIFIED_TS_FIELD).lessThan(options.getBefore());
			}

			if(!options.isIncludeDeletes()){
				builder.and(METADATA+DOT+EVENT_TYPE).notEquals("delete");
			}

			if(options.getQuery() != null){
				builder.and(options.getQuery());
			}

			
		DBCursor cursor = collection.find(builder.get());
		cursor.sort(new BasicDBObject(METADATA+DOT+"lastModified",-1));
		
		if(options.getLimit() > 0){
			cursor.limit(options.getLimit());
		}
		return cursor;
	}
	
	public GridFSDBFile getRefData(String contentType, String fileName) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		GridFS gridFS = new GridFS(db,"content");
		DBObject query = new BasicDBObject("filename", fileName);
		return gridFS.findOne(query);
	}
	
	public List<GridFSDBFile> getRefData(String contentType, DBObject query) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		GridFS gridFS = new GridFS(db,"content");
		return gridFS.find(query);
	}
	
	public DBCursor getRefDataListFromLive(Options options) throws MongoUtilsException, OptionsException{
		
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not been set. Set the content type in options class to retrieve the docuement list");
		}
		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type not defined");
		}
		
		String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), ConfigurationManager.METADATA_LIVE_COLLECTION);
		if(isNull(liveCollectionName)){
			throw new OptionsException("Cannot find a valid metadata collection "+liveCollectionName+" provided for the specified content type : "+options.getContentType());
		}
		
		DBObject query = new BasicDBObject();


		// last_modified > after and last_modified < before
		if(options.getAfter() > 0){
			query.put(LAST_MODIFIED_TS_FIELD, new BasicDBObject("$gte", (options.getAfter())));
		}

		if(options.getBefore() > 0){
			query.put(LAST_MODIFIED_TS_FIELD, new BasicDBObject("$lt", (options.getBefore())));
		}

		if(!options.isIncludeDeletes()){
			query.put("eventType", new BasicDBObject("$ne","delete"));
		}
		if(!StringUtils.isEmpty(options.getCompany())){
			query.put(COMPANY_INFO , options.getCompany());
		}

		if(options.getQuery() != null){
			query.putAll(options.getQuery());
		}

		DBObject fields=  options.getDisplayFields();
		if(fields == null){
			fields = new BasicDBObject();
		}
		DBCursor cursor = mongoUtils.readMeta(options.getContentType(), liveCollectionName, query,fields);
		// reverse the order of the results
		cursor.sort(new BasicDBObject("lastModified",-1));

		//set limit if requested
		if(options.getLimit() > 0)
			cursor.limit(options.getLimit());
		//get the documents from collection and store it in memory to be returned to the calling method.

		return cursor;
	}
	
	public BasicDBList getDistinctDocumentList(Options options) throws MongoUtilsException{
		  DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
		  DBCollection dbCollection = db.getCollection("content.files");
			
		  QueryBuilder builder = QueryBuilder.start();

		  	builder.and(METADATA+DOT+IS_ATTACHMENT).is(false);

			// last_modified > after and last_modified < before
			if(options.getAfter() > 0){
				builder.and(METADATA+DOT+LAST_MODIFIED_TS_FIELD).greaterThanEquals(options.getAfter());
			}

			if(options.getBefore() > 0){
				builder.and(METADATA+DOT+LAST_MODIFIED_TS_FIELD).lessThan(options.getBefore());
			}

			if(!options.isIncludeDeletes()){
				builder.and(METADATA+DOT+EVENT_TYPE).notEquals("delete");
			}

			if(options.getQuery() != null){
				builder.and(options.getQuery());
			}
			if(!StringUtils.isEmpty(options.getCompany())){
				builder.and(METADATA+DOT+COMPANY_INFO).is(options.getCompany().toUpperCase());
			}

		/*		  BasicDBObject query = new BasicDBObject("metadata.isAttachment",false);
		
		  if(options.getAfter() > 0){
				query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD, new BasicDBObject("$gte", (options.getAfter())));
			}

			if(options.getBefore() > 0){
				query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD, new BasicDBObject("$lt", (options.getBefore())));
			}

			if(!options.isIncludeDeletes()){
				query.put(METADATA+DOT+LAST_MODIFIED_TS_FIELD+EVENT_TYPE, new BasicDBObject("$ne","delete"));
			}

			if(options.getQuery() != null){
				query.putAll(options.getQuery());
			}*/


		  //Set<String> docIdSet = new TreeSet<String>();
		//	System.out.println("Query : "+builder.get());
		  DBCursor cursor =  dbCollection.find(builder.get());
		  
		  cursor.sort(new BasicDBObject(METADATA+DOT+LAST_MODIFIED_TS_FIELD,-1));
		  
		    
		  // if (cursor != null  && !(cursor.size() == 0)) {
		  if(options.getLimit() != 0)
			  cursor.limit(options.getLimit());
		  else
			  cursor.limit(10000);
		  
//		  if(options.isReverse()){
//				cursor.sort(new BasicDBObject(METADATA+DOT+LAST_MODIFIED_TS_FIELD,1));
//			}
		  BasicDBList resultList = new BasicDBList();
			  while(cursor.hasNext()){
			    DBObject record = cursor.next();
			    if(record != null &&  record.get("metadata") != null){
					//if(docIdSet.add(((BasicDBObject)record.get("metadata")).get("docid").toString())){
					resultList.add(((BasicDBObject)record.get("metadata")).get("docid").toString());
					//}
				    //if(options.getLimit() > 0 && docIdSet.size() == options.getLimit()){
				    //	break;
				   // }
				}
			}			
		//}
		return resultList;
	}
	
	
	public Iterable<DBObject> getMapReduceWithHasAttachments(Options options,BasicDBList docidList){
		  DB db=null;
		  try {
			db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
		  } catch (MongoUtilsException e) {
			e.printStackTrace();
		  }
		  DBCollection dbCollection = db.getCollection("content.files");
		
		  String map ="if(this.metadata.docid && this.metadata.fileName){var id = this.metadata.docid;"+
          		"if(this.metadata.fileName === (id + '.xml')){emit(id, {doc: this}); }else {emit(id, {attach: true});}}";
          
          String reduce = "function(key,vals){return vals.reduce(function(o1, o2){return{doc: o1.doc || o2.doc,"+
          "attach: o1.attach || o2.attach};});}";
          
          String finalize = "function(key, val){if(val.doc){val.doc.hasAttachment = !!val.attach;}return val.doc;}";
          
          BasicDBObject query = new BasicDBObject("metadata.docid",new BasicDBObject("$in", docidList));         
          
          MapReduceCommand cmd = new MapReduceCommand(dbCollection, map, reduce, null, MapReduceCommand.OutputType.INLINE, query);
//          if(options.getLimit()!=0)
//        	  cmd.setLimit(options.getLimit());
//          else 
//        	 cmd.setLimit(10000);
          //cmd.setSort(new BasicDBObject("metadata.lastModified","-1"));
          cmd.setFinalize(finalize);
          
          MapReduceOutput out = dbCollection.mapReduce(cmd);

          return out.results();
	}
	
	public static void main(String[] args){
		System.setProperty("mongo.configuration","config/mongo.properties");
		try {
//			DBCursor cursor = new RefDataDAO().getRefDataList("soar_ref_data");
//			while(cursor.hasNext()){
//				System.out.println(cursor.next());
//			}
			GridFSDBFile file = new RefDataDAO().getRefData("soar_ref_data", "environments.xml");
			System.out.println(file);
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param value
	 * @return
	 */
	private boolean isNull(String value) {
		if(value == null || "".equals(value))
			return true;
		return false;
	}
}
