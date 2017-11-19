package com.hp.loader.priorityLoader;

import com.hp.loader.source.Source;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.Log;
import com.hp.loader.workItem.IWorkItem;


/**
 * This class implements the Work Item process (worker thread), which is responsible
 * for the the execution of the document events (update, delete, touch).
 * The Work Items are obtained from the content queues by using the pop operation.
 *
 * @author      GADSC IPG-IT CR
 * @version     %I%, %G%
 * @since       1.0
 */
public class WIWorker implements Runnable {

	private static int numWorkers=0;

	// Constants

	// Attributes

	// workItem queues
	WIQueueManager queueManager;

	// buffer for outputting messages
	StringBuffer processMsg = new StringBuffer();

	// base length of message
	int baseMsgLength;

	// holds the retries count used to determine when to send messages
	private int numRetries;

	// Id of this specific worker
	private int workerId;

	// Run controls
	private boolean exiting = false;
	private boolean exited = false;
	
	IWorkItem workItem = null;

	Thread thread;

	// Constructors

	/**
	 * Constructor for WIWorker  
	 * The thread this worker runs in is a daemon thread so it will exit if the program exits and
	 * doesn't need to be stopped explicitly
	 * 
	 * @param queueManager	Pool that has all of the workItems in it
	 * @param source payload source
	 * @param historyHandler	
	 */
	public WIWorker(WIQueueManager queueManager, Source source) {

		this.queueManager = queueManager;
		workerId = numWorkers++;

		// Assigns the parameter values
//		this.historyHandler = historyHandler;

		// Create the thread, set it to be a daemon and get it started
		thread = new Thread(this, "WI-"+workerId);
		thread.setDaemon(true);
		thread.start();

		processMsg.append(thread.getName()).append(" ");
		baseMsgLength = processMsg.length();

	}

	// Methods

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		IWorkItem lastWorkItem = null;
		// this thread never exits until the program exits.
		while (true) {
			try {
				// get a workItem to process
				while(workItem == null) {
					synchronized(queueManager) {
						workItem = queueManager.pop(lastWorkItem);
						if (workItem == null) {
							// Nothing to do and we already told the qManager we are done with the lastWorkItem so don't tell it again.
							lastWorkItem = null;
							// Nothing to process so wait on the queue
							try {
								if (Log.isInfoEnabled()) {
									processMsg.setLength(baseMsgLength);
									processMsg.append("Waiting for item");
									Log.info(processMsg.toString());
								}
								queueManager.wait();
							} catch(InterruptedException ie) {}
						} else {
							lastWorkItem = workItem;
							// put the token for this workItem on the history save queue
							// doing this while still holding the lock on the queues insures no other thread
							// can get an item and put itself on the list before I get my item on the list
							workItem.createHistoryToken();
							
							// reset the retries counter
							numRetries = 0;
						}
					}
				}
				
				// set whether to load this workitem or not
				if (workItem.isValid()) {
					// Logs a processing message
					if (Log.isInfoEnabled()) {
						processMsg.setLength(baseMsgLength);
						processMsg.append("Loading   ").append(workItem.getFullTag());
						Log.info(processMsg.toString());
					}
				
					boolean loaded = workItem.loadItem();

					// exiting flag was set so skip to while loop
					if (exiting) {
						if (workItem != null) {
							// we are exiting cleanup if the temp directory was created.
							workItem.cleanup();
							// drop the workItem
							workItem = null;
						}
						//requested to exit so break out of while(true)
						break;
					}

					if (!loaded || !workItem.isValid()) {
						if (Log.isInfoEnabled() && !workItem.isValid()) {
							processMsg.setLength(baseMsgLength);
							processMsg.append("Not valid - skipping - ").append(workItem.getFullTag());
							Log.info(processMsg.toString());
						}
						// remove the token from the historyHandler as this token failed
						workItem.removeToken(queueManager.isLastEvent());
					
						// indicate that the workItem is complete so pending events can start
						workItem.done();
					} else {
						
						// reset the failedCount as we were successful loading
						workItem.clearFailed();

						// have the source, send it to the destination it
						IWorkItem prevWorkItem = workItem.getPrevItem();
						if (prevWorkItem != null) {
							// save the file
							synchronized(prevWorkItem) {
								while(prevWorkItem != null && (prevWorkItem.isRunning() || prevWorkItem.isValid())) {
									if (Log.isInfoEnabled()) {
										processMsg.setLength(baseMsgLength);
										processMsg.append("Waiting for event: ").append(prevWorkItem.getFullTag());
										Log.info(processMsg.toString());
									}
									try {
										prevWorkItem.wait();
									} catch (InterruptedException ie) {}
									if (Log.isDebugEnabled()) {
										processMsg.setLength(baseMsgLength);
										processMsg.append("Interrupted");
										Log.debug(processMsg.toString());
									}
									prevWorkItem = workItem.getPrevItem();
								}
							}
						}
						
						// save the file

						/* this was to simulate random save times for testing
  			  try {
				long sleepTime = new Double(((Math.random() % 20)*1000)).longValue();
				Thread.sleep(sleepTime);
		      } catch (InterruptedException ie){}
						 */
						workItem.storeItem();
						// The workItem is done.  Notify any pending event that it can continue
						workItem.done();
					}
				}

				// cleanup the workItem
				workItem.cleanup();

				// setup for the next workItem
				workItem = null;
			} catch(Exception e) {
				// We don't want this thread to ever fail due to a problem, so we are
				// catching all problems here
				processMsg.setLength(baseMsgLength);
				processMsg.append(Log.getExceptionMsgForLog(e));
				Log.error(processMsg.toString());
				ErrorLog.logStackTrace(e);
			}
		}

		exited = true;
	}

	/**
	 * request that the worker exit
	 */
	public void exit() {
		exiting = true;
		if (workItem != null) {
			// tell the workItem we are exiting
			workItem.exit();
		}
	}


}
