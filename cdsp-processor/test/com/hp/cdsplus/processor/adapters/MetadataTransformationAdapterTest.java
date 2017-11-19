/*package com.hp.cdsplus.processor.adapters;

import static org.junit.Assert.assertNotNull;

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
 * The class <code>MetadataTransformationAdapterTest</code> contains tests for the class <code>{@link MetadataTransformationAdapter}</code>.
 *
 * @generatedBy CodePro at 7/17/13 2:21 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 *//*
public class MetadataTransformationAdapterTest {
	*//**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 7/17/13 2:21 PM
	 *//*
	public MetadataTransformationAdapterTest() {
	}

	*//**
	 * @throws Exception
	 *//*
	@Test
	public void testMetadataTransformationAdapter_1() throws Exception{
		Adapter result = new MetadataTransformationAdapter();
		assertNotNull(result);
	}
	
	*//**
	 * Content Type : library
	 * Document : c50326166
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_1()
		throws Exception {
		Adapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50326166");
		fixture.evaluate(item);
	}
	
	*//**
	 * Content Type : library
	 * Document : c50326205
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_2()
		throws Exception {
		Adapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50326205");
		fixture.evaluate(item);
	}
	*//**
	 * c50324845
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_3()
		throws Exception {
		Adapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50324845");
		fixture.evaluate(item);
	}
	*//**
	 * c50324856
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_4()
		throws Exception {
		Adapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50324856");
		fixture.evaluate(item);
	}
	*//**
	 * 4aa1-2823enw
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_5()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("marketingstandard");
			item.setId("4aa1-2823enw");
			fixture.evaluate(item);
		}

	*//**
	 * Content Type : SOAR
	 * Document ID :col60753
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_6()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col60753");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : col20695
	 *  attachments:""
	 *  software-items : 4
	 *  product-supported : product : 2 for 4th Item
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_7()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col20695");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : col1278
	 *  collection :attachments:""
	 *  software-items : 7
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_8()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col1278");
			fixture.evaluate(item);
		}
	
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : bi100
	 *  collection :attachments:1
	 *  software-items : 14
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_9()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("bi100");
			fixture.evaluate(item);
		}
	
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : col21513
	 *  collection :attachments:0
	 *  software-items : 2
	 *  products : 1
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_10()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col21513");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : col24426
	 *  collection :attachments:1
	 *  software-items : 1
	 *  software-items :attachments : 3
	 *  products : 1
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_11()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col24426");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : SOAR
	 * 	Document ID : col28436
	 *  collection :attachments:1
	 *  software-items : 1
	 *  software-items : attachments:0
	 *  products : 0
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_12()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("soar");
			item.setId("col28436");
			fixture.evaluate(item);
		}
	
	*//**
	 *  Content Type : library
	 * 	Document ID : c50326204
	 * file_name : c50326204.bmp
	 *  products : ""
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_13()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("library");
			item.setId("c50326204");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : library
	 * 	Document ID : c50326209
	 *  file_name : ""
	 *  products : ""
	 *  hasAttachments =false
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_14()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("library");
			item.setId("c50326204");
			fixture.evaluate(item);
		}
	*//**
	 *  Content Type : marketingstandard
	 * 	Document ID : c50324824
	 *  renditions : 1
	 *  products : ""
	 *  hasAttachments =true
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_15()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("marketingstandard");
			item.setId("c50324824");
			fixture.evaluate(item);
	}
	*//**
	 *  Content Type : marketingstandard
	 * 	Document ID : c50317050
	 *  renditions : 0
	 *  products : ""
	 *  hasAttachments : false
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_16()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("marketingstandard");
			item.setId("c50317050");
			fixture.evaluate(item);
		}
	
	*//**
	 *  Content Type : marketingstandard
	 * 	Document ID : c50324881
	 *  renditions : 0
	 *  products : ""
	 *  hasAttachments : false
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_17()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("marketingstandard");
			item.setId("c50324881");
			fixture.evaluate(item);
		}
	*//**
	 *  Test Case : attachment name should be in lower case in projectref
	 *  Content Type : marketingstandard
	 * 	Document ID : 4aa1-2791enw
	 *  renditions : 3
	 *  products : ""
	 *  hasAttachments : true
	 * 
	 * @throws Exception
	 *//*
	@Test
	public void testEvaluate_18()
			throws Exception {
			Adapter fixture = new MetadataTransformationAdapter();
			WorkItem item = new WorkItem();
			item.setContentType("marketingstandard");
			item.setId("c50324881");
			fixture.evaluate(item);
		}
	@Test
	public void testGetList_1(){}
	@Test
	public void testGetObject_1(){}
	@Test
	public void testGetRegionTransformation_1() throws OptionsException, MongoUtilsException{
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		BasicDBList regionList = new BasicDBList();
		fixture.getRegionTransformation(regionList);
	}
	@Test
	public void testGetRegionTransformation_2() throws OptionsException, MongoUtilsException{
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		BasicDBList regionList = new BasicDBList();
		regionList.add("Asia Pacific and Japan");
		regionList.add("Western Europe");
		regionList.add("Americas");
		regionList.add("Europe");
		regionList.add("Africa");
		fixture.getRegionTransformation(regionList);
	}
	@Test
	public void testGetFilenameTransformation_1(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50245700");
		fixture.getFilenameTransformation(item, "c50245700.jpg");
	}
	@Test
	public void testGetFilenameTransformation_2(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50245700");
		fixture.getFilenameTransformation(item, "c50245700.jpg");
	}
	*//**
	 * TESTCASE 1 :Content Type-library 
	 * Document-c50245700
	 * renditions- null
	 *//*
	@Test
	public void testGetRenditionTransformation_1(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50245700");
		fixture.getRenditionTransformation(item, new BasicDBList());
	}
	*//**
	 * TESTCASE 2 : Content Type- MarketingStandard 
	 * document -c50326100
	 * 
	 *//*
	@Test
	public void testGetRenditionTransformation_2(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50326100");
		BasicDBList renditionList = new BasicDBList();
		BasicDBObject rendition = new BasicDBObject();
		DBObject output = fixture.getRenditionTransformation(item, renditionList);
		System.out.println("testGetRenditionTransformation_2()"+output);
	}
	*//**
	 * TESTCASE 3 :Marketing Standard Document
	 * Content Type- MarketingStandard 
	 * Document-c50326604
	 * Renditions-c50326604.pdf,c50326604.zip
	 *//*
	@Test
	public void testGetRenditionTransformation_3(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50326604");
		BasicDBList renditionsList =new BasicDBList();
		DBObject output = fixture.getRenditionTransformation(item, renditionsList);
		System.out.println("testGetRenditionTransformation_3()"+output);
	}
	*//**
	 * TESTCASE 1: ProductList is null
	 *//*
	@Test
	public void testGetProductTransformation_1(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		BasicDBList productsList =new BasicDBList();
		DBObject output = fixture.getProductTransformation(productsList);
		System.out.println("testGetProductTransformation_1()"+output);
	}
	*//**
	 * TESTCASE 2: ProductList has one product only
	 *//*
	@Test
	public void testGetProductTransformation_2(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		BasicDBList productsList =new BasicDBList();
		productsList.add("5034238");
		DBObject output = fixture.getProductTransformation(productsList);
		System.out.println("testGetProductTransformation_3() : "+output);
	}
	*//**
	 * TESTCASE 3: ProductList has more than one products
	 * Content Type- library, 
	 * Document-c50326107
	 *//*
	@Test
	public void testGetProductTransformation_3(){
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		BasicDBList productsList =new BasicDBList();
		productsList.add("5034238");
		productsList.add("3442753");
		productsList.add("5303451");
		productsList.add("5034231");
		DBObject output = fixture.getProductTransformation(productsList);
		System.out.println("testGetProductTransformation_3() : "+output);
	}
	
	*//**
	 * TESTCASE 1 :library Document
	 * Content Type- library, 
	 * Document-c50245700, 
	 * Products-296719, 
	 * file_name-c50245700.jpg
	 * region-Europe
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testTransformMetadata_1() throws AdapterException, MongoUtilsException, OptionsException{
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem item = new WorkItem();
		item.setContentType("library");
		item.setId("c50245700");
		DBObject output = fixture.transformMetadata(item);
		System.out.println("testTransformMetadata_1"+output);
	}
	*//**
	 * TESTCASE 2 : Marketing Standard Document
	 * Content Type- MarketingStandard, 
	 * Document-c50324782, 
	 * 
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testTransformMetadata_2() throws AdapterException, MongoUtilsException, OptionsException{
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50324782");
		DBObject output = fixture.transformMetadata(item);
		System.out.println("testTransformMetadata_2"+output);
	}
	*//**
	 * TESTCASE 3 :
	 * Content Type- MarketingStandard, 
	 * Document-c50324743
	 * 
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 *//*
	@Test
	public void testTransformMetadata_3() throws AdapterException, MongoUtilsException, OptionsException{
		MetadataTransformationAdapter fixture = new MetadataTransformationAdapter();
		ContentDAO contentDao = new ContentDAO();
		fixture.setContentDao(contentDao);
		WorkItem item = new WorkItem();
		item.setContentType("marketingstandard");
		item.setId("c50324743");
		DBObject output = fixture.transformMetadata(item);
		System.out.println("testTransformMetadata_3"+output);
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
		new org.junit.runner.JUnitCore().run(MetadataTransformationAdapterTest.class);
	}
}*/