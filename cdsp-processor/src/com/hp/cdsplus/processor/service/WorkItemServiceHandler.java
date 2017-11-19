package com.hp.cdsplus.processor.service;

import java.io.IOException;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.adapters.CGSRulesAdapter;
import com.hp.cdsplus.processor.adapters.HierarchyExpansionAdapter;
import com.hp.cdsplus.processor.adapters.MarketingFastXMLCreationAdapter;
import com.hp.cdsplus.processor.adapters.MetadataTransformationAdapter;
import com.hp.cdsplus.processor.adapters.SupportFastXMLCreationAdapter;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.exception.ServiceHandlerException;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;

/**
 */
public class WorkItemServiceHandler implements ServiceHandler{
	
	private HierarchyExpansionAdapter hierEvaluator;
	private MetadataTransformationAdapter mdTrEvaluator;
	private MarketingFastXMLCreationAdapter marketingFastXMLCreationAdapter;
	private SupportFastXMLCreationAdapter supportFastXMLCreationAdapter;
	
	public WorkItemServiceHandler(){
		this.hierEvaluator = new HierarchyExpansionAdapter();
		this.mdTrEvaluator = new MetadataTransformationAdapter();
	}
	
	/**
	 * Method doService.
	 * @param item Item
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 * @throws ProcessException 
	 * @throws IOException 
	 * @see com.hp.cdsplus.processor.service.ServiceHandler#doService(Item)
	 */
	@Override
	public void doService(Item item) throws MongoUtilsException, OptionsException, ProcessException, IOException{	
		if(item == null){
			// we may have to throw an exception here.
			throw new ServiceHandlerException("Item passed is null");
		}
		if(item instanceof WorkItem){
			WorkItem wItem = (WorkItem)item;
			if(wItem.getEventType().equalsIgnoreCase("delete")){
				CGSRulesAdapter.getInstance().handleDelete(wItem);
				return;
			}
		}
			switch(item.getStatus()){
			
			case LOAD_COMPLETE:
				item.setStatus(ProcessStatus.CGS_EVALUATION_STARTED);
				CGSRulesAdapter.getInstance().evaluate(item);
				item.setStatus(ProcessStatus.CGS_EVALUATION_COMPLETE);
			case CGS_EVALUATION_COMPLETE:
				item.setStatus(ProcessStatus.HIERARCY_EVAL_STARTED);
				this.hierEvaluator.evaluate(item);
				item.setStatus(ProcessStatus.HIERARCY_EVAL_COMPLETE);
			case HIERARCY_EVAL_COMPLETE:
				item.setStatus(ProcessStatus.METADATA_TRANS_STARTED);
				this.mdTrEvaluator.evaluate(item);
				//Phase2 fast XML creation for marketing standard documents.
				if(item.getContentType().equalsIgnoreCase("marketingstandard")) {
					item.setStatus(ProcessStatus.FASTXML_CREATION_STARTED);
					this.marketingFastXMLCreationAdapter= new MarketingFastXMLCreationAdapter();
					this.marketingFastXMLCreationAdapter.evaluate(item);
					item.setStatus(ProcessStatus.FASTXML_CREATION_COMPLETE);
				}
				//Phase2 fast XML creation for support documents.
				if(item.getContentType().equalsIgnoreCase("support")){
					item.setStatus(ProcessStatus.FASTXML_CREATION_STARTED);
					this.supportFastXMLCreationAdapter= new SupportFastXMLCreationAdapter();
					this.supportFastXMLCreationAdapter.evaluate(item);
					item.setStatus(ProcessStatus.FASTXML_CREATION_COMPLETE);
				}
				item.setStatus(ProcessStatus.METADATA_TRANS_COMPLETE, true);
			default:
				break;
			}
		


				

	}
}
