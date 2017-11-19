/**
 * #(@) And.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Element;

/**
 * Condition with sub conditions.
 *
 * @author  Kelly
 * @version $Revision$
 */
public abstract class SubCondition extends Condition {
	
	private static final long serialVersionUID = 1L;

		
	final List<Condition> subConditions = new ArrayList<Condition>();
	
	/**
	 * Creates a new And.
	 * @param condition JDOM element that defines the condition
	 * @param conditions sub-conditions
	 */
	public SubCondition(Element condition, Collection<Condition> subConditions) {
		super(condition);
		this.subConditions.addAll(subConditions);
	}

	
	/**
	 * Getter: subConditions
	 * @return the value of subConditions
	 */
	public List<Condition> getSubConditions() {
		return subConditions;
	}

	/**
	 * Add a condition
	 * @param condition condition to add.
	 */
	public void addSubCondition(Condition condition) {
		subConditions.add(condition);
	}
	
	
}
