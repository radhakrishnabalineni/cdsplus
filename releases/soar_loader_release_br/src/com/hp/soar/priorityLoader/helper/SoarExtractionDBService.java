package com.hp.soar.priorityLoader.helper;

/****************************************************************************************************************
 * @(#)$Project: SOAR Project
 * @(#)$Source: .SoarExtractionDBService.java
 * @(#)$Revision: $
 * @(#)$Date: Nov 7, 2008
 * @(#)$Author: mariswam
 *
 * Copyright (c) 2006 Hewlet-Packard Company
 * All Rights Reserved
 *
 ****************************************************************************************************************/

import java.util.Date;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
import com.hp.soar.priorityLoader.services.SoarDBService;
import com.hp.soar.priorityLoader.utils.DocbaseUtils;
import com.hp.soar.priorityLoader.workItem.SoarItem;

public class SoarExtractionDBService extends SoarDBService {

	public SoarExtractionDBService(IDfSession session) {
		super(session);
	}

	public Date getLastEmailTimeStamp(String collectionId) throws DfException {
		String dql = "select last_email_timestamp from dm_dbo.soar_invalid_collections where collection_id='"+collectionId+"'"; 
		return getColValueAsDate(dql, "last_email_timestamp", "getLastEmailTimeStamp");
	}

	public void createInvalidCollectionRecord(String collectionId, String soarBusinessLead) throws DfException {
		String dql = "insert into dm_dbo.soar_invalid_collections values ('"+collectionId+"', date('"+dfcTime()+"','"+DFC_DATE_FORMAT+"'), '"+soarBusinessLead+"')";
		executeUpdate(dql, "createInvalidCollectionRecord");
	}

	public void updateInvalidCollectionRecord(String collectionId, String soarBusinessLead) throws DfException {
		String dql = "update dm_dbo.soar_invalid_collections set last_email_timestamp=date('"+dfcTime()+"','"+DFC_DATE_FORMAT+"'), soar_business_lead='"+soarBusinessLead+"' where collection_id='"+collectionId+"'";
		executeUpdate(dql, "updateInvalidCollectionRecord");
	}

	public void deleteInvalidCollectionRecord(String collectionId) throws DfException  {
		String dql = "delete from dm_dbo.soar_invalid_collections where collection_id='"+collectionId+"'";
		executeDelete(dql, "deleteInvalidCollectionRecord");
	}

	public void deleteSoarExtractProducts(String partner) throws DfException {
		String dql = "delete from dm_dbo.soar_extract_products where extract_id = '" + partner + ".map'";
		executeDelete(dql, "deleteSoarExtractProducts");
	}

	public Date getLastExtractionTimeStamp(String collectionId, String chronicleId) throws DfException {
		Date lastTimeStamp = null;
		StringBuffer dql = new StringBuffer("select event_date from dm_dbo.soar_extractor_events where chronicle_id='");
		dql.append(chronicleId).append("' and collection_id='");
		dql.append(collectionId).append("' and extracted=1 order by event_id desc enable (RETURN_TOP 1)");
		try {
			results = DocbaseUtils.executeQuery(session, dql.toString(),IDfQuery.DF_EXEC_QUERY, 
					"getLastExtractionTimeStamp");
			while (results.next()){
				IDfTime date = results.getTime("event_date");
				lastTimeStamp = date.getDate();
			}
		}finally{
			DocbaseUtils.closeResults(results);
		}
		
		return lastTimeStamp;
	}
	
	public IDfCollection getResults(String dql, String description) throws DfException {
		return DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_READ_QUERY, description);
	}
	
	public IDfDocument getDocumentByQualification(String qualification) throws DfException {
		return (IDfDocument) session.getObjectByQualification(qualification);
	}
	
	public IDfPersistentObject getObjectByQualification(String qualification) throws DfException {
		return session.getObjectByQualification(qualification);
	}
	
//	public String getSubmittalType(String collectionId) throws DfException{
//		String dql = "select submittal_type_oid from sw_collection where collection_id = '"+collectionId+"'";
//		return getColValueAsString(dql, "submittal_type_oid", "getSubmittalType");
//	}
	
	public String previouslyExtractedItem (String itemId, String chronicleId) throws DfException {
		StringBuffer dql = new StringBuffer();
		dql.append("select event from soar_event_log where ");
		dql.append("object_id='").append(itemId).append("'");
		dql.append(" and chronicle_id='").append(chronicleId).append("'");
		dql.append(" and event in ('").append(SoarItem.EXTITMDEL).append("','").append(SoarItem.EXTITMUPD);
		dql.append("') order by event_id asc enable(RETURN_TOP 1)");
		IDfCollection results = null;
		try {
			results = DocbaseUtils.executeQuery(session, dql.toString(), IDfQuery.READ_QUERY, "Get Item extraction status");
			if (results.next()) {
				return results.getString("event");
			}
			return null;
		} finally {
			DocbaseUtils.closeResults(results);
		}
	}

	public void updateExtractorStatus(String status, String soarMsgId) throws Exception {
		if(soarMsgId != null && !soarMsgId.equals("")){
			String dql = "update dm_dbo.soar_processing_feedback set processed_status='"+status+"', " +
					"sent_timestamp=date('"+dfcTime()+"','"+DFC_DATE_FORMAT+"') " +
					"where soar_msg_id='"+soarMsgId +"'";
			executeUpdate(dql, "updateExtractorStatus");
		}
	}

	public void insertExtractorStatus(String status, String soarMsgId, String partnerId, String event, 
			String itemId, String processId) throws DfException {
		if(soarMsgId != null && !soarMsgId.equals("")){
			String dqlStatement = "insert into dm_dbo.soar_processing_feedback values('" + soarMsgId +"','"+processId+"','"+itemId + "','" + event + "',Date(Now),'" + partnerId + "','','','','"+status+"','','')";
		    executeUpdate(dqlStatement, "updateExtractorStatus");
		}
	}
	
    
	public Date getCollectionCreationDate(String collectionId) throws DfException {
		String dql = "select r_creation_date from sw_collection (all) where collection_id='"+collectionId+"' and r_object_id=i_chronicle_id";
		Date date = getColValueAsDate(dql, "r_creation_date", "getCollectionCreationDate");
		if(date == null){
			dql = "select r_creation_date from sw_collection (all) where collection_id='"+collectionId+"'";
			date = getColValueAsDate(dql, "r_creation_date", "getCollectionCreationDate");
		}
		return date;
	}
	
	public Date getItemCreationDate(String itemId, String collectionId) throws DfException {
		String dql = "select r_creation_date from sw_item (all) where item_id='"+itemId+"' and r_object_id=i_chronicle_id and collection_id='"+collectionId+"'";
		Date date = getColValueAsDate(dql, "r_creation_date", "getItemCreationDate");
		if(date == null){
			dql = "select r_creation_date from sw_item (all) where item_id='"+itemId+"' and collection_id='"+collectionId+"'";
			date = getColValueAsDate(dql, "r_creation_date", "getItemCreationDate");
		}
		return date;
	}
	
	public Date getFileCreationDate(String rObjectId) throws DfException {
		String dql = "select r_creation_date from sw_file (all) where r_object_id='"+rObjectId+"' and r_object_id=i_chronicle_id";
		Date date = getColValueAsDate(dql, "r_creation_date", "getFileCreationDate");
		if(date == null){
			dql = "select r_creation_date from sw_file (all) where r_object_id='"+rObjectId+"'";
			date = getColValueAsDate(dql, "r_creation_date", "getFileCreationDate");
		}
		return date;
		
	}
	public String getSoarBusinessLead(String collectionId) throws DfException {
		String dql = "select g.contact_email as contact_address from dm_dbo.soar_submittal_groups g, " +
				"sw_collection c where c.submittal_group_oid=g.group_name and collection_id = '" + collectionId + "'";
		return getColValueAsString(dql, "contact_address", "getSoarBusinessLead");
	}

	public boolean isItemDuplicated(String itemId, String collectionId) throws DfException {
		String dql = "select count(item_id) as duplicate_count from sw_item (all) where item_id='"+itemId+"' and collection_id='"+collectionId+"' and item_state <> 'APPROVED' and item_state <> 'OBSOLETE' ";
		int count = getColValueAsInt(dql, "duplicate_count", "isItemDuplicated");
		if(count > 1){
			return true;
		}
		dql = "select count(item_id) as duplicate_count from sw_item (all) where item_id='"+itemId+"' and collection_id='"+collectionId+"' and item_state='APPROVED'";
		count = getColValueAsInt(dql, "duplicate_count", "isItemDuplicated");
		if(count > 1){
			return true;
		}
		return false;
	}
	
	public boolean isCollectionDuplicated(String collectionId) throws DfException {
		String dql = "select count(collection_id) as duplicate_count from sw_collection (all) where collection_id='"+collectionId+"' and collection_state <> 'APPROVED' and collection_state <> 'OBSOLETE'";
		int count = getColValueAsInt(dql, "duplicate_count", "isCollectionDuplicated");
		if(count > 1){
			return true;
		}
		dql = "select count(collection_id) as duplicate_count from sw_collection (all) where collection_id='"+collectionId+"' and collection_state='APPROVED'";
		count = getColValueAsInt(dql, "duplicate_count", "isCollectionDuplicated");
		if(count > 1){
			return true;
		}
		return false;
	}

	/*TODO: determine if this needs to be implemented
	public List<GBLSubmittalPartnerInfoBean> getBSPUsersOfInterest(String extractionId) throws DfException {
		String dql = "select partner_id from dm_dbo.gbl_submittal_partner_info where delivery_partners_of_interest like '%"+extractionId+"%' and active=1";
		List<GBLSubmittalPartnerInfoBean> bspList = new ArrayList<GBLSubmittalPartnerInfoBean>();
		try {
			results = DocbaseUtils.executeQuery(session, dql, IDfQuery.DF_EXEC_QUERY, "getBSPUsersOfInterest");
			if (results != null){
				while (results.next()){
					String bspPartnerId = results.getString("partner_id");
					GBLSubmittalPartnerInfoBean spib = new GBLSubmittalPartnerInfoBean(session, bspPartnerId);
					spib.populateSelf();
					
					bspList.add(spib);
				}
			}
		} catch (DfException e) {
			logError("DfException in getBSPUsersOfInterest :"+e.getMessage());
			throw e;
		}finally{
			DocbaseUtils.closeResults(results);
		} 
		return bspList;
	}
	*/
	
	public boolean wasCollectionExtractedToPartnerBefore(String collectionId, String partnerFlag) throws DfException {
		String dql = "select count(collection_id) as col_count from dm_dbo.soar_extraction_tracker " +
				"where collection_id='"+ collectionId+ "' and "+ partnerFlag + "=1";
		int colCount = getColValueAsInt(dql, "col_count", "getCollectionEvent");

		return colCount > 0;
	}
	
	public boolean wasItemExtractedToPartnerBefore(String collectionId, String itemId, String partnerFlag) throws DfException {
		String dql = "select count(item_id) as item_count from dm_dbo.soar_extraction_tracker " +
			"where collection_id='"+ collectionId+ "' and item_id='"+itemId+"' and "+ partnerFlag + "=1";
		int itemExtractedCount = getColValueAsInt(dql, "item_count", "getItemEvent");
		
		return itemExtractedCount > 0;
	}
	
	public void deleteFromTmpTrackerTable(String partnerId) throws DfException {
			String dql = "delete from dm_dbo.soar_extraction_tracker_tmp where partner_name='"+ partnerId + "' ";
			executeDelete(dql, "deleteFromTmpTrackerTable");
	}


}
