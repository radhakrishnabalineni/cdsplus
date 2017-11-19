package com.hp.cdsplus.update.Exception;

public class Log4JException extends Exception {
	public Log4JException(){
		super();
	}
	
	public Log4JException(String message){
		super(message);
	}
	public Log4JException(String message, Throwable t){
		super(message, t);
	}
}
