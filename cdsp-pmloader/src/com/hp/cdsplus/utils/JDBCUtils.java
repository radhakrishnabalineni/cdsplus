package com.hp.cdsplus.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hp.cdsplus.pmloader.PMasterLoader;
import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class JDBCUtils {

	private static String url;
	private static String username;
	private static String password;

	/**
	 * Method init.
	
	 * @param dbUrl String
	
	
	
	 * @param uname String
	 * @param passwd String
	 * @throws SQLException  * @throws LoaderInitializationException * @throws LoaderInitializationException
	 */
	public static final void init(String dbUrl, String uname, String passwd) throws SQLException, LoaderInitializationException {
		url = dbUrl;
		username = uname;
		password = passwd;
		
		PMasterLoader.isNull("JDBC_URL", url);
		PMasterLoader.isNull("DB_USERNAME",username);
		PMasterLoader.isNull("DB_PASSWORD", password);
		
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		
		validateDBConnection();
		
	}



	// ~ Methods
	// ----------------------------------------------------------------

	private static void validateDBConnection() throws SQLException {
		Connection connection = getConnection();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select sysdate from dual");
		if(rs.next()){
			System.out.println("Concentra DB authentication successful");
		}
		rs.close();
		rs = stmt.executeQuery("SELECT DISTINCT PRODUCT_TYPE_OID FROM LUXOR.PATH_RELATIONSHIP_ALL_US_EN@LUXOR_PMASTER_LINK.AUSTIN.HP.COM");
		if(rs.next()){
			System.out.println("Pmaster DBlink authentication successful");
			
		}
		rs.close();
		stmt.close();
		connection.close();
		
	}



	/**
	 * Method getConnection.
	
	
	
	 * @return Connection * @throws SQLException * @throws SQLException * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}
	

}
