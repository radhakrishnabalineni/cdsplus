package com.hp.concentra;

/**
 * QueryLoaderException aids in the stack trace and debugging, indicating this
 * particular exception. This exception will be handle when a new query loading
 * is done
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class QueryLoaderException extends Exception {
    /**
     * Constructor for QueryLoaderException
     * 
     * @see Exception
     */
    public QueryLoaderException() {
        super();
    }

    /**
     * Constructor for QueryLoaderException
     * 
     * @param String
     *            Specific Exception message
     * @see Exception
     */
    public QueryLoaderException(String exceptionMsg) {
        super(exceptionMsg);
    }

}
