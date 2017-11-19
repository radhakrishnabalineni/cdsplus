package com.hp.concentra.extractor.documents;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.core.document.ComponentBean;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.StringOps;
import com.hp.concentra.DQLParameter;
import com.hp.concentra.PreparedDQL;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.Result;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Support type document
 * property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class SupportDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

  private static final String ELEMENT_COMPONENT_NAME = "component_name";
  private static final String ATTACHMENT = "attachment";
  private static final String INLINE = "inline";
  private static final String ATTRIBUTE_TYPE = "type";
  private static final String ELEMENT_ESSO_REGION = "esso_region";
  private static final String ELEMENT_ESSO_REGIONS = "esso_regions";
  private static final String ELEMENT_ENVIRONMENT_DETAILS = "environment_details";
  private static final String ELEMENT_ENVIRONMENT_DETAIL = "environment_detail";
  private static final String ELEMENT_CONTENT_TOPIC_DETAILS = "content_topic_details";
  private static final String ELEMENT_CONTENT_TOPIC_DETAIL = "content_topic_detail";
  private static final String ELEMENT_ENVIRONMENTS = "environments";
  private static final String ELEMENT_ENVIRONMENT = "environment";
  private static final String ELEMENT_ISSUE_NUMBERS = "issue_numbers";
  private static final String ELEMENT_ISSUE_NUMBER = "issue_number";
  private static final String ELEMENT_MAIN_COMPONENT_DETAILS = "main_component_details";
  private static final String ELEMENT_MAIN_COMPONENT_DETAIL = "main_component_detail";
  private static final String ELEMENT_MINOR_COMPONENT1_DETAILS = "minor_component1_details";
  private static final String ELEMENT_MINOR_COMPONENT1_DETAIL = "minor_component1_detail";
  private static final String ELEMENT_MINOR_COMPONENT2_DETAILS = "minor_component2_details";
  private static final String ELEMENT_MINOR_COMPONENT2_DETAIL = "minor_component2_detail";
  private static final String ELEMENT_PRODUCT_FUNCTION_DETAILS = "product_function_details";
  private static final String ELEMENT_PRODUCT_FUNCTION_DETAIL = "product_function_detail";
  private static final String ELEMENT_SERIAL_NUMBER_BEGINNINGS = "serial_number_beginnings";
  private static final String ELEMENT_SERIAL_NUMBER_BEGINNING = "serial_number_beginning";
  private static final String ELEMENT_SERIAL_NUMBER_ENDINGS = "serial_number_endings";
  private static final String ELEMENT_SERIAL_NUMBER_ENDING = "serial_number_ending";
  private static final String ELEMENT_SERIAL_NUMBER_PRODUCTS = "serial_number_products";
  private static final String ELEMENT_SERIAL_NUMBER_PRODUCT = "serial_number_product";
  private static final String ELEMENT_SYMPTOM_DETAILS = "symptom_details";
  private static final String ELEMENT_SYMPTOM_DETAIL = "symptom_detail";
  private static final String ELEMENT_USER_TASK_DETAILS = "user_task_details";
  private static final String ELEMENT_USER_TASK_DETAIL = "user_task_detail";
  private static final String ELEMENT_OPERATINGS_SYSTEM_GROUPS = "operating_system_groups";
  private static final String ELEMENT_OPERATINGS_SYSTEM_GROUP = "operating_system_group";
  private static final String ELEMENT_SOFTWARE_GROUPS = "software_groups";
  private static final String ELEMENT_SOFTWARE_GROUP = "software_group";
  private static final String ELEMENT_FAQ_REGIONS = "faq_regions";
  private static final String ELEMENT_FAQ_REGION = "faq_region";
  private static final String ELEMENT_ACTION_REQUIRED = "action_required";
  private static final String ELEMENT_FAQ_PRODUCTS = "faq_products";
  private static final String ELEMENT_BIZ_DEFINED_PROPERTIES = "biz_defined_properties";
  private static final String ELEMENT_BIZ_DEFINED_PROPERTY = "biz_defined_property";
  private static final String ELEMENT_BIZ_DEFINED_PROPERTY_NAME = "biz_defined_property_name";
  private static final String ELEMENT_BIZ_DEFINED_PROPERTY_VALUES = "biz_defined_property_values";
  private static final String ELEMENT_BIZ_DEFINED_PROPERTY_VALUE = "biz_defined_property_value";
  private static final String ELEMENT_MDO_PARENT_IDS = "mdo_parent_ids";
  private static final String ELEMENT_MDO_PARENT_ID = "mdo_parent_id";

  private static final String ELEMENT_COL_CONTENT_ASSOCIATIONS = "col_content_associations";
  private static final String ELEMENT_COL_ASSOC_CONTENT = "col_assoc_content";
  private static final String ELEMENT_COL_ASSOC_CONTENT_NAME = "col_assoc_content_name";
  private static final String ELEMENT_COL_ASSOC_CONTENT_VALUES = "col_assoc_content_values";
  private static final String ELEMENT_COL_ASSOC_CONTENT_VALUE = "col_assoc_content_value";

  private static final String ELEMENT_DOC_CONTENT_ASSOCIATIONS = "doc_content_associations";
  private static final String ELEMENT_DOC_ASSOC_CONTENT = "doc_assoc_content";
  private static final String ELEMENT_DOC_ASSOC_CONTENT_NAME = "doc_assoc_content_name";
  private static final String ELEMENT_DOC_ASSOC_CONTENT_VALUES = "doc_assoc_content_values";
  private static final String ELEMENT_DOC_ASSOC_CONTENT_VALUE = "doc_assoc_content_value";

  private static final String QUERY_GET_CONTENT_TOPIC_DETAILS = "getContentTopicDetails";
  private static final String QUERY_GET_ENVIRONMENT_DETAILS = "getEnvironmentDetails";
  private static final String QUERY_GET_ENVIRONMENTS = "getEnvironments";
  private static final String QUERY_GET_ISSUE_NUMBERS = "getIssueNumbers";
  private static final String QUERY_GET_MAIN_COMPONENT_DETAILS = "getMainComponentDetails";
  private static final String QUERY_GET_MINOR_COMPONENT1_DETAILS = "getMinorComponent1Details";
  private static final String QUERY_GET_MINOR_COMPONENT2_DETAILS = "getMinorComponent2Details";
  private static final String QUERY_GET_PRODUCT_FUNCTION_DETAILS = "getProductFunctionDetails";
  private static final String QUERY_GET_SERIAL_NUMBER_BEGINNINGS = "getSerialNumberBeginnings";
  private static final String QUERY_GET_SERIAL_NUMBER_ENDINGS = "getSerialNumberEndings";
  private static final String QUERY_GET_SERIAL_NUMBER_PRODUCTS = "getSerialNumberProducts";
  private static final String QUERY_GET_SYMPTOM_DETAILS = "getSymptomDetails";
  private static final String QUERY_GET_USER_TASK_DETAILS = "getUserTaskDetails";
  private static final String QUERY_GET_OPERATINGS_SYSTEM_GROUPS = "getOperatingSystemGroups";
  private static final String QUERY_GET_SOFTWARE_GROUPS = "getSoftwareGroups";
  private static final String QUERY_GET_FAQ_REGIONS = "getFaqRegions";
  private static final String QUERY_GET_SUPPORT_DOC_INFO_ELEMENTS = "getSupportDocInfo";
  private static final String QUERY_GET_FAQ_PRODUCT_GROUPS = "getFaqProductGroups";
  private static final String QUERY_GET_SUPPORT_DOC_INFO_SINGLE_JOINS = "getSupportDocInfoSingleJoins";
  private static final String QUERY_GET_ACTION_REQUIRED = "getActionRequired";
  private static final String QUERY_ESSO_REGIONS = "getEssoRegions";
  private static final String QUERY_FAILURE_CODE = "getFailureCode";
  private static final String QUERY_GET_BIZ_DEFINED_PROPERTIES = "getBizDefinedProperties";
  //KM3.1 (#BR_0906) - Query
  private static final String QUERY_GET_COL_BIZ_DEFINED_PROPERTIES = "getColBizDefinedProperties";

  private static final String QUERY_GET_MDO_PARENT_IDS = "getMDOPatentIDs";
  private static final String QUERY_GET_COL_CONTENT_ASSOCIATIONS = "getColContentAssociations";
  private static final String QUERY_GET_DOC_CONTENT_ASSOCIATIONS = "getDocContentAssociations";

  //KM3.1 (#BR_0912) - XML Elements 
  private static final String CUSTOM_PRODUCT_GROUPS  = "custom_product_groups";
  private static final String CUSTOM_PRODUCT_GROUP  = "custom_product_group";
  private static final String FAQ_CUSTOM_PRODUCT_GROUPS  = "faq_custom_product_groups";
  private static final String FAQ_CUSTOM_PRODUCT_GROUP  = "faq_custom_product_group";

  // Column names for the elements to be generated
  private static final String QUERY_COLUMN_MDO_PARENT_IDS = "mdo_parent_id";
  
  //For Taxonomy Changes 
  private static final String QUERY_GET_TMS_CATEGORIZATION_TOPICS = "getTmsCategorizationTopics";
  

  /***************************************************************************
   * Default class constructor
   * 
   * @param objectId
   *            Unique identifier of the document object id
   * @param chronicleId
   *            Unique identifier of the document chronicle id
   * @param token
   *            Latest document token
   * @param objectType
   *            Document object type
   */
  public SupportDoc(String objectId, String chronicleId, String token, String objectType, String colId, int priority,
      String activeFlag) {
    super(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
  }
  
  /***************************************************************************
   * Retrieves for this specific type of document, all the necessary
   * information needed to finish the creation of the property.xml file
   * 
   * @param productGroups
   *            that need to be check to determine if the element has valid
   *            products
   * @throws DmRepositoryException 
   * @throws DfException 
   */
  protected ArrayList getSpecificDocInfo(Element productGroups) throws DfException, DmRepositoryException {
    ArrayList contentElements = new ArrayList();
    ArrayList supportInfo = getSupportInfo();
    ArrayList supportDocInfoSingleJoins = getSupportDocInfoSingleJoins();
    ArrayList contentElementsThatMightHaveNull = new ArrayList();

    contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
    contentElementsThatMightHaveNull.add(getContentUpdateDate());

    // Adds the active flag element
    contentElementsThatMightHaveNull.add(getActiveFlag());

    // TR132010 : Begin.
    contentElementsThatMightHaveNull.add(getContentUpdateUser());
    contentElementsThatMightHaveNull.add(getFutureDisclosureDate());
    contentElementsThatMightHaveNull.add(getFutureDisclosureLevel());
    contentElementsThatMightHaveNull.add(getIsTranslationFlag());
    contentElementsThatMightHaveNull.add(getLifecycleStateUpdateDate());
    contentElementsThatMightHaveNull.add(getLifecycleStateUpdateUser());
    contentElementsThatMightHaveNull.add(getPropertyUpdateUser());
    contentElementsThatMightHaveNull.add(getCmgName());
    contentElementsThatMightHaveNull.add(getColMasterId());
    contentElementsThatMightHaveNull.add(getCollectionRefUpdateDate());
    contentElementsThatMightHaveNull.add(getCollectionUpdateDate());
    contentElementsThatMightHaveNull.add(getCollectionUpdateUser());
    contentElementsThatMightHaveNull.add(getProductAnnouncementDate());
    contentElementsThatMightHaveNull.add(getProductReleaseDate());
    contentElementsThatMightHaveNull.add(getWebReleaseDate());
    contentElementsThatMightHaveNull.add(getIChronicleId());
    // TR132010 : End.
    contentElementsThatMightHaveNull.add(getContentTopicDetails());
    contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
    contentElementsThatMightHaveNull.add(getEnvironmentDetails());
    contentElementsThatMightHaveNull.add(getEnvironments());
    contentElementsThatMightHaveNull.add(getIssueNumbers());
    contentElementsThatMightHaveNull.add(getMainComponentDetails());
    contentElementsThatMightHaveNull.add(getMinorComponent1Details());
    contentElementsThatMightHaveNull.add(getMinorComponent2Details());
    contentElementsThatMightHaveNull.add(getProductFunctionDetails());
    contentElementsThatMightHaveNull.add(getProductLevels());
    contentElementsThatMightHaveNull.add(getSerialNumberBeginnings());
    contentElementsThatMightHaveNull.add(getSerialNumberEndings());
    contentElementsThatMightHaveNull.add(getSerialNumberProducts());
    contentElementsThatMightHaveNull.add(getSymptomDetails());
    contentElementsThatMightHaveNull.add(getUserTaskDetails());
    contentElementsThatMightHaveNull.add(getAuthors());
    contentElementsThatMightHaveNull.add(getFaqRegions());
    contentElementsThatMightHaveNull.add(getPublicationDestinations());
    contentElementsThatMightHaveNull.add(getRegions());
    contentElementsThatMightHaveNull.add(getEssoRegions());
    contentElementsThatMightHaveNull.add(getSearchKeywords());
    contentElementsThatMightHaveNull.add(getSoftwareGroups());
    contentElementsThatMightHaveNull.add(getOperatingSystemGroups());
    contentElementsThatMightHaveNull.add(getContentType());
    contentElementsThatMightHaveNull.add(getLanguageLabel());
    contentElementsThatMightHaveNull.add(getActionRequired());
    contentElementsThatMightHaveNull.add(getCollectionValidFlag());
    contentElementsThatMightHaveNull.add(getInformationSource());
    contentElementsThatMightHaveNull.add(getPublicationCode());
    contentElementsThatMightHaveNull.add(getAcronym());
    contentElementsThatMightHaveNull.add(getFeedbackAddress());
    contentElementsThatMightHaveNull.add(getFullTitle());
    contentElementsThatMightHaveNull.add(getPlannedPublicDate());
    contentElementsThatMightHaveNull.add(getMasterObjectName());
    //KM3.1 (#BR_0912) -- add cpgs and faq cpg elements
    contentElementsThatMightHaveNull.add(getCustomProductGroups());
    contentElementsThatMightHaveNull.add(getFaqProductGroups());
    contentElementsThatMightHaveNull.add(getFaqCustomProductGroups());
    //KM3.1 (#BR_0939) - Add JPC to XML
    contentElementsThatMightHaveNull.add(getJointProductCollections(productGroups));
    contentElementsThatMightHaveNull.add(getProjectName());
    contentElementsThatMightHaveNull.add(getExtraProperties());
    contentElementsThatMightHaveNull.add(getFileName());
    contentElementsThatMightHaveNull.add(getComponents());
    contentElementsThatMightHaveNull.add(getFailureCode());
    contentElementsThatMightHaveNull.add(getBizDefinedProperties());
    contentElementsThatMightHaveNull.add(getColContentAssociations());
    contentElementsThatMightHaveNull.add(getDocContentAssociations());
    contentElementsThatMightHaveNull.add(getMDOParentIDs());
    //Taxanomy Changes 
    contentElementsThatMightHaveNull.add(getTmsCategorizationTopics());
    

    Element hasValidProducts = new BaseElement(ExtractorConstants.ELEMENT_HAS_VALID_PRODUCTS);

    if (productGroups.element(ExtractorConstants.ELEMENT_PRODUCT) != null) {
      hasValidProducts.addText(ExtractorConstants.TRUE);
    } else {
      hasValidProducts.addText(ExtractorConstants.FALSE);
    }
    contentElementsThatMightHaveNull.add(hasValidProducts);

    for (int contentIter = 0; contentIter < contentElementsThatMightHaveNull.size(); contentIter++) {
      if (contentElementsThatMightHaveNull.get(contentIter) != null) {
        contentElements.add(contentElementsThatMightHaveNull.get(contentIter));
      }
    }
    for (int supportIter = 0; supportIter < supportInfo.size(); supportIter++) {
      if (supportInfo.get(supportIter) != null) {
        contentElements.add(supportInfo.get(supportIter));
      }
    }
    for (int supportJoinIter = 0; supportJoinIter < supportDocInfoSingleJoins.size(); supportJoinIter++) {
      if (supportDocInfoSingleJoins.get(supportJoinIter) != null) {
        contentElements.add(supportDocInfoSingleJoins.get(supportJoinIter));
      }
    }
    return contentElements;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's content topic
   * details
   * 
   * @return Xml element with the document's content topic details
   */
  private Element getEnvironmentDetails() {
    return getElementFromObjectTypeColumnId("getEnvironmentDetails",
        QUERY_GET_ENVIRONMENT_DETAILS, ELEMENT_ENVIRONMENT_DETAILS, ELEMENT_ENVIRONMENT_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's environment details
   * 
   * @return Xml element with the document's environment details
   */
  private Element getContentTopicDetails() {
    return getElementFromObjectTypeColumnId("getContentTopicDetails",
        QUERY_GET_CONTENT_TOPIC_DETAILS, ELEMENT_CONTENT_TOPIC_DETAILS, ELEMENT_CONTENT_TOPIC_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's environments
   * 
   * @return Xml element with the document's environments
   */
  private Element getEnvironments() {
    return getElementFromObjectTypeColumnId("getEnvironments", QUERY_GET_ENVIRONMENTS,
        ELEMENT_ENVIRONMENTS, ELEMENT_ENVIRONMENT);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's issue numbers
   * 
   * @return Xml element with the document's issue numbers
   */
  private Element getIssueNumbers() {
    return getElementFromObjectTypeColumnId("getIssueNumbers", QUERY_GET_ISSUE_NUMBERS,
        ELEMENT_ISSUE_NUMBERS, ELEMENT_ISSUE_NUMBER);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's main component
   * details
   * 
   * @return Xml element with the document's main component details
   */
  private Element getMainComponentDetails() {
    return getElementFromObjectTypeColumnId("getMainComponentDetails",
        QUERY_GET_MAIN_COMPONENT_DETAILS, ELEMENT_MAIN_COMPONENT_DETAILS, ELEMENT_MAIN_COMPONENT_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's minor component1
   * details
   * 
   * @return Xml element with the document's minor component1 details
   */
  private Element getMinorComponent1Details() {
    return getElementFromObjectTypeColumnId("getMinorComponent1Details",
        QUERY_GET_MINOR_COMPONENT1_DETAILS, ELEMENT_MINOR_COMPONENT1_DETAILS, ELEMENT_MINOR_COMPONENT1_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's minor component2
   * details
   * 
   * @return Xml element with the document's minor component2 details
   */
  private Element getMinorComponent2Details() {
    return getElementFromObjectTypeColumnId("getMinorComponent2Details",
        QUERY_GET_MINOR_COMPONENT2_DETAILS, ELEMENT_MINOR_COMPONENT2_DETAILS, ELEMENT_MINOR_COMPONENT2_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's product function
   * details
   * 
   * @return Xml element with the document's product function details
   */
  private Element getProductFunctionDetails() {
    return getElementFromObjectTypeColumnId("getProductFunctionDetails",
        QUERY_GET_PRODUCT_FUNCTION_DETAILS, ELEMENT_PRODUCT_FUNCTION_DETAILS, ELEMENT_PRODUCT_FUNCTION_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's main Serial Number
   * Beginnings
   * 
   * @return Xml element with the document's Serial Number Beginnings
   */
  private Element getSerialNumberBeginnings() {
    return getElementFromObjectTypeColumnId("getSerialNumberBeginnings",
        QUERY_GET_SERIAL_NUMBER_BEGINNINGS, ELEMENT_SERIAL_NUMBER_BEGINNINGS, ELEMENT_SERIAL_NUMBER_BEGINNING);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's SerialNumberEndings
   * 
   * @return Xml element with the document's SerialNumberEndings
   */
  private Element getSerialNumberEndings() {
    return getElementFromObjectTypeColumnId("getSerialNumberEndings",
        QUERY_GET_SERIAL_NUMBER_ENDINGS, ELEMENT_SERIAL_NUMBER_ENDINGS, ELEMENT_SERIAL_NUMBER_ENDING);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's SerialNumberProducts
   * 
   * @return Xml element with the document's SerialNumberProducts
   */
  private Element getSerialNumberProducts() {
    return getElementFromObjectTypeColumnId("getSerialNumberProducts",
        QUERY_GET_SERIAL_NUMBER_PRODUCTS, ELEMENT_SERIAL_NUMBER_PRODUCTS, ELEMENT_SERIAL_NUMBER_PRODUCT);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's SymptomDetails
   * 
   * @return Xml element with the document's SymptomDetails
   */
  private Element getSymptomDetails() {
    return getElementFromObjectTypeColumnId("getSymptomDetails", QUERY_GET_SYMPTOM_DETAILS,
        ELEMENT_SYMPTOM_DETAILS, ELEMENT_SYMPTOM_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's UserTaskDetails
   * 
   * @return Xml element with the document's UserTaskDetails
   */
  private Element getUserTaskDetails() {
    return getElementFromObjectTypeColumnId("getUserTaskDetails",
        QUERY_GET_USER_TASK_DETAILS, ELEMENT_USER_TASK_DETAILS, ELEMENT_USER_TASK_DETAIL);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's
   * operatingSystemGroups
   * 
   * @return Xml element with the document's operatingSystemGroups
   */
  private Element getOperatingSystemGroups() {
    return getElementFromObjectTypeColumnId("getOperatingSystemGroups",
        QUERY_GET_OPERATINGS_SYSTEM_GROUPS, ELEMENT_OPERATINGS_SYSTEM_GROUPS, ELEMENT_OPERATINGS_SYSTEM_GROUP);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's SoftwareGroups
   * 
   * @return Xml element with the document's SoftwareGroups
   */
  private Element getSoftwareGroups() {
    return getElementFromObjectTypeColumnId("getSoftwareGroups", QUERY_GET_SOFTWARE_GROUPS,
        ELEMENT_SOFTWARE_GROUPS, ELEMENT_SOFTWARE_GROUP);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's faqRegions
   * 
   * @return Xml element with the document's faqRegions
   */
  private Element getFaqRegions() {
    return getElementFromObjectTypeId("getFaqRegions", QUERY_GET_FAQ_REGIONS,
        ELEMENT_FAQ_REGIONS, ELEMENT_FAQ_REGION);
  }

  /***************************************************************************
   * Retrieves from the Concentra database the FAQ Product Groups of the given
   * document
   * 
   * @return Xml element with the document faq product groups
   * @throws Exception 
   */
  private Element getFaqProductGroups() {
    Element faqProductGroups = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();
    LoaderLog.info("SupportDoc getFaqProductGroups");
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_FAQ_PRODUCT_GROUPS, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      faqProductGroups = new BaseElement(ELEMENT_FAQ_PRODUCTS);
      Set productsResult = resolveProductEntries(resultInfo, ExtractorConstants.ELEMENT_PRODUCT);
      Iterator it = productsResult.iterator();
      while (it.hasNext()) {
        String value = (String) it.next();
        Element children = new BaseElement(ExtractorConstants.ELEMENT_PRODUCT);
        children.addText(value);
        faqProductGroups.add(children);
      }

    } else {
      LoaderLog.error("No results from query getFawProductGroups");
    }
    return faqProductGroups;
  }

  /***************************************************************************
   * Retrieves from Concentra database the support general properties that
   * have a single join
   * 
   * @return ArrayList with the Elements containing the support general
   *         properties that have a single join
   */
  private ArrayList getSupportDocInfoSingleJoins() {
    // Use the Collection Id as parameter
    return getGeneralInfo("getSupportDocInfoSingleJoins",
        QUERY_GET_SUPPORT_DOC_INFO_SINGLE_JOINS, this.colId);
  }

  /***************************************************************************
   * Retrieves from Concentra database the support general properties
   * 
   * @return ArrayList with the Elements containing the support general
   *         properties
   */
  private ArrayList getSupportInfo() {
    return getGeneralInfo("getSupportInfo", QUERY_GET_SUPPORT_DOC_INFO_ELEMENTS);
  }

  /**
   * Retrieves from the Concentra database the action required of the given
   * document
   * 
   * @return Xml element with the document action required element
   */
  private Element getActionRequired() {
    Element singleElement = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();

    LoaderLog.info("SupportDoc getActionRequired");
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_ACTION_REQUIRED, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(2, objectId);
    commonInfo.setParameter(3, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(4, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(5, objectId);
    Result resultInfo = commonInfo.execute();
    Object val = null;
    
    if (resultInfo != null) {
      // Add Result to Element
      singleElement = new BaseElement(ELEMENT_ACTION_REQUIRED);
      if ((val = resultInfo.getValue(0, ELEMENT_ACTION_REQUIRED)) != null) {
        singleElement.addText(val.toString());
      } else {
        singleElement.addText(ExtractorConstants.EMPTY_STRING);
      }
    } else {
      LoaderLog.error("No results from query "+token+" getActionRequired");
    }
    return singleElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's FailureCode
   * 
   * @return Xml element with the document's EssoRegions
   */
  private Element getFailureCode() {
    Element failureCodeElement = new BaseElement("failure_code");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_FAILURE_CODE, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultFailureCode = commonInfo.execute();
    if (resultFailureCode != null) {

      String[] columnNames = resultFailureCode.getColumnsName();
      for (int childrenIter = 0; childrenIter < resultFailureCode.getRowCount(); childrenIter++) {
        String failureCode = resultFailureCode.getValue(childrenIter, columnNames[0]).toString();
        if (!failureCode.equals("")) {
          failureCodeElement.addText(failureCode);
        }
      }
    } else {
      LoaderLog.error("No results from query "+token+ " getFailureCode");
    }
    return failureCodeElement;
  }

  /***************************************************************************
   * Retrieves from the Concentra database the document's EssoRegions
   * 
   * @return Xml element with the document's EssoRegions
   */
  private Element getEssoRegions() {
    Element essoRegionsElement = new BaseElement(ELEMENT_ESSO_REGIONS);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_ESSO_REGIONS, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultEssoRegions = commonInfo.execute();
    if (resultEssoRegions != null) {

      String[] columnNames = resultEssoRegions.getColumnsName();
      for (int childrenIter = 0; childrenIter < resultEssoRegions.getRowCount(); childrenIter++) {
        String essoContent = resultEssoRegions.getValue(childrenIter, columnNames[0]).toString();
        if (!essoContent.equals("")) {
          Element essoRegionElement = new BaseElement(ELEMENT_ESSO_REGION);
          essoRegionElement.addText(essoContent);
          essoRegionsElement.add(essoRegionElement);
        }
      }

    } else {
      LoaderLog.error("No results from query "+token+" getEssoRegions");
    }
    return essoRegionsElement;
  }

  /**
   * KM3.1 (#BR_0906) - Get Collection level BDPs and document level bdps and to the same list
   * 
   * Retrieves from Concentra the Business defined property values
   * 
   *      <biz_defined_properties>
   *			<biz_defined_property>
   *				<biz_defined_property_name>
   *						Prop1
   *				</biz_defined_property_name>
   *				<biz_defined_property_values>
   *					<biz_defined_property_value>
   *						valu1
   *					</biz_defined_property_value>
   *				</biz_defined_property_values>
   *			</biz_defined_property>
   *		</biz_defined_property>
   *
   * @return Xml Element with the Business defined property values
   */
  protected Element getBizDefinedProperties() {
    Element bizDefinedProperties = null;
    Element bizDefinedPropertyName = null;
    Element bizDefinedPropertyValues = null;
    Element bizDefinedPropertyValue = null;
    Element bizDefinedProperty = null;



    LoaderLog.info("SupportDoc getBizDefinedProperties");

    //get document level bizDefined Properties and add to map 
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_BIZ_DEFINED_PROPERTIES, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo_doc = commonInfo.execute();
    HashMap<String, List<String>> biZDefPropValuesMap = new HashMap<String, List<String>>();
    addBizDefinedToPropertyValuesMap(resultInfo_doc, biZDefPropValuesMap);

    //KM3.1 (#BR_0906) - get column level bizDefined Properties and add to map 
    PreparedDQL commonInfo_col = PreparedDQL.getPrepareDQL(QUERY_GET_COL_BIZ_DEFINED_PROPERTIES, docbaseSession);
    commonInfo_col.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo_col.setParameter(1, colId);
    Result resultInfo_col = commonInfo_col.execute();
    addBizDefinedToPropertyValuesMap(resultInfo_col, biZDefPropValuesMap);
    bizDefinedProperties = new BaseElement(ELEMENT_BIZ_DEFINED_PROPERTIES);
    if(biZDefPropValuesMap.size()>0){

      for (Map.Entry<String,List<String>> resultInfoEntry : biZDefPropValuesMap.entrySet() ) {

        String newProperName = resultInfoEntry.getKey() ;          	
        bizDefinedProperty = new BaseElement(ELEMENT_BIZ_DEFINED_PROPERTY);
        bizDefinedProperties.add(bizDefinedProperty);
        bizDefinedPropertyName = new BaseElement(ELEMENT_BIZ_DEFINED_PROPERTY_NAME);
        bizDefinedPropertyName.addText(newProperName);
        bizDefinedPropertyValues = new BaseElement(ELEMENT_BIZ_DEFINED_PROPERTY_VALUES);
        bizDefinedProperty.add(bizDefinedPropertyName);
        bizDefinedProperty.add(bizDefinedPropertyValues);
        List<String> newProperValues = resultInfoEntry.getValue();

        for(String newPropertyValue : newProperValues){

          bizDefinedPropertyValue = new BaseElement(ELEMENT_BIZ_DEFINED_PROPERTY_VALUE);
          bizDefinedPropertyValue.addText(newPropertyValue);
          bizDefinedPropertyValues.add(bizDefinedPropertyValue);

        }
      }
    }
    return bizDefinedProperties;
  }

  /***************************************************************************
   * Retrieves from Concentra the Collection Content Association property values
   * 
   * @return Xml Element with the Content Association property
   */
  protected Element getColContentAssociations() {
    Element ColContentAssociations = null;
    Element AssocContent = null;
    Element AssocContentName = null;
    Element AssocContentValues = null;
    Element AssocContentValue = null;
    String newPropertyName = null;
    String previousPropertyName = null;

    LoaderLog.info("SupportDoc getColContentAssociations");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_COL_CONTENT_ASSOCIATIONS, docbaseSession);

    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);

    Result resultInfo = commonInfo.execute();
    
    if (resultInfo != null) {
      // Add Result to Element
      ColContentAssociations = new BaseElement(ELEMENT_COL_CONTENT_ASSOCIATIONS);

      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {
        
        newPropertyName = resultInfo.getValue(childrenIter, ELEMENT_COL_ASSOC_CONTENT_NAME).toString();
        if (!(newPropertyName == null || newPropertyName.equals(""))) {
          if (previousPropertyName == null || !previousPropertyName.equals(newPropertyName)) {
            // Adds the <col_content_associations>

            AssocContent = new BaseElement(ELEMENT_COL_ASSOC_CONTENT);
            ColContentAssociations.add(AssocContent);
            // Adds the
            // <col_assoc_content_name>xxxx</col_assoc_content_name>
            AssocContentName = new BaseElement(ELEMENT_COL_ASSOC_CONTENT_NAME);
            AssocContentName.addText(newPropertyName);
            // Adds the <col_assoc_content_values>
            AssocContentValues = new BaseElement(ELEMENT_COL_ASSOC_CONTENT_VALUES);
            AssocContent.add(AssocContentName);
            AssocContent.add(AssocContentValues);
            previousPropertyName = newPropertyName;
          }

          // Adds the <col_assoc_content_value>some
          // value</col_assoc_content_value>
          AssocContentValue = new BaseElement(ELEMENT_COL_ASSOC_CONTENT_VALUE);
          AssocContentValue.addText(resultInfo.getValue(childrenIter,
              ELEMENT_COL_ASSOC_CONTENT_VALUE).toString());
          AssocContentValues.add(AssocContentValue);
        }
      }
    }
    return ColContentAssociations;
  }

  /***************************************************************************
   * Retrieves from Concentra the Collection Content Association property values
   * 
   * @return Xml Element with the Content Association property
   */
  protected Element getDocContentAssociations() {
    Element DocContentAssociations = null;
    Element DocAssocContent = null;
    Element DocAssocContentName = null;
    Element DocAssocContentValues = null;
    Element DocAssocContentValue = null;
    String newPropertyName = null;
    String previousPropertyName = null;

    LoaderLog.info("SupportDoc getDocContentAssociations");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_DOC_CONTENT_ASSOCIATIONS, docbaseSession);

    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);

    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      DocContentAssociations = new BaseElement(ELEMENT_DOC_CONTENT_ASSOCIATIONS);

      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {

        newPropertyName = resultInfo.getValue(childrenIter, ELEMENT_DOC_ASSOC_CONTENT_NAME).toString();
        if (!(newPropertyName == null || newPropertyName.equals(""))) {
          if (previousPropertyName == null || !previousPropertyName.equals(newPropertyName)) {
            // Adds the <col_content_associations>
            //System.out.println("inside Loop");
            DocAssocContent = new BaseElement(ELEMENT_DOC_ASSOC_CONTENT);
            DocContentAssociations.add(DocAssocContent);
            // Adds the
            // <col_assoc_content_name>xxxx</col_assoc_content_name>
            DocAssocContentName = new BaseElement(ELEMENT_DOC_ASSOC_CONTENT_NAME);
            DocAssocContentName.addText(newPropertyName);
            // Adds the <col_assoc_content_values>
            DocAssocContentValues = new BaseElement(ELEMENT_DOC_ASSOC_CONTENT_VALUES);
            DocAssocContent.add(DocAssocContentName);
            DocAssocContent.add(DocAssocContentValues);
            previousPropertyName = newPropertyName;
          }

          // Adds the <col_assoc_content_value>some
          // value</col_assoc_content_value>
          DocAssocContentValue = new BaseElement(ELEMENT_DOC_ASSOC_CONTENT_VALUE);
          DocAssocContentValue.addText(resultInfo.getValue(childrenIter,
              ELEMENT_DOC_ASSOC_CONTENT_VALUE).toString());
          DocAssocContentValues.add(DocAssocContentValue);
        }
      }
    }
    return DocContentAssociations;
  }

  /**
   * overridden from ConcentraDoc 
   * @param componentsElement
   * @param component
   * @param fileName
   * @return
   */
  protected void addComponentElement(Element componentsElement, ComponentBean component, String fileNameExt) {
    if (fileNameExt == null) {
      // duplicate component so don't add it
      return;
    }
    Element componentElement = new BaseElement(ELEMENT_COMPONENT);

    String content_type = component.getContentType();
    if (XML_EXTENSION.equalsIgnoreCase(content_type)) {
      componentElement.addAttribute(ATTRIBUTE_TYPE, INLINE);

      Element componentName = new BaseElement(ELEMENT_COMPONENT_NAME);
      componentName.addText(component.getObjectName()+fileNameExt);
      componentElement.add(componentName);

      // add the regions
      componentElement.add(getMultilineElement(component.getRegions(), 
          ExtractorConstants.ELEMENT_REGIONS, 
          ExtractorConstants.ELEMENT_REGION));

    } else {
      PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_COMPONENT, docbaseSession);
      commonInfo.setParameter(0, component.getChronicleId());
      Result resultInfo = commonInfo.execute();

      if (resultInfo != null) {

        Element compFileName = new BaseElement(ELEMENT_FILE_NAME);
        String extension = resultInfo.getValue(0, ExtractorConstants.ELEMENT_CONTENT_TYPE).toString();
        compFileName.addText(component.getObjectName()+fileNameExt+ExtractorConstants.DOT_EXTENSION+extension);

        componentElement.addAttribute(ATTRIBUTE_TYPE, ATTACHMENT);
        
        Element compSize = new BaseElement(ELEMENT_FILE_BYTES);
        compSize.addText(component.getFileSize());

        Element compVersion = new BaseElement(ExtractorConstants.ELEMENT_VERSION_LABEL);
        compVersion.addText(component.getSymbolicLabel());
        
        // add pieces to the component
        componentElement.add(compFileName);
        componentElement.add(compSize);
        componentElement.add(compVersion);
      }   
    }
    // add component to the components
    componentsElement.add(componentElement);				
  }

  /***************************************************************************
   * Retrieves from the Concentra database the mdo_parent_ids of the given
   * document
   * 
   * @return Xml element with the document type details
   */
  protected Element getMDOParentIDs() {
    return getElementFromObjectTypeColumnId("getMDOParentIDs", QUERY_GET_MDO_PARENT_IDS,
        ELEMENT_MDO_PARENT_IDS, ELEMENT_MDO_PARENT_ID, QUERY_COLUMN_MDO_PARENT_IDS);
  }

  public void addBizDefinedToPropertyValuesMap(Result resultInfo,HashMap<String, List<String>> biZDefPropResult){

    String newPropertyName = null;
    String previousPropertyName = null;


    if (resultInfo != null) {

      for(int resultCount = 0;resultCount<resultInfo.getRowCount();resultCount++){

        newPropertyName = resultInfo.getValue(resultCount, ELEMENT_BIZ_DEFINED_PROPERTY_NAME).toString();


        if (!(newPropertyName == null || newPropertyName.equals(""))) {

          String newPropertyValue = resultInfo.getValue(resultCount,ELEMENT_BIZ_DEFINED_PROPERTY_VALUE).toString();
          List<String> newPropValueList = null;

          if(!biZDefPropResult.containsKey(newPropertyName)){
            newPropValueList = new ArrayList<String>();
          }else{
            newPropValueList = biZDefPropResult.get(newPropertyName);
          }
          newPropValueList.add(newPropertyValue);
          biZDefPropResult.put(newPropertyName,newPropValueList);
        }
      }
    }
  }

  /**
   * KM3.1 (#BR_0912)
   * @return custom_product_groups/custom_product_group element
   * CPGs will be starting with 0000
   */
  protected Element getCustomProductGroups() {

    Element customproductGroups = null;

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PRODUCT_GROUPS, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      customproductGroups = new BaseElement(CUSTOM_PRODUCT_GROUPS);
      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {

        Object resultObject = resultInfo.getValue(childrenIter, ExtractorConstants.ELEMENT_PRODUCT);
        if (resultObject != null) {
          String resultValue = resultObject.toString();
          if (resultValue != null && !resultValue.equals(EMPTY_STRING)) {
            if ((resultValue.length() == R_OBJECT_ID_LENGTH && resultValue
                .startsWith(CPG_R_OBJECT_ID_BEGINNING))) {

              Element customproductGroup = new BaseElement(CUSTOM_PRODUCT_GROUP);
              customproductGroup.addText(resultValue);
              customproductGroups.add(customproductGroup);

            } 
          }
        }
      }
    } else {
      LoaderLog.error("Get CPGs return null result info");
    }

    return customproductGroups;
  }

  /**
   * KM3.1 (#BR_0912)
   * @return faq_custom_product_groups/faq_custom_product_group element
   * CPGs will be starting with 0000
   */
  protected Element getFaqCustomProductGroups() {
    Element faqCustomproductGroups = null;

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_FAQ_PRODUCT_GROUPS, docbaseSession);
    commonInfo.setParameter(0, getObjectColumn(), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, colId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      faqCustomproductGroups = new BaseElement(FAQ_CUSTOM_PRODUCT_GROUPS);
      for (int childrenIter = 0; childrenIter < resultInfo.getRowCount(); childrenIter++) {

        Object resultObject = resultInfo.getValue(childrenIter, ExtractorConstants.ELEMENT_PRODUCT);
        if (resultObject != null) {
          String productGroupId = resultObject.toString();
          if (productGroupId != null && !productGroupId.equals(EMPTY_STRING)) {

            if ((productGroupId.length() == R_OBJECT_ID_LENGTH && productGroupId
                .startsWith(CPG_R_OBJECT_ID_BEGINNING))) {


              Element faqCustomproductGroup = new BaseElement(FAQ_CUSTOM_PRODUCT_GROUP);
              faqCustomproductGroup.addText(productGroupId);
              faqCustomproductGroups.add(faqCustomproductGroup);

            } 
          }
        }
      }
    } else {
      LoaderLog.error("Get FAQ Products return null result info");
    }
    return faqCustomproductGroups;
  }

}
