package com.hp.concentra.extractor.utils;

/**
 * Class used to time stamp the elapsed time between a given time and another
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class TimeStamper {

    private static final String ELAPSE = " elapse: ";
    /**
     * Time stamp start point
     */
    private long start;
    /**
     * Name of the measured object or method
     */
    private String name;

    /**
     * Default class that initializes the start time
     */
    public TimeStamper() {
        start = -1;
    }

    /**
     * Class constructor that sets the time stamper name
     * 
     * @param name
     *            Time stamper name
     */
    public TimeStamper(String name) {
        this();
        this.name = name;
    }

    /**
     * Start the time stamper
     */
    public void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Stop the time stamper
     * 
     * @return String with elapsed time
     */
    public String stop() {
        StringBuffer sb = new StringBuffer();
        long elapse = System.currentTimeMillis() - start;
        if (name != null) {
            sb.append(name);
        }
        return sb.append(ELAPSE).append(elapse).toString();
    }
}
