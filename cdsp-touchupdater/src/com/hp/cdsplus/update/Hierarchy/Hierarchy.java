package com.hp.cdsplus.update.Hierarchy;

import com.hp.cdsplus.update.UpdateManager;
import com.hp.cdsplus.update.UpdateThread;
import com.hp.cdsplus.update.Exception.MongoDataException;
import com.hp.cdsplus.update.Exception.MongoQueryException;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * @author nidoh
 *
 */
public abstract class Hierarchy {
	//have upMan and query as private as opposed to protected because the subclasses don't require access to those members
	private UpdateManager upMan;
	protected UpdateThread t;
	private DBObject query = new BasicDBObject();
	
	public Hierarchy(UpdateManager up, UpdateThread upT){
		upMan = up;
		t = upT;
	}
	
	//climb() to be implemented in the subclasses because the level names are different for the 'trees'
	//	climbs each level up to category and finds OIDs as appropriate -- passes each to findGUIDs()
	public abstract void climb(DBObject obj) throws MongoDataException, MongoQueryException;
	
	//findGUIDs() can be implemented here because a level OID can be passed as a parameter - this process for finding GUIDs is the same regardless of which tree is involved
	public void findGUIDs(String levelOID) throws MongoQueryException, MongoDataException{		
		query.keySet().clear(); // clear out the query search criteria so we can ensure results are accurate
		query.put("asset_rendition.products.product", Integer.parseInt(levelOID)); // need to convert the OID value here to integer- it is how it is stored in mongo for this collection
		
		DBCursor results = upMan.getRichMediaColl().find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		if( results == null ){
			throw new MongoQueryException("Exception on querying richmedia DB in Mongo from hierarchy class- returned a null object on querying.");
		}
		
		if( results.count() > 0 ){ // if some GUIDs are tagged against that OID
			t.getLogger().info("Found " + results.count() + " GUIDs tagged against " + levelOID + " which was found during hierarchy climb of a new OID");
			while( results != null && results.hasNext() ){
				DBObject entry = results.next();
				String GUID = entry.get("_id").toString(); // _id stored in this collection as a string
				
				if(GUID != null && !GUID.equals("")){
					//when GUIDs are found, utilize the UpdateManager's queue for GUIDs: upMan.pushGUID(String GUID)
					upMan.pushGUID(GUID); // place only the unique OIDs in the queue to ensure we don't touch the same object multiple times
				} else {
					throw new MongoDataException("Unexpected null value for schema property _id on processing " + entry.toString() + " in metadata_staging collection from hierarchy class.");
				}
			} // end while loop
		}
	}
	
		
}
