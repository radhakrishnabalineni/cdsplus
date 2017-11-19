package com.hp.soar.priorityLoader.workItem;

public class ICMException extends Exception {
	private long statusCode;
	private String response;
	
	public ICMException(long statusCode, String response) {
		super();
		this.statusCode = statusCode;
		this.response = response;
	}

	public long getStatusCode() {
		return statusCode;
	}

	public String getResponse() {
		return response;
	}
	
}
