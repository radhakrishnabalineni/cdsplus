/**
 * 
 */
package com.hp.loader.source;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.hp.loader.history.HistoryHandler;
import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.RevisitWorkItem;


/**
 * @author dahlm
 *
 */
public interface Source {
	
	/**
	 * hasSideLoader returns true if a sideLoader mechanism is to be started for this source
	 * @return
	 */
	public boolean hasSideLoader();
	/**
	 * getWorkItemsSince returns an ordered list of WorkItems from the source.
	 * The last element of the list will hold the last token processed.  The WorkItems returned should
	 * extend WorkItem and handle load/store operations for this particular load
	 * 
	 * @param startToken
	 * @return
	 */
	public ArrayList<IWorkItem> getWorkItemsSince(Long startToken) throws ProcessingException;
	
	/**
	 * Exiting is used to tell a Source that an exit request has been communicated
	 */
	public void exiting();
	
	/**
	   * loadItems is to be overloaded by classes that extend this handler
	 * @return List of loaded workItems
	 * @throws ProcessingException
	   */
	public ArrayList<RevisitWorkItem> loadRevisitWorkItems(FileInputStream fis, RevisitHistoryHandler historyHandler) throws ProcessingException;
	  
}
