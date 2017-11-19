/**
 * 
 */
package com.hp.cdsplus.http;

import org.apache.log4j.Logger;

/**
 * @author VinodPothuru
 *
 */
public class StatusReporter extends Thread {
	
	static Logger LOG = Logger.getLogger(StatusReporter.class);
	
	static int docIdsRead = 0;
	static int docIdsProcessed = 0;
	static int docIdsInvalid = 0;
	
	static long startTime = System.currentTimeMillis();
	static long reportingTime = 0;
	long timeElapsed = 0;
	
	int sleepTime = 10*1000;
	
	public void run() {
		
		while (ContentMetaDataProcessorHTTP.writingData) {
			
			LOG.info("Docs Read: " + docIdsRead + 
			         "; Processed: " + docIdsProcessed +
			         "; Errored: " + docIdsInvalid);
			
			try {
				sleep(sleepTime);
			} catch (InterruptedException e) {
				LOG.error(e);
			}
		}

		timeElapsed = (reportingTime - startTime)/1000/60;
		LOG.info("docs read: " + docIdsRead + 
		         "; processed: " + docIdsProcessed +
		         "; errored: " + docIdsInvalid);
		LOG.info("total time: " + timeElapsed + " min(s)");
	}
}
