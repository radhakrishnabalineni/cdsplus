package com.hp.cdsplus.web.resource;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.hp.cdsplus.mongo.utils.DiagnosticLogger;
import com.hp.cdsplus.util.hpsmxmlutil.HpsmXmlMailUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author reddypm
 *
 */
@Path("/healthcheck")
public class HealthCheckResource {

	@GET
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkApplicationStatus(){

		return Response.ok("SUCCESS: Jboss deployment for URL used running fine").build();

	}

	@GET
	@Path("/mongodb")
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkMongoDBStatus(){
		String mailPropertiesPath= System.getProperty("mail.configuration");
		HpsmXmlMailUtils mail;
		StringBuffer mailMessage= new StringBuffer();
		try {
			mail = new HpsmXmlMailUtils(mailPropertiesPath);
			//MongoClient client = getMongoClient();
			MongoClient client = mongoClientInstance();
			List<String> docdbList= prepareDocumentDBList();
			for (String dbName : docdbList) {
				DB db = client.getDB(dbName);
				if(db==null){
					mailMessage= mailMessage.append("Not able to connect to MongoDB's db-"+dbName);
					mail.sendNotificationMail("MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Not able to connect to MongoDB's db-"+dbName).build();
				}
				Set<String> collectionSet= db.getCollectionNames();

				if(collectionSet.isEmpty()){
					mailMessage= mailMessage.append(" Collections in "+dbName+" is empty.");
					mail.sendNotificationMail("MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Collections in "+dbName+" is empty.").build();
				}
				if(!collectionSet.contains("metadata_staging") || !collectionSet.contains("metadata_live") || !collectionSet.contains("metadata_cache") || !collectionSet.contains("metadata_history")){
					mailMessage= mailMessage.append(" Do not have all the required collections in "+dbName);
					mail.sendNotificationMail("MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Do not have all the required collections in "+dbName).build();
				}
			}

			List<String> contentdbList= prepareContentDBList();

			for (String dbName : contentdbList) {

				DB db = client.getDB(dbName);
				if(db==null){
					mailMessage= mailMessage.append(" Not able to connect to MongoDB's db-"+dbName);
					mail.sendNotificationMail("MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Not able to connect to MongoDB's db-"+dbName).build();
				}
				Set<String> collectionSet= db.getCollectionNames();

				if(collectionSet.isEmpty()){
					mailMessage= mailMessage.append(" Collections in "+dbName+" is empty.");
					mail.sendNotificationMail("MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Collections in "+dbName+" is empty.").build();
				}
				if(!collectionSet.contains("content.files")){
					mailMessage= mailMessage.append("Do not have all the required collections in "+dbName);
					mail.sendNotificationMail("CDSPLUS Redesign-MongoDB Not Working", mailMessage.toString());
					return Response.ok("ERROR: Do not have all the required collections in "+dbName).build();
				}
			}
		} catch (Exception e) {
			if(clientInstance != null){
                clientInstance.close();
                clientInstance = null;
          }
			return Response.ok("ERROR: Not able to connect to MongoDB:"+e.getMessage()).build();
		}

		return Response.ok("SUCCESS: MongoDB running fine").build();

	}

	@GET
	@Path("/concentraloader")
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkConcentraLoaderStatus(){
		boolean raiseWIFlag=false;
		String mailPropertiesPath= System.getProperty("mail.configuration");
		HpsmXmlMailUtils mail;
		StringBuffer mailMessage= new StringBuffer();
		try{
			mail = new HpsmXmlMailUtils(mailPropertiesPath);

			DBCollection col = getMongoCollection();
			long lastmodified=0;
			long currentTime= System.currentTimeMillis();
			DBCursor cursor= col.find(new BasicDBObject("_id","concentraloader"));
			if(cursor.count()!=0){
				DBObject dbo= cursor.next();
				lastmodified= Long.valueOf(dbo.get("lastmodified").toString());
				//Notify when getworkitems is not updated for last 10 mins.
				if(lastmodified<(currentTime-(10*60*1000))){
					raiseWIFlag=true;
				}
			}else{
				raiseWIFlag=true;
			}

			if(raiseWIFlag){
				mailMessage=mailMessage.append("\n").append("Concentra Loader get work items thread is not working. This thread last updated on-" + lastmodified);
				mail.sendNotificationMail("CDSPLUS Redesign-Concentra Loader Not Working", mailMessage.toString());
				return Response.ok("ERROR: Concentra Loader is not working.").build();
			}


		}catch (Exception e) {
			return Response.ok("ERROR: Not able to perform this check:"+e.getMessage()).build();
		}

		return Response.ok("SUCCESS: Concentra Loader running fine").build();
	}

	@GET
	@Path("/soarloader")
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkSoarLoaderStatus(){
		boolean raiseWIFlag=false;
		String mailPropertiesPath= System.getProperty("mail.configuration");
		HpsmXmlMailUtils mail;
		StringBuffer mailMessage= new StringBuffer();
		try{
			mail = new HpsmXmlMailUtils(mailPropertiesPath);

			DBCollection col = getMongoCollection();
			long lastmodified=0;
			long currentTime= System.currentTimeMillis();
			DBCursor cursor= col.find(new BasicDBObject("_id","soarloader"));
			if(cursor.count()!=0){
				DBObject dbo= cursor.next();
				lastmodified= Long.valueOf(dbo.get("lastmodified").toString());
				//Notify when getworkitems is not updated for last 10 mins.
				if(lastmodified<(currentTime-(10*60*1000))){
					raiseWIFlag=true;
				}
			}else{
				raiseWIFlag=true;
			}

			if(raiseWIFlag){
				mailMessage=mailMessage.append("\n").append("SOAR Loader get work items thread is not working. This thread last updated on-" + lastmodified);
				mail.sendNotificationMail("CDSPLUS Redesign-SOAR Loader Not Working", mailMessage.toString());
				return Response.ok("ERROR: SOAR Loader is not working.").build();
			}

		}catch (Exception e) {
			return Response.ok("ERROR: Not able to perform this check:"+e.getMessage()).build();
		}

		return Response.ok("SUCCESS: SOAR Loader running fine").build();

	}

	@GET
	@Path("/pmasterloader")
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkPMLoaderStatus(){
		boolean raiseFlag=false;
		String mailPropertiesPath= System.getProperty("mail.configuration");
		HpsmXmlMailUtils mail;
		StringBuffer mailMessage= new StringBuffer();
		try{
			mail = new HpsmXmlMailUtils(mailPropertiesPath);
			DBCollection col = getMongoCollection();
			long lastmodified=0;
			long currentTime= System.currentTimeMillis();
			DBCursor cursor= col.find(new BasicDBObject("_id","pmloader"));
			if(cursor.count()!=0){
				DBObject dbo= cursor.next();
				lastmodified= Long.valueOf(dbo.get("lastmodified").toString());
				//Notify when getworkitems is not updated for last 24 hours.
				if(lastmodified<(currentTime-(24*60*60*1000))){
					raiseFlag=true;
				}
			}else{
				raiseFlag=true;
			}

			if(raiseFlag){
				mailMessage=mailMessage.append("\n").append("Product Master Loader is not working. This thread last updated on-" + lastmodified);
				mail.sendNotificationMail("CDSPLUS Redesign-Product Master Loader Not Working", mailMessage.toString());
				return Response.ok("ERROR: PMaster Loader is not working.").build();
			}

		}catch (Exception e) {
			return Response.ok("ERROR: Not able to perform this check:"+e.getMessage()).build();
		}

		return Response.ok("SUCCESS: PMaster Loader running fine").build();

	}

	@GET
	@Path("/processor")
	@Produces({MediaType.TEXT_PLAIN})
	public Response checkProcessorStatus(){
		boolean raiseMTFlag=false;
		String mailPropertiesPath= System.getProperty("mail.configuration");
		HpsmXmlMailUtils mail;
		StringBuffer mailMessage= new StringBuffer();
		try{
			mail = new HpsmXmlMailUtils(mailPropertiesPath);

			DBCollection col = getMongoCollection();
			long lastmodified=0;
			long currentTime= System.currentTimeMillis();
			DBCursor cursor= col.find(new BasicDBObject("component","processor"));
			if(cursor.count()!=0){
				while(cursor.hasNext()){
					DBObject dbo= cursor.next();
					String instanceName=dbo.get("_id").toString();
					lastmodified= Long.valueOf(dbo.get("lastmodified").toString());
					//Notify when getworkitems is not updated for last 1 hour.
					//changing this to QA test.
					if(lastmodified<(currentTime-(5*60*1000))){
					//if(lastmodified<(currentTime-(1*60*60*1000))){
						raiseMTFlag=true;
						mailMessage=mailMessage.append("\n").append("Processor instance "+instanceName+" main thread is not working. This thread last updated on-" + lastmodified+".");
					}
				}
			}else{
				raiseMTFlag=true;
				mailMessage=mailMessage.append("\n").append("Seems none of the processor instance is running. Please check the 'processor access ui'.");
			}

			if(raiseMTFlag){
				mailMessage=mailMessage.append("\n").append("For additional information please check the playbook/user guide to stop/start the processor instances. ");
				mail.sendNotificationMail("CDSPLUS Redesign-Processor Not Working", mailMessage.toString());
				return Response.ok("ERROR: Processor is not working.").build();
			}

		}catch (Exception e) {
			return Response.ok("ERROR: Not able to perform this check:"+e.getMessage()).build();
		}

		return Response.ok("SUCCESS: All the registered Processor instances running fine").build();

	}

	private List<String> prepareDocumentDBList(){
		List<String> list = new ArrayList<String>();
		list.add("library");
		list.add("soar");
		list.add("marketingstandard");
		return list;
	}

	private List<String> prepareContentDBList(){
		List<String> list = new ArrayList<String>();
		list.add("librarycontent");
		list.add("soar_ref_data");
		return list;
	}

	private DBCollection getMongoCollection() throws UnknownHostException {
		MongoClient client = mongoClientInstance();
		DB db = client.getDB(DiagnosticLogger.DB_NAME);
		DBCollection col=db.getCollection(DiagnosticLogger.COLLECTION_NAME);
		return col;
	}

	/*private MongoClient getMongoClient() throws UnknownHostException {
		String mongoURI = System.getProperty("mongoClientURI");
		MongoClient client= new MongoClient(new MongoClientURI(mongoURI));
		return client;
	}*/
	
	private static MongoClient clientInstance = null;
    
    public MongoClient mongoClientInstance() throws UnknownHostException {
           String mongoURI = System.getProperty("mongoClientURI");
           //mongoURI = "mongodb://cdspdbrw:WelcomeRW_11243@g9t3236.houston.hp.com:20001,g9t3218.houston.hp.com:20001,g9t3219.houston.hp.com:20001/admin?replicaSet=rs0&maxPoolSize=50";
           if(clientInstance == null){
                  clientInstance = new MongoClient(new MongoClientURI(mongoURI));
           }
           
           return clientInstance;
    }

}
