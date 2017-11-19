/**
 * 
 */
package com.hp.loader.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author srivasni
 *
 */
public class ErrorLog {
	// Constants
    
    // Labels for the log message types
    private static final String DEBUG_LABEL = "DEBUG: ";
    private static final String WARN_LABEL = "WARN: ";
    private static final String INFO_LABEL = "INFO: ";
    private static final String ERROR_LABEL = "ERROR: ";
    private static final String FATAL_LABEL = "FATAL: ";
    
    private static Logger logger = Logger.getLogger(ErrorLog.class.getName());
    
	public static  void setLevel(Level level){
		logger.setLevel(level);
	}

	public static  Level getLevel(){
		return logger.getLevel();
	}
	
    public static void logStackTrace(Throwable t) {
        String message = getStackTrace(t);
		try {
            logger.error(message);
        } catch (Exception e) {
            System.err.println(ERROR_LABEL.concat(message));
        }
    }
    /**
     * Writes to the log an error message
     * @param message Error message
     */
    public static void error(String message){
        try {
            logger.error(message);
        } catch (Exception e) {
            System.out.println(ERROR_LABEL.concat(message));
        }
    }
    /**
     * Writes to the log a fatal message
     * @param message Fatal message
     */
    public static void fatal(String message){
        try {
            logger.fatal(message);
        } catch (Exception e) {
            System.out.println(FATAL_LABEL.concat(message));
        }
    }
    

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String message = sw.toString().trim();
		return message;
	}
}
