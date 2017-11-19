package com.hp.soar.priorityLoader.filter;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;

public class SinglePropertyNode extends PropertyNode {
	
	public SinglePropertyNode(Element e) {
		super(e);
	}

	@Override
	public boolean eval(IDfDocument dbObj) throws DfException {
		String itemValue = dbObj.getString(fieldName);
		return fieldValue.equals(itemValue);
	}


}
