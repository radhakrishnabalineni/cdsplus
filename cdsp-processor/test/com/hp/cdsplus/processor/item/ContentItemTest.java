package com.hp.cdsplus.processor.item;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.queue.QueueManager;

/**
 * The class <code>ContentItemTest</code> contains tests for the class <code>{@link ContentItem}</code>.
 *
 * @generatedBy CodePro at 8/8/13 9:41 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class ContentItemTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	public ContentItemTest() {
	}

	/**
	 * Run the ContentItem(String,Long) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testContentItem_1()
		throws Exception {
		String contentType = null;
		

		try{
			new ContentItem(contentType, 0L, null);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		
		contentType = "";
		try{
			new ContentItem(contentType, 0L, null);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		
		contentType = "library";
		
		ContentItem result = new ContentItem(contentType, 0L, null );
		assertNotNull(result);
	}

	/**
	 * Run the ContentItem(String,Long) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testContentItem_2()
		throws Exception {
		String contentType = "library";
		ContentItem result = new ContentItem(contentType, null, null);
		assertNotNull(result);
		
		result = new ContentItem(contentType, 0L, null);
		assertNotNull(result);
		
		result = new ContentItem(contentType, 0L, null);
		assertNotNull(result);
	}
	
	/**
	 * Run the ContentItem(String,Long) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testContentItem_3()
		throws Exception {
		String contentType = "library";
		
		ContentItem result = new ContentItem(contentType,0L, null);
		assertNotNull(result);
		try{
			new ContentItem("library",null, null);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		
	}

	/**
	 * Run the String getContentType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testGetContentType_1()
		throws Exception {
		ContentItem fixture = new ContentItem("library", 0L, null);
		String result = fixture.getContentType();
		assertNotNull(result);	
	}

	/**
	 * Run the Long getLastModified() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testGetLastModified_1()
		throws Exception {
		ContentItem fixture = new ContentItem("library", 0L, null);
		Long result = fixture.getLastModified();
		assertNotNull(result);
		fixture = new ContentItem("library", 0L, null);
		assertNotNull(fixture.getLastModified());
	}

	/**
	 * Run the void load() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testLoad_1()
		throws Exception {
		ContentItem fixture = new ContentItem("library",0L, null);
		fixture.load();
		// at this point we need to check if subscription eval worked and if we moved processed records from temp to live.
		// nothing much can be clarified at this point so leaving it as is for now.
	}

	/**
	 * Run the void save() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Test
	public void testSave_1()
		throws Exception {
		QueueManager queueMgr = new QueueManager();
		ContentItem fixture = new ContentItem("library",0L, null);
		assertNotNull(fixture);
		fixture.load();
		fixture.save();
		assertTrue(queueMgr.size() > 0);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("mongo.configuration","config/mongo.properties");
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 8/8/13 9:41 AM
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
	 * @generatedBy CodePro at 8/8/13 9:41 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ContentItemTest.class);
	}
}