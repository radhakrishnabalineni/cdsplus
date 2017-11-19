package com.hp.cdsplus.update.Hierarchy;

import java.util.ArrayList;

import com.hp.cdsplus.update.UpdateManager;
import com.hp.cdsplus.update.UpdateThread;
import com.hp.cdsplus.update.Exception.MongoDataException;
import com.hp.cdsplus.update.Exception.MongoQueryException;
import com.mongodb.DBObject;

/**
 * 
 * @author nidoh
 *
 */
public class MarketingHierarchy extends Hierarchy {
	private ArrayList<String> mktg_hier = new ArrayList<String>(6);
	
	//constructor will initialize the marketing tree  as well as call super() to ensure they get the right objects they need
	public MarketingHierarchy(UpdateManager up, UpdateThread upT){
		super(up, upT);
		
		mktg_hier.add("PRODUCT_NUMBER"); // index 0
		mktg_hier.add("PRODUCT_NAME");
		// when querying db.hierarchy.findOne({ "hierarchy_level": "SUPPORT_NAME" }), no results. so just use product and can add logic later to find support_name_oids
		//referring to the above, that doesn't mean there aren't any support_name_oids tagged anywhere. for example, the query db.hierarchy.findOne({}, {"_id":1,"SUPPORT_NAME_OID":1 }) has results
		mktg_hier.add("PRODUCT_SERIES");
		mktg_hier.add("PRODUCT_BIGSERIES");
		mktg_hier.add("MARKETING_SUBCATEGORY");
		mktg_hier.add("MARKETING_CATEGORY"); // index 5
	}
	
	@Override
	public void climb(DBObject obj) throws MongoDataException, MongoQueryException{
		//so the param is a PM hierarchy object which has 0 GUIDs tagged against its "_id" [OID] -- meaning it is a new OID intro'd into PM hierarchy
		//in this case, navigate the PM hierarchy up to category level, and at each level up, find the level's OID and then find all GUIDs tagged against it and place those GUIDs for touch updates [put in upMan's queue]
		//we do this because when the OID gets its hierarchy expanded in CDS+, needs to pick up the accurate hierarchy metadata for the expansion
		
		int matched_level_index = -1;
		String level = obj.get("hierarchy_level").toString(); // find what level this OID is introduced to
		String level_oid;
		
		if( level == null || level.equals("") ){
			throw new MongoDataException("The OID currently being processed, " + obj.get("_id").toString() + " has level " + level + " which is empty or null. Moving to next OID.");
		}
		
		t.getLogger().debug("Level of the new OID " + obj.get("_id").toString() + " is " + level);
		
		for(int i=0; i < mktg_hier.size(); i++){// taking the current level the hierarchy collection tells us this OID is at, go through the support array i made instantiating the hierarchy to traverse the tree
			if(level.equalsIgnoreCase(mktg_hier.get(i))){
				matched_level_index = i; // found what level we are on and therefore what levels we need to visit
				break; // no need to continue as only one level will match
			}
		}
		
		if( matched_level_index == -1){ // did not find a level match - throw exception because there should be a match found
			throw new MongoDataException("The OID currently being processed, " + obj.get("_id").toString() + " has level " + obj.get("hierarchy_level").toString() + " which was not found in the PM hierarchy and could not be matched. Progressing to the next OID for processing.");
		}
		
		//t.getLogger().debug("Found Matched level in the PM Hierarchy is " + supp_hier.get(matched_level_index));
		
		for(int i=matched_level_index; i < mktg_hier.size(); i++){ // so now, starting at the level we found above, go up the tree to category level and find OIDs
			if( mktg_hier.get(i).equalsIgnoreCase("PRODUCT_NAME") ){ // handle the SUPPORT_NAME OIDs too at this point
				level_oid = obj.get("SUPPORT_NAME_OID").toString();
				//have to handle SUPPORT_NAME_OIDs separately because there are SUPPORT_NAME_OIDs referenced in the hierarchy collection although it is never mentioned as a level in the hierarchy
				//all OIDs at the name level are referenced by the PRODUCT_NAME, not SUPPORT_NAME. so this mechanism ensures we don't miss these OIDs
				
				if( level_oid != null && !level_oid.equals("") ){
					t.getLogger().info("The SUPPORT_NAME_OID for " + level + " " + obj.get("_id").toString()  + " is " + level_oid);
					findGUIDs(level_oid); // call superclass's function to handle finding the GUIDs for this level's OID
				} else{
					throw new MongoDataException("Level OID for " + level + " and oid " + obj.get("_id").toString() + " is empty or nonexistant. Check the data. skipping this OID's processing.");
				}
			}//end of special case handling SUPPORT_NAME OIDs
			
			level_oid = obj.get(mktg_hier.get(i)+"_OID").toString(); // fetch the OID for this level so we can figure out what GUIDs are tagged to it
			
			if( level_oid != null && !level_oid.equals("") ){
				t.getLogger().info("The " + (mktg_hier.get(i)+"_OID").toString() + " for " + level + " " + obj.get("_id").toString()  + " is " + level_oid);
				findGUIDs(level_oid); // call superclass's function to handle finding the GUIDs for this level's OID
			} else{
				throw new MongoDataException("Level OID for " + level + " and oid " + obj.get("_id").toString() + " is empty or nonexistant. Check the data. skipping this OID's processing.");
			}
			
		} // end for loop	
		
		t.getLogger().info("********Done climbing marketing hierarchy for " + obj.get("_id").toString() + "********");
	}

}