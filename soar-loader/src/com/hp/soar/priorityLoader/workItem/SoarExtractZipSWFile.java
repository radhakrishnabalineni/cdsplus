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

import org.apache.commons.vfs.FileSystemException;





import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.PendingDeleter;
import com.jcraft.jsch.jce.MD5;

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
	 * @param destinationAdapter TODO
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
	public SoarExtractZipSWFile(DestinationAdapter destinationAdapter,
			SoarExtractionDBService dbService, String objectId, IDfDocument dbObj,
			String updateType, Date lastExtractionTime, String eventId,
			String collectionId, String itemId) throws DfException,
			IOException {
		super(destinationAdapter, dbService, objectId, dbObj, updateType,
				lastExtractionTime, eventId, collectionId, itemId);
		
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
				if (!ze.isDirectory()) { // nidoh -- what if the zip contains a directory of files?!?
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
	public void doImmediateDelete(DestinationAdapter destinationAdapter, boolean inSubscription, HashSet<String> publishedLocations) throws IOException, ProcessingException {
		if (((updateType == ITEM_IMMEDIATE_DELETED) || (!inSubscription && published)) && !completed) {
			for(String fName : remoteFiles) {
				if (!publishedLocations.contains(fName)) {
					// This is supposed to delete immediately so execute it
					if(!isPublishedByMultiCols(remoteFile, collectionId)){
					try {
						long startTime = System.currentTimeMillis();
						if (fName != null) {
							LoaderLog.info("IMMEDIATE DELETE "+
								(updateType != ITEM_IMMEDIATE_DELETED ? "(should not publish)":"")+" swfile: "+fName);
						
							destinationAdapter.remove(fName);					
							LoaderLog.info("IMMEDIATE DELETE finished :"+fName+ "  <"+(System.currentTimeMillis() - startTime)+">");
						}
					} catch (IOException e) {
						LoaderLog.error("FAILED ImmediateDelete of :"+remoteFile+" : "+e.getClass()+" - "+e.getMessage());
						throw e;
					}
				}
			}
			//Remove Software File Info from publish table
			removePublishedInfo(collectionId, remoteFile);
			}
			completed = true;
			// turn of the published flag
			setPublished(false);
		}
	}
	
	@Override
	public void doUpdate(DestinationAdapter destination) throws IOException, ProcessingException {		
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
							//LoaderLog.info("UPDATE ZIP ["+ count++ + " of " + numFiles + "] "+(updateType == ITEM_UPTODATE ? "(republishing)" : "")+" swFile : " + name + " to " + remoteFile);
							long startTime = System.currentTimeMillis();
							InputStream is = zf.getInputStream(ze);
							if (is != null) {
								//sftpDestination.put(remoteFile, is);
								try {
									//nidoh soar loader mods
									if(!md5match && !MD5Check()){ // we did the md5 check on the parent zip already. now we have to do a separate check on the individual file as well (minimal publishing mechanism)
										destination.putZip(remoteFile, new ZipFileSource(zf, ze), count, numFiles, (updateType == ITEM_UPTODATE ? "(republishing)" : ""));
										updatePublishTable(true);
									} else {
										LoaderLog.info("NO UPDATE swFile no changes to content: "+name);
									}
									}catch (FileNotFoundException fnfe) {
									LoaderLog.info("UPDATE swFile No Entry for file : "+name);
									}
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
	protected boolean remoteFileExists(DestinationAdapter destination) throws DfException, ZipException, IOException {
		// check each file to see if it is there, if it isn't return false
		for(String rFile : remoteFiles) {
			if (!destination.exists(rFile)) {
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
	protected boolean remoteFileDoesNotExist(DestinationAdapter destinationAdapter) throws FileNotFoundException,
			ZipException, DfException, IOException {
		// check each file to see if it is not there, if it is return false
		for(String rFile : remoteFiles) {
			if (destinationAdapter.exists(rFile)) {
				return false;
			}
		}
		// all of the files are there so return true
		return true;
	}
	
	@Override
	protected void setupFile(DestinationAdapter destinationAdapter, String eventId,
			String collectionId, String itemId) throws DfException,
			FileNotFoundException, ZipException, IOException {
		// setup the remote file information
		setupRemoteFile();
		
		// checkout the zip file to get the remote destinations		
		checkoutLocalFile(eventId, collectionId, itemId);
		
		// get the set of remote files from the zip file
		getRemoteFiles(localFile);
		
		published = isPublished(destinationAdapter);
		
		if (published || (updateType == ITEM_DELETED || updateType == ITEM_IMMEDIATE_DELETED)) {
			// remove the local file as it isn't needed
			localFile.delete();
		}
		
	}
	
	
	//nidoh soar loader mod
	protected boolean MD5Check(){
		//go to the server and find the file from the zip's MD5 and compare it with what we have in the published table to see if we need to publish this specific file again
		try{
			zipElementMD5 = getServerMD5();
			
			String compare_md5_query = "select checkvalue from dm_dbo.soar_published_files where remote_server_path='" + remoteFile + "'";
			String storedMD5 = dbService.getColValueAsString(compare_md5_query, "checkvalue", "MD5Check");
			
			if(zipElementMD5 != null && storedMD5 != null){ // make sure we won't be working with bad data
				if(storedMD5.equals(zipElementMD5) || storedMD5 == zipElementMD5){
					//MD5s are the same, no need to publish again
					LoaderLog.info("Since MD5 of " + remoteFile + " has not changed since last publish, not re-publishing individual file.");
					return true;
				}
			} else {
				//the MD5s are different - update our table
				LoaderLog.info("Since MD5 of " + remoteFile + " has changed since last publish, re-publishing individual file.");
				String update_checksum_query = "update dm_dbo.soar_published_files object set checkvalue='" + zipElementMD5 + "' where remote_server_path='" + remoteFile + "'";
				dbService.executeUpdate(update_checksum_query, "MD5Check");
				return false;
			}
		}catch(DfException e){
			LoaderLog.error("Inside of MD5Check, error while issuing docbase query --  republishing " + remoteFile + " by default.");
			LoaderLog.error(e.getMessage());
			return false;
		}catch(Exception e){
			LoaderLog.error("Inside of MD5Check, generic error caught --  republishing " + remoteFile + " by default.");
			LoaderLog.error(e.getMessage());
			return false;
		}
		
		//default action if it ever reaches here (by some miracle) would be to publish just to be safe
		return false;
	}
	
	
	
}
