package com.hp.soar.priorityLoader.workItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.ProductNames;

public class SoarExtractItem extends SoarItem {

	// replaced itemId
	private SoarItem replacedItem = null;
	

	public SoarExtractItem(SoarExtractionDBService dbService, String eventId,
			String objectId, boolean isICM, String icmRule, String chronicleId, String collectionId, String itemId,
			String itemState, boolean isSuspended, boolean collectionAccepted,
			Date lastExtractionTime, ProductNames prodNamesMap)
			throws DfException, ProcessingException, IOException {
		super(dbService, eventId, objectId, isICM, icmRule, chronicleId, collectionId, itemId,
				itemState, isSuspended, collectionAccepted, lastExtractionTime, prodNamesMap);

		// see if this item is replacing another one
		String replacedItemId = dbObj.getString("replaces_item_id");
		if (replacedItemId != null && replacedItemId.length() > 0) {
			replacedItem = loadReplacedItem(eventId, replacedItemId, lastExtractionTime, collectionAccepted);
		}
		
	}

	/**
	 * loadReplacedItem builds the tree for a replaced item
	 * @param eventId
	 * @param replacedItemId
	 * @param lastExtractionTime
	 * @param collectionAccepted TODO
	 * @return
	 * @throws DfException 
	 * @throws ProcessingException 
	 * @throws IOException 
	 */
	private SoarItem loadReplacedItem(String eventId, String replacedItemId, Date lastExtractionTime, boolean collectionAccepted) throws DfException, 
																										ProcessingException, IOException{
		IDfCollection results = null;

		StringBuffer dqlQuery = new StringBuffer();
		dqlQuery.append("SELECT distinct r_object_id, item_id, icm_driver, icm_rule_id, i_chronicle_id, item_state, is_suspended FROM sw_item (ALL) ");
		dqlQuery.append("WHERE collection_id='").append(collectionId).append("' AND item_id='").append(replacedItemId).append("'");
		String dqlStr = dqlQuery.toString();
		/*
		 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- COnverting the logger from info to debug to 
		 * avoid DQL statements in the log
		 */
		//LoaderLog.info("getReplacedSoftwareItem DQL " + dqlStr);
		LoaderLog.debug("getReplacedSoftwareItem DQL " + dqlStr);
		
		SoarItem swItem = null;

		try {
			results = dbService.getResults(dqlStr, "getReplacedSoftwareItem");
			if (results != null) {
				while (results.next()) {
					String itemId = results.getString("item_id");
					String itemState = results.getString("item_state");
					boolean isSuspended = results.getBoolean("is_suspended");
					String chronicleId = results.getString("i_chronicle_id");
					String objectId = results.getString("r_object_id");
					// ICM changes
					boolean isICM = results.getBoolean("icm_driver");
					String icmRule = results.getString("icm_rule_id");

					// replaced Item is always flagged as obsolete so that it can be removed, even if the properties have been changed.
					return new SoarItem(dbService, eventId, objectId, isICM, icmRule, chronicleId, collectionId, itemId, OBSOLETE, false, collectionAccepted, null, prodNamesMap);
				}
			}
		} catch (DfException e) {
			LoaderLog.error("Error in getReplacedSoftwareItem "+ e.getMessage());
			throw e;
		} catch (FileNotFoundException e) {
			LoaderLog.error("Error in getReplacedSoftwareItem "+ e.getMessage());
			throw e;
		} catch (ProcessingException e) {
			LoaderLog.error("Error in getReplacedSoftwareItem "+ e.getMessage());
			throw e;
		} finally {
			DocbaseUtils.closeResults(results);
		}

		return null;
	}

	/**
	 * doUpdates handles updating of a soar item
	 * @param publishedLocations
	 * @throws ProcessingException
	 */
	public void doUpdates(HashSet<String> publishedLocations) throws ProcessingException {
		super.doUpdates();
		if (replacedItem != null) {
			// This item is being replaced so delete anything that isn't in it. 
			replacedItem.doDelete(publishedLocations);
		}
	}
	
	/**
	 * doDelete queues the softwareFiles for future deletion
	 * @param publishedLocations 
	 */
	public void doDelete(HashSet<String> publishedLocations) throws ProcessingException {
		
		super.doDelete(publishedLocations);
		
		if (replacedItem != null) {
			// queue the deletes for the replaced item
			replacedItem.doDelete(publishedLocations);
		}
	}

	public void getPublishedLocations(HashSet<String> publishedLocations) {
		super.getPublishedLocations(publishedLocations);
		
		if (replacedItem != null) {
			// get duplicates from the replaced Item
			replacedItem.getPublishedLocations(publishedLocations);
		}		
	}

}
