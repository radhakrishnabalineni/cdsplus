/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.hp.loader.priorityLoader.ProcessingException;

/**
 * @author dahlm
 *
 */
public class ProductEnvironmentReferenceTable extends SimpleReferenceTable {

	private static final String GROUPID = "group_id";
	private static final String PRODUCT = "product";
	private static final String TYPE = "type";
	
	/**
	 * @param e
	 * @param refFile
	 * @param referenceFiles
	 */
	public ProductEnvironmentReferenceTable(Element e, ReferenceFile refFile,
			ArrayList<ReferenceFile> referenceFiles) {
		super(e, refFile, referenceFiles);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.ReferenceTable#createElement(java.util.HashMap)
	 */
	@Override
	public void createElement(HashMap<String, Date> updateDateMap)
			throws ProcessingException {
		// This table doesn't have a "super" element just the individual ones
		tableElement = new BaseElement(tableElemName);
		addEntries();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.SimpleReferenceTable#addTable(org.dom4j.Element, java.util.HashMap)
	 */
	@Override
	public void addTable(Element e, HashMap<String, Date> updateDateMap)
			throws ProcessingException {
		// as this unhooks everything from the tableElement on storage, the second time around
		// it will have to be recreated.
		if (tableElement == null) {
			createElement(updateDateMap);
		}
		
		// need to add the subelements to e.  This table doesn't have an enclosing element
		List<Element> els = tableElement.elements();
		for(Element el : els) {
			e.add(el.detach());
		}
		
		// force this element to be recreated as we just detached everything from under it
		tableElement = null;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.ReferenceTable#getValues(java.util.HashMap, int)
	 */
	@Override
	protected void getValues(HashMap<String, String> results, int rowIdx) {
		// TODO Auto-generated method stub
		super.getValues(results, rowIdx);
		// This is special code to split the group_id column into product and type fields
		String groupId = results.get(GROUPID);
		if (groupId == null) {
			results.put(PRODUCT, "MISSING-group_id");
			results.put(TYPE, "MISSING-group_id");
		}
		int idx = groupId.indexOf("_");
		if (idx == -1) {
			results.put(PRODUCT, "MISSING-badformat-"+groupId);
			results.put(TYPE, "MISSING-badformat-"+groupId);
		} else {
			results.put(PRODUCT, groupId.substring(idx+1));
			results.put(TYPE, groupId.substring(0, idx));
		}
	}
}
