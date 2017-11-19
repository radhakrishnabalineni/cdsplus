/**
 * 
 */
package com.hp.loader.single.loader;

/**
 * @author dahlm
 *
 */
public class SingleHistoryException extends Exception {

	/**
	 * 
	 */
	public SingleHistoryException() {
	}

	/**
	 * @param message
	 */
	public SingleHistoryException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SingleHistoryException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SingleHistoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
