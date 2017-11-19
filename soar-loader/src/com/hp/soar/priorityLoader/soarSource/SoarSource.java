/**
 * 
 */
package com.hp.soar.priorityLoader.soarSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.loader.history.HistoryHandler;
import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.source.Source;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.workItem.RevisitWorkItem;
import com.hp.loader.workItem.IWorkItem;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.ConnectionPool;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.MappingHandler;
import com.hp.soar.priorityLoader.utils.PendingDeleter;
import com.hp.soar.priorityLoader.utils.VirusScanner;
import com.hp.soar.priorityLoader.workItem.SoarExtractCollection;
import com.hp.soar.priorityLoader.workItem.SoarExtractElement;
import com.hp.soar.priorityLoader.workItem.SoarItem;
import com.hp.soar.priorityLoader.workItem.SoarWorkItem;

/**
 * @author dahlm
 *
 */
public class SoarSource implements Source {

	private static final String SOAREVENTXML = "soar_event_xml_file";
	private static final String REVISITFILE  = "revisit_file_name";
	public static final String WORKITEMSQUERY_FILE = "workitems_query_file";
	
	//private StringBuffer getWorkItemsBuf;
	//private int getWorkItemsStartLength;
	private MappingHandler mappingHandler;
	private RevisitHistoryHandler revisit;
	
	private String workItemsQuery;
	//private Long loadBatchCount;
	private String top_result_count;

	
	public SoarSource(ConfigurationReader config) {
		ConnectionPool.init(config);
		//getWorkItemsBuf = new StringBuffer("select * from dm_dbo.soar_extractor_events where event_id > '");
		//getWorkItemsStartLength = getWorkItemsBuf.length();
		String getWorkItemsQueryFile = config.getAttribute(WORKITEMSQUERY_FILE);
		
		Properties properties = new Properties();
		
		try {
			properties.load(new FileInputStream(getWorkItemsQueryFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		workItemsQuery = properties.getProperty("findworkitemsquery");
		top_result_count= properties.getProperty("top_result_count");
		mappingHandler = new MappingHandler(config.getAttribute(SOAREVENTXML));
		
		try {
			// put all of the constant values into a workItem
			SoarExtractElement.init(config);
			
			// create and start the reference list loader
			ReferenceLists.start(config);

			// create and start the pendingDeleter
			PendingDeleter.start(config);

			// create the virus scanner
			VirusScanner.init(config);
			
			// initialize the filters for the collection and item  This has to happen after the reference lists are created
			SoarExtractCollection.init(config);
			SoarItem.init(config);

			String revisitFileName = config.getAttribute(REVISITFILE);
			String numPrioritiesStr = config.getAttribute("num_priorities");
			String eventsAreSeqStr = config.getAttribute(PriorityLoader.EVENTS_ARE_SEQUENTIAL);
			if (eventsAreSeqStr == null) {
				throw new IllegalArgumentException("Missing "+PriorityLoader.EVENTS_ARE_SEQUENTIAL+" from config file");
			}
			// revisit for SOAR never holds revisit events, but just holds manualPublish events
			revisit = new RevisitHistoryHandler(revisitFileName, Integer.valueOf(numPrioritiesStr), PriorityLoader.getInstance().getQueueManager(), 
					Boolean.valueOf(eventsAreSeqStr), this);
		} catch (Exception e) {
			// failed to initialize.  Something drastic is wrong so abort this loader 
			
			// Start code changes by <048174> Niharika on 07-05-2013 for CR 475

			//System.out.println("Initialization FAILED " + e.getMessage());
			e.printStackTrace();
			LoaderLog.error("Initialization FAILED : " + e.getMessage());
			LoaderLog.info("Error Occured : Exiting from loader");
			
			// End code changes by <048174> Niharika on 07-05-2013 for CR 475

			System.exit(2);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#getWorkItemsSince(java.lang.Long)
	 */
	public ArrayList<IWorkItem> getWorkItemsSince(Long startToken)
			throws ProcessingException {
		ArrayList<IWorkItem> workItems = new ArrayList<IWorkItem>();
		try {
			// get session
			IDfSession session = ConnectionPool.getDocbaseSession();
			//getWorkItemsBuf.setLength(getWorkItemsStartLength);
			//getWorkItemsBuf.append(startToken).append("' order by event_id enable(RETURN_TOP 1000)");

			String workItemsQueryReplaced= workItemsQuery.replace("STARTTOKEN",startToken.toString()).concat(" ").concat(top_result_count);
			//LoaderLog.debug("Query Formed New : "+workItemsQueryReplaced);
			//LoaderLog.debug("Query Formed Existing : "+getWorkItemsBuf.toString());
			IDfCollection results = null;
			try {
				//results = DocbaseUtils.executeQuery(session, getWorkItemsBuf.toString(), IDfQuery.READ_QUERY, "getWorkItems");
				results = DocbaseUtils.executeQuery(session, workItemsQueryReplaced, IDfQuery.READ_QUERY, "getWorkItems");
				while(results.next()) {
					// get an event
					Long eventId = results.getLong("event_id");
					String event = results.getString("event");
					String collectionId = results.getString("collection_id");
					String chronicleId = results.getString("chronicle_id");
					String objectId = results.getString("object_id");
					workItems.add(new SoarWorkItem(eventId, mappingHandler.getPriority(event), 
							mappingHandler.getExtractorEvent(event), collectionId, chronicleId, objectId));
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
		} catch (DfException e) {
			throw new ProcessingException("Failed to get docbase session",e,true);
		}
		
		// add any revisit workItems
		revisit.addWorkItems(startToken, workItems);
		try {
			HashMap<String , Object> logMap = new HashMap<String, Object>();
			logMap.put("Total workorder Items", workItems.size());
			logMap.put("lastmodified", System.currentTimeMillis());
			DiagnosticLogger.setEnabled(true);
			DiagnosticLogger.log("soarloader", logMap);
		} catch (MongoUtilsException e) {
			//just eat this exception to avoid any unwanted things in loader so that this will be taken care by healthcheck.
		}

		return workItems;
	}

	
	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#hasSideLoader()
	 */
	public boolean hasSideLoader() {
		// TODO Auto-generated method stub
		return false;
	}

	
	private void setupEventTable(String eventMapFileName) throws IllegalArgumentException {
		if (eventMapFileName == null) {
			throw new IllegalArgumentException(SOAREVENTXML+" is not defined in config file");
		}
		File f = new File(eventMapFileName);
		if (!f.exists() || !f.canRead()) {
			throw new IllegalArgumentException(SOAREVENTXML+" file doesn't exist or is not readable");
		}
		
	}

	/**
	 * exiting indicates that the loader is exiting.
	 * Need to tell ConnectionPool to exit
	 */
	public void exiting() {
		// Notification is given that we are attempting to exit
		ConnectionPool.exiting();
	}

	/**
	   * loadItems is to be overloaded by classes that extend this handler
	   * @throws IOException
	 * @throws ProcessingException 
	   */
	public ArrayList<RevisitWorkItem> loadRevisitWorkItems(FileInputStream fis, RevisitHistoryHandler historyHandler) throws ProcessingException {
		Scanner scanner = new Scanner(fis);
		ArrayList<RevisitWorkItem> revisitItems = new ArrayList<RevisitWorkItem>();
		try {
			while(scanner.hasNextLine()){
				String[] sVals = scanner.nextLine().split(" ");
				
				ArrayList<String> vals = new ArrayList<String>();
				for(String s : sVals) {
					vals.add(s);
				}
				Iterator<String> vIt = vals.iterator();
				IWorkItem workItem = new SoarWorkItem(vIt);
				Long timeStamp = Long.valueOf(vIt.next());
				
				RevisitWorkItem loadedItem = new RevisitWorkItem(timeStamp, workItem, historyHandler);
				revisitItems.add(loadedItem);
			}
		} finally {
			scanner.close();		;
		}
		return revisitItems;
	}
}
