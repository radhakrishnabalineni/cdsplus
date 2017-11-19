package com.hp.cdsplus.pmloader.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.pmloader.PMLoaderConstants;
import com.hp.cdsplus.pmloader.PMasterLoader;
import com.hp.cdsplus.pmloader.item.WorkItem;
import com.mongodb.BasicDBObject;
/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class QueueManager {
	
	private static final Logger logger = Logger.getLogger(QueueManager.class);

	private ConcurrentLinkedQueue<WorkItem> queue;
	private boolean exit = false;
	private ConcurrentSkipListSet<String> oidS;
	Options options;
	ProductMasterDAO pmDao ;
	
	public QueueManager(){
		queue = new ConcurrentLinkedQueue<WorkItem>();
		oidS = new ConcurrentSkipListSet<String>();
		options = new Options();
		pmDao = new ProductMasterDAO();
	}
	/**
	 * Method push.
	 * @param item WorkItem
	 */
	public synchronized void push(WorkItem item){
		if(oidS.add(item.getOid())){
			queue.add(item);
			this.persistQueue(item, false);
		}else
			logger.info("Skippind duplicate oid : "+item.getOid() + " level : "+item.getLevel().toString());
			
	}
	/**
	 * Method pop.
	
	 * @return WorkItem */
	public synchronized WorkItem pop(){
			if (queue!=null && !queue.isEmpty() && !this.exit){
				WorkItem item = queue.remove();	
				oidS.remove(item.getOid());
				this.persistQueue(item, true);
				return  item;
			} else 
				return null;
	}
	
	/**
	 * Method isEmpty.
	
	 * @return boolean */
	public boolean isEmpty(){
		if(this.exit)
			return true;
		else 
			return  queue.isEmpty();
	}
	
	/**
	 * Method size.
	
	 * @return int */
	public int size(){
		if(this.exit)
			return 0;
		else
		return queue.size();
	}
	
	/**
	 * Method setExit.
	 * @param exit boolean
	 */
	public void setExit(boolean exit) {
		this.exit = exit;	
	}
	
	/**
	 * Method isExit.
	 * @return boolean
	 */
	public boolean isExit() {
		return this.exit;	
	}
	
	private synchronized void persistQueue(WorkItem item, boolean isDelete){
		options.setContentType("productmaster");
		options.setQuery(new BasicDBObject("_id",item.getOid()));
		options.setDocid(item.getOid());
		
		options.setMetadataDocument(new BasicDBObject("_id",item.getOid()).append("level", item.getLevel().toString()));
		try {
		
		
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue("productmaster", PMLoaderConstants.TEMP_COLLECTION);
		if(StringUtils.isEmpty(tempCollectionName))
		    tempCollectionName = "pm_temp";
		
			if(isDelete){
				pmDao.delete(options, tempCollectionName);
			}else{
				pmDao.update(tempCollectionName, options);
				//matt added this next line in order to persistently have a way to access PM delta OIDs for MSC touch updates
				//potentially add logic here [not literally, but somewhere] to have the collection created before any adds occur
				//SMO User story## 7471 Changes for including company_info in msc_temp
				options.setMetadataDocument(new BasicDBObject("_id",item.getOid()).append("level", item.getLevel().toString()).append("company_info", PMasterLoader.company_info));
				
				pmDao.update("msc_temp", options);
			}
				
		} catch (OptionsException e) {
			System.out.println("Warning :: Cannot load Queue state to CDS+ db collection. ");
			e.printStackTrace();
		} catch (MongoUtilsException e) {
			System.out.println("Warning :: Cannot load Queue state to CDS+ db collection. ");
			e.printStackTrace();
		}
		
		
		
	}
}
