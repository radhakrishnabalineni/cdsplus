package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.ZipException;

import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.PendingDeleter;
import com.hp.soar.priorityLoader.utils.VirusScanException;

public class SoarExtractSWFile extends SoarExtractElement {

//	public static SFTPDestination sftpDestination = null;
	// ICM changes
	public static Destination sftpDestination;
	public static Destination icmDestination;
	protected Destination destination = null;	// target destination for this object
	
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
	// file name to send to icm request
	protected String icmFileName;
	
	// collectionId, itemId and objectId are saved for delete events
	protected String collectionId;
	protected String itemId;
	protected boolean published;	// when true, the file is marked as externally available
	protected boolean externallyAvailable = false; // when true, the file is actually on the ftp server
	
	/**
	 * 
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
	public SoarExtractSWFile(SoarExtractionDBService dbService, String objectId, boolean icmDriver, String icmRuleId, IDfDocument dbObj, String updateType,
			Date lastExtractionTime, String eventId, String collectionId, String itemId) throws DfException, ZipException, IOException {
		super(dbService, objectId, icmDriver, icmRuleId, updateType);

		this.collectionId = collectionId;
		this.itemId = itemId;
		
		this.dbObj = dbObj;
		
		setFileEvent(lastExtractionTime);
		published = published();
		
		setupFile(eventId, collectionId, itemId);
		
//		setupRemoteFile();
//		if (updateType == ITEM_MODIFIED || updateType == ITEM_UPTODATE && published) {
//			// the file should be there see if it is
//			if (!remoteFileExists(eventId,collectionId,itemId)) {
//				// the file is marked as published but isn't available on the FTP server so mark it as not published so it will publish again
//				published = false;
//			}
//		}
//		setupLocalFiles(eventId, collectionId, itemId);
		
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
	protected boolean remoteFileExists() throws FileNotFoundException, DfException, ZipException, IOException {
		try {
			if (remoteFile != null && !sftpDestination.exists(remoteFile)) {
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
	 * @return
	 * @throws FileNotFoundException
	 * @throws ZipException
	 * @throws DfException
	 * @throws IOException
	 */
	protected boolean remoteFileDoesNotExist() throws FileNotFoundException, ZipException, DfException, IOException {
		return !remoteFileExists();
	}
	
	/**
	 * isPublished checks the remote system to see if the files are actually there
	 * @return
	 * @throws IOException 
	 * @throws DfException 
	 * @throws ZipException 
	 * @throws FileNotFoundException 
	 */
	protected boolean isPublished() throws FileNotFoundException, ZipException, DfException, IOException {
		// at this point, published holds the state of the file in the DB
		if (updateType == ITEM_UPTODATE) {
			if (published) {
				// The file is supposed to be there so see if the file is there 
				return remoteFileExists();
			} else {
				return false;
			}
		} else if ((updateType == ITEM_IMMEDIATE_DELETED) || (updateType == ITEM_DELETED)) {
			if (!published) {
				// see if the file is not there
				return !remoteFileDoesNotExist();
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
	 */
	public void doImmediateDelete(boolean inSubscription, HashSet<String> publishedLocations) throws IOException {
		if (((updateType == ITEM_IMMEDIATE_DELETED) || (!inSubscription && published)) && !completed) {
			if (published && !publishedLocations.contains(remoteFile)) {
				// This is supposed to delete immediately so execute it
				try {
					long startTime = System.currentTimeMillis();
					if (remoteFile != null) {
						LoaderLog.info("IMMEDIATE DELETE "+
								(updateType != ITEM_IMMEDIATE_DELETED ? "(should not publish)":"")+" swfile: "+remoteFile);
						// ICM changes
						sftpDestination.executeDelete(remoteFile, null, null);					
						icmDestination.executeDelete(remoteFile, icmFileName, serverDir);
						LoaderLog.info("IMMEDIATE DELETE finished :"+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
					}
					
				} catch (IOException e) {
					LoaderLog.error("FAILED ImmediateDelete of :"+remoteFile+" : "+e.getClass()+" - "+e.getMessage());
					throw e;
				}
			}
			
			completed = true;
			// flag that this swFile is no longer published (It may have been overwritten by a newer version but this one is gone)
			setPublished(false);
		}
	}
	
	/**
	 * deleteIcmSoftwareFromSFTP removes the swFile from the remote server once found licensed
	 * @throws IOException 
	 * @throws Exception 
	 */
	protected void deleteIcmSoftwareFromSFTP() throws IOException {
			// This is supposed to delete immediately
			try {
				long startTime = System.currentTimeMillis();
				if (remoteFile != null) {
					LoaderLog.info("IMMEDIATE SFTP DELETE OF ICM FILE "+
							(updateType != ITEM_IMMEDIATE_DELETED ? "(should not publish)":"")+" swfile: "+ remoteFile);	
					destination.executeDelete(remoteFile, null, null);
					LoaderLog.info("IMMEDIATE SFTP DELETE OF ICM protected file finished :"+ remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
				}				
			} catch (IOException e) {
				LoaderLog.error("FAILED IMMEDIATE SFTP DELETE OF ICM protected file :"+ remoteFile+" : "+e.getClass()+" - "+e.getMessage());
				throw e;
			}
			completed = true;
	}
	/**
	 * doUpdate sends the swFile to the remote sftp and/or icm (WS) server
	 * @param published Indicates if the swFile has been published
	 * @param shouldPublish indicates the swFile should be published
	 * @throws IOException
	 * @throws ProcessingException 
	 */
	public void doUpdate() throws IOException, ProcessingException {		
		if ((updateType == ITEM_NEW || updateType == ITEM_MODIFIED || (updateType == ITEM_UPTODATE && !published)) && !completed) {
			if (remoteFile == null) {
				LoaderLog.warn("UPDATE swFile from: " + localFile+ " has no destination");
				completed = true;
				return;
			}
			
			// This swFile is now updating so take it out of the pending delete list
			PendingDeleter.removeSwFile(remoteFile);
			LoaderLog.info("UPDATE "+(updateType == ITEM_UPTODATE ? "(republishing)" : "")+" swFile from: " + localFile + " to " + remoteFile);
			executeUpdateAndDelete();
//			try {
//				LoaderLog.info("UPDATE "+(updateType == ITEM_UPTODATE ? "(republishing)" : "")+" swFile from: " + localFile + " to " + remoteFile);
//				long startTime = System.currentTimeMillis();
				// ICM changes
	//				sftpDestination.put(remoteFile, new FileInputStream(localFile));
//				executeUpdateAndDelete();
				
//				completed = true;
//				LoaderLog.info("SFTP UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
				
//				setPublished(true);
	//			completed = true;
//			} catch (IOException e) {
//				LoaderLog.error("FAILED UPDATE/DELETE of: "+remoteFile+" : "+e.getClass()+" - "+ e.getMessage());
//				throw e;
//			}
		}
	}

		protected void executeUpdateAndDelete() throws IOException, ProcessingException {
			long startTime = 0;
			if (super.isIcmDriver()) {
				if (icmDestination.getSoarIcmIntegrationOn().equalsIgnoreCase("Y")) {
					icmDestination.setIcmRule(super.getIcmRule());
					startTime = System.currentTimeMillis();
				LoaderLog.info("before execute update method");
					icmDestination.executeUpdate(null, null, icmFileName, serverDir, localFile.getAbsolutePath());
					setPublished(true);
					completed = true;
					LoaderLog.info("ICM UPDATE of protected swFile finished: "+remoteFile+ "  <"+(System.currentTimeMillis() - startTime)+">");
					try {
						startTime = System.currentTimeMillis();
						sftpDestination.executeUpdate(remoteFile, new FileInputStream(localFile), null, null, null);
						setPublished(true);
						completed = true;
						LoaderLog.info("SFTP UPDATE of protected swFile finished (ICM integration ON): "+remoteFile+ "  <"+(System.currentTimeMillis() - startTime)+">");
					} catch (FileNotFoundException fe) {
						LoaderLog.error("FAILED SFTP UPDATE of: "+remoteFile+" : "+fe.getClass()+" - "+ fe.getMessage());
						throw fe;
					} catch (IOException ie) {
						LoaderLog.error("FAILED SFTP UPDATE of: "+remoteFile+" : "+ie.getClass()+" - "+ ie.getMessage());
						throw ie;
					}
				} else {
					icmDestination.setIcmRule(super.getIcmRule());
					startTime = System.currentTimeMillis();
					icmDestination.executeUpdate(null, null, icmFileName, serverDir, localFile.getAbsolutePath());
					setPublished(true);
					completed = true;
					LoaderLog.info("ICM UPDATE of protected swFile finished: "+remoteFile+ "  <"+(System.currentTimeMillis() - startTime)+">");
					destination = sftpDestination;
					deleteIcmSoftwareFromSFTP();
				}
			} else {
				try {
					startTime = System.currentTimeMillis();
					sftpDestination.executeUpdate(remoteFile, new FileInputStream(localFile), icmFileName, serverDir, localFile.getAbsolutePath());
					setPublished(true);
					completed = true;
					LoaderLog.info("SFTP UPDATE of swFile finished: "+remoteFile+ "  <"+(System.currentTimeMillis() - startTime)+">");
				} catch (FileNotFoundException fe) {
					LoaderLog.error("FAILED SFTP UPDATE of: "+remoteFile+" : "+fe.getClass()+" - "+ fe.getMessage());
					throw fe;
				} catch (IOException ie) {
					LoaderLog.error("FAILED SFTP UPDATE of: "+remoteFile+" : "+ie.getClass()+" - "+ ie.getMessage());
					throw ie;
				}
				destination = icmDestination;
				//Debug
//				icmDestination.setIcmRule(super.getIcmRule());
//				destination.executeUpdate(remoteFile, new FileInputStream(localFile), icmFileName, serverDir, localFile.getAbsolutePath());
				// End debug
				startTime = System.currentTimeMillis();
				destination.executeDelete(remoteFile, icmFileName, serverDir);
				LoaderLog.info("ICM DELETE OF unprotected swFile finished : "+ remoteFile+ "  <"+(System.currentTimeMillis() - startTime)+">");			
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
			updateType = ITEM_NEW;
		} else if ((lastExtractionTime != null) && updateDate.after(lastExtractionTime)) {
			// if updated after last extraction time then it was modified.
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
		// ICM changes
		icmFileName = contentFileName;
		for (int i=0; i<serverNameOids.length && i < relDirs.length; i++) {
			if (serverNameOids[i].equals(sftpDestination.getServerOid())) {
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
	 * @param eventId
	 * @param collectionId
	 * @param itemId
	 * @throws DfException 
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws FileNotFoundException 
	 */
	protected void setupFile(String eventId, String collectionId, String itemId) throws DfException, FileNotFoundException, ZipException, IOException {
		setupRemoteFile();

		published = isPublished();
		
		if (!published && !(updateType == ITEM_DELETED || updateType == ITEM_IMMEDIATE_DELETED)) {
			// checkout the file
			checkoutLocalFile(eventId, collectionId, itemId);
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
	 */
	public static void initDestinations(ConfigurationReader config) throws FileSystemException {
//		sftpDestination = new SFTPDestination(config);
		// ICM changes
		sftpDestination = DestinationFactory.buildDestination(config, DestinationType.SFTP);
		icmDestination = DestinationFactory.buildDestination(config, DestinationType.ICM);
	}
	
	public static SoarExtractSWFile getSWFile(SoarExtractionDBService dbService, boolean icmDriver, String icmRuleId, String objectId, String updateType,
			Date lastExtractionTime, String eventId, String collectionId, String itemId) throws DfException, ZipException, IOException {
		
		IDfDocument dbObj = dbService.getDocumentByQualification("sw_file where r_object_id='"+ objectId + "'");
		if (dbObj == null) {
			throw new IllegalArgumentException("Collection "+collectionId+", item "+itemId+" is missing swFile: "+objectId);
		}
		String fileTypeOid = dbObj.getString("file_type_oid");
		
		// if this is an unzip file type, return a new unzip swFile
		if (fileTypeOid != null && fileTypeOid.equals(unzipFileTypeOid)) {
			return new SoarExtractZipSWFile(dbService, objectId, icmDriver, icmRuleId, dbObj, updateType, lastExtractionTime, eventId, collectionId, itemId);
		} else {
			return new SoarExtractSWFile(dbService, objectId, icmDriver, icmRuleId, dbObj, updateType, lastExtractionTime, eventId, collectionId, itemId);
		}
	}
}
