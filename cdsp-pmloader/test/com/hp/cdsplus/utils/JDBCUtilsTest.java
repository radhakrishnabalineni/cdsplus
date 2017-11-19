package com.hp.cdsplus.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.cdsplus.pmloader.exception.LoaderInitializationException;

/**
 * The class <code>JDBCUtilsTest</code> contains tests for the class <code>{@link JDBCUtils}</code>.
 *
 * @generatedBy CodePro at 6/10/13 7:15 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class JDBCUtilsTest {
	/**
	 * Initialize a newly create test instance to have the given name.
	 *
	 * @param name the name of the test
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	public JDBCUtilsTest() {
	}

	/**
	 * Run the Connection getConnection() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testGetConnection_1()
		throws Exception {
		/*
		 * when connection pool has been initialized
		 */
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))(ADDRESS = (PROTOCOL = TCP)" +
				"(HOST = gvu0600.austin.hp.com)(PORT = 1525))" +
				" (LOAD_BALANCE = yes))(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)(RETRIES = 3)(DELAY = 15)))";
		String username = "xdmsdoc";
		String password = "Itg1-Concentra";
		try{
		JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(false);
		}
		Connection connection = JDBCUtils.getConnection();
		assertNotNull(connection);
		connection.close();
		
		
	}

	/**
	 * Run the Connection getConnection() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testGetConnection_2()
		throws Exception {
		Connection connection = null;
		/*
		 * when connection pool has not been initialized
		 */
		try{
			connection = JDBCUtils.getConnection();
		}catch(Exception e){
			if(e instanceof SQLException){
				assertTrue(true);
			}else
				assertTrue(false);
		}finally{
			connection.close();
		}
		
	}


	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_1()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = "xdmsdoc";
		String password = "Itg1-Concentra";
		try{
		JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(false);
		}
		try{
			Connection result = JDBCUtils.getConnection();
			assertNotNull(result);
			result.close();
			
		}catch(Exception e){
			assertTrue(false);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_2()
		throws Exception {
		String dbUrl = "";
		String username = "xdmsdoc";
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_3()
		throws Exception {

		String username = "";
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_4()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = "xdmsdoc";
		String password = "";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_5()
		throws Exception {
		String dbUrl = null;
		String username = "xdmsdoc";
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_6()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = null;
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_7()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = "xdmsdoc";
		String password = null;

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_8()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = "xdmsdoc";
		String password = "invalidPassword";
		
		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof SQLException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_9()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0599.austin.hp.com)" +
				"(PORT = 1525))" +
				"(ADDRESS = (PROTOCOL = TCP)(HOST = gvu0600.austin.hp.com)" +
				"(PORT = 1525)) (LOAD_BALANCE = yes))" +
				"(CONNECT_DATA =(SERVER = DEDICATED)" +
				"(SERVICE_NAME = XMLDMSI))" +
				"(FAILOVER_MODE = (TYPE = SELECT)" +
				"(METHOD = BASIC)" +
				"(RETRIES = 3)" +
				"(DELAY = 15)))";
		String username = "invalidUserName";
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof SQLException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_10()
		throws Exception {
		String dbUrl = "jdbc:oracle:thin:@localhost:1525/invalidURL";
		String username = "xdmsdoc";
		String password = "Itg1-Concentra";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof SQLException);
		}
	}

	/**
	 * Run the void init(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Test
	public void testInit_11()
		throws Exception {
		String dbUrl = "";
		String username = "";
		String password = "";

		try{
			JDBCUtils.init(dbUrl, username, password);
		}catch(Exception e){
			assertTrue(e instanceof LoaderInitializationException);
		}
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 6/10/13 7:15 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(JDBCUtilsTest.class);
	}
}