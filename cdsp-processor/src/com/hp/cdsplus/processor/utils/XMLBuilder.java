package com.hp.cdsplus.processor.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;

public class XMLBuilder {

	public static Element getDocumentElement(Document document) {
		Element root = document.addElement("document");
		root.addAttribute("xml:base", "http://cdsplus.houston.hp.com/cadence/app/");
		root.addNamespace("ens", "http://concentra.boi.itc.hp.com/extractor");
		root.addNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
		root.addNamespace("xlink", "http://www.w3.org/1999/xlink");
		root.addNamespace("xml", "http://www.w3.org/XML/1998/namespace");
		root.addNamespace("proj", "http://www.hp.com/cdsplus");
		return root;
	}

	/*public static String getXsdDateTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return df.format(new Date());
	}*/

	public static void addXMLAttribute(Element element, String attributeName, Object attributeValObj) {
		String attributeVal = "";
		if (attributeValObj != null) {
			attributeVal = ((String) attributeValObj).trim();
			if (attributeVal.toUpperCase().startsWith("NO VALUE")
					|| attributeVal.equals("")
					|| attributeVal.equals("nulldate")) {
				attributeVal = "";
			}
			element.addAttribute(attributeName, attributeVal);
		}

	}

	public static Element addXMLElementWithAttribute(Element parentElement,String eleName, String attributeName,Object attributeValObj) {
		Element element=parentElement.addElement(eleName);
		addXMLAttribute(element,attributeName, attributeValObj);
		return element;
	}

public static Element addXMLElement(Element parentElement, String eleName, Object eleVal) {
	String val = "";
	if (eleVal != null) {
		val = ((String) eleVal).trim();
		if (val.toUpperCase().startsWith("NO VALUE") || val.equals("")
				|| val.equals("nulldate")) {
			val = "";
		}
	}
	Element element = parentElement.addElement(eleName);
	return element.addText(val);
}

public static Element addXMLElement(Element parentElement, String eleName) {
	return parentElement.addElement(eleName);
}

public static void addNullbaleElement(Element parentElement, String elementName, String elementValue){
	Element chInEngElement = null;
	if (elementValue == null || elementValue.equals("")) {
		chInEngElement = addXMLElement(parentElement, elementName);	
	}else{
		chInEngElement = addXMLElement(parentElement, elementName, elementValue);
	}
	if (elementValue == null || elementValue.equals("")) {
		addXMLAttribute(chInEngElement, "nil", "true");
	} else {
		addXMLAttribute(chInEngElement, "nil", "false");
	}
}

public Element addElement(Element parentElement, String eleName, String eleVal) {
	return parentElement.addElement(eleName, eleVal);
}

public Element addElement(Element parentElement, String eleName) {
	return parentElement.addElement(eleName);
}

static public byte[] getBytes(Document document) throws MongoUtilsException {
	XMLWriter writer = null;
	ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048*1000);

	try {
		document.normalize();
		OutputFormat format = OutputFormat.createPrettyPrint();
		writer = new XMLWriter(outStream, format);
		writer.write(document);
	} catch (IOException ioe) {
		throw new MongoUtilsException("IOException happened-", ioe);
	} finally {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
		}
	}
	return outStream.toByteArray();
}
}

