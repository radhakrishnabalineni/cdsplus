package com.hp.cdsplus.web.contentservice.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import com.hp.cdsplus.bindings.output.schema.expattachment.Attachments;
import com.hp.cdsplus.bindings.output.schema.expversions.Versions;
import com.hp.cdsplus.bindings.output.schema.productmastercache.Content;
import com.hp.cdsplus.bindings.output.schema.productmastercache.MarketingCategory;
import com.hp.cdsplus.bindings.output.schema.productmastercache.MarketingSubcategory;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ObjectFactory;
import com.hp.cdsplus.bindings.output.schema.productmastercache.OldOne;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductBigSeries;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductLines;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductName;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductNumber;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductNumbers;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductSeries;
import com.hp.cdsplus.bindings.output.schema.productmastercache.ProductType;
import com.hp.cdsplus.bindings.output.schema.productmastercache.Source;
import com.hp.cdsplus.bindings.output.schema.productmastercache.SupportCategory;
import com.hp.cdsplus.bindings.output.schema.productmastercache.SupportNameOids;
import com.hp.cdsplus.bindings.output.schema.productmastercache.SupportSubcategory;
import com.hp.cdsplus.bindings.output.schema.productmastercache.View;
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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;

/**
 * @author reddypm
 * 
 */
public class ProductmastercacheServiceImpl extends AbstractGenericService {

	ProductMasterDAO productMasterDAO = new ProductMasterDAO();
	ContentDAO contentDAO = new ContentDAO();
	ConversionUtils conversion = new ConversionUtils();
	
	// SMO:User Story #7992
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

	// SMO:User Story #7992
	@Override
	public Object getDocumentList(Options options) throws ApplicationException, WebApplicationException{
		DBCursor docList = null;

		if (options.getSubscription() != null
				&& options.getSubscription().equals(
						ServiceConstants.stylesheetSub))
			throw new ApplicationException(
					ServiceConstants.STYLESHEET_ERROR_MSG
							+ options.getContentType());

		Result result = new Result();
		Ref ref = null;

		result.setBase(options.getBaseUri());

		// SMO:
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
			docList = productMasterDAO.getHierarchyList(options);
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
			} else
				result.setCount("0");
			result.setConsidered("0");

			return result;

		} catch (OptionsException e) {
			throw new ApplicationException(e.getMessage());
		} catch (MongoUtilsException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(500);
		}
	}

	// SMO:User Story #7992
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
		View view = null;
		Source source = null;
		ObjectFactory factory = new ObjectFactory();
		view = factory.createView();
		source = factory.createSource();
		MarketingCategory marketingCategory = factory.createMarketingCategory();
		MarketingSubcategory marketingSubcategory = factory
				.createMarketingSubcategory();
		OldOne oldOne = factory.createOldOne();
		ProductBigSeries productBigSeries = factory.createProductBigSeries();
		ProductLines productLines = factory.createProductLines();
		ProductName productName = factory.createProductName();
		ProductNumbers productNumbers = factory.createProductNumbers();
		ProductNumber productNumber = factory.createProductNumber();
		ProductSeries productSeries = factory.createProductSeries();
		ProductType productType = factory.createProductType();
		SupportNameOids supportNameOids = factory.createSupportNameOids();
		SupportCategory supportCategory = factory.createSupportCategory();
		SupportSubcategory supportSubcategory = factory
				.createSupportSubcategory();

		String subscriptionDet = options.getSubscription() == null ? "content"
				: options.getSubscription();
		try {
			options.setContentType("productmaster");

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
			document = productMasterDAO.getHierarchy(options);
			if (document == null) {
				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ options.getDocid() + " "
								+ ServiceConstants.errorMsg_doesnt_exist);
			}
			source.setId(options.getContentType() + "/" + subscriptionDet + "/"
					+ document.get(ServiceConstants.id).toString());
			view.setSource(source);
			view.setOid(new BigInteger(document.get(ServiceConstants.id)
					.toString()));
			view.setBase(options.getBaseUri());

			if (document.get("MARKETING_CATEGORY_OID") != null) {
				marketingCategory.getNode().addAll(
						convertToList(document.get("MARKETING_CATEGORY_OID")));
				view.setMarketingCategory(marketingCategory);
			}

			if (document.get("MARKETING_SUBCATEGORY_OID") != null) {
				marketingSubcategory.getNode()
						.addAll(convertToList(document
								.get("MARKETING_SUBCATEGORY_OID")));
				view.setMarketingSubcategory(marketingSubcategory);
			}

			oldOne.setNode(document.get("old_one").toString());
			view.setOldOne(oldOne);

			if (document.get("PRODUCT_BIGSERIES_OID") != null) {
				productBigSeries.getNode().addAll(
						convertToList(document.get("PRODUCT_BIGSERIES_OID")));
				view.setProductBigSeries(productBigSeries);
			}

			if (document.get("PRODUCT_LINE_CODE") != null) {
				productLines.getNode().addAll(
						convertToList(document.get("PRODUCT_LINE_CODE")));
				view.setProductLines(productLines);
			}

			if (document.get("PRODUCT_NAME_OID") != null) {
				productName.getNode().addAll(
						convertToList(document.get("PRODUCT_NAME_OID")));
				view.setProductName(productName);
			}

			if (document.get("PRODUCT_NUMBER_OID") != null) {
				productNumber.getNode().addAll(
						convertToList(document.get("PRODUCT_NUMBER_OID")));
				view.setProductNumber(productNumber);
			}

			if (document.get("PRODUCT_NUMBER_NAME") != null) {
				productNumbers.getNode().addAll(
						convertToList(document.get("PRODUCT_NUMBER_NAME")));
				view.setProductNumbers(productNumbers);
			}

			if (document.get("PRODUCT_SERIES_OID") != null) {
				productSeries.getNode().addAll(
						convertToList(document.get("PRODUCT_SERIES_OID")));
				view.setProductSeries(productSeries);
			}

			if (document.get("PRODUCT_TYPE_OID") != null) {
				productType.getNode().addAll(
						convertToList(document.get("PRODUCT_TYPE_OID")));
				view.setProductType(productType);
			}

			if (document.get("SUPPORT_NAME_OID") != null) {
				supportNameOids.getNode().addAll(
						convertToList(document.get("SUPPORT_NAME_OID")));
				view.setSupportNameOids(supportNameOids);
			}

			if (document.get("SUPPORT_CATEGORY_OID") != null) {
				supportCategory.getNode().addAll(
						convertToList(document.get("SUPPORT_CATEGORY_OID")));
				view.setSupportCategory(supportCategory);
			}

			if (document.get("SUPPORT_SUBCATEGORY_OID") != null) {
				supportSubcategory.getNode().addAll(
						convertToList(document.get("SUPPORT_SUBCATEGORY_OID")));
				view.setSupportSubcategory(supportSubcategory);
			}

		} catch (OptionsException oe) {
			throw new ApplicationException(oe.getMessage());
		} catch (MongoUtilsException mue) {
			//throw new ApplicationException(mue.getMessage());
			throw new WebApplicationException(mue, 500);
		} catch (MongoException e) {
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		}

		return view;

	}

	private List<String> convertToList(Object jsonString) {
		String[] splitedString = null;
		List<String> convertArray = new ArrayList<String>();
		if (jsonString != null) {
			splitedString = jsonString.toString().split(",");
		}
		if (splitedString.length == 1) {
			convertArray.add(splitedString[0]);
		} else {

			convertArray.addAll(Arrays.asList(splitedString));
		}
		return convertArray;

	}

	@Override
	public Object getExpandDetails(Options options) throws ApplicationException, WebApplicationException {
		DBObject document = null;
		options.setContentType("productmaster");
		String subscriptionDet = options.getSubscription() == null ? "content"
				: options.getSubscription();

		if (options.getExpand() != null) {
			if (options.getExpand().equalsIgnoreCase("true")) {
				com.hp.cdsplus.bindings.output.schema.productmastercache.Result result = new com.hp.cdsplus.bindings.output.schema.productmastercache.Result();
				com.hp.cdsplus.bindings.output.schema.productmastercache.Ref ref = new com.hp.cdsplus.bindings.output.schema.productmastercache.Ref();
				Content content = new Content();
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
				String href = "productmastercache" + "/" + subscriptionDet
						+ "/" + document.get("_id");
				ref.setEventType("update");
				ref.setHasAttachments(false);
				if (last_modified != null)
					ref.setLastModified(BigInteger.valueOf(last_modified));
				ref.setPriority(BigInteger.valueOf(4));
				ref.setHref(href);
				ref.setType(ServiceConstants.xmlElementType);
				Object Obj = getDocumentMetaData(options);
				content.setView((View) Obj);
				ref.setContent(content);
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

				response = getExpandVersions(options);
			} else if (options.getExpand().equalsIgnoreCase(
					ServiceConstants.expand_attachments)) {
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
		String href = "productmastercache" + "/" + subscription + "/"
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
		String href = "productmastercache" + "/" + subscription + "/"
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
		String href = "productmastercache" + "/" + subscription + "/"
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
		String urlLink = "productmastercache" + "/" + subscription + "/";
		try {
			docList = productMasterDAO.getHierarchyList(options);
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

}
