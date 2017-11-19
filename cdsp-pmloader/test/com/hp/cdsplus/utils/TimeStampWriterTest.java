package com.hp.cdsplus.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>TimeStampWriterTest</code> contains tests for the class <code>{@link TimeStampWriter}</code>.
 *
 * @generatedBy CodePro at 6/12/13 10:07 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class TimeStampWriterTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/12/13 10:07 AM
	 */
	public TimeStampWriterTest() {
	}

	/**
	 * Run the TimeStampWriter(OutputStream) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:07 AM
	 */
	@Test
	public void testTimeStampWriter_1()
		throws Exception {
		OutputStream os = new TimeStampWriter(System.out);

		TimeStampWriter result = new TimeStampWriter(os);

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.checkError());
	}

	/**
	 * Run the void println(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:07 AM
	 */
	@Test
	public void testPrintln_1()
		throws Exception {
		TimeStampWriter fixture = new TimeStampWriter(new ByteArrayOutputStream());
		Object o = new Object();

		fixture.println(o);
		fixture.close();

		// add additional test code here
	}

	/**
	 * Run the void println(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 10:07 AM
	 */
	@Test
	public void testPrintln_2()
		throws Exception {
		TimeStampWriter fixture = new TimeStampWriter(new ByteArrayOutputStream());
		String s = "";

		fixture.println(s);
		fixture.close();
		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/12/13 10:07 AM
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
	 * @generatedBy CodePro at 6/12/13 10:07 AM
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
	 * @generatedBy CodePro at 6/12/13 10:07 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(TimeStampWriterTest.class);
	}
}