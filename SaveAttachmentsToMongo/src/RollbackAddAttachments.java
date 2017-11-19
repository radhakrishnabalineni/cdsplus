import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;

public class RollbackAddAttachments {
	private static MongoAPIUtils mongoapi = new MongoAPIUtils();
	
	public static void main(String[] args) throws MongoUtilsException {
		System.setProperty("mongo.configuration","/opt/ais/app/applications/soar_attachment_import/mongo.properties");

		String idListFile = "/opt/ais/app/applications/soar_attachment_import/update.log";
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(idListFile));			
			while ((line = br.readLine()) != null) {
				String lineArray[] = line.split("\\t");
				if(removeAttachment(lineArray[0],lineArray[1],lineArray[2])){
					System.out.println("Success: Successfully Removed File "+lineArray[0]+"/"+lineArray[1]+"/"+lineArray[2]);
				}else{
					System.out.println("Error: Unable to Remove File "+lineArray[0]+"/"+lineArray[1]+"/"+lineArray[2]);
				}
	 		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean removeAttachment(String dbName,String id,String gridFSFileName){
		
		try {
			String contentBucketName = ConfigurationManager.getInstance().getMappingValue(dbName, ConfigurationManager.CONTENT_BUCKET_NAME);
			mongoapi.deleteContent(dbName,id,contentBucketName,gridFSFileName);
			return true;
		} catch (MongoUtilsException e) {
			e.printStackTrace();
		}
		return false;	
	}	
	
}
