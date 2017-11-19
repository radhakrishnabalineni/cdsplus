/**
 * 
 */
package com.hp.cdsplus.wds.exception;

/**
 * @author kashyaks
 *
 */
public class DestinationException extends Exception {
	private boolean isRetry;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DestinationException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DestinationException(String arg0) {
		super(arg0);
		this.setRetry(false);
	}

	/**
	 * @param arg0
	 */
	public DestinationException(Throwable arg0) {
		super(arg0);
		this.setRetry(false);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DestinationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.setRetry(false);
	}
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public DestinationException(String arg0, Throwable arg1,boolean isRetry) {
		super(arg0, arg1);
		this.setRetry(isRetry);
	}
	
	public DestinationException(Throwable t, boolean is_retry){
		super(t);
		this.isRetry=is_retry;
	}

	public void setRetry(boolean isRetry) {
		this.isRetry = isRetry;
	}

	public boolean isRetry() {
		return isRetry;
	}
}
