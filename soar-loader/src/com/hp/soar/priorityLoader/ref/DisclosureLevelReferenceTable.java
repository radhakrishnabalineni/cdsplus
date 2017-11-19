/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.Element;

/**
 * @author dahlm
 *
 */
public class DisclosureLevelReferenceTable extends SimpleReferenceTable {

	/**
	 * @param e
	 * @param refFile TODO
	 * @param referenceFiles TODO
	 */
	public DisclosureLevelReferenceTable(Element e, ReferenceFile refFile, ArrayList<ReferenceFile> referenceFiles) {
		super(e, refFile, referenceFiles);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.SimpleReferenceTable#addEntries()
	 */
	@Override
	protected void addEntries() {
		int count = result.getRowCount();
		HashMap<String, String> vals = new HashMap<String,String>();
		for(int i=0; i<count; i++) {
			getValues(vals, i);
			if (shouldAdd(vals)) {
				for (RefElement element : elements) {
					element.addXML(vals, tableElement);
				}
			}
		}
	}
	
	private boolean shouldAdd(HashMap<String, String>vals) {
		String usage = vals.get("usage");
		return (usage.indexOf("SOAR") != -1);
	}
}
