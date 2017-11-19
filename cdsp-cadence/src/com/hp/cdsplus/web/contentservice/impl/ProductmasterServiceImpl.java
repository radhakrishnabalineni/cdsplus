package com.hp.cdsplus.web.contentservice.impl;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.hp.cdsplus.bindings.output.schema.expattachment.Attachments;
import com.hp.cdsplus.bindings.output.schema.expversions.Versions;
import com.hp.cdsplus.bindings.output.schema.productmaster.Child;
import com.hp.cdsplus.bindings.output.schema.productmaster.Children;
import com.hp.cdsplus.bindings.output.schema.productmaster.LongName;
import com.hp.cdsplus.bindings.output.schema.productmaster.Name;
import com.hp.cdsplus.bindings.output.schema.productmaster.Node;
import com.hp.cdsplus.bindings.output.schema.productmaster.ObjectFactory;
import com.hp.cdsplus.bindings.output.schema.productmaster.Parent;
import com.hp.cdsplus.bindings.output.schema.productmaster.Parents;
import com.hp.cdsplus.bindings.output.schema.productmaster.View;
import com.hp.cdsplus.bindings.output.schema.subscription.Ref;
import com.hp.cdsplus.bindings.output.schema.subscription.Result;
import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.ProductMasterDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 * 
 */
public class ProductmasterServiceImpl extends AbstractGenericService {

	ProductMasterDAO productMasterDAO = new ProductMasterDAO();
	ContentDAO contentDAO = new ContentDAO();
	ConversionUtils conversion = new ConversionUtils();

	// SMO:User Story #7555
	@Override
	public Object getSubcriptionList(Options options)
			throws ApplicationException, WebApplicationException {
		TreeSet<String> subscriptionList = null;
		try {
			subscriptionList = contentDAO.getSubscriptionList(options);
			if(subscriptionList==null)
			    subscriptionList = new TreeSet<String>();
			Result result = new Result();
			String hrefLink = options.getContentType() + "/";
			List<Ref> refs = new ArrayList<Ref>();
			Ref ref = null;
			if(!ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
			{
			    subscriptionList.add(("content"));
			}
			if (subscriptionList != null && !subscriptionList.isEmpty()) {
				for (String refDet : subscriptionList) {
					ref = new Ref();
					ref.setHref(hrefLink + refDet);
					ref.setType(ServiceConstants.xmlElementType);
					refs.add(ref);
					result.setCount(String.valueOf(subscriptionList.size()));
				}
			}
			result.getRef().addAll(refs);
			return result;
		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}
	}

	
	// SMO:User Story #7555
	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException {

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		DBCursor docList = null;

		Result result = new Result();
		Ref ref = null;
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		String urlLink = options.getContentType() + "/" + subscription + "/";
		try {
			options.setContentType("productmaster");
			if (!validateSubcsription(options, subscription))
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ subscription + "  "
								+ ServiceConstants.errorMsg_doesnt_exist);
			if ("hpicontent".equals(subscription)) {
				options.setCompany("hpi");
			} else if ("hpecontent".equals(subscription)) {
				options.setCompany("hpe");
			}
			 docList = productMasterDAO.getContentList(options);
			validateDocumentCount(docList, options);

			if (docList != null && !(docList.size() == 0)) {

				for (DBObject docObject : docList) {
					ref = new Ref();
					ref.setEventType("update");
					ref.setPriority("4");
					ref.setHasAttachments("false");
					if (docObject.get(ServiceConstants.id) != null)
						ref.setHref(urlLink
								+ docObject.get(ServiceConstants.id));
					ref.setType(ServiceConstants.xmlElementType);
					if (docObject.get(ServiceConstants.last_Modified) != null)
						ref.setLastModified(docObject.get(
								ServiceConstants.last_Modified).toString());
					result.getRef().add(ref);
				}

				result.setCount(String.valueOf(docList.size()));
				result.setConsidered("0");
				return result;
			} else {
				result.setCount("0");
				result.setConsidered("0");
				return result;
			}
		} catch (OptionsException e) {

			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
	}

	// SMO:User Story #7555
	@Override
	public Object getDocumentMetaData(Options options)
			throws ApplicationException, WebApplicationException {

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		DBObject document;
		Node node = null;
		Name name = null;
		LongName longname = null;
		View view = null;

		ObjectFactory factory = new ObjectFactory();

		String subscriptionDet = options.getSubscription() == null ? "content"
				: options.getSubscription();
		
		try {

			if (!validateSubcsription(options, subscriptionDet)) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ subscriptionDet + "  "
								+ ServiceConstants.errorMsg_doesnt_exist);
			} 
			if ("hpicontent".equals(subscriptionDet)) {
					options.setCompany("hpi");
			
			} else if ("hpecontent".equals(subscriptionDet)) {
				options.setCompany("hpe");
				
			}
			document = productMasterDAO.getContent(options);
			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ options.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}

			// JAXBElement jaxElements=null;

			node = factory.createNode();
			node.setBase(options.getBaseUri());
			node.setOid(new BigInteger(document.get(ServiceConstants.id)
					.toString()));
			if (document.get("hierarchy_level") != null)
				node.getContent().add(
						factory.createHeirarchyLevel(document
								.get("hierarchy_level").toString()
								.toLowerCase()));
			if (document.get("node_type") != null)
				node.getContent().add(
						factory.createNodeType(document.get("node_type")
								.toString()));
			if (document.get("name") != null) {
				BasicDBList nameList = (BasicDBList) document.get("name");
				Iterator<?> nameit = nameList.iterator();
				// for(String str: document.keySet()){
				while (nameit.hasNext()) {
					name = factory.createName();
					DBObject dbobj = (DBObject) nameit.next();
					if (dbobj.get("lang") != null)
						name.setLanguage(dbobj.get("lang").toString()
								.toLowerCase());
					if (dbobj.get("cc") != null)
						name.setCountry((String) dbobj.get("cc").toString()
								.toLowerCase());
					if (dbobj.get("name") != null)
						name.setValue((String) dbobj.get("name"));
					node.getContent().add(name);

				}
			}
			if (document.get("long_name") != null) {
				BasicDBList longNameList = (BasicDBList) document
						.get("long_name");
				Iterator<?> longnameit = longNameList.iterator();
				// for(String str: document.keySet()){
				while (longnameit.hasNext()) {
					longname = factory.createLongName();
					DBObject lognameobj = (DBObject) longnameit.next();
					if (lognameobj.get("lang") != null)
						longname.setLanguage(lognameobj.get("lang").toString()
								.toLowerCase());
					if (lognameobj.get("cc") != null)
						longname.setCountry((String) lognameobj.get("cc")
								.toString().toLowerCase());
					if (lognameobj.get("name") != null)
						longname.setValue((String) lognameobj.get("name"));
					node.getContent().add(longname);

				}
			}

			if (document.get("PRODUCT_LINE_CODE") != null)
				node.getContent().add(
						factory.createProductLine(document.get(
								"PRODUCT_LINE_CODE").toString()));
			if (document.get("CC_BSC_RELEASE_DATE") != null) {
				String actualDateFormat = document.get("CC_BSC_RELEASE_DATE")
						.toString();
				try {
					String legacyDateFormat = getLagacyDateFormat(actualDateFormat);
					if (legacyDateFormat != null) {
						node.getContent()
								.add(factory
										.createCcBscReleaseDate(legacyDateFormat));
					} else {
						node.getContent()
								.add(factory
										.createCcBscReleaseDate(actualDateFormat));
					}
				} catch (ParseException e) {
					node.getContent().add(
							factory.createCcBscReleaseDate(actualDateFormat));
				}
			}
			if (document.get("HIERARCHY_STATUS") != null)
				node.getContent().add(
						factory.createHierarchyStatus(document.get(
								"HIERARCHY_STATUS").toString()));
			if (document.get("PLATFORM_ID") != null)
				node.getContent().add(
						factory.createPlatformId(document.get("PLATFORM_ID")
								.toString()));
			if (document.get("PRODUCT_CLASSIFICATION") != null)
				node.getContent().add(
						factory.createProductClassification(document.get(
								"PRODUCT_CLASSIFICATION").toString()));
			if (document.get("PRODUCT_LINE_NAME") != null)
				node.getContent().add(
						factory.createProductLineDescription(document.get(
								"PRODUCT_LINE_NAME").toString()));
			if (document.get("PRODUCT_NAME_SNF") != null)
				node.getContent().add(
						factory.createSnf(document.get("PRODUCT_NAME_SNF")
								.toString()));
			if (document.get("PRODUCT_NAME_SNI") != null)
				node.getContent().add(
						factory.createSni(document.get("PRODUCT_NAME_SNI")
								.toString()));
			if (document.get("PRODUCT_BIGSERIES_SNI") != null)
				node.getContent().add(
						factory.createSni(document.get("PRODUCT_BIGSERIES_SNI")
								.toString()));
			if (document.get("SUPPORT_NAME_OID") != null)
				node.getContent().add(
						factory.createSupportNameOid(document.get(
								"SUPPORT_NAME_OID").toString()));
			if (document.get("SUPPORT_PUBLISHING_FLAG") != null)
				node.getContent().add(
						factory.createSupportPublishingFlag(document.get(
								"SUPPORT_PUBLISHING_FLAG").toString()));
			if (document.get("WEB_DESTINATION") != null)
				node.getContent().add(
						factory.createWebDestination(document.get(
								"WEB_DESTINATION").toString()));
			if (document.get("PRODUCT_NUMBER_NAME") != null)
				node.getContent().add(
						factory.createProductNumber(document.get(
								"PRODUCT_NUMBER_NAME").toString()));
			if (document.get("PRODUCT_NUMBER_DESC") != null)
				node.getContent().add(
						factory.createProductNumberDescription(document.get(
								"PRODUCT_NUMBER_DESC").toString()));
			if (document.get("RICH_TEXT") != null)
				node.getContent().add(
						factory.createRichText(document.get("RICH_TEXT")
								.toString()));
			if (document.get("COMPANY_DESIGNATION") != null)
				node.getContent().add(
						factory.createCompanyDesignation(document.get(
								"COMPANY_DESIGNATION").toString()));
			if (document.get("LIFECYCLE_STATUS_CODE") != null)
				node.getContent().add(
						factory.createLifecycleStatusCode(document.get(
								"LIFECYCLE_STATUS_CODE").toString()));
			if (document.get("MATERIAL_TYPE_CODE") != null)
				node.getContent().add(
						factory.createMaterialTypeCode(document.get(
								"MATERIAL_TYPE_CODE").toString()));
			if (document.get("SERIAL_FLAG") != null)
				node.getContent().add(
						factory.createSerialFlag(document.get("SERIAL_FLAG")
								.toString()));
			if (document.get("TANGIBLE_FLAG") != null)
				node.getContent().add(
						factory.createTangibleFlag(document
								.get("TANGIBLE_FLAG").toString()));
			// if(document.get("last_modified")!=null)
			// node.getContent().add(factory.createLastModified(BigInteger.valueOf((Long)document.get("last_modified"))));

			view = factory.createView();
			if (document.get(ServiceConstants.id) != null)
				view.setId("productmastercache" + "/" + subscriptionDet + "/"
						+ document.get(ServiceConstants.id).toString());
			node.getContent().add(view);

			Parents parents = factory.createParents();
			List<?> parentList = (List<?>) document.get("parents");

			for (Object obj : parentList) {
				Parent parent = new Parent();
				parent.setId("productmaster" + "/" + subscriptionDet + "/"
						+ (String) obj);
				parents.getParent().add(parent);
			}

			node.getContent().add(parents);

			Children children = factory.createChildren();

			List<?> childrenList = (List<?>) document.get("children");
			for (Object obj : childrenList) {
				Child child = new Child();
				child.setId("productmaster" + "/" + subscriptionDet + "/"
						+ (String) obj);
				children.getChild().add(child);
			}
			node.getContent().add(children);

		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

		return node;

	}

	private String getLagacyDateFormat(String strDate) throws ParseException {
		DateFormat convertFrom = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat convertTo = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String legacyDateFormat = null;
		legacyDateFormat = convertTo.format(convertFrom.parse(strDate));
		return legacyDateFormat;
	}

	@Override
	public Object getExpandDetails(Options options) throws ApplicationException, WebApplicationException {
		DBObject document = null;
		options.setContentType("productmaster");
		String subscriptionDet = options.getSubscription() == null ? "content"
				: options.getSubscription();

		if (options.getExpand() != null && options.getDocid() != null) {
			if (options.getExpand().equalsIgnoreCase("true")) {
				com.hp.cdsplus.bindings.output.schema.productmaster.Result result = new com.hp.cdsplus.bindings.output.schema.productmaster.Result();
				com.hp.cdsplus.bindings.output.schema.productmaster.Ref ref = new com.hp.cdsplus.bindings.output.schema.productmaster.Ref();
				try {
					if (!validateSubcsription(options, subscriptionDet))
						throw new ApplicationException(
								ServiceConstants.errorMsg_The_Entry
										+ " "
										+ subscriptionDet
										+ "  "
										+ ServiceConstants.errorMsg_doesnt_exist);
					document = productMasterDAO.getHierarchy(options);
				} catch (OptionsException e) {
					throw new ApplicationException(e.getMessage());
				} catch (MongoUtilsException e) {
					//throw new ApplicationException(e.getMessage());
					throw new WebApplicationException(500);
				}
				Long last_modified = (Long) document.get("last_modified");
				String href = options.getContentType() + "/" + subscriptionDet
						+ "/" + document.get("_id");
				ref.setEventType("update");
				ref.setHasAttachments(false);
				if (last_modified != null)
					ref.setLastModified(BigInteger.valueOf(last_modified));
				ref.setPriority(BigInteger.valueOf(4));
				ref.setHref(href);
				ref.setType(ServiceConstants.xmlElementType);
				result.setBase(options.getBaseUri());
				result.setConsidered(new BigInteger("0"));
				result.setCount(new BigInteger("1"));
				result.setRef(ref);
				return result;

			}
		}
		return getDocumentExpandDetails(options);
	}

	@Override
	public Object getDocumentExpandDetails(Options options) {
		Object response = null;
		String subscriptionDet = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {
			if (!validateSubcsription(options, subscriptionDet))
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ subscriptionDet + "  "
								+ ServiceConstants.errorMsg_doesnt_exist);
			if (options.getExpand().equalsIgnoreCase(
					ServiceConstants.expand_versions)) {
				if (options.getDocid() == null)
					response = getExpandedDocumentList(options);
				else
					response = getExpandVersions(options);
			} else if (options.getExpand().equalsIgnoreCase(
					ServiceConstants.expand_attachments)) {
				// response = getExpandAttachments(options);
				if (options.getDocid() == null)
					response = getExpandAttachmentsList(options);
				else
					response = getExpandAttachments(options);
			} else {
				response = getGenericExpand(options);
			}

		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		}
		return response;
	}

	@Override
	public Object getExpandVersions(Options options) {
		DBObject document = null;
		options.setContentType("productmaster");
		com.hp.cdsplus.bindings.output.schema.expversions.Result result = new com.hp.cdsplus.bindings.output.schema.expversions.Result();
		com.hp.cdsplus.bindings.output.schema.expversions.Ref ref = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
		Versions versions = new Versions();
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {

			document = productMasterDAO.getHierarchy(options);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
		Long last_modified = (Long) document.get("last_modified");
		String href = options.getContentType() + "/" + subscription + "/"
				+ document.get("_id");
		ref.setEventType("update");
		ref.setHasAttachments(false);
		if (last_modified != null)
			ref.setLastModified(BigInteger.valueOf(last_modified));
		ref.setPriority(BigInteger.valueOf(4));
		ref.setHref(href);
		ref.setType(ServiceConstants.xmlElementType);
		ref.setVersions(versions);
		result.setBase(options.getBaseUri());
		result.setCount(new BigInteger("1"));
		result.setConsidered(new BigInteger("0"));
		result.setRef(ref);
		return result;
	}

	@Override
	public Object getExpandAttachments(Options options) {
		DBObject document = null;
		options.setContentType("productmaster");
		com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
		com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();
		Attachments attachments = new Attachments();
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {

			document = productMasterDAO.getHierarchy(options);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
		Long last_modified = (Long) document.get("last_modified");
		String href = "productmaster" + "/" + subscription + "/"
				+ document.get("_id");
		ref.setEventType("update");
		ref.setHasAttachments(false);
		if (last_modified != null)
			ref.setLastModified(BigInteger.valueOf(last_modified));
		ref.setPriority(BigInteger.valueOf(4));
		ref.setHref(href);
		ref.setType(ServiceConstants.xmlElementType);
		ref.setAttachments(attachments);
		result.setBase(options.getBaseUri());
		result.setCount(new BigInteger("1"));
		result.setConsidered(new BigInteger("0"));
		result.setRef(ref);
		return result;
	}

	public Object getGenericExpand(Options options) {
		DBObject document = null;
		options.setContentType("productmaster");
		com.hp.cdsplus.bindings.output.schema.expattachment.Result result = new com.hp.cdsplus.bindings.output.schema.expattachment.Result();
		com.hp.cdsplus.bindings.output.schema.expattachment.Ref ref = new com.hp.cdsplus.bindings.output.schema.expattachment.Ref();

		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {

			document = productMasterDAO.getHierarchy(options);
		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
		Long last_modified = (Long) document.get("last_modified");
		String href = options.getContentType() + "/" + subscription + "/"
				+ document.get("_id");
		ref.setEventType("update");
		ref.setHasAttachments(false);
		if (last_modified != null)
			ref.setLastModified(BigInteger.valueOf(last_modified));
		ref.setPriority(BigInteger.valueOf(4));
		ref.setHref(href);
		ref.setType(ServiceConstants.xmlElementType);
		result.setConsidered(new BigInteger("0"));
		result.setBase(options.getBaseUri());
		result.setRef(ref);
		return result;
	}

	@Override
	public Object getVersions(Options options) {
		return getGenericExpand(options);
	}

	@Override
	public Object getDocumentAttachments(Options options) {
		Result result = new Result();
		result.setBase(options.getBaseUri());
		result.setConsidered("0");
		result.setCount("0");
		return result;
	}

	@Override
	public Object getExpandAttachmentsList(Options options) {

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		DBCursor docList = null;

		com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result result = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Result();
		com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref ref = null;
		com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments attachments = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Attachments();
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		String urlLink = options.getContentType() + "/" + subscription + "/";
		try {
			docList = productMasterDAO.getContentList(options);
			validateDocumentCount(docList, options);

			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new com.hp.cdsplus.bindings.output.schema.expattachmentlist.Ref();
					ref.setEventType("update");
					ref.setPriority(new BigInteger("4"));
					ref.setHasAttachments(false);
					ref.setAttachments(attachments);
					if (docObject.get(ServiceConstants.id) != null)
						ref.setHref(urlLink
								+ docObject.get(ServiceConstants.id));
					ref.setType(ServiceConstants.xmlElementType);
					if (docObject.get(ServiceConstants.last_Modified) != null)
						ref.setLastModified(new BigInteger(docObject.get(
								ServiceConstants.last_Modified).toString()));
					result.getRef().add(ref);
				}

				result.setCount(new BigInteger(String.valueOf(docList.size())));
				result.setConsidered(new BigInteger("0"));
				return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
		} catch (OptionsException e) {

			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
	}

	public Object getExpandVersionsList(Options options) {

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		DBCursor docList = null;

		com.hp.cdsplus.bindings.output.schema.expversions.Result result = new com.hp.cdsplus.bindings.output.schema.expversions.Result();
		com.hp.cdsplus.bindings.output.schema.expversions.Ref ref = null;
		result.setBase(options.getBaseUri());
		String subscription = options.getSubscription() == null ? "content"
				: options.getSubscription();
		String urlLink = options.getContentType() + "/" + subscription + "/";
		try {
			docList = productMasterDAO.getContentList(options);
			validateDocumentCount(docList, options);

			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new com.hp.cdsplus.bindings.output.schema.expversions.Ref();
					Versions versions = new Versions();
					ref.setEventType("update");
					ref.setPriority(new BigInteger("4"));
					ref.setHasAttachments(false);
					ref.setVersions(versions);
					if (docObject.get(ServiceConstants.id) != null)
						ref.setHref(urlLink
								+ docObject.get(ServiceConstants.id));
					ref.setType(ServiceConstants.xmlElementType);
					if (docObject.get(ServiceConstants.last_Modified) != null)
						ref.setLastModified(new BigInteger(docObject.get(
								ServiceConstants.last_Modified).toString()));
					// result.getRef().add(ref);
				}

				result.setCount(new BigInteger(String.valueOf(docList.size())));
				result.setConsidered(new BigInteger("0"));
				return result;
			} else {
				result.setCount(new BigInteger("0"));
				result.setConsidered(new BigInteger("0"));
				return result;
			}
		} catch (OptionsException e) {

			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
	}

}
