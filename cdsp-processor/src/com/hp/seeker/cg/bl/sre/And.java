/**
 * #(@) And.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;

import com.hp.seeker.cg.bl.CGS;

/**
 * AND expression.
 *
 * @author  Kelly
 * @version $Revision: 2$
 */
public class And extends SubCondition {
	
	private static final Logger logger = LogManager.getLogger(And.class);
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new And.
	 * @param condition JDOM element that defines the condition
	 * @param subConditions sub-conditions
	 */
	public And(Element condition, List<Condition> subConditions) {
		super(condition, subConditions);
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		if (subConditions.size() < 2 && ! SREngine.getEngine().isAllowSingleValueSubAndOr()) {
			throw new RuleException("AND has too few sub-conditions.");
		}
		
		LDC.add(depth, "And", id, null);
		
		for (Condition c : subConditions) {
			boolean subCon = c.eval(md, depth+1);
			if (! subCon) {
				logger.debug("Sub condition is false: " + c);
				return false;
			}
		}
		
		logger.debug("All subconditions are true.");
		return true;
	}

	
}
