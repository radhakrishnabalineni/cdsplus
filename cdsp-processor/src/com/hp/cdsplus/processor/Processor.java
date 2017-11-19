package com.hp.cdsplus.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.threads.ProcessorThreadManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


/**
 */
public class Processor implements Runnable{
	private static final Logger logger = LogManager.getLogger(Processor.class);
	
	private QueueManager queueMgr;
	private ProcessorThreadManager processorThreadMgr;
	
	private boolean isExit = false;
	
	public static boolean reloadedTemp = false;

	private static boolean freshStart =true;
	private Manager manager;

	public static HashMap<String, String> regionListCache = new HashMap<String, String>();
	/**
	 * 
	 */
	public Processor(){
		queueMgr = new QueueManager();
		processorThreadMgr = new ProcessorThreadManager(queueMgr);
		manager = new Manager(queueMgr);
	}

	/**
	 * this method starts the processor
	 */
	public void start(){
		processorThreadMgr.start();
		logger.info("Processor started");
	}
	

	/**
	 * this method starts the processor manager
	 */
	public void startmanager(){
		manager.setDaemon(true);
		manager.start();
		logger.info("Processor instance Manager started.");
	}
	
	/**
	 * used to stop all components of the processor gracefully
	 */
	public void stop(){
		logger.info("Found request to stop Processor");
		processorThreadMgr.stop();
		this.isExit = true;
		this.queueMgr.setExit(true);
		this.manager.setExit(true);
		logger.info("Processor stopped");
	}
		
	/**
	
	 * @return - returns true/false indicating if there is a need to exit the process */
	public boolean isExit(){
		return this.isExit;
	}
	
	public void checkStatus(){
		if(processorThreadMgr.isFatal()){
			this.stop();
			
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	/*	try {
			logger.info("Trying to load items from temp");
			System.out.println("run=======>Trying to load items from temp");
			processorThreadMgr.loadTempItems();
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			this.stop();
		} catch (ProcessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			this.stop();
		} catch (OptionsException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			this.stop();
		}*/
		while(!this.isExit){
			try {
				if(Processor.freshStart  || (Manager.reloadQueue && !Processor.reloadedTemp)){
					logger.info("Manager.reloadQueue->"+Manager.reloadQueue);
					logger.info("Trying to load items from temp");
					processorThreadMgr.loadTempItems();
					if(!Processor.freshStart){
						Processor.reloadedTemp = true;
						logger.info("Notified Manager thread.");
					}
					freshStart = false;
				}
				if(queueMgr.getContentItemSize() == 0)
					processorThreadMgr.loadContentItems();
			} catch (MongoUtilsException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				this.stop();
			} catch (ProcessException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				this.stop();
			} catch (OptionsException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				this.stop();
			}
			Thread.currentThread();
			try {
				Thread.sleep((long) (Math.random() * 5 * 1000));
			} catch (InterruptedException e) {}
		}	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args){
		
		//initialize Application properties
		initAppProperties();

		//processor2
		//get the processor instance name.
		String instance_name = System.getProperty("instance_name");
		if(instance_name==null){
			String message = "ERROR::Processor instance name is null. Please supply valid instancename.";
			System.err.println(message);
			System.exit(1);
		}
		logger.info("instance_name-->"+instance_name);
		//register the instance name in registry.
		ContentDAO contentDAO = new ContentDAO();
		String installationlocation=new File("").getAbsolutePath();
		String lockfileName=installationlocation+File.separator+instance_name+".lck";
		try{
			String servername = InetAddress.getLocalHost().getHostName();
			logger.info("instance_name"+contentDAO.isProcessorInstanceExists(instance_name));
			if(contentDAO.isProcessorInstanceExists(instance_name)){
				DBCursor cursor=contentDAO.getProcessorRegistry(new BasicDBObject("_id",instance_name));
				DBObject dbo= cursor.next();
				if(dbo.get("servername").toString().equalsIgnoreCase(servername)){
					if(dbo.get("installationlocation").toString().equalsIgnoreCase(installationlocation)){
						File lockfile= new File(lockfileName);
						if(lockfile.exists()){
							String message = "ERROR::Processor instance name "+instance_name+" seems already running from this installation location( "+installationlocation+" )on this server ("+servername+" )";
							System.err.println(message);
							System.exit(0);
						}else{
							logger.info("Lock file does not exists hence starting the processor instance.");
							lockfile.createNewFile();
						}
					}else{
						String message = "ERROR::Processor instance name "+instance_name+" seems already running from other installation location( "+dbo.get("installationlocation").toString()+" )on this server ("+servername+" )";
						System.err.println(message);
						System.exit(0);
					}

				}else{
					String message = "ERROR::Processor instance name "+instance_name+" already registered by other server(" +dbo.get("servername").toString()+" )";
					System.err.println(message);
					System.exit(0);
				}
			}
			else{
				DBCursor cursor=contentDAO.getProcessorRegistry(new BasicDBObject("servername",servername));
				while(cursor.hasNext()){
					DBObject dbo= cursor.next();
					if(dbo.get("installationlocation").toString().equalsIgnoreCase(installationlocation)){
						String message = "ERROR::Processor instance name "+dbo.get("instance_name").toString()+" already running from this installation location( "+dbo.get("installationlocation").toString()+" )on this server ("+servername+" )";
						System.err.println(message);
						System.exit(0);
					}
				}
				File lockfile= new File(lockfileName);
				lockfile.createNewFile();
				contentDAO.registerProcessorInstance(new BasicDBObject("_id",instance_name).append("servername", servername).append("installationlocation", installationlocation));
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Not able to register the processor so quiting.");
			System.exit(1);
		}

		//start the processor
		Processor processor = new Processor();
		Thread main = new Thread(processor,"Processor Thread");
		populateRegions();
		// starting main thread
		main.start();
		// calling Processor.start() to start ProcessThreads
		processor.start();
		processor.startmanager();
		File stopFile = new File("processor.stop");
		
		//		 sleep until
		//		  	1. there is a stop requested
		//		   	2. the process is complete and processor is in batch mode execution
		 
		
		while(!processor.isExit()){
			if (stopFile.exists() && stopFile.isFile()){
				logger.info("Shutdown signal found. Initializing shutdown");
				processor.stop();
				File lockfile= new File(lockfileName);
				lockfile.delete();
			}
			processor.checkStatus();
			try {
				HashMap<String , Object> logMap = new HashMap<String, Object>();
				logMap.put("lastmodified", System.currentTimeMillis());
				logMap.put("component", "processor");
				DiagnosticLogger.setEnabled(true);
				DiagnosticLogger.log(instance_name, logMap);
			} catch (MongoUtilsException e) {
				//just eat this exception to avoid any unwanted things in loader so that this will be taken care by healthcheck.
			}
	
			Thread.currentThread();
			try {
				Thread.sleep((long) (Math.random()*5 * 1000));
			} catch (InterruptedException e) {
			}
		}
	}
	
	/*
	 * static utility methods
	 * 
	 */
	
	public static void initAppProperties() {
		String configLocation = System.getProperty("CONFIG_LOCATION");
		if(configLocation == null || "".equals(configLocation)){
			System.err.println("Application Configuration File Missing. " +
					"Set system.property \'CONFIG_LOCATION\' to a valid application config properties file location");
			//System.exit(1);
			return;
		}
		Properties sysProperties = System.getProperties();
		try {
			sysProperties.load(new FileInputStream(configLocation));
		} catch (FileNotFoundException e1) {
			System.err.println("Invalid config file location specified : "+configLocation);
			System.exit(1);
		} catch (IOException e1) {
			System.err.println("Unable to access specified app configuration file : "+configLocation);
			System.exit(1);
		}
		System.setProperties(sysProperties);
		logger.info("application properties loaded successfully");
	}

	/**
	 * Method to populate region list  
	 */
	public static void populateRegions(){
		ContentDAO contentDao = new ContentDAO();
		Options options = new Options();
		DBObject query = new BasicDBObject();
		DBObject displayFields = new BasicDBObject();
		try {
			String regionCollection = ConfigurationManager.getInstance()
					.getMappingValue("region", "metadataLiveCollection");
			options.setContentType("region");
			options.setCollectionName(regionCollection);
			options.setQuery(query);
			displayFields.put("region.name", 1);
			options.setDisplayFields(displayFields);
			ArrayList<DBObject> regions = contentDao.getAllMetadata(options);
			for (DBObject regionTemp : regions) {
				//System.out.println(regionTemp.get("_id").toString()+((DBObject)regionTemp.get("region")).get("name"));
				DBObject region = (DBObject)regionTemp.get("region");
				regionListCache.put(region.get("name").toString(), regionTemp.get("_id").toString());
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
		} catch (OptionsException e) {
			e.printStackTrace();
		}
	}
	
	}
