package com.hp.loader.priorityLoader;


public class NonRecoverableException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NonRecoverableException(String message) {
		super(message, false);
	}
	
	public NonRecoverableException(String message, Throwable t){
		super(message,t,false);
	}
	public NonRecoverableException(Throwable t){
		super(t,false);
	}
}
