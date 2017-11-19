/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.util.ArrayList;

import org.dom4j.Element;

/**
 * ReferenceTableFactory takes in an Element and determines the type of reference table to be created.
 * @author dahlm
 *
 */
public class ReferenceTableFactory {
	public static final String REFFILE = "refFile";
	public static final String REFTABLE = "refTable";
	public static final String TABLE_TYPE = "tableType";
	private static final String SIMPLEREFTABLE = "SimpleReferenceTable";
	private static final String NONSAVEDREFTABLE = "NonSavedReferenceTable";
	private static final String DISCLOSURELEVELREFTABLE = "DisclosureLevelReferenceTable";
	private static final String PRODUCTENVIRONMENTREFTABLE = "ProductEnvironmentReferenceTable";
	private static final String LANGUAGECOUNTRYREFTABLE = "LanguageCountryReferenceTable";
	private static final String COUNTRYPREFERENCEREFTABLE = "CountryPreferenceReferenceTable";
	private static final String DOCUMENTTYPESREFTABLE = "DocumentTypesReferenceTable";	
	
	
	public static ReferenceTable newTable(Element e, ReferenceFile refFile, ArrayList<ReferenceFile> referenceFiles) {
		String tableType = e.attributeValue(TABLE_TYPE);
		if (tableType == null || tableType.length()==0) {
			throw new IllegalArgumentException(REFTABLE+" element is not well defined.  No "+TABLE_TYPE+" attribute");
		}
		if (tableType.equals(SIMPLEREFTABLE)) {
			return new SimpleReferenceTable(e, refFile, referenceFiles);
		} else if (tableType.equals(NONSAVEDREFTABLE)) {
			return new NonSaveReferenceTable(e);
		} else if (tableType.equals(DISCLOSURELEVELREFTABLE)) {
			return new DisclosureLevelReferenceTable(e, refFile, referenceFiles);
		} else if (tableType.equals(PRODUCTENVIRONMENTREFTABLE)) {
			return new ProductEnvironmentReferenceTable(e, refFile, referenceFiles);
		} else if (tableType.equals(LANGUAGECOUNTRYREFTABLE)) {
			return new LanguageCountryReferenceTable(e, refFile, referenceFiles);
		} else if (tableType.equals(COUNTRYPREFERENCEREFTABLE)) {
			return new CountryPreferenceReferenceTable(e, refFile);
		} else if (tableType.equals(DOCUMENTTYPESREFTABLE)) {
			return new DocumentTypesReferenceTable(e, refFile, referenceFiles);
		}
		throw new IllegalArgumentException("Unknown tableType: "+tableType);
	}
}
