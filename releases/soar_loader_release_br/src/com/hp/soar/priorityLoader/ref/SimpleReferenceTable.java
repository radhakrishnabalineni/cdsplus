/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import com.hp.loader.priorityLoader.ProcessingException;

/**
 * @author dahlm
 *
 */
public class SimpleReferenceTable extends ReferenceTable {

	protected ArrayList<RefElement> elements;

	/**
	 * @param e //description of table
	 * @param refFile Owning reference file
	 * @param referenceFiles list of reference files into which will be added the file for this table
	 */
	public SimpleReferenceTable(Element e, ReferenceFile refFile) {
		super(e);
		elements = new ArrayList<RefElement>();
		List<Element> els = e.elements(RefField.ELEMENT);
		for ( Element el : els ) {
			elements.add(RefElement.getElement(el));
		}
		// create a reference file for this table, and add this table to the referenceFiles list
		refFiles = new ArrayList<ReferenceFile>();
		// save the file for notification
		refFiles.add(refFile);
		// add as part of that file
		refFile.addTable(tableName);
	}	
	
	/**
	 * @param e //description of table
	 * @param refFile Owning reference file
	 * @param referenceFiles list of reference files into which will be added the file for this table
	 */
	public SimpleReferenceTable(Element e, ReferenceFile refFile, ArrayList<ReferenceFile> referenceFiles) {
		this(e, refFile);
		// make a new ReferenceFile
		ReferenceFile myRefFile = new ReferenceFile(tableElemName, "");
		// add me to it for storing
		myRefFile.addTable(tableName);
		// add the refFile for notification on update
		refFiles.add(myRefFile);
		// add my file as one to be stored
		referenceFiles.add(myRefFile);
	}	
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.ReferenceTable#addTable(org.dom4j.Element)
	 */
	@Override
	public void addTable(Element e, HashMap<String, Date> updateDateMap) throws ProcessingException {
		if (tableElement == null) {
			createElement(updateDateMap);
		}
		if (tableElement.getParent() == null) {
			e.add(tableElement);
		} else {
			e.add(tableElement.detach());
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.ref.AbstractReferenceTable#loadEntries(com.documentum.fc.client.IDfCollection)
	 */
	@Override
	protected void addEntries() {
		int count = result.getRowCount();
		try {
			HashMap<String, String> vals = new HashMap<String,String>();
			for(int i=0; i<count; i++) {
				getValues(vals, i);
				for (RefElement element : elements) {
					element.addXML(vals, tableElement);
				}
			}
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException(tableName+":"+iae.getMessage(), iae);
		}
	}

}
