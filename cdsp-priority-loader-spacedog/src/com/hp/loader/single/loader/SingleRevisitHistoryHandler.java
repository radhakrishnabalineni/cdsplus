package com.hp.loader.single.loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.priorityLoader.WIQueueManager;
import com.hp.loader.source.Source;
import com.hp.loader.utils.Log;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.single.loader.SingleIWorkItem;
import com.hp.loader.single.loader.SingleRevisitWorkItem;
import com.hp.loader.single.loader.SingleIWorkItem;


/**
 * ItemHistoryHandler provides handling of a history file for non-sequential events.  It is specifically used for
 * manual updates to a loader, and revisits for WDS.
 *
 * @author      mdahl
 * @version     %I%, %G%
 * @since       1.0
 */
public class SingleRevisitHistoryHandler extends SingleHistoryHandler implements SingleHistoryWriteListener {

	private static final String REVISIT_HISTORY_FILE_FAILURE = "Failed to update revisit history file";
	public static final String REVISITTAG = "_revisit.txt";
	
	static private final Comparator<SingleIWorkItem> revisitComparator = new Comparator<SingleIWorkItem>() {
		public int compare(SingleIWorkItem o1, SingleIWorkItem o2) {
			int retVal = o1.getToken().compareTo(o2.getToken()); 
			if ( retVal == 0) {
				if (o1 instanceof SingleRevisitWorkItem && !(o2 instanceof SingleRevisitWorkItem)) {
					return -1;
				} else if (!(o1 instanceof SingleRevisitWorkItem) && (o2 instanceof SingleRevisitWorkItem)) {
					return 1;
				} else {
					return 0;
				}
  			} else {
  				return retVal;
  			}
  		}
  	};
  	
	// inorderMap holds the revisits in order of processing
	private TreeMap<Long, SingleRevisitWorkItem> inOrderMap = new TreeMap<Long, SingleRevisitWorkItem>();
	private HashMap<String, SingleRevisitWorkItem> revisitMap = new HashMap<String, SingleRevisitWorkItem>();

	private Long lastAfterToken = new Long(0);
	private boolean eventsAreSeq = false;
	// Constructors

	/**
	 * Constructs a newly allocated HistoryHandler object.
	 * @param filePath Path of the history xml file
	 * @param numPriorities Number of priorities that are defined
	 * @param queueManager Reference to QueueManager to determine if a queue is empty
	 * @param eventsAreSeq 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws SingleHistoryException 
	 * @throws ProcessingException 
	 * @throws Exception
	 */
	public SingleRevisitHistoryHandler(String filePath, int numPriorities, SingleWIQueueManager queueManager, boolean eventsAreSeq, SingleSource source) throws IOException, SingleHistoryException, ProcessingException {
		super(filePath, numPriorities, queueManager);
		
		this.eventsAreSeq = eventsAreSeq;
		
		// register with the main handler
		SingleHistoryHandler tokenHandler = SingleTokenHistoryHandler.getInstance().addWriteListener(this);
		// now add the tokenHandler to my list so it can be notified when a writeHistory comes to this object
		addWriteListener(tokenHandler);
		
		loadItems(source);
	}

	// Methods

	/**
	 * 
	 * @param revisitTimeStamp
	 * @param workItem
	 */
	public void addRevisit(Long revisitToken, SingleIWorkItem workItem)  {
		ThreadLog.debug("Adding revisit for "+workItem.getIdentifier());
		// lock the map to make a change
		SingleRevisitWorkItem rWorkItem = new SingleRevisitWorkItem(revisitToken, workItem, this);
		addRevisitItem(rWorkItem, true);
	}
	
	private void addRevisitItem(SingleRevisitWorkItem workItem, boolean updateHistory) {
		synchronized(inOrderMap) {
			// see if we have a duplicate
			if (revisitMap.containsKey(workItem.getIdentifier())) {
				ThreadLog.debug("Duplicate revisit for "+workItem.getIdentifier()+" found. Resolving");
				// This is a duplicate event, remove the old one and put the new one in
				SingleRevisitWorkItem oldItem = revisitMap.remove(workItem.getIdentifier());
				// take the old item out of the tree
				removeItem(oldItem, false);
				// update the priority of the new item if the old item priority was higher
				if (oldItem.getPriority() < workItem.getPriority()) {
					ThreadLog.debug("Changing revisit priority from "+workItem.getPriority()+" to "+oldItem.getPriority()+
							" for "+workItem.getIdentifier());
					workItem.setPriority(oldItem.getPriority());
				}
				// now put this one back into the map
				revisitMap.put(workItem.getIdentifier(), workItem);
			} else {
				// don't have it yet so put it in the map
				revisitMap.put(workItem.getIdentifier(), workItem);
			}
			// now put the new item in the tree
			if (inOrderMap.containsKey(workItem.getToken())) {
				// change to list so multiples can be stored at the same point
				SingleRevisitWorkItem val = inOrderMap.get(workItem.getToken());
				val.add(workItem);
			} else {
				inOrderMap.put(workItem.getToken(), workItem);
			}
			if (updateHistory) {
				try {
					updateHistory();
				} catch (IOException e) {
					StringBuffer sb = new StringBuffer("Failed to update revisit file for item: ");
					workItem.save(sb);
					sb.append(Log.getExceptionMsgForLog(e));
					Log.report(REVISIT_HISTORY_FILE_FAILURE, sb.toString());
				}
			}
		}
	}

	/**
	 * addWorkItems puts new RevisitWorkItems into the list
	 * @param workItemList
	 */
	public void addWorkItems(Long afterToken, ArrayList<SingleIWorkItem> workItemList) {
		Long beforeToken = afterToken + 1;
		if (workItemList.size() != 0) {
			beforeToken = workItemList.get(workItemList.size() -1).getToken() + 1;
		} else if (!eventsAreSeq) {
			// set before to be the current timestamp as events are not sequential
			beforeToken = new Long(System.currentTimeMillis());
			if (beforeToken < afterToken) {
				// the handler is working in the future so look in the current timeframe
				afterToken = lastAfterToken;
				lastAfterToken = beforeToken;
			}
		}

		// get any entries that are after the last time they were loaded but before currTime
		SortedMap<Long, SingleRevisitWorkItem> tailMap = inOrderMap.subMap(afterToken, beforeToken);
		int numAdded = 0;
		if (tailMap.size() > 0) {
			for(SingleRevisitWorkItem item : tailMap.values()) {
				numAdded += item.addWorkItems(workItemList);
			}
			
			// sort the added in workitems
			Collections.sort(workItemList, revisitComparator);
		}
		
		ThreadLog.debug("Added "+numAdded+" revisit entries");
	}
	
	
	private void loadItems(SingleSource source) throws ProcessingException, FileNotFoundException   {
		ArrayList<SingleRevisitWorkItem> workItems = source.loadRevisitWorkItems(new FileInputStream(histFile), this);
		// lock the map and put these all in at the same time
		synchronized(inOrderMap) {
			for(SingleRevisitWorkItem item : workItems) {
				addRevisitItem(item, false);
			}
		}
	}


	@Override
	public void writeHistory(SingleHistoryHandler historyHandler) throws IOException, SingleHistoryException {
		// notify other handlers to write their history
		super.writeHistory(historyHandler);
		
		// now write my history
		boolean valuesModified = false;
		LinkedList<SingleHistoryToken> sqList = null;
		
		// setup to log completed
		logBuffer.setLength(0);
		logBuffer.append(Thread.currentThread().getName()).append(" ");
		logBuffer.append("Completed ");
		int baseLength = logBuffer.length();
		ArrayList<String> completedList = new ArrayList<String>();
		
		// Each priority list needs to be scanned for saved tokens.
		// As there is no inorder requirement for revisit items, the list must be scanned until
		// you have scanned them all due to out of order processing.
		// by locking on the inOrderMap, changes to the map can't happen while the history is being written
		synchronized(inOrderMap) {
			for(Integer intVal : priorityInts) {
				sqList = saveHistoryQueues.get(intVal);
				boolean done=false;
				while(!done && sqList.size() > 0) {
					SingleHistoryToken token = sqList.removeFirst();
					if (!token.isSaved()) {
						// hit the end of saved workItems
						done = true;
						sqList.addFirst(token);
					} else {
						SingleIWorkItem workItem = token.getWorkItem();
						// queue the completed message
						if (Log.isInfoEnabled()) {
							logBuffer.setLength(baseLength);
							logBuffer.append(workItem.getTag());
							token.addStats(logBuffer);
							completedList.add(logBuffer.toString());
						}
						
						// 
						if (workItem.getPrevItem() == null) {
							valuesModified = true;
							removeItem(workItem, false);
						}
					}
				}
			}

			if (valuesModified) {
				// the history file changed so save it
				try {
					updateHistory();
				} catch (IOException e) {
					StringBuffer sb = new StringBuffer();
					sb.append("Entries are completed and should be removed \n");
					sb.append(Log.getExceptionMsgForLog(e));
					Log.report(REVISIT_HISTORY_FILE_FAILURE, sb.toString() );
				}
			}
			
			// now write out the saved messages
			for(String token : completedList) {
				Log.info(token);
			}
		}
		
	}

	/**
	 * remove the workItem and possibly reset the stored value
	 * @param workItem
	 * @param updateHistory indicates if the history file should be updated on this removal
	 */
	public void removeItem(SingleIWorkItem workItem, boolean updateHistory) {
		synchronized (inOrderMap) {
			// take the item out of the revisitMap
			revisitMap.remove(workItem.getIdentifier());
			// take the item out of the treeMap
			if (inOrderMap.containsKey(workItem.getToken())) {
				SingleRevisitWorkItem curItem = inOrderMap.get(workItem.getToken());
				SingleRevisitWorkItem newItem = curItem.remove((SingleRevisitWorkItem)workItem); 
				if (newItem == null) {
					inOrderMap.remove(workItem.getToken());
				} else if (curItem != newItem){
					// the front item
					inOrderMap.put(workItem.getToken(), newItem);
				}
			}
			if (updateHistory) {
				try {
					updateHistory();
				} catch (IOException e) {
					StringBuffer sb = new StringBuffer("Failed to update revisit file removing item: ");
					workItem.save(sb);
					sb.append(Log.getExceptionMsgForLog(e));
					Log.report(REVISIT_HISTORY_FILE_FAILURE, sb.toString());
				}
			}
		}
	}
	
	@Override
	public void removeToken(SingleHistoryToken token) {
		// get the history token off of the list
		super.removeToken(token);
		// remove the workItem as it isn't going to be processed
		removeItem(token.getWorkItem(), true);
	}
	
	/**
	 * update history checks the tokens to see if any on the doneList have no dependencies and can be removed
	 * @throws IOException 
	 */
	private void updateHistory() throws IOException {
		
		sb.setLength(0);
		for(SingleRevisitWorkItem item : inOrderMap.values()) {
			item.save(sb);
		}

		// Now open the file and write it out
		FileOutputStream fileHandler = new FileOutputStream(histFile);
		fileHandler.write(sb.toString().getBytes());
		fileHandler.close();
	}
	
	@Override
	public void prevDone(SingleIWorkItem workItem) {
		if (!workItem.isValid()) {
			// the previous item is done and this item was only a place holder so this can be removed
			removeItem(workItem, true);
		}
	}
	
	/**
	 * All revisit type workItems schedule into the system
	 */
	@Override
	public boolean scheduleWorkItem(SingleIWorkItem workItem) {
		// check priority
		if (validateWIPriority(workItem)) {
			if (Log.isInfoEnabled()) {
				queueMsg.setLength(0);
				queueMsg.append(workItem.getTag()).append("Resetting priority to lowest known");
				Log.info(queueMsg.toString());
			}
		}
			
		// Schedule the item
		queueManager.push(workItem);

		if (Log.isInfoEnabled()) {
			queueMsg.setLength(0);
			queueMsg.append(workItem.getPackageType()).append(" ").append(workItem.getFullTag());
			Log.info(queueMsg.toString());
		}
		// this Item scheduled
		return true;
	}

	

}	
		
