package com.hp.soar.priorityLoader.workItem;

import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;

import com.hp.loader.utils.ConfigurationReader;

/**
 * Destination is super class for all destinations.
 * It maintains a "factory" instance for creating destinations 
 * 
 * @author dahlm
 *
 */
public abstract class DestinationAdapter {
	
	// Singleton for the destinations
	private static SFTPDestination sftpDestination = null;
	private static ICMDestination  icmDestination = null;
	

	/**
	 * init configures the SFTPDestination and gets the serverOid for soar
	 * and the ICMDestination
	 * @param config
	 * @throws FileSystemException 
	 */
	public static void initDestinations(ConfigurationReader config) throws FileSystemException {
		 sftpDestination = new SFTPDestination(config);
		 icmDestination = new ICMDestination(config);
	}
	
	abstract public void remove(String remoteFile) throws IOException;
	
	abstract public boolean exists(String remoteFile) throws FileSystemException;
	
	abstract public void putFile(String remoteFile, Source src) throws IOException;
	
	abstract public void putZip(String remoteFile, Source src, int count, int numFiles, String republish) throws IOException;
	
	/**
	 * getInstance returns the destination instance associated with the isICM and if there is a rule
	 * isICM=false  return sftpdestination
	 * isICM=true	return new ICMDestination(ICMRule, icmDestination)
	 * @param isICM
	 * @param ICMRule
	 */
	public static DestinationAdapter getInstance(boolean isICM, String ICMRule ) {
		if (isICM) {
			return new ICMAdapter(ICMRule, icmDestination, sftpDestination);
		} else {
			return new SFTPAdapter(sftpDestination);
		}
	}

	/**
	 * get the server oid for the sftp destination server
	 * @return
	 */
	public static String getServerOid() {
		return sftpDestination.returnServerOid();
	}
	

}
