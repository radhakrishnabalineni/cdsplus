package com.hp.cdsplus.web.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.cdsplus.web.util.MimeTypes;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.sun.jersey.api.view.Viewable;
import javax.ws.rs.WebApplicationException;
/**
 * @author jaiswaln
 *
 */
@Path("/processor")
public class ProcessorResource {

	public final static String REGISTRY_COLLECTION_NAME = "registry";
	public final static String STATUS_COLLECTION_NAME = "status";
	public final static String TRANSACTIONS_COLLECTION_NAME = "transactions";
	public final static long MILLISECONDS_IN_1_HOUR = 5*60*1000;
	
	ContentDAO contentDAO = new ContentDAO();
	
	@GET
	@Produces({MediaType.TEXT_HTML})
	public Response checkApplicationStatus(){
		CacheControl defaultCache = new CacheControl();
		defaultCache.setMaxAge(120);
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<ProcessorInstance> processorInstanceList = new ArrayList<ProcessorInstance>();
		List<ContentClassMapping> assignedCCMappingList = new ArrayList<ContentClassMapping>();
		List<ContentClassMapping> intermediateCCMappingList = new ArrayList<ContentClassMapping>();
		List<ContentClassMapping> assigningCCMappingList = new ArrayList<ContentClassMapping>();
		List<String> unassignedContentClasses = new ArrayList<String>();
		ProcessorInstance processorInstance = null;
		try {
			DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
			DBCollection registryCollection = null;
			DBCollection statusCollection = null;
			if(configdb!=null){
				registryCollection = configdb.getCollection(REGISTRY_COLLECTION_NAME);
				DBCursor processorRegistryCursor = registryCollection.find();
				while(processorRegistryCursor.hasNext()){
					DBObject record = processorRegistryCursor.next();
					processorInstance = new ProcessorInstance();
					processorInstance.set_id(record.get("_id").toString());
					processorInstance.setServername(record.get("servername").toString());
					processorInstance.setInstallationlocation(record.get("installationlocation").toString());		
					processorInstance.setIsrunning(isProcessorInstanceRunning(record.get("_id").toString()).toString());
					processorInstanceList.add(processorInstance);
				}	
				
				statusCollection = configdb.getCollection(STATUS_COLLECTION_NAME);
				
				QueryBuilder builder = QueryBuilder.start();
				builder.put("assigned").is("true");
				DBObject subQuery1 = builder.get();
				
				builder = QueryBuilder.start();
				builder.put("instance_name");
				builder.exists(true);
				DBObject subQuery2 = builder.get();
				
				builder = QueryBuilder.start();
				builder.and(subQuery1,subQuery2);				
				
				DBCursor assignedClassesCursor = statusCollection.find(builder.get());
				
				while(assignedClassesCursor.hasNext()){
					ContentClassMapping assignedCCMapping = new ContentClassMapping();
					DBObject record = assignedClassesCursor.next();
					assignedCCMapping.setContentClassName(record.get("_id").toString());
					assignedCCMapping.setProcessorInstanceName(record.get("instance_name").toString());
					assignedCCMappingList.add(assignedCCMapping);
				}				
				
				builder = QueryBuilder.start();
				builder.put("assigned");
				builder.exists(false);
				subQuery1 = builder.get();
				
				builder = QueryBuilder.start();
				builder.put("instance_name");
				builder.exists(false);
				subQuery2 = builder.get();
				
				builder = QueryBuilder.start();
				builder.and(subQuery1,subQuery2);
				
				DBCursor unassignedClassesCursor = statusCollection.find(builder.get());
				while(unassignedClassesCursor.hasNext()){
					unassignedContentClasses.add(unassignedClassesCursor.next().get("_id").toString());
				}
				
				builder = QueryBuilder.start();
				builder.put("assigned").is("true");
				subQuery1 = builder.get();
				
				builder = QueryBuilder.start();
				builder.put("instance_name");
				builder.exists(false);
				subQuery2 = builder.get();
				
				builder = QueryBuilder.start();
				builder.and(subQuery1,subQuery2);	
				
				DBCursor intermediateClassesCursor = statusCollection.find(builder.get());
				
				while(intermediateClassesCursor.hasNext()){
					ContentClassMapping intermediateCCMapping = new ContentClassMapping();
					DBObject record = intermediateClassesCursor.next();
					intermediateCCMapping.setContentClassName(record.get("_id").toString());
					//intermediateCCMapping.setProcessorInstanceName(record.get("instance_name").toString());
					intermediateCCMappingList.add(intermediateCCMapping);
				}		
				
				builder = QueryBuilder.start();
				builder.put("assigned");
				builder.exists(false);
				subQuery1 = builder.get();
				
				builder = QueryBuilder.start();
				builder.put("instance_name");
				builder.exists(true);
				subQuery2 = builder.get();
				
				builder = QueryBuilder.start();
				builder.and(subQuery1,subQuery2);
				
				DBCursor assignmentInProgressClassesCursor = statusCollection.find(builder.get());
				
				while(assignmentInProgressClassesCursor.hasNext()){
					ContentClassMapping assigningCCMapping = new ContentClassMapping();
					DBObject record = assignmentInProgressClassesCursor.next();
					assigningCCMapping.setContentClassName(record.get("_id").toString());
					assigningCCMapping.setProcessorInstanceName(record.get("instance_name").toString());
					assigningCCMappingList.add(assigningCCMapping);
				}		
			}			
			
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, 500);
		} 
		map.put("processors", processorInstanceList);
		map.put("assignedclasses", assignedCCMappingList);
		map.put("unassignedclasses", unassignedContentClasses);
		map.put("intermediateclasses", intermediateCCMappingList);
		map.put("assigningprogressclasses", assigningCCMappingList);
		return Response.ok(new Viewable("/processor-ui",map)).build();
	}	
	
//	@GET
//	@Path("/processorInstances")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getProcessorInstances(){
//		JSONArray jsonArray = new JSONArray();
//		JSONObject jsonObject = null;
//		try {
//			DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
//			DBCollection registryCollection = null;
//			if(configdb!=null){
//				registryCollection = configdb.getCollection(REGISTRY_COLLECTION_NAME);
//				DBCursor processorRegistryCursor = registryCollection.find();
//				while(processorRegistryCursor.hasNext()){
//					DBObject record = processorRegistryCursor.next();
//					jsonObject = new JSONObject();
//					jsonObject.put("_id", record.get("_id").toString());
//					jsonObject.put("servername", record.get("servername").toString());
//					jsonObject.put("installationlocation", record.get("installationlocation"));		
//					jsonObject.put("isrunning",isProcessorInstanceRunning(record.get("_id").toString()).toString());
//					jsonArray.put(jsonObject);
//				}				
//			}
//		} catch (MongoUtilsException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return Response.ok().entity(jsonArray).build();
//	}
	
	@GET
	@Path("/unassignContentClass")
	public Response unassignContentClass(@QueryParam("processorinstance") String processorInstance,@QueryParam("contentclass") String contentClass){
		try {
			contentDAO.removeInstanceNameFromStatus(contentClass);
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, 500);
		}
		String result = "ok";
		return Response.ok().entity(result).build();		
	}
	
	@GET
	@Path("/assignContentClass")
	public Response assignContentClass(@QueryParam("processorinstance") String processorInstance,@QueryParam("contentclass") String contentClass){
		try {
			contentDAO.addInstanceNameInStatus(contentClass, processorInstance);
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, 500);
		}
		String result = "ok";
		return Response.ok().entity(result).build();		
	}
	
	
	public Boolean isProcessorInstanceRunning(String processorInstanceName){
		Boolean isRunning = false;
		String status = "stopped";
		try {
			DB diagnosticDB = ConfigurationManager.getInstance().getMongoDBAuthenticated(DiagnosticLogger.DB_NAME);
			DBCollection transactionsCollection = null;
			if(diagnosticDB!=null){
				transactionsCollection = diagnosticDB.getCollection(TRANSACTIONS_COLLECTION_NAME);
				
				QueryBuilder builder = QueryBuilder.start();
				builder.put("_id").is(processorInstanceName);
				builder.and("component").is("processor");
				DBObject dbObject = transactionsCollection.findOne(builder.get());
				
				if(dbObject!=null){
					long currentTime = System.currentTimeMillis();
					long lastModified = Long.valueOf(dbObject.get("lastmodified").toString());
					if(currentTime-lastModified <= MILLISECONDS_IN_1_HOUR){
						isRunning = true;
						status = "running";
					}
				}
			}
			
			DB configdb = ConfigurationManager.getInstance().getMongoDBAuthenticated(ConfigurationManager.CONFIG_DB_NAME);
			DBCollection registryCollection = null;
			if(configdb!=null){
				registryCollection = configdb.getCollection(REGISTRY_COLLECTION_NAME);
				DBObject registryObject = registryCollection.findOne(new BasicDBObject("_id",processorInstanceName));
				if(registryObject.get("status")==null||!registryObject.get("status").toString().equalsIgnoreCase(status)){
					contentDAO.updateStatusInProcessorRegistry(processorInstanceName, status);
				}
			}
			
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, 500);
		} 
		return isRunning;
	}
}
