package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Manual document
 * property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class ManualDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

    private static final String QUERY_GET_MANUAL_INFO = "getManualInfo";

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
    public ManualDoc(String objectId, String chronicleId, String token, String objectType, String colId, int priority,
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
        ArrayList manualInfo = getGeneralInfo("getManualInfo", QUERY_GET_MANUAL_INFO);
        ArrayList contentElementsThatMightHaveNull = new ArrayList();

        contentElementsThatMightHaveNull.add(getRegions());
        contentElementsThatMightHaveNull.add(getAuthors());
        contentElementsThatMightHaveNull.add(getSearchKeywords());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        contentElementsThatMightHaveNull.add(getMasterObjectName());
        contentElementsThatMightHaveNull.add(getFullTitle());
        contentElementsThatMightHaveNull.add(getProductLevels());
        contentElementsThatMightHaveNull.add(getPublicationDestinations());
        contentElementsThatMightHaveNull.add(getAcronym());
        contentElementsThatMightHaveNull.add(getFeedbackAddress());
        contentElementsThatMightHaveNull.add(getFileName());
        contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
        contentElementsThatMightHaveNull.add(getContentUpdateDate());
        contentElementsThatMightHaveNull.add(getLanguageCode());
        //KM3.1 (#BR_0939) - Add JPC to XML
        contentElementsThatMightHaveNull.add(getJointProductCollections(productGroups));
        contentElementsThatMightHaveNull.add(getExtraProperties());
        contentElementsThatMightHaveNull.add(getPlannedPublicDate());
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

        for (int manualIter = 0; manualIter < manualInfo.size(); manualIter++) {
            if (manualInfo.get(manualIter) != null) {
                contentElements.add(manualInfo.get(manualIter));
            }
        }
        return contentElements;
    }
}
