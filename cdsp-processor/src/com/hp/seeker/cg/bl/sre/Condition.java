/**
 * #(@) Condition.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.io.Serializable;

import org.jdom.Element;


/**
 * A condition to evaluate base on the given metadata.
 *
 * @author  Kelly
 * @version $Revision: 2$
 */
public abstract class Condition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	
	/** ID of the condition. */
	String id = "n/a";
	
	/**
	 * Creates a new condition.
	 * @param condition JDOM element that defines the condition.
	 */
	public Condition(Element condition) {
		if (condition != null && condition.getAttributeValue("id") != null) {
			this.id = condition.getAttributeValue("id");
		}
	}
	
	/**
	 * Evaluate the condition
	 * @param md metadata
	 * @param depth depth of the recursion (used for debug indetation)
	 * @return <code>true</code> if condition is true.
	 * @throws RuleException If condition could not be evaluated.
	 */
	abstract boolean eval(MDProperties md, int depth) throws RuleException;

	/**
	 * Getter: id
	 * @return the value of id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter: id
	 * @param id new value of id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append("[id=");
		sb.append(id);
		sb.append("]");
		return sb.toString();
	}

}
