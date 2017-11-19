package com.hp.cdsplus.processor.adapters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.item.WorkItem;

/**
 * The class <code>DeleteEventHandlerAdapterTest</code> contains tests for the class <code>{@link DeleteEventHandlerAdapter}</code>.
 *
 * @generatedBy CodePro at 8/12/13 1:12 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class DeleteEventHandlerAdapterTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 */
	public DeleteEventHandlerAdapterTest() {
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_1()
		throws Exception {
		DeleteEventHandlerAdapter fixture = new DeleteEventHandlerAdapter();
		WorkItem item = new WorkItem();
		item.setId("");

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_2()
		throws Exception {
		DeleteEventHandlerAdapter fixture = new DeleteEventHandlerAdapter();
		WorkItem item = new WorkItem();
		item.setId("");

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_3()
		throws Exception {
		DeleteEventHandlerAdapter fixture = new DeleteEventHandlerAdapter();
		WorkItem item = new WorkItem();
		item.setId("");

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
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
	 * @generatedBy CodePro at 8/12/13 1:12 PM
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
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(DeleteEventHandlerAdapterTest.class);
	}
}