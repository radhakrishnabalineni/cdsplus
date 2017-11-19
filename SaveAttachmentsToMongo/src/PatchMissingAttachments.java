import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author srivasni
 *
 */
public class PatchMissingAttachments {
	private static MongoAPIUtils mongoapi = new MongoAPIUtils();
	/**
	 * @param args
	 * @throws MongoUtilsException 
	 */
	
	static DefaultHttpClient httpClient = new DefaultHttpClient();	
	
	public static void main(String[] args) throws MongoUtilsException, OptionsException {
		System.setProperty("mongo.configuration","/opt/ais/app/applications/soar_attachment_import/mongo.properties");
		String line = "";
		try {
			ContentDAO contentDAO = new ContentDAO();
			Options options = new Options();
			options.setContentType("soar");
			DBCursor cursor = contentDAO.getLiveDocumentList(options);
			
			ArrayList<String> processedList = new ArrayList<String>();
			int i=0;
			BufferedReader br = new BufferedReader(new FileReader("/opt/ais/app/applications/soar_attachment_import/status.log"));			
			while ((line = br.readLine()) != null) {
				i = Integer.valueOf(line.trim().split(" ")[0])+1;
				processedList.add(line.trim().split(" ")[1]);
	 		}
			
			FileWriter updateLogFile = new FileWriter("/opt/ais/app/applications/soar_attachment_import/search.log",true);
			FileWriter statusLogFile = new FileWriter("/opt/ais/app/applications/soar_attachment_import/status.log",true);
			String newLine = System.getProperty("line.separator");
			for (DBObject docObject : cursor) {
				String doc_id = docObject.get("_id").toString();
				if(processedList.contains(doc_id))
					continue;
				options.setDocid(doc_id);
				int doccount = getAttachmentCountForDocId(doc_id);
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
	
	public static int getAttachmentCountForDocId(String DOC_ID){		
		HttpGet getRequest = new HttpGet("http://cdsplus.austin.hp.com/cadence/app/soar/content/"+DOC_ID+"/*");
		String dbName = "soar";
		try {
			HttpResponse response = httpClient.execute(getRequest);
			SAXReader reader = new SAXReader();
			Document document = null;
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream in = response.getEntity().getContent();
				reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				document = reader.read(in);
			}
			return document.selectNodes("/result/proj:ref").size();
			
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} 
		return 0;
	}
	
}
