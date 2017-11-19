package com.hp.cdsplus.wds.destination;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Templates;

import org.w3c.dom.Element;

import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;

public class FileSystemDestination implements IDestination {

	private File destination; 

	
	public void init(Element root, File workingDir) {
		String destString = root.getAttribute( "dir");
		destination = new File(destString); 
		if (!destination.exists() ) {
			destination.mkdirs(); 
		}
	}
	
	public boolean put(String location, byte[] bytes) throws DestinationException {
		File file = new File( destination, location );
		File parentFile = file.getParentFile();
		if ( !parentFile.exists() ) {
			parentFile.mkdirs(); 
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream( file );
		} catch (FileNotFoundException e) {
			throw new NonRetryException("File not found - "+file.getAbsolutePath(),e);
		}
		BufferedOutputStream bos = new BufferedOutputStream( fos );
		try {
			bos.write(bytes);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			throw new NonRetryException("Exception while trying to write content to file - "+file.getAbsolutePath(),e);
		}
		return true;
	}

	/**
	 * @throws NonRecoverableException 
	 * 
	 */
	
	public boolean remove(String location) throws DestinationException {
		File file = new File( destination, location );
		if ( file.exists() && !file.delete() ) {
			throw new NonRetryException("Exception while trying to delete file - "+file.getAbsolutePath(),new Throwable().fillInStackTrace());
		}
		return true;
	}  
	public boolean finalizeDest(boolean force) { 
		return true;
	}

	/**
	 * every store to a fileSystem should be unique.  They can run in parallel.
	 */
	public boolean needStoreSync() {
		return false;
	}

	public boolean remove(String location, Templates sTemplates)
			throws DestinationException {
		// TODO Auto-generated method stub
		return false;
	}
}
