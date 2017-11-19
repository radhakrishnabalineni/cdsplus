package com.hp.cdsplus.processor.service;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;

/**
 * The class <code>WorkItemServiceHandlerTest</code> contains tests for the class <code>{@link WorkItemServiceHandler}</code>.
 *
 * @generatedBy CodePro at 8/9/13 9:54 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class WorkItemServiceHandlerTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	public WorkItemServiceHandlerTest(String name) {
	}

	/**
	 * Run the WorkItemServiceHandler() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testWorkItemServiceHandler_1()
		throws Exception {

		WorkItemServiceHandler result = new WorkItemServiceHandler();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_1()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_2()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_3()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_4()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_5()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_6()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_7()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test
	public void testDoService_8()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		WorkItem item = new WorkItem();
		item.setEventType("");

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Run the void doService(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	@Test(expected = com.hp.cdsplus.processor.exception.ServiceHandlerException.class)
	public void testDoService_9()
		throws Exception {
		WorkItemServiceHandler fixture = new WorkItemServiceHandler();
		Item item = null;

		fixture.doService(item);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
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
	 * @generatedBy CodePro at 8/9/13 9:54 AM
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
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(WorkItemServiceHandlerTest.class);
	}
}