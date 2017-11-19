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
public class RefDateAttribute extends RefAttribute {
	private static final String WITHTIME = "withTime";
	
	private boolean withTime = false;
	
	/**
	 * @param e
	 */
	public RefDateAttribute(Element e) {
		super(e);
		String wt = e.attributeValue(WITHTIME);
		withTime = (wt != null && "yes".equalsIgnoreCase(wt));
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefAttribute#addXML(java.util.HashMap, org.dom4j.Element)
	 */
	@Override
	public void addXML(HashMap<String, String> result, Element el) {
		String val = result.get(fieldValueName);
		int idx = -1;
		if (!withTime && val != null && ((idx = val.indexOf("T")) != -1)) {
			val = val.substring(0, idx);
			// replace the value with just the date information
			result.put(fieldValueName, val);
		}
		super.addXML(result, el);
	}
}
