/**
 * #(@) MultiValueProperty.java Aug 14, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.util.Collection;

/**
 * Multi value property.
 *
 * @author  Kelly
 * @version $Revision: 3$
 */
public class MultiValueProperty implements MDProperty {
	
	private String[] values = new String[0];
	
	/**
	 * Creates a new instance of MultiValueProperty.
	 */
	public MultiValueProperty(String[] values) {
		if (values == null) {
			this.values = null;
		} else {
			this.values = new String[values.length];
			System.arraycopy(values, 0, this.values, 0, values.length);
		}
	}

	/**
	 * Creates a new instance of MultiValueProperty.
	 */
	public MultiValueProperty(Collection<String> values) {
		this.values = values.toArray(this.values);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.MDProperty#getValue()
	 */
	public String[] getValues() {
		if (values == null) {
			return null;
		} else {
			String[] retVal = new String[values.length];
			System.arraycopy(this.values, 0, retVal, 0, retVal.length);
			return retVal;
		}
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString() {
	    final String tab = "    ";
	    
	    StringBuffer retValue = new StringBuffer();
	    
	    retValue.append("MultiValueProperty ( ");
	    retValue.append(super.toString() + tab);
	    retValue.append("values = [");
	    if ((this.values != null) && (this.values.length != 0)) {
	    	int index=0;
	    	for(index=0; index<this.values.length-1; index++) {
	    		retValue.append(this.values[index]);
	    		retValue.append(",");
	    	}
	    	retValue.append(this.values[index]);
	    }
	    retValue.append(tab + " )");
	
	    return retValue.toString();
	}

}
