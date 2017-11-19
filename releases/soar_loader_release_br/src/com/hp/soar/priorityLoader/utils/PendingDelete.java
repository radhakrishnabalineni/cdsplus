package com.hp.soar.priorityLoader.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.workItem.SoarExtractSWFile;

/**
 * PendingDelete is an object representing a software binary that is to be deleted in the future
 * 
 * @author dahlm
 *
 */
public class PendingDelete {

	private static final String DELETEDELAYINMINUTES = "deleteDelayInMinutes";
	private static final String SWFILEID = "sw_file_id";
	private static final String SWPATH = "sw_path";
	private static final String COLLECTIONID = "collection_id";
	private static final String ITEMID = "item_id";
	private static final String REQUEST_TIME = "request_time";
	private static final String EXECUTE_TIME = "execute_time";

	private static long deleteDelayInMillis = 0;
	
	private String swPath;
	private String swContentId;
	private String swItemId;
	private String swFileOid;
	private Date   queuedTime;
	private Date   deleteTime;
	
	
	public PendingDelete(String swPath, String swContentId, String swItemId,
			String swFileOid, long queuedTime, long deleteTime) {
		super();
		this.swPath = swPath;
		this.swContentId = swContentId;
		this.swItemId = swItemId;
		this.swFileOid = swFileOid;
		this.queuedTime = new Date(queuedTime);
		this.deleteTime = new Date(deleteTime);
	}

	public PendingDelete(String swPath, String swContentId, String swItemId,
			String swFileOid) {
		super();
		this.swPath = swPath;
		this.swContentId = swContentId;
		this.swItemId = swItemId;
		this.swFileOid = swFileOid;
		queuedTime = new Date(System.currentTimeMillis());
		deleteTime = new Date(System.currentTimeMillis() + deleteDelayInMillis);
	}

	public PendingDelete(IDfCollection result) throws DfException {
		super();
		this.swPath = result.getString(SWPATH);
		IDfTime tmpDate = result.getTime(EXECUTE_TIME);
		// either get a date or delete it delay time from now
		this.deleteTime = (tmpDate != null) ? tmpDate.getDate() : new Date(System.currentTimeMillis()+deleteDelayInMillis);
		tmpDate = result.getTime(REQUEST_TIME);
		this.queuedTime = (tmpDate != null) ? tmpDate.getDate() : new Date(System.currentTimeMillis()+deleteDelayInMillis);
		this.swItemId = result.getString(ITEMID);
		this.swContentId = result.getString(COLLECTIONID); 
		this.swFileOid = result.getString(SWFILEID);
	}
	
	public String getSwPath() {
		return swPath;
	}


	public String getSwContentId() {
		return swContentId;
	}


	public String getSwItemId() {
		return swItemId;
	}


	public String getSwFileOid() {
		return swFileOid;
	}


	public Date getQueuedTime() {
		return queuedTime;
	}


	public Date getDeleteTime() {
		return deleteTime;
	}
	
	/**
	 * delete removes the file specified by path from the server
	 * @throws IOException 
	 * @throws DfException 
	 */
	public void delete() throws IOException  {
		LoaderLog.info("Deleting "+swPath+".");
		// ICM changes
		int indexOfLastSlash = swPath.lastIndexOf('/');
		SoarExtractSWFile.sftpDestination.executeDelete(swPath, null, null);
		SoarExtractSWFile.icmDestination.executeDelete(swPath, swPath.substring(indexOfLastSlash+1), swPath.substring(0,indexOfLastSlash));
		try {
			// now delete the file from the pending deletes
			LoaderLog.debug("Remove persisted delete: "+swPath+" at "+deleteTime);
			IDfSession session = ConnectionPool.getDocbaseSession();
			StringBuffer sb = new StringBuffer("delete from dm_dbo.soar_pending_sw_deletes where sw_file_id='");
			sb.append(swFileOid).append("' and ");
			sb.append("sw_path='").append(swPath).append("' and ");
			sb.append("collection_id='").append(swContentId).append("' and ");
			sb.append("item_id='").append(swItemId).append("'");
			
			IDfCollection results = null;
			try {
				results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.DF_EXEC_QUERY, "RemovePendDelete "+swFileOid);
				if (results == null || !results.next()) {
					LoaderLog.warn("Failed to delete pending delete: "+this);
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
			try {
				// flag that this is no longer published.
				// get the dbObject for the sw_file
				IDfDocument dbObj = (IDfDocument) session.getObjectByQualification("sw_file where r_object_id='"+ swFileOid + "'");
				// set all of the repeating attributes for 
				SoarExtractSWFile.setFilePublished(dbObj, false);
			} catch (DfException dfe) {
				LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot set file as unpublished. "+this, dfe));
			}
		} catch (DfException dfe) {
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot delete pending entry"+this, dfe));
		}
	}
	
	/**
	 * format the Date for documentum date
	 * @param date
	 * @return
	 */
	private String getDateString(Date date) {
	    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss");
	    
		StringBuffer sb = new StringBuffer("Date('");
		sb.append(df.format(date)).append("','mm/dd/yyyy hh:mi:ss')");
		return sb.toString();
	}
	
	public void persist() throws DfException {
		LoaderLog.debug("Persist delete: "+swPath+" at "+deleteTime);
		try {
			IDfSession session = ConnectionPool.getDocbaseSession();
			IDfCollection results = null;
			StringBuffer sb = new StringBuffer();
			sb.append("select sw_path from dm_dbo.soar_pending_sw_deletes where sw_file_id='").append(swFileOid).append("'");
			try {
				results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.DF_EXEC_QUERY, "PendDelete "+swFileOid);
				if (results == null || !results.next()) {
					LoaderLog.error(swPath+" already persisted.");
					return;
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
			// now persist the delete
			sb.setLength(0);
			sb.append("insert into dm_dbo.soar_pending_sw_deletes (sw_file_id, sw_path, collection_id, item_id, request_time, execute_time) values('");
			sb.append(swFileOid).append("','").append(swPath).append("','").append(swContentId).append("',");
			sb.append("'").append(swItemId).append("',").append(getDateString(queuedTime)).append(",").append(getDateString(deleteTime)).append(")");
			try {
				results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.DF_EXEC_QUERY, "PendDelete "+swFileOid);
				if (results == null || !results.next()) {
					LoaderLog.error("Failed to persist delete: "+this);
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
		} catch (DfException dfe) {
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot persist pending entry"+this, dfe));
			throw dfe;
		}
	}
	
	public String toString() {
		return "swPath: "+swPath+" | swCollectionId: "+swContentId + " | swItemId: "+swItemId+" | swFileObjId: "+
				swFileOid+" | queuedTime: "+queuedTime+" | deleteTime: "+deleteTime;
	}
	
	public void unPersist() throws DfException {
		LoaderLog.info("Remove pending delete: "+swPath+" :"+swContentId+":"+swItemId+" at "+deleteTime);
		try {
			IDfSession session = ConnectionPool.getDocbaseSession();
			StringBuffer sb = new StringBuffer("delete from dm_dbo.soar_pending_sw_deletes where ");
			sb.append("sw_file_id='").append(swFileOid).append("' and ");
			sb.append("collection_id='").append(swContentId).append("' and ");
			sb.append("item_id='").append(swItemId).append("' and ");
			sb.append("sw_path='").append(swPath).append("'");
			IDfCollection results = null;
			try {
				results = DocbaseUtils.executeQuery(session, sb.toString(), IDfQuery.DF_EXEC_QUERY, "Remove pendDelete "+swFileOid);
				if (results == null || !results.next()) {
					LoaderLog.error("Failed to UnPersist delete: "+this);
				}
			} finally {
				DocbaseUtils.closeResults(results);
				ConnectionPool.releaseDocbaseSession(session);
			}
		} catch (DfException dfe) {
			LoaderLog.warn(LoaderLog.getExceptionMsgForLog("Cannot unPersist pending entry"+this, dfe));
			throw dfe;
		}
	}
	
	public static void start(ConfigurationReader config) {
		String delDelayStr = config.getAttribute(DELETEDELAYINMINUTES);
		if (delDelayStr == null || delDelayStr.length() == 0) {
			throw new IllegalArgumentException(DELETEDELAYINMINUTES + " is not set in config file.");
		}
		long delayInMinutes = 0;
		try {
			delayInMinutes = Long.parseLong(delDelayStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(DELETEDELAYINMINUTES + " is not a number in config file.");
		}
		deleteDelayInMillis = delayInMinutes * 60 * 1000; 
	}
}
