/**
 * 
 */
package com.hp.cdsplus.processor.service;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;

/**
 * @author kashyaks
 *
 */
public class ReferenceItemServiceHandler implements ServiceHandler {

	/**
	 * 
	 */
	public ReferenceItemServiceHandler() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.hp.cdsplus.processor.service.ServiceHandler#doService(com.hp.cdsplus.processor.item.Item)
	 */
	@Override
	public void doService(Item item) throws MongoUtilsException,
			OptionsException, ProcessException {
		// TODO Auto-generated method stub

	}

}
