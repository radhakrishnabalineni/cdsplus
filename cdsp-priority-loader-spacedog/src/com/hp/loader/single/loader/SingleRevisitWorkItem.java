package com.hp.loader.single.loader;

import java.io.IOException;
import java.util.ArrayList;

import com.hp.loader.single.loader.SingleHistoryException;
import com.hp.loader.single.loader.SingleHistoryHandler;
import com.hp.loader.single.loader.SingleRevisitHistoryHandler;
import com.hp.loader.single.loader.SingleProcessingException;
import com.hp.loader.utils.ThreadLog;

public class SingleRevisitWorkItem implements SingleIWorkItem {
	
	// token that identifies when the revisit is to happen
	private Long revisitToken;
	// 
	private SingleIWorkItem workItem;
	private SingleRevisitHistoryHandler handler;
	private String revisitTag;
	private boolean queued = false;
	// link to next item if multiples are stored at the same event token
	private SingleRevisitWorkItem next = null;
	
	public SingleRevisitWorkItem(Long revisitToken, SingleIWorkItem workItem, SingleRevisitHistoryHandler handler) {
		this.revisitToken = revisitToken;
		this.workItem = workItem;
		this.handler = handler;
		revisitTag = "R: "+revisitToken+" - ";
	}

	/**
	 * the revisit items are at the same token so link it at the end of the list
	 * @param item
	 */
	public void add (SingleRevisitWorkItem item) {
		SingleRevisitWorkItem curItem = this;
		while(curItem.next != null) {
			curItem = curItem.next;
		}
		curItem.next = item;
	}
	
	public int addWorkItems(ArrayList<SingleIWorkItem> workItemList) {
		int numAdded = 0;
		if (!queued) {
			// set this item to valid so it will attempt to load
			setValid(true);
			
			numAdded++;
			workItemList.add(this);
			queued = true;
		}
		SingleRevisitWorkItem curItem = this;
		while(curItem.next != null) {
			curItem = curItem.next;
			if (!curItem.queued) {
				numAdded++;
				workItemList.add(curItem);
				curItem.queued = true;
			}
		}
		return numAdded;
	}	
	/**
	 * remove the workItem.  If it is this, just return the rest (possibly null)
	 * @param item
	 * @return
	 */
	public SingleRevisitWorkItem remove (SingleRevisitWorkItem item) {
		if (item == this) {
			SingleRevisitWorkItem nextVal = this;
			nextVal = next;
			// disconnect this item from the list so it can be garbage collected
			next = null;
			// take me out of the list
			return nextVal;
		} else {
			SingleRevisitWorkItem nextItem = this;
			while(nextItem != null) {
				if (nextItem.next == item) {
					// disconnect the item from the list
					nextItem.next = item.next;
					item.next = null;
					break;
				} else {
					nextItem = nextItem.next;
				}
			}
			return this;
		}
	}
	
	public boolean isQueued() {
		return queued;
	}


	public void setQueued(boolean queued) {
		this.queued = queued;
	}


	public Long getRevisitToken() {
		return revisitToken;
	}


	public void createHistoryToken() {
		workItem.createHistoryToken(this, handler);
	}


	public void cleanup() {
		workItem.cleanup();
	}


	public String getFullTag() {
		return revisitTag+workItem.getFullTag();
	}


	public String getIdentifier() {
		return workItem.getIdentifier();
	}


	public String getPackageType() {
		return workItem.getPackageType();
	}


	public String getTag() {
		return revisitTag+workItem.getTag();
	}


	public void load() throws SingleProcessingException {
		workItem.load();
	}


	public void logStatus(String status, Exception e) throws SingleProcessingException {
		workItem.logStatus(status, e);
	}


	public void save(StringBuffer sb) {
		workItem.save(sb);
		sb.append(revisitToken).append("\n");
		if (next != null) {
			next.save(sb);
		}
	}


	public boolean store() throws SingleProcessingException {
		return workItem.store();
	}


	public boolean syncStore() {
		return workItem.syncStore();
	}


	public void exit() {
		workItem.exit();
	}


	public boolean failed() {
		return workItem.failed();
	}


	public void clearFailed() {
		workItem.clearFailed();
	}


	public String getEventType() {
		return workItem.getEventType();
	}


	public Long getToken() {
		return revisitToken;
	}


	public void manageDuplicate(SingleIWorkItem oldWorkItem) {
		SingleWorkItem.resolveDuplicate(this, oldWorkItem);
	}


	public void exiting() {
		workItem.exiting();
	}


	public void setPostItem(SingleWorkItem postItem) {
		workItem.setPostItem(postItem);
	}


	public void setToken(Long token) {
		workItem.setToken(token);
	}


	public void setPriority(Integer priority) {
		workItem.setPriority(priority);
	}


	public Integer getPriority() {
		return workItem.getPriority();
	}


	public SingleIWorkItem getPrevItem() {
		return workItem.getPrevItem();
	}


	public SingleIWorkItem getPostItem() {
		return workItem.getPostItem();
	}


	public boolean loadItem() throws SingleProcessingException {
		return workItem.loadItem();
	}


	public void logStatus(String status) throws SingleProcessingException {
		workItem.logStatus(status);
	}


	public void prevDone() {
		workItem.prevDone();
		handler.prevDone(this);
	}


	public void setValid(boolean isValid) {
		workItem.setValid(isValid);
	}


	public boolean isValid() {
		return workItem.isValid();
	}


	public boolean isRunning() {
		return workItem.isRunning();
	}


	public void setRunning(boolean running) {
		workItem.setRunning(running);
	}


	public void done() {
		workItem.done();
		if (!isValid()) {
			// This item was skipped take it out of the handler queue.
			handler.removeItem(this, true);
		}
	}


	public void removeToken(boolean lastToken) throws IOException,	SingleHistoryException {
		workItem.removeToken(lastToken);
	}

	public void storeItem() throws SingleProcessingException, IOException, SingleHistoryException {
		workItem.storeItem();
	}

	public void createHistoryToken(SingleIWorkItem item, SingleHistoryHandler handler) {
		workItem.createHistoryToken(item, handler);
	}


	public boolean schedule() {
		return handler.scheduleWorkItem(this);
	}

	public String toString() {
		return "rToken: "+revisitToken+" "+workItem.toString();
	}


	public void setPostItem(SingleIWorkItem newWorkItem) {
		workItem.setPostItem(newWorkItem);
	}


	public void setPrevItem(SingleIWorkItem newWorkItem) {
		workItem.setPrevItem(newWorkItem);
	}


	public boolean isStored() {
		return workItem.isStored();
	}
	
}
