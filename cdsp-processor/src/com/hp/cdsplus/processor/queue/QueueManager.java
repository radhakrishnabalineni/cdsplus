package com.hp.cdsplus.processor.queue;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import com.hp.cdsplus.processor.item.CGSContentItem;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;

/**
 */
public class QueueManager {
	
	private ConcurrentLinkedQueue<Item> queue;
	private ConcurrentSkipListSet<String> contentTypeSet;
	private ArrayList<String> currentContentTypelist;
	
	private boolean isExit = false;
	public QueueManager(){
		queue = new ConcurrentLinkedQueue<Item>();
		contentTypeSet = new ConcurrentSkipListSet<String>();
		setCurrentContentTypeSet(new ArrayList<String>());
	}
	
	/**
	 * Method push.
	 * @param item Item
	 */
	public void push(Item item){
		synchronized(queue){
			if(item instanceof ContentItem || item instanceof CGSContentItem){
				if(!contentTypeSet.contains(item.getContentType())){
					this.queue.add(item);
					this.contentTypeSet.add(item.getContentType());
				} 
			}else
				this.queue.add(item);
			queue.notifyAll();
		}
		
	}
	
	/**
	 * Method pop.
	 * @return Item
	 */
	public Item pop(){
		Item item = null;
		if(this.isExit || this.queue.isEmpty())
			return null;
		else{
			synchronized(queue){
				item = this.queue.remove();
			}
			return item;
		}
	}
	
	public void cleanUpContentItem(Item item){
		if (item != null && (item instanceof ContentItem || item instanceof CGSContentItem)){
			synchronized (contentTypeSet) {
				contentTypeSet.remove(item.getContentType());
			}
			
		}
	}
	
	/**
	 * Method size.
	 * @return int
	 */
	public int size(){
		return this.queue.size();
	}
	
	/**
	 * Method isEmpty.
	 * @return boolean
	 */
	public boolean isEmpty(){
		if(this.queue != null && this.queue.size() == 0)
			return true;
		else return false;
	}
	
	public int getContentItemSize(){
		return this.contentTypeSet.size();
	}
	
	public int getWorkItemSize(){
		return this.queue.size() - this.contentTypeSet.size();
	}

	/**
	 * @return the isExit
	 */
	public boolean isExit() {
		return isExit;
	}

	/**
	 * @param isExit the isExit to set
	 */
	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}

	public void cleanUp() {
		synchronized(queue){
			this.queue.clear();
		}
		this.contentTypeSet.clear();
	}

	/**
	 * @return the contentTypeSet
	 */
	public ConcurrentSkipListSet<String> getContentTypeSet() {
		return contentTypeSet;
	}

	/**
	 * @param contentTypeSet the contentTypeSet to set
	 */
	public void setContentTypeSet(ConcurrentSkipListSet<String> contentTypeSet) {
		this.contentTypeSet = contentTypeSet;
	}

	public void setCurrentContentTypeSet(ArrayList<String> currentContentTypeSet) {
		this.currentContentTypelist = currentContentTypeSet;
	}

	public ArrayList<String> getCurrentContentTypeSet() {
		return currentContentTypelist;
	}

	
	
}
