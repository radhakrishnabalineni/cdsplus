/**
 * #(@) SingleValueProperty.java Aug 13, 2007
 */
package com.hp.seeker.cg.bl.sre;

/**
 * Single value property.
 * 
 * @author Kelly
 * @version $Revision: 2$
 */
public class SingleValueProperty implements MDProperty {

	private String[] value;

	public SingleValueProperty(String value) {
		this.value = new String[1];
		this.value[0] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.seeker.cg.bl.sre.MDProperty#getValue()
	 */
	public String[] getValues() {
		String[] retVal = {value[0]};
		return retVal;
	}

	/**
	 * Constructs a <code>String</code> with all attributes in name = value
	 * format.
	 * 
	 * @return a <code>String</code> representation of this object.
	 */
	public String toString() {
		String retValue = "";

		retValue = "SingleValueProperty ( " + "value = " + this.value[0] + " )";

		return retValue;
	}

}
