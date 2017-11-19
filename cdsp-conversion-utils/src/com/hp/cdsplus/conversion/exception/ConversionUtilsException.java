package com.hp.cdsplus.conversion.exception;

public class ConversionUtilsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConversionUtilsException(){
		super();
	}

	public ConversionUtilsException(String message){
		super(message);
	}
	public ConversionUtilsException(String message, Throwable t){
		super(message, t);
	}

}
