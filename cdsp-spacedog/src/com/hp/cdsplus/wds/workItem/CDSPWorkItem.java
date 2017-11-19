package com.hp.cdsplus.wds.workItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.hp.cdsplus.util.http.HttpUtils;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.loader.priorityLoader.PriorityLoader;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ErrorLog;
import com.hp.loader.utils.Log;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;

public class CDSPWorkItem extends WorkItem  {

	static public final String GETVERSIONS = "?expand=versions";
	
	protected String base;
	protected String reference;
	private boolean hasAttachments;
	protected ArrayList<CDSPContentItem> contentList;
	protected CDSPSubscription subscription;
	private String tag;
	private String identifier;

	public CDSPWorkItem(Long token, String base, String reference, Integer priority, String eventType, boolean hasAttachments,
			CDSPSubscription subscription) {
		super(token, priority, eventType);
		this.hasAttachments = hasAttachments;
		this.subscription = subscription;
		contentList = new ArrayList<CDSPContentItem>();
		this.reference = reference;
		this.base = base;
		
		// get the identifier (last piece of the reference path)
		identifier = HttpUtils.deriveName(reference);
		makeTag();
	}

	public CDSPWorkItem (Iterator<String> vIt, HashMap<String, CDSPSubscription> cDSPSubscriptions) throws ProcessingException {
		super(vIt);
		base = getFieldValue(vIt, "base");
		reference = getFieldValue(vIt, "reference");
		hasAttachments = Boolean.valueOf(getFieldValue(vIt, "hasAttachments"));
		String subName = getFieldValue(vIt,"subscription");
		subscription = cDSPSubscriptions.get(subName);
		if (subscription == null) {
			throw new ProcessingException("Failed to find subscription: "+ subName);
		}
		contentList = new ArrayList<CDSPContentItem>();
		
		identifier = HttpUtils.deriveName(reference);
		makeTag();
	}
	
	private void makeTag() {
		tag = token+" - "+identifier+" : "+priority+" - "+(eventType == EVENT_UPDATE ? "U" : (eventType == EVENT_DELETE ? "D" : "T"));
	}
	
	@Override
	public synchronized void done() {
		// remove the content that is in memory
		contentList.clear();
		// notify depencencies that this one is done
		super.done();
	}
	
	public String getBase() {
		return base;
	}


	public String getReference() {
		return reference;
	}


	public boolean getHasAttachments() {
		return hasAttachments;
	}


	public CDSPSubscription getSubscription() {
		return subscription;
	}

	public URL getDeleteUrl(String ref) throws MalformedURLException {
		return new URL(base + ref);
	}
	
	public URL getVersionsUrl() throws MalformedURLException {
		return new URL(base+reference+GETVERSIONS);
	}
	
	public URL getUrl() throws MalformedURLException {
		return new URL(base+reference);
	}

	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#load()
	 */
	public void load() throws ProcessingException {
		try {
			subscription.load(this);
		} catch (ProcessingException pe) {
			// see if this is going to be retried.
			if (!pe.shouldRetry() ) {
				// This event isn't going to complete so set it to be invalid
				setValid(false);
			}
			throw pe;
		}
		
	}


	/* (non-Javadoc)
	 * @see com.hp.loader.workItem.WorkItem#logStatus(java.lang.String, java.lang.Exception)
	 */
	public void logStatus(String arg0, Exception arg1)
			throws ProcessingException {
		// no DB so status of this event is not logged
	}

	@Override
	public void setPriority(Integer priority) {
		super.setPriority(priority);
		// change the tag so it reflects the update change
		makeTag();
	}
	
	
	public boolean store() throws ProcessingException {
		try {
			return storeItems();
		} catch (DestinationException e) {
			throw new ProcessingException(e);
		}
	}
	
	private boolean storeItems() throws DestinationException {
		// have each of the contentItems store
		// shouldFinalize is true if any of the destinations needs to "flush" to the final location
		boolean shouldFinalize = true;
		for(CDSPContentItem item : contentList) {
			shouldFinalize &= item.store(eventType);
		}
		
		boolean updateHistory = shouldFinalize || PriorityLoader.getInstance().getQueueManager().isLastEvent(); 
		if (updateHistory) {
			// finalize all of the destinations as they need to be kept in sync
			for(CDSPSelection selection : this.subscription.getcDSPSelections()) {
				// if one of the destinations fails to finalize we don't want to update the history file
				updateHistory&=selection.finalizeAll();
			}
		}
		
		return updateHistory;
	}
	
	public boolean syncStore() {
		// see if any of the destinations need to be synchronized
		boolean needsSync = false;
		for(CDSPContentItem item : contentList) {
			if (item.needStoreSync()) {
				needsSync = true;
				break;
			}
		}
		return needsSync;
	}
	
	/**
	 * @return the projContent
	 */
	public ArrayList<CDSPContentItem> getCDSPFileHandler() {
		return contentList;
	}

	/**
	 * @param projContent the projContent to add
	 */
	public void addContent(CDSPContentItem cdspfilehandler) {
		this.contentList.add(cdspfilehandler);
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getPackageType() {
		return "Item";
	}

	public String getTag() {
		return tag;
	}

	public String getFullTag() {
		// return the tag plus the full cds+ path
		return tag +" "+reference;
	}

	@Override
	public void save(StringBuffer sb) {
		super.save(sb);
		sb.append(base).append(" ").append(reference).append(" ").append(hasAttachments).append(" ").append(subscription.getSubscriptionName()).append(" ");
	}

	public void cleanup() {
		if (!isValid() &&  PriorityLoader.getInstance().getQueueManager().isLastEvent()) {
			// this is the last event and it didn't store so clear the contentList, and call storeItems to push 
			// clearing the content list stops the store from happening, but the destinations will be finalized
			contentList.clear();
			// get the destinations to send to target and update the history file
			try {
				storeItems();
			} catch (DestinationException e) {
				Log.info("Exception while sending Package");
				ErrorLog.logStackTrace(e);
			}
		}
	}
}
