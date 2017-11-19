/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;

/**
 * @author dahlm
 *
 */
public abstract class ReferenceTable {
	// the two values in an element or attribute set.  labelName for the xml and fieldName for getting the value of the label
	public static final int LABELNAME = 0;
	public static final int FIELDFORLABELNAME = 1;
	
	// constants for loading tables
	public static final String TABLE_NAME = "tableName";
	public static final String REF_LIST = "refList";
	public static final String TABLE_ELEM_NAME = "tableElemName";
	public static final String TABLES = "tables";	
	public static final String WHERE = "where";
	
	public static final String SORT_COLUMN_NAME = "sortColumnName";
	public static final String DEPENDANTS = "dependants";
	
	// static fields reused for creating a formatted date string
	private static final StringBuffer dateBuffer = new StringBuffer();
	private static final GregorianCalendar formatDate = new GregorianCalendar();

	// Elment name of the reference table
	protected String tableElemName;
	
	// Name assigned to this table It can have an extension <databaseTableName>.<ext> to make two reference unique
	protected String tableName;

	// Documentum table name 
	protected String dbTableName;

	protected String whereClause = null;
	
	// columnAs used to remap column names
	private HashMap<String, String> columnAs;
	
	// other tables this table depends on
	protected String[] dependants = null;
	
	// Description of reference lists this table supports
	protected ArrayList<ReferenceList> refLists;
	
	protected String sortColumnName;

	// element to hold the XML for this table
	protected Element tableElement;
	
	// Values for this table, it is populated if it had dependent tables
	protected Result result;
	// cols in the result;
	String[] cols = null;

	// Files that use this table
	ArrayList<ReferenceFile> refFiles;
		
	public ReferenceTable(Element e) {
		tableName = e.attributeValue(TABLE_NAME);
		tableElemName = e.attributeValue(TABLE_ELEM_NAME);
		whereClause = e.attributeValue(WHERE);
		
		int idx = tableName.indexOf(".");
		if (idx != -1) {
			dbTableName = tableName.substring(0, idx);
		} else {
			dbTableName = tableName;
		}
		sortColumnName = e.attributeValue(SORT_COLUMN_NAME);
		Element depEl = e.element(DEPENDANTS);
		if (depEl != null) {
			String deps = depEl.attributeValue(TABLES);
			if (deps == null) {
				throw new IllegalArgumentException("Table "+tableName+" dependants table label is incorrect.  No dependants found in the element");
			}
			dependants = deps.split(",");
		}
	
		refLists = new ArrayList<ReferenceList>();
		List<Element> refListEls = e.elements(REF_LIST);
		for(Element refListEl : refListEls) {
			refLists.add(new ReferenceList(refListEl));
		}
		
		columnAs = new HashMap<String,String>();
		List<Element> fAs = e.elements(TableRef.FIELDAS);
		for(Element fa : fAs) {
			columnAs.put(fa.attributeValue(TableRef.FIELDNAME), fa.attributeValue(TableRef.ASVALUE));
		}
	}
	
	/**
	 * addEntries add elements that go into the main element for this table
	 */
	protected void addEntries() {
		// default is to do nothing;
	}
	
	/**
	 * addTable puts this reference table into an element
	 * @param e
	 * @param refUpdateMap 
	 * @throws ProcessingException 
	 */
	public void addTable(Element e, HashMap<String, Date> updateDateMap) throws ProcessingException {
		// default is to do nothing.
	}
	
	/**
	 * loadTable gets the table information from the docbase, and creates both the xml and refList for the table
	 * @param session
	 * @param updateDateMap
	 * @throws ProcessingException 
	 */
	public void createElement(HashMap<String, Date> updateDateMap) throws ProcessingException {
		
		tableElement = new BaseElement(tableElemName);
		Date tableDate = updateDateMap.get(dbTableName);
		if (tableDate == null) {
			throw new ProcessingException("Table update date not found for table: "+dbTableName);
		}
		tableElement.addAttribute("update-date", formatDateValue(tableDate, true));
		
		addEntries();
	}
	
	/**
	 * dependsOn returns true if this table depends on the t2 table.
	 * @param t2
	 * @return
	 */
	public boolean dependsOn(ReferenceTable t2) {
		String[] t2Dependants = t2.dependants;
		if (t2Dependants != null) {
			for(String dependant : t2Dependants) {
				if (dependant.equals(tableName)) {
					return true;
				}
			}
		} 
		return false;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}

	protected void getValues(HashMap<String, String> results, int rowIdx) {
		for(String col : cols) {
			if (columnAs.containsKey(col)) {
				results.put(columnAs.get(col), result.getValue(rowIdx, col).toString());
			} else {
				results.put(col, result.getValue(rowIdx, col).toString());
			}
		}
	}
	
	protected String getLoadDQL() {
		StringBuffer dql = new StringBuffer();
		dql.append("select * from dm_dbo.").append(dbTableName);
		if (whereClause != null && whereClause.length() > 0) {
			dql.append(" where ").append(whereClause);
		}
		dql.append(" order by ").append(sortColumnName);
		return dql.toString();
	}
	
	/**
	 * loadTable gets the table information from the docbase, and creates both the xml and refList for the table
	 * @param session
	 * @param refTableMap 
	 * @param updateDateMap
	 * @throws DfException
	 */
	public void loadTable(IDfSession session, HashMap<String, HashMap<String, String>>refListMap, HashMap<String, ReferenceTable> refTableMap) throws DfException {
		long start = System.currentTimeMillis();
		LoaderLog.info("Loading refTable: "+tableName);
		String dql = getLoadDQL();
		IDfCollection results = null;
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.READ_QUERY, "Get ReferenceTable: "+
					tableName);
			result = new Result(results);
			cols = result.getColumnsName();
			for(ReferenceList refList : refLists) {
				refList.addEntry(result);
			}
			LoaderLog.info("Loaded refTable: "+tableName+" "+(System.currentTimeMillis() - start)+" ms");
		} catch (DfException dfe) {
			LoaderLog.fatal("Loading table "+tableName+" dql: "+dql+" FAILED "+ dfe.getMessage());
			throw dfe;
		} finally {
			DocbaseUtils.closeResults(results);
		}
		// now put the referenceLists into the map
		for(ReferenceList refList : refLists) {
			refListMap.put(refList.getListName(), refList.getList());
		}
		// remove the last element that was created for this table
		tableElement = null;
		
		// tell the dependants that they need to rebuild the output xml
		if (dependants != null) {
			for(String tableName : dependants) {
				ReferenceTable refTable = refTableMap.get(tableName);
				if (refTable != null) {
					refTable.tableRefChanged();
				}
			}
		}
		
		// tell the files that this table has changed
		if (refFiles != null) {
			for(ReferenceFile refFile : refFiles) {
				refFile.setUpdated(true);
			}
		}
	}
	
	/**
	 * tableRefChanged sets the flag indicating that something this table is dependent on has changed
	 */
	public void tableRefChanged() {
		// clear the table Element so it will be recreated
		tableElement = null;
		
		// tell the files that this table has changed
		if (refFiles != null) {
			for(ReferenceFile refFile : refFiles) {
				refFile.setUpdated(true);
			}
		}
	}
	
	static public synchronized String formatDateValue(boolean withTime) {
		return formatDateValue(null, withTime);
	}
	
	static public synchronized String formatDateValue(Date dateField, boolean withTime) {
		if (dateField == null) {
			formatDate.setTimeInMillis(System.currentTimeMillis());
		} else {
			formatDate.setTime(dateField);
		}

		// clear the buffer
		dateBuffer.setLength(0);

		addDateValue(dateBuffer, "", formatDate.get(Calendar.YEAR));
		addDateValue(dateBuffer, "-", (formatDate.get(Calendar.MONTH) + 1));
		addDateValue(dateBuffer, "-", formatDate.get(Calendar.DATE));

		if (withTime) {
			addDateValue(dateBuffer, "T", formatDate.get(Calendar.HOUR));
			addDateValue(dateBuffer, ":", formatDate.get(Calendar.MINUTE));
			addDateValue(dateBuffer, ":", formatDate.get(Calendar.SECOND));
		}
		return dateBuffer.toString();
	}

	static private void addDateValue(StringBuffer sb, String sep, int val) {
		sb.append(sep);
		if (val < 10) {
			sb.append(0);
		}
		sb.append(val);
	}

}
