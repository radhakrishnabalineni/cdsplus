package com.hp.cdsplus.wds.destination;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.Templates;

import org.w3c.dom.Element;

import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;
import com.hp.loader.utils.ThreadLog;

public class WildcatDestination implements IDestination {

	private static final String METADIR = "meta_dir";
	private static final String CONTENTDIR= "content_dir";
	private static final String ATTACHDIR= "attach_dir";
	private static final String PDFDIR= "pdf_dir";
	private static final String XML = ".xml";
	private static final String PDF = ".pdf";
	private static final String fileSeparator = System.getProperty("file.separator");
	
	private File metaDir = null;
	private File contentDir = null;
	private File attachDir = null;
	private File pdfDir = null;
	
	HashMap<String, String> extMap = new HashMap<String, String>();
	
	public void init(Element root, File workingDir) throws DestinationException {
		String metaDirStr = root.getAttribute( METADIR );
		if ((metaDirStr == null || metaDirStr.length() == 0)) {
			metaDirStr = null;
			ThreadLog.info(METADIR+" attribute is not defined in destination config. Metadata will not be stored");
		}
		
		String contentDirStr = root.getAttribute( CONTENTDIR );
		if ((contentDirStr == null || contentDirStr.length() == 0)) {
			contentDirStr = null;
			ThreadLog.info(CONTENTDIR+" attribute is not defined in destination config. Content will not be stored");
		}
		
		String attachDirStr = root.getAttribute( ATTACHDIR );
		if ((attachDirStr == null || attachDirStr.length() == 0)) {
			attachDirStr = null;
			ThreadLog.info(ATTACHDIR+" attribute is not defined in destination config. Attachments will not be stored");
		}
		
		String pdfDirStr = root.getAttribute( PDFDIR );
		if ((pdfDirStr == null || pdfDirStr.length() == 0)) {
			pdfDirStr = null;
			ThreadLog.info(PDFDIR+" attribute is not defined in destination config. Pdfs will not be stored");
		}
		
		if (contentDirStr != null) {
			contentDir = new File(contentDirStr); 
			if (!contentDir.exists() ) {
				contentDir.mkdirs(); 
			}
		}

		if (metaDirStr != null ) {
			metaDir = new File(metaDirStr); 
			if (!metaDir.exists() ) {
				metaDir.mkdirs(); 
			}
		}

		if (attachDirStr != null) {
			attachDir = new File(attachDirStr); 
			if (!attachDir.exists() ) {
				attachDir.mkdirs(); 
			}
		}
		
		if (pdfDirStr != null) {
			pdfDir = new File(pdfDirStr); 
			if (!pdfDir.exists() ) {
				pdfDir.mkdirs(); 
			}
		}
	}

	private StringBuffer getLocation(File root, String interDir, String location) {
		if (root == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(root.getAbsolutePath()).append(fileSeparator).append(interDir).append(fileSeparator).append(location);
		return sb;
	}
	
	private String getHashString(String location) {
		int hash = location.hashCode();
		if (hash < 0) 
			hash *= -1;
		// set it to 1 of 1024 directories
		hash %= 1024;
		
		return Integer.toString(hash);
	}
	
	
	/**
	 * @param location name of the file to be stored.  The extenstion is used to determine where to put the file
	 * @bytes content of the file
	 */
	public boolean put(String location, byte[] bytes) throws DestinationException {
		// get the document name out of the location
		int idx = -1;
		if ((idx = location.lastIndexOf("/")) != -1) {
			location = location.substring(idx+1);
		}
		
		// if location has no extension, it is the metadata
		// otherwise it is a type of content <xml, image, pdf>
		StringBuffer fileDir = null;
		if (((idx = location.lastIndexOf('.')) > 0) && (idx < location.length()-1)) {
			File destDir = attachDir;
			String ext = location.substring(idx);
			if (ext.equalsIgnoreCase(XML)) {
				location = location.substring(0, idx);
				destDir = contentDir;
			} else if (ext.equalsIgnoreCase(PDF)) {
				destDir = pdfDir;
			}
			fileDir = getLocation(destDir, getHashString(location), location);
		} else {
			// received metadata (xml withouth extension)
			fileDir = getLocation(metaDir, getHashString(location), location);
		}
		
		if (fileDir == null) {
			// This file isn't supposed to store so return that it did and go on.
			return true;
		}
		
		int fileLen = fileDir.length();
		
		fileDir.append(".tmp");

		File dest = new File(fileDir.toString());
		File parent = dest.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new NonRetryException("Failed to create directory "+parent.getAbsolutePath());
			}
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream( dest );
		} catch (FileNotFoundException e) {
			throw new NonRetryException("Could not create file - "+dest.getAbsolutePath(),e);
		}
		BufferedOutputStream bos = new BufferedOutputStream( fos );
		try {
			bos.write(bytes);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			throw new NonRetryException("Exception while trying to write content to file - "+dest.getAbsolutePath(),e);
		}
		
		fileDir.setLength(fileLen);
		File finalFile = new File(fileDir.toString());
		if (finalFile.exists()) {
			finalFile.delete();
		}
		dest.renameTo(finalFile);
		return true;		
	}


	public boolean remove(String location) throws DestinationException {
		// get the document name out of the location
		int idx = -1;
		if ((idx = location.lastIndexOf(fileSeparator)) != -1) {
			location = location.substring(idx+1);
		}
		
		// if location has no extension, it is the metadata
		// otherwise it is a type of content <xml, image, pdf>
		StringBuffer fileDir = null;
		if (((idx = location.lastIndexOf('.')) < 0)) {
			// received metadata (xml withouth extension)
			fileDir = getLocation(metaDir, getHashString(location), location);
		} else {
			// don't delete other supporting files at this time
			return true;
		}
		
		if (fileDir == null ) {
			// This isn't being stored
			return true;
		}
		
		File remFile = new File(fileDir.toString());
		if (remFile.exists()) {
			return remFile.delete();
		}
		
		// file is already gone 
		return true;
		
	}

	/**
	 * All docs are written as part of put
	 */
	public boolean finalizeDest(boolean force) throws DestinationException {
		return true;
	}

	public boolean needStoreSync() {
		return false;
	}

	public boolean remove(String location, Templates sTemplates)
			throws DestinationException {
		// TODO Auto-generated method stub
		return false;
	}

}
