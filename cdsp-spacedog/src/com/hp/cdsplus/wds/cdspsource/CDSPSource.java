package com.hp.cdsplus.wds.cdspsource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.wds.constants.IConstants;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.workItem.CDSPSubscription;
import com.hp.cdsplus.wds.workItem.CDSPWorkItem;
import com.hp.loader.history.HistoryException;
import com.hp.loader.history.HistoryHandler;
import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.source.Source;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.RevisitWorkItem;

/**
 * @author kashyaks
 *
 */
public class CDSPSource implements Source {
	//Start code changes by <048174> Niharika on 20-06-2013 for CDSP Re-Design
	
	//RD: Configuration Reader for wdsClient
	ConfigurationReader wdsClientConfig;
	
	//Re-Design :  in batch mode, this flag indicates when to stop loading from the main Source (CDS+)
	boolean loadBatch = true;
	
	// subscriptions being serviced by this source
	private HashMap<String, CDSPSubscription> cDSPSubscriptions = new HashMap<String, CDSPSubscription>();


	/**
	 * Constructor for CDSPSource
	 * It takes wdsClient File and creates a new Configuration Reader
	 *  
	 * @param configReader
	 * @throws ProcessingException 
	 * @throws Exception
	 */
	public CDSPSource(ConfigurationReader priorityLoaderConfig)  {
		try {
		
		//RD: Number of Priorities  This was check for validity in the PriorityLoader so it will work here
		int numPriorities = Integer.parseInt(priorityLoaderConfig.getAttribute(PriorityLoader.NUM_PRIORITIES));
		
		CDSPSubscription.eventsAreSeq = Boolean.valueOf(priorityLoaderConfig.getAttribute(PriorityLoader.EVENTS_ARE_SEQUENTIAL));
				
		//RD: Get wdsClient Configuration File Path
		String wdsClientConfigFile = priorityLoaderConfig.getAttribute(IConstants.WDS_CLIENT);
		//RD: Validate the wdsClientCOnfig file accesibilty
		if(wdsClientConfigFile !=null && wdsClientConfigFile!=""){
			File configFile = new File(wdsClientConfigFile);
			if (!configFile.canRead()) {
				System.out.println(configFile+ " is not readable.");
				System.exit(1);
			}
			wdsClientConfig = new ConfigurationReader(wdsClientConfigFile);
		} else {
			System.out.println("Can't find configuration "+wdsClientConfigFile+" on classpath.");
			System.exit(1);
		}
			// The revisit files should be in the same location as the history file
			String historyFileString = priorityLoaderConfig.getAttribute(PriorityLoader.HISTORY_FILE_NAME);
			File historyFile = new File(historyFileString);
			//RD: Load subscriptions specified in wdsClientConfig
			loadSubscriptions(historyFile.getParentFile(), wdsClientConfig, numPriorities);
			ThreadLog.info("CDSPSource intialized successfully");
		} catch (Exception e) {
			// failed to initialize.  Something drastic is wrong so abort this loader i
			System.out.println("Initialization FAILED\n" + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
	}

	
	
	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#getWorkItemsSince(java.lang.Long)
	 */
	public ArrayList<IWorkItem> getWorkItemsSince(Long since) throws ProcessingException {
		long start = System.currentTimeMillis();
		ArrayList<IWorkItem> workItemsList = new ArrayList<IWorkItem>();
		// since is the timestamp of the last workItem loaded.  After in CDS+ includes this time so it
		// needs to be incremented by 1 to not include the last event
		since = since + 1;
		// for every subscription
		for (Map.Entry<String, CDSPSubscription> e: cDSPSubscriptions.entrySet()){
			CDSPSubscription sub = (CDSPSubscription)e.getValue();
			sub.addWorkItems(since, workItemsList, loadBatch);
		}
		
		ThreadLog.info("getWorkItemsSince complete <" + (System.currentTimeMillis() - start) + ">");
		// turn off source load when no results are returned and leave it off
		if(PriorityLoader.batchMode){
			loadBatch = loadBatch && (workItemsList.size() > 0);
		}
		return workItemsList;
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#hasSideLoader()
	 */
	public boolean hasSideLoader() {
		// by setting this true we are making sure that 
		//priority loader identifies Manual republish 
		//and process revisit requests.
		return true;
	}

	/**
	 * @param configReader
	 * @param revisitErrorItems TODO
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws DestinationException 
	 * @throws HistoryException 
	 * @throws IOException 
	 * @throws ProcessingException 
	 */
	private void loadSubscriptions(File revisitDir, ConfigurationReader wdsClientConfig, int numPriorities) throws IOException, HistoryException, DestinationException, ClassNotFoundException, 
																							InstantiationException, IllegalAccessException, ProcessingException {
		ThreadLog.info("CDSPSource:loadSubscription");
		Element subscription = null;
		String cdsp_sub_name = null;
		CDSPSubscription cdspSubscription = null;
		
		ThreadLog.debug("Loading subscriptions.");
		defineCDSPNamespace(wdsClientConfig);
		
		NodeList subscriptions = wdsClientConfig.getElements(IConstants.SUBSCRIPTION_CONFIG_ELE_NAME);
		
		for (int index = 0; index < subscriptions.getLength(); index++) {
			subscription = (Element) subscriptions.item(index);
			cdsp_sub_name = XMLUtils.nullCheck(IConstants.SUBSCRIPTION_ATTR_NAME,
					XMLUtils.getAttributeValue(subscription, IConstants.SUBSCRIPTION_ATTR_NAME));
			cdspSubscription = new CDSPSubscription(subscription);
			cDSPSubscriptions.put(cdspSubscription.getSubscriptionName(), cdspSubscription);
			// load the revisits for the subscription
			cdspSubscription.loadRevisits(revisitDir, numPriorities, this);
		}

		ThreadLog.info("Subscriptions loaded successfully");
	}
	//End code changes by <048174> Niharika on 20-06-2013 for CDSP Redesign- Date Filter	
	/**
	 * @param configReader
	 */
	private void defineCDSPNamespace(ConfigurationReader wdsClientConfig) {
		ThreadLog.info("CDSPSource:defineCDSPNamespace");
		XMLUtils.defineNamespace(IConstants.PROJ_NS_KEY,wdsClientConfig.getAttribute(IConstants.PROJ_NS_VALUE));
		XMLUtils.defineNamespace(IConstants.SD_NS_KEY,wdsClientConfig.getAttribute(IConstants.SD_NS_VALUE));
		XMLUtils.defineNamespace(IConstants.XLINK_NS_KEY,wdsClientConfig.getAttribute(IConstants.XLINK_NS_VALUE));
		XMLUtils.defineNamespace(IConstants.SUB_NS_KEY,wdsClientConfig.getAttribute(IConstants.SUB_NS_VALUE));
		XMLUtils.defineNamespace(IConstants.SEL_NS_KEY,wdsClientConfig.getAttribute(IConstants.SEL_NS_VALUE));
	}
	
	/* (non-Javadoc)
	 * @see com.hp.loader.source.Source#exiting()
	 */
	public void exiting() {

	}

	/**
	   * loadItems is to be overloaded by classes that extend this handler
	   * @throws IOException
	 * @throws ProcessingException 
	   */
	public ArrayList<RevisitWorkItem> loadRevisitWorkItems(FileInputStream fis, RevisitHistoryHandler historyHandler) throws ProcessingException {
		Scanner scanner = new Scanner(fis);
		ArrayList<RevisitWorkItem> revisitItems = new ArrayList<RevisitWorkItem>();
		ArrayList<String> failedEntries = new ArrayList<String>();
		try {
			while(scanner.hasNextLine()){
				String entry = scanner.nextLine();
				String[] sVals = entry.split(" ");
				
				ArrayList<String> vals = new ArrayList<String>();
				for(String s : sVals) {
					if (s != null && s.trim().length() > 0) {
						vals.add(s);
					}
				}
				if (vals.size() == 0) {
					// nothing in this line
					continue;
				}
				try {
					Iterator<String> vIt = vals.iterator();
					IWorkItem workItem = new CDSPWorkItem(vIt, cDSPSubscriptions);
					Long timeStamp = Long.valueOf(vIt.next());
					
					RevisitWorkItem loadedItem = new RevisitWorkItem(timeStamp, workItem, historyHandler);
					revisitItems.add(loadedItem);
					
				} catch (ProcessingException ie) {
					failedEntries.add(entry+" "+ie.getMessage());
				}
			}
		} finally {
			scanner.close();		;
		}
		
		if (failedEntries.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for(String s : failedEntries) {
				sb.append(s).append("\n");
			}
			throw new ProcessingException(sb.toString());
		}
		
		return revisitItems;
	}
}