package com.hp.cdsplus.web.util;

import static com.hp.cdsplus.web.util.ServiceConstants.errorMsg_The_PARAM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.exception.ResourceNotFoundException;
import com.hp.cdsplus.web.model.ServiceDelegateBO;
import com.hp.cdsplus.web.model.ServiceDelegateBO.RequestMethod;
import com.hp.cdsplus.web.model.WebOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import javax.ws.rs.WebApplicationException;

public class URLProcessing {

	private RequestMethod requestMethod;

	public URLProcessing() {
		// TODO Auto-generated constructor stub
	}

	public Map<String, String> processURL(UriInfo uriInfo) {
		Map<String, String> url_parameters = new HashMap<String, String>();

		String path = uriInfo.getPath();

		String[] url_path = path.split("/");

		Integer uriPathCount = 0;

		for (String path_components : url_path) {

			if (path_components == null || path_components.trim().length() <= 0)
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_URI + " '"
								+ uriInfo.getRequestUri() + "'"
								+ ServiceConstants.errorMsg_isInvalid);

			switch (uriPathCount) {
			case 0:
				if (path_components.equals("app")
						|| path_components.equals("app/")) {
					url_parameters.put(ServiceConstants.contextUrl,
							path_components);
				} else {
					throw new ResourceNotFoundException(
							ServiceConstants.errorMsg_doesnt_exist);
				}

				break;

			case 1:
				if (path_components.equalsIgnoreCase("*")) {
					url_parameters.put(ServiceConstants.wildcard,
							path_components);
				} else {
					url_parameters.put(ServiceConstants.contentType,
							path_components);
				}
				break;

			case 2:

				//SMO : ID:7955 - prevent content in cdsplus URL 

				if (path_components.equalsIgnoreCase("content")) {

					try {
					    if(ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
					    	throw new ApplicationException(
					    			ServiceConstants.errorMsg_The_URI + " '"
					    					+ uriInfo.getRequestUri() + "'"
					    					+ ServiceConstants.errorMsg_isInvalid);
					}
					catch (MongoUtilsException e) {
					    e.printStackTrace();
						throw new WebApplicationException(e, 500);
					}
				} else if (path_components.equalsIgnoreCase("*")) {
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_URI + " '"
									+ uriInfo.getRequestUri() + "'"
									+ ServiceConstants.errorMsg_isInvalid);
				}
				else if (!path_components.equals("content"))
					url_parameters.put(ServiceConstants.subscriptionType,
							path_components);
				break;

			case 3:

				if (path_components.equalsIgnoreCase("*")) {
					url_parameters.put(ServiceConstants.wildcard,
							path_components);
					uriPathCount--;
				} else
					url_parameters.put(ServiceConstants.docId, path_components);
				break;

			case 4:

				if (path_components.equalsIgnoreCase("*"))
					url_parameters.put(ServiceConstants.wildcard,
							path_components);
				else
					url_parameters.put(ServiceConstants.attachment,
							path_components);
				break;
			default:

				url_parameters.put(ServiceConstants.errorMsg_doesnt_exist,
						path_components);
			}
			uriPathCount++;
		}
		url_parameters.put(ServiceConstants.maxLevel, uriPathCount.toString());

		String[] uriStr = uriInfo.getRequestUri().toASCIIString().split("/");

		url_parameters.put("baseUri", uriStr[0] + "//"
				+ uriInfo.getAbsolutePath().getAuthority()
				+ ServiceConstants.base_uri);

		url_parameters
				.put("actualUri", String.valueOf(uriInfo.getRequestUri()));

		Map<String, List<String>> query_params = uriInfo.getQueryParameters();

		Set<String> paramSet = query_params.keySet();
		if (!paramSet.isEmpty()) {
			
			if(url_parameters.get(ServiceConstants.subscriptionType) != null && 
			   url_parameters.get(ServiceConstants.subscriptionType).equals(ServiceConstants.stylesheetSub))
					throw new ApplicationException("Stylesheet does not allow the parameters");
				
			
			checkValidParams(paramSet);
		}
		for (Entry<String, List<String>> entry : query_params.entrySet()) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				String[] entryValue = entry.getValue().get(0).split(",");

				if (entryValue != null && entryValue.length == 1) {
					if (entryValue[0] != null
							&& entryValue[0].trim().length() > 0)

						url_parameters.put(entry.getKey(),
								entryValue[0].toString());

					else
						throw new ApplicationException(
								ServiceConstants.errorMsg_The_URI + " '"
										+ uriInfo.getBaseUri() + "'"
										+ ServiceConstants.errorMsg_isInvalid);
				} else
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_URI + " '"
									+ uriInfo.getBaseUri() + "'"
									+ ServiceConstants.errorMsg_isInvalid);
			}
		}
		// setting the request method
		return url_parameters;
	}

	private void checkValidParams(Set<String> paramSet) {
		for(String param : paramSet){
			if(!ServiceParams.isValid(param)){
				throw new ApplicationException(errorMsg_The_PARAM + " " + param + " " + ServiceConstants.errorMsg_isInvalid);
			}
		} 
	}

	public ServiceDelegateBO setUrlParameters(Map<String, String> urlParam) {

		WebOptions web_options = new WebOptions();
		Options options = new Options();
		ServiceDelegateBO delegatorBO = new ServiceDelegateBO();

		if (null != urlParam && !urlParam.isEmpty()) {
			
			for (Entry<String, String> entry : urlParam.entrySet()) {
				String key = entry.getKey();
				ServiceParams svcParam= ServiceParams.valueOf(key);
				if (key != null && entry.getValue() != null
						&& entry.getValue().trim().length() > 0) {

					switch (svcParam) {

					case base:
						web_options.setBase(entry.getValue());
						break;
					case contentType:
						options.setContentType(entry.getValue());
						break;
					case subscriptionType:
						options.setSubscription(entry.getValue());
						break;
					case docId:
						options.setDocid(entry.getValue());
						break;
					case attachments:
						options.setAttachmentName(entry.getValue());
						break;
					case after:
						options.setAfter(getMaxLongValue(entry.getValue(),
								urlParam.get("actualUri")));
						break;
					case before:
						options.setBefore(getMaxLongValue(entry.getValue(),
								urlParam.get("actualUri")));
						break;
					case limit:
						options.setLimit(getMaxIntValue(entry.getValue(),
								urlParam.get("actualUri")));
						break;
					case versions:
						options.setVersions(entry.getValue());
						break;
					case includeDeletes:
						options.setIncludeDeletes(Boolean.parseBoolean(entry
								.getValue()));
						break;
					case reverse:
						options.setReverse(Boolean.parseBoolean(entry
								.getValue()));
						break;
					case maxLevel:
						web_options.setMaxLevel(Integer.parseInt(entry
								.getValue()));
						break;
					case baseUri:
						options.setBaseUri(entry.getValue());
						break;
					case expand:
						options.setExpand(entry.getValue());
						break;
					case wildcard:
						web_options.setWildCard(entry.getValue());
						handleInvalidURL(urlParam);
						break;
					case task:
						options.setTask(entry.getValue());
						break;
					case priority:
						DBObject query = options.getQuery();
						if (query == null) {
							query = new BasicDBObject();
						}
						query.put(ServiceParams.priority.name(), Integer.parseInt(entry.getValue()));
						options.setQuery(query);
						options.setPriority(entry.getValue());
						break;
					case eventType:
						if(entry.getValue().equalsIgnoreCase("delete")){
							options.setIncludeDeletes(true);
						}
						setQuery(ServiceParams.eventType,
								entry.getValue(), options);
						options.setEventtype(entry.getValue());
						break;
					case lastModified:
						options.setLastModified(Long.parseLong(entry.getValue()));
						break;
					case hasAttachments:
						options.setHasAttachments(entry.getValue());
						break;
					default:
						break;
					}
				}

			}
		}
		if (this.requestMethod != null) {
			web_options.setRequestMethod(this.requestMethod);
		}
		delegatorBO.setOptions(options);
		delegatorBO.setWebOptions(web_options);
		return delegatorBO;
	}

	private void setQuery(ServiceParams paramName, String paramValue,
			Options options) {
		DBObject query = options.getQuery();
		if (query == null) {
			query = new BasicDBObject();
		}
		query.put(paramName.name(), paramValue);
		options.setQuery(query);
	}

	public enum ServiceParams {

		base, contentType, subscriptionType, docId, after, limit, before, includeDeletes, reverse, wildcard, maxLevel, versions, attachments, expand, subCheck,
		// added
		task, baseUri, actualUri, priority, eventType, lastModified, hasAttachments, status;
		
		private static Set<String> svcParamStrSet = new HashSet<String>();
		static{
			for(ServiceParams param : ServiceParams.values()){
				svcParamStrSet.add(param.toString());
			}
		}
		
		public static boolean isValid(ServiceParams svcParam) {
			return svcParamStrSet.contains(svcParam.toString());
		}

		public static boolean isValid(String param) {
			return svcParamStrSet.contains(param);
		}
	}

	public static int getMaxIntValue(String num, String actualUri) {
		int limit = 0;
		if (num.matches("[0-9]+")) {

			try {
				limit = Integer.parseInt(num);
			} catch (NumberFormatException ne) {
				limit = Integer.MAX_VALUE;
			}

		} else {
			throw new ApplicationException(ServiceConstants.errorMsg_The_URI
					+ " " + actualUri + " "
					+ ServiceConstants.errorMsg_isInvalid);
		}
		return limit;
	}

	// CR363
	public static long getMaxLongValue(String num, String actualUri) {
		long filter = 0;
		if (num.matches("[0-9]+")) {

			try {
				filter = Long.parseLong(num);
			} catch (NumberFormatException ne) {
				filter = Long.MAX_VALUE;
			}

		} else {
			throw new ApplicationException(ServiceConstants.errorMsg_The_URI
					+ " " + actualUri + " "
					+ ServiceConstants.errorMsg_isInvalid);
		}
		return filter;
	}

	// CR 302
	public static void handleInvalidURL(Map<String, String> urlParam) {
		if (null != urlParam && !urlParam.isEmpty()) {
			//for (Entry<String, String> entry : urlParam.entrySet()) {
				String actualUri = urlParam.get("actualUri");
				if ((actualUri.contains("limit") && urlParam.get("docId") != null)
						|| (actualUri.contains("priority") && urlParam
								.get("docId") != null)) {
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_URI + " " + actualUri
									+ " " + ServiceConstants.errorMsg_isInvalid);
				}

			//}

		}

	}

	public void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}
}
