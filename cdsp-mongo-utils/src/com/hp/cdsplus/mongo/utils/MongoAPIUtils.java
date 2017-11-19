package com.hp.cdsplus.mongo.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
/**
 * 
 * <br><br>
 * This class provides methods to possible CURD operations over mongoDB.<br>
 * <i><b>Dependencies:</b></i><br>
 *	mongo-java-driver-2.10.1.jar
 * 
 * 
 */


public class MongoAPIUtils {

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * Provide options to specify upsert, multi parameters same as the mongo update command and works the same way as mongo update.<br>
	 * Since the collection name is not part of the parameter, by default this method operates on collection named <b>metadata</b><br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, java.lang.String, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String id, String collectionName, DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @param dbObject
	 * @param upsert
	 * @param multi
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, String id, DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException{
		// by default writes into staging collection in the database
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_STAGING_COLLECTION);
		return writeMeta(dbName, id, collectionName , dbObject, upsert, multi);
	}

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * Provide options to specify upsert, multi parameters same as the mongo update command and works the same way as mongo update.<br>
	 * This method operates on collection name provided in the parameter(param, <b>collectionName</b>).<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, DBObject, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @param collectionName
	 * @param dbObject
	 * @param upsert
	 * @param multi
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, String id, String collectionName, DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException{
		return writeMeta(dbName, collectionName, new BasicDBObject(ConfigurationManager.ID_FIELD,id), dbObject, upsert, multi);
	}

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * There is no parameters to specify upsert, multi parameters.
	 * By default <i>false</i> is passed for both upsert, multi.<br>
	 * This method operates on collection name provided in the parameter(param, <b>collectionName</b>).<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, java.lang.String, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String id, String collectionName, DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @param collectionName
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, String id, String collectionName, DBObject dbObject) throws MongoUtilsException{
		return writeMeta(dbName, id, collectionName, dbObject, false, false);
	}

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * There is no parameters to specify upsert, multi parameters.
	 * By default <i>false</i> is passed for both upsert, multi.<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String id, DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, String id, DBObject dbObject) throws MongoUtilsException{
		return writeMeta(dbName, id, dbObject, false, false);
	}

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * Provide options to specify upsert, multi parameters same as the mongo update command and works the same way as mongo update.<br>
	 * Since the collection name is not part of parameter list, this method operates on collection name <b>metadata</b>.<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, DBObject, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param findQuery
	 * @param dbObject
	 * @param upsert
	 * @param multi
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException{
		//by default it writes into metadata staging collection
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_STAGING_COLLECTION);
		return writeMeta(dbName, collectionName, findQuery, dbObject, upsert, multi);
	}

	/**
	 * This method authenticates the mongo database(param,<b>dbName</b>).<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(DB, java.lang.String, DBObject, DBObject, boolean, boolean)">
	 * writeMeta(DB db, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param findQuery
	 * @param dbObject
	 * @param upsert
	 * @param multi
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */

	public WriteResult writeMeta(String dbName, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException {
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return writeMeta(db, collectionName, findQuery, dbObject, upsert , multi);
	}
	
	/**
	 * This method used to write data to mongo database(com.mongodb.DB param,<b>db</b> of collection <b>collectionName</b>).<br>
	 * Provide options to specify upsert, multi parameters same as the mongo update command and works the same way as mongo update.<br>
	 * This method operates on collection name provided in the parameter(param, <b>collectionName</b>).<br>
	 * This method calls <i>collection.update(findQuery, dbObject,upsert,multi)</i><br>
	 * 
	 * @param db
	 * @param collectionName
	 * @param findQuery
	 * @param dbObject
	 * @param upsert
	 * @param multi
	 * @return com.mongodb.WriteResult
	 * @throws IOException 
	 *  
	 * 
	 * @throws IOException
	 */

	public WriteResult writeMeta(DB db, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi) throws MongoUtilsException {
		DBCollection collection = db.getCollection(collectionName);
		return collection.update(findQuery, dbObject,upsert,multi);
	}
	
	/**
	 * This method used to insert data to the collection <b>collectionName</b> of the com.mongodb.DB param <b>db</b><br> 
	 * @param db
	 * @param collectionName
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult insertMeta(DB db, String collectionName, DBObject dbObject) throws MongoUtilsException {
		DBCollection collection = db.getCollection(collectionName);
		return collection.insert(dbObject);
	}
	
	/**
	 * This method authenticates the mongodb<b>dbName</b> and calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#insertMeta(DB, java.lang.String, DBObject)">
	 * insertMeta(DB db, String collectionName, DBObject dbObject)</A>
	 * @param dbName
	 * @param collectionName
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult insertMeta(String dbName, String collectionName, DBObject dbObject) throws MongoUtilsException {
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return insertMeta(db, collectionName, dbObject);
	}	
		
	

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * There is no parameters to specify upsert, multi parameters.
	 * By default <i>false</i> is passed for both upsert, multi.<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, java.lang.String, DBObject, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, String collectionName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param findQuery
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, String collectionName, DBObject findQuery , DBObject dbObject) throws MongoUtilsException{
		return writeMeta(dbName, collectionName, findQuery, dbObject, false, false);
	}

	/**
	 * This method used to write data to mongo database(param,<b>dbName</b>).<br>
	 * There is no parameters to specify upsert, multi parameters.
	 * By default <i>false</i> is passed for both upsert, multi.<br>
	 * Since the collection name is not part of parameter list, this method operates on collection name <b>metadata</b>.<br>
	 * 
	 *  This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeMeta(java.lang.String, DBObject, DBObject, boolean, boolean)">
	 * writeMeta(String dbName, DBObject findQuery , DBObject dbObject, boolean upsert, boolean multi)</A>
	 * 
	 * @param dbName
	 * @param findQuery
	 * @param dbObject
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws IOException
	 */
	public WriteResult writeMeta(String dbName, DBObject findQuery , DBObject dbObject) throws MongoUtilsException{
		return writeMeta(dbName, findQuery, dbObject, false, false);
	}

	/**
	 * This Method authenticates the mongodb <b>dbName</b> and calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeContent(DB, java.lang.String, java.lang.String, java.lang.String, java.lang.String, DBObject, boolean)">
	 * writeContent(DB db, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean)</A>
	 * 
	 * @param dbName
	 * @param bucketName
	 * @param id - document id to which the binary is associated.
	 * @param gridFSFileName - File name to be set in GridFS.
	 * @param filePath - Physical file location in the desk, to be saved to GridFS.
	 * @param deleteFile - flag to delete the file from disk once it is saved to GridFS.
	 * @param document - used to set metadata of the file being saved.
	 * 
	 * @throws MongoUtilsException
	 */

	public void writeContent(String dbName, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean deleteFile) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		writeContent(db, bucketName, id, gridFSFileName, filePath, document, deleteFile);
	}
	
	/**
	 * This method used to write binary files to mongo GridFS bucket of the mongo database(param,<b>dbName</b>).<br>
	 * This method connects to <b>bucketName</b> of <b>dbName</b> and saves the binary file.<br>
	 * This method takes DBObject parameter to set metadata for the file being saved to GridFS.<br>
	 * This method will save the file along with the following mandatory metadata(in addition to the user supplied DBObject)-<br>
	 * "docid" set to  <i>id</i><br>
	   "fileName" set to  <i>gridFSFileName</i><br>
	   "fileType" set to <i>extension of gridFSFileName</i><br>
	   "lastModified" set to <i>System.currentTimeMillis()</i><br>

	   This method checks the existance of the <i>gridFSFileName</i> in the provided <i>id</i>, if the file exists, then it will remove the old entry and writes the new one to GridFS.
	   If the file does not exists with the provided id, then it writes the entry against the id.<br> 
	 * 
	 * @param db
	 * @param bucketName
	 * @param id - document id to which the binary is associated.
	 * @param gridFSFileName - File name to be set in GridFS.
	 * @param filePath - Physical file location in the desk, to be saved to GridFS.
	 * @param document - used to set metadata of the file being saved.
	 * @param deleteFile
	 * 
	 * @throws MongoUtilsException
	 */

	public void writeContent(DB db, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean deleteFile) throws MongoUtilsException{
		GridFS gfs = new GridFS(db, bucketName);
		File binFile = new File(filePath);
		//Check whether the (id, gridFSFileName) already exists in the collection
		List<GridFSDBFile> fileList=gfs.find(new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id).append(ConfigurationManager.METADATA_FILENAME_FIELD,gridFSFileName));
		if(fileList.size()==0){
			saveFile(id, gridFSFileName, binFile, gfs,document);
		}else if (fileList.size()>=1){
			GridFSDBFile gridFSDBFile = (GridFSDBFile) fileList.iterator().next();
			gfs.remove(new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id).append(ConfigurationManager.METADATA_FILENAME_FIELD,gridFSFileName));
			saveFile(id, gridFSFileName, binFile, gfs,document);
		}/*else if (fileList.size()>1){
			throw new MongoUtilsException("Exception Occurred while writing the files to GRIDFS." +
					"In any content collection, there should be unique (docid,fileName)." +
					" please check the database for document(id) "+id+" , file name "+gridFSFileName);
		}*/
		if(deleteFile){
			File file = new File(filePath);
			file.delete();
		}
	}
	
	
	public void writeFASTXMLContent(String dbName, String id,byte[] fastXMLBytes) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		GridFS gfs = new GridFS(db, ConfigurationManager.GRIDFS_FAST_BUCKET);
		
		GridFSInputFile gfsFile = gfs.createFile(fastXMLBytes);
		
		gfsFile.setFilename(id);
		gfsFile.setContentType(dbName);
		
		List<GridFSDBFile> fileList=gfs.find(new BasicDBObject().append(ConfigurationManager.GRIDFS_FAST_XML_FILE_NAME,id).append(ConfigurationManager.GRIDFS_FAST_XML_CONTENT_TYPE,dbName));
		if(fileList.size()==0){
			gfsFile.save();
		}else if (fileList.size()>0){
			gfs.remove(new BasicDBObject().append(ConfigurationManager.GRIDFS_FAST_XML_FILE_NAME,id).append(ConfigurationManager.GRIDFS_FAST_XML_CONTENT_TYPE,dbName));
			gfsFile.save();	
		}
	}
	
	
	public List<GridFSDBFile> readFASTXMLFile(String dbName,String id) throws MongoUtilsException{
		
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		GridFS gfs = new GridFS(db, ConfigurationManager.GRIDFS_FAST_BUCKET);
		
		List<GridFSDBFile> fileList=gfs.find(new BasicDBObject().append(ConfigurationManager.GRIDFS_FAST_XML_FILE_NAME,id).append(ConfigurationManager.GRIDFS_FAST_XML_CONTENT_TYPE,dbName));
		return fileList;
		
	}

	/**
	 * There is no parameter to specify the GridFS bucket, so by default will save the binary files in <i>content</i> bucket of the <b>dbName</b>.<br>
	 * 
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, DBObject, boolean)">
	 * writeContent(String dbName, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean)</A>
	 * @param dbName
	 * @param id
	 * @param gridFSFileName
	 * @param filePath
	 * @param document
	 * @param deleteFile
	 * 
	 * @throws MongoUtilsException
	 */
	public void writeContent(String dbName, String id, String gridFSFileName, String filePath, DBObject document, boolean deleteFile) throws MongoUtilsException{
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		writeContent(dbName, contentBucketName, id, gridFSFileName, filePath, document, deleteFile);
	}

	/**
	 * There is no parameter to specify the DBObject, hence a new BasicDBObject is created and passed to the method to be called.<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, DBObject, boolean)">
	 * writeContent(String dbName, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean)</A>
	 * 
	 * @param dbName
	 * @param bucketName
	 * @param id
	 * @param gridFSFileName
	 * @param filePath
	 * @param deleteFile
	 * 
	 * @throws MongoUtilsException
	 */
	public void writeContent(String dbName, String bucketName, String id, String gridFSFileName, String filePath, boolean deleteFile) throws MongoUtilsException{
		writeContent(dbName, bucketName, id, gridFSFileName, filePath, new BasicDBObject(), deleteFile);
	}

	/**
	 * There is no parameter to specify the GridFS bucket, so by default will save the binary files in <i>content</i> bucket of the <b>dbName</b>.
	 * There is no parameter to specify the DBObject, hence a new BasicDBObject is created and passed to the method to be called.<br>
	 * This method calls 
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#writeContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, DBObject, boolean)">
	 * writeContent(String dbName, String bucketName, String id, String gridFSFileName, String filePath, DBObject document, boolean)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @param gridFSFileName
	 * @param filePath
	 * @param deleteFile
	 * 
	 * @throws MongoUtilsException
	 */
	public void writeContent(String dbName, String id, String gridFSFileName, String filePath, boolean deleteFile) throws MongoUtilsException{
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		writeContent(dbName, contentBucketName, id, gridFSFileName, filePath, new BasicDBObject(), deleteFile);
	}

	private void saveFile(String key, String gridFSFileName, File binFile, GridFS gfs, DBObject document )throws MongoUtilsException {
		GridFSInputFile gfsFile;
		try {
			gfsFile = gfs.createFile(binFile);
		} catch (IOException e) {
			throw new MongoUtilsException("Exception while trying to save File "+binFile.getName()+" to db",e);
		}
		gfsFile.setFilename(gridFSFileName);
		String [] fileType=gridFSFileName.split("\\.");
		
		document.put("docid", key);
		document.put("fileName", gridFSFileName);
		document.put("fileType", fileType[1]);
		document.put("lastModified", System.currentTimeMillis());
		gfsFile.setMetaData(document);
		gfsFile.save();	
	}


	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(java.lang.String, java.lang.String, DBObject)">
	 * readMeta(String dbName, String collectionName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName, String collectionName) throws MongoUtilsException{
		return readMeta(dbName, collectionName, new BasicDBObject());
	}

	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(java.lang.String, java.lang.String, DBObject)">
	 * readMeta(String dbName, String collectionName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param id
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName, String collectionName, String id) throws MongoUtilsException{
		return readMeta(dbName, collectionName, new BasicDBObject(ConfigurationManager.ID_FIELD,id));
	}

	/**
	 * There is no parameter to specify the collectionName, hence the call is made with collection name <i>metadata</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(java.lang.String, java.lang.String)">
	 * readMeta(String dbName, String collectionName)</A>
	 * 
	 * @param dbName
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName) throws MongoUtilsException{
		// if collection name is not specified, by default it reads from Live Collection
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_LIVE_COLLECTION);
		return readMeta(dbName, collectionName);
	}

	/**
	 * This method authenticates for the mongodb name <b>dbName</b> supplied and calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(DB, java.lang.String, DBObject)">
	 * readMeta(DB db, String collectionName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param dbo
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName, String collectionName, DBObject dbo) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return readMeta(db, collectionName, dbo, new BasicDBObject());
	}
	
	/**
	 * This method authenticates for the mongodb name <b>dbName</b> supplied and calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(DB, java.lang.String, DBObject)">
	 * readMeta(DB db, String collectionName, DBObject dbo, DBObject fields)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param dbo
	 * @param fields - display fields
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName, String collectionName, DBObject dbo, DBObject keys) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return readMeta(db, collectionName, dbo, keys);
	}/**
	 * This method finds all collections that matches with DBObject being passed and returns the DBCursor.<br>
	 * 
	 * @param db - DB Instance to which we need to connect
	 * @param collectionName - Name of the collection to be accessed
	 * @param filters - Query filter in form of a DBObject
	 * @param fielts - fields to be returned by the query
	 * @return com.mongodb.DBCursor 
	 * 
	 * @throws MongoUtilsException
	 */

	public DBCursor readMeta(DB db, String collectionName, DBObject dbo, DBObject keys) throws MongoUtilsException{
		DBCollection collection = db.getCollection(collectionName);
		DBCursor cursor = collection.find(dbo, keys);
		return cursor;
	}

	/**
	 * There is no parameter to specify the collectionName, hence the call is made with collection name <i>metadata</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readMeta(java.lang.String, java.lang.String, DBObject)">
	 * readMeta(String dbName, String collectionName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param dbo
	 * @return com.mongodb.DBCursor
	 * 
	 * @throws MongoUtilsException
	 */
	public DBCursor readMeta(String dbName, DBObject dbo) throws MongoUtilsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_LIVE_COLLECTION);
		return readMeta(dbName, collectionName, dbo);
	}


	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">
	 * readContent(String dbName, String bucketName, String id, String gridFSFileName)</A><br>
	 * Since <i>gridFSFileName</i> is not known the call sends null for this. i.e readContent(dbName, bucketName, id, null)<br>
	 * 
	 * @param dbName
	 * @param id
	 * @param bucketName
	 * @return List<GridFSDBFile> 
	 * 
	 * @throws MongoUtilsException
	 */
	public List<GridFSDBFile> readContent(String dbName, String id, String bucketName) throws MongoUtilsException{
		return readContent(dbName, bucketName, id, null);
	}

	/**
	 * If <i>gridFSFileName</i> is null, then the list returned will contain all the binaries associated with <i>id</i>.<br>
	 * Otherwise list will contain hopefully one file associated with <i>id</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readContent(java.lang.String, java.lang.String, DBObject)">
	 * readContent(String dbName, String bucketName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param bucketName
	 * @param id
	 * @param gridFSFileName
	 * @return List<GridFSDBFile> 
	 * 
	 * @throws MongoUtilsException
	 */
	public List<GridFSDBFile> readContent(String dbName, String bucketName, String id, String gridFSFileName) throws MongoUtilsException{
		if(gridFSFileName!=null){
			return readContent(dbName, bucketName, new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id).append(ConfigurationManager.METADATA_FILENAME_FIELD,gridFSFileName));
		}else{
			return readContent(dbName, bucketName, new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id));
		}
	}

	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readContent(java.lang.String, DBObject)">
	 * readContent(String dbName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param id
	 * @return List<GridFSDBFile> 
	 * 
	 * @throws MongoUtilsException
	 */
	public List<GridFSDBFile> readContent(String dbName, String id) throws MongoUtilsException{
		return readContent(dbName, new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id));
	}

	/**
	 * This method will authenticate against the mongodb name <b>dbName</b> and calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readContent(DB, java.lang.String, DBObject)">
	 * readContent(DB db, String bucketName, DBObject dbo)</A>
	 * @param dbName
	 * @param bucketName
	 * @param dbo
	 * @return List<GridFSDBFile>
	 * 
	 * @throws MongoUtilsException
	 */
	public List<GridFSDBFile> readContent(String dbName, String bucketName, DBObject dbo) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return readContent(db, bucketName, dbo);
	}
	
	/**
	 * This method will return the list of files matching with DBObject.<br>
	 * @param dbName
	 * @param bucketName
	 * @param dbo
	 * @return List<GridFSDBFile>
	 * 
	 * @throws MongoUtilsException
	 */

	public List<GridFSDBFile> readContent(DB db, String bucketName, DBObject dbo) throws MongoUtilsException{
		GridFS gfs = new GridFS(db, bucketName);
		List<GridFSDBFile> fileList=gfs.find(dbo);
		return fileList;
	}

	/**
	 * There is no parameter to specify the bucketName, hence the call is made with bucketName name <i>content</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#readContent(java.lang.String, java.lang.String, DBObject)">
	 * readContent(String dbName, String bucketName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param dbo
	 * @return
	 * 
	 * @throws MongoUtilsException
	 */
	public List<GridFSDBFile> readContent(String dbName, DBObject dbo) throws MongoUtilsException{
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		return readContent(dbName, contentBucketName, dbo);
	}

	/**
	 * This method deletes metadata, content of the <i>id</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteMeta(java.lang.String, java.lang.String)">
	 *  deleteMeta(String dbName,String id)</A> to delete metadata of the id.<br>
	 *  
	 *  This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(java.lang.String, java.lang.String)">
	 * deleteContent(String dbName,String id)</A> to delete content of the id.<br>	 
	 * 
	 * @param dbName
	 * @param id
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteWholeDocument(String dbName,String id) throws MongoUtilsException{
		deleteMeta(dbName, id);
		deleteContent(dbName, id);
	}

	/**
	 * There is no parameter to specify the collectionName, hence the call is made with collection name <i>metadata</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteMeta(java.lang.String, java.lang.String, java.lang.String)">
	 *  deleteMeta(String dbName, String collectionName, String id)</A> to delete metadata of the id.<br>
	 *  
	 * @param dbName
	 * @param id
	 * @return  com.mongodb.WriteResult
	 * 
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(String dbName,String id) throws MongoUtilsException{
		// we dont want to remove a collection from live database. So by default we try to remove only from the metadata staging collection
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_STAGING_COLLECTION);
		return deleteMeta(dbName, collectionName, id);
	}

	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteMeta(java.lang.String, java.lang.String, DBObject)">
	 *  deleteMeta(String dbName, String collectionName, DBObject dbo)</A> to delete metadata of the id.<br>
	 *  
	 * @param dbName
	 * @param collectionName
	 * @param id
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(String dbName, String collectionName, String id) throws MongoUtilsException{
		return deleteMeta(dbName, collectionName, new BasicDBObject(ConfigurationManager.ID_FIELD,id));
	}

	/**
	 * There is no parameter to specify the collectionName, hence the call is made with collection name <i>metadata</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteMeta(java.lang.String, java.lang.String, DBObject)">
	 *  deleteMeta(String dbName, String collectionName, DBObject dbo)</A>
	 *  
	 * @param dbName
	 * @param dbo
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(String dbName,DBObject dbo) throws MongoUtilsException{
		// we dont want to remove a collection from live database. So by default we try to remove only from the metadata staging collection
		String collectionName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_STAGING_COLLECTION);
		return deleteMeta(dbName, collectionName, dbo);
	}

	/**
	 * This method authenticates the mongodb <b>dbName</b> and calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteMeta(DB, java.lang.String, DBObject)">
	 *  deleteMeta(DB db, String collectionName, DBObject dbo)</A>
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param dbo
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(String dbName, String collectionName, DBObject dbo) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return deleteMeta(db, collectionName, dbo);
	}
	
	/**
	 * This method will remove all the documents in <i><collectionName></i> of mongo db com.mongodb.DB  <i>db</i>that matches with DBObject.<br>
	 * @param db
	 * @param collectionName
	 * @param dbo
	 * @return com.mongodb.WriteResult
	 * 
	 * @throws MongoUtilsException
	 */

	public WriteResult deleteMeta(DB db, String collectionName, DBObject dbo) throws MongoUtilsException{
		DBCollection collection = db.getCollection(collectionName);
		DBCursor cursor = collection.find(dbo,new BasicDBObject("_id",1));
		WriteResult result=null;
		while(cursor.hasNext()){
			
			DBObject query = cursor.next();
			result = collection.remove(query);
		}
		return result;
	}
	
	/**
	 * @param dbName
	 * @param collectionName
	 * @param dbo
	 * @param offset
	 * @return
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(DB db, String collectionName, DBObject dbo, DBObject sortFields, int offset) throws MongoUtilsException{
		DBCollection collection = db.getCollection(collectionName);
		DBCursor cursor = collection.find(dbo);
		WriteResult result=null;
		cursor.sort(sortFields);
		cursor.skip(offset);
		while(cursor.hasNext()){
			result = collection.remove(cursor.next());
		}
		return result;
	}
	
	/**
	 * @param dbName
	 * @param collectionName
	 * @param dbo
	 * @param offset
	 * @return
	 * @throws MongoUtilsException
	 */
	public WriteResult deleteMeta(String dbName, String collectionName, DBObject dbo, DBObject sortFields, int offset) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		return this.deleteMeta(db, collectionName, dbo, sortFields, offset);
	}

	/**
	 * If <i>gridFSFileName</i> is null, then all the content associated with <i>id</i> will be removed.<br>
	 * Otherwise it will remove all the content associated with <i>id</i> and <i>gridFSFileName</i>.<br>
	 * 
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(java.lang.String, java.lang.String, DBObject)">
	 * deleteContent(String dbName, String bucketName, DBObject dbo)</A><br>	 
	 * 
	 * @param dbName
	 * @param id
	 * @param bucketName
	 * @param gridFSFileName
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteContent(String dbName, String id, String bucketName, String gridFSFileName) throws MongoUtilsException{

		if(gridFSFileName!=null){
			deleteContent(dbName, bucketName, new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id).append(ConfigurationManager.METADATA_FILENAME_FIELD,gridFSFileName));
		}else{
			deleteContent(dbName, bucketName, new BasicDBObject().append(ConfigurationManager.METADATA_DOCID_FIELD, id));
		}
	}

	/**
	 * There is no parameter to specify the bucketName, hence the call is made with bucketName name <i>content</i>.<br>
	 * 
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(java.lang.String, java.lang.String, java.lang.String)">
	 * deleteContent(String dbName, String id, String bucketName)</A><br>	
	 * 
	 * @param dbName
	 * @param id
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteContent(String dbName,String id) throws MongoUtilsException{
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		deleteContent(dbName, id, contentBucketName);
	}

	/**
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">
	 * deleteContent(String dbName, String id, String bucketName, String gridFSFileName)</A><br>
	 * Since <i>gridFSFileName</i> is not known the call sends null for this. i.e deleteContent(dbName, id, bucketName, null)<br>
	 * 
	 * @param dbName
	 * @param id
	 * @param bucketName
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteContent(String dbName, String id, String bucketName) throws MongoUtilsException{
		deleteContent(dbName, id, bucketName, null);
	}

	/**
	 * There is no parameter to specify the bucketName, hence the call is made with bucketName <i>content</i>.<br>
	 * This method calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(java.lang.String, java.lang.String, DBObject)">
	 * deleteContent(String dbName, String bucketName, DBObject dbo)</A><br>
	 * 
	 * @param dbName
	 * @param dbo
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteContent(String dbName, DBObject dbo) throws MongoUtilsException{
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		deleteContent(dbName, contentBucketName, dbo);
	}

	/**
	 * This method will authenticate mongodb <b>dbName</b> and calls
	 * <A HREF="../../../../../com/hp/cdsplus/mongo/utils/MongoAPIUtils.html#deleteContent(DB, java.lang.String, DBObject)">
	 * deleteContent(DB db, String bucketName, DBObject dbo)</A><br>
	 * @param dbName
	 * @param bucketName
	 * @param dbo
	 * 
	 * @throws MongoUtilsException
	 */
	public void deleteContent(String dbName, String bucketName, DBObject dbo) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		deleteContent(db, bucketName, dbo);
	}
	
	/**
	 * Deletes all the content that matches with DBObject.
	 * @param db
	 * @param bucketName
	 * @param dbo
	 * 
	 * @throws MongoUtilsException
	 */

	public void deleteContent(DB db, String bucketName, DBObject dbo) throws MongoUtilsException{
		GridFS gfs = new GridFS(db, bucketName);
		List<GridFSDBFile> gridFSDBFileList=readContent(db, bucketName, dbo);
		if(gridFSDBFileList!=null && gridFSDBFileList.size()>0){
			for (Iterator<GridFSDBFile> iterator = gridFSDBFileList.iterator(); iterator.hasNext();) {
				gfs.remove((GridFSDBFile) iterator.next());
			}
		}
	}
	
	public long countMeta(String dbName, String collectionName, DBObject query) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		DBCollection collection = db.getCollection(collectionName);
		return collection.count(query);
	}
	
	public int countAttachments(String dbName, String contentBucket, DBObject query) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		GridFS gridFS = new GridFS(db, contentBucket);
		return gridFS.find(query).size();
	}
	
	public boolean hasAttachments(String dbName, String contentBucket, DBObject query) throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		GridFS gridFS = new GridFS(db, contentBucket);
		if(gridFS.find(query).size() > 0)
			return true;
		else 
			return false;
	}

	public WriteResult insertMeta(String dbName, String collectionName,
			ArrayList<DBObject> records) throws MongoUtilsException {
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		DBCollection collection = db.getCollection(collectionName);
		return collection.insert(records);
		
	}
}
