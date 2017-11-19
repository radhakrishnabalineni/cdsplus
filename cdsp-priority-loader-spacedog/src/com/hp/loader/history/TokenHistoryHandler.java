package com.hp.loader.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.hp.loader.priorityLoader.WIQueueManager;
import com.hp.loader.utils.Log;
import com.hp.loader.workItem.IWorkItem;
import com.hp.loader.workItem.WorkItem;


/**
 * As the history file is written once for each workItem, it is important that it be small and simple.
 * It is there for just n rows of numbers.  They are stored in priority order, one for each priority.
 * Number of priorities is configurable from config file.
 *
 * @author      mdahl
 * @version     %I%, %G%
 * @since       1.0
 */
public class TokenHistoryHandler extends HistoryHandler {

	private static TokenHistoryHandler historyHandler;

	private static Long START_TOKEN = new Long(0);

	// array of token values for each of the tokens 
	private long[]	completedTokens;

	private long highestFinishedToken;

	private boolean eventsAreSequential;
	
	// When getting a set of WorkItems, start with the next one after this token
	private Long startToken;
	
	// flag to indicate we are in waiting mode so only 1 Waiting message is put in the queue
	private boolean waitingForEvent;
	
	
	// Constructors

	/**
	 * Constructs a newly allocated HistoryHandler object.
	 * @param filePath Path of the history xml file
	 * @param numPriorities Number of priorities that are defined
	 * @param queueManager Reference to QueueManager to determine if a queue is empty
	 * @param eventsAreSequential 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws HistoryException 
	 * @throws Exception
	 */
	public TokenHistoryHandler(String filePath, int numPriorities, WIQueueManager queueManager, boolean eventsAreSequential) throws IOException, HistoryException {
		super(filePath, numPriorities, queueManager);

		this.eventsAreSequential = eventsAreSequential;

		// read the file to get the tokens
		BufferedReader reader = new BufferedReader(new FileReader(histFile));
		String line = null;
		ArrayList<String> lines = new ArrayList<String>();
		while( (line = reader.readLine()) != null) {
			if (line.length() > 0) {
				// skip any blank lines that may be trailing due to an edit
				lines.add(line);
			}
		}

		completedTokens = new long[lines.size()];
		int i=0;
		for(String l : lines) {
			completedTokens[i++] = Long.parseLong(l);
		}

		if (completedTokens.length == 0) {
			throw new HistoryException("No initial token in the history file");
		}

		if (completedTokens.length != numPriorities) {
			throw new HistoryException("Number of entries in the history file "+filePath+" <"+completedTokens.length+
					"> does not match the number of priorities in the config file <"+numPriorities+">!");
		}

		highestFinishedToken = -1;
		for(long j : completedTokens) {
			highestFinishedToken = (j > highestFinishedToken ? j : highestFinishedToken);
		}

		// Get the starting point for the first workorder
		// we checked for 0 length array above
		long earliestToken = completedTokens[0];
		for(long tok : completedTokens) {
			earliestToken = (tok != 0 ? (earliestToken < tok ? earliestToken : tok) : earliestToken);
		}
		startToken = earliestToken;
		Log.info("StartToken: "+startToken);
	}

	// Methods

	
	/**
	 * getMaxProcessed returns the highest token processed
	 * @return max processed token
	 */
	public long getMaxProcessed() {
		long maxToken = completedTokens[0];
		for(long i : completedTokens) {
			maxToken = (i > maxToken ? i : maxToken);
		}
		return maxToken;
	}

	/**
	 * getLastToken returns the earliest (smallest) token from among the
	 * priorities being processed.
	 */
	public Long getStartToken() {
		return startToken;
	}

	/**
	 * shouldSchedule checks to see if this workItem is in the past for the priority it is in
	 * @param wi
	 * @return true if it is not in the past, false otherwise
	 */
	public void shouldSchedule(IWorkItem wi) {
		if (highestFinishedToken == -1) {
			// we are beyond the starting point
			return;
		} else if (wi.getToken().longValue() > highestFinishedToken) {
			highestFinishedToken = -1;
			return;
		} else if (!wi.isValid()) {
			// This token is invalid and shouldn't be considered in the history
			return;
		}

		// need to check this token
		int priority = wi.getPriority().intValue();
		if (priority >= 0 && priority < completedTokens.length) {
			// set the item invalid if it is valid and has already been processed
			wi.setValid(completedTokens[priority] < wi.getToken().longValue());
		} else {
			// Don't know about the priority yet, add this priority to the completedTokens array
			long[] newCompletedTokens = new long[priority+1];
			for(int i=0; i<completedTokens.length; i++) {
				newCompletedTokens[i] = completedTokens[i];
			}
			for(int i=completedTokens.length; i<newCompletedTokens.length; i++) {
				// set this new one to one before this token
				newCompletedTokens[i] = wi.getToken().longValue() - 1;
			}
			completedTokens = newCompletedTokens;
		}
	}

	/**
	 * Writes the history file to disk
	 * This method is only called during a save.  Synchronization is handled at the WIQueueManager lock.
	 * @param priority Priority of the last token
	 * @throws IOException 
	 * @throws HistoryException 
	 */
	public void writeHistory(HistoryHandler historyHandler) throws IOException, HistoryException {
		// notify other handlers to write their history
		super.writeHistory(historyHandler);

		// now write my history
		FileOutputStream fileHandler = null;
		LinkedList<HistoryToken> sqList = null;
		Long maxCompletedToken = START_TOKEN;
		ArrayList<String> completedList = new ArrayList<String>();
		
		// setup to log completed
		synchronized(saveHistoryQueues) {
			logBuffer.setLength(0);
			logBuffer.append(Thread.currentThread().getName()).append(" ");
			logBuffer.append("Completed ");
			int baseLength = logBuffer.length();
			boolean valuesModified = false;
			for(Integer intVal : priorityInts) {
				sqList = saveHistoryQueues.get(intVal);
				boolean done=false;
				Long newToken = null;
				while(!done && sqList.size()> 0) {
					HistoryToken token = sqList.removeFirst();
					if (!token.isSaved()) {
						// hit the end of saved workItems
						done = true;
						sqList.addFirst(token);
					} else {
						newToken = token.getWorkItem().getToken();
						// now write out the saved messages
						if (Log.isInfoEnabled()) {
							logBuffer.setLength(baseLength);
							logBuffer.append(token.getWorkItem().getTag());
							token.addStats(logBuffer);
							completedList.add(logBuffer.toString());
						}
					}
				}
				if (newToken != null) {
					// something on this priority was saved so updated the completed timestamp
					if (completedTokens[intVal.intValue()] > newToken) {
						Log.error("Just processed a token from the past! newToken<"+newToken+">  currentToken < "+completedTokens[intVal.intValue()]+">");
					} else {
						completedTokens[intVal.intValue()] = newToken;
						maxCompletedToken = (maxCompletedToken < newToken ? newToken : maxCompletedToken);
						valuesModified = true;
					}
				}
			}

			if (!valuesModified) {
				// None of the head items are done so don't change the log
				return;
			}

			// Check to see if priorities should be moved due to empty queues
			for(Integer intVal : priorityInts) {
				sqList = saveHistoryQueues.get(intVal);
				if ((sqList.size() == 0) && 
						(queueManager.queueSize(intVal) == 0) && 
						(completedTokens[intVal.intValue()] < maxCompletedToken)) {

					sb.setLength(0);
					sb.append("Moving p").append(intVal).append(" event from ").append(completedTokens[intVal.intValue()]).append(" to ").append(maxCompletedToken);
					Log.debug(sb.toString());
					// only move it forward if it is behind this token
					completedTokens[intVal.intValue()] = maxCompletedToken;
				}
			}

			sb.setLength(0);
			// now build the output string
			for(long i : completedTokens ) {
				sb.append(i).append("\n");
			}

			// Now open the file and write it out
			fileHandler = new FileOutputStream(histFile);
			fileHandler.write(sb.toString().getBytes());
			fileHandler.close();

			// now write out the saved messages
			for(String token : completedList) {
				Log.info(token);
			}
		}
	}

	/**
	 * parameterized getInstance is called to set up the singleton history handler
	 * @param filePath
	 * @param numPriorities
	 * @param queueManager
	 * @param eventsAreSequential 
	 * @throws IOException
	 * @throws HistoryException
	 */
	static public TokenHistoryHandler getInstance(String filePath, int numPriorities, WIQueueManager queueManager, boolean eventsAreSequential) throws IOException, HistoryException {
		if (historyHandler != null) {
			throw new HistoryException("TokenHistoryHandler already initialized");
		}
		historyHandler = new TokenHistoryHandler(filePath, numPriorities, queueManager, eventsAreSequential);

		return historyHandler;
	}

	/**
	 * getInstance returns the singleton handler
	 * @return
	 */
	static public TokenHistoryHandler getInstance() {
		return historyHandler;
	}

	@Override
	public boolean scheduleWorkItem(IWorkItem workItem) {
		Long token = workItem.getToken();
		if ((token == startToken+1) || !eventsAreSequential) {
			waitingForEvent = false;
			startToken = token;

			// check priority
			if (validateWIPriority(workItem)) {
				if (Log.isInfoEnabled()) {
					queueMsg.setLength(0);
					queueMsg.append(workItem.getTag()).append("Resetting priority to lowest known");
					Log.info(queueMsg.toString());
				}
			}
			
			// Schedule the item
			queueManager.push(workItem);
			// check it after pushing it into the queue as it may change priority as part of the push
			shouldSchedule(workItem);

			if (Log.isInfoEnabled()) {
				queueMsg.setLength(0);
				queueMsg.append(workItem.getPackageType()).append(" ").append(workItem.getFullTag());
				Log.info(queueMsg.toString());
			}
			// this Item scheduled
			return true;
		} else {
			if (!waitingForEvent) {
				waitingForEvent = true;
				// The next sequential event is not in the list.  Probably a timing issue in the DB.
				if (Log.isInfoEnabled()) {
					queueMsg.setLength(0);
					queueMsg.append("Waiting for WorkItem "+(startToken+1));
					Log.info(queueMsg.toString());
				}
			}
			// this item didn't schedule
			return false;
		}	
	}
}