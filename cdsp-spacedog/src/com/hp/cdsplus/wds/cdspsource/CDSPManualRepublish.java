package com.hp.cdsplus.wds.cdspsource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.cdsplus.util.http.HttpNonRetryException;
import com.hp.cdsplus.util.http.HttpRetryException;
import com.hp.cdsplus.util.http.HttpUtils;
import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.wds.constants.IConstants;
import com.hp.cdsplus.wds.workItem.CDSPSubscription;
import com.hp.cdsplus.wds.workItem.CDSPWorkItem;
import com.hp.loader.priorityLoader.ManualRepublish;
import com.hp.loader.priorityLoader.NonRecoverableException;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.priorityLoader.RecoverableException;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;

public class CDSPManualRepublish extends ManualRepublish {

	private StringBuffer sb = new StringBuffer();
	private int sbBaseLen;
	private CDSPSubscription sub;
	private Integer loadPriority;
	
	public CDSPManualRepublish(File dir, String fileName, String base, String subscription, CDSPSubscription sub) throws IOException {
		super(new File(dir, fileName));
		sb.append(base).append(subscription);
		if (subscription.charAt(subscription.length()-1) != '/') {
			// make sure it ends in /
			sb.append("/");
		}
		sbBaseLen = sb.length();
		this.sub = sub;
	}

	/**
	 * getNextLine loads a file name with options : priority
	 * @param scan
	 * @return
	 */
	private String getNextLine(Scanner scan) {
		String line = (scan.hasNextLine() ? scan.nextLine() : null);
		if (line != null) {
			int idx = line.indexOf(":");
			if (idx != -1 ) {
				try {
					Integer newPriority = Integer.valueOf(line.substring(idx+1).trim());
					loadPriority = newPriority;
				} catch (NumberFormatException nfe) {
					ThreadLog.error("Bad priority value in manual republish file");
					ErrorLog.logStackTrace(nfe);
				}
				// take off the priority
				line = line.substring(0, idx).trim();
			}
		}
		return line;
	}
	
	@Override
	public ArrayList<IWorkItem> loadManualPublishItems(FileInputStream fis)	throws IOException {
		loadPriority = null;
		ArrayList<IWorkItem> newItems = new ArrayList<IWorkItem>();
		Scanner scan = new Scanner(fis);
		String line = getNextLine(scan);
		try {
			while (line != null) {
				try {
					if (line.length() > 0) {
						IWorkItem item = getItem(line);
						if (item != null){
							newItems.add(item);
						}
					}
					line = getNextLine(scan);
				} catch (ProcessingException e) {
					if (!e.shouldRetry()) {
						//skip the previous line it isn't right
						ThreadLog.error(ThreadLog.getExceptionMsgForLog("Manual load failed for: "+ line, e));
						ErrorLog.logStackTrace(e);
						line = getNextLine(scan);
					}else if(e.shouldRetry()){
						if( !PriorityLoader.retryOnError){
						PriorityLoader.exitOnError(e);
						break;
						}else{
							try {
								Thread.sleep(15*1000);
							} catch (InterruptedException ie){}
						}
					}
				}
			}
		} finally {
			scan.close();
		}
		return newItems;
	}

	private IWorkItem getItem(String fileName) throws ProcessingException {
		Document doc = null;
		sb.setLength(sbBaseLen);
		sb.append(fileName).append(IConstants.GETFILEINFO);

		String contentUrl = sb.toString();
		ThreadLog.info("Manual load: "+fileName);

		try{
			// download the list of items to be loaded
			byte[] content = HttpUtils.download(new URL(contentUrl));
			// create the document
			if(content.length > 0){
				doc = XMLUtils.parse(new ByteArrayInputStream(content));

				Element root = null;
				String base = null;
				String href = null;
				String lastModified = null;
				String priority= null;
				String eventType= null;
				String hasAttachments= null;

				root = doc.getDocumentElement();
				base = root.getAttribute(IConstants.ROOT_ELEMENT);

				NodeList children = root.getElementsByTagName(IConstants.PROJ_REV_ELEMENT);
				// There should just be one
				for (int index = 0; index < children.getLength(); index++) {

					href 			= XMLUtils.getAttributeValue(children.item(index),IConstants.HREF_ATTRIBUTE);
					lastModified 	= XMLUtils.getAttributeValue(children.item(index),IConstants.LAST_MODIFIED);
					hasAttachments 	= XMLUtils.getAttributeValue(children.item(index),IConstants.HAS_ATTACHMENTS);
					priority 		= XMLUtils.getAttributeValue(children.item(index),IConstants.PRIORITY);
					eventType 		= XMLUtils.getAttributeValue(children.item(index),IConstants.EVENT_TYPE);

					try {
						return new CDSPWorkItem(Long.valueOf(lastModified), base, href,
								(loadPriority != null ? loadPriority : Integer.valueOf(priority)), 
								WorkItem.getEvent(eventType), Boolean.valueOf(hasAttachments).booleanValue(), sub);
					} catch (NumberFormatException e) {
						throw new ProcessingException("Could not parse last modified timestamp. lastModified="+lastModified, false);
					}
				}
			}else{
				throw new ProcessingException("stream not saved properly into byte array", false);
			}

		} catch (SAXException e) {
			// some problem in download and parsing. Try downloading it again.
			throw new ProcessingException(e.getMessage(), e, false);
		} catch (IOException e) {
			throw new ProcessingException(e.getMessage(), e, false);
		} catch (HttpRetryException e) {
			throw new RecoverableException(e.getMessage(), e);
		} catch (HttpNonRetryException e) {
			throw new NonRecoverableException(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new NonRecoverableException(e.getMessage(), e);
		}
		return null;

	}
}
