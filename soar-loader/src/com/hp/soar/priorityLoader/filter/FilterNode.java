package com.hp.soar.priorityLoader.filter;


import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;


abstract public class FilterNode {
	public static final String FILTER = "filter";
	public static final String COLLECTION = "collection";
	public static final String ITEM = "item";
	
	protected static final String PROPERTY = "property";
	protected static final String PROPERTYTYPE = "propertyType";
	protected static final String REPEATING = "Repeating";
	protected static final String VALUE = "value";
	protected static final String LOOKUP = "lookup";
	protected static final String OR = "or";
	protected static final String AND = "and";
	protected static final String NOT = "not";

	public FilterNode() {
		super();
	}

	abstract public boolean eval(IDfDocument dbObj) throws DfException;
	
	/*
	 * getElements returns the elements under a node in the document.
	 */
	public static ArrayList<Element> getElements(Node n) {
		ArrayList<Element> elems = new ArrayList<Element>();
		NodeList nodes = n.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				elems.add((Element)nodes.item(i));
			}
		}
		return elems;
	}
	
	public static FilterNode getNode(Element e) {
		String name = e.getNodeName();
		if (name.equalsIgnoreCase(AND)) {
			return new AndFilterNode(e);
		} else if (name.equalsIgnoreCase(OR)) {
			return new OrFilterNode(e);
		} else if (name.equalsIgnoreCase(PROPERTY)) {
			String attrType = e.getAttribute(PROPERTYTYPE);
			if (attrType != null && attrType.equalsIgnoreCase(REPEATING)) {
				return new RepeatingPropertyNode(e);
			} else {
				return new SinglePropertyNode(e);
			}
		} else if (name.equalsIgnoreCase(NOT)) {
			return new NotFilterNode(e);
		}
		throw new IllegalArgumentException("Filter Parse Exception unknown element type: "+name);
	}
}
