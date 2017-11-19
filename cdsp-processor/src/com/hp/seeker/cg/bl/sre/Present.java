/**
 * #(@) Presnet.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;


/**
 * Check for the presence of a property with a given name. 
 *
 * @author  Kelly
 * @version $Revision: 2$
 */
public class Present extends Condition {
	
	private static final long serialVersionUID = 1L;
	String name;
	
	/**
	 * Creates a new Present.
	 * @param name property name
	 */
	public Present(Element condition) {
		super(condition);
		
		this.name = condition.getAttributeValue("name");
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		boolean result = true;
		
		MDProperty p = md.get(name);
		if (p == null) {
			result = false;
		}
		
//		String[] vals = p.getValues();
//		if (vals == null || vals.length == 0)
//			throw new RuleException("Metadata property has no values: " + name);
		
		LDC.add(depth, "Present", id, "Property '" + name + "' is present => " + result);

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

}
