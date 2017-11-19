package com.hp.cdsplus.wds.workItem;

import com.hp.cdsplus.wds.destination.IDestination;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.loader.workItem.IWorkItem;

public class CDSPContentItem {
	String destination;
	byte[] content;
	CDSPSelection selection;

	public CDSPContentItem(String destination, byte[] content, CDSPSelection selection) {
		this.destination=destination;
		this.content = content;
		this.selection=selection;
	}
	
	
	

	/**
	 * Have the selection finalize its destinations.
	 * @return
	 * @throws DestinationException 
	 */
	public boolean finalizeAll() throws DestinationException {
		return selection.finalizeAll();
	}
	
	/**
	 * get whether any destination requires syncing.
	 * @return
	 */
	public boolean needStoreSync() {
		return selection.needStoreSync();
	}
	
	/**
	 * store puts the content into the 
	 * @param eventType
	 * @return
	 * @throws DestinationException
	 */
	public boolean store(String eventType) throws DestinationException {
		boolean storeCompleted = true;
		for(IDestination destination : selection.getDestinations()) {
			if (eventType == IWorkItem.EVENT_DELETE) {
				if(selection.getStyleSheetTemplates()==null){
					storeCompleted &= destination.remove(this.destination);
				}
				else{
					storeCompleted &= destination.remove(this.destination, selection.getStyleSheetTemplates());
				}
			} else {
				storeCompleted &= destination.put(this.destination, content);
			}
		}
		return storeCompleted;
	}
}
