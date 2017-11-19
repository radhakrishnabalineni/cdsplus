package com.hp.cdsplus.pmloader.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.sql.Date;
import java.util.Properties;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.Level;
import com.hp.cdsplus.pmloader.item.WorkItem;
import com.hp.cdsplus.pmloader.queue.QueueManager;
import com.hp.cdsplus.utils.JDBCUtils;

/**
 * The class <code>ExtractorThreadTest</code> contains tests for the class <code>{@link ExtractorThread}</code>.
 *
 * @generatedBy CodePro at 6/17/13 12:21 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ExtractorThreadTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	public ExtractorThreadTest() {
	}

	/**
	 * Run the ExtractorThread(QueueManager,Level,Object,Date[],boolean) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testExtractorThread_1()
		throws Exception {
		
		QueueManager queueMgr = new QueueManager();
		Level level = Level.MARKETING_CATEGORY;
		ObjectId currentLoadId = new ObjectId("51c2cd194b865c4c264d89f8");
		Date[] dateRange = new Date[] {new Date(Long.parseLong("123456789")),new Date(Long.parseLong("123466789"))};
		
		boolean isDeltaMode = true;
		boolean isTempReload = true;
		boolean isRepublishMode = false;
		ExtractorThread result = new ExtractorThread(queueMgr, level, currentLoadId, dateRange, isDeltaMode,isRepublishMode, isTempReload);

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isCompleted());
	}

	/**
	 * Run the boolean isCompleted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testIsCompleted_1()
		throws Exception {
		ExtractorThread fixture = new ExtractorThread(new QueueManager(), Level.MARKETING_CATEGORY,
				new ObjectId("51c2cd194b865c4c264d89f8"),
				new Date[] {new Date(Long.parseLong("123456789")),
				new Date(Long.parseLong("123466789"))},
				true,false,true);
		boolean result = fixture.isCompleted();
		assertEquals(false, result);
	}


	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testRun_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		ExtractorThread fixture = new ExtractorThread(queueMgr, Level.PRODUCT_TYPE,
				null,
				null,
				false,false,true);
		while(!fixture.isCompleted()){
			Thread.currentThread();
			Thread.sleep((long) (Math.random()*2) *1000);
		}
		assertNotNull(queueMgr);
		//assertFalse(0 == queueMgr.size());
		WorkItem item = queueMgr.pop();
		assertNotNull(item);
		assertEquals(Level.PRODUCT_TYPE, item.getLevel());
		assertNotNull(item.getOid());

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testRun_2()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		ExtractorThread fixture = new ExtractorThread(queueMgr, Level.PRODUCT_TYPE,
				new ObjectId("51c2cd194b865c4c264d89f8"),
				null,
				true,false,true);
		while(!fixture.isCompleted()){
			Thread.currentThread();
			Thread.sleep((long) (Math.random()*2) *1000);
		}
		assertNotNull(queueMgr);
		//assertFalse(0 == queueMgr.size());
		WorkItem item = queueMgr.pop();
		assertNotNull(item);
		assertEquals(Level.PRODUCT_TYPE, item.getLevel());
		assertNotNull(item.getOid());

		// add additional test code here
	}
	
	/**
	 * Run the void setExit(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testSetExit_1()
		throws Exception {
		ExtractorThread fixture = new ExtractorThread(
				new QueueManager(),
				Level.MARKETING_CATEGORY,
				new ObjectId("51c2cd194b865c4c264d89f8"), 
				new Date[] {},
				true,false,true);
		boolean b = true;

		fixture.setExit(b);
		assertTrue(fixture.isExit());
	}
	

	/**
	 * Run the void setExit(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Test
	public void testLogExtractorStatus_1()
		throws Exception {
		ExtractorThread fixture = new ExtractorThread(new QueueManager(),
				Level.MARKETING_CATEGORY, 
				new ObjectId("51c2cd194b865c4c264d89f8"),
				new Date[] {}, 
				true,false,true);
		
		fixture.logExtractorStatus("Status completed");
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		Properties properties =System.getProperties();
		
		properties.load(new FileInputStream("config/pmloader-sql.properties"));
		
		System.setProperties(properties);
		
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
	 * @generatedBy CodePro at 6/17/13 12:21 PM
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
	 * @generatedBy CodePro at 6/17/13 12:21 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ExtractorThreadTest.class);
	}
}