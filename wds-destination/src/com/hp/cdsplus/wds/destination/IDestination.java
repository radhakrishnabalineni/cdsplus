package com.hp.cdsplus.wds.destination;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import javax.xml.transform.Templates;

import com.hp.cdsplus.wds.exception.DestinationException;


public interface IDestination {

	public void init(Element root,File workingDir) throws DestinationException;

	/**
	 * Stores the bytes (as is) at the location specified the location is left
	 * undefined because location's meaning can vary based on implementation.
	 * 
	 * 
	 * @param location
	 *          path or something that represents the destination
	 * @param is
	 *          bytes to be written
	 */
	public boolean put(String location, byte[] bytes) throws DestinationException;

	/**
	 * deletes the file at a particular location... again the location only has
	 * meaning to the implementing object
	 * 
	 * @param location
	 *          path or something that represents the destination
	 * @throws IOException
	 */
	
	public boolean remove(String location) throws DestinationException;
	
	/***
	 * Method added to provide flexibility to apply style sheet for deleted documents
	 * */
	public boolean remove(String location, Templates sTemplates) throws DestinationException;

	public boolean finalizeDest(boolean force) throws DestinationException;
	
	/**
	 * needStoreSync should be true for destinations that should store one item at a time
	 * @return
	 */
	public boolean needStoreSync();
}
