package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the General Purpose
 * document property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class GeneralPurposeDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {
    private static final String QUERY_GET_GENERAL_PURPOSE_INFO = "getGeneralPurposeInfo";

    public GeneralPurposeDoc(String objectId, String chronicleId, String token, String objectType, String colId,
            int priority, String activeFlag) {
        super(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
    }
    
    /***************************************************************************
     * @param productGroups
     *            that need to be check to determine if the element has valid
     *            products
     * @return
     * @throws DmRepositoryException 
     * @throws DfException 
     */

    protected ArrayList getSpecificDocInfo(Element productGroups) throws DfException, DmRepositoryException {
        ArrayList contentElements = new ArrayList();
        ArrayList contentElementsThatMightHaveNull = new ArrayList();
        ArrayList contentGeneralPurposeInfo = getGeneralPurposeInfo();

        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        contentElementsThatMightHaveNull.add(getFullTitle());
        contentElementsThatMightHaveNull.add(getContentUpdateDate());
        contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
        contentElementsThatMightHaveNull.add(getProductLevels());
        contentElementsThatMightHaveNull.add(getAuthors());
        contentElementsThatMightHaveNull.add(getPublicationDestinations());
        contentElementsThatMightHaveNull.add(getSearchKeywords());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getLanguageLabel());
        contentElementsThatMightHaveNull.add(getAcronym());
        contentElementsThatMightHaveNull.add(getFeedbackAddress());
        contentElementsThatMightHaveNull.add(getCollectionValidFlag());
        contentElementsThatMightHaveNull.add(getPublicationCode());
        contentElementsThatMightHaveNull.add(getPlannedPublicDate());
        contentElementsThatMightHaveNull.add(getMasterObjectName());
        contentElementsThatMightHaveNull.add(getExtraProperties());
        contentElementsThatMightHaveNull.add(getFileName());
        contentElementsThatMightHaveNull.add(getRegions());
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

        for (int contentIter = 0; contentIter < contentGeneralPurposeInfo.size(); contentIter++) {
            if (contentGeneralPurposeInfo.get(contentIter) != null) {
                contentElements.add(contentGeneralPurposeInfo.get(contentIter));
            }
        }

        return contentElements;
    }

    /***************************************************************************
     * Retrieves from Concentra database the general purpose doc properties
     * 
     * @return ArrayList with the Elements containing the general purpose
     *         document general properties
     */
    private ArrayList getGeneralPurposeInfo() {
        return getGeneralInfo("getGeneralPurposeInfo", QUERY_GET_GENERAL_PURPOSE_INFO);
    }

}
