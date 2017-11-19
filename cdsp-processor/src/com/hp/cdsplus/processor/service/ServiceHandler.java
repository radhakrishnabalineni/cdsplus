package com.hp.cdsplus.processor.service;

import java.io.IOException;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;


/**
 */
public  interface ServiceHandler {
	/**
	 * Method doService.
	 * @param item Item
	 * @throws MongoUtilsException 
	 * @throws AdapterException 
	 * @throws OptionsException 
	 * @throws ProcessException 
	 * @throws IOException 
	 */
	public void doService(Item item) throws MongoUtilsException, OptionsException, ProcessException, IOException;
}
