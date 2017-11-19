package com.hp.loader.single.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.hp.loader.history.RevisitHistoryHandler;
import com.hp.loader.utils.ThreadLog;
import com.hp.loader.workItem.IWorkItem;

abstract public class SingleManualRepublish implements Runnable {
	
	public static final String MANUALREPUBTAG = "_republish.txt";

	private ArrayList<IWorkItem> itemsToLoad = new ArrayList<IWorkItem>();
	private File repubFile;
	private String fileName;
	private Thread t;
	public static boolean isManualRepubDone = false; //Flag to notify Batch Mode
	
	public SingleManualRepublish(File repubFile) throws IOException {
		this.fileName = repubFile.getAbsolutePath();
		this.repubFile = repubFile;
		File parent = repubFile.getParentFile();
		if (!parent.exists()) {
			ThreadLog.info("Creating directory for manual republish: "+repubFile);
			if (!parent.mkdirs()) {
				throw new IOException("Unable to create manual republish directory: "+parent.getAbsolutePath());
			}
		}
		// start the thread running
		t = new Thread(this,"ManP");
		t.setDaemon(true);
		t.start();
	}
	
	public void addRevisitItems(Long token, RevisitHistoryHandler handler)  {
		if (itemsToLoad.size() > 0) {
			synchronized (itemsToLoad) {
				for(IWorkItem item : itemsToLoad) {
					handler.addRevisit(token, item);
				}
				
				itemsToLoad.clear();
			}
		}
	}
	
	/**
	 * loadManualPublishItems is overridden by each specific loader type
	 * @param fis
	 * @throws IOException
	 */
	abstract public ArrayList<IWorkItem> loadManualPublishItems(FileInputStream fis) throws IOException;
	
	public void run() {
		boolean loadFailing = false;
		
		// loop forever
		while( true ) {
			try {
				if (loadFailing) {
					// the last load could not be moved or renamed don't load it again.
					if (!repubFile.exists()) {
						//the file has been removed so start again
						loadFailing = false;
						
					}
				} else {
					if (repubFile.exists()) {
						isManualRepubDone =false;
						ArrayList<IWorkItem> newItems = loadManualPublishItems(new FileInputStream(repubFile));
						synchronized(itemsToLoad) {
							itemsToLoad.addAll(newItems);
							// make sure the items aren't being held in the memory
							newItems.clear();
						}
						// if the file fails to delete, stop trying to load it
						loadFailing = !repubFile.delete();
					}else{
						isManualRepubDone =true;
					}
				}
					try {
						// sleep for 2 minutes between load requests
						Thread.sleep(60*2*1000);
					} catch (InterruptedException ie){}
				
			} catch (Exception e) {
				// something happened on ingestion so move the file to err and make a new one
				File errorFile = new File(fileName+".err");
				loadFailing = !repubFile.renameTo(errorFile);
				repubFile = new File(fileName);
			}
		}

	}

}
