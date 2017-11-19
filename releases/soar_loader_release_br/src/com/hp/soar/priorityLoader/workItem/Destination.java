package com.hp.soar.priorityLoader.workItem;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileSystemException;
import org.apache.http.client.ClientProtocolException;

import com.hp.loader.utils.ConfigurationReader;


public abstract class Destination {
	
	private DestinationType destType = null;
		
	public Destination (ConfigurationReader config, DestinationType destType) throws FileSystemException {
		this.destType = destType;
		initDestination(config);
	}
	
	protected abstract void initDestination(ConfigurationReader config) throws FileSystemException;

	protected abstract void executeUpdate(String remoteFile, InputStream is, String icmFileName, String icmServerDir, String localFileAbsolutePath)  throws ClientProtocolException, IOException;

	public abstract void executeDelete(String remoteFile, String icmFileName, String icmServerDir) throws ClientProtocolException, IOException;
	
	protected abstract boolean exists(String remoteFile) throws FileSystemException;

	protected abstract void setIcmRule(String icmRule);

	protected abstract String getSoarIcmIntegrationOn();

	protected abstract String getServerOid();
}
