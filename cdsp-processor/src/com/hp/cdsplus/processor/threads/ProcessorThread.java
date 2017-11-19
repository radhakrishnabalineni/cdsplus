package com.hp.cdsplus.processor.threads;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.queue.QueueManager;

/**
 */
public class ProcessorThread implements Runnable {
	private static final Logger logger = LogManager.getLogger(com.hp.cdsplus.processor.threads.ProcessorThread.class);
	private QueueManager queue ;
	
	private boolean isExit = false; 
	private boolean isComplete = false;
	private boolean isFatal = false;
	
	/**
	 * Constructor for ProcessorThread.
	 * @param queue QueueManager
	 */
	public ProcessorThread(QueueManager queue) {
		this.queue = queue;
	}

	/**
	 * Method run.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(!this.isExit()){
			
			Item item = null;
			synchronized (queue) {
				item = queue.pop();
				
			}
			if(item != null){
				try {
					item.process();
					item.logStats();
				} catch (ProcessException e) {
					e.printStackTrace();
					logger.error(item.getLogMessage()+" : "+e.getMessage(), e);
					this.isFatal = true;
				} catch (MongoUtilsException e) {
					e.printStackTrace();
					logger.error(item.getLogMessage()+" : "+e.getMessage(), e);
					this.isFatal = true;
				} catch (OptionsException e) {
					e.printStackTrace();
					logger.error(item.getLogMessage()+" : "+e.getMessage(), e);
					this.isFatal = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
			
		}
		logger.info(Thread.currentThread().getName() + " stopped");
		this.setComplete(true);
	}

	public void stop() {
		this.isExit = true;
	}

	/**
	
	 * @return the isComplete */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	
	 * @return the isExit */
	public boolean isExit() {
		return isExit;
	}

	/**
	 * @param isExit the isExit to set
	 */
	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}

	/**
	 * Method isFatal.
	 * @return boolean
	 */
	public boolean isFatal() {
		return this.isFatal;
	}

}
