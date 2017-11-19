/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.dom4j.tree.FlyweightAttribute;

/**
 * @author dahlm
 *
 *  This class handles an element that is iterated over for creating children from a lookup
 */
public class RefNonStoredElement extends RefElement {

	/**
	 * @param e
	 */
	public RefNonStoredElement(Element e) {
		super(e);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefElement#addXML(java.util.HashMap, org.dom4j.Element)
	 */
	@Override
	public void addXML(HashMap<String, String> result, Element el) {
		if (table != null) {
			table.getIterator(result);

			while(table.hasNext()) {
				table.next(result);
				addXMLRow(result, el);
			}
		} else {
			addXMLRow(result, el);
		}
	}
}
