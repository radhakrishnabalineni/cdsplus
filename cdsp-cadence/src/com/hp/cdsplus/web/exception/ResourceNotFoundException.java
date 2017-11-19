package com.hp.cdsplus.web.exception;



/**
 * @author reddypm
 * 
 */
public class ResourceNotFoundException extends RuntimeException  
{

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String msg) {
		super(msg);
	}
	
	
}