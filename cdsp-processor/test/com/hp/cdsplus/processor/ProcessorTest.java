package com.hp.cdsplus.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ProcessorTest</code> contains tests for the class <code>{@link Processor}</code>.
 *
 * @generatedBy CodePro at 7/17/13 2:12 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ProcessorTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	public ProcessorTest() {
	}

	/**
	 * Run the Processor() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testProcessor_1()
		throws Exception {

		Processor result = new Processor();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isExit());
	}

	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testIsExit_1()
		throws Exception {
		Processor fixture = new Processor();

		boolean result = fixture.isExit();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isExit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testIsExit_2()
		throws Exception {
		Processor fixture = new Processor();

		boolean result = fixture.isExit();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the void main(String[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testMain_1()
		throws Exception {
		//String[] args = new String[] {};

		//Processor.main(args);

		// add additional test code here
	}

	
	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testRun_1()
		throws Exception {
		//Processor fixture = new Processor();
//
		//fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testStart_1()
		throws Exception {
		//Processor fixture = new Processor();

		//fixture.start();

		// add additional test code here
	}

	/**
	 * Run the void stop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	@Test
	public void testStop_1()
		throws Exception {
		//Processor fixture = new Processor();

		//fixture.stop();

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 7/17/13 2:12 PM
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
	 * @generatedBy CodePro at 7/17/13 2:12 PM
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
	 * @generatedBy CodePro at 7/17/13 2:12 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ProcessorTest.class);
	}
}