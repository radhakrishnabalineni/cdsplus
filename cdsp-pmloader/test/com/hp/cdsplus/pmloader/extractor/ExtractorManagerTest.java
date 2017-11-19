package com.hp.cdsplus.pmloader.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;
import com.hp.cdsplus.pmloader.queue.QueueManager;
import com.hp.cdsplus.utils.JDBCUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * The class <code>ExtractorManagerTest</code> contains tests for the class <code>{@link ExtractorManager}</code>.
 *
 * @generatedBy CodePro at 6/17/13 1:03 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ExtractorManagerTest {
	
	MongoClient mongoClient;
	DB db ;
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	public ExtractorManagerTest() {
	}

	/**
	 * Run the ExtractorManager(QueueManager,boolean) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testExtractorManager_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		boolean isDeltaMode = true;
		boolean isTempReload = true;
		
		ExtractorManager result = new ExtractorManager(queueMgr, isDeltaMode, isTempReload);	
		assertNotNull(result);
		
		assertFalse(result.isActive());
		assertFalse(result.isExit());
		
	}


	/**
	 * Run the Object getCurrentLoadId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testGetCurrentLoadId_1()
		throws Exception {
		
		DBCollection collection = db.getCollection("pm_extraction_logs");
		DBObject query = new BasicDBObject("status","successful");
		
		
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		
		fixture.logDateRange(new Date[]{
				new Date(System.currentTimeMillis()-10000),
				new Date(System.currentTimeMillis())
				}
		);
		DBCursor obj = collection.find(query).sort(new BasicDBObject("end_time",-1));
		if(obj.hasNext()){
			DBObject res = obj.next();
			assertNotNull(res);
			assertNotNull(obj);
			ObjectId result = fixture.getCurrentLoadId();
			assertNotNull(result);
			assertEquals((ObjectId) res.get("_id"),result);
		}
		assertTrue(false);
		
	}

	/**
	 * Run the Date[] getDateRange() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testGetDateRange_1()
		throws Exception {
		DBCollection collection = db.getCollection("pm_extraction_logs");
		
		DBObject obj = new BasicDBObject("extraction_status","successful").append("start_time", "123456").append("end_time", "098765");
		collection.insert(obj);
		
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);

		Date[] result = fixture.getDateRange();
		
		assertNotNull(result);
		assertNotNull(result[0]);
		assertNotNull(result[1]);
		assertEquals(new Date(Long.parseLong("098765")),result[0]);
	}

	/**
	 * Run the Date[] getDateRange() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testGetDateRange_2()
		throws Exception {
		DBCollection collection = db.getCollection("pm_extraction_logs");
		
		collection.remove(new BasicDBObject());
		
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);

		Date[] result = fixture.getDateRange();
		
		assertNull(result);
	}



	/**
	 * Run the boolean isActive() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testIsActive_1()
		throws Exception {
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.startExtractors();
		assertTrue(fixture.isActive());
	}

	/**
	 * Run the boolean isActive() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testIsActive_2()
		throws Exception {
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.startExtractors();
		assertTrue(fixture.isActive());
		fixture.setExit(true);
		assertFalse(fixture.isActive());
	}


	/**
	 * Run the boolean isCompleted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testIsCompleted_1()
		throws Exception {
		

		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.startExtractors();
		assertNotNull(fixture);
		fixture.setExit(true);
		fixture.stopExtractors();
	}


	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testIsExit_1()
		throws Exception {
		

		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.setExit(true);
		boolean result = fixture.isExit();
		assertTrue(result);
	}

	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testIsExit_2()
		throws Exception {
		
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		boolean result = fixture.isExit();
		assertFalse(result);
	}

	/**
	 * Run the Object logDateRange(Date[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testLogDateRange_1()
		throws Exception {
		

		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		Date[] range = new Date[] {};

		fixture.logDateRange(range);
		assertNotNull(fixture.getCurrentLoadId());
	}



	/**
	 * Run the void setExit(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testSetExit_1()
		throws Exception {
		

		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		boolean b = true;

		fixture.setExit(b);
	}

	/**
	 * Run the void startExtractors() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testStartExtractors_1()
		throws Exception {
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.setExit(false);
		try{
			fixture.startExtractors();
		}catch(Exception e){
			e.printStackTrace();
		}
		assertTrue(fixture.isInitialized());
		assertFalse(fixture.isExit());
		assertTrue(fixture.isActive());
	}


	/**
	 * Run the void stopExtractors() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Test
	public void testStopExtractors_1()
		throws Exception {
		
		ExtractorManager fixture = new ExtractorManager(new QueueManager(), true, true);
		fixture.startExtractors();
		fixture.stopExtractors();
		assertFalse(fixture.isActive());
	}



	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		Properties properties =System.getProperties();
		properties.load(new FileInputStream("config/pmloader-sql.properties"));
		System.setProperties(properties);
		
		String uri = "mongodb://cdspdb:cdspdb@g2t1888c.austin.hp.com:27017/productmaster";
		try {
			MongoClientURI clientURI = new MongoClientURI(uri);
			mongoClient = new MongoClient(clientURI);
			
		} catch (UnknownHostException e) {
			throw new LoaderInitializationException("Mongodb connection failed. mongodb uri - "+uri);
		}
		db = mongoClient.getDB("productmaster");
		
		JDBCUtils.init("jdbc:oracle:thin:@(DESCRIPTION =" +
				"(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)" +
				"(HOST = gvu0599.austin.hp.com)(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)(METHOD = BASIC)" +
				"(RETRIES = 3)(DELAY = 15)))", "xdmsdoc", "Itg1-Concentra");
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 6/17/13 1:03 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ExtractorManagerTest.class);
	}
}