package com.hp.soar.priorityLoader.filter;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;

public class AndFilterNode extends FilterNode {

	// list of nodes to be anded together
	private ArrayList<FilterNode> andNodes;
	
	public AndFilterNode(Element e) {
		super();
		andNodes = new ArrayList<FilterNode>();
		ArrayList<Element> elements = FilterNode.getElements(e);
		for(Element elem : elements){
			andNodes.add(FilterNode.getNode(elem));
		}
	}

	public AndFilterNode(ArrayList<Element> list) {
		super();
		for(Element e : list) {
			andNodes.add(FilterNode.getNode(e));
		}
	}
	
	/**
	 * eval evaluates logical and of the nodes in the list
	 * @throws DfException 
	 */
	@Override
	public boolean eval(IDfDocument dbObj) throws DfException {
		for(FilterNode node : andNodes) {
			if (!node.eval(dbObj)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(" and {");
		for(FilterNode node : andNodes) {
			sb.append("(").append(node.toString()).append(")");
		}
		sb.append("}");
		return sb.toString();
	}
	
}
