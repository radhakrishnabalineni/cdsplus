package com.hp.cdsplus.processor.item;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.ProcessStatus;
import com.hp.cdsplus.processor.adapters.CGSRulesAdapter;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.exception.ServiceHandlerException;

public class CGSWorkItem extends Item {
	
	private String id;
	private Boolean isDelete ;
	public CGSWorkItem(String contentType, String docid, Boolean isDelete) throws MongoUtilsException, OptionsException {
	this.setId(docid);
	this.setStatus(ProcessStatus.NEW_ITEM);
	this.setContentType(contentType);
	this.isDelete = isDelete;
	
	}

	@Override
	protected void load() throws ProcessException, OptionsException,
			MongoUtilsException, ServiceHandlerException, AdapterException {
	}

	@Override
	public void service() throws ProcessException, ServiceHandlerException,
			MongoUtilsException, OptionsException {
		if(this.isDelete){
			CGSRulesAdapter.getInstance().handleDelete(this);
		}else{
			CGSRulesAdapter.getInstance().evaluate(this);
		}
		this.setStatus(ProcessStatus.SERVICE_COMPLETE);
	}

	@Override
	public void save() throws ProcessException, MongoUtilsException,
			OptionsException {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
