/**
 * 
 */
package com.hp.cdsplus.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * @author VinodPothuru
 * Date 03/18/2014
 */
public class DocIdReader extends Thread{
	
	static Logger LOG = Logger.getLogger(DocIdReader.class);
	
	String filePath = null;
	String fileName = null;
	int maxQueueSize = 500;
	int minQueueSize = 50;
	int pauseTime = 10000;
	
	Queue<String> docIdQueue = null;
	
	public DocIdReader(String filePath, String fileName, Queue<String> docIdQueue, int maxQueueSize) {
		this.filePath = filePath;
		this.fileName = fileName;
		this.docIdQueue = docIdQueue;
		this.maxQueueSize = maxQueueSize;
		this.minQueueSize = maxQueueSize/4;
	}
	
	public void run() {

		File file = new File(filePath + fileName);
        String line;
        int qSize = 0;
		
		try {
	        FileReader fr = new FileReader(file);
	        BufferedReader br = new BufferedReader(fr);
	        
	        while((line = br.readLine()) != null){
	        	synchronized(docIdQueue) {
		        	docIdQueue.add(line);
		        	StatusReporter.docIdsRead++;
		        	qSize = docIdQueue.size();
		        	//LOG.info("docIdQueue: " + qSize);
	        	}
	        	if (qSize >= maxQueueSize) {
	        		while (qSize > minQueueSize ) {
	        			try {
	        				//LOG.info("DocIdReader paused, queue size: " + qSize);
							sleep(pauseTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	        			synchronized(docIdQueue) {
	        				qSize = docIdQueue.size();
	        			}
	        		}
	        	}
	        }

	        //notify that reading is done
	        ContentMetaDataPullUsingHTTP.readingDocs = false;
	        //LOG.info("readingDocs = false");
	        br.close();
	        fr.close();
		}catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
