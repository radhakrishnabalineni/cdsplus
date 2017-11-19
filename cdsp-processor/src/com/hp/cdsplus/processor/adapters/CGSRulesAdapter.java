package com.hp.cdsplus.processor.adapters;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.JDOMException;

import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.CGSWorkItem;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.seeker.cg.bl.CGS;
import com.hp.seeker.cg.bl.sre.RuleException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/*
 * 1. get CGSRules from the gridfs bucket in 'cgs' database
 * 2. get CGS instance 
 * 3. for every support and manual document generate the CGS rules
 * 4. save it to 'cgs' database
 */

public class CGSRulesAdapter implements Adapter{
	
	private static final Logger logger = LogManager.getLogger(com.hp.seeker.cg.bl.CGS.class);
	private static final ThreadLocal<String> company_info = new ThreadLocal<String>();
	private static final String COMPANY_INFO = "company_info";
	public static final String CGS_CONTENT_TYPE_NAME = "cgs";
	public static final String CGS_GRIDFS_BUCKET_NAME = "content";
	public static final String CGS_RULESET_NAME = "cgs";
	public static final String SUPPORT_CONTENT_TYPE_NAME = "support";
	public static final String MANUAL_CONTENT_TYPE_NAME = "manual";

	public static final String CGS_FILE_NAME = "CGSRules";
	public static final String CGS_RULES_FILE_LOCATION = "config/CGSRules.xml";

	public static final String JAVA_PACKAGE_NAME_KEY = "javaPackageName";
	public static final String JAVA_CLASS_NAME_KEY = "javaClassName";
	public static final String FILE_NAME_KEY = "filename";
	public static final String IS_MODIFIED_KEY = "isModified";

	public static final String DB_NAME_KEY = "mongoDB";
	
	private boolean isRulesLoaded = false;
	
	private static CGSRulesAdapter adapter;
	ConversionUtils conversionUtils;
	ContentDAO contentDAO ;

	private CGSRulesAdapter(){
		//loadCGSRules();
		conversionUtils = new ConversionUtils();
		contentDAO = new ContentDAO();
	}
	
	public static synchronized CGSRulesAdapter getInstance() throws AdapterException{
		if(adapter == null){
			adapter = new CGSRulesAdapter();
			adapter.loadCGSRules();	
		}
		return adapter;
	}
	//SMO  Changes : methods added for setting and getting companyinfo in a threadlocal variable.
		private static void setCompany_info(String info)
		{
			company_info.set(info);
		}
		
		
		private static String getCompany_info()
		{
			return company_info.get();
		}

	/* (non-Javadoc)
	 * @see com.hp.cdsplus.processor.adapters.Adapter#evaluate(com.hp.cdsplus.processor.item.Item)
	 */
	@Override
	public void evaluate(Item item) throws ProcessException, OptionsException, MongoUtilsException {
		try {
			
			applyCGSRules(item);
		} catch (ConversionUtilsException e) {
			e.printStackTrace();
			throw new ProcessException(e);
		} catch (RuleException e) {
			e.printStackTrace();
			throw new ProcessException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProcessException(e);
		} catch (TransformerException e) {
			e.printStackTrace();
			throw new ProcessException(e);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new ProcessException(e);
		}
	}
	
	/**
	 * @param item
	 * @throws MongoUtilsException
	 * @throws AdapterException
	 */
	public void handleDelete(Item item) throws MongoUtilsException, AdapterException{
		
		String contentType = item.getContentType();
		if(!(SUPPORT_CONTENT_TYPE_NAME.equalsIgnoreCase(contentType) || MANUAL_CONTENT_TYPE_NAME.equalsIgnoreCase(contentType))){
			return;
		}
		
		if(contentType == null && "".equals(contentType)){
			throw new AdapterException("ContentType cannot be null/blank");
		}
		
		String id = null;
		if(item instanceof WorkItem ){
			id = ((WorkItem) item).getId();
		}else if(item instanceof CGSWorkItem){
			id = ((CGSWorkItem) item).getId();
		}else{
			return;
		}
		String cgsDBName = ConfigurationManager.getInstance().getMappingValue(CGSRulesAdapter.CGS_CONTENT_TYPE_NAME, CGSRulesAdapter.DB_NAME_KEY);
		DB cgsDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(cgsDBName);
		String metadataCollectionName = ConfigurationManager.getInstance().getMappingValue(CGSRulesAdapter.CGS_CONTENT_TYPE_NAME, ConfigurationManager.METADATA_LIVE_COLLECTION);
		
		DBCollection metadataCollection = cgsDB.getCollection(metadataCollectionName);
		metadataCollection.remove(new BasicDBObject("_id",id).append("contentType",contentType));
		return;
	}

	/**
	 * @param contentType
	 * @return
	 * @throws MongoUtilsException
	 * @throws AdapterException
	 */
	private String getBindingClassName(String contentType) throws MongoUtilsException, AdapterException{
		String bindingClassName = null;

		String packageName = ConfigurationManager.getInstance().getMappingValue(contentType, JAVA_PACKAGE_NAME_KEY);
		String className = ConfigurationManager.getInstance().getMappingValue(contentType, JAVA_CLASS_NAME_KEY);
		if(packageName == null || "".equals(packageName)){
			throw new AdapterException("JAXB Package Name undefined for the jaxb binding class");
		}
		if(className == null || "".equals(className)){
			throw new AdapterException("JAXB Class Name undefined for the jaxb binding class");
		}
		bindingClassName = packageName + "."+className;

		return bindingClassName;
	}

	/**
	 * @param contentType
	 * @param id
	 * @return
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 */
	private String getDocumentFromDB(String contentType, String id) throws MongoUtilsException, OptionsException{
		Options options = new Options();
		options.setContentType(contentType);
		options.setDocid(id);
		DBObject record = contentDAO.getStagedMetadata(options);
		DBObject document = (DBObject) record.get("document");
		//SMO Change 
		if (document != null) {
			String companyType = (String)document.get(COMPANY_INFO);
			if(StringUtils.isEmpty(companyType))
			{
				logger.debug("Company_info "+ companyType +" is an invalid value");
				setCompany_info("");
			}
			else
				setCompany_info(companyType);
		}
		DBObject dbo = new BasicDBObject("document", document);
		return dbo.toString();
	}

	/**
	 * @param item
	 * @throws MongoUtilsException
	 * @throws ConversionUtilsException
	 * @throws OptionsException
	 * @throws RuleException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws AdapterException
	 * @throws JDOMException
	 */
	private void applyCGSRules(Item item) throws MongoUtilsException, ConversionUtilsException, OptionsException, RuleException, IOException, TransformerException, AdapterException, JDOMException {
		
		String contentType = null;
		String id = null;
		
		// check content type for 'support' or 'manual'
		
		if(item instanceof WorkItem){
			contentType = ((WorkItem)item).getContentType();
			id = ((WorkItem)item).getId();
		}else if(item instanceof WorkItem){
			contentType = ((CGSWorkItem)item).getContentType();
			id = ((CGSWorkItem)item).getId();
		}else{
			return;
		}
		

		String bindingClassName = null;

		if(contentType == null && "".equals(contentType)){
			throw new AdapterException("ContentType cannot be null/blank");
		}

		if(SUPPORT_CONTENT_TYPE_NAME.equalsIgnoreCase(contentType) || MANUAL_CONTENT_TYPE_NAME.equalsIgnoreCase(contentType) ){
			bindingClassName = getBindingClassName(contentType);
		}else{
			return;
		}
		String jsonString = getDocumentFromDB(contentType, id);
		
		String xml = conversionUtils.convertJSONtoXML(jsonString, bindingClassName);
		long start_time = System.currentTimeMillis();
		
		Map<String, String> cgsResult = CGS.getInstance().process(xml, CGS_RULESET_NAME);
		
		logger.debug(contentType+"/"+id+" rule eval time : "+(System.currentTimeMillis()-start_time));
		saveCGSDoc(contentType, id, cgsResult);
	}

	/**
	 * @param contentType
	 * @param id
	 * @param cgsResult
	 * @throws MongoUtilsException
	 */
	private void saveCGSDoc(String contentType, String id, Map<String, String> cgsResult) throws MongoUtilsException{
		DBObject update = new BasicDBObject();
		update.put("groups", new BasicDBObject("group",cgsResult.keySet().toArray(new String[0])));
		update.put("lastModified", System.currentTimeMillis());
		update.put("hasAttachments", "false");
		update.put ("status", "update");
		update.put ("eventType", "update");
		update.put("priority", 2);
		//SMO Change : Setting company info in processed document.
		update.put("company_info", getCompany_info());
		DBObject query = new BasicDBObject("_id",id);
		query.put("contentType", contentType);
		
		
		
		String dbName = ConfigurationManager.getInstance().getMappingValue(CGS_CONTENT_TYPE_NAME, DB_NAME_KEY);
		
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);
		String liveCollectionName = ConfigurationManager.getInstance().getMappingValue(CGS_CONTENT_TYPE_NAME, ConfigurationManager.METADATA_LIVE_COLLECTION);
		DBCollection collection = db.getCollection(liveCollectionName);
		
		collection.update(query, new BasicDBObject("$set",update),true,false);
		
	}
	
	/**
	 * @return
	 * @throws AdapterException
	 * @throws MongoUtilsException
	 * @throws IOException
	 */
	public boolean isRulesModified() throws AdapterException, MongoUtilsException, IOException{
		
		File cgsFile = new File(CGS_RULES_FILE_LOCATION);
		String dbName = ConfigurationManager.getInstance().getMappingValue(CGS_CONTENT_TYPE_NAME, DB_NAME_KEY);
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);

		GridFS content = new GridFS(db, CGS_GRIDFS_BUCKET_NAME);
		GridFSDBFile dbFile = content.findOne(new BasicDBObject(FILE_NAME_KEY,CGS_FILE_NAME));
		if(dbFile == null){
			throw new AdapterException("CGSRules File not available in Database");
		}
		
		
		Boolean isModified = Boolean.parseBoolean(dbFile.getMetaData().get(IS_MODIFIED_KEY).toString());
		logger.debug("Is CGS Rules modified returned : "+isModified);
		if(isModified){
			dbFile.writeTo(cgsFile);
			dbFile.setMetaData(new BasicDBObject(IS_MODIFIED_KEY, false));
			dbFile.save();
			loadCGSRules();
			return true;
		}else{
			return false;
		}
	}
	
	public void loadCGSRules() throws AdapterException{
		File cgsFile = new File(CGS_RULES_FILE_LOCATION);
		if(!(cgsFile.exists() && cgsFile.isFile())){
			downloadCGSRules();
		}
		try {
			CGS.getInstance().loadRules("cgs",cgsFile);
		} catch (RuleException e) {
			throw new AdapterException(e);
		}
		this.isRulesLoaded = true;
	}
	
	private void downloadCGSRules() throws  AdapterException{
		File cgsFile = new File(CGS_RULES_FILE_LOCATION);
		String dbName;
		try {
			dbName = ConfigurationManager.getInstance().getMappingValue(CGS_CONTENT_TYPE_NAME, DB_NAME_KEY);
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(dbName);

			GridFS content = new GridFS(db, CGS_GRIDFS_BUCKET_NAME);
			GridFSDBFile dbFile = content.findOne(new BasicDBObject(FILE_NAME_KEY,CGS_FILE_NAME));
			if(dbFile == null){
				throw new AdapterException("CGSRules File not available in Database");
			}			
			dbFile.writeTo(cgsFile);
		} catch (MongoUtilsException e) {
			throw new AdapterException(e);
		} catch (IOException e) {
			throw new AdapterException(e);
		}
		
	}

	public boolean isRulesLoaded() {
		return isRulesLoaded;
	}

	public static void uploadCGSRules() throws MongoUtilsException, AdapterException, IOException{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(CGS_CONTENT_TYPE_NAME);
		GridFS gridFS = new GridFS(db, CGS_GRIDFS_BUCKET_NAME);
		File file = new File(CGS_RULES_FILE_LOCATION);
		if(file != null && file.exists() && file.isFile()){
			GridFSInputFile gridFSFile;
			gridFSFile = gridFS.createFile(file);
			gridFSFile.setMetaData(new BasicDBObject("isModified", true).append("lastModified", System.currentTimeMillis()));
			gridFSFile.save();
		} else{
			throw new AdapterException("Cannot find the specified file"+CGS_RULES_FILE_LOCATION);
		}	
	}
	public static void main(String[] args){
		CGSRulesAdapter adapter;
		try {
			adapter = getInstance();
		
			System.setProperty("mongo.configuration", "config/mongo.properties");
		
			DBObject obj = new BasicDBObject();
			obj.put("_id", "c00750966");
			obj.put("priority", 3);	
			obj.put("eventType", "update");
			obj.put("lastModified" , 1386793107624L);
			QueueManager queMgr= new QueueManager();
			queMgr.push(new ContentItem("manual",0L, queMgr));
			WorkItem item = new WorkItem("manual", obj, queMgr);	
			adapter.evaluate(item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
