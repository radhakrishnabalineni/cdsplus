/**
 * 
 */
package com.hp.cdsplus.mongo.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;

/**
 * @author kashyaks
 *
 */
public class MongoAppender extends AppenderSkeleton{
	
	String MongoURI;
	String UserName;
	String Password;
	String DBName;
	String CollectionName;
	ArrayList<LoggingEvent> buffer;
	ArrayList<LoggingEvent> removes;
	int bufferSize=1;

	public MongoAppender(){
		super();
		buffer = new ArrayList<LoggingEvent>(bufferSize);
		removes = new ArrayList<LoggingEvent>(bufferSize);
	}
	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected void append(LoggingEvent event) {
		event.getNDC();
		event.getThreadName();
		// Get a copy of this thread's MDC.
		event.getMDCCopy();
		event.getLocationInformation();
		event.getRenderedMessage();
		event.getThrowableStrRep();
		buffer.add(event);

		if (buffer.size() >= bufferSize)
		flushBuffer();
		}

	private void flushBuffer() {
		ArrayList<DBObject> records = new ArrayList<DBObject>();
		for(Iterator<LoggingEvent> i = buffer.iterator(); i.hasNext();){
			DBObject dbObj = (BasicDBObject) JSON.parse(getLayout().format(i.next()));
			records.add(dbObj);
		}
		MongoClient mongoClient = null;
		DB db = null;
		DBCollection logCollection = null;
		try {
			mongoClient = new MongoClient(new MongoClientURI(MongoURI));
			db = mongoClient.getDB(this.getDBName());
			logCollection = db.getCollection(this.getCollectionName());
		} catch (UnknownHostException e) {
			errorHandler.error(e.getMessage(), e,1);
		}
		if(logCollection==null){
			errorHandler.error("cannot get handle on the logger collection specified - "+this.getMongoURI()+"/"+this.getDBName()+"/"+this.getCollectionName());
		}
		logCollection.insert(records);
	}
	

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
	 */
	@Override
	public boolean requiresLayout() {
		return true;
	}

	/**
	 * @return the mongoURI
	 */
	public String getMongoURI() {
		return MongoURI;
	}

	/**
	 * @param mongoURI the mongoURI to set
	 */
	public void setMongoURI(String mongoURI) {
		MongoURI = mongoURI;
	}

	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return CollectionName;
	}

	/**
	 * @param collectionName the collectionName to set
	 */
	public void setCollectionName(String collectionName) {
		CollectionName = collectionName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return UserName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		UserName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return Password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		Password = password;
	}

	/**
	 * @return the dBName
	 */
	public String getDBName() {
		return DBName;
	}

	/**
	 * @param dBName the dBName to set
	 */
	public void setDBName(String dBName) {
		DBName = dBName;
	}

	/**
	 * @return the buffer
	 */
	public ArrayList<LoggingEvent> getBuffer() {
		return buffer;
	}

	/**
	 * @param buffer the buffer to set
	 */
	public void setBuffer(ArrayList<LoggingEvent> buffer) {
		this.buffer = buffer;
	}

	/**
	 * @return the removes
	 */
	public ArrayList<LoggingEvent> getRemoves() {
		return removes;
	}

	/**
	 * @param removes the removes to set
	 */
	public void setRemoves(ArrayList<LoggingEvent> removes) {
		this.removes = removes;
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public static void main(String[] args){
		DOMConfigurator.configure("config/log4j.xml");
		Logger logger = Logger.getLogger(MongoAppender.class);
		logger.debug("printing debug message");
		MongoClient mongoClient = null;
		DB db = null;
		DBCollection logCollection = null;
		try {
			mongoClient = new MongoClient(new MongoClientURI("mongodb://cdspdb:cdspdb@localhost:27017/cdsplogs"));
			db = mongoClient.getDB("cdsplogs");
			logCollection = db.getCollection("logs");
			DBCursor cursor = logCollection.find();
			while(cursor.hasNext()){
				System.out.println(cursor.next());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

}
