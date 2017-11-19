package com.hp.cdsplus.processor.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hp.cdsplus.dao.CgsDAO;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.FastXMLDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.SupportContentDAO;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.hp.cdsplus.processor.utils.XMLBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class SupportFastXMLCreationAdapter implements Adapter {

	private MongoAPIUtils mongoUtils = new MongoAPIUtils();	
	private ContentDAO contentDao;
	private CgsDAO cgsDao;
	private FastXMLDAO fastDao;
	private SupportContentDAO supportContentDao;
	public static final String ELEMENT_NAME = "element";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ELEMENT_VALUE = "value";

	private static final String DOCUMENT_DOC = "document";
	private static final String REGION_NAME_FILED = "region.name";
	public static final String REGION_DB_NAME = "region";
	public static final String REGION_COLLECTION_NAME = "metadataLiveCollection";
	private static final String REGION_DOC = "region";
	private static final Logger logger = LogManager.getLogger(SupportFastXMLCreationAdapter.class);

	@Override
	public void evaluate(Item item) throws OptionsException, MongoUtilsException, IOException {
		WorkItem wItem = (WorkItem) item;

		byte[] fastXMLBytes= createFASTXMLDocument(item);
		mongoUtils.writeFASTXMLContent(wItem.getContentType(),wItem.getId(), fastXMLBytes);
	}

	/**
	 * Changes in code to add new elements 
	 * contentbody, embeddedlinks and componentreuse with data coming from supportcontent (refer SupportContentDAO)
	 * and
	 * description, projectname, publicationdestinations, maincomponent, maincomponentdetails, symptom, symptomdetails, faqregions,
	 * faqcountries, publicationdestinations,originaldocid, minorcomponent1, minorcomponent1details, minorcomponent2, minorcomponent2details
	 * failurecode, productdivision, productcategory, contenttopic, contenttopicdetail, projectname, usertask, usertaskdetail
	 * with data coming from support content type (refer FastXMLDAO)
	 * @param item
	 * @return
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 * @throws IOException
	 */
	
	private byte[] createFASTXMLDocument(Item item) throws MongoUtilsException, OptionsException, IOException {
		logger.debug("Entering createFASTXMLDocument to create support FAST xml");
		Element rootElement = null;
		Document document = null;
		document = DocumentHelper.createDocument();
		rootElement = XMLBuilder.getDocumentElement(document);

		WorkItem wItem = (WorkItem) item;

		contentDao = new ContentDAO();	
		cgsDao= new CgsDAO();
		fastDao= new FastXMLDAO();
		supportContentDao = new SupportContentDAO();


		Options cgsoptions = new Options();
		cgsoptions.setContentType("cgs");
		cgsoptions.setDocid(wItem.getId());
		DBObject cgsdbObject = cgsDao.getDocumentMetadata(cgsoptions);

		Options options = new Options();
		options.setContentType(wItem.getContentType());
		options.setDocid(wItem.getId());

		logger.debug(" Started preparing FAST xml for "+options.getDocid()+" ("+options.getContentType()+ ") Thread"+Thread.currentThread().getName());

		DBObject dbObject = contentDao.getTempMetadata(options);
		Map<String, String> supportContent = supportContentDao.retrieveSupportContentData(options.getDocid());

		if(dbObject==null){
			logger.error("Failed to create FAST xml: "+options.getContentType()+ " document "+options.getDocid()+" is "+dbObject);
		}

		if(dbObject!=null){

			DBObject dbDocument = (DBObject) dbObject.get(DOCUMENT_DOC);

			if(dbDocument!=null){

				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"url"), ELEMENT_VALUE, fastDao.url(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"countpmoids"), ELEMENT_VALUE, fastDao.getProductCount(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"countfaqpmoids"), ELEMENT_VALUE, fastDao.getFaqProductCount(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"counttotalpmoids"), ELEMENT_VALUE, fastDao.getTotalProductCount(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"charset"), ELEMENT_VALUE, fastDao.getCharSet(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"robjectid"), ELEMENT_VALUE, fastDao.getRObjectID(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"priority"), ELEMENT_VALUE, fastDao.getPriority(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pmoids"), ELEMENT_VALUE, fastDao.getProductList(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productline"), ELEMENT_VALUE, fastDao.getProductLines(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"producthierarchy"), ELEMENT_VALUE, fastDao.getSupportProductHierarchy(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"documenttype"), ELEMENT_VALUE, fastDao.document_type(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"versionlabel"), ELEMENT_VALUE, fastDao.version_label(dbDocument));
				//For Taxonomy Changes
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"csdocumenttopic"), ELEMENT_VALUE, fastDao.taxonomy_categorizations(dbDocument));
				
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"propertyupdatedate"), ELEMENT_VALUE, fastDao.property_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentupdatedate"), ELEMENT_VALUE, fastDao.content_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"activeflag"), ELEMENT_VALUE, fastDao.active_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentupdateuser"), ELEMENT_VALUE, fastDao.content_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"futuredisclosuredate"), ELEMENT_VALUE, fastDao.future_disclosure_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"istranslationflag"), ELEMENT_VALUE, fastDao.is_translation_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"lifecyclestateupdatedate"), ELEMENT_VALUE, fastDao.lifecycle_state_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"lifecyclestateupdateuser"), ELEMENT_VALUE, fastDao.lifecycle_state_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"propertyupdateuser"), ELEMENT_VALUE, fastDao.property_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgname"), ELEMENT_VALUE, fastDao.cmg_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"colmasterid"), ELEMENT_VALUE, fastDao.col_master_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionrefupdatedate"), ELEMENT_VALUE, fastDao.collection_ref_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionupdatedate"), ELEMENT_VALUE, fastDao.collection_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionupdateuser"), ELEMENT_VALUE, fastDao.collection_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productannouncementdate"), ELEMENT_VALUE, fastDao.product_announcement_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productreleasedate"), ELEMENT_VALUE, fastDao.product_release_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"webreleasedate"), ELEMENT_VALUE, fastDao.web_release_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"ichronicleid"), ELEMENT_VALUE, fastDao.i_chronicle_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contenttopicdetails"), ELEMENT_VALUE, fastDao.content_topic_details(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"document_type_details"), ELEMENT_VALUE, fastDao.document_type_details(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"environments"), ELEMENT_VALUE, fastDao.environments(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"authors"), ELEMENT_VALUE, fastDao.authors(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"regions"), ELEMENT_VALUE, getRegions(fastDao.regions(dbDocument)));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"regionslabel"), ELEMENT_VALUE, fastDao.regionslabel(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"countries"), ELEMENT_VALUE, fastDao.countries(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"searchkeywords"), ELEMENT_VALUE, fastDao.search_keywords(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contenttype"), ELEMENT_VALUE, fastDao.content_type(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"languagelabel"), ELEMENT_VALUE, fastDao.language_label(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"actionrequired"), ELEMENT_VALUE, fastDao.action_required(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionvalidflag"), ELEMENT_VALUE, fastDao.collection_valid_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"publicationcode"), ELEMENT_VALUE, fastDao.publication_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"originaldocid"), ELEMENT_VALUE, fastDao.originaldocid(dbDocument));
				
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"maincomponentdetails"), ELEMENT_VALUE, fastDao.maincomponentdetails(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"minorcomponent1"), ELEMENT_VALUE, fastDao.minorcomponent1(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productfunctiondetails"), ELEMENT_VALUE, fastDao.productfunctiondetails(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"failurecode"), ELEMENT_VALUE, fastDao.failurecode(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productfunction"), ELEMENT_VALUE, fastDao.productfunction(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productdivision"), ELEMENT_VALUE, fastDao.productdivision(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productcategory"), ELEMENT_VALUE, fastDao.productcategory(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contenttopic"), ELEMENT_VALUE, fastDao.contenttopic(dbDocument));
				
				
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgacronym"), ELEMENT_VALUE, fastDao.cmg_acronym(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgfeedbackaddress"), ELEMENT_VALUE, fastDao.cmg_feedback_address(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"fulltitle"), ELEMENT_VALUE, fastDao.full_title(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"plannedpublicdate"), ELEMENT_VALUE, fastDao.planned_public_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"masterobjectname"), ELEMENT_VALUE, fastDao.master_object_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"customproductgroups"), ELEMENT_VALUE, fastDao.custom_product_groups(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqpmoids"), ELEMENT_VALUE, fastDao.getFaqProductList(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productline"), ELEMENT_VALUE, fastDao.getProductLines(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqproducthierarchy"), ELEMENT_VALUE, fastDao.getFaqProductHierarchy(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqcustomproductgroups"), ELEMENT_VALUE, fastDao.faq_custom_product_groups(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"jointproductcollections"), ELEMENT_VALUE, fastDao.joint_product_collections(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"extraproperties"), ELEMENT_VALUE, fastDao.extra_properties(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"components"), ELEMENT_VALUE, fastDao.components(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"failurecode"), ELEMENT_VALUE, fastDao.failure_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"bizdefinedproperties"), ELEMENT_VALUE, fastDao.biz_defined_properties(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"hasvalidproducts"), ELEMENT_VALUE, fastDao.has_valid_products(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentversiondate"), ELEMENT_VALUE, fastDao.content_version_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"validflag"), ELEMENT_VALUE, fastDao.valid_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cleancontentflag"), ELEMENT_VALUE, fastDao.clean_content_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"objectname"), ELEMENT_VALUE, fastDao.object_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"colid"), ELEMENT_VALUE, fastDao.col_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"disclosurelevel"), ELEMENT_VALUE, fastDao.disclosure_level(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentversion"), ELEMENT_VALUE, fastDao.content_version(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"dpiresolution"), ELEMENT_VALUE, fastDao.dpi_resolution(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"languagecode"), ELEMENT_VALUE, fastDao.language_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"creationdate"), ELEMENT_VALUE, fastDao.creation_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"originalfilename"), ELEMENT_VALUE, fastDao.original_filename(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"ownername"), ELEMENT_VALUE, fastDao.owner_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pixelheight"), ELEMENT_VALUE, fastDao.pixel_height(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pixelwidth"), ELEMENT_VALUE, fastDao.pixel_width(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"publicationpriority"), ELEMENT_VALUE, fastDao.publication_priority(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"documentclass"), ELEMENT_VALUE, fastDao.document_class(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"reviewdate"), ELEMENT_VALUE, fastDao.review_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"topissueexpiry"), ELEMENT_VALUE, fastDao.top_issue_expiry(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"topissueexpirydate"), ELEMENT_VALUE, fastDao.top_issue_expiry_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"filebytes"), ELEMENT_VALUE, fastDao.file_bytes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"costperissue"), ELEMENT_VALUE, fastDao.cost_per_issue(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"costperunit"), ELEMENT_VALUE, fastDao.cost_per_unit(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"laborhourspaid"), ELEMENT_VALUE, fastDao.labor_hours_paid(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"laborminutespaid"), ELEMENT_VALUE, fastDao.labor_minutes_paid(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"laborrateperhour"), ELEMENT_VALUE, fastDao.labor_rate_per_hour(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"materialcostperunit"), ELEMENT_VALUE, fastDao.material_cost_per_unit(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"actionexpirydate"), ELEMENT_VALUE, fastDao.action_expiry_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"actionstartdate"), ELEMENT_VALUE, fastDao.action_start_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"effectivitydate"), ELEMENT_VALUE, fastDao.effectivity_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentgroups"), ELEMENT_VALUE, fastDao.contentgroups(cgsdbObject));

				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentbody"), ELEMENT_VALUE, supportContent.get("content_body"));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"documentembeddedlink"), ELEMENT_VALUE, supportContent.get("embedded_links"));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"componentreuse"), ELEMENT_VALUE, supportContent.get("component_reuse"));


				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cdscontentlastModified"), ELEMENT_VALUE, fastDao.lastModified(dbObject));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"description"), ELEMENT_VALUE, fastDao.description(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"publicationdestinations"), ELEMENT_VALUE, fastDao.publicationDestinations(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"projectname"), ELEMENT_VALUE, fastDao.projectName(dbDocument));
								//SMO :getting company information from document in xml file......story number is 8004
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"company_info"), ELEMENT_VALUE, fastDao.getCompany_info(dbDocument));				
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"maincomponent"), ELEMENT_VALUE, fastDao.mainComponent(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"symptom"), ELEMENT_VALUE, fastDao.symptom(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"symptomdetails"), ELEMENT_VALUE, fastDao.symptomDetails(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqregions"), ELEMENT_VALUE, fastDao.faqRegions(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqcountries"), ELEMENT_VALUE, fastDao.faqCountries(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"minorcomponent1details"), ELEMENT_VALUE, fastDao.minorComponent1Details(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"minorcomponent2"), ELEMENT_VALUE, fastDao.minorComponent2(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"minorcomponent2details"), ELEMENT_VALUE, fastDao.minorComponent2Details(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"usertask"), ELEMENT_VALUE, fastDao.userTask(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"usertaskdetails"), ELEMENT_VALUE, fastDao.userTaskDetails(dbDocument));				
			}
		}

		logger.debug("Exiting createFASTXMLDocument to create support FAST xml");

		return XMLBuilder.getBytes(document);
	}

	private String getRegions(BasicDBList regionList)throws OptionsException, MongoUtilsException {
		StringBuffer regionsIDs= new StringBuffer();
		DBObject query = new BasicDBObject();
		query.put(REGION_NAME_FILED, new BasicDBObject("$in", regionList));
		Options options = new Options();
		options.setContentType(REGION_DB_NAME);
		options.setQuery(query);
		options.setDisplayFields(new BasicDBObject("_id",1));
		String regionCollection = ConfigurationManager.getInstance().getMappingValue(REGION_DB_NAME, REGION_COLLECTION_NAME);
		ArrayList<DBObject> regions = contentDao.getAllMetadata(regionCollection, options);

		for (DBObject regionDocument : regions) {
			regionsIDs.append(regionDocument.get("_id")).append(";");
		}
		return regionsIDs.toString();
	}

	public static void main(String[] args) throws ProcessException, MongoUtilsException, OptionsException, IOException{//System.setProperty("mongo.configuration","C:/SVN-CODE/Redesign-Tagging/2.0/mongo-mine.properties");
		//Mongo mongo = new Mongo("g2t1888c.austin.hp.com");

		/*DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("support");

		DBCollection metadata_temp = db.getCollection("metadata_staging");
		DBObject obj = new BasicDBObject();
		//"eventType" : "update" , "priority" : 1 , "lastModified" : 1374487506735}
		obj.put("_id", "c50328134");
		obj.put("priority", 1);
		obj.put("eventType", "update");
		obj.put("lastModified" , 1373956803456L);
		QueueManager queMgr= new QueueManager();
		queMgr.push(new ContentItem("marketingstandard",0L, queMgr));
		WorkItem item = new WorkItem("support", obj,queMgr);	
		//item.load();
		 */		
		//#############################################
		/*DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("support");
		DBCollection metadata_staging = db.getCollection("metadata_staging");*/

		Options options = new Options();
		options.setContentType("support");
		options.setDocid("c50341800");
		//options.setSubscription("fastxml");

		logger.debug(" Started preparing FAST xml for "+options.getDocid()+" ("+options.getContentType()+ ") Thread"+Thread.currentThread().getName());
		ContentDAO contentDao = new ContentDAO();
		DBObject dbObject = contentDao.getStagedMetadata(options);
		FastXMLDAO fastDao= new FastXMLDAO();
		if(dbObject!=null){
			DBObject dbDocument = (DBObject) dbObject.get(DOCUMENT_DOC);
			if(dbDocument != null){
				fastDao.owner_name(dbDocument);
				fastDao.description(dbDocument);
				fastDao.publicationDestinations(dbDocument);
				fastDao.projectName(dbDocument);
			}
		}
		//############################################
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("support");
		DBCollection metadata_staging = db.getCollection("metadata_staging");

		DBObject obj = new BasicDBObject();
		obj.put("_id", "c50341800"); //martin sent
		obj.put("priority", 4);
		obj.put("eventType", "update");
		obj.put("lastModified" , 1386915333238L);
		QueueManager queMgr= new QueueManager();
		queMgr.push(new ContentItem("support",0L, queMgr));
		WorkItem item = new WorkItem("support", obj,queMgr);

		//item.load();

		/*db = ConfigurationManager.getInstance().getMongoDBAuthenticated("supportcontent");
		BasicDBObject query = new BasicDBObject().append("filename", "c04459315.xml");*/

		SupportFastXMLCreationAdapter supportFastXMLCreationAdapter = new SupportFastXMLCreationAdapter();
		supportFastXMLCreationAdapter.evaluate(item);
		//System.out.println(metadata_temp.findOne(new BasicDBObject("_id","c50328134")));}

}
}
