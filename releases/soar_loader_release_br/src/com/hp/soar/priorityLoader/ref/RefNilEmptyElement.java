/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 * @author dahlm
 *
 */
public class RefNilEmptyElement extends RefElement {

	/**
	 * @param e
	 */
	public RefNilEmptyElement(Element e) {
		super(e);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefElement#createElement(java.util.HashMap)
	 */
	@Override
	protected Element createElement(HashMap<String, String> result) {
		Element rowElem = new BaseElement(name);
		if (fieldValueName != null) {
			String fieldValue = result.get(fieldValueName);
			if (fieldValue == null || "".equals(fieldValue)) {
				rowElem.addAttribute("xsi:nil", "true");
			} else {
				addElementValue(fieldValue.trim(), rowElem);
			}
		}
		
		for(RefAttribute attr : attrs) {
			attr.addXML(result, rowElem);
		}

		return rowElem;		

	}
}
