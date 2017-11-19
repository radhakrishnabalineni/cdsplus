/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.dom4j.Element;

/**
 * @author dahlm
 *
 */
public class LanguageCountryReferenceTable extends SimpleReferenceTable {

	/**
	 * @param e
	 * @param refFile
	 */
	public LanguageCountryReferenceTable(Element e, ReferenceFile refFile) {
		super(e, refFile);
	}

	/**
	 * @param e
	 * @param refFile
	 * @param referenceFiles
	 */
	public LanguageCountryReferenceTable(Element e, ReferenceFile refFile,
			ArrayList<ReferenceFile> referenceFiles) {
		super(e, refFile, referenceFiles);
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.SimpleReferenceTable#addEntries()
	 */
	@Override
	protected void addEntries() {
		// Add only one entry per language_oid so keep track of the oids that have been done
		HashSet<String> languageOidsDone = new HashSet<String>();
		int count = result.getRowCount();
		try {
			HashMap<String, String> vals = new HashMap<String,String>();
			for(int i=0; i<count; i++) {
				getValues(vals, i);
				String language_oid = vals.get("language_oid");
				if (languageOidsDone.contains(language_oid)) {
					continue;
				} else {
					languageOidsDone.add(language_oid);
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
