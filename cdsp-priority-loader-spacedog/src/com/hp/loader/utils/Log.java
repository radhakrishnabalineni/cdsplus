package com.hp.loader.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Log class which wraps all the functionality of log4j
 * 
 * @author      GADSC IPG-IT CR
 * @version     %I%, %G%
 * @since       1.0
 */

public class Log {
    
    // Constants
    
    // Labels for the log message types
    private static final String DEBUG_LABEL = "DEBUG: ";
    private static final String WARN_LABEL = "WARN: ";
    private static final String INFO_LABEL = "INFO: ";
    private static final String ERROR_LABEL = "ERROR: ";
    private static final String FATAL_LABEL = "FATAL: ";
    // Labels for messages
    private static final String CLASS_LABEL = "Class ";
    private static final String METHOD_LABEL = "Method ";
    private static final String MESSAGE_SEPARATOR = " : ";
    private static final String EMPTY_MESSAGE = "";
    private static HashSet<String> reportedMessages = new HashSet<String>();
    private static boolean sendIMTickets = true;
    
    // Attributes
    
    private static Logger logger = Logger.getLogger(Log.class.getName());

    /**
     * Writes to the log a debug message
     * @param message Debug message
     */
    public static void debug(String message){
        try {
            logger.debug(message);
        } catch (Exception e) {
            System.out.println(DEBUG_LABEL.concat(message));
        }
    }
    
    /**
     * Writes to the log a warn message
     * @param message Warning message
     */
    public static void warn(String message){
        try {
            logger.warn(message);
        } catch (Exception e) {
            System.out.println(WARN_LABEL.concat(message));
        }
    }
    
    /**
     * Writes to the log an info message
     * @param message Info message
     */
    public static void info(String message){
        try {
            logger.info(message);
        } catch (Exception e) {
            System.out.println(INFO_LABEL.concat(message));
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
    
    /**
     * Returns Checks the debug flag
     * @return <code>true</code> if the debug is on;
     *         <code>false</code> otherwise.
     */
    public static boolean isDebugEnabled(){
        return logger.isDebugEnabled();
    }

    /**
     * Returns Checks the info flag
     * @return <code>true</code> if the info is on;
     *         <code>false</code> otherwise.
     */
    public static boolean isInfoEnabled(){
        return logger.isInfoEnabled();
    }

    /**
     * Generates a message with the class, method and information of an exception
     * @param e Exception for information gathering 
     * @return
     */
    public static String getExceptionMsgForLog(Exception exception) {
        StringBuffer message = new StringBuffer();
        
        try {
    		Throwable currException = exception;
    		while(currException != null) {
    			if (currException.getMessage() != null) {
    				message.append(currException.getMessage());
    			}

    			StackTraceElement[] stackTrace = currException.getStackTrace();
    			if (stackTrace.length > 0) {
    				message.append(" | ").append(stackTrace[0].toString());
    			}
    			currException = currException.getCause();
    			if (currException != null) {
    				message.append("\n Caused by  ");
    			}
    		}
        } catch (Exception e) {
            message = null;
            exception.printStackTrace();
        }
        
        return (message != null ? message.toString() : EMPTY_MESSAGE);
    }

    public static void logStackTrace(Throwable t) {
        String message = getStackTrace(t);
		try {
            logger.error(message);
        } catch (Exception e) {
            System.err.println(ERROR_LABEL.concat(message));
        }
    }

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String message = sw.toString().trim();
		return message;
	}
	
	public static  void setLevel(Level level){
		logger.setLevel(level);
	}
	
	public static  Level getLevel(){
		return logger.getLevel();
	}
    
	public static void setSendIMTickets(boolean val) {
		sendIMTickets = val;
	}
   
	public static void report(String tag, String msg) {
		if (!reportedMessages.contains(tag)) {
			logger.error("Send IM ticket | "+tag+" | "+msg);
			reportedMessages.add(tag);
			if (sendIMTickets) {
				// send an im ticket
			}
		} else {
			logger.error("Already sent IM ticket | "+tag+" | "+msg);
		}
	}

}
