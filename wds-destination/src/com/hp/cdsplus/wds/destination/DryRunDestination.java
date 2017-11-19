package com.hp.cdsplus.wds.destination;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.transform.Templates;

import org.w3c.dom.Element;

import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;

public class DryRunDestination implements IDestination {
	
	File logFile;
	PrintStream ps;
	File working_directory;
	String loc_path_prefix;
	
	
	public void init(Element root, File workingDir) throws DestinationException {
		loc_path_prefix = root.getAttribute("path_prefix");
		this.working_directory = workingDir;
		// open a file and write the details of what was done.
		if(!this.working_directory.exists()){
			this.working_directory.mkdirs();
		}
		if(logFile == null)
			logFile = new File(this.working_directory,new Long(System.currentTimeMillis()).toString());
		try {
			ps = new PrintStream(logFile);
		} catch (FileNotFoundException e) {
			throw new NonRetryException("File Not Found - "+logFile.getAbsolutePath(), e);
		}
	}

	
	public boolean put(String location, byte[] bytes) throws DestinationException{
		
		location = loc_path_prefix+location;
		ps.println("U "+location);
		/*FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(this.working_directory,location));
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new NonRetryException("File Not Found - "+logFile.getAbsolutePath(), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new NonRetryException("Cannot write to file - "+logFile.getAbsolutePath(), e);
		}*/
		return true;
		
		// decide if you want to disconnect here or keep it open.
	}

	
	public boolean remove(String location) {
		ps.println("D "+loc_path_prefix+location);
		return true;
	}

	
	public boolean finalizeDest(boolean force) {
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
