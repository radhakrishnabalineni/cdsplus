package com.hp.cdsplus.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;

/**
 */
public class ContentDAO {

	public static final String SUBSCRIPTION_COLLECTION_NAME = "subscriptionCollection";
	public static final String METADATA_STAGING_COLLECTION_NAME = "metadataStagingCollection";
	public static final String METADATA_LIVE_COLLECTION_NAME = "metadataLiveCollection";
	public static final String METADATA_TEMP_COLLECTION_NAME = "metadataTempCollection";
	public static final String METADATA_HISTORY_COLLECTION_NAME = "metadataHistoryCollection";
	public static final String METADATA_CACHE_COLLECTION_NAME = "metadataCacheCollection";
	public static final String CONTENT_FILES_COLLECTION_NAME = "content.files";

	private static final String GROOM_MAPPINGS_COLLECTION_NAME = "groom_mappings";
	private static final String GROOMS_COLLECTION_NAME = "grooms";
	
	private static final String STATUS_COLLECTION_NAME = "status";
	private static final String CONTENT_BUCKET_NAME = "contentBucket";

	private static final String ID_FIELD = "_id";
	private static final String EVENT_TYPE_FIELD = "eventType";
	private static final String PRIORITY_FIELD="priority";
	private static final String LAST_MODIFIED_TS_FIELD="lastModified";
	private static final String SUBSCRIPTIONS_FIELD = "subscriptions";
	private static final String HASATTACHMENTS_FIELD = "hasAttachments";
	private static final String MEATDATA_DOCID_FIELD = "metadata.docid";
	private static final String DELETE_SUBS = "deleteSubs";
	
	private static final String PROCESSOR_REGISTRY_COLLECTION_NAME = "registry";

	/**
	 * No Argument Constructor
	 */
	public ContentDAO(){
	}

	/**
	 * This method will be called for the following URL pattern in Cadence<br/>
	 * http://cdsplus.austin.hp.com/cadence/app</href><br/>
	 * This is used to display all the supported content types in the application<br/>
	 * the list is cached in ConfigurationManager singleton class in MongoUtils.<br/>
	 * The list is populated from MAPPINGS collection in configDB for the calling application<br/>
	 * Web Service 	- WEB_MAPPINGS<br/>
	 * Processor	- PROCESSOR_MAPPINGS<br/>
	 * SpaceDog		- REF_CLIENT_MAPPINGS<br/>
	 * Loaders		- LOADER_MAPPINGS<br/>
	 * 
	 * @return ArrayList<DBObject><br/>
	 * @throws MongoUtilsException <br/>
	 */
	public ArrayList<DBObject> getContentTypeList(Options options) throws MongoUtilsException{
		// based on which module is calling this, the mapping key set will have its own set of content types returned in the set
		Set<String> contentTypeSet = ConfigurationManager.getInstance().keySet();
		ArrayList<DBObject> results = new ArrayList<DBObject>();
		for(String id : contentTypeSet){
			results.add(new BasicDBObject("_id",id).append("count", ConfigurationManager.getInstance().getMappingValue(id, "count")));
		}
		return results;

	}

	/**
	 * This is to support the following URL pattern<br/>
	 * http://cdsplus.austin.hp.com/cadence/app/library/*<br/>
	 * http://cdsplus.austin.hp.com/cadence/app/library/?limit=10<br/>
	 * <br/>
	 * Mandatory Parameter to set in Options Object<br/>
	 * String contentType		-	options.setContentType()<br/>
	 * <br/>
	 * Optional Parameters <br/>
	 * int limit				- options.setLimit()<br/>
	 * <br/>
	 * If for a given content type subscriptions collection is not defined, request throws OptionsException<br/>
	 * @return ArrayList<DBObject><br/>
	 * @throws MongoUtilsException <br/>
	 * @throws OptionsException <br/>
	 */
	public TreeSet<String> getSubscriptionList(Options options) throws MongoUtilsException, OptionsException{

		// check if a valid content type has been provided
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not defined to get the subscription list");
		}

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Invalid content type specified.");
		}
		DBObject record = ConfigurationManager.getInstance().getConfigMappings().getConfigCache().get(options.getContentType());
		//System.out.println(record);
		//System.out.println(record.containsField(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY));
		if(record.containsField(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY)){
			TreeSet<String> subSet =  (TreeSet<String>) record.get(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY);
			//System.out.println("In content DAO method - "+subSet);
			return subSet;
		}
		return new TreeSet<String>();
	}

	/**
	 * @param options
	 * @return hashMap<String,String> 
	 * 		key 	- subscription name
	 * 		value 	- subscription Filter
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public Map<String, DBObject> getAllSubscriptions(Options options) throws OptionsException, MongoUtilsException{
		HashMap<String, DBObject> subscriptionMap = new HashMap<String, DBObject>();
		String javafilter = null;
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not defined to get the subscription list");
		}
		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Invalid content type specified.");
		}
		// check if for the given content type we get a valid subscription collection name
		String subCollName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), SUBSCRIPTION_COLLECTION_NAME);
		if(isNull(subCollName)){
			throw new OptionsException("Cannot find a subscription Collection for the specified content type : "+options.getContentType());
		}
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), subCollName, new BasicDBObject());
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		while(cursor.hasNext()){
			DBObject subFilter = null;
			DBObject sub = cursor.next();
			String filter  =  sub.get("filter").toString();
			if(!("".equals(javafilter)) && sub.get("javafilter")!=null){
				javafilter = sub.get("javafilter").toString();
				javafilter=javafilter.replace("//", ".");
				javafilter=javafilter.replace("#", "$");
				javafilter=javafilter.replace("\\", "");
				if(javafilter.startsWith("[")){
					javafilter = javafilter.replace(" ", "");
					javafilter = javafilter.substring(2, javafilter.length()-2);
				}
				subFilter = (DBObject) JSON.parse(javafilter);
			}else if(!("".equals(filter)) && filter!=null){
				subFilter = (DBObject) JSON.parse(filter);
			}else{
				subFilter = new BasicDBObject();
			}		
			subscriptionMap.put(sub.get("_id").toString(),subFilter);
		}
		cursor.close();
		return subscriptionMap;
	}

	/**
	 * Method getTempDocumentList - This method will get the delta document list from staged metadata collection
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBCursor getTempDocumentList(Options options) throws MongoUtilsException, OptionsException{
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		return this.getDocumentList(metadataCollectionName, options);
	}

	/**
	 * Method getStagedDocumentList - This method will get the delta document list from staged metadata collection
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBCursor getStagedDocumentList(Options options) throws MongoUtilsException, OptionsException{
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_STAGING_COLLECTION_NAME);
		return this.getDocumentList(metadataCollectionName, options);
	}

	/**
	 * Method getLiveDocumentList - This method will get the delta document list from live metadata collection
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBCursor getLiveDocumentList(Options options) throws MongoUtilsException, OptionsException{
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_CACHE_COLLECTION_NAME);
		return this.getDocumentList(metadataCollectionName, options);
	}

	/** This method is used for following URL requests in Cadence
	 *  ~base_url/cadence/app/library/content/?limit=10&before=123456789&after=1234566666
	 *  ~base_url/cadence/app/library/atlas_201/?limit=10&before=123456789&after=1234566666
	 *  required option parameters 
	 *  contentType - options.setContentType()
	 *  
	 *  optional Parameters
	 *  Long after 				- options.setAfter()
	 *  Long before 			- options.setBefore()
	 *  int limit	 			- options.setLimit()
	 *  String subscription		- options.getSubscription()
	 *  DBObject displayFields	- options.getDisplayFields()
	 *  by default display fields is hard coded to : _id, eventType, priority, lastModified, subscriptions
	 *  
	 *  Result is sorted by "lastModified" time stamp field by default and is hard coded for now
	 *  
	 * Method getDocumentList.
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	private DBCursor getDocumentList(String collectionName, Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not been set. Set the content type in options class to retrieve the docuement list");
		}

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type not defined");
		}
		if(isNull(collectionName)){
			throw new OptionsException("Cannot find a valid metadata collection "+collectionName+" provided for the specified content type : "+options.getContentType());
		}
		QueryBuilder builder = QueryBuilder.start();


		// workaround
		/*
		 * 1. check if subscriptions  is available in metadata_cache collection. 
		 * 2. if metadata_cache doesn't have subscription array. then include all deleted records
		 * 
		 */

		// if its subscription specific
		if(!isNull(options.getSubscription())){
			ArrayList<String> subscriptions = new ArrayList<String>();
			subscriptions.add(options.getSubscription());

			//CR 482 - handling deletes
			if(options.isIncludeDeletes()){
				BasicDBObject subCheck = new BasicDBObject("subscriptions",new BasicDBObject("$exists",true).append("$in", subscriptions));
				//Deleted Subscription feature Change 
				BasicDBObject subChange = new BasicDBObject("deleteSubs",
						new BasicDBObject("$exists", true).append("$in",
								subscriptions));
				builder.or(subCheck, new BasicDBObject("eventType", "delete"),
						subChange);
			}else{
				builder.put("subscriptions").in(subscriptions);
			}


			// CR482 ends
		}


		// last_modified > after and last_modified < before
		if(options.getAfter() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).greaterThanEquals(options.getAfter());
		}

		if(options.getBefore() > 0){
			builder.and(LAST_MODIFIED_TS_FIELD).lessThan(options.getBefore());
		}

		if(!options.isIncludeDeletes()){
			builder.and("eventType").notEquals("delete");
		}

		if(options.getQuery() != null){
			builder.and(options.getQuery());
		}

		if(options.getHasAttachments()!=null){
			builder.and(HASATTACHMENTS_FIELD).is(Boolean.valueOf(options.getHasAttachments()));
		}

		DBObject fields=  options.getDisplayFields();

		if(fields == null){
			fields = new BasicDBObject(ID_FIELD,true)
			.append(LAST_MODIFIED_TS_FIELD, true)
			.append(PRIORITY_FIELD, true)
			.append(SUBSCRIPTIONS_FIELD, true)
			.append(EVENT_TYPE_FIELD, true)
			.append(HASATTACHMENTS_FIELD, true)
			//Deleted Subscription feature Change 
					.append(DELETE_SUBS, true);
		}

		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), collectionName, builder.get(),fields);
		// reverse the order of the results
		cursor.sort(new BasicDBObject("lastModified",-1));

		//System.out.println("Record Count - "+cursor.count());

		//set limit if requested
		if(options.getLimit() > 0)
			cursor.limit(options.getLimit());
		//get the documents from collection and store it in memory to be returned to the calling method.

		return cursor;
	}

	/**
	 * Method getTempDocumentList - This method will get the delta document list from staged metadata collection
	 * this method makes following calls to options object
	 * options.getContentType()
	 * options.getDocid()
	 * [ options.getSubscription() ] - optional 
	 * 
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getTempMetadata(Options options) throws MongoUtilsException, OptionsException{
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		return this.getMetadata(metadataCollectionName, options);
	}

	/**
	 * Method getStagedDocumentList - This method will get the delta document list from staged metadata collection
	 * this method makes following calls to options object
	 * options.getContentType()
	 * options.getDocid()
	 * [ options.getSubscription() ] - optional 
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getStagedMetadata(Options options) throws MongoUtilsException, OptionsException{
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_STAGING_COLLECTION_NAME);
		return this.getMetadata(metadataCollectionName, options);
	}

	/**
	 * Method getLiveDocumentList - This method will get the delta document list from live metadata collection
	 * this method makes following calls to options object
	 * options.getContentType()
	 * options.getDocid()
	 * [ options.getSubscription() ] - optional 
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getLiveMetadata(Options options) throws MongoUtilsException, OptionsException{
		DBObject displayFields = options.getDisplayFields();
		if(displayFields==null)
			displayFields= new BasicDBObject();
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_LIVE_COLLECTION_NAME);
		String subColName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), SUBSCRIPTION_COLLECTION_NAME);

		String subscription = options.getSubscription();
		if(options.isIncludeDeletes()==false)
		if(subscription != null && !"".equals(subscription)){
			DBCursor hierarchyCursor =  options.getMongoUtils().readMeta(options.getContentType(), subColName, new BasicDBObject("_id",options.getSubscription()),new BasicDBObject("hierarchyExpansions",1).append("_id", 0));
			if(hierarchyCursor.hasNext()){
				DBObject hierarchyExpansionsTemp = hierarchyCursor.next();
				DBObject hierarchyExpansions = (DBObject)hierarchyExpansionsTemp.get("hierarchyExpansions");
				for(String key : hierarchyExpansions.keySet()){
					if(subscription == null || "".equals(subscription)){
						displayFields.put(key, 0);
						continue;
					}
					//SMO Change : CR-10349 
					if("soar-software-feed.collection.company_info".equalsIgnoreCase(key))
					{
						continue;
					}
					//Change ends
					
					String value = hierarchyExpansions.get(key).toString();
					if(value == null || "".equals(value)){
						continue;
					}

					Boolean val = Boolean.parseBoolean(value);
					if(!val  || subscription == null || "".equals(subscription)){
						displayFields.put(key, 0);
					}
					if(key.equalsIgnoreCase("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list") && val && displayFields.containsField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers")){
						displayFields.removeField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers");
					}
				}
			} 
		} 
		options.setDisplayFields(displayFields);
		return this.getMetadata(metadataCollectionName, options);
	}
	
	
	/**
	 * Method getCacheDocumentList - This method will get the delta document list from live metadata collection
	 * this method makes following calls to options object
	 * options.getContentType()
	 * options.getDocid()
	 * [ options.getSubscription() ] - optional 
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getCacheMetadata(Options options) throws MongoUtilsException, OptionsException{
		DBObject displayFields = options.getDisplayFields();
		if(displayFields==null)
			displayFields= new BasicDBObject();
		
		String metadataCacheCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_CACHE_COLLECTION_NAME);
		String subColName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), SUBSCRIPTION_COLLECTION_NAME);

		String subscription = options.getSubscription();
		if(options.isIncludeDeletes()==false)
		if(subscription != null && !"".equals(subscription)){
			DBCursor hierarchyCursor =  options.getMongoUtils().readMeta(options.getContentType(), subColName, new BasicDBObject("_id",options.getSubscription()),new BasicDBObject("hierarchyExpansions",1).append("_id", 0));
			if(hierarchyCursor.hasNext()){
				DBObject hierarchyExpansionsTemp = hierarchyCursor.next();
				DBObject hierarchyExpansions = (DBObject)hierarchyExpansionsTemp.get("hierarchyExpansions");
				for(String key : hierarchyExpansions.keySet()){
					if(subscription == null || "".equals(subscription)){
						displayFields.put(key, 0);
						continue;
					}

					String value = hierarchyExpansions.get(key).toString();
					if(value == null || "".equals(value)){
						continue;
					}

					Boolean val = Boolean.parseBoolean(value);
					if(!val  || subscription == null || "".equals(subscription)){
						displayFields.put(key, 0);
					}
					if(key.equalsIgnoreCase("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list") && val && displayFields.containsField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers")){
						displayFields.removeField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers");
					}
				}
			} 
		} 
		options.setDisplayFields(displayFields);
		return this.getMetadata(metadataCacheCollectionName, options);
	}

	/**
	 * Method getHistoryMetadata - This method will get the document from History collection
	 * this method makes following calls to options object
	 * Options options
	 * [ options.getSubscription() ] - optional 
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getHistoryMetadata(Options options) throws MongoUtilsException, OptionsException{
		if(options.getDocid() == null || options.getLastModified() ==0l ){
			throw new OptionsException("Last modified and Docid is a mandatory parameter.");
		}
		DBObject displayFields = options.getDisplayFields();
		if(displayFields==null)
			displayFields= new BasicDBObject();
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_HISTORY_COLLECTION_NAME);
		String subColName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), SUBSCRIPTION_COLLECTION_NAME);

		String subscription = options.getSubscription();
		if(subscription !=null && !("".equals(subscription))){
			DBCursor hierarchyCursor =  options.getMongoUtils().readMeta(options.getContentType(), subColName, new BasicDBObject("_id",options.getSubscription()),new BasicDBObject("hierarchyExpansions",1).append("_id", 0));
			if(hierarchyCursor.hasNext()){
				DBObject hierarchyExpansionsTemp = hierarchyCursor.next();
				DBObject hierarchyExpansions = (DBObject)hierarchyExpansionsTemp.get("hierarchyExpansions");
				for(String key : hierarchyExpansions.keySet()){
					//SMO Change : CR-10349 
					if("soar-software-feed.collection.company_info".equalsIgnoreCase(key))
					{
						continue;
					}
					//Change ends
					
					String value = hierarchyExpansions.get(key).toString();
					if(value == null || "".equals(value)){
						continue;
					}
					
					Boolean val = Boolean.parseBoolean(value);
					if(!val){
						displayFields.put(key, 0);
					}
					if(key.equalsIgnoreCase("soar-software-feed.collection.software-items.software-item.products-supported.product_number_list") && val && displayFields.containsField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers")){
						displayFields.removeField("soar-software-feed.collection.software-items.software-item.products-supported.product_numbers");
					}
				}
			}
			options.setDisplayFields(displayFields);
		}
		DBObject query = options.getQuery();
		if(query==null){
			options.setQuery(new BasicDBObject("lastModified",options.getLastModified()).append("documentId", options.getDocid()));
		}
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), metadataCollectionName, options.getQuery(),displayFields);
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		if(cursor.hasNext()){
			BasicDBObject record = (BasicDBObject) cursor.next();
			if(record != null)
				return record;
		}
		cursor.close();
		return null;
	}
	/**
	 * this method makes following calls to options object
	 * options.getContentType()
	 * options.getDocid()
	 * [ options.getSubscription() ] - optional 
	 *
	 * @return DBObject
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	private DBObject getMetadata(String collectionName, Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(options.getDocid())){
			throw new OptionsException("Docid is a mandatory parameter to search for attachment. Please provide a valid document id.");
		}

		if(isNull(collectionName)){
			throw new OptionsException("Collection Name not defined. Please provide a valid collection name to retrieve metadata");
		}

		QueryBuilder qBuilder = QueryBuilder.start();

		qBuilder.put("_id").is(options.getDocid());

		DBObject displayFields = null;
		if(options.getDisplayFields() != null){
			displayFields = options.getDisplayFields();
		}


		if(!isNull(options.getSubscription())){
			//add subscription filter
			ArrayList<String> subscriptions = new ArrayList<String>();
			subscriptions.add(options.getSubscription());
			qBuilder.and("subscriptions").in(subscriptions);
		}

		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), collectionName, qBuilder.get(),displayFields);
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		if(cursor.hasNext()){
			BasicDBObject record = (BasicDBObject) cursor.next();
			if(record != null)
				return record;
		}
		cursor.close();
		return null;
	}

	/**
	 * @param collectionName
	 * @param options
	 * @return
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public ArrayList<DBObject> getAllMetadata(String collectionName, Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(collectionName)){
			throw new OptionsException("Collection Name not defined. Please provide a valid collection name to retrieve metadata");
		}

		ArrayList<DBObject> returnList = new ArrayList<DBObject>();
		//System.out.println(options.getContentType());
		//System.out.println(collectionName);
		//System.out.println(options.getQuery());
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), collectionName,options.getQuery());
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		//System.out.println("cursor size "+cursor.size());
		while(cursor.hasNext()){
			returnList.add(cursor.next());
		}
		cursor.close();
		return returnList;
	}

	public ArrayList<DBObject> getAllMetadataFromTemp( Options options) throws MongoUtilsException, OptionsException{
		String metadataTempCollection = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		return this.getAllMetadata(metadataTempCollection, options);
	}

	/**
	 * Method getAttachment.
	 * @return GridFSDBFile
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public List<GridFSDBFile> getAttachment(Options options) throws MongoUtilsException, OptionsException{

		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(options.getDocid())){
			throw new OptionsException("Docid is a mandatory parameter to search for attachment. Please provide a valid document id.");
		}

		if(isNull(options.getAttachmentName())){
			throw new OptionsException("Docid is a mandatory parameter to search for attachment. Please provide a valid document id.");
		}

		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), CONTENT_BUCKET_NAME);
		if(isNull(contentBucketName)){
			throw new OptionsException("GridFS Bucket not defined in the mappings collection for the specified content type - "+options.getContentType());
		}

		return options.getMongoUtils().readContent(options.getContentType(), contentBucketName,options.getDocid(),options.getAttachmentName());
	}


	public List<GridFSDBFile> getFASTXMLFile(Options options) throws MongoUtilsException, OptionsException{

		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(options.getDocid())){
			throw new OptionsException("Docid is a mandatory parameter to search for attachment. Please provide a valid document id.");
		}

		return options.getMongoUtils().readFASTXMLFile(options.getContentType(), options.getDocid());
	}

	/**
	 * @param options
	 * @return
	 * @throws MongoUtilsException
	 * @throws OptionsException 
	 */
	public List<GridFSDBFile> getAllAttachments(Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");
		}

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(options.getDocid())){
			throw new OptionsException("Docid is a mandatory parameter to search for attachment. Please provide a valid document id.");
		}

		String contentBucketName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), CONTENT_BUCKET_NAME);
		if(isNull(contentBucketName)){
			throw new OptionsException("GridFS Bucket not defined in the mappings collection for the specified content type - "+options.getContentType());
		}

		return options.getMongoUtils().readContent(options.getContentType(), contentBucketName,options.getDocid(),null);
	}

	/**
	 * @param dbName
	 * @param dbCollectionName
	 * @param query
	 * @return
	 * @throws MongoUtilsException
	 */
	public Long getDocumentCount(Options options) throws MongoUtilsException{
		DBObject query = options.getQuery();
		if(query == null){
			query = new BasicDBObject();
		}
		return options.getMongoUtils().countMeta(options.getContentType(), options.getCollectionName(), query);
	}


	/**
	 * Method getAttachmentList.
	 * @return ArrayList<DBObject>
	 * @throws MongoUtilsException 
	 */
	public ArrayList<DBObject> getAttachmentList(Options options) throws MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new MongoUtilsException("Content Type not specified to get the document list");
		}

		if(isNull(options.getDocid())){
			throw new MongoUtilsException("Content Type not specified to get the document list");
		}
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), CONTENT_FILES_COLLECTION_NAME, new BasicDBObject(MEATDATA_DOCID_FIELD,options.getDocid()));
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		ArrayList<DBObject> results = new ArrayList<DBObject>();

		while(cursor.hasNext()){
			BasicDBObject record = (BasicDBObject) cursor.next();
			if(record != null)
				results.add(record);
		}
		cursor.close();
		return results;
	}

	/**
	 * 1. connect to processor db
	 * 
	 * 2. get hold of status collection in processor db
	 * 
	 * 3. for each content type in the status collection,
	 * 	  query the respective database and look for any delta
	 * 
	 * 4. if there is delta change to a specific content type,
	 * 	  create corresponding content Item entry into the HashMap
	 * 
	 * @return HashMap<String, Long>
	 * @throws MongoUtilsException
	 */
	public HashMap<String,Long> getLastProcessedTokens(Options options, String statusCollectionName) throws MongoUtilsException{

		return getLastProcessedTokens(options, statusCollectionName, new BasicDBObject());
	}
	
	
	/**
	 * 1. connect to processor db
	 * 
	 * 2. get hold of status collection in processor db
	 * 
	 * 3. for each content type in the status collection,
	 * 	  query the respective database and look for any delta
	 * 
	 * 4. if there is delta change to a specific content type,
	 * 	  create corresponding content Item entry into the HashMap
	 * 
	 * @return HashMap<String, Long>
	 * @throws MongoUtilsException
	 */
	public HashMap<String,Long> getLastProcessedTokens(Options options, String statusCollectionName, DBObject filter) throws MongoUtilsException{

		HashMap<String,Long> deltaMap = new HashMap<String, Long>();
		DBCursor cursor = options.getMongoUtils().readMeta(ConfigurationManager.CONFIG_DB_NAME, statusCollectionName, filter);
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		while(cursor.hasNext()){
			DBObject content_type = cursor.next();
			String dbName = content_type.get(ID_FIELD).toString();

			if(dbName.endsWith("content")){
				continue;
			}
			Long last_modified = (Long) content_type.get(LAST_MODIFIED_TS_FIELD);
			if(last_modified == null)
				deltaMap.put(dbName, 0L);
			else
				deltaMap.put(dbName, last_modified);
		}
		cursor.close();
		return deltaMap;
	}
	

	/**
	 * 1. connect to processor db
	 * 
	 * 2. get hold of status collection in processor db
	 * 
	 * 3. for each content type in the status collection,
	 * 	  query the respective database and look for any delta
	 * 
	 * 4. if there is delta change to a specific content type,
	 * 	  create corresponding content Item entry into the HashMap
	 * 
	 * @return HashMap<String, Long>
	 * @throws MongoUtilsException
	 */
	public HashMap<String,Long> getLastProcessedTokens(Options options) throws MongoUtilsException{
		return getLastProcessedTokens(options, STATUS_COLLECTION_NAME);
	}
	
	/**
	 * 1. connect to processor db
	 * 
	 * 2. get hold of status collection in processor db
	 * 
	 * 3. for each content type in the status collection,
	 * 	  query the respective database and look for any delta
	 * 
	 * 4. if there is delta change to a specific content type,
	 * 	  create corresponding content Item entry into the HashMap
	 * 
	 * @return HashMap<String, Long>
	 * @throws MongoUtilsException
	 */
	public HashMap<String,Long> getLastProcessedTokens(Options options, DBObject filter) throws MongoUtilsException{
		return getLastProcessedTokens(options, STATUS_COLLECTION_NAME, filter);
	}

	/**
	 * @param value
	 * @return
	 */
	private boolean isNull(String value) {
		if(value == null || "".equals(value))
			return true;
		return false;
	}

	/**
	 * @param options
	 * @throws MongoUtilsException
	 * @throws OptionsException 
	 */
	public void writeMetadataToTemp(Options options) throws MongoUtilsException, OptionsException {
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		this.writeMetadata(tempCollectionName,options);

	}

	/**
	 * @param options
	 * @throws MongoUtilsException
	 * @throws OptionsException 
	 */
	public void writeMetadataToLive(Options options) throws MongoUtilsException, OptionsException {
		String tempCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_LIVE_COLLECTION_NAME);
		this.writeMetadata(tempCollectionName,options);

	}
	/**
	 * @param options
	 * @throws MongoUtilsException
	 * @throws OptionsException 
	 */
	public void writeMetadataToHistory(Options options) throws MongoUtilsException, OptionsException {
		String historyCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_HISTORY_COLLECTION_NAME);
		this.writeMetadata(historyCollectionName,options);

	}

	/**
	 * @param collectionName
	 * @param options
	 * @throws MongoUtilsException
	 * @throws OptionsException 
	 */
	private void writeMetadata(String collectionName, Options options) throws MongoUtilsException, OptionsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified to get the document list");
		}
		if(isNull(collectionName)){
			throw new OptionsException("Collection Name not defined. Please provide a valid collection name to retrieve metadata");
		}
		if(isNull(options.getDocid())){
			throw new OptionsException("Document id not specified to get the document list");
		}
		if(options.getMetadataDocument() == null){
			throw new OptionsException("Metadata Document is not set. Please call setMetadataDocument(DBObject document) available in options object before calling this method");
		}

		options.getMongoUtils().writeMeta(options.getContentType(),collectionName, new BasicDBObject("_id",options.getDocid()), options.getMetadataDocument(),true,false);
	}

	public void insertAllMetadata(Options options, ArrayList<DBObject> records) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified to get the document list");
		}
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		if(isNull(collectionName)){
			throw new OptionsException("Collection Name not defined. Please provide a valid collection");
		}

		options.getMongoUtils().insertMeta(options.getContentType(),collectionName,records);

	}

	public void historyCleanup(Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified to get the document list");
		}
		if(isNull(options.getDocid())){
			throw new OptionsException("Document id not specified to get the document list");
		}

		// check the number of versions for a given document
		// needs dbName, Collection name defined
		String historyCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_HISTORY_COLLECTION_NAME); 
		options.setCollectionName(historyCollectionName);

		String limit = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), "HISTORY_VERSION_LIMIT");
		//CR257 - start
		//changed the history_version _limit from Long to int data type.
		int history_version_limit = 5;
		if(!isNull(limit)){
			history_version_limit = Integer.parseInt(limit);
		}
		long count = this.getDocumentCount(options);

		if (count > history_version_limit){
			DBObject query = new BasicDBObject("documentId",options.getDocid());
			DBObject sort = new BasicDBObject("_id",0);
			options.getMongoUtils().deleteMeta(options.getContentType(), historyCollectionName, query, sort, history_version_limit);
		}
		// CR257 - End
		// if its more than 5
		// remove the oldest version 
	}


	/**
	 * @param options
	 * @param lastModified
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateStatusCollection(Options options , Long lastModified) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}
		BasicDBObject record = new BasicDBObject("$set",new BasicDBObject(LAST_MODIFIED_TS_FIELD,lastModified));

		options.getMongoUtils().writeMeta(ConfigurationManager.CONFIG_DB_NAME, STATUS_COLLECTION_NAME,  new BasicDBObject("_id",options.getContentType()), record,true,false);
	}

	/**
	 * @param options
	 * @param lastModified
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateStatusCollection(Options options , Long lastModified, String statusCollectionName) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}
		BasicDBObject record = new BasicDBObject("$set",new BasicDBObject(LAST_MODIFIED_TS_FIELD,lastModified));

		options.getMongoUtils().writeMeta(ConfigurationManager.CONFIG_DB_NAME, statusCollectionName,  new BasicDBObject("_id",options.getContentType()), record,true,false);
	}

	
	public void removeInstanceNameFromStatus(String contentType) throws MongoUtilsException{
		
		DBObject dbObject = new BasicDBObject("$unset",new BasicDBObject("instance_name",1));
		Options.getMongoUtils(contentType).writeMeta(ConfigurationManager.CONFIG_DB_NAME, STATUS_COLLECTION_NAME,  new BasicDBObject("_id",contentType), dbObject,true,false);
	
	}
	
	public void addInstanceNameInStatus(String contentType,String instanceName) throws MongoUtilsException{
		
		DBObject dbObject = new BasicDBObject("$set", new BasicDBObject("instance_name", instanceName));
		Options.getMongoUtils(contentType).writeMeta(ConfigurationManager.CONFIG_DB_NAME, STATUS_COLLECTION_NAME,  new BasicDBObject("_id",contentType), dbObject,true,false);
	
	}
	

	public void updateStatusInProcessorRegistry(String instanceName,String status) throws MongoUtilsException{
		
		DBObject dbObject = new BasicDBObject("$set", new BasicDBObject("status", status));
		Options.getMongoUtils(PROCESSOR_REGISTRY_COLLECTION_NAME).writeMeta(ConfigurationManager.CONFIG_DB_NAME, PROCESSOR_REGISTRY_COLLECTION_NAME,  new BasicDBObject("_id",instanceName), dbObject,true,false);
	
	}
	
	/**
	 * @param collectionName TODO
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateMetadata(String collectionName, Options options) throws OptionsException, MongoUtilsException {
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}
		if(isNull(options.getDocid())){
			throw new OptionsException("Document id not specified ");
		}
		if(options.getMetadataDocument() == null){
			throw new OptionsException("Metadata Document is not set. Please call setMetadataDocument(DBObject document) available in options object before calling this method");
		}

		if(isNull(collectionName)){
			throw new OptionsException("Collection name is null/blank");
		}

		if(options.getQuery() == null){
			throw new OptionsException("Update cannot be performed on a database without query filter. Please set a valid query filter using options.setQuery() method");
		}

		options.getMongoUtils().writeMeta(options.getContentType(), collectionName, options.getQuery(), options.getMetadataDocument(), false, false);

	}

	/**
	 * @param collectionName TODO
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateAllMetadata(String collectionName, Options options) throws OptionsException, MongoUtilsException {
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}

		if(options.getMetadataDocument() == null){
			throw new OptionsException("Metadata Document is not set. Please call setMetadataDocument(DBObject document) available in options object before calling this method");
		}

		if(isNull(collectionName)){
			throw new OptionsException("Collection name is null/blank");
		}

		if(options.getQuery() == null){
			throw new OptionsException("Update cannot be performed on a database without query filter. Please set a valid query filter using options.setQuery() method");
		}

		options.getMongoUtils().writeMeta(options.getContentType(), collectionName, options.getQuery(), options.getMetadataDocument(), false, true);

	}

	/**
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateMetadataToLive(Options options) throws OptionsException, MongoUtilsException {
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_LIVE_COLLECTION_NAME);
		this.updateMetadata(collectionName, options);
	}

	/**
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateMetadataToTemp(Options options) throws OptionsException, MongoUtilsException {
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		this.updateMetadata(collectionName, options);
	}

	/**
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateAllMetadataInLive(Options options) throws OptionsException, MongoUtilsException {
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_LIVE_COLLECTION_NAME);
		this.updateAllMetadata(collectionName, options);
	}

	/**
	 * @param options
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public void updateAllMetadataInTemp(Options options) throws OptionsException, MongoUtilsException {
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		this.updateAllMetadata(collectionName, options);
	}

	public void hasAttachments(Options options) throws OptionsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}
		if(isNull(options.getDocid())){
			throw new OptionsException("Document id not specified ");
		}
	}

	public void deleteMetadataFromTemp(Options options) throws OptionsException, MongoUtilsException{
		String collectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_TEMP_COLLECTION_NAME);
		this.deleteMetadata(collectionName, options);
	}

	private void deleteMetadata(String collectionName, Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}
		if(isNull(options.getDocid())){
			throw new OptionsException("Document id not specified ");
		}
		if(isNull(collectionName)){
			throw new OptionsException("Collection Name not specified ");
		}
		options.getMongoUtils().deleteMeta(options.getContentType(), collectionName, options.getDocid());
	}


	public ArrayList<DBObject> getMetadataRevisionList(Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType())){
			throw new OptionsException("Content Type not specified ");
		}

		if(isNull(options.getDocid())){
			throw new OptionsException("Docid is null/blank");
		}

		DBObject displayFields = options.getDisplayFields();

		if(displayFields == null){
			displayFields = new BasicDBObject();
			displayFields.put("documentId", 1);
			displayFields.put("status", 1);
			displayFields.put("eventType", 1);
			displayFields.put("priority", 1);
			displayFields.put("lastModified", 1);
			displayFields.put("hasAttachments", 1);
		}
		String historyCollectionName = ConfigurationManager.getInstance().getMappingValue(options.getContentType(), METADATA_HISTORY_COLLECTION_NAME);
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(), historyCollectionName , new BasicDBObject("documentId",options.getDocid()), displayFields);
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		ArrayList<DBObject> resultList = new ArrayList<DBObject>();
		cursor.sort(new BasicDBObject("lastModified",-1));
		while(cursor.hasNext()){
			resultList.add(cursor.next());
		}
		return resultList;
	}
	

	public String getGroomName(String contentType, String subscriptionName) throws MongoUtilsException{
		DBCursor cursor = Options.getMongoUtils(contentType).readMeta(ConfigurationManager.CONFIG_DB_NAME, GROOM_MAPPINGS_COLLECTION_NAME, QueryBuilder.start("contentType").is(contentType).and("subscription").is(subscriptionName).get());
		DBObject dbObject = null;
		if(cursor!=null && cursor.hasNext()){
			dbObject = cursor.next();
		}
		
		if(dbObject!=null && dbObject.get("groomName")!=null)
			return dbObject.get("groomName").toString();
		else
			return null;
	}
	
	public DBCursor getAllGrooms(String contentType) throws MongoUtilsException{
		
		return Options.getMongoUtils(contentType).readMeta(contentType, GROOMS_COLLECTION_NAME);	
		
	}
	
	public DBCursor getAllSubscriptions(String contentType) throws MongoUtilsException{
		
		return Options.getMongoUtils(contentType).readMeta(contentType, SUBSCRIPTIONS_FIELD);	
		
	}
	
	public void registerProcessorInstance(DBObject dbObject) throws MongoUtilsException {
		DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		DBCollection registry = configdb.getCollection(PROCESSOR_REGISTRY_COLLECTION_NAME);
		registry.save(dbObject);
	}
	
	public DBCursor getProcessorRegistry(DBObject dbObject) throws MongoUtilsException {
		DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		DBCollection registry = configdb.getCollection(PROCESSOR_REGISTRY_COLLECTION_NAME);
		return registry.find(dbObject);
	}
	
	public boolean isProcessorInstanceExists(String instanceName) throws MongoUtilsException {
		DBCursor cursor=getProcessorRegistry(new BasicDBObject("_id",instanceName));
		if(cursor!=null && cursor.hasNext()&& cursor.next().get("_id").equals(instanceName))
			return true;
		else
			return false;
	}
	/**
	 * Method to retrieve metadata with displayfield options
	 * @param options
	 * @return
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 */
	public ArrayList<DBObject> getAllMetadata(Options options) throws OptionsException, MongoUtilsException{
		if(isNull(options.getContentType()))
			throw new OptionsException("Content Type not defined. Please provide valid content type as defined in the mappings collection");

		if(!ConfigurationManager.getInstance().keySet().contains(options.getContentType())){
			throw new OptionsException("Content Type invalid. Please provide valid content type as defined in the mappings collection");
		}

		if(isNull(options.getContentType())){
			throw new OptionsException("Collection Name not defined. Please provide a valid collection name to retrieve metadata");
		}

		ArrayList<DBObject> returnList = new ArrayList<DBObject>();
		DBCursor cursor = options.getMongoUtils().readMeta(options.getContentType(),options.getCollectionName(),options.getQuery(), options.getDisplayFields());
		if(options.getSortFields() != null && cursor != null){
			cursor.sort(options.getSortFields());
		}
		//System.out.println("cursor size "+cursor.size());
		while(cursor.hasNext()){
			returnList.add(cursor.next());
		}
		cursor.close();
		return returnList;
	}	
	public static void main(String[] args) throws MongoUtilsException, OptionsException {

		System.setProperty("mongo.configuration", "config/mongo.properties");

		ContentDAO dao= new ContentDAO();
		Options options= new Options();

		options.setContentType("generalpurpose");
		options.setSubscription("astro2");
		options.setIncludeDeletes(true);
		//options.setLimit(100);
		DBCursor cursor = dao.getLiveDocumentList(options);
		System.out.println(cursor.size());
		for(DBObject obj : cursor){
			System.out.println(obj);
		}
	}
}
