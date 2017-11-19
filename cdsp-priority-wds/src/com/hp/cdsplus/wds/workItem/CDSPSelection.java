package com.hp.cdsplus.wds.workItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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
import com.hp.cdsplus.wds.constants.IConstants;
import com.hp.cdsplus.wds.destination.IDestination;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.loader.priorityLoader.NonRecoverableException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.priorityLoader.RecoverableException;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.workItem.IWorkItem;

public class CDSPSelection {
	private static final String XPSELECT = "select";
	private static final String XPSOURCE = "source";
	private static final String XPDESTINATION = "destination";
	private static final String XPMEETS = "meets";
	private static final String XPCONTENTTYPE = "contentType";
	private static final String XPISARCHIVE = "isArchive";
	private static final String XPVERIFYLIST = "verifyList";
	private static final String XPEMBEDDEDELEM = "embeddedElement";
	private static final String SAVEXML = "saveXML";
	private static final String EXTERNAL_XSL = "transform";//Re-Design
	
	public static final Class<?>[] SIG = { File.class, Element.class };
	private boolean xmlContentType = false;

	private File workingDir;

	private XPathExpression xPmeets;
	private XPathExpression xPselect;
	private XPathExpression xPsource;
	private XPathExpression xPdestination;
	private XPathExpression xPcontentType;
	private XPathExpression xPisArchive;
	private XPathExpression xPembeddedElement;
	private XPathExpression xPverifyList;
	private boolean saveXML = true;
	
	private List<IDestination> destinations = new ArrayList<IDestination>();
	private HashSet<String> verifySet = null;
	
	//Re-Design : start
	private String external_xsl =null;
	private Templates sTemplates = null;
	private Transformer transformer =null;
	//Re-Design : End
	/**
	 * As part of instantiation following calls are made.<br/>
	 * 1. load all XPathExpressions for this selection.<br/>
	 * 2. load all the RemoteDestinations for this selection. <br/>
	 * 
	 * @param CDSPSubscription
	 *            cDSPSubscription
	 * @param File
	 *            workingDir
	 * @param Element
	 *            selection
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws DestinationException
	 * @throws TransformerConfigurationException 
	 * @throws ProcessingException
	 */
	public CDSPSelection(File workingDir, Element selection,
			Map<String, IDestination> namedDestinations)
			throws DestinationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, TransformerConfigurationException {
		ThreadLog.debug("CDSPSelection object instantiated");
		this.workingDir = workingDir;
		loadXPathExpressions(selection);
		loadDestinations(selection, namedDestinations);
		external_xsl = selection.getAttribute(EXTERNAL_XSL); //Re-Design]
		if (external_xsl != null && external_xsl !="") {
			sTemplates = CDSPSubscription.transformerFactory
					.newTemplates(new StreamSource(new File(external_xsl)));
			transformer = sTemplates.newTransformer();
		}
	}

	/**
	 * This method is called to download the metadata XML. <br/>
	 * If configuration XML does not have 'selection' element defined, metadata
	 * itself is being sent to destinations.<br/>
	 * 
	 * @param workItem
	 * @param loadedContent
	 * @return
	 * @throws ProcessingException
	 */
	public void loadUpdate(Document doc, byte[] content, CDSPWorkItem workItem,
			HashSet<String> loadedContent) throws ProcessingException {
		XMLUtils.getVariableResolver().setVariable("identifier",
				workItem.getIdentifier());

		// we will continue with the load only if it "meets" the criteria.
		// if it doesn't, then we don't want to go ahead with the load/store for
		// this work item
		if (xPmeets != null) {
			try {
				Boolean meetsCriteria = (Boolean) xPmeets.evaluate(doc,
						XPathConstants.BOOLEAN);
				if (!meetsCriteria) {
					ThreadLog.info("skipping load for - "
							+ workItem.getIdentifier()
							+ "since it doesn't meet the criteria");
					// Mark the workItem as invalid so it will be skipped
					workItem.setValid(false);
					return;
				}
			} catch (XPathExpressionException e) {
				throw new ProcessingException(e);
			}
		}
		if (xPselect == null) {
			// this selection wants the metadata so set it up
			String source = null;
			String dest = null;
			if (xPsource != null)
				try {
					source = (String) xPsource.evaluate(doc,
							XPathConstants.STRING);
				} catch (XPathExpressionException e) {
					throw new ProcessingException(e);
				}
			else
				source = workItem.getIdentifier();

			XMLUtils.getVariableResolver().setVariable("originDocName",
					HttpUtils.deriveName(source));
			dest = resolveDest(doc, xPdestination, source);
			ThreadLog.info("source - " + source + "     destination - " + dest);
			//Re-Design
			if(transformer!=null && (workItem.getEventType() != IWorkItem.EVENT_DELETE)){
				content = applyExternalXSLT(content);
				// add the transformed metadata to the content
				workItem.addContent(new CDSPContentItem(
						dest,content, this));
			 			
			}else{
			// add the metadata to the content
			workItem.addContent(new CDSPContentItem(
					dest,
					((workItem.getEventType() != IWorkItem.EVENT_DELETE) ? content
							: null), this));
			}
		} else if (content.length > 0) {
			loadContentUpdate(doc, workItem, loadedContent);
		}

	}

	/**
	 * @param doc
	 * @param workItem
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ProcessingException
	 * @throws TransformerException
	 */
	private void loadContentUpdate(Document doc, CDSPWorkItem workItem,
			HashSet<String> loadedContent) throws ProcessingException {
		// Document doc;
		NodeList nodeList;
		// doc = XMLUtils.parse(new ByteArrayInputStream(metaData));
		// at this point metadata is already downloaded. so just get the docid
		// from the identifier.

		// get the content references to load
		nodeList = resolveNodeList(doc);

		if (xPcontentType != null) {
			xmlContentType = getContentType(doc).equals("xml");
		}
		for (int index = 0; index < nodeList.getLength(); index++) {
			Node node = nodeList.item(index);
			downloadContent(node, workItem, loadedContent);
		}
		ThreadLog.debug("CDSPSelection:loadContentUpdate "
				+ workItem.getIdentifier() + " complete.");
	}

	/**
	 * downloadContent brings the content from the source into a byteArray
	 * 
	 * @param source
	 * @return
	 * @throws ProcessingException
	 */
	private byte[] downloadContent(String source) throws ProcessingException {
		ThreadLog.debug("DownloadContent ");
		byte[] content;
		try {
			content = HttpUtils.download(new URL(source));
			return content;
		} catch (MalformedURLException e) {
			throw new NonRecoverableException("MalformedURL: " + source);
		} catch (HttpRetryException e) {
			throw new RecoverableException(e);
		} catch (HttpNonRetryException e) {
			String msg = e.getMessage();
			if (msg != null) {
				throw new NonRecoverableException(msg + " "
						+ source.substring(source.indexOf("app/") + 4));
			} else {
				throw new NonRecoverableException(e);
			}
		}
	}
	
	/**
	 * @param node
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void downloadContent(Node node, CDSPWorkItem workItem,
			HashSet<String> loadedContent) throws ProcessingException {
		try {
			String source = null;
			if (xPsource != null){
				source = (String) xPsource
						.evaluate(node, XPathConstants.STRING);
				
			}
			
			else
				source = workItem.getIdentifier();
			
			if (loadedContent.contains(source)) {
				// This is already loaded
				return;
			} else {
				// remember that this content is loaded
				loadedContent.add(source);
			}
			String sourceName = HttpUtils.deriveName(source);
			if (xmlContentType && (sourceName.indexOf('.') == -1)) {
				XMLUtils.getVariableResolver().setVariable("originDocName",
						sourceName + ".xml");
			} else {
				XMLUtils.getVariableResolver().setVariable("originDocName",
						sourceName);
			}

			// XMLUtils.getVariableResolver().setVariable("sourceName",
			// HttpUtils.deriveName(source));
			long startTime = System.currentTimeMillis();

			boolean isArchive = (xPisArchive != null && (Boolean) xPisArchive
					.evaluate(node, XPathConstants.BOOLEAN));
			byte[] content = null;

			if ((workItem.getEventType() != IWorkItem.EVENT_DELETE)
					|| isArchive
					|| (xmlContentType && ((xPembeddedElement != null) || (xPverifyList != null)))) {
				// only load the content if it will be needed later
				ThreadLog.debug("downloadContent " + workItem.getIdentifier()
						+ " start.");
					content = downloadContent(source);
				ThreadLog.debug("downloadContent " + workItem.getIdentifier()
						+ " complete. <"
						+ (System.currentTimeMillis() - startTime) + ">");
			}

			// this is required here to make sure we are creating proper entries
			// for the archive content.
			String dest = resolveDest(node, xPdestination, source);

			if (isArchive) {
				int lastSlash = 0;
				// need to get the path to the parent dir of the zip file
				String destParent = "";
				if ((lastSlash = dest.lastIndexOf('/')) >= 0) {
					destParent = dest.substring(0, lastSlash);
				}
				try {
					ZipInputStream zis = new ZipInputStream(
							new ByteArrayInputStream(content));
					ZipEntry ze = null;
					ze = zis.getNextEntry();
					while (ze != null) {
						if (!ze.isDirectory()) {
							String entryName = ze.getName();
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							if (workItem.getEventType() != IWorkItem.EVENT_DELETE) {
								byte[] buf = new byte[1024];
								int n;
								while ((n = zis.read(buf, 0, 1024)) > -1)
									bos.write(buf, 0, n);
							}
							zis.closeEntry();
							ze = zis.getNextEntry();
							if (loadedContent.contains(entryName))
								continue;
							workItem.addContent(new CDSPContentItem(
									destParent + "/" + entryName,
									((workItem.getEventType() != IWorkItem.EVENT_DELETE) ? bos
											.toByteArray() : null), this));
						}
					}
				} catch (IOException e) {
					throw new ProcessingException(e.getMessage(), e);
				}
			}

			// if there is a destination, save the content
			if (destinations.size() > 0) {
				// downloading the content itself

				ThreadLog.info("source - " + getSourceName(source)
						+ "     destination - " + dest);
				workItem.addContent(new CDSPContentItem(dest, ((workItem
						.getEventType() != IWorkItem.EVENT_DELETE) ? content
						: null), this));
			}

			if (xmlContentType
					&& ((xPembeddedElement != null) || (xPverifyList != null))) {
				startTime = System.currentTimeMillis();
				Document doc;
				try {
					doc = XMLUtils.parse(new ByteArrayInputStream(content));
					if (xPverifyList != null) {
						// get the list of internal components in this xml to
						// make sure we have all of the attachments after
						// everything is loaded
						NodeList nodeList = (NodeList) xPverifyList.evaluate(
								doc, XPathConstants.NODESET);
						if (verifySet == null) {
							verifySet = new HashSet<String>();
						}
						for (int i = 0; i < nodeList.getLength(); i++) {
							Node n = nodeList.item(i);
							verifySet.add(n.getTextContent());
						}
					}
					if (xPembeddedElement != null) {
						// get the list of internal components in this xml to
						// make sure we have all of the attachments after
						// everything is loaded
						NodeList nodeList = (NodeList) xPembeddedElement
								.evaluate(doc, XPathConstants.NODESET);
						for (int i = 0; i < nodeList.getLength(); i++) {
							Node n = nodeList.item(i);
							downloadContent(n, workItem, loadedContent);
						}
					}
				} catch (SAXException e) {
					throw new ProcessingException(e.getMessage(), e);
				} catch (IOException e) {
					throw new ProcessingException(e.getMessage(), e);
				} catch (ParserConfigurationException e) {
					throw new ProcessingException(e.getMessage(), e);
				}
			}
		} catch (XPathExpressionException e) {
			throw new ProcessingException(e.getMessage(), e);
		}
	}

	public boolean finalizeAll() throws DestinationException {
		ThreadLog.debug("CDSPSelection: finalizeAll called.");
		boolean return_val = true;
		for (IDestination dest : destinations) {
			return_val &= dest.finalizeDest(true);
		}
		return return_val;
	}

	public String getContentType(Document doc) throws ProcessingException {
		ThreadLog.debug("getContentType called.");
		try {
			return (String) xPcontentType.evaluate(doc, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new ProcessingException(e);
		}
	}

	public List<IDestination> getDestinations() {
		return destinations;
	}

	/**
	 * strip the leading string off of the name so it isn't so bulky in the log
	 * 
	 * @param source
	 * @return
	 */
	private String getSourceName(String source) {
		int idx = source.indexOf("app/");
		String sourceVal = (idx != -1 ? source.substring(idx + 4) : source);
		return sourceVal;
	}

	/**
	 * @param selection
	 * @throws DestinationException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ProcessingException
	 */
	private void loadDestinations(Element selection,
			Map<String, IDestination> namedDestinations)
			throws DestinationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		ThreadLog.debug("CDSPSelection:loadDestinations");
		/*
		 * here we check if selection has a named destination or just an unnamed
		 * destination. if its named destination, its been instantiated in
		 * CDSPSubscription class, we just have to add reference here. if its
		 * unnamed destination, we go ahead and instantiate it here. named
		 * destination can be shared among all the selections under a
		 * subscription. unnamed destination will be specific to that particular
		 * selection.
		 */
		String className = null;
		Class<?> theClass = null;
		IDestination destination = null;
		Node dest_node = null;
		String destination_name = null;
		NodeList destination_nodes = XMLUtils.getChildElements(selection,
				IConstants.DEST_ELE_NAME);

		for (int rd_index = 0; destination_nodes != null
				&& rd_index < destination_nodes.getLength(); rd_index++) {
			dest_node = destination_nodes.item(rd_index);
			destination_name = XMLUtils.getAttributeValue(dest_node,
					IConstants.NAME_ATTRIBUTE);
			if (destination_name != null) {
				if (namedDestinations.containsKey(destination_name)) {
					destinations.add(namedDestinations.get(destination_name));
					continue;
				} else {
					throw new DestinationException(
							"Undefined namedDestination: " + destination_name
									+ " specified");
				}
			}
			className = Utils.nullCheck(IConstants.DEST_CLASS_ATTR_NAME,
					XMLUtils.getAttributeValue(dest_node,
							IConstants.DEST_CLASS_ATTR_NAME));
			theClass = Class.forName(className);
			destination = (IDestination) theClass.newInstance();
			destination.init((Element) dest_node, this.workingDir);
			destinations.add(destination);
		}
		ThreadLog.debug("complete");
	}

	private XPathExpression loadXPath(Element selection, String expName) {
		String xpString = XMLUtils.getAttributeValue(selection, expName);
		if (xpString != null && xpString.trim().length() > 0) {
			ThreadLog.info(expName + ": " + xpString);
		}
		return XMLUtils.compileXPath(expName, xpString, null);
	}

	/**
	 * @param selection
	 */
	private void loadXPathExpressions(Element selection) {
		xPselect = loadXPath(selection, XPSELECT);
		xPsource = loadXPath(selection, XPSOURCE);
		xPdestination = loadXPath(selection, XPDESTINATION);
		xPmeets = loadXPath(selection, XPMEETS);
		xPcontentType = loadXPath(selection, XPCONTENTTYPE);
		xPisArchive = loadXPath(selection, XPISARCHIVE);
		xPverifyList = loadXPath(selection, XPVERIFYLIST);
		xPembeddedElement = loadXPath(selection, XPEMBEDDEDELEM);
		
	}

	public boolean needStoreSync() {
		for (IDestination dest : destinations) {
			if (dest.needStoreSync()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param workItem
	 * @throws ProcessingException
	 */
	public void loadDelete(CDSPWorkItem workItem) throws ProcessingException {
		// String embedded_dest =
		// HttpUtils.deriveName(workItem.getUrl().toString());
		workItem.addContent(new CDSPContentItem(workItem.getIdentifier()+".xml", null,
				this));
	}

	/**
	 * This parameter takes a context node, and xpathApi, an xpath and the
	 * source url for a particular destination. Basically the point is if the
	 * xpath is not defined then we just assume that the destination fileName is
	 * just the trailing part of the source so if the source url is
	 * "http://www.hp.com/images/hi.jpg" the value returned is "hi.jpg" if the
	 * xpath is defined then the return is whatever it evaluates to
	 * 
	 * @param n
	 * @param api
	 * @param xpath
	 * @param source
	 * @return
	 */
	public String resolveDest(Object n, XPathExpression xpath, String source)
			throws ProcessingException {
		// ThreadLog.debug("called for :"+source);
		String xo = null;
		if (xpath != null) {
			try {
				xo = (String) xpath.evaluate(n, XPathConstants.STRING);
			} catch (XPathExpressionException e) {
				// e.printStackTrace();
				throw new NonRecoverableException("CDSPSelection:resovleDest",
						e);
			}
		} else {
			xo = HttpUtils.deriveName(source);
		}
		return xo;
	}

	/**
	 * resolveNodeList first checks to make sure it "meets" the criteria if one
	 * is specified Then it returns all nodes from the select that match.
	 * 
	 * @param doc
	 * @return
	 * @throws ProcessingException
	 */
	public NodeList resolveNodeList(Document doc) throws ProcessingException {
		ThreadLog.debug("resolveNodeList() called.");
		try {
			if (xPmeets == null
					|| ((Boolean) xPmeets.evaluate(doc, XPathConstants.BOOLEAN))
							.booleanValue()) {
				NodeList nodeList = (NodeList) xPselect.evaluate(doc,
						XPathConstants.NODESET);
				return nodeList;
			} else {
				ThreadLog.debug("complete.");
				return null;
			}
		} catch (XPathExpressionException e) {
			throw new NonRecoverableException("CDSPSelection:resolveNodeList ",
					e);
		}
	}

	public int size() {
		return destinations.size();
	}

	/**
	 * verifyFiles checks that every file in the verify list is in the
	 * loadedContent set
	 * 
	 * @param loadedContent
	 * @throws NonRecoverableException
	 */
	public void verifyFiles(HashSet<String> loadedContent)
			throws NonRecoverableException {
		if (verifySet != null) {
			// take out all of the files that we loaded
			verifySet.removeAll(loadedContent);
			if (verifySet.size() > 0) {
				// There is something required we didn't get the document isn't
				// complete
				StringBuffer sb = new StringBuffer();
				sb.append("Missing attachments for xml file: ");
				for (String s : verifySet) {
					sb.append(s).append(", ");
				}
				sb.setLength(sb.length() - 2);
				throw new NonRecoverableException(sb.toString());
			}
		}
	}

	//Start code changes by <048174> Niharika on 19-06-2013 for CDSP Redesign
	
	// ********************************************
	// Saxon Extension Functions
	// ********************************************

	/**
	 * converts date  to Epoch
	 * @param sourceDates
	 * @return
	 */
	public static long[] dateToEpoch(String[] sourceDates) {
		long[] toReturn = new long[sourceDates.length];
		for (int i = 0; i < sourceDates.length; i++) {
			toReturn[i] = dateToEpoch(sourceDates[i],
					IConstants.STANDARD_DATE_FORMAT);
		}
		return toReturn;
	}

	/**
	 *  converts timestamp to date and then to Epoch
	 * @param sourceDate
	 * @param inFormat
	 * @return
	 */
	public static long dateToEpoch(String sourceDate, String inFormat) {
		Date dateIn = getDate(sourceDate, inFormat);
		if (dateIn == null)
			return 0L;
		return dateIn.getTime();
	}

	/**
	 * Converts timestamp format to Simple Date format
	 * @param date
	 * @param timestampFormat
	 * @return
	 */
	private static java.util.Date getDate(String date, String timestampFormat) {
		try {
			if (date == null || date.equals(""))
				return null;
			if (timestampFormat == null || timestampFormat.equals(""))
				return null;
			SimpleDateFormat format = new SimpleDateFormat(timestampFormat);
			return (java.util.Date) format.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * checks all Filter Dates are before currentTimestamp or not
	 * @param sourceDates
	 * @return
	 */
	public static boolean areBeforeNow(String[] sourceDates) {
		boolean areAllBeforeNow = true;
		long currentTime = System.currentTimeMillis();
		for (int i = 0; areAllBeforeNow && i < sourceDates.length; i++) {
			if (sourceDates[i] != null && !sourceDates[i].equals("")) {
				if (currentTime > dateToEpoch(sourceDates[i],
						IConstants.STANDARD_DATE_FORMAT)) {
					areAllBeforeNow = false;
				}
			}
		}
		return areAllBeforeNow;
	}

	/**
	 * checks all Filter Dates are after currentTimestamp or not
	 * @param sourceDates
	 * @return
	 */
	public static boolean areAfterNow(String[] sourceDates) {
		boolean areAllAfterNow = true;
		long currentTime = System.currentTimeMillis();
		for (int i = 0; areAllAfterNow && i < sourceDates.length; i++) {
			if (sourceDates[i] != null && !sourceDates[i].equals("")) {
				if (currentTime < dateToEpoch(sourceDates[i],
						IConstants.STANDARD_DATE_FORMAT)) {
					areAllAfterNow = false;
				}
			}
		}
		return areAllAfterNow;
	}
	
	/**
	 * Method to apply External XSL
	 * It takes metadata content byte array, applies XSLT 
	 * and returns back transformed metadata byte array
	 * 
	 * @param sourceContent
	 * @return transformedContent
	 * @throws ProcessingException
	 */
	public byte[] applyExternalXSLT(byte[] sourceContent) throws ProcessingException{
		byte[] tranformedContent = null;
		ByteArrayInputStream bis = null;
		ByteArrayOutputStream bos = null;
		try {
			bis = new ByteArrayInputStream(sourceContent);	
			bos = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(bis),
					new StreamResult(bos));
			tranformedContent = bos.toByteArray();

		} catch (TransformerConfigurationException e) {
			throw new ProcessingException(e);
		} catch (TransformerException e) {
			throw new ProcessingException(e);
		}
		return tranformedContent;
	}
	// End code changes by <048174> Niharika on 16-07-2013 for CDSP Redesign
	
	/**
	 * Getter for stylesheet template
	 * @return stylesheet
	 */
	public Templates getStyleSheetTemplates() {
		return sTemplates;
	}

}

