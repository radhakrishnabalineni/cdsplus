/**
 * 
 */
package com.hp.cdsplus.http;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.hp.cdsplus.http.DocIdReader;
import com.hp.cdsplus.http.DocumentBean;
import com.hp.cdsplus.http.DocumentPmoidWriter;
import com.hp.cdsplus.http.StatusReporter;

/**
 * @author VinodPothuru
 *
 */
public class ContentMetaDataProcessorHTTP {

	static Logger LOG = Logger.getLogger(ContentMetaDataProcessorHTTP.class);
	
	static final String FILE_PATH = "C:\\HP-Projects\\data\\http\\";
	static final String DOC_ID_FILE = "doc_id_list.txt";
	static final int MAX_QUEUE_SIZE = 10000;
	
	static boolean writingData = true;
	
	Queue<String> docIdQueue = new LinkedList <String>();
	Queue<String> invalidDocIdQueue = new LinkedList <String>();
	Queue<DocumentBean> docPmoidQueue = new LinkedList <DocumentBean>();
	
	DocIdReader docIdReader = null;
	DocumentPmoidWriter dpWriter = null;
	StatusReporter sr = new StatusReporter();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ContentMetaDataProcessorHTTP cmdp = new ContentMetaDataProcessorHTTP();
		

		cmdp.docIdReader = new DocIdReader(FILE_PATH,DOC_ID_FILE,cmdp.docIdQueue, MAX_QUEUE_SIZE);
		Runnable cpp01 = new ContentMetaDataPullUsingHTTP(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue);
		Runnable cpp02 = new ContentMetaDataPullUsingHTTP(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue);
		Runnable cpp03 = new ContentMetaDataPullUsingHTTP(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue);
		
		Thread cppThread01 = new Thread(cpp01);
		Thread cppThread02 = new Thread(cpp02);
		Thread cppThread03 = new Thread(cpp03);
		
		cmdp.dpWriter = new DocumentPmoidWriter(FILE_PATH, cmdp.docPmoidQueue,cmdp.invalidDocIdQueue);
		
		//LOG.info("started content extraction " + System.currentTimeMillis());
		
		cmdp.sr.start();
		cmdp.docIdReader.start();
		cppThread01.start();
		cppThread02.start();
		cppThread03.start();
		cmdp.dpWriter.start();
		
		//LOG.info("ended " + System.currentTimeMillis());
	}
}
