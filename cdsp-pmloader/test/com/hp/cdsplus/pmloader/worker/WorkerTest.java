package com.hp.cdsplus.pmloader.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.queue.QueueManager;

/**
 * The class <code>WorkerTest</code> contains tests for the class <code>{@link WorkerThread}</code>.
 *
 * @generatedBy CodePro at 6/13/13 1:32 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class WorkerTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	public WorkerTest() {
	}

	/**
	 * Run the Worker(QueueManager,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testWorker_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		int workerId = 1;

		WorkerThread result = new WorkerThread(queueMgr, workerId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isExit());
		assertEquals(false, result.isCompleted());
	}

	/**
	 * Run the Worker(QueueManager,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test(expected = com.hp.cdsplus.pmloader.exception.LoaderException.class)
	public void testWorker_2()
		throws Exception {
		QueueManager queueMgr = null;
		int workerId = 1;

		WorkerThread result = new WorkerThread(queueMgr, workerId);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the boolean isCompleted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testIsCompleted_1()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(true);

		boolean result = fixture.isCompleted();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isCompleted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testIsCompleted_2()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(true);

		boolean result = fixture.isCompleted();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testIsExit_1()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(true);

		boolean result = fixture.isExit();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testIsExit_2()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(false);

		boolean result = fixture.isExit();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testRun_1()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(true);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void setExit(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	@Test
	public void testSetExit_1()
		throws Exception {
		WorkerThread fixture = new WorkerThread(new QueueManager(), 1);
		fixture.setExit(true);
		boolean exit = true;

		fixture.setExit(exit);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/13/13 1:32 PM
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
	 * @generatedBy CodePro at 6/13/13 1:32 PM
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
	 * @generatedBy CodePro at 6/13/13 1:32 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(WorkerTest.class);
	}
}