/**
 * 
 */
package com.hp.concentra.extractor.concentraSource;

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
import com.documentum.fc.common.DfException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.core.session.SessionException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.DocbaseUtils;
import com.hp.concentra.PreparedDQL;
import com.hp.concentra.extractor.utils.ExtractorSessionPool;
import com.hp.concentra.extractor.utils.MappingHandler;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;
import com.hp.concentra.extractor.workItem.ConcentraWorkItem;
import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.source.Source;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.RevisitWorkItem;


/**
 * @author dahlm
 *
 */
public class SingleConcentraSource implements Source {

	private static final String CONCENTRAEVENTXML = "concentra_event_xml_file";
	private static final String REVISITFILE  = "revisit_file_name";
	public static final String WORKITEMSQUERY_FILE = "workitems_query_file";


	/*private StringBuffer getWorkItemsBuf;
	private int getWorkItemsStartLength;*/
	/*private StringBuffer getArchFlagBuf;
	private int getArchFlagStartLength;*/
	private MappingHandler mappingHandler;
	private RevisitHistoryHandler revisit;
	
	private String workItemsQuery;
	private String archiveQuery;
	private Long loadBatchCount;

	public SingleConcentraSource(ConfigurationReader config) {
		ExtractorSessionPool.init(config);
		
		String getWorkItemsQueryFile = config.getAttribute(WORKITEMSQUERY_FILE);
		
		Properties properties = new Properties();
		
		try {
			properties.load(new FileInputStream(getWorkItemsQueryFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*System.out.println("findworkitemsquery:"+properties.getProperty("findworkitemsquery"));
		System.out.println("findarchivequery:"+properties.getProperty("findarchivequery"));*/
		workItemsQuery = properties.getProperty("findworkitemsquery");
		archiveQuery= properties.getProperty("findarchivequery");
		loadBatchCount= Long.valueOf(properties.getProperty("loadBatchCount"));
		
		/*getWorkItemsBuf = new StringBuffer("select * from dm_dbo.c_extractor_events where event_id > '");
		getWorkItemsStartLength = getWorkItemsBuf.length();

		getArchFlagBuf = new StringBuffer("select r_object_id, archived_flag from c_base_object where r_object_id in ");
		getArchFlagBuf.append("(select object_id from dm_dbo.c_extractor_events where event_id > '");
		getArchFlagStartLength = getArchFlagBuf.length();*/

		mappingHandler = new MappingHandler(config.getAttribute(CONCENTRAEVENTXML));

		try {
			// put all of the constant values into a workItem
			ConcentraExtractElement.init(config);

			// initialize the DQL strings
			PreparedDQL.init(config);

			String revisitFileName = config.getAttribute(REVISITFILE);
			String numPrioritiesStr = config.getAttribute("num_priorities");
			String eventsAreSeqStr = config.getAttribute(PriorityLoader.EVENTS_ARE_SEQUENTIAL);
			if (eventsAreSeqStr == null) {
				throw new IllegalArgumentException("Missing "+PriorityLoader.EVENTS_ARE_SEQUENTIAL+" from config file");
			}

			// ConcentraLoader only has Manual updates in there is no revisit entries
			revisit = new RevisitHistoryHandler(revisitFileName, Integer.valueOf(numPrioritiesStr), PriorityLoader.getInstance().getQueueManager(),
					Boolean.valueOf(eventsAreSeqStr), this);


		} catch (Exception e) {
			// failed to initialize.  Something drastic is wrong so abort this loader i
			//System.out.println("Initialization FAILED" + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}


	}

	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#getWorkItemsSince(java.lang.Long)
	 */
	public ArrayList<IWorkItem> getWorkItemsSince(Long startToken) throws ProcessingException {
		ArrayList<IWorkItem> workItems = new ArrayList<IWorkItem>();
		Long endToken = startToken + loadBatchCount;
		HashMap<String, String> archMap = new HashMap<String, String>();
		try {
			// get session
			ConcentraAdminSession session = ExtractorSessionPool.getDocbaseSession();
			/*getWorkItemsBuf.setLength(getWorkItemsStartLength);
			getWorkItemsBuf.append(startToken).append("' and event_id <= '").append(endToken).append("' and object_type='c_library_doc' order by event_id");*/
			String workItemsQueryReplaced= workItemsQuery.replace("STARTTOKEN",startToken.toString()).replace("ENDTOKEN",endToken.toString());
			System.out.println("#################################################");
			System.out.println("workItemsQueryReplaced : "+workItemsQueryReplaced);
			System.out.println("#################################################");
			String archiveQueryReplaced= archiveQuery.replace("STARTTOKEN",startToken.toString()).replace("ENDTOKEN",endToken.toString());
			System.out.println("#################################################");
			System.out.println("archiveQueryReplaced : "+archiveQueryReplaced);
			System.out.println("#################################################");
			System.out.println("workItemsQuery:"+workItemsQueryReplaced);
			System.out.println("archiveQuery:"+archiveQueryReplaced);

			/*getArchFlagBuf.setLength(getArchFlagStartLength);
			getArchFlagBuf.append(startToken).append("' and event_id <= '").append(endToken).append("')");*/

			IDfCollection results = null;
			try {
				//results = DocbaseUtils.executeQuery(session, getArchFlagBuf.toString(), IDfQuery.READ_QUERY, "getArchFlags");
				results = DocbaseUtils.executeQuery(session, archiveQueryReplaced, IDfQuery.READ_QUERY, "getArchFlags");
				while(results.next()) {
					archMap.put(results.getString("r_object_id"), results.getString("archived_flag"));
				}
				// drop the collection
				DocbaseUtils.closeCollection(results);
				
				// now get the events
				//results = DocbaseUtils.executeQuery(session, getWorkItemsBuf.toString(), IDfQuery.READ_QUERY, "getWorkItems");
				results = DocbaseUtils.executeQuery(session, workItemsQueryReplaced, IDfQuery.READ_QUERY, "getWorkItems");
				
				while(results.next()) {
					// get an event
					Long eventId = results.getLong("event_id");
					/*System.out.println("eventId:"+eventId);*/
					String event = results.getString("event");
					String objectName = results.getString("object_name");
					String chronicleId = results.getString("chronicle_id");
					String objectId = results.getString("object_id");
					String object_type = results.getString("object_type");
					
					// see if this event needs to be set to delete as the doc is archived
					boolean isDeleted = (archMap.containsKey(objectId) && "1".equals(archMap.get(objectId)));
					
					// add every item so no holes in the events.  
					// If an item is bad, (not extractable), it will not be valid
					workItems.add(new ConcentraWorkItem(eventId, mappingHandler.getPriority(event), 
							mappingHandler.getExtractorEvent(event), objectName, chronicleId, objectId, object_type, isDeleted));
				}
			} catch (DfException e) {
				throw new ProcessingException("Failed to get results for workItem ",e,true);
			} catch (DmRepositoryException e) {
				throw new ProcessingException("Failed to get workItems ", e, true);
			} finally {
				DocbaseUtils.closeCollection(results);
				ExtractorSessionPool.releaseDocbaseSession(session);
			}
		} catch (SessionException e) {
			throw new ProcessingException("Failed to get docbase session", e, true);
		}
		// add any revisit workItems
		revisit.addWorkItems(startToken, workItems);
		
		//Writing to mongo 'diagnostics' db 'transactions' collection for healthcheck reasons.
		
		try {
			HashMap<String , Object> logMap = new HashMap<String, Object>();
			logMap.put("Total workorder Items", workItems.size());
			logMap.put("lastmodified", System.currentTimeMillis());
			DiagnosticLogger.setEnabled(true);
			DiagnosticLogger.log("concentraloader", logMap);
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

	public void exiting() {
		// ConcentraLoader has no dependencies on exit

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
				IWorkItem workItem = new ConcentraWorkItem(vIt);
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
