package com.hp.cdsplus.pmloader.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.item.WorkItem;

/**
 * The class <code>QueueManagerTest</code> contains tests for the class <code>{@link QueueManager}</code>.
 *
 * @generatedBy CodePro at 6/12/13 10:02 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class QueueManagerTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	public QueueManagerTest() {
	}

	/**
	 * Run the QueueManager() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testQueueManager_1()
		throws Exception {

		QueueManager result = new QueueManager();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(null, result.pop());
		assertEquals(true, result.isEmpty());
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testIsEmpty_1()
		throws Exception {
		QueueManager fixture = new QueueManager();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testIsEmpty_2()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(EasyMock.createNiceMock(WorkItem.class));

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the WorkItem pop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testPop_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(EasyMock.createNiceMock(WorkItem.class));

		WorkItem result = fixture.pop();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getLevel());
	}

	/**
	 * Run the WorkItem pop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testPop_2()
		throws Exception {
		QueueManager fixture = new QueueManager();

		WorkItem result = fixture.pop();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the void push(WorkItem) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testPush_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(EasyMock.createNiceMock(WorkItem.class));
		WorkItem item = EasyMock.createMock(WorkItem.class);
		// add mock object expectations here

		EasyMock.replay(item);

		fixture.push(item);

		// add additional test code here
		EasyMock.verify(item);
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	@Test
	public void testSize_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(EasyMock.createNiceMock(WorkItem.class));

		int result = fixture.size();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/12/13 10:02 PM
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
	 * @generatedBy CodePro at 6/12/13 10:02 PM
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
	 * @generatedBy CodePro at 6/12/13 10:02 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(QueueManagerTest.class);
	}
}