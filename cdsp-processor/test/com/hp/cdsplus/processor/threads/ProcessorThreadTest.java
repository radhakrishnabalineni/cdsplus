package com.hp.cdsplus.processor.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;

/**
 * The class <code>WorkerTest</code> contains tests for the class <code>{@link ProcessorThread}</code>.
 *
 * @generatedBy CodePro at 7/16/13 9:53 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ProcessorThreadTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	public ProcessorThreadTest() {
	}

	/**
	 * Run the Worker(QueueManager) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testWorker_1()
		throws Exception {
		QueueManager workItemQueue = new QueueManager();

		ProcessorThread result = new ProcessorThread(workItemQueue);
		Thread it = new Thread(result,"worker");
		it.start();
		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isComplete());
		assertEquals(false, result.isExit());
	}

	/**
	 * Run the boolean isComplete() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testIsComplete_1()
		throws Exception {
		ProcessorThread fixture = new ProcessorThread(new QueueManager());
		assertFalse(fixture.isComplete());
		Thread it = new Thread(fixture,"worker");
		it.start();
		fixture.setExit(true);
		it.join();

		assertTrue(fixture.isComplete());
		fixture.setComplete(false);
		assertFalse(fixture.isComplete());

	}



	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testIsExit_1()
		throws Exception {
		ProcessorThread fixture = new ProcessorThread(new QueueManager());
		assertFalse(fixture.isExit());
		fixture.setExit(true);
		assertTrue(fixture.isExit());
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testRun_1()
		throws Exception {
		QueueManager queue = new QueueManager();
		WorkItem item = new WorkItem();
		queue.push(item);
		ProcessorThread fixture = new ProcessorThread(queue);
		Thread workerThread = new Thread(fixture, "Worker");
		workerThread.start();
		while(!item.isProcessed()){
			Thread.sleep(1000);
		}
		fixture.setExit(true);
	}


	/**
	 * Run the void setComplete(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testSetComplete_1()
		throws Exception {
		
		ProcessorThread fixture = new ProcessorThread(new QueueManager());
		
		fixture.setComplete(true);
		boolean isComplete = true;

		fixture.setComplete(isComplete);
		assertTrue(fixture.isComplete());
		
		fixture.setComplete(false);
		assertFalse(fixture.isComplete());
	}

	/**
	 * Run the void setExit(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/16/13 9:53 AM
	 */
	@Test
	public void testSetExit_1()
		throws Exception {
		ProcessorThread fixture = new ProcessorThread(new QueueManager());
		boolean isExit = true;

		fixture.setExit(isExit);
		assertTrue(fixture.isExit());
		
		fixture.setExit(false);
		assertFalse(fixture.isExit());
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
		ProcessorThread fixture = new ProcessorThread(new QueueManager());
		Thread it = new Thread(fixture,"Worker");
		it.start();
		Thread.sleep(1000);
		fixture.stop();
		it.join();
		assertTrue(fixture.isExit());
		assertTrue(fixture.isComplete());
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
		// add additional set up code here
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
		new org.junit.runner.JUnitCore().run(ProcessorThreadTest.class);
	}
}