/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * @author dahlm
 *
 */
public class NonSaveReferenceTable extends ReferenceTable {
	private static final String DEPENDANTS = "dependants";
	
	/**
	 * @param e
	 * @param newParam TODO
	 */
	public NonSaveReferenceTable(Element e) {
		super(e);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.ReferenceTable#loadTable(com.documentum.fc.client.IDfSession, java.util.HashMap)
	 */
	@Override
	public void loadTable(IDfSession session,
			HashMap<String, HashMap<String, String>> refListMap,
			HashMap<String, ReferenceTable>refTableMap) throws DfException {
		super.loadTable(session, refListMap, refTableMap);
		if (dependants == null) {
			// throw away the result as we don't need it anymore
			result = null;
		}
	}
}
