package com.hp.cdsplus.update.Exception;

public class MongoQueryException extends Exception {
	public MongoQueryException(){
		super();
	}
	
	public MongoQueryException(String message){
		super(message);
	}
	public MongoQueryException(String message, Throwable t){
		super(message, t);
	}
}
