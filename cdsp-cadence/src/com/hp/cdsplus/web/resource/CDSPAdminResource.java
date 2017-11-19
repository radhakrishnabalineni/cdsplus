package com.hp.cdsplus.web.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.hp.cdsplus.dao.ConfigDAO;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import javax.ws.rs.WebApplicationException;

/**
 * @author sunil
 * 
 */
@Path("/admin")
public class CDSPAdminResource {

	public List<String> getFilterKeys;
	public List<List<String>> getFilterValues;
	public List<String> getOperators;
	private Map<String, DBObject> subscriptionMap = new HashMap<String, DBObject>();

	@GET
	@Path("/operators")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOperatorList() throws JSONException {
		JSONObject operators = new JSONObject();
		operators.put("GREATER THAN", "$gt");
		operators.put("GREATER THAN EQUALS", "$gte");
		operators.put("LESS THAN", "$lt");
		operators.put("LESS THAN EQUALS", "$lte");
		operators.put("NOT EQUALS", "$ne");
		operators.put("EXISTS", "$exists");
		operators.put("TYPE", "$type");
		operators.put("IN", "$in");
		operators.put("NOT IN", "$nin");
		operators.put("ALL", "$all");
		operators.put("ELEM MATCH", "$elemMatch");
		operators.put("SIZE", "$size");
		operators.put("MOD", "$mod");
		operators.put("REGEX", "$regex");
		operators.put("WHERE", "$where");

		JSONObject logicalOperators = new JSONObject();

		logicalOperators.put("OR", "$or");
		logicalOperators.put("AND", "$and");
		logicalOperators.put("NOT", "$not");
		logicalOperators.put("NOR", "$nor");

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("operators", operators);
		jsonObj.put("logicalOperators", logicalOperators);
		return Response.ok().entity(jsonObj).build();
	}

	@GET
	@Path("/contentTypes")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getCollectionList() throws MongoUtilsException,
			JSONException {
		Set<String> contentTypes = ConfigurationManager.getInstance().keySet();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("contentTypes", new TreeSet<String>(contentTypes));
		return Response.ok().entity(jsonObj).build();
	}

	@GET
	@Path("/subscriptions")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSubscriptionList(
			@QueryParam("contentType") String contentType)
			throws MongoUtilsException, OptionsException, JSONException {
		ContentDAO contentDao = new ContentDAO();
		Options options = new Options();
		options.setContentType(contentType);
		Set<String> subSet = contentDao.getSubscriptionList(options);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("subscriptions", subSet);
		return Response.ok().entity(jsonObj).build();
	}

	@GET
	@Path("/subscription")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSubscription(
			@QueryParam("contentType") String contentType,
			@QueryParam("subscriptionName") String subscriptionName)
			throws MongoUtilsException, OptionsException, JSONException {
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(
				contentType);
		DBCollection collection = db.getCollection("subscriptions");
		DBObject subscription = collection.findOne(new BasicDBObject("_id",
				subscriptionName));
		return Response.ok().entity(subscription.toString()).build();
	}

	@GET
	    @Path("/republish")
	    @Produces({ MediaType.APPLICATION_JSON })
	    public Response refreshSubscription(@QueryParam("contentType") String contentType, @QueryParam("subscription") String subscription) throws MongoUtilsException, OptionsException, JSONException {

		ConfigDAO configDao = new ConfigDAO();
		// Delete Subscription change feature
		configDao.deleteSubscriptionEntry(contentType, subscription);
		prepareSubscriptionsMap(contentType, subscription);
		evaluateAndUpdateDocuments(contentType, ServiceConstants.METADATA_LIVE_COLLECTION, ServiceConstants.METADATA_CACHE_COLLECTION);
		JSONObject response = new JSONObject();
		response.put("status", "Republish complete");
		return Response.ok().entity(response).build();
	    }

	@GET
	@Path("/edit")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response editSubscription(
			@QueryParam("contentType") String contentType,
			@QueryParam("subscriptionName") String subscriptionName)
			throws MongoUtilsException, OptionsException, JSONException {
		String filter = null;
		String hierarchyExpansions = null;
		BasicDBList list = null;
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(
				contentType);
		DBCollection collection = db.getCollection("subscriptions");
		DBObject subscription = collection.findOne(new BasicDBObject("_id",
				subscriptionName));
		list = getList(subscription,"javafilter");
		if(list.size()==0){
		list = getList(subscription, "filter");
		}
		Iterator<?> it = list.iterator();
		while (it.hasNext()) {
			filter = it.next().toString();
			filter = filter.replace("#", "$");
			filter = filter.replace("//", ".");
		}
		Set<String> filterKeys = getKeys(filter);
		BasicDBList list1 = getList(subscription, "hierarchyExpansions");
		Iterator<?> it1 = list1.iterator();
		while (it1.hasNext()) {
			hierarchyExpansions = it1.next().toString();
		}
		Set<String> hierarchykeys = getKeys(hierarchyExpansions);
		getFilter(filter);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("filterKeys", filterKeys);
		jsonObj.put("hierarchykeys", hierarchykeys);
		jsonObj.put("getFilterKeys", getFilterKeys);
		jsonObj.put("getFilterValues", getFilterValues);
		jsonObj.put("getOperators", getOperators);
		return Response.ok().entity(jsonObj).build();
	}
   //delete subscription feature is disabled as of now
	/*@GET
	@Path("/delete")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteSubscription(
			@QueryParam("contentType") String contentType,
			@QueryParam("subscriptionName") String subscriptionName)
			throws MongoUtilsException, OptionsException, JSONException {
		ConfigDAO configDao = new ConfigDAO();
		long deletedRowCount = configDao.deleteSubscriptionEntry(contentType,
				subscriptionName);
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(
				contentType);
		DBCollection subscriptions = db.getCollection("subscriptions");
		BasicDBObject subscription = (BasicDBObject) subscriptions.findOne(new BasicDBObject("_id",
				subscriptionName));
		BasicDBObject subscriptionId = new BasicDBObject().append("_id", subscriptionName);
		BasicDBObject subscriptionVal = new BasicDBObject();
		String query = subscription.toString();
		query = query.replace("$", "#");
		query = query.replace(".", "//");

		//System.out.println(query);
		subscriptionVal = (BasicDBObject) JSON.parse(query);
		
		DBCollection archivesubscriptions = db
				.getCollection("archivesubscriptions");
		archivesubscriptions.insert(subscriptionVal);
		DBCollection subscriptionCollection = db.getCollection("subscriptions");
		subscriptionCollection.remove(subscriptionId);
		
		JSONObject insertObj = new JSONObject();
		insertObj.put("insertresult", "Subscription Successfully Deleted");
		return Response.ok().entity(insertObj).build();
	}
*/
	private BasicDBList getList(DBObject dbObject, String key) {
		Object list = dbObject.get(key);
		if (list == null) {
			return new BasicDBList();
		}
		BasicDBList returnList = new BasicDBList();

		if (list instanceof BasicDBList) {
			return (BasicDBList) list;
		} else if (list instanceof DBObject) {
			returnList.add((DBObject) list);
		} else if (list instanceof String) {
			returnList.add((String) list);

		}
		return returnList;
	}

	private Set getKeys(String filter) {
		Pattern pattren = Pattern.compile("\"([^\"]*)\"");
		String result = null;
		Matcher m = pattren.matcher(filter);
		Set<String> s = new HashSet<String>();
		List<String> operatorList = new ArrayList<String>();
		operatorList.add("$or");
		operatorList.add("$and");
		operatorList.add("$not");
		operatorList.add("$nor");
		operatorList.add("$gt");
		operatorList.add("$gte");
		operatorList.add("$lt");
		operatorList.add("$lte");
		operatorList.add("$ne");
		operatorList.add("$exists");
		operatorList.add("$type");
		operatorList.add("$in");
		operatorList.add("$nin");
		operatorList.add("$all");
		operatorList.add("$elemMatch");
		operatorList.add("$size");
		operatorList.add("$mod");
		operatorList.add("$regex");
		operatorList.add("$where");
		while (m.find()) {
			result = m.group(1);
			if (!operatorList.contains(result)
					&& (result.contains("document") || (result
							.contains("soar-software-feed")))) {
				s.add(result);
			}
		}
		return s;
	}

	private void getFilter(String filter) {
		getFilterKeys = new ArrayList<String>();
		getFilterValues = new ArrayList<List<String>>();
		getOperators = new ArrayList<String>();
		Pattern pattren = Pattern.compile("\"([^\"]*)\"");
		Pattern pattren1 = Pattern.compile("\"([^\"]*)\"|\\((.*?)\\)");
		String result = null;

		String temp = filter;
		temp = temp.replaceAll("[^0-9]+", " ");
		Set<String> numbers = new HashSet<String>(Arrays.asList(temp.trim()
				.split(" ")));
		for (String s : numbers) {
			filter = filter.replace(s, "(" + s + ")");
		}
		Matcher m = pattren1.matcher(filter);

		List<String> values = new ArrayList<String>();
		List<String> operatorList = new ArrayList<String>();
		operatorList.add("$gt");
		operatorList.add("$gte");
		operatorList.add("$lt");
		operatorList.add("$lte");
		operatorList.add("$ne");
		operatorList.add("$exists");
		operatorList.add("$type");
		operatorList.add("$in");
		operatorList.add("$nin");
		operatorList.add("$all");
		operatorList.add("$elemMatch");
		operatorList.add("$size");
		operatorList.add("$mod");
		operatorList.add("$regex");
		operatorList.add("$where");
		List<String> logicalOperatorList = new ArrayList<String>();
		logicalOperatorList.add("$or");
		logicalOperatorList.add("$and");
		logicalOperatorList.add("$nor");
		logicalOperatorList.add("$nand");
		String key = null;
		String operator = null;

		while (m.find()) {
			if (m.group(1) == null)
				result = m.group(2);
			else
				result = m.group(1);

			result = result.replace("(", "");
			result = result.replace(")", "");

			//System.out.println(result);
			if (!operatorList.contains(result)
					&& !logicalOperatorList.contains(result)
					&& (result.contains("document") || result
							.contains("soar-software-feed"))) {
				if (values.size() != 0) {
					getFilterKeys.add(key);
					getFilterValues.add(values);
					getOperators.add(operator);
				}
				values = new ArrayList<String>();
				key = result;
				operator = "";
			} else if (!logicalOperatorList.contains(result)
					&& !(result.contains("document") || result
							.contains("soar-software-feed"))) {
				if (operatorList.contains(result))
					operator = result;
				else {
					if (!result.equals("")) {
						values.add(result);
					}
				}
			}
		}
		getFilterKeys.add(key);
		getFilterValues.add(values);
		getOperators.add(operator);
	}

	@GET
	@Path("/submit")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response submitSubscription(
			@QueryParam("contentType") String contentType,
			@QueryParam("subscriptionName") String subscriptionName,
			@QueryParam("newQuery") String newQuery,
			@QueryParam("hierarchyKeyChange") String hierarchyKeyChange,
			@QueryParam("hierarchyValueChange") String hierarchyValueChange)
			throws WebApplicationException, OptionsException, JSONException {

		String hierarchyKeyChangeVal = null;
		boolean hierarchyValueChangeVal = false;
		DBObject queryobj = new BasicDBObject();
		boolean result = false;
		String count = null;
		JSONObject resultObj = new JSONObject();

	if (newQuery != null && !(newQuery.equals("NoChange"))) {
			//System.out.println("newQuery:"+newQuery);
			//newQuery = newQuery.replace("!", "{");
			newQuery = newQuery.replace("$", "#");
			newQuery = newQuery.replace(".", "//");
			//newQuery = newQuery.replace("@", "#");
			//System.out.println("newQuery:"+newQuery);
			newQuery = "{" + newQuery + "}";
			//System.out.println("Query:" + newQuery);
			queryobj = new BasicDBObject("newQuery",newQuery);
		}
		if (hierarchyKeyChange != null
				&& !("Select").equals(hierarchyKeyChange))
			hierarchyKeyChangeVal = hierarchyKeyChange;
		if (hierarchyValueChange != null
				&& !("Select").equals(hierarchyValueChange))
			hierarchyValueChangeVal = Boolean.valueOf(hierarchyValueChange);

		try {
			if (contentType != null && subscriptionName!=null) {
				DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
		        DBCollection subscriptions = db.getCollection("subscriptions");
		        BasicDBObject subscription = new BasicDBObject().append("_id", subscriptionName);
				if (queryobj != null && !(("NoChange").equals(newQuery))) {
				        BasicDBObject newfilter = new BasicDBObject();
				        //System.out.println(queryobj.get("newQuery").toString());
				        //DBObject query = new BasicDBObject("javafilter", new BasicDBObject("$exists", true));
				        /*DBCursor cursorSize = subscriptions.find(query);
				        if(cursorSize.size()!=0){*/
				        newfilter.append("$set", new BasicDBObject().append("javafilter", queryobj.get("newQuery")));
				       /* }
				        else{
				        newfilter.append("$addToSet", new BasicDBObject().append("javafilter", queryobj.get("newQuery")));
				        }
				        cursorSize.close();*/
						subscriptions.update(subscription, newfilter,true,false);
						resultObj.put("resultfilter",
							"Successfully updated the filter");
						
				}
		        
				
				if (hierarchyKeyChangeVal != null && hierarchyValueChange != null) {
					DBObject dbHierachyObj = new BasicDBObject();
					dbHierachyObj.put("hierarchyValueChangeVal",
							hierarchyValueChangeVal);						
					BasicDBObject hierarchyObj = new BasicDBObject();
					hierarchyObj.append("$set", new BasicDBObject().append(
							"hierarchyExpansions." + hierarchyKeyChangeVal,
							dbHierachyObj.get("hierarchyValueChangeVal")));
					subscriptions.update(subscription, hierarchyObj);
					resultObj.put("resulthierarchy",
							"Successfully updated the Hierarchy value");
				}
			}else{
				resultObj.put("result",
						"Query is not Updated");
			}
		} catch (MongoUtilsException e) {
			resultObj.put("result",
					"Invalid query so subscription is not updated");
			//throw new ApplicationException("Invalid query:" + e.getMessage());
			throw new WebApplicationException(e, 500);
		} catch (Exception e) {
			resultObj.put("result",
					"Invalid query so subscription is not updated");
			throw new ApplicationException("Invalid query:" + e.getMessage());
		}
		return Response.ok().entity(resultObj).build();
	}
	public void prepareSubscriptionsMap(String contentType,String subscription) throws OptionsException, MongoUtilsException{
		Options options = new Options();
		options.setContentType(contentType);
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(contentType);
        DBCollection subscriptions = db.getCollection("subscriptions");
		DBObject query = new BasicDBObject("_id",
				subscription);
		DBObject queryparam = new BasicDBObject();
        DBCursor cursor = subscriptions.find(query);
        DBObject subFilter = null;
        String javafilter = null;
        String filter  = null;
        DBObject queryobj = new BasicDBObject();
        
        
        while(cursor.hasNext()){
			DBObject sub = cursor.next();
			String subname = sub.get("_id").toString();
			filter  =  sub.get("filter").toString();
			//System.out.println("SubscriptionName:"+subname);			
			if(!("".equals(javafilter)) && sub.get("javafilter")!=null){		
				//System.out.println("in javafilter");
				javafilter = sub.get("javafilter").toString();
				javafilter=javafilter.replace("//", ".");
				javafilter=javafilter.replace("#", "$");
				javafilter=javafilter.replace("\\", "");
				if(javafilter.startsWith("[")){
					javafilter = javafilter.replace(" ", "");
					javafilter = javafilter.substring(2, javafilter.length()-2);
				}
				subFilter = (DBObject) JSON.parse(javafilter);
				//System.out.println("Query:"+subFilter+"javafilter:"+javafilter);
			}else if(!("".equals(filter)) && sub.get("filter")!=null){
				subFilter = (DBObject) JSON.parse(filter);
				//System.out.println("Query:"+subFilter);
			}else{
				subFilter = new BasicDBObject();	
			}
			subscriptionMap.put(sub.get("_id").toString(),subFilter);
			}
        cursor.close();
	}
	public void evaluateAndUpdateDocuments(String contentType,String targetLiveCollectionName,String targetCacheCollectionName) throws OptionsException, MongoUtilsException{
		DBCursor cursor = null;
		try{
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(
				contentType);
		DBCollection targetCacheCollection  = db.getCollection(targetCacheCollectionName);
		DBCollection targetLiveCollection  = db.getCollection(targetLiveCollectionName);
		
		DBObject dbobject;
		for (Map.Entry<String, DBObject> subscription : subscriptionMap.entrySet()){
			dbobject= subscription.getValue();
			// changes to make sure only the documents in temp table with SUB_EVAL_STARTED status will be picked up for evaluation
			cursor = targetLiveCollection.find(dbobject).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				for(DBObject record : cursor){
					//updateDocument(subscription.getKey(), record, targetCacheCollection);
					updateDocument(subscription.getKey(), record, targetLiveCollection,targetCacheCollection);
					
				}		
			}
		}finally{
			cursor.close();
		}
	}
	
	    public void updateDocument(String subscriptionName, DBObject dbObject, DBCollection targetLiveCollection, DBCollection targetCacheCollection) throws MongoUtilsException, OptionsException {
		BasicDBList subscriptionsList = (BasicDBList) dbObject.get("subscriptions");
		String Id = (String) dbObject.get("_id");
		//BasicDBObject dbObj = new BasicDBObject();
		if (subscriptionsList != null) {
		    if (!subscriptionsList.contains(subscriptionName)) {
			subscriptionsList.add(subscriptionName);
		    }
		}
		else {
		    subscriptionsList = new BasicDBList();
		    subscriptionsList.add(subscriptionName);
		}
		dbObject.put("subscriptions", subscriptionsList);
		targetLiveCollection.save(dbObject);

		DBObject query = new BasicDBObject("_id", Id);
		//commenting delete sub change for faster revaluation on 2/8/2015
		// delete Subscription change feature
		//DBObject update = new BasicDBObject("$pull", new BasicDBObject("deleteSubs", new BasicDBObject("$in", subscriptionsList)));
		//DBObject document = targetCacheCollection.findAndModify(query, new BasicDBObject(), new BasicDBObject(), false, update, true, true);
		DBObject document = targetCacheCollection.findOne(Id);
		document.put("subscriptions", subscriptionsList);
		targetCacheCollection.save(document);

	    }
}