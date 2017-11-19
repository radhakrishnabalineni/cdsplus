/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;

/**
 * @author dahlm
 *
 */
public class RefAttribute extends RefField {

	/**
	 * @param e
	 */
	public RefAttribute(Element e) {
		super(e);
	}

	public void addXML(HashMap<String, String> result, Element el) {
		String val = result.get(fieldValueName);
		if (val == null) {
			el.addAttribute(name, "MISSING-fieldValue"+fieldValueName);
		} else {
			el.addAttribute(name, val.trim());
		}
	}
	
	static public RefAttribute getAttribute(Element e) {
		String type = e.attributeValue(TYPE);
		if (type == null) {
			return new RefAttribute(e);
		} else if (type.equals("boolean")) {
			return new RefBooleanAttribute(e);
		} else if (type.equals("nonNil")) {
			return new RefNonNilAttribute(e);
		} else if (type.equals("date")) {
			return new RefDateAttribute(e);
		}
		throw new IllegalArgumentException("Unknown Attribute type value: "+type);
	}
}
