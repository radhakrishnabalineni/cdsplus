package com.hp.concentra.extractor.documents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.tree.BaseElement;
import org.xml.sax.SAXException;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfTime;
import com.hp.cdspDestination.ProjContent;
import com.hp.cks.concentra.core.content.ContentOut;
import com.hp.cks.concentra.core.document.CBaseObject;
import com.hp.cks.concentra.core.document.CBaseObjectCreator;
import com.hp.cks.concentra.core.document.CDocument;
import com.hp.cks.concentra.core.document.CSupportDoc;
import com.hp.cks.concentra.core.document.ComponentBean;
import com.hp.cks.concentra.core.document.ConcentraConstants;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.core.session.SessionLog;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.StringOps;
import com.hp.concentra.DQLParameter;
import com.hp.concentra.PreparedDQL;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.IDfTimeConcentra;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.Result;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;
import com.hp.loader.priorityLoader.ProcessingException;


/**
 * Abstract class that models the operations necessary for the creation of a
 * Concentra document
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public abstract class ConcentraDoc {
  private static final String QUERY_COLUMN_CONTENT_TYPE = "content_type";
  // TR132010 : Begin.
  private static final String QUERY_COLUMN_BUSINESS_UNIT = "business_unit";
  private static final String QUERY_COLUMN_R_VERSION_LABEL = "r_version_label";
  private static final String QUERY_COLUMN_DOCUMENT_TYPE_DETAIL = "document_type_detail";
  private static final String QUERY_COLUMN_PRINT_MANAGEMENT_PROGRAM = "print_management_program";
  private static final String QUERY_COLUMN_EDS_BUSINESS_ISSUE = "business_issue";
  private static final String QUERY_COLUMN_EDS_MARKET_TECHNOLOGY_TREND = "market_technology_trend";
  // TR132010 : End.
  private static final String C_LIBRARY_DOC_DTD = "c_library_doc.dtd";
  private static final String C_CONTENT_FEEDBACK_DOC_DTD = "c_content_feedback_doc_dtd";
  private static final String C_HHO_MARKETING_DOC_DTD = "c_hho_marketing_doc.dtd";
  private static final String C_SUPPORT_DOC_DTD = "c_support_doc.dtd";

  private static final String ALL_CONTENT = "all";
  private static final String ATTRIBUTE_SIZE = "size";
  private static final String ATTRIBUTE_LAST_UPDATED_DATE = "last_updated_date";
  protected static final String ATTRIBUTE_CONTENT_TYPE = QUERY_COLUMN_CONTENT_TYPE;
  private static final String CONTENT_SIZE = "content_size";
  private static final String SET_TIME = "set_time";
  private static final String DOS_EXTENSION = "dos_extension";
  private static final String ELEMENT_RENDITION = "rendition";
  private static final String ELEMENT_RENDITIONS = "renditions";
  protected static final String TABLE_C_BASE_OBJECT = "c_base_object";
  private static final String INLINE_DOC_NAME = "INLINE";
  private static final String PUBLIC = "public";
  private static final String FUTURE_DISCLOSURE_MODE = "future_disclosure_mode";
  private static final String FUTURE_DISCLOSURE_LEVEL = "future_disclosure_level";
  private static final String DISCLOSURE_LEVEL = "disclosure_level";
  private static final String WEB_RELEASE_DATE = "web_release_date";
  private static final String PRODUCT_ANNOUNCEMENT_DATE = "product_announcement_date";
  private static final String PRODUCT_RELEASE_DATE = "product_release_date";
  private static final String RESULT_MULTI = "MULTI";
  protected static final String EMPTY_STRING = "";
  private static final String ELEMENT_ACCOUNT = "account";
  private static final String ELEMENT_ACCOUNTS = "accounts";
  private static final String ELEMENT_PRODUCT_LEVEL = "product_level";
  private static final String ELEMENT_PRODUCT_LEVELS = "product_levels";
  private static final String ELEMENT_DOCUMENT_TYPE_DETAILS = "document_type_details";
  private static final String ELEMENT_DOCUMENT_TYPE_DETAIL = "document_type_detail";
  private static final String ELEMENT_PROPERTY_UPDATE_DATE = "property_update_date";
  private static final String ELEMENT_COMPONENTS = "components";
  protected static final String ELEMENT_COMPONENT = "component";
  private static final String ELEMENT_MARKETING_TEAM = "marketing_team";
  private static final String ELEMENT_MARKETING_TEAMS = "marketing_teams";
  private static final String ELEMENT_PUBLICATION_DESTINATIONS = "publication_destinations";
  private static final String ELEMENT_PUBLICATION_DESTINATION = "publication_destination";
  private static final String ELEMENT_R_LINK_CNT = "r_link_cnt";
  private static final String ELEMENT_AUDIENCES = "audiences";
  private static final String ELEMENT_AUDIENCE = "audience";
  private static final String ELEMENT_AUDIENCE_NAME = "audience_name";
  private static final String ELEMENT_AUDIENCE_RELEASE_DATE = "audience_release_date";
  private static final String ELEMENT_AUDIENCE_EXPIRY_DATE = "audience_expiry_date";
  private static final String ELEMENT_CMG_ACRONYM = "cmg_acronym";
  private static final String ELEMENT_CMG_FEEDBACK_ADDRESS = "cmg_feedback_address";
  private static final String ELEMENT_COLLECTION_VALID_FLAG = "collection_valid_flag";
  private static final String ELEMENT_FULL_TITLE = "full_title";
  private static final String ELEMENT_FULL_TITLES = "full_titles";
  protected static final String ELEMENT_FILE_NAME = "file_name";
  protected static final String ELEMENT_FILE_BYTES = "file_bytes";
  private static final String ELEMENT_EXTRA_PROPERTIES = "extra_properties";
  private static final String ELEMENT_EXTRA_PROPERTY = "extra_property";
  private static final String ELEMENT_EXTRA_PROPERTY_NAME = "extra_property_name";
  private static final String ELEMENT_EXTRA_PROPERTY_VALUES = "extra_property_values";
  private static final String ELEMENT_EXTRA_PROPERTY_VALUE = "extra_property_value";
  private static final String ELEMENT_INFORMATION_SOURCE = "information_source";
  private static final String ELEMENT_LANGUAGE_CODES = "language_codes";
  private static final String ELEMENT_LANGUAGE_LABEL = "language_label";
  private static final String ELEMENT_MASTER_OBJECT_NAME = "master_object_name";
  private static final String ELEMENT_PLANNED_PUBLIC_DATE = "planned_public_date";
  private static final String ELEMENT_PRODUCTS = "products";
  private static final String ELEMENT_PUBLICATION_CODE = "publication_code";
  private static final String ELEMENT_SEARCH_KEYWORDS = "search_keywords";
  private static final String ELEMENT_SEARCH_KEYWORD = "search_keyword";
  private static final String ELEMENT_BUSINESS_UNITS = "business_units";
  private static final String ELEMENT_BUSINESS_UNIT = "business_unit";
  private static final String ELEMENT_ORGANIZATION = "organization";
  private static final String ELEMENT_CONTENT_TYPES = "smartflow_content_types";
  private static final String ELEMENT_CONTENT_TYPE = "smartflow_content_type";
  private static final String ELEMENT_OBJECT_ID = "r_object_id";
  // TR132010 : Begin.
  private static final String ELEMENT_CONTENT_UPDATE_USER = "content_update_user";
  private static final String ELEMENT_FUTURE_DISCLOSURE_DATE = "future_disclosure_date";
  private static final String ELEMENT_FUTURE_DISCLOSURE_LEVEL = "future_disclosure_level";
  private static final String ELEMENT_IS_TRANSLATION_FLAG = "is_translation_flag";
  private static final String ELEMENT_LIFECYCLE_STATE_UPDATE_DATE = "lifecycle_state_update_date";
  private static final String ELEMENT_LIFECYCLE_STATE_UPDATE_USER = "lifecycle_state_update_user";
  private static final String ELEMENT_PROPERTY_UPDATE_USER = "property_update_user";
  private static final String ELEMENT_CMG_NAME = "cmg_name";
  private static final String ELEMENT_COL_MASTER_ID = "col_master_id";
  private static final String ELEMENT_COLLECTION_REF_UPDATE_DATE = "collection_ref_update_date";
  private static final String ELEMENT_COLLECTION_UPDATE_DATE = "collection_update_date";
  private static final String ELEMENT_COLLECTION_UPDATE_USER = "collection_update_user";
  private static final String ELEMENT_PRODUCT_ANNOUNCEMENT_DATE = "product_announcement_date";
  private static final String ELEMENT_PRODUCT_RELEASE_DATE = "product_release_date";
  private static final String ELEMENT_WEB_RELEASE_DATE = "web_release_date";
  private static final String ELEMENT_I_CHRONICLE_ID = "i_chronicle_id";
  private static final String ELEMENT_ADDR_COUNTRY_CODE = "addr_country_code";
  private static final String ELEMENT_BUSINESS_GROUP = "business_group";
  private static final String ELEMENT_CONTENT_CLASS = "content_class";
  private static final String ELEMENT_INVENTORY_MANAGEMENT = "inventory_management";
  private static final String ELEMENT_OBJECT_CREATE_DATE = "object_create_date";
  private static final String ELEMENT_OWNER_NAME = "owner_name";
  private static final String ELEMENT_PRINT_MANAGEMENT_PROGRAMS = "print_management_programs";
  private static final String ELEMENT_PRINT_MANAGEMENT_PROGRAM = "print_management_program";
  private static final String ELEMENT_PROJECT_ID = "project_id";
  private static final String ELEMENT_PROJECT_NAME = "project_name";
  private static final String ELEMENT_R_OBJECT_TYPE = "r_object_type";
  private static final String ELEMENT_R_VERSION_LABELS = "r_version_labels";
  private static final String ELEMENT_R_VERSION_LABEL = "r_version_label";
  private static final String ELEMENT_TASK_DUE_DATE = "task_due_date";
  private static final String ELEMENT_EDS_DOCUMENT = "eds_document";
  private static final String ELEMENT_EDS_CLIENT_TYPE = "eds_client_type";
  private static final String ELEMENT_EDS_CLIENT_QUOTE = "eds_client_quote";
  private static final String ELEMENT_EDS_GOLD_STANDARD_FLAG = "eds_gold_standard_flag";
  //CR840/BR603977  Changes in EDS document properties
  private static final String ELEMENT_EDS_BUSINESS_ISSUES = "eds_business_topics";
  private static final String ELEMENT_EDS_BUSINESS_ISSUE = "eds_business_topic";
  //CR840/BR603977  Changes in EDS document properties
 // private static final String ELEMENT_EDS_MARKET_TECHNOLOGY_TRENDS = "eds_market_technology_trends";
 // private static final String ELEMENT_EDS_MARKET_TECHNOLOGY_TREND = "eds_market_technology_trend";
  // TR132010 : End.

  // Reduce Latency and Document State Management projects
  private static final String ELEMENT_ACTIVE_FLAG = "active_flag";

  private static final int INITIAL_INDEX = 0;
  private static final int ADJUSTMENT = 1;
  private static final char UNDERSCORE_CHAR = '_';
  private static final String TABLE_COL = "col";
  protected static final String XML_EXTENSION = "xml";
  private static final String PROPERTY_FILE = "property.xml";
  private static final String CONTENT_DIRECTORY = "content/";
  private static final String DOCUMENT_ROOT = "document";
  // private static final String NAMESPACE_PREFIX = "ens";
  private static final String QUERY_GET_ACCOUNTS = "getAccounts";
  private static final String QUERY_GET_ACRONYM = "getAcronym";
  private static final String QUERY_GET_AUDIENCES = "getAudiences";
  private static final String QUERY_GET_AUTHORS = "getAuthors";
  private static final String QUERY_GET_BUSINESS_UNITS = "getBusinessUnit";
  private static final String QUERY_GET_DOCUMENT_TYPE_DETAILS = "getDocumentTypeDetails";

  protected static final String QUERY_GET_PRODUCT_GROUPS = "getProductGroups";
  private static final String QUERY_GET_DOCUMENT_TYPE = "getDocumentType";
  private static final String QUERY_GET_REGIONS = "getRegions";
  private static final String QUERY_GET_VERSION_LABEL = "getVersionLabel";
  // private static final String QUERY_SHOULD_EXTRACT_BASED_ON_CHANGE_LOG =
  // "shouldExtractBasedOnChangeLog";
  // private static final String QUERY_SHOULD_EXTRACT_BASED_ON_CHRONICLE =
  // "shouldExtractBasedOnChronicle";
  private static final String QUERY_GET_LANGUAGE_LABEL = "getLanguageLabel";
  private static final String QUERY_GET_INFORMATION_SOURCE = "getInformationSource";
  private static final String QUERY_GET_DOS_EXTENSION = "getDosExtension";
  private static final String QUERY_GET_PROPERTY_UPDATE_DATE = "getPropertyUpdateDate";
  private static final String QUERY_GET_FULL_TITLE = "getFullTitle";
  private static final String QUERY_GET_FULL_TITLES = "getFullTitles";
  private static final String QUERY_GET_MARKETING_TEAMS = "getMarketingTeams";
  private static final String QUERY_PRODUCT_LEVELS = "getProductLevels";
  private static final String QUERY_GET_PUBLICATION_DESTINATION = "getPublicationDestination";
  private static final String QUERY_GET_SEARCH_KEYWORDS = "getSearchKeywords";
  private static final String QUERY_GET_MASTER_OBJECT_NAME = "getMasterObjectName";
  private static final String QUERY_GET_FEEDBACK_ADDRESS = "getFeedbackAddress";
  private static final String QUERY_GET_COLLECTION_VALID_FLAG = "getCollectionValidFlag";
  private static final String QUERY_GET_PUBLICATION_CODE = "getPublicationCode";
  private static final String QUERY_GET_CONTENT_UPDATE_DATE = "getContentUpdateDate";
  private static final String QUERY_GET_LANGUAGE_CODE = "getLanguageCode";
  private static final String QUERY_GET_LANGUAGE_CODES_MULTI = "getLanguageCodesMulti";
  private static final String QUERY_GET_LANGUAGE_CODES_SINGLE = "getLanguageCodesSingle";
  private static final String QUERY_GET_EXTRA_PROPERTIES = "getExtraPoperties";
  protected static final String QUERY_GET_COMPONENT = "getComponent";
  private static final String QUERY_GET_PLANNED_PUBLIC_DATE = "getPlannedPublicDate";
  private static final String QUERY_GET_RENDITIONS = "getRendition";
  private static final String QUERY_GET_RENDITIONS_COUNT_ZERO = "getRenditionCountZero";
  private static final String QUERY_GET_LINK_COUNT = "getLinkCount";
  private static final String QUERY_GET_PRODUCT_GROUPS_MEMBERS = "getProductGroupMembers";
  private static final String QUERY_GET_ORGANIZATION = "getOrganization";
  private static final String QUERY_GET_CONTENT_TYPE = "getContentType";
  // TR132010 : Begin.
  private static final String QUERY_GET_CONTENT_UPDATE_USER = "getContentUpdateUser";
  private static final String QUERY_GET_FUTURE_DISCLOSURE_DATE = "getFutureDisclosureDate";
  private static final String QUERY_GET_FUTURE_DISCLOSURE_LEVEL = "getFutureDisclosureLevel";
  private static final String QUERY_GET_IS_TRANSLATION_FLAG = "getIsTranslationFlag";
  private static final String QUERY_GET_LIFECYCLE_STATE_UPDATE_DATE = "getLifecycleStateUpdateDate";
  private static final String QUERY_GET_LIFECYCLE_STATE_UPDATE_USER = "getLifecycleStateUpdateUser";
  private static final String QUERY_GET_PROPERTY_UPDATE_USER = "getPropertyUpdateUser";
  private static final String QUERY_GET_CMG_NAME = "getCmgName";
  private static final String QUERY_GET_COL_MASTER_ID = "getColMasterId";
  private static final String QUERY_GET_COLLECTION_REF_UPDATE_DATE = "getCollectionRefUpdateDate";
  private static final String QUERY_GET_COLLECTION_UPDATE_DATE = "getCollectionUpdateDate";
  private static final String QUERY_GET_COLLECTION_UPDATE_USER = "getCollectionUpdateUser";
  private static final String QUERY_GET_PRODUCT_ANNOUNCEMENT_DATE = "getProductAnnouncementDate";
  private static final String QUERY_GET_PRODUCT_RELEASE_DATE = "getProductReleaseDate";
  private static final String QUERY_GET_WEB_RELEASE_DATE = "getWebReleaseDate";
  private static final String QUERY_GET_I_CHRONICLE_ID = "getIChronicleId";
  private static final String QUERY_GET_ADDR_COUNTRY_CODE = "getAddrCountryCode";
  private static final String QUERY_GET_BUSINESS_GROUP = "getBusinessGroup";
  private static final String QUERY_GET_CONTENT_CLASS = "getContentClass";
  private static final String QUERY_GET_INVENTORY_MANAGEMENT = "getInventoryManagement";
  private static final String QUERY_GET_OBJECT_CREATE_DATE = "getObjectCreateDate";
  private static final String QUERY_GET_OWNER_NAME = "getOwnerName";
  private static final String QUERY_GET_PRINT_MANAGEMENT_PROGRAM = "getPrintManagementProgram";
  private static final String QUERY_GET_PROJECT_ID = "getProjectId";
  private static final String QUERY_GET_PROJECT_NAME = "getProjectName";
  private static final String QUERY_GET_R_OBJECT_TYPE = "getRObjectType";
  private static final String QUERY_GET_R_VERSION_LABELS = "getRVersionLabels";
  private static final String QUERY_GET_TASK_DUE_DATE = "getTaskDueDate";
  private static final String QUERY_GET_EDS_DOCUMENT = "getEdsDocument";
  private static final String QUERY_GET_EDS_CLIENT_TYPE = "getEdsClientType";
  private static final String QUERY_GET_EDS_CLIENT_QUOTE = "getEdsClientQuote";
  private static final String QUERY_GET_EDS_GOLD_STANDARD_FLAG = "getEdsGoldStandardFlag";
  private static final String QUERY_GET_EDS_BUSINESS_ISSUES = "getEdsBusinessIssues";
  private static final String QUERY_GET_EDS_MARKET_TECHNOLOGY_TRENDS = "getEdsMarketTechnologyTrends";
  private static final String JOINT_PRODUCT_COLLECTIONS = "joint_product_collections";
  private static final String JOINT_PRODUCT_COLLECTION = "joint_product_collection";

  private static final String QUERY_GET_JPC_IDS = "getJointProductCollectionIds";
  private static final String QUERY_GET_PRODUCTNUMBER_INFO = "getProductNumberInfo";

  //Taxonomy Changes
  private static final String QUERY_COLUMN_TMS_CATEGORIZATION_TOPIC = "tms_categorization_topics";
  private static final String ELEMENT_TMS_CATEGORIZATION_TOPICS = "taxonomy_categorizations";
  private static final String ELEMENT_TMS_CATEGORIZATION_TOPIC = "taxonomy_categorization";
  private static final String ELEMENT_TMS_CATEGORIZATION_KEY = "categorization_key";
  private static final String ELEMENT_TMS_CATEGORIZATION_VALUE = "categorization_value";
  private static final String QUERY_GET_TMS_CATEGORIZATION_TOPICS = "getTmsCategorizationTopics";
  //End of Changes 

  // TR132010 : End.

  // Constants use to resolve product groups
  protected static final String CPG_PREFIX_ONE = "PMPIG";
  protected static final String CPG_PREFIX_TWO = "PMPI_";
  protected static final String CPG_R_OBJECT_ID_BEGINNING = "00";
  private static final String INCOMPLETE_PRODUCT_GROUP_TEMP = "PMN_T_";
  private static final String INCOMPLETE_PRODUCT_GROUP_PERM = "PMN_P_";
  protected static final int R_OBJECT_ID_LENGTH = 16;

  // Constants for planned public date
  private static final String FUTURE_DISCLORE_DATE = "future_disclosure_date";

  protected ConcentraAdminSession docbaseSession = null;
  protected String objectId;
  protected String objectType;
  protected String token;
  protected String contentType;
  protected String objectName;
  protected String chronicleId;
  protected String colId;
  protected int priority;
  // Active flag of the documents (DSM)
  protected String activeFlag;

  private Set<String> docProdGroups;
  // place holders for keeping the document_type_details and contentType fields
  private String docType;

  private Vector fileResults = new Vector();

  //SMO : : US #7469
  private static final String QUERY_GET_COMPANY = "getCompanyName";
  private String company;
  
  public Set<String> getDocProdGroups() {
    return docProdGroups;
  }

  /**
   * saveValue catches properties that are needed later
   * @param value
   * @param element
   */
  private void saveValue(String value, String element) {
    if (element == ExtractorConstants.ELEMENT_DOCUMENT_TYPE) {
      docType = value;
    } 
  }
  
  public void setDocProdGroups(Set<String> docProdGroups) {
    this.docProdGroups = docProdGroups;
  }

  /***************************************************************************
   * Class constructor that initializes the common information for all doc
   * types
   * 
   * @param objectId
   * @param chronicleId
   */
  public ConcentraDoc(String objectId, String chronicleId, String token, String objectType, String colId,
      int priority, String activeFlag) {
    this.objectId = objectId;
    this.chronicleId = chronicleId;
    this.token = token;
    this.objectType = objectType;
    this.colId = colId;
    this.priority = priority;
    this.activeFlag = activeFlag;
  }

  /***************************************************************************
   * Main method used to request the extraction of the specified document
   * 
   * @return ByteArrayOutputStream with all the document information
   * @throws IOException 
   * @throws SupportCommMergerException 
   * @throws DmRepositoryException 
   * @throws DfException 
   * @throws ProcessingException 
   * @throws SAXException 
   * @throws DocumentException 
   */
  public boolean extract(ConcentraExtractElement extractElem) throws DfException, DmRepositoryException, SupportCommMergerException, IOException, ProcessingException, DocumentException, SAXException {
    LoaderLog.info("ConcentraDoc extract");
    
    Document propertyDoc = getPropertyFile();
    if (propertyDoc == null) {
      // This document failed to extract
      LoaderLog.error("Property file failed to extract.  Giving up");
      return false;
    }
    getDocumentContent(extractElem);
    //SMO : 
    this.company = getCompanyName(objectId);
  
    // write the properties to a byte[]
    String contentType = extractElem.getCDSPlusContentType();
    StringBuffer projPath = new StringBuffer(contentType);
    // put the properties in as the first to be extracted
    ProjContent propertyProjContent = new ProjContent(contentType, extractElem.getObjectName()+ConcentraExtractElement.XML_EXTENSION, 
        ConcentraExtractElement.getBytes(propertyDoc));
    // set up path for property file <contentType>/loader/objectName
    projPath.append(ConcentraExtractElement.LOADER);
    projPath.append( ProjContent.normalizeContentNameForUrl( propertyProjContent.getExtentionlessContentName() ) );
    propertyProjContent.setPath( projPath.toString() );
    extractElem.addProjContent(propertyProjContent);      
    //SMO : 
    propertyProjContent.setCompany(this.company);
    // add the ProjContent for loading the content documents
    projPath.setLength(0);
    projPath.append(extractElem.getCDSPlusContentType());
    
    int contentLen = 0;
    // find and create the primary content.  This also determines if it is going under the property path, or <contentType>content path
    for (int i=0; i < fileResults.size(); i++) {
      ContentOut.ContentOutFileInfo fi = (ContentOut.ContentOutFileInfo)fileResults.get(i);
      if (fi.getType() == ContentOut.ContentOutFileInfo.OBJECT_FILE) {
        File file = new File(fi.getFile());
        if (!file.exists()) {
          throw new ProcessingException("Checkout of content failed for file: "+file.toString(), false);
        }
		

        if (fi.getFile().endsWith(XML_EXTENSION)) {
          // add the content/loader/ to the path as it is xml
          projPath.append(ConcentraExtractElement.CONTENT);
          // main file is put it in <content_type>content/loader/<objectName>/
          // this is the main XML file.  You have to read the bytes for the ProjContent as it can't take it directly from the disk
          // if the content is going into the loader space, it needs to be put in as binary so the name doesn't have the extension stripped
          
          ProjContent newContent = new ProjContent(contentType, file.getName(), ConcentraExtractElement.getBytes(file));
          projPath.append(ProjContent.normalizeContentNameForUrl(newContent.getExtentionlessContentName()));
          newContent.setPath(projPath.toString());
          //SMO : 
          newContent.setCompany(this.company);
          extractElem.addProjContent(newContent);
          // set the contentPath to be under the xml entry
          projPath.append(ConcentraExtractElement.file_separator);
          // secondary files go under the XML primary file
          contentLen = projPath.length();
        } else {

          // main file is put it in <content_type>/loader/<objectName>/
		   
          ProjContent newContent = new ProjContent(contentType, file.getName(), null, true);
          projPath.append(ConcentraExtractElement.LOADER);
          projPath.append(ProjContent.normalizeContentNameForUrl(propertyProjContent.getExtentionlessContentName()));
          projPath.append(ConcentraExtractElement.file_separator);
          // secondary files go under property file
          contentLen = projPath.length();
          
          projPath.append(ProjContent.normalizeContentNameForUrl(file.getName()));
          newContent.setPath(projPath.toString());
          // reset the contentName to be whatever the file is as we are reading it from the disk.
          //SMO : 
          newContent.setCompany(this.company);
          newContent.setContentName(file.getName());
          newContent.setContentPath(file.getParent());
          extractElem.addProjContent(newContent);
        }
        break;
      }
    }
    
    if (contentLen == 0) {
    	// contentLen wasn't set so there is no primary content for this document.  All attachments go under the metadata
        projPath.append(ConcentraExtractElement.LOADER);
        projPath.append(objectName);
        projPath.append(ConcentraExtractElement.file_separator);
        // secondary files go under property file
        contentLen = projPath.length();
    }
    
    // now add all of the other files
    for (int i=0; i < fileResults.size(); i++) {
      ContentOut.ContentOutFileInfo fi = (ContentOut.ContentOutFileInfo)fileResults.get(i);
      if (fi.getType() == ContentOut.ContentOutFileInfo.OBJECT_FILE ||
          fi.getType() == ContentOut.ContentOutFileInfo.COMPONENT_INLINE) {
        continue;
      }
      File theFile = new File(fi.getFile());
      if (theFile.exists()) {
        projPath.setLength(contentLen);
        ProjContent newContent = new ProjContent(contentType, theFile.getName(), null, true);
        // reset the contentName to match the name on the disk
        newContent.setContentName(theFile.getName());
        newContent.setContentPath(theFile.getParent());
        //SMO : 
        newContent.setCompany(company);
        projPath.append(ProjContent.normalizeContentNameForUrl( theFile.getName() ));
        newContent.setPath(projPath.toString());
        extractElem.addProjContent(newContent);
      }
    }
    return true;
  }

  /***************************************************************************
   * Sets the ExtractSession object to be used in all the Concentra requests
   * 
   * @param session
   *            Instance of the ExtractSession class
   */

  public void setSession(ConcentraAdminSession docbaseSession) {
    this.docbaseSession = docbaseSession;
  }

  /***************************************************************************
   * Retrieves the specific information of a file, depending of its type
   * 
   * @param productGroups
   *            that need to be check to determine if the element has valid
   *            products
   * @return Xml element with the specific information of the given document
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  protected abstract ArrayList getSpecificDocInfo(Element productGroups) throws DfException, DmRepositoryException ;

  /***************************************************************************
   * Creates a XML element that contains more than one child
   * 
   * @param values
   *            Values to be added to main element
   * @param mainElementName
   *            Name of the main element
   * @param childElementsName
   *            Name of the child elements
   * @return
   */
  protected Element getMultilineElement(String[] values, String mainElementName, String childElementsName) {
    // Add Result to Element
    Element mainElement = new BaseElement(mainElementName);
    if (values != null) {
      for(String val : values) {
        Element child = new BaseElement(childElementsName);
        child.addText(val);
        mainElement.add(child);
      }
    }

    return mainElement;
  }

  /***************************************************************************
   * Creates a XML element that contains more than one children
   * 
   * @param propertyDoc
   *            Xml document needed for xml elements creation
   * @param resultInfo
   *            Result to be parsed during the element creation
   * @param mainElementName
   *            Name of the main element
   * @param childElementsName
   *            Name of the children elements
   * @return
   */
  protected Element getMultilineElement(Result resultInfo, String mainElementName, String childElementsName) {
    Element mainElement = null;

    LoaderLog.info("ConcentraDoc getMultilineElement");

    // Add Result to Element
    mainElement = new BaseElement(mainElementName);
    for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
      Object resultObject = resultInfo.getValue(childrenIter, childElementsName);
      if (resultObject != null) {
        String resultValue = resultObject.toString();
        if (resultValue != null && !resultValue.equals(EMPTY_STRING)) {
          Element children = new BaseElement(childElementsName);
          children.addText(resultValue);
          mainElement.add(children);
        }
      }
    }
    return mainElement;
  }

  /***************************************************************************
   * Creates a XML element that contains more than one children
   * 
   * @param propertyDoc
   *            Xml document needed for xml elements creation
   * @param resultInfo
   *            Result to be parsed during the element creation
   * @param mainElementName
   *            Name of the main element
   * @param childElementsName
   *            Name of the children elements
   * @param colName
   *            Column name from query
   * @return
   */
  protected Element getMultilineElement(Result resultInfo, String mainElementName, String childElementsName,
      String colName) {
    Element mainElement = null;

    LoaderLog.info("ConcentraDoc getMultilineElement");

    // Add Result to Element
    mainElement = new BaseElement(mainElementName);
    ArrayList<String>  businessUnitList = new ArrayList<String>();
    for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
      Object resultObject = resultInfo.getValue(childrenIter, colName);
      if (resultObject != null) {
        String resultValue = resultObject.toString();
        if (resultValue != null && !resultValue.equals(EMPTY_STRING)) {
        	//Taxonomy Changes
        	if(childElementsName != null && childElementsName.equals(ELEMENT_TMS_CATEGORIZATION_TOPIC)){
	         
	          StringTokenizer tmscattopicsKeyVal = new StringTokenizer(resultValue,"=");
	          while (tmscattopicsKeyVal.hasMoreElements()) {
	        	  Element children = new BaseElement(childElementsName);
		          Element child_key = new BaseElement(ELEMENT_TMS_CATEGORIZATION_KEY);
		          Element child_value = new BaseElement(ELEMENT_TMS_CATEGORIZATION_VALUE);
		          
		          child_key.addText((String)tmscattopicsKeyVal.nextElement());
		          child_value.addText((String)tmscattopicsKeyVal.nextElement());
		          children.add(child_key);
		          children.add(child_value);
		          mainElement.add(children);
		          
	          }
        	}else if(childElementsName != null && childElementsName.equals(ELEMENT_BUSINESS_UNIT)){   
        		businessUnitList.add(resultValue); 
        	}else{
        	  Element children = new BaseElement(childElementsName);
  	          children.addText(resultValue);
  	          mainElement.add(children);
        	}
        }
      }
    }
    if(!businessUnitList.isEmpty()){
    	int listSize = businessUnitList.size()-1;;
	    for(int i=listSize;i>=0;i--){
	    Element children = new BaseElement(childElementsName);
	 	  if(i == listSize){
	 		  children.addAttribute("type","Primary" );
	 		  children.addText(businessUnitList.get(i));
		      mainElement.add(children);
	 	  }else{
	 		 children.addAttribute("type","Secondary" );
	 		 children.addText(businessUnitList.get(i));
		     mainElement.add(children);
	 	  }
	    }
    }
    return mainElement;
  }


  //Taxanomy Changes - Adding <taxonomy-categorizations> tag 14.1 release
  /*****************************
   *Retrieves tms_categorization_topics from the Concentra database for the
   * given document
   * 
   * @return Xml element with the tms_categorization_topics
   */
  
  protected Element getTmsCategorizationTopics(){
	  return getElementFromObjectTypeColumnId("getTmsCategorizationTopics",
			  QUERY_GET_TMS_CATEGORIZATION_TOPICS, ELEMENT_TMS_CATEGORIZATION_TOPICS, ELEMENT_TMS_CATEGORIZATION_TOPIC,
			  QUERY_COLUMN_TMS_CATEGORIZATION_TOPIC);
  }//End of Taxonomy Changes
  
  /***************************************************************************
   * Retrieves from the Concentra database the language label of the given
   * document
   * 
   * @return Xml Element with the language label
   */
  protected Element getLanguageLabel() {
    return getSingleElementFromObjectTypeId("getLanguageLabel", QUERY_GET_LANGUAGE_LABEL,
        ELEMENT_LANGUAGE_LABEL);
  }

  /***************************************************************************
   * Retrieves from Concentra the DOS extension of the document content and
   * converts it to an Xml Element
   * 
   * @return Xml Element with the DOS extension of the content
   */
  protected Element getContentType() {
    Element contentTypeElement = getSingleElementFromObjectTypeId("getContentType",
        QUERY_GET_DOS_EXTENSION, ExtractorConstants.ELEMENT_CONTENT_TYPE);
    contentType = contentTypeElement.getText();
    return contentTypeElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Acronym of the given document
   * 
   * @return Xml Element with the Acronym
   */
  protected Element getAcronym() {
    return getSingleElementFromObjectTypeId("getAcronym", QUERY_GET_ACRONYM,
        ELEMENT_CMG_ACRONYM);
  }

  /**
   * Retrieves from the Concentra database the Organization of the given
   * document
   * 
   * @return
   */
  protected Element getOrganization() {
    return getSingleElementFromObjectTypeId("getOrganization", QUERY_GET_ORGANIZATION,
        ELEMENT_ORGANIZATION);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Feedback Address of the given
   * document
   * 
   * @return Xml Element with the Feedback Address
   */
  protected Element getFeedbackAddress() {
    return getSingleElementFromObjectTypeId("getFeedbackAddress", QUERY_GET_FEEDBACK_ADDRESS,
        ELEMENT_CMG_FEEDBACK_ADDRESS);
  }

  /**
   * Retrieves from the Concentra database a single element defined by the
   * query of a given document
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param element
   *            that will be used on the result set
   */
  protected Element getSingleElementFromObjectTypeId(String methodName, String queryName, String element) {
    Element singleElement = null;


    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      singleElement = new BaseElement(element);
      Object elementToAdd = resultInfo.getValue(0, element);
      if (elementToAdd != null && !elementToAdd.equals(EMPTY_STRING)) {
        singleElement.addText(elementToAdd.toString());
      }
    } else {
      LoaderLog.error("No results from query "+token+" getLanguageLabel");
    }
    return singleElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Search Keywords of the given
   * document
   * 
   * @return Xml Element with the document search keywords
   */
  protected Element getSearchKeywords() {
    return getElementFromObjectTypeId("getSearchKeywords", QUERY_GET_SEARCH_KEYWORDS,
        ELEMENT_SEARCH_KEYWORDS, ELEMENT_SEARCH_KEYWORD);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Authors of the given document
   * 
   * @return Xml element with the document authors
   */
  protected Element getAuthors() {
    return getElementFromObjectTypeId("getAuthors", QUERY_GET_AUTHORS,
        ExtractorConstants.ELEMENT_AUTHORS, ExtractorConstants.ELEMENT_AUTHOR);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Addr Country Code of the given
   * document
   * 
   * @return Xml Element with the Addr Country Code
   */
  protected Element getAddrCountryCode() {
    return getSingleElementFromObjectTypeId("getAddrCountryCode",
        QUERY_GET_ADDR_COUNTRY_CODE, ELEMENT_ADDR_COUNTRY_CODE);
  }

  protected Element getBusinessUnits() {
    return getElementFromObjectTypeId("getBusinessUnits", QUERY_GET_BUSINESS_UNITS,
        ELEMENT_BUSINESS_UNITS, ELEMENT_BUSINESS_UNIT, QUERY_COLUMN_BUSINESS_UNIT);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Business Group of the given
   * document
   * 
   * @return Xml Element with the Business Group
   */
  protected Element getBusinessGroup() {
    return getSingleElementFromObjectTypeId("getBusinessGroup", QUERY_GET_BUSINESS_GROUP,
        ELEMENT_BUSINESS_GROUP);
  }

  protected Element getContentTypes() {
    return getElementFromObjectTypeId("getContentTypes", QUERY_GET_CONTENT_TYPE,
        ELEMENT_CONTENT_TYPES, ELEMENT_CONTENT_TYPE, QUERY_COLUMN_CONTENT_TYPE);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Regions of the given document
   * 
   * @return Xml Element with the document regions
   */
  protected Element getRegions() {
    return getElementFromObjectTypeId("getRegions", QUERY_GET_REGIONS,
        ExtractorConstants.ELEMENT_REGIONS, ExtractorConstants.ELEMENT_REGION);
  }

  protected Element getComponentRegions(String ComponentObjectId, String ComponentObjectType) {
    return getElementFromComponentObjectTypeId("getComponentRegions", QUERY_GET_REGIONS,
        ExtractorConstants.ELEMENT_REGIONS, ExtractorConstants.ELEMENT_REGION, ComponentObjectId, ComponentObjectType);
  }

  /**
   * Retrieves from Concentra the accounts of the document
   * 
   * @return Xml element with the document accounts
   */
  protected Element getAccounts() {
    return getElementFromObjectTypeId("getAccounts", QUERY_GET_ACCOUNTS, ELEMENT_ACCOUNTS,
        ELEMENT_ACCOUNT);
  }

  /**
   * Retrieves from Concentra the marketing teams of the document
   * 
   * @return Xml element with the document marketing accounts
   */
  protected Element getMarketingTeams() {
    return getElementFromObjectTypeId("getMarketingTeams", QUERY_GET_MARKETING_TEAMS,
        ELEMENT_MARKETING_TEAMS, ELEMENT_MARKETING_TEAM);
  }

  /***************************************************************************
   * Retrieves from Concentra database the publication destination
   * 
   * @return Xml element with the publication destination content
   */
  protected Element getPublicationDestinations() {
    return getElementFromObjectTypeId("getPublicationDestinations",
        QUERY_GET_PUBLICATION_DESTINATION, ELEMENT_PUBLICATION_DESTINATIONS, ELEMENT_PUBLICATION_DESTINATION);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document type details of the
   * given document
   * 
   * @return Xml element with the document type details
   */
  protected Element getDocumentTypeDetails() {
    return getElementFromObjectTypeColumnId("getDocumentTypeDetails",
        QUERY_GET_DOCUMENT_TYPE_DETAILS, ELEMENT_DOCUMENT_TYPE_DETAILS, ELEMENT_DOCUMENT_TYPE_DETAIL,
        QUERY_COLUMN_DOCUMENT_TYPE_DETAIL);
  }

  /**
   * Retrieves from Concentra the product levels of the document
   * 
   * @return Xml element with the product levels
   */
  protected Element getProductLevels() {
    return getElementFromObjectTypeColumnId("getProductLevels", QUERY_PRODUCT_LEVELS,
        ELEMENT_PRODUCT_LEVELS, ELEMENT_PRODUCT_LEVEL);
  }

  /**
   * Retrieves a specific MultilineElement defined by the query
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param parentElement
   *            that will be used on the result set
   * @param childElement
   *            that will be used on the result set
   * @return XML element with the property, defined by the query
   */
  protected Element getElementFromId(String methodName, String queryName, String parentElement, String childElement) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement);
    } else {
      LoaderLog.error("No results from query: "+queryName);
    }
    return elementDetails;
  }

  /**
   * Retrieves a specific MultilineElement defined by the query
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param parentElement
   *            that will be used on the result set
   * @param childElement
   *            that will be used on the result set
   * @return XML element with the property, defined by the query
   */
  protected Element getElementFromObjectTypeId(String methodName, String queryName, String parentElement,
      String childElement) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement);
    } else {
      LoaderLog.error("No results from query "+queryName);
    }
    return elementDetails;
  }

  protected Element getElementFromComponentObjectTypeId(String methodName, String queryName, String parentElement,
      String childElement, String ComponentObjectId, String ComponentObjectType) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, ComponentObjectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, ComponentObjectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement);
    } else {
      LoaderLog.error("No results from query "+queryName);
    }
    return elementDetails;
  }

  /**
   * Retrieves a specific MultilineElement defined by the query
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param parentElement
   *            that will be used on the result set
   * @param childElement
   *            that will be used on the result set
   * @param colName
   *            Column name utilized by DQL
   * @return XML element with the property, defined by the query
   */
  protected Element getElementFromObjectTypeId(String methodName, String queryName, String parentElement,
      String childElement, String colName) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement, colName);
    } else {
      LoaderLog.error("No result from query "+token+" "+queryName);
    }
    return elementDetails;
  }

  /**
   * Retrieves a specific MultilineElement defined by the query
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param parentElement
   *            that will be used on the result set
   * @param childElement
   *            that will be used on the result set
   * @return XML element with the property, defined by the query
   */
  protected Element getElementFromObjectTypeColumnId(String methodName, String queryName, String parentElement,
      String childElement) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement);
    } else {
      LoaderLog.error("No result from query "+token+" "+queryName);
    }
    return elementDetails;
  }

  /**
   * Retrieves a specific MultilineElement defined by the query
   * 
   * @param methodName
   *            to be used by logging system
   * @param queryName
   *            to be executed at concentra
   * @param parentElement
   *            that will be used on the result set
   * @param childElement
   *            that will be used on the result set
   * @param colName
   *            the column name where the attribute will be grabbed
   * @return XML element with the property, defined by the query
   */
  protected Element getElementFromObjectTypeColumnId(String methodName, String queryName, String parentElement,
      String childElement, String colName) {
    Element elementDetails = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      elementDetails = getMultilineElement(resultInfo, parentElement, childElement, colName);
    } else {
      LoaderLog.error("No result from query "+token+" "+queryName);
    }
    return elementDetails;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the full title of the given
   * document ex <full_title language_code="EN_US">Testing</full_title>
   * 
   * @return Xml Element with the full title
   */
  protected Element getFullTitle() {
    Element mainElement = null;

    LoaderLog.info("ConcentraDoc getFullTitle");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_FULL_TITLE, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();
    String val = null;
    String langCode = null; ;
    if (resultInfo != null && ((langCode = resultInfo.getResultString(0, ExtractorConstants.ELEMENT_LANGUAGE_CODE)) != null)) {
      // If the document has multi titles, gets all of them
      if (langCode.equals(RESULT_MULTI)) {
        // Build the other query
        mainElement = getAttributedMultilineElement(QUERY_GET_FULL_TITLES, ELEMENT_FULL_TITLES,
            ELEMENT_FULL_TITLE, ExtractorConstants.ELEMENT_LANGUAGE_CODE);
        // If the element is a single element of a manual doc, adds
        // the full_titles element
      } else if (objectType.equals(ExtractorConstants.MANUAL_DOC)) {
        mainElement = new BaseElement(ELEMENT_FULL_TITLES);
        Element childElement = new BaseElement(ELEMENT_FULL_TITLE);

        if ( !langCode.equals(EMPTY_STRING)) {
          childElement.addAttribute(ExtractorConstants.ELEMENT_LANGUAGE_CODE, langCode);
        }

        if ((val = resultInfo.getResultString(0, ELEMENT_FULL_TITLE)) != null) {
          childElement.addText(val);
        }
        mainElement.add(childElement);
        // In any other element, adds the single full_title element
      } else {
        mainElement = new BaseElement(ELEMENT_FULL_TITLE);
        if (!langCode.equals(EMPTY_STRING)) {
          mainElement.addAttribute(ExtractorConstants.ELEMENT_LANGUAGE_CODE, langCode);
        }
        if ((val = resultInfo.getResultString(0, ELEMENT_FULL_TITLE)) != null) {
          mainElement.addText(val);
        }
      }
    } else {
      LoaderLog.error("No results from query "+token+" getFullTitle");
    }
    return mainElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the language code of the given
   * document
   * 
   * @return Xml Element with the document language code(s)
   */
  protected Element getLanguageCode() {
    Element mainElement = null;

    LoaderLog.info("ConcentraDoc getLanguageCode");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_LANGUAGE_CODE, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();
    String langCode = null;
    
    if (resultInfo != null && ((langCode = resultInfo.getResultString(0, ExtractorConstants.ELEMENT_LANGUAGE_CODE)) != null)) {
      // Add Result to Element
      if (langCode.equals(RESULT_MULTI)) {
        // Build the other query
        mainElement = getAttributedMultilineElement(QUERY_GET_LANGUAGE_CODES_MULTI, ELEMENT_LANGUAGE_CODES,
            ExtractorConstants.ELEMENT_LANGUAGE_CODE, ELEMENT_LANGUAGE_LABEL);
      } else {
        mainElement = getAttributedMultilineElement(QUERY_GET_LANGUAGE_CODES_SINGLE,
            ELEMENT_LANGUAGE_CODES, ExtractorConstants.ELEMENT_LANGUAGE_CODE, ELEMENT_LANGUAGE_LABEL);
      }
    } else {
      LoaderLog.error("No results from query "+token+" getLanguageCode");
    }
    return mainElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the information source of the given
   * document
   * 
   * @return Xml element with the version label
   */
  protected Element getInformationSource() {
    return getSingleElementFromObjectTypeObjectColumnId("getInformationSource",
        QUERY_GET_INFORMATION_SOURCE, ELEMENT_INFORMATION_SOURCE);
  }

  /***************************************************************************
   * Retrieves from Concentra the masterObjectName of the document content and
   * converts it to an Xml Element
   * 
   * @return Xml Element with the masterObjectName of the content
   */
  protected Element getMasterObjectName() {
    return getSingleElementFromObjectTypeObjectColumnId("getMasterObjectName",
        QUERY_GET_MASTER_OBJECT_NAME, ELEMENT_MASTER_OBJECT_NAME);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document type of the given
   * document
   * 
   * @return Xml Element with the document type
   */
  private Element getDocumentType() {
    return getSingleElementFromObjectTypeObjectColumnId("getDocumentType",
        QUERY_GET_DOCUMENT_TYPE, ExtractorConstants.ELEMENT_DOCUMENT_TYPE);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Collection Valid Flag of the
   * given document
   * 
   * @return Xml Element with the Collection Valid Flag
   */
  protected Element getCollectionValidFlag() {
    return getSingleElementFromObjectColumnId("getCollectionValidFlag",
        QUERY_GET_COLLECTION_VALID_FLAG, ELEMENT_COLLECTION_VALID_FLAG);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Publication Code of the given
   * document
   * 
   * @return Xml Element with the Publication Code
   */
  protected Element getPublicationCode() {
    return getSingleElementFromObjectTypeObjectColumnId("getPublicationCode",
        QUERY_GET_PUBLICATION_CODE, ELEMENT_PUBLICATION_CODE);
  }

  /**
   * Retrieves from the Concentra database a single element defined by the
   * query of a given document
   * 
   * @return single element to be added to the properties file
   */
  private Element getSingleElementFromObjectTypeObjectColumnId(String methodName, String queryName, String element) {
    Element singleElement = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(2, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      singleElement = new BaseElement(element);
      Object result = resultInfo.getValue(0, element); 
      if (result != null) {
        singleElement.addText(result.toString());
        saveValue(result.toString(), element);
      } else {
        singleElement.addText(EMPTY_STRING);
      }
    } else {
      LoaderLog.error("No results from query "+token+" "+queryName);
    }
    return singleElement;
  }

  /**
   * Retrieves from the Concentra database a single element defined by the
   * query of a given document
   * 
   * @return single element to be added to the properties file
   */
  protected Element getSingleElementFromObjectColumnId(String methodName, String queryName, String element) {
    Element singleElement = null;

    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      singleElement = new BaseElement(element);
      Object value = resultInfo.getValue(0, element); 
      if ( value != null ) {
        singleElement.addText(value.toString());
      } else {
        singleElement.addText(EMPTY_STRING);
      }
    } else {
      LoaderLog.error("No results from query "+token+" "+queryName);
    }
    return singleElement;
  }

  /***************************************************************************
   * Retrieves from Concentra database the latest property update date to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the latest property update date
   */
  protected Element getPropertyUpdateDate() {
    Element propertyUpdateDate = null;

    LoaderLog.info("ConcentraDoc getPropertyUpdateDate");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PROPERTY_UPDATE_DATE, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();
    Object val = null;
    
    if (resultInfo != null) {
      String biggestDate = EMPTY_STRING;
      for (int dateIter = 0; dateIter < resultInfo.getColumnCount(); dateIter++) {
        if ((val = resultInfo.getValue(0, dateIter)) != null) {
          String currentDate = val.toString();
          if (currentDate.compareTo(biggestDate) > 0) {
            biggestDate = currentDate;
          }
        }
      }
      propertyUpdateDate = new BaseElement(ELEMENT_PROPERTY_UPDATE_DATE);
      if (biggestDate != null) {
        propertyUpdateDate.addText(biggestDate);
      }
    } else {
      LoaderLog.error("No results form query getPropertyUpdateDate");
    }
    return propertyUpdateDate;
  }

  /***************************************************************************
   * Retrieves from Concentra database the latest content update date to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the latest content update date
   */
  protected Element getContentUpdateDate() {
    Element contentUpdateDate = null;

    LoaderLog.info("ConctentraDoc getContenUpdateDate");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_CONTENT_UPDATE_DATE, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    commonInfo.setParameter(2, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(3, objectId);
    Result resultInfo = commonInfo.execute();
    Object val = null;
    
    if (resultInfo != null) {
      String biggestDate = EMPTY_STRING;
      for (int dateIter = 0; dateIter < resultInfo.getRowCount(); dateIter++) {
        if ((val = resultInfo.getValue(dateIter, ExtractorConstants.ELEMENT_CONTENT_UPDATE_DATE)) != null) {
          String currentDate = val.toString();
          if (currentDate.compareTo(biggestDate) > 0) {
            biggestDate = currentDate;
          }
        }
      }
      contentUpdateDate = new BaseElement(ExtractorConstants.ELEMENT_CONTENT_UPDATE_DATE);
      if (biggestDate != null) {
        contentUpdateDate.addText(biggestDate);
      }
    } else {
      LoaderLog.error("No results from query "+token+" getContentUpdateDate");
    }
    return contentUpdateDate;
  }

  /***************************************************************************
   * Retrieves from Concentra database the latest content update user to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the latest content update user
   */
  protected Element getContentUpdateUser() {
    return getSingleElementFromObjectTypeId("getContentUpdateUser",
        QUERY_GET_CONTENT_UPDATE_USER, ELEMENT_CONTENT_UPDATE_USER);
  }

  /***************************************************************************
   * Retrieves from Concentra database the audiences data
   * (audience_name,audience_release_date,audience_expiry_date)
   * 
   * @return Xml element with the audiences data details
   */
  protected Element getAudiences() {
    Element audiences = null;

    LoaderLog.info("ConcentraDoc getAudiences");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_AUDIENCES, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultAudiences = commonInfo.execute();

    if (resultAudiences != null) {
      audiences = new BaseElement(ELEMENT_AUDIENCES);
      for (int childrenIter = 0; childrenIter < resultAudiences.getRowCount(); childrenIter++) {
        String audienceName = resultAudiences.getValue(childrenIter, ELEMENT_AUDIENCE_NAME).toString();
        String audienceReleaseDate = resultAudiences.getValue(childrenIter,
            ELEMENT_AUDIENCE_RELEASE_DATE).toString();
        String audienceExpiryDate = resultAudiences.getValue(childrenIter, ELEMENT_AUDIENCE_EXPIRY_DATE).toString();

        if (!audienceName.equals(EMPTY_STRING) && !audienceReleaseDate.equals(EMPTY_STRING)
            && !audienceExpiryDate.equals(EMPTY_STRING)) {

          Element audienceElement = new BaseElement(ELEMENT_AUDIENCE);
          Element audienceNameElement = new BaseElement(ELEMENT_AUDIENCE_NAME);
          Element audienceReleaseDateElement = new BaseElement(ELEMENT_AUDIENCE_RELEASE_DATE);
          Element audienceExpiryDateElement = new BaseElement(ELEMENT_AUDIENCE_EXPIRY_DATE);

          audienceNameElement.addText(resultAudiences.getValue(childrenIter, 0).toString());
          audienceReleaseDateElement.addText(resultAudiences.getValue(childrenIter, 1).toString());
          audienceExpiryDateElement.addText(resultAudiences.getValue(childrenIter, 2).toString());

          audienceElement.add(audienceNameElement);
          audienceElement.add(audienceReleaseDateElement);
          audienceElement.add(audienceExpiryDateElement);
          audiences.add(audienceElement);
        }
      }
    } else {
      LoaderLog.error("No results for query "+token+" getAudiences");
    }
    return audiences;
  }

  /***************************************************************************
   * Using the already existing information of the root xml document element,
   * generates the file_name element for the property file (i.e
   * "c50084311.txt")
   * 
   * @return Xml element for the full content name
   */
  protected Element getFileName() {
    Element fileName = null;

    LoaderLog.info("ConcentraDoc getFileName");

    if (objectName != null && contentType != null) {
      fileName = new BaseElement(ELEMENT_FILE_NAME);
      fileName.addText(objectName + ExtractorConstants.DOT_EXTENSION + contentType);
    }
    return fileName;
  }

  /**
   * getMultiFileNumber determines if an additional version of a component is being added to a property file and returns
   * count-1 as an extension for the "next" component.
   * @param component
   * @param map
   * @return Name of the property to add.
   */
  private String getMultiFileNumber(ComponentBean component, HashMap<String, ArrayList<String>>map) {
    String fileNameExt = null;
    if (map.containsKey(component.getObjectName())) {
      ArrayList<String> objectIds = map.get(component.getObjectName());
      int idx = objectIds.indexOf(component.getObjectId());
      if (idx < 0) {
        // This is a different version of this object
        // Get the fileName + extension
        fileNameExt = Integer.toString((objectIds.size()-1));
        objectIds.add(component.getObjectId());
      } else {
        // Have seen this file before.  
      }
    } else {
      ArrayList<String> newList = new ArrayList<String>();
      // haven't seen this object yet so add it
      map.put(component.getObjectName(), newList);
      newList.add(component.getObjectId());
      fileNameExt = "";
    }
    return fileNameExt;
  }

  /**
   * Retrieves from Concentra the information of the document components
   * (files associated to the document)
   * 
   * @return Xml element with the components information
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  protected Element getComponents() throws DfException, DmRepositoryException {
    Element componentsElement = null;
    boolean result;

    LoaderLog.info("ConcentraDoc getComponents");

    componentsElement = new BaseElement(ELEMENT_COMPONENTS);
    if (objectType.equals(ExtractorConstants.HHO_MARKETING_DOC)
        || objectType.equals(ExtractorConstants.SUPPORT_DOC)) {

      CBaseObject extractedObject = CBaseObjectCreator.constructExistingCBaseObject(docbaseSession, objectId);
      if (extractedObject instanceof CDocument) {
        CDocument extractedDocument = (CDocument) extractedObject;
        List components = extractedDocument.getComponents();
        // map object_name->List of object_ids.  Used to eliminate duplicates, find extra copies
        HashMap<String, ArrayList<String>> compMap = new HashMap<String, ArrayList<String>>();
        for(Iterator compItr = components.iterator(); compItr.hasNext();) {
          ComponentBean component = (ComponentBean)compItr.next();
          // If the component is not null and its original
          // name is not INLINE and
          // its object id is not equal to the current
          // document object id

          if(component != null && !component.getObjectId().equals(objectId)) {
            if (objectType.equals(ExtractorConstants.SUPPORT_DOC) || !component.getOriginalFilename().equals(INLINE_DOC_NAME) ) {
              String fileNameExt = getMultiFileNumber(component, compMap);
              addComponentElement(componentsElement, component, fileNameExt);
            }
          }
        }
      }
    }
    return componentsElement;
  }

  /**
   * Adds a Component element to the Components element if one is called for
   * @param componentsElement Components element being added to
   * @param component Component to add if fileName != null
   * @param fileNameExt fileNameExt to be added.  null indicates it is a duplicate
   * 
   * @return Xml element with the component information
   */
  protected void addComponentElement(Element componentsElement, ComponentBean component, 
      String fileNameExt) {

    if (fileNameExt == null) {
      LoaderLog.debug("[***] Duplicated Component found... skipping");
      return;
    }

    LoaderLog.info("ConcentraDoc addComponentElement");
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_COMPONENT, docbaseSession);
    commonInfo.setParameter(0, component.getChronicleId());
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {

      Element componentElement = new BaseElement(ELEMENT_COMPONENT);
      Element compFileName = new BaseElement(ELEMENT_FILE_NAME);
      String extension = resultInfo.getValue(0, ExtractorConstants.ELEMENT_CONTENT_TYPE).toString();
      compFileName.addText(component.getObjectName()+fileNameExt+ExtractorConstants.DOT_EXTENSION+extension);

      Element compSize = new BaseElement(ELEMENT_FILE_BYTES);
      compSize.addText(component.getFileSize());
      
      Element compVersion = new BaseElement(ExtractorConstants.ELEMENT_VERSION_LABEL);
      compVersion.addText(component.getSymbolicLabel());
      
      // add pieces to the component
      componentElement.add(compFileName);
      componentElement.add(compSize);
      componentElement.add(compVersion);

      // add component to the components
      componentsElement.add(componentElement);
    }
  }

  /***************************************************************************
   * Retrieves from Concentra the Extra properties values
   * 
   * @return Xml Element with the Extra properties values
   */
  protected Element getExtraProperties() {
    Element extraProperties = null;

    LoaderLog.info("ConcentraDoc getExtraProperties");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_EXTRA_PROPERTIES, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      extraProperties = new BaseElement(ELEMENT_EXTRA_PROPERTIES);
      // Continue with the xml structure
      /**
       * Create the following structure <extra_property>
       * <extra_property_name><extra_property_name/>
       * <extra_property_values> <extra_property_value/>
       * <extra_property_values/> <extra_property/>
       */
      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {

        if (!resultInfo.getValue(childrenIter, ELEMENT_EXTRA_PROPERTY_NAME).toString().equals("")) {
          Element extraPropertyName = new BaseElement(ELEMENT_EXTRA_PROPERTY_NAME);
          extraPropertyName.addText(resultInfo.getValue(childrenIter, ELEMENT_EXTRA_PROPERTY_NAME)
              .toString());
          Element extraPropertyValues = new BaseElement(ELEMENT_EXTRA_PROPERTY_VALUES);

          Element extraPropertyValue = new BaseElement(ELEMENT_EXTRA_PROPERTY_VALUE);
          extraPropertyValue.addText(resultInfo.getValue(childrenIter, ELEMENT_EXTRA_PROPERTY_VALUE)
              .toString());
          extraPropertyValues.add(extraPropertyValue);

          Element extraProperty = new BaseElement(ELEMENT_EXTRA_PROPERTY);
          extraProperty.add(extraPropertyName);
          extraProperty.add(extraPropertyValues);

          extraProperties.add(extraProperty);
        }
      }
    }
    return extraProperties;
  }

  /***************************************************************************
   * Depending on the object type value, returns the name of the content
   * column
   * 
   * @return Content column name
   */
  protected String getObjectColumn() {
    return objectType.substring(INITIAL_INDEX, objectType.lastIndexOf(UNDERSCORE_CHAR) + ADJUSTMENT).concat(
        TABLE_COL);
  }

  /***************************************************************************
   * Creates an XML element that contains more than one children along with an
   * attribute
   * 
   * @param propertyDoc
   *            Xml document needed for xml elements creation
   * @param resultInfo
   *            Result to be parsed during the element creation
   * @param mainElementName
   *            Name of the main element
   * @param childElementsName
   *            Name of the children elements
   * @param attribName
   *            Name of the attribute to be added
   * @return
   */
  protected Element getMultilineElementWithAttribute(Result resultInfo, String mainElementName,
      String childElementsName, String attribName) {
    Element mainElement = null;

    LoaderLog.info("ConcentraDoc getMultilineElementWithAttribute");

    // Add Result to Element
    mainElement = new BaseElement(mainElementName);
    for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
      String resultValue = resultInfo.getValue(childrenIter, childElementsName).toString();
      if (resultValue != null && !resultValue.equals(EMPTY_STRING)) {
        Element children = new BaseElement(childElementsName);
        children.addText(resultValue);
        String attribValue = resultInfo.getValue(childrenIter, attribName).toString();
        children.addAttribute(attribName, attribValue);
        mainElement.add(children);
      }
    }
    return mainElement;
  }

  /**
   * zips the Document Content
   * 
   * @param documentContent
   *            content to be zipped
   * @param zippedExtractedDoc
   *            holds the object where the content will be zipped
   * @throws ZipContentException
   *             if zips process fails

    private void zipDocumentContent(File documentContent, ZipOutputStream zippedExtractedDoc)
            throws ZipContentException {
        LoaderLog.debug("ConcentraDoc "
                DEBUG_ZIP_DOCUMENT_CONTENT));

        String[] dirContents = documentContent.list();
        File contentFile = null;
        int inputByte = -1;
        for (int documentIter = 0; documentIter < dirContents.length; documentIter++) {
            contentFile = new File(documentContent, dirContents[documentIter]);
            if (contentFile.isFile() && contentFile.canRead()) {
                // output content through zip stream
                BufferedInputStream documentInput = null;
                BufferedOutputStream documentOuput = null;
                try {
                    ZipEntry contentDoc = new ZipEntry(CONTENT_DIRECTORY
                            dirContents[documentIter].toLowerCase()));
                    zippedExtractedDoc.putNextEntry(contentDoc);
                    documentInput = new BufferedInputStream(new FileInputStream(contentFile));
                    documentOuput = new BufferedOutputStream(zippedExtractedDoc);

                    while ((inputByte = documentInput.read()) != -1) {
                        documentOuput.write(inputByte);
                    }
                    documentInput.close();
                    documentOuput.flush();
                    zippedExtractedDoc.flush();
                    zippedExtractedDoc.closeEntry();
                } catch (IOException e) {
                    LoaderLog.fatal(ERROR_ZIPPING_DOCUMENT_CONTENTStoken));
                    throw new ZipContentException(ERROR_ZIPPING_DOCUMENT_CONTENTS);
                } catch (Exception e) {
                    LoaderLog.error(ERROR_GENERAL_PROCESS_FAILURE"ConcentraDoc ")
                            DEBUG_ZIP_DOCUMENT_CONTENT)
                            ExtractorConstants.LEFT_SQUARE_BRACKET)e.getMessage())
                            ExtractorConstants.RIGHT_SQUARE_BRACKET));
                    throw new ZipContentException(ERROR_ZIPPING_DOCUMENT_CONTENTS);
                }
            }
        }
    }
   */

  /**
   * Creates the content directory
   * 
   * @param zippedExtractedDoc
   *            holds the object where the content will be zipped
   * @throws ZipContentException
   *             if zip process fails

    private void createContentDirectory(ZipOutputStream zippedExtractedDoc) throws ZipContentException {
        LoaderLog.debug("ConcentraDoc "
                DEBUG_CREATE_CONTENT_DIRECTORY));
        try {
            ZipEntry contentDirectory = new ZipEntry(CONTENT_DIRECTORY);
            zippedExtractedDoc.putNextEntry(contentDirectory);
            zippedExtractedDoc.flush();
            zippedExtractedDoc.closeEntry();
        } catch (IOException e) {
            LoaderLog.fatal(ERROR_CREATING_CONTENT_DIRECTORYtoken));
            throw new ZipContentException(ERROR_CREATING_CONTENT_DIRECTORY);
        } catch (Exception e) {
            LoaderLog.error(ERROR_GENERAL_PROCESS_FAILURE"ConcentraDoc ")
                    DEBUG_CREATE_CONTENT_DIRECTORY)
                    ExtractorConstants.LEFT_SQUARE_BRACKET)e.getMessage())
                    ExtractorConstants.RIGHT_SQUARE_BRACKET));
            throw new ZipContentException(ERROR_CREATING_CONTENT_DIRECTORY);
        }
    }
   */

  /**
   * Zips the content of the property.xml file
   * 
   * @param zippedExtractedDoc
   *            holds the object where the property will be zipped
   * @throws ZipContentException
   *             if zip process fails

    private void zipPropertyContent(ZipOutputStream zippedExtractedDoc, Document propertyDoc)
            throws ZipContentException {
        LoaderLog.debug("ConcentraDoc "
                DEBUG_ZIP_PROPERTY_CONTENT));
        try {
            ZipEntry propertyEntry = new ZipEntry(PROPERTY_FILE);
            zippedExtractedDoc.putNextEntry(propertyEntry);
            BufferedOutputStream propertyOutputStream = new BufferedOutputStream(zippedExtractedDoc);
            XMLOutputter propertyOutputer = new XMLOutputter(ExtractorConstants.INDENTATION_SIZE, false,
                    ExtractorConstants.UTF_8);
            propertyOutputer.output(propertyDoc, propertyOutputStream);
            propertyOutputStream.flush();
            zippedExtractedDoc.closeEntry();
        } catch (IOException e) {
            LoaderLog.fatal(ERROR_ZIPPING_PROPERTY_CONTENTStoken));
            throw new ZipContentException(ERROR_ZIPPING_PROPERTY_CONTENTS);
        } catch (Exception e) {
            LoaderLog.error(ERROR_GENERAL_PROCESS_FAILURE"ConcentraDoc ")
                    DEBUG_ZIP_PROPERTY_CONTENT)
                    ExtractorConstants.LEFT_SQUARE_BRACKET)e.getMessage())
                    ExtractorConstants.RIGHT_SQUARE_BRACKET));
            throw new ZipContentException(ERROR_ZIPPING_PROPERTY_CONTENTS);
        }
    }
   */

 
  /***************************************************************************
   * Gets from Concentra the files of the given document
   * 
   * @return File with the path where the retrieved files have been cached
   * @throws DmRepositoryException 
   * @throws DfException 
   * @throws IOException 
   * @throws SupportCommMergerException 
   * @throws ProcessingException 
   * @throws SAXException 
   * @throws DocumentException 
   */
  private void getDocumentContent(ConcentraExtractElement extractElem) throws DfException, DmRepositoryException, SupportCommMergerException, IOException, ProcessingException, DocumentException, SAXException  {
    File contentOutPath = null;
    LoaderLog.info("ConcentraDoc getDocumentContent");

    contentOutPath = extractElem.getContentOutPath();

    long timeMillis = System.currentTimeMillis();

    LoaderLog.info("Extracting Document Content for document "+objectName + " : " + objectId);
    // If the extracted document is a support communication
    // document and its content is an xml
    if (isXMLSupportCommunication()){
      // this is a support_communications xml document
      try {
        ContentOut.simpleConcentraExport(docbaseSession, objectId, contentOutPath.toString(), true,
            true, false, false, null, fileResults);
      } catch (Exception e) {
        throw new ProcessingException ("SupportComm content checkout failed ",e, (e instanceof IOException));
      }
      CSupportDoc extractedDocument = (CSupportDoc)CBaseObjectCreator.constructExistingCBaseObject(docbaseSession, objectId);
      SupportCommMerger.supportCommExport((CSupportDoc) extractedDocument, objectId, fileResults);
    } else {
      try {
        ContentOut.simpleConcentraExport(docbaseSession, objectId, contentOutPath.toString(), true, false, false, false, ALL_CONTENT, fileResults);
		   
       
      } catch (Exception e) {
        throw new ProcessingException ("Content checkout failed ",e, (e instanceof IOException));
      }
    }
    LoaderLog.info("Time taken to extract content document :"+(System.currentTimeMillis() - timeMillis ));
    deleteDocumentDTDs(contentOutPath);
     
  }

  
  /**
   * @return the fileResults
   */
  public Vector getFileResults() {
    return fileResults;
  }

  /**
   * isXMLSupportCommunication 
   * @return true if this is an XML support communications document
   */
  private boolean isXMLSupportCommunication() {
    if (!objectType.equals(ExtractorConstants.SUPPORT_DOC) || !XML_EXTENSION.equals(contentType)) {
      // isn't a support doc or XML content
      return false;
    }
    // now check the docType is a supportCommunications type
    for(String dType : ConcentraConstants.SUPPORT_COMMUNICATION_DOC_TYPES) {
      if (dType.equals(docType)) {
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * Deletes the documents DTDs
   * 
   * @param contentCache
   *            place where the content is beign build
   */
  private void deleteDocumentDTDs(File contentCache) {
    // the dtds really needed this is a hack fix for the fact the
    // simpleConcentraExport is now
    // passing the "withDTD" even though it is set to false
    File dtd = new File(contentCache, C_SUPPORT_DOC_DTD);
    if (dtd.isFile()) {
      dtd.delete();
    }
    dtd = new File(contentCache, C_HHO_MARKETING_DOC_DTD);
    if (dtd.isFile()) {
      dtd.delete();
    }
    dtd = new File(contentCache, C_LIBRARY_DOC_DTD);
    if (dtd.isFile()) {
      dtd.delete();
    }
    dtd = new File(contentCache, C_CONTENT_FEEDBACK_DOC_DTD);
    if (dtd.isFile()) {
      dtd.delete();
    }
  }

  /***************************************************************************
   * Creates the document property file
   * 
   * @return Xml Document with all the document properties
   * @throws DmRepositoryException 
   * @throws DfException 
   *  
   */
  public Document getPropertyFile() throws DfException, DmRepositoryException {
    Document propertyDoc = null;

    LoaderLog.info("ConcentraDoc getPropertyFile");

    propertyDoc = new DOMDocument();
    // Create an element root
    Element root = new BaseElement(DOCUMENT_ROOT);
    propertyDoc.setRootElement(root);
    // Artifact 742: Add a new element that holds the r_object_id value.
    root.add(getObjectId());
    // Adss the priority property
    root.add(getPriority());
    
    //SMO : US #7469 add Company info element
    root.add(getCompany(objectId));
    
    Element productGroups = getProductGroups();
    Element documentType = getDocumentType();
    Element versionLabel = getVersionLabel();
    ArrayList specificDocInfo = getSpecificDocInfo(productGroups);

    if (productGroups != null && documentType != null && versionLabel != null && specificDocInfo != null) {
      root.add(productGroups);
      root.add(documentType);
      root.add(versionLabel);
      for (int specificIter = 0; specificIter < specificDocInfo.size(); specificIter++) {
        root.add((Element) specificDocInfo.get(specificIter));
      }
    }
    
    return propertyDoc;
  }

  /***************************************************************************
   * Creates a set list containing the solution for product groups, in the
   * case where product entries need to be resolved to another product.
   * 
   * @param propertyDoc
   *            Xml document needed for xml elements creation
   * @param resultInfo
   *            Result to be parsed during the element creation
   * @param childElementsName
   *            Name of the children elements
   * @return SET list for products
   */
  protected Set resolveProductEntries(Result resultInfo, String childElementsName) {
    HashSet listProducts = null;

    LoaderLog.info("ConcentraDoc resolveProductEntries");

    listProducts = new HashSet();
    for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
      Object resultObject = resultInfo.getValue(childrenIter, childElementsName);
      if (resultObject != null) {
        String resultValue = resultObject.toString();
        if (resultValue != null && !resultValue.equals(EMPTY_STRING)) {
          if (resultValue.startsWith(CPG_PREFIX_ONE)
              || resultValue.startsWith(CPG_PREFIX_TWO)
              || (resultValue.length() == R_OBJECT_ID_LENGTH && resultValue
                  .startsWith(CPG_R_OBJECT_ID_BEGINNING))) {
            // resolve product group with query and recall
            // resolveProductEntry
            Result toResolve = resolveCustomProductGroup(resultValue);
            listProducts.addAll(resolveProductEntries(toResolve, childElementsName));
          } else if (resultValue.indexOf(INCOMPLETE_PRODUCT_GROUP_TEMP) == -1
              && resultValue.indexOf(INCOMPLETE_PRODUCT_GROUP_PERM) == -1) {
            listProducts.add(resultValue);
          }
        }
      }
    }
    return listProducts;
  }

  /***************************************************************************
   * Gets the additional information for a specific product
   * 
   * @param resultValue
   *            product that needs additional info brought up.
   * @return result list holding the expanded list of products
   */
  private Result resolveCustomProductGroup(String resultValue) {
    Result res = null;
    LoaderLog.info("ConcentraDoc resolveCustomProductGroup");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PRODUCT_GROUPS_MEMBERS, docbaseSession);
    commonInfo.setParameter(0, resultValue);
    res = commonInfo.execute();
    return res;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Product Groups of the given
   * document
   * 
   * @return Xml element with the document product groups
   */
  protected Element getProductGroups() {
    Element productGroups = null;
    LoaderLog.info("ConcentraDoc getProductGroups");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PRODUCT_GROUPS, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      productGroups = new BaseElement(ELEMENT_PRODUCTS);
      Set productsResult = resolveProductEntries(resultInfo, ExtractorConstants.ELEMENT_PRODUCT);
      setDocProdGroups(productsResult);
      Iterator it = productsResult.iterator();
      while (it.hasNext()) {
        String value = (String) it.next();
        Element children = new BaseElement(ExtractorConstants.ELEMENT_PRODUCT);
        children.addText(value);
        productGroups.add(children);
      }
    } else {
      LoaderLog.error("No results from query "+token+" getProductGroups");
    }
    return productGroups;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the version label of the given
   * document (ex. 1.0 or 2.3)
   * 
   * @return Xml element with the version label
   */
  protected Element getVersionLabel() {
    return getVersionLabel(objectType, objectId);
  }

  /**
   * Retrieves from Concentra database the version label of the document
   * passed as parameter
   * 
   * @param documentId
   *            Unique identifier of the document owner of the version label
   * @return Xml Element with the version label
   */
  protected Element getVersionLabel(String docTable, String documentId) {
    Element versionLabel = null;

    LoaderLog.info("ConcentraDoc getVersionLabel");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_VERSION_LABEL, docbaseSession);
    commonInfo.setParameter(0, docTable, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, documentId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      versionLabel = new BaseElement(ExtractorConstants.ELEMENT_VERSION_LABEL);
      for (int versionIter = 0; versionIter < resultInfo.getRowCount(); versionIter++) {
        String versionValue = resultInfo.getResultString(versionIter, ExtractorConstants.ELEMENT_VERSION_LABEL);
        if (versionValue != null && versionValue.indexOf(ExtractorConstants.DOT_EXTENSION) != -1) {
          versionLabel.addText(versionValue);
          break;
        }
      }
    } else {
      LoaderLog.error("No results from query "+token+" getVersionLabel");
    }
    return versionLabel;
  }

  /***************************************************************************
   * Removes local files -- assumming that there are no directories under
   * cache directory
   * 
   * @param contentCache
   *            directory where the clean up needs to happen
   */
  private void cleanUp(File contentCache) {
    if (contentCache != null && contentCache.exists()) {
      String[] cacheContents = contentCache.list();
      File cacheContentFile = null;
      for (int fileIter = 0; fileIter < cacheContents.length; fileIter++) {
        cacheContentFile = new File(contentCache, cacheContents[fileIter]);
        cacheContentFile.delete();
      }
      contentCache.delete();
    }
  }

  /**
   * Executes the given query and creates a multilined element with the given
   * attribute
   * 
   * @param query
   *            Query to be executed to retrieved the required information
   *            from Concentra
   * @param mainElementName
   *            Name of the parent element
   * @param childElementName
   *            Name of the child element
   * @param attributeName
   *            Name of the attribute that each child element will contain
   * @return Multilined element on which each children has the given attribute
   */
  private Element getAttributedMultilineElement(String query, String mainElementName, String childElementName,
      String attributeName) {
    Element attributedElement = null;

    LoaderLog.info("ConcentraDoc getAttributedMultilineElement");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(query, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      attributedElement = getMultilineElementWithAttribute(resultInfo, mainElementName, childElementName,
          attributeName);
    } else {
      LoaderLog.error("No results from query "+token+" "+query);
    }
    return attributedElement;
  }


  /**
   * Retrieves from Concentra database the publication destination
   * 
   * @return Xml element with the publication destination content
   */
  protected Element getPlannedPublicDate() {
    Element element = null;

    LoaderLog.info("ConcentraDoc getPlannedPublicDate");

    PreparedDQL plannedPublicDateInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PLANNED_PUBLIC_DATE, docbaseSession);
    plannedPublicDateInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    plannedPublicDateInfo.setParameter(1, getObjectColumn(), DQLParameter.DQL_LABEL);
    plannedPublicDateInfo.setParameter(2, objectId);
    plannedPublicDateInfo.setParameter(3, objectType, DQLParameter.DQL_LABEL);
    plannedPublicDateInfo.setParameter(4, objectId);
    plannedPublicDateInfo.setParameter(5, getObjectColumn(), DQLParameter.DQL_LABEL);

    Result result = plannedPublicDateInfo.execute();
    
    if (result != null) {
      element = new BaseElement(ELEMENT_PLANNED_PUBLIC_DATE);
      IDfTime smallestDate = null;
      IDfTime future_disclosure_date = result.getResultDate(0, FUTURE_DISCLORE_DATE);
      IDfTime product_release_date = result.getResultDate(0, PRODUCT_RELEASE_DATE);
      IDfTime product_announcement_date = result.getResultDate(0, PRODUCT_ANNOUNCEMENT_DATE);
      IDfTime web_release_date = result.getResultDate(0, WEB_RELEASE_DATE);
      String disclosure_level = result.getResultString(0, DISCLOSURE_LEVEL);
      String future_disclosure_level = result.getResultString(0, FUTURE_DISCLOSURE_LEVEL);
      int future_disclosure_mode = -1;
      try {
        future_disclosure_mode = result.getResultInt(0, FUTURE_DISCLOSURE_MODE);
      } catch (NumberFormatException nfe) {
        // just leave it as -1
      }
        
      if ((disclosure_level != null && future_disclosure_level != null) &&
          !disclosure_level.trim().toLowerCase().equals(PUBLIC)
          && future_disclosure_level.trim().toLowerCase().equals(PUBLIC)) {

        switch (future_disclosure_mode) {
        case 0: {
          smallestDate = future_disclosure_date;
          break;
        }
        case 1: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_release_date);
          break;
        }
        case 2: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_announcement_date);
          break;
        }
        case 3: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_release_date);
          smallestDate = resolveEarliestDate(smallestDate, product_announcement_date);
          break;
        }
        case 4: {
          smallestDate = resolveEarliestDate(future_disclosure_date, web_release_date);
          break;
        }
        case 5: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_release_date);
          smallestDate = resolveEarliestDate(smallestDate, web_release_date);
          break;
        }
        case 6: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_announcement_date);
          smallestDate = resolveEarliestDate(smallestDate, web_release_date);
          break;
        }
        case 7: {
          smallestDate = resolveEarliestDate(future_disclosure_date, product_release_date);
          smallestDate = resolveEarliestDate(smallestDate, product_announcement_date);
          smallestDate = resolveEarliestDate(smallestDate, web_release_date);
          break;
        }
        }// ...switch
        
        if (smallestDate != null)
          element.addText(smallestDate.asString(IDfTimeConcentra.CONCENTRA_PATTERN));
      }
    } else {
      LoaderLog.error("No results from query "+token+" getPlannedPublicDate");
    }
    return element;
  }

  /**
   * Compares the two given dates and returns the earliest date
   * 
   * @param dateOne
   *            First date to be compared
   * @param dateTwo
   *            Second date to be compared
   * @return IDFTime with the earliest date
   */
  private IDfTime resolveEarliestDate(IDfTime dateOne, IDfTime dateTwo) {
    if (dateOne != null && !dateOne.isNullDate() && dateTwo != null && !dateTwo.isNullDate())
      return dateOne.getDate().compareTo(dateTwo.getDate()) <= 0 ? dateOne : dateTwo;
    else if (dateOne != null && !dateOne.isNullDate())
      return dateOne;
    else if (dateTwo != null && !dateTwo.isNullDate())
      return dateTwo;
    return null;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the rendition of the given document
   * 
   * @return Xml Element with the rendition
   */
  protected Element getRenditions() {
    Element renditions = null;

    LoaderLog.info("ConcentraDoc getRenditions");

    PreparedDQL linkCountquery = PreparedDQL.getPrepareDQL(QUERY_GET_LINK_COUNT, docbaseSession);
    linkCountquery.setParameter(0, objectId);
    Result resultLinkCount = linkCountquery.execute();
    String queryLoad = null;

    if (resultLinkCount != null) {
      String countResult = resultLinkCount.getValue(0, ELEMENT_R_LINK_CNT).toString();
      PreparedDQL commonInfo = null;
      if (countResult.equals("0")) {
          commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_RENDITIONS_COUNT_ZERO, docbaseSession);
          commonInfo.setParameter(0, objectId);
      } else {
    	  commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_RENDITIONS, docbaseSession);    	  
    	  commonInfo.setParameter(0, objectId);
          commonInfo.setParameter(1, objectId);
      }

      Result resultInfo = commonInfo.execute();

      if (resultInfo != null) {
        // Add Result to Element
        renditions = new BaseElement(ELEMENT_RENDITIONS);
        for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
          Object resultObject = resultInfo.getValue(childrenIter, ExtractorConstants.ELEMENT_OBJECT_NAME);
          if (resultObject != null) {
            /**
             * Create the following structure <rendition
             * content_type="doc"
             * last_updated_date="2007-11-15T05:41:30"
             * size="442880">
             */
            Element rendition = new BaseElement(ELEMENT_RENDITION);
            rendition.addAttribute(ATTRIBUTE_CONTENT_TYPE, resultInfo.getValue(childrenIter,
                DOS_EXTENSION).toString());
            rendition.addAttribute(ATTRIBUTE_LAST_UPDATED_DATE, resultInfo.getValue(childrenIter,
                SET_TIME).toString());
            rendition.addAttribute(ATTRIBUTE_SIZE, resultInfo.getValue(childrenIter, CONTENT_SIZE)
                .toString());
            String objName = resultInfo.getValue(childrenIter, ExtractorConstants.ELEMENT_OBJECT_NAME)
            .toString();
            String extName = resultInfo.getValue(childrenIter, DOS_EXTENSION).toString();
            if (!objName.equals(EMPTY_STRING) && !extName.equals(EMPTY_STRING)) {
              rendition.addText(objName+ExtractorConstants.DOT_EXTENSION+extName);
              renditions.add(rendition);
            }
          }
        }
      }
    }
    return renditions;
  }

  /**
   * Retrieves from Concentra database the properties defined by the query
   * 
   * @return Arraylist with the Elements containing the information needed.
   */
  protected ArrayList getGeneralInfo(String methodName, String queryName) {
    return getGeneralInfo(methodName, queryName, this.objectId);
  }

  /**
   * Retrieves from Concentra database the properties defined by the query
   * 
   * @return Arraylist with the Elements containing the information needed.
   */
  protected ArrayList getGeneralInfo(String methodName, String queryName, String objectId) {
    ArrayList info = null;


    LoaderLog.info("ConcentraDoc "+methodName);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      info = new ArrayList();
      String[] columnNames = resultInfo.getColumnsName();
      for (int elementsIter = 0; elementsIter < resultInfo.getColumnCount(); elementsIter++) {
        String currentColumn = columnNames[elementsIter];
        Object resultValue = resultInfo.getValue(0, currentColumn);

        if (resultValue != null) {
          Element element = new BaseElement(currentColumn);
          element.addText(resultValue.toString());
          info.add(element);
          /*
           * Validates if the element contains the information
           * necessary for the creation of the file name, avoiding
           * another query to Concentra in order to retrieve this
           * information
           */
          if (currentColumn.equals(ExtractorConstants.ELEMENT_OBJECT_NAME)) {
            objectName = resultValue.toString();
          }

        }
      }
    } else {
      LoaderLog.error("No results from query "+token+" "+queryName);
    }
    return info;
  }

  public synchronized String getDocumentName() {
    return objectName;
  }

  /***************************************************************************
   * Using the already existing information of the root xml document element,
   * generates the r_object_id element for the property file (i.e
   * "0900a5a5803ff897")
   * 
   * @return Xml element for the r_object_id
   */
  protected Element getObjectId() {
    Element elemObjectId = null;

    LoaderLog.info("ConcentraDoc getObjectId");

    if (objectId != null) {
      elemObjectId = new BaseElement(ELEMENT_OBJECT_ID);
      elemObjectId.addText(objectId);
    }
    return elemObjectId;
  }

  /***************************************************************************
   * Using the already existing information of the root xml document element,
   * generates the priority element for the property file (i.e "0")
   * 
   * @return Xml element for the priority
   */
  protected Element getPriority() {
    Element elemPriority = null;
    String priorityValue = Integer.toString(priority);

    LoaderLog.info("ConcentraDoc getPriority");

    if (priorityValue != null) {
      elemPriority = new BaseElement(ExtractorConstants.ELEMENT_PRIORITY);
      elemPriority.addText(priorityValue);
    }

    return elemPriority;
  }

  /***************************************************************************
   * Using the already existing information of the root xml document element,
   * generates the priority element for the property file (i.e "0")
   * 
   * @return Xml element for the priority
   */
  protected Element getActiveFlag() {
    Element elemActiveFlag = null;

    LoaderLog.info("ConcentraDoc getActiveFlag");

    if (activeFlag != null) {
      elemActiveFlag = new BaseElement(ELEMENT_ACTIVE_FLAG);
      elemActiveFlag.addText(activeFlag);
    }
    return elemActiveFlag;
  }

  /***************************************************************************
   * Retrieves from Concentra database the latest Future Disclosure Date to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the latest Future Disclosure Date
   */
  protected Element getFutureDisclosureDate() {
    return getSingleElementFromObjectTypeId("getFutureDisclosureDate",
        QUERY_GET_FUTURE_DISCLOSURE_DATE, ELEMENT_FUTURE_DISCLOSURE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the latest Future Disclosure Level to
   * be used as part of the property xml file
   * 
   * @return Xml Element with the latest Future Disclosure Level
   */
  protected Element getFutureDisclosureLevel() {
    return getSingleElementFromObjectTypeId("getFutureDisclosureLevel",
        QUERY_GET_FUTURE_DISCLOSURE_LEVEL, ELEMENT_FUTURE_DISCLOSURE_LEVEL);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Is Translation Flag
   * to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Is Translation Flag
   */
  protected Element getIsTranslationFlag() {
    return getSingleElementFromObjectTypeId("getIsTranslationFlag",
        QUERY_GET_IS_TRANSLATION_FLAG, ELEMENT_IS_TRANSLATION_FLAG);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Lifecycle State
   * Update Date to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Lifecycle State Update Date
   */
  protected Element getLifecycleStateUpdateDate() {
    return getSingleElementFromObjectTypeId("getLifecycleStateUpdateDate",
        QUERY_GET_LIFECYCLE_STATE_UPDATE_DATE, ELEMENT_LIFECYCLE_STATE_UPDATE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Lifecycle State
   * Update User to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Lifecycle State Update User
   */
  protected Element getLifecycleStateUpdateUser() {
    return getSingleElementFromObjectTypeId("getLifecycleStateUpdateUser",
        QUERY_GET_LIFECYCLE_STATE_UPDATE_USER, ELEMENT_LIFECYCLE_STATE_UPDATE_USER);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Property Update User
   * to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Property Update User
   */
  protected Element getPropertyUpdateUser() {
    return getSingleElementFromObjectTypeId("getPropertyUpdateUser",
        QUERY_GET_PROPERTY_UPDATE_USER, ELEMENT_PROPERTY_UPDATE_USER);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the CMG's name to be used
   * as part of the property xml file
   * 
   * @return Xml Element with the value for the CMG's name
   */
  protected Element getCmgName() {
    return getSingleElementFromObjectTypeId("getCmgName", QUERY_GET_CMG_NAME,
        ELEMENT_CMG_NAME);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Col Master Id to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the value for the Col Master Id
   */
  protected Element getColMasterId() {
    return getSingleElementFromObjectTypeObjectColumnId("getColMasterId",
        QUERY_GET_COL_MASTER_ID, ELEMENT_COL_MASTER_ID);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Collection Ref Update
   * Date to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Collection Ref Update Date
   */
  protected Element getCollectionRefUpdateDate() {
    return getSingleElementFromObjectColumnId("getCollectionRefUpdateDate",
        QUERY_GET_COLLECTION_REF_UPDATE_DATE, ELEMENT_COLLECTION_REF_UPDATE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Collection Update
   * Date to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Collection Update Date
   */
  protected Element getCollectionUpdateDate() {
    return getSingleElementFromObjectColumnId("getCollectionUpdateDate",
        QUERY_GET_COLLECTION_UPDATE_DATE, ELEMENT_COLLECTION_UPDATE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Collection Update
   * User to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Collection Update User
   */
  protected Element getCollectionUpdateUser() {
    return getSingleElementFromObjectColumnId("getCollectionUpdateUser",
        QUERY_GET_COLLECTION_UPDATE_USER, ELEMENT_COLLECTION_UPDATE_USER);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Product Announcement
   * Date to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Product Announcement Date
   */
  protected Element getProductAnnouncementDate() {
    return getSingleElementFromObjectColumnId("getProductAnnouncementDate",
        QUERY_GET_PRODUCT_ANNOUNCEMENT_DATE, ELEMENT_PRODUCT_ANNOUNCEMENT_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Product Release Date
   * to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Product Release Date
   */
  protected Element getProductReleaseDate() {
    return getSingleElementFromObjectColumnId("getProductReleaseDate",
        QUERY_GET_PRODUCT_RELEASE_DATE, ELEMENT_PRODUCT_RELEASE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Web Release Date to
   * be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Web Release Date
   */
  protected Element getWebReleaseDate() {
    return getSingleElementFromObjectColumnId("getWebReleaseDate",
        QUERY_GET_WEB_RELEASE_DATE, ELEMENT_WEB_RELEASE_DATE);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the IChronicle ID of the given
   * document
   * 
   * @return Xml Element with the IChronicle ID
   */
  protected Element getContentClass() {
    return getSingleElementFromObjectTypeId("getContentClass", QUERY_GET_CONTENT_CLASS,
        ELEMENT_CONTENT_CLASS);
  }
  
  /***************************************************************************
   * Retrieves from the Concentra database the IChronicle ID of the given
   * document
   * 
   * @return Xml Element with the IChronicle ID
   */
  protected Element getIChronicleId() {
    return getSingleElementFromObjectTypeId("getIChronicleId", QUERY_GET_I_CHRONICLE_ID,
        ELEMENT_I_CHRONICLE_ID);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Inventory Management of the
   * given document
   * 
   * @return Xml Element with the Inventory Management
   */
  protected Element getInventoryManagement() {
    return getSingleElementFromObjectTypeId("getInventoryManagement",
        QUERY_GET_INVENTORY_MANAGEMENT, ELEMENT_INVENTORY_MANAGEMENT);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Object Create Date of the given
   * document
   * 
   * @return Xml Element with the Object Create Date
   */
  protected Element getObjectCreateDate() {
    return getSingleElementFromObjectTypeId("getObjectCreateDate",
        QUERY_GET_OBJECT_CREATE_DATE, ELEMENT_OBJECT_CREATE_DATE);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Owner Name of the given
   * document
   * 
   * @return Xml Element with the Owner Name
   */
  protected Element getOwnerName() {
    return getSingleElementFromObjectTypeId("getOwnerName", QUERY_GET_OWNER_NAME,
        ELEMENT_OWNER_NAME);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Print Management Program of the
   * given document
   * 
   * @return Xml Element with the Print Management Program
   */
  protected Element getPrintManagementProgram() {
    return getElementFromObjectTypeId("getPrintManagementProgram",
        QUERY_GET_PRINT_MANAGEMENT_PROGRAM, ELEMENT_PRINT_MANAGEMENT_PROGRAMS,
        ELEMENT_PRINT_MANAGEMENT_PROGRAM, QUERY_COLUMN_PRINT_MANAGEMENT_PROGRAM);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Project Id of the given
   * document
   * 
   * @return Xml Element with the Project Id
   */
  protected Element getProjectId() {
    return getSingleElementFromObjectTypeId("getProjectId", QUERY_GET_PROJECT_ID,
        ELEMENT_PROJECT_ID);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Project Name of the given
   * document
   * 
   * @return Xml Element with the Project Name
   */
  protected Element getProjectName() {
    return getSingleElementFromObjectTypeId("getProjectName", QUERY_GET_PROJECT_NAME,
        ELEMENT_PROJECT_NAME);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the R Object Type of the given
   * document
   * 
   * @return Xml Element with the R Object Type
   */
  protected Element getRObjectType() {
    return getSingleElementFromObjectTypeId("getRObjectType", QUERY_GET_R_OBJECT_TYPE,
        ELEMENT_R_OBJECT_TYPE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the R Version Labels to
   * be used as part of the property xml file
   * 
   * @return Xml Element with the value for the R Version Labels
   */
  protected Element getRVersionLabels() {
    return getElementFromObjectTypeId("getRVersionLabels", QUERY_GET_R_VERSION_LABELS,
        ELEMENT_R_VERSION_LABELS, ELEMENT_R_VERSION_LABEL, QUERY_COLUMN_R_VERSION_LABEL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the Task Due Date of the given
   * document
   * 
   * @return Xml Element with the Task Due Date
   */
  protected Element getTaskDueDate() {
    return getSingleElementFromObjectTypeId("getTaskDueDate", QUERY_GET_TASK_DUE_DATE,
        ELEMENT_TASK_DUE_DATE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Document to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Document
   */
  protected Element getEdsDocument() {
    return getSingleElementFromObjectTypeId("getEdsDocument", QUERY_GET_EDS_DOCUMENT,
        ELEMENT_EDS_DOCUMENT);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Client Type to be
   * used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Client Type
   */
  protected Element getEdsClientType() {
    return getSingleElementFromObjectTypeId("getEdsClientType", QUERY_GET_EDS_CLIENT_TYPE,
        ELEMENT_EDS_CLIENT_TYPE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Client Quote to
   * be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Client Quote
   */
  protected Element getEdsClientQuote() {
    return getSingleElementFromObjectTypeId("getEdsClientQuote", QUERY_GET_EDS_CLIENT_QUOTE,
        ELEMENT_EDS_CLIENT_QUOTE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Gold Standard
   * Flag to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Gold Standard Flag
   */
  protected Element getEdsGoldStandardFlag() {
    return getSingleElementFromObjectTypeId("getEdsGoldStandardFlag",
        QUERY_GET_EDS_GOLD_STANDARD_FLAG, ELEMENT_EDS_GOLD_STANDARD_FLAG);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Business Issues
   * to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Business Issues
   */
  protected Element getEdsBusinessIssues() {
    return getElementFromObjectTypeId("getEdsBusinessIssues", QUERY_GET_EDS_BUSINESS_ISSUES,
        ELEMENT_EDS_BUSINESS_ISSUES, ELEMENT_EDS_BUSINESS_ISSUE, QUERY_COLUMN_EDS_BUSINESS_ISSUE);
  }

  /***************************************************************************
   * Retrieves from Concentra database the value for the Eds Market Technology
   * Trends to be used as part of the property xml file
   * 
   * @return Xml Element with the value for the Eds Market Technology Trends
   */
  
  //CR840/BR603977  Changes in EDS document properties
/*  protected Element getEdsMarketTechnologyTrends() {
    return getElementFromObjectTypeId("getEdsMarketTechnologyTrends",
        QUERY_GET_EDS_MARKET_TECHNOLOGY_TRENDS, ELEMENT_EDS_MARKET_TECHNOLOGY_TRENDS,
        ELEMENT_EDS_MARKET_TECHNOLOGY_TREND, QUERY_COLUMN_EDS_MARKET_TECHNOLOGY_TREND);
  }*/


  /**
   * KM3.1 (#BR_0939) - Get joint_product_collections/joint_product_collection elements
   * Query Used 
   *   select r_object_id as jpc_id, member_groups as product from c_jpc_view 
   *   where r_object_id in (select joint_product_collections from c_base_col 
   *   where r_object_id = ? ) and member_groups in 
   *   (select group_id from c_product_group_names where update_flag in ('M', 'C') or update_flag is null)
   *   
   *  
   * 1. Prepares joint_product_collections/joint_product_collection
   * 2. Takes the intersection of products and to the productGroups element passed
   */
  protected Element getJointProductCollections(Element productGroups) {

    List <String> jpcIds = new ArrayList<String>();
    Element jointProductCollections = new BaseElement(JOINT_PRODUCT_COLLECTIONS);
    PreparedDQL jpcInfo = PreparedDQL.getPrepareDQL(QUERY_GET_JPC_IDS, docbaseSession);
    jpcInfo.setParameter(0, colId);
    Result resultInfo = jpcInfo.execute();
    if (resultInfo != null) {
      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
        Object resultObject = resultInfo.getValue(childrenIter, ExtractorConstants.JPC_ID);

        if (resultObject != null) {
          String jpcId = resultObject.toString();
          if (jpcId != null && !jpcId.equals(EMPTY_STRING)) {
            Element jointProductCollection = new BaseElement(JOINT_PRODUCT_COLLECTION);
            jointProductCollection.addText(jpcId);
            jpcIds.add(jpcId);
            jointProductCollections.add(jointProductCollection);
          }
        }
      }
    }else{
      LoaderLog.error("getJointProductCollectionIds : No results from query for event Id :"+token);
    }
    resultInfo = null;

    if (jpcIds.size() > 0) {
      addIntersectionProductsToProductsElement(productGroups, jpcIds);
    }else{
      LoaderLog.error("getJointProductCollectionIds : No results from query for event Id :"+token);
    }
    return jointProductCollections;
  }

  /**
   * KM3.1 (#BR_0939)
   * 1. Get intersection of products from the productList
   * 2. Add the products to products element (adds only products which are not in list already)
   */
  private void addIntersectionProductsToProductsElement(Element productGroups, List<String> jpcIds) {
    Collection<String> intersectionProducts = new ArrayList<String>();
    intersectionProducts = getProductNumbers(jpcIds.get(0));
    if (intersectionProducts.size() == 0) {
      return;
    }
    for (int i = 1; i < jpcIds.size(); i++) {
      List<String> list = getProductNumbers(jpcIds.get(i));
      if(list.isEmpty()){
        return;
      }
      intersectionProducts = CollectionUtils.intersection(intersectionProducts, list);	
    }

    for(String value : intersectionProducts) {
      //If this product is already added as part of products selection or CPG selection,
      //so not add this product
      if( !docProdGroups.contains(value) ){
        Element children = new BaseElement(ExtractorConstants.ELEMENT_PRODUCT);
        children.addText(value);
        productGroups.add(children);
      }
    }
  }

  private List<String> getProductNumbers(String jpcId){
    List<String> pnList = new ArrayList<String>();
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PRODUCTNUMBER_INFO, docbaseSession);
    commonInfo.setParameter(0, jpcId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
        Object resultObject = resultInfo.getValue(childrenIter, "product_number");
        if (resultObject != null) {
          pnList.add(resultObject.toString());
        }
      }
    }

    return pnList;
  }

  /**
   * SMO : US #7469
   * New method added to get Company Information  
   * @return
   */
  protected String getCompanyName(String object_id) {
	    String comapnyName="";
	  	LoaderLog.info("ConcentraDoc getCompany");
	    PreparedDQL companyInfo = PreparedDQL.getPrepareDQL(QUERY_GET_COMPANY, docbaseSession);
	    companyInfo.setParameter(0, object_id);
	    Result resultInfo = companyInfo.execute();
        
        if (resultInfo != null) {
	        Object elementToAdd = resultInfo.getValue(0, ExtractorConstants.COMPANY_NAME);
	        if(elementToAdd!=null)
	            comapnyName= elementToAdd.toString();
        }
	    return comapnyName;
	  }

  /**
   * SMO : US #7469
   * New method added to get Company Information  
   * @return
   */
  protected Element getCompany(String object_id) {
	    Element elemCompany =  new BaseElement(ExtractorConstants.ELEMENT_COMPANY);
	    String elementToAdd = getCompanyName(object_id);
	    elemCompany.addText(elementToAdd);
	     if(elementToAdd.isEmpty())
		 LoaderLog.error("No results from query "+token+" getCompany");
	      return elemCompany;
	  }
}
