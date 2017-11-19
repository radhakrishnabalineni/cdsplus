/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

/**
 * @author dahlm
 *
 */
public class TableRef {
	private static final String TABLENAME = "tableName";
	private static final String PRIMARYKEY= "primaryKey";
	private static final String FOREIGNKEY= "foreignKey";
	public static final String FIELDAS = "fieldAs";
	public static final String FIELDNAME = "fieldName";
	public static final String ASVALUE = "asValue";
	public static final String REPORTMISSING = "reportMissing";
	
	private String tableName;
	private String primaryKey;
	private String foreignKey;
	
	// stores row indexes for primary key so don't need to do linear search
	private HashMap<String, Integer> primaryKeyIndex;
	private ArrayList<TableRef> joinTables;
	String[] cols = null;
	
	private boolean reportMissing = true;
	
	// names to remap column names to
	private HashMap<String, String> columnAs;
	// the backing data for this table reference
	Result tableResult;
	
	// the primary column 
	ArrayList<String> primaryCol;
	// the current entry in the primary column
	private Object primaryVal;
	// index of the next entry to return in an iterator
	private int nextRow;
	
	public TableRef(Element e) {
		tableName = e.attributeValue(TABLENAME);
		primaryKey = e.attributeValue(PRIMARYKEY);
		foreignKey = e.attributeValue(FOREIGNKEY);
		String tmp = e.attributeValue(REPORTMISSING);
		reportMissing = (tmp != null && "false".equalsIgnoreCase(tmp)) ? false : true;
		
		columnAs = new HashMap<String,String>();
		joinTables = new ArrayList<TableRef>();
		List<Element> fAs = e.elements(FIELDAS);
		for(Element fa : fAs) {
			columnAs.put(fa.attributeValue(FIELDNAME), fa.attributeValue(ASVALUE));
		}
	}
	
	public void addJoinTable(TableRef tr) {
		joinTables.add(tr);
	}
	
	/**
	 * getNextRow searches the primary column for the next entry that matches the key value
	 * @param keyVal
	 */
	private void getNextRow() {
		if (primaryVal == null) {
			nextRow = primaryCol.size();
			return;
		}
		while(++nextRow < primaryCol.size()) {
			if (primaryVal.equals(primaryCol.get(nextRow))) {
				break;
			}
		}
	}
	
	public void getIterator(HashMap<String, String>result) {
		ReferenceTable refTable = ReferenceLists.getRefTable(tableName);
		if (refTable == null) {
			throw new IllegalArgumentException("Missing Reference Table : "+tableName);
		}
		// get the results set from the table
		tableResult = refTable.getResult();
		// get the column of the primaryKey
		primaryCol = tableResult.getColumn(primaryKey);
		// get the value we are looking for out of result
		primaryVal = result.get(primaryKey);
		// get the columnNames for this table
		cols = tableResult.getColumnsName();
		// nextRow is incremented before getting so this allows you to get r[0]
		nextRow = -1;
		for(TableRef tab : joinTables) {
			tab.init();
		}
		getNextRow();
	}
	
	/**
	 * init initializes fields for a table lookup
	 */
	private void init() {
		ReferenceTable refTable = ReferenceLists.getRefTable(tableName);
		if (refTable == null) {
			throw new IllegalArgumentException("Missing Reference Table : "+tableName);
		}
		// get the results set from the table
		tableResult = refTable.getResult();
		// get the column of the primaryKey
		primaryCol = tableResult.getColumn(primaryKey);
		// get the columnNames for this table
		cols = tableResult.getColumnsName();
		// setup the index for lookup in this table
		primaryKeyIndex = new HashMap<String, Integer>();
		for(int i=0; i<primaryCol.size(); i++) {
			Object val = primaryCol.get(i);
			primaryKeyIndex.put(val.toString(), new Integer(i));
		}
	}
	
	public boolean hasNext() {
		return nextRow < primaryCol.size();
	}

	/**
	 * get used the values in the result hashmap to lookup values based on the primary key
	 * @param result
	 */
	private void get(HashMap<String, String> result) {
		// lookup the value of the foreign key and index to that row
		primaryVal = result.get(foreignKey);
		if (primaryVal == null) {
			throw new IllegalArgumentException("MISSING value for foreign key-"+foreignKey);
		}
		Integer index = primaryKeyIndex.get(primaryVal);
		if (index == null) {
			nextRow = -1;
		} else {
			nextRow = index.intValue();
		}
		addRow(result);
	}
	
	private void addRow(HashMap<String, String> result) {
		// put all of the values from  rowIdx into the hashMap
//		String[] cols = tableResult.getColumnsName();
		for(String col : cols) {
			Object value = null;
			if (nextRow != -1) {
				value = tableResult.getValue(nextRow, col);
			} else {
				if (reportMissing) {
					value = "MISSING for key: "+primaryKey+" value: "+primaryVal;
				}
			}
			if (columnAs.containsKey(col)) {
				if (value == null) {
					result.remove(columnAs.get(col));
				} else {
					result.put(columnAs.get(col), value.toString());
				}
			} else {
				if (value == null) {
					result.remove(col);
				} else {
					result.put(col, value.toString());
				}
			}
		}
	}
	
	/**
	 * Next uses rowIdx to determine where to get the values from
	 * @param result
	 */
	public void next(HashMap<String, String> result) {
		addRow(result);
		for(TableRef tab : joinTables) {
			tab.get(result);
		}
		getNextRow();
	}
}
