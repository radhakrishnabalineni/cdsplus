package com.hp.cdsplus.pmloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.exception.LoaderException;
import com.hp.cdsplus.pmloader.item.WorkItem;
import com.hp.cdsplus.pmloader.queue.QueueManager;

/**
 * The class <code>LevelTest</code> contains tests for the class <code>{@link Level}</code>.
 *
 * @generatedBy CodePro at 6/13/13 10:09 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class LevelTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	public LevelTest() {
	}

	/**
	 * Run the String getContentSQL() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetContentSQL_1()
		throws Exception {
		assertEquals(System.getProperty(PMLoaderConstants.PRODUCT_TYPE_CONTENT), Level.PRODUCT_TYPE.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_CAT_CONTENT), Level.MARKETING_CATEGORY.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.SUPPORT_CAT_CONTENT), Level.SUPPORT_CATEGORY.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_CONTENT), Level.SUPPORT_SUBCATEGORY.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_CONTENT), Level.MARKETING_SUBCATEGORY.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.BIG_SERIES_CONTENT), Level.PRODUCT_BIGSERIES.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_SERIES_CONTENT), Level.PRODUCT_SERIES.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_MODEL_CONTENT), Level.PRODUCT_NAME.getContentSQL());
		assertEquals(System.getProperty(PMLoaderConstants.SKU_CONTENT), Level.PRODUCT_NUMBER.getContentSQL());
	}

	/**
	 * Run the String getDeltaLoadKey() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetDeltaLoadKey_1()
		throws Exception {
		assertEquals("PRODUCT_TYPE_KY", Level.PRODUCT_TYPE.getDeltaLoadKey());
		assertEquals("MKTG_PRODUCT_CAT_KY", Level.MARKETING_CATEGORY.getDeltaLoadKey());
		assertEquals("SPT_PRODUCT_CAT_KY", Level.SUPPORT_CATEGORY.getDeltaLoadKey());
		assertEquals("MKTG_PRODUCT_SUBCAT_KY", Level.MARKETING_SUBCATEGORY.getDeltaLoadKey());
		assertEquals("SPT_PRODUCT_SUBCAT_KY", Level.SUPPORT_SUBCATEGORY.getDeltaLoadKey());
		assertEquals("PRODUCT_BIGSERIES_KY", Level.PRODUCT_BIGSERIES.getDeltaLoadKey());
		assertEquals("PRODUCT_SERIES_KY", Level.PRODUCT_SERIES.getDeltaLoadKey());
		assertEquals("PRODUCT_MODEL_KY", Level.PRODUCT_NAME.getDeltaLoadKey());
		assertEquals("PRODUCT_KY", Level.PRODUCT_NUMBER.getDeltaLoadKey());
		
	}

	/**
	 * Run the String getDeltaQry() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetDeltaQry_1()
		throws Exception {
		assertEquals(System.getProperty(PMLoaderConstants.PRODUCT_TYPE_DELTA), Level.PRODUCT_TYPE.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_CAT_DELTA), Level.MARKETING_CATEGORY.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.SUPPORT_CAT_DELTA), Level.SUPPORT_CATEGORY.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_DELTA), Level.SUPPORT_SUBCATEGORY.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_DELTA), Level.MARKETING_SUBCATEGORY.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.BIG_SERIES_DELTA), Level.PRODUCT_BIGSERIES.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_SERIES_DELTA), Level.PRODUCT_SERIES.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_MODEL_DELTA), Level.PRODUCT_NAME.getDeltaQry());
		assertEquals(System.getProperty(PMLoaderConstants.SKU_DELTA), Level.PRODUCT_NUMBER.getDeltaQry());
	}

	/**
	 * Run the String getFullLoadKey() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetFullLoadKey_1()
		throws Exception {
		assertEquals("PRODUCT_TYPE_OID", Level.PRODUCT_TYPE.getFullLoadKey());
		assertEquals("MARKETING_CATEGORY_OID", Level.MARKETING_CATEGORY.getFullLoadKey());
		assertEquals("SUPPORT_CATEGORY_OID", Level.SUPPORT_CATEGORY.getFullLoadKey());
		assertEquals("SUPPORT_SUBCATEGORY_OID", Level.SUPPORT_SUBCATEGORY.getFullLoadKey());
		assertEquals("MARKETING_SUBCATEGORY_OID", Level.MARKETING_SUBCATEGORY.getFullLoadKey());
		assertEquals("PRODUCT_BIGSERIES_OID", Level.PRODUCT_BIGSERIES.getFullLoadKey());
		assertEquals("PRODUCT_SERIES_OID", Level.PRODUCT_SERIES.getFullLoadKey());
		assertEquals("PRODUCT_NAME_OID", Level.PRODUCT_NAME.getFullLoadKey());
		assertEquals("PRODUCT_NUMBER_OID", Level.PRODUCT_NUMBER.getFullLoadKey());
	}

	/**
	 * Run the String getFullLoadSql() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetFullLoadSql_1()
		throws Exception {
		assertEquals(System.getProperty(PMLoaderConstants.PRODUCT_TYPE_ALL), Level.PRODUCT_TYPE.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_CAT_ALL), Level.MARKETING_CATEGORY.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.SUPPORT_CAT_ALL), Level.SUPPORT_CATEGORY.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_ALL), Level.SUPPORT_SUBCATEGORY.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_ALL), Level.MARKETING_SUBCATEGORY.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.BIG_SERIES_ALL), Level.PRODUCT_BIGSERIES.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_SERIES_ALL), Level.PRODUCT_SERIES.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_MODEL_ALL), Level.PRODUCT_NAME.getFullLoadSql());
		assertEquals(System.getProperty(PMLoaderConstants.SKU_ALL), Level.PRODUCT_NUMBER.getFullLoadSql());
	}

	/**
	 * Run the String getHierarchySQL() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetHierarchySQL_1()
		throws Exception {
		assertEquals(System.getProperty(PMLoaderConstants.PRODUCT_TYPE_HIERARCHY), Level.PRODUCT_TYPE.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_CAT_HIERARCHY), Level.MARKETING_CATEGORY.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.SUPPORT_CAT_HIERARCHY), Level.SUPPORT_CATEGORY.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_HIERARCHY), Level.SUPPORT_SUBCATEGORY.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_HIERARCHY), Level.MARKETING_SUBCATEGORY.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.BIG_SERIES_HIERARCHY), Level.PRODUCT_BIGSERIES.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_SERIES_HIERARCHY), Level.PRODUCT_SERIES.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.PROD_MODEL_HIERARCHY), Level.PRODUCT_NAME.getHierarchySQL());
		assertEquals(System.getProperty(PMLoaderConstants.SKU_HIERARCHY), Level.PRODUCT_NUMBER.getHierarchySQL());
	}

	/**
	 * Run the String getTreeType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testGetTreeType_1()
		throws Exception {
		assertEquals("support", Level.PRODUCT_TYPE.getTreeType());
		assertEquals("marketing",  Level.MARKETING_SUBCATEGORY.getTreeType());
		assertEquals("marketing",  Level.MARKETING_CATEGORY.getTreeType());
		assertEquals("support", Level.SUPPORT_CATEGORY.getTreeType());
		assertEquals("support", Level.SUPPORT_SUBCATEGORY.getTreeType());
		assertEquals("support", Level.PRODUCT_BIGSERIES.getTreeType());
		assertEquals("support", Level.PRODUCT_SERIES.getTreeType());
		assertEquals("support", Level.PRODUCT_NAME.getTreeType());
		assertEquals("support", Level.PRODUCT_NUMBER.getTreeType());
	
		
	}

	/**
	 * Run the void process(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testProcess_1()
		throws Exception {
		Level fixture = Level.PRODUCT_TYPE;
		QueueManager queueManager = null;
		// QueueManager not initialized
		try{
			fixture.process(queueManager);
		}catch(Exception e){
			assertTrue(e instanceof LoaderException);
		}
	}

	/**
	 * Run the void process(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testProcess_2()
		throws Exception {
		//test when jdbc utils initialized
		Level fixture = Level.PRODUCT_TYPE;
		
		QueueManager queueManager = new QueueManager();

		fixture.process(queueManager);
		
		assertNotNull(queueManager);
		assertTrue( queueManager.size() > 0);
		assertFalse(queueManager.isEmpty());
		
		WorkItem item = queueManager.pop();
		assertNotNull(item.getOid());
		assertNotNull(item.getLevel());
		assertEquals(Level.PRODUCT_TYPE, item.getLevel());
	}

	/**
	 * Run the void process(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testProcess_3()
		throws Exception {
		Level fixture = Level.PRODUCT_TYPE;
		QueueManager queueManager = new QueueManager();
		// JDBCUtils not initialized
		try{
			fixture.process(queueManager);
		}catch(Exception e){
			assertTrue(e instanceof SQLException);
		}
	}

	/**
	 * Run the void process(QueueManager) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Test
	public void testProcess_4()
		throws Exception {
		Level fixture = Level.MARKETING_CATEGORY;
		QueueManager queueManager = new QueueManager();
		fixture.process(queueManager);
	}
	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("CONFIG_LOCATION","config/pmloader.properties");
		new PMasterLoader();
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 6/13/13 10:09 AM
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
	 * @generatedBy CodePro at 6/13/13 10:09 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(LevelTest.class);
	}
}