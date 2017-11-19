package com.hp.cdsplus.pmloader.single;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class LoaderInitializationException extends Exception {

	/**
	 * Constructor for LoaderInitializationException.
	 * @param string String
	 */
	public LoaderInitializationException(String string) {
		super(string);
	}

	/**
	 * Constructor for LoaderInitializationException.
	 * @param msg String
	 * @param t Throwable
	 */
	public LoaderInitializationException(String msg, Throwable t) {
			super(msg,t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -881546253796143421L;

}
