package com.hp.cdsplus.pmloader.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.Level;
import com.hp.cdsplus.pmloader.PMLoaderConstants;
import com.hp.cdsplus.pmloader.PMasterLoader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * The class <code>WorkItemTest</code> contains tests for the class <code>{@link WorkItem}</code>.
 *
 * @generatedBy CodePro at 6/12/13 9:38 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class WorkItemTest {
	MongoClient mongoClient;
	DB db;
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	public WorkItemTest() {
	}

	/**
	 * Run the WorkItem(String,Level) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testWorkItem_1()
		throws Exception {
		String oid = "5276630";
		Level level = Level.SUPPORT_SUBCATEGORY;

		WorkItem result = new WorkItem(oid, level);

		// add additional test code here
		assertNotNull(result);
		assertEquals("5276630", result.getOid());
	}

	/**
	 * Run the Level getLevel() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testGetLevel_1()
		throws Exception {
		
		WorkItem fixture = new WorkItem("5276630", Level.SUPPORT_SUBCATEGORY);

		Level result = fixture.getLevel();

		assertNotNull(result);
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_CONTENT), result.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_HIERARCHY), result.getHierarchySQL());
		assertEquals("support", result.getTreeType());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_ALL), result.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_DELTA), result.getDeltaQry());
		assertEquals("SPT_PRODUCT_SUBCAT_KY", result.getDeltaLoadKey());
		assertEquals("SUPPORT_SUBCATEGORY_OID", result.getFullLoadKey());
		assertEquals("SUPPORT_SUBCATEGORY", result.name());
		assertEquals("SUPPORT_SUBCATEGORY", result.toString());
		assertEquals(4, result.ordinal());
	}

	/**
	 * Run the String getOid() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testGetOid_1()
		throws Exception {
		WorkItem fixture = new WorkItem("5276630", Level.SUPPORT_SUBCATEGORY);
		String result = fixture.getOid();
		assertEquals("5276630", result);
	}

	/**
	 * Run the void loadContent() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testLoadContent_1()
		throws Exception {
		String oid = "5276630";
		WorkItem fixture = new WorkItem(oid, Level.SUPPORT_SUBCATEGORY);
		fixture.loadContent();
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));

		content.remove(new BasicDBObject("_id",oid));

	}


	/**
	 * Run the void loadHierarchy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testLoadHierarchy_1()
		throws Exception {
		String oid = "5276630";
		WorkItem fixture = new WorkItem(oid, Level.SUPPORT_SUBCATEGORY);
		
		fixture.loadHierarchy();
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));
		hierarchy.remove(new BasicDBObject("_id",oid));
	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_1()
		throws Exception {
		String oid = "5098089";
		WorkItem fixture = new WorkItem("5098089", Level.SUPPORT_CATEGORY);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);

		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));

		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);

		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));

		hierarchy.remove(new BasicDBObject("_id",oid));

		content.remove(new BasicDBObject("_id",oid));

	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_2()
		throws Exception {
		String oid = "5098084";
		WorkItem fixture = new WorkItem("5098084", Level.MARKETING_CATEGORY);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);

		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));

		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);

		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));

	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_3()
		throws Exception {
		String oid = "5276630";
		WorkItem fixture = new WorkItem("5276630", Level.SUPPORT_SUBCATEGORY);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);

		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));

		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);

		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));

		hierarchy.remove(new BasicDBObject("_id",oid));

		content.remove(new BasicDBObject("_id",oid));

	}

	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_4()
		throws Exception {
		String oid = "5276628";
		WorkItem fixture = new WorkItem("5276628", Level.MARKETING_SUBCATEGORY);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));
		
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));

	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_5()
		throws Exception {
		String oid = "5319202";
		WorkItem fixture = new WorkItem("5319202", Level.PRODUCT_BIGSERIES);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));
		hierarchy.remove(new BasicDBObject("_id",oid));
		content.remove(new BasicDBObject("_id",oid));
	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_6()
		throws Exception {
		String oid = "5319203";
		WorkItem fixture = new WorkItem("5319203", Level.PRODUCT_SERIES);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));
	}

	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_7()
		throws Exception {
		String oid = "5319204";
		WorkItem fixture = new WorkItem("5319204", Level.PRODUCT_NAME);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));
	}
	
	/**
	 * Run the void process() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Test
	public void testProcess_8()
		throws Exception {
		String oid = "5319206";
		WorkItem fixture = new WorkItem("5319206", Level.PRODUCT_NUMBER);

		fixture.process();
		
		DBCollection content = db.getCollection(PMLoaderConstants.CONTENT_COLLECTION);
		
		assertNotNull(content.findOne(new BasicDBObject("_id",oid)));
		DBCollection hierarchy = db.getCollection(PMLoaderConstants.HIERARCHY_COLLECTION);
		
		assertNotNull(hierarchy.findOne(new BasicDBObject("_id",oid)));
	}
	
	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("CONFIG_LOCATION", "config/pmloader.properties");
		new PMasterLoader();
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 6/12/13 9:38 PM
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
	 * @generatedBy CodePro at 6/12/13 9:38 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(WorkItemTest.class);
	}
}