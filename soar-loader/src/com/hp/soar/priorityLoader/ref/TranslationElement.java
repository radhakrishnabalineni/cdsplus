/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.HashMap;

import org.dom4j.Element;

import com.documentum.fc.common.DfException;

/**
 * @author dahlm
 *
 */
public class TranslationElement extends RefField {
	private static final String TABLENAME = "tableName";
	private static final String KEYS= "keys";
	private static final String COLUMNAS = "columnAs";
	
	private String transTableName;
	private String[] keys;
	private HashMap<String, String> columnAs;
	
	/**
	 * @param e
	 */
	public TranslationElement(Element e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.RefField#addXML(java.util.HashMap, org.dom4j.Element)
	 */
	@Override
	public void addXML(HashMap<String, String> result, Element el)
			throws DfException {
		// TODO Auto-generated method stub

	}

}
