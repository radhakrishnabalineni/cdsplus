package com.hp.cdsplus.pmloader.single;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class LoaderException extends Exception {

	/**
	 * Constructor for LoaderException.
	 * @param string String
	 */
	public LoaderException(String string) {
		super(string);
	}
	
	/**
	 * Constructor for LoaderException.
	
	 * @param t Throwable
	 */
	public LoaderException(Throwable t) {
		super(t);
	}

	public LoaderException(String string, Throwable t) {
		super(string, t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7877182548081953612L;

}
