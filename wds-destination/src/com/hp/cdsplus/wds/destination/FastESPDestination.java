package com.hp.cdsplus.wds.destination;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.transform.Templates;

import org.w3c.dom.Element;

import com.fastsearch.esp.content.DocumentFactory;
import com.fastsearch.esp.content.DocumentFeederFactory;
import com.fastsearch.esp.content.DuplicateElementException;
import com.fastsearch.esp.content.FactoryException;
import com.fastsearch.esp.content.IDocument;
import com.fastsearch.esp.content.IDocumentFeeder;
import com.fastsearch.esp.content.IDocumentFeederFactory;
import com.fastsearch.esp.content.IDocumentFeederStatus;
import com.fastsearch.esp.content.ShutdownException;
import com.fastsearch.esp.content.errors.DocumentError;
import com.fastsearch.esp.content.errors.DocumentWarning;
import com.fastsearch.esp.content.util.Pair;
import com.hp.cdsplus.util.logger.LoggerUtil;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;
import com.hp.cdsplus.wds.exception.RetryException;


public class FastESPDestination implements IDestination {

	private String destination;
	private String collection;
	private String docPrefix;
	private int batchsize;
	private int activeDocumentLimit;
	private int maxDocsInBatch;
	private long initialDelay = 60000;
	private long deltaDelay;
	private IDocumentFeederFactory documentFeederFactory = null;
	private IDocumentFeeder documentFeeder = null;
	private Properties p = new Properties();
		
	public void init(Element root, File workingDir) throws DestinationException {
		destination 		= root.getAttribute( "url");
		collection  		= root.getAttribute( "collection");
		docPrefix 			= root.getAttribute( "docPrefix" );
		batchsize			= Integer.parseInt(root.getAttribute("batchSize"));
		activeDocumentLimit = Integer.parseInt(root.getAttribute("activeDocumentLimit"));
		maxDocsInBatch 		= Integer.parseInt(root.getAttribute("maxDocsInBatch"));
		deltaDelay 			= Integer.parseInt(root.getAttribute("delayInterval"));
			p.put("com.fastsearch.esp.content.http.contentdistributors", destination);
			
			/* the timer is being introduced for FAST ESP destination implementation 
			 * the task being done by the timer is to get the status report every 2 mins
			 * FastESPDestination will handle WARNING/FAILURE/ERROR messages from the report.
			 */

			TimerTask timerTask = new ReportTask();
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(timerTask, initialDelay, deltaDelay);
	
			getDocumentFeeder();
	}
	public synchronized void getDocumentFeeder() throws DestinationException{
		try {
			if(documentFeederFactory == null)
					documentFeederFactory 	= DocumentFeederFactory.newInstance();
		} catch (FactoryException e) {
			throw new NonRetryException("Cannot obtain DocumentFeederFactory instance", e);
		}
		try{
			if(documentFeeder ==  null)
				documentFeeder = documentFeederFactory.createDocumentFeeder(collection, p);
		} catch (FactoryException e) {
			throw new NonRetryException("Cannot obtain DocumentFeeder instance", e);
		}

		/*
		 * a.	The default size limit for one batch is 1024 KB
		 * b.	The default limit for the number of documents in one batch is 1000
		 * c.	The default limit for the size of all pending batches is 100MB
		 * 
		 * following setters allow us to customize the above values.
		 */
		documentFeeder.setBatchSizeKB(batchsize);
		documentFeeder.setMaxDocsInBatch(maxDocsInBatch);
		documentFeeder.setActiveDocumentsLimitMB(activeDocumentLimit);

	}
	

	public boolean remove(String location) throws DestinationException{
		if(documentFeeder == null)
			getDocumentFeeder();
		try {
			documentFeeder.removeDocument(docPrefix + location);
		}
		catch (ShutdownException e) {
			throw new RetryException("Remote System has been shutdown.",e);
		}
		return true;
	}

	public boolean finalizeDest(boolean force){
		// as per recommendation from microsoft fast team we dont have to deactivate feeder.
		return true;
	}

	public static void printDocumentFeederStatus(IDocumentFeederStatus errorReport) {
		if (errorReport.hasDocumentErrors() == false && errorReport.hasDocumentWarnings() == false) {
			LoggerUtil.info("Documents processed and persisted successfully by FAST ESP");
			return;
		}

		if (errorReport.hasDocumentErrors()) {
			printDocumentErrors(errorReport.getAllDocumentErrors());
		}

		if (errorReport.hasDocumentWarnings()) {
			printDocumentWarnings(errorReport.getDocumentWarnings());
		}
	}

	public static void printDocumentErrors(Collection<?> errors) {

		LoggerUtil.error("Document errors:");
		Iterator<?> it = errors.iterator();
		while (it.hasNext()) {
			Pair elem = (Pair) it.next();
			DocumentError docError = (DocumentError) elem.getSecond();
			Long operationId = (Long) elem.getFirst();
			LoggerUtil.error("Operation id:" + operationId + " Error:" + docError.toString());
		}
	}

	public static void printDocumentWarnings(Collection<?> warnings) {

		LoggerUtil.warn("Document warnings:");
		Iterator<?> it = warnings.iterator();
		while (it.hasNext()) {
			Pair elem = (Pair) it.next();
			DocumentWarning docWarning = (DocumentWarning) elem.getSecond();
			Long operationId = (Long) elem.getFirst();
			LoggerUtil.error("Operation id:" + operationId + " Error:" + docWarning.toString());
		}
	}

	private class ReportTask extends TimerTask{

		public ReportTask(){
			LoggerUtil.info("ReportTask Instantiated");
		}
		
		public void run() {
			if(documentFeeder != null) {
				IDocumentFeederStatus status = documentFeeder.getStatusReport();
				printDocumentFeederStatus(status);
			}
		}
	}

	
	public boolean put(String location, byte[] bytes) throws DestinationException {
		if(documentFeeder == null)
			getDocumentFeeder();
		IDocument document = DocumentFactory.newDocument(docPrefix + location);
		try {
			document.addElement(DocumentFactory.newByteArray("data", bytes));
			// Have the Document Feeder send the Document to FAST ESP
			documentFeeder.addDocument(document);
			
			// here we trigger off the timer. Timer task will just pull the Status report and print it.
		}
		catch (ShutdownException e) {
			throw new RetryException("Remote System has been shutdown", e);
		}
		catch (DuplicateElementException e) {
			LoggerUtil.error(e.getMessage());
		}
		return true;
	}
	
	public boolean needStoreSync() {
		return false;
	}
	public boolean remove(String location, Templates sTemplates)
			throws DestinationException {
		// TODO Auto-generated method stub
		return false;
	}
}