package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.zip.ZipException;

import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.ConnectionPool;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.PendingDeleter;
import com.hp.soar.priorityLoader.utils.VirusScanException;

public class SoarExtractSWFile extends SoarExtractElement {

	public static SFTPDestination sftpDestination = null;
	// public as these are used by the deleter to update the field as well
    public static final String FILE_DELIVERY_CONFIRMATION = "file_delivery_confirmation";
    public static final String R_IMMUTABLE_FLAG = "r_immutable_flag";
	
	// flag indicating that this update has been performed 
	// it is used for state recovery on a retry
	protected boolean completed = false;

	// disk file 
	protected File localFile;
	// remote file
	protected String remoteFile;
	// remote file directory
	protected String serverDir;
	
	// collectionId, itemId and objectId are saved for delete events
	protected String collectionId;
	protected String itemId;
	protected boolean published;	// when true, the file is actually on the ftp server
	//protected boolean externallyAvailable = false; // when true, the file is marked as externally available -- is this variable ever used?
	protected boolean md5match; // nidoh soar loader mod
	protected String zipElementMD5; //nidoh soar loader mod
	
	
	/**
	 * 
	 * @param destinationAdapter TODO
	 * @param dbService
	 * @param objectId
	 * @param updateType
	 * @param lastExtractionTime
	 * 
	 *  These parameters are used to build the local path where the binaries are checked out to
	 * @param eventId	
	 * @param collectionId
	 * @param itemId
	 * @throws DfException
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws VirusScanException 
	 */
	public SoarExtractSWFile(DestinationAdapter destinationAdapter, SoarExtractionDBService dbService, String objectId, IDfDocument dbObj,
			String updateType, Date lastExtractionTime, String eventId, String collectionId, String itemId) throws DfException, ZipException, IOException {
		super(dbService, objectId, updateType);

		this.collectionId = collectionId;
		this.itemId = itemId;
		
		this.dbObj = dbObj;
		
		setFileEvent(lastExtractionTime);
		published = published();
		
		setupFile(destinationAdapter, eventId, collectionId, itemId);		
	}
	
	/**
	 * Check to see if a file exists on the FTP server
	 * The parameters are used for when the local file needs to be checked out
	 * @return
	 * @throws DfException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws ZipException 
	 */
	protected boolean remoteFileExists(DestinationAdapter destination) throws FileNotFoundException, DfException, ZipException, IOException {
		try {
			if (remoteFile != null && !destination.exists(remoteFile)) {
				return false;
			} 
			return true;
		} catch (FileSystemException e) {
			// if we can't check that it exists, try to publish it again
			return false;
		}
	}
	
	/**
	 * For single files, remoteFileDoesNotExist is the opposite of remoteFileExists
	 * @param destinationAdapter TODO
	 * @return
	 * @throws FileNotFoundException
	 * @throws ZipException
	 * @throws DfException
	 * @throws IOException
	 */
	protected boolean remoteFileDoesNotExist(DestinationAdapter destinationAdapter) throws FileNotFoundException, ZipException, DfException, IOException {
		return !remoteFileExists(destinationAdapter);
	}
	
	/**
	 * isPublished checks the remote system to see if the files are actually there
	 * @param destinationAdapter TODO
	 * @return
	 * @throws IOException 
	 * @throws DfException 
	 * @throws ZipException 
	 * @throws FileNotFoundException 
	 */
	protected boolean isPublished(DestinationAdapter destinationAdapter) throws FileNotFoundException, ZipException, DfException, IOException {
		//at this point, published holds the state of the file in the DB
		if (updateType == ITEM_UPTODATE) {
			if (published) {
				// The file is supposed to be there so see if the file is there 
				return remoteFileExists(destinationAdapter);
			} else {
				return false;
			}
		} else if ((updateType == ITEM_IMMEDIATE_DELETED) || (updateType == ITEM_DELETED)) {
			if (!published) {
				// see if the file is not there
				return !remoteFileDoesNotExist(destinationAdapter);
			} else {
				// the file is there
				return true;
			}
		} else if (updateType == ITEM_NEW || updateType == ITEM_MODIFIED) {
			// This needs to be published
			return false;
		}
		// The published state is OK for this item so return it
		return published;
	}
	
	/**
	 * published determines if this swFile has been published or not according to the DB.
	 * @return
	 */
	private boolean published() {
		try {
			int numValues = dbObj.getValueCount(FILE_DELIVERY_CONFIRMATION);
			if (numValues >= 1) {
				return dbObj.getRepeatingBoolean(FILE_DELIVERY_CONFIRMATION, 0);
			}
		} catch (DfException e) {
			StringBuffer sb = new StringBuffer();
			sb.append("Failed to get "+ FILE_DELIVERY_CONFIRMATION +" for ").append(collectionId).append("|").append(itemId).append("|").append(getIdentifier());
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog(sb.toString(), e));
		}
		return false;
	}
	
	/**
	 * addSWFileElement to the XML
	 * @param softwareSetElement
	 * @param languageAtFileLevel
	 * @throws DfException
	 */
	public void addSWFileElement(Element softwareSetElement, boolean languageAtFileLevel) throws DfException {
		// Add software-file element
		Element sfFileElement = SoarExtractXMLBuilder.addXMLElement(softwareSetElement, "software-file");
		LoaderLog.info("Event for software file " + dbObj.getString("object_name")+ " is " + updateType);
		SoarExtractXMLBuilder.addXMLAttribute(sfFileElement, "partner-update-type", (updateType == ITEM_IMMEDIATE_DELETED ? ITEM_DELETED : updateType), false);
		SoarExtractXMLBuilder.addXMLAttribute(sfFileElement, "file-sequence", dbObj.getString("file_sequence"),false);
		SoarExtractXMLBuilder.addXMLElement(sfFileElement, "checkvalue", dbObj.getString("checkvalue"));

		String cmdLine = dbObj.getString("cmdline");

		SoarExtractXMLBuilder.addNullbaleElement(sfFileElement, "cmdline", cmdLine);

		SoarExtractXMLBuilder.addXMLElement(sfFileElement, "filename", dbObj.getString("object_name"));
		SoarExtractXMLBuilder.addXMLElement(sfFileElement, "filesize", dbObj.getString("r_content_size"));

		String[] serverNameOids = getRepeatingAttr(dbObj, "server_name_oids");
		String[] relDirs = getRepeatingAttr(dbObj, "release_directories");
		Element serverLocationElemet = SoarExtractXMLBuilder.addXMLElement(sfFileElement, "server-locations");
		for (int i=0; i<serverNameOids.length && i < relDirs.length; i++) {
			String elValue = "Yes";
			String fileDelConfirmation = dbObj.getValue("file_delivery_confirmation").asString();
			if (fileDelConfirmation.length() < 1 || fileDelConfirmation.equals("F")) {
				elValue = "No";
			}
			Element serverDirElement = SoarExtractXMLBuilder.addXMLElement(serverLocationElemet, "server-directory");
			SoarExtractXMLBuilder.addXMLAttribute(serverDirElement,"file-delivery-confirmation", elValue, false);

			String serverNameOid = serverNameOids[i];
			LoaderLog.info("server-location oid value: "+ serverNameOid);
			if (serverNameOid != null) {
				Element serverElement = SoarExtractXMLBuilder.addXMLElement(serverDirElement, "server");
				SoarExtractXMLBuilder.addXMLAttribute(serverElement, "oid", serverNameOid, false);

				SoarExtractXMLBuilder.addXMLElement(serverElement, "protocol", 
						ReferenceLists.getLabel(ReferenceLists.SERVER_PROTOCOLS_LIST, serverNameOid));
				SoarExtractXMLBuilder.addXMLElement(serverElement, "server-name",
						ReferenceLists.getLabel(ReferenceLists.SERVER_FEEDS_LIST, serverNameOid));
			}
			String directory = relDirs[i];
			if (directory != null) {
				SoarExtractXMLBuilder.addXMLElement(serverDirElement, "directory", formatPath(directory));
			} else {
				SoarExtractXMLBuilder.addXMLElement(serverDirElement, "directory");
			}
		}

		SoarExtractXMLBuilder.addXMLElement(sfFileElement, "soar-access-url", "/soar/getContent.do?objectId=" + objectId);

		addFlagOidsElementForComponent(sfFileElement, getRepeatingAttr(dbObj, "flag_oids"), "FILE");
		
		Element langInFileElement = SoarExtractXMLBuilder.addXMLElement(sfFileElement, "languages-in-file", "");

		if (languageAtFileLevel) {
			String[] langChrOids = getRepeatingAttr(dbObj, "language_charset_oids");
			for(String langChrOid : langChrOids) {
				Element langElement = SoarExtractXMLBuilder.addXMLElement(langInFileElement, "language");
				
				SoarExtractXMLBuilder.addXMLAttribute(langElement, "oid", langChrOid, false);

				SoarExtractXMLBuilder.addXMLElement(langElement,"language-in-English",
						ReferenceLists.getLabel(ReferenceLists.LANGUAGE_LIST, langChrOid));

				SoarExtractXMLBuilder.addNullbaleElement(langElement, "character-set-in-English",
						ReferenceLists.getLabel(ReferenceLists.LANGUAGE_CHAR_SETS_LIST, langChrOid));

				SoarExtractXMLBuilder.addXMLElement(langElement, "local-language",
						ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_LIST, langChrOid));

				SoarExtractXMLBuilder.addNullbaleElement(langElement, "local-character-set",
						ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_CHAR_SETS_LIST, langChrOid));

				SoarExtractXMLBuilder.addXMLAttribute(langElement, "language-iso-code",
						ReferenceLists.getLabel(ReferenceLists.LANGUAGE_ISO_CODES_LIST, langChrOid), false);
			}
		}

		String convertValue = dbObj.getBoolean("is_signed") ? "Yes" : "No";
		SoarExtractXMLBuilder.addXMLElement(sfFileElement, "is-digitally-signed", convertValue);
		
	}

	/**
	 * doImmediateDelete removes the swFile from the remote server
	 * @param inSubscription (Indicates this item should not be published due to filtering
	 * @param publishedLocations List of locations that are going to publish back into
	 * @throws IOException
	 * @throws ProcessingException 
	 */
	public void doImmediateDelete(DestinationAdapter destination, boolean inSubscription, HashSet<String> publishedLocations) throws IOException, ProcessingException {
		if (((updateType == ITEM_IMMEDIATE_DELETED) || (!inSubscription && published)) && !completed) {
			if (published && !publishedLocations.contains(remoteFile)) {
				// This is supposed to delete immediately so execute it
				if(!isPublishedByMultiCols(remoteFile, collectionId)){
					try {
						long startTime = System.currentTimeMillis();
						if (remoteFile != null) {
							LoaderLog.info("IMMEDIATE DELETE "+
									(updateType != ITEM_IMMEDIATE_DELETED ? "(should not publish)":"")+" swfile: "+remoteFile);

							destination.remove(remoteFile);					
							LoaderLog.info("IMMEDIATE DELETE finished :"+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
						}

					} catch (IOException e) {
						LoaderLog.error("FAILED ImmediateDelete of :"+remoteFile+" : "+e.getClass()+" - "+e.getMessage());
						throw e;
					}
				}
				//Remove Software File Info from publish table
				removePublishedInfo(collectionId, remoteFile);
			}
			completed = true;
			// flag that this swFile is no longer published (It may have been overwritten by a newer version but this one is gone)
			setPublished(false);
		}
	}
	
	/**
	 * doUpdate sends the swFile to the remote server
	 * @param published Indicates if the swFile has been published
	 * @param shouldPublish indicates the swFile should be published
	 * @throws IOException
	 * @throws ProcessingException 
	 */
	public void doUpdate(DestinationAdapter destination) throws IOException, ProcessingException {		
		if ((updateType == ITEM_NEW || updateType == ITEM_MODIFIED || (updateType == ITEM_UPTODATE && !published)) && !completed) {
			if (remoteFile == null) {
				LoaderLog.warn("UPDATE swFile from: " + localFile+ " has no destination");
				completed = true;
				return;
			}
			
			// This swFile is now updating so take it out of the pending delete list
			PendingDeleter.removeSwFile(remoteFile);
			try {
				LoaderLog.info("UPDATE "+(updateType == ITEM_UPTODATE ? "(republishing)" : "")+" swFile from: " + localFile + " to " + remoteFile);
				long startTime = System.currentTimeMillis();
				destination.putFile(remoteFile, new FileSource(localFile));
				//nidoh soar loader mod
				updatePublishTable(false);
				
				LoaderLog.info("UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
				
				setPublished(true);
				completed = true;
			} catch (IOException e) {
				LoaderLog.error("FAILED UPDATE of: "+remoteFile+" : "+e.getClass()+" - "+ e.getMessage());
				throw e;
			}
		}
	}
	
	/**
	 * set the sw_file FILE_DELIVERY_CONFIRMATION to identify if a file is published or not
	 * @param val
	 */
	protected void setPublished(boolean val) {
		try {
			// Call the static method to set the file published
			setFilePublished(dbObj, val);
		} catch (DfException e) {
			StringBuffer sb = new StringBuffer();
			sb.append("Failed to setPublished for ").append(collectionId).append("|").append(itemId).append("|").append(getIdentifier());
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog(sb.toString(), e));
		}

	}

	/**
	 * method to set a file as being published or not.  This is static as it is used by the swFile and the pending deleter
	 * @param dbObj
	 * @param val
	 * @throws DfException
	 */
	public static void setFilePublished(IDfDocument dbObj, boolean val) throws DfException {
		boolean immutable = dbObj.getBoolean(R_IMMUTABLE_FLAG);
		if (immutable) {
			dbObj.setBoolean(R_IMMUTABLE_FLAG, false);
		}
		int numVals = dbObj.getValueCount(FILE_DELIVERY_CONFIRMATION);
		
		for (int i=0; i<numVals; i++) {
			dbObj.setRepeatingBoolean(FILE_DELIVERY_CONFIRMATION, i, val);
		}
		dbObj.save();
		if (immutable) {
			dbObj.setBoolean(R_IMMUTABLE_FLAG, true);
			dbObj.save();
		}
	}
	
	/**
	 * doDelete removes the swFile from the remote server
	 * @param publishedLocations 
	 * @throws ProcessingException 
	 */
	public void doDelete(HashSet<String> publishedLocations) {
		if ((updateType == ITEM_DELETED)  && !completed) {
			if (published && !publishedLocations.contains(remoteFile)) {
				// schedule this swFile to be deleted at some future time
				PendingDeleter.addSwFileToDelete(remoteFile, collectionId, itemId, objectId);
			}
			completed = true;
		}
	}
	
	protected void setFileEvent(Date lastExtractionTime) throws DfException {
		if (updateType.equals(ITEM_DELETED) || updateType.equals(ITEM_IMMEDIATE_DELETED) || updateType.equals(ITEM_NEW)) {
			return;
		}
		Date creationDate = dbService.getFileCreationDate(objectId);
		Date updateDate = dbObj.getTime("r_modify_date").getDate();
		
		// if there is no creation date found, assume it is new
		if ((lastExtractionTime == null) || (creationDate == null || creationDate.after(lastExtractionTime))) {
			if (creationDate == null) {
				LoaderLog.warn("Creation date is null for swFile: "+ objectId+" collection: "+ collectionId+" item: "+itemId);
			}
		} else if ((lastExtractionTime != null) && updateDate.after(lastExtractionTime)) {
			updateType = ITEM_MODIFIED;
		} else {
			updateType = ITEM_UPTODATE;
		}
	}

	/**
	 * set this software file for deletion
	 */
	public void setupDelete() {
		// change update type to deleted if it isn't already immediate deleted
		if (updateType != ITEM_IMMEDIATE_DELETED) {
			updateType = ITEM_DELETED;
		}
	}
	
	protected void setupRemoteFile() throws DfException {
		// find the server this file is supposed to be loaded to ( 1 = ftp.hp.com )
		String[] serverNameOids = getRepeatingAttr(dbObj, "server_name_oids");
		String[] relDirs = getRepeatingAttr(dbObj, "release_directories");
		String contentFileName = dbObj.getString("object_name");
		
		for (int i=0; i<serverNameOids.length && i < relDirs.length; i++) {
			if (serverNameOids[i].equals(DestinationAdapter.getServerOid())) {
				// this file is to go to ftp.hp.com
				serverDir = relDirs[i];
				if (serverDir == null) {
					// this binary doesn't have a target location so skip it
					break;
				}
				serverDir = formatPath(serverDir);
				remoteFile = serverDir + file_separator + contentFileName;
				break;
			}
		}
	}
	
	protected void checkoutLocalFile(String eventId, String collectionId, String itemId) throws DfException, FileNotFoundException {
		String contentFileName = dbObj.getString("object_name");
		// This sw file needs to be extracted and virus scanned
		// setup the local file for extraction
		StringBuffer localContentFile = new StringBuffer(SoarExtractElement.getContentPath().getAbsolutePath());

		localContentFile.append(FILE_SEP).append(eventId).append(FILE_SEP).append(collectionId);
		if (itemId != null) {
			localContentFile.append(FILE_SEP).append(itemId);
		}

		// create the checkout location and get the file 
		localFile= new File(localContentFile.toString().toLowerCase());
		localFile.mkdirs();
		localFile = new File(localFile, contentFileName);
		if (!(localFile.exists() && (localFile.length() == dbObj.getContentSize()))) {
			// we don't have this file yet so check it out
			dbObj.getFile(localFile.getAbsolutePath());
		}

		// see if the file was actually checked out
		if (!localFile.exists() || !localFile.canRead()) {
			throw new FileNotFoundException("Failed to checkout "+contentFileName);
		}
		

	}
	
	/**
	 * setupFile does setup for a single file
	 * Note: it is overwritten by Zip files to setup differently
	 * @param destinationAdapter TODO
	 * @param eventId
	 * @param collectionId
	 * @param itemId
	 * @throws DfException 
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws FileNotFoundException 
	 */
	protected void setupFile(DestinationAdapter destinationAdapter, String eventId, String collectionId, String itemId) throws DfException, FileNotFoundException, ZipException, IOException {
		setupRemoteFile();
		
		//nidoh soar loader mod
		md5match = matchingMD5s();

		published = isPublished(destinationAdapter);
		
		if (!published && !(updateType == ITEM_DELETED || updateType == ITEM_IMMEDIATE_DELETED)) {
			if(!md5match){ // if the file has changed, its MD5 checksum won't match
				// checkout the file
				checkoutLocalFile(eventId, collectionId, itemId);
			} else { // otherwise there is no change to the content and no need to perform those operations on the content
				LoaderLog.info("For " + itemId + " inside " + collectionId + ", checksum is the same - publishing skipped for this file.");
			}
		}
		
	}
	
	/**
	 * getFileSequence returns the file_sequence of this sw_file
	 * @return
	 * @throws DfException 
	 */
	public String getFileSequence() {
		try {
			return dbObj.getString("file_sequence");
		} catch (DfException e) {
			LoaderLog.error("No file_sequence in sw_file "+objectId);
			return "1";
		}
	}
	
	/**
	 * getFileTypeOid returns the file_type_oid of this sw_file
	 * @return
	 * @throws DfException 
	 */
	public String getFileTypeOid() throws DfException {
		return dbObj.getString("file_type_oid");
	}

	@Override
	protected String getIdentifier() {
		try {
			return dbObj != null ? dbObj.getString("object_name") : "Unknown";
		} catch (DfException e) {
			return "Unknown";
		}
	}
	
	public void getPublishedLocations(HashSet<String> publishedLocations) {
		if ((updateType != ITEM_DELETED) && (updateType != ITEM_IMMEDIATE_DELETED)) {
			publishedLocations.add(remoteFile);
		}
	}
	
	/**
	 * init configures the SFTPDestination and gets the serverOid for soar
	 * @param config
	 * @throws FileSystemException 
	 
	public static void initDestinations(ConfigurationReader config) throws FileSystemException {
		sftpDestination = new SFTPDestination(config);
	}*/
	
	public static SoarExtractSWFile getSWFile(DestinationAdapter destination, SoarExtractionDBService dbService, String objectId, String updateType,
			Date lastExtractionTime, String eventId, String collectionId, String itemId) throws DfException, ZipException, IOException {
		
		IDfDocument dbObj = dbService.getDocumentByQualification("sw_file where r_object_id='"+ objectId + "'");
		if (dbObj == null) {
			throw new IllegalArgumentException("Collection "+collectionId+", item "+itemId+" is missing swFile: "+objectId);
		}
		String fileTypeOid = dbObj.getString("file_type_oid");
		
		// if this is an unzip file type, return a new unzip swFile
		if (fileTypeOid != null && fileTypeOid.equals(unzipFileTypeOid)) {
			return new SoarExtractZipSWFile(destination, dbService, objectId, dbObj, updateType, lastExtractionTime, eventId, collectionId, itemId);
		} else {
			return new SoarExtractSWFile(destination, dbService, objectId, dbObj, updateType, lastExtractionTime, eventId, collectionId, itemId);
		}
	}
	
	
	//nidoh soar loader mod
	public void updatePublishTable(boolean isZip){
		//everyone publishing to this spot on the server needs an update
		try{
			Calendar date = new GregorianCalendar();
			Date now = date.getTime();
			
			if(isZip){ // if the file is one which was extracted from the zip file, we will need to go fetch its MD5 since we don't store it
				String dqlQuery = "update dm_dbo.soar_published_files object set checkvalue='" + zipElementMD5 + "', set published_date=" + now + " where remote_server_path='" + remoteFile + "'";
				int num_updates = dbService.executeUpdate(dqlQuery, "updatePublishTable");
			} else { // a normal file which has all data already so it is an easy update
				String dqlQuery = "update dm_dbo.soar_published_files object set checkvalue='" + dbObj.getString("checkvalue") + "', set published_date=" + now + " where remote_server_path='" + remoteFile + "'";
				int num_updates = dbService.executeUpdate(dqlQuery, "updatePublishTable");
			}
		} catch (DfException e){
			LoaderLog.error("Inside updatePublishTable, could not update the published table with the new checksum value for " + remoteFile + " due to some docbase or query error.");
			LoaderLog.error(e.getMessage());
		} catch (Exception e) {
			LoaderLog.error("Inside updatePublishTable, could not update the published table with the new checksum value for " + remoteFile + " due to some error.");
			LoaderLog.error(e.getMessage());
		}
	}
	
	
	//nidoh soar mod
	public boolean matchingMD5s(){
		//if any exception is caught inside this function, return false as worst case scenario is to re-publish a file
		//as far as maintaining the DB values...will just be incomplete
		
		if(remoteFile == null || remoteFile.equals("") || remoteFile == ""){
			LoaderLog.error("Got into matchingMD5 function with a null or unusable remoteFile path value. Exiting function but continuing publish per usual.");
			return false;
		}
		
		if(dbObj == null){
			LoaderLog.error("Got into matchingMD5 function with a null or unusable DB Object for the file. Exiting function but continuing publish per usual.");
			return false;
		}
		
		try{
			//use file path to ensure we got the right checksum for the specific file we are checking
			String MD5Qry = "select collection_name, checkvalue from dm_dbo.soar_published_files where remote_server_path='" + remoteFile + "'";
			String PublishedMD5 = dbService.getColValueAsString(MD5Qry, "checkvalue", "matchingMD5s");
			String colID = dbService.getColValueAsString(MD5Qry, "collection_name", "matchingMD5s");
	
			//if there was no entry found for this file in the table, fetch the file from our server and calculate an MD5 for it
			//put THIS file in our table and use IT to compare the incoming MD5 from the sw_file table
			if(PublishedMD5 == null || PublishedMD5 == "" || PublishedMD5.equals("")){
				//if no entry, create one
				createPublishStatus(colID);
				
				File localFile = new File(remoteFile);
				if(localFile != null && !localFile.exists()){
					LoaderLog.error("Local file " + remoteFile + " does not exist. Cannot create checksum dynamically inside matchingMD5s - publish this file per usual.");
					return false;			
				} else {
					PublishedMD5 = getServerMD5();
					String update_checksum_query = "update dm_dbo.soar_published_files object set checkvalue='" + PublishedMD5 + "' where remote_server_path='" + remoteFile + "'";
					dbService.executeUpdate(update_checksum_query, "MD5Check");
				}
			}
			
			//if the MD5 for the file on the server (that we have recorded) matches the one 'coming in', it is the same file and no need to re-publish the same file
			//otherwise if the MD5s do NOT match, there has been some change to the file and it needs to be published out to capture the changes
			if(PublishedMD5 == dbObj.getString("checkvalue") || PublishedMD5.equals(dbObj.getString("checkvalue"))){
				return true;
			} else {
				return false;
			}
		}catch(DfException e){
			LoaderLog.error("One of the docbase queries inside matchingMD5s function for file " + remoteFile + " failed. Setting mode to republish this file as checksum comparison cannot happen.");
			LoaderLog.error(e.getMessage());
			return false;
		}catch(Exception e){
			LoaderLog.error("Trouble populating item-->file relationship inside matchingMD5s for file " + remoteFile + ". Setting mode to republish this file as checksum comparison cannot happen.");
			LoaderLog.error(e.getMessage());
			return false;
		}
		
	}
	
	
	//nidoh soar loader mod
	private void createPublishStatus(String colID) throws DfException{
		if(colID == null || colID.equals("") || colID == ""){
			LoaderLog.info("Tried to create an entry into soar_published_files table with a null collection ID. How did this happen or how to recover? Continuing per usual for now..");
			//insert the entry - default MD5 value of 0 and colID value of dummy string since value can't be null in the table
			String insertQry = "insert into dm_dbo.soar_published_files(collection_name, remote_server_path, checkvalue) values ('colValue', '" + remoteFile + "', '0')";
			IDfCollection insertRows = dbService.getResults(insertQry, "checkPublishStatus");
		} else {
			//insert the entry - default MD5 value of 0
			String insertQry = "insert into dm_dbo.soar_published_files(collection_name, remote_server_path, checkvalue) values ('" + colID + "', '" + remoteFile + "', '0')";
			IDfCollection insertRows = dbService.getResults(insertQry, "checkPublishStatus");
		}
		
	}
	
	
	//nidoh soar loader mod
	protected String getServerMD5(){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			if(md == null){ // somehow the object did not instantiate return that the file needs to publish as can't find MD5 for it anyway
				LoaderLog.error("Could not instantiate the MD5 checksum object. Going to publish " + remoteFile + " by default action.");
				return "DummyMD5";
			}
			
			//remoteFile won't be null as those checks were already made in the calling function matchingMD5 above
	        FileInputStream fis = new FileInputStream(remoteFile);
	        
	        byte[] byteBuffer = new byte[1024]; //arbitrary
	        int progress = 0; 
	        
	        while ((progress = fis.read(byteBuffer)) != -1) {
	          md.update(byteBuffer, 0, progress);
	        };
	        byte[] mdbytes = md.digest();

	        //now that the MD5 is calculated, need to convert it to a "readable" string format
	        String MD5 = "";
	        for (int i = 0; i < mdbytes.length; i++) {
	          MD5.concat(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        return MD5;
	        
		} catch (NoSuchAlgorithmException e){
			LoaderLog.error("Error finding MD5 algoritm for calculating checksum of file. Unable to find MD5 of physical file so going to re-publish " + remoteFile + " by default action.");
			LoaderLog.error(e.getMessage());
			return "DummyMD5";
		} catch (IOException e){
			LoaderLog.error("Error finding MD5 algoritm for calculating checksum of file. Unable to read physical file so going to re-publish " + remoteFile + " by default action.");
			LoaderLog.error(e.getMessage());
			return "DummyMD5";
		} catch (Exception e){
			LoaderLog.error("Error finding MD5 algoritm for calculating checksum of file. Going to re-publish " + remoteFile + " by default action.");
			LoaderLog.error(e.getMessage());
			return "DummyMD5";
		}
	}

	/** 
	 *
	 * This method will check if the sw_file is been published by multiple collections
	 * @param dbObj
	 * @return
	 * @throws ProcessingException 
	 * @throws DfException
	 */
	public static boolean isPublishedByMultiCols(String remoteFile, String collection_id) throws ProcessingException{
		IDfCollection results = null;
		IDfSession session=null;
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT count(DISTINCT collection_name) as rec_count FROM dm_dbo.SOAR_PUBLISHED_FILES where remote_server_path='"+remoteFile+"' and NOT collection_name='"+collection_id+"'");
		
		try {
			session = ConnectionPool.getDocbaseSession();
			results =DocbaseUtils.executeQuery(session, sb.toString(),IDfQuery.DF_READ_QUERY, "isPublishedByMultiCols");
			if (results != null && results.next() && results.getInt("rec_count")>0) {
				LoaderLog.info("Remote File "+remoteFile+" is being published by other collections");
				return true;
			}
		} catch (DfException e) {
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot get publishing entry for : "+remoteFile, e));
			//throw new ProcessingException(e);
		} finally {
			DocbaseUtils.closeResults(results);
			try {
				ConnectionPool.releaseDocbaseSession(session);
			} catch (DfException e) {
				LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Unable to close Session in removePublishedInfo()", e));
				throw new ProcessingException(e);
			}
		}
		return false;
	}
	/**
	 * Method to remove Publishing information for SWFile to be deleted
	 * @param collectionId
	 * @param swfileId
	 * @throws DfException
	 * @throws ProcessingException 
	 */
	public static void removePublishedInfo(String collectionId, String remoteFile) throws ProcessingException{
		IDfCollection results=null;
		IDfSession session=null;
		String delQuery ="DELETE FROM dm_dbo.SOAR_PUBLISHED_FILES where remote_server_path='"+remoteFile+"' and collection_name='"+collectionId+"'";
		LoaderLog.debug("removePublishedInfo)"+delQuery);
		try {
			session = ConnectionPool.getDocbaseSession();
			results = DocbaseUtils.executeQuery(session, delQuery, IDfQuery.DF_EXEC_QUERY, "removePublishedInfo :"+remoteFile);
			LoaderLog.debug("REMOVED PUBLISHING INFO :"+collectionId +" - "+ remoteFile);
			if (results == null || !results.next()) {
				LoaderLog.error("Failed to delete publishing info: "+remoteFile);
			}
		} catch (DfException e) {
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot get delete entry for : "+remoteFile, e));
			//throw new ProcessingException(e); Should not throw this as the exception is thrown even if no record found
		}finally {
			DocbaseUtils.closeResults(results);
			try {
				ConnectionPool.releaseDocbaseSession(session);
			} catch (DfException e) {
				LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Unable to close Session in removePublishedInfo()", e));
				throw new ProcessingException(e);
			}
		}
	}	
}
