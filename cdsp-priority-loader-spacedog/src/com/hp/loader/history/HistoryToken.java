/**
 * 
 */
package com.hp.loader.history;

import java.io.IOException;

import com.hp.loader.workItem.IWorkItem;


/**
 * HistoryToken holds the times for a document and status of when it is loaded/saved
 * 
 * @author dahlm
 *
 */
public class HistoryToken {

	private long phaseStartTime;
	private long loadTime;
	private long saveTime;
	private boolean saved = false;
	private IWorkItem workItem;
	private HistoryHandler handler;
	
	public HistoryToken(IWorkItem wi, HistoryHandler handler) {
		workItem = wi;
		this.handler = handler;
		phaseStartTime = System.currentTimeMillis();
		handler.addToken(this);
	}
	
	/**
	 * loaded should be called when the workItem has finished loading
	 */
	public void loaded() {
		long currTime = System.currentTimeMillis();
		loadTime = currTime - phaseStartTime;
		phaseStartTime = currTime;
	}
	
	/**
	 * saved is called after the workItem has been saved
	 */
	public synchronized void saved() {
		long currTime = System.currentTimeMillis();
		saveTime = currTime - phaseStartTime;
		phaseStartTime = currTime;
		saved = true;
	}		
	
	/**
	 * isSaved returns the state of this workItem
	 * @return saved
	 */
	public synchronized boolean isSaved() {
		return saved;
	}
	
	/**
	 * addStats adds the statistics of this workItem to the stringBuffer
	 * @param sb
	 */
	public void addStats(StringBuffer sb) {
		long waitTime = System.currentTimeMillis() - phaseStartTime;
		sb.append(" L: ").append(loadTime).append(" S: ").append(saveTime);
		sb.append(" W: ").append(waitTime);
	}
	
	public IWorkItem getWorkItem() {
		return workItem;
	}
	
	public void removeToken() {
		handler.removeToken(this);
	}
	
	/**
	 * pass the request on to the history handler.
	 * @throws HistoryException 
	 * @throws IOException 
	 */
	public void writeHistory() throws IOException, HistoryException {
		handler.writeHistory(handler);
	}
}
