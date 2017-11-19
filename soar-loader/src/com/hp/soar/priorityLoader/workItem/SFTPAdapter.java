package com.hp.soar.priorityLoader.workItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileSystemException;

import com.hp.soar.priorityLoader.utils.LoaderLog;

public class SFTPAdapter extends DestinationAdapter {
	private SFTPDestination destination;

	public SFTPAdapter(SFTPDestination destination) {
		super();
		this.destination = destination;
	}

	@Override
	public void remove(String location) throws IOException {
		// pass through to SFTP destination call
		destination.remove(location);		
	}

	@Override
	public boolean exists(String location) throws FileSystemException {
		// pass through to SFTP destination call
		return destination.exists(location);
	}
	
	@Override
	public void putZip(String remoteFile, Source src, int count,
			int numFiles, String republish) throws IOException {
		LoaderLog.info("UPDATE ZIP ["+ count + " of " + numFiles + "] "+republish +" swFile : " + src.getName() + " to " + remoteFile);
		long startTime = System.currentTimeMillis();
		InputStream is = src.getInputStream();
		try {
			destination.put(remoteFile, is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		LoaderLog.info("UPDATE ZIP finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
	}
	
	@Override
	public void putFile(String remoteFile, Source src) throws IOException {
		LoaderLog.info("UPDATE swFile from: "+src.getName()+" to: "+remoteFile);
		long startTime = System.currentTimeMillis();
		
		// create the input stream to read from
		InputStream is = src.getInputStream();
		try {
			destination.put(remoteFile, is);
			LoaderLog.info("UPDATE swFile finished: "+remoteFile + "  <"+(System.currentTimeMillis() - startTime)+">");
		} finally {
			if (is != null) {
				// close the input stream
				is.close();
			}
		}
	}
	
	
}
