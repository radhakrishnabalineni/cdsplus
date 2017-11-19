package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;

/**
 * Concept reference - reference to a global concept.
 *
 * @author  Patrick
 * @version $Revision: 6$
 */

public class AndRef extends Condition {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Creates a new Not.
	 * @param condition sub-condition
	 */
	public AndRef(Element condition) {
		super(condition);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		And and = RuleFactory.AND_CONDITIONS.get(id);
		
		if (and != null) {
			return and.eval(md, depth);
		}
		
		throw new RuleException("And reference not found for ID: " + id);
	}
}
