package com.hp.cdsplus.pmloader.single;

import org.apache.log4j.Logger;

import com.hp.cdsplus.pmloader.single.LoaderException;
import com.hp.cdsplus.pmloader.single.WorkItem;
import com.hp.cdsplus.pmloader.single.QueueManager;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class WorkerThread implements Runnable{
	
	private static final Logger logger = Logger.getLogger(WorkerThread.class);
	private QueueManager queueMgr = null;
	
	private int id;
	private Thread workerThread;
	private boolean exit = false;
	private boolean completed = false;
	private boolean isFatal = false;

	/**
	 * Constructor for Worker.
	 * @param queueMgr QueueManager
	 * @param workerId int

	 * @throws LoaderException  */
	public WorkerThread(QueueManager queueMgr,int workerId) throws LoaderException {

		this.queueMgr = queueMgr;
		if(this.queueMgr == null)
			throw new LoaderException("QueueManager has not been initialized");
		this.id = workerId;
		logger.info("Creating Worker Thread - Worker-"+this.id);
		workerThread = new Thread(this, "Worker-"+this.id);
		workerThread.setDaemon(true);
		workerThread.start();
	}

	/**
	 * Method run.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		WorkItem item = null;
		while(!exit){
			if(!queueMgr.isEmpty()){
				item = queueMgr.pop();
				if(item != null){
					logger.info("Processing oid-"+item.getOid()+" level-"+item.getLevel().name().toLowerCase());
					
					item.process();
					if(item.isFatal()){
						// something going wrong while processing items. Need to retry or exit based on configuration
						logger.fatal("Fatal Error occurred while trying to process item. initializing exit.");
						this.isFatal = true;
						this.exit = true;
						break;
					}
					logger.info("Completed oid-"+item.getOid()
							+" level-"+item.getLevel().name().toLowerCase()
							+" C: "+item.getContentLoadTime()
							+" H: "+item.getHierarchyLoadTime()
							+" Total: "+item.getProcessTime());
				}
			}else{
				try {
					Thread.currentThread();
					Thread.sleep(new Long((long) (Math.random()*5) * 1000));
				} catch (InterruptedException e) {}
			}
		}
		logger.info(Thread.currentThread().getName()+" exits.");
		this.completed = true;

	}

	/**

	 * @return the exit */
	public boolean isExit() {
		return exit;
	}

	/**
	 * @param exit the exit to set
	 */
	public void setExit(boolean exit) {
		this.exit = exit;
	}

	/**

	 * @return the completed */
	public boolean isCompleted() {
		return completed;
	}
	
	/**
	 * @return
	 */
	public boolean isFatal(){
		return isFatal;
	}
	
	/**
	 * @param fatal
	 */
	public void setFatal(boolean fatal){
		this.isFatal = fatal;
	}
}
