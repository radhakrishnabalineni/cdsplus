package com.hp.cdsplus.dao.exception;

public class OptionsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OptionsException(){super();}
	public OptionsException(Throwable t){super(t);}
	public OptionsException(String message){super(message);}
	public OptionsException(String message, Throwable t){super(message,t);}
}
