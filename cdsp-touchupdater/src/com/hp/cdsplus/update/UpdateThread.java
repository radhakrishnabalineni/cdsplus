package com.hp.cdsplus.update;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hp.cdsplus.update.Exception.MongoDataException;
import com.hp.cdsplus.update.Exception.MongoQueryException;
import com.hp.cdsplus.update.Hierarchy.Hierarchy;
import com.hp.cdsplus.update.Hierarchy.MarketingHierarchy;
import com.hp.cdsplus.update.Hierarchy.SupportHierarchy;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * 
 * @author nidoh
 *
 */
public class UpdateThread implements Runnable{
	private Thread t;
	private static Logger logger = Logger.getLogger(UpdateThread.class);
	private UpdateManager upMan;
	private String oid;
	private String guid;
	private DBObject query = new BasicDBObject();
	private DBObject new_mongo_obj = new BasicDBObject();
	private DBObject curr_mongo_obj = new BasicDBObject();
	private Hierarchy hier_obj;
	private boolean paused = false;
	public static final String HIERARCHY_COLLECTION_NAME = "hierarchy";
	

	public UpdateThread(int id, UpdateManager up){
		
		upMan = up;
		t = new Thread(this, "[UpdateThread#"+id+"]");
		t.setDaemon(false);
		//logger.debug(t.getName() + " initialized and ready to go. Awaiting orders.");
		t.start();
	}
	
	
	@Override
	public void run() {
		logger.info("********Thread started up. Now getting to work!!!********");

			//first task - find GUIDs tagged against delta OIDs
			try{
				while( (oid = upMan.popOID()) != null ){ // while the queue has items in it / there is still work to do - take one
					logger.debug(oid + " is being processed for tagged GUIDs.");
					findDistinctGUIDs(); // this is not synchronized because updates are independent

					upMan.incrementOIDUpdates(); // for logging
				}
			} catch (MongoDataException e){
				logger.error(e.getMessage()); // defined in findDistinctOIDs()
				logger.error("Error not fatal- just progressing to the next OID.");
			} catch (MongoQueryException e){
				logger.error(e.getMessage()); // defined in findDistinctOIDs()
				logger.error("Error not fatal- just progressing to the next OID.");
			} catch (NumberFormatException e){
				logger.error(e.getMessage());
				logger.error("Error in parsing the oid integer for querying Mongo. Error not fatal- just progressing to the next OID.");
			} catch (Exception e){
				
			}
			
					
			//now first task is done, next task...is to wait
			try{
				synchronized(upMan){
					paused = true;
					upMan.wait(); // force the thread to stop until we are ready to keep going with the next part of the code. this ensures we are in manageable and expected states
				}
			} catch (InterruptedException e){
				//do nothing. once the thread is woken up, will pick up where it left off after the wait
				//this type of exception is expected to be thrown when it wakes up via notifyAll from upMan class
			} catch (IllegalMonitorStateException e) {
				logger.error(e.getMessage());
				logger.error("Found that the threads entered an invalid state. Exiting");
				System.exit(0); //can exit without explicitly stopping the threads because the 'daemon' is false. this means the threads will exit if the parent process exits.
			}
			
			//next task -- for each GUID in the queue needing an update, make an object appropriately and push it to mongo
			try{
				while( (guid = upMan.popGUID()) != null ){ // while the queue has items in it / there is still work to do
					makeTouches(); // this is not synchronized because updates are independent
					
					upMan.incrementGUIDUpdates(); // for logging
				}
			} catch (MongoException e){ // this will be thrown if the insert fails
				logger.error(e.getMessage());
				logger.error("Error in creating new mongo object for GUID: " + guid + " . Could not insert into " + upMan.getRichMediaColl() + " . Error not fatal- just progressing to the next OID.");
			} catch (MongoDataException e){
				logger.error(e.getMessage()); // defined in makeTouches()
				logger.error("Error not fatal- just progressing to the next OID.");
			} catch (MongoQueryException e){
				logger.error(e.getMessage()); // defined in makeTouches()
				logger.error("Error not fatal- just progressing to the next OID.");
			} catch (Exception e){
				
			}
			
			
			logger.info("********Thread exiting. Work is done!********");
			upMan.thisThreadDone(); // tell the queue manager you are done as a thread object
		
	}
	
	
	//////////////////////////////////////
	//getters for any other class needing to access these private members
	public Thread getThread(){
		return this.t;
	}
	
	
	public boolean getIsPaused(){
		return paused;
	}
	
	
	public Logger getLogger(){
		return logger;
	}
	
	
	public void setPaused(boolean b){
		paused = b;
	}
	//////////////////////////////////////
	
	
	//after taking a PM delta OID, find all MSC GUIDs tagged to that OID [distinct GUIDs]
	private void findDistinctGUIDs() throws MongoDataException, MongoQueryException, NumberFormatException{
		
		query.keySet().clear(); // make sure there is nothing left over from last search on this query object to ensure accurate results
		query.put("asset_rendition.products.product", Integer.parseInt(oid)); // need to convert the OID value here to integer- it is how it is stored in mongo for this collection
		DBCursor results = upMan.getRichMediaColl().find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		if( results == null ){
			throw new MongoQueryException("Exception on querying richmedia DB in Mongo- returned a null object on querying.");
		}
		
		if( results.count() > 0 ){ // if some GUIDs are tagged against that OID
			
			logger.debug("Found " + results.count() + " GUIDs tagged against this OID. Processing list to find distinct GUIDs not found yet.");
			
			while( results != null && results.hasNext() ){
				DBObject entry = results.next();
				String GUID = entry.get("_id").toString(); // _id stored in this collection as a string
				
				if(GUID != null && !GUID.equals("")){
					upMan.pushGUID(GUID); // place only the unique OIDs in the queue to ensure we don't touch the same object multiple times
				} else {
					throw new MongoDataException("Unexpected null value for schema property _id on processing " + entry.toString() + " in metadata_staging collection.");
				}
			} // end while loop
		} else {
			//no GUIDs tagged to this OID, utilize the hierarchy classes
			logger.info("No GUIDs found tagged against " + oid + ". Going to Hierarchy climb and tag.");
			
			query.keySet().clear();
			query.put("_id", oid);
			//SMO User story## 7471 Changes for constructing hierarchy collection name
			String hierarchy= upMan.getCompanyInfoValueMap().get(oid)+HIERARCHY_COLLECTION_NAME;
			if(StringUtils.isEmpty(hierarchy))
			{
				logger.debug("Hierarchy collection name  "+ hierarchy +" is invalid");
				return ;
			}
			//SMO User story## 7471 Change ends here
			DBObject hierarchy_oid = upMan.getPMDB().getCollection(hierarchy).findOne(query);
			
			if( hierarchy_oid == null ){
				throw new MongoQueryException("Exception querying hierarchy collection inside PM DB since " + oid + " has no GUIDs tagged against it.");
			}
			
			String node_type = hierarchy_oid.get("node_type").toString(); // "marketing" or "support" tree
			
			if( node_type == null  || node_type.equals("")){
				throw new MongoDataException("Node type of oid " + oid + " in the hierarchy collection is null or empty.");
			}
			
			if(node_type.equalsIgnoreCase("support")){
				logger.debug("Found a support tree OID " + oid + ". Going to climb and update in the support tree.");
				hier_obj = new SupportHierarchy(upMan, this);
				
			} else if (node_type.equalsIgnoreCase("marketing")){
				logger.debug("Found a marketing tree OID " + oid + ". Going to climb and update in the marketing tree.");
				hier_obj = new MarketingHierarchy(upMan, this);
				
			} else {
				throw new MongoDataException("Unexpected value for node_type of oid " + oid + " in hierarchy collection. Value is: " + node_type);
			}
			
			hier_obj.climb(hierarchy_oid); // see notes in hierarchy subclasses about why this is necessary
			
		}// end of else block
		
	}
	
	
	//this is the class which will actually create the new objects, and push those objects with the necessary property/metadata changes to the appropriate mongo collection for MSC/richmedia
	private void makeTouches() throws MongoException, MongoDataException, MongoQueryException{
		
		WriteResult errors;
		query.keySet().clear(); // make sure there is nothing left over from last search on this query object to ensure accurate results
		query.put("_id", guid); // need to convert the OID value here to integer- it is how it is stored in mongo for this collection

		curr_mongo_obj = upMan.getRichMediaColl().findOne(query); // will only be a single result for this as _id is the 'primary key' or unique identifier
		
		if(curr_mongo_obj == null){
			throw new MongoQueryException("Could not fetch object corresponding to GUID " + guid + " from " + upMan.getRichMediaColl());
		}
		
		new_mongo_obj = curr_mongo_obj; // set all of new object's properties to the current obj's
		
		/*
		if(new_mongo_obj.get("_id").toString() != null && curr_mongo_obj.get("_id").toString() != null){
			if(!new_mongo_obj.get("_id").toString().equalsIgnoreCase(curr_mongo_obj.get("_id").toString())){ // the GUIDs dont match up, something went wrong
				throw new MongoDataException("GUIDs between new mongo object and current mongo object do not match up. GUIDs are: ['new': " + new_mongo_obj.get("_id").toString() + "]  ['current': " + curr_mongo_obj.get("_id").toString() + "]" );
			}
		} else {
			throw new MongoDataException("GUID is null for the mongo object fetched. Current GUID was: " + guid);
		}*/
		
		//change individual properties by removing and then re-adding
		new_mongo_obj.removeField("eventType");
		new_mongo_obj.removeField("priority");
		new_mongo_obj.removeField("lastModified");
		
		new_mongo_obj.put("eventType", "touch");
		new_mongo_obj.put("priority", 4);
		new_mongo_obj.put("lastModified", System.currentTimeMillis());
		//have to update like this rather than using the update() with multi boolean param as true in the mongo API -- wildcat/spacedog event handling expect each event to have a unique timestamp
		
		//before inserting the new object into the collection, have to remove the old/current one as otherwise get a duplicate unique key error
		/*errors = upMan.getRichMediaColl().remove(query);
		
		if( errors.getLastError().getErrorMessage() != null ){
			//logger.error("Error while removing current object from metadata_staging collection: " + errors.getLastError().getErrorMessage());
			throw new MongoQueryException("Error while removing current object from metadata_staging collection: " + errors.getLastError().getErrorMessage());
		}
		
		errors = upMan.getRichMediaColl().insert(new_mongo_obj);*/
		
		
		//don't do deletes and inserts- use the below mechanism. it is safer:
		//return mongoapi.writeMeta(dbName, new BasicDBObject("_id", id), document, true, false);
		// the above calls [eventually] : return collection.update(findQuery, dbObject, upsert, multi);
		// first param - query criteria
		// second param - object containing changes to apply
		// third param - if existing, make those changes, otherwise create object with only those properties		
		errors = upMan.getRichMediaColl().update(query, new_mongo_obj, true, false);
		
		if( errors.getLastError().getErrorMessage() != null ){
			//logger.error("Error while inserting new object into metadata_staging collection: " + errors.getLastError().getErrorMessage());
			throw new MongoQueryException("Error while updating current object " + guid + " in metadata_staging collection: " + errors.getLastError().getErrorMessage());
		} else {
			logger.info("GUID " + guid + " updated.");
		}
		
	}
		
	

}
