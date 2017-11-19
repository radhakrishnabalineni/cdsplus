package com.hp.cdsplus.update.Exception;

public class MongoConnectionException extends Exception {
	public MongoConnectionException(){
		super();
	}
	
	public MongoConnectionException(String message){
		super(message);
	}
	public MongoConnectionException(String message, Throwable t){
		super(message, t);
	}
}
