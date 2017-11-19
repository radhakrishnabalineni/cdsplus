/**
 * #(@) RuleException.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

/**
 * Exception evaluating rules.
 *
 * @author  Kelly
 * @version $Revision$
 */
public class RuleException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new RuleException.
	 */
	public RuleException() {
	}

	/**
	 * Creates a new RuleException.
	 * @param message
	 */
	public RuleException(String message) {
		super(message);
	}

	/**
	 * Creates a new RuleException.
	 * @param cause
	 */
	public RuleException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new RuleException.
	 * @param message
	 * @param cause
	 */
	public RuleException(String message, Throwable cause) {
		super(message, cause);
	}

}
