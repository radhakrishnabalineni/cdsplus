package com.hp.cdsplus.mongo.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;

import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;


public class ConversionUtilsTest extends TestCase{
	
	ConversionUtils conversion;

	public ConversionUtilsTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		//super.setUp();
		conversion= new ConversionUtils();

	}

	protected void tearDown() throws Exception {
		//super.tearDown();
	}

	public void testJSONtoXML() throws ClassNotFoundException, JSONException, JAXBException, XMLStreamException, IOException, ConversionUtilsException{
		
		//File jsonFile = new File("C:/Suresh/workspace/mongo/Research/resources/c50326736-json.txt");
		File jsonFile = new File("C:/Suresh/workspace/mongo/Research/resources/c50326185 - Support.txt");
		//File jsonFile = new File("C:/Suresh/workspace/mongo/Research/resources/c50326181 - Marketing-Standard.txt");
        InputStream is= new FileInputStream(jsonFile);
        String jsonData = IOUtils.toString(is);
       //String result=conversion.convertJSONtoXML(jsonData, "com.hp.soar.bindings.input.schema.soar.SoarSoftwareFeed") ;
        String result=conversion.convertJSONtoXML(jsonData, "com.hp.concentra.bindings.input.schema.support.Document") ;
        //String result=conversion.convertJSONtoXML(jsonData, "com.hp.concentra.bindings.input.schema.marketingstandard.Document") ;
        System.out.println("result is --->"+result);

	}

	public void testXMLtoJSON() throws ClassNotFoundException, Exception, IOException{
		
		File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/c50326185 - Support.xml");
		//File xmlFile = new File("C:/Suresh/workspace/mongo/Research/resources/c50326181 - Marketing-Standard.xml");
        InputStream is= new FileInputStream(xmlFile);
        //String result= conversion.convertXMLtoJSON(IOUtils.toByteArray(is), "com.hp.concentra.bindings.input.schema.marketingstandard.Document");
        String result= conversion.convertXMLtoJSON(IOUtils.toByteArray(is), "com.hp.concentra.bindings.input.schema.support.Document");
        
        System.out.println("result is --->"+result);

	}

}
