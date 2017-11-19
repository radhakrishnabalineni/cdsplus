package com.hp.loader.workItem;

import java.io.IOException;

import com.hp.loader.history.HistoryException;
import com.hp.loader.history.HistoryHandler;
import com.hp.loader.priorityLoader.ProcessingException;

public interface IWorkItem {

	public static final Object storeSyncObj = new Object();
	public static final String TOKEN_NAME = "token";
	//  private static final String STATE_NAME = "state";
	//  private static final String DOCUMENT_NAME = "document_name";
	//  private static final String DOCUMENT_CLASS_NAME = "document_class";
	public static final String PRIORITY = "priority";
	public static final String LOAD_FAILED = "Load Failed";
	public static final String SAVE_FAILED = "Save Failed";
	public static final String SUCCESS = "Success";
	// Extractor event types constants
	public static final String EVENT_TOUCH = "touch";
	public static final String EVENT_UPDATE = "update";
	public static final String EVENT_DELETE = "delete";

	/**
	 * exit is called to notify a workItem that a shutdown has been requested
	 * This can be overridden by inheriting classes
	 */
	public  void exit();

	/**
	 * failed increments the failed count for this workitem and checks to see if it can still
	 * be loaded or stored.  If it has exceeded the number of tries, it sets valid to false.
	 */
	public  boolean failed();

	/**
	 * clearFailed is used to reset the failedCount back to 0 to start the next phase
	 */
	public  void clearFailed();

	/**
	 * cleanup the workItem (temp directory)
	 */
	 public void cleanup();

	/**
	 * getIdentifier returns a unizue identified for the package this workitem holds
	 * @return
	 */
	 public String getIdentifier();

	/**
	 * getPackageType returns the type of data in the package
	 * @return
	 */
	 public String getPackageType();

	/**
	 * getTag returns the abbreviated tag string for this workItem
	 * @return
	 */
	 public String getFullTag();

	/**
	 * getTag returns the abbreviated tag string for this workItem
	 * @return
	 */
	 public String getTag();
	/**
	 * getEventType returns the type of event this workItem is handling Update/Delete/Touch
	 * @return
	 */
	public  String getEventType();

	public  Long getToken();

	/**
	 * manageDuplicate determines what should happen with a workItem.
	 * @param wi
	 * @return
	 */
	public void manageDuplicate(IWorkItem oldWorkItem);

	/**
	 * method to get exiting notification
	 */
	public  void exiting();

	/**
	 * set the event that is after this one.
	 * set valid to remain true if the next event is a touch event
	 * @param newWorkItem
	 */
	public void setPostItem(IWorkItem newWorkItem);

	/**
	 * set the event that is before this one.
	 * @param newWorkItem
	 */
	public  void setPrevItem(IWorkItem newWorkItem);
	
	public  void setToken(Long token);

	public  void setPriority(Integer priority);

	public  Integer getPriority();

	/**
	 * @return the prevItem
	 */
	public  IWorkItem getPrevItem();

	/**
	 * @return the postItem
	 */
	public  IWorkItem getPostItem();

	/**
	 * load 
	 * @throws ProcessingException
	 */
	 public void load() throws ProcessingException;

	/**
	 * loadItem is final as it should only be called by the WIWorker and is internal to the priority system
	 * @return
	 * @throws ProcessingException
	 */
	public  boolean loadItem() throws ProcessingException;

	public  void logStatus(String status) throws ProcessingException;

	 public void logStatus(String status, Exception e)
			throws ProcessingException;

	/**
	 * The previous Item is done so there is no longer a dependency on it.
	 */
	public  void prevDone();

	/**
	 * The valid and running values should only be set in a synchronized block for a workItem
	 */
	/**
	 * setValid synchronizes the value of valid so it is only being set by one thread at a time
	 * @param isValid
	 */
	public  void setValid(boolean isValid);

	/**
	 * return if this item has been stored already
	 * @return
	 */
	public boolean isStored();
	
	/**
	 * get the valid flag for this item
	 * @return
	 */
	public  boolean isValid();

	/**
	 * @return the running
	 */
	public  boolean isRunning();

	/**
	 * @param running the running to set
	 */
	public  void setRunning(boolean running);

	/**
	 */
	public  void done();

	public  void removeToken(boolean lastToken) throws IOException,
			HistoryException;

	 public boolean store() throws ProcessingException;

	/**
	 * syncStore returns true if storing should be synchronized
	 * @return
	 */
	 public boolean syncStore();

	public  void storeItem() throws ProcessingException, IOException,
			HistoryException;

	/**
	 * getHistoryToken returns a history token from the default history handler.
	 * @return
	 */
	public  void createHistoryToken();

	public  void createHistoryToken(IWorkItem item, HistoryHandler handler);

	public  String toString();

	public  void save(StringBuffer sb);

	public boolean schedule();
	
}