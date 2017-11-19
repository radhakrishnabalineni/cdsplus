/**
 * 
 */
package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.cdspDestination.ProjContent;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.utils.Log;
import com.hp.loader.workItem.WorkItem;
import com.hp.soar.priorityLoader.filter.AndFilterNode;
import com.hp.soar.priorityLoader.filter.FilterNode;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.ExtractorUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.VirusScanException;
import com.hp.soar.priorityLoader.utils.VirusScanner;

/**
 * @author dahlm
 *
 */
public class SoarExtractCollection extends SoarExtractElement {
	
	private static final String SOAR_SOFTWARE_FEED_V_E4_XSD = "soar-software-feed-vE4.xsd"; // CDSPlus
	
	private static final String EXTCOLDEL = "EXTCOLDEL";
	private static final String EXTCOLDELFAIL = "EXTCOLDELFAIL";
	private static final String EXTCOLUPD = "EXTCOLUPD";
	private static final String EXTCOLUPDFAIL = "EXTCOLUPDFAIL";
	private static FilterNode filter = null;
	
	// last time this collection was extracted
	Date lastExtractionTimeStamp;

	// swItems that belong to this collection
	ArrayList<SoarExtractItem> swItems = new ArrayList<SoarExtractItem>();

	// attachments belonging to this collection to be stored on CDS+
	ArrayList<SoarExtractAttachment> attachments = null;
	
	// eventId for this collection
	protected String eventId;
	
	// chronicleId of the collection
	protected String chronicleId;

	// collectionId of the collection
	private String collectionId;

	// cds+ metadata file to be stored
	protected ProjContent projContent;
	
	// inSubscription holds value of the subscription filter
	private boolean inSubscription = true;
	
	// new publish state of the collection 
	private boolean shouldPublish = true;
	
	// current publish state of the collection
	private boolean published;
	
	// number of times a store is attempted
	private int	retryCount = 0;
	
	

	/**
	 * @param eventId 
	 * @param chronicleId 
	 * @param updateType
	 * @throws DfException 
	 */
	public SoarExtractCollection(SoarExtractionDBService dbService, String eventId, String objectId, String chronicleId, String collectionId, String updateType) throws DfException {
		super(dbService, objectId, updateType);
		
		this.eventId = eventId;
		this.chronicleId = chronicleId;
		this.collectionId = collectionId;
		lastExtractionTimeStamp = dbService.getLastExtractionTimeStamp(collectionId, chronicleId);
	}
	
	/**
	 * addCollectionAttributes adds all required attributes to the collection element
	 * @param collectionElement
	 * @throws DfException
	 */
	private void addCollectionAttributes(Element collectionElement) throws DfException {
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "collection-ID", dbObj.getString("collection_id"), false);
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "state", dbObj.getString("collection_state"), false);
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "partner-update-type", updateType, false);
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "soar-msg-ID", "SOAR-" + System.currentTimeMillis(), false);
		// set the submittal type oid 
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "submittal-type-oid", dbObj.getString("submittal_type_oid"), false);
		SoarExtractXMLBuilder.addXMLAttribute(collectionElement, "submittal-group-oid", dbObj.getString("submittal_group_oid"), false);
	}
	
	/**
	 * addCollectionElements add all required subelements to the collection element
	 * @param dbObj
	 * @param collectionElement
	 * @throws DfException
	 * @throws ProcessingException
	 */
	 private void addCollectionElements(Element collectionElement) throws DfException, ProcessingException {

		 SoarExtractXMLBuilder.addXMLElement(collectionElement,"collection-title", dbObj.getString("title"));
		 SoarExtractXMLBuilder.addXMLElement(collectionElement,"collection-description", dbObj.getString("description").replace('\n', ' '));

		 // Add update-types
		 addUpdateTypeElement(collectionElement);

		 // Add update-priority
		 String subGrpOid = dbObj.getString("submittal_priority_oid");
		 Element updPriElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "update-priority", 
				 ReferenceLists.getLabel(ReferenceLists.SUBMITTAL_PRIORITIES_LIST, subGrpOid));

		 SoarExtractXMLBuilder.addXMLAttribute(updPriElement, "oid", subGrpOid, false);

		 // Add update-date
		 String updDtVal = getDateValue(dbObj, "update_date", true);
		 SoarExtractXMLBuilder.addXMLElement(collectionElement, "update-date", updDtVal);

		 // Add software-type
		 addSoftwareTypeElement(collectionElement);

		 // Add product-line
		 String submittalGrpName = getStringValue(dbObj, "submittal_group_oid");
		 SoarExtractXMLBuilder.addXMLElement(collectionElement, "product-line",
				 ReferenceLists.getLabel(ReferenceLists.PRODUCT_LINE_CODE_LIST, submittalGrpName));

		 // Add contacts
		 addContactsElement(collectionElement, submittalGrpName);

		 // Add compression-utility
		 String comUtilOid = getStringValue(dbObj, "compression_utility_oid");
		 Element comUtilElement = SoarExtractXMLBuilder.addXMLElement( collectionElement, "compression-utility", 
				 ReferenceLists.getLabel(ReferenceLists.COMPRESSION_UTILTIES_LIST, comUtilOid));

		 SoarExtractXMLBuilder.addXMLAttribute(comUtilElement, "oid", comUtilOid, false);

		 addDmsDocumentsElement(collectionElement);

		 addCopyrightElement( collectionElement);

		 addRelatedCollectionsElement( collectionElement);

		 String[] flagOids = getRepeatingAttr(dbObj, "flag_oids");

		 String registrationFlag = hasStringValue(flagOids, "2");
		 SoarExtractXMLBuilder.addXMLElement(collectionElement, "registration-flag", registrationFlag);

		 String notificationFlag = hasStringValue(flagOids, "1");
		 SoarExtractXMLBuilder.addXMLElement(collectionElement, "notification-flag", notificationFlag);

		 addFlagOidsElementForComponent(collectionElement, flagOids, "COLLECTION");

		 // no itemId at the collection level
		 addAttachmentsElements( collectionElement, attachments, collectionId, null);
		 
		 // availability-schedules is always empty
		 SoarExtractXMLBuilder.addXMLElement(collectionElement, "availability-schedules");
	 }

	 private void addContactsElement(Element collectionElement, String submittalGrpName) {
		 Element contactsElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "contacts");

		 Element swGenContactElement = SoarExtractXMLBuilder.addXMLElement(contactsElement, "contact");
		 SoarExtractXMLBuilder.addXMLAttribute(swGenContactElement,"contact-type", "Software Generation Contact", false);

		 SoarExtractXMLBuilder.addXMLElement(swGenContactElement, "contact-name",
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_NAMES_LIST, submittalGrpName));
		 SoarExtractXMLBuilder.addXMLElement(swGenContactElement, "contact-phone", 
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_PHONES_LIST, submittalGrpName));
		 SoarExtractXMLBuilder.addXMLElement(swGenContactElement, "contact-email", 
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_EMAILS_LIST, submittalGrpName));

		 Element soarColContactElement = SoarExtractXMLBuilder.addXMLElement(contactsElement, "contact");
		 SoarExtractXMLBuilder.addXMLAttribute(soarColContactElement, "contact-type","SOAR Collection Contact", false);

		 SoarExtractXMLBuilder.addXMLElement(soarColContactElement, "contact-name",
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_NAMES2_LIST, submittalGrpName));
		 SoarExtractXMLBuilder.addXMLElement(soarColContactElement, "contact-phone", 
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_PHONES2_LIST, submittalGrpName));
		 SoarExtractXMLBuilder.addXMLElement(soarColContactElement, "contact-email", 
				 ReferenceLists.getLabel(ReferenceLists.CONTACT_EMAILS2_LIST, submittalGrpName));
	 }

	 private void addCopyrightElement(Element collectionElement) throws DfException {
		 Element copyRightElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "copyright");

		 String cpRtDate = getStringValue(dbObj, "copyright_date");
		 if (cpRtDate.equals("")) {
			 Element hpCopyRightNoticeElement = SoarExtractXMLBuilder.addXMLElement(copyRightElement,"hp-copyright-notice");
			 SoarExtractXMLBuilder.addXMLAttribute(hpCopyRightNoticeElement, "nil", "true", true);
		 } else {
			 Element hpCopyRightNoticeElement = SoarExtractXMLBuilder.addXMLElement(copyRightElement, 
					 "hp-copyright-notice", "Copyright � " + cpRtDate + " Hewlett-Packard Company");
			 SoarExtractXMLBuilder.addXMLAttribute(hpCopyRightNoticeElement, "nil", "false", true);
		 }

		 String cpRtAck = getStringValue(dbObj, "copyright_acknowledgement");
		 if (cpRtAck.equals("")) {
			 Element hpCopyRightAckElement = SoarExtractXMLBuilder.addXMLElement(copyRightElement, "additional-copyright-ack");
			 SoarExtractXMLBuilder.addXMLAttribute(hpCopyRightAckElement, "nil", "true", true);
		 } else {
			 Element hpCopyRightAckElement = SoarExtractXMLBuilder.addXMLElement(copyRightElement, "additional-copyright-ack", cpRtAck);
			 SoarExtractXMLBuilder.addXMLAttribute(hpCopyRightAckElement, "nil", "false", true);
		 }
	 }

	 private void addDmsDocumentsElement(Element collectionElement) throws DfException {
		 IDfCollection results = null;
		 Element dmsDocsElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "dms-documents");

		 String[] dmsDocsList = getRepeatingAttr(dbObj, "dms_documents");
		 if (dmsDocsList.length > 0) {
			 String dql = "SELECT object_name, title FROM dm_dbo.dms_cache WHERE i_chronicle_id in (" + getSQLInString(dmsDocsList) + ")";
			 try {
				 results = dbService.getResults(dql, "Query dms_cach table");
				 while (results.next()) {
					 Element dmsDocElement = SoarExtractXMLBuilder.addXMLElement(dmsDocsElement, "dms-document",results.getString("title"));
					 SoarExtractXMLBuilder.addXMLAttribute(dmsDocElement,"dms-doc-ID", results.getString("object_name"),false);
				 }
			 } finally {
				 DocbaseUtils.closeResults(results);
			 }
		 }
	 }
	 
	 private void addRelatedCollectionsElement(Element collectionElement) throws DfException {
		 String[] relCols = getRepeatingAttr(dbObj, "related_collections");
		 String[] relTypeOids = getRepeatingAttr(dbObj, "relationship_type_oids");

		 Element relColsMainElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "related-collections");
		 for(int i=0; i<relCols.length && i < relTypeOids.length; i++) {
			 Element relColElement = SoarExtractXMLBuilder.addXMLElement(relColsMainElement, "related-collection");

			 SoarExtractXMLBuilder.addXMLAttribute(relColElement, "collection-ID", relCols[i], false);

			 Element relEle = SoarExtractXMLBuilder.addXMLElement(relColElement, "relationship", 
					 ReferenceLists.getLabel(ReferenceLists.RELATIONSHIP_TYPES_LIST, relTypeOids[i]));

			 SoarExtractXMLBuilder.addXMLAttribute(relEle, "oid-ID", relTypeOids[i], false);
		 }
	 }

	 private void addSoftwareItems(Element collectionElement) throws ProcessingException {
		// StringBuffer dql = new StringBuffer();

		 Element itemsMainElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "software-items");
		 int count = 0;
		 for (SoarExtractItem item : swItems) {
			 LoaderLog.info("EXTRACTING ITEM "+(++count)+" of "+swItems.size()+" <"+item.getItemId()+ "> from COLLECTION : "+collectionId);
			 try {
				 item.addElement(itemsMainElement);
			 } catch (DfException e) {
				 String msg = "Failed to extract Software Item :" + item.getItemId() + "; cause -->" + ExtractorUtils.getStackTrace(e);
				 LoaderLog.error(msg);
				 item.setExtractMsg(msg);
				 item.setExtractResult(SoarExtractElement.ITEMEXTFAILED);
			 }

		 }

	 }
	 
	/**
	 * addSoftwareTypeElement to the collection element
	 * @param collectionElement
	 * @throws DfException
	 */
	private void addSoftwareTypeElement(Element collectionElement) throws DfException {

		Element softwareTypeElement = SoarExtractXMLBuilder.addXMLElement(collectionElement, "software-type");

		SoarExtractXMLBuilder.addXMLAttribute(softwareTypeElement,  "publish-software-type", getBooleanValue(dbObj, "publish_software_type"), false);

		String swTypeOid = getStringValue(dbObj, "software_type_oid");
		Element swTypeElement = SoarExtractXMLBuilder.addXMLElement(softwareTypeElement, "sw-type", 
				ReferenceLists.getLabel(ReferenceLists.SOFTWARE_TYPES_LIST, swTypeOid));
		SoarExtractXMLBuilder.addXMLAttribute(swTypeElement, "oid", swTypeOid, false);

		String softwareSubTypeOid = getStringValue(dbObj, "software_sub_type_oid");
		if (!softwareSubTypeOid.equals("")) {
			Element swSubTypeElement = SoarExtractXMLBuilder.addXMLElement( softwareTypeElement, "software-sub-type",
					ReferenceLists.getLabel(ReferenceLists.SOFTWARE_SUB_TYPES_LIST, softwareSubTypeOid));

			SoarExtractXMLBuilder.addXMLAttribute(swSubTypeElement, "oid", softwareSubTypeOid, false);
		}
	}

	/**
	 * addUpdateTypeElement puts the update types into a collection
	 * @param collectionElement
	 * @throws DfException
	 */
	private void addUpdateTypeElement(Element collectionElement) throws DfException {
		Element updateTypeElement = SoarExtractXMLBuilder.addXMLElement(
				collectionElement, "update-types");
		String[] updateTypeOids = getRepeatingAttr(dbObj, "update_type_oids");
		for(String updateTypeOid : updateTypeOids) {
			Element updTypeElement = SoarExtractXMLBuilder.addXMLElement(updateTypeElement, "update-type", 
					ReferenceLists.getLabel(ReferenceLists.UPDATE_TYPES_LIST, updateTypeOid));
			SoarExtractXMLBuilder.addXMLAttribute(updTypeElement, "oid", updateTypeOid, false);
		}
	}
	  
	/**
	 * checkItems determines if any item failed to load and throws an exception if it did.
	 * @throws ProcessingException
	 */
	public void checkItems() throws ProcessingException {
		StringBuffer sb = new StringBuffer();
		for(SoarExtractItem item : swItems) {
			if (item.getExtractResult() != ITEMEXTRACTED) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(item.getExtractMsg());
			}
		}
		if (sb.length() > 0) {
		  // something failed to extract so the collection won't extract
			throw new ProcessingException(sb.toString(), false);
		}
	}
	
	/**
	 * 
	 * @param session
	 * @throws DfException
	 * @throws ProcessingException
	 * @throws IOException
	 * @throws VirusScanException 
	 * @throws InterruptedException 
	 */
	public boolean load() throws DfException, ProcessingException, IOException, VirusScanException, InterruptedException {
		
		// load the collection object from the docbase
		boolean hasApprovedItems = loadCollection();
		if ((updateType == ITEM_DELETED) || (!hasApprovedItems)) {
			if (updateType != ITEM_DELETED) {
				LoaderLog.info("Collection "+collectionId+" has no APPROVED items.  Changing event to DELETED");
			}
			updateType = ITEM_DELETED;
			// This collection has a delete event 
			setupDelete();
			// this collection is being deleted due to no valid items
			shouldPublish = false;
			return true;
		}
		
		shouldPublish = true;
		
		// Files are now on disk virus scan the entire collection
		StringBuffer collectionDirName = new StringBuffer(SoarExtractElement.getContentPath().getAbsolutePath());

		collectionDirName.append(FILE_SEP).append(eventId);
		File colPath = new File(collectionDirName.toString());
		try {
			//virus scan the collection
			VirusScanner scanner = new VirusScanner(colPath, collectionId);
			scanner.scan();
		} catch (VirusScanException vse) {
			LoaderLog.error(collectionDirName.append(" FAILED Virus Scan ").append(vse.getMessage()).toString());
			throw vse;
		}

		if (SoarItem.ftpOnlyUpdate) {
			// All of the files are loaded.  Don't need the metadata so don't load it
		    LoaderLog.info("Collection "+collectionId+" loaded ftpUpdateOnly.");
			return true;
		}
		
		// create the metadata file
	    Element swFeedElement = null;
	    Document document = null;
		
		document = DocumentHelper.createDocument();
		swFeedElement = SoarExtractXMLBuilder.getSwFeedElement(document, SoarExtractElement.schema_file);
		
		LoaderLog.info("Event for collection " + collectionId + " is " + updateType);

		// add full extract = No
		SoarExtractXMLBuilder.addXMLElement(swFeedElement, "full-extract", "No");

	    Element collectionElement = SoarExtractXMLBuilder.addXMLElement(swFeedElement, "collection");
	    addCollectionAttributes(collectionElement);
	    addCollectionElements(collectionElement);
	    
	    addSoftwareItems(collectionElement);
	    
	    // Make sure all of the items loaded cleanly.  If not, this will throw an exception
	    checkItems();

	    // write the document to a byte[]
	    String contentType = getContent_type() + collectionId;
      
	    // Setting content_type to soar/loader/<collectionId> sets up to store the xml file into this location on cds+
	    // put the properties in as the first to be extracted
	    projContent = new ProjContent(contentType.toLowerCase(),collectionId+"-"+dbObj.getString("collection_state")+".xml", getBytes(document));
	    LoaderLog.info("Collection "+collectionId+" loaded.");
	    return true;
	}

	
	/**
	 * loadCollection
	 * @return			true if it has at least 1 item (attachment or software)is available to load
	 * @throws DfException
	 * @throws ProcessingException
	 * @throws IOException 
	 */
	private boolean loadCollection() throws DfException, ProcessingException, IOException {
		dbObj = dbService.getDocumentByQualification("sw_collection where collection_id='" + collectionId + "'");
		if (dbObj == null) {
			dbObj = dbService.getDocumentByQualification("sw_collection(all) where collection_id='" + collectionId + "'");
		}
		if (dbObj == null) {
	        // this collection can't be extracted
			String msg = "Collection "+collectionId+" has no database object.";
			LoaderLog.info(msg);
			throw new ProcessingException(msg, false);
		}
		// now check if this collection is in the right state
		String collectionState = dbObj.getString("collection_state");
		//added by venkat for the 13.4 Release(BR684965)--start
		if(collectionState.equals(OBSOLETE) && updateType != ITEM_DELETED) {
			LoaderLog.info("Collection "+collectionId+" is obsolete. Changing"+updateType+" event to DELETED");
            updateType = ITEM_DELETED;
            }
		//added by venkat for the 13.4 Release(BR684965)--End
		if (!(collectionState.equals(APPROVED) || collectionState.equals(OBSOLETE))) {
			// this is a draft collection.  Try to find the last approved one
			String antecedentId = dbObj.getString("i_antecedent_id");
			dbObj = dbService.getDocumentByQualification("sw_collection (all) where collection_id='" + collectionId + "' and r_object_id='" 
					+ antecedentId + "'");
			if (dbObj == null) {
				String msg = "Collection "+collectionId+" is "+collectionState+" and has no previous version. Unextractable.";
				LoaderLog.error(msg + " antecedent: "+antecedentId);
				throw new ProcessingException(msg, false);
			}
			
			collectionState = dbObj.getString("collection_state");
			if (!(collectionState.equals(APPROVED) || collectionState.equals(OBSOLETE))) {
				String msg = "Collection "+collectionId+" is "+collectionState+" and has no APPROVED/OBSOLETE version. Unextractable.";
				LoaderLog.error(msg + " antecedent: "+antecedentId);
				throw new ProcessingException(msg, false);
			}
		}

		// determine if the item is on ftp.hp.com
		published = EXT_AVAIL.equals(dbObj.getString("externally_available_col"));

		// now load/checkout the attachments for the collection
		attachments = loadAttachments(dbService, updateType, eventId, collectionId, null);
		
		// see what type of subscription this is for
		String subTypeOID = dbObj.getString("submittal_type_oid");
		boolean isPhysicalMediaItems = (subTypeOID != null && subTypeOID.equalsIgnoreCase("2"));
		
		// now load the swItems for this collection
		return loadSoftwareItems(isPhysicalMediaItems) || (attachments.size() > 0);
	}

	private boolean loadSoftwareItems(boolean isPhysicalMediaItems) throws DfException, ProcessingException, IOException {
		// get the swItems that are part of this collection
		boolean hasApprovedItem = false;
		IDfCollection results = null;
		String vDocBuildMethod = "document";
		if (!dbObj.getString("r_assembled_from_id").equals(FIRST_VERSION_ANTECEDENT_ID)) {
			vDocBuildMethod = "assembly";
		}
		StringBuffer dqlQuery = new StringBuffer();
		// ICM changes
		dqlQuery.append("SELECT distinct r_object_id, item_id, icm_driver, icm_rule_id, i_chronicle_id, item_state, is_suspended FROM sw_item (ALL) ");
		dqlQuery.append("WHERE collection_id='").append(collectionId).append("' AND ");
		dqlQuery.append("item_state in ('APPROVED','OBSOLETE') AND ");
		dqlQuery.append(" r_object_id IN (SELECT r_object_id from dm_sysobject (all) in ");
		dqlQuery.append(vDocBuildMethod).append(" id('").append(dbObj.getString("r_object_id"));
		dqlQuery.append("') WHERE r_object_type = 'sw_item') ORDER BY item_id");
		String dqlStr = dqlQuery.toString();
		/*
		 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- COnverting the logger from info to debug to 
		 * avoid DQL statements in the log
		 */
		//LoaderLog.info("getSoftwareItemsToExtract DQL " + dqlStr);
		LoaderLog.debug("getSoftwareItemsToExtract DQL " + dqlStr);
		inSubscription = (filter != null && filter.eval(dbObj));

		SoarExtractItem swItem = null;

		try {
			results = dbService.getResults(dqlStr, "getSoftwareItemsToExtract");
			if (results != null) {
				while (results.next()) {
					String itemId = results.getString("item_id");
					String itemState = results.getString("item_state");
					boolean isSuspended = results.getBoolean("is_suspended");
					String chronicleId = results.getString("i_chronicle_id");
					String objectId = results.getString("r_object_id");
					// ICM changes
					boolean icmDriver = results.getBoolean("icm_driver");
					String icmRule = results.getString("icm_rule_id");

					if (dbService.isItemDuplicated(itemId, collectionId)) {
						LoaderLog.error("Found duplicate APPROVED/DRAFT versions of item for itemId :" + itemId);
					} else {
						// if lastExtractionTimeStamp == null this is a new extraction
						if ((lastExtractionTimeStamp == null) && (itemState.equals(OBSOLETE) || isSuspended)) {
							LoaderLog.info("Collection " + collectionId + " is extracted for the first time. Item "
									+ itemId + " is either OBSOLETE or suspended. No need to extract this item");
							continue;
						} else {
							if (isPhysicalMediaItems) {
								// ICM changes
								swItem = new PhysicalMediaExtractItem(dbService, eventId, objectId, icmDriver, icmRule, chronicleId, collectionId, itemId, 
										itemState, isSuspended, inSubscription, lastExtractionTimeStamp);
							} else {
								// ICM changes
								swItem = new SoarExtractItem(dbService, eventId, objectId, icmDriver, icmRule, chronicleId, collectionId, itemId, 
										itemState, isSuspended, inSubscription, lastExtractionTimeStamp); 
							}
							swItems.add(swItem);
							hasApprovedItem = hasApprovedItem || (!((swItem.getUpdateType() == ITEM_DELETED) || 
																	(swItem.getUpdateType() == ITEM_IMMEDIATE_DELETED))) ||
																	swItem.hasAttachments();
						}
					}
				}
			}
			
		} catch (DfException e) {
			LoaderLog.error("Error in getSoftwareItemsToExtract "+ e.getMessage());
			throw e;
		} finally {
			DocbaseUtils.closeResults(results);
		}
		// notify if this collection has at least 1 approved item in it
		return hasApprovedItem;
	}
	
	
	protected String getIdentifier() {
		return collectionId;
	}

	public String getCollectionId() {
		return collectionId;
	}
	
	protected String getEvent(String status) {
		if (status == WorkItem.SUCCESS) {
			return (updateType == ITEM_DELETED ? EXTCOLDEL : EXTCOLUPD);
		} else {
			return (updateType == ITEM_DELETED ? EXTCOLDELFAIL : EXTCOLUPDFAIL);
		}
	}
	

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.workItem.SoarExtractElement#getObjectType()
	 */
	protected String getObjectType() {
		return "sw_collection";
	}

	/**
	 * logStatus puts entries into the docbase for the results of an extraction
	 * @param session
	 * @param status
	 * @param token
	 * @throws DfException 
	 */
	public void logStatus(IDfSession session, String status, Long token) throws DfException {
		logStatus(session, status, token, null);
	}
	
	/**
	 * logStatus puts entries into the docbase for the results of an extraction
	 * @param session
	 * @param status
	 * @param token
	 * @throws DfException 
	 */
	public void logStatus(IDfSession session, String status, Long token, Exception e) throws DfException {
		if(SoarItem.primary_loader){
			// update the extractor_events table that this event was extracted if success
			StringBuffer dql = new StringBuffer();
			IDfCollection results = null;
			if (status == WorkItem.SUCCESS) {
				dql.append("update soar_extractor_events set extracted=1 where event_id='").append(token).append("'");
				try {
					results = DocbaseUtils.executeQuery(session, dql.toString(), IDfQuery.EXEC_QUERY, "Set extracted status");
					if (results == null || !results.next()) {
						throw new DfException("Update of soar_extractor_events returned no value");
					}
				} finally {
					DocbaseUtils.closeResults(results);
				}
			}

			// update the soar_event_log for this collection
			logSoarEvent(session, dql, status, getEvent(status), chronicleId, collectionId, getObjectType(), e);

			// update the soar_event_log for all of the items
			for(SoarExtractItem item : swItems) {
				item.logSoarEvent(session, dql, status);
			}

		}
	}
	
//	public void orderItems() {
//		Collections.sort(extractItems, new Comparator<SoarExtractItem>() {
//			public int compare(SoarExtractItem o1, SoarExtractItem o2) {
//				return o1.getItemId().compareTo(o2.getItemId());
//			}
//		});
//	}
//	
	
	/**
	 * setPublished sets the low bit of the externally_available_item
	 * @param val
	 * @throws ProcessingException
	 */
	private void setPublished(boolean val) throws ProcessingException {
		try {
			boolean immutable = dbObj.getBoolean("r_immutable_flag");
            dbObj.setBoolean("r_immutable_flag", false);
			dbObj.setString("externally_available_col", (val ? EXT_AVAIL : NOT_EXT_AVAIL));
			dbObj.save();
			if (immutable) {
				dbObj.setBoolean("r_immutable_flag", immutable);
				dbObj.save();
			}
		} catch (DfException e) {
			Log.report("Failed to update collection externally_available_col",
					LoaderLog.getExceptionMsgForLog(collectionId+" Failed to update externally_available_col", e));
		}
	}
	
	/**
	 * setupDelete sets up all of the items for deletion and creates a delete metadata
	 * @throws ProcessingException
	 */
	private void setupDelete() throws ProcessingException {
		LoaderLog.info("Deleting collection "+collectionId);

		// This is a delete request for the collection.  Set up the ProjContent 
		String contentType = SoarExtractElement.getContent_type() + collectionId;
		// Setting content_type to soar/loader/<collectionId> sets up to store the xml file into this location on cds+
		// put the properties in as the first to be extracted
		projContent = new ProjContent(contentType.toLowerCase(), collectionId +"-"+OBSOLETE+".xml", new byte[0]);
		// setup the swItems to be deleted
		for(SoarExtractItem item : swItems) {
			item.setupDelete();
		}
	}

	/**
	 * store publishes the binaries to the destination if it passes the filter, and sends metadata to cds+
	 * @param priority
	 * @throws ProcessingException
	 */
	public void store(Integer priority) throws ProcessingException {
		
		LoaderLog.info("Collection "+collectionId+" begin store.");
		
		// first resolve any swFile that may have multiptle items point to it.
		// This is to remove any delete that shouldn't be done immediate or delayed
		// This scenario happens when the GBL loads a new item and sets the path on the ftp server to exactly the same place as a suspended/obsoleted item
		HashSet<String> publishedLocations = new HashSet<String>();
		
		// get all of the locations that will be published to
		if (inSubscription) {
			// this collection should publish, see what it is going to publish
			for(SoarExtractItem item : swItems) {
				item.getPublishedLocations(publishedLocations);
			}
		}
		
		// first do all immediate_delete updates
		for(SoarExtractItem item : swItems) {
			try {
				item.doImmediateDelete(publishedLocations);
			} catch (IOException e) {
				throw new ProcessingException(e, true);
			}
		}
		
		for(SoarExtractItem item : swItems) {
			item.doUpdates(publishedLocations);
		}

		// Store the collection attachments to CDS+
		for(SoarExtractAttachment attachment : attachments) {
			try {
				attachment.doStore(priority);
			} catch (ProcessingException e) {
				LoaderLog.error("Collection "+collectionId+" attachment update failed "+e.getMessage()+ " retry count = "+ (++retryCount));
				throw e;
			}
		}

			
		// now store the item documents
		for (SoarExtractItem item : swItems) {
			try {
				item.doStore(priority);
			} catch (ProcessingException e) {
				LoaderLog.error("Collection "+collectionId+" update failed "+e.getMessage()+ " retry count = "+ (++retryCount));
				throw e;
			}
		}
		
		// store the collection properties to CDS+ (This is done last so all the other files are available before CDS+ sees the metadata update)
		if (!SoarItem.ftpOnlyUpdate) {
			// store the metadata to CDS+ if it is not an ftpOnlyUpdate
			storePC(projContent, priority);
		} else {
			LoaderLog.debug("Collection "+collectionId+" Skip CDS+ update. ftpOnlyUpdate");
		}
		
		//  finally add the delayed deletes
		for (SoarExtractItem item : swItems) {
			try {
				item.doDelete(publishedLocations);
			} catch (ProcessingException e) {
				LoaderLog.error("Collection "+collectionId+" delete failed "+e.getMessage()+ " retry count = "+ (++retryCount));
				throw e;
			}

		}
		
		if (inSubscription && !published && shouldPublish) {
			setPublished(true);
		} else if (!inSubscription || (published && !shouldPublish)) {
			setPublished(false);
		}
		
		LoaderLog.info("Collection "+collectionId+" stored.");
	}
	
	/**
	 * init sets up the filter for a collection
	 * @param config
	 */
	public static void init(ConfigurationReader config) {
		org.w3c.dom.Element destElem = config.getElement(SFTPDestination.REMOTEDESTINATION);

        NodeList nodes = destElem.getElementsByTagName(FilterNode.FILTER);
        if (nodes == null) {
        	return;
        }
        if (nodes.getLength() > 1) {
        	throw new IllegalArgumentException("Filter Parse Exception - Multiple filter elements not supported");
        }

        // Get the collection element
        nodes = ((org.w3c.dom.Element)nodes.item(0)).getElementsByTagName(FilterNode.COLLECTION);
        
        //if more than one element is found, none of the nodes are loaded
        if (nodes !=  null && nodes.getLength() == 1){
        	ArrayList<org.w3c.dom.Element> elements = FilterNode.getElements(nodes.item(0));
            if (elements.size() == 1){
            	filter = FilterNode.getNode( elements.get(0) );
            } else {
            	// multiple filters not encased in an And so wrap them
            	filter = new AndFilterNode(elements);
            }
        	if (LoaderLog.isInfoEnabled()) {
        		LoaderLog.info("Collection Filter: "+filter.toString());
        	}
        } else {
        	throw new IllegalArgumentException("Filter Parse Exception - Multiple collection elements found in filter");
        }
        
	}
}
