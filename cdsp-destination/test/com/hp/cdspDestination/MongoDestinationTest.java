/*package com.hp.cdspDestination;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;


public class MongoDestinationTest extends TestCase {
	
	MongoDestination mongoDest;
	MongoAPIUtils mongoUtils;
	ConversionUtils conversion;
	ConfigurationManager configMgr;

	public MongoDestinationTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mongoDest = new MongoDestination();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testProcessUpdateMetaConcentra() {
		System.out.println("entering testProcessUpdateMetaConcentra");
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/ms.xml");
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processUpdate("marketingstandard/loader/c245", "text/xml", IOUtils.toByteArray(is), "update", 2, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("exiting testProcessUpdateMetaConcentra");
	}
	
	public void testProcessUpdateContentConcentra() {
		System.out.println("entering testProcessUpdateContentConcentra");
		
		try {
			mongoDest.processUpdate("library/loader/c345/logo.gif", "gif", "C:/Suresh/workspace/mongo/Research/resources/logo.gif", "update", 3, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("exiting testProcessUpdateContentConcentra");
	}
	
	*//**
	 * this test case to test the xml content which is sent as payload for content classes like support content, but need to save this xml file as content in mongodb.
	 *//*
	public void testProcessUpdateMetaContent2Concentra() {
		System.out.println("entering testProcessUpdateMetaContent2Concentra");
		
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/sc.xml");
		
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processUpdate("librarycontent/loader/c246", "text/xml", IOUtils.toByteArray(is), "update", 1, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("exiting testProcessUpdateMetaContent2Concentra");
	}

	public void testProcessDeleteMetaConcentra() {
		System.out.println("entering testProcessDeleteMetaConcentra");
		
		try {
			mongoDest.processDelete("marketingstandard/loader/c245", "delete", 1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("exiting testProcessDeleteMetaConcentra");
	}

	public void testProcessTouchConcentra() {
		System.out.println("entering testProcessTouchConcentra");
		
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/ms.xml");
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processTouch("marketingstandard/loader/c245", "text/xml", IOUtils.toByteArray(is), "update", 2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//mongoDest.processTouch(String path, String mimeType, byte[] payload, String updateType, Integer priority)

		System.out.println("exiting testProcessTouchConcentra");
	}
	
	public void testProcessUpdateMetaSoar() {
		System.out.println("entering testProcessUpdateMetaSoar");
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/col33544.xml");
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processUpdate("soar/loader/c245", "text/xml", IOUtils.toByteArray(is), "update", 2, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("exiting testProcessUpdateMetaSoar");
	}
	
	
	*//**
	 * this test case to test the xml content which is sent as payload for content classes like support content, but need to save this xml file as content in mongodb.
	 *//*
	public void testProcessUpdateMetaContent2Soar() {
		System.out.println("entering testProcessUpdateMetaContent2Soar");
		
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/sc.xml");
		
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processUpdate("soar_ref_data/loader/c246", "text/xml", IOUtils.toByteArray(is), "update", 1, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("exiting testProcessUpdateMetaContent2Soar");
	}

	public void testProcessDeleteMetaSoar() {
		System.out.println("entering testProcessDeleteMetaSoar");
		
		try {
			mongoDest.processDelete("soar/loader/c245", "delete", 1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("exiting testProcessDeleteMetaSoar");
	}

	public void testProcessTouchSoar() {
		System.out.println("entering testProcessTouchSoar");
		
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/col33544.xml");
		try {
			InputStream is= new FileInputStream(xmlFile);
			mongoDest.processTouch("soar/loader/c245", "text/xml", IOUtils.toByteArray(is), "update", 2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//mongoDest.processTouch(String path, String mimeType, byte[] payload, String updateType, Integer priority)

		System.out.println("exiting testProcessTouchSoar");
	}

}

*/