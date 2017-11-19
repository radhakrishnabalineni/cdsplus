package com.hp.cdsplus.mongo.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoInternalException;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.log4j.Logger;

/**
 * This class was created for soar feed as there was a need to handle documents beyond
 * the size limit permitted by the mongo API. All soar collections will invoke the writeMeta
 * method but only the larger collections will invoke the disAssembleLargeCollection. Such
 * documents will be classified as  isLargeData
 * size limitations 
 * 
 */
public class SoarMongoAPIUtils extends MongoAPIUtils {
	Logger logger = Logger.getLogger(SoarMongoAPIUtils.class);

	public static final int BIG_MESSAGE_LIMIT = 1024 * 1024 * 14;
	public static final String SOAR_DB_NAME = "soar";
	public static final String ID_FIELD = "_id";
	private static final String SOAR_SOFTWARE_FEED_DOC = "soar-software-feed";
	private static final String SOAR_DOC_COLLECTION = "collection";
	private static final String SOAR_SOFTWARE_ITEMS_DOC = "software-items";
	private static final String SOAR_SOFTWARE_ITEM_DOC = "software-item";
	private static final String PRODUCT_ENVIRONMENTS_SUPPORTED_DOC = "products-environments-supported";
	private static final String SOAR_ITEM_ID="@item-ID";
	private static final String SW_ITEM_ENVIRONMENT_FILE_COLLECTION = "environment_data";

	/**
	 * This method is overridden from MongoAPIUtils specifically for soar documents
	 * It attaches the data that has been persisted to gridfs during staging
	 * size limitations 
	 * @return
	 */
	
	@Override
	public WriteResult writeMeta(DB db, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException {
		WriteResult result = null;
		DBCollection collection = db.getCollection(collectionName);
		String docId = (String)dbObject.get(ID_FIELD);
		try {
			int metadataSize = dbObject.toString().length();
			if(metadataSize >= BIG_MESSAGE_LIMIT) {
				logger.debug("Collection " + docId + " is big data");
				dbObject = disAssembleLargeCollection(dbObject);
			}

			result =  collection.update(findQuery, dbObject,upsert,multi);
		}

		catch( MongoInternalException ex){
			logger.debug("MongoInternalException exception thrown for collection " + docId + ".");
			logger.debug("Message for collection " + docId + " " + ex.getMessage());
			throw ex;
		}
		return result;
	}
	
	/**
	 * This method will be executed only for soar documents which had crossed the
	 * size limitations during staging
	 * @param dbObject
	 * @param key
	 * @return
	 */

	private DBObject disAssembleLargeCollection(DBObject largeObject) throws MongoUtilsException{
		String docId = (String)largeObject.get(ID_FIELD);
		String associatedFile = docId + "_" + System.currentTimeMillis();
		largeObject.put("largeMetadataFile", associatedFile);
		DBObject gridFSDBFile = new BasicDBObject();
		DBObject soarsoftwarefeed = getObject(largeObject, SOAR_SOFTWARE_FEED_DOC);
		if (soarsoftwarefeed == null) {
			String message = "soar-software-feed for document " + docId + " is null. Parsing issue.";
			throw new MongoInternalException(message);
		}
		else {
			DBObject collect = getObject(soarsoftwarefeed,SOAR_DOC_COLLECTION);
			if (collect != null) {
				DBObject software_items = getObject(collect, SOAR_SOFTWARE_ITEMS_DOC);
				if (software_items != null) {
					BasicDBList software_item_list = getList(software_items,SOAR_SOFTWARE_ITEM_DOC);
					Iterator<?> itr = software_item_list.iterator();
					while (itr.hasNext()) {
						DBObject software_item = (DBObject) itr.next();
						String item_id= (String)software_item.get(SOAR_ITEM_ID);
						DBObject env = (DBObject) software_item.removeField(PRODUCT_ENVIRONMENTS_SUPPORTED_DOC);
						gridFSDBFile.put(item_id, env);
					}

					DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(SOAR_DB_NAME);				
					GridFS gfs = new GridFS(db, SW_ITEM_ENVIRONMENT_FILE_COLLECTION);
					GridFSInputFile gfsFile = gfs.createFile(gridFSDBFile.toString().getBytes());
					gfsFile.setFilename(associatedFile);
					gfsFile.setContentType(SOAR_DB_NAME);
					gfsFile.save();
				}
				else{
					String message = "No software items found for large SOAR document " + docId + "." ;
					throw new MongoUtilsException(message);
				}
			}
		}

		return largeObject;
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return
	 */
	private DBObject getObject(DBObject dbObject, String key) {
		DBObject returnObj = new BasicDBObject();
		Object obj = dbObject.get(key);
		if (obj == null || obj == "") {
			return null;
		} else if (obj instanceof DBObject) {
			returnObj = (DBObject) obj;

		} else if (obj instanceof String) {
			returnObj = (DBObject) obj;

		}
		return returnObj;
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return list of DB Object
	 */
	private BasicDBList getList(DBObject dbObject, String key) {
		Object object = dbObject.get(key);
		if (object == null || object=="") {
			return new BasicDBList();
		}

		BasicDBList returnList = new BasicDBList();

		if (object instanceof BasicDBList) {
			return (BasicDBList) object;
		} else if (object instanceof DBObject) {
			returnList.add((DBObject) object);
		} else if (object instanceof String) {
			String str = (String) object;
			if (key.equalsIgnoreCase(SOAR_SOFTWARE_ITEM_DOC) ) {
				returnList.addAll(Arrays.asList(str.split(",")));
			}
			else {
				returnList.add(str);
			}

		}else{
			returnList.add(object);
		}
		return returnList;
	}
	
	/**
	 * This method is overridden from MongoAPIUtils to remove the respective environment file
	 * which was created during staging for metadata which had size beyond BSON size limit 
	 * @return WriteResult
	 */
	
	@Override
	public WriteResult deleteMeta(DB db, String collectionName, DBObject dbo, DBObject sortFields, int offset) throws MongoUtilsException {
		DBCollection collection = db.getCollection(collectionName);
		GridFS gfs = new GridFS(db, SW_ITEM_ENVIRONMENT_FILE_COLLECTION);
		DBCursor cursor = collection.find(dbo);
		WriteResult result=null;
		cursor.sort(sortFields);
		cursor.skip(offset);
		while(cursor.hasNext()){
			DBObject toBeRemoved = cursor.next();
			String file =  (String) toBeRemoved.get("largeMetadataFile");
			if( file != null ){
				gfs.remove(file);
			}
			
			result = collection.remove(toBeRemoved);
		}
		
		return result;
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws MongoUtilsException {
		// TODO Auto-generated method stub
		/*String mailPropertiesPath= "C://c_p//COL58727 - Copy//COL58727 - Copy.xml";
		BufferedReader br = new BufferedReader(new FileReader(mailPropertiesPath));
		String line = br.readLine();*/
		try {
			ConfigurationManager.getInstance().printCache();
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BasicDBObject gridFSDBFile = new BasicDBObject();
		gridFSDBFile.put("data", "abcdef");
		gridFSDBFile.put("name", "rtccffee");
		gridFSDBFile.put("city", "fremont");
		
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(SOAR_DB_NAME);				
		GridFS gfs = new GridFS(db, SW_ITEM_ENVIRONMENT_FILE_COLLECTION);
		GridFSInputFile gfsFile = gfs.createFile(gridFSDBFile.toString().getBytes());
		
		String docId = "COL58727Test";
		long millisStart = Calendar.getInstance().getTimeInMillis();
		
		SoarMongoAPIUtils soar = new SoarMongoAPIUtils();
		//soar.versionize(gfsFile, docId, millisStart );		

	}
}
