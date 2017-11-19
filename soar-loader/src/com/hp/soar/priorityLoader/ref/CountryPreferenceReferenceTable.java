/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

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
public class CountryPreferenceReferenceTable extends SimpleReferenceTable {

	/**
	 * @param e
	 */
	public CountryPreferenceReferenceTable(Element e, ReferenceFile f) {
		super(e, f);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.ReferenceTable#getLoadDQL()
	 */
	@Override
	protected String getLoadDQL() {
		StringBuffer dql = new StringBuffer();
		dql.append("select * from dm_dbo.").append(dbTableName).append(" where wds_release like '%ftp%ap%' order by ").append(sortColumnName);
		return dql.toString();
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
}
