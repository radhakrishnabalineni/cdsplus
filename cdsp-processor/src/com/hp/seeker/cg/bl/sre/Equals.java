/**
 * #(@) Or.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;


/**
 * Check if a given property has a given value. 
 *
 * @author  Kelly
 * @version $Revision$
 */
public class Equals extends Condition {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Concept.class);
	
	String name;
	String value;
	
	/**
	 * Creates a new Equals.
	 * @param name property name
	 * @param value property value
	 */
	public Equals(Element condition) {
		super(condition);
		this.name = condition.getAttributeValue("name");
		this.value = condition.getAttributeValue("value");
	}

	
	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		boolean result = false;
		
		MDProperty p = md.get(name);
		if (p == null) {
			LDC.add(depth, "Equals", id, "Property '" + name + "' = '" + value + "' => false (not present)");
			return false;
		}
		
		String[] vals = p.getValues();
		if (vals == null || vals.length == 0){
			LDC.add(depth, "Equals", id, "Property '" + name + "' = '" + value + "' => false (has no value)");
			return false;
		}
		for (String val : vals) {
			if (value.equalsIgnoreCase(val)) {
				logger.debug("Condition is true: " + name + "=" + value);
				result = true;
				break;
			}
		}
		
		LDC.add(depth, "Equals", id, "Property '" + name + "' = '" + value + "' => " + result);
		return result;
	}

	/**
	 * Getter: attribute name
	 * @return Returns the value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter: attribute name
	 * @param name new value of name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter: attribute value
	 * @return Returns the value of value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Setter: attribute value
	 * @param value new value of value.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append("[id=");
		sb.append(id);
		sb.append(", name=");
		sb.append(name);
		sb.append(", value='");
		sb.append(value);
		sb.append("']");
		return sb.toString();
	}
	
	
}
