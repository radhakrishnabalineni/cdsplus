package com.hp.cdsplus.adapters;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

public class SWItemEnvironmentAdapter {
	private static final Logger logger = LogManager.getLogger(SWItemEnvironmentAdapter.class);

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

	
	public DBObject disAssembleLargeCollection(DBObject largeObject){
		largeObject.put("isLargeData", true);
		String docId = (String)largeObject.get(ID_FIELD);
		try{
			DBObject gridFSDBFile = new BasicDBObject();
			DBObject soarsoftwarefeed = getObject(largeObject, SOAR_SOFTWARE_FEED_DOC);
			if (soarsoftwarefeed == null) {
				logger.debug("Sub ducument: document for is null");
				return null;
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
						gfsFile.setFilename(docId + "env");
						gfsFile.setContentType(SOAR_DB_NAME);
						List<GridFSDBFile> fileList=gfs.find(new BasicDBObject().append("filename", docId + "env"));
						if(fileList.size()==0){
							gfsFile.save();
						}else if (fileList.size()>0){
							gfs.remove(new BasicDBObject().append("filename",docId + "env"));
							gfsFile.save();	
						}
					}
				}
			}

		}
		catch(MongoUtilsException e){

		}

		return largeObject;
	}

	public DBObject assembleLargeCollection(String docId, DBObject largeObject) {
		GridFSDBFile gridFSDBFile = null;
		InputStream inputStream = null;
		StringWriter writer = new StringWriter();
		String gridFSData = null;
		DBObject allSwItemEnv = null;

		try {

			//DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(SOAR_DB_NAME);
			GridFS gfs = new GridFS(db, SW_ITEM_ENVIRONMENT_FILE_COLLECTION);

			List<GridFSDBFile> gridFSDBFileList = gfs.find(new BasicDBObject().append("filename",docId + "env"));
			if(gridFSDBFileList.iterator().hasNext()){
				gridFSDBFile = gridFSDBFileList.iterator().next();
				inputStream = gridFSDBFile.getInputStream(); 
				IOUtils.copy(inputStream, writer, "UTF-8");
				gridFSData = writer.toString();
				allSwItemEnv = (DBObject) JSON.parse(gridFSData);

				DBObject soarsoftwarefeed = getObject(largeObject, SOAR_SOFTWARE_FEED_DOC);
				if (soarsoftwarefeed == null) {
					logger.debug("Sub ducument: document for is null");
					return null;
				}
				else {
					DBObject collect = getObject(soarsoftwarefeed,SOAR_DOC_COLLECTION);
					if (collect != null) {
						DBObject software_items = getObject(collect, SOAR_SOFTWARE_ITEMS_DOC);
						BasicDBList software_item_list = getList(software_items,SOAR_SOFTWARE_ITEM_DOC);
						Iterator<?> itr = software_item_list.iterator();
						while (itr.hasNext()) {
							DBObject software_item = (DBObject) itr.next();
							String item_id= (String)software_item.get(SOAR_ITEM_ID);
							DBObject env = (DBObject)allSwItemEnv.get(item_id);
							software_item.put(PRODUCT_ENVIRONMENTS_SUPPORTED_DOC, env);

						}

					}

				}

			}
		}catch (MongoUtilsException e) {
			e.printStackTrace();
			return null;
		}catch (IOException e) {
			e.printStackTrace();
			return null;
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

	public static void main(String [] args) throws Exception {
		String abcd = "DBObject of size 5242897 is over Max BSON size 16777216";
		if((abcd != null) && (abcd.toLowerCase().contains("maxx bson size"))){
			System.out.println("true");
		}
		else{
			System.out.println("false");
		}
		/*SWItemEnvironmentAdapter adapter = new SWItemEnvironmentAdapter();
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		//DBCollection collection = db.getCollection(this.mappingCollectionName);
		//List<String> databases = ConfigurationManager.getInstance().getConfigMappings().loadConfigDB();

		ConversionUtils conversion = new ConversionUtils();
		DBObject gridFSUpdateDocument = new BasicDBObject();
		boolean isBigData =  true;

		//File xmlFile = new File("C:/c_p/one_software_item.xml");
		File xmlFile = new File("C:/c_p/two_software_items.xml");
		InputStream is= new FileInputStream(xmlFile);
		//String result= conversion.convertXMLtoJSON(IOUtils.toByteArray(is), "com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed");
		String json= conversion.convertXMLtoJSON(IOUtils.toByteArray(is), "com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed");
		//Object object = conversion.convertJSONtoObject(result, "com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed");
		DBObject dbObject = (DBObject) JSON.parse(json);
		System.out.println(dbObject);
		int dbl = dbObject.toString().length();

		int length = new BasicBSONEncoder().encode(dbObject).length;
		System.out.println("BSON LENGTH " + length + " DBOBJECT LENGTH " + dbl );
		String docId = "COL58727";
		
		dbObject = adapter.disassembleLargeCollection(dbObject);
		DBObject finalObject = adapter.assembleLargeCollection(docId, dbObject);
		
		System.out.println(finalObject);*/
	}
}
