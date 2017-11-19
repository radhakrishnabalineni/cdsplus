package com.hp.concentra.extractor.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Log class which wraps all the functionality of log4j
 * 
 * @author      GADSC IPG-IT CR
 * @version     %I%, %G%
 * @since       1.0
 */

public class LoaderLog {

	// Constants

	// Labels for the log message types
	private static final String DEBUG_LABEL = "DEBUG: ";
	private static final String WARN_LABEL =  "WARN: ";
	private static final String INFO_LABEL =  "INFO: ";
	private static final String ERROR_LABEL = "ERROR: ";
	private static final String FATAL_LABEL = "FATAL: ";
	// Labels for messages
	private static final String CLASS_LABEL = "Class ";
	private static final String METHOD_LABEL = "Method ";
	private static final String MESSAGE_SEPARATOR = " : ";
	private static final String EMPTY_MESSAGE = "";

	// Attributes

	private static Logger logger = Logger.getLogger(LoaderLog.class.getName());

	/**
	 * Writes to the log a debug message
	 * @param message Debug message
	 */
	public static void debug(String message){
		try {
			logger.debug(message);
		} catch (Exception e) {
			//System.out.println(DEBUG_LABEL.concat(message));
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
			//System.out.println(WARN_LABEL.concat(message));
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
			//System.out.println(INFO_LABEL.concat(message));
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
			//System.out.println(ERROR_LABEL.concat(message));
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
			//System.out.println(FATAL_LABEL.concat(message));
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
	 * @param exception Exception for information gathering 
	 * @return
	 */
	public static String getExceptionMsgForLog(Exception exception) {
		StringBuffer message;

		try {
			message = new StringBuffer().append(
					CLASS_LABEL).append(
							exception.getStackTrace()[0].getClassName()).append(
									MESSAGE_SEPARATOR).append(
											METHOD_LABEL).append(
													exception.getStackTrace()[0].getMethodName()).append(
															MESSAGE_SEPARATOR).append(
																	exception.toString());
		} catch (Exception e) {
			message = null;
			exception.printStackTrace();
		}

		return (message != null ? message.toString() : EMPTY_MESSAGE);
	}

	public static void logStackTrace(Throwable t) {
	  StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  t.printStackTrace(pw);

	  try {
	    logger.error(sw.getBuffer().toString());
	  } catch (Exception e) {
	    System.err.println(ERROR_LABEL.concat(sw.getBuffer().toString()));
	  }
	}

	public static  void setLevel(Level level){
		logger.setLevel(level);
	}

	public static  Level getLevel(){
		return logger.getLevel();
	}
}
