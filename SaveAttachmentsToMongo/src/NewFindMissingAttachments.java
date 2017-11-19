import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;

/**
 * @author srivasni
 *
 */
public class NewFindMissingAttachments {
	private static MongoAPIUtils mongoapi = new MongoAPIUtils();
	/**
	 * @param args
	 * @throws MongoUtilsException 
	 */
	
	static DefaultHttpClient httpClient = new DefaultHttpClient();	
	
	public static void main(String[] args) throws MongoUtilsException, OptionsException, JSONException {
		System.setProperty("mongo.configuration","/opt/ais/app/applications/soar_attachment_import_new/mongo.properties");
		BufferedReader br = null;
		String line = "";
		try {
			ContentDAO contentDAO = new ContentDAO();
			Options options = new Options();
			options.setContentType("soar");
			DBCursor cursor = contentDAO.getLiveDocumentList(options);
			FileWriter updateLogFile = new FileWriter("/opt/ais/app/applications/soar_attachment_import_new/search.log",true);
			FileWriter statusLogFile = new FileWriter("/opt/ais/app/applications/soar_attachment_import_new/status.log",false);
			String newLine = System.getProperty("line.separator");
			int i=0;
			for (DBObject docObject : cursor) {
				String doc_id = docObject.get("_id").toString();
				options.setDocid(doc_id);
				int doccount = getDocumentAttachmentCount(doc_id);
				if(contentDAO.getAllAttachments(options).size()!=doccount){
					//SaveAttachmentsToMongo.saveAttachmentForDocid(doc_id);
					updateLogFile.write(doc_id+newLine);	
					updateLogFile.flush();
				}
				statusLogFile.write(i++ +" "+doc_id+newLine);	
				statusLogFile.flush();
			}			
			updateLogFile.close();
			statusLogFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public static int getDocumentAttachmentCount(String doc_id) throws JSONException{
		
		//QueryBuilder builder = QueryBuilder.start();
		BasicDBObject dbObject = new BasicDBObject("_id",doc_id);
		
		BasicDBObject fields = new BasicDBObject("soar-software-feed.collection.attachments.attachment.filename",true)
			.append("soar-software-feed.collection.software-items.software-item.attachments.attachment.filename", true);
		MongoAPIUtils mongoUtils = new MongoAPIUtils();
		try {
			DBCursor cursor = mongoUtils.readMeta("soar", "metadata_live", dbObject,fields);
			if(cursor.hasNext()){
				DBObject dbo = cursor.next();
				int count = 0;
				if(dbo.get("soar-software-feed")!=null && ((BasicDBObject)dbo.get("soar-software-feed")).get("collection")!=null){
					BasicDBObject collectionObject = (BasicDBObject)((BasicDBObject)dbo.get("soar-software-feed")).get("collection");
					
					if(collectionObject.get("attachments")!=null){
						BasicDBList attachments = (BasicDBList)((BasicDBObject)collectionObject.get("attachments")).get("attachment");
						count+= attachments.size();						
					}
					
					if(collectionObject.get("software-items")!=null && ((BasicDBObject)collectionObject.get("software-items")).get("software-item")!=null){
						
						if(((BasicDBObject)collectionObject.get("software-items")).get("software-item") instanceof BasicDBList){
							BasicDBList softwareItems = (BasicDBList)((BasicDBObject)collectionObject.get("software-items")).get("software-item");
							if(softwareItems.size()>0){
								Iterator iterator = softwareItems.iterator();
								while(iterator.hasNext()){
									BasicDBObject bdo = (BasicDBObject)iterator.next();
									//System.out.println(softwareItems.toString());
									if(bdo.get("attachments")!=null){
										BasicDBList attachments = (BasicDBList)((BasicDBObject)bdo.get("attachments")).get("attachment");
										count+= attachments.size();										
									}
								}
							}			
						}else{
							BasicDBObject softwareItem = (BasicDBObject)((BasicDBObject)collectionObject.get("software-items")).get("software-item");
							if(softwareItem!=null){
								//Iterator iterator = softwareItems.iterator();
								//while(iterator.hasNext()){
									//BasicDBObject bdo = (BasicDBObject)iterator.next();
									//System.out.println(softwareItems.toString());
									if(softwareItem.get("attachments")!=null){
										if(((BasicDBObject)softwareItem.get("attachments")).get("attachment") instanceof BasicDBList){
											BasicDBList attachments = (BasicDBList)((BasicDBObject)softwareItem.get("attachments")).get("attachment");
											count+= attachments.size();
										}else{
											BasicDBObject attachment = (BasicDBObject)((BasicDBObject)softwareItem.get("attachments")).get("attachment");
											count+= 1;
										}																				
									}
								//}
							}			
						}						
									
					}
					
					return count;
				}
				
			}
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return 0;
	}
	
}
