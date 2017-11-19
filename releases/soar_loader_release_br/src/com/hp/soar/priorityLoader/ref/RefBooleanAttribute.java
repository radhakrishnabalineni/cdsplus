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
public class RefBooleanAttribute extends RefAttribute {

	/**
	 * @param e
	 */
	public RefBooleanAttribute(Element e) {
		super(e);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefAttribute#addXML(java.util.HashMap, org.dom4j.Element)
	 */
	@Override
	public void addXML(HashMap<String, String> result, Element el) {
		String val = result.get(fieldValueName);
		if (val != null && val.equals("1")) {
			result.put(fieldValueName, "Yes");
		} else {
			result.put(fieldValueName, "No");
		}
		super.addXML(result, el);
	}
}
