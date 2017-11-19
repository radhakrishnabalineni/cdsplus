package com.hp.cdsplus.processor.adapters;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProcessorDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.item.Item;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MetadataPersistenceAdapter implements Adapter{
	
	public void evaluate(Item item) throws AdapterException, MongoUtilsException, OptionsException {
		
		Options options = new Options();
		options.setContentType(item.getContentType());
		DBObject filter = new BasicDBObject("processStatus",ProcessStatus.SUB_EVAL_COMPLETE.toString());
		options.setQuery(filter);
		ProcessorDAO procDAO = new ProcessorDAO();
		procDAO.handleUpdateEvent(options);
	}
}
