package com.hp.cdsplus.mongo.exception;

public class MongoUtilsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MongoUtilsException(){
		super();
	}
	
	public MongoUtilsException(String message){
		super(message);
	}
	public MongoUtilsException(String message, Throwable t){
		super(message, t);
	}
}
