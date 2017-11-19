/**
 * 
 */
package com.hp.loader.priorityLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.loader.history.HistoryException;
import com.hp.loader.history.TokenHistoryHandler;
import com.hp.loader.source.Source;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.Log;
import com.hp.loader.utils.SendMail;
import com.hp.loader.utils.TicketCreator;
import com.hp.loader.workItem.IWorkItem;

/**
 * @author dahlm
 *
 */
public class PriorityLoader implements Runnable {
	
	public static final String NOT_FOUND_MSG 				= " was not found or is incorrect.";

	public static final String LOG4JCONFIG          = "log4jConfig";

	// SSL constants
	public static final String SSL_TRUST_STORE_LABEL 		= "sslTrustParam: ";
	public static final String SSL_KEY_STORE_LABEL 			= "sslStoreParam: ";
	public static final String SSL_KEY_PASSWORD_LABEL 		= "sslPasswordParam: ";
	public static final String SSL_KEY_STORE_ATTR_NAME		= "ssl_key_store";
	public static final String SSL_TRUST_PASSWORD_LABEL 	= "sslTrustPasswordParam: ";
	public static final String SSL_TRUST_STORE_ATTR_NAME 	= "ssl_trust_store";
	public static final String SSL_KEY_PASSWORD_ATTR_NAME 	= "ssl_key_password";
	public static final String THE_KEY_TRUST_SSL_STORE_AT 	= "The key/trust ssl store at '";
	public static final String SSL_TRUST_PASSWORD_ATTR_NAME = "ssl_trust_password";

	// email constants
	public static final String TO_ADDR 		= "to_addr";
	public static final String TO_ADDRS 	= "to_addrs";
	public static final String FROM_ADDR 	= "from_addr";
	public static final String SMTP_HOST 	= "smtp_host";
	public static final String SMTP_PORT 	= "smtp_port";

	
	// history
	public static final String HISTORY_FILE_NAME="history_file_name";

	// loader config fields
	public static final String NUM_WORKER_THREADS = "num_worker_threads";
	public static final String NUM_PRIORITIES = "num_priorities";
	public static final String SOURCE_CLASS = "source_class";
	public static final String DESTINATION_CLASS = "destination_class";
	public static final String WORK_ORDER_WAIT_TIME = "work_order_wait_time";
	public static final String EVENTS_ARE_SEQUENTIAL = "events_are_sequential";

	// Singleton of the loader
	private static PriorityLoader priorityLoader = null;

	// base string for this loader. There should be a corresponding <loaderType>config.xml file in the 
	// config directory 
	String loaderType = null;

	// reader for the config.xml file
	ConfigurationReader config = null;

	// reference to the historyHandler
	TokenHistoryHandler historyHandler = null;
	
	// queueManager
	WIQueueManager queueManager = null;

	// number of ways we have been requested to pause
	int numPauseReq = 0;
	Object pauseCountSync = new Object();

	// list holding the WIWorder threads.  Used for indicating we should stop
	ArrayList<WIWorker> workers = new ArrayList<WIWorker>();

	// Source object providing workItems
	Source source = null;

	// workOrder messaging
	StringBuffer workOrderMsg = new StringBuffer("Total WorkOrder Items: ");
	int workOrderBase = workOrderMsg.length();

	// wait time between work order requests
	int workOrderWaitTime = 60;

	// thread control flags
	boolean exiting = false;
	boolean exited = false;

	//Re-Design Variables start
	
	//Re-Design : Made numWorkerThreads as member variable for Batch Mode to check if all workers are done or not
	private int numWorkerThreads = 0;	

	// Re-Design: Flag for Batch Mode
	public static boolean batchMode = false;
	
	// Re-Design: Flag to indicate whether to retry on Error or not
	public static boolean retryOnError = true;

	// Re-Design : To indicate that workers have completed processing Last Item
	private static boolean processingComplete = false;

	public static final String RETRY_ON_ERROR = "retryOnError";
	public static final int MAX_WORKER_THREADS = 4;
	
	/**
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws AddressException 
	 * @throws HistoryException 
	 * 
	 */
	public PriorityLoader(ConfigurationReader config) throws ParserConfigurationException, SAXException, IOException, AddressException, HistoryException {

		this.config = config;

		String log4jConfig = config.getAttribute(LOG4JCONFIG);
		if ((log4jConfig == null) || (log4jConfig.length() == 0)) {
			throw new IllegalArgumentException(LOG4JCONFIG+NOT_FOUND_MSG);
		}

		if(log4jConfig.endsWith(".xml")){
			DOMConfigurator.configure(log4jConfig);
			System.out.println("Log4j: xml configuration initiated");
		}
		else if(log4jConfig.endsWith(".properties")){
			PropertyConfigurator.configure(log4jConfig);
			System.out.println("Log4j: properties configuration initiated");
		}else{
			PropertyConfigurator.configure(log4jConfig);
		}

		// initialize the two loggers
		//Log.setLevel(Level.DEBUG);
		//ThreadLog.setLevel(Level.DEBUG);
		
		Log.info("Loading ssl configuration");

		loadSSLParameters();

		Log.info("Loading sendMail configuration");
		loadSendMail();

		try {
			numWorkerThreads = Integer.parseInt(config.getAttribute(NUM_WORKER_THREADS));
			if (numWorkerThreads < 1) 
				throw new NumberFormatException();
			else if(numWorkerThreads>MAX_WORKER_THREADS){ 
				numWorkerThreads = MAX_WORKER_THREADS;
				Log.info(NUM_WORKER_THREADS+"exceeds maximum limit"+MAX_WORKER_THREADS+". Using default of "+MAX_WORKER_THREADS);
			}
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(NUM_WORKER_THREADS+NOT_FOUND_MSG);
		
		}

		// Number of Priorities
		int numPriorities = 0;
		try {
			numPriorities = Integer.parseInt(config.getAttribute(NUM_PRIORITIES));
			if (numPriorities < 1) 
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(NUM_PRIORITIES+NOT_FOUND_MSG);
		}

		// Create the queueManager
		queueManager = new WIQueueManager(numPriorities, numWorkerThreads);

		// Gets the name of the history file
		String historyFileName = config.getAttribute(HISTORY_FILE_NAME);
		if (historyFileName == null) {
			throw new IllegalArgumentException(HISTORY_FILE_NAME + NOT_FOUND_MSG);
		}
		
		Log.info("Creating history Handler: "+historyFileName);
		
		
		
		// Reads and loads the history file
		historyHandler = TokenHistoryHandler.getInstance(historyFileName, numPriorities, queueManager, 
				"true".equalsIgnoreCase(config.getAttribute(EVENTS_ARE_SEQUENTIAL)));

		try {
			int waitTime = Integer.parseInt(config.getAttribute(WORK_ORDER_WAIT_TIME));
			//Re-Design : If Work_order_wait_time is negative, it is in Batch Mode
			if(waitTime<0){
				Log.info("This is Batch Mode Processing");
				batchMode =true;
			}
			else if (waitTime > 60) { 
				Log.info("Setting "+WORK_ORDER_WAIT_TIME+" to "+waitTime);
				workOrderWaitTime = waitTime;
			}
		} catch (NumberFormatException nfe) {
			Log.info(WORK_ORDER_WAIT_TIME+NOT_FOUND_MSG+" Using default of 60");
		}

		// Creates the WI Worker threads and gets them running
		for(int i=0; i<numWorkerThreads; i++) {
			workers.add(new WIWorker(queueManager, source));
		}
		if(config.getAttribute(RETRY_ON_ERROR)!=null && config.getAttribute(RETRY_ON_ERROR)!=""){
				retryOnError =Boolean.parseBoolean(config.getAttribute(RETRY_ON_ERROR).toString());
		}
	}

	/**
	 * start is used to control when the source is initialized.  Don't want it to beging before the priorityLoader is instanciated.
	 */
	public void start() {
		// get Source instance and initialize it 
		source = (Source)getClassInstance(SOURCE_CLASS);
	}

	/**
	 * exit is called to indicate that an exit is requested.
	 */
	public void exit() {
		exiting = true;
		int numTries = 0;

		Log.info("Loader exit requested.");

		// notify the source that we are exiting
		source.exiting();

		// notify the workers that we are exiting
		Log.info("Shutting down "+workers.size()+" workers. Try "+numTries);
		for(WIWorker worker : workers) {
			worker.exit();
		}
	}

	/**
	 * method to create the Source/Destination objects for this loader
	 * @param classField
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Object getClassInstance(String classField) throws IllegalArgumentException {
		try {
			// get the name from the config file
			String className = config.getAttribute(classField);
			
			
			Log.info("Create source instance "+classField+" : "+className);

			if (className == null || className.length() == 0) {
				throw new IllegalArgumentException(classField+NOT_FOUND_MSG);
			}

			Class newClass = Class.forName(className);
			if (newClass == null) {
				// didn't find a class for this name
				throw new IllegalArgumentException(classField+": No class reference found.");
			}
			ArrayList classTypes = new ArrayList();
			ArrayList parms = new ArrayList();

			classTypes.add(ConfigurationReader.class);
			parms.add(config);
			
			Constructor c = newClass.getConstructor((Class[])classTypes.toArray(new Class[classTypes.size()]));
			if (c == null) {
				throw new IllegalArgumentException(classField+": Cannot instanciate instance.");
			}
			return c.newInstance(parms.toArray());
		} catch (InvocationTargetException ite) {
			Throwable target = ite.getTargetException();
			Throwable cause = ite.getCause();
			if (cause instanceof IllegalArgumentException) {
				throw (IllegalArgumentException)cause;
			} else if (target instanceof IllegalArgumentException) {
				throw (IllegalArgumentException)target;
			} else if (cause != null) {
				throw new IllegalArgumentException("Class creation failed: "+cause.getClass()+" : "+cause.getMessage());
			} else {
				throw new IllegalArgumentException("Class creation failed with exception: "+ite.getClass()+" : " + ite.getMessage());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(classField+": Creation failed : "+e.getMessage(),e);
		}
	}

	/**
	 * Loads the SSL parameters for https protocol and
	 * assigns them to respective system properties
	 * @return <code>true</code> if the configuration file was loaded successfully;
	 *         <code>false</code> otherwise.
	 * @throws FileNotFoundException 
	 */
	protected void loadSSLParameters() throws FileNotFoundException {
		String attrValue = null;
		String sslStoreParam = null;
		String sslPasswordParam = null;
		String sslTrustParam = null;
		String sslTrustPasswordParam = null;
		StringBuffer errorMessage = null;
		StringBuffer infoMessage = null;
		File fileHandler = null;

		if ((attrValue = config.getAttribute(SSL_KEY_STORE_ATTR_NAME)) == null) {
			throw new FileNotFoundException("Attribute "+SSL_KEY_STORE_ATTR_NAME+" is incorrect or missing");
		}
		fileHandler = new File(attrValue);
		if (!fileHandler.isFile()) {
			errorMessage = new StringBuffer(THE_KEY_TRUST_SSL_STORE_AT).append(
					fileHandler.getAbsolutePath()).append(NOT_FOUND_MSG);
			throw new FileNotFoundException(errorMessage.toString());
		}
		sslStoreParam = attrValue;
		if (Log.isInfoEnabled()) {
			infoMessage = new StringBuffer(SSL_KEY_STORE_LABEL).append(sslStoreParam);
			Log.info(infoMessage.toString());
			infoMessage = null;
		}
		fileHandler = null;

		attrValue = config.getAttribute(SSL_KEY_PASSWORD_ATTR_NAME);
		if (!"".equals(attrValue))
			sslPasswordParam = attrValue;
		if (Log.isInfoEnabled()) {
			infoMessage = new StringBuffer(SSL_KEY_PASSWORD_LABEL).append(sslPasswordParam);
			Log.info(infoMessage.toString());
			infoMessage = null;
		}

		attrValue = config.getAttribute(SSL_TRUST_STORE_ATTR_NAME);
		fileHandler = new File(attrValue);
		if (!fileHandler.isFile()) {
			errorMessage = new StringBuffer(THE_KEY_TRUST_SSL_STORE_AT).append(
					fileHandler.getAbsolutePath()).append(NOT_FOUND_MSG);
			throw new FileNotFoundException(errorMessage.toString());
		}
		sslTrustParam = attrValue;
		if (Log.isInfoEnabled()){
			infoMessage = new StringBuffer(SSL_TRUST_STORE_LABEL).append(sslTrustParam);
			Log.info(infoMessage.toString());
			infoMessage = null;
		}
		fileHandler = null;

		attrValue = config.getAttribute(SSL_TRUST_PASSWORD_ATTR_NAME);
		if (!"".equals(attrValue))
			sslTrustPasswordParam = attrValue;
		if (Log.isInfoEnabled()) {
			infoMessage = new StringBuffer(SSL_TRUST_PASSWORD_LABEL).append(sslTrustPasswordParam);
			Log.info(infoMessage.toString());
			infoMessage = null;
		}

		// Assigns the values to the system properties for SSL
		System.setProperty("javax.net.ssl.keyStore",sslStoreParam);
		System.setProperty("javax.net.ssl.trustStore",sslTrustParam);
		System.setProperty("javax.net.ssl.keyStoreType","jks");
		System.setProperty("javax.net.ssl.trustStoreType","jks");
		System.setProperty("javax.net.ssl.keyStorePassword",sslPasswordParam);
		System.setProperty("javax.net.ssl.trustStorePassword",sslTrustPasswordParam);
	}

	private int loadWorkOrder() throws ProcessingException {
		// Gets the list of work items
		ArrayList<IWorkItem> workItemsList = source.getWorkItemsSince(historyHandler.getStartToken());

		if (Log.isInfoEnabled()) {
			workOrderMsg.setLength(workOrderBase);
			workOrderMsg.append(workItemsList.size());
			Log.info(workOrderMsg.toString());
		}

		int numProcessed = 0;

		// populate queue
		if (workItemsList.size() > 0) {

			// Stop the queuemanager from allowing any items to be popped off until this set has been processed.
			synchronized(queueManager) {
				for(IWorkItem workItem : workItemsList) {
					if (workItem.schedule()) {
						// this workItem scheduled so add it to the number processed
						numProcessed++;
					}
				}
			}
		}
		return numProcessed;
	}
	/**
	 * loads the SendMail configuration information
	 * @param sendMailElem
	 * @throws AddressException 
	 */
	public void loadSendMail() throws AddressException {
		String fromAddr = getValue(config.getElement(FROM_ADDR));
		NodeList toAddrs = config.getElements(TO_ADDR);
		String server = getValue(config.getElement(SMTP_HOST));
		String port = getValue(config.getElement(SMTP_PORT));

		Node node = null;
		// Loads the <content> elements which defines
		// the valid Concentra content classes for the
		// documents to be uploaded
		String[] toAddresses = new String[toAddrs.getLength()];
		for (int index = 0; index < toAddrs.getLength(); index++ ) {
			node = toAddrs.item(index);
			if (node instanceof Element) {
				toAddresses[index] = getValue((Element)node);
			}
		}

		SendMail.setupSendMail(server, port, fromAddr, toAddresses);
	}

	/**
	 * Sends mail as per configuration loaded from Loader config file
	 * @param msgHeader - Subject of the mail 
	 * @throws msgBody  - Message body
	 */
	public boolean sendEmail(String msgHeader, String msgBody) {				
		try {
			SendMail.sendMessage(msgHeader, msgBody);
			return true;
		} catch (MessagingException me) {
			Log.error("SendMail failed to send message: "+Log.getExceptionMsgForLog(me));
			ErrorLog.logStackTrace(me);
			return false;
		}
	}
	
	
	public void run() {
		boolean keepRunning = true;
		int lastNumLoaded = 0;
		int numFails = 0;
		long startTime = System.currentTimeMillis();
		// Recover the queue back to the last known state
		try {
			synchronized(queueManager) {
				if (Log.isInfoEnabled()) {
					Log.info("Recover queue to last state "+historyHandler.getStartToken()+":"+historyHandler.getMaxProcessed());
				}
				boolean recovered = false;
				while (!recovered) {
					try {
						initNonProcessedItems();
						recovered = true;
					} catch (ProcessingException pe) {
						if (!pe.shouldRetry()) {
							throw pe;
						} 
						else if(pe.shouldRetry() && !retryOnError){ //Re-Design
							exitOnError(pe);
							break;
						}
						else {
							try {
								Thread.sleep(15*1000);
							} catch (InterruptedException ie){}
						}
					}
				}
				
				if (Log.isInfoEnabled()) {
					Log.info("Recovered queue to last state. nextToken:"+historyHandler.getStartToken()+" RecoveryTime (ms): "+
							(System.currentTimeMillis()-startTime));
				}

			}
		} catch (IOException ioe) {
			ErrorLog.logStackTrace(ioe);
			ioe.printStackTrace();
			Log.fatal(Log.getExceptionMsgForLog(ioe));
			System.exit(2);
		} catch (ProcessingException pe) {
			ErrorLog.logStackTrace(pe);
			pe.printStackTrace();
			Log.fatal(Log.getExceptionMsgForLog(pe));
			System.exit(3);
		}
		
		while (!exiting) {
			
				startTime = System.currentTimeMillis();
				try {
					lastNumLoaded = loadWorkOrder();
					// just successfully loaded a workOrder
					numFails = 0;

				}
				// Don't want this thread to exit on an Exception in service
				// mode so we catch all exceptions, and
				// loop until told to exit.
				catch (ProcessingException pe) {
					// failed to get the workOrder set lastNumLoaded to 0 so it
					// will sleep
					lastNumLoaded = 0;
					if (!pe.shouldRetry()) {
						ErrorLog.logStackTrace(pe);
						TicketCreator
								.createTicket("PriorityLoader for "
										+ loaderType
										+ " unrecoverable error. Failed to get workorder.");
						// notify the workers that we are exiting
						exit();
						return;
					}//Re-Design
					else if (pe.shouldRetry()) {
					if(!retryOnError){
						exitOnError(pe);
					} else{
						// sleep until it's time to run again
						try {
							Thread.sleep(15*1000);
						} catch (InterruptedException ie) {}	
						Log.error(Log.getExceptionMsgForLog(pe));
						ErrorLog.logStackTrace(pe);
						continue;
					}
					}
				} catch (Exception e) {
					// Logging the exception and trying again.
					Log.error(Log.getExceptionMsgForLog(e));
					ErrorLog.logStackTrace(e);
				}
				// see if we should sleep
				// don't sleep if we just loaded something as we may not have gotten it all.
				long timeToSleep = (lastNumLoaded != 0) ? 0	: ((workOrderWaitTime * 1000) - (System.currentTimeMillis() - startTime));
				// Re-Design : one more check for Batch Mode
				if (timeToSleep > 0) {
					if(batchMode && ManualRepublish.isManualRepubDone && queueManager.isEmpty()){
						Log.info("No item to process. Exiting as it is Batch Mode Processing");
						// call exit to have the system shutdown.  
						exit();
						processingComplete = true;
					}
					else{
					// sleep until it's time to run again
					try {
						Thread.sleep(timeToSleep);
					} catch (InterruptedException ie) {
					}
				}
			}
		}
	}

	/**
	 * method to make queueManager available to add on classes
	 * @return
	 */
	public WIQueueManager getQueueManager() {
		return queueManager;
	}
	
	/**
	 * helper method to get a trimmed value or an empty string
	 * @param e
	 * @return
	 */
	private String getValue(Element e) {
		if (e == null) {
			return "";
		} else {
			return e.getTextContent().trim();
		}
	}

	/**
	 * initNonProcessedItems loads all workItems from history.min+1 to history.max, determines which
	 * ones have not been processed yet, based on history numbers, and queues them into the queue. 
	 * @throws ExtractorException
	 * @throws IOException 
	 * @throws ProcessingException 
	 */

	protected void initNonProcessedItems() throws IOException, ProcessingException {
		long maxToken = historyHandler.getMaxProcessed();

		while (historyHandler.getStartToken().longValue() < maxToken && (loadWorkOrder() != 0)) { 
			// 
			;
		}
	}

	/**
	 * return true if this is the last event in the queue
	 * @return
	 */
	public boolean isLastQueuedEvent() {
		return queueManager.isLastEvent();
	}
	
	/**
	 * report if the loader is paused
	 * @return
	 */
	public boolean isPaused() {
		return numPauseReq > 0;
	}

	/**
	 * pauseQueue tells the WIQueueManager to pause and doesn't return until it is paused
	 */
	public void pauseQueue(){
		synchronized (pauseCountSync) {
			numPauseReq++;
		}
		queueManager.pause();
	}

	public void resumeQueue() {
		synchronized (pauseCountSync) {
			if (numPauseReq > 0) {
				numPauseReq--;
			}
		}
		if (numPauseReq == 0) {
			queueManager.resume();
		}
	}



	/**
	 * need a singleton of the priorityloader
	 * @param args
	 * @return
	 * @throws HistoryException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws AddressException 
	 */
	static public synchronized PriorityLoader getInstance(ConfigurationReader config) throws AddressException, ParserConfigurationException, SAXException, IOException, HistoryException {
		if (priorityLoader == null) {
			priorityLoader = new PriorityLoader(config);
			// now start the source
			priorityLoader.start();
		}
		return priorityLoader;
	}

	/**
	 * method used to get access to the loader object to determine queue state
	 * @return
	 */
	static public PriorityLoader getInstance() {
		return priorityLoader;
	}
	
	static public void pause() {
		priorityLoader.pauseQueue();
	}

	static public void resume() {
		priorityLoader.resumeQueue();
	}

	static public boolean isLastEvent() {
		return priorityLoader.isLastQueuedEvent();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create the priority loader
		try {
			if (args.length < 1) {
				System.out.println("Missing loader type parameter");
				System.exit(1);
			}

			ConfigurationReader config = null;
			String configFileName = args[0]+"Config.xml";
		
			config = new ConfigurationReader("C:\\Users\\RADHA\\PROJECTS\\CDSPLUS\\cdsp-priority-loader-spacedog\\concentraConfig.xml");
			String stopFileName = args[0]+".stop";
			File stopFile = new File(stopFileName);
			if (stopFile.exists()) {
				System.out.println("Exiting due to stop file. "+stopFile.getAbsolutePath());
				System.exit(2);
			}

			String pauseFileName = args[0]+".pause";
			File pauseFile = new File(pauseFileName);

			String resumeFileName = args[0]+".resume";
			File resumeFile = new File(resumeFileName);
			 
			
			PriorityLoader loader = PriorityLoader.getInstance(config);

			Thread t = new Thread(loader,"Loader");
			t.setDaemon(true);
			t.start();

			if (pauseFile.exists()) {
				pause();
				// we are paused so delete the pause file
				pauseFile.delete();
			}

			// now watch for a stop file 
			while(!stopFile.exists()) {
				if (pauseFile.exists()) {
					PriorityLoader.pause();
					pauseFile.delete();
				} else if (resumeFile.exists()) {
					PriorityLoader.resume();
					resumeFile.delete();
				}
				if (loader.isPaused()) {
					Log.info(" Loader is paused ");
				}
				if (batchMode && processingComplete) { 
					// Re-Design : Stopping Loader for Batch Mode Processing
					Log.info("Batch Mode Processing Complete.");
					Log.info("Exited");
					System.exit(0);

				} else{
					try {
						Thread.sleep(60000);
					} catch (InterruptedException ie){}
				}	
			}
			Log.info("Exiting due to stop file requested. "+stopFile.getAbsolutePath());
			
			Log.info("Notify Source to stop.");
			// tell the loader we are exiting
			loader.exit();
			
			Log.info("Pausing workers.");
			
			// Now pause the queue Manager (waiting until all of the workers have stopped processing
			pause();
			
			Log.info("Exited ");
			System.exit(0);
			
		} catch (IllegalArgumentException iae) {
			ErrorLog.logStackTrace(iae);
			System.out.println(Log.getExceptionMsgForLog(iae));
			System.out.println("Usage: java priorityLoader.PriorityLoader <loaderType> [-conf <configDir>]");
			System.exit(1);
		} catch (Exception e) {
			Log.fatal(Log.getExceptionMsgForLog(e));
			ErrorLog.logStackTrace(e);
			System.err.println(Log.getStackTrace(e));
			System.exit( 1 );
		}
	}
	public static void exitOnError(Throwable t){
			ErrorLog.logStackTrace(t);
			Log.info("Exiting on Error based on Configuration");
			PriorityLoader.getInstance().exit();
			Log.info("Exited");
			System.exit(1);
	}
	
}
