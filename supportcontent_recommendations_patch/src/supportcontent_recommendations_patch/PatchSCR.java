package supportcontent_recommendations_patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBObject;

public class PatchSCR {
	private static MongoAPIUtils mongoapi = new MongoAPIUtils();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String idListFile = "/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/"+args[0]+".log";
		BufferedReader br = null;
		String line = "";
		String docid = null;
		String newLine = System.getProperty("line.separator");
		try {
			System.setProperty("mongo.configuration", "/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/mongo.properties");
			File updateLogFile = new File(
					"/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/"+args[0]+".update.log");
			updateLogFile.delete();
			updateLogFile = new File(
					"/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/"+args[0]+".update.log");
			updateLogFile.createNewFile();
			FileWriter fileWriter = new FileWriter("/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/"+args[0]+".update.log", true);
			
			br = new BufferedReader(new FileReader(idListFile));
			while ((line = br.readLine()) != null) {
				line = line.trim();
				docid = line.substring(0, line.length() - 4);
				
				fileWriter.write("supportcontent_recommendations" + "\t" + docid + "\t" + line + newLine);
				fileWriter.flush();
				
				writeContent(
						"supportcontent_recommendations",
						docid,
						line,
						"text/xml",
						"/opt/ais/app/applications/reference_client/Spacedog_3.1_supportcontent_recommendations_content/packages_bkup/"+args[0]+"/"
								+ line, "update", Integer.valueOf(4), false);
				System.out.println("Successfully Saved File " + docid + File.separator + line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MongoUtilsException e) {
			e.printStackTrace();
		}

	}

	public static void writeContent(String dbName, String id,
			String gridFSFileName, String mimeType, String filePath,
			String updateType, Integer priority, boolean deleteFile)
			throws MongoUtilsException {
		BasicDBObject document = new BasicDBObject();
		document.put("mime", mimeType);
		document.put("eventType", updateType);
		document.put("priority", priority);
		mongoapi.writeContent(dbName, id, gridFSFileName, filePath, document,
				deleteFile);
	}
}
