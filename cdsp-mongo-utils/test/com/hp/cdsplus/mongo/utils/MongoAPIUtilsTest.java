package com.hp.cdsplus.mongo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

//import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.mongo.config.ConfigMapper;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;

/*public class MongoAPIUtilsTest extends TestCase {

	MongoAPIUtils mongoUtils;
	ConversionUtils conversion;
	ConfigurationManager configMgr;

	public MongoAPIUtilsTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		configMgr= ConfigurationManager.getInstance();
		conversion= new ConversionUtils();
		mongoUtils= new MongoAPIUtils();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWriteMeta() {
		System.out.println("entering testWriteMeta");
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/ms.xml");
		try{
			InputStream is= new FileInputStream(xmlFile);
			WriteResult result=writeMeta("marketingstandard", "4AA1-2759ENW", "text/xml", is, "update", 2, true);
			if(result.getError()==null)
				assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testWriteMeta");
	}

	public WriteResult writeMeta(String dbName, String id, String mimeType, InputStream is, String updateType, Integer priority, boolean convertToJSON) throws FileNotFoundException, IOException, ClassNotFoundException, JAXBException{
		if(convertToJSON){
			HashMap<String, ConfigMapper> configMapper=configMgr.getConfigMapper();
			String fullClassName= configMapper.get(dbName).getPackageName()+"."+configMapper.get(dbName).getClassName();
			String jsonString= conversion.convertXMLtoJSON(IOUtils.toByteArray(is), fullClassName);
			return writeMeta(dbName,id,mimeType,jsonString,updateType,priority);
		}else{
			String jsonString= IOUtils.toString(is);
			return writeMeta(dbName,id,mimeType,jsonString,updateType,priority);
		}

	}

	public WriteResult writeMeta(String dbName, String id, String mimeType, String jsonString, String updateType, Integer priority) throws FileNotFoundException, IOException {
		BasicDBObject document=(BasicDBObject)JSON.parse(jsonString);
		document.put("_id", id);
		document.put("mime", mimeType);
		document.put("eventType", updateType);
		document.put("priority", priority);
		document.put("lastModified", System.currentTimeMillis());
		return mongoUtils.writeMeta(dbName, new BasicDBObject("_id", id), document, true, false);
	}

	public void testReadMeta() throws IOException, ClassNotFoundException, JAXBException{
		System.out.println("entering testReadMeta");
		DBCursor cur=mongoUtils.readMeta("marketingstandard");
		if(cur.count()!=0){
			assertTrue(true);
		}else assertTrue(false);
		System.out.println("exiting testReadMeta");
	}

	public void testWriteContent(){
		System.out.println("entering testWriteContent");
		try {
			writeContent("support", "c123", "col33544-json.txt", "", "C:/Suresh/workspace/mongo/Research/resources/col33544-json.txt", "update", 2, false);
			assertTrue(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testWriteContent");
	}

	public void writeContent(String dbName, String id, String gridFSFileName, String mimeType,  String filePath, String updateType, Integer priority, boolean deleteFile) throws FileNotFoundException, IOException{
		BasicDBObject document= new BasicDBObject();
		document.put("mime",mimeType);
		document.put("eventType", updateType);
		document.put("priority", priority);
		//changing w.r.t mongo api V2.
		mongoUtils.writeContent(dbName, id, gridFSFileName, filePath, document,deleteFile);
		//mongoapi.writeContent(ConfigurationManager.getInstance().getMongoConnection().getDB(dbName), "content", id, gridFSFileName, filePath, document, deleteFile);
	}

	public void testReadContent(){
		System.out.println("entering testReadContent");
		try {
			List<GridFSDBFile> contentFileList= mongoUtils.readContent("support", "content", "c123", "col33544-json.txt");
			if(contentFileList.size()==1){
				assertTrue(true);
			}else assertTrue(false);
			if(contentFileList.toString().contains("col33544-json.txt")){
				assertTrue(true);
			}else assertTrue(false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testReadContent");
	}

	public void testInsertMeta(){
		System.out.println("entering testInsertMeta");
		try {
			WriteResult result = mongoUtils.insertMeta("library", "test", new BasicDBObject());
			if(result.getError()==null)
				assertTrue(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testInsertMeta");
	}

	public void testDeleteMeta(){
		System.out.println("entering testDeleteMeta");
		try {
			WriteResult result = mongoUtils.deleteMeta("marketingstandard","4AA1-2759ENW");
			System.out.println("result.getError()->"+result.getError());
			if(result.getError()==null)
				assertTrue(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testDeleteMeta");
	}
	
	public void testDeleteMetaNegative(){
		System.out.println("entering testDeleteMetaNegative");
		try {
			WriteResult result = mongoUtils.deleteMeta("marketingstandard","4AA1-2759ENW");
			if(result==null)
				assertTrue(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testDeleteMetaNegative");
	}

	public void testDeleteContent(){
		System.out.println("entering testDeleteContent");
		try {
			mongoUtils.deleteContent("support","c123");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testDeleteContent");
	}
	
	public void testReadContentNegative(){
		System.out.println("entering testReadContentNegative");
		try {
			List<GridFSDBFile> contentFileList= mongoUtils.readContent("support",  "c123");
			if(contentFileList.size()==1){
				assertTrue(false);
			}else assertTrue(true);
			if(contentFileList.toString().contains("col33544-json.txt")){
				assertTrue(false);
			}else assertTrue(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("exiting testReadContentNegative");
	}

}
*/