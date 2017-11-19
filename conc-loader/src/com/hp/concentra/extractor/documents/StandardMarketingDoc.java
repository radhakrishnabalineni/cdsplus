package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.hp.concentra.DQLParameter;
import com.hp.concentra.PreparedDQL;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.Result;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Standard Marketing
 * document property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class StandardMarketingDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

  private static final String ELEMENT_PUBV_INITIAL_DATE = "pubv_initial_date";
  private static final String ELEMENT_PUBV_REORDER_QUANTITY = "pubv_reorder_quantity";
  private static final String ELEMENT_PUBV_INITIAL_AIR_QUANTITY = "pubv_initial_air_quantity";
  private static final String ELEMENT_PUBV_INITIAL_QUANTITY = "pubv_initial_quantity";
  private static final String ELEMENT_PUBV_UNIT_CODE = "pubv_unit_code";
  private static final String ELEMENT_PUBV_STRICT_HOLD_FLAG = "pubv_strict_hold_flag";
  private static final String ELEMENT_PUBV_RESTRICTION = "pubv_restriction";
  private static final String ELEMENT_PUBV_ORDER_LIMIT = "pubv_order_limit";
  private static final String ELEMENT_PUBV_INVENTORY_POLICY = "pubv_inventory_policy";
  private static final String ELEMENT_PUBV_VENDOR = "pubv_vendor";
  private static final String ELEMENT_PUB_VENDOR_DETAIL = "pub_vendor_detail";
  private static final String ELEMENT_PUB_VENDOR_DETAILS = "pub_vendor_details";
  private static final String ELEMENT_PARTNER_SEGMENT_CODE = "partner_segment_code";
  private static final String ELEMENT_WEB_NEWSLETTER_CODE = "web_newsletter_code";
  private static final String ELEMENT_TECHNICALITY_LEVEL_CODE = "technicality_level_code";
  private static final String ELEMENT_PUB_RETAIL_ACCOUNT = "pub_retail_account";
  private static final String ELEMENT_PARTNER_CODE = "partner_code";
  private static final String ELEMENT_OPERATING_SYSTEM_CODE = "operating_system_code";
  private static final String ELEMENT_OFFICE_COUNT_CODE = "office_count_code";
  private static final String ELEMENT_MARKETING_PROGRAM_CODE = "marketing_program_code";
  private static final String ELEMENT_JOB_RESPONSIBILITY_CODE = "job_responsibility_code";
  private static final String ELEMENT_IT_BUDGET_CODE = "it_budget_code";
  private static final String ELEMENT_INDUSTRY_VERTICAL_CODE = "industry_vertical_code";
  private static final String ELEMENT_INDUSTRY_SEGMENT_CODE = "industry_segment_code";
  private static final String ELEMENT_EMPLOYEE_COUNT_CODE = "employee_count_code";
  private static final String ELEMENT_CUSTOMER_CODE = "customer_code";
  private static final String ELEMENT_BUYER_ROLE_CODE = "buyer_role_code";
  private static final String ELEMENT_BUSINESS_FUNCTION_CODE = "business_function_code";
  private static final String ELEMENT_CUSTOMER_SEGMENT_CODE = "customer_segment_code";
  private static final String QUERY_GET_DOCUMENT_RELATIONS = "getDocumentRelations";
  private static final String QUERY_GET_COLLECTION_FULL_TITLE = "getCollectionFullTitle";
  private static final String QUERY_GET_PRIVATE_DOCUMENT_FLAG = "getPrivateDocumentFlag";
  private static final String QUERY_GET_REPEATING_STANDARD_MARKETING = "getRepeatingStandardMarketing";
  private static final String QUERY_GET_MANAGER_BACKUP = "getManagerBackup";
  private static final String QUERY_GET_PUB_MANAGER = "getPubManager";
  private static final String QUERY_GET_PUB_CONTROLLER = "getPubController";
  private static final String QUERY_GET_STANDARD_MARKETING_INFO = "getStandardMarketingInfo";
  private static final String QUERY_GET_EDS_DOCUMENT_SINGLE_ELEMENTS = "getEdsDocumentSingleElements";
  private static final String QUERY_GET_COLLECTION_SEARCH_KEYWORDS = "getCollectionSearchKeywords";
  private static final String QUERY_PUBV = "getAllPubvs";
  private static final String ELEMENT_DOCUMENT_RELATION = "document_relation";
  private static final String ELEMENT_DOCUMENT_RELATIONS = "document_relations";
  private static final String ELEMENT_COLLECTION_FULL_TITLE = "collection_full_title";
  private static final String ELEMENT_PRIVATE_DOCUMENT_FLAG = "private_document_flag";
  private static final String ELEMENT_PUB_MANAGER_BACKUP = "pub_manager_backup";
  private static final String EMPTY_STRING = "";
  private static final String ELEMENT_PUB_MANAGER = "pub_manager";
  private static final String ELEMENT_PUB_CONTROLLER = "pub_controller";
  private static final String ELEMENT_COLLECTION_SEARCH_KEYWORDS = "collection_search_keywords";
  private static final String ELEMENT_COLLECTION_SEARCH_KEYWORD = "collection_search_keyword";
  private static final String ELEMENT_CRM_ASSET_CODE = "crm_asset_code";
  private static final String QUERY_GET_CRM_ASSET_CODE = "getCrmAssetCode";
  //Concentra 14.4 Matt Nidoh added additional fields for std marketing docs
  private static final String ELEMENT_GSB_APPLICATION_TYPE = "gsb_application_type";
  private static final String ELEMENT_GSB_CONTENT_TYPE = "gsb_content_type";
  private static final String ELEMENT_GSB_BUSINESS_TYPE = "gsb_business_type";
  private static final String ELEMENT_UNIT_PRICE = "stock_unit_price";
  private static final String ELEMENT_UNIT_WEIGHT = "stock_unit_weight";



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
   * @param colId
   *            Unique identifier of the document collection id
   */

  public StandardMarketingDoc(String objectId, String chronicleId, String token, String objectType, String colId,
      int priority, String activeFlag) {
    super(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
  }
  
  /***************************************************************************
   * Retrieves for this specific type of document, all the necessary
   * information needed to finish the creation of the property.xml file
   * 
   * @param productGroups
   *            that need to be check to determine if the element has valid
   *            products
   */
  protected ArrayList getSpecificDocInfo(Element productGroups) {
    ArrayList contentElements = new ArrayList();
    ArrayList standardMarketingInfo = getStandardMarketingInfo();
    ArrayList repeatingElementsInfo = getRepeatingElementsStandardMarketing();
    ArrayList contentElementsThatMightHaveNull = new ArrayList();
    // TR132010 : Begin.
    contentElementsThatMightHaveNull.add(getAddrCountryCode());
    contentElementsThatMightHaveNull.add(getBusinessGroup());
    // already business_units
    contentElementsThatMightHaveNull.add(getCmgName());
    contentElementsThatMightHaveNull.add(getColMasterId());
    contentElementsThatMightHaveNull.add(getCollectionRefUpdateDate());
    contentElementsThatMightHaveNull.add(getCollectionUpdateDate());
    contentElementsThatMightHaveNull.add(getCollectionUpdateUser());
    contentElementsThatMightHaveNull.add(getCollectionValidFlag());
    contentElementsThatMightHaveNull.add(getContentClass());
    contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
    contentElementsThatMightHaveNull.add(getFutureDisclosureDate());
    contentElementsThatMightHaveNull.add(getIChronicleId());
    contentElementsThatMightHaveNull.add(getInventoryManagement());
    contentElementsThatMightHaveNull.add(getLifecycleStateUpdateDate());
    contentElementsThatMightHaveNull.add(getLifecycleStateUpdateUser());
    contentElementsThatMightHaveNull.add(getObjectCreateDate());
    contentElementsThatMightHaveNull.add(getOwnerName());
    contentElementsThatMightHaveNull.add(getPrintManagementProgram());
    contentElementsThatMightHaveNull.add(getProjectId());
    contentElementsThatMightHaveNull.add(getProjectName());
    contentElementsThatMightHaveNull.add(getPropertyUpdateUser());
    // already pub_controller
    // already pub_manager
    contentElementsThatMightHaveNull.add(getRObjectType());
    contentElementsThatMightHaveNull.add(getRVersionLabels());
    contentElementsThatMightHaveNull.add(getTaskDueDate());
    ArrayList edsSingleElementsInfo = getEdsSingleElements();
    for (int edsSingleIter = 0; edsSingleIter < edsSingleElementsInfo.size(); edsSingleIter++) {
      if (edsSingleElementsInfo.get(edsSingleIter) != null) {
        contentElements.add(edsSingleElementsInfo.get(edsSingleIter));
      }
    }
    contentElementsThatMightHaveNull.add(getEdsBusinessIssues());
    //CR840/BR603977  Changes in EDS document properties
   // contentElementsThatMightHaveNull.add(getEdsMarketTechnologyTrends());
    // TR132010 : End.
    contentElementsThatMightHaveNull.add(getRegions());
    contentElementsThatMightHaveNull.add(getAuthors());
    contentElementsThatMightHaveNull.add(getSearchKeywords());
    contentElementsThatMightHaveNull.add(getCrmAssetCode());
    contentElementsThatMightHaveNull.add(getAcronym());
    contentElementsThatMightHaveNull.add(getFeedbackAddress());
    contentElementsThatMightHaveNull.add(getPrivateDocumentFlag());
    contentElementsThatMightHaveNull.add(getMasterObjectName());
    contentElementsThatMightHaveNull.add(getLanguageLabel());
    contentElementsThatMightHaveNull.add(getFullTitle());
    contentElementsThatMightHaveNull.add(getCollectionFullTitle());
    contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
    contentElementsThatMightHaveNull.add(getContentUpdateDate());
    contentElementsThatMightHaveNull.add(getCollectionSearchKeywords());
    contentElementsThatMightHaveNull.add(getProductLevels());
    contentElementsThatMightHaveNull.add(getAudiences());
    contentElementsThatMightHaveNull.add(getDocumentRelations());
    contentElementsThatMightHaveNull.add(getExtraProperties());
    contentElementsThatMightHaveNull.add(getPubController());
    contentElementsThatMightHaveNull.add(getPubManager());
    contentElementsThatMightHaveNull.add(getPubManagerBackup());
    contentElementsThatMightHaveNull.add(getBusinessUnits());
    contentElementsThatMightHaveNull.add(getOrganization());
    contentElementsThatMightHaveNull.add(getContentTypes());
    contentElementsThatMightHaveNull.add(getPubvElementsStructure());
    contentElementsThatMightHaveNull.add(getRenditions());


    for (int contentIter = 0; contentIter < contentElementsThatMightHaveNull.size(); contentIter++) {
      if (contentElementsThatMightHaveNull.get(contentIter) != null) {
        contentElements.add(contentElementsThatMightHaveNull.get(contentIter));
      }
    }
    for (int standardMarketingIter = 0; standardMarketingIter < standardMarketingInfo.size(); standardMarketingIter++) {
      if (standardMarketingInfo.get(standardMarketingIter) != null) {
        contentElements.add(standardMarketingInfo.get(standardMarketingIter));
      }
    }

    for (int standardMarketingIter = 0; standardMarketingIter < repeatingElementsInfo.size(); standardMarketingIter++) {
      if (repeatingElementsInfo.get(standardMarketingIter) != null) {
        contentElements.add(repeatingElementsInfo.get(standardMarketingIter));
      }
    }
    return contentElements;
  }

  private Element getCollectionSearchKeywords() {
    return getElementFromId("getCollectionSearchKeywords",
        QUERY_GET_COLLECTION_SEARCH_KEYWORDS, ELEMENT_COLLECTION_SEARCH_KEYWORDS,
        ELEMENT_COLLECTION_SEARCH_KEYWORD);
  }

  /**
   * Groups all the elements from RepeatingJoinDocPropertyRule and iterates
   * over a collection of fields in order to create the corresponding element
   * 
   * @return A list of elements from RepeatingJoinDocPropertyRule for Standard
   *         marketing document
   */
  private ArrayList getRepeatingElementsStandardMarketing() {
    final String PLURAL_SUFIX_S = "s";
    final String PLURAL_SUFIX_IES = "ies";

    ArrayList groupElements = new ArrayList();
    String[] fields = { ELEMENT_CUSTOMER_SEGMENT_CODE, ELEMENT_BUSINESS_FUNCTION_CODE, ELEMENT_BUYER_ROLE_CODE,
        ELEMENT_CUSTOMER_CODE, ELEMENT_EMPLOYEE_COUNT_CODE, ELEMENT_INDUSTRY_SEGMENT_CODE,
        ELEMENT_INDUSTRY_VERTICAL_CODE, ELEMENT_IT_BUDGET_CODE, ELEMENT_JOB_RESPONSIBILITY_CODE,
        ELEMENT_MARKETING_PROGRAM_CODE, ELEMENT_OFFICE_COUNT_CODE, ELEMENT_OPERATING_SYSTEM_CODE,
        ELEMENT_PARTNER_CODE, ELEMENT_PUB_RETAIL_ACCOUNT, ELEMENT_TECHNICALITY_LEVEL_CODE,
        ELEMENT_WEB_NEWSLETTER_CODE, ELEMENT_PARTNER_SEGMENT_CODE };

    for (int index = 0; index < fields.length; index++) {
      String pluralWord = null;
      if (fields[index].endsWith("y")) {
        String rawPlural = fields[index].substring(0, fields[index].length() - 1);
        pluralWord = rawPlural.concat(PLURAL_SUFIX_IES);
      } else {
        pluralWord = fields[index].concat(PLURAL_SUFIX_S);
      }
      Element newElement = buildDeepElements(pluralWord, pluralWord, fields[index]);
      if (newElement != null) {
        groupElements.add(newElement);
      }
    }
    return groupElements;
  }

  /**
   * Build the following structure <parents><child/><child/><child/></parents>
   * 
   * @param fieldName
   *            Use as parameter in the dql to complete the query
   * @param parentElement
   *            Plural of field name used as a parent element
   * @param childElement
   *            Field name used as child element name
   * @return Element
   */
  private Element buildDeepElements(String fieldName, String parentElement, String childElement) {
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_REPEATING_STANDARD_MARKETING, docbaseSession);
    commonInfo.setParameter(0, fieldName.concat(" as ").concat(childElement), DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();
    if (resultInfo != null) {
      return getMultilineElement(resultInfo, parentElement, childElement);
    } else {
      LoaderLog.error("No results from query getRepeatingStandardMarketing");
      return null;
    }
  }

  /***************************************************************************
   * Retrieves from Concentra database the standard marketing EDS specific
   * properties
   * 
   * @return ArrayList with the Elements containing the standard marketing EDS
   *         document specific properties
   */
  protected ArrayList getEdsSingleElements() {
    ArrayList edsDocSingleElements = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();
    LoaderLog.info("StandardMarketingDoc getEdsSingleElements");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_EDS_DOCUMENT_SINGLE_ELEMENTS, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();
    if (resultInfo != null) {
      edsDocSingleElements = new ArrayList();
      String[] columnNames = resultInfo.getColumnsName();
      for (int elementsIter = 0; elementsIter < resultInfo.getColumnCount(); elementsIter++) {
        String currentColumn = columnNames[elementsIter];
        Object resultValue = resultInfo.getValue(0, currentColumn);
        if (resultValue != null) {
          Element element = new BaseElement(currentColumn);
          element.addText(resultValue.toString());
          edsDocSingleElements.add(element);

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
      edsDocSingleElements = null;
      LoaderLog.error("No results from query getEdsDocumentSingleElements");
    }
    return edsDocSingleElements;
  }

  /***************************************************************************
   * Retrieves from Concentra database the standard marketing specific
   * properties
   * 
   * @return ArrayList with the Elements containing the standard marketing
   *         document specific properties
   */
  private ArrayList getStandardMarketingInfo() {
    ArrayList standardMarketingElements = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();

    LoaderLog.info("StandardMarkeginDoc getStandardMarketingInfo");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_STANDARD_MARKETING_INFO, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();
    if (resultInfo != null) {
      standardMarketingElements = new ArrayList();
      String[] columnNames = resultInfo.getColumnsName();
      for (int elementsIter = 0; elementsIter < resultInfo.getColumnCount(); elementsIter++) {
        String currentColumn = columnNames[elementsIter];
        Object resultValue = resultInfo.getValue(0, currentColumn);
        if (resultValue != null) {
          Element element = new BaseElement(currentColumn);
          element.addText(resultValue.toString());
          standardMarketingElements.add(element);

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
      standardMarketingElements = null;
      LoaderLog.error("No results from query getStandardMarketingInfo");
    }

    return standardMarketingElements;
  }

  /**
   * Retrieves the Pub Controller element needed on the Standard Marketing
   * property file
   * 
   * @return Xml Element with the pub controller information
   */
  private Element getPubController() {
    LoaderLog.info("StandardMarketingDoc getPubController");

    return getPubElement(QUERY_GET_PUB_CONTROLLER, ELEMENT_PUB_CONTROLLER);
  }

  /**
   * Retrieves the Pub Manager element needed on the Standard Marketing
   * property file
   * 
   * @return Xml Element with the pub manager information
   */
  private Element getPubManager() {
    LoaderLog.info("StandardMarketingDoc getPubManager");

    return getPubElement(QUERY_GET_PUB_MANAGER, ELEMENT_PUB_MANAGER);
  }

  /**
   * Retrieves the Pub Manager Backup element needed on the Standard Marketing
   * property file
   * 
   * @return Xml Element with the pub manager backup information
   */
  private Element getPubManagerBackup() {
    LoaderLog.info("StandardMarketingDoc getPubManagerBackup");

    return getPubElement(QUERY_GET_MANAGER_BACKUP, ELEMENT_PUB_MANAGER_BACKUP);
  }

  /**
   * Retrieves from Concentra the requested pub content for the standard
   * marketing doc
   * 
   * @param queryName
   *            Name of the Query to be Executed
   * @param pubElementName
   *            Name of the element to be created
   * @return Xml Element with the Pub content requested for the standard
   *         marketing document
   */
  private Element getPubElement(String queryName, String pubElementName) {
    Element pubElement = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();
    LoaderLog.info("StandardMarketingDoc getPubElement");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(queryName, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();
    if (resultInfo != null) {
      pubElement = new BaseElement(pubElementName);
      String[] columnNames = resultInfo.getColumnsName();
      for (int elementsIter = 0; elementsIter < resultInfo.getColumnCount(); elementsIter++) {
        String currentColumn = columnNames[elementsIter];
        Object resultValue = resultInfo.getValue(0, currentColumn);
        Element element = new BaseElement(currentColumn);
        if (resultValue != null) {
          element.addText(resultValue.toString());
        } else {
          element.addText(EMPTY_STRING);
        }
        pubElement.add(element);
      }
    } else {
      LoaderLog.error("No results from query "+token+" "+queryName);
    }
    return pubElement;
  }

  /**
   * Retrieves from Concentra the private document flag element for the
   * standard marketing document
   * 
   * @return Xml Element with the private document flag
   */
  private Element getPrivateDocumentFlag() {
    Element privateDocumentFlagElement = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();
    LoaderLog.info("StandardMarketingDoc getPrivateDocumentFlag");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_PRIVATE_DOCUMENT_FLAG, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      privateDocumentFlagElement = new BaseElement(ELEMENT_PRIVATE_DOCUMENT_FLAG);
      Object resultValue = resultInfo.getValue(0, ELEMENT_PRIVATE_DOCUMENT_FLAG);
      if (resultValue != null) {
        privateDocumentFlagElement.addText(resultValue.toString());
      } else {
        privateDocumentFlagElement.addText(EMPTY_STRING);
      }
    } else {
      LoaderLog.error("No results from query getPrivateDocumentFlag");
    }
    return privateDocumentFlagElement;
  }

  /**
   * Retrieves from Concentra the collection full title element for the
   * standard marketing document
   * 
   * @return Xml Element with the collection full title
   */
  private Element getCollectionFullTitle() {
    Element collectionFullTitleElement = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();

    LoaderLog.info("StandartMarketingDoc getCollectionFullTitle");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_COLLECTION_FULL_TITLE, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();
    if (resultInfo != null) {
      collectionFullTitleElement = new BaseElement(ELEMENT_COLLECTION_FULL_TITLE);
      Object resultValue = resultInfo.getValue(0, ELEMENT_COLLECTION_FULL_TITLE);
      if (resultValue != null) {
        collectionFullTitleElement.addText(resultValue.toString());
        Object attributeValue = resultInfo.getValue(0, ExtractorConstants.ELEMENT_LANGUAGE_CODE);
        if (attributeValue != null) {
          collectionFullTitleElement.attributeValue(ExtractorConstants.ELEMENT_LANGUAGE_CODE,
              attributeValue.toString());
        }
      } else {
        collectionFullTitleElement.addText(EMPTY_STRING);
      }
    } else {
      LoaderLog.error("No results from query " + token + " getCollectionFullTitle");
    }
    return collectionFullTitleElement;
  }

  /**
   * Retrieves from Concentra the document relations element for the standard
   * marketing document
   * 
   * @return Xml Element with the document relations
   */
  private Element getDocumentRelations() {
    Element documentRelationsElement = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();

    LoaderLog.info("StandardMarketingDoc getDocumentRelations");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_DOCUMENT_RELATIONS, docbaseSession);
    commonInfo.setParameter(0, chronicleId);
    commonInfo.setParameter(1, chronicleId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      documentRelationsElement = new BaseElement(ELEMENT_DOCUMENT_RELATIONS);

      String[] columnNames = resultInfo.getColumnsName();
      for (int rowsIter = 0; rowsIter < resultInfo.getRowCount(); rowsIter++) {
        Element documentRelationElement = new BaseElement(ELEMENT_DOCUMENT_RELATION);
        for (int elementsIter = 0; elementsIter < resultInfo.getColumnCount(); elementsIter++) {
          String currentColumn = columnNames[elementsIter];
          Object resultValue = resultInfo.getValue(rowsIter, currentColumn);
          Element element = new BaseElement(currentColumn);
          if (resultValue != null) {
            element.addText(resultValue.toString());
          } else {
            element.addText(EMPTY_STRING);
          }
          documentRelationElement.add(element);
        }
        documentRelationsElement.add(documentRelationElement);
      }
    } else {
      LoaderLog.error("No results from query "+token+ " getDocumentRelations");
    }
    return documentRelationsElement;
  }

  /**
   * Build the new pubvs structure Example: <pub_vendor_details> -
   * <pub_vendor_detail> <pubv_vendor>KP Lit</pubv_vendor>
   * <pubv_inventory_policy>pod only</pubv_inventory_policy>
   * <pubv_order_limit>0</pubv_order_limit> <pubv_restriction />
   * <pubv_strict_hold_flag>0</pubv_strict_hold_flag> <pubv_unit_code />
   * <pubv_initial_quantity>0</pubv_initial_quantity>
   * <pubv_initial_air_quantity>0</pubv_initial_air_quantity>
   * <pubv_reorder_quantity>0</pubv_reorder_quantity> <pubv_initial_date />
   * </pub_vendor_detail> </pub_vendor_details>
   * 
   * @return
   */
  private Element getPubvElementsStructure() {
    Element pubvRootDetails = new BaseElement(ELEMENT_PUB_VENDOR_DETAILS);

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_PUBV, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultPubvs = commonInfo.execute();
    if (resultPubvs != null) {
      String[] columnNames = resultPubvs.getColumnsName();
      for (int childrenIter = 0; childrenIter < resultPubvs.getRowCount(); childrenIter++) {
        String pubvVendor = resultPubvs.getValue(childrenIter, columnNames[0]).toString();
        String pubvInventoryPolicies = resultPubvs.getValue(childrenIter, columnNames[1]).toString();
        String pubvOrderLimits = resultPubvs.getValue(childrenIter, columnNames[2]).toString();
        String pubvRestrictions = resultPubvs.getValue(childrenIter, columnNames[3]).toString();
        String pubvStrictHoldFlags = resultPubvs.getValue(childrenIter, columnNames[4]).toString();
        String pubvUnitCodes = resultPubvs.getValue(childrenIter, columnNames[5]).toString();
        String pubvInitialQuantities = resultPubvs.getValue(childrenIter, columnNames[6]).toString();
        String pubvInitialAirQuantities = resultPubvs.getValue(childrenIter, columnNames[7]).toString();
        String pubvReorderQuantities = resultPubvs.getValue(childrenIter, columnNames[8]).toString();
        String pubvInitialDates = resultPubvs.getValue(childrenIter, columnNames[9]).toString();
        //Concentra 14.4 release Matt Nidoh added new fields for standard marketing class docs
        String gsbAppType = resultPubvs.getValue(childrenIter, columnNames[10]).toString();
        String gsbContentType = resultPubvs.getValue(childrenIter, columnNames[11]).toString();
        String gsbBizType = resultPubvs.getValue(childrenIter, columnNames[12]).toString();
        String pubvUnitPrice = resultPubvs.getValue(childrenIter, columnNames[13]).toString();
        String pubvUnitWeight = resultPubvs.getValue(childrenIter, columnNames[14]).toString();
        
        // PubvVendor must have content in order to create
        // structure
        if (!pubvVendor.equals("")) {
          // Start the build of tags
          Element pubvDetail = new BaseElement(ELEMENT_PUB_VENDOR_DETAIL);
          // Build pubvs Internal structures
          buildSubPubvElement(pubvVendor, pubvDetail, ELEMENT_PUBV_VENDOR);
          buildSubPubvElement(pubvInventoryPolicies, pubvDetail, ELEMENT_PUBV_INVENTORY_POLICY);
          buildSubPubvElement(pubvOrderLimits, pubvDetail, ELEMENT_PUBV_ORDER_LIMIT);
          buildSubPubvElement(pubvRestrictions, pubvDetail, ELEMENT_PUBV_RESTRICTION);
          buildSubPubvElement(pubvStrictHoldFlags, pubvDetail, ELEMENT_PUBV_STRICT_HOLD_FLAG);
          buildSubPubvElement(pubvUnitCodes, pubvDetail, ELEMENT_PUBV_UNIT_CODE);
          buildSubPubvElement(pubvInitialQuantities, pubvDetail, ELEMENT_PUBV_INITIAL_QUANTITY);
          buildSubPubvElement(pubvInitialAirQuantities, pubvDetail, ELEMENT_PUBV_INITIAL_AIR_QUANTITY);
          buildSubPubvElement(pubvReorderQuantities, pubvDetail, ELEMENT_PUBV_REORDER_QUANTITY);
          buildSubPubvElement(pubvInitialDates, pubvDetail, ELEMENT_PUBV_INITIAL_DATE);
          //Concentra 14.4 now bind the fetched data to the XML tag
          if (pubvVendor.equals("myGSBprint")) {
        	  buildSubPubvElement(gsbAppType, pubvDetail, ELEMENT_GSB_APPLICATION_TYPE);
        	  buildSubPubvElement(gsbContentType, pubvDetail, ELEMENT_GSB_CONTENT_TYPE);
        	  buildSubPubvElement(gsbBizType, pubvDetail, ELEMENT_GSB_BUSINESS_TYPE);
        	  buildSubPubvElement(pubvUnitPrice, pubvDetail, ELEMENT_UNIT_PRICE);
        	  buildSubPubvElement(pubvUnitWeight, pubvDetail, ELEMENT_UNIT_WEIGHT);
          }
          
          // Last step link to root element
          pubvRootDetails.add(pubvDetail);
        }
      }

    } else {
      LoaderLog.error("No results from query "+token+" getAllPubvs");
    }
    return pubvRootDetails;
  }

  /**
   * 
   * Retrieves the Crm Asset Code element needed on the Standard Marketing
   * property file
   * @return Xml Element with the crm Asset Code
   */
  private Element getCrmAssetCode() {
    LoaderLog.info("StandareMarketingDoc getCrmAssetCode");

    return getCrmAssetCodeElement(QUERY_GET_CRM_ASSET_CODE, ELEMENT_CRM_ASSET_CODE);
  }

  /**
   * Retrieves from the Concentra database a single element defined by the
   * query of a given document
   * 
   * @param queryName to be executed at concentra
   * @param element that will be used on the result set
   */
  private Element getCrmAssetCodeElement(String queryName, String element) {
    Element crmAssetCode = null;

    LoaderLog.info("StandardMarketingDoc getCrmAssetCodeElement");

    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_CRM_ASSET_CODE, docbaseSession);
    commonInfo.setParameter(0, objectType, DQLParameter.DQL_LABEL);
    commonInfo.setParameter(1, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      // Add Result to Element
      crmAssetCode = new BaseElement(element);
      Object elementToAdd = resultInfo.getValue(0, element);
      if (elementToAdd != null && !elementToAdd.equals(EMPTY_STRING)) {
        crmAssetCode.addText(elementToAdd.toString());
      }
    } else {
      LoaderLog.error("No results from query "+token+" getCrmAssetCode");
    }
    return crmAssetCode;
  }

  private void buildSubPubvElement(String content, Element root, String elementName) {
    // Create sub pubv element
    Element pubvVendorElement = new BaseElement(elementName);
    // Check if there is content otherwise create a empty element
    if (!content.equals("")) {
      pubvVendorElement.addText(content);
    }
    // Links sub elements
    root.add(pubvVendorElement);
  }

}
