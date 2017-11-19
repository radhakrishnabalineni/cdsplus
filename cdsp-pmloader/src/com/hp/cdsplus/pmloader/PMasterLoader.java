package com.hp.cdsplus.pmloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.bson.types.ObjectId;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.cdsplus.pmloader.exception.LoaderException;
import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;
import com.hp.cdsplus.pmloader.extractor.ExtractorManager;
import com.hp.cdsplus.pmloader.queue.QueueManager;
import com.hp.cdsplus.pmloader.worker.WorkerManager;
import com.hp.cdsplus.utils.JDBCUtils;
import com.hp.cdsplus.utils.TimeStampWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class PMasterLoader implements PMLoaderConstants, Runnable{

	private static final Logger logger = Logger.getLogger(PMasterLoader.class);
	private static boolean exit = false;
	private static final File stopFile = new File("loader.stop");
	//SMO:User Story #7850
	public static String company_info=null;
	/**
	 * Method loadSystemProperties.
	
	
	 * @param propertyName String
	 * @throws LoaderInitializationException  * @throws IOException * @throws FileNotFoundException */
	void loadSystemProperties(String propertyName) throws LoaderInitializationException{
		if (propertyName == null || "".equals(propertyName)){
			throw new LoaderInitializationException("Invalid File name. " +
					"Filename is either null/zero length.");
		}
		String fileName = System.getProperty(propertyName);
		isNull(propertyName, fileName);
		
		Properties properties = new Properties();
		//load properties from file
		try {
			properties.load(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			throw new LoaderInitializationException("Exception while trying " +
					"to access file in following location : "+fileName,e);
		} catch (IOException e) {
			throw new LoaderInitializationException("Exception while trying " +
					"to read file in following location : "+fileName,e);
		}
		System.getProperties().putAll(properties);
		logger.info("Properties loaded from "+fileName);
		
	}

	/**
	 * Method initLog4J.
	
	 * @throws LoaderInitializationException */
	void initLog4J() throws LoaderInitializationException{
		String log4jConfig = System.getProperty("LOG4J_LOCATION");
		isNull(log4jConfig,"LOG4J_LOCATION");
		DOMConfigurator.configure(log4jConfig);
	}
	
	/**
	 * Method initJDBCUtils.
	
	
	 * @throws LoaderInitializationException  * @throws SQLException */
	void initJDBCUtils() throws LoaderInitializationException{
		String dbURL = System.getProperty("DB_URL");
		String userName = System.getProperty("DB_USERNAME");
		String password = System.getProperty("DB_PASSWORD");
		
		isNull(dbURL, "DB_URL") ;
		isNull(userName, "DB_USERNAME");
		isNull(password, "DB_PASSWORD");
		try {
			logger.debug("JDBC URL - "+dbURL);
			logger.debug("JDBC USERNAME - "+userName);
			logger.debug("JDBC PASSWORD - "+password);
			JDBCUtils.init(dbURL,userName,password);
		} catch (SQLException e) {
			logger.error(e);
			throw new LoaderInitializationException("Exception while" +
					" trying to initialize JDBC utility",e);
		}
		
	}
	
	
	/**
	 * Method initMailUtils.
	
	
	
	
	 * @throws LoaderInitializationException  * @throws IOException * @throws AddressException * @throws MailUtilsException */
	void initMailUtils() throws LoaderInitializationException{
		String hpsmPropFile = System.getProperty("HPSM_PROPERTIES_LOCATION");
		isNull(hpsmPropFile, "HPSM_PROPERTIES_LOCATION");
		//mailUtils = new MailUtils(hpsmPropFile);
	}
	/**
	
	 * @throws LoaderInitializationException  */
	public PMasterLoader() throws LoaderInitializationException{
		
		//initialize log4j
		initLog4J();
		logger.info("Log4j initialized");

		// load config file
		loadSystemProperties("CONFIG_LOCATION");
		logger.info("Config items Loaded to system properties");
		
		// load SQL queries
		loadSystemProperties("SQL_FILE_LOCATION");
		logger.info("SQL Queries Loaded to system properties");
		
		// initialize jdbc 
		initJDBCUtils();
		logger.info("JDBC initialized");
		
		// Initialize Mail Utility
		initMailUtils();
		logger.info("Mail Utils initialized");
		
		//SMO:User Story #7850
		initCompany();
//		try {
//			ConfigurationManager.getInstance().printCache();
//		} catch (MongoUtilsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	//SMO:User Story #7850
	public void initCompany() throws LoaderInitializationException{
		company_info = System.getProperty("COMPANY_INFO");
		if(company_info == null || "".equals(company_info)){
				System.err.println("company_info string has been set to null/blank");
				throw new  LoaderInitializationException("company_info property is not " +
						"set or is invalid."); 
			}
		
	}

	/**
	 * Method checkNull.
	 * @param name String
	 * @param value String
	
	
	 * @throws LoaderInitializationException * @throws IllegalArgumentException */
	public static void isNull(  String name, String value) throws LoaderInitializationException {
		
		if(value == null || "".equals(value)){
			if(name !=null || !"".equals(name)){
				System.err.println(name + " string has been set to null/blank");
				throw new  LoaderInitializationException(name+" property is not " +
						"set or is invalid."); 
			}else{
				System.err.println("?? property set to null/blank value");
				throw new  LoaderInitializationException("?? property is not " +
						"set or is invalid.");
			}
			  
		}		
	}

	/**
	 * 
	
	 * @throws LoaderException
	 * @throws LoaderInitializationException
	 * @throws OptionsException 
	 * @throws MongoUtilsException */
	void execute() throws LoaderException, LoaderInitializationException, MongoUtilsException, OptionsException {
		
		exit = false;
		// initialize QueueManager
		QueueManager queueMgr = new QueueManager();
		logger.info("QueueManager initialized");
		
		// create Extractor Manager to manage the extractor threads
		ExtractorManager extractorMgr = initExtractor(queueMgr);
		logger.info("Extractor Manager initialized");
		
		// create Worker Manager to manage the worker threads
		WorkerManager workerMgr = initWorkers(queueMgr, extractorMgr.getCurrentLoadId());
		logger.info("Worker Manager initialized");
		
		
		while(!exit){
			
			logger.info("Total Items waiting in queue : "+queueMgr.size());
			try {
				//check for any fatal errors that needs the loader to exit
				if(extractorMgr.isFatal() || workerMgr.isFatal()){
					exit = true;
					logger.fatal("Fatal Exception triggering shutdown");
					extractorMgr.stopExtractors();
					logger.warn("Extractor shutdown - Completed");
					workerMgr.stopWorkers();
					logger.warn("Worker shutdown - Completed");
					
					extractorMgr.logExtractionStatus("failed");
					workerMgr.logDBLoadStatus("failed");
					break;
					
				}else if (isExit()){
					//check for any shutdown requests 
					logger.warn("Signal to stop loader found. Initializing Loader shutdown");
					exit = true;
					extractorMgr.stopExtractors();
					logger.warn("Extractor shutdown - Completed");
					workerMgr.stopWorkers();
					logger.warn("Worker shutdown - Completed");
					
					extractorMgr.logExtractionStatus("stopped");
					workerMgr.logDBLoadStatus("stopped");
					break;
					
				}
				
				if(extractorMgr.isComplete() && queueMgr.isEmpty()){
					// check if extractor have completed processing and queue is empty
					exit = true;
					extractorMgr.stopExtractors();
					logger.warn("Extractor shutdown - Completed");
					workerMgr.stopWorkers();
					logger.warn("Worker shutdown - Completed");
					if (extractorMgr.isComplete()){
						logger.info("Extraction complete successfully. All Extraction Threads are now in shutdown state.");
						extractorMgr.logExtractionStatus("successful");
					}
					
					if (workerMgr.isComplete()){
						logger.info("Loading completed successfully. All Worker Threads are now in shutdown state.");
						workerMgr.logDBLoadStatus("successful");
					}
					break;
				}
				
				
				Thread.sleep((long) (Math.random()*5) * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	/**
	 * Method initWorkers.
	 * @param queueMgr QueueManager
	 * @return WorkerManager
	 * @throws LoaderException
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	WorkerManager initWorkers(QueueManager queueMgr, ObjectId currentLoadId) throws LoaderException, MongoUtilsException, OptionsException{
		String workerCount = System.getProperty("WORKER_THREAD_COUNT");		 
		int workerThreadCount = 4; 
		if (workerCount != null || "".equals(workerCount)){
			workerThreadCount = Integer.parseInt(workerCount);
		}
		logger.info("No of worker threads is set to "+workerThreadCount);
		WorkerManager wMgr = new WorkerManager(queueMgr, currentLoadId,  workerThreadCount);
		wMgr.startWorkers();
		return  wMgr;	
	}
	
	/**
	 * Method initExtractor.
	 * @param queueMgr QueueManager
	 * @return ExtractorManager
	 * @throws LoaderException
	 * @throws LoaderInitializationException
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	static ExtractorManager initExtractor(QueueManager queueMgr) throws 
	LoaderException, LoaderInitializationException, OptionsException, MongoUtilsException{
		String isDelta = System.getProperty("IS_DELTA_MODE");
		String isRepublish = System.getProperty("IS_REPUBLISH_MODE");
		boolean isDeltaMode = false;
		boolean isRepublishMode = false;
		
		try{
			isNull("IS_REPUBLISH_MODE", isRepublish);
		}catch(LoaderInitializationException e){
			isRepublishMode = false;
			logger.warn("Republish mode not specified. Hence setting to default value (false)");
		}
		isRepublishMode  =  Boolean.parseBoolean(isRepublish);
		try{
			isNull("IS_DELTA_MODE", isDelta);
		}catch(LoaderInitializationException e){
			logger.warn("Execution mode not specified. Hence setting to default mode(DELTA)");
		}
		logger.info("Delta execution mode set to "+isDelta);
		
		isDeltaMode = Boolean.parseBoolean(isDelta);
		
		ExtractorManager emgr = new ExtractorManager(queueMgr, isDeltaMode, isRepublishMode);
		emgr.startExtractors();
		return emgr;
	}
	
	private static boolean isExit(){
		if(stopFile.exists() && stopFile.isFile()){
			logger.warn("Received signal to stop loader execution. Initiating shutdown");
			return true;
			
		}
		return false;
	}
	
	/**
	 * Replaces Starting point.
	 * 
	 * @param args
	 *            are ignored
	 */
	public static void main(String[] args) {
		System.setOut( new TimeStampWriter( System.out ) );
		System.setErr( new TimeStampWriter( System.err ) );
		PMasterLoader psdm = null;
		try {
			psdm = new PMasterLoader();
			Thread t = new Thread(psdm,"PMLoader");
			t.start();
		} catch (LoaderInitializationException e) {
			notifyPSDMFailed(e);
			e.printStackTrace();
			psdm = null;
		}
		/*try {
		if(psdm != null){
		psdm.execute();
		} else
			System.out.println("Loader failed during initialization");
	}  catch (LoaderException e) {
		e.printStackTrace();
		notifyPSDMFailed(e);
	} catch (LoaderInitializationException e) {
		e.printStackTrace();
		notifyPSDMFailed(e);
	} catch (MongoUtilsException e) {
		e.printStackTrace();
		notifyPSDMFailed(e);
	} catch (OptionsException e) {
		e.printStackTrace();
		notifyPSDMFailed(e);
	}
*/  }

	/**
	 * Generates an HPSM incident ticket if the job fails, with the exception 
	 * stact trace provided.
	 * 
	
	 * @param t Throwable
	 */
	static void notifyPSDMFailed(Throwable t) {
		t.printStackTrace();
		logger.error(t);
	}
	@Override
	public void run() {
		System.out.println("inside run method");
		boolean processingComplete= false;
		String batchMode = System.getProperty("BATCH_MODE");
		String sleepTime= System.getProperty("SLEEP_TIME");
		boolean isBatchMode = Boolean.parseBoolean(batchMode);
		long sleep_time=0l;
		while (!processingComplete && !isExit()) {

			try {
				
				// Save start status in DB
				this.writePMUpdateStatus("STRT");
				
				Calendar cal=new GregorianCalendar();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 3, 0, 0);
				long time3amUTCNextday= cal.getTimeInMillis();
				
				this.execute();
				
				// Save end status in DB
				this.writePMUpdateStatus("DONE");
				
				// After successful run , sleep for service mode
				if(!isBatchMode){
					if(sleepTime!=null){
						sleep_time = Long.parseLong(sleepTime);
					}else
						//sleep_time=this.getSleepTime(); commented as now PM loader needs to run 3 AM everyday
						sleep_time = this.get3AMSleepTime(time3amUTCNextday);
				if(sleep_time>0){
					try {
						HashMap<String , Object> logMap = new HashMap<String, Object>();
						logMap.put("lastmodified", System.currentTimeMillis());
						DiagnosticLogger.setEnabled(true);
						DiagnosticLogger.log("pmloader", logMap);
					} catch (MongoUtilsException e) {
						//just eat this exception to avoid any unwanted things in loader so that this will be taken care by healthcheck.
					}
					try {
						logger.info("Sleep Time after last successful run (in ms): " + sleep_time);
						Thread.sleep(sleep_time);
						} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				}else{
					processingComplete =true;
				}
			} catch (LoaderException e) {
				notifyPSDMFailed(e);
				e.printStackTrace();
			} catch (LoaderInitializationException e) {
				notifyPSDMFailed(e);
				e.printStackTrace();
			} catch (MongoUtilsException e) {
				notifyPSDMFailed(e);
				e.printStackTrace();
			} catch (OptionsException e) {
				notifyPSDMFailed(e);
				e.printStackTrace();
			} catch (SQLException e) {
				notifyPSDMFailed(e);
				e.printStackTrace();
			}

		}
	}
	/**
	 * This method returns sleep time for PM loader
	 * If Current time is less than 3AM UTC, then return sleep time for time3amUTCNextday-end,
	 * otherwise sleep time will be 10 secs 
	 * @param time3amUTCNextday
	 * @return
	 */
	private long get3AMSleepTime(long time3amUTCNextday) {
		long end = System.currentTimeMillis();
		if(time3amUTCNextday>end){
			return time3amUTCNextday-end;
		}
		return 10000;
	}

	/**
	 * @return
	 * @throws LoaderException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	private Long getSleepTime() throws LoaderException,
			OptionsException, MongoUtilsException {
		long sleep_time = 0L;
		DBObject query = new BasicDBObject();
		DBObject sortFields = new BasicDBObject("end_time", -1);

		Options options = new Options();
		options.setContentType("productmaster");
		options.setQuery(query);
	
		ProductMasterDAO pmasterDao = new ProductMasterDAO();
		
		DBCursor results = pmasterDao.getLogEntry(options);
		
		results.sort(sortFields);

		if (results != null && results.hasNext()) {
			DBObject result = results.next();
			Date end_time = (Date) result.get("end_time");
			Date start_time = (Date) result.get("start_time");

			long lastrunduration = end_time.getTime() - start_time.getTime();
			long last_run_duration_min = TimeUnit.MILLISECONDS
					.toMinutes(lastrunduration);
			String extraction_status = (String) result.get("extraction_status");
			String loading_status = (String) result.get("loading_status");
		
			if (extraction_status.equals("successful")
					&& loading_status.equals("successful")
					&& last_run_duration_min < 1440) {
				long sleep_time_min = 1440 - last_run_duration_min;
				sleep_time = TimeUnit.MINUTES.toMillis(sleep_time_min);
			}

		}
		return sleep_time;
	}

	/**
	 * Method to insert status of PM Loader run in Database
	 * @param status
	 * @throws SQLException
	 */
	private void writePMUpdateStatus(String status) throws SQLException {
		PreparedStatement pstmt = null;
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();

			String insertQuery = "INSERT into c_mv_refresh_log (mv_name,EXEC_TIME,status,comments) values(?,?,?,?)";

			pstmt = connection.prepareStatement(insertQuery);

			pstmt.setString(1, "PMasterUpdate");
			pstmt.setTimestamp(2,
					new java.sql.Timestamp(System.currentTimeMillis()));

			if ("STRT".equalsIgnoreCase(status)) {
				// String startSQL ="INSERT into c_mv_refresh_log (mv_name,EXEC_TIME,status,comments) values('PMasterUpdate',sysdate,'STRT','CDSP PMaster loader job has been started successfully.')";
				pstmt.setString(3, "STRT");
				pstmt.setString(4,PMasterLoader.company_info+"CDSP PMaster loader job has been started successfully.");
			} else {
				// String endSQL ="INSERT into c_mv_refresh_log (mv_name,EXEC_TIME,status,comments) values('PMasterUpdate',sysdate,'DONE','CDSP PMaster loader job has been completed successfully.')";
				pstmt.setString(3, "DONE");
				pstmt.setString(4,PMasterLoader.company_info+"CDSP PMaster loader job has been completed successfully.");
			}
		} finally {
			pstmt.executeUpdate();
			pstmt.close();
			connection.close();
		}
	}
	
}
