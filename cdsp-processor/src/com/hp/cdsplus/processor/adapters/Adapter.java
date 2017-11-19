/**
 * 
 */
package com.hp.cdsplus.processor.adapters;

import java.io.IOException;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;


/**
 * @author kashyaks
 *
 * @version $Revision: 1.0 $
 */
public interface Adapter {

	/**
	 * @param item
	 * @throws ProcessException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 * @throws IOException 
	 */
	public abstract void evaluate(Item item) throws ProcessException, OptionsException, MongoUtilsException, IOException;	
}
