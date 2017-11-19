package com.hp.loader.priorityLoader;


public class RecoverableException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RecoverableException(String message, Throwable t){
		super(message,t,true);
	}
	public RecoverableException(Throwable t){
		super(t,true);
	}


}
