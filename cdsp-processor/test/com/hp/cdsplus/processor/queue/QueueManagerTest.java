package com.hp.cdsplus.processor.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;


/**
 * The class <code>QueueManagerTest</code> contains tests for the class <code>{@link QueueManager}</code>.
 *
 * @generatedBy CodePro at 8/8/13 8:41 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class QueueManagerTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	public QueueManagerTest() {
	}

	/**
	 * Run the QueueManager() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
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
		assertEquals(0, result.getContentItemSize());
		assertEquals(0, result.getWorkItemSize());
	}

	/**
	 * Run the int getContentItemSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testGetContentItemSize_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		
		fixture.push(new ContentItem("library", 0L, null));
		int result = fixture.getContentItemSize();
		assertEquals(1, result);
		fixture.push(new ContentItem("MarketingStandard", 0L, null));
		result = fixture.getContentItemSize();
		assertEquals(2, result);
		fixture.push(new ContentItem("library", 0L, null));
		result = fixture.getContentItemSize();
		assertEquals(2, result);
	}

	/**
	 * Run the int getWorkItemSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testGetWorkItemSize_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new ContentItem("library", 0L, null));
		fixture.push(new WorkItem());
		fixture.push(new WorkItem());
		fixture.push(new WorkItem());
		int result = fixture.getWorkItemSize();

		// add additional test code here
		assertEquals(3, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testIsEmpty_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		boolean result = fixture.isEmpty();
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testIsEmpty_2()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new WorkItem());
		boolean result = fixture.isEmpty();
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testIsEmpty_3()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new ContentItem("library", 0L, null));
		boolean result = fixture.isEmpty();
		assertEquals(false, result);
	}

	/**
	 * Run the Item pop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPop_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		Item result = fixture.pop();
		assertEquals(null, result);
	}

	/**
	 * Run the Item pop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPop_2()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new WorkItem());
		assertEquals(1, fixture.getWorkItemSize());	
		Item result = fixture.pop();
		assertNotNull(result);
		assertEquals(0, fixture.getWorkItemSize());
	}

	/**
	 * Run the Item pop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPop_3()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new ContentItem("library", 0L, null));
		assertEquals(1, fixture.getContentItemSize());
		Item result = fixture.pop();

		assertNotNull(result);
		assertEquals(0, fixture.getContentItemSize());
	}

	/**
	 * Run the void push(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPush_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new WorkItem());
		Item item = new WorkItem();

		fixture.push(item);

		assertTrue(fixture.getWorkItemSize() > 0);
	}

	/**
	 * Run the void push(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPush_2()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new ContentItem("library", 0L, null));
		Item item = new ContentItem("marketingStandard", 0L, null);
		fixture.push(item);
		assertTrue(fixture.getContentItemSize() > 0);
	}

	/**
	 * Run the void push(Item) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testPush_3()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new ContentItem("library", 0L, null));
		fixture.push(new ContentItem("marketingStandard", 0L, null));
		assertTrue(fixture.getContentItemSize()  == 2);
		fixture.push(new ContentItem("marketingStandard", 0L, null));
		
		assertEquals(2, fixture.getContentItemSize());
		
		assertNotNull(fixture.pop());
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	@Test
	public void testSize_1()
		throws Exception {
		QueueManager fixture = new QueueManager();
		fixture.push(new WorkItem());
		int result = fixture.size();
		assertEquals(1, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/8/13 8:41 PM
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
	 * @generatedBy CodePro at 8/8/13 8:41 PM
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
	 * @generatedBy CodePro at 8/8/13 8:41 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(QueueManagerTest.class);
	}
}