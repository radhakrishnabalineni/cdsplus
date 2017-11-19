/**
 * 
 */
package com.hp.cdsplus.db;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * @author VinodPothuru
 *
 */
public class ContentMetaDataPullUsingDB extends Thread {

	static Logger LOG = Logger.getLogger(ContentMetaDataPullUsingDB.class);

	static final String DB_CONNECT_STRING = "http://cdsplus.austin.hp.com/cadence/app/support/content/";
	static final String INVALID_DOC_ID_FORMAT = "%1$-10s";
	static boolean readingDocs = true;
	String lineSeparator = System.getProperty("line.separator");

	private static final AtomicInteger nextId = new AtomicInteger(0);
	
    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };
	Queue <String> docIdQueue = null;
	Queue <DocumentBean> docPmoidQueue = null;
	Queue<String> invalidDocIdQueue = null;
	
	DBCollection collection = null;
	DBCollection collectionStg = null;

	public ContentMetaDataPullUsingDB(Queue<String> docIdQ, Queue<DocumentBean> docPmoidQ, 
			Queue<String> invalidDocQ, DBCollection collection, DBCollection collectionStg) {
		this.docIdQueue = docIdQ;
		this.docPmoidQueue = docPmoidQ;
		this.invalidDocIdQueue = invalidDocQ;
		this.collection = collection;
		this.collectionStg = collectionStg;
	}

	@Override
	public void run() {
		String docId = null;
		int docIdQueueSize = 0;
		
        synchronized (docIdQueue) {
        	docIdQueueSize = docIdQueue.size();
        }
        while (docIdQueueSize > 0 || readingDocs) {
        	
        	//if queue is not empty process
        	if(docIdQueueSize > 0){
	        	synchronized (docIdQueue) {
	        		docId = docIdQueue.poll();
	        		//LOG.info(threadId.get() + " docId: " + docId);
	        	}
	        	if (docId != null) {
		    		DBCursor cursor = getCursor(docId);
		    		ContentMetaDataParser cmdp = new ContentMetaDataParser();
		    		DocumentBean docBean = cmdp.parseDocument(docId, cursor);
		    		if (docBean.isProductNull()) {
		    			cmdp = new ContentMetaDataParser();
		    			cursor = getCursorStg(docId);
		    			docBean = cmdp.parseDocument(docId, cursor, true);
		    		}
	    			if (!docBean.isParseFailed()) {
        	        	synchronized (docPmoidQueue) {
        	        		docPmoidQueue.add(docBean);
        	        		//LOG.info(threadId + "docPmoidQueue added, size: " + docPmoidQueue.size());
        	        	}
	    			} else {
	    				add2InvalidDocsQueue(docBean);
	    			}
	    			cursor.close();
			    } 
	        } else {
        		//if queue is empty pause for a while
    			//LOG.info(threadId + "DocumentPmoidWriter paused, docIdSize: " + docIdQueueSize);
    			try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
        	}
        	synchronized (docIdQueue) {
        		docIdQueueSize = docIdQueue.size();
        		//LOG.info(threadId + "docIdQueue after poll: " + docIdQueueSize);
        		//LOG.info(threadId + "readingDocs: " + readingDocs);
        	}
        }
        //notify that parsing is done to doc writer
        DocumentPmoidWriter.parsingData = false;
        ContentMetaDataProcessorDB.closeDBCollection();
	}

	private synchronized DBCursor  getCursor(String docId) {
		DBCursor cursor = collection.find(new BasicDBObject("_id",docId), 
		          new BasicDBObject("document.products.product.#",1).
		                 append("document.document_type", 1).
		                 append("document.content_topic", 1).
		                 append("document.content_topic_details.content_topic_detail", 1));
		if (cursor == null) {
			LOG.error("cursor is null for docid: " + docId);
		}
		return cursor;
	}
	
	private synchronized DBCursor  getCursorStg(String docId) {
		DBCursor cursor = collectionStg.find(new BasicDBObject("_id",docId), 
		          new BasicDBObject("document.products.product",1).
		                 append("document.document_type", 1).
		                 append("document.content_topic", 1).
		                 append("document.content_topic_details.content_topic_detail", 1));
		if (cursor == null) {
			LOG.error("cursor is null for docid: " + docId);
		}
		return cursor;
	}
	
    private void add2InvalidDocsQueue(DocumentBean docBean) {
    	synchronized(invalidDocIdQueue) {
    		invalidDocIdQueue.add(String.format(INVALID_DOC_ID_FORMAT, docBean.getId()) +
    				              docBean.getErrMsg() + lineSeparator);
    		StatusReporter.docIdsInvalid++;
    	}
    }
}
