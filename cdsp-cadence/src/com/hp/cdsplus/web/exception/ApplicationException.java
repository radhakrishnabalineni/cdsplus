package com.hp.cdsplus.web.exception;

/**
 * @author reddypm
 *
 */
public class ApplicationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ApplicationException() {
		super();
	}

	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationException(Throwable cause) {
		super(cause);
	}

	public ApplicationException(String msg) {
		super(msg);
	}

}