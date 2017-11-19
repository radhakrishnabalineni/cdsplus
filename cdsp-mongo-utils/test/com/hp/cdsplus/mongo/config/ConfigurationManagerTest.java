/*package com.hp.cdsplus.mongo.config;

import java.util.HashMap;

import junit.framework.TestCase;

import com.mongodb.DB;
import com.mongodb.MongoClient;


public class ConfigurationManagerTest extends TestCase {
	ConfigurationManager configMgr;
	
	public ConfigurationManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		configMgr= ConfigurationManager.getInstance();
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMongoConnectVersionCheck(){
		System.out.println("entering testMongoConnectVersionCheck");
		MongoClient client=configMgr.getMongoConnection();
		String version= client.getVersion();
		if(version.equals("2.10.1")){
			assertTrue(true);
		}else assertTrue(false);
		System.out.println("exiting testMongoConnectVersionCheck");
	}
	
	public void testMongoConnectAddressCheck(){
		System.out.println("entering testMongoConnectAddressCheck");
		MongoClient client=configMgr.getMongoConnection();
		if(client.getAddress().toString().contains("g2t1888c.austin.hp.com")){
			assertTrue(true);
		}else assertTrue(false);
		
		System.out.println("exiting testMongoConnectAddressCheck");
	}
	
	
	public void testMongoDBAuthenticated(){
		System.out.println("entering testMongoDBAuthenticated");
		DB dbObj=configMgr.getMongoDBAuthenticated("support");
		if(dbObj!=null){
			if(dbObj.getCollectionNames().contains("metadata")){
				assertTrue(true);
			}else assertTrue(false);
		}else assertTrue(false);
		System.out.println("exiting testMongoDBAuthenticated");
	}
	
	public void testMongoDBAuthenticatedWithRightPWD(){
		System.out.println("entering testMongoDBAuthenticatedWithRightPWD");
		DB dbObj=configMgr.getMongoDBAuthenticated("library","cdspdb","cdspdb");
		if(dbObj!=null){
			if(dbObj.getCollectionNames().contains("metadata")){
				assertTrue(true);
			}else assertTrue(false);
		}else assertTrue(false);
		
		System.out.println("exiting testMongoDBAuthenticatedWithRightPWD");
	}
	
	public void testMongoDBAuthenticatedWithRightPWD2(){
		System.out.println("entering testMongoDBAuthenticatedWithRightPWD2");
		DB dbObj=configMgr.getMongoDBAuthenticated("marketinghho","cdspdb","cdspdb");
		if(dbObj!=null){
			System.out.println(dbObj.getCollectionNames());
			if(dbObj.getCollectionNames().contains("metadata")){
				assertTrue(true);
			}else assertTrue(false);
		}else assertTrue(false);
		
		System.out.println("exiting testMongoDBAuthenticatedWithRightPWD2");
	}
	
	public void testMongoDBAuthenticatedWithWrongPWD(){
		System.out.println("entering testMongoDBAuthenticatedWithWrongPWD");
		DB dbObj=configMgr.getMongoDBAuthenticated("manual","zunk","zunk");
		if(dbObj!=null){
			if(dbObj.getCollectionNames().contains("metadata")){
				assertTrue(true);
			}else assertTrue(false);
		}else assertTrue(true);
		System.out.println("exiting testMongoDBAuthenticatedWithWrongPWD");
	}
	
	
	public void testJaxbMapperSize(){
		System.out.println("entering testJaxbMapperSize");
		HashMap<String, ConfigMapper> jaxbClassMapper=configMgr.getConfigMapper();
		if(jaxbClassMapper.size()==33){
			assertTrue(true);
		}else assertTrue(false);
		
		System.out.println("exiting testJaxbMapperSize");
	}
	
	public void testJaxbMapperValues(){
		System.out.println("entering testJaxbMapperValues");
		HashMap<String, ConfigMapper> jaxbClassMapper=configMgr.getConfigMapper();
		if(jaxbClassMapper.get("manual").getClassName().equals("Document")){
			assertTrue(true);
		}else assertTrue(false);
		
		System.out.println("exiting testJaxbMapperValues");
	}
	
	

}
*/