package com.hp.cdspDestination;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;

import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.hp.cdsplus.mongo.utils.SoarMongoAPIUtils;
import com.hp.loader.utils.Log;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoDestination {

	MongoAPIUtils mongoapi;
	private static HashSet<String> contentSet= new HashSet<String>();
	static{
		contentSet.add("soar_ref_data");
		contentSet.add("supportcontent");
		contentSet.add("librarycontent");
		contentSet.add("contentfeedbackcontent");
		contentSet.add("generalpurposecontent");
		contentSet.add("marketinghhocontent");
		contentSet.add("marketingstandardcontent");
	}

	public MongoDestination(){

		mongoapi= new MongoAPIUtils();
	}
	
	public MongoDestination(String contentType){
		if(contentType.equalsIgnoreCase("SOAR")) {
			mongoapi= new SoarMongoAPIUtils();
		}
		else{
			mongoapi= new MongoAPIUtils();
		}
	}

	public String processUpdate(String path, String mimeType, String fileName, String updateType, Integer priority, boolean deleteFile, String company) throws MongoUtilsException{
		Log.info("processUpdate path:"+path+" fileName:"+fileName+" deleteFile:"+deleteFile+" priority:"+priority+" mimeType"+mimeType+" updateType:"+updateType);
		String dbName= getDBName(path);
		String id= getID(path);
		String gridFSFileName= getGFSFileName(path);
		try {
			writeContent(dbName, id, gridFSFileName, mimeType, fileName, updateType, priority, deleteFile, company);
			return "success";
		} catch (Exception e) {
			throw new MongoUtilsException("Failed to update "+path, e);
		}
	}

	public String processUpdate(String path, String mimeType, byte[] payload, String updateType, Integer priority, boolean convertToJSON, String company) throws MongoUtilsException{
		String dbName= getDBName(path);
		String id= getID(path);
		Log.info("processUpdate path:"+path+" convertToJSON:"+convertToJSON+" id:"+id+" dbName:"+dbName+" priority:"+priority+" mimeType"+mimeType+" updateType:"+updateType);
		try {
			if(contentSet.contains(dbName)){
				String gridFSFileName = id;
				if(!id.contains(".xml")){
					gridFSFileName = id+".xml";
				}
				BufferedOutputStream bos = null;
				String filepath=new File("temp").getAbsolutePath()+File.separator+gridFSFileName;
				FileOutputStream fos = new FileOutputStream(filepath);
				bos = new BufferedOutputStream(fos); 
				bos.write(payload);
				bos.flush();
				bos.close(); 
				writeContent(dbName, id, gridFSFileName, mimeType, filepath, updateType, priority, true, company);
				return "success";
			}else{
				return writeMeta(dbName, id, mimeType, payload, updateType, priority, convertToJSON).toString();
			}
		} 
		catch (Exception e) {
			throw new MongoUtilsException("Failed to update "+path, e);
		}
	}

	public String processDelete(String path, String updateType, Integer priority) throws MongoUtilsException{
		String dbName= getDBName(path);
		String id= getID(path);
		//added bucket name for attachment issue
		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
		Log.info("processDelete path:"+path+" updateType:"+updateType+" priority:"+priority +" id:"+id+" dbName:"+dbName);
		try{
			//calling the 4 parameter overloaded method instead of 2 parameter method attachment issue
			//mongoapi.deleteContent(dbName,id,contentBucketName,path);
			String[] pathArray=path.split("/");
			if(pathArray.length>3){
				String gridFSFileName = pathArray[3].toString();
				if(gridFSFileName!=null && !"".equals(gridFSFileName)){
					mongoapi.deleteContent(dbName, id, null, gridFSFileName);	
				}else{
					mongoapi.deleteContent(dbName,id);
				}
			}else{
				mongoapi.deleteContent(dbName,id);	
			}
			if(!contentSet.contains(dbName)){
				return writeDelete(dbName, id, updateType, priority).toString();
			}
			return "success";
		}
		catch (Exception e) {
			throw new MongoUtilsException("Failed to delete "+path, e);
		}
	}

	public String processTouch(String path, String mimeType, byte[] payload, String updateType, Integer priority) throws MongoUtilsException {
		String dbName= getDBName(path);
		String id= getID(path);
		Log.info("processTouch path:"+path+" updateType:"+updateType+"mimeType:"+mimeType+" priority:"+priority +" id:"+id+" dbName:"+dbName);
		try{
			return writeTouch(dbName, id, mimeType, updateType, priority).toString();
		}
		catch (Exception e) {
			Log.error("Failed to touch "+path+"---Exception---" + e);
			return "";
		}
	}

	private String getID(String path) {

		String[] pathArray=path.split("/");
		String dbName=null;
		if(pathArray.length>=3){
			dbName = pathArray[2].toString();
		}

		return dbName;
	}

	private String getDBName(String path) {
		String[] pathArray=path.split("/");
		String id=null;
		if(pathArray.length>=3){
			id = pathArray[0].toString();
		}
		return id;
	}

	private String getGFSFileName(String path) {
		String[] pathArray=path.split("/");
		String gridFSFileName=null;
		if(pathArray.length>=4){
			gridFSFileName = pathArray[3].toString();
		}
		return gridFSFileName;
	}

	public WriteResult writeMeta(String dbName, String id, String mimeType, byte[] payload, String updateType, Integer priority, boolean convertToJSON) throws MongoUtilsException, ConversionUtilsException, IOException {
		if(convertToJSON){
			String fullClassName= ConfigurationManager.getInstance().getMappingValue(dbName, "javaPackageName")+ "."+ ConfigurationManager.getInstance().getMappingValue(dbName, "javaClassName");
			ConversionUtils conversion=new ConversionUtils();
			String jsonString= conversion.convertXMLtoJSON(payload, fullClassName);
			String jsonStringReplaed= jsonString.replaceAll("xsi.nil", "xsi?nil");
			return writeMeta(dbName,id,mimeType,jsonStringReplaed,updateType,priority);
		}else{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(payload);
			String jsonString= IOUtils.toString(inputStream);
			return writeMeta(dbName,id,mimeType,jsonString,updateType,priority);
		}
	}

	public WriteResult writeDelete(String dbName, String id, String updateType, Integer priority) throws MongoUtilsException {
		BasicDBObject document=new BasicDBObject();
		document.put("_id", id);
		document.put("eventType", updateType);
		document.put("priority", priority);
		document.put("isDelete", "true");
		document.put("lastModified", System.currentTimeMillis());
		return mongoapi.writeMeta(dbName, new BasicDBObject("_id", id), document, true, false);
	}

	public WriteResult writeTouch(String dbName, String id, String mimeType, String updateType, Integer priority) throws MongoUtilsException {
		DBCursor cursor= mongoapi.readMeta(dbName,ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.METADATA_STAGING_COLLECTION), id);
		if(cursor.hasNext()){
			BasicDBObject tempDocument= (BasicDBObject)cursor.next();
			BasicDBObject document = updateDocument(updateType,priority, tempDocument);
			document.put("lastModified", System.currentTimeMillis());
			document.put("eventType", updateType);
			document.put("priority", priority);
			
			/*if((document.get("docid")+".xml").equals(document.get("filename"))){
				document.put("isAttachment", false);
			}else{
				document.put("isAttachment", true);
			}*/
			
			return mongoapi.writeMeta(dbName, new BasicDBObject("_id", id), document, true, false);
		}else{
			throw new MongoUtilsException("Document "+id+" does not exist in "+dbName);
		}
	}

	public WriteResult writeMeta(String dbName, String id, String mimeType, String jsonString, String updateType, Integer priority) throws MongoUtilsException {
		BasicDBObject tempDocument=(BasicDBObject)JSON.parse(jsonString);
		BasicDBObject document = updateDocument(updateType,priority, tempDocument);
		document.put("_id", id);
		document.put("mime", mimeType);
		document.put("eventType", updateType);
		document.put("priority", priority);
		document.put("lastModified", System.currentTimeMillis());
		
		/*if((document.get("docid")+".xml").equals(document.get("filename"))){
			document.put("isAttachment", false);
		}else{
			document.put("isAttachment", true);
		}*/
		
		return mongoapi.writeMeta(dbName, new BasicDBObject("_id", id), document, true, false);

	}
	
	private BasicDBObject updateDocument(String updateType,Integer priority	,BasicDBObject tempDocument) {
		BasicDBObject document=new BasicDBObject();
		BasicDBObject subDocument= (BasicDBObject)tempDocument.get("document");
		if(subDocument!=null){
			subDocument.append("action",updateType);
			subDocument.append("priority",priority);
			document= new BasicDBObject("document",subDocument);
		}else{
			document=tempDocument;
		}
		return document;
	}


	public void writeContent(String dbName, String id, String gridFSFileName, String mimeType,  String filePath, String updateType, Integer priority, boolean deleteFile,String company) throws MongoUtilsException {
		BasicDBObject document= new BasicDBObject();
		document.put("mime",mimeType);
		document.put("eventType", updateType);
		document.put("priority", priority);
		//SMO: Adding company flag
		document.put("company_info",company);
		if((id+".xml").equals(gridFSFileName)){
			document.put("isAttachment", false);
		}else{
			document.put("isAttachment", true);
		}
		mongoapi.writeContent(dbName, id, gridFSFileName, filePath, document,deleteFile);
	}

}
