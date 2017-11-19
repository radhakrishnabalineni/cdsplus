package com.hp.soar.priorityLoader.filter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.soar.priorityLoader.ref.ReferenceLists;

abstract public class PropertyNode extends FilterNode {
	private static final String NAME = "name";
	private static final String LISTNAME = "listName";
	
	protected String fieldName;
	protected String fieldValue;

	public PropertyNode(Element e) {
		super();
		fieldName = e.getAttribute(NAME);
		if (fieldName == null) {
			throw new IllegalArgumentException("Filter Parse Exception - No name attribute for property: ");
		}		
		
		NodeList nodeList = e.getElementsByTagName(VALUE);
		if (nodeList == null || nodeList.getLength() != 1) {
			throw new IllegalArgumentException("Filter Parse Exception - No value element for property: "+fieldName);
		}
		fieldValue = getFieldValue((Element)nodeList.item(0));
	}

	/**
	 * getFieldValue returns the value of this property for comparison
	 * @param e
	 * @return
	 */
	private String getFieldValue(Element e) {
		String retValue = null; 
		NodeList nodeList = e.getElementsByTagName(LOOKUP);
		if (nodeList == null || nodeList.getLength() != 1) {
			retValue = e.getTextContent();
		} else {
			Element lookupElem = (Element)nodeList.item(0);
			// lookup the key given the value from the reference list
			String listName = lookupElem.getAttribute(LISTNAME);
			String value = lookupElem.getAttribute(VALUE);
			if (listName == null || listName.length() == 0) {
				throw new IllegalArgumentException("listName attribute missing in lookup element");
			}
			if (value == null || value.length() == 0) {
				throw new IllegalArgumentException("listName attribute missing in lookup element");
			}
			retValue = ReferenceLists.getKey(listName, value);
			if (retValue == null) {
				throw new IllegalArgumentException("Filter Parse Exception - No key in reference list "+listName+" for value "+value);
			} 
			if (retValue.startsWith("MISSING")) {
				throw new IllegalArgumentException("Filter Parse Exception - No reference list "+listName);
			}
		}
		return retValue;
	}
	
	@Override
	public String toString() {
		return fieldName +"="+fieldValue;
	}
		
}
