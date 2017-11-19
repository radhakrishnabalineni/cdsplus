/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.utils.Log;
import com.hp.soar.priorityLoader.utils.ConnectionPool;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;


/**
 * @author dahlm
 *
 */
public class ReferenceLists implements Runnable {
	
	private static final String REFERENCE_LIST_HISTORY_FILE = "reflist_history_file_name";
	private static final String REFERENCE_LIST_UPDATE_SECONDS = "reference_list_update_seconds";
	private static final String REFTABLEDEFS = "reference_table_definitions";

	private static final String SOAR_SERVER_FEED = "soar_server_feeds";
	private static final String SOAR_SERVER_FEED_CP = "soar_server_feeds.cp";
	
	// commands to load each map

//	private static final String BOM_CHECKSUMS_DQL			= "select checksum_type_oid as val, checksum_type as label from dm_dbo.soar_bom_checksums order by checksum_type_oid";		
//	private static final String COMPRESSION_UTILTIES_DQL		= "select compression_utility_oid as val, compression_utility as label from dm_dbo.soar_compression_utilities order by 1";
//	private static final String CONTACT_NAMES_DQL 			= "select group_name as val, contact_name as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String CONTACT_NAMES2_DQL 			= "select group_name as val, contact_name_2 as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String CONTACT_PHONES_DQL 			= "select group_name as val, contact_phone as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String CONTACT_PHONES2_DQL 		= "select group_name as val, contact_phone_2 as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String CONTACT_EMAILS_DQL 			= "select group_name as val, contact_email as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String CONTACT_EMAILS2_DQL 		= "select group_name as val, contact_email_2 as label from dm_dbo.soar_submittal_groups order by 1";
//	private static final String DISCLOSURE_LEVELS_DQL		= "select disclosure_level_oid as val, disclosure_level as label from dm_dbo.soar_disclosure_levels order by 1";		
//	private static final String DOCUMENT_TYPES_DQL			= "select document_type_oid as val, document_type as label from dm_dbo.soar_document_types order by 1";		
//	private static final String DRIVER_MODEL_DQL			= "select driver_model_oid as val, driver_model_name as label from dm_dbo.soar_driver_models order by 1";		
//	private static final String REGIONS_DQL				= "select region_oid as val, region_description as label from dm_dbo.soar_regions order by 1";		
//	private static final String ENVIRONMENTS_DQL			= "select environments_details_oid as val, environments as label from dm_dbo.soar_environments order by 1";		
//	private static final String ENVIRONMENT_DETAILS_DQL 	  	= "select environments_details_oid as val, environments_details_short as label from dm_dbo.soar_environments order by 1";
//	private static final String ENVIRONMENT_MAPPINGS_DQL	  	= "select environments_details_oid as val, pm_oid as label from dm_dbo.soar_environments order by 1";		
//	private static final String FILE_TYPES_DQL		  	= "select file_type_oid as val, file_type as label from dm_dbo.soar_file_types order by 1";		
//	private static final String FULFILLMENT_METHODS_DQL	  	= "select fulfillment_method_oid as val, fulfillment_method as label from dm_dbo.soar_fulfillment_methods order by 1";		
//	private static final String DRIVER_INSTALLATION_METHOD_DQL	= "select install_method_oid as val, install_method as label from dm_dbo.soar_install_methods order by 1";		
//	private static final String INSTALL_FORMAT_DQL			= "select install_format_oid as val, install_format as label from dm_dbo.soar_install_formats order by 1";		
//	private static final String BOM_LEVELS_DQL			= "select level_oid as val,level_name as label from dm_dbo.soar_bom_levels order by level_oid";		
//	private static final String MEDIA_TYPES_DQL			= "select media_type_oid as val, media_type as label from dm_dbo.soar_media_types order by 1";		
//	private static final String NOTIFICATION_TYPES_DQL 		= "select notification_type_oid as val, notification_type as label from dm_dbo.soar_notification_types order by 1";
//	private static final String ORDER_LINKS_DQL			= "select order_link_oid as val, order_link_url as label from dm_dbo.soar_order_links order by order_link_oid";		
//	private static final String CURRENCIES_DQL			= "select currency_code as val, currency_description as label from dm_dbo.soar_currencies order by 1";		
//	private static final String PRICE_TYPES_DQL			= "select price_type_oid as val, price_type as label from dm_dbo.soar_price_types order by 1";		
//	private static final String PRODUCT_FAMILY_OIDS_DQL 		= "select product_number_name as val, support_subcategory_oid as label from dm_dbo.product_master_view order by 1";
//	private static final String PRODUCT_NAME_FAMILIES_DQL 		= "select product_number_name as val, support_subcategory_name as label from dm_dbo.product_master_view order by 1";
//	private static final String PRODUCT_NAME_NAMES_DQL 		= "select product_number_name as val, product_name_name as label from dm_dbo.product_master_view order by 1";
//	private static final String PRODUCT_NAME_OIDS_DQL 		= "select product_number_name as val, product_name_oid as label from dm_dbo.product_master_view order by 1";
//	private static final String PROJECT_NAMES_DQL			= "select project_oid as val, project_name as label from dm_dbo.soar_project_names order by project_oid";		
//	private static final String RELATIONSHIP_TYPES_DQL		= "select relationship_type_oid as val, relationship_type as label from dm_dbo.soar_relationship_types order by 1";		
//	private static final String SEVERITIES_DQL			= "select severity_oid as val, severity as label from dm_dbo.soar_severities order by 1";		
//	private static final String SERVER_FEEDS_DQL			= "select server_feed_oid as val, server_name as label from dm_dbo.soar_server_feeds order by 1";		
//	private static final String SERVER_PROTOCOLS_DQL		= "select server_feed_oid as val, protocol as label from dm_dbo.soar_server_feeds order by 1";		
//	private static final String LANGUAGE_CHAR_SETS_DQL		= "select language_oid as val, character_set as label from dm_dbo.soar_languages order by 1";		
//	private static final String LANGUAGE_ISO_CODES_DQL		= "select language_oid as val, language_iso as label from dm_dbo.soar_languages order by 1";		
//	private static final String LANGUAGE_DQL			= "select language_oid as val, soar_language as label from dm_dbo.soar_languages order by 1";		
//	private static final String LOCAL_LANGUAGE_CHAR_SETS_DQL	= "select language_oid as val, local_character_set as label from dm_dbo.soar_languages order by 1";		
//	private static final String LOCAL_LANGUAGE_DQL			= "select language_oid as val, local_language as label from dm_dbo.soar_languages order by 1";		
//	private static final String PRODUCT_LINE_CODE_DQL 		= "select group_name as val, product_line_code as label from dm_dbo.soar_submittal_groups order by 2";
////	private static final String SOFTWARE_FILE_TYPES_DQL 		= "select file_type_oid as val, file_type as label from dm_dbo.soar_file_types where displayed = true order by 1";
//	private static final String SOFTWARE_INSTALL_TYPES_DQL 		= "select software_install_type_oid as val, software_install_type as label from dm_dbo.soar_software_install_types order by 1";
//	private static final String SOFTWARE_SUB_TYPES_DQL		= "select software_subtype_oid as val, software_subtype as label from dm_dbo.soar_software_subtypes order by 1";		
//	private static final String SOFTWARE_TYPES_DQL			= "select software_type_oid as val, software_type as label from dm_dbo.soar_software_types order by 1";		
//	private static final String SUBMITTAL_GROUPS_DQL 		= "select group_name as val, description as label from dm_dbo.soar_submittal_groups order by 2";
//	private static final String SUBMITTAL_PRIORITIES_DQL		= "select priority_oid as val, priority_label as label from dm_dbo.soar_submittal_priorities order by 1 desc";		
//	private static final String SUBMITTAL_TYPES_DQL 		= "select submittal_type_oid as val, submittal_type as label from dm_dbo.soar_submittal_types order by 2";
//	private static final String UPDATE_TYPES_DQL			= "select update_type_oid as val, update_type as label from dm_dbo.soar_update_types order by 1";		

	// table names
	public static final String BOM_CHECKSUMS_LIST 			= "bom_checksums";			
	public static final String COMPRESSION_UTILTIES_LIST = "compression_utilties";	
	public static final String CONTACT_NAMES_LIST			= "contact_names"; 			
	public static final String CONTACT_NAMES2_LIST			= "contact_names2"; 				
	public static final String CONTACT_PHONES_LIST			= "contact_phones"; 				
	public static final String CONTACT_PHONES2_LIST			= "contact_phones2"; 				
	public static final String CONTACT_EMAILS_LIST			= "contact_emails"; 				
	public static final String CONTACT_EMAILS2_LIST			= "contact_emails2"; 				
	public static final String DISCLOSURE_LEVELS_LIST		= "disclosure_levels";				
	public static final String DOCUMENT_TYPES_LIST			= "document_types";			
	public static final String DRIVER_MODEL_LIST			= "driver_model";			
	public static final String REGIONS_LIST				= "regions";					
	public static final String ENVIRONMENTS_LIST			= "environments";			
	public static final String ENVIRONMENTS_DETAILS_LIST		= "environments_details"; 		
	public static final String ENVIRONMENTS_DETAILS_SHORT_LIST	= "environments_details_short"; 		
	public static final String ENVIRONMENT_MAPPINGS_LIST		= "environment_mappings";			
	public static final String FILE_TYPES_LIST			= "file_types";					
	public static final String FULFILLMENT_METHODS_LIST		= "fulfillment_methods";			
	public static final String DRIVER_INSTALLATION_METHOD_LIST	= "driver_installation_method";			
	public static final String INSTALL_FORMAT_LIST			= "install_format";			
	public static final String BOM_LEVELS_LIST			= "bom_levels";					
	public static final String MEDIA_TYPES_LIST			= "media_types";			
	public static final String MIN_SERVICE_RELEASES_LIST	= "min_service_releases";			
	public static final String NOTIFICATION_TYPES_LIST		= "notification_types"; 			
	public static final String ORDER_LINKS_LIST			= "order_links";				
	public static final String CURRENCIES_LIST			= "currencies";					
	public static final String PRICE_TYPES_LIST			= "price_types";				
	public static final String PRODUCT_FAMILY_OIDS_LIST		= "product_family_oids"; 		
	public static final String PRODUCT_NAME_FAMILIES_LIST		= "product_name_families"; 		
	public static final String PRODUCT_NAME_NAMES_LIST		= "product_name_names"; 			
	public static final String PRODUCT_NAME_OIDS_LIST		= "product_name_oids"; 				
	public static final String PROJECT_NAMES_LIST				= "projects";					
	public static final String RELATIONSHIP_TYPES_LIST		= "relationship_types";				
	public static final String SEVERITIES_LIST			= "severities";					
	public static final String SERVER_FEEDS_LIST			= "server_feeds";			
	public static final String SERVER_PROTOCOLS_LIST			= "server_protocols";				
	public static final String LANGUAGE_CHAR_SETS_LIST		= "language_char_sets";				
	public static final String LANGUAGE_ISO_CODES_LIST		= "language_iso_codes";				
	public static final String LANGUAGE_LIST				= "language";					
	public static final String LOCAL_LANGUAGE_CHAR_SETS_LIST		= "local_language_char_sets";			
	public static final String LOCAL_LANGUAGE_LIST			= "local_language";			
	public static final String PRODUCT_LINE_CODE_LIST		= "product_line_code"; 				
//	public static final String SOFTWARE_FILE_TYPES_LIST		= "software_file_types";		
	public static final String SOFTWARE_INSTALL_TYPES_LIST		= "software_install_types"; 			
	public static final String SOFTWARE_SUB_TYPES_LIST		= "software_sub_types";				
	public static final String SOFTWARE_TYPES_LIST			= "software_types";			
	public static final String SUBMITTAL_GROUPS_LIST			= "submittal_groups"; 				
	public static final String SUBMITTAL_PRIORITIES_LIST		= "submittal_priorities";		
	public static final String SUBMITTAL_TYPES_LIST			= "submittal_types"; 				
	public static final String UPDATE_TYPES_LIST			= "update_types";			

  
	// Singleton that holds the reference lists.  This object handles the
	// updating of the lists on a timer as well as issuing the lists to requestors
	static private ReferenceLists referenceLists = null;

	private ArrayList<ReferenceFile> referenceFiles = new ArrayList<ReferenceFile>();
	
  // table name -> table for update
  private HashMap<String, ReferenceTable> refTableMap = new HashMap<String, ReferenceTable>();
  
  // Reference Lists populated during the table load
  private HashMap<String, HashMap<String,String>> refListMap = new HashMap<String, HashMap<String,String>>();
  
  private int nbTimesReloaded = 0;
  private Thread listThread;
//  private IDfSession aSession;
  private int statusMsgCount = 0;

  // file the history token is kept in
  private File historyFile;
  
  private int referenceListUpdateSeconds = 60;
  
	// last token processed
	private int	completedToken;
	
	// Token associated to the current reference lists
	private int refTableToken;
	
	private Comparator<String> tableOrderer;

  static private int nbTimesNudged = 0;

  /**
   * Constructor for the ReferenceLists object  
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws IllegalArgumentException 
   * @throws DocumentException 
   * @throws DfException 
   */
  public ReferenceLists(ConfigurationReader config) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, DocumentException, DfException {
  	tableOrderer = new Comparator<String>() {
  		/* (non-Javadoc)
  		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
  		 */
  		public int compare(String o1, String o2) {
  			// get the dependancies and order the two tables
  			ReferenceTable t1 = ReferenceLists.getRefTable(o1);
  			ReferenceTable t2 = ReferenceLists.getRefTable(o2);
  			if (t1 == null || t2 == null) {
  				return 0;
  			}
  			if (t1.dependsOn(t2)) {
  				return -1;
  			} else if (t2.dependsOn(t1)) {
  				return 1;
  			} else {
  				return 0;
  			}
  		}
  	};
 
  	// get the last event/timestamp when the lists were loaded,
  	String historyFileName = config.getAttribute(REFERENCE_LIST_HISTORY_FILE);
  	historyFile = new File(historyFileName);
		if (!historyFile.exists() || !historyFile.canRead()) {
			throw new IllegalArgumentException("ReferenceList history file <"+historyFileName+"> does not exist or is not readable");
		} else if (!historyFile.canWrite()) {
			throw new IllegalArgumentException("ReferenceList history file <"+historyFileName+"> is not writable");
		}

		// read the file to get the tokens
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(historyFile));
			line = reader.readLine();
		} catch (IOException ioe) {
		}
		if (line == null) {
			// initial run
			completedToken = 0;
		} else {
			completedToken = Integer.parseInt(line);
		}
		
		// show that the current reference tables goes with this token
		refTableToken = completedToken;

  	// initialize the referenceFile object
  	ReferenceFile.init(config);
  	

  	// start the list monitor thread which will begin loading the lists
  	listThread = new Thread(this, "ReferenceLists");
  	// make this a daemon so that applications which use this lib will exit when the main thread exits.
  	listThread.setDaemon(true);
  }

  /**
   * loadTables reads the table definitions from the XML file and creates the table objects
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws DocumentException 
   * @throws DfException 
   * @throws ProcessingException 
   * 
   * @throws Exception
   */
  private void loadRefTables(String tableDefFileName) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, DocumentException, DfException, ProcessingException {
  	long start = System.currentTimeMillis();
    LoaderLog.info("Reference tables creation/load.");

    if (tableDefFileName == null) {
    	throw new IllegalArgumentException("Error tableDefFileName is null");
    }
    File defFile = new File(tableDefFileName);
    if (!defFile.canRead()) {
    	throw new IllegalArgumentException("Error tableDefFileName does not exist or is not readable");
    }

    SAXReader reader = new SAXReader();
    Document doc = reader.read(defFile); 
    
    Element root = doc.getRootElement();
    List<Element> refFiles = root.elements(ReferenceTableFactory.REFFILE);
    for(Element refFile : refFiles) {
    	// for each refFile definition
    	ReferenceFile rFile = new ReferenceFile(refFile.attributeValue(ReferenceFile.FILEELEMENT), refFile.attributeValue(ReferenceFile.SCHEMA));
    	referenceFiles.add(rFile);
    	List<Element> refTableDefs = refFile.elements(ReferenceTableFactory.REFTABLE);
    	// load the table
    	for(Element refTableDef : refTableDefs) {
    		ReferenceTable refTable = ReferenceTableFactory.newTable(refTableDef, rFile, referenceFiles);
    		if (refTableMap.containsKey(refTable.getTableName())) {
    			throw new IllegalArgumentException("Duplicate table specified in refTableDefs.xml file."+refTable.getTableName());
    		}
    		refTableMap.put(refTable.getTableName(), refTable);
    	}
    }
    IDfSession session = ConnectionPool.getDocbaseSession();
    ArrayList<String> tableNames = new ArrayList<String>(refTableMap.keySet());
    Collections.sort(tableNames, tableOrderer);
    try {
    	for(String refTableName : tableNames) {
    		ReferenceTable refTable = refTableMap.get(refTableName);
    		refTable.loadTable(session, refListMap, refTableMap);
    	}
    } finally {
    	ConnectionPool.releaseDocbaseSession(session);
    }

    LoaderLog.info("Reference tables loaded: "+(System.currentTimeMillis()-start)+" ms");
    
  	// store the reference list for initialization
  	if (completedToken == 0) {
  		// we are initializing
    	storeFiles();
  	}
  	
    // now clear all of the update flags as we just loaded the lists
    for(ReferenceFile f : referenceFiles) {
    	f.setUpdated(false);
    }
  }

  private void logRefTablesExtracted(int minToken, int maxToken) throws DfException {
  	IDfCollection results = null;
    IDfSession session = ConnectionPool.getDocbaseSession();
    try {
    	String dql = "update dm_dbo.soar_reference_data_log set extracted=1 where event_id >'" + minToken + "' and event_id <='"+maxToken+"'";
      results = DocbaseUtils.executeQuery(session, dql, DfQuery.EXEC_QUERY, "log reference data extracted");
    } finally {
      DocbaseUtils.closeResults(results);
      ConnectionPool.releaseDocbaseSession(session);
    }
  	
  }
  /**
   * put the updated tables in the reference map, store the tables to disk and put them on CDS+
   * @param newLists
   * @param newToken
   * @throws IOException
   * @throws DfException
   * @throws ProcessingException
   */
  private void publishTables(HashMap<String, HashMap<String,String>> newLists, int newToken) throws IOException, DfException, ProcessingException {
  	LoaderLog.info("Updating reference lists");
  	
  	// get lock on the refListMap
  	synchronized(refListMap) {
  		// swap the reference lists in
  		refListMap.putAll(newLists);
  		
  		// update the refTable token to show that the ref tables have changed.
  		refTableToken = newToken;
  	}
  	
  	// now write out the new referenceFiles
  	storeFiles();
  }

  private void storeFiles() throws DfException, IOException, ProcessingException {
  	// get the reference update time information
  	HashMap<String, Date> updateDateMap = getUpdateDateMap();
  	for(ReferenceFile f : referenceFiles) {
  		f.store(updateDateMap);
  	}
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run() {
  	int failCount = 0;
  	boolean mailSent = false;
		
  	for(;;) {
  		try {
  			// Check the lists to see if they are OK
  			int newToken = getTableUpdates();
  			if (newToken != completedToken) {
  				int minToken = completedToken;
  				int maxToken = newToken;
  				completedToken = newToken;
  				// just processed an event so update the history file
  				FileOutputStream fileHandler = new FileOutputStream(historyFile);
  				String tokStr = Integer.toString(completedToken);
  				// Now open the file and write it out
  				fileHandler.write(tokStr.getBytes());
  				fileHandler.close();
  				
  				// now update the docbase, setting the extracted fields
  				logRefTablesExtracted(minToken, maxToken);
  			}
  			
  			try {
  				// Sleep (default 60 seconds)
  				Thread.sleep(referenceListUpdateSeconds*1000);
  			} catch (InterruptedException ie) {
  			}
  			failCount=0;
  			mailSent = false;
  		} catch (Exception e) {
  			LoaderLog.error("ERROR: CALL DEV NOW.ARGGHHHH. failed to publish reference data: "+(++failCount)+"." + e.getMessage());
  			try {
  				Thread.sleep(failCount < 10 ? 1000*60 : 1000*120); //Sleep 1 minute for the first 10, then 2 minutes after, then see if you can get it again
  			} catch (InterruptedException ie) {
  			}
  			if (!mailSent) {
  				mailSent = true;
  				//TODO - Handle sending notification of problem
  				/*
  				String from =(String)PropertiesLoader.getProperty("FROM_ADDR");
  				String[] to = new String[1];
  				to[0] = PropertiesLoader.getProperty("SUPPORT_EMAIL");
  				
  				String msg = "Hi SUPPORT";
  				msg = msg + "\nReference data failed to load. Please check the xdmsdoc logs and take necessary action.";
  				msg = msg + "\nRegards,";
  				msg = msg+"\nConcentra Application";
  				
  				SendMail sendMail = new SendMail();
  				try {
  					sendMail.sendMailWithAttachment("Concentra",from,to,msg,"Reference List refresh failed.",null);
  				} catch (DmRepositoryException dme) {
  				}
  				*/
  			}
  		}
  	}
  }
    
  /**
   * lgetKey returns the key associated with a value in a reference table 
   * @param tableName	name of the reference table
   * @param value name of the value being looked up
   * @return key or null if the value isn't there
   */
  public String lgetKey(String tableName, String value) {
  	String retVal = "";
  	HashMap<String, String> list = refListMap.get(tableName);
  	if (list != null) {
  		for(String key : list.keySet()) {
  			String val = list.get(key);
  			if (value.equals(val)) {
  				return key;
  			}
  		}
  	} else {
  		retVal = "MISSING_REF_LIST-"+tableName;
  	}
  	return retVal;
  }
  
  /**
   * Get the last completed token value 
   * @return
   */
  public synchronized int lgetRefTableToken() {
	  synchronized(refTableMap) {
		  return refTableToken;
	  }
  }
  
  /**
   * lgetLabel returns the value associated with a label in a reference table 
   * @param tableName	name of the reference table
   * @param label name of the label being looked up
   * @return label or null if the label isn't there
   */
  public String lgetLabel(String tableName, String label) {
  	String retVal = "";
  	HashMap<String, String> list = refListMap.get(tableName);
  	if (list != null) {
  		retVal = list.get(label);
  	} else {
  		retVal = "MISSING_REF_LIST-"+tableName;
  	}
  	return retVal;
  }
  
  /**
   * startReferenceLoad begins the loading of the reference files and the thread running after they are loaded.
   * @param config
   * @throws IllegalArgumentException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws DocumentException
   * @throws DfException
   * @throws ProcessingException 
   */
  public void startReferenceLoad(ConfigurationReader config) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, DocumentException, DfException, ProcessingException {
	 	// get the reference table definitions file
  	String refTableDefsName = config.getAttribute(REFTABLEDEFS);
  	loadRefTables(refTableDefsName);
  	
  	// now start the listener
  	listThread.start();
  	
  	// now start the loader
  	PriorityLoader.resume();

  }
  
  /**
   * create the referenceLists and get the thread running.
   * @param config
   * @throws IllegalArgumentException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws DocumentException
   * @throws DfException
   * @throws ProcessingException 
   */
  public synchronized static void start(ConfigurationReader config) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, DocumentException, DfException, ProcessingException {
  	if (referenceLists == null) {
  		referenceLists = new ReferenceLists(config);
  		referenceLists.startReferenceLoad(config);
  	} 
  }
  
  public static int getRefTableToken() {
	  return referenceLists.lgetRefTableToken();
  }
  public static String getKey(String tableName, String value) {
	  return referenceLists.lgetKey(tableName, value);
  }
  
  public static String getLabel(String tableName, String label) {
  	return referenceLists.lgetLabel(tableName, label);
  }
  
  public ReferenceTable lgetRefTable(String tableName) {
	  // get the lock on the map and then get the list
	  synchronized (refTableMap) {
		  return refTableMap.get(tableName);
	  }
  }
  
  /**
   * get the reference table by name
   * @param tableName
   * @return
   */
  public static ReferenceTable getRefTable(String tableName) {
  	return referenceLists.lgetRefTable(tableName);
  }
  
  /**
   * getTableUpdates checks for reference updates, updates the reference lists,
   * publishes the changes and returns the last token processed
   * @param lastToken
   * @return the last token read
   * @throws DfException 
   * @throws IOException 
   * @throws ProcessingException 
   */
  private int getTableUpdates() throws DfException, IOException, ProcessingException {
    IDfCollection results = null;
    String  newCompletedToken = null;
    ArrayList <String> updatedTables = new ArrayList<String>();
    IDfSession session = ConnectionPool.getDocbaseSession();
    try {
    	String dql = "select * from dm_dbo.soar_reference_data_log where event_id >'" + completedToken + "' order by event_id";
      results = DocbaseUtils.executeQuery(session, dql, DfQuery.READ_QUERY, "get ReferenceLists events");
      
      while (results.next()) {
      	String table = results.getString("ref_table_name");
      	if (!updatedTables.contains(table)) {
      		updatedTables.add(table);
      	}
      	// if the soar_server_feeds is updated, the soar_server_feeds.cp also needs to be updated
      	if (table.equals(SOAR_SERVER_FEED)) {
      		if (!updatedTables.contains(SOAR_SERVER_FEED_CP)) {
      			updatedTables.add(SOAR_SERVER_FEED_CP);
      		}
      	}
      	
      	newCompletedToken = results.getString("event_id");
      }

    } finally {
      DocbaseUtils.closeResults(results);
      ConnectionPool.releaseDocbaseSession(session);
    }
    
    if (updatedTables.size() > 0) {
    	// sort the tables by dependency
    	Collections.sort(updatedTables, tableOrderer);
    	
    	session = ConnectionPool.getDocbaseSession();
    	HashMap<String, Date> updateDateMap = getUpdateDateMap();
    	HashMap<String, HashMap<String, String>> newLists = new HashMap<String, HashMap<String,String>>();
    	try {
    		// the lists have changed so get the new lists
    		for(String tableName : updatedTables) {
    			ReferenceTable refTable = refTableMap.get(tableName);
    			if (refTable == null) {
    				LoaderLog.error("Reference table "+tableName+" is not in the map!!!!");
    			} else {
    				refTable.loadTable(session, newLists, refTableMap);
    			}
    		}
    	} finally {
        ConnectionPool.releaseDocbaseSession(session);
    	}
    	// now publish the tables to CDS+ and locally
    	publishTables(newLists, (newCompletedToken != null ? Integer.parseInt(newCompletedToken) : completedToken));

  		statusMsgCount = 0;
  		LoaderLog.info("Updated reference list(s). LastToken: "+newCompletedToken);
    	
    } else {
    	if ((statusMsgCount - 6 < 0) || (statusMsgCount % 6 == 0)) {
    		// put out a message for every check for the first minute, then once every minute after that.
    		LoaderLog.info("Checked and we have the lists up to date");
    	}
    	statusMsgCount++;
    }
    return newCompletedToken != null ? Integer.parseInt(newCompletedToken) : completedToken;
  }

	
  /**
   * get the table_name->update_date map for the reference tables
   * @param session
   * @return
   * @throws DfException
   */
  private HashMap<String, Date> getUpdateDateMap() throws DfException {
  	String dql = "select * from dm_dbo.soar_ref_table_info";
  	HashMap<String, Date> resMap = new HashMap<String, Date>();
  	IDfCollection results = null;
  	IDfSession session = ConnectionPool.getDocbaseSession();
  	try {
  		results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, "ExtractReferenceTable : GetUpdateDate");
  		if (results != null) {
  			while (results.next()) {
  				String tableName = results.getString("table_name");
  				Date updateDate = results.getTime("update_date").getDate();
  				resMap.put(tableName, updateDate);
  			}
  		} else {
  			throw new DfException("No Reference Table Info returned!");
  		}
  	} finally {
  		DocbaseUtils.closeResults(results);
  		ConnectionPool.releaseDocbaseSession(session);
  	}
  	return resMap;
  }
}
