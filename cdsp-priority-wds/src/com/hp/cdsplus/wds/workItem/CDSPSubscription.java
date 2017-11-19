package com.hp.cdsplus.wds.workItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.cdsplus.util.Utils;
import com.hp.cdsplus.util.http.HttpNonRetryException;
import com.hp.cdsplus.util.http.HttpRetryException;
import com.hp.cdsplus.util.http.HttpUtils;
import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.wds.cdspsource.CDSPManualRepublish;
import com.hp.cdsplus.wds.constants.IConstants;
import com.hp.cdsplus.wds.destination.IDestination;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.loader.history.HistoryException;
import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.priorityLoader.ManualRepublish;
import com.hp.loader.priorityLoader.NonRecoverableException;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.priorityLoader.RecoverableException;
import com.hp.loader.source.Source;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;

public class CDSPSubscription {

	public static boolean eventsAreSeq = false;

	private File workingDir;
	
	private String server;
	private String subscription;
	private String subscriptionName;
	private int limit;
	public XPathExpression xPrevisit;
	private XPathExpression xDeleteVersion;

	private final List<CDSPSelection> cDSPSelections = new ArrayList<CDSPSelection>();

	private final Map<String, IDestination> namedDestinations = new HashMap<String, IDestination>();

	private RevisitHistoryHandler revisit;

	private CDSPManualRepublish manualRepublish;

	private Pattern datePattern;
	
	private File tempDir;
	
	//Re-Design : Start
	protected static TransformerFactory transformerFactory;
	static{
		transformerFactory = TransformerFactory.newInstance();
	}
	//Re-Design : End
	private boolean includeDeletes = true; //Re-Design : Include deletes
	/**
	 * A cDSPSubscription represents a set of content that we use to determine
	 * what content to send where. The constructor takes an Element parameter
	 * which is from the wrapping config file. Even though it is not enforced in
	 * code it assumes there will be the following elements "server" e.g.
	 * "http://cdsplus-itg.austin.hp.com" "cDSPSubscription" e.g. "soar/content"
	 * "workingDir" which is a directory where this cDSPSubscription can create
	 * temporary content and store metadata "limit" which is an integer. valid
	 * children are "selection" elements
	 * 
	 * @param subscription
	 * @throws HistoryException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws DestinationException
	 * @throws ProcessingException
	 */
	public CDSPSubscription(Element subscription) throws IOException,
			HistoryException, DestinationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, ProcessingException {
		ThreadLog.debug("Create CDSPSubscription object");
		this.server = XMLUtils.nullCheck("server",
				XMLUtils.getAttributeValue(subscription, "server"));
		// Set system property for CDSP URL
		System.setProperty("spacedog_cdsp_server_url", server);

		this.subscription = XMLUtils.nullCheck(
				IConstants.SUBSCRIPTION_ATTR_NAME, XMLUtils.getAttributeValue(
						subscription, IConstants.SUBSCRIPTION_ATTR_NAME));
		String workingDirString = XMLUtils.nullCheck("workingDir",
				XMLUtils.getAttributeValue(subscription, "workingDir"));
		String tempDirString = XMLUtils.getAttributeValue(subscription,
				"tempDir");
		// this.oldsub =
		// XMLUtils.getAttributeValue(subscription,"oldSubscription");
		String limitString = XMLUtils.nullCheck("limit",
				XMLUtils.getAttributeValue(subscription, "limit"));
		xPrevisit = XMLUtils.compileXPath("revisit",
				XMLUtils.getAttributeValue(subscription, "revisit"), null);
		xDeleteVersion = XMLUtils
				.compileXPath("/result/proj:ref/versions/proj:ref/@xlink:href");

		this.limit = Integer.parseInt(limitString);

		workingDir = new File(workingDirString);
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}

		if (XMLUtils.isNotNull(tempDirString)) {
			tempDir = new File(workingDir, tempDirString);
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
		} else
			tempDir = null;

		subscriptionName = this.subscription.replace('/', '_');
		
		//includeDeletes Configuration
		if (XMLUtils.isNotNull(XMLUtils.getAttributeValue(subscription,IConstants.INCLUDE_DELETED_DOC))) {
			includeDeletes = Boolean.parseBoolean(XMLUtils.getAttributeValue(subscription,IConstants.INCLUDE_DELETED_DOC)); 
		}
		
		// all information required at subscription level is now loaded.
		// go on to load selection level information.
		loadNamedDestinations(subscription);
		loadSelections(subscription);

		// compile the pattern matcher for the revisit date fields
		datePattern = Pattern
				.compile("(\\d*)-(\\d*)-(\\d*)T(\\d*):(\\d*):(\\d*)");

	}

	private void loadNamedDestinations(Element subscription)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, DestinationException {
		ThreadLog.debug("CDSPSubscription:loadNamedDestinations");
		Element namedDest = null;
		String name = null;
		String className = null;
		Class<?> theClass = null;
		IDestination destination = null;

		NodeList destinations = XMLUtils.getChildElements(subscription,
				IConstants.NAMED_DEST_ELE_NAME);
		if (destinations == null) {
			// no named destinations in this subscription
			return;
		}
		ThreadLog.info("Total number of named destinations - "
				+ destinations.getLength());

		for (int selection_index = 0; selection_index < destinations
				.getLength(); selection_index++) {
			namedDest = null;
			namedDest = (Element) destinations.item(selection_index);
			name = XMLUtils.getAttributeValue(namedDest,
					IConstants.NAME_ATTRIBUTE);
			className = Utils.nullCheck(IConstants.DEST_CLASS_ATTR_NAME,
					XMLUtils.getAttributeValue(namedDest,
							IConstants.DEST_CLASS_ATTR_NAME));
			theClass = Class.forName(className);
			destination = (IDestination) theClass.newInstance();
			destination.init((Element) namedDest, this.workingDir);
			namedDestinations.put(name, destination);
		}
	}

	public String getSubscriptionName() {
		return subscriptionName;
	}

	private String getMatch(Matcher m, int idx) {
		String group = m.group(idx);
		return group;
	}

	private long getEpoch(String dateStr) {
		long epoch = 0;
		Matcher m = datePattern.matcher(dateStr);
		boolean matches = m.matches();
		if (matches) {
			int numMatches = m.groupCount();
			int year = Integer.parseInt(getMatch(m, 1));
			int month = Integer.parseInt(getMatch(m, 2));
			int day = Integer.parseInt(getMatch(m, 3));
			int hour = Integer.parseInt(getMatch(m, 4));
			int min = Integer.parseInt(getMatch(m, 5));
			int sec = Integer.parseInt(getMatch(m, 6));
			GregorianCalendar cal = new GregorianCalendar(year, month - 1, day,
					hour, min, sec);
			epoch = cal.getTimeInMillis();
		}
		return epoch;
	}

	/**
	 * loadDeleteContent gets the XML for the file that is to be delete
	 * 
	 * @param workITem
	 * @throws ProcessingException
	 */
	private byte[] loadDeleteContent(CDSPWorkItem workItem)
			throws ProcessingException {
		// get the last version that was loaded
		byte[] content;
		String level = "getVersions";

		try {
			// get the versions bytes
			content = HttpUtils.download(workItem.getVersionsUrl());
			// parse the bytes to an XML doc
			Document doc = XMLUtils.parse(new ByteArrayInputStream(content));
			try {
				level = "getPreviousLoadedVersion";
				NodeList nl = (NodeList) xDeleteVersion.evaluate(doc,
						XPathConstants.NODESET);
				if (nl.getLength() > 0) {
					Node n = nl.item(0);
					String deleteRef = n.getTextContent();
					// get the metadata bytes
					content = HttpUtils.download(workItem
							.getDeleteUrl(deleteRef));
					return content;
				} else {
					ThreadLog.info("Unable to find previous load for "
							+ workItem.getIdentifier() + " ignore delete.");
					//workItem.setValid(false); //Commented to allow Spacedog to send metadata singal for deleted document
					return null;
				}
			} catch (XPathExpressionException e) {
				throw new NonRecoverableException("XpathExpression failure ", e);
			}

		} catch (MalformedURLException e) {
			throw new NonRecoverableException(
					"Failed getting Deleted metadata " + level
							+ " - MalformedURL ", e);
		} catch (HttpRetryException e) {
			throw new RecoverableException(e);
		} catch (HttpNonRetryException e) {
			/*throw new NonRecoverableException(
					"Failed getting Deleted metadata " + level
							+ " - HTTPNonRetry ", e);*/
			return null;
		} catch (SAXException e) {
			throw new NonRecoverableException(
					"Failed getting Deleted metadata " + level + " - SAX ", e);
		} catch (IOException e) {
			throw new NonRecoverableException(
					"Failed getting Deleted metadata " + level + " - IO ", e);
		} catch (ParserConfigurationException e) {
			throw new NonRecoverableException(
					"Failed getting Deleted metadata " + level
							+ " - ParserConfiguration ", e);
		}
	}
	

	/**
	 * load performs load phase for workItem
	 * 
	 * @param workItem
	 * @throws ProcessingException
	 */
	public void load(CDSPWorkItem workItem) throws ProcessingException {
		byte[] content;
		try {
			if (workItem.getEventType() == IWorkItem.EVENT_DELETE) {
					content = loadDeleteContent(workItem);
				if(content==null){
					// now see which selections
					for (CDSPSelection selection : cDSPSelections) {
						selection.loadDelete(workItem);
					}
					return;
				}
				if (!workItem.isValid()) {
					return;
				}
			} else {
					content = HttpUtils.download(workItem.getUrl());
			}
		} catch (MalformedURLException e) {
			throw new NonRecoverableException(
					"CDSPSubscription:loadUpdate download ", e);
		} catch (HttpRetryException e) {
			throw new RecoverableException(e);
		} catch (HttpNonRetryException e) {
			String msg = e.getMessage();
			if (msg != null) {
				// This is a 404 error. Don't need a stack trace
				throw new NonRecoverableException(msg);
			} else {
				throw new NonRecoverableException(
						"CDSPSubscription:loadUpdate download ", e);
			}
		} 
		Document doc = null;
		try {
				doc = XMLUtils.parse(new ByteArrayInputStream(content));
			
		} catch (SAXException e) {
			throw new NonRecoverableException(
					"CDSPSubscription:loadUpdate setupRevisit ", e);
		} catch (IOException e) {
			throw new NonRecoverableException(
					"CDSPSubscription:loadUpdate setupRevisit ", e);
		} catch (ParserConfigurationException e) {
			throw new NonRecoverableException(
					"CDSPSubscription:loadUpdate setupRevisit ", e);
		}
		// see if this subscription wants to revisit this file
		if ((workItem.getEventType() != IWorkItem.EVENT_DELETE)
				&& xPrevisit != null) {
			try {
				NodeList nl = (NodeList) xPrevisit.evaluate(doc,
						XPathConstants.NODESET);
				long firstEpoch = 0;
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					String dateStr = n.getTextContent();
					long newEpoch = getEpoch(dateStr);
					if (newEpoch > System.currentTimeMillis()) {
						if (firstEpoch == 0 || newEpoch < firstEpoch) {
							firstEpoch = newEpoch;
						}
					}
				}
				if (firstEpoch != 0) {
					// need to revisit this item
					revisit.addRevisit(firstEpoch, workItem);
					ThreadLog.info("Set " + workItem.getIdentifier()
							+ " to revisit at: " + firstEpoch);
					// This item should not save as it is no longer valid. Mark
					// it as such
					workItem.setValid(false);
					return;
				}

			} catch (XPathExpressionException e) {
				throw new NonRecoverableException(
						"CDSPSubscription:loadUpdate setupRevisit ", e);
			}
		}

		// looking for something in the metadata
		HashSet<String> loadedContent = new HashSet<String>();

		// now see which selections
		for (CDSPSelection selection : cDSPSelections) {
			selection.loadUpdate(doc, content, workItem, loadedContent);
		}

		// now see if any of the selections want to verify that all the content
		// is there
		// the files in the xml are just the file names so reset the
		// loadedContent to be just the filenames
		String[] sourceFiles = loadedContent.toArray(new String[loadedContent
				.size()]);
		loadedContent.clear();
		for (String s : sourceFiles) {
			loadedContent.add(HttpUtils.deriveName(s));
		}

		for (CDSPSelection selection : cDSPSelections) {
			selection.verifyFiles(loadedContent);
		}
	}

	/**
	 * load the revisits is called after the subscription is configured as it is
	 * needed to finish a lookup
	 * 
	 * @param revisitDir
	 * @param numPriorities
	 * @param source
	 * @throws IOException
	 * @throws HistoryException
	 * @throws ProcessingException
	 */
	public void loadRevisits(File revisitDir, int numPriorities, Source source)
			throws IOException, HistoryException, ProcessingException {
		// setup and start the manual republish thread
		manualRepublish = new CDSPManualRepublish(revisitDir, subscriptionName
				+ ManualRepublish.MANUALREPUBTAG, server, this.subscription,
				this);

		// revisit object created for the subscription.
		try {
			revisit = new RevisitHistoryHandler(
					new File(revisitDir, subscriptionName
							+ RevisitHistoryHandler.REVISITTAG)
							.getAbsolutePath(),
					numPriorities, PriorityLoader.getInstance()
							.getQueueManager(), CDSPSubscription.eventsAreSeq,
					source);

		} catch (ProcessingException pe) {
			// load of revisits failed
			throw new ProcessingException("Load of "
					+ revisitDir.getAbsolutePath()
					+ " failed to parse items.\n", pe);
		}
	}

	/**
	 * This simply traverses the child elements and builds up selection
	 * sections... There is no return because it builds up an internal structure
	 * 
	 * @param subscription
	 *            root element
	 * @throws DestinationException
	 * @throws ProcessingException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws  
	 */
	private void loadSelections(Element subscription)
			throws DestinationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		ThreadLog.debug("CDSPSubscription:loadSelections");
		Element selection = null;
		CDSPSelection cDSPSelection = null;

		NodeList selections = XMLUtils.getChildElements(subscription,
				IConstants.SELECTION_CONFIG_ELE_NAME);
		if (selections == null) {
			ThreadLog.info("No selections in subscription");
			return;
		}

		ThreadLog
				.info("Total number of selections - " + selections.getLength());

		for (int selection_index = 0; selection_index < selections.getLength(); selection_index++) {
			selection = null;
			selection = (Element) selections.item(selection_index);
			try {
				cDSPSelection = new CDSPSelection(workingDir, selection,
						namedDestinations);
			} catch (TransformerConfigurationException e) {
				//TODO : Handle  
			}
			cDSPSelections.add(cDSPSelection);
		}
	}

	/**
	 * This method will do following tasks.<br/>
	 * 1. connect to CDS+ and get list of items to be processed.<br/>
	 * 2. populate the work item objects and save it into a List.<br/>
	 * 
	 * @param Long
	 *            after
	 * @throws ParserConfigurationException
	 * @throws NumberFormatException
	 * @throws MalformedURLException
	 */
	public void addWorkItems(Long after, ArrayList<IWorkItem> workItemList, boolean loadBatch)
			throws ProcessingException {
		Long currTime = System.currentTimeMillis();

		if ((currTime > after) && loadBatch) {
			// There is a potential for updates so check

			Document doc = null;
					StringBuffer contenturl = new StringBuffer();

					contenturl.append(this.server);
					contenturl.append(this.subscription);
					
					//Re-Design :  Configurable includeDeletes start
					if(includeDeletes){
						contenturl.append(IConstants.PARAM_ELEMENT);
						contenturl.append(IConstants.INCLUDE_DELETES);
						contenturl.append(IConstants.AND_ELEMENT);
						contenturl.append(IConstants.REVERSE_LIST);
					}else{
						contenturl.append(IConstants.PARAM_ELEMENT);
						contenturl.append(IConstants.REVERSE_LIST);
					}
					//Re-Design :  Configurable includeDeletes end
					
					contenturl.append(IConstants.AFTER).append(after);
					contenturl.append(IConstants.RESULT_LIMIT).append(limit);

					String contentUrl = contenturl.toString();
					ThreadLog.info("Creating work items for - " + contentUrl);
					try {

					// download the list of items to be loaded
					byte[] content = HttpUtils.download(new URL(contentUrl));	
					ThreadLog.info("download complete");
					// create the document
					if (content.length > 0) {
						doc = XMLUtils.parse(new ByteArrayInputStream(content));
						// populate the work item list that will be sent back to
						// priority loader
						addWorkItemsToList(doc, workItemList);
					} else {
						ThreadLog
								.error("stream not saved properly into byte array");
					}
				
			} catch (SAXException e) {
				// some problem in download and parsing. Try downloading it
				// again.
				throw new ProcessingException("CDSPSubscription:addWorkItems ",
						e);
			} catch (IOException e) {
				throw new ProcessingException("CDSPSubscription:addWorkItems ",
						e);
			} catch (HttpRetryException e) {
				throw new RecoverableException(
						"CDSPSubscription:addWorkItems ", e);
			} catch (HttpNonRetryException e) {
				throw new NonRecoverableException(
						"CDSPSubscription:addWorkItems ", e);
			} catch (ParserConfigurationException e) {
				throw new NonRecoverableException(
						"CDSPSubscription:addWorkItems ", e);
			}
		}

		// now add any items that should be revisited
		revisit.addWorkItems(after, workItemList);

		// load the manualRepublish requests, they will be picked up on the next
		// addWorkItems request
		Long manualToken = after;
		if (workItemList.size() > 0) {
			// use the same token as the last one in the list. It is going to
			// become the new after
			manualToken = workItemList.get(workItemList.size() - 1).getToken() + 1;
		} else {
			if (!CDSPSubscription.eventsAreSeq) {
				currTime = new Long(System.currentTimeMillis());
				if (currTime < manualToken) {
					// reset the time to now as after is in the future and
					// manual uploads should happen now
					manualToken = currTime;
				}
			}
		}

		// load any republish items into the revisit handler
		manualRepublish.addRevisitItems(manualToken, revisit);
		
	}

	/**
	 * Result returned from CDS+ will be in form of an xml. Its saved into a
	 * Document and parsed to populate work item objects.
	 * 
	 * @param doc
	 * @param sub
	 * @return
	 * @throws ProcessingException
	 * @throws NumberFormatException
	 * @throws MalformedURLException
	 */
	private void addWorkItemsToList(Document doc,
			ArrayList<IWorkItem> workItemList) throws ProcessingException {
		Element root = null;
		String base = null;
		String href = null;
		String lastModified = null;
		String priority = null;
		String eventType = null;
		String hasAttachments = null;

		root = doc.getDocumentElement();
		base = root.getAttribute(IConstants.ROOT_ELEMENT);

		NodeList children = root.getElementsByTagName(IConstants.ALL_ELEMENTS);
		ThreadLog.info("returned " + children.getLength() + " workitems");
		for (int index = 0; index < children.getLength(); index++) {

			href = XMLUtils.getAttributeValue(children.item(index),
					IConstants.HREF_ATTRIBUTE);
			lastModified = XMLUtils.getAttributeValue(children.item(index),
					IConstants.LAST_MODIFIED);
			hasAttachments = XMLUtils.getAttributeValue(children.item(index),
					IConstants.HAS_ATTACHMENTS);
			priority = XMLUtils.getAttributeValue(children.item(index),
					IConstants.PRIORITY);
			eventType = XMLUtils.getAttributeValue(children.item(index),
					IConstants.EVENT_TYPE);

			try {
				workItemList.add(new CDSPWorkItem(Long.valueOf(lastModified),
						base, href, Integer.valueOf(priority), WorkItem
								.getEvent(eventType), Boolean.valueOf(
								hasAttachments).booleanValue(), this));
				} catch (NumberFormatException e) {
				ThreadLog
						.error("Could not parse last modified timestamp. lastModified="
								+ lastModified);
				ThreadLog.error("skipped creating work item for - " + base
						+ href);
				ErrorLog.logStackTrace(e);
				continue;
			}
		}
		return;
	}

	

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Map<String, IDestination> getNamedDestinations() {
		return namedDestinations;
	}

	public List<CDSPSelection> getcDSPSelections() {
		return cDSPSelections;
	}

}
