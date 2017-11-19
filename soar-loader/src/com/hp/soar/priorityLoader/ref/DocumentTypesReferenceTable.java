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
public class DocumentTypesReferenceTable extends SimpleReferenceTable {

	/**
	 * @param e
	 * @param refFile
	 */
	public DocumentTypesReferenceTable(Element e, ReferenceFile refFile) {
		super(e, refFile);
	}

	/**
	 * @param e
	 * @param refFile
	 * @param referenceFiles
	 */
	public DocumentTypesReferenceTable(Element e, ReferenceFile refFile,
			ArrayList<ReferenceFile> referenceFiles) {
		super(e, refFile, referenceFiles);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.SimpleReferenceTable#addEntries()
	 */
	@Override
	protected void addEntries() {
		// Add only the entries that are active
		int count = result.getRowCount();
		try {
			HashMap<String, String> vals = new HashMap<String,String>();
			for(int i=0; i<count; i++) {
				getValues(vals, i);
				String isActive = vals.get("is_active");
				if (isActive.equals("0")) {
					continue;
				}
				for (RefElement element : elements) {
					element.addXML(vals, tableElement);
				}
			}
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException(tableName+":"+iae.getMessage(), iae);
		}
	}
	
}
