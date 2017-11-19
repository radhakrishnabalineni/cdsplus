/**
 * 
 */
package com.hp.cdsplus.pmloader.single;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.pmloader.single.Level;
import com.hp.cdsplus.pmloader.single.PMLoaderConstants;
import com.hp.cdsplus.pmloader.single.PMasterLoader;
import com.hp.cdsplus.pmloader.single.LoaderException;
import com.hp.cdsplus.pmloader.single.LoaderInitializationException;
import com.hp.cdsplus.pmloader.single.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author kashyaks
 *
 * @version $Revision: 1.0 $
 */
public class ExtractorManager implements PMLoaderConstants, Runnable{

	private volatile boolean isExit = false;
	private volatile boolean isComplete = false;
	private volatile boolean isInitialized = false;
	private boolean isDeltaMode = false;

	private QueueManager queueMgr;
	private ArrayList<ExtractorThread> extractors;
	private ObjectId currentLoadId;
	private Date[] dateRange;
	private volatile boolean isFatal;
	private boolean isRepublishMode;
	private boolean isTempReload;
	
	private static final Logger logger = Logger.getLogger(ExtractorManager.class);
	/**
	 * Constructor for DBExtractor.

	
	 * @param queueMgr QueueManager
	 * @param isDeltaMode boolean
	 * @throws LoaderInitializationException * @throws LoaderException   */
	public ExtractorManager(QueueManager queueMgr, boolean isDeltaMode, boolean isRepublishMode) throws LoaderInitializationException, LoaderException{
		this.queueMgr = queueMgr;
		this.isDeltaMode = isDeltaMode;
		this.isRepublishMode = isRepublishMode;
		/*Thread extrMgr = new Thread(this,"ExtractorManager");
		extrMgr.setDaemon(true);
		extrMgr.start();*/
	}

	/**
	 * Method run.
	
	
	 * @throws LoaderException  * @see java.lang.Runnable#run() */
	@Override
	public void run() {
		while(!isComplete){
			if(isExit){
				stopExtractors();
			}else{
				//System.out.println("Extractor this.isInitialized"+this.isInitialized);
				//logger.info("Extraction initialized status : "+this.isInitialized);
				//check if the threads are initialized before trying to kill them
				if(this.isInitialized){
					//check for any fatal errors
					for(ExtractorThread extractor : this.extractors){
						if(extractor.isFatal()){
							this.isExit = true;
							this.stopExtractors();
							this.isFatal = true;
							break;
						}
					}	
					// if there are no fatal errors look for completion status
					
					for(ExtractorThread extractor : this.extractors){
						//logger.info("Extractor Thread Completion status : "+this.isComplete);
						if(!extractor.isCompleted()){
							this.isComplete = false;
							break;
						}else
							this.isComplete=true;
						
					}				
				}else {
					// since the extraction manager has not been initialized, checking if extraction is complete is an invalid scenario
					this.isComplete = false;
				}
			}
		}	
	}
	public void startExtractors() throws LoaderException, OptionsException, MongoUtilsException {
		/*
		 * If deltaMode is set to true, we look for date
		 * range from the DB, if we don't find any previous 
		 * records (which may happen first time) we do 
		 * full load
		 */
		if(isDeltaMode)
			dateRange = this.getDateRange();
		else
			dateRange = null;
		// if date range is null it will create an entry with start time as 1970 and end time as current milliseconds
		//create a new load entry in for the given date range in the extractor table

		this.logDateRange(dateRange);
		/*
		 * If pm_temp is not empty, queue should be loaded with data in pm_temp,
		 * it should not query the product master database 
		 * */
		try {
			isTempReload = itemsPendingInTemp();
			//System.out.println("isTempReload : "+isTempReload);
		} catch (MongoUtilsException e1) {
			this.isFatal = true;
			logger.error(e1);
			e1.printStackTrace();
		}
		
		extractors = new ArrayList<ExtractorThread>(Level.values().length);
		for (final Level level : Level.values()) {
			if(isDeltaMode)
			extractors.add(new ExtractorThread(this.queueMgr, level,
					this.currentLoadId, dateRange, isDeltaMode, isRepublishMode,this.isTempReload));
			else 
				extractors.add(new ExtractorThread(this.queueMgr, level,this.isTempReload));
		}
		//logger.info("Extractor Threads initialized.");
		this.isInitialized = true;
		logger.info("Extractor Threads initialized."+this.isInitialized);
	}

	public void stopExtractors(){
		if(this.isInitialized){
			for(ExtractorThread extractor : this.extractors){
				//set extractors to exit
				extractor.setExit(true);
				// wait for the extractor thread to exit
				while(!extractor.isCompleted()){
					try {
						Thread.currentThread();
						Thread.sleep((long) (Math.random()*5)*1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	/**
	 * Method isInitialized.
	 * @return boolean
	 */
	public boolean isInitialized(){
		return this.isInitialized;
	}
	/**
	 * Method isActive.
	 * @return boolean
	 */
	public boolean isActive(){
		return (this.isInitialized && !isComplete && !isExit );
	}

	/**
	 * Method setExit.
	 * @param b boolean
	 */
	public void setExit(boolean b) {
		isExit = b;

	}

	/**
	 * Method isExit.
	 * @return boolean
	 */
	public boolean isExit() {
		return isExit;

	}

	/**
	 * Method logDateRange.
	 * @param range Date[]
	

	 * @throws LoaderException  */
	protected void logDateRange(Date[] range) throws LoaderException {
		System.out.println("logDateRange called");
		DBObject record = new BasicDBObject();
		
		if(range == null || range.length != 2){
			// new java.util.Date().getTime() gives start time as JAN 1 1970
			record.put("start_time",new Date(0));
			record.put("end_time", new Date(System.currentTimeMillis()));
		} else{
			record.put("start_time",range[0]);
			record.put("end_time", range[1]);
		}
		//SMO Change : Changes for adding company_info in transaction logs
		record.put("company_info", PMasterLoader.company_info);
		record.put("extraction_status", "started");
		
		logger.info("Current load : start_time - "+record.get("start_time")+" " +
				"end_time - "+record.get("end_time"));
		
		Options options = new Options();
		options.setContentType("productmaster");
		options.setMetadataDocument(record);
		
		ProductMasterDAO pmasterDAO = new ProductMasterDAO();
		try {
			this.currentLoadId = (ObjectId) pmasterDAO.addLogEntry(options);
		} catch (MongoUtilsException e) {
			throw new LoaderException(e);
		} catch (OptionsException e) {
			throw new LoaderException(e);
		}
		logger.info(record);
	}

	/**
	
	 * @return array of dates 
	 * date[0] will be the start time for the current load
	 * date[1] will be the end time for the current load  
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public Date[] getDateRange() throws LoaderException, OptionsException, MongoUtilsException{
		Date[] dateRange = new Date[2];
		Long lastLoadEndTime = this.getLastLoadEndTime();
		if(lastLoadEndTime == null){
			return null;
		}
		dateRange[0] = new Date(lastLoadEndTime);
		dateRange[1] = new Date(System.currentTimeMillis());
		return dateRange;
	}

	/**
	 * Method getLastLoadEndTime.
	
	 * @return Long * @throws LoaderException  * @throws LoaderException * @throws LoaderException
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	
	private Long getLastLoadEndTime() throws LoaderException, OptionsException, MongoUtilsException{

		//SMO Change : Changes for adding company_info in transaction logs
		DBObject query = new BasicDBObject("extraction_status","successful").append("loading_status", "successful").append("company_info",PMasterLoader.company_info);

		DBObject sortFields = new BasicDBObject("end_time",-1);
		
		Options options = new Options();
		options.setContentType("productmaster");
		options.setQuery(query);
		ProductMasterDAO pmasterDao = new ProductMasterDAO();
		DBCursor results = pmasterDao.getLogEntry(options);
		results.sort(sortFields);
		
		if( results != null && results.hasNext()){
			DBObject result = results.next();
			Date end_date = (Date) result.get("end_time");
			return end_date.getTime();
		}
		return null;
	}

	/**
	
	 * @return the currentLoadId */
	public ObjectId getCurrentLoadId() {
		return currentLoadId;
	}

	/**
	 * Method logExtractionStatus.
	 * @param status String
	 * @throws LoaderException
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public void logExtractionStatus(String status) throws LoaderException, MongoUtilsException, OptionsException {
		DBObject query = new BasicDBObject("_id", currentLoadId);
		DBObject record = new BasicDBObject();
		record.put("extraction_status", status);
		Options options = new Options();
		options.setContentType("productmaster");
		options.setMetadataDocument(new BasicDBObject("$set",record));
		options.setQuery(query);
		ProductMasterDAO pmDao = new ProductMasterDAO();
		pmDao.updateLogEntry(options);
		
		System.out.println("Extractor Message "+record);
		
	}

	public boolean isFatal() {
		// TODO Auto-generated method stub
		return this.isFatal;
	}

	public boolean isComplete() {
		// TODO Auto-generated method stub
		return this.isComplete;
	}


	public boolean itemsPendingInTemp() throws MongoUtilsException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("productmaster");
		
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue("productmaster", PMLoaderConstants.TEMP_COLLECTION);
		if(StringUtils.isEmpty(tempCollectionName))
		    tempCollectionName = "pm_temp";
		DBCollection collection = db.getCollection(tempCollectionName);
		DBCursor cursor = collection.find();
		//System.out.println("isTempReload : "+cursor.count());
		if(cursor.size()>0){
			return true;
		}
		return false;
	}
}