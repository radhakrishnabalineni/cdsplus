/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.Element;

import com.documentum.fc.common.DfException;

/**
 * @author dahlm
 *
 */
public class ReferenceList {
	public static final String LIST_NAME = "listName";
	public static final String VALUE_COLUMN_NAME = "valueColumnName";
	public static final String LABEL_COLUMN_NAME = "labelColumnName";

	private String listName;
	private String valueColumnName;
	private String labelColumnName;
	private HashMap<String, String> list;
	
	public ReferenceList(Element e) {
		listName = e.attributeValue(LIST_NAME);
		valueColumnName = e.attributeValue(VALUE_COLUMN_NAME);
		labelColumnName = e.attributeValue(LABEL_COLUMN_NAME);
		list = new HashMap<String, String>();
	}

	public void addEntry(Result result) throws DfException {
		ArrayList<String> valueColumn = result.getColumn(valueColumnName);
		ArrayList<String> labelColumn = result.getColumn(labelColumnName);
		
		if (valueColumn == null || labelColumn == null || valueColumn.size() != labelColumn.size()) {
			StringBuffer msg = new StringBuffer();
			msg.append("ReferenceList Error ").append(listName);
			msg.append(" value: ").append(valueColumnName).append((valueColumn != null) ? ":OK " : ":MISSING label: ");
			msg.append(labelColumnName).append((labelColumn != null) ? ":OK " : ":MISSING ");
			msg.append(((valueColumn != null) ? valueColumn.size() : "null")).append(" - ");
			msg.append(((labelColumn != null) ? labelColumn.size() : "null"));
			
			throw new DfException(msg.toString());
		}
		for(int i=0; i<valueColumn.size(); i++) { 
			Object val = labelColumn.get(i);
			if (!( val instanceof String)) {
				val = val.toString();
			}
			list.put(valueColumn.get(i), (String)val);
		}
	}
	
	/**
	 * @return the listName
	 */
	public String getListName() {
		return listName;
	}
	
	/**
	 * getList returns this list and prepares to create a new one
	 * @return
	 */
	public HashMap<String, String> getList() {
		HashMap<String, String> retList = list;
		list = new HashMap<String, String>();
		return retList;
	}
}
