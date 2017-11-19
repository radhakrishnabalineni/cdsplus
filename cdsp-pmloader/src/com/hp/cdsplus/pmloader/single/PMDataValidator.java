package com.hp.cdsplus.pmloader.single;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;


import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;

import com.hp.cdsplus.pmloader.single.LoaderInitializationException;
import com.hp.cdsplus.utils.JDBCUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class PMDataValidator {
	private static final Logger logger = Logger.getLogger(PMDataValidator.class);
	Properties properties;
	public PMDataValidator() {
		properties = new Properties();
		String republishFile = System.getProperty("REPUBLISH_FILE");
		logger.info("republish file - "+republishFile);
		if(republishFile != null || ! "".equals(republishFile)){
			try {
				File file = new File(republishFile);
				if(file.exists() && file.isFile())
					properties.load(new FileReader(republishFile));
				else
					logger.error("Invalid Republish file name specified - "+file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public TreeSet<String> getPMDBRecords(Level level) throws SQLException{
		TreeSet<String> pmdbResultSet = new TreeSet<String>();

		Connection connection = JDBCUtils.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(level.getFullLoadSql());

		while (resultSet.next() ) {
			String oid = resultSet.getString(level.getFullLoadKey());
			if(oid == null || "".equals(oid)){
				logger.info(Thread.currentThread().getName()+" NULL OID FOUND IN PM Results");
				//pmdbResultSet.add("NULL OID FOUND IN PM Results");
			}else
				pmdbResultSet.add(oid);
		}
		return pmdbResultSet;
	}

	public TreeSet<String> getCDSPRecords(Level level) throws MongoUtilsException{
		TreeSet<String> cdspResultSet = new TreeSet<String>();

		DB pmDB = ConfigurationManager.getInstance().getMongoDBAuthenticated("productmaster");
		String collectionName = ConfigurationManager.getInstance().getMappingValue("productmaster", "hierarchyCollection");
		DBCollection contentCollection = pmDB.getCollection(collectionName);
		DBCursor cursor = contentCollection.find(new BasicDBObject("hierarchy_level",level.toString()));
		for(DBObject result : cursor){
			String oid = result.get("_id").toString();
			cdspResultSet.add(oid);
		}

		return cdspResultSet;
	}
	
	public TreeSet<String> getPMDeletedOids(Level level) throws MongoUtilsException, SQLException{
		
		TreeSet<String> cdspResultSet = getCDSPRecords(level);

		Connection connection = JDBCUtils.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(level.getFullLoadSql());

		while (resultSet.next() ) {
			String oid = resultSet.getString(level.getFullLoadKey());
			if(oid == null || "".equals(oid)){
				logger.info(Thread.currentThread().getName()+" NULL OID FOUND IN PM Results");
				//pmdbResultSet.add("NULL OID FOUND IN PM Results");
			}else
				cdspResultSet.remove(oid);
		}

		return  cdspResultSet;
	}
	
	
	public TreeSet<String> getPMMissingOids(Level level) throws MongoUtilsException, SQLException{
		TreeSet<String> pmdbResultSet = getPMDBRecords(level);
		DB pmDB = ConfigurationManager.getInstance().getMongoDBAuthenticated("productmaster");
		String collectionName = ConfigurationManager.getInstance().getMappingValue("productmaster", "hierarchyCollection");
		DBCollection contentCollection = pmDB.getCollection(collectionName);
		DBCursor cursor = contentCollection.find(new BasicDBObject("hierarchy_level",level.toString()));
		logger.info("pmdb record count - "+pmdbResultSet.size() + " cds+ record count - "+cursor.size());
		for(DBObject result : cursor){
			String oid = result.get("_id").toString();
			pmdbResultSet.remove(oid);
		}
		return  pmdbResultSet;
	}
	
	public TreeSet<String> getRepublishOids(Level level){
		
		TreeSet<String> republishSet =  new TreeSet<String>();
		if(properties !=null){
			String oidS = properties.getProperty(level.toString());
			if(oidS != null || !"".equals(oidS)){
				republishSet.addAll(Arrays.asList(oidS.split(",")));
			}
		}
		
		return republishSet;
	}
	public static void validate(Level level){
		PMDataValidator validator = new PMDataValidator();
		try {
			//System.out.println("total "+ level + " oids Deleted from PMaster "+validator.getPMDeletedOids(level).size());
			System.out.println("total "+ level + " oids present in Pmaster (missing in CDS+) "+validator.getPMMissingOids(level).size());
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]){
		System.setProperty("LOG4J_LOCATION","config/log4j.xml");
		System.setProperty("CONFIG_LOCATION","config/pmloader.properties");

		try {
			new PMasterLoader();
		} catch (LoaderInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.hp.cdsplus.pmloader.exception.LoaderInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(final Level level : Level.values()){
			Thread it = new Thread(new Runnable() {
				
				@Override
				public void run() {
					validate(level);
				}
			});
			it.start();
			
		}
	}
}
