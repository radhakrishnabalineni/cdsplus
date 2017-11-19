/**
 * #(@) Or.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;


/**
 * Not expression.
 *
 * @author  Kelly
 * @version $Revision$
 */
public class Not extends Condition {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	private Condition condition;
	
	/**
	 * Creates a new Not.
	 * @param condition sub-condition
	 */
	public Not(Element condition, Condition subCondition) {
		super(condition);
		this.condition = subCondition;
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		LDC.add(depth, "Not", id, null);
		return ! condition.eval(md, depth+1);
	}

	/**
	 * Getter: attribute condition
	 * @return Returns the value of condition.
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Setter: attribute condition
	 * @param condition new value of condition.
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
}
