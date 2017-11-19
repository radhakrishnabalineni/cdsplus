package com.hp.cdsplus.processor.exception;

public class ProcessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessException(){super();}
	public ProcessException(Throwable t){super(t);}
	public ProcessException(String message){super(message);}
	public ProcessException(String message, Throwable t){super(message,t);}
}
