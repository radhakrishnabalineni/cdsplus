package com.hp.cdsplus.processor.adapters;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;

/**
 * The class <code>MetadataPersistenceAdapterTest</code> contains tests for the class <code>{@link MetadataPersistenceAdapter}</code>.
 *
 * @generatedBy CodePro at 8/12/13 1:35 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class MetadataPersistenceAdapterTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	public MetadataPersistenceAdapterTest() {
	}

	/**
	 * Run the MetadataPersistenceAdapter() constructor test.
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test
	public void testMetadataPersistenceAdapter_1()
		throws Exception {
		Adapter result = new MetadataPersistenceAdapter();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test
	public void testEvaluate_1()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50322566");
		fixture.evaluate(item);
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_2()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_3()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_4()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_5()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_6()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_7()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Run the void evaluate(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Test(expected = com.hp.cdsplus.dao.exception.OptionsException.class)
	public void testEvaluate_8()
		throws Exception {
		Adapter fixture = new MetadataPersistenceAdapter();
		Item item = new WorkItem();

		fixture.evaluate(item);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("mongo.configuration", "config/mongo.properties");
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	@After
	public void tearDown()
		throws Exception {
		
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 8/12/13 1:35 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(MetadataPersistenceAdapterTest.class);
	}
}