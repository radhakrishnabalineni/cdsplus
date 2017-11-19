package com.hp.cdsplus.mongo.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
/**
 * This class is a POJO class to bind mongo database collection.<br>
 * mongo database name:<b>configDB</b><br>
 * mongo database collection name:<b>mappings</b><br>
 *
 */
public class ConfigMapper implements Runnable{
	private String mappingCollectionName;
	private ConcurrentHashMap<String, DBObject> configCache = new ConcurrentHashMap<String, DBObject>();
	private boolean enableDocCount = false;
	private boolean enableCacheRefresh = true;
	private boolean enableSubscriptionLoad = true;
	private boolean enableSubscriptionLoadParam = false;
	private  final boolean smoEnabledFlag;
	/**
	 * @return the smoEnabledFlag
	 */
	public boolean isSmoEnabledFlag() {
	    return smoEnabledFlag;
	}


	Thread thread;
	private static HashMap<String, Integer> subscriptionCount = new HashMap<String, Integer>();
	private int refresh_interval=1;
	
	
	public ConfigMapper(String mappingCollectionName) {
		this.mappingCollectionName = mappingCollectionName;
		
		String cacheRefreshBool = System.getProperty("ENABLE_CACHE_REFRESH");
		System.out.println("ENABLE_CACHE_REFRESH : "+cacheRefreshBool);
		if(cacheRefreshBool != null && !"".equals(cacheRefreshBool)){
			enableCacheRefresh = Boolean.parseBoolean(cacheRefreshBool);
			System.out.println(" Inside cacheRefreshBool: check enableCacheRefresh--"+enableCacheRefresh );
		}
		String refreshInterval= System.getProperty("REFRESH_INTERVAL");
		System.out.println(" CACHE_REFRESH INTERVAL in minutes: "+refreshInterval);
		
		if(refreshInterval != null && !"".equals(refreshInterval)){
			System.out.println(" Inside refreshInterval is ->"+ refreshInterval);
			refresh_interval = Integer.parseInt(refreshInterval);
			System.out.println(" Inside refreshInterval parsed to ->"+ refresh_interval);
		}
		String is_smo_enabled = System.getProperty("ENABLE_SMO");
		System.out.println("IS_SMO_ENABLED is set to -> "+ is_smo_enabled);
		smoEnabledFlag = (StringUtils.isEmpty(is_smo_enabled)|| is_smo_enabled.equalsIgnoreCase("true")) ? true : false;
		 
		System.out.println("Configuration Data being loaded from collection - "+this.mappingCollectionName);
		if(this.enableCacheRefresh){
			thread = new Thread(this,"CacheRefreshThread");
			thread.setDaemon(true);
			thread.start();
		}
		
	}


	/**
	 * @param db
	 * @param mapping_name
	 * @throws MongoUtilsException
	 */

	public void loadMappings() throws MongoUtilsException{
		
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
			// if the collection name is valid then query the collection in configDB
			DBCollection collection = db.getCollection(this.mappingCollectionName);
			DBCursor results = collection.find();
			System.out.println("Total No of records loaded from mapping collection - "+results.count());
			// iterate through the records and evaluate them individually
			if(results.count() == 0){
				throw new MongoUtilsException("No Records found in Mapping Collection - "+this.mappingCollectionName);
			}
			while(results.hasNext()){
				
				DBObject result = results.next();
				String contentType = result.get("_id").toString();
				this.configCache.put(contentType, result);
			}
			
			String docCountBool = System.getProperty("ENABLE_DOC_COUNT");
			System.out.println("ENABLE_DOC_COUNT : "+docCountBool);
			
			if(docCountBool != null && !"".equals(docCountBool)){
				enableDocCount = Boolean.parseBoolean(docCountBool);
			}
			
			String loadSubscriptions =  System.getProperty("LOAD_SUBSCRIPTION_LIST");
			System.out.println("LOAD_SUBSCRIPTION_LIST : "+loadSubscriptions);
			if(loadSubscriptions != null && !"".equals(loadSubscriptions)){
				enableSubscriptionLoadParam = Boolean.parseBoolean(loadSubscriptions);
			}
			this.refreshMappings();
	}
	
	public void loadDocCount(String contentType) throws MongoUtilsException{
			DBObject record = this.configCache.get(contentType);
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(record.get("mongoDB").toString());
			String colName = ConfigurationManager.getInstance().getMappingValue(contentType, ConfigurationManager.DOC_COUNT_COLLECTION);
			if(colName == null || "".equals(colName.trim())){
				//System.out.println("Cannot load Document count since collection to be used for getting the count has not been defined for content type : "+contentType);
				return;
			}
			DBCollection collection = db.getCollection(colName);
			//System.out.println("contentType - "+contentType+", collection name - "+colName+" count - "+collection.count());
			if(record.get("docCountCollection").equals("content.files") && !contentType.equals("soar_ref_data"))
				record.put("count", collection.count(new BasicDBObject("metadata.isAttachment",false)));
			else
				record.put("count", collection.count());
			this.configCache.put(contentType, record);	
	}
	
	public void loadSubscriptionList(String contentType) throws MongoUtilsException{
			
			DBObject record = this.configCache.get(contentType);
			DB contentDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(record.get("mongoDB").toString());
			
			String subCollName = ConfigurationManager.getInstance().getMappingValue(contentType,ConfigurationManager.SUBSCRIPTION_COLLECTION);
			
			if(subCollName == null || "".equals(subCollName)){
				//System.out.println("Cannot load subscription list since collection to be used for getting the subscriptions has not been defined for content type : "+contentType);
				return;
			}
			DBCollection subCollection = contentDB.getCollection(subCollName);
			
			DBCursor cursor = subCollection.find();
			subscriptionCount.put(contentType, cursor.count());
			
			TreeSet<String> subscriptions = new TreeSet<String>();
			while(cursor.hasNext()){
				DBObject sub = cursor.next();
				String id = sub.get("_id").toString();
				/*if(id == null || "".equals(id))
					continue;*/ //commented as null check is not required for id 
				subscriptions.add(id);
			}
			record.put(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY, subscriptions);
			
			//System.out.println("Subscription Cache : "+subscriptionCache);
			this.configCache.put(contentType, record);
	}
	/**
	 * Method to check any changes in subscription list
	 * @param contentType
	 * @throws MongoUtilsException
	 */
	public void checkSubscriptionChange(String contentType) throws MongoUtilsException{
		DBObject record = this.configCache.get(contentType);
		//System.out.println("record:"+record);
		DB contentDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(record.get("mongoDB").toString());
		
		String subCollName = ConfigurationManager.getInstance().getMappingValue(contentType,ConfigurationManager.SUBSCRIPTION_COLLECTION);
		
		if(subCollName == null || "".equals(subCollName)){
			//System.out.println("Cannot load subscription list since collection to be used for getting the subscriptions has not been defined for content type : "+contentType);
			return;
		}
		DBCollection subCollection = contentDB.getCollection(subCollName);
		
		DBCursor cursor = subCollection.find();
		
		if(cursor.hasNext()){
			// If subscriptionCount in cache is not matching to DB/ subscriptionCount is empty (server re-start case)- set flag to reload the subscription
			if((subscriptionCount.get(contentType) !=null && cursor.count()!=subscriptionCount.get(contentType)) || subscriptionCount.get(contentType) ==null){
					this.enableSubscriptionLoad =true;
					System.out.println(" Change in subscriptions for .."+contentType);		
					return;
			}
		}
			this.enableSubscriptionLoad =false;
		
}
	public Set<String> keySet() throws MongoUtilsException{
		return this.getConfigCache().keySet();
	}
	
	
	/**
	 * @param mapper_type - any valid mapping collection in configDB
	 * @param key - any valid key in the corresponding mapper_type
	 * @return
	 * @throws MongoUtilsException 
	 */
	public String getValue(String content_type, String key) throws MongoUtilsException{
		this.getConfigCache();
		if (this.getConfigCache().containsKey(content_type) && this.getConfigCache().get(content_type).containsField(key)){
			return this.getConfigCache().get(content_type).get(key).toString();
		}
		return null;
	}
	
	public synchronized void refreshMappings() throws MongoUtilsException{
		if(this.configCache.size() == 0){
			this.loadMappings();
		}
		
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
		DBCollection collection = db.getCollection(this.mappingCollectionName);
		DBCursor cursor = collection.find();
		Set<String> contentTypeSet = new HashSet<String>();
		
		while(cursor.hasNext()){
			DBObject result = cursor.next();
			String contentType = result.get("_id").toString();
			if(this.getConfigCache().get(contentType)!=null && this.getConfigCache().get(contentType).get("count")!=null){
				result.put(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY, this.getConfigCache().get(contentType).get(ConfigurationManager.SUBSCRIPTIONS_LIST_KEY));
				result.put("count", this.getConfigCache().get(contentType).get("count"));
			}
			this.getConfigCache().put(contentType, result);
			
			if(enableSubscriptionLoadParam){
				checkSubscriptionChange(contentType);
				if(this.enableSubscriptionLoad){
					//System.out.println("Subscriptions list is getting refreshed");
					this.loadSubscriptionList(contentType);
				}
			}
			if(this.enableDocCount){
				this.loadDocCount(contentType);
			}
			
			contentTypeSet.add(contentType);
		}
		//System.out.println("Checking for sub change"+ subscriptionCount);
		this.handleObsoletes(contentTypeSet);
		contentTypeSet.clear();
	}
	
	private void handleObsoletes(Set<String> contentTypeSet){
		for(String contentType : this.configCache.keySet()){
			if(!contentTypeSet.contains(contentType)){
				this.configCache.remove(contentType);
			}
		}
	}

	@Override
	public void run() {
		
		do{
			{
				try {
						this.refreshMappings();
				} catch (MongoUtilsException e) {
					e.printStackTrace();
					break;
				}
				try {
					Thread.sleep(refresh_interval*60*1000);
				} catch (InterruptedException e) {
					
				}
			}
		}while(true);
		
	}

	
	public void printEntry(String key) throws MongoUtilsException {
		System.out.println(this.getConfigCache().get(key));
		
	}


	public void printCache() throws MongoUtilsException {
		for(DBObject record : this.getConfigCache().values()){
			System.out.println(record);
		}
		
	}
	
	public static void main(String[] args){
		System.setProperty("mongo.configuration", "config/mongo.properties");
		try {
			ConfigurationManager.getInstance().getConfigMappings().printCache();
			ConfigurationManager.getInstance().getConfigMappings().refreshMappings();
		} catch (MongoUtilsException e) {
			e.printStackTrace();
		} 
	}


	/**
	 * @return the configCache
	 * @throws MongoUtilsException 
	 */
	public ConcurrentHashMap<String, DBObject> getConfigCache() throws MongoUtilsException {
		if(this.configCache.size() == 0)
			this.loadMappings();
		return configCache;
	}
}
