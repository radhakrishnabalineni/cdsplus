package com.hp.cdsplus.web.contentservice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.soar.bindings.output.schema.soar.Attachment;
import com.hp.soar.bindings.output.schema.soar.Authors;
import com.hp.soar.bindings.output.schema.soar.AvailabilitySchedule;
import com.hp.soar.bindings.output.schema.soar.Collection;
import com.hp.soar.bindings.output.schema.soar.Contact;
import com.hp.soar.bindings.output.schema.soar.Contacts;
import com.hp.soar.bindings.output.schema.soar.Content;
import com.hp.soar.bindings.output.schema.soar.Copyright;
import com.hp.soar.bindings.output.schema.soar.Flag;
import com.hp.soar.bindings.output.schema.soar.IaddProperties;
import com.hp.soar.bindings.output.schema.soar.InstallFormat;
import com.hp.soar.bindings.output.schema.soar.ItemSuspended;
import com.hp.soar.bindings.output.schema.soar.Language;
import com.hp.soar.bindings.output.schema.soar.PartnerAvailability;
import com.hp.soar.bindings.output.schema.soar.ProductsSupported;
import com.hp.soar.bindings.output.schema.soar.Ref;
import com.hp.soar.bindings.output.schema.soar.Result;
import com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed;
import com.hp.soar.bindings.output.schema.soar.SoftwareFile;
import com.hp.soar.bindings.output.schema.soar.SoftwareItem;
import com.hp.soar.bindings.output.schema.soar.SoftwareItems;
import com.hp.soar.bindings.output.schema.soar.SoftwareSet;
import com.hp.soar.bindings.output.schema.soar.UpdateType;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import javax.ws.rs.WebApplicationException;

public class SoarServiceImpl extends AbstractGenericService {
	private static final Logger logger = Logger.getLogger(SoarServiceImpl.class);

	public static final int BIG_MESSAGE_LIMIT = 1024 * 1024 * 14;
	public static final String SOAR_DB_NAME = "soar";
	public static final String ID_FIELD = "_id";
	private static final String SOAR_SOFTWARE_FEED_DOC = "soar-software-feed";
	private static final String SOAR_DOC_COLLECTION = "collection";
	private static final String SOAR_SOFTWARE_ITEMS_DOC = "software-items";
	private static final String SOAR_SOFTWARE_ITEM_DOC = "software-item";
	private static final String PRODUCT_ENVIRONMENTS_SUPPORTED_DOC = "products-environments-supported";
	private static final String SOAR_ITEM_ID="@item-ID";
	private static final String SW_ITEM_ENVIRONMENT_FILE_COLLECTION = "environment_data";

	
	ContentDAO contentDAO = new ContentDAO();

	@Override
	public Object getExpandDetails(Options options) {
		DBObject document = null;
		if (options.getExpand() != null) {
		    
			if (options.getExpand().equalsIgnoreCase("true")) {
				Result result = new Result();
				Ref ref = new Ref();
				Content content = new Content();
				document = getDocument(options);
				if (document == null) {
					throw new ApplicationException(
							ServiceConstants.errorMsg_The_Entry + " "
									+ options.getDocid() + "  "
									+ ServiceConstants.errorMsg_doesnt_exist);
				}
				String subscription = options.getSubscription() == null ? "content" : options.getSubscription(); 
				String href = options.getContentType() + "/" + subscription + "/" + document.get("_id");

				Long lastModified = (Long) document.get("lastModified");
				Integer priority = (Integer) document.get("priority");

				if ((String) document.get("eventType") != null)
					ref.setEventType((String) document.get("eventType"));

				if ((Boolean) document.get("hasAttachments") != null)
					ref.setHasAttachments((Boolean) document
							.get("hasAttachments"));

				if (lastModified != null)
					ref.setLastModified(BigInteger.valueOf(lastModified));

				if (priority != null)
					ref.setPriority(BigInteger.valueOf(priority));

				if (href != null)
					ref.setHref(href);

				ref.setType(ServiceConstants.xmlElementType);

				Object Obj = getGenericExpandTrue(options);

				content.setSoarSoftwareFeed((SoarSoftwareFeed) Obj);

				ref.setContent(content);
				result.setConsidered(new BigInteger("0"));
				result.setBase(options.getBaseUri());
				result.getRef().add(ref);
				result.setCount(new BigInteger("1"));
				String str = convertObjectToXml("Result", result, options);
				str=removeIndentation(str);
				return str;

			}

		}

		return getDocumentExpandDetails(options);
	}

	@Override
	public Object getDocumentMetaData(Options options)
			throws ApplicationException, WebApplicationException {


		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub)){
			return stylesheetUtil.getStylesheetXMLDocument(options);
		}


		String doc = null;
		Document document = null;
		SoarSoftwareFeed documentXml = (SoarSoftwareFeed) super
				.getDocumentMetaData(options);

		if(options.getSubscription()!=null && options.getSubscription().equals("cfr_201")){

			List<Collection> SoarCollection = documentXml.getCollection();

			for (Collection collection : SoarCollection) {
				SoftwareItems switems = collection.getSoftwareItems();
				if (switems != null && !switems.equals("")) {
					List<SoftwareItem> softwareitem = switems.getSoftwareItem();
					if (softwareitem != null && !softwareitem.equals("")) {
						for (SoftwareItem softwareItem2 : softwareitem) {

							//softwareItem2.setProductsSupported(null);
							softwareItem2.setProductsSupported(new ProductsSupported());

						}


					}
				}
			}

			/*documentXml.getCollection().
			doc = doc.replaceAll("\r*\n*<products-supported>.*</products-supported>","<products-supported/>");*/
		}

		addNillableTags(documentXml);

		documentXml.setBase(options.getBaseUri());
		doc = convertObjectToXml(options, documentXml);
		if (options.getSubscription() != null) {
			if (options.getSubscription().equals("astro2")||options.getSubscription().equals("hpsu_201")||options.getSubscription().startsWith("ods")) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				try {
					DocumentBuilder docBuilder;

					docBuilder = docFactory.newDocumentBuilder();
					document = docBuilder.parse(new InputSource(
							new StringReader(doc)));
					document.getDocumentElement().normalize();
					if(options.getSubscription().equals("astro2")){
						doc = changeTagName(document, "product", "pm_oid");
						doc = changeTagName(document, "product_support_subcategories", "support_subcategory");
						doc = changeTagName(document, "product_names", "product_name");
						doc = changeTagName(document, "product_types", "product_type");
					}

					NodeList psElements = document.getElementsByTagName("products-supported");
					if(psElements!=null){
						for(int i=0;i<psElements.getLength(); i++){
							Node psElement = psElements.item(i);
							removeExtraNodeAndValueRetainChilds(psElement,"product");
						}
						doc = changeDOMObjectToXml(document);
						doc = doc.replaceAll("<products-supported>\\s*<","<products-supported><");
					}
				} catch (ParserConfigurationException e) {
					throw new ApplicationException(e.getMessage());
				} catch (SAXException e) {
					throw new ApplicationException(e.getMessage());
				} catch (IOException e) {
					throw new ApplicationException(e.getMessage());
				}
			}/*else if(options.getSubscription().equals("cfr_201")){
				doc = doc.replaceAll("\r*\n*<products-supported>.*</products-supported>","<products-supported/>");
			}*/
		}
		doc = doc.replaceAll("<bundled-item ([^ ]*) item-ID=\"([^ ]*)\"/>",
				"<bundled-item $1 item-ID=\"$2\">$2</bundled-item>");
		//doc = doc.replace("<soar-software-feed ","<soar-software-feed xmlns:dctm=\"http://www.documentum.com\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" ");
		// Remove Indentation
		doc = removeIndentation(doc);		
		doc = doc.replaceAll("</review-flag>\r*\n*<flags/>","</review-flag>\n<flags xsi:nil=\"true\"/>");

		if (options.getSubscription()!=null && options.getSubscription().equals("astro2")){
			doc = getExpandedPmoid(doc);
		}
		return doc;
	}

	/**
	 * This method removes Extra Indentation for soar introduced due to JAXB formatting.
	 * It should be called after convertObjectToXml() method to remove extra spaces 
	 * @param document
	 * @return document without indentation
	 */
	@Override
	protected String removeIndentation(String doc){

		doc = doc.replaceAll("<([^>]*)></\\1>", "<$1/>");
		doc = doc.replaceAll("<([^>]*)> *\r*\n *</\\1>", "<$1/>");
		//doc = doc.replaceAll(" *xsi:nil=\"true\" *xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" */>", "/>");
		//doc = doc.replaceAll(" *xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" *xsi:nil=\"true\" */>", "/>");

		doc = doc.replaceAll(">\r*\n* *<", ">\n<");
		doc = doc.replaceAll("> *\r*\n* *<proj:", "><proj:");
		doc = doc.replaceAll("<([^/][^?>]*)> *\r*\n* *<", "<$1><");
		doc = doc.replaceAll("(<[^/>]*/><[^<>]*>) *\r*\n* *(<[^<>]*>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*/>) *\r*\n *(<[^/<>]*/>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*>) *\r*\n* *(<[^/<>]*>) *\r*\n* *(<[^/<>]*>) *\r*\n* *([^/<>]*</[^/<>]*>)","$1$2$3$4");
		doc = doc.replaceAll("(<[^/<>]*/>) *\r*\n* *(<[^/<>]*>)", "$1$2");
		doc = doc.replaceAll("(<[^/<>]*>) *\r*\n *(<[^/<>]*>)", "$1$2");
		try {
		    if(!ConfigurationManager.getInstance().getConfigMappings().isSmoEnabledFlag())
		       doc= doc.replaceAll("(hp[i|e]content)", "content");
		}
		catch (MongoUtilsException e) {
		    // TODO Auto-generated catch block
		   //throw new ApplicationException();
			throw new WebApplicationException(500);
		}
	
		return doc;

	}

	private void addNillableTags(SoarSoftwareFeed documentXml) {

		List<Collection> SoarCollection = documentXml.getCollection();

		for (Collection collection : SoarCollection) {

			if (collection.getUpdateTypes() != null
					&& !collection.getUpdateTypes().equals("")) {
				List<UpdateType> updatetype = collection.getUpdateTypes()
						.getUpdateType();
				if (updatetype != null && updatetype.size() > 0
						&& !updatetype.equals("")) {
				} else {
					collection.setUpdateTypes(null);
				}
			}

			Copyright copyright = collection.getCopyright();
			if (copyright != null && !copyright.equals("")) {
				String additional_copyright_ack = copyright
						.getAdditionalCopyrightAck();
				if (additional_copyright_ack != null
						&& additional_copyright_ack.equals("")) {
					copyright.setAdditionalCopyrightAck(null);
				}
				String hp_copyright_notice = copyright.getHpCopyrightNotice();
				if (hp_copyright_notice != null
						&& hp_copyright_notice.equals("")) {
					copyright.setHpCopyrightNotice(null);
				}
			}

			List<AvailabilitySchedule> availabilitySchedule = collection
					.getAvailabilitySchedules().getAvailabilitySchedule();
			if (availabilitySchedule != null
					&& !availabilitySchedule.equals("")
					&& availabilitySchedule.size() > 0) {
				for (AvailabilitySchedule availabilitySchedule2 : availabilitySchedule) {
					String alternate_recommendation = availabilitySchedule2
							.getAlternateRecommendation();
					if (alternate_recommendation != null
							&& alternate_recommendation.equals("")) {
						availabilitySchedule2.setAlternateRecommendation(null);
					}
				}
			}

			com.hp.soar.bindings.output.schema.soar.Attachments attachments = collection
					.getAttachments();
			if (attachments != null && !attachments.equals("")) {
				List<Attachment> attachment = attachments.getAttachment();
				if (attachment != null && attachment.size() > 0) {
					for (Attachment attachment2 : attachment) {
						String attachment_source = attachment2
								.getAttachmentSource();
						if (attachment_source != null
								&& attachment_source.equals("")) {
							attachment2.setAttachmentSource(null);
						}
						Authors authors = attachment2.getAuthors();
						if (authors != null && !authors.equals("")) {
							List<String> author = authors.getAuthor();
							if (author != null && author.size() > 0)
								for (String string : author) {
									if (string != null && !string.equals("")) {
									} else {
										if (string.equals("")) {
											attachment2.getAuthors()
											.getAuthor().clear();
											attachment2.getAuthors()
											.getAuthor().add(null);
										}
									}
								}
						}
						List<Language> language = attachment2.getLanguages()
								.getLanguage();
						if (language != null && !language.equals("")) {
							for (Language language2 : language) {
								String charsetinenglish = language2
										.getCharacterSetInEnglish();
								if (charsetinenglish != null
										&& charsetinenglish.equals("")) {
									language2.setCharacterSetInEnglish(null);
								}
								String localcharset = language2
										.getLocalCharacterSet();
								if (localcharset != null
										&& localcharset.equals("")) {
									language2.setLocalCharacterSet(null);
								}
							}
						}

					}
				}
			}

			SoftwareItems switems = collection.getSoftwareItems();
			if (switems != null && !switems.equals("")) {
				List<SoftwareItem> softwareitem = switems.getSoftwareItem();
				if (softwareitem != null && !softwareitem.equals("")) {
					for (SoftwareItem softwareItem2 : softwareitem) {


						if (softwareItem2.getPartnerFeedback() != null
								&& !softwareItem2.getPartnerFeedback().equals(
										"")) {
							if (softwareItem2.getPartnerFeedback()
									.getPhysicalAvailability() != null
									&& !softwareItem2.getPartnerFeedback()
									.getPhysicalAvailability()
									.equals("")) {
								if (softwareItem2.getPartnerFeedback()
										.getPhysicalAvailability()
										.getPartnerAvailability() != null
										&& !softwareItem2.getPartnerFeedback()
										.getPhysicalAvailability()
										.equals("")) {
									List<PartnerAvailability> partneravailability = softwareItem2
											.getPartnerFeedback()
											.getPhysicalAvailability()
											.getPartnerAvailability();
									if (partneravailability != null
											&& partneravailability.size() > 0
											&& !partneravailability.equals("")) {
										for (PartnerAvailability partnerAvailability2 : partneravailability) {
											if (partnerAvailability2
													.getHpAgentNote() != null
													&& partnerAvailability2
													.getHpAgentNote()
													.equals("")) {
												partnerAvailability2
												.setHpAgentNote(null);
											}
											if (partnerAvailability2
													.getHpCustomerNote() != null
													&& partnerAvailability2
													.getHpCustomerNote()
													.equals("")) {
												partnerAvailability2
												.setHpCustomerNote(null);
											}
										}
									}
								}
							}
						}

						com.hp.soar.bindings.output.schema.soar.Attachments itemattachments = softwareItem2
								.getAttachments();
						if (itemattachments != null
								&& !itemattachments.equals("")) {
							List<Attachment> attachment = itemattachments
									.getAttachment();
							if (attachment != null && attachment.size() > 0) {
								for (Attachment attachment2 : attachment) {
									String attachment_source = attachment2
											.getAttachmentSource();
									if (attachment_source != null
											&& attachment_source.equals("")) {
										attachment2.setAttachmentSource(null);
									}
									Authors authors = attachment2.getAuthors();
									if (authors != null && !authors.equals("")) {
										List<String> author = authors
												.getAuthor();
										if (author != null && author.size() > 0)
											for (String string : author) {
												if (string != null
														&& !string.equals("")) {
												} else {
													if (string.equals("")) {
														attachment2
														.getAuthors()
														.getAuthor()
														.clear();
														attachment2
														.getAuthors()
														.getAuthor()
														.add(null);
													}
												}
											}
									}

									List<Language> language = attachment2
											.getLanguages().getLanguage();
									if (language != null
											&& !language.equals("")) {
										for (Language language2 : language) {
											String charsetinenglish = language2
													.getCharacterSetInEnglish();
											if (charsetinenglish != null
													&& charsetinenglish
													.equals("")) {
												language2
												.setCharacterSetInEnglish(null);
											}
											String localcharset = language2
													.getLocalCharacterSet();
											if (localcharset != null
													&& localcharset.equals("")) {
												language2
												.setLocalCharacterSet(null);
											}
										}
									}
								}
							}
						}

						if (softwareItem2.getInstallationFilename() != null
								&& softwareItem2.getInstallationFilename()
								.equals("")) {
							softwareItem2.setInstallationFilename(null);
						}

						if (softwareItem2.getItemUrl() != null
								&& softwareItem2.getItemUrl().equals("")) {
							softwareItem2.setItemUrl(null);
						}

						if (softwareItem2.getPricing() != null
								&& !softwareItem2.getPricing().equals("")) {
							if (softwareItem2.getPricing().getPriceComment() != null
									&& softwareItem2.getPricing()
									.getPriceComment().equals("")) {
								softwareItem2.getPricing()
								.setPriceComment(null);
							}
						}

						if (softwareItem2.getReplacesItemID() != null
								&& softwareItem2.getReplacesItemID().equals("")) {
							softwareItem2.setReplacesItemID(null);
						}

						if (softwareItem2.getSoftwarePassword() != null
								&& softwareItem2.getSoftwarePassword().equals(
										"")) {
							softwareItem2.setSoftwarePassword(null);
						}

						ItemSuspended itemsuspended = softwareItem2
								.getItemSuspended();
						if (itemsuspended != null && !itemsuspended.equals("")) {
							if (itemsuspended.getSuspendedReason() != null
									&& itemsuspended.getSuspendedReason()
									.equals("")) {
								itemsuspended.setSuspendedReason(null);
							}
						}

						if (softwareItem2.getUpdateTypes() != null
								&& !softwareItem2.getUpdateTypes().equals("")) {
							List<UpdateType> updatetype = softwareItem2
									.getUpdateTypes().getUpdateType();
							if (updatetype != null && updatetype.size() > 0
									&& !updatetype.equals("")) {
							} else {
								softwareItem2.setUpdateTypes(null);
							}
						}

						if (softwareItem2.getFlags() != null
								&& !softwareItem2.getFlags().equals("")) {
							List<Flag> flags = softwareItem2.getFlags()
									.getFlag();
							if (flags != null && flags.size() > 0
									&& !flags.equals("")) {
							} else {
								softwareItem2.setFlags(null);
							}
						}
						if (softwareItem2.getInstallFormats() != null
								&& !softwareItem2.getInstallFormats()
								.equals("")) {
							List<InstallFormat> installformats = softwareItem2
									.getInstallFormats().getInstallFormat();
							if (installformats != null
									&& installformats.size() > 0
									&& !installformats.equals("")) {
							} else {
								softwareItem2.setInstallFormats(null);
							}
						}

						IaddProperties iaddproperties = softwareItem2
								.getIaddProperties();
						if (iaddproperties != null
								&& !iaddproperties.equals("")) {
							if (iaddproperties.getDriverName() != null
									|| iaddproperties.getDriverReleaseDate() != null
									|| iaddproperties.getPrimaryInfFile() != null
									|| iaddproperties
									.getDriverInstallationMethod() != null
									|| iaddproperties.getDriverModels() != null
									|| iaddproperties
									.getDriverNetworkCompatible() != null) {
							} else {
								softwareItem2.setIaddProperties(null);
							}
						}

						List<Language> language = softwareItem2.getLanguages()
								.getLanguage();
						if (language != null && !language.equals("")) {
							for (Language language2 : language) {
								String charsetinenglish = language2
										.getCharacterSetInEnglish();
								if (charsetinenglish != null
										&& charsetinenglish.equals("")) {
									language2.setCharacterSetInEnglish(null);
								}
								String localcharset = language2
										.getLocalCharacterSet();
								if (localcharset != null
										&& localcharset.equals("")) {
									language2.setLocalCharacterSet(null);
								}
							}
						}

						List<SoftwareSet> softwareset = softwareItem2
								.getSoftwareFiles().getSoftwareSet();
						if (softwareset != null && !softwareset.equals("")) {
							for (SoftwareSet softwareSet2 : softwareset) {
								List<SoftwareFile> softwarefile = softwareSet2
										.getSoftwareFile();
								if (softwarefile != null
										&& !softwarefile.equals("")) {
									for (SoftwareFile softwareFile2 : softwarefile) {
										String cmdline = softwareFile2
												.getCmdline();
										if (cmdline != null
												&& cmdline.equals("")) {
											softwareFile2.setCmdline(null);
										}
									}
								}
							}
						}
					}
				}
			}
			Contacts contacts = collection.getContacts();
			if (contacts != null && !contacts.equals("")) {
				List<Contact> contact = contacts.getContact();
				if (contact != null && !contact.equals("")) {
					for (Contact contact2 : contact) {
						String contactemail = contact2.getContactEmail();
						if (contactemail != null && contactemail.equals("")) {
							contact2.setContactEmail(null);
						}
					}
				}
			}

			if (collection.getFlags() != null
					&& !collection.getFlags().equals("")) {
				List<Flag> flags = collection.getFlags().getFlag();
				if (flags != null && flags.size() > 0 && !flags.equals("")) {
				} else {
					collection.setFlags(null);
				}
			}
		}
	}

	private String changeTagName(Document doc, String fromTag, String toTag) {
		StringWriter sw = null;
		StreamResult result = null;
		NodeList nodes = doc.getElementsByTagName(fromTag);
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i) instanceof Element) {
					Element elem = (Element) nodes.item(i);
					if (!elem.hasAttributes())
						doc.renameNode(elem, elem.getNamespaceURI(), toTag);
				}
			}

			try {
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				sw = new StringWriter();
				result = new StreamResult(sw);
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);
			} catch (TransformerException e) {
				throw new ApplicationException(e.getMessage());
			}

		}
		return sw.toString();
	}

	@Override
	public Object getExpandedDocumentList(Options options) {
		DBCursor docList = null;
		Ref ref = null;
		ArrayList<Ref> refs = new ArrayList<Ref>();
		Result result = new Result();
		try {
			if (options.getLimit() > 10000)
				options.setLimit(10000);
			else if (options.getLimit() == 0)
				options.setLimit(20);

			String subscription = options.getSubscription() == null ? "content"
					: options.getSubscription();
			String urlLink = options.getContentType() + "/" + subscription
					+ "/";

			if (!("content".equals(subscription))
					&& !validateSubcsription(options, subscription)
					&& !validateFastxmlSubscription(options, subscription)) {

				throw new ApplicationException(
						ServiceConstants.errorMsg_The_Entry + " "
								+ subscription + "  "
								+ ServiceConstants.errorMsg_doesnt_exist);
			} else {
				docList = contentDAO.getLiveDocumentList(options);
			}

			validateDocumentCount(docList, options);
			if (options.isReverse()) {
				docList.sort(new BasicDBObject("lastModified", 1));
			}

			result.setBase(options.getBaseUri());

			if (docList != null && !(docList.size() == 0)) {
				for (DBObject docObject : docList) {
					ref = new Ref();
					ref.setEventType(docObject.get(ServiceConstants.eventType) == null ? "update"
							: docObject.get(ServiceConstants.eventType)
							.toString());
					ref.setHasAttachments(docObject
							.get(ServiceConstants.hasAttachments) == null ? null
									: Boolean
									.valueOf(docObject.get(
											ServiceConstants.hasAttachments)
											.toString()));
					ref.setLastModified(docObject
							.get(ServiceConstants.lastModified) == null ? null
									: BigInteger.valueOf(Long.valueOf(docObject.get(
											ServiceConstants.lastModified).toString())));
					ref.setPriority(docObject.get(ServiceConstants.priority) == null ? BigInteger
							.valueOf(4l) : BigInteger.valueOf(Long
									.valueOf(docObject.get(ServiceConstants.priority)
											.toString())));
					/*
					 * if(ref.getEventType()!=null){
					 * if(ref.getEventType().equalsIgnoreCase("delete"))
					 * ref.setStatus
					 * (docObject.get(ServiceConstants.eventType).toString()); }
					 */
					ref.setType(ServiceConstants.xmlElementType);
					ref.setHref(urlLink + docObject.get(ServiceConstants.id));
					Content content = new Content();
					options.setDocid(docObject.get(ServiceConstants.id)
							.toString());
					content.setSoarSoftwareFeed((SoarSoftwareFeed) super
							.getDocumentMetaData(options));
					ref.setContent(content);
					refs.add(ref);
				}
				result.getRef().addAll(refs);
				result.setCount(BigInteger.valueOf(Long.valueOf(String.valueOf(docList.size()))));
			}else
				result.setCount(new BigInteger("0"));
			result.setBase(options.getBaseUri());
			result.setConsidered(new BigInteger("0"));
			options.setDocid(null);
			String str = convertObjectToXml("Result", result, options);
			return str
					.replace(
							"<soar-software-feed",
							"<soar-software-feed xsi:noNamespaceSchemaLocation=\"soar-software-feed-vE4.xsd\"");

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

	public String getExpandedPmoid(String doc){
		return doc.replaceAll("<pm_oid/>", "<pm_oid></pm_oid>");	
	}

	/**
	 * This method is  overridden from the parent class AbstractGenericService
	 * because currently only soar feed has large metadata. Will invoke  assembleLargeCollection
	 * only for the metadata documents that have been classified as isLargeData.
	 * @param parent the original document
	 * @param dbObject the DBObject which is being constructed
	 * @return
	 */
	@Override
	public void mergeLargeData(DBObject parent, DBObject dbObject) throws MongoInternalException, MongoUtilsException, IOException, ApplicationException {
		String file =  null;
		String docId = null ;
		
		if(parent.containsField("largeMetadataFile")) {
				file = (String) parent.get("largeMetadataFile");
		}
	
		if(parent.containsField("documentId")){
			docId = (String)parent.get("documentId");
		}
		else
		if(parent.containsField(ID_FIELD)){
			docId = (String)parent.get(ID_FIELD);
		}
		
		logger.debug("Large document " + docId + " retrieving environment file " + file );

		if(file != null){

			try{
				assembleLargeCollection(dbObject, file, docId);
			}
			catch(MongoUtilsException ex){
				logger.debug("Error assembling collection " + docId);
				logger.debug(ex);
				throw ex ;
			}

			catch( IOException ex){
				logger.debug("Error fetching file contents for collection " + docId);
				logger.debug(ex);
				throw ex;
			}
		}
	}

	/**
	 * This method will be executed only by soar documents that have been
	 * classified as isLargeData. It gets the separated data (during staging) and
	 * assemble it back. Currently only modifies software items 
	 * @param DBObject where the detached data will be re-attached
	 * @param String the docId for the document
	 * @return
	 */
	private void assembleLargeCollection(DBObject largeObject, String file, String docId) throws MongoInternalException, IOException, MongoUtilsException,
	ApplicationException {
		StringWriter writer = new StringWriter();
		DBObject allSwItemEnv = null;
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(SOAR_DB_NAME);
		GridFS gfs = new GridFS(db, SW_ITEM_ENVIRONMENT_FILE_COLLECTION);

		BasicDBObject query = new BasicDBObject().append("filename", file);
		List<GridFSDBFile> gridFSDBFileList = gfs.find(query);
		if(gridFSDBFileList.size() > 0 ) {
			GridFSDBFile gridFSDBFile = gridFSDBFileList.get(0);
			InputStream inputStream = gridFSDBFile.getInputStream(); 
			IOUtils.copy(inputStream, writer, "UTF-8");
			String gridFSData = writer.toString();
			allSwItemEnv = (DBObject) JSON.parse(gridFSData);
			DBObject soarsoftwarefeed = getObject(largeObject, SOAR_SOFTWARE_FEED_DOC);
			if (soarsoftwarefeed == null) {
				String message = "Sub ducument: soar-software-feed document for docId " + docId + " is null. Probable parsing error.";
				throw new ApplicationException(message);
			}
			else {
				DBObject collect = getObject(soarsoftwarefeed,SOAR_DOC_COLLECTION);
				if (collect != null) {
					DBObject software_items = getObject(collect, SOAR_SOFTWARE_ITEMS_DOC);
					BasicDBList software_item_list = getList(software_items,SOAR_SOFTWARE_ITEM_DOC);
					Iterator<?> itr = software_item_list.iterator();
					while (itr.hasNext()) {
						DBObject software_item = (DBObject) itr.next();
						String item_id= (String)software_item.get(SOAR_ITEM_ID);
						DBObject env = (DBObject)allSwItemEnv.get(item_id);
						software_item.put(PRODUCT_ENVIRONMENTS_SUPPORTED_DOC, env);

					}

				}
				else{
					String message = "Sub ducument: collection for docId " + docId + " is null. Probable parsing error.";
					throw new ApplicationException(message);
				}

			}
		}
		else {
			String message = "No gridfs file found for the collection " + docId ;
			throw new ApplicationException(message);
		}

		return;
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return
	 */
	private DBObject getObject(DBObject dbObject, String key) {
		DBObject returnObj = new BasicDBObject();
		Object obj = dbObject.get(key);
		if (obj == null || obj == "") {
			return null;
		} else if (obj instanceof DBObject) {
			returnObj = (DBObject) obj;

		} else if (obj instanceof String) {
			returnObj = (DBObject) obj;

		}
		return returnObj;
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return list of DB Object
	 */
	private BasicDBList getList(DBObject dbObject, String key) {
		Object object = dbObject.get(key);
		if (object == null || object=="") {
			return new BasicDBList();
		}

		BasicDBList returnList = new BasicDBList();

		if (object instanceof BasicDBList) {
			return (BasicDBList) object;
		} else if (object instanceof DBObject) {
			returnList.add((DBObject) object);
		} else if (object instanceof String) {
			String str = (String) object;
			if (key.equalsIgnoreCase(SOAR_SOFTWARE_ITEM_DOC) ) {
				returnList.addAll(Arrays.asList(str.split(",")));
			}
			else {
				returnList.add(str);
			}

		}else{
			returnList.add(object);
		}
		return returnList;
	}


}

