package com.hp.cdsplus.update;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.hp.cdsplus.update.Exception.Log4JException;
import com.hp.cdsplus.update.Exception.MongoConnectionException;
import com.hp.cdsplus.update.Exception.MongoDataException;
import com.hp.cdsplus.update.Exception.MongoQueryException;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


/**
 * 
 * @author nidoh
 *
 */
public class UpdateManager {
	private static Logger logger = Logger.getLogger(UpdateManager.class);
	private MongoClient mongoConn;
	private DB PMDB;
	private DB RMDB;
	private DBCollection msc_temp;
	private DBCollection rm_stage;
	
	// SMO:- User Story ## 7471 companyInfoValue is added to hold company_info from msc_temp
	private Map<String,String>companyInfoValueMap = new HashMap<String, String>();
	private ArrayList<String> delta_oid_queue = new ArrayList<String>();
	private ArrayList<String> guid_queue = new ArrayList<String>();
	private static int num_threads;
	private ArrayList<UpdateThread> threads = new ArrayList<UpdateThread>(); // list of all threads. could potentially use for state management. revisit later.
	private int num_threads_done = 0;
	private int paused_threads = 0;
	private int num_updates = 0; // for logging purposes
	private static int update_threshhold = 0; // ^^
	private int max_queue_size = 0; // ^^
	private static boolean skip = false;

	
	private UpdateManager(){} // only to instantiate the object	
	// SMO:- User Story ## 7471 
	public Map<String,String> getCompanyInfoValueMap()
	{
		return companyInfoValueMap;
	}
	
	public static void main(String args[]){
		UpdateManager upMan = new UpdateManager();
		
		//first task - initialize project work and properties to run
		try{
			upMan.setProps();
			upMan.setLog4j();
			logger.info("********TOUCH UPDATER JOB ON CDS+ HAS STARTED********");
			logger.info("********Log4J initialized********");
			logger.info("********Properties initialized********");
		} catch (IOException e){
			logger.error(e.getMessage());
			System.exit(0); // if the properties file cannot be read, exit. it contains vital values to proceed so if we don't have them then bail out
		} catch (Log4JException e){
			logger.error(e.getMessage());
			System.exit(0); // if the logger can't be initialized, exit. no point in running without being able to log
		} catch (NumberFormatException e){
			logger.error(e.getMessage());
			logger.error("Non-fatal error. Fell back to default value for the incorrect property inside mongo.properties and continuing.");
			update_threshhold = 50;
		} catch (Exception e){
			logger.error(e.getMessage());
			logger.error("Something went wrong which the program did not account for. Bailing out as unpredictable behavior potential.");
			System.exit(0);
		} 
		
		//next task - connect to mongo using properties fetched and find PM delta OIDs
		try{
			upMan.connect();
		} catch (NumberFormatException e){
			logger.error(e.getMessage());
			logger.error("Could not parse port # from properties file. Exiting program");
			System.exit(0);
		} catch (UnknownHostException e){
			logger.error(e.getMessage());
			logger.error("Host defined in properties file for mongo connection is incorrect or does not exist. A connection could not be made. Exiting.");
			System.exit(0);
		} catch (MongoConnectionException e){
			logger.error(e.getMessage()); // defined in the function connect()
			logger.error("Exiting.");
			System.exit(0);
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("Something went wrong which the program did not account for. Bailing out as unpredictable behavior potential.");
			System.exit(0);
		}
		
		
		//logger.info("********Successfully Connected to Mongo DB instance " + mongoConn.getAddress() + "". Now progressing to find all delta PM OIDs********");
		
		
		//next task - use the objects fetched from mongo in finding PM delta OIDs
		try{
			upMan.findPMOIDs();
		} catch (MongoQueryException e){
			logger.error(e.getMessage()); // already defined in findPMOIDs()
			logger.error("Exiting.");
			System.exit(0);
		} catch (MongoDataException e){
			logger.error(e.getMessage()); // already defined in findPMOIDs()
			logger.error("Exiting.");
			System.exit(0);
		} catch (Exception e){
			logger.error(e.getMessage());
			logger.error("Something went wrong which the program did not account for. Bailing out as unpredictable behavior potential.");
			System.exit(0);
		}
		
		
		logger.info("********Successfully fetched all PM delta OIDs. Now progressing to make the threads to find MSC GUIDs and touch/update their timestamps********");
		
		
		//next task - now that the PM delta OIDs queue is populated, create and manage the threads which will be doing the processing
		try{
			upMan.makeThreads();
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
			logger.error("Could not initialize the threads properly because the number of threads property could not be read from properties file [mongo.properties]. Using default of 4 threads and continuing.");
			skip = true;
			num_threads = 4; // default value
			upMan.makeThreads(); //this is the fallback mechanism for not being able to read the num_threads from properties file - call function again but this time skip reading from properties file
		} catch (Exception e){
			logger.error(e.getMessage());
			logger.error("Could not initialize the threads for some generic error. Exiting");
			//can exit without explicitly stopping the threads because the 'daemon' is false. this means the threads will exit if the parent process exits.
			System.exit(0);
		}
		
		
		//next task- this process waits for the threads to reach a midway point to ensure queues are established in a controlled fashion
		upMan.checkResume();		
		
		//next task - now we can wait here until all of the threads have finished all of the work
		upMan.checkFinish();
		
		
		//now clear the collection in Mongo out -- doing this hear as a recovery mechanism for potential failures in the middle of the job
		try{
			upMan.clearCollection();
		} catch (MongoQueryException e){
			logger.error(e.getMessage()); // already defined in findPMOIDs()
			logger.error("Exiting.");
			System.exit(0);
		}
		
				
		//this is for the last task - exit cleanly
		logger.info("********Update Job for MSC assets finished. Exiting cleanly.********");
		System.exit(0);
	}
	
	
	//fetch properties file and read from it -- store in "System" so can fetch it on-demand later
	private void setProps() throws IOException, NumberFormatException{
		Properties p = new Properties(System.getProperties());
		FileInputStream propFile = new FileInputStream("config/mongo.properties"); // this will automatically throw an exception if the file is not found
        
		p.load(propFile);
        System.setProperties(p);

        //  -- for debugging only -- display new properties
        //System.getProperties().list(System.out);
        
        update_threshhold = Integer.parseInt(System.getProperty("UPDATE_FREQUENCY")); 
        // this will throw an exception as is if the property is not an integer and bail out
        //handle default values in the catch block
        
        if(update_threshhold < 0){
        	throw new NumberFormatException("update threshhold value in mongo.properties is negative. Continuing with default value of 50. Non-fatal.");
        }
        
        propFile.close();

	}
	
	//initialize the log4j logger for the project
	private void setLog4j() throws Log4JException{
		String log4jConfig = System.getProperty("LOG4J_LOCATION");
		if(log4jConfig != null){
			DOMConfigurator.configure(log4jConfig);
		} else {
			throw new Log4JException("Could not configure Log4J logger because System property for log4j location not read correctly.");
		}
	}

	
	//use connection info from properties files and connect to mongo DBs
	private void connect() throws NumberFormatException, UnknownHostException, MongoConnectionException{
		
		String mongoURI = System.getProperty("mongoClientURI");
		
		if(mongoURI == null || "".equals(mongoURI)){
			throw new MongoConnectionException("mongoClientURI property undefined or unreaadable in string format in mongo.properties");
		}
		
		try {
			mongoConn = new MongoClient(new MongoClientURI(mongoURI));
		} catch (UnknownHostException e) {
			throw new MongoConnectionException("Cannot Connect to the given Mongo URI : "+ mongoURI, e);
		}
		
		//mongoConn = new MongoClient( System.getProperty("MONGO_HOST"), Integer.parseInt(System.getProperty("MONGO_PORT")) ); //from config file mongo.properties
		
		if(mongoConn == null){
			throw new MongoConnectionException("Could not connect to Mongo from properties file credentials.");
		}else {
			logger.debug("Connected to " + mongoConn.getAddress() + " from properties file. Now going to get DB instances");
		}
		
		PMDB = mongoConn.getDB(System.getProperty("PM_DB")); // PM db has the msc_temp and hierarchy collections
		
		if(PMDB == null){
			throw new MongoConnectionException("Could not fetch product master DB instance from Mongo.");
		}else {
			logger.debug("Fetched " + PMDB.getName() + " DB instance in Mongo. Now going to get RichMedia DB instance.");
		}
		
		RMDB = mongoConn.getDB(System.getProperty("RM_DB")); // this is the DB instance for the richmedia/MSC content
		
		if(RMDB == null){
			throw new MongoConnectionException("Could not fetch richmedia DB instance from Mongo.");
		}else {
			logger.debug("Fetched " + RMDB.getName() + " DB instance in Mongo. Now going to fetch collection instances.");
		}
		
		
		msc_temp = PMDB.getCollection("msc_temp");
		
		if(msc_temp == null){
			throw new MongoConnectionException("Could not fetch msc_temp collection from Mongo's product master DB.");
		}else {
			logger.debug("Fetched " + msc_temp.getFullName() + " collection instance in Mongo. Now going to fetch richmedia collection instance.");
		}
		
		rm_stage = RMDB.getCollection("metadata_staging");
		
		if(rm_stage == null){
			throw new MongoConnectionException("Could not fetch metadata_staging collection from Mongo's richmedia DB.");
		}else {
			logger.debug("Fetched " + rm_stage.getFullName() + " collection instance in Mongo. Now going to the rest of the program.");
		}
		
		logger.info("********Successfully Connected to Mongo DB instance " + mongoConn.getAddress() + " and fetched all relevant DB instances and collections. Now progressing to find all delta PM OIDs********");

	}
	
	//////////////////////////////////////
	//getters for any other class needing to access these private members
	public DB getPMDB(){
		return PMDB;
	}
	
	public DBCollection getRichMediaColl(){
		return rm_stage;
	}
	
	public ArrayList<String> getGUIDQueue(){ //queue containing the distinct GUIDs to be updated in this job
		return guid_queue;
	}
	
	public ArrayList<String> getOIDQueue(){ // queue containing the distinct OIDs detected by the PM loader as having changed recently. will pull GUIDs from these OIDs
		return delta_oid_queue;
	}
	//////////////////////////////////////
	
	
	//fetch PM OIDs from mongo which have changed in the last 24 hours [already stored in mongo collection "msc_temp" from PM loader]
	private void findPMOIDs() throws MongoQueryException, MongoDataException{
		DBCursor pm_delta_oids = msc_temp.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		if(pm_delta_oids != null){
				logger.info("********Found " + pm_delta_oids.size() + " OIDs from Product Master which need to be processed during this job run.********");	
		} else {
			throw new MongoQueryException("Query to find PM delta OIDs returned a null object. Something was not initialized correctly.");
		}
		
		while( pm_delta_oids != null && pm_delta_oids.hasNext() ){
			DBObject entry = pm_delta_oids.next();
			String OID = entry.get("_id").toString();
			String c_info = entry.get("company_info").toString();
			
			if(OID != null && !OID.equals("")){
				this.pushOID(OID);
				//SMO User story## 7471 Changes for making a map with key as oids and value as company_info  
				this.companyInfoValueMap.put(OID,c_info);
				//msc_temp.remove(entry);
				// >> decided to keep the collection until the end of the job and drop it- that way in case of a job failure in between can revert and ensure no updates are missed <<
				// no longer need this in the collection in mongo -- plus want to clear it out for next job's run so can ensure unique and fresh delta changes to be processed
					//identical to the PM loader and how it uses pm_temp
			} else {
				throw new MongoDataException("Unexpected null value for schema property _id on processing " + entry.toString() + " in msc_temp collection.");
			}
		}
		
		max_queue_size = delta_oid_queue.size();

	}
	
	
	//take the argument OID and put it into the queue of PM delta OIDs to be managed
	private void pushOID(String oid){
	//don't need to sync on this method here because this processing will all be done before i call the threads. plus none of them will even call this method.
		if( oid != null && !oid.equals("")) {
			if( !this.delta_oid_queue.contains(oid) ){
				logger.info("Adding " + oid + " to the OID queue.");
				this.delta_oid_queue.add(oid);
			} 
			//if it is already in the list, do nothing -- don't want duplicates
		}
	}

	
	//create the thread objects and call the UpdateThread class. put each thread class object into a queue for management ease
	private void makeThreads() throws NumberFormatException{
		int t_id = 0;
		
		if(!skip){
			num_threads = Integer.parseInt(System.getProperty("NUM_THREADS"));
		}
		
		if(num_threads < 0){
			throw new NumberFormatException("Negative number used for number of threads in mongo.properties.");
		}
		
		while ( t_id < num_threads ){ // from mongo.properties
			try {
				UpdateThread upT = new UpdateThread(t_id, this); // pass 'this' class instance which has getter's so that if the threads need the private members, they can get them
				threads.add(upT);
				t_id++;
			} catch (Exception e){
				logger.error(e.getMessage());
				logger.error("Something went wrong initializing the threads. Exiting.");
				//can exit without explicitly stopping the threads because the 'daemon' is false. this means the threads will exit if the parent process exits.
				System.exit(0);
			}
		} // end while loop
	}
	
	
	//fetch the 'top' OID from the OID queue. then remove it from the queue
	public String popOID(){
		//sync on this, so each thread gets a unique OID
		synchronized(this){
			if( !delta_oid_queue.isEmpty() ){
				String oid = delta_oid_queue.remove(0); // so we don't end up in infinite loop and can progress
				return oid;
			} else {
				return null;
			}
		}
	}
	
	
	//this is how the thread class tells this class when it is done and how this class can tell when all of the threads are done
	public void thisThreadDone(){
		synchronized(this){
			num_threads_done++;
			logger.debug("The number of threads done with all of their work is now: " + num_threads_done);
		}
	}
	
	
	//the thread class calls this function to put a GUID into the queue to be processed
	public void pushGUID(String GUID){
		synchronized(this){ // need to sync on this because multiple threads will be calling this via hierarchy classes potentially
			if( GUID != null && !GUID.equals("")) {
				if( !guid_queue.contains(GUID) ){
					logger.info("Adding " + GUID + " to the GUID queue.");
					guid_queue.add(GUID);
				} 
				//if it is already in the list, do nothing -- don't want duplicates
			}
		}
	}
	
	
	//this is how this class checks to see if all of the OID processing has been done and the GUID processing can begin.
	//if all instances of the thread classes have their 'paused' set to true- then they are all wait()'ing. wake them up with the notify call so they can progress to the next step
	private void checkResume() throws IllegalMonitorStateException{
		while (true){
			for(UpdateThread upT : threads){
				if(upT.getIsPaused()){
					paused_threads++;
				} 
			}
			
			if(paused_threads == num_threads){
				//wake up the threads which are all currently waiting..aka all of them
				synchronized(this){
					logger.debug("All threads have been woken up via interrupt signal.");
					
					num_updates = 0;
					max_queue_size = guid_queue.size();
					//both of the above are a 'reset' in a sense for the logging percentage accuracy
					
					notifyAll();
					logger.info("********All distinct GUIDs have been found for the list of delta PM  OIDs. Progressing to make the appropriate updates********");
				}
				
				return;
			} else {
				paused_threads = 0; // dont want to return as we want to wait here until all threads reach here to wake everyone up simultaneously
			}
		} // end while loop

	}
	
	
	//this is how this class can tell when all threads are done with their work and can move to the end of the code
	private void checkFinish(){
		while( num_threads_done < num_threads ){
			try {
				Thread.sleep(100); // wait for a second -- without this mechanism, the loop iterates so fast it doesn't allow for any context switching
			} catch (InterruptedException e) { // expected interrupt after the time interval specified above. dont need to do anything
			}
			continue; // do nothing
		}
		
		logger.info("********All Threads are done with work. Now preparing shutdown tasks.********");
		return; // now we can return as we are in an expected and manageable state
	}
	
	
	
	//take the 'top' GUID from the GUID queue to process
	public String popGUID(){
		//sync on this, so each thread gets a unique GUID
		synchronized(this){
			if( !guid_queue.isEmpty() ){
				String GUID = guid_queue.remove(0); // so we don't end up in infinite loop and can progress
				logger.debug("GUID taken out of the queue now is: " + GUID);
				return GUID;
			} else { return null; }
		}
	}
	
	
	//this function will remove all entries from the msc_temp collection
	//this is done as a mechanism to track what work needs to be done with each job- otherwise we don't have a clean way to tell what work is required.
	//this is similar to how the PM Loader treats the pm_temp collection
	private void clearCollection() throws MongoQueryException{
		DBCursor pm_delta_oids = msc_temp.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		if(pm_delta_oids == null){
			throw new MongoQueryException("Query to find PM delta OIDs for clearence returned a null object. Something has changed during the course of this run.");
		}
		
		while( pm_delta_oids != null && pm_delta_oids.hasNext() ){
			DBObject entry = pm_delta_oids.next();
			if(entry != null){
				msc_temp.remove(entry);
			} else {
				logger.debug("Entry fetched from msc_temp has a null entry and will be left in the collection.");
			}
		}
		
		logger.info("msc_temp collection cleared from Mongo successfully.");
	}

	
	public void incrementOIDUpdates(){
		synchronized(this){
			num_updates++;
		}
		
		if( (num_updates % update_threshhold == 0) ||  getOIDQueue().size() == 0){
			//log updated progress here
			logger.info("Either finding distinct GUIDs from OIDs given or making updates to the GUIDs' Progress: " + (int) ( ((float)num_updates / (float)max_queue_size) * 100 ) + "%" );
		}
		
	}
	
	
	public void incrementGUIDUpdates(){
		synchronized(this){
			num_updates++;
		
			if( (num_updates % update_threshhold == 0) ||  getGUIDQueue().size() == 0){
				//log updated progress here
				logger.info("Either finding distinct GUIDs from OIDs given or making updates to the GUIDs' Progress: " + (int) ( ((float)num_updates / (float)max_queue_size) * 100 ) + "%" );
			}
		}
		
	}
	
	
}
