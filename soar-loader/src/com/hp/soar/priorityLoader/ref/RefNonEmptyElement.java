/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 * @author dahlm
 * RefNonEmptyElement only puts in an element if it has a value.  empty element space holders are not put in.
 */
public class RefNonEmptyElement extends RefElement {

	/**
	 * @param e
	 */
	public RefNonEmptyElement(Element e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefElement#getFieldValue(java.util.HashMap)
	 */
	@Override
	protected String getFieldValue(HashMap<String, String> result) {
		if (fieldValueName == null) {
			return null;
		}
		return result.get(fieldValueName);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefElement#setupTable(java.util.HashMap)
	 */
	@Override
	protected boolean setupTable(HashMap<String, String> result) {
		super.setupTable(result);
		// setup returns false if there is no elements in the table to iterator over 
		return table == null || table.hasNext();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefElement#addElement(boolean, org.dom4j.Element, org.dom4j.Element)
	 */
	@Override
	protected void addElement(Element elem, Element parent) {
		// only add this element if it has something in it
		if ((table != null && elem.elements().size()>0) || 
				(table == null && (elem.elements().size() > 0 || elem.attributes().size() > 0))) {
			super.addElement(elem, parent);
		}
	}
}
