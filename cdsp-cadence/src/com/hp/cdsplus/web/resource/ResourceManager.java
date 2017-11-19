package com.hp.cdsplus.web.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.exception.ResourceNotFoundException;
import com.hp.cdsplus.web.model.ServiceDelegateBO;
import com.hp.cdsplus.web.model.ServiceDelegateBO.RequestMethod;
import com.hp.cdsplus.web.resourcedelegate.ResourceDelegateManager;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.cdsplus.web.util.URLProcessing;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 *
 */
@Path("/app{base:.*}")
public class ResourceManager {
	private static final Logger logger = LogManager.getLogger(ResourceManager.class);
	
	static{
		try {
			//System.setProperty("mongo.configuration", "config/mongo.properties");
			ConfigurationManager.getInstance().getConfigMappings().refreshMappings();
		} catch (MongoUtilsException e) {
			//throw new ApplicationException("Cache not refresed :"+e.getMessage());
			throw new WebApplicationException(e, 500);
		}
	}
	/**
	 * These resource is to get all Content Type details
	 * 
	 * @return Request
	 */
	@GET
	@Produces({MediaType.WILDCARD})
	public Response getResourceDetails(@Context UriInfo uriInfo)  throws ResourceNotFoundException,ApplicationException {
		long start_time = System.currentTimeMillis();
		ResourceDelegateManager delegateManager = new ResourceDelegateManager();
	
		ServiceDelegateBO delegatorBO=null;
		Response response=null;
		URLProcessing processing = new URLProcessing();
		processing.setRequestMethod(RequestMethod.GET);
		Map<String ,String>  urlParametersMap=processing.processURL(uriInfo);
		if(urlParametersMap!=null && !urlParametersMap.isEmpty())
		{
			delegatorBO = processing.setUrlParameters(urlParametersMap);	
		}
		if(delegatorBO!=null){
			response=delegateManager.resourceDelegate(delegatorBO);
		}
		long end_time = System.currentTimeMillis();
		logger.info(RequestMethod.GET.toString()+" "+uriInfo.getPath()+" "+(end_time-start_time));
		return response;
		
	}
	
	@PUT
	public Response putResourceDetails(@Context UriInfo uriInfo, InputStream is){
		long start_time = System.currentTimeMillis();
		Response response=null;
		ResourceDelegateManager delegateManager = new ResourceDelegateManager();
		
		URLProcessing processing = new URLProcessing();
		processing.setRequestMethod(RequestMethod.PUT);
		Map<String ,String>  urlParametersMap=processing.processURL(uriInfo);
		ServiceDelegateBO delegatorBO=null;
		if(urlParametersMap!=null && !urlParametersMap.isEmpty())
		{
			delegatorBO = processing.setUrlParameters(urlParametersMap);	
		}
		if(delegatorBO!= null){
			delegatorBO.getOptions().setInputStream(is);
			response=delegateManager.resourceDelegate(delegatorBO);
		}
		long end_time = System.currentTimeMillis();
		logger.info(RequestMethod.PUT.toString()+" "+uriInfo.getPath()+" "+(end_time-start_time));
		return response;
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		
		Response response=null;
		ResourceDelegateManager delegateManager = new ResourceDelegateManager();
		
		URLProcessing processing = new URLProcessing();
		processing.setRequestMethod(RequestMethod.PUT);
		
		Map<String ,String>  urlParametersMap = new HashMap<String, String>();
		urlParametersMap.put(ServiceConstants.contextUrl, "http://localhost:8080/cadence/app");
		urlParametersMap.put(ServiceConstants.contentType, "cgs");
		urlParametersMap.put(ServiceConstants.subscriptionType, "system");
		urlParametersMap.put(ServiceConstants.docId, "CGSRules");
		urlParametersMap.put(ServiceConstants.maxLevel, "4");
		ServiceDelegateBO delegatorBO=null;
		if(urlParametersMap!=null && !urlParametersMap.isEmpty())
		{
			delegatorBO = processing.setUrlParameters(urlParametersMap);	
		}
		FileInputStream is = new FileInputStream("config/CGSRules.xml");
		if(delegatorBO!= null){
			delegatorBO.getOptions().setInputStream(is);
			response=delegateManager.resourceDelegate(delegatorBO);
		}
		System.out.println(response);
		
	}
}
