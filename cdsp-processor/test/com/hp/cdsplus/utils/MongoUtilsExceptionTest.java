package com.hp.cdsplus.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;

/**
 * The class <code>MongoUtilsExceptionTest</code> contains tests for the class <code>{@link MongoUtilsException}</code>.
 *
 * @generatedBy CodePro at 7/31/13 2:09 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class MongoUtilsExceptionTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/31/13 2:09 PM
	 */
	public MongoUtilsExceptionTest() {
	}

	/**
	 * Run the MongoUtilsException() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/31/13 2:09 PM
	 */
	@Test
	public void testMongoUtilsException_1()
		throws Exception {

		MongoUtilsException result = new MongoUtilsException();

		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("com.hp.cdsplus.utils.MongoUtilsException", result.toString());
		assertEquals(null, result.getMessage());
		assertEquals(null, result.getLocalizedMessage());
	}

	/**
	 * Run the MongoUtilsException(String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/31/13 2:09 PM
	 */
	@Test
	public void testMongoUtilsException_2()
		throws Exception {
		String message = "";

		MongoUtilsException result = new MongoUtilsException(message);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("com.hp.cdsplus.utils.MongoUtilsException: ", result.toString());
		assertEquals("", result.getMessage());
		assertEquals("", result.getLocalizedMessage());
	}

	/**
	 * Run the MongoUtilsException(String,Throwable) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 7/31/13 2:09 PM
	 */
	@Test
	public void testMongoUtilsException_3()
		throws Exception {
		String message = "";
		Throwable t = new Throwable();

		MongoUtilsException result = new MongoUtilsException(message, t);

		// add additional test code here
		assertNotNull(result);
		assertEquals("com.hp.cdsplus.utils.MongoUtilsException: ", result.toString());
		assertEquals("", result.getMessage());
		assertEquals("", result.getLocalizedMessage());
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 7/31/13 2:09 PM
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
	 * @generatedBy CodePro at 7/31/13 2:09 PM
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
	 * @generatedBy CodePro at 7/31/13 2:09 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(MongoUtilsExceptionTest.class);
	}
}