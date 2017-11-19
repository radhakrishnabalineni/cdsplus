package com.hp.cdsplus.wds.exception;

public class RetryException extends DestinationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public RetryException(String message) {
		super(message,new Throwable().fillInStackTrace(),true);
	}
	
	/**
	 * @param message
	 */
	public RetryException(Throwable t) {
		super(t,true);
	}
	/**
	 * @param message
	 * @param t
	 */
	public RetryException(String message,Throwable t) {
		super(message,t,true);
	}
	
	
}
