/**
 * 
 */
package com.hp.seeker.cg.bl.sre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Sanity checker for Rules and conditions.
 * 
 * @author Kelly
 * @version $Revision: 2$
 */
public class RuleChecker {
	private static final Logger logger = LogManager.getLogger(RuleChecker.class);
	public static boolean checkRule(Rule rule) {
	    
		Condition condition = rule.getCondition();
		if (condition == null) {
			logger.warn("No condition for rule: " + rule.getName());
			return false;
		}
		
		return checkCondition(condition);
    }

	public static boolean checkCondition(Condition condition) {
		if (condition instanceof SubCondition) {
	        SubCondition sc = (SubCondition) condition;
	        
	        if (sc.subConditions == null || sc.subConditions.isEmpty()) {
	        	logger.warn("No sub-condition for condition: "
	        			+ sc.getClass() + " - id: "
	        			+ sc.getId());
				return false;
	        }
	        
	        for (Condition con : sc.subConditions) {
	            if (! checkCondition(con)) {
	            	return false;
	            }
            }
        }
		
		if (condition instanceof And || 
				condition instanceof Or) {
	        SubCondition sc = (SubCondition) condition;
	        

	        
	        if (sc.subConditions.size() < 1 && SREngine.getEngine().isAllowSingleValueSubAndOr()) {
	        	logger.warn("No sub-conditions for condition: "
	        			+ sc.getClass() + " - id: "
	        			+ sc.getId());
				return false;
	        }
	        
	        if (sc.subConditions.size() < 2 && ! SREngine.getEngine().isAllowSingleValueSubAndOr()) {
	        	logger.warn("Less than 2 sub-conditions for condition: "
	        			+ sc.getClass() + " - id: "
	        			+ sc.getId());
				return false;
	        }
        }
		
	    return true;
    }

}
