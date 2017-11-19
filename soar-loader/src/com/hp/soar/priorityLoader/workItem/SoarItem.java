/**
 * 
 */
package com.hp.soar.priorityLoader.workItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.dom4j.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
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
import com.hp.soar.priorityLoader.services.SoarDBService;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.utils.ExtractorUtils;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.utils.ProductEnvironmentRelation;
import com.hp.soar.priorityLoader.utils.ProductNameBean;
import com.hp.soar.priorityLoader.utils.ProductNames;
import com.hp.soar.priorityLoader.utils.VirusScanException;

/**
 * SoarExtractItem hold the extraction information for 1 Collection Item
 * 
 * @author dahlm
 *
 */
public class SoarItem extends SoarExtractElement {

	public static final String EXTITMDEL = "EXTITMDEL";
	public static final String EXTITMDELFAIL = "EXTITMDELFAIL";
	public static final String EXTITMUPD = "EXTITMUPD";
	public static final String EXTITMUPDFAIL = "EXTITMUPDFAIL";
	public static final String FTPONLYUPDATE = "ftpOnlyUpdate";
	public static final String PUSHFTPTOO = "pushFTPtoo";
	public static final String PRIMARY_lOADER = "primary_loader";


	
	// Filter to determine if the software associate with this item should publish to the destination
	private static FilterNode filter = null;
	
	protected static boolean ftpOnlyUpdate = false;
	
	protected String itemId;		// id(name) of the sw_item 	
	
	protected String collectionId;	// id(name) of the sw_collection this sw_item belongs to
	
	// cds+ metadata to be stored
	protected ArrayList<ProjContent> projContent;

	// attachments for this element
	protected ArrayList<SoarExtractAttachment> attachments;
	
	// software files to be loaded to ftp.hp.com
	private HashMap<String, ArrayList<SoarExtractSWFile>> swFileSets = new HashMap<String, ArrayList<SoarExtractSWFile>>();

	// flag indicating swFile updates are already done
	private boolean updateCompleted = false;
	
	// flag to indicate if this item satisfies the subscription for publishing to ftp.com (true if it passes the filter)
	private boolean inSubscription = true;
	
	// availability bits
	private boolean published;
	
	// flag indicating attachment updates are already done	
	private boolean storeCompleted = false;
	
	// publish state of the item 
	private boolean shouldPublish = true;
	
	// number of times a store is attempted
	private int	retryCount = 0;
	
	
	// the destination processor for this item
	protected DestinationAdapter destination = null; 
	
	//flag to check ftp push is enabled
	protected static boolean pushFTPtoo = true;
	
	protected static boolean primary_loader = true;

	protected ProductNames prodNamesMap;
	
	/**
	 * Create a new item entry for extraction	
	 * @param dbService  // Database Service.  This holds the session 
	 * @param eventId 
	 * @param objectId 
	 * @param chronicleId 
	 * @param itemId
	 * @param itemState
	 * @param isSuspended
	 * @param collectionAccepted TODO
	 * @param prodNamesMap TODO
	 * @throws DfException 
	 * @throws VirusScanException 
	 * @throws ProcessingException 
	 * @throws IOException 
	 */
	public SoarItem(SoarExtractionDBService dbService, String eventId, String objectId, boolean isICM, String icmRule,
			String chronicleId, String collectionId, String itemId, String itemState, boolean isSuspended, 
			boolean collectionAccepted, Date lastExtractionTime, ProductNames prodNamesMap) throws DfException, ProcessingException, IOException {
		super(dbService, objectId, (itemState.equals(OBSOLETE) || isSuspended) ? ITEM_DELETED : ITEM_MODIFIED);
		
		// set the destination for this item
		destination = DestinationAdapter.getInstance(isICM, icmRule);

		this.itemId = itemId;
		this.collectionId = collectionId;
		this.prodNamesMap = prodNamesMap;
		
		dbObj = dbService.getDocumentByQualification("sw_item where item_id='" 
				+ itemId + "' and collection_id='" + collectionId + "' order by item_state");
		// If the item is OBSOLETE it will not be listed from above query
		if (dbObj == null) {
			dbObj = dbService.getDocumentByQualification("sw_item (all) where item_id='" + itemId + "' and collection_id='" + collectionId + "' ");
		}
		if (dbObj != null) {
			// If the item is in DRAFT or REVIEW state, get the previous version
			// of the item which will be APPROVED
			/*if (!(itemState.equals(APPROVED) || itemState.equals(OBSOLETE))) {
				dbObj = dbService.getDocumentByQualification("sw_item (all) where item_id='"
						+ itemId + "' and r_object_id='" + dbObj.getString("i_antecedent_id")
						+ "' and collection_id='" + collectionId + "' ");
			}*/
			//item state is passed as approved but actual item state is draft (publishing draft issue)
			//Commented the above if and added the following condition as changes done in 14.1.1 release(draft item publishing issue) of legacy soar loader
			if(!(dbObj.getString("item_state").equals(APPROVED) || dbObj.getString("item_state").equals(OBSOLETE))){
				dbObj = dbService.getDocumentByQualification("sw_item (all) where item_id='"
						+ itemId + "' and r_object_id='" + dbObj.getString("i_antecedent_id")
						+ "' and collection_id='" + collectionId + "' ");
			}
			
			Date updateDate = dbObj.getTime("update_date").getDate();
			
			// determine publishing state
			inSubscription = collectionAccepted && ((filter == null) || filter.eval(dbObj));
			String lastEvent = dbService.previouslyExtractedItem(dbObj.getString("r_object_id"), chronicleId);
			
			// determine if the item is on ftp.hp.com
			published = EXT_AVAIL.equals(dbObj.getString("externally_available_item")); 
			
			// Items are deleted first, It is only upToDate if it is extracted and hasn't changed.
			if (itemState.equals(OBSOLETE)) {
				updateType = ITEM_DELETED;
			} else if (isSuspended) {
				updateType = ITEM_IMMEDIATE_DELETED;
			} else if ((lastExtractionTime != null) && updateDate.before(lastExtractionTime) && SoarItem.primary_loader) {
				updateType = ITEM_UPTODATE;
			} else {
				// modified if there was a previous event of any type
				updateType = ((lastEvent != null) ? ITEM_MODIFIED : ITEM_NEW);
			}

			//nidoh soar loader mod
			// set shouldPublish based on if the item is obsolete or suspended, or whether or not the file's MD5 checksum has changed
			shouldPublish = (!(itemState.equals(OBSOLETE) || isSuspended) && !matchingMD5s(dbObj.getString("r_object_id")));
			
			if (ftpOnlyUpdate) {
				LoaderLog.info("Collection "+collectionId+" Item "+ itemId + " updateType: "+updateType);
			}
			// load attachments for this item
			attachments = loadAttachments(dbService, updateType, eventId, collectionId, itemId);

			// load the swFiles for this item
			
				loadSWFiles(lastExtractionTime, eventId);
						
		} else {
			LoaderLog.error("Failed to get ITEM for item Id " + objectId);
			setExtractMsg("Failed to get item "+objectId+" from docbase");
			setExtractResult(SoarExtractElement.ITEMEXTFAILED);
		}
	}

	
	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}
	
	public void addElement(Element itemsMainElement) throws DfException, ProcessingException {

		LoaderLog.info("Event for Item " + itemId + " <"+objectId+"> is " + updateType);

		Element itemElement = SoarExtractXMLBuilder.addXMLElement(itemsMainElement, "software-item");

		// Add software-item's attributes
		addItemAttributes(itemElement);

		// end of changes artifact 31 -soar 9.2
		SoarExtractXMLBuilder.addXMLElement(itemElement, "version", dbObj.getString("software_version"));

		addOrderablePartIdElement(itemElement);
		addBOMElements(itemElement);
		//***************************************************************************************************************

		/*@Vishnu Gowda Harish
		 * January 30th,2011
		 * SOAR11.1,RCD
		 * Below is the extraction for project name property, which exists in all submittal types.
		 *  will look something like this.
		 *  <project-name oid="2"> test</project-name>
		 * This element in the schema should be defined before install-formats. maintain the sequence
		 * 
		 * 
		 */
		
		/*String projectOid = dbObj.getString("project_oid");
					if(projectOid!=null){
						Element projectElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "project-name",
								ReferenceLists.getLabel(ReferenceLists.PROJECT_NAMES_LIST, projectOid));
						SoarExtractXMLBuilder.addXMLAttribute(projectElement,"oid", projectOid, false);
					}
		 */

		// put in InstallFormatOids
		String[] instFmtOids = getRepeatingAttr(dbObj, "install_format_oids");
		Element instFmtElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "install-formats");
		if (instFmtOids.length == 0) {
			SoarExtractXMLBuilder.addXMLAttribute(instFmtElement, "nil", "true", true);
		} else {
			for(String instFmtOid : instFmtOids) {
				Element instFmtEle = SoarExtractXMLBuilder.addXMLElement(instFmtElement, "install-format",
						ReferenceLists.getLabel(ReferenceLists.INSTALL_FORMAT_LIST, instFmtOid));
				SoarExtractXMLBuilder.addXMLAttribute(instFmtEle, "oid", instFmtOid, false);
			}
		}

		// put in replacesItemId
		String replaceItemId = dbObj.getString("replaces_item_id");
		SoarExtractXMLBuilder.addNullbaleElement(itemElement,"replaces-item-ID", replaceItemId);

		addSupersedeInfo(itemElement);

		String disclosureLevelOid = dbObj.getString("disclosure_level_oid");
		Element disLvlElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "disclosure-level",
				ReferenceLists.getLabel(ReferenceLists.DISCLOSURE_LEVELS_LIST, disclosureLevelOid));
		SoarExtractXMLBuilder.addXMLAttribute(disLvlElement, "oid", disclosureLevelOid, false);

		String[] mediaTypeOids = getRepeatingAttr(dbObj, "media_type_oids");
		Element mediaTypeElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "media-types");
		for(String mediaTypeOid : mediaTypeOids) {
			Element mtEle = SoarExtractXMLBuilder.addXMLElement(mediaTypeElement, "media-type", 
					ReferenceLists.getLabel(ReferenceLists.MEDIA_TYPES_LIST, mediaTypeOid));
			SoarExtractXMLBuilder.addXMLAttribute(mtEle, "oid", mediaTypeOid, false);
		}

		String[] updateTypeOids = getRepeatingAttr(dbObj, "update_type_oids");
		Element updTypeMainElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "update-types");
		for(String updateTypeOid : updateTypeOids) {
			Element mtEle = SoarExtractXMLBuilder.addXMLElement(updTypeMainElement, "update-type",
					ReferenceLists.getLabel(ReferenceLists.UPDATE_TYPES_LIST, updateTypeOid));
			SoarExtractXMLBuilder.addXMLAttribute(mtEle, "oid", updateTypeOid, false);
		}

		String seviorityOid = dbObj.getString("severity_oid");
		Element sevElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "severity",
				ReferenceLists.getLabel(ReferenceLists.SEVERITIES_LIST, seviorityOid));
		SoarExtractXMLBuilder.addXMLAttribute(sevElement, "oid", seviorityOid, false);
		
		String updateDate = getDateValue(dbObj, "update_date", true);
		SoarExtractXMLBuilder.addXMLElement(itemElement, "update-date", updateDate);

		String custAvailDate = getDateValue(dbObj, "customer_available_date", false);
		SoarExtractXMLBuilder.addXMLElement(itemElement, "customer-available-date", custAvailDate);

		String obsoleteDate = getDateValue(dbObj, "obsolete_date", false);
		SoarExtractXMLBuilder.addNullbaleElement(itemElement, "obsolete-date", obsoleteDate);
		
		addProductsSupportedElement(itemElement);
		
		addProductEnvironmentsSupportedElement(itemElement);
		
		//Taxonomy changes- Adding tag <Taxonomy-Categorizations>		
		String[] tmscattopicsOids = getRepeatingAttr(dbObj, "tms_categorization_topics");
		Element tmscattopicsElements = SoarExtractXMLBuilder.addXMLElement(itemElement, "taxonomy-categorizations");
        for(String tmscattopicsOidsVal : tmscattopicsOids) {                 
               if(tmscattopicsOidsVal != null && !tmscattopicsOidsVal.equals("")){
                     StringTokenizer tmscattopics = new StringTokenizer(tmscattopicsOidsVal,"=,");
                     while (tmscattopics.hasMoreElements()) {
                            Element tmscattopicsEle = SoarExtractXMLBuilder.addXMLElement(tmscattopicsElements, "taxonomy-categorization");
                            SoarExtractXMLBuilder.addXMLElement(tmscattopicsEle, "categorization-key",  tmscattopics.nextElement());                  
                             SoarExtractXMLBuilder.addXMLElement(tmscattopicsEle, "categorization-value", tmscattopics.nextElement());
                     }
               }
        }

		//End of Taxonomy changes- Adding tag <Taxonomy-Categorizations>
		
		// Add is-suspended
		Element itemSuspElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "item-suspended");
		String isSuspendedStr = dbObj.getValue("is_suspended").asString().equals("F") ? "No" : "Yes";
		SoarExtractXMLBuilder.addXMLElement(itemSuspElement, "is-suspended", isSuspendedStr);

		String suspendedReason = dbObj.getValue("suspended_reason").asString();
		SoarExtractXMLBuilder.addNullbaleElement(itemSuspElement, "suspended-reason", suspendedReason);

		String[] flagOids = getRepeatingAttr(dbObj, "flag_oids");
		addFlagOidsElementForComponent(itemElement, flagOids, "ITEM");

		// Add languages
		addLanguageOidsElement(itemElement);

		// Add distribution-region
		String[] dstRegOids = getRepeatingAttr(dbObj, "distribution_region_oids");
		if (dstRegOids.length > 0) {
			Element dstRegOidsElement = SoarExtractXMLBuilder.addXMLElement(itemElement,"distribution-regions");
			for(String dstRegOid : dstRegOids) {
				Element dstRegElement = SoarExtractXMLBuilder.addXMLElement(dstRegOidsElement, "distribution-region",
						ReferenceLists.getLabel(ReferenceLists.REGIONS_LIST, dstRegOid));
				SoarExtractXMLBuilder.addXMLAttribute(dstRegElement, "oid", dstRegOid, false);
			}
		}

		// Add pricing
		Element pricingElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "pricing");
		
		String priceTypeOid = dbObj.getValue("price_type_oid").asString();
		Element priceTypeElement = SoarExtractXMLBuilder.addXMLElement(pricingElement, "price-type",
				ReferenceLists.getLabel(ReferenceLists.PRICE_TYPES_LIST, priceTypeOid));
		SoarExtractXMLBuilder.addXMLAttribute(priceTypeElement, "oid", priceTypeOid, false);

		String priceComment = dbObj.getString("price_comment");
		SoarExtractXMLBuilder.addNullbaleElement(pricingElement, "price-comment", priceComment);
		
		Element priceElement = SoarExtractXMLBuilder.addXMLElement(pricingElement, "price");
		SoarExtractXMLBuilder.addXMLElement(priceElement, "price-value", getDoubleValue("price"));
		
		String priceCurOid = dbObj.getValue("price_currency_oid").asString();
		Element priceCurrencyElement = SoarExtractXMLBuilder.addXMLElement(priceElement, "price-currency",
				ReferenceLists.getLabel(ReferenceLists.CURRENCIES_LIST, priceCurOid));
		SoarExtractXMLBuilder.addXMLAttribute(priceCurrencyElement, "oid", priceCurOid, false);
		
		// Add software-password
		String sftPwd = dbObj.getString("software_password");
		SoarExtractXMLBuilder.addNullbaleElement(itemElement, "software-password", sftPwd);

		// Add installation-filename
		String sftInstFileName = dbObj.getString("software_install_filename");
		SoarExtractXMLBuilder.addNullbaleElement(itemElement, "installation-filename", sftInstFileName);

		String fullMethOid = dbObj.getValue("fulfillment_method_oid").asString();
		if (!fullMethOid.equals("")) {
			// Add physical-fulfillment
			Element phyFullElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "physical-fulfillment");

			// Add fulfillment-method
			Element fullMethElement = SoarExtractXMLBuilder.addXMLElement(phyFullElement, "fulfillment-method",
					ReferenceLists.getLabel(ReferenceLists.FULFILLMENT_METHODS_LIST, fullMethOid));
			SoarExtractXMLBuilder.addXMLAttribute(fullMethElement, "oid", fullMethOid, false);

			// Add fulfillment-dock-date
			String dockDate = getDateValue(dbObj, "dock_date", false);
			SoarExtractXMLBuilder.addNullbaleElement(phyFullElement, "fulfillment-dock-date", dockDate);
		}
		
		// Add bundled-items
		String[] bundItems = getRepeatingAttr(dbObj, "bundled_items");
		int bundleIndex = 0;
		Element bundItemMainElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "bundled-items", "");
		for(String bundItem : bundItems) {
			if (bundItem != null) {
				Element bundItemElement = SoarExtractXMLBuilder.addXMLElement(bundItemMainElement, "bundled-item", bundItem);
				SoarExtractXMLBuilder.addXMLAttribute(bundItemElement, "item-ID", bundItem, false);
				bundleIndex++;
				SoarExtractXMLBuilder.addXMLAttribute(bundItemElement, "bundled-item-sequence",
						String.valueOf(bundleIndex), false);
			}
		}
		if (bundleIndex > 0) {
			SoarExtractXMLBuilder.addXMLAttribute(bundItemMainElement, "total-number-of-items",
					String.valueOf(bundleIndex), false);
		}
		
		// Add cd-checkvalues
		String[] cdChkVals = getRepeatingAttr(dbObj, "cd_checkvalues");
		
		if (cdChkVals.length > 0 && SoarExtractElement.ignoreRCD) {                    
			
			Element cdChkValsMainElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "cd-checkvalues","");
			for(String cdChkVal : cdChkVals) {
				if (cdChkVal != null) {
					SoarExtractXMLBuilder.addXMLElement(cdChkValsMainElement, "cd-checkvalue", cdChkVal);
				}
			}
		}
		
		/*@Vishnu Gowda Harish
		 * January 30th,2011
		 * SOAR11.1 RCD
		 * for order-links property
		 * will look something lke this..
		 * <order-links>                -- should be declared as minoccurs=0 in the xsd. as this wont exist for items of other submittal type
		 * <order-link oid="1"> www.google.com/sds/blah</order-link>
		 * <order-links>.
		 * this property should be defined in all the xsd's required after cd-checkvalues property, maintain the same sequence in xsd, as what you have
		 * written in the code
		 * 
		 * 
		 */
		
		addOrderLinksElement(itemElement);
		
		// Add item-url
		String url = dbObj.getValue("url").asString();
		SoarExtractXMLBuilder.addNullbaleElement(itemElement, "item-url", url);

		// Add partner-feedback
		addPartnerFeedBackElement(itemElement);

		// Add iadd-properties
		addIAddProperties(itemElement);

		boolean bLanguageAtFileLevel = false;
		if (dbObj.getString("language_at_file_level").equals("2")) {
			bLanguageAtFileLevel = true;
		}
			 
		addSoftwareFilesElement(itemElement, bLanguageAtFileLevel);

		addAttachmentsElements( itemElement, attachments, collectionId, itemId);

		extractResult = ITEMEXTRACTED;
		
	}

	protected void addBOMElements(Element itemElement) throws DfException {
		// default is no BOM elements
	}
	
	private void addIAddProperties(Element itemElement) throws  DfException {
		Element iAddPropsElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "iadd-properties");
		String driverName = dbObj.getString("driver_name");
		if (driverName == null || driverName.trim().equals("")) {
			SoarExtractXMLBuilder.addXMLAttribute(iAddPropsElement, "nil", "true", true);
		} else {
			SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "driver-name", driverName);

			String driverModelsOid = dbObj.getString("driver_model_oids");
			String[] driverModelsOidsArr = driverModelsOid.split(",");

			Element driverModelsElement = SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "driver-models");
			for (int j = 0; j < driverModelsOidsArr.length; j++) {
				String driModOid = driverModelsOidsArr[j];
				if (!driModOid.equals("")) {
					Element driverModElement = SoarExtractXMLBuilder.addXMLElement(driverModelsElement, "driver-model",
							ReferenceLists.getLabel(ReferenceLists.DRIVER_MODEL_LIST, driModOid));
					SoarExtractXMLBuilder.addXMLAttribute(driverModElement, "oid", driModOid, false);
				}
			}

			String labRelDate = getDateValue(dbObj, "lab_release_date", false);
			SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "driver-release-date", labRelDate);

			// Add driver-installation-method
			// ------------------------------
			String instMethOid = dbObj.getString("install_method_oid");
			Element driInstMethElement = SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "driver-installation-method",
					ReferenceLists.getLabel(ReferenceLists.DRIVER_INSTALLATION_METHOD_LIST, instMethOid));
			SoarExtractXMLBuilder.addXMLAttribute(driInstMethElement, "oid", instMethOid, false);

			// Add primary-inf-file
			SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "primary-inf-file", 
					dbObj.getString("primary_inf_file"));

			// Add driver-network-compatible
			String driNetCompatible = dbObj.getBoolean("driver_network_compatible") ? "Yes" : "No";
			SoarExtractXMLBuilder.addXMLElement(iAddPropsElement, "driver-network-compatible", driNetCompatible);
		}

	}
	
	protected void addOrderablePartId(Element itemElement) throws DfException {
        SoarExtractXMLBuilder.addXMLAttribute(itemElement, "item-ID", dbObj.getString("item_id"), false);
	}
	
	protected void addOrderablePartIdElement(Element itemElement) throws DfException {
		// default is no OrderablePartId
	}

	protected void addOrderLinksElement(Element itemElement) throws DfException {
		// default is no OrderLinks
	}
	
	private void addPartnerFeedBackElement(Element itemElement) throws DfException {
		Element partnerFeedbackElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "partner-feedback");
		IDfCollection results = null;
		try {
			String dqlStatement = "SELECT item_id, partner_id,received_timestamp, url FROM dm_dbo.soar_url_feedback f1 WHERE item_id = '"
		        + objectId 
		        + "' AND received_timestamp in " 
		        + "(SELECT MAX(received_timestamp) FROM dm_dbo.soar_url_feedback f2 WHERE f1.item_id=f2.item_id AND f1.partner_id=f2.partner_id) "
		        + "ORDER BY item_id";
			/*
			 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- COnverting the logger from info to debug to 
			 * avoid DQL statements in the log
			 */
		      //LoaderLog.info("soar_url_feedback DQL :" + dqlStatement);
		      LoaderLog.debug("soar_url_feedback DQL :" + dqlStatement);
		      results = dbService.getResults(dqlStatement, "Extractor : ExtractObjectAttrItem : query soar url feedback");

		      Element webAccessElement = SoarExtractXMLBuilder.addXMLElement(partnerFeedbackElement, "web-access");

		      while (results.next()) {
		        Element webAccessUrlElement = SoarExtractXMLBuilder.addXMLElement(webAccessElement, "web-access-url", results.getString("url"));
		        SoarExtractXMLBuilder.addXMLAttribute(webAccessUrlElement, "partner-id", results.getString("partner_id"), false);
		      }
		} finally {
		      DocbaseUtils.closeResults(results);
		}
		
		// clear out the old results
		results = null;
		
		try {
			String dqlStatement = "SELECT item_id, partner_id,received_timestamp, hp_agent_note, hp_customer_note, item_availability"
					+ " FROM dm_dbo.soar_orderability_feedback f1 WHERE item_id = '"
					+ itemId
					+ "' AND received_timestamp in "
					+ "(SELECT MAX(received_timestamp) FROM dm_dbo.soar_orderability_feedback f2 WHERE f1.item_id=f2.item_id AND f1.partner_id=f2.partner_id)"
					+ " ORDER BY item_id";
			/*
			 *SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- Converting the logger from info to debug to 
			 * avoid DQL statements in the log
			 */
			//LoaderLog.info("soar_orderability_feedback DQL :" + dqlStatement);
			LoaderLog.debug("soar_orderability_feedback DQL :" + dqlStatement);
			
			results = dbService.getResults(dqlStatement, "Extractor : ExtractObjectAttrItem : query soar_orderability_feedback");

			Element phyAvailElement = SoarExtractXMLBuilder.addXMLElement(partnerFeedbackElement, "physical-availability");

			while (results.next()) {
				Element partnerAvailElement = SoarExtractXMLBuilder.addXMLElement(phyAvailElement, "partner-availability");
				SoarExtractXMLBuilder.addXMLAttribute(partnerAvailElement, "partner-id", results.getString("partner_id"), false);
		        SoarExtractXMLBuilder.addXMLAttribute(partnerAvailElement, "item-availability", results.getString("item_availability"), false);

		        String hpAgentNotice = results.getString("hp_agent_note");
		        SoarExtractXMLBuilder.addNullbaleElement(partnerAvailElement, "hp-agent-note", hpAgentNotice);

		        String hpCustNote = results.getString("hp_customer_note");
		        SoarExtractXMLBuilder.addNullbaleElement(partnerAvailElement, "hp-customer-note", hpCustNote);
			}
		} catch (DfException e) {
			LoaderLog.error("Error adding Partner Feedback " + ExtractorUtils.getStackTrace(e));
			throw e;
		} finally {
			DocbaseUtils.closeResults(results);
		}
	}
	
	protected void addItemAttributes(Element itemElement) throws DfException {
		addOrderablePartId(itemElement);
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "state", dbObj.getString("item_state"), false);

		// IMMEDIATE_DELETED updateType is put in metadata as ITEM_DELETED
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "partner-update-type", (updateType == ITEM_IMMEDIATE_DELETED ? ITEM_DELETED : updateType), false);

		String convertValue = dbObj.getString("from_toad").equals("F") ? "No" : "Yes";
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "from-toad", convertValue, false);
		String toadOid = dbObj.getString("toad_oid");
		if (!toadOid.equals("")) {
			SoarExtractXMLBuilder.addXMLAttribute(itemElement, "toad-OID", toadOid, false);
		}
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "chronicle-ID",dbObj.getString("i_chronicle_id"), false);
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "soar-description-overridden", "No", false);
		String lanAtFileLevel = dbObj.getString("language_at_file_level").equals("2") ? "Yes" : "No";
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "languages-at-file-level",lanAtFileLevel, false);

		String prodLvlSensitive = dbObj.getString("product_level_sensitive");
		String isProdLvlSensitive = prodLvlSensitive.equals("F") ? "No" : "Yes";
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "product-level-sensitive",isProdLvlSensitive, false);
	}

	private void addLanguageOidsElement(Element parentElement) throws DfException {

		Element langMainElement = SoarExtractXMLBuilder.addXMLElement(parentElement, "languages");

		String[] langChOids = getRepeatingAttr(dbObj, "language_char_set_oids");
		for(String langChOid : langChOids) {
			Element langEle = SoarExtractXMLBuilder.addXMLElement(langMainElement, "language");

			SoarExtractXMLBuilder.addXMLAttribute(langEle, "oid", langChOid, false);
			SoarExtractXMLBuilder.addXMLAttribute(langEle, "language-iso-code",
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_ISO_CODES_LIST, langChOid), false);
			
			SoarExtractXMLBuilder.addXMLElement(langEle, "language-in-English",
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_LIST, langChOid));
			
			SoarExtractXMLBuilder.addNullbaleElement(langEle, "character-set-in-English", 
					ReferenceLists.getLabel(ReferenceLists.LANGUAGE_CHAR_SETS_LIST, langChOid));
			
			SoarExtractXMLBuilder.addXMLElement(langEle, "local-language",
					ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_LIST, langChOid));

			SoarExtractXMLBuilder.addNullbaleElement(langEle, "local-character-set", 
					ReferenceLists.getLabel(ReferenceLists.LOCAL_LANGUAGE_CHAR_SETS_LIST, langChOid));
		}
	}


	private void addProductEnvironmentsSupportedElement(Element itemElement) throws DfException  {
		IDfCollection results = null;
		ArrayList<ProductEnvironmentRelation> perList = new ArrayList<ProductEnvironmentRelation>();
		ProductEnvironmentRelation currentPER = null;
		HashSet<String> validEnvironments = getValidEnvironmentsForItem();
		Element prodEnvisSupportedElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "products-environments-supported");
		Element prdEnvisRelElemnt = null;
		
		try {
			String dqlStatement = "SELECT * FROM dm_dbo.new_view2 WHERE r_object_id = '"
		        + objectId + "' AND product_group_oid = " + dbObj.getInt("product_group_oid") + 
		        " AND (update_flag is null OR update_flag <> 'D') ORDER BY group_id, environment_detail_oid";

			/*
			 *SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- Converting the logger from info to debug to 
			 * avoid DQL statements in the log
			 */
			//LoaderLog.info("extractProductEnvironmentsSupported DQL: "+ dqlStatement);
			LoaderLog.debug("extractProductEnvironmentsSupported DQL: "+ dqlStatement);
			results = dbService.getResults(dqlStatement, "extractProductEnvironmentsSupported");

			while (results.next()) {
				// Add product information
				// -----------------------
				String prodGrpOid = results.getString("group_id");
				if (prodGrpOid == null) {
					// skip this entry it is empty
					continue;
				}
				if (currentPER != null && (currentPER.getGroupId().equals(prodGrpOid))) {
					currentPER.addEnvironment(results, validEnvironments);
				} else {
					currentPER = new ProductEnvironmentRelation(results, validEnvironments);
					perList.add(currentPER);
				}
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}

		// arrayList to collect the environment names that are used
		ArrayList<String> envLabels = new ArrayList<String>();
		
		// now put each Product Environment Relation in 
		for(ProductEnvironmentRelation per : perList) {
			per.addElement(prodEnvisSupportedElement, getEnvMinServiceReleaseMap(), envLabels);
		}
		
		StringBuffer labels = new StringBuffer();
		for (int i=0; i<envLabels.size();) {
			labels.append(envLabels.get(i++));
			if (i < envLabels.size()) {
				labels.append(",");
			}
		}
		
		// Add the distinct environments attribute
		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "soar-distinct-environments", labels.toString(), false);
		
	}
	
	private void addProductsSupportedElement(Element itemElement) throws  DfException {

		// Add product section
		// -------------------
		Element prodsSupportedElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "products-supported");

		ArrayList<ProductNameBean> nameBeans = prodNamesMap.getNameBeans(dbService.getSession(), dbObj.getString("product_group_oid"));
		LoaderLog.info("number of products : " + nameBeans.size());
		for(ProductNameBean nameBean : nameBeans) {
			boolean validBean = (nameBean.getProductUpdate().indexOf("D") == -1);
			if ( validBean ) {
				if (nameBean.getProductNameOID().indexOf("_") != -1) {
					Element prodElement = SoarExtractXMLBuilder.addXMLElement(prodsSupportedElement,
							"product", nameBean.getProductName());
					SoarExtractXMLBuilder.addXMLAttribute(prodElement,
							"type", nameBean.getProductHierarchyType(), false);
					SoarExtractXMLBuilder.addXMLAttribute(prodElement,
							"oid", nameBean.getProductMasterOID(),
							false);
					SoarExtractXMLBuilder.addXMLAttribute(prodElement,
							"deleted-product", (validBean ? "No" : "Yes"), false);
				} else {
					LoaderLog.info("product can not be added. product name oid = "
							+ nameBean.getProductNameOID());
				}
			}
		}
	}

	private void addSoftwareFilesElement(Element itemElement, boolean languageAtFileLevel) throws DfException {
		Element softFilesElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "software-files");
		// for each fileSet
		LoaderLog.info("SoarItem addSoftwareFilesElement swFileSets size: " + swFileSets.size());
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {
					LoaderLog.info("SoarItem addSoftwareFilesElement swFileSets col: " +collectionId+":"+itemId+":"+swFile.getIdentifier());
			}
		}
		for(String fileTypeOid : swFileSets.keySet()) {
			Element softwareSetElement = SoarExtractXMLBuilder.addXMLElement(softFilesElement, "software-set");
			Element fileTypeElement = SoarExtractXMLBuilder.addXMLElement( softwareSetElement, "file-type",
					ReferenceLists.getLabel(ReferenceLists.FILE_TYPES_LIST, fileTypeOid));
			SoarExtractXMLBuilder.addXMLAttribute(fileTypeElement, "oid", fileTypeOid, false);
			ArrayList<SoarExtractSWFile> fileList = swFileSets.get(fileTypeOid);
			
            SoarExtractXMLBuilder.addXMLAttribute(softwareSetElement, "total-number-of-files", String.valueOf(fileList.size()), false);
        
            for(SoarExtractSWFile swFile : fileList) {
            	swFile.addSWFileElement(softwareSetElement, languageAtFileLevel);
            }
		}
	}

	private void addSupersedeInfo(Element itemElement) throws DfException {
		// CDSPlus
		String isSuperSeded = dbObj.getString("is_superseded").equals("F") ? "No" : "Yes";
		Element supersedeInfoEle = SoarExtractXMLBuilder.addXMLElement(itemElement, "supersede-info");
		SoarExtractXMLBuilder.addXMLAttribute(supersedeInfoEle,"software-item-group", 
				dbObj.getString("software_item_group"), false);
		SoarExtractXMLBuilder.addXMLAttribute(supersedeInfoEle, "superseded", isSuperSeded, false);
		
		String supersedesItemId = dbObj.getString("supersedes_item_id");
		if (!supersedesItemId.equals("")) {
			SoarExtractXMLBuilder.addXMLElement(supersedeInfoEle, "supersedes-item-ID", supersedesItemId);
		}
		
		String supersededByItemId = dbObj.getString("superseded_by_item_id");
		if (!supersededByItemId.equals("")) {
			Element supByIdElement = SoarExtractXMLBuilder.addXMLElement(
					supersedeInfoEle, "superseded-by-item-ID", supersededByItemId);
			String supersedeDate = getDateValue(dbObj, "superseded_date", false);
			SoarExtractXMLBuilder.addXMLAttribute(supByIdElement, "superseded-on-date", supersedeDate, false);
		}
	}

	/**
	 * doDelete queues the softwareFiles for future deletion
	 * @param publishedLocations 
	 * @throws ProcessingException 
	 */
	public void doDelete(HashSet<String> publishedLocations) throws ProcessingException {
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {
				swFile.doDelete(publishedLocations);
			}
		}
		// set the availability to false
		if ((updateType == ITEM_DELETED) && published) {
			setPublished(false);
		}
	}
	
	/**
	 * doImmediateDelete gets the softwareFiles that are in immediateDelete mode deleted
	 * @param publishedLocations 
	 * @throws IOException 
	 * @throws ProcessingException 
	 */
	public void doImmediateDelete(HashSet<String> publishedLocations) throws IOException, ProcessingException {
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {
				swFile.doImmediateDelete(destination, inSubscription, publishedLocations);
			}
		}
		// set the availability to false
		if (((updateType == ITEM_IMMEDIATE_DELETED) || !inSubscription) && published) {
			setPublished(false);
		}
	}

	/**
	 * doUpdates gets the softwareFiles that are in Updates mode updated
	 * @throws IOException 
	 * @throws ProcessingException 
	 */
	public void doUpdates() throws ProcessingException {
		if (!updateCompleted) {
			if (inSubscription && shouldPublish) {
				// This item is in the subscription so publish it
				for(String key : swFileSets.keySet()) {
					for(SoarExtractSWFile swFile : swFileSets.get(key)) {
						try {
							LoaderLog.info("SoarItem doUpdates swFileSets col: " +collectionId+":"+itemId+":"+swFile.getIdentifier());
							if(pushFTPtoo){
								LoaderLog.debug("pushFTPtoo is ->"+pushFTPtoo+" hence loading the software files to push to ftp.");
								swFile.doUpdate(destination);
							}else{
								LoaderLog.debug("pushFTPtoo is ->"+pushFTPtoo+" hence NOT loading the software files to push to ftp.");
							}
						} catch (IOException e) {
							// TODO:  Determine if different types of IOExceptions mean retry or don't retry
							LoaderLog.error("swFile update failed "+e.getMessage()+ " retry count = "+ (++retryCount));
							if (retryCount >= 5) {
								Log.report("Failed to write file to FTP server", 
										LoaderLog.getExceptionMsgForLog("Failed write:"+collectionId+":"+itemId+":"+swFile.getIdentifier(), e));
								System.exit(1);  //abnormal termintion when FTP session fails(BR686204--13.2 Release)
								throw new ProcessingException(e, false);
							} else {
								throw new ProcessingException(e, true);
							}
						}
					}
				}
			}
			// the bit should show this item as published
			if (inSubscription && shouldPublish && !published) {
				setPublished(true);
			}
			
			// if I get to here, the swFiles for this item have been completed
			updateCompleted = true;
		}
	}

	/**
	 * doStore puts attachments into cdsplus
	 * @throws IOException 
	 * @throws ProcessingException 
	 */
	public void doStore(Integer priority) throws ProcessingException {
		System.out.println("storign item :"+ itemId);
		if (!storeCompleted && !ftpOnlyUpdate) {
			for(SoarExtractAttachment attachment : attachments) {
				try {
					attachment.doStore(priority);
				} catch (ProcessingException e) {
					LoaderLog.error("Item "+itemId+" update failed "+e.getMessage()+ " retry count = "+ (++retryCount));
					throw e;
				}
			}

			// if I get to here, this attachments have been stored
			storeCompleted = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.workItem.SoarExtractElement#getEvent(java.lang.String)
	 */
	protected String getEvent(String status) {
		if (status == WorkItem.SUCCESS) {
			return ((updateType == ITEM_DELETED || (updateType == ITEM_IMMEDIATE_DELETED)) ? EXTITMDEL : EXTITMUPD);
		} else {
			return ((updateType == ITEM_DELETED || (updateType == ITEM_IMMEDIATE_DELETED)) ? EXTITMDELFAIL : EXTITMUPDFAIL);
		}
	}

	private HashMap<String, String> getEnvMinServiceReleaseMap() throws DfException {
		HashMap<String, String> envMsrMap = new HashMap<String, String>();
		// create the map of environmentOid -> MinServiceReleaseOid
		String[] environments = getRepeatingAttr(dbObj, "environment_detail_oids");
        String[] minServRels = getRepeatingAttr(dbObj, "min_service_release_oids");
        
        for(int i=0; i<environments.length && i<minServRels.length; i++) {
        	envMsrMap.put(environments[i], minServRels[i]);
        }
        return envMsrMap;
	}
	
	protected String getIdentifier() {
		return itemId;
	}

	/* (non-Javadoc)
	 * @see com.hp.soar.priorityLoader.workItem.SoarExtractElement#getObjectType()
	 */
	protected String getObjectType() {
		return "sw_item";
	}
	
	/**
	 * getValidEnvironmentsForItem returns the HashSet of environment oids for this item 
	 * @return
	 * @throws DfException
	 */
	private HashSet<String> getValidEnvironmentsForItem() throws DfException {
		HashSet<String> validEnvironments = new HashSet<String>();
		
		//SOAR 13.5.1 release,add the environments based on the check list of active flag and release_date
		String dqlStr;
		IDfCollection results;		
		
		String[] environmentDetailOids = getRepeatingAttr(dbObj,"environment_detail_oids");
		StringBuilder envi = new StringBuilder();
		
		if (environmentDetailOids!=null && environmentDetailOids.length > 0) {
		   for (String n : environmentDetailOids) { 
		     if (envi.length() > 0) 
			 envi.append(',');
		     envi.append("'").append(n).append("'");
		   }
		   try{
		     dqlStr="select environments_details_oid from dm_dbo.soar_environments where environments_details_oid in ("+envi.toString()+")"+" "+"and active=1 and date_available < date(tomorrow)";
		     //System.out.println("dql string"+dqlStr);
		     results=dbService.getResults(dqlStr, "to get the details of environment");
		     if (results != null) {
			   while (results.next()) {
			      String validoid=results.getString("environments_details_oid");
			      //System.out.println("valid envis:"+validoid);
			      validEnvironments.add(validoid);				
		       }			
		     }
		   }
		   catch (DfException e) {
			LoaderLog.error("Failed to retrive flag oid's " + ExtractorUtils.getStackTrace(e));
			throw e;
		   }
	    }
		else
			LoaderLog.error("validevironments list is empty");		
		
		return validEnvironments;
	}
	
	/**
	 * hasAttachments return true if there are attachments in the swItem
	 * @return
	 */
	public boolean hasAttachments() {
		return attachments.size() > 0;
	}
	
	/**
	 * loadSWFiles gets the swFiles that belong to this item into memory
	 * @param eventId 
	 * @throws DfException 
	 * @throws IOException 
	 * @throws VirusScanException 
	 */
	private void loadSWFiles(Date lastExtractionTime, String eventId) throws DfException, IOException {
		String assembleId = dbObj.getString("r_assembled_from_id");
		String itemObjId = dbObj.getString("r_object_id");

		String vDocBuildMethod = "document";
		if (!assembleId.equals(FIRST_VERSION_ANTECEDENT_ID)) {
			vDocBuildMethod = "assembly";
		}

		// Order by file_type_oid and file_sequence to get output ordering
		String dqlQuery1 = "SELECT distinct r_object_id as file_obj_id FROM sw_file (ALL) WHERE r_object_id IN (SELECT r_object_id from dm_sysobject (all) in "
				+ vDocBuildMethod + " id('" + itemObjId  + "') WHERE r_object_type = 'sw_file')";
		/*
		 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- Converting the logger from info to debug to 
		 * avoid DQL statements in the log
		 */
		//LoaderLog.info("extractSoftwareFiles DQL : " + dqlQuery1);
		LoaderLog.debug("extractSoftwareFiles DQL : " + dqlQuery1);
		    
		List<String> fileObjIds = dbService.getAsList(dqlQuery1, "file_obj_id", "getSoftwareFiles");
		
		for(String fileObjId : fileObjIds) {
			try {
				SoarExtractSWFile swFile = SoarExtractSWFile.getSWFile(destination, dbService, fileObjId, updateType, lastExtractionTime, eventId, collectionId, itemId);
				String fileTypeOid = swFile.getFileTypeOid();
				ArrayList<SoarExtractSWFile> setList = swFileSets.get(fileTypeOid);
				if (setList == null) {
					setList = new ArrayList<SoarExtractSWFile>();
					swFileSets.put(fileTypeOid, setList);
				}
				setList.add(swFile);
			} catch (IllegalArgumentException ie) {
				LoaderLog.error(ie.getMessage());
			}
		}
		
		// now sort each fileSets into sequence
		for(String key : swFileSets.keySet()) {
			ArrayList<SoarExtractSWFile> swList = swFileSets.get(key);
			Collections.sort(swList, new Comparator<SoarExtractSWFile>() {
		  		/* (non-Javadoc)
		  		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		  		 */
		  		public int compare(SoarExtractSWFile o1, SoarExtractSWFile o2) {
		  			// order based on file_sequence
		  			String fs1 = o1.getFileSequence();
		  			String fs2 = o2.getFileSequence();
		  			return fs1.compareTo(fs2);
		  		}
			});

		}
		LoaderLog.info("SoarItem loadSWFiles swFileSets size: " + swFileSets.size());	
		
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {
					LoaderLog.info("SoarItem loadSWFiles swFileSets col: " +collectionId+":"+itemId+":"+swFile.getIdentifier());
			}
		}
		
	}
	
	public void logSoarEvent(IDfSession session, StringBuffer dql, String status) throws DfException {
		if (updateType != ITEM_UPTODATE) {
			// only put in a log entry if this item is not uptodate
			super.logSoarEvent(session, dql, status, getEvent(status),  dbObj.getString("i_chronicle_id"), collectionId, getObjectType(), null);
		}
	}

	/**
	 * setPublished sets the low bit of the externally_available_item
	 * @param val
	 * @throws ProcessingException
	 */
	private void setPublished(boolean val) throws ProcessingException {
		try {
			boolean immutable = dbObj.getBoolean("r_immutable_flag");
            dbObj.setBoolean("r_immutable_flag", false);
			dbObj.setString("externally_available_item", (val ? EXT_AVAIL : NOT_EXT_AVAIL));
			dbObj.save();
			if (immutable) {
				dbObj.setBoolean("r_immutable_flag", immutable);
				dbObj.save();
			}
		} catch (DfException e) {
			Log.report("Failed to update item externally_available_item",
					LoaderLog.getExceptionMsgForLog(collectionId+":"+itemId+" Failed to update externally_available_item", e));
		}
	}
	
	public void getPublishedLocations(HashSet<String> publishedLocations) {
		if (inSubscription) {
			// only get locations that will actually publish.  If this item is not in the subscription, it shouldn't publish
			for(String key : swFileSets.keySet()) {
				for(SoarExtractSWFile swFile : swFileSets.get(key)) {
					swFile.getPublishedLocations(publishedLocations);
				}
			}		
		}
	}
	
	/**
	 * setupDelete sets the swFiles up for deletion
	 */
	public void setupDelete() {
		LoaderLog.info("SoarItem setupDelete swFileSets size: " + swFileSets.size());	
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {
					LoaderLog.info("SoarItem setupDelete swFileSets col: " +collectionId+":"+itemId+":"+swFile.getIdentifier());
			}
		}
		for(String key : swFileSets.keySet()) {
			for(SoarExtractSWFile swFile : swFileSets.get(key)) {				
				swFile.setupDelete();
			}
		}
	}
	
	/**
	 * init sets up the filter for a collection
	 * @param config
	 */
	public static void init(ConfigurationReader config) {
		// see if it is set not to send to ftp site
		
		pushFTPtoo= Boolean.parseBoolean(config.getAttribute(PUSHFTPTOO));
		primary_loader= Boolean.parseBoolean(config.getAttribute(PRIMARY_lOADER));
		
		//System.out.println("pushFTPtoo->"+pushFTPtoo);
		
		// see if it is set to only do FTP updates
		String upd = config.getAttribute(FTPONLYUPDATE);
		ftpOnlyUpdate = ( upd != null && "true".equalsIgnoreCase(upd));

		org.w3c.dom.Element destElem = config.getElement(SFTPDestination.REMOTEDESTINATION);
		
        NodeList nodes = destElem.getElementsByTagName(FilterNode.FILTER);
        if (nodes == null) {
        	return;
        }
        if (nodes.getLength() > 1) {
        	throw new IllegalArgumentException("Filter Parse Exception - Multiple filter elements not supported");
        }

        // Get the item element
        nodes = ((org.w3c.dom.Element)nodes.item(0)).getElementsByTagName(FilterNode.ITEM);
        
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
            	LoaderLog.info("Item Filter: "+filter.toString());
            }
        } else {
        	throw new IllegalArgumentException("Filter Parse Exception - Multiple item elements found in filter");
        }
        
		// get the unzip fileType oid 
		unzipFileTypeOid = ReferenceLists.getKey(ReferenceLists.FILE_TYPES_LIST, "unzip");
		if (unzipFileTypeOid == null) {
			throw new IllegalArgumentException("Unzip file type not in reference list");
		}
	}
	
	
}
