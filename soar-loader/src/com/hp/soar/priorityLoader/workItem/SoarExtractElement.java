/**
 * 
 */
package com.hp.soar.priorityLoader.workItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
import com.hp.cdspDestination.Delete;
import com.hp.cdspDestination.MongoDestination;
import com.hp.cdspDestination.Post;
import com.hp.cdspDestination.ProjContent;
import com.hp.cdspDestination.Put;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.workItem.WorkItem;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.ExtractorUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.VirusScanner;

/**
 * @author dahlm
 *
 */
public abstract class SoarExtractElement {
	public final static String ITEMEXTRACTED = "Item Extracted";
	public final static String ITEMEXTFAILED = "Item Extract Failed";
	
	// extrenally available bit
	protected static final String EXT_AVAIL = "7";
	protected static final String NOT_EXT_AVAIL = "6";
	
	// Collection states we are concerned about
	public static final String OBSOLETE = "OBSOLETE";
	public static final String APPROVED = "APPROVED";
	
	// extraction states
	public static final String ITEM_NEW = "NEW";
	public static final String ITEM_UPTODATE = "UPTODATE";
	public static final String ITEM_MODIFIED = "MODIFIED";
	public static final String ITEM_DELETED = "DELETED";
	public static final String ITEM_IMMEDIATE_DELETED = "IMMEDIATE_DELETED";
	public static final String ITEM_UNKNOWN = "UNKNOWN";
	
	public static final String FIRST_VERSION_ANTECEDENT_ID = "0000000000000000";
	public static final String FILE_SEP = System.getProperty("file.separator");
	
	// empty String Array
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	// initialization fields
	private static final String SCHEMAFILE = "soar_schema_file";
	private static final String SOAR_DNS_NAME = "soar_dns_name";
	private static final String CONTENT_PATH = "content_path";
	private static final String CONTENT_TYPE = "content_type";
	public static final String PROJECT_SERVER = "project_server";
	private static final String PROJECT_FILE_SEPARATOR = "project_file_separator";
	private static final String SOARIGNORERCD = "soar_ignore_rcd";
	
	// this is where to put checkOut of content for the extractor
	protected static File contentPath = null;
	
	public static String schema_file = null;
	public static String dns_name = null;
	public static String file_separator = null;
	// Oid for unzip file type
	public static String unzipFileTypeOid = null;


	// flag indicating that RCD should be ignored for this extraction
	public static boolean ignoreRCD = false;
	
	private static String content_type = null;

	
	private static Post   post = null;
	private static Put    put = null;
	private static Delete del = null;

	protected String objectId;		// r_object_id of this dbObject
	protected String updateType;	// type of update for this object
	
	protected String extractResult;	// final result of this extraction
	protected String extractMsg = "";
	
	// DBService for this extraction
	protected SoarExtractionDBService dbService;
	
	// db document for this element
	IDfDocument	dbObj;
	
	private static boolean legacy= false;
	private static  MongoDestination mongoDest = null;
	

	/**
	 * @param eventId 
	 * @param chronicleId
	 * @param updateType
	 * @param projContent
	 */
	public SoarExtractElement(SoarExtractionDBService dbService, String objectId, String updateType) {
		super();
		this.dbService = dbService;
		this.objectId = objectId;
		this.updateType = updateType;
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


	protected String getBooleanValue(IDfDocument idfDoc, String attr)
			throws DfException {
		if (idfDoc.getBoolean(attr)) {
			return "Yes";
		}
		return "No";
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
	public String getUpdateType() {
		return updateType;
	}

	/**
	 * @param updateType the updateType to set
	 */
	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

//	/**
//	 * getEvent determines the event to be entered based on the status for an element
//	 * @param status
//	 * @return
//	 */
//	abstract protected String getEvent(String status);
	
//	/**
//	 * getObjectType returns the type of object this status is for
//	 * @return
//	 */
//	abstract protected String getObjectType();
//	
	/**
	 * getIdentifier returns the string that identifies this element
	 * @param status
	 * @return
	 */
	abstract protected String getIdentifier();
	
	/**
	 * getDescription returns the String describing the extraction event
	 * @param status
	 * @param exc Exception that may have been thrown
	 * @return
	 */
	private String getDescription(String status, Exception exc) {
		StringBuffer sb = new StringBuffer();
		sb.append(getIdentifier());
		if (status == WorkItem.SUCCESS) {
			if (updateType == ITEM_DELETED){
				sb.append(" delete scheduled");
			} else if (updateType == ITEM_IMMEDIATE_DELETED) {
				sb.append(" immediate delete completed");
			} else {
				sb.append(" updated");
			}

		} else {
			if (updateType == ITEM_DELETED){
				sb.append(" delete failed ");
			} else if (updateType == ITEM_IMMEDIATE_DELETED) {
				sb.append(" immediate delete failed ");
			} else {
				sb.append(" update failed ");
			}
			if (exc != null && exc.getMessage() != null) {
				sb.append("| ").append(exc.getMessage());
			}
			// remove all "'" from the string
			int idx = 0;
			while ((idx = sb.indexOf("'")) != -1 ) {
				sb.deleteCharAt(idx);
			}
			
			if (sb.length()>2000) {
				// truncate the message to 2000 chars to fit into docbase
				sb.setLength(2000);
			}
		}

		return sb.toString();
	}
	
	protected String getDoubleValue(String docAttr) {
		String attrValue = "";
		try {
			// Convert float/double numbers to string
			Double tmpDble = new Double(dbObj.getValue(docAttr).asDouble());
			attrValue = tmpDble.toString();
			int nPos = attrValue.indexOf(".");
			if (nPos != -1) {
				if (attrValue.substring(nPos + 1).length() < 2) {
					attrValue += "0";
				}
			}
		} catch (DfException ex) {
			LoaderLog.error("getAttributeValue error: " + ex.toString());
		}
		return attrValue;
	}	
	
	protected ArrayList<SoarExtractAttachment> loadAttachments(SoarExtractionDBService dbService, String updateType,
			String eventId, String collectionId, String itemId) throws DfException, ProcessingException {
		ArrayList<SoarExtractAttachment> attachments = new ArrayList<SoarExtractAttachment>();
		if (SoarItem.ftpOnlyUpdate) {
			// don't need attachments, only doing ftp server
			return attachments;
		}
		

		String vDocBuildMethod = "document";
		if (!dbObj.getString("r_assembled_from_id").equals(FIRST_VERSION_ANTECEDENT_ID)) {
			vDocBuildMethod = "assembly";
		}

		StringBuffer dql = new StringBuffer("SELECT r_object_id FROM sw_descriptive_content (ALL) WHERE title != ' ' and r_content_size > 0 and ");
		dql.append("(translation_state is null or translation_state NOT in ('Pending','Released','Submitted','Canceled','Not Required')) AND ");
		dql.append("r_object_id IN (SELECT r_object_id from dm_sysobject (all) in ");
		dql.append(vDocBuildMethod).append(" id('").append(dbObj.getString("r_object_id"));
		dql.append("') WHERE r_object_type = 'sw_descriptive_content') ORDER BY i_chronicle_id");

		String dqlStr = dql.toString();
		/*
		 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- COnverting the logger from info to debug to 
		 * avoid DQL statements in the log
		 */
		//LoaderLog.info("extractAttchments DQL :" + dqlStr);
		LoaderLog.debug("extractAttchments DQL :" + dqlStr);
		IDfCollection results = null;
		try {
			results = dbService.getResults(dqlStr, "loadCollectionAttachments");
			if (results != null) {
				// set up the cds+ collection location for all of the attachments
				StringBuffer cdspPath = new StringBuffer(SoarExtractElement.getContent_type());
				cdspPath.append(ProjContent.normalizeContentNameForUrl(collectionId));
				cdspPath.append(SoarExtractElement.file_separator);
				int cdspPathLen = cdspPath.length();
				
				while(results.next()) {
					cdspPath.setLength(cdspPathLen);
					try {
						SoarExtractAttachment attachment = new SoarExtractAttachment(dbService, results.getString("r_object_id"), updateType,
								eventId, collectionId, itemId, cdspPath);
						attachments.add(attachment);
					} catch (IllegalArgumentException ie) {
						LoaderLog.error(ie.getMessage());
					}
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return attachments;
	}

	
	protected void logSoarEvent(IDfSession session, StringBuffer dql, String status, String event, String chronicleId, String collectionId, String objectType, Exception exc) throws DfException {
		dql.setLength(0);
		dql.append("insert into soar_event_log (event, event_date, user_name, object_id, chronicle_id, ");
		dql.append("collection_id, object_type, description) values ('");
		dql.append(event).append("', DATE(NOW), 'cdsplusExt', '").append(objectId).append("','");
		dql.append(chronicleId).append("','").append(collectionId).append("','").append(objectType).append("','");
		dql.append(getDescription(status, exc)).append("')");
		IDfCollection results = null;
		try {
			results = DocbaseUtils.executeQuery(session, dql.toString(), IDfQuery.DF_READ_QUERY, "Log extract status");
			if (results == null || !results.next()) {
				throw new DfException("Insert of extraction status failed");
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
	}
	
	/**
	 * store walks the list of projContent items and sends them to cds+
	 * @throws ProcessingException 
	 * 
	 */
	public void storePC(ProjContent pc, Integer priority) throws ProcessingException {
		// retVal is used for debugging.  Otherwise you can't see the return value
		String retVal = null;
		try {
			if(legacy){
				if ((updateType == ITEM_MODIFIED) || (updateType == ITEM_NEW)) {
					retVal = pc.put(put, WorkItem.EVENT_UPDATE, priority);
				} else if ((updateType == ITEM_DELETED) || (updateType == ITEM_IMMEDIATE_DELETED)) {
					retVal = pc.del(del, WorkItem.EVENT_DELETE, priority);
				}
			}else{
				if ((updateType == ITEM_MODIFIED) || (updateType == ITEM_NEW)) {
					retVal = pc.processUpdate(mongoDest, WorkItem.EVENT_UPDATE, priority, true);
				} else if ((updateType == ITEM_DELETED) || (updateType == ITEM_IMMEDIATE_DELETED)) {
					retVal = pc.processDelete(mongoDest, WorkItem.EVENT_DELETE, priority);
				}

			}
		} catch (IOException ioe) {
			throw new ProcessingException("Store Failed", ioe, true);
		}catch (MongoUtilsException cnfe) {
			throw new ProcessingException("Store Failed", true);
		}
	}
	
	protected void addFlagOidsElementForComponent(Element attElement,
			String[] flagOids, String applicability) throws DfException {
		boolean bAddedFlag = false;
		Element flagsElement = SoarExtractXMLBuilder.addXMLElement(attElement, "flags");

		StringBuffer dql2 = new StringBuffer("SELECT name, flag_oid FROM dm_dbo.soar_flags WHERE applicability like '%");
		dql2.append(applicability).append("%'");

		if (flagOids != null && flagOids.length > 0) {
			dql2.append(" AND (active=true OR (");
			for(int i=0; i<flagOids.length; ) {
				dql2.append("flag_oid=").append(flagOids[i++]);
				if (i<flagOids.length) {
					dql2.append(" OR ");
				}
			}
			dql2.append("))");
		} else {
			dql2.append(" AND active=true");
		}

		IDfCollection results = null;
		try {
			results = dbService.getResults(dql2.toString(), "addFlagOidsElementForComponent");

			if (results != null) {
				while (results.next()) {
					String flagOid = results.getString("flag_oid");
					String flagName = results.getString("name");
					String flagVal = hasStringValue(flagOids, flagOid);

					Element flagElement = SoarExtractXMLBuilder.addXMLElement(flagsElement, "flag", flagName);
					SoarExtractXMLBuilder.addXMLAttribute(flagElement, "oid", flagOid, false);
					SoarExtractXMLBuilder.addXMLAttribute(flagElement, "flag-value", flagVal, false);
					bAddedFlag = true;
				}
			}
		} catch (DfException e) {
			LoaderLog.error("Failed to add flags " + ExtractorUtils.getStackTrace(e));
			throw e;
		} finally {
			DocbaseUtils.closeResults(results);
		}
		if (!bAddedFlag) {
			SoarExtractXMLBuilder.addXMLAttribute(flagsElement, "nil", "true", true);
		}
	}

	protected void addLanguageOidsElement(Element parentElement, IDfDocument idfDoc) throws DfException {

		Element langMainElement = SoarExtractXMLBuilder.addXMLElement(parentElement, "languages");

		String[] langChOids = getRepeatingAttr(idfDoc, "language_char_set_oids");
		for(String langChOid : langChOids) {
			Element langEle = SoarExtractXMLBuilder.addXMLElement(langMainElement, "language");

			SoarExtractXMLBuilder.addXMLAttribute(langEle, "oid", langChOid, false);
			SoarExtractXMLBuilder.addXMLAttribute(langEle, "language-iso-code",
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_ISO_CODES_LIST, langChOid), false);

			SoarExtractXMLBuilder.addXMLElement(langEle, "language-in-English",
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_LIST, langChOid));

			SoarExtractXMLBuilder.addNullbaleElement(langEle, "character-set-in-English", 
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_CHAR_SETS_LIST, langChOid));

			SoarExtractXMLBuilder.addXMLElement(langEle, "local-language",
					ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_LIST, langChOid));

			SoarExtractXMLBuilder.addNullbaleElement(langEle, "local-character-set", 
					ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_CHAR_SETS_LIST, langChOid));
		}
	}

	protected void addAttachmentsElements(Element parentElement,
			ArrayList<SoarExtractAttachment> attachments, String collectionId, String itemId ) throws DfException, ProcessingException {
		
		// set up the object path for the attachments
		StringBuffer objPath = new StringBuffer(collectionId);
		if (itemId != null) {
			objPath.append(SoarExtractElement.file_separator).append(itemId).append("_");
		} else {
			objPath.append(SoarExtractElement.file_separator);
		}
		int objPathLen = objPath.length();

		Element atmtsElement = SoarExtractXMLBuilder.addXMLElement(parentElement, "attachments");
		for(SoarExtractAttachment attachment : attachments) {
			// clear the last entry
			objPath.setLength(objPathLen);
			attachment.addAttachmentElement(atmtsElement, objPath);
		}
	 }
	 
	static public byte[] getBytes(Document document) throws IOException {
		XMLWriter writer = null;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048*1000);

		try {
			document.normalize();
			OutputFormat format = OutputFormat.createPrettyPrint();
			writer = new XMLWriter(outStream, format);
			writer.write(document);
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

	/**
	 * @return the content_type
	 */
	public static String getContent_type() {
		return content_type;
	}

	/**
	 * getContentPath
	 * @return
	 */
	public static File getContentPath() {
		return contentPath;
	}
	
	public static void init(ConfigurationReader config) throws DocumentException, DfException {
		schema_file = config.getAttribute(SCHEMAFILE);
		if (schema_file == null) {
			throw new IllegalArgumentException(SCHEMAFILE+ " not specified in config file.");
		}

		dns_name = config.getAttribute(SOAR_DNS_NAME);
		if (dns_name == null) {
			throw new IllegalArgumentException(SOAR_DNS_NAME+" not specified in config file.");
		}
		
		String contPath = config.getAttribute(CONTENT_PATH);
		contentPath = new File(contPath);
		if (!contentPath.isDirectory()) {
			throw new IllegalArgumentException(CONTENT_PATH+" is not a directory!");
		}
		
		file_separator = config.getAttribute(PROJECT_FILE_SEPARATOR);
		
		content_type = config.getAttribute(CONTENT_TYPE)+ file_separator+"loader"+file_separator;
		
		String rcdProperty = config.getAttribute(SOARIGNORERCD);
		ignoreRCD = (rcdProperty != null && rcdProperty.trim().equalsIgnoreCase("yes"));
		
//		String iniFileName = config.getAttribute(INI_FILE_NAME);
//		iniParser = new SoarExtractorIniParser(iniFileName);
		
		String baseUrl = config.getAttribute( PROJECT_SERVER ); 
		put = new Put( baseUrl );
		post = new Post( baseUrl );
		del = new Delete( baseUrl );
		legacy= Boolean.parseBoolean(config.getAttribute("legacy"));
		//System.out.println("legacy:->"+legacy);
		mongoDest = new MongoDestination("soar");
		
		try {
			DestinationAdapter.initDestinations(config);
		} catch (FileSystemException e) {
			LoaderLog.error("Unable to configure destination: -->"+e.getMessage());
			throw new IllegalArgumentException("Unable to configure destination: "+e.getMessage(),e);
		}
			
	}
	
	/**
	 * getRepeatingAttr returns an array of values for a repeating string value
	 * @param DfDoc
	 * @param attribute
	 * @return
	 * @throws DfException
	 */
	protected String[] getRepeatingAttr(IDfDocument DfDoc, String attribute) throws DfException {
		String[] attrs = EMPTY_STRING_ARRAY;
		String allAttr = DfDoc.getAllRepeatingStrings(attribute, "|");
		if (!allAttr.startsWith("No Value") && !allAttr.equals("")) {
			ArrayList<String> strings = new ArrayList<String>();
			int nPosTo = allAttr.indexOf("|");
			int nPosFrom = 0;
			while (nPosTo != -1) {
				strings.add(allAttr.substring(nPosFrom, nPosTo));
				nPosFrom = nPosTo + 1;
				nPosTo = allAttr.indexOf("|", nPosFrom);
			}
			strings.add(allAttr.substring(nPosFrom));
			attrs = strings.toArray(new String[strings.size()]);
		}
		return attrs;
	}

	protected String getDateValue(IDfTypedObject idfDoc, String field,
			boolean withTime) {
		String dateTimeVal = "";

		try {
			if (idfDoc != null) {
				try {
					IDfTime time = idfDoc.getTime(field);
					dateTimeVal = getFormattedDate(time, withTime);
				} catch (Exception e) {
					LoaderLog.info("Field " + field + " is not a Date");
					dateTimeVal = idfDoc.getString(field);
				}
			}
		} catch (DfException ex) {
			LoaderLog.error("Error getting date value for : " + field
					+ "; Cause -->" + ex.toString());
		}
		return dateTimeVal;
	}

	protected String getFormattedDate(IDfTime time, boolean withTime) {
		String dateTimeVal = "";
		if (!time.isNullDate()) {
			String sYear = "" + time.getYear();
			String sMonth = getZeroAppended(time.getMonth());
			String sDay = getZeroAppended(time.getDay());
			dateTimeVal = sYear + "-" + sMonth + "-" + sDay;

			if (withTime) {
				String sHour = getZeroAppended(time.getHour());
				String sMinute = getZeroAppended(time.getMinutes());
				String sSecond = getZeroAppended(time.getSeconds());
				dateTimeVal += "T" + sHour + ":" + sMinute + ":" + sSecond;
			}
		}
		return dateTimeVal;
	}

	/**
	 * getSQLInString creates a comma separated string of quoted values.
	 * @param vals
	 * @return
	 */
	protected String getSQLInString(String[] vals) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<vals.length;)  {
			sb.append("'").append(vals[i++]).append("'");
			if (i < vals.length) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	protected String getStringValue(IDfDocument idfDoc, String attr)
			throws DfException {
		String val = idfDoc.getString(attr);
		if (val == null) {
			val = "";
		}
		return val;
	}
	  
	protected String getZeroAppended(int val) {
		String sHour = "" + val;
		if (sHour.length() == 1) {
			sHour = "0" + sHour;
		}
		return sHour;
	}

	/**
	 * hasStringValue is used to check an array to see if it has the requested value in it 
	 * @param strings
	 * @param val
	 * @return
	 */
	protected String hasStringValue(String[] strings, String val) {
		for(String s : strings) {
			if (val.equals(s)) {
				return "Yes";
			}
		}
		return "No";
	}

	protected String formatPath(String path) {
		path = path.replaceAll("\\\\", "/");
		return path.replaceAll("//", "/");
	}
	
	
	//nidoh soar mod
	//put this in this file because the item file needed it in a specific place to assist in figuring shouldPublish boolean value
	public boolean matchingMD5s(String itemROID){
		//if any exception is caught inside this function, return false as worst case scenario is to re-publish a file
		//as far as maintaining the DB values...will just be incomplete
		
		if(itemROID == null || itemROID == "" || itemROID.equals("")){
			LoaderLog.error("Inside matchingMD5s, the item r_object_id passed is null or unusable. Exiting function but continuing publish per usual."); // not sure how to provide useful info for what object we have without making it more expansive than its value
			return false;
		}
		
		try{
			String itemQry = "select r_assembled_from_id from sw_item WHERE r_object_id='" + itemROID + "'";
			String AFI = dbService.getColValueAsString(itemQry, "r_assembled_from_id", "matchingMD5s");
			
			String vDocBuildMethod = "document";
			if(AFI != null && !AFI.equals(FIRST_VERSION_ANTECEDENT_ID)) {
				vDocBuildMethod = "assembly";
			}

			String fileQry = "select distinct r_object_id as file_obj_id from sw_file (ALL) where r_object_id IN (select r_object_id from dm_sysobject (all) in " + vDocBuildMethod + " id('" + itemROID  + "') where r_object_type = 'sw_file')";
			String fileROID = dbService.getColValueAsString(fileQry, "r_object_id", "matchingMD5s");
			
			if(fileROID == null || fileROID == "" || fileROID.equals("")){
				LoaderLog.error("For item with r_object_id " + itemROID + ", no associated files were found from query inside matchingMD5. Exiting function but continuing publish per usual.");
				return false;
			}
			
			IDfDocument dbObj = dbService.getDocumentByQualification("sw_file where r_object_id='"+ fileROID + "'");
			
			if(dbObj == null){
				LoaderLog.error("DB Object constructed for file with r_object_id " + fileROID + " is null and matchingMD5 cannot continue its logic. Exiting function but continuing publish per usual.");
				return false;
			}
			
			String fullFilePath = dbObj.getRepeatingString("release_directories", 0).concat("/").concat(dbObj.getString("object_name"));
			
			//use file path to ensure we got the right checksum for the specific file we are checking
			String MD5Qry = "select checkvalue from dm_dbo.soar_published_files where remote_server_path='" + fullFilePath + "'";
			String PublishedMD5 = dbService.getColValueAsString(MD5Qry, "checkvalue", "matchingMD5s");			
			
			//if the MD5 for the file on the server (that we have recorded) matches the one 'coming in', it is the same file and no need to re-publish the same file
			//otherwise if the MD5s do NOT match, there has been some change to the file and it needs to be published out to capture the changes
			if(PublishedMD5 != null && (PublishedMD5 == dbObj.getString("checkvalue") || PublishedMD5.equals(dbObj.getString("checkvalue")))){
				return true;
			} else {
				return false;
			}
		
		}catch (DfException e) {
			LoaderLog.error("One of the docbase queries inside matchingMD5s function for item with r_object_id " + itemROID + " failed. Setting mode to republish this file as checksum comparison cannot happen.");
			return false;
		}catch(Exception e){
			LoaderLog.error("Trouble populating item-->file relationship inside matchingMD5s for item with r_object_id of " + itemROID + ". Setting mode to republish this file as checksum comparison cannot happen.");
			return false;
		}
		
	}

}
