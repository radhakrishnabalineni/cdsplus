package com.hp.cdsplus.pmloader.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.pmloader.Level;
import com.hp.cdsplus.pmloader.PMLoaderConstants;
import com.hp.cdsplus.pmloader.PMasterLoader;
import com.hp.cdsplus.pmloader.exception.LoaderException;
import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;
import com.hp.cdsplus.utils.JDBCUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class WorkItem implements PMLoaderConstants {

	protected static final Logger logger = Logger.getLogger(WorkItem.class);
	private String oid;
	private Level level;
	private DBObject query = null;
	private long contentLoadTime;
	private long hierarchyLoadTime;
	private long processTime;
	private boolean isFatalException = false;
	private boolean isDelete = false;
	
	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	private ProductMasterDAO pmasterDao;
	/**
	 * Constructor for WorkItem.
	 * @param oid String
	 * @param level Level
	 */
	public WorkItem(String oid , Level level) {
		this.oid = oid;
		this.level = level;
		this.query = new BasicDBObject("_id",this.oid);
		pmasterDao = new ProductMasterDAO();
	}
	
	/**
	 * Constructor for WorkItem.
	 * @param oid String
	 * @param level Level
	 */
	public WorkItem(String oid , Level level, boolean isDelete) {
		this.oid = oid;
		this.level = level;
		this.isDelete = isDelete;
		this.query = new BasicDBObject("_id",this.oid);
		pmasterDao = new ProductMasterDAO();
	}

	/**
	
	
	
	 * @throws SQLException  * @throws LoaderException * @throws LoaderException * @throws LoaderException
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public void loadContent() throws SQLException, LoaderException, OptionsException, MongoUtilsException {
		DBObject updatedRecord = null;
		String sql = this.getLevel().getContentSQL();
		
		//check for validity of SQL queries
		if (sql == null || "".equals(sql)){
			throw new LoaderException("Cannot find the SQL query to retrieve content information for "+this.getLevel()+" level.");
		}
		
		logger.debug("sql query : "+sql.replace("?", this.oid));
		
		// try to query the data from pmaster database for given oid
		Connection connection = JDBCUtils.getConnection();
		
		PreparedStatement pstmt = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		switch(this.level){
		case PRODUCT_TYPE:
		case SUPPORT_CATEGORY:
		case SUPPORT_SUBCATEGORY:
		case MARKETING_CATEGORY:
		case MARKETING_SUBCATEGORY:
		case PRODUCT_BIGSERIES:
		case PRODUCT_SERIES:
			pstmt.setString(1, this.oid);
			pstmt.setString(2, this.oid);
			pstmt.setString(3, this.oid);
			pstmt.setString(4, this.oid);
			pstmt.setString(5, this.oid);
			pstmt.setString(6, this.oid);
			pstmt.setString(7, this.oid);
			pstmt.setString(8, this.oid);
			pstmt.setString(9, this.oid);
			pstmt.setString(10, this.oid);
			pstmt.setString(11, this.oid);
			pstmt.setString(12, this.oid);
			pstmt.setString(13, this.oid);
			break;
		case PRODUCT_NAME:
			pstmt.setString(1, this.oid);
			pstmt.setString(2, this.oid);
			pstmt.setString(3, this.oid);
			pstmt.setString(4, this.oid);
			pstmt.setString(5, this.oid);
			pstmt.setString(6, this.oid);
			pstmt.setString(7, this.oid);
			pstmt.setString(8, this.oid);
			pstmt.setString(9, this.oid);
			pstmt.setString(10, this.oid);
			pstmt.setString(11, this.oid);
			pstmt.setString(12, this.oid);
			pstmt.setString(13, this.oid);
			pstmt.setString(14, this.oid);
			pstmt.setString(15, this.oid);
			pstmt.setString(16, this.oid);
			pstmt.setString(17, this.oid);
			pstmt.setString(18, this.oid);
			pstmt.setString(19, this.oid);
			pstmt.setString(20, this.oid);
			pstmt.setString(21, this.oid);
			pstmt.setString(22, this.oid);
			pstmt.setString(23, this.oid);
			pstmt.setString(24, this.oid);
			pstmt.setString(25, this.oid);
			break;
		case PRODUCT_NUMBER:
			pstmt.setString(1, this.oid);
			break;

		default:
			break;
		}
		
		ResultSet result = pstmt.executeQuery();
		ResultSetMetaData metaRS = pstmt.getMetaData();
		// iterate through the result set and save it to cdsplus database
		while (result.next()){
			updatedRecord = new BasicDBObject();
			BasicDBList names = new BasicDBList();
			BasicDBList long_names = new BasicDBList();
			//convert the oracle ResultSet object to a mongodb DBObject
			for (int index = 1; index <= metaRS.getColumnCount(); index ++){
				String key =  metaRS.getColumnName(index);			
				String value = result.getString(key);
				String lang=null, cc=null;
				
				if(key.equalsIgnoreCase("ENUSNAME")){
					lang = key.substring(0,2);
					cc =key.substring(2,4);
					DBObject name = new BasicDBObject();
					name.put("lang", lang);
					name.put("cc", cc);
					name.put("name", value);
					names.add(name);
				} else if (key.equalsIgnoreCase("BRPTNAME")
						|| key.equalsIgnoreCase("CNZHNAME")
						|| key.equalsIgnoreCase("CZCSNAME")
						|| key.equalsIgnoreCase("DEDENAME")
						|| key.equalsIgnoreCase("ESESNAME")
						|| key.equalsIgnoreCase("FRFRNAME")
						|| key.equalsIgnoreCase("ITITNAME")
						|| key.equalsIgnoreCase("JPJANAME")
						|| key.equalsIgnoreCase("KRKONAME")
						|| key.equalsIgnoreCase("NLNLNAME")
						|| key.equalsIgnoreCase("SESVNAME")
						|| key.equalsIgnoreCase("TWZHNAME")) 
				{
					 cc = key.substring(0,2);
					 lang =key.substring(2,4);
					 DBObject name = new BasicDBObject();
						name.put("lang", lang);
						name.put("cc", cc);
						name.put("name", value);
						names.add(name);
				}else if(key.equalsIgnoreCase("ENUSNAMEDESC")){
					lang = key.substring(0,2);
					cc =key.substring(2,4);
					DBObject long_name = new BasicDBObject();
					long_name.put("lang", lang);
					long_name.put("cc", cc);
					long_name.put("name", value);
					long_names.add(long_name);
				} else if (key.equalsIgnoreCase("BRPTNAMEDESC")
						|| key.equalsIgnoreCase("CNZHNAMEDESC")
						|| key.equalsIgnoreCase("CZCSNAMEDESC")
						|| key.equalsIgnoreCase("DEDENAMEDESC")
						|| key.equalsIgnoreCase("ESESNAMEDESC")
						|| key.equalsIgnoreCase("FRFRNAMEDESC")
						|| key.equalsIgnoreCase("ITITNAMEDESC")
						|| key.equalsIgnoreCase("JPJANAMEDESC")
						|| key.equalsIgnoreCase("KRKONAMEDESC")
						|| key.equalsIgnoreCase("NLNLNAMEDESC")
						|| key.equalsIgnoreCase("SESVNAMEDESC")
						|| key.equalsIgnoreCase("TWZHNAMEDESC"))
				{
					 cc = key.substring(0,2);
					 lang =key.substring(2,4);
					 DBObject long_name = new BasicDBObject();
					 long_name.put("lang", lang);
						long_name.put("cc", cc);
						long_name.put("name", value);
						long_names.add(long_name);
				}else{
					updatedRecord.put(key, value);
				}
				if (names.size() > 0){
					updatedRecord.put("name", names);
				}
				if(long_names.size() > 0){
					updatedRecord.put("long_name", long_names);
				}
				
			}
			// check if the DBObject has any data populated before inserting to cdsplus db
			updatedRecord.put("_id", this.oid);
			updatedRecord.put("last_modified", System.currentTimeMillis());
			updatedRecord.put("hierarchy_level",this.level.name());
			updatedRecord.put("node_type", this.level.getTreeType());
			// upsert the record to content collection
			Options options = new Options();
			options.setContentType("productmaster");
			options.setQuery(query);
			
			//SMO:User Story #7850 setting Company_info from pPMasterLoader 
			options.setCompany(PMasterLoader.company_info);
			
			options.setMetadataDocument(updatedRecord);
			//System.out.println(updatedRecord);
			pmasterDao.updateContent(options);
			
		}
		result.close();
		pstmt.close();
		connection.close();	
	}

	/**
	
	
	
	 * @throws SQLException  * @throws LoaderException
	 * @throws OptionsException 
	 * @throws MongoUtilsException 
	 */
	public void loadHierarchy() throws SQLException, LoaderException, MongoUtilsException, OptionsException {
		DBObject updatedRecord = new BasicDBObject();
		HashMap<String,TreeSet<String>> hierarchyMap = new HashMap<String,TreeSet<String>>();
		
		String sql = this.getLevel().getHierarchySQL();
		
		if(sql == null || "".equals(sql)){
			throw new LoaderException("hierarchy sql not defined for "+this.level.name()+" level");
		}
		
		logger.debug("sql query : "+sql.replace("?", this.oid));
		
		Connection connection = JDBCUtils.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);	
		pstmt.setString(1, this.oid);
		ResultSet result = pstmt.executeQuery();
		ResultSetMetaData metaRS = pstmt.getMetaData();
		
		for (int index = 1; index <= metaRS.getColumnCount(); index ++){
			hierarchyMap.put( metaRS.getColumnName(index), new TreeSet<String>());
		}
		
		while (result.next()){	
			for (int index = 1; index <= metaRS.getColumnCount(); index ++){
				String key =  metaRS.getColumnName(index);			
				String value = result.getString(key);
				if(value==null ||"".equals(value)){
					continue;
				}
				hierarchyMap.get(key).add(value);
			}
		}
		/* CR372 - commenting out since this will be handled in processor. 
		 * if(hierarchyMap.containsKey("PRODUCT_LINE_CODE")){
			updatedRecord.put("PRODUCT_LINE_CODE",hierarchyMap.remove("PRODUCT_LINE_CODE"));
		}*/
		//System.out.println(updatedRecord);
		StringBuffer buffer = new StringBuffer();
		for(Entry<String, TreeSet<String>> entry: hierarchyMap.entrySet()){
				buffer.setLength(0);
				for(String oid : entry.getValue()){
					if(buffer.length() ==0) 
						buffer.append(oid);
					else
						buffer.append(",").append(oid);
				}
				updatedRecord.put(entry.getKey(), buffer.toString());	
		}
		updatedRecord.put("_id", this.oid);
		updatedRecord.put("hierarchy_level", this.level.name());
		//ALM CR 220 - key has been changed in the following statement instead of "treeType". 
		updatedRecord.put("node_type", this.level.getTreeType());
		//ALM CR 220 - ends here
		updatedRecord.put("last_modified", System.currentTimeMillis());
		//ALM CR - 224 - old_one is one of the nodes to be available in all the records. Hence adding it to the record here.
		updatedRecord.put("old_one","top");
		//ALM CR 224 - Ends Here
		// upsert the record to hierarchy history collection
		Options options = new Options();
		//System.out.println(updatedRecord);
		options.setContentType("productmaster");
		options.setQuery(query);
		
		//SMO:User Story #7850 setting Company_info from pPMasterLoader 
		options.setCompany(PMasterLoader.company_info);
		options.setMetadataDocument(updatedRecord);
		
		
		pmasterDao.updateHierarchy(options);
		
		
		result.close();
		pstmt.close();
		connection.close();
		
	}

	//setters and getters.
	/**
	
	 * @return the oid */
	public String getOid() {
		return oid;
	}

	/**
	
	 * @return the level */
	public Level getLevel() {
		return level;
	}


	public void process(){
		if(this.oid == null){
			logger.warn("null oid found at level - "+this.level.toString());
			return;
		}
		long process_start = System.currentTimeMillis();
		
			if(this.isDelete){
				try {
					this.deleteContent();
				} catch (MongoUtilsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to delete Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (OptionsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to delete Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				}
				try {
					this.deleteHierarchy();
				} catch (MongoUtilsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to delete hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (OptionsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to delete hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				}
			}else{
				try {
					long start_time = System.currentTimeMillis();
					this.loadContent();
					// time taken by the worker to finish loading content to cds+ db
					contentLoadTime = (System.currentTimeMillis() - start_time);
				} catch (SQLException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (LoaderException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (OptionsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (MongoUtilsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load Content for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				}
		
		
				try{
					long start_time = System.currentTimeMillis();
					this.loadHierarchy();
					// time taken by the worker thread to finish loading hierarchy to cds+ db
					hierarchyLoadTime = (System.currentTimeMillis() - start_time);
				} catch (SQLException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (LoaderException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
					setFatal(true);
				} catch (MongoUtilsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				} catch (OptionsException e) {
					setFatal(true);
					e.printStackTrace();
					logger.error("Failed to load hierarchy for "+getOid()+ ". Error - "+ e.getMessage());
					logger.error(e);
				}
			}
		
		
		this.processTime = System.currentTimeMillis() - process_start;	
	}

	private void deleteHierarchy() throws MongoUtilsException, OptionsException {
		Options options = new Options();
		
		options.setContentType("productmaster");
		options.setDocid(this.oid);
		//SMO:User Story #7850 setting Company_info from pPMasterLoader 
		options.setCompany(PMasterLoader.company_info);
	
		pmasterDao.deleteHierarchy(options);
	}

	private void deleteContent() throws MongoUtilsException, OptionsException {
		Options options = new Options();
		
		options.setContentType("productmaster");
		options.setDocid(this.oid);
		
		//SMO:User Story #7850 setting Company_info from pPMasterLoader 
		options.setCompany(PMasterLoader.company_info);
		
		pmasterDao.deleteContent(options);
		
	}

	/**
	 * @return the content_load_time
	 */
	public long getContentLoadTime() {
		return contentLoadTime;
	}

	/**
	 * @return the hierarchy_load_time
	 */
	public long getHierarchyLoadTime() {
		return hierarchyLoadTime;
	}

	/**
	 * @return the processTime
	 */
	public long getProcessTime() {
		return processTime;
	}

	/**
	 * @return the isFatalException
	 */
	public boolean isFatal() {
		return isFatalException;
	}

	/**
	 * @param isFatalException the isFatalException to set
	 */
	public void setFatal(boolean isFatalException) {
		this.isFatalException = isFatalException;
	}
	
	public static void main(String[] args){
		System.setProperty("LOG4J_LOCATION", "config/log4j.xml");
		System.setProperty("CONFIG_LOCATION","config/pmloader.properties");
		try {
			new PMasterLoader();
		} catch (LoaderInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WorkItem item = new WorkItem("444841", Level.MARKETING_CATEGORY);
		try {
			//System.out.println("start loading content");
			///item.loadContent();
			//System.out.println("done loading content");
			System.out.println("start loading hierarchy");
			item.loadHierarchy();
			System.out.println("done loading hierarchy");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
