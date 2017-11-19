package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.hp.concentra.PreparedDQL;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.Result;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for a Product Setup document
 * property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class ProductSetupDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

  private static final String ELEMENT_ASSOCIATION = "association";
  private static final String ELEMENT_ASSOCIATIONS = "associations";
  private static final String QUERY_GET_ASSOCIATIONS = "getAssociations";
  private static final String QUERY_GET_PRODUCT_SETUP_INFO = "getProductSetupInfo";
  private static final String QUERY_GET_PRODUCT_MARKETS = "getProductMarkets";
  private static final String QUERY_GET_ORIGIN_COUNTRIES = "getOriginCountries";
  private static final String ELEMENT_PRODUCT_MARKET = "product_market";
  private static final String ELEMENT_PRODUCT_MARKETS = "product_markets";
  private static final String ELEMENT_ORIGIN_COUNTRIES = "origin_countries";

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

  public ProductSetupDoc(String objectId, String chronicleId, String token, String objectType, String colId,
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
    ArrayList productSetupInfo = getGeneralInfo("getProductSetupInfo",
        QUERY_GET_PRODUCT_SETUP_INFO);
    ArrayList contentElementsThatMightHaveNull = new ArrayList();

    contentElementsThatMightHaveNull.add(getRegions());
    contentElementsThatMightHaveNull.add(getAuthors());
    contentElementsThatMightHaveNull.add(getSearchKeywords());
    contentElementsThatMightHaveNull.add(getLanguageLabel());
    contentElementsThatMightHaveNull.add(getFullTitle());
    contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
    contentElementsThatMightHaveNull.add(getMasterObjectName());
    contentElementsThatMightHaveNull.add(getFeedbackAddress());
    contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
    contentElementsThatMightHaveNull.add(getAudiences());
    contentElementsThatMightHaveNull.add(getExtraProperties());
    contentElementsThatMightHaveNull.add(getAccounts());
    contentElementsThatMightHaveNull.add(getMarketingTeams());
    contentElementsThatMightHaveNull.add(getOriginCountries());
    contentElementsThatMightHaveNull.add(getProductMarkets());
    contentElementsThatMightHaveNull.add(getAssociations());

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

    for (int productSetupIter = 0; productSetupIter < productSetupInfo.size(); productSetupIter++) {
      if (productSetupInfo.get(productSetupIter) != null) {
        contentElements.add(productSetupInfo.get(productSetupIter));
      }
    }

    return contentElements;
  }

  /**
   * Retrieves from Concentra the origin countries of the document
   * 
   * @return Xml element with the document origin countries
   */
  private Element getOriginCountries() {
    return getElementFromId("getOriginCountries", QUERY_GET_ORIGIN_COUNTRIES,
        ELEMENT_ORIGIN_COUNTRIES, ELEMENT_ORIGIN_COUNTRIES);
  }

  /**
   * Retrieves from Concentra the product markets of the document
   * 
   * @return Xml element with the document product markets
   */
  private Element getProductMarkets() {
    return getElementFromId("getProductMarkets", QUERY_GET_PRODUCT_MARKETS,
        ELEMENT_PRODUCT_MARKETS, ELEMENT_PRODUCT_MARKET);
  }

  /**
   * Retrieves from Concentra the associations of the document
   * 
   * @return Xml element with the document associations
   */
  private Element getAssociations() {
    Element associations = null;
    String thisMethodName = new Exception().getStackTrace()[0].getMethodName();
    LoaderLog.info("ProductSetupDoc getAssociations");
    PreparedDQL commonInfo = PreparedDQL.getPrepareDQL(QUERY_GET_ASSOCIATIONS, docbaseSession);
    commonInfo.setParameter(0, objectId);
    Result resultInfo = commonInfo.execute();

    if (resultInfo != null) {
      associations = new BaseElement(ELEMENT_ASSOCIATIONS);
      for (int rowIter = 0; rowIter < resultInfo.getRowCount(); rowIter++) {
        Element association = new BaseElement(ELEMENT_ASSOCIATION);

        for (int columnIter = 0; columnIter < resultInfo.getColumnCount(); columnIter++) {
          Element assocInfo = new BaseElement(resultInfo.getColumnName(columnIter));
          assocInfo.addText(resultInfo.getValue(rowIter, columnIter).toString());
          association.add(assocInfo);
        }
        associations.add(association);
      }
    } else {
      LoaderLog.error("No results from query getAssociations");
      associations = null;
    }
    return associations;
  }
}
