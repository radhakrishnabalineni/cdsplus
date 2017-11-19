package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.PendingDeleter;

/**
 * this class handles zip files for publication to ftp.hp.com.  When an unzip filetype is presented, this class will publish/delete
 * the zip file.
 * @author dahlm
 *
 */
public class SoarExtractZipSWFile extends SoarExtractSWFile {

	private String[] remoteFiles;
	
	/**
	 * 
	 * @param dbService
	 * @param objectId
	 * @param dbObj
	 * @param updateType
	 * @param lastExtractionTime
	 * @param eventId
	 * @param collectionId
	 * @param itemId
	 * @throws DfException
	 * @throws IOException 
	 */
	public SoarExtractZipSWFile(SoarExtractionDBService dbService,
			String objectId, boolean icmDriver, String icmRuleId, IDfDocument dbObj, String updateType,
			Date lastExtractionTime, String eventId, String collectionId,
			String itemId) throws DfException,
			IOException {
		super(dbService, objectId, icmDriver, icmRuleId, dbObj, updateType, lastExtractionTime,
				eventId, collectionId, itemId);
		if (remoteFiles == null) {
			// This isn't an update type so it must be a delete.  Get the remote files so 
			// they can be checked for publishing state
			
		}
		
	}
	
	/**
	 * getRemoteFiles gets the entries in the zip file to identify what it will updated/delete from ftp.hp.com
	 * @param file
	 * @throws IOException 
	 * @throws ZipException 
	 */
	private void getRemoteFiles(File file) throws ZipException, IOException {
		ArrayList<String> remoteFileList = new ArrayList<String>();
		
		ZipFile zf = null; 
		try {
			zf = new ZipFile(file);
			String fName = null;
			for (Enumeration<?> e = zf.entries(); e.hasMoreElements();) {
				ZipEntry ze = (ZipEntry) e.nextElement();	
				if (!ze.isDirectory()) {
					fName = serverDir + file_separator + ze.getName();
					remoteFileList.add(fName);
				}
			}
			remoteFiles = remoteFileList.toArray(new String[remoteFileList.size()]);
		} finally {
			if (zf != null) {
				try {
					zf.close();
				} catch (IOException e) {
					LoaderLog.error("Failed to close zip file: "+e.getMessage());
				}
			}
		}		
	}

	@Override
	public void doDelete(HashSet<String> publishedLocations) {
		if ((updateType == ITEM_DELETED)  && !completed) {
			for(String fName : remoteFiles) {
				if (!publishedLocations.contains(fName)) {
					// schedule this swFile to be deleted at some future time
					PendingDeleter.addSwFileToDelete(fName, collectionId, itemId, objectId);
				}
			}
			completed = true;
			// set that the files have been deleted
			setPublished(false);
		}
	}
	
	@Override
	public void doImmediateDelete(boolean inSubscription, HashSet<String> publishedLocations) throws IOException {
		if (((updateType == ITEM_IMMEDIATE_DELETED) || (!inSubscription && published)) && !completed) {
			for(String fName : remoteFiles) {
				if (!publishedLocations.contains(fName)) {
					// This is supposed to delete immediately so execute it
					try {
						long startTime = System.currentTimeMillis();
						if (fName != null) {
							LoaderLog.info("IMMEDIATE DELETE "+
								(updateType != ITEM_IMMEDIATE_DELETED ? "(should not publish)":"")+" swfile: "+fName);
							// ICM changes
							// sftpDestination.remove(fName);
							sftpDestination.executeDelete(fName, null, null);					
							LoaderLog.info("IMMEDIATE DELETE finished :"+fName+ "  <"+(System.currentTimeMillis() - startTime)+">");
						}
					} catch (IOException e) {
						LoaderLog.error("FAILED ImmediateDelete of :"+remoteFile+" : "+e.getClass()+" - "+e.getMessage());
						throw e;
					}
				}
			}
			completed = true;
			// turn of the published flag
			setPublished(false);
		}
	}
	
	@Override
	public void doUpdate() throws IOException, ProcessingException {		
		if ((updateType == ITEM_NEW || updateType == ITEM_MODIFIED || (updateType == ITEM_UPTODATE && !published)) && !completed) {
			if (remoteFile == null) {
				LoaderLog.warn("UPDATE swFile from: " + localFile + " has no destination");
				completed = true;
				return;
			}
			
			// now load the zip file and publish every one of the entries
			String name = null;
			
			// do archive stuff
			LoaderLog.info("Unpacking archive " + localFile);
			ZipFile zf = null; 
			try {
				zf = new ZipFile(localFile);
				int count=1;
				int numFiles = zf.size();
				for (Enumeration<?> e = zf.entries(); e.hasMoreElements();) {
					ZipEntry ze = (ZipEntry) e.nextElement();	
					if (!ze.isDirectory()) {
						name = ze.getName();
						remoteFile = serverDir + file_separator + name;
						// This swFile is updating now take it out of the pending delete list
						PendingDeleter.removeSwFile(remoteFile);
						try {
							LoaderLog.info("UPDATE ZIP ["+ count++ + " of " + numFiles + "] "+(updateType == ITEM_UPTODATE ? "(republishing)" : "")+" swFile : " + name + " to " + remoteFile);
							long startTime = System.currentTimeMillis();
							InputStream is = zf.getInputStream(ze);
							if (is != null) {
								// ICM changes
//								sftpDestination.put(remoteFile, is);
								super.executeUpdateAndDelete();
								LoaderLog.info("UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
							} else {
								LoaderLog.info("UPDATE swFile No Entry for file : "+name);
							}
						} catch (IOException ioe) {
							LoaderLog.error("FAILED UPDATE of: "+remoteFile+" : "+ioe.getClass()+" - "+ ioe.getMessage());
							throw ioe;
						}
					} else {
						LoaderLog.info("UPDATE ZIP ["+ count++ + " of " + numFiles + "] skip directory: "+ name);
					}
				}
				completed = true;
			} finally {
				if (zf != null) {
					zf.close();
				}
			}
			setPublished(true);
		}
	}
	
	@Override
	public void getPublishedLocations(HashSet<String> publishedLocations) {
		if ((updateType != ITEM_DELETED) && (updateType != ITEM_IMMEDIATE_DELETED)) {
			for(String fName : remoteFiles) {
				publishedLocations.add(fName);
			}
		}
	}
	
	/**
	 * for zip files, the remote file exists if all of the files are there
	 */
	@Override
	protected boolean remoteFileExists() throws DfException, ZipException, IOException {
		// check each file to see if it is there, if it isn't return false
		for(String rFile : remoteFiles) {
			if (!sftpDestination.exists(rFile)) {
				return false;
			}
		}
		// all of the files are there so return true
		return true;
	}
	
	/**
	 * for zip files the remote file does not exist if all of the files are not there
	 */
	@Override
	protected boolean remoteFileDoesNotExist() throws FileNotFoundException,
			ZipException, DfException, IOException {
		// check each file to see if it is not there, if it is return false
		for(String rFile : remoteFiles) {
			if (sftpDestination.exists(rFile)) {
				return false;
			}
		}
		// all of the files are there so return true
		return true;
	}
	
	@Override
	protected void setupFile(String eventId, String collectionId,
			String itemId) throws DfException,
			FileNotFoundException, ZipException, IOException {
		// checkout the zip file to get the remote destinations
		checkoutLocalFile(eventId, collectionId, itemId);
		
		// setup the remote file information
		setupRemoteFile();
		
		// get the set of remote files from the zip file
		getRemoteFiles(localFile);
		
		published = isPublished();
		
		if (published || (updateType == ITEM_DELETED || updateType == ITEM_IMMEDIATE_DELETED)) {
			// remove the local file as it isn't needed
			localFile.delete();
		}
	}
}
