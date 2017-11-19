package com.hp.soar.priorityLoader.filter;


import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;

public class RepeatingPropertyNode extends PropertyNode {

	public RepeatingPropertyNode(Element e) {
		super(e);
	}

	@Override
	public boolean eval(IDfDocument dbObj) throws DfException {
		int numStrings = dbObj.getValueCount(fieldName);
		for(int i=0; i<numStrings; i++) {
			String val = dbObj.getRepeatingString(fieldName, i);
			if (fieldValue.equals(val)) {
				return true;
			}
		}
		return false;
	}

}
