package com.hp.soar.priorityLoader.ref;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfValue;


/**
 * Result is a structure that organizes data returned by a dql query (DQL)
 * like a table.
 * 
 * Every query to the docbase returns an IDfCollection object that is taken by the
 * Result Object and transformed to a multidimensional array structure
 * containing the values in Java objects format, avoiding the handle of IDfValue
 * 
 * The Result object gives methods to organize, retrieve and manipulate data.
 * 
 * @author GADSC IPG-IT CR
 * @version 1.0
 * @since 1.0
 */

public class Result implements Serializable {
	private static final String SUFFIX_BOOLEAN = "_FLAG";
	private static final int CONCENTRA_TRUE_VALUE = 1;
	private HashMap indexByName;
	private ArrayList indexByNumber;
	private ArrayList data;

	/**
	 * Construct a new structure taking and transforming the values contained
	 * into collection
	 * 
	 * @param collection
	 *            The IDfCollection object contains the results of DQL
	 *            execution.
	 * @throws DfException 
	 * @see IDfCollection
	 */

	public Result(IDfCollection collection) throws DfException {
		if (collection != null) {
			int attrCounter = collection.getAttrCount();
			
			indexByName = new HashMap(attrCounter);
			indexByNumber = new ArrayList(attrCounter);

			data = new ArrayList(attrCounter);

			for (int index = 0; index < attrCounter; index++) {
				indexByName.put(collection.getAttr(index).getName(), new Integer(index));
				indexByNumber.add(collection.getAttr(index).getName());
				data.add(new ArrayList());
			} // ...for

			IDfValue value = null;

			while (collection.next()) {
				for (int index = 0; index < indexByNumber.size(); index++) {
					ArrayList col = (ArrayList) data.get(index);

					/** ********************************************************************************* */
					value = collection.getValue((String) indexByNumber.get(index));

					if (((String) indexByNumber.get(index)).toUpperCase().endsWith(SUFFIX_BOOLEAN)
							&& value.getDataType() == IDfValue.DF_INTEGER) {
						if (!value.toString().equals("")) {
							col.add(new Boolean(value.asInteger() == CONCENTRA_TRUE_VALUE));
						} else {
							col.add(value);
						}
					} else {
						col.add(extractValue(value));
					}

					/** ********************************************************************************* */

					// row.add(extractValue(collection.getValue((String)indexByNumber.get(index))));
				}
			} // ...while
		}
	}

	private void addIndex(HashMap indexByKey, Object val, Integer idx) {
		ArrayList list = (ArrayList)indexByKey.get(val);
		if (list == null) {
			list = new ArrayList();
			indexByKey.put(val, list);
		}
		list.add(idx);
	}
	
	/**
	 * Transform an IDfValue to equivalent Java Object
	 * 
	 * @param value
	 *            Object wrap to be transformed
	 * @return Specific Java Object containing the value
	 * @see IDfVAlue
	 */
	private Object extractValue(IDfValue value) {
		if (value != null) {
			switch (value.getDataType()) {
			case IDfValue.DF_BOOLEAN:
				return value.asBoolean() ? Boolean.TRUE : Boolean.FALSE;

			case IDfValue.DF_DOUBLE:
				return getNumber(value);
				
			case IDfValue.DF_ID:
				return value.asId();
				
			case IDfValue.DF_INTEGER:
				return getNumber(value);
				
			case IDfValue.DF_STRING:
				return value.asString();
				
			case IDfValue.DF_TIME:
				return new IDfTimeFormatter(value.asTime());
				
			case IDfValue.DF_UNDEFINED:
				return value;
			}
		}

		// If value is null, or value doesn't correspond to any type in the
		// switch struct
		return value;
	}

	/**
	 * Takes an IDfValue wrap object representing a number and transform it to
	 * the close corresponding type. That means if a Double is reached but it
	 * have not decimal values, then a Integer is returned, and other case
	 * return the Double. In case of Integer, always return Integer, and for
	 * other values no transformations are performed.
	 * 
	 * 
	 * @param value
	 * @return An object
	 */
	private Object getNumber(IDfValue value) {
		switch (value.getDataType()) {
		case IDfValue.DF_DOUBLE:
			Double dvalue = new Double(value.asDouble());
			if (dvalue.doubleValue() - dvalue.intValue() > 0) {
				return dvalue;
			}

			return new Integer(dvalue.intValue());
		case IDfValue.DF_INTEGER:
			return new Integer(value.asInteger());

		default:
			return value;
		}
	}

	/**
	 * Obtain the number of columns captured into the structure
	 * 
	 * @return int value with the number of columns
	 * 
	 */
	public int getColumnCount() {
		return !data.isEmpty() ? data.size() : 0;
	}

	/**
	 * Obtain the number of rows captured into the structure
	 * 
	 * @return int value with the number of rows
	 * 
	 */
	public int getRowCount() {
		return !data.isEmpty() ? ((ArrayList) data.get(0)).size() : 0;
	}
	
	/**
	 * Obtain an elements set that represent a tuple
	 * 
	 * @param rowIndex
	 *            int value that indicates the specific row to be returned.
	 * @return ArrayList Collection object with all elements that integrate the
	 *         row if the <b>rowIndex</b> is valid, in other case a null value
	 *         will be returned.
	 * 
	 */
	public ArrayList getRow(int rowIndex) {
		ArrayList row = null;

		if (getRowCount() >= rowIndex) {
			row = new ArrayList(data.size());

			for (int index = 0; index < data.size(); index++) {
				row.add(index, ((ArrayList) data.get(index)).get(rowIndex));
			}// ...for
		}

		return row;
	}

	/**
	 * Obtain an elements set that represent a column
	 * 
	 * @param colIndex
	 *            int value that indicates the specific column to be returned.
	 * @return ArrayList Collection object with all elements that integrate the
	 *         column if the <b>colIndex</b> is valid, in other case a null
	 *         value will be returned.
	 * 
	 */
	public ArrayList getColumn(int colIndex) {
		return (ArrayList) data.get(colIndex);
	}

	/**
	 * Obtain an elements set that represent a column
	 * 
	 * @param columnName
	 *            String value that indicates the specific column to be returned
	 *            using the name attribute.
	 * @return ArrayList Collection object with all elements that integrate the
	 *         column if the <b>columnName</b> is valid name, in other case a
	 *         null value will be returned.
	 * 
	 */
	public ArrayList getColumn(String columnName) {
		Object column = indexByName.get(columnName);

		if (column != null) {
			int colIndex = ((Integer) column).intValue();
			return (ArrayList) data.get(colIndex);
		}

		return null;
	}

	/**
	 * Obtain a specific column name
	 * 
	 * @param colIndex
	 *            int value that indicates the specific column to be query.
	 * @return A String value with the name of column linked to the specific
	 *         <b>colIndex</b> if exist, in other case a null value is
	 *         returned.
	 * 
	 */
	public String getColumnName(int colIndex) {
		if (getColumnCount() >= colIndex) {
			return indexByNumber.get(colIndex).toString();
		}
		return null;
	}

	/**
	 * Obtain a Sting set representing all columns name declared into the
	 * structure, the order is established by individual column index.
	 * 
	 * @return An array with columns names
	 * 
	 */
	public String[] getColumnsName() {
		return (String[]) indexByNumber.toArray(new String[0]);
	}

	/**
	 * Replace an specific value contained into the structure, in a specific
	 * position
	 * 
	 * @param rowIndex
	 *            int value that represents the row that contains the value to
	 *            be replaced
	 * @param colIndex
	 *            int value that represents the column that contains the value
	 *            to be replaced
	 * @param value
	 *            Any valid Java object containing the new value to be inserted
	 *            into the structure
	 * 
	 * @return boolean value indicates the success of the process
	 * 
	 */
	public boolean setValue(int rowIndex, int colIndex, Object value) {
		if (rowIndex >= 0 && rowIndex < getRowCount() && colIndex >= 0 && colIndex < getColumnCount()) {
			ArrayList columnData = ((ArrayList) data.get(colIndex));
			columnData.set(rowIndex, value);
			return true;
		}

		return false;
	}

	/**
	 * Replace an specific value contained into the structure, in a specific
	 * position
	 * 
	 * @param rowIndex
	 *            int value that represents the row that contains the value to
	 *            be replaced
	 * @param columnName
	 *            String value that represents the column that contains the
	 *            value to be replaced
	 * @param value
	 *            Any valid Java object containing the new value to be inserted
	 *            into the structure
	 * 
	 * @return boolean value indicates the success of the process
	 * 
	 */
	public boolean setValue(int rowIndex, String columnName, Object value) {
		Object column = indexByName.get(columnName);

		if (column != null) {
			int colIndex = ((Integer) column).intValue();
			return setValue(rowIndex, colIndex, value);
		}

		return false;
	}

	/**
	 * Returns the element at the specified position in the structure.
	 * 
	 * @param rowIndex
	 *            int value that represents the row that contains the value to
	 *            be returned
	 * @param colIndex
	 *            int value that represents the column that contains the value
	 *            to be returned
	 * @return the java object at the specified position in the structure if
	 *         exist.
	 * 
	 */
	public Object getValue(int rowIndex, int colIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount() && colIndex >= 0 && colIndex < getColumnCount()) {
			return data.isEmpty() ? null : ((ArrayList) data.get(colIndex)).get(rowIndex);
		}

		return null;
	}

	/**
	 * Returns the element at the specified position in the structure.
	 * 
	 * @param rowIndex
	 *            int value that represents the row that contains the value to
	 *            be returned
	 * @param columnName
	 *            String value that represents the column that contains the
	 *            value to be returned
	 * @return the java object at the specified position in the structure if
	 *         exist.
	 * 
	 */
	public Object getValue(int rowIndex, String columnName) {
		Object column = indexByName.get(columnName);

		if (column != null) {
			int colIndex = ((Integer) column).intValue();
			return getValue(rowIndex, colIndex);

		}

		return null;
	}

	/**
	 * Returns an array list containing a set of array list that represent the
	 * information organized by column.
	 * 
	 * @return an array list containing all elements in the structure
	 * 
	 */
	public ArrayList getData() {
		return data;
	}

}
