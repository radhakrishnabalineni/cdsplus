/****************************************************************************************************************
 * @(#)$Project: SOAR Project
 * @(#)$Source: com.hp.cks.soar.utils.SoarStringUtils.java
 * @(#)$Revision: $
 * @(#)$Date: Nov 10, 2008
 * @(#)$Author: mariswam
 *
 * Copyright (c) 2006 Hewlet-Packard Company
 * All Rights Reserved
 *
 ****************************************************************************************************************/
package com.hp.soar.priorityLoader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class ExtractorUtils {

    /**
     * isInteger() matches strings of type 0, 99, 22, etc.,
     */
    public static boolean isInteger(String value) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(value);

        return matcher.matches();
    }

    public static String esacapeSQL(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer source = new StringBuffer(str);
        StringBuffer target = new StringBuffer();
        char c;

        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            // double all single quotes
            if (c == '\'') {
                target.append('\'');
            }

            target.append(c);
        }

        return target.toString();
    }

    /**
     * Removes the extra spaces and replaces with the single space soar 9.3 -
     * 815*
     */
    public static String removeDoubleSpaces(String str) {
        String[] rawString = str.split(" ");
        String newStr = "";
        for (int i = 0; i < rawString.length; i++) {
            String trim = rawString[i].trim();
            if (!trim.equals("")) {
                newStr += rawString[i] + " ";
            }
        }
        newStr = newStr.trim();
        return newStr;
    }

    /**
     * Trims the query to update in the soar_extraction_Details table soar 9.3 -
     * 815 *
     */
    public static String trimSpaceForExtractorQuery(String str) {
        String newStr = removeDoubleSpaces(str);

        newStr = StringUtils.replace(newStr, "= ", "=");
        newStr = StringUtils.replace(newStr, " =", "=");
        newStr = StringUtils.replace(newStr, "( '", "('");
        newStr = StringUtils.replace(newStr, "' )", "')");
        newStr = StringUtils.replace(newStr, "' ,", "',");
        newStr = StringUtils.replace(newStr, ", '", ",'");
        return newStr;
    }

    public static String getStackTrace(Throwable t) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);

      return sw.toString().trim();
    }

    public static String getStackTrace(Throwable t, int noOfLines) {
      BufferedReader reader = null;
      String stackTrace = getStackTrace(t);
      String trace = "";
      try {
          reader = new BufferedReader(new StringReader(stackTrace));
          for (int i = 0; i < noOfLines; i++) {
              trace += reader.readLine() + "\n";
          }
      } catch (IOException e) {
          trace = e.getMessage();
      }
      return trace;
    }
}
