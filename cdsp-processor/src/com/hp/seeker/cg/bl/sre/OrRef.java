package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;

/**
 * Class description goes here.
 *
 * @author  Kelly
 * @version $Revision: 6$
 */
public class OrRef extends Condition {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new Not.
	 * @param condition sub-condition
	 */
	public OrRef(Element condition) {
		super(condition);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		Or or = RuleFactory.OR_CONDITIONS.get(id);
		
		if (or != null) {
			return or.eval(md, depth);
		}
		
		throw new RuleException("Or reference not found for ID: " + id);
	}
}
