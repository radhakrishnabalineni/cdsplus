package com.hp.concentra.extractor.documents;

/**
 * Exception thrown when the merge operation over a support communication object
 * has failed
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class SupportCommMergerException extends Exception {

    /***************************************************************************
     * Default class constructor
     */
    public SupportCommMergerException() {
        super();
    }

    /***************************************************************************
     * Class constructor that sets the exception message
     * 
     * @param exceptionMsg
     *            Message of the exception
     */
    public SupportCommMergerException(String exceptionMsg) {
        super(exceptionMsg);
    }
}
