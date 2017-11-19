package com.hp.cdsplus.processor.exception;

public class AdapterException extends ProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AdapterException(){
		super();
	}
	public AdapterException(Throwable t){
		super(t);
	}
	public AdapterException(String message){
		super (message);
	}
	public AdapterException(String message, Throwable t){
		super(message, t);
	}

}
