package com.hp.cdsplus.pmloader.single;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.pmloader.single.PMLoaderConstants;
import com.hp.cdsplus.pmloader.single.LoaderException;
import com.hp.cdsplus.pmloader.single.QueueManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 */
public class WorkerManager implements PMLoaderConstants,Runnable {
	private static final Logger logger = Logger.getLogger(WorkerManager.class);
	private QueueManager queueManager;
	private int size = 4;
	
	private ArrayList<WorkerThread> workers;
	private volatile boolean isInitialized = false;
	private volatile boolean isExit = false;
	private volatile boolean isComplete = false;
	private volatile boolean isFatal = false;
	private ObjectId currentLoadId;
	/**
	 * Constructor for WorkerManager.
	 * @param queueMgr QueueManager
	 */
	public WorkerManager(QueueManager queueMgr, ObjectId currentLoadId){
		this.queueManager = queueMgr;
		this.size = 4;	
		this.currentLoadId = currentLoadId;
		workers = new ArrayList<WorkerThread>(this.size);
		logger.info("WorkerManager Initialized. Current Load Id : "+this.currentLoadId+" , WorkerThreads : "+this.size);
	}
	
	/**
	 * Constructor for WorkerManager.
	 * @param queueMgr QueueManager
	 * @param threadPoolSize int
	 */
	public WorkerManager(QueueManager queueMgr, ObjectId currentLoadId, int threadPoolSize){
		this.queueManager = queueMgr;
		this.size = threadPoolSize;	
		this.currentLoadId = currentLoadId;
		workers = new ArrayList<WorkerThread>(this.size);
		Thread workerMgr = new Thread(this,"WorkerManager");
		workerMgr.setDaemon(true);
		workerMgr.start();
		logger.info("WorkerManager Initialized. Current Load Id : "+this.currentLoadId+" , WorkerThreads : "+this.size);
		
	}
	
	
	/**
	 * Method startWorkers.
	 * @throws LoaderException
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public void startWorkers() throws LoaderException, MongoUtilsException, OptionsException{
		for (int count = 0; count <= this.size ;  count++){
			workers.add(new WorkerThread(this.queueManager, count));
		}
		logger.info("Workers Started successfully");
		this.logDBLoadStatus("started");
		this.isInitialized  = true;
	}
	
	/**
	 * Method stopWorkers.
	 * @throws LoaderException
	 */
	public void stopWorkers(){
		this.isExit = true;
		for (WorkerThread worker : workers){
			worker.setExit(true);
			while(!worker.isCompleted()){
				Thread.currentThread();
				try {
					Thread.sleep((long)(Math.random()*5)*1000);
				} catch (InterruptedException e) {
					//dont do anything here
				}
			}
		}
		logger.info("Workers stopped successfully");
		this.isComplete = true;
	}
	
	/**
	 * Method isActive.
	 * @return boolean
	 */
	public boolean isActive(){
		for (WorkerThread worker : workers){
			if (worker.isFatal() )
				this.stopWorkers();
			this.isExit = true;
		}
		return (this.isInitialized && !this.isComplete && !this.isExit);
	}

	/**
	 * Method setExit.
	 * @param exit boolean
	 */
	public void setExit(boolean exit) {
		this.isExit = exit;
		
	}


	/**
	 * @param isCompleted the isCompleted to set
	 */
	public void setCompleted(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	
	 * @return the isInitialized */
	public boolean isInitialized() {
		return this.isInitialized;
	}

	/**
	
	 * @return the isExit */
	public boolean isExit() {
		return isExit;
	}

	public void logDBLoadStatus(String status) throws LoaderException, MongoUtilsException, OptionsException {
		Options options = new Options();
		ProductMasterDAO pmDAO = new ProductMasterDAO();
		DBObject query = new BasicDBObject("_id", currentLoadId);
		
		DBObject record = new BasicDBObject();
		record.put("loading_status", status);	
		
		options.setContentType("productmaster");
		options.setMetadataDocument(new BasicDBObject("$set",record));
		options.setQuery(query);
		
		pmDAO.updateLogEntry(options);
		System.out.println("WorkerManager Message "+record);
	}

	public boolean isFatal() {
		return this.isFatal;
	}

	@Override
	public void run() {
		while(!this.isComplete){
			if(this.isExit){
				stopWorkers();
			}else{
				//check if the threads are initialized before trying to kill them
				if(this.isInitialized){ 
					//check for any fatal errors
					for(WorkerThread worker : this.workers){
						if(worker.isFatal()){
							this.isExit = true;
							this.stopWorkers();
							this.isFatal = true;
							break;
						}
					}	
					// if there are no fatal errors look for completion status
					
					for(WorkerThread worker : this.workers){
						if(!worker.isCompleted()){
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

	public boolean isComplete() {
		return this.isComplete;
	}
	
	
}
