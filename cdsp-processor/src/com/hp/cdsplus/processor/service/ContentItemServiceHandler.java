package com.hp.cdsplus.processor.service;

import java.io.IOException;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.adapters.Adapter;
import com.hp.cdsplus.processor.adapters.MetadataPersistenceAdapter;
import com.hp.cdsplus.processor.adapters.SubscriptionEvaluationAdapter;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;

public class ContentItemServiceHandler implements ServiceHandler {
	
	@Override
	public void doService(Item item) throws ProcessException, MongoUtilsException, OptionsException, IOException {
		SubscriptionEvaluationAdapter subscriptionEvaluator= new SubscriptionEvaluationAdapter();
		
		item.setStatus(ProcessStatus.METADATA_TRANS_COMPLETE,ProcessStatus.SUB_EVAL_STARTED, true);
		subscriptionEvaluator.evaluate(item);
		item.setStatus(ProcessStatus.SUB_EVAL_STARTED,ProcessStatus.SUB_EVAL_COMPLETE,true);
		
		
		/*SubscriptionProcessingAdapter subscriptionprocess = new SubscriptionProcessingAdapter();
		item.setStatus(ProcessStatus.SUB_PROCESSING_STARTED);
		subscriptionprocess.evaluate(item);
		item.setStatus(ProcessStatus.SUB_PROCESSING_COMPLETE);*/
		
		Adapter mdPersistenceAdapter = new MetadataPersistenceAdapter();
		
		item.setStatus(ProcessStatus.DOCUMENT_PERSISTENCE_STARTED);
		mdPersistenceAdapter.evaluate(item);
		item.setStatus(ProcessStatus.SUB_EVAL_COMPLETE,ProcessStatus.DOCUMENT_PERSISTENCE_COMPLETE,false);
	}
}
