package com.hp.cdsplus.pmloader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.hp.cdsplus.pmloader.exception.LoaderException;
import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;
import com.hp.cdsplus.pmloader.item.WorkItem;
import com.hp.cdsplus.pmloader.queue.QueueManager;
import com.hp.cdsplus.utils.JDBCUtils;

/**
 * @author kashyaks
 * 
 * @version $Revision: 1.0 $
 */
public enum Level {
	PRODUCT_NUMBER(System.getProperty(PMLoaderConstants.SKU_ALL), System
			.getProperty(PMLoaderConstants.SKU_DELTA), System
			.getProperty(PMLoaderConstants.SKU_HIERARCHY), System
			.getProperty(PMLoaderConstants.SKU_CONTENT), "PRODUCT_KY",
			"PRODUCT_NUMBER_OID"),

	PRODUCT_NAME(System.getProperty(PMLoaderConstants.PROD_MODEL_ALL), System
			.getProperty(PMLoaderConstants.PROD_MODEL_DELTA), System
			.getProperty(PMLoaderConstants.PROD_MODEL_HIERARCHY), System
			.getProperty(PMLoaderConstants.PROD_MODEL_CONTENT),
			"PRODUCT_MODEL_KY", "PRODUCT_NAME_OID"),

	PRODUCT_SERIES(System.getProperty(PMLoaderConstants.PROD_SERIES_ALL),
			System.getProperty(PMLoaderConstants.PROD_SERIES_DELTA), System
					.getProperty(PMLoaderConstants.PROD_SERIES_HIERARCHY),
			System.getProperty(PMLoaderConstants.PROD_SERIES_CONTENT),
			"PRODUCT_SERIES_KY", "PRODUCT_SERIES_OID"),

	PRODUCT_BIGSERIES(System.getProperty(PMLoaderConstants.BIG_SERIES_ALL),
			System.getProperty(PMLoaderConstants.BIG_SERIES_DELTA), System
					.getProperty(PMLoaderConstants.BIG_SERIES_HIERARCHY),
			System.getProperty(PMLoaderConstants.BIG_SERIES_CONTENT),
			"PRODUCT_BIGSERIES_KY", "PRODUCT_BIGSERIES_OID"),

	SUPPORT_SUBCATEGORY(System.getProperty(PMLoaderConstants.SUP_SUB_CAT_ALL),
			System.getProperty(PMLoaderConstants.SUP_SUB_CAT_DELTA), System
					.getProperty(PMLoaderConstants.SUP_SUB_CAT_HIERARCHY),
			System.getProperty(PMLoaderConstants.SUP_SUB_CAT_CONTENT),
			"SPT_PRODUCT_SUBCAT_KY", "SUPPORT_SUBCATEGORY_OID"),

	SUPPORT_CATEGORY(System.getProperty(PMLoaderConstants.SUPPORT_CAT_ALL),
			System.getProperty(PMLoaderConstants.SUPPORT_CAT_DELTA), System
					.getProperty(PMLoaderConstants.SUPPORT_CAT_HIERARCHY),
			System.getProperty(PMLoaderConstants.SUPPORT_CAT_CONTENT),
			"SPT_PRODUCT_CAT_KY", "SUPPORT_CATEGORY_OID"),

	MARKETING_SUBCATEGORY(System
			.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_ALL), System
			.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_DELTA), System
			.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_HIERARCHY), System
			.getProperty(PMLoaderConstants.MARKETING_SUB_CAT_CONTENT),
			"MKTG_PRODUCT_SUBCAT_KY", "MARKETING_SUBCATEGORY_OID"),

	MARKETING_CATEGORY(System.getProperty(PMLoaderConstants.MARKETING_CAT_ALL),
			System.getProperty(PMLoaderConstants.MARKETING_CAT_DELTA), System
					.getProperty(PMLoaderConstants.MARKETING_CAT_HIERARCHY),
			System.getProperty(PMLoaderConstants.MARKETING_CAT_CONTENT),
			"MKTG_PRODUCT_CAT_KY", "MARKETING_CATEGORY_OID"),

	PRODUCT_TYPE(System.getProperty(PMLoaderConstants.PRODUCT_TYPE_ALL), System
			.getProperty(PMLoaderConstants.PRODUCT_TYPE_DELTA), System
			.getProperty(PMLoaderConstants.PRODUCT_TYPE_HIERARCHY), System
			.getProperty(PMLoaderConstants.PRODUCT_TYPE_CONTENT),
			"PRODUCT_TYPE_KY", "PRODUCT_TYPE_OID");

	private String delta_load_key;
	private String full_load_key;
	private String fullLoadSql;
	private String deltaLoadSQL;
	private String contentSQL;
	private String hierarchySQL;
	final Logger logger = Logger.getLogger(Level.class);

	/**
	 * Constructor for Level.
	 * 
	 * @param fullLoadQry
	 *            String
	 * @param deltaQry
	 *            String
	 * @param viewQry
	 *            String
	 * @param nodeQry
	 *            String
	 * @param delta_key
	 *            String
	 * @param full_load_ky
	 *            String
	 */
	private Level(String fullLoadQry, String deltaQry, String viewQry,
			String nodeQry, String delta_key, String full_load_ky) {
		this.fullLoadSql = fullLoadQry;
		this.deltaLoadSQL = deltaQry;
		this.hierarchySQL = viewQry;
		this.contentSQL = nodeQry;
		this.delta_load_key = delta_key;
		this.full_load_key = full_load_ky;
	}

	/**
	 * Method getTreeType.
	 * 
	 * @return String
	 */
	public String getTreeType() {
		switch (this) {
		case MARKETING_SUBCATEGORY:
		case MARKETING_CATEGORY:
			return "marketing";
		default:
			return "support";
		}
	}

	/**
	 * Method getDeltaQry.
	 * 
	 * @return String
	 */
	public String getDeltaQry() {
		return deltaLoadSQL;
	}

	/**
	 * Method getHierarchySQL.
	 * 
	 * @return String
	 */
	public String getHierarchySQL() {
		return this.hierarchySQL;
	}

	/**
	 * Method getContentSQL.
	 * 
	 * @return String
	 */
	public String getContentSQL() {
		return this.contentSQL;
	}

	/**
	 * Method getDeltaLoadKey.
	 * 
	 * @return String
	 */
	public String getDeltaLoadKey() {
		return delta_load_key;
	}

	/**
	 * Method getFullLoadKey.
	 * 
	 * @return String
	 */
	public String getFullLoadKey() {
		return full_load_key;
	}

	/**
	 * This method is called while performing Delta Load
	 * 
	 * @param queueMgr
	 * @param dateRange
	 * 
	 * 
	 * 
	 * @throws LoaderInitializationException
	 *             * @throws SQLException * @throws Exception * @throws
	 *             LoaderException
	 */
	public void process(QueueManager queueMgr, Date[] dateRange)
			throws SQLException, LoaderException {

		String sql = this.getDeltaQry();
		try {
			PMasterLoader.isNull(this.name() + " SQL query", sql);
		} catch (LoaderInitializationException e) {
			throw new LoaderException(e);
		}

		if (dateRange.length < 2) {
			logger.error("Both Start time and End time has not been specified for Delta load to be executed. Switching to Full Load");
			this.process(queueMgr);
			return;

		} else if (dateRange.length >= 2) {
			if (dateRange[0] == null) {
				logger.error("Invalid start time provided for delta load. Switching to Full Load");
				this.process(queueMgr);
				return;
			} else if (dateRange[1] == null) {
				logger.error("Invalid end time provided for delta load. Switching to Full Load");
				this.process(queueMgr);
				return;
			}

			Connection connection = JDBCUtils.getConnection();

			PreparedStatement pstmt = connection.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			// setting start and end time for modified_timestamp fields in the
			// query
			pstmt.setDate(1, new java.sql.Date(dateRange[0].getTime()));
			pstmt.setDate(2, new java.sql.Date(dateRange[1].getTime()));
			// setting start and end timestamps for insert_time fields in the
			// query
			pstmt.setDate(3, new java.sql.Date(dateRange[0].getTime()));
			pstmt.setDate(4, new java.sql.Date(dateRange[1].getTime()));

			ResultSet resultset = pstmt.executeQuery();
			
			int record_count = 0;
			while (resultset.next()) {
				String oid = resultset.getString(this.getDeltaLoadKey());
				if (oid == null | "".equals(oid)) {
					logger.error("Skipping record since mandatory field OID is missing (level="
							+ this.name() + ")");
					continue;
				}
				queueMgr.push(new WorkItem(oid, this));
				record_count++;
			}
			logger.info("Finished extraction for level - " + this.name()
					+ ". Delta count - " + record_count);
			resultset.close();
			pstmt.close();
			connection.close();
		}
	}

	/**
	 * This method is called while performing Full Load
	 * 
	 * 
	 * 
	 * @param queueManager
	 *            QueueManager
	 * 
	 * 
	 *            * @throws SQLException * @throws LoaderException
	 */
	public void process(QueueManager queueManager) throws SQLException,
			LoaderException {
		if (queueManager == null)
			throw new LoaderException("QueueManager Object not initialized");
		String sql = this.getFullLoadSql();
		final Logger logger = Logger.getLogger(Level.class);
		logger.debug(sql);

		Connection connection = JDBCUtils.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet resultset = pstmt.executeQuery(sql);
		int record_count = 0;
		while (resultset.next() && !queueManager.isExit()) {
			String fullLoadKey = resultset.getString(this.getFullLoadKey());
			
			if(fullLoadKey != null && fullLoadKey != ""){
				queueManager.push(new WorkItem(fullLoadKey, this));
			}else{
				logger.info(" Null Oid found while processing at "+ this.name());
			}
			record_count++;
		}
		logger.info(this.name() + " - " + record_count);
		resultset.close();
		pstmt.close();
		connection.close();

	}

	/**
	 * This method is called while performing Republish
	 * 
	 * @param oid
	 * @param queue
	 */
	public void process(String oid, QueueManager queue) {
		System.out.println("Needs to be implemented");
	}

	/**
	 * 
	 * @return the fullLoadSql
	 */
	public String getFullLoadSql() {
		return fullLoadSql;
	}
}