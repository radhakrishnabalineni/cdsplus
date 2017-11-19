package com.hp.soar.priorityLoader.workItem;

import org.apache.commons.vfs.FileSystemException;

import com.hp.loader.utils.ConfigurationReader;

public class DestinationFactory {
	public static Destination buildDestination (ConfigurationReader config, DestinationType destType) throws FileSystemException {
		Destination destination = null;
		switch (destType){
		case ICM:
			destination = new ICMWSDestination(config, destType);
			break;
		case SFTP:
			destination = new SFTPDestination(config, destType);
			break;
		}
		return destination;
	}
}
