/**
 * #(@) True.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;

/**
 * TRUE - Condition that is always true.
 *
 * @author  Kelly
 * @version $Revision$
 */
public class True extends Condition {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param condition JDOM element that defines the condition.
	 */
	public True(Element condition) {
		super(condition);
	}

	
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		LDC.add(depth, "True", id, "always true");
		return true;
	}

}
