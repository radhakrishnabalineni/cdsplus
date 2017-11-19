package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Hho Marketing
 * document property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class HhoMarketingDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

    private static final String ELEMENT_GO_TO_MARKET_INITIATIVES = "go_to_market_initiatives";
    private static final String ELEMENT_GO_TO_MARKET_INITIATIVE = "go_to_market_initiative";
    private static final String ELEMENT_SUBJECT_TYPES = "subject_types";
    private static final String ELEMENT_SUBJECT_TYPE = "subject_type";
    private static final String ELEMENT_ACTIVITY_TYPE_DETAILS = "activity_type_details";
    private static final String ELEMENT_ACTIVITY_TYPE_DETAIL = "activity_type_detail";
    private static final String ELEMENT_CONTACTS = "contacts";
    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_ASSOCIATED_PROJECT_PAGES = "associated_project_pages";
    private static final String ELEMENT_ASSOCIATED_PROJECT_PAGE = "associated_project_page";
    private static final String QUERY_GET_HHO_MARKETING_INFO = "getHhoMarketingInfo";
    private static final String QUERY_GET_ACTIVITY_TYPE_DETAILS = "getActivityTypeDetails";
    private static final String QUERY_GET_GO_TO_MARKET_INITIATIVES = "getGoToMarketInitiatives";
    private static final String QUERY_GET_SUBJECT_TYPES = "getSubjectTypes";
    private static final String QUERY_GET_CONTACTS = "getContacts";
    private static final String QUERY_GET_ASSOCIATED_PROJECT_PAGES = "getAssociatedProjectPages";

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
    public HhoMarketingDoc(String objectId, String chronicleId, String token, String objectType, String colId,
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
     * @throws DmRepositoryException 
     * @throws DfException 
     */
    protected ArrayList getSpecificDocInfo(Element productGroups) throws DfException, DmRepositoryException {
        ArrayList contentElements = new ArrayList();
        ArrayList contentElementsThatMightHaveNull = new ArrayList();
        ArrayList hhoMarketingInfo = getHhoMarketingInfo();

        // TR132010: Begin - Add new attributes to support KM2 search.
        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        // TR132010: End.

		// Added as part of 12.3 Release - BR586144 requirement
        contentElementsThatMightHaveNull.add(getContentUpdateDate());
        contentElementsThatMightHaveNull.add(getLanguageCode());
        //commenting this line as the products tag is coming twice in the document in cdsplus.
        //contentElementsThatMightHaveNull.add(getProductGroups());
        contentElementsThatMightHaveNull.add(getRObjectType());
		// End of 12.3 Release - BR586144

        contentElementsThatMightHaveNull.add(getFullTitle());
        contentElementsThatMightHaveNull.add(getLanguageLabel());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getAuthors());
        contentElementsThatMightHaveNull.add(getRegions());
        contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
        contentElementsThatMightHaveNull.add(getActivityTypeDetails());
        contentElementsThatMightHaveNull.add(getGoToMarketInitiatives());
        contentElementsThatMightHaveNull.add(getSubjectTypes());
        contentElementsThatMightHaveNull.add(getContacts());
        contentElementsThatMightHaveNull.add(getAssociatedProjectPages());
        contentElementsThatMightHaveNull.add(getSearchKeywords());
        contentElementsThatMightHaveNull.add(getMasterObjectName());
        contentElementsThatMightHaveNull.add(getAudiences());
        contentElementsThatMightHaveNull.add(getFileName());
        contentElementsThatMightHaveNull.add(getComponents());

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
        for (int hhoIter = 0; hhoIter < hhoMarketingInfo.size(); hhoIter++) {
            if (hhoMarketingInfo.get(hhoIter) != null) {
                contentElements.add(hhoMarketingInfo.get(hhoIter));
            }
        }
        return contentElements;
    }

    /***************************************************************************
     * Retrieves from Concentra database the hho marketing general properties
     * 
     * @return ArrayList with the Elements containing the hho marketing document
     *         general properties
     */
    private ArrayList getHhoMarketingInfo() {
        return getGeneralInfo("getHhoMarketingInfo", QUERY_GET_HHO_MARKETING_INFO);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the document activity type details
     * of the given document
     * 
     * @return Xml element with the document activity type details
     */
    private Element getActivityTypeDetails() {
        return getElementFromObjectTypeColumnId("getActivityTypeDetails",
                QUERY_GET_ACTIVITY_TYPE_DETAILS, ELEMENT_ACTIVITY_TYPE_DETAILS, ELEMENT_ACTIVITY_TYPE_DETAIL);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the document go-to-market
     * initiatives
     * 
     * @return Xml element with the document go-to-market initiatives
     */
    private Element getGoToMarketInitiatives() {
        return getElementFromObjectTypeColumnId("getGoToMarketInitiatives",
                QUERY_GET_GO_TO_MARKET_INITIATIVES, ELEMENT_GO_TO_MARKET_INITIATIVES, ELEMENT_GO_TO_MARKET_INITIATIVE);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the document subject types
     * 
     * @return Xml element with the document subject types
     */
    private Element getSubjectTypes() {
        return getElementFromObjectTypeColumnId("getSubjectTypes", QUERY_GET_SUBJECT_TYPES,
                ELEMENT_SUBJECT_TYPES, ELEMENT_SUBJECT_TYPE);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the document contacts
     * 
     * @return Xml element with the document contacts
     */
    private Element getContacts() {
        return getElementFromObjectTypeId("getContacts", QUERY_GET_CONTACTS, ELEMENT_CONTACTS,
                ELEMENT_CONTACT);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the document associated project
     * pages
     * 
     * @return Xml element with the document associated project pages
     */
    private Element getAssociatedProjectPages() {
        return getElementFromObjectTypeId("getAssociatedProjectPages",
                QUERY_GET_ASSOCIATED_PROJECT_PAGES, ELEMENT_ASSOCIATED_PROJECT_PAGES, ELEMENT_ASSOCIATED_PROJECT_PAGE);
    }

}
