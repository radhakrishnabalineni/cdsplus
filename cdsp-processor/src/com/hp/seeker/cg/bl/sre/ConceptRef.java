/**
 * #(@) Or.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;


/**
 * Concept reference - reference to a global concept.
 *
 * @author  Kelly
 * @version $Revision: 5$
 */
public class ConceptRef extends Condition {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new Not.
	 * @param condition sub-condition
	 */
	public ConceptRef(Element condition) {
		super(condition);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		Concept concept = RuleFactory.CONCEPTS.get(id);
		
		if (concept != null) {
			return concept.eval(md, depth);
		}
		
		throw new RuleException("Concept reference not found for ID: " + id);
	}

}
