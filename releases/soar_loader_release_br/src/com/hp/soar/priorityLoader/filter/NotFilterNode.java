package com.hp.soar.priorityLoader.filter;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;

public class NotFilterNode extends FilterNode {

	private FilterNode node;
	
	public NotFilterNode(Element e) {
		super();
		ArrayList<Element> elements = FilterNode.getElements(e);
		if (elements.size() != 1) {
			throw new IllegalArgumentException("Filter Parse Exception - not does not contain a single element");
		}
		node = FilterNode.getNode(elements.get(0));
	}

	/**
	 * return not of eval for the node
	 * @throws DfException 
	 */
	@Override
	public boolean eval(IDfDocument dbObj) throws DfException {
		return !node.eval(dbObj);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(" not [[");
		sb.append(node.toString()).append("]]");
		return sb.toString();
	}
	
	
}
