package com.hp.cdsplus.processor.exception;

public class ServiceHandlerException extends ProcessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ServiceHandlerException(){
		super();
	}
	
	public ServiceHandlerException(Throwable t){
		super(t);
	}
	
	public ServiceHandlerException(String message){
		super(message);
	}

	public ServiceHandlerException(String message, Throwable t){
		super(message, t);
	}
}
