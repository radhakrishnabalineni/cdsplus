/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 * @author dahlm
 *
 */
public class RefElement extends RefField {

	// attributes for this element
	ArrayList<RefAttribute> attrs;
	ArrayList<RefElement>   elements;
	
	TableRef table;
	boolean hasData = true;
	
	/**
	 * @param labelName
	 * @param fieldValueName
	 * @param attrs
	 */
	public RefElement(Element e) {
		super(e);
		attrs = new ArrayList<RefAttribute>();
		elements = new ArrayList<RefElement>();
		
		List<Element> trEls = e.elements(TABLEREF);
		for(Element tr : trEls) {
			TableRef ref = new TableRef(tr);
			if (table == null) {
				table = ref;
			} else {
				table.addJoinTable(ref);
			}
		}
		
		List<Element> elAttrs = e.elements(ATTRIBUTE);
		for ( Element el : elAttrs ) {
			attrs.add(RefAttribute.getAttribute(el));
		}
		
		List<Element> els = e.elements(ELEMENT);
		for ( Element el : els ) {
			elements.add(RefElement.getElement(el));
		}
	}

	protected void addElementValue(String fieldValue, Element e) {
		if (fieldValue != null) {
			e.addText(fieldValue);
		}
	}
	
	protected void addXMLRow(HashMap<String, String>result, Element el) {
		for(RefElement subEl : elements) {
			subEl.addXML(result, el);
		}
	}
	
	protected void addRowElem(boolean shouldAdd, Element rowEl, Element parent) {
		parent.add(rowEl);
	}

	/**
	 * Add the element to the parent
	 * @param elem
	 * @param parent
	 * @return
	 */
	protected void addElement(Element elem, Element parent) {
		parent.add(elem);
	}
	
	/**
	 * create and populate the element
	 * @param result
	 * @return
	 */
	protected Element createElement(HashMap<String, String>result) {
		Element rowElem = new BaseElement(name);
		String fieldValue = getFieldValue(result);
		if (fieldValue != null) {
			addElementValue(fieldValue, rowElem);
		}
		
		for(RefAttribute attr : attrs) {
			attr.addXML(result, rowElem);
		}

		return rowElem;
	}
	
	protected String getFieldValue(HashMap<String, String>result) {
		if (fieldValueName == null) {
			return null;
		}
		String fieldValue = result.get(fieldValueName);
		if (fieldValue == null) {
			return("MISSING-fieldValue -- "+fieldValueName);
		} else {
			return(fieldValue.trim());
		}
	}
	
	/**
	 * setupTable gets the iterator for the primary table created if it needs to be
	 * @param result
	 * @return
	 */
	protected boolean setupTable(HashMap<String, String>result) {
		if (table != null) {
			table.getIterator(result);
		}
		return true;
	}
	
	public void addXML(HashMap<String, String> result, Element el) {
		// setup the table if there is one
		if (setupTable(result) ) {
			// we will have at least one element or we are supposed to create an empty element 
			Element rowElem = createElement(result);
			if (table != null) {
				while(table.hasNext()) {
					table.next(result);
					addXMLRow(result, rowElem);
				}
			} else {
				addXMLRow(result, rowElem);
			}
			addElement(rowElem, el);
		}
	}
	
	/**
	 * RefElement should always add 
	 * @param table
	 * @return
	 */
	protected boolean getShouldAdd(TableRef table) {
		return true;
	}
	
	static public RefElement getElement(Element e) {
		String type = e.attributeValue(TYPE);
		if (type == null) {
			return new RefElement(e);
		} else if (type.equals("nonStored")) {
			return new RefNonStoredElement(e);
		} else if (type.equals("nonEmpty")) {
			return new RefNonEmptyElement(e);
		} else if (type.equals("nilEmpty")) {
			return new RefNilEmptyElement(e);
		}
		throw new IllegalArgumentException("Unknown Element type value: "+type);
	}
}
