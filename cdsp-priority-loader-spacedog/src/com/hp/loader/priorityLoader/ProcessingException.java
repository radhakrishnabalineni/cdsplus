/**
 * 
 */
package com.hp.loader.priorityLoader;

/**
 * @author dahlm
 *
 */
public class ProcessingException extends Exception {

	// shouldRetry indicates that this exception can be recovered from.
	private boolean shouldRetry = false;

	/**
	 * 
	 * @param shouldRetry indicates that this is a recoverable error and should be tried again
	 */
	public ProcessingException() {
		super();
	}

	/**
	 * 
	 * @param shouldRetry indicates that this is a recoverable error and should be tried again
	 */
	public ProcessingException(boolean shouldRetry) {
		super();
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param message
	 */
	public ProcessingException(String message) {
		super(message);
	}

	/**
	 * @param message
	 */
	public ProcessingException(String message, boolean shouldRetry) {
		super(message);
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param cause
	 */
	public ProcessingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param cause
	 */
	public ProcessingException(Throwable cause, boolean shouldRetry) {
		super(cause);
		this.shouldRetry = shouldRetry;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessingException(String message, Throwable cause, boolean shouldRetry) {
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
