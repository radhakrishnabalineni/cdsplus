/**
 * #(@) Rule.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * A rule for the SRE.
 *
 * @author  Kelly
 * @version $Revision: 3$
 */
public final class Rule implements Serializable {
	private static final Logger logger = LogManager.getLogger(Rule.class);
	private static final long serialVersionUID = 1L;
	private String name;
	private Condition condition;
	private boolean isFinal = false;
	private String id = "n/a";
	
	boolean ignorBadRules = true;
	
	/**
	 * Creates a new Rule.
	 * 
	 * @param name of the rule
	 */
	public Rule(String name) {
		this.name = name;
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


	/**
	 * Getter: attribute name
	 * @return Returns the value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter: attribute name
	 * @param name new value of name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter: attribute final
	 * @param isFinal final means that the ruleset stop here if this rule
	 * matches.
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * Returns true/false whether we need to stop if this rule matches (the rule is final).
	 * @return true/false whether this rule is final or not.
	 */
	public boolean isFinal() {
		return this.isFinal;
	}
	
	/**
     * Getter for id.
     * @return the id
     */
    public String getId() {
    	return id;
    }

	/**
     * Setter for id.
     * @param id the id
     */
    public void setId(String id) {
    	this.id = id;
    }

	/**
	 * Evaluate the rule.
	 * @param md metadata
	 * @return <code>true</code> if rule matches
	 * @throws RuleException rule could not be evaluated
	 */
	public boolean eval(MDProperties md) throws RuleException {
		logger.debug("Evaluating rule: " + name);
		try {
			LDC.add(0, "rule", id, null);
			return condition.eval(md, 1);
		}
		catch (RuleException e) {
			if (ignorBadRules) {
				logger.debug("Bad rule: " + name);
				logger.error(e);
				return false;
			}
			throw e;
		}
	}

}
