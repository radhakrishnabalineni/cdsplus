/**
 * 
 */
package com.hp.concentra.extractor.workItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.hp.cdspDestination.Delete;
import com.hp.cdspDestination.MongoDestination;
import com.hp.cdspDestination.Post;
import com.hp.cdspDestination.ProjContent;
import com.hp.cdspDestination.Put;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cks.concentra.core.event.EventLog;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.DocbaseUtils;
import com.hp.concentra.extractor.utils.ExtractorSessionPool;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.workItem.WorkItem;

/**
 * Documents are stored in CDS+ in the following path:
 * <content_type>/loader/<docName>
 * 
 * i.e. support/loader/c12345689
 *   or supportcontent/loader/c123456789
 *   
 * @author dahlm
 *
 */
public abstract class ConcentraExtractElement {

	// initialization fields
	public static final String PROJECT_SERVER = "project_server";
	public static final String XML_EXTENSION = ".xml";

	private static final String CONTENT_PATH = "content_path";
	private static final String PROJECT_FILE_SEPARATOR = "project_file_separator";
	// object_type (concentra) -> content_type (cds+) 
	public static final String CONTENT_ELEMENT_NAME = "content";
	private static final String CONTENT_CLASS_ATTR_NAME = "class";
	private static final String CONTENT_TYPE_ATTR_NAME = "type";

	public static final String LOADER = "/loader/"; 
	public static final String CONTENT = "content/loader/"; 

	// this is where to put checkOut of content for the extractor
	private static File contentPath = null;
	private static HashMap<String, String> contentClassesToTypes = new HashMap<String, String>();

	public static String file_separator = null;

	private static Post   post = null;
	private static Put    put = null;
	private static Delete del = null;

	private static  MongoDestination mongoDest = null;
	private static boolean legacy= false;

	protected String objectName;
	protected String chronicleId;
	protected String objectId;
	protected String eventType;
	protected String contentType = null;
	protected String extractResult;
	protected String extractMsg = "";

	protected ArrayList<ProjContent> projContent;
	protected String eventId;
	private File contentOutPath = null;

	/**
	 * @param eventId 
	 * @param chronicleId
	 * @param eventType
	 * @param projContent
	 */
	public ConcentraExtractElement(String eventId, String eventType, String objectName, String chronicleId, String objectId, String contentType) {
		super();
		this.eventId = eventId;
		this.eventType = eventType;
		this.objectName = objectName;
		this.chronicleId = chronicleId;
		this.objectId = objectId;
		this.contentType = contentType;

		projContent = new ArrayList<ProjContent>();

	}

	/**
	 * @return the projContent
	 */
	public ArrayList<ProjContent> getProjContent() {
		return projContent;
	}

	/**
	 * @param projContent the projContent to add
	 */
	public void addProjContent(ProjContent projContent) {
		this.projContent.add(projContent);
	}

	/**
	 * addProject at a specific index in the list
	 * @param idx
	 * @param projContent
	 */
	public void addProjContent(int idx, ProjContent projContent) {
		this.projContent.add(idx, projContent);
	}


	/**
	 * @return the documentId
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * There is no ItemId in the base element
	 * @return
	 */
	public String getItemId() {
		return null;
	}

	/**
	 * @return the eventId
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * @return the extractResult
	 */
	public String getExtractResult() {
		return extractResult;
	}

	/**
	 * @param extractResult the extractResult to set
	 */
	public void setExtractResult(String extractResult) {
		this.extractResult = extractResult;
	}

	/**
	 * Cleanup the temp directory
	 * @throws IOException
	 */
	public void cleanup() throws IOException {
		cleanupDirectory(contentOutPath);
	}

	/**
	 * cleanupDirectory 
	 * @param dir
	 */
	private void cleanupDirectory(File dir) {
		if (dir == null || !dir.exists()) {
			// directory is not there to delete
			return;
		}
		// list files
		File[] files = dir.listFiles();
		for(File f : files) {
			if (f.isDirectory()) {
				cleanupDirectory(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}


	/**
	 * @return the extractMsg
	 */
	public String getExtractMsg() {
		return extractMsg;
	}

	/**
	 * @param extractMsg the extractMsg to set
	 */
	public void setExtractMsg(String extractMsg) {
		this.extractMsg = extractMsg;
	}

	/**
	 * @return the chronicle_id
	 */
	public String getChronicleId() {
		return chronicleId;
	}

	/**
	 * @return the objectId
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * @return the updateType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * getObjectType returns the type of object this status is for
	 * @return
	 */
	protected String getObjectType() {
		return contentType;
	}

	/**
	 * getIdentifier returns the string that identifies this element
	 * @param status
	 * @return
	 */
	abstract protected String getIdentifier();

	/**
	 * getDescription returns the String describing the extraction event
	 * @param status
	 * @return
	 */
	private String getDescription(String status) {
		StringBuffer sb = new StringBuffer("Document ");
		if (status == WorkItem.SUCCESS) {
			if (eventType == WorkItem.EVENT_DELETE){
				sb.append(" unextracted");
			} else if (eventType == WorkItem.EVENT_UPDATE) {
				sb.append(" extracted");
			} else {
				sb.append(" touched");
			}
		} else {
			if (eventType == WorkItem.EVENT_DELETE){
				sb.append(" unextract failed");
			} else if (eventType == WorkItem.EVENT_UPDATE){
				sb.append(" update failed");
			} else {
				sb.append(" touch failed");
			}
		}
		sb.append(" on cds+");
		return sb.toString();
	}

	/**
	 * getDescription returns the String describing the extraction event
	 * @param status
	 * @return
	 */
	private String getEvent(String status) {
		if (status == WorkItem.SUCCESS) {
			if (eventType == WorkItem.EVENT_DELETE){
				return EventLog.DOCUNXTRACTD;
			} else {
				return EventLog.DOCEXTRACTED;
			}
		} else {
			return EventLog.DOCXTRACTFAIL;
		}
	}


	/**
	 * Log the results of the extraction to the c_event_log
	 * @param dql
	 * @param status
	 * @throws DmRepositoryException
	 * @throws DfException 
	 */
	protected void logConcentraEvent(StringBuffer dql, String status) throws DmRepositoryException, DfException {

		ConcentraAdminSession docbaseSession = null;
		IDfCollection results = null;
		String docVersion = null;

		LoaderLog.info("logConcentraEvent: "+ objectName+" : " + status+" : "+eventType);

		if (eventType == WorkItem.EVENT_TOUCH) {
			LoaderLog.info("Touch events not logged.");
			return;
		}

		try {
			docbaseSession = ExtractorSessionPool.getDocbaseSession();

			dql.setLength(0);
			dql.append("select r_version_label from c_base_object (all) where r_object_id = '");
			dql.append(objectId).append("' and ANY r_version_label = 'FINAL'");
			results = DocbaseUtils.executeQuery(docbaseSession, dql.toString(), IDfQuery.EXEC_QUERY, "getExtractDocVersion");

			while(results.next() && (docVersion == null)) {
				String val = results.getString("r_version_label");
				if (val != null && val.indexOf('.') > -1) {
					docVersion = val;
				}
			}

			// put the extraction event into the log
			EventLog.insert(docbaseSession, chronicleId, objectId, objectName, docVersion, getEvent(status), getDescription(status), null, null);

		} finally {
			DocbaseUtils.closeCollection(results);
			ExtractorSessionPool.releaseDocbaseSession(docbaseSession);
		}

	}


	/**
	 * store walks the list of projContent items and sends them to cds+
	 * @throws ProcessingException 
	 * @throws JAXBException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public void store(Integer priority) throws ProcessingException {
		String retVal = null;
		//System.out.println("reached to ConcentraExtractElement store method."+ System.currentTimeMillis());
		try {

			if(legacy){

				if (eventType == WorkItem.EVENT_UPDATE) {
					for(ProjContent pc : projContent) {
						retVal = pc.put(put, eventType, priority );
					}
				} else if (eventType == WorkItem.EVENT_DELETE) {
					for(ProjContent pc : projContent) {
						retVal = pc.del(del, eventType, priority);
					}			
				} else if (eventType == WorkItem.EVENT_TOUCH) {
					for(ProjContent pc : projContent) {
						retVal = pc.post(post, eventType, priority);
					}
				}
			}
			else{
				if (eventType == WorkItem.EVENT_UPDATE) {
					for(ProjContent pc : projContent) {
						retVal = pc.processUpdate(mongoDest, eventType, priority, true );
					}
				} else if (eventType == WorkItem.EVENT_DELETE) {
					for(ProjContent pc : projContent) {
						retVal = pc.processDelete(mongoDest, eventType, priority);
					}			
				} else if (eventType == WorkItem.EVENT_TOUCH) {
					for(ProjContent pc : projContent) {
						retVal = pc.processTouch(mongoDest, eventType, priority);
					}
				}

			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ProcessingException("Store Failed", true);
		}catch (MongoUtilsException cnfe) {
			cnfe.printStackTrace();
			throw new ProcessingException("Store Failed", true);
		}
	}

	/**
	 * @return the content_type for the property file
	 */
	public String getCDSPlusContentType() {
		return contentClassesToTypes.get(contentType);
	}

	/**
	 * getContentPath
	 * @return
	 */
	public static File getContentPath() {
		return contentPath;
	}

	public File getContentOutPath() throws IOException {

		// if it is already set up, just return it.
		if (contentOutPath != null) {
			return contentOutPath;
		}

		LoaderLog.info("ConcentraExtractElement getContentOutPath");
		contentOutPath= new File(contentPath, eventId);
		if (contentOutPath.exists()) {
			if (contentOutPath.isDirectory()) {
			} else {
				contentOutPath.delete();
			}
		}
		if (!contentOutPath.mkdirs()) {
			throw new IOException("Failed to create temp dir "+contentOutPath);
		}
		return contentOutPath;
	}


	public static void init(ConfigurationReader config) throws DocumentException, DfException {
		String contPath = config.getAttribute(CONTENT_PATH);
		if (contPath == null || contPath.length() == 0) {
			throw new IllegalArgumentException(CONTENT_PATH + " is not specified");
		}
		contentPath = new File(contPath);
		if (!contentPath.isDirectory()) {
			throw new IllegalArgumentException(CONTENT_PATH+" is not a directory!");
		} else if (!contentPath.canWrite()) {
			throw new IllegalArgumentException(CONTENT_PATH+" is not writable!");
		}


		file_separator = config.getAttribute(PROJECT_FILE_SEPARATOR);

		String baseUrl = config.getAttribute( PROJECT_SERVER ); 
		put = new Put( baseUrl );
		post = new Post( baseUrl );
		del = new Delete( baseUrl );
		
		legacy= Boolean.valueOf(config.getAttribute("legacy"));
		//System.out.println("legacy:->"+legacy);
		mongoDest = new MongoDestination();

		// get the object_type(Concentra) -> content_type(CDS+) mapping
		NodeList contentClasses = config.getElements(CONTENT_ELEMENT_NAME);
		for (int index = 0; index < contentClasses.getLength(); index++ ) {
			org.w3c.dom.Node node = contentClasses.item(index);
			if (node instanceof org.w3c.dom.Element) {
				org.w3c.dom.Element element = (org.w3c.dom.Element) node;
				String contentClass = element.getAttribute(CONTENT_CLASS_ATTR_NAME);
				String contentType = element.getAttribute(CONTENT_TYPE_ATTR_NAME);
				if ((!"".equals(contentClass)) && (!"".equals(contentType))) {
					if (LoaderLog.isInfoEnabled())
						LoaderLog.info("Loading content class: " + contentClass);
					contentClassesToTypes.put(contentClass,contentType.toLowerCase());
				}
			}
		}
	}

	/**
	 * getBytes returns an array of bytes repesenting a Document
	 * @param propertyDoc
	 * @return
	 * @throws IOException
	 */
	static public byte[] getBytes( Document propertyDoc) throws IOException {
		XMLWriter writer = null;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048*1000);

		try {
			//      propertyDoc.normalize();
			OutputFormat format = OutputFormat.createPrettyPrint();
			writer = new XMLWriter(outStream, format);
			writer.write(propertyDoc);
		} catch (IOException ioe) {
			LoaderLog.error("Failed to get Document bytes.");
			throw ioe;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
		return outStream.toByteArray();
	}

	static public byte[] getBytes ( File f ) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048*1000);
		FileInputStream in = null;
		byte[] buffer = new byte[8092];
		int numRead;
		try {
			in = new FileInputStream(f);
			while ((numRead = in.read(buffer)) != -1) {
				outStream.write(buffer, 0, numRead);
			}
		} catch (IOException ioe) {
			LoaderLog.error("Failed to get file bytes.");
			throw ioe;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
		return outStream.toByteArray();

	}

	/**
	 * check to see if an object_type is supposed to be extracted.
	 * @param object_type
	 * @return
	 */
	static public boolean isValidObjectType(String object_type) {
		return contentClassesToTypes.containsKey(object_type);
	}

}
