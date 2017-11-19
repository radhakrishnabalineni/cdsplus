package com.hp.concentra.extractor.documents;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.xml.sax.SAXException;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.core.content.ContentOut;
import com.hp.cks.concentra.core.document.CSupportDoc;
import com.hp.cks.concentra.utils.DateOps;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.XmlHandler;

/**
 * Class in charge of setting the property file correctly for the following
 * support communication documents: "Advisory", "Bulletin", "Notice", "Product
 * Change Notification", "Service Program Announcement", "Security Bulletin"
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class SupportCommMerger {

  private static final String ELEMENT_ACCOUNT_CODE = "account_code";
  private static final String ELEMENT_ACTION_EXPIRY_DATE = "action_expiry_date";
  private static final String ELEMENT_ACTION_START_DATE = "action_start_date";
  private static final String ELEMENT_ACTION_OBJECTIVE = "action_objective";
  private static final String ELEMENT_ACTION_REQUIRED = "action_required";
  private static final String ELEMENT_CONTENT_VERSION_DATE = "content_version_date";
  // private static final String ELEMENT_CREATION_DATE = "creation_date";
  private static final String ELEMENT_DISCLOSURE_LEVEL = "disclosure_level";
  private static final String ELEMENT_DOC_CONTENT_VERSION = "doc_content_version";
  private static final String ELEMENT_EFFECTIVITY_DATE = "effectivity_date";
  private static final String ELEMENT_IA_IDENTIFIER = "ia_identifier";
  private static final String ELEMENT_ISSUE_NUMBERS = "issue_numbers";
  private static final String ELEMENT_ISSUE_NUMBER = "issue_number";
  private static final String ELEMENT_ISSUE_STATUS = "issue_status";
  private static final String ELEMENT_LABOR_COST = "labor_cost";
  private static final String ELEMENT_LABOR_MINUTES_PAID = "labor_minutes_paid";
  private static final String ELEMENT_LABOR_HOURS_PAID = "labor_hours_paid";
  private static final String ELEMENT_LABOR_RATE_PER_HOUR = "labor_rate_per_hour";
  private static final String ELEMENT_MANUAL_ACTION = "manual_action";
  private static final String ELEMENT_OPERATING_SYSTEM_GROUPS = "operating_system_groups";
  private static final String ELEMENT_OPERATING_SYSTEM_GROUP = "operating_system_group";
  private static final String ELEMENT_OPERATING_SYSTEM_GROUP_NAME = "operating_system_group_name";
  private static final String ELEMENT_OPERATING_SYSTEM_GROUP_NAMES = "operating_system_group_names";
  private static final String ELEMENT_PARTS_COST = "parts_cost";
  private static final String ELEMENT_PARTS_STRATEGY = "parts_strategy";
  private static final String ELEMENT_PRODUCT_GROUPS = "product_groups";
  private static final String ELEMENT_PRODUCT_GROUP = "product_group";
  private static final String ELEMENT_PRODUCT_GROUP_NAMES = "product_group_names";
  private static final String ELEMENT_PRODUCT_GROUP_NAME = "product_group_name";
  private static final String ELEMENT_RECOMMENDED_ACTION = "recommended_action";
  // private static final String ELEMENT_REVIEW_DATE = "review_date";
  private static final String ELEMENT_SERIAL_NUMBER_ENDING = "serial_number_ending";
  private static final String ELEMENT_SERIAL_NUMBER_ENDINGS = "serial_number_endings";
  private static final String ELEMENT_SERIAL_NUMBER_BEGINNING = "serial_number_beginning";
  private static final String ELEMENT_SERIAL_NUMBER_BEGINNINGS = "serial_number_beginnings";
  private static final String ELEMENT_SERIAL_NUMBER = "serial_number";
  private static final String ELEMENT_SERIAL_NUMBERS = "serial_numbers";
  private static final String ELEMENT_SERVICE_TYPE = "service_type";
  private static final String ELEMENT_SERVICE_PROGRAM = "service_program";
  private static final String ELEMENT_SERVICE_INVENTORY = "service_inventory";
  private static final String ELEMENT_SOFTWARE_GROUPS = "software_groups";
  private static final String ELEMENT_SOFTWARE_GROUP = "software_group";
  private static final String ELEMENT_SOFTWARE_GROUP_NAME = "software_group_name";
  private static final String ELEMENT_SOFTWARE_GROUP_NAMES = "software_group_names";
  private static final String ELEMENT_TRAVEL_COST = "travel_cost";
  // private static final String ELEMENT_VALID_FLAG = "valid_flag";
  private static final String ELEMENT_USED_PARTS_ACTION = "used_parts_action";

  private static final String ELEMENT_C_SUPPORT_DOC = "/c_support_doc";
  private static final String TEXT_DESCRIPTION = "this tag is a placeholder for all the properties that need to be part of the content";
  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ELEMENT_PROPERTIES = "properties";
  private static final String SERVICE_PROG_ANNOUNCEMENT_SUPPORT_CONTENT = "Service Program Announcement";
  private static final String PRODUCT_CHANGE_NOTIF_SUPPORT_CONTENT = "Product Change Notification";
  private static final String SECURITY_BULLETIN_SUPPORT_CONTENT = "Security Bulletin";
  private static final String NOTICE_SUPPORT_CONTENT = "Notice";
  private static final String BULLETIN_SUPPORT_CONTENT = "Bulletin";
  private static final String ADVISORY_SUPPORT_CONTENT = "Advisory";
  private static final String SUPPORT_COMM_EXPORT_METHOD = "SupportCommMerger.supportCommExport";

  /***************************************************************************
   * Put the additional properties to the content for each one of the specific
   * support communication files
   * 
   * @param theDoc
   *            The document retrieved from Concentra
   * @param objectId
   *            Unique identifier of the document object
   * @param fileResults
   *            Files obtained for the given document
   * @throws SupportCommMergerException
   *             If an error occurred while trying to add the additional
   *             properties, this exception is thrown
   * @throws IOException  
   * @throws SAXException 
   * @throws DocumentException 
   */
  public static void supportCommExport(CSupportDoc theDoc, String objectId, Vector fileResults)
  throws SupportCommMergerException, IOException, DocumentException, SAXException {

    LoaderLog.info(SUPPORT_COMM_EXPORT_METHOD);

    // This is a result of decisions made during the beginning of April 05
    // that summarized are:
    // downstream of Concentra is incapable and unwilling to merge CPF and
    // Content and apply style sheets
    // to order xml elements. This logic is being forced into Concentra.

    Enumeration enumaVector = fileResults.elements();

    File originalFile = null;
    // iterates over the files obtained from Concentra, searching for the
    // object file
    while (enumaVector.hasMoreElements()) {
      ContentOut.ContentOutFileInfo fileInfo = (ContentOut.ContentOutFileInfo) enumaVector.nextElement();
      if (fileInfo.getType() == ContentOut.ContentOutFileInfo.OBJECT_FILE) {
        if (fileInfo.getFile() != null) {
          originalFile = new File(fileInfo.getFile());
        }
      }
    }
    // if the preview file was found, interacts over it
    if (originalFile != null) {
      XmlHandler xmldoc = new XmlHandler(originalFile.getAbsolutePath());

      Element propElem = new BaseElement(ELEMENT_PROPERTIES);
      propElem.addAttribute(ATTRIBUTE_DESCRIPTION, TEXT_DESCRIPTION);

      try {
        String documentType = theDoc.getDocumentType();
        // Common information for all Support Communication content
        propElem.add(getSimpleElement(ExtractorConstants.ELEMENT_LANGUAGE_CODE, theDoc.getLanguageCode()));
        propElem.add(getSimpleRepeatingElement(ExtractorConstants.ELEMENT_REGIONS,
            ExtractorConstants.ELEMENT_REGION, theDoc.getRegions()));
        propElem.add(getSimpleElement(ExtractorConstants.ELEMENT_DOCUMENT_TYPE, theDoc.getDocumentType()));
        propElem.add(getSimpleElement(ExtractorConstants.ELEMENT_OBJECT_NAME, theDoc.getObjectName()));

        propElem.add(getSimpleElement(ELEMENT_DOC_CONTENT_VERSION, theDoc.getContentVersion()));
        propElem.add(getSimpleElement(ELEMENT_CONTENT_VERSION_DATE, DateOps.getStandardUIDate(theDoc.getContentVersionDate())));
        propElem.add(getSimpleElement(ExtractorConstants.ELEMENT_CONTENT_UPDATE_DATE, DateOps.getStandardUIDate(theDoc.getContentUpdateDate())));

        // Sets the special properties of the Advisory type
        if (ADVISORY_SUPPORT_CONTENT.equals(documentType) || BULLETIN_SUPPORT_CONTENT.equals(documentType)) {
          propElem.add(getSimpleElement(ELEMENT_DISCLOSURE_LEVEL, theDoc.getDisclosureLevel()));
          propElem.add(getSimpleElement(ELEMENT_ACTION_REQUIRED, Boolean.toString(theDoc.getActionRequired())));
          propElem.add(getSimpleElement(ELEMENT_IA_IDENTIFIER, theDoc.getIaIdentifier()));
          propElem.add(getSimpleElement(ELEMENT_ISSUE_STATUS, theDoc.getIssueStatus()));

          // now comes all elements that are meant to be in the XML
          propElem.add(getSimpleRepeatingElement(ELEMENT_SERIAL_NUMBERS, ELEMENT_SERIAL_NUMBER, theDoc.getSerialNumberProducts()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SERIAL_NUMBER_BEGINNINGS,
              ELEMENT_SERIAL_NUMBER_BEGINNING, theDoc.getSerialNumberBeginnings()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SERIAL_NUMBER_ENDINGS,
              ELEMENT_SERIAL_NUMBER_ENDING, theDoc.getSerialNumberEndings()));

          propElem.add(getSimpleElement(ELEMENT_ACTION_OBJECTIVE, theDoc.getActionObjective()));
          propElem.add(getSimpleElement(ELEMENT_RECOMMENDED_ACTION, theDoc.getRecommendedAction()));
          propElem.add(getSimpleElement(ELEMENT_LABOR_RATE_PER_HOUR, Double.toString(theDoc.getLaborRatePerHour())));
          propElem.add(getSimpleElement(ELEMENT_LABOR_HOURS_PAID, Integer.toString(theDoc.getLaborHoursPaid())));
          propElem.add(getSimpleElement(ELEMENT_LABOR_MINUTES_PAID, Integer.toString(theDoc.getLaborMinutesPaid())));
          propElem.add(getSimpleElement(ELEMENT_SERVICE_TYPE, theDoc.getServiceType()));
          propElem.add(getSimpleElement(ELEMENT_ACTION_START_DATE, DateOps.getStandardUIDate(theDoc.getActionStartDate())));
          propElem.add(getSimpleElement(ELEMENT_ACTION_EXPIRY_DATE, DateOps.getStandardUIDate(theDoc.getActionExpiryDate())));
          propElem.add(getSimpleElement(ELEMENT_LABOR_COST, theDoc.getLaborCost()));
          propElem.add(getSimpleElement(ELEMENT_PARTS_COST, theDoc.getPartsCost()));
          propElem.add(getSimpleElement(ELEMENT_TRAVEL_COST, theDoc.getTravelCost()));
          propElem.add(getSimpleElement(ELEMENT_ACCOUNT_CODE, theDoc.getAccountCode()));
          propElem.add(getSimpleElement(ELEMENT_PARTS_STRATEGY, theDoc.getPartsStrategy()));
          propElem.add(getSimpleElement(ELEMENT_SERVICE_INVENTORY, theDoc.getServiceInventory()));
          propElem.add(getSimpleElement(ELEMENT_USED_PARTS_ACTION, theDoc.getUsedPartsAction()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUPS, ELEMENT_PRODUCT_GROUP, theDoc.getProductGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUP_NAMES,
              ELEMENT_PRODUCT_GROUP_NAME, theDoc.getProductNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUPS,
              ELEMENT_OPERATING_SYSTEM_GROUP, theDoc.getOperatingSystemGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUP_NAMES,
              ELEMENT_OPERATING_SYSTEM_GROUP_NAME, theDoc.getOperatingSystemGroupNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUPS, ELEMENT_SOFTWARE_GROUP,
              theDoc.getSoftwareGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUP_NAMES,
              ELEMENT_SOFTWARE_GROUP_NAME, theDoc.getSoftwareGroupNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_ISSUE_NUMBERS, ELEMENT_ISSUE_NUMBER, theDoc.getIssueNumbers()));
          propElem.add(getSimpleRepeatingElement(ExtractorConstants.ELEMENT_AUTHORS,
              ExtractorConstants.ELEMENT_AUTHOR, theDoc.getAuthors()));
        }
        if (NOTICE_SUPPORT_CONTENT.equals(documentType)) {
          propElem.add(getSimpleElement(ELEMENT_DISCLOSURE_LEVEL, theDoc.getDisclosureLevel()));
          propElem.add(getSimpleElement(ELEMENT_ACTION_REQUIRED, Boolean.toString(theDoc.getActionRequired())));
          propElem.add(getSimpleElement(ELEMENT_IA_IDENTIFIER, theDoc.getIaIdentifier()));
          propElem.add(getSimpleElement(ELEMENT_ISSUE_STATUS, theDoc.getIssueStatus()));

          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUPS, ELEMENT_PRODUCT_GROUP, theDoc.getProductGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUP_NAMES,
              ELEMENT_PRODUCT_GROUP_NAME, theDoc.getProductNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUPS,
              ELEMENT_OPERATING_SYSTEM_GROUP, theDoc.getOperatingSystemGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUP_NAMES,
              ELEMENT_OPERATING_SYSTEM_GROUP_NAME, theDoc.getOperatingSystemGroupNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUPS, ELEMENT_SOFTWARE_GROUP,
              theDoc.getSoftwareGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUP_NAMES,
              ELEMENT_SOFTWARE_GROUP_NAME, theDoc.getSoftwareGroupNames()));
        }
        if (SECURITY_BULLETIN_SUPPORT_CONTENT.equals(documentType)) {
          propElem.add(getSimpleRepeatingElement(ELEMENT_ISSUE_NUMBERS, ELEMENT_ISSUE_NUMBER, theDoc.getIssueNumbers()));
          propElem.add(getSimpleElement(ELEMENT_MANUAL_ACTION, theDoc.getManualAction()));
        }
        if (PRODUCT_CHANGE_NOTIF_SUPPORT_CONTENT.equals(documentType)) {
          propElem.add(getSimpleElement(ELEMENT_EFFECTIVITY_DATE, DateOps.getStandardUIDate(theDoc.getEffectivityDate())));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUPS, ELEMENT_PRODUCT_GROUP, theDoc.getProductGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUP_NAMES,
              ELEMENT_PRODUCT_GROUP_NAME, theDoc.getProductNames()));
        }
        if (SERVICE_PROG_ANNOUNCEMENT_SUPPORT_CONTENT.equals(documentType)) {
          propElem.add(getSimpleElement(ELEMENT_DISCLOSURE_LEVEL, theDoc.getDisclosureLevel()));
          propElem.add(getSimpleElement(ELEMENT_SERVICE_PROGRAM, String.valueOf(theDoc.getServiceProgram())));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUPS, ELEMENT_PRODUCT_GROUP, theDoc.getProductGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_PRODUCT_GROUP_NAMES,
              ELEMENT_PRODUCT_GROUP_NAME, theDoc.getProductNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUPS,
              ELEMENT_OPERATING_SYSTEM_GROUP, theDoc.getOperatingSystemGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_OPERATING_SYSTEM_GROUP_NAMES,
              ELEMENT_OPERATING_SYSTEM_GROUP_NAME, theDoc.getOperatingSystemGroupNames()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUPS, ELEMENT_SOFTWARE_GROUP,
              theDoc.getSoftwareGroups()));
          propElem.add(getSimpleRepeatingElement(ELEMENT_SOFTWARE_GROUP_NAMES,
              ELEMENT_SOFTWARE_GROUP_NAME, theDoc.getSoftwareGroupNames()));
        }

        // Adds the generated content
        xmldoc.getSingleElement(ELEMENT_C_SUPPORT_DOC).add(propElem);
        // writes the file on disk
        xmldoc.writeFile();
      } catch (DfException e) {
        SupportCommMergerException exception = new SupportCommMergerException(e.getMessage());
        exception.setStackTrace(e.getStackTrace());
      } catch (DmRepositoryException e) {
        SupportCommMergerException exception = new SupportCommMergerException(e.getMessage());
        exception.setStackTrace(e.getStackTrace());
      }
    } else {
      String msg = "The document doesn't contain its original object file "+objectId;
      LoaderLog.fatal(msg);
      throw new SupportCommMergerException(msg);
    }
  }

  /***************************************************************************
   * Creates an simple xml element with the given value
   * 
   * @param elementName
   *            Name of the element to be created
   * @param elementValue
   *            Value of the element
   * @return Simple xml element
   */
  private static Element getSimpleElement(String elementName, String elementValue) {
    Element simpleElement = new BaseElement(elementName);
    simpleElement.addText(elementValue);
    return simpleElement;
  }

  /***************************************************************************
   * Creates an xml element that contains the several sub elements, whose
   * values are passed as a parameter
   * 
   * @param elementName
   *            Name of the main element
   * @param elementSubName
   *            Name of each one of the sub elements
   * @param elementValue
   *            Values of each one of the sub elements
   * @return Xml element with all its sub elements and values
   */
  private static Element getSimpleRepeatingElement(String elementName, String elementSubName, String elementValue[]) {
    Element mainElement = new BaseElement(elementName);
    if (elementValue != null && elementValue.length > 0) {
      for (int subIter = 0; subIter < elementValue.length; subIter++) {
        Element subElement = new BaseElement(elementSubName);
        subElement.setText(elementValue[subIter]);
        mainElement.add(subElement);
      }
    }
    return mainElement;
  }
}
