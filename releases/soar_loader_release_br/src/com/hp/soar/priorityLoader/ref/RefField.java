/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;
import org.dom4j.Namespace;

import com.documentum.fc.common.DfException;

/**
 * @author dahlm
 *
 */
abstract public class RefField {
	// xml field names
	public static final String ELEMENT = "element";
	public static final String ATTRIBUTE = "attribute";
	protected static final String TYPE = "type";	
	public static final String NONNILATTRIBUTE = "nonNilAttribute";
	public static final String TABLEREF = "tableRef";
	
	public static final String NAME = "name";
	public static final String FIELD_VALUE_NAME = "fieldValueName";

	protected String name;
	protected String fieldValueName;
	protected static Namespace nameSpace = new Namespace("NO_NS","NO_NS_URI");
	
	/**
	 * 
	 * @param labelName  // XML tag name
	 * @param fieldValueName  // name of table field to get this value from
	 */
	public RefField(Element e) {
		name = e.attributeValue(NAME);
		fieldValueName = e.attributeValue(FIELD_VALUE_NAME);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the fieldValue
	 */
	public String getFieldValueName() {
		return fieldValueName;
	}
	
	abstract public void addXML(HashMap<String, String>result, Element el) throws DfException;
}
