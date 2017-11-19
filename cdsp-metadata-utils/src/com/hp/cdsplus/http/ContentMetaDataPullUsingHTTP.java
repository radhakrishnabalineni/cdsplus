/**
 * 
 */
package com.hp.cdsplus.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * @author VinodPothuru
 * Date 03/18/2014
 */
public class ContentMetaDataPullUsingHTTP extends Thread{
	
	static Logger LOG = Logger.getLogger(ContentMetaDataPullUsingHTTP.class);
	
	private static final AtomicInteger nextId = new AtomicInteger(0);
	
    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };
	static final String URL_STRING = "http://cdsplus.austin.hp.com/cadence/app/support/content/";
	static final String INVALID_DOC_ID_FORMAT = "%1$-10s";
	static boolean readingDocs = true;
	String lineSeparator = System.getProperty("line.separator");

	Queue <String> docIdQueue = null;
	Queue <DocumentBean> docPmoidQueue = null;
	Queue<String> invalidDocIdQueue = null;
	
	public ContentMetaDataPullUsingHTTP(Queue<String> docIdQ, 
			Queue<DocumentBean> docPmoidQ, Queue<String> invalidDocQ) {
		this.docIdQueue = docIdQ;
		this.docPmoidQueue = docPmoidQ;
		this.invalidDocIdQueue = invalidDocQ;
	}

	public void run() {
		String docId = null;
		int docIdQueueSize = 0;
		byte [] xmlBytes = null;
		
        synchronized (docIdQueue) {
        	docIdQueueSize = docIdQueue.size();
        }
        while (docIdQueueSize > 0 || readingDocs) {
        	
        	//if queue is not empty process
        	if(docIdQueueSize > 0){
	        	synchronized (docIdQueue) {
	        		docId = docIdQueue.poll();
	        		LOG.info(threadId.get() + " doc id: " + docId);
	        	}
	        	if (docId != null) {
	        		xmlBytes = getXML(URL_STRING+docId).toString().getBytes();
	        		ContentMetaDataParser xmlParser = new ContentMetaDataParser();
	        		DocumentBean docBean = xmlParser.parseXML(docId, xmlBytes);
 
	        		if (!docBean.isParseFailed()) {
	        	        synchronized (docPmoidQueue) {
	        	        docPmoidQueue.add(docBean);
       	        		//LOG.info(threadId + " docPmoidQueue added, size: " + docPmoidQueue.size());
	        	        }
	        		} else {
	        			add2InvalidDocsQueue(docBean);
	        		}
	        	}
        	} else {
        		//if queue is empty pause for a while
    			//LOG.info(threadId + " DocumentPmoidWriter paused, docIdSize: " + docIdQueueSize);
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
        	}
        	synchronized (docIdQueue) {
        		docIdQueueSize = docIdQueue.size();
        		//LOG.info(threadId + " docIdQueue after poll: " + docIdQueueSize);
        	}
        }
        //notify that parsing is done to doc writer
        DocumentPmoidWriter.parsingData = false;
	}
	
	private StringBuffer getXML(String urlAddress){
		  
	    URL url;
	    HttpURLConnection conn;
	    InputStream inputStream = null;
	    BufferedReader bufferedReader = null;
	    StringBuffer sb = new StringBuffer();
	    String inputLine = null;
	    
		try {
			url = new URL(urlAddress);
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			inputStream = (InputStream) conn.getContent();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	        while ((inputLine = bufferedReader.readLine()) != null) {
	            sb.append(inputLine);
	        }
	        //LOG.info("xml: " + sb.toString());
	        inputStream.close();
	        conn.disconnect();
		} catch (MalformedURLException e) {
			inputStream = null;
		} catch (IOException e) {
			inputStream = null;
		} catch (Exception e) {
			inputStream = null;
		}
		return sb;
	}
    
    private void add2InvalidDocsQueue(DocumentBean docBean) {
    	synchronized(invalidDocIdQueue) {
    		invalidDocIdQueue.add(String.format(INVALID_DOC_ID_FORMAT, docBean.getId()) +
    				              docBean.getErrMsg() + lineSeparator);
    		StatusReporter.docIdsInvalid++;
    	}
    }
}
