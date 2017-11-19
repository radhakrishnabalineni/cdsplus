/**
 * 
 */
package com.hp.cdsplus.db;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author VinodPothuru 
 * Date 03/18/2014
 * 
 */
public class ContentMetaDataProcessorDB {
	
	static Logger LOG = Logger.getLogger(ContentMetaDataProcessorDB.class);
	
	static final String FILE_PATH = "C:\\HP-Projects\\data\\db\\";
	static final String DOC_ID_FILE = "doc_id_list.txt";
	static final int MAX_QUEUE_SIZE = 10000;
	
	static boolean writingData = true;
	
	Queue<String> docIdQueue = new LinkedList <String>();
	Queue<String> invalidDocIdQueue = new LinkedList <String>();
	Queue<DocumentBean> docPmoidQueue = new LinkedList <DocumentBean>();
	
	DocIdReader docIdReader = null;
	DocumentPmoidWriter dpWriter = null;
	StatusReporter sr = new StatusReporter();

	static MongoClient mongoConnection = null;
	DB db = null;
	DBCollection collection = null;
	DBCollection collectionStg = null;

	private void openDBCollection () {
		try {
			// ITG g9t3417.houston.hp.com,g9t3418.houston.hp.com,g9t3384.houston.hp.com
			// ITG g9t3417.houston.hp.com:20001,g9t3418.houston.hp.com:20001,g9t3384.houston.hp.com:20001
			//prod g9t3218.houston.hp.com:20001,g9t3219.houston.hp.com:20001,g9t3220.houston.hp.com:20001
			String str = "mongodb://cdspdbr:WelcomeR_31841@g9t3218.houston.hp.com:20001,g9t3219.houston.hp.com:20001,g9t3220.houston.hp.com:20001/admin?replicaSet=rs0";
			System.out.println(str);

			mongoConnection = new MongoClient(new MongoClientURI(str));
			db  = mongoConnection.getDB("support");
			collection = db.getCollection("metadata_live");
			collectionStg = db.getCollection("metadata_staging");
			
		} catch (UnknownHostException e) {
			LOG.error(e);
		}

	}
	
	static public void closeDBCollection () {
		
		mongoConnection.close();
		
	}
	
/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ContentMetaDataProcessorDB cmdp = new ContentMetaDataProcessorDB();
		
		cmdp.openDBCollection ();

		cmdp.docIdReader = new DocIdReader(FILE_PATH,DOC_ID_FILE,cmdp.docIdQueue, MAX_QUEUE_SIZE);
		Runnable cpp01 = new ContentMetaDataPullUsingDB(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue, cmdp.collection, cmdp.collectionStg);
		Runnable cpp02 = new ContentMetaDataPullUsingDB(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue, cmdp.collection, cmdp.collectionStg);
		Runnable cpp03 = new ContentMetaDataPullUsingDB(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue, cmdp.collection, cmdp.collectionStg);
		Runnable cpp04 = new ContentMetaDataPullUsingDB(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue, cmdp.collection, cmdp.collectionStg);
		Runnable cpp05 = new ContentMetaDataPullUsingDB(cmdp.docIdQueue,cmdp.docPmoidQueue,cmdp.invalidDocIdQueue, cmdp.collection, cmdp.collectionStg);
		
		Thread cppThread01 = new Thread(cpp01);
		Thread cppThread02 = new Thread(cpp02);
		Thread cppThread03 = new Thread(cpp03);
		Thread cppThread04 = new Thread(cpp04);
		Thread cppThread05 = new Thread(cpp05);
		
		cmdp.dpWriter = new DocumentPmoidWriter(FILE_PATH, cmdp.docPmoidQueue,cmdp.invalidDocIdQueue);
		
		//LOG.info("started content extraction " + System.currentTimeMillis());
		
		cmdp.sr.start();
		cmdp.docIdReader.start();
		cppThread01.start();
		cppThread02.start();
		cppThread03.start();
		cppThread04.start();
		cppThread05.start();
		cmdp.dpWriter.start();
		
		//LOG.info("ended " + System.currentTimeMillis());
	}
}
