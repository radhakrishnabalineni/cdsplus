package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSource extends Source {
	File localFileName;
	
	public FileSource(File localFileName) {
		super();
		this.localFileName = localFileName;
	}

	@Override
	public String getName() {
		// return the localFileName
		return localFileName.getAbsolutePath();
	}
	
	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(localFileName);
	}

}
