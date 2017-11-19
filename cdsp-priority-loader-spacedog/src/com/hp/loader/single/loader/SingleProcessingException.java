/**
 * 
 */
package com.hp.loader.single.loader;

/**
 * @author dahlm
 *
 */
public class SingleProcessingException extends Exception {

	// shouldRetry indicates that this exception can be recovered from.
	private boolean shouldRetry = false;

	/**
	 * 
	 * @param shouldRetry indicates that this is a recoverable error and should be tried again
	 */
	public SingleProcessingException() {
		super();
	}

	/**
	 * 
	 * @param shouldRetry indicates that this is a recoverable error and should be tried again
	 */
	public SingleProcessingException(boolean shouldRetry) {
		super();
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param message
	 */
	public SingleProcessingException(String message) {
		super(message);
	}

	/**
	 * @param message
	 */
	public SingleProcessingException(String message, boolean shouldRetry) {
		super(message);
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param cause
	 */
	public SingleProcessingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param cause
	 */
	public SingleProcessingException(Throwable cause, boolean shouldRetry) {
		super(cause);
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SingleProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SingleProcessingException(String message, Throwable cause, boolean shouldRetry) {
		super(message, cause);
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @return the shouldRetry
	 */
	public boolean shouldRetry() {
		return shouldRetry;
	}
}
