package com.hp.soar.priorityLoader.helper;

/****************************************************************************************************************
 * @(#)Project: SOAR Project
 * @(#)Source: SoarExtractXMLBuilder
 * @(#)Revision: $
 * @(#)Date: Nov 14, 2008
 * @(#)Author: mariswam
 *
 * Copyright (c) 2006 Hewlet-Packard Company
 * All Rights Reserved
 *
 ****************************************************************************************************************/

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.Element;

import com.hp.soar.priorityLoader.workItem.SoarExtractElement;
import com.hp.soar.priorityLoader.workItem.SoarWorkItem;

public class SoarExtractXMLBuilder {

	public static final String NAME_SPACE_PREFIX = "xsi";
	
	public static Element getSwFeedElement(Document document, String schemaFile) {
    Element root = document.addElement("soar-software-feed");
    root.addAttribute("date", getXsdDateTime());
    root.addNamespace("xsi", SoarExtractElement.dns_name);
    root.addAttribute("xsi:noNamespaceSchemaLocation", schemaFile);
    return root;
	}

  public static String getXsdDateTime() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    return df.format(new Date());
  }

	public static void addXMLAttribute(Element element, String attributeName,
			Object attributeValObj, boolean withNS) {
		String attributeVal = "";
		if (attributeValObj != null) {
			attributeVal = ((String) attributeValObj).trim();
			if (attributeVal.toUpperCase().startsWith("NO VALUE")
					|| attributeVal.equals("")
					|| attributeVal.equals("nulldate")) {
				attributeVal = "";
			}
			if (withNS) {
				element.addAttribute(NAME_SPACE_PREFIX + ":" + attributeName, attributeVal);
			} else {
				element.addAttribute(attributeName, attributeVal);
			}
		}

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
			addXMLAttribute(chInEngElement, "nil", "true", true);
		} else {
			addXMLAttribute(chInEngElement, "nil", "false", true);
		}
	}

	public Element addElement(Element parentElement, String eleName, String eleVal) {
		return parentElement.addElement(eleName, eleVal);
	}

	public Element addElement(Element parentElement, String eleName) {
		return parentElement.addElement(eleName);
	}
}
