/****************************************************************************************************************
 * @(#)$Project: SOAR Project
 * @(#)$Source: com.hp.cks.soar.services.SoarDBService.java
 * @(#)$Revision: 1.19 $
 * @(#)$Date: Aug 6, 2008
 * @(#)$Author: mariswam
 *
 * Copyright 2001 Hewlett-Packard/TMC
 *
 ****************************************************************************************************************/
package com.hp.soar.priorityLoader.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;

public class SoarDBService {

	public static final String JAVA_DATE_FORMAT_FOR_DFC = "yyyy/MM/dd HH:mm:ss";
	public static final String DFC_DATE_FORMAT = "yyyy/mm/dd hh:mi:ss";

	public static int BULK_UPDATE_LIMIT = 100;
	public static final String DCTM_DATETIME_FORMAT = "yyyy/mm/dd hh:mi:ss";
	protected IDfSession session = null;
	protected IDfCollection results = null;
	private static Logger logger = null;

	public SoarDBService() {

	}

	public SoarDBService(IDfSession session) {
		this.session = session;
		logger = Logger.getLogger(this.getClass());
	}

	public List getAsDistinctList(String dql, String attributeName, String methodName) throws DfException {
		List attValList = new ArrayList();
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, this.getClass().getName() + " : "
					+ methodName);
			if (results != null) {
				while (results.next()) {
					String val = results.getString(attributeName); 
					if (!attValList.contains(val)) {
						attValList.add(val);
					}
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return attValList;
	}
	
	public List getAsList(String dql, String attributeName, String methodName) throws DfException {
		List attValList = new ArrayList();
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, this.getClass().getName() + " : "
					+ methodName);
			if (results != null) {
				while (results.next()) {
					attValList.add(results.getString(attributeName));
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return attValList;
	}

	public Map getAsMap(String dql, String keyColumn, String valueColumn, String methodName) throws DfException {
		Map attValList = new HashMap();
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, this.getClass().getName() + " : "
					+ methodName);
			if (results != null) {
				while (results.next()) {
					attValList.put(results.getString(keyColumn), results.getString(valueColumn));
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return attValList;
	}

	public String getColValueAsString(String dql, String colName, String methodName) throws DfException {
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, this.getClass().getName() + " : "
					+ methodName);
			if (results != null) {
				if (results.next()) {
					return results.getString(colName);
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return null;
	}

	public Date getColValueAsDate(String dqlQuery, String columnName, String methodName) throws DfException {
		try {
			results = DocbaseUtils.executeQuery(session, dqlQuery, IDfQuery.DF_READ_QUERY, this.getClass().getName()
					+ ": " + methodName);
			if (results != null && results.next()) {
				return results.getTime(columnName).getDate();
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return null;
	}

	public int getColValueAsInt(String dql, String colName, String methodName) throws DfException {
		// SessionLog.logInfo(methodName+"-->Executing select query "+dql,
		// session, this.getClass().getName());
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, this.getClass().getName() + " : "
					+ methodName);
			if (results != null) {
				if (results.next()) {
					String count = results.getString(colName);
					return (new Integer(count)).intValue();
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return 0;
	}

	public int executeUpdate(String dqlQuery, String methodName) throws DfException {
		int rowsAffected = executeUpdateQuery(dqlQuery, methodName);
		logInfo(rowsAffected + " records got updated ");
		return rowsAffected;
	}

	public int executeDelete(String dqlQuery, String methodName) throws DfException  {
		int rowsAffected = executeUpdateQuery(dqlQuery, methodName);
		logInfo(rowsAffected + " records got deleted ");
		return rowsAffected;
	}

	private int executeUpdateQuery(String dqlQuery, String methodName) throws DfException {
		try {
			results = DocbaseUtils.executeQuery(session, dqlQuery, IDfQuery.DF_READ_QUERY, this.getClass().getName()
					+ " : " + methodName);
			if (results != null && results.next()) {
				return results.getValueAt(0).asInteger();
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}
		return 0;
	}

	protected String getDateString(Date date) {
		DfTime dfcTime = getDfcTime(date);
		return "date('" + dfcTime + "', '" + DCTM_DATETIME_FORMAT + "')";
	}

	public IDfSession getSession () {
		return session;
	}
	
	static public DfTime dfcTime() {
		return getDfcTime(new Date());
	}

	static protected DfTime getDfcTime(Date date) {
		DateFormat dfcNiceDate = new SimpleDateFormat(JAVA_DATE_FORMAT_FOR_DFC);
		String formattedDate = dfcNiceDate.format(date);
		return new DfTime(formattedDate, DFC_DATE_FORMAT);
	}

	protected void logError(String message) {
		logger.error(message);
	}

	protected void logInfo(String message) {
		logger.info(message);
	}

	protected String unescapeSingleQuotes(String target) {
		String resultString = "";
		int nPos;

		resultString = target;
		nPos = resultString.indexOf("&#39;");
		while (nPos >= 0) {
			resultString = resultString.substring(0, nPos) + "'" + resultString.substring(nPos + 5);
			nPos = resultString.indexOf("&#39;");
		}
		return resultString;
	}

}