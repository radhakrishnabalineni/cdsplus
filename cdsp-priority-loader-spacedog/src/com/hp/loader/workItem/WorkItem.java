package com.hp.loader.workItem;


import java.io.IOException;
import java.util.Iterator;

import com.hp.loader.history.HistoryException;
import com.hp.loader.history.HistoryHandler;
import com.hp.loader.history.HistoryToken;
import com.hp.loader.history.TokenHistoryHandler;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.Log;
import com.hp.loader.utils.ThreadLog;


abstract public class WorkItem implements IWorkItem {

	// max_fail_count is the number of tries for load/store are made.
	private static final int MAX_FAIL_COUNT = 2;

	// failedCount is used to track how many times this has failed the current phase
	protected int failedCount = 0;

	// unique identifier of an event
	protected Long token;

	// eventType indicates what is to happen for this event
	protected String eventType;

	// Attributes added to handle the Loader new approach base on
	// priorities
	protected Integer priority;

	// valid indicates that this Item is still valid and should continue
	protected boolean valid;

	// running indicates this item is no longer in the queue, but is in a worker
	protected boolean running;

	// Item that must happen before this item
	protected IWorkItem prevItem;

	// Item that is happening after this item
	protected IWorkItem postItem;

	// history token for the history update of this item
	protected HistoryToken historyToken;

	// flag to indicate that an exit has been requested
	private boolean exiting = false;

	// buffer for outputting messages
	private StringBuffer processMsg = new StringBuffer();
	private int baseMsgLength = 0;

	public WorkItem( Long token, Integer priority, String eventType ) {
		// Added to handle the Loader new approach base on
		// priorities
		this.valid = true;
		this.token = token;
		this.priority = priority;
		this.eventType = eventType;
		prevItem = null;
		postItem = null;
		historyToken = null;
	}

	/**
	 * vals contains three string representing the token, priority, and eventType
	 * @param vals
	 * @throws ProcessingException
	 */
	public WorkItem(Iterator<String> vIt) throws ProcessingException {
		valid = true;
		prevItem = null;
		postItem = null;
		historyToken = null;		
		token = Long.valueOf(getFieldValue(vIt, "token"));
		priority = Integer.valueOf(getFieldValue(vIt, "priority"));
		eventType = getEvent(getFieldValue(vIt,"eventType"));
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#exit()
	 */
	public void exit() {
		exiting = true;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#failed()
	 */
	public boolean failed() {
		failedCount++;
		return failedCount >= MAX_FAIL_COUNT;
	}


	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#clearFailed()
	 */
	public void clearFailed() {
		failedCount = 0;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#cleanup()
	 */
	abstract public void cleanup();

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getIdentifier()
	 */
	abstract public String getIdentifier();

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getPackageType()
	 */
	abstract public String getPackageType();

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getTag()
	 */
	abstract public String getTag();

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getEventType()
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * reset event type so it can be tested using ==
	 * @param eventType
	 * @return
	 */
	static public String getEvent(String eventType) {
		if (eventType.equalsIgnoreCase(IWorkItem.EVENT_UPDATE)) {
			return IWorkItem.EVENT_UPDATE;
		} else if (eventType.equalsIgnoreCase(IWorkItem.EVENT_DELETE)) {
			return IWorkItem.EVENT_DELETE;
		} else if (eventType.equalsIgnoreCase(IWorkItem.EVENT_TOUCH)) {
			return IWorkItem.EVENT_TOUCH;
		} else {
			ThreadLog.warn("Unknown eventType: "+eventType+" defaulting to Update");
			return IWorkItem.EVENT_UPDATE;
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getToken()
	 */
	public Long getToken() {
		return token;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#manageDuplicate(com.hp.loader.workItem.WorkItem)
	 */
	public void manageDuplicate(IWorkItem oldWorkItem) {
		//
		WorkItem.resolveDuplicate(this, oldWorkItem);
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#manageDuplicate(com.hp.loader.workItem.WorkItem)
	 */
	static public void resolveDuplicate(IWorkItem newWorkItem, IWorkItem oldWorkItem) {
		// tell the old workItem this item is happening next
		synchronized(oldWorkItem) {
			if (oldWorkItem.isRunning() || oldWorkItem.isValid()) {
				if (Log.isInfoEnabled()) {
					Log.info("Setting duplicate "+oldWorkItem.getToken()+"  "+newWorkItem.getToken()+" : "+newWorkItem.getIdentifier());
				}
				oldWorkItem.setPostItem(newWorkItem);

				// set the previous item for the newWorkItem to oldWorkItem
				newWorkItem.setPrevItem(oldWorkItem);

				//Change the priority of the extraction item to the highest priority 
				if ((oldWorkItem.getPriority().intValue() >= 0) && newWorkItem.getPriority() > oldWorkItem.getPriority()) {
					Log.info(newWorkItem.getIdentifier() +" Moving priority from "+newWorkItem.getPriority() +" to "+oldWorkItem.getPriority());
					newWorkItem.setPriority(oldWorkItem.getPriority());
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#exiting()
	 */
	synchronized public void exiting() {
		exiting = true;
	}

	synchronized public void setPostItem(IWorkItem postItem) {
		// If the next item is a touch event or isn't valid, this item must complete if it is valid to start with
		valid = valid && ((postItem.getEventType() == EVENT_TOUCH) || !postItem.isValid());
		if (eventType == EVENT_TOUCH && prevItem != null) {
			// there is another event before this one so take this one out of the chain
			synchronized(prevItem) {
				prevItem.setPostItem(postItem);
			}
			prevItem = null;
		} else {
			// This item is supposed to go before
			this.postItem = postItem;
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#setPrevItem(com.hp.loader.workItem.IWorkItem)
	 */
	synchronized public void setPrevItem(IWorkItem prevItem) {
		// decisions on validity are made in setPostItem method
		// just set the prevItem
		this.prevItem = prevItem;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#setToken(java.lang.Long)
	 */
	public void setToken(Long token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#setPriority(java.lang.Integer)
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getPriority()
	 */
	public Integer getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getPrevItem()
	 */
	public IWorkItem getPrevItem() {
		return prevItem;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#getPostItem()
	 */
	public IWorkItem getPostItem() {
		return postItem;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#load()
	 */
	abstract public void load() throws ProcessingException;

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#loadItem()
	 */
	final public boolean loadItem() throws ProcessingException {
		// setup the messaging
		processMsg.append(Thread.currentThread().getName()).append(" ");
		baseMsgLength = processMsg.length();

		while(!exiting) {
			// execute the load
			try {
				load();
				// this is now loaded
				historyToken.loaded();
				return true;
			} catch (Error e) {
				// this item is too big to load
				processMsg.setLength(baseMsgLength);
				processMsg.append(e.getClass().getName()).append(getFullTag());
				Log.error(processMsg.toString());
				// mark this as not loadable
				valid = false;
				ErrorLog.logStackTrace(e);
				return false;
			} catch (Exception e) {
				//Re-Design : Exit On Error changes Starts Here
				if((e instanceof ProcessingException) && ((ProcessingException)e).shouldRetry()){
					if(!PriorityLoader.retryOnError){
					processMsg.setLength(baseMsgLength);
					processMsg.append(e.getClass().getName()).append(getFullTag());
					Log.error(processMsg.toString());
					PriorityLoader.exitOnError(e);
					valid = false;
					return false;
					}
					else{
						processMsg.setLength(baseMsgLength);
						processMsg.append(e.getClass().getName()).append(getFullTag());
						Log.error(processMsg.toString());
						ErrorLog.logStackTrace(e);
						// sleep until it's time to run again
						try {
							Thread.sleep(15*1000);
						} catch (InterruptedException ie) {}	
					
					}
				}
				//Re-Design : Exit On Error changes Ends Here
				else if (!exiting) {
					// only log the message if we were not requested to exit.
					// may have gotten here from an interrupted exception
					if (! logLoadException(e)) {
						ErrorLog.logStackTrace(e);
						valid = false;
						return false;
					}
				}
				
			}
		}

		// if we get here, this didn't load and exiting was called
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#logStatus(java.lang.String)
	 */
	public void logStatus(String status) throws ProcessingException {
		logStatus(status, null);
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#logStatus(java.lang.String, java.lang.Exception)
	 */
	abstract public void logStatus(String status, Exception e) throws ProcessingException;

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#prevDone()
	 */
	public void prevDone() {
		prevItem = null;
	}

	/**
	 * The valid and running values should only be set in a synchronized block for a workItem
	 */
	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#setValid(boolean)
	 */
	public void setValid(boolean isValid) {
		valid = isValid;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#isValid()
	 */
	public boolean isValid() {
		return valid;
	}


	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#isRunning()
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * return true if the item has been saved already
	 */
	public boolean isStored() {
		return (historyToken != null && historyToken.isSaved());
	}
	
	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#setRunning(boolean)
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#done()
	 */
	synchronized public void done() {
		if (postItem != null) {
			postItem.prevDone();
		}
		// no longer running
		running = false;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#removeToken(boolean)
	 */
	public void removeToken(boolean lastToken) throws IOException, HistoryException {
		// get off of the historyHandler
		historyToken.removeToken();
		if (lastToken) {
			historyToken.writeHistory();
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#store()
	 */
	abstract public boolean store() throws ProcessingException;

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#syncStore()
	 */
	abstract public boolean syncStore();

	private void doStore() throws ProcessingException, IOException, HistoryException {
		boolean updateHistory = store();
		historyToken.saved();
		if (updateHistory) {
			historyToken.writeHistory();
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#storeItem()
	 */
	final public void storeItem() throws ProcessingException, IOException, HistoryException {
		// loop until we save or return due to an error
		while(!exiting) {
			try {
				if (syncStore()) {
					synchronized(storeSyncObj) {
						if (Log.isInfoEnabled()) {
							processMsg.setLength(baseMsgLength);
							processMsg.append("Saving inOrder");
							Log.info(processMsg.toString());
						}
						doStore();
					}
				} else {
					if (Log.isInfoEnabled()) {
						processMsg.setLength(baseMsgLength);
						processMsg.append("Saving");
						Log.info(processMsg.toString());
					}
					doStore();
				}
				try {
					// Log that this was a successful extraction
					logStatus(WorkItem.SUCCESS);
				} catch (ProcessingException pe) {
					processMsg.setLength(baseMsgLength);
					processMsg.append("LogStatus exception: ");
					processMsg.append(Log.getExceptionMsgForLog(pe));
					Log.error(processMsg.toString());
					ErrorLog.logStackTrace(pe);
				}
				return;
			} catch (HistoryException he) {
				// we failed to get the history file updated, but the save was done so return
				try {
					// The history file update failed, but we have moved the package so log it as a success
					logStatus(WorkItem.SUCCESS);
				} catch (ProcessingException pe) {
					processMsg.setLength(baseMsgLength);
					processMsg.append("LogStatus exception: ");
					processMsg.append(Log.getExceptionMsgForLog(pe));
					Log.error(processMsg.toString());
					ErrorLog.logStackTrace(pe);
				}
			} catch (Exception e) {
				boolean tooManyFails = false;
				if (!(e instanceof ProcessingException) || !((ProcessingException)e).shouldRetry()) {
					// There was a store problem log it as a failure and check to see if we should quit.
					tooManyFails = failed();
				}

				if (valid && !tooManyFails) {
					// Logs a processing message
					try {
						long sleepTime = (new Double(((Math.random() * 100) % 19)).longValue()) + 1;
						processMsg.setLength(baseMsgLength);
						processMsg.append("Save Failed - retry ").append("in ").append(sleepTime).append(" secs. ").append(getTag());
						Log.info(processMsg.toString());
						Thread.sleep(sleepTime * 1000); // sleep between 1 and 20 seconds 
					} catch (InterruptedException ie){}
				} else {
					logError("Save Failed - skipped ", e);
					ErrorLog.logStackTrace(e);
					try {
						logStatus(IWorkItem.SAVE_FAILED, e);
					} catch (ProcessingException pe) {
						logError("LogStatus failed : " , pe);
						ErrorLog.logStackTrace(e);
					}
					// get off of the queue as you failed to store.
					historyToken.removeToken();
					return;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#createHistoryToken()
	 */
	public void createHistoryToken() {
		createHistoryToken(this, TokenHistoryHandler.getInstance());
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#createHistoryToken(com.hp.loader.history.HistoryHandler)
	 */
	public void createHistoryToken(IWorkItem item, HistoryHandler handler) {
		historyToken = new HistoryToken(item, handler);
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#toString()
	 */
	@Override
	public String toString() {
		return "Token: " +token+", " +
				"Priority: "+priority+", " +
				"Identifier: "+getIdentifier()+", " +
				"IsValid: "+valid ;
	}

	/**
	 * logLoadException determines the message to generate and whether the load should continue
	 * @param workItem
	 * @param e
	 * @return
	 */
	private boolean logLoadException(Exception e) {
		boolean load = false;
		if (e instanceof ProcessingException) {
			load = ((ProcessingException)e).shouldRetry();
		} else {
			// this isn't a system communications error, increment workItem failcount
			load = failed();
		}

		load = isValid() && load;
		if (load) {
			// Logs a processing message
			if (Log.isInfoEnabled()) {
				processMsg.setLength(baseMsgLength);
				processMsg.append("Load Failed - retry ").append(getFullTag());
				Log.info(processMsg.toString());
			}
			try {
				Thread.sleep(1000); // sleep for 1 second and try again
			} catch (InterruptedException ie){}
		} else {
			logError("Load Failed - skipped ", e);
			try {
				logStatus(IWorkItem.LOAD_FAILED, e);
			} catch (ProcessingException pe) {
				logError("Log Status Failed : ", e);
			}
		}
		return load;
	}

	/**
	 * logError is a single logging mechanism.
	 * @param e
	 */
	private void logError(String reason, Exception e) {
		// now build the log message
		processMsg.setLength(baseMsgLength);
		processMsg.append(getFullTag()).append(" - ");
		processMsg.append(reason).append(" - ");
		Throwable currException = e;
		while(currException != null) {
			if (currException.getMessage() != null) {
				processMsg.append(currException.getMessage());
			}

			StackTraceElement[] stackTrace = currException.getStackTrace();
			if (stackTrace.length > 0) {
				processMsg.append(" | ").append(stackTrace[0].getFileName()).append(":").append(stackTrace[0].getLineNumber()).append(" | ");
			}
			currException = currException.getCause();
			if (currException != null) {
				processMsg.append("\n Caused by  ");
			}
		}

		Log.error(processMsg.toString());
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.IWorkItem#save(java.lang.StringBuffer)
	 */
	public void save(StringBuffer sb) {
		sb.append(token).append(" ").append(priority).append(" ").append(eventType).append(" ");
	}

	public boolean schedule() {
		return TokenHistoryHandler.getInstance().scheduleWorkItem(this);
	}
	
	protected String getFieldValue(Iterator<String> vIt, String fieldName) throws ProcessingException {
		String val = vIt.next();
		if (val == null) {
			throw new ProcessingException("Failed at field "+fieldName);
		}
		return val;
	}
	
}

