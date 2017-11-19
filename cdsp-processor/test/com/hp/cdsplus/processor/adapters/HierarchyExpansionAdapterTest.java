/*package com.hp.cdsplus.processor.adapters;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Array;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.item.WorkItem;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

*//**
 * The class <code>HierarchyExpansionAdapterTest</code> contains tests for the class <code>{@link HierarchyExpansionAdapter}</code>.
 *
 * @generatedBy CodePro at 7/17/13 2:21 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 *//*
public class HierarchyExpansionAdapterTest {
	*//**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/17/13 2:21 PM
	 *//*
	public HierarchyExpansionAdapterTest() {
	}

	*//**
	 * TESTCASE : 1
	 *//*
	@Test
	public void testHierarchyExpansionAdapter_1(){
		HierarchyExpansionAdapter hierarchyExpansionAdapter = new HierarchyExpansionAdapter();
		assertNotNull(hierarchyExpansionAdapter);
	}

	*//**
	 * TESTCASE : 2
	 * Content Type : marketingstandard
	 * Document - c50324782
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_1() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("marketingstandard");
		workItem.setId("c50324782");
		fixture.evaluate(workItem);
	}
	*//**
	 * TESTCASE : 3
	 * Content Type : library
	 * Document : c50326107
	 * Products : { "product" : [ "PMN_5034238" , "PMN_3442753" , "PMN_5303451" , "PMN_5034231"]}
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_2() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("library");
		workItem.setId("c50326107");
		fixture.evaluate(workItem);
	}
	*//**
	 * TestCase : 4
	 * Content Type : library
	 * c50326400
	 * Products : ""
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_3() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("library");
		workItem.setId("c50326400");
		fixture.evaluate(workItem);
	}
	*//**
	 * 
	 *//*
	@Test
	public void testGetList_1(){
		
	}
	
	*//**
	 * TESTCASE : 5
	 * Content-Type - library
	 * Document - c50326166
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testGetProductList_1() throws AdapterException, MongoUtilsException, OptionsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		
		WorkItem workItem = new WorkItem();
		workItem.setContentType("library");
		workItem.setId("c50326166");
		TreeSet<String> output = fixture.getProductList(workItem);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 6
	 * Content-Type - library
	 * Document -c50326204
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testGetProductList_2() throws AdapterException, MongoUtilsException, OptionsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem workItem = new WorkItem();
		workItem.setContentType("library");
		workItem.setId("c50326204");
		TreeSet<String> output = fixture.getProductList(workItem);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 7
	 * Content-Type - "marketingstandard"
	 * Document -c50324784
	 * Products - { "product" : "PMS_412160"} 
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testGetProductList_3() throws AdapterException, MongoUtilsException, OptionsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem workItem = new WorkItem();
		workItem.setContentType("marketingstandard");
		workItem.setId("c50324784");
		TreeSet<String> output = fixture.getProductList(workItem);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 8
	 * 
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testExpandProductHierarchy_1() throws OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		TreeSet<String> product_list = new TreeSet<String>();
		DBObject updatedoc= new BasicDBObject();
		DBObject output = fixture.expandProductHierarchy(product_list,updatedoc);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 9
	 * Content Type : marketingstandard
	 * Document : c50324782
	 * 
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testExpandProductHierarchy_2() throws OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		TreeSet<String> product_list = new TreeSet<String>();
		product_list.add("412160");
		DBObject updatedoc= new BasicDBObject();
		DBObject output = fixture.expandProductHierarchy(product_list,updatedoc);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 10
	 * Content Type : library
	 * Document : c50326107
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testExpandProductHierarchy_3() throws OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		TreeSet<String> product_list = new TreeSet<String>();
		product_list.add("5034238");
		product_list.add("3442753");
		product_list.add("5303451");
		product_list.add("5034231");
		DBObject updatedoc= new BasicDBObject();
		DBObject output = fixture.expandProductHierarchy(product_list,updatedoc);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 11
	 * Content Type : soar
	 * Document - bi100
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluateSoar_1() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("bi100");
		fixture.evaluate(workItem);
	}
	*//**
	 * TESTCASE : 12
	 * Content Type : soar
	 * Document : col61018
	 * Products : { "product" : [ "225882"]}
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluateSoar_2() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("col61018");
		fixture.evaluate(workItem);
}
*//**
	 * TESTCASE : 13
	 * Content-Type - soar
	 * Document - col61018
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testGetProductListSoar_1() throws AdapterException, MongoUtilsException, OptionsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("col61018");
		DBObject updatedoc= new BasicDBObject();
		BasicDBObject output = fixture.getSoarProductList(workItem,updatedoc);
		//System.out.println(output);
	}
	*//**
	 * TESTCASE : 14
	 * Content-Type - "soar"
	 * Document -mtx-fb41d42c63ed404c
	 * Products - { "product" : "4311905"} 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testGetProductListSoar_3() throws AdapterException, MongoUtilsException, OptionsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("mtx-fb41d42c63ed404c");
		DBObject updatedoc = new BasicDBObject();
		BasicDBObject output = fixture.getSoarProductList(workItem,updatedoc);
		}
		*//**
	 * TESTCASE : 15
	 * Content Type : soar
	 * Document : mtx-d085e1b694b9441a
	 * product-[{ "@type" : "PMS" , "@oid" : "1146658" , "@deleted-product" : "No" , "#" : "HP Integrated Lights-Out 2 (iLO 2) Featuring iLO Advanced"}]
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testExpandProductHierarchySoar_2() throws OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		BasicDBList product=null;
		//for testing
		product.add("PMS");
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		TreeSet<String> product_list = new TreeSet<String>();
		product_list.add("1146658");
		DBObject updatedoc = new BasicDBObject();
		String item_id="abc123";
		DBObject output = fixture.expandSoarProductHierarchy(product_list,product,updatedoc,item_id);
		//System.out.println(output);
	}
		*//**
	 * TESTCASE : 16
	 * Content Type : soar
	 * Document : bi100
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testExpandProductHierarchySoar_3() throws OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		BasicDBList product=null;
		//for testing
		product.add("PMS");
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		TreeSet<String> product_list = new TreeSet<String>();
		product_list.add("21437");
		product_list.add("21436");
		DBObject updatedoc = new BasicDBObject();
		String item_id="abc123";
		DBObject output = fixture.expandSoarProductHierarchy(product_list,product,updatedoc,item_id);
		System.out.println(output);
	}
	*//**
	 * 
	 * Content Type : soar
	 * col24426
	 * Products : 1
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_4() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("col24426");
		fixture.evaluate(workItem);
	}
	*//**
	 * 
	 * Content Type : soar
	 * col21513
	 * Products : 1
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_5() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("col21513");
		fixture.evaluate(workItem);
	}
	*//**
	 * 
	 * Content Type : soar
	 * bi100
	 * Products : 2
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_6() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("bi100");
		fixture.evaluate(workItem);
	}
	*//**
	 * 
	 * Content Type : soar
	 * col28436
	 * Products : 0
	 * @throws AdapterException
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 *//*
	@Test
	public void testEvaluate_7() throws AdapterException, OptionsException, MongoUtilsException{
		HierarchyExpansionAdapter fixture = new HierarchyExpansionAdapter();
		WorkItem workItem = new WorkItem();
		workItem.setContentType("soar");
		workItem.setId("col28436");
		fixture.evaluate(workItem);
	}
	*//**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 *//*
	@Before
	public void setUp()
		throws Exception {
		System.setProperty("mongo.configuration", "config/mongo.properties");
	}

	*//**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 8/12/13 1:12 PM
	 *//*
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}

	*//**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 7/17/13 2:21 PM
	 *//*
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(HierarchyExpansionAdapterTest.class);
	}
}*/