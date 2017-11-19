package com.hp.cdsplus.update.Exception;

public class MongoDataException extends Exception {
	public MongoDataException(){
		super();
	}
	
	public MongoDataException(String message){
		super(message);
	}
	public MongoDataException(String message, Throwable t){
		super(message, t);
	}
}
