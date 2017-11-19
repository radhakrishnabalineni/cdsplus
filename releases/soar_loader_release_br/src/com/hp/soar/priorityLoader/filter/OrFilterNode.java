package com.hp.soar.priorityLoader.filter;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;

public class OrFilterNode extends FilterNode {
	// list of nodes to be or'd together
	private ArrayList<FilterNode> orNodes;
	
	public OrFilterNode(Element e) {
		super();
		orNodes = new ArrayList<FilterNode>();
		ArrayList<Element> elements = FilterNode.getElements(e);
		for(Element elem : elements) {
			orNodes.add(FilterNode.getNode(elem));
		}
	}
	
	@Override
	public boolean eval(IDfDocument dbObj) throws DfException {
		for(FilterNode node : orNodes) {
			if (node.eval(dbObj)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(" or [");
		for(FilterNode node : orNodes) {
			sb.append("(").append(node.toString()).append(")");
		}
		sb.append("]");
		return sb.toString();
	}
}
