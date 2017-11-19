package com.hp.seeker.cg.bl.sre;

import com.hp.seeker.cg.util.MetadataFormat;


/**
 * A factory returning the appropriate MDPropertyBuilder 
 * according to the metadata XML format.
 * 
 * @author Patrick Xureb
 */
public class MDPropertiesBuilderFactory {
	
	/**
	 * Returns the appropriate MDProperties builder according to the XML format.
	 * @param format The XML format
	 * @return The appropriate MDPropertiesBuilder
	 * @throws RuleException 
	 */
	public static MDPropertiesBuilder getBuilder(MetadataFormat format) 
			throws RuleException {
		switch (format) {
			case CPF : 
				return new CPFBuilder();
			case CDSplus :
				return new CDSPlusBuilder();
			default :
				throw new RuleException("Unsupported Metadata format " + format.toString());
		}
	}
	
}
