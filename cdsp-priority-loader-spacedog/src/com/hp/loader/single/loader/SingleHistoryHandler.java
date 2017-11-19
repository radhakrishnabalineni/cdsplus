package com.hp.loader.single.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.hp.loader.priorityLoader.WIQueueManager;
import com.hp.loader.utils.Log;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;


/**
 * As the history file is written once for each workItem, it is important that it be small and simple.
 * It is there for just n rows of numbers.  They are stored in priority order, one for each priority.
 * Initially there will be 4 numbers as there are priorities 0-3.
 *
 * @author      mdahl
 * @version     %I%, %G%
 * @since       1.0
 */
abstract public class SingleHistoryHandler implements SingleHistoryWriteListener {

	protected Integer[] priorityInts;

	protected File histFile;

	protected StringBuffer sb = new StringBuffer();

	// for logging save messages
	protected StringBuffer logBuffer = new StringBuffer();

	// save history queues
	protected TreeMap<Integer, LinkedList<SingleHistoryToken>> saveHistoryQueues;

	// queueManager
	protected SingleWIQueueManager queueManager;

	protected StringBuffer queueMsg = new StringBuffer();

	// Listeners for updateEvents
	ArrayList<SingleHistoryWriteListener> writeListeners = new ArrayList<SingleHistoryWriteListener>();

	// Constructors

	/**
	 * Constructs a newly allocated HistoryHandler object.
	 * @param filePath Path of the history xml file
	 * @param numPriorities Number of priorities that are defined
	 * @param queueManager Reference to QueueManager to determine if a queue is empty
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws SingleHistoryException 
	 * @throws Exception
	 */
	public SingleHistoryHandler(String filePath, int numPriorities, SingleWIQueueManager queueManager) throws IOException, SingleHistoryException {
		this.queueManager = queueManager;

		// set up the Integer constants for referencing the priorities
		priorityInts = new Integer[numPriorities];
		for(int i=0; i<numPriorities; i++) {
			priorityInts[i] = new Integer(i);
		}

		histFile = new File(filePath);
		if (!histFile.exists() || !histFile.canRead()) {
			throw new IOException("History file <"+filePath+"> does not exist or is not readable");
		} else if (!histFile.canWrite()) {
			throw new IOException("History file <"+filePath+"> is not writable");
		}

		// setup the save queue
		// Create the saveQueue
		saveHistoryQueues = new TreeMap<Integer, LinkedList<SingleHistoryToken>>();
		for(Integer iObj : priorityInts) {
			saveHistoryQueues.put(iObj, new LinkedList<SingleHistoryToken>());
		}

	}

	// Methods

	/**
	 * addToken puts a new token on the save queue
	 * @param token HistoryToken for a workItem
	 */
	public void addToken(SingleHistoryToken token) {
		synchronized(saveHistoryQueues) {
			LinkedList<SingleHistoryToken> sqList = saveHistoryQueues.get(token.getWorkItem().getPriority());
			if (sqList != null) {
				sqList.addLast(token);
			} else {
				// Don't know what priority this is, but add a queue to handle it
				sqList = new LinkedList<SingleHistoryToken>();
				sqList.addLast(token);
				saveHistoryQueues.put(token.getWorkItem().getPriority(), sqList);
			}
		}
	}

	/**
	 * getLowestPriority returns the lowest priority the history file knows about
	 * @return
	 */
	public int getLowestPriority() {
		int retVal = priorityInts.length-1;
		return retVal > 0 ? retVal : 0;
	}

	/**
	 * HistoryHandlers are listen to each other so that which ever one recieves the writeHistory
	 * request, the others can be notified that they should write the history as well
	 * @param listener
	 * @return
	 */
	public SingleHistoryHandler addWriteListener(SingleHistoryWriteListener listener) {
		writeListeners.add(listener);
		return this;
	}

	public void prevDone(SingleIWorkItem workItem) throws IOException {
		// default is a noop.
	}
	
	public void removeToken(SingleHistoryToken token) {
		synchronized(saveHistoryQueues) {
			Integer priority = token.getWorkItem().getPriority();
			LinkedList<SingleHistoryToken> sqList = saveHistoryQueues.get(priority);
			// take yourself off of the save queue
			sqList.remove(token);
		}
	}

	/**
	 * scheduleWorkItem determines if a workItem satisfies the scheduling criteria and
	 * sends it to the queueManager if it does.
	 * @param item
	 * @return
	 */
	abstract public boolean scheduleWorkItem(SingleIWorkItem item);

	/**
	 * Validates that a workOrder item is
	 * @param workItem
	 * @return true if the priority was changed
	 */
	protected boolean validateWIPriority(SingleIWorkItem workItem) {
		if (workItem.getPriority().intValue() < 0) {
			// no priority, set it to the "lowest" known priority
			workItem.setPriority(priorityInts[priorityInts.length-1]);
			return true;
		}
		return false;
	}

	public void writeHistory(SingleHistoryHandler historyHandler) throws IOException, SingleHistoryException {
		for(SingleHistoryWriteListener listener: writeListeners) {
			if (listener == historyHandler) {
				// This is the originating listener, don't fire the event
				continue;
			}
			listener.writeHistory(historyHandler);
		}
	}
}