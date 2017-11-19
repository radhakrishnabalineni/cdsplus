package com.hp.cdsplus.processor.adapters;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.FastXMLDAO;
import com.hp.cdsplus.dao.Options;
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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MarketingFastXMLCreationAdapter implements Adapter {

	private MongoAPIUtils mongoUtils = new MongoAPIUtils();	
	private ContentDAO contentDao;
	private FastXMLDAO fastDao;
	public static final String ELEMENT_NAME = "element";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ELEMENT_VALUE = "value";

	private static final String DOCUMENT_DOC = "document";
	private static final String REGION_NAME_FILED = "region.name";
	public static final String REGION_DB_NAME = "region";
	public static final String REGION_COLLECTION_NAME = "metadataLiveCollection";
	private static final String REGION_DOC = "region";
	private static final Logger logger = LogManager.getLogger(MarketingFastXMLCreationAdapter.class);

	@Override
	public void evaluate(Item item) throws OptionsException,
	MongoUtilsException {
		WorkItem wItem = (WorkItem) item;

		byte[] fastXMLBytes= createFASTXMLDocument(item);
		mongoUtils.writeFASTXMLContent(wItem.getContentType(),wItem.getId(), fastXMLBytes);
	}

	private byte[] createFASTXMLDocument(Item item) throws MongoUtilsException, OptionsException {
		logger.debug("Entering createFASTXMLDocument to create marketingstandard FAST xml");
		Element rootElement = null;
		Document document = null;
		document = DocumentHelper.createDocument();
		rootElement = XMLBuilder.getDocumentElement(document);

		WorkItem wItem = (WorkItem) item;

		contentDao = new ContentDAO();	
		fastDao= new FastXMLDAO();

		Options options = new Options();
		options.setContentType(wItem.getContentType());
		options.setDocid(wItem.getId());

		logger.debug(" Started preparing FAST xml for "+options.getDocid()+" ("+options.getContentType()+ ") Thread"+Thread.currentThread().getName());

		DBObject dbObject = contentDao.getTempMetadata(options);

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
				if(fastDao.existsProducts(dbDocument))
					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pmoids"), ELEMENT_VALUE, fastDao.getProductList(dbDocument));
				if(fastDao.existsFaqProducts(dbDocument))
					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"faqpmoids"), ELEMENT_VALUE, fastDao.getFaqProductList(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productline"), ELEMENT_VALUE, fastDao.getProductLines(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"producthierarchy"), ELEMENT_VALUE, fastDao.getMSProductHierarchy(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"documenttype"), ELEMENT_VALUE, fastDao.document_type(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"versionlabel"), ELEMENT_VALUE, fastDao.version_label(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsdocument"), ELEMENT_VALUE, fastDao.eds_document(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsclienttype"), ELEMENT_VALUE, fastDao.eds_client_type(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsclientquote"), ELEMENT_VALUE, fastDao.eds_client_quot(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsgoldstandardflag"), ELEMENT_VALUE, fastDao.eds_gold_standard_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"addrcountrycode"), ELEMENT_VALUE, fastDao.addr_country_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"businessgroup"), ELEMENT_VALUE, fastDao.business_group(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgname"), ELEMENT_VALUE, fastDao.cmg_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"colmasterid"), ELEMENT_VALUE, fastDao.col_master_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionrefupdatedate"), ELEMENT_VALUE, fastDao.collection_ref_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionupdatedate"), ELEMENT_VALUE, fastDao.collection_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionupdateuser"), ELEMENT_VALUE, fastDao.collection_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionvalidflag"), ELEMENT_VALUE, fastDao.collection_valid_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentclass"), ELEMENT_VALUE, fastDao.content_class(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"futuredisclosuredate"), ELEMENT_VALUE, fastDao.future_disclosure_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"ichronicleid"), ELEMENT_VALUE, fastDao.i_chronicle_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"inventorymanagement"), ELEMENT_VALUE, fastDao.inventory_management(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"lifecyclestateupdatedate"), ELEMENT_VALUE, fastDao.lifecycle_state_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"lifecyclestateupdateuser"), ELEMENT_VALUE, fastDao.lifecycle_state_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"objectcreatedate"), ELEMENT_VALUE, fastDao.object_create_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"ownername"), ELEMENT_VALUE, fastDao.owner_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"projectid"), ELEMENT_VALUE, fastDao.project_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"projectname"), ELEMENT_VALUE, fastDao.project_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubmedia"), ELEMENT_VALUE, fastDao.pub_media(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubrefupdatedate"), ELEMENT_VALUE, fastDao.pub_ref_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubpagesize"), ELEMENT_VALUE, fastDao.pub_page_size(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contenturl"), ELEMENT_VALUE, fastDao.content_url(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"printmanagementprograms"), ELEMENT_VALUE, fastDao.printmanagementprograms(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"originaldocid"), ELEMENT_VALUE, fastDao.originaldocid(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsclientquote"), ELEMENT_VALUE, fastDao.edsclientquote(dbDocument));

				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"propertyupdateuser"), ELEMENT_VALUE, fastDao.property_update_user(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"robjecttype"), ELEMENT_VALUE, fastDao.r_object_type(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"rversionlabels"), ELEMENT_VALUE, fastDao.r_version_labels(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"edsbusinesstopics"), ELEMENT_VALUE, fastDao.eds_business_topics(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"regions"), ELEMENT_VALUE, getRegions(fastDao.regions(dbDocument)));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"regionslabel"), ELEMENT_VALUE, fastDao.regionslabel(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"countries"), ELEMENT_VALUE, fastDao.countries(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"authors"), ELEMENT_VALUE, fastDao.authors(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"searchkeywords"), ELEMENT_VALUE, fastDao.search_keywords(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"crmassetcode"), ELEMENT_VALUE, fastDao.crm_asset_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgacronym"), ELEMENT_VALUE, fastDao.cmg_acronym(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"cmgfeedbackaddress"), ELEMENT_VALUE, fastDao.cmg_feedback_address(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"privatedocumentflag"), ELEMENT_VALUE, fastDao.private_document_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"masterobjectname"), ELEMENT_VALUE, fastDao.master_object_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"languagelabel"), ELEMENT_VALUE, fastDao.language_label(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"fulltitle"), ELEMENT_VALUE, fastDao.full_title(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionfulltitle"), ELEMENT_VALUE, fastDao.collection_full_title(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"propertyupdatedate"), ELEMENT_VALUE, fastDao.property_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentupdatedate"), ELEMENT_VALUE, fastDao.content_update_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectionsearchkeywords"), ELEMENT_VALUE, fastDao.collection_search_keywords(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"productlevels"), ELEMENT_VALUE, fastDao.product_levels(dbDocument));

				BasicDBList audienceList = fastDao.audiences(dbDocument);	

				for (Object object : audienceList) {

					BasicDBObject audience=(BasicDBObject)object;

					String audience_name = audience.get("audience_name").toString();
					String audience_release_date = audience.get("audience_release_date").toString();
					String audience_expiry_date = audience.get("audience_expiry_date").toString();

					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"audiencename"+audience_name.toLowerCase()), ELEMENT_VALUE, audience_name);
					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"audiencereleasedate"+audience_name.toLowerCase()), ELEMENT_VALUE, audience_release_date);
					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"audienceexpirydate"+audience_name.toLowerCase()), ELEMENT_VALUE, audience_expiry_date);

				}

				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"extraproperties"), ELEMENT_VALUE, fastDao.extra_properties(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubcontroller"), ELEMENT_VALUE, fastDao.pub_controller(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubmanager"), ELEMENT_VALUE, fastDao.pub_manager(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubmanagerbackup"), ELEMENT_VALUE, fastDao.pub_manager_backup(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"businessunits"), ELEMENT_VALUE, fastDao.business_units(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"organization"), ELEMENT_VALUE, fastDao.organization(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"smartflowcontenttypes"), ELEMENT_VALUE, fastDao.smartflow_content_types(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubvendordetails"), ELEMENT_VALUE, fastDao.pub_vendor_details(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"renditions"), ELEMENT_VALUE, fastDao.renditions(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"colid"), ELEMENT_VALUE, fastDao.col_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contenturl"), ELEMENT_VALUE, fastDao.contenturl(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentversiondate"), ELEMENT_VALUE, fastDao.content_version_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"description"), ELEMENT_VALUE, fastDao.description(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"disclosurelevel"), ELEMENT_VALUE, fastDao.disclosure_level(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"contentversion"), ELEMENT_VALUE, fastDao.content_version(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"feedbackaddress"), ELEMENT_VALUE, fastDao.feedback_address(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"documentclass"), ELEMENT_VALUE, fastDao.document_class(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"languagecode"), ELEMENT_VALUE, fastDao.language_code(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"concentrainternalid"), ELEMENT_VALUE, fastDao.concentra_internal_id(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"creationdate"), ELEMENT_VALUE, fastDao.creation_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"objectname"), ELEMENT_VALUE, fastDao.object_name(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"originalfilename"), ELEMENT_VALUE, fastDao.original_filename(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubairprogramflag"), ELEMENT_VALUE, fastDao.pub_air_program_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubflag"), ELEMENT_VALUE, fastDao.pub_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubinventoryscrapcount"), ELEMENT_VALUE, fastDao.pub_inventory_scrap_count(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"pubpagecount"), ELEMENT_VALUE, fastDao.pub_page_count(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"reviewdate"), ELEMENT_VALUE, fastDao.review_date(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"collectiondescription"), ELEMENT_VALUE, fastDao.collection_description(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"corporatecontentflag"), ELEMENT_VALUE, fastDao.corporate_content_flag(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"informationsource"), ELEMENT_VALUE, fastDao.information_source(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"customersegmentcodes"), ELEMENT_VALUE, fastDao.customer_segment_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"businessfunctioncodes"), ELEMENT_VALUE, fastDao.business_function_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"buyerrolecodes"), ELEMENT_VALUE, fastDao.buyer_role_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"customercodes"), ELEMENT_VALUE, fastDao.customer_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"employeecountcodes"), ELEMENT_VALUE, fastDao.employee_count_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"industrysegmentcodes"), ELEMENT_VALUE, fastDao.industry_segment_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"industryverticalcodes"), ELEMENT_VALUE, fastDao.industry_vertical_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"itbudgetcodes"), ELEMENT_VALUE, fastDao.it_budget_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"jobresponsibilitycodes"), ELEMENT_VALUE, fastDao.job_responsibility_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"marketingprogramcodes"), ELEMENT_VALUE, fastDao.marketing_program_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"officecountcodes"), ELEMENT_VALUE, fastDao.office_count_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"operatingsystemcodes"), ELEMENT_VALUE, fastDao.operating_system_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"partnercodes"), ELEMENT_VALUE, fastDao.partner_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"technicalitylevelcodes"), ELEMENT_VALUE, fastDao.technicality_level_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"webnewslettercodes"), ELEMENT_VALUE, fastDao.web_newsletter_codes(dbDocument));
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"partnersegmentcodes"), ELEMENT_VALUE, fastDao.partner_segment_codes(dbDocument));
				//SMO :getting company information from document in xml file......story number is 7554
				XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"company_info"), ELEMENT_VALUE, fastDao.getCompany_info(dbDocument));
					XMLBuilder.addXMLElement(XMLBuilder.addXMLElementWithAttribute(rootElement,ELEMENT_NAME,ATTRIBUTE_NAME,"originalsystem"), ELEMENT_VALUE, fastDao.originalSystem(dbDocument));
			}
		}
		logger.debug("Exiting createFASTXMLDocument to create marketingstandard FAST xml");
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

	public static void main(String[] args) throws ProcessException, MongoUtilsException, OptionsException, UnknownHostException{
		System.setProperty("mongo.configuration","C:/SVN-CODE/Redesign-Tagging/2.0/mongo-mine.properties");
		//Mongo mongo = new Mongo("g2t1888c.austin.hp.com");

		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("marketingstandard");

		DBCollection metadata_temp = db.getCollection("metadata_temp");
		//DBObject obj = new BasicDBObject();

		//"eventType" : "update" , "priority" : 1 , "lastModified" : 1374487506735}

		DBCursor cursor = metadata_temp.find();
		MarketingFastXMLCreationAdapter msFastXMLCreationAdapter = new MarketingFastXMLCreationAdapter();

		/*obj.put("_id", "c50324743");
		obj.put("priority", 1);
		obj.put("eventType", "update");
		obj.put("lastModified" , 1373956803456L);*/
		QueueManager queMgr= new QueueManager();
		queMgr.push(new ContentItem("marketingstandard",0L, queMgr));
		while(cursor.hasNext()){

			WorkItem item = new WorkItem("marketingstandard", cursor.next(),queMgr);	
			msFastXMLCreationAdapter.evaluate(item);
		}
	}
}

