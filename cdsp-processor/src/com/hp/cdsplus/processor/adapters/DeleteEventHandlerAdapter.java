package com.hp.cdsplus.processor.adapters;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProcessorDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.Processor;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.queue.QueueManager;


public class DeleteEventHandlerAdapter implements Adapter{

	public void evaluate(Item item) throws AdapterException, OptionsException, MongoUtilsException {		
		ContentItem cItem = (ContentItem) item;
		Options options = new Options();
		options.setContentType(cItem.getContentType());
		ProcessorDAO procDAO= new ProcessorDAO();
		procDAO.handleDeleteEvent(options);
	}
	
	public static void main(String[] args){
		System.setProperty("CONFIG_LOCATION", "config/processor.properties");
		Processor.initAppProperties();
		try {
			ContentItem item = new ContentItem("soar", System.currentTimeMillis(), new QueueManager());
			new DeleteEventHandlerAdapter().evaluate(item);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
