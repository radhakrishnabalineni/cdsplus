/**
 * #(@) False.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;

/**
 * FALSE - Condition that is always false.
 *
 * @author  Kelly
 * @version $Revision$
 */
public class False extends Condition {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param condition JDOM element that defines the condition.
	 */
	public False(Element condition) {
		super(condition);
	}

	
	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		LDC.add(depth, "False", id, "Always false");
		return false;
	}

}
