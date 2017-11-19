package com.hp.cdsplus.mongo.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * 
 *<br><br>
 * This class is a singleton class that maintains mongo connection and jaxb class mapper for a given content class.<br>
 * Please set system property <i>mongo.configuration</i> which should point to <i>mongo.properties</i> file.<br>
 * mongo.properties should have defined <b>mongoClientURI</b> key with value equivalent to MongoClientURI.<br>
 * 
 *<i><b>MongoClientURI format:</b></i><br>
   mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
 <br><br>

    ->mongodb:// is a required prefix to identify that this is a string in the standard connection format.<br>
    ->username:password@ are optional. If given, the driver will attempt to login to a database after connecting to a database server.<br>
    ->host1 is the only required part of the URI. It identifies a server address to connect to.<br>
    ->:portX is optional and defaults to :27017 if not provided.<br>
    ->/database is the name of the database to login to and thus is only relevant if the username:password@ syntax is used. If not specified the "admin" database will be used by default.<br>
    ->?options are connection options. Note that if database is absent there is still a / required between the last host and the ? introducing the options. Options are name=value pairs and the pairs are separated by "&". For backwards compatibility, ";" is accepted as a separator in addition to "&", but should be considered as deprecated.<br>

	<br>Initializing this class will connect to mongo database named <b>configDB</b> and its collection named <b>mappings</b> and loads the collection into JAXBMongoMapper pojo objects. 
	<br>This JAXBMongoMapper pojo objects are stored in HashMap and can be retrieved using getJaxbClassMapper() method.

 */


public final class ConfigurationManager{
	
	public static final String MONGODB_USERNAME = "userName";
	public static final String MONGODB_PASSWORD = "password";
	public static final String MONGO_DB_NAME = "mongoDBName";
	
	public static final String METADATA_TEMP_COLLECTION = "metadataTempCollection";
	public static final String METADATA_LIVE_COLLECTION = "metadataLiveCollection";
	public static final String METADATA_STAGING_COLLECTION = "metadataStagingCollection";
	public static final String METADATA_HISTORY_COLLECTION = "metadataHistoryCollection";
	public static final String METADATA_CACHE_COLLECTION = "metadataCacheCollection";
	
	public static final String CONTENT_COLLECTION = "contentCollection";
	public static final String HIERARCHY_COLLECTION = "hierarchyCollection";
		
	public static final String SUBSCRIPTION_COLLECTION = "subscriptionCollection";
	public static final String DOC_COUNT_COLLECTION = "docCountCollection";
	
	public static final String JAVA_CLASS_NAME = "javaClassName";
	public static final String JAVA_PACKAGE_NAME = "javaPackageName";
	
	public static final String CONTENT_BUCKET_NAME = "contentBucket";
	
	public static final String CONFIG_DB_NAME = "configDB";
	
	public static final String ID_FIELD = "_id";
	public static final String METADATA_DOCID_FIELD = "metadata.docid";
	public static final String METADATA_FILENAME_FIELD = "metadata.fileName";
	public static final String SUBSCRIPTIONS_LIST_KEY = "subscriptions";
	
	public static final String GRIDFS_FAST_BUCKET = "fastxml";
	public static final String GRIDFS_FAST_XML_FILE_NAME = "filename";
	public static final String GRIDFS_FAST_XML_CONTENT_TYPE = "contentType";
	
	private static ConfigurationManager instance = null;
	private MongoClient mongoConnection=null;
	private ConfigMapper configMappings;
	
	private ConfigurationManager() throws MongoUtilsException {
		initialize();
	}

	/**
	 * This method is singleton class instantiation method, to get the instance of the class.
	 * @return instance of  com.hp.cdsplus.mongo.config.ConfigurationManager
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static synchronized ConfigurationManager getInstance() throws MongoUtilsException{
		
			if (instance == null) {
				instance = new ConfigurationManager();
			}
			return instance;
	}


	/**
	 * @throws MongoUtilsException
	 */
	private void initialize() throws MongoUtilsException{
			
		 loadProperties();
		 System.out.println("loadProperties complete");
		 mongoConnect();
		 System.out.println("mongoConnect complete");
		 String type = System.getProperty("MAPPER_COLLECTION");
		 if(type == null || "".equals(type)){
			throw new MongoUtilsException("Mapping Collection name is a mandatory property. Set it in mongo.properties against MAPPER_COLLECTION property");
		 }
		 configMappings = new ConfigMapper(type);
		 System.out.println("initialization complete");
	}
	
	/**
	 * @throws MongoUtilsException
	 */
	private void loadProperties() throws MongoUtilsException{
		Properties properties = new Properties();
		String propertyFilePath=System.getProperty("mongo.configuration");
		if(propertyFilePath == null || "".equals(propertyFilePath)){
			throw new MongoUtilsException("mongo.configuration property not set.");
		}
		try {
			properties = System.getProperties();
			properties.load(new FileInputStream(propertyFilePath));
			System.setProperties(properties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MongoUtilsException("Cannot find mongo.configuration file", e);
		} catch (IOException e) {
			throw new MongoUtilsException("Invalid mongo.configuration file", e);
		}
	}

	/**
	 * @throws MongoUtilsException
	 */
	private void mongoConnect() throws MongoUtilsException{
		String mongoURI = System.getProperty("mongoClientURI");
		if(mongoURI == null || "".equals(mongoURI)){
			throw new MongoUtilsException("mongoClientURI property undefined");
		}
		System.out.println(mongoURI);
		try {
			mongoConnection= new MongoClient(new MongoClientURI(mongoURI));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new MongoUtilsException("Cannot Connect to the given Mongo URI : "+ mongoURI, e);
		}
	}
	
	/**
	 * This method is used to authenticate the mongodb <b>dbName</b>.<br>
	 * Authentication parameters(username, password) are taken from <b>configDB</b> collection name:<b>mappings</b> for the mongodb <b>dbName</b> <br>
	 * 
	 * This will by default get you values from loader mapping collection in configDB
	 * @param dbName
	 * @return com.mongodb.DB
	 * @throws MongoUtilsException 
	 */
	public DB getMongoDBAuthenticated(String dbName) throws MongoUtilsException{
		if(dbName == null || "".equals(dbName)){
			throw new MongoUtilsException("DB Name string cannot be Null/Blank. Provide a valid value");
		}
		if(!dbName.equals(CONFIG_DB_NAME) && !configMappings.keySet().contains(dbName)){
			throw new MongoUtilsException("Invalid DB name provided. Provide a valid value");
		}
		
		return mongoConnection.getDB(dbName);
	}

	/**
	 * This method is used to authenticate the mongodb <b>dbName</b>.<br>
	 * Authentication parameters(username, password) are taken from <b>configDB</b> collection name:<b>mappings</b> for the mongodb <b>dbName</b> <br>
	 * 
	 * This will by default get you values from loader mapping collection in configDB
	 * @param dbName
	 * @return com.mongodb.DB
	 * @throws MongoUtilsException 
	 */
	public DB getMongoDBAuthenticated(String dbName, String userName, String password) throws MongoUtilsException{
		if(dbName == null || "".equals(dbName)){
			throw new MongoUtilsException("DB Name string cannot be Null/Blank. Provide a valid value");
		}
		if(!configMappings.keySet().contains(dbName)){
			throw new MongoUtilsException("Invalid DB name provided. Provide a valid value");
		}
		
		DB db = mongoConnection.getDB(dbName);
		if(!db.authenticate(userName, password.toCharArray())){
			throw new MongoUtilsException("");
		}
		return db;
	}
	
	public Set<String> keySet() throws MongoUtilsException{
		return this.configMappings.keySet();
	}

	public String getMappingValue(String content_type, String key){
		if(content_type == null || key == null || "".equals(content_type) || "".equals(key))
			return null;
		try {
			return this.configMappings.getValue(content_type, key);
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isValidSubscription(String contentType, String subscription) throws MongoUtilsException{
		DBObject record = this.configMappings.getConfigCache().get(contentType);
		if(record!=null && record.containsField(SUBSCRIPTIONS_LIST_KEY)){
			TreeSet<String> subscriptions = null;
			if(record.get(SUBSCRIPTIONS_LIST_KEY)!=null)
				subscriptions = (TreeSet<String>) record.get(SUBSCRIPTIONS_LIST_KEY);
			if(subscriptions!=null)
				return subscriptions.contains(subscription);
		}
		return false;
		
	}
	
	public void printCacheEntry(String key) throws MongoUtilsException{
		configMappings.printEntry(key);
	}
	
	public void printCache() throws MongoUtilsException{
		configMappings.printCache();
	}
	
	public void loadMappings() throws MongoUtilsException{
		this.configMappings.loadMappings();
	}
	
	public static void main(String[] args){
		System.setProperty("mongo.configuration","config/mongo.properties");
		try {
			ConfigurationManager.getInstance().printCache();
		} catch (MongoUtilsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the configMappings
	 */
	public ConfigMapper getConfigMappings() {
		return configMappings;
	}

	/**
	 * @param configMappings the configMappings to set
	 */
	public void setConfigMappings(ConfigMapper configMappings) {
		this.configMappings = configMappings;
	}
}
