/**
 * #(@) Or.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;

/**
 * OR expression.
 *
 * @author  Kelly
 * @version $Revision: 2$
 */
public class Or extends SubCondition {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Or.class);
	
	/**
	 * Creates a new Or.
	 * @param condition JDOM element that defines the condition
	 * @param subConditions sub-conditions
	 */
	public Or(Element condition, Collection<Condition> subConditions) {
		super(condition, subConditions);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		if (subConditions.size() < 2 && ! SREngine.getEngine().isAllowSingleValueSubAndOr()) {
			throw new RuleException("OR has less than 2 sub-conditions.");
		}
		
		LDC.add(depth, "Or", id, null);
		
		for (Condition c : subConditions) {
			boolean subCon = c.eval(md, depth+1);
			if (subCon) {
				logger.debug("Sub condition is true: " + c);
				return true;
			}
		}
		
		logger.debug("All subconditions are false.");
		return false;
	}
	
}
