package com.hp.cdsplus.wds.exception;

public class NonRetryException extends DestinationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public NonRetryException(String message) {
		super(message,new Throwable().fillInStackTrace(),true);
	}
	
	/**
	 * @param message
	 * @param t
	 */
	public NonRetryException(String message,Throwable t) {
		super(message,t,true);
	}
	
	/**
	 * @param t
	 */
	public NonRetryException(Throwable t){
		super(t,false);
	}
}
