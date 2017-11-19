package com.hp.cdsplus.pmloader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;
import com.hp.cdsplus.pmloader.extractor.ExtractorManager;
import com.hp.cdsplus.pmloader.queue.QueueManager;
import com.hp.cdsplus.pmloader.worker.WorkerManager;

/**
 * The class <code>PMasterLoaderTest</code> contains tests for the class <code>{@link PMasterLoader}</code>.
 *
 * @generatedBy CodePro at 6/17/13 11:06 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class PMasterLoaderTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	public PMasterLoaderTest() {
	}

	/**
	 * Run the PMasterLoader() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testPMasterLoader_1()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		PMasterLoader result = new PMasterLoader();

		assertNotNull(result);
	}

	/**
	 * Run the PMasterLoader() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testPMasterLoader_2()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "");
		
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		System.setProperty("SQL_FILE_LOCATION", "");
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		System.setProperty("LOG4J_LOCATION", "");
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		
	}

	/**
	 * Run the PMasterLoader() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testPMasterLoader_3()
		throws Exception {
		//by not setting CONFIG_LOCATION we are testing the null condition
		
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		
		System.clearProperty("SQL_FILE_LOCATION");
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		System.clearProperty("LOG4J_LOCATION");
		try{
			new PMasterLoader();
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the PMasterLoader() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testPMasterLoader_4()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/invalid.properties");
		
		try{
			new PMasterLoader();
		}catch(Exception e){
			
			assertTrue(e instanceof LoaderInitializationException);
		}
		
		System.setProperty("SQL_FILE_LOCATION", "config/invalid.properties");
		try{
			new PMasterLoader();
		}catch(Exception e){
			
			assertTrue(e instanceof LoaderInitializationException);
		}
		System.setProperty("LOG4J_LOCATION", "config/invalid.xml");
		try{
			new PMasterLoader();
		}catch(Exception e){
			
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/*
	 * Run the void execute() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 
	@Test
	public void testExecute_1()
		throws Exception {
		PMasterLoader fixture = new PMasterLoader();

		fixture.execute();
	}
*/
	/**
	 * Run the ExtractorManager initExtractor(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitExtractor_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();

		ExtractorManager result = PMasterLoader.initExtractor(queueMgr);
		assertTrue(result.isActive());
		assertFalse(result.isExit());
	}

	/**
	 * Run the void initJDBCUtils() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitJDBCUtils_1()
		throws Exception {
		
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		PMasterLoader fixture = new PMasterLoader();
		try{
			fixture.initJDBCUtils();
			assertTrue(true);
		}catch(Exception e){
			
			assertTrue(false);
		}

	}


	/**
	 * Run the void initLog4J() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitLog4J_1()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		PMasterLoader fixture = new PMasterLoader();
		try{
			fixture.initJDBCUtils();
			assertTrue(true);
		}catch(Exception e){
			
			assertTrue(false);
		}
	}


	/**
	 * Run the void initMailUtils() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitMailUtils_1()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		PMasterLoader fixture = new PMasterLoader();
		try{
			fixture.initJDBCUtils();
			assertTrue(true);
		}catch(Exception e){
			
			assertTrue(false);
		}
	}

	/**
	 * Run the void initMongoUtils() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitMongoUtils_1()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		PMasterLoader fixture = new PMasterLoader();
		try{
			fixture.initJDBCUtils();
			assertTrue(true);
		}catch(Exception e){
			
			assertTrue(false);
		}
	}

	/**
	 * Run the WorkerManager initWorkers(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testInitWorkers_1()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");

		PMasterLoader fixture = new PMasterLoader();
		QueueManager queueMgr = new QueueManager();
		WorkerManager result = fixture.initWorkers(queueMgr,new ObjectId("51c2cd194b865c4c264d89f8"));
		assertNotNull(result);
		assertTrue(result.isInitialized());
		assertTrue(result.isActive());
		assertFalse(result.isExit());
	}

	/**
	 * Run the void isNull(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testIsNull_1()
		throws Exception {
		String name = "test-property";
		String value = null;

		try{
			PMasterLoader.isNull(name, value);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		name = "test-property";
		value = "";
		try{
			PMasterLoader.isNull(name, value);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		name = "test-property";
		value = "value";
		try{
			PMasterLoader.isNull(name, value);
		}catch(Exception e){
			assertTrue(false);
		}
	}
	
	/**
	 * Run the void loadSystemProperties(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testLoadSystemProperties_1()
		throws Exception {
		PMasterLoader fixture = new PMasterLoader();

		try{
			fixture.loadSystemProperties(null);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
		 
	}

	/**
	 * Run the void loadSystemProperties(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testLoadSystemProperties_2()
		throws Exception {
		PMasterLoader fixture = new PMasterLoader();

		try{
			fixture.loadSystemProperties("");
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void loadSystemProperties(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testLoadSystemProperties_3()
		throws Exception {
		PMasterLoader fixture = new PMasterLoader();

		try{
			fixture.loadSystemProperties("INVALID_CONFIG_LOCATION");
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}



	/*
	 * Run the void main(String[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 
	@Test
	public void testMain_1()
		throws Exception {
		String[] args = new String[] {};

		PMasterLoader.main(args);

		// add additional test code here
	}
	*/
	/**
	 * Run the void notifyPSDMFailed(Throwable) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Test
	public void testNotifyPSDMFailed_1()
		throws Exception {
		Throwable t = new Throwable();

		PMasterLoader.notifyPSDMFailed(t);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	@Before
	public void setUp()
		throws Exception {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 6/17/13 11:06 AM
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
	 * @generatedBy CodePro at 6/17/13 11:06 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(PMasterLoaderTest.class);
	}
}