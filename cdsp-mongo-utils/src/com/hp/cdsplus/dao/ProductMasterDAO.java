package com.hp.cdsplus.dao;

import java.util.Arrays;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class ProductMasterDAO {

	private static final String CONTENT_COLLECTION_NAME = "contentCollection";
	private static final String HIERARCHY_COLLECTION_NAME = "hierarchyCollection";
	private static final String EXTRACTION_LOGS_COLLECTION_NAME="pmExtractionLogsCollection";

	public ProductMasterDAO() {
	}

	public DBObject getContent(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");
		
		// check if for the given content type we get a valid subscription collection name
		//SMO:User Story #7850 adding options.getCompany() in .getMappingValue method
		String subCollName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+CONTENT_COLLECTION_NAME);
		if(isNull(subCollName)){
			throw new OptionsException("Cannot find a "+CONTENT_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		if(isNull(options.getDocid()))
			throw new OptionsException("OID cannot be null. Set Oid using Options.setDocid() method");

		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), subCollName, options.getDocid());
		if(cursor.hasNext()){
			DBObject record = cursor.next();
			String hLevel = (String) record.get("hierarchy_level");
			ProductLevel level = ProductLevel.valueOf(hLevel);
			if(level == null)
				return record;
			System.out.println(level);
			DBObject query = new BasicDBObject("_id",options.getDocid());
			DBObject childfields = level.getChildLevelsFields();
			DBObject parentFields = level.getParentLevelFields();
			DBObject keys = new BasicDBObject();
			keys.putAll(childfields);
			keys.putAll(parentFields);
			
			Options hOptions = new Options();
			hOptions.setContentType(options.getContentType());
			hOptions.setQuery(query);
			//SMO : #7555
			hOptions.setCompany(options.getCompany());
			hOptions.setDisplayFields(keys);
			DBObject hRecord = this.getHierarchy(hOptions);
			// SMO:User Story #7555
			if (hRecord == null) 
			{
				return null;
			}
			StringBuffer temp = new StringBuffer();
			
			for(String key : childfields.keySet()){
				String oidS = (String)hRecord.get(key);
				if(oidS.endsWith(",")){
					temp.append(oidS);
				}else{
					temp.append(oidS).append(",");
				}
			}
			String str = temp.toString();
			if(str.endsWith(","))
				str= str.substring(0, str.lastIndexOf(","));
			record.put("children", Arrays.asList(str.split(",")));
			
			temp.setLength(0);
			
			for(String key : parentFields.keySet()){
				String oidS = (String)hRecord.get(key);
				if(oidS.endsWith(",")){
					temp.append(oidS);
				}else{
					temp.append(oidS).append(",");
				}
			}
			str = temp.toString();
			if(str.endsWith(","))
				str= str.substring(0, str.lastIndexOf(","));
			record.put("parents", Arrays.asList(str.split(",")));
			return record;
		}else 
			return null;
	}
	
	

	public DBObject getHierarchy(Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");


		// check if for the given content type we get a valid subscription collection name
		//SMO:User Story #7850 adding options.getCompany() in .getMappingValue method
		String subCollName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+HIERARCHY_COLLECTION_NAME);
		if(isNull(subCollName)){
			throw new OptionsException("Cannot find a "+HIERARCHY_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}
		
		DBObject query = options.getQuery();
		
		if(query == null){
			query = new BasicDBObject();
			if(isNull(options.getDocid()))
				throw new OptionsException("OID cannot be null. Set Oid using Options.setDocid() method");
			query.put("_id", options.getDocid());
		}
		
		
		DBObject dispFields = options.getDisplayFields();
		if(dispFields == null)
			dispFields = new BasicDBObject();
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), subCollName, query, dispFields);
		if(cursor.hasNext())
			return cursor.next();
		else return null;
	}

	public DBCursor getContentList(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");


		// check if for the given content type we get a valid subscription collection name
		//SMO:User Story #7850 adding options.getCompany() in .getMappingValue method
		String subCollName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+CONTENT_COLLECTION_NAME);
		if(isNull(subCollName)){
			throw new OptionsException("Cannot find a "+CONTENT_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		QueryBuilder queryBuilder = QueryBuilder.start();

		/*if(queryBuilder == null){
			queryBuilder = QueryBuilder.start();
		}*/

		if(options.getAfter() > 0L)
			//query.put("lastModified", new BasicDBObject("$gt",options.getAfter()));
			queryBuilder.and("last_modified").greaterThan(options.getAfter());

		if(options.getBefore() > 0L)
			queryBuilder.and("last_modified").lessThanEquals(options.getBefore());

		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), subCollName, queryBuilder.get());
		cursor.sort(new BasicDBObject("last_modified",-1));
		if(options.getLimit() > 0){
			cursor.limit(options.getLimit());
		}
		return cursor;
	}

	public DBCursor getHierarchyList(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");


		// check if for the given content type we get a valid subscription collection name
		//SMO:User Story #7850 adding options.getCompany() in .getMappingValue method
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+HIERARCHY_COLLECTION_NAME);
		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a "+HIERARCHY_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		/*DBObject query = options.getQuery();

		if(query == null){
			query = new BasicDBObject();
		}*/
		QueryBuilder queryBuilder=QueryBuilder.start();

		if(options.getAfter() > 0L)
		//	query.put("lastModified", new BasicDBObject("$gte",options.getAfter()));
			queryBuilder.and("last_modified").greaterThan(options.getAfter());
		if(options.getBefore() > 0L)
//			query.put("lastModified", new BasicDBObject("$lte",options.getBefore()));
			queryBuilder.and("last_modified").lessThanEquals(options.getBefore());
		
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), collectionName, queryBuilder.get());
		cursor.sort(new BasicDBObject("last_modified",-1));
		if(options.getLimit() > 0){
			cursor.limit(options.getLimit());
		}
		return cursor;
	}

	public Object addLogEntry(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");

		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), EXTRACTION_LOGS_COLLECTION_NAME);

		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a "+EXTRACTION_LOGS_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		DBObject metadataDoc = options.getMetadataDocument();
		if(metadataDoc == null){
			throw new OptionsException("Metadata Document cannot be passed null");
		}

		options.getMongoUtils().insertMeta(options.getContentType(), collectionName, metadataDoc);

		return metadataDoc.get("_id");
	}

	public void updateLogEntry(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");

		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), EXTRACTION_LOGS_COLLECTION_NAME);

		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a "+EXTRACTION_LOGS_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		if(options.getMetadataDocument() == null){
			throw new OptionsException("Metadata Document cannot be passed null");
		}

		if(options.getQuery() == null){
			throw new OptionsException("Query cannot be passed null");
		}

		options.getMongoUtils().writeMeta(options.getContentType(), collectionName,options.getQuery(), options.getMetadataDocument());
	}

	public DBCursor getLogEntry(Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");

		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), EXTRACTION_LOGS_COLLECTION_NAME);

		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a "+EXTRACTION_LOGS_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		if(options.getQuery() == null){
			throw new OptionsException("Query cannot be passed null");
		}

		return options.getMongoUtils().readMeta(options.getContentType(), collectionName, options.getQuery());

	}

	public void updateContent(Options options) throws OptionsException, MongoUtilsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+CONTENT_COLLECTION_NAME);
		this.update(collectionName, options);
	}
	
	public void updateHierarchy(Options options) throws MongoUtilsException, OptionsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+HIERARCHY_COLLECTION_NAME);
		this.update(collectionName, options);
	}
	
	public void update(String collectionName, Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");

		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a "+CONTENT_COLLECTION_NAME+" property against "+options.getContentType()+" DB");
		}

		if(options.getQuery() == null){
			throw new OptionsException("Query cannot be passed null");
		}
		if(options.getMetadataDocument() == null){
			throw new OptionsException("Update document cannot be passed null");
		}
		
		options.getMongoUtils().writeMeta(options.getContentType(), collectionName, options.getQuery(), options.getMetadataDocument(),true, false);
	}
	
	public void deleteContent(Options options) throws MongoUtilsException, OptionsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+CONTENT_COLLECTION_NAME);
		this.delete(options, collectionName);
	}
	
	public void deleteHierarchy(Options options) throws MongoUtilsException, OptionsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), options.getCompany()+HIERARCHY_COLLECTION_NAME);
		this.delete(options, collectionName);
	}
	
	public void delete(Options options, String collectionName) throws OptionsException, MongoUtilsException {
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type cannot be null. Set content type using Options.setContentType() method");

		if(isNull(collectionName)){
			throw new OptionsException("Collection name cannot be null");
		}
		
		if(isNull(options.getDocid())){
			throw new OptionsException("OID cannot be null. Set Oid using Options.setDocid() method");
		}
		options.getMongoUtils().deleteMeta(options.getContentType(), collectionName, options.getDocid());
	}

	/**
	 * @param value
	 * @return
	 */
	private boolean isNull(Object value) {
		if(value == null || "".equals(value))
			return true;
		return false;
	}

	public static void main(String[] args){
		System.setProperty("mongo.configuration", "config/mongo.properties");
		Options options = new Options();
		options.setContentType("productmaster");
		options.setLimit(10);
		options.setBefore(1381997731551L);
		options.setAfter(1381246360765L);
		//options.setDocid("255222");
		
		ProductMasterDAO pmDao = new ProductMasterDAO();
		try {
			DBCursor record = pmDao.getHierarchyList(options);
			System.out.println(record);
			for(DBObject obj : record){
			System.out.println(obj);
		}
			//System.out.println(record);
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
