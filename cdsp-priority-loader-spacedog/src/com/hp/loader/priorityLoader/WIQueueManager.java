package com.hp.loader.priorityLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import com.hp.loader.utils.Log;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;


/**
 * Class is used to handle the WorkItems queue.  Items are placed in the queue  
 *
 * @author      mdahl
 * @version     %I%, %G%
 * @since       1.0
 */
public class WIQueueManager {

  public static Integer NO_PRIORITY_QUEUE;

  // Attributes

  // WorkItem pool which is a linked list one for each priority
  // they are stored in a treeMap to keep the priorities in order when checking for a 
  // workItem to process
  private TreeMap<Integer,LinkedList<IWorkItem>> contentQueuesPool;

  // Index of the queues content,  used for fast lookup of duplicate entries
  private HashMap<String, IWorkItem> wiQueuesIndex = null;

  // numWorkers
  private int numWorkers;

  // number of waiting workers
  private int numPaused;

  // state of the queue manager 
  private boolean paused = false;

  // request to pause
  private boolean pausing;

  // pause object to be waited on
  private Object pauseObject = new Object();

  /**
   * Constructs a newly allocated WIQueueManager object.
   * @param numWorkers TODO
   */
  public WIQueueManager(int numPriorities, int numWorkers) {
    contentQueuesPool = new TreeMap<Integer,LinkedList<IWorkItem>>();
    for(int i=0; i<numPriorities; i++) {
      contentQueuesPool.put(new Integer(i), new LinkedList<IWorkItem>());
    }
    wiQueuesIndex  = new HashMap<String, IWorkItem>();
    NO_PRIORITY_QUEUE = new Integer(numPriorities-1);
    this.numWorkers = numWorkers;
    // start paused so everything can get configured and pause/resume can be calculated
    this.pausing = true;
  }

  /**
   * pause tells the queueManager to pause the queue 
   */
  public void pause() {
    synchronized(this) {
      if (!pausing) {
        pausing = true;
        // tell everyone we are pausing
        this.notifyAll();
      }
    }

    int num = -1;

    while(pausing && !paused) {
      if (++num %30 == 0) {
        Log.info("Waiting for qManager to pause queue");
      }

      synchronized(pauseObject) {
        try {
          // waits 3 minutes then sends msg again
          pauseObject.wait(2*1000);
        } catch (InterruptedException ie) {}
      }
    }
  }

  /**
   * queueSize returns the size of the queue for a specific priority
   * @param priority
   * @return
   */
  public int queueSize(Integer priority) {
    LinkedList<IWorkItem> l = contentQueuesPool.get(priority);
    return (l != null ? l.size() : -1);
  }

  /**
   * tell the queueManager to start giving out work
   */
  public void resume() {
    synchronized(this) {
      Log.info("qManager resuming queue.");
      pausing = false;
      paused = false;
      // clear the number of paused threads
      numPaused = 0;
      this.notifyAll();
    }
  }

  /**
   * Adds the Work Item to the respective queue based on the
   * priority attribute of the Work Order where it comes from.
   * This method is called inside of a synchronized block from the loader to stop pop's from happening.
   * @param workItem Work Item object
   * @see WorkItem
   */
  public void push(IWorkItem workItem) {
    // Check for duplicates
    if (wiQueuesIndex.containsKey(workItem.getIdentifier())) {
      // if a Work Item for the document already exists in the queues
      // checks for the priority in order to move to the most appropriate
      // queue according to the event types
      workItem.manageDuplicate(wiQueuesIndex.remove(workItem.getIdentifier()));
    } 

    // Add the Work Item object to the Index
    // This overwrites any duplicate object and becomes the current object
    wiQueuesIndex.put(workItem.getIdentifier(), workItem);
    
    // if the priority is NO_PRIORITY(-1) put it in the lowest priority queue
    int priority = workItem.getPriority().intValue();
    if (priority < 0 || priority > NO_PRIORITY_QUEUE.intValue()) {
    Log.info("Priority for "+workItem.getIdentifier()+" is out of range. Changing Priority from"+workItem.getPriority()+ " to "+NO_PRIORITY_QUEUE);
      // if the priority is out of the priority range for this extractor, put it in the lowest priority queue
      workItem.setPriority(NO_PRIORITY_QUEUE);
    }
    
    // Adds the Work Item object to the queue
    contentQueuesPool.get(workItem.getPriority()).addLast(workItem);
    
    if (!pausing) {
      // don't notify if pausing as we don't want to wake up any paused workers
      // notify the processing threads that something has been put in the queue
      this.notifyAll();
    }
  }

  /**
   * getWorkItem checks the head of the list and returns it if it is valid, skipping all invalid
   * workItems
   * @param list
   * @return
   */
  private IWorkItem getWorkItem(LinkedList<IWorkItem> list) {
    while(list.size() > 0) {
      IWorkItem retVal = list.removeFirst();
      synchronized(retVal) {
        if (retVal.isValid()) {
          // this workItem is now in the queue
          retVal.setRunning(true);
          return retVal;
        } else {
          // This workItem was set to invalid, so mark it as done
          IWorkItem postItem = retVal.getPostItem();
          if (postItem != null) {
            Log.info("Skip event "+retVal.getTag() + " for event "+postItem.getToken());
          } else {
        	// remove the workItem from the queueIndexes as it is done and there isn't another one to process
          	wiQueuesIndex.remove(retVal.getIdentifier());
            Log.info("Skip event "+retVal.getTag()+" already done ");
          }
          retVal.done();
        }
      }
    }
    return null;
  }

  /**
   * initItem puts a new item in the queue, handles duplicates, and sets the new Item to invalid if
   * processed == true
   * @param workItem
   * @param processed
  public void initItem(IWorkItem workItem, boolean processed) {
    // Check for duplicates
    if (wiQueuesIndex.containsKey(workItem.getIdentifier())) {
      // if a Work Item for the document already exists in the queues
      // checks for the priority in order to move to the most appropriate
      // queue according to the event types
      workItem.manageDuplicate(wiQueuesIndex.remove(workItem.getIdentifier()));
    } 

    // Add the Work Item object to the Index
    // This overwrites any duplicate object and becomes the current object
    wiQueuesIndex.put(workItem.getIdentifier(), workItem);

    // if the priority is NO_PRIORITY(-1) put it in the lowest priority queue
    Integer priority = workItem.getPriority();
    if (priority.intValue() < 0 || priority.intValue() > NO_PRIORITY_QUEUE.intValue()) {
      // if the priority is out of the priority range for this extractor, put it in the lowest priority queue
      workItem.setPriority(NO_PRIORITY_QUEUE);
    }

    // Adds the Work Item object to the queue
    contentQueuesPool.get(workItem.getPriority()).addLast(workItem);

  }
   */

  /**
   * return that this is the last event if wiQueuesIndex is 1
   * @return
   */
  public boolean isLastEvent() {
	  synchronized(this) {
		  return wiQueuesIndex.size() == 1;
	  }
  }
  
  /**
   * Provides the next WorkItem (based on priority)
   * to be pulled from Concentra and then put into CDS+.
   * This method removes the entry from the queue and the index.
   * @return next workItem object to be processed or null if no workItems are present.
   * 
   * @see WorkItem
   */
  public IWorkItem pop(IWorkItem lastItem) {
    IWorkItem retVal = null;
    synchronized(this) {
      // remove the lastItem from the queueIndex
      // take this one out of the indexes as well
      if (lastItem != null) {
        wiQueuesIndex.remove(lastItem.getIdentifier());
      }
      
      if (pausing) {
        if (++numPaused == numWorkers) {
          paused = true;
          pausing = false;
          synchronized (pauseObject) {
            // tell the requester we are paused
            pauseObject.notifyAll();
          }
        }
        return null;
      }
      for(LinkedList<IWorkItem> list : contentQueuesPool.values()) {
        if ((retVal = getWorkItem(list)) != null) {
          return retVal;
        }
      }
    }
    return null;
  }
  	public boolean isEmpty(){
		synchronized (this) {
			return  wiQueuesIndex.isEmpty();	
		}
  		
	}
	
}
