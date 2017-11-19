/**
 * 
 */
package com.hp.loader.history;

/**
 * @author dahlm
 *
 */
public class HistoryException extends Exception {

	/**
	 * 
	 */
	public HistoryException() {
	}

	/**
	 * @param message
	 */
	public HistoryException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public HistoryException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HistoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
