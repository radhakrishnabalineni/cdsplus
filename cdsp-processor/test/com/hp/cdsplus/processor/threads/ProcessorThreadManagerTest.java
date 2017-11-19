package com.hp.cdsplus.processor.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.queue.QueueManager;

/**
 * The class <code>WorkerManagerTest</code> contains tests for the class <code>{@link ProcessorThreadManager}</code>.
 *
 * @generatedBy CodePro at 7/16/13 9:53 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ProcessorThreadManagerTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	public ProcessorThreadManagerTest() {
	}

	/**
	 * Run the WorkerManager(QueueManager) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testWorkerManager_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		ProcessorThreadManager result = new ProcessorThreadManager(queueMgr);

		assertNotNull(result);
		assertNotNull(result.getQueueMgr());
		assertEquals(queueMgr,result.getQueueMgr());

		assertEquals(false, result.isComplete());
		assertEquals(false, result.isExit());
	}

	/**
	 * Run the QueueManager getQueueMgr() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testGetQueueMgr_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.setQueueMgr(new QueueManager());
		QueueManager result = fixture.getQueueMgr();

		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(null, result.pop());
	}

	/**
	 * Run the void setComplete(boolean) and isComplete() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testSetComplete_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.setComplete(true);
		assertTrue(fixture.isComplete());
		fixture.setComplete(false);
		assertFalse(fixture.isComplete());
	}

	/**
	 * Run the void setExit(boolean) and isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testSetExit_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.setComplete(true);
		fixture.setQueueMgr(new QueueManager());
		fixture.setExit(true);
		boolean isExit = true;

		fixture.setExit(isExit);

		// add additional test code here
	}

	/**
	 * Run the void setQueueMgr(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testSetQueueMgr_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.setComplete(true);
		fixture.setQueueMgr(new QueueManager());
		fixture.setExit(true);
		QueueManager queueMgr = new QueueManager();

		fixture.setQueueMgr(queueMgr);

		assertEquals(queueMgr, fixture.getQueueMgr());
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testStart_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.start();
		assertTrue(fixture.getProcessorThreadListSize() > 0);
	}

	/**
	 * Run the void stop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testStop_1()
		throws Exception {
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.start();
		assertEquals(fixture.getProcessorThreadListSize(),6);
		assertTrue(fixture.isInitialized());
		assertTrue(fixture.isActive());
		assertFalse(fixture.isComplete());
		assertFalse(fixture.isExit());
		
		fixture.stop();
		//assertEquals(fixture.getWorkerListSize(),0);
		assertFalse(fixture.isActive());
		assertTrue(fixture.isComplete());
		
		assertTrue(fixture.isExit());
		
	}
	
	/**
	 * Run the void loadContentItems() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 9:53 AM
	 */
	@Test
	public void testLoadContentItems_1()
			throws Exception {
		QueueManager queueManager = new QueueManager();
		ProcessorThreadManager fixture = new ProcessorThreadManager(queueManager);
		fixture.loadContentItems();
		assertNotNull(fixture.getQueueMgr());
		assertTrue(fixture.getQueueMgr().size() > 0);
		while(!fixture.getQueueMgr().isEmpty()){
			assertNotNull(fixture.getQueueMgr().pop());
		}
	}
	
	/**
	 * Run the void setFatal(boolean isExit) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 9:53 AM
	 */
	@Test
	public void testSetFatal_1()
		throws Exception{
		ProcessorThreadManager fixture = new ProcessorThreadManager(new QueueManager());
		fixture.setFatal(true);
		assertTrue(fixture.isFatal());
		
		fixture.setFatal(false);
		assertFalse(fixture.isFatal());
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("mongo.configuration","config/mongo.properties");
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
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
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ProcessorThreadManagerTest.class);
	}
}