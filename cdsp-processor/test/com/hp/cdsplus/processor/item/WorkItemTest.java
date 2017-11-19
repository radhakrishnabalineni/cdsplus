/*package com.hp.cdsplus.processor.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

*//**
 * The class <code>WorkItemTest</code> contains tests for the class <code>{@link WorkItem}</code>.
 *
 * @generatedBy CodePro at 8/9/13 9:54 AM
 * @author kashyaks
 * @version $Revision: 1.0 $
 *//*
public class WorkItemTest {
	*//**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	public WorkItemTest() {
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_1()
		throws Exception {
		String contentType = "library";
		DBObject item = new BasicDBObject();
		item.put("_id", "");
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}

		item.put("_id",null);
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_2()
		throws Exception {
		String contentType = "library";
		String _id = "c0123456";
		DBObject item = new BasicDBObject();
		item.put("_id",_id);
		item.put("eventType", "");
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(e instanceof ProcessException);
		}

		item.put("eventType", null);
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_3()
		throws Exception {
		String contentType = "library";
		String _id = "c0123456";
		String eventType = "update";
		DBObject item = new BasicDBObject();
		item.put("_id",_id);
		item.put("eventType", eventType);
		item.put("priority", "");
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}

		item.put("priority", null);
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_4()
		throws Exception {
		String contentType = "library";
		String _id = "c0123456";
		String eventType = "update";
		int priority = 1;
		DBObject item = new BasicDBObject();
		item.put("_id",_id);
		item.put("eventType", eventType);
		item.put("priority", priority);
		item.put("lastModified", "");
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}

		item.put("priority", null);
		
		try{
			new WorkItem(contentType, item);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_5()
		throws Exception {
		String contentType = "library";
		String _id = "c0123456";
		String eventType = "update";
		int priority = 1;
		long lastModified = 0L;
		DBObject dbo = new BasicDBObject();
		dbo.put("_id",_id);
		dbo.put("eventType", eventType);
		dbo.put("priority", priority);
		dbo.put("lastModified", lastModified);
		Item wItem = new WorkItem(contentType,dbo);
		assertNotNull(wItem);
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_6()
		throws Exception {
		String contentType = "";
		String _id = "c0123456";
		String eventType = "update";
		int priority = 1;
		long lastModified = 0L;
		DBObject dbo = new BasicDBObject();
		dbo.put("_id",_id);
		dbo.put("eventType", eventType);
		dbo.put("priority", priority);
		dbo.put("lastModified", lastModified);
		
		try{
			new WorkItem(contentType,dbo);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		contentType = null;
		
		try{
			new WorkItem(contentType,dbo);
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the WorkItem(String,DBObject) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testWorkItem_7()
		throws Exception {
		String contentType = "";
		DBObject item = EasyMock.createMock(DBObject.class);
		// add mock object expectations here

		EasyMock.replay(item);

		WorkItem result = new WorkItem(contentType, item);

		// add additional test code here
		EasyMock.verify(item);
		assertNotNull(result);
	}

	*//**
	 * Run the DBObject getDocument() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testGetDocument_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("update");
		fixture.setId("c0123456");
		fixture.setProcessed(true);

		DBObject result = fixture.getDocument();
		assertNotNull(result);
		
	}

	*//**
	 * Run the String getEventType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testGetEventType_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("update");
		fixture.setId("c0123456");
		fixture.setProcessed(true);

		String result = fixture.getEventType();
		assertEquals("update", result);
	}

	*//**
	 * Run the String getId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testGetId_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("");
		fixture.setId("c0123456");
		fixture.setProcessed(true);

		String result = fixture.getId();

		// add additional test code here
		assertEquals("c0123456", result);
	}

	*//**
	 * Run the Long getLastModified() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testGetLastModified_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("");
		fixture.setId("c0123456");
		fixture.setProcessed(true);

		Long result = fixture.getLastModified();

		// add additional test code here
		assertNotNull(result);
		assertEquals(new Long(1L), result);

	}

	*//**
	 * Run the long getPriority() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testGetPriority_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("");
		fixture.setId("");
		fixture.setProcessed(true);

		long result = fixture.getPriority();

		// add additional test code here
		assertEquals(1, result);
	}

	*//**
	 * Run the boolean isProcessed() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testIsProcessed_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("");
		fixture.setId("");
		fixture.setProcessed(true);

		assertTrue(fixture.isProcessed());
	}

	*//**
	 * Run the boolean isProcessed() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testIsProcessed_2()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setPriority(1);
		fixture.setDocument(EasyMock.createNiceMock(DBObject.class));
		fixture.setLastModified(new Long(1L));
		fixture.setEventType("");
		fixture.setId("");
		fixture.setProcessed(false);

		assertFalse(fixture.isProcessed());
	}

	*//**
	 * Run the void load() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testLoad_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setContentType("library");
		fixture.setId("c50322566");

		fixture.load();
		assertNotNull(fixture.getDocument());
		assertEquals("processing", fixture.getDocument().get("status"));
		
		ContentDAO cDao = new ContentDAO();
		Options options = new Options();
		options.setContentType("library");
		options.setDocid("c50322566");
		DBObject result = cDao.getTempMetadata(options);
		System.out.println(result);
		assertNotNull(result);
		assertEquals(result, fixture.getDocument());
	}

	*//**
	 * Run the void load() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testLoad_2()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setId("");
		fixture.setContentType("library");
		fixture.load();
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		fixture.setId(null);
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}	
		fixture.setId("invalid");
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}

	*//**
	 * Run the void load() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testLoad_3()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setId("c50322566");
		fixture.setContentType("");
		fixture.load();
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
		fixture.setContentType(null);
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}	
		fixture.setContentType("invalid");
		try{
			fixture.load();
		}catch(Exception e){
			assertTrue(e instanceof ProcessException);
		}
	}


	*//**
	 * Run the void service() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testService_1()
		throws Exception {
		WorkItem fixture = new WorkItem();fixture.setContentType("library");
		fixture.setId("c50322566");

		fixture.load();
		assertNotNull(fixture.getDocument());
		assertEquals("In process", fixture.getDocument().get("status"));
		
		fixture.service();
		// at this point we dont know what needs to be checked so leaving it at that without any check

	}
	*//**
	 * Run the void service() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSave_1()
		throws Exception {
		WorkItem fixture = new WorkItem();fixture.setContentType("library");
		fixture.setId("c50322566");

		fixture.save();
		//assertNotNull(fixture.getDocument());
		//assertEquals("processed", fixture.getDocument().get("status"));
		
		Options options = new Options();
		options.setContentType("library");
		options.setDocid("c50322566");
		ContentDAO dao = new ContentDAO();
		DBObject result = dao.getTempMetadata(options);
		
		assertNotNull(result);
		assertEquals("processed",result.get("status"));
	}

	*//**
	 * Run the void setDocument(DBObject) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetDocument_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		fixture.setDocument(new BasicDBObject("_id", "c0123456"));
		assertNotNull(fixture.getDocument());
		assertTrue(fixture.getDocument().get("_id").toString().equalsIgnoreCase("c0123456"));
	}

	*//**
	 * Run the void setEventType(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetEventType_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		String eventType = "update";
		fixture.setEventType(eventType);
		assertNotNull(fixture.getEventType());
		assertEquals(eventType, fixture.getEventType());
	}

	*//**
	 * Run the void setId(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetId_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		String id = "c1234567";

		fixture.setId(id);

		assertEquals(id, fixture.getId());
	}

	*//**
	 * Run the void setLastModified(Long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetLastModified_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		Long lastModified = new Long(1L);

		fixture.setLastModified(lastModified);

		assertEquals(lastModified, fixture.getLastModified());
	}

	*//**
	 * Run the void setPriority(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetPriority_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		int priority = 1;
		fixture.setPriority(priority);
		assertEquals(priority, fixture.getPriority());
	}

	*//**
	 * Run the void setProcessed(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	@Test
	public void testSetProcessed_1()
		throws Exception {
		WorkItem fixture = new WorkItem();
		boolean isProcessed = true;

		fixture.setProcessed(isProcessed);

		assertTrue(fixture.isProcessed());
		
	}

	*//**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/9/13 9:54 AM
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
	 * @generatedBy CodePro at 8/9/13 9:54 AM
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
	 * @generatedBy CodePro at 8/9/13 9:54 AM
	 *//*
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(WorkItemTest.class);
	}
}*/