package com.hp.cdsplus.dao;

import java.io.InputStream;

import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.hp.cdsplus.mongo.utils.SoarMongoAPIUtils;
import com.mongodb.DBObject;


/**
 */
public class Options {
	
	private static MongoAPIUtils s_mongoUtils = new MongoAPIUtils();
	private static SoarMongoAPIUtils s_soarMongoUtils = new SoarMongoAPIUtils();
	
	private MongoAPIUtils mongoUtils ;
	private String contentType;
	private String subscription;
	private long before;
	private long after;
	private int limit;
	private boolean includeDeletes = false;
	private boolean reverse = false;
	private String versions;
	private String docid;
	private String attachmentName;
	
	private String collectionName;	
	private DBObject query;
	private DBObject displayFields;
	private DBObject metadataDocument;
	private DBObject sortFields;


	private String baseUri;
	private String hasAttachments;
	private String eventtype;
	private String priority;
	private String expand;
	private String task;
	private String company;
	
	private long lastModified;
	
	private InputStream inputStream;
	
	/**
	
	 * @return the contentType */
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * In the recent enhancement mongoUtils is assigned
	 * the reference of right type of MongoAPIUtils implementation depending on the contentType 
	 * @param contentType the contentType to set
	 */
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
		if("soar".equalsIgnoreCase(contentType)){
			mongoUtils = s_soarMongoUtils;
		}
		else {
			mongoUtils = s_mongoUtils;
		}
	}
	
	public MongoAPIUtils getMongoUtils() {
		if(this.contentType != null && "soar".equalsIgnoreCase(contentType)){
			mongoUtils = s_soarMongoUtils;
		}
		else{			
			mongoUtils = s_mongoUtils;
		}
		
		return mongoUtils;
	}
	
	/**
	 * Returns the right type of MongoAPIUtils reference depending on the contentType
	 * The method is static for cases where only contentType is available. 
	 * @param contentType the contentType to set
	 */
	
	public static MongoAPIUtils getMongoUtils(String contentType) {
		MongoAPIUtils localMongoUtils = null;
		if("soar".equalsIgnoreCase(contentType)){
			localMongoUtils = s_soarMongoUtils;
		}
		else {
			localMongoUtils = s_mongoUtils;
		}
		
		return localMongoUtils;
	}
	
	/**
	
	 * @return the subscription */
	public String getSubscription() {
		return subscription;
	}
	/**
	 * @param subscription the subscription to set
	 */
	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}
	/**
	
	 * @return the before */
	public long getBefore() {
		return before;
	}
	/**
	 * @param before the before to set
	 */
	public void setBefore(long before) {
		this.before = before;
	}
	/**
	
	 * @return the after */
	public long getAfter() {
		return after;
	}
	/**
	 * @param after the after to set
	 */
	public void setAfter(long after) {
		this.after = after;
	}
	/**
	
	 * @return the limit */
	public int getLimit() {
		return limit;
	}
	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	
	 * @return the includeDeletes */
	public boolean isIncludeDeletes() {
		return includeDeletes;
	}
	/**
	 * @param includeDeletes the includeDeletes to set
	 */
	public void setIncludeDeletes(boolean includeDeletes) {
		this.includeDeletes = includeDeletes;
	}
	/**
	
	 * @return the versions */
	public String getVersions() {
		return versions;
	}
	/**
	 * @param versions the versions to set
	 */
	public void setVersions(String versions) {
		this.versions = versions;
	}
	/**
	
	 * @return the docid */
	public String getDocid() {
		return docid;
	}
	/**
	 * @param docid the docid to set
	 */
	public void setDocid(String docid) {
		this.docid = docid;
	}
	/**
	
	 * @return the attachmentName */
	public String getAttachmentName() {
		return attachmentName;
	}
	/**
	 * @param attachmentName the attachmentName to set
	 */
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}
	/**
	 * @return the query
	 */
	public DBObject getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(DBObject query) {
		this.query = query;
	}
	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return collectionName;
	}
	/**
	 * @param collectionName the collectionName to set
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * @return the displayFields
	 */
	public DBObject getDisplayFields() {
		return displayFields;
	}
	/**
	 * @param displayFields the displayFields to set
	 */
	public void setDisplayFields(DBObject displayFields) {
		this.displayFields = displayFields;
	}
	/**
	 * @return the metadataDocument
	 */
	public DBObject getMetadataDocument() {
		return metadataDocument;
	}
	/**
	 * @param metadataDocument the metadataDocument to set
	 */
	public void setMetadataDocument(DBObject metadataDocument) {
		this.metadataDocument = metadataDocument;
	}
	/**
	 * @return the sortFields
	 */
	public DBObject getSortFields() {
		return sortFields;
	}
	/**
	 * @param sortFields the sortFields to set
	 */
	public void setSortFields(DBObject sortFields) {
		this.sortFields = sortFields;
	}
	/**
	 * @return the baseUri
	 */
	public String getBaseUri() {
		return baseUri;
	}
	/**
	 * @param baseUri the baseUri to set
	 */
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}
	/**
	 * @return the hasAttachments
	 */
	public String getHasAttachments() {
		return hasAttachments;
	}
	/**
	 * @param hasAttachments the hasAttachments to set
	 */
	public void setHasAttachments(String hasAttachments) {
		this.hasAttachments = hasAttachments;
	}
	/**
	 * @return the eventtype
	 */
	public String getEventtype() {
		return eventtype;
	}
	/**
	 * @param eventtype the eventtype to set
	 */
	public void setEventtype(String eventtype) {
		this.eventtype = eventtype;
	}
	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
	/**
	 * @return the expand
	 */
	public String getExpand() {
		return expand;
	}
	/**
	 * @param expand the expand to set
	 */
	public void setExpand(String expand) {
		this.expand = expand;
	}
	/**
	 * @return the task
	 */
	public String getTask() {
		return task;
	}
	/**
	 * @param task the task to set
	 */
	public void setTask(String task) {
		this.task = task;
	}
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	public boolean isReverse() {
		return reverse;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	/**
	 * @param inputStream the inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public String getCompany() 
	{
	    if(this.company == null)
		this.company = "";
	    return this.company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	
}
