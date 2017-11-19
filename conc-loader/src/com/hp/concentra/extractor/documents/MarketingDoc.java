package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Marketing document
 * property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class MarketingDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

    private static final String QUERY_GET_MARKETING_DOC_INFO = "getMarketingInfo";
    private static final String QUERY_GET_MARKETING_KEYWORDS = "getMarketingKeywords";
    private static final String ELEMENT_MARKETING_KEYWORD = "marketing_keyword";
    private static final String ELEMENT_MARKETING_KEYWORDS = "marketing_keywords";
    private static final String QUERY_GET_SELLING_WINDOWS = "getSellingWindows";
    private static final String ELEMENT_SELLING_WINDOW = "selling_window";
    private static final String ELEMENT_SELLING_WINDOWS = "selling_windows";

    public MarketingDoc(String objectId, String chronicleId, String token, String objectType, String colId,
            int priority, String activeFlag) {
        super(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
    }
    
    protected ArrayList getSpecificDocInfo(Element productGroups) {
        ArrayList contentElements = new ArrayList();
        ArrayList marketingDocInfo = getGeneralInfo("getMarketingDocInfo",
                QUERY_GET_MARKETING_DOC_INFO);
        ArrayList contentElementsThatMightHaveNull = new ArrayList();

        contentElementsThatMightHaveNull.add(getFeedbackAddress());
        contentElementsThatMightHaveNull.add(getCollectionValidFlag());
        contentElementsThatMightHaveNull.add(getInformationSource());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getContentUpdateDate());
        contentElementsThatMightHaveNull.add(getMasterObjectName());
        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
        contentElementsThatMightHaveNull.add(getMarketingKeywords());
        contentElementsThatMightHaveNull.add(getLanguageLabel());
        contentElementsThatMightHaveNull.add(getAudiences());
        contentElementsThatMightHaveNull.add(getExtraProperties());
        contentElementsThatMightHaveNull.add(getAccounts());
        contentElementsThatMightHaveNull.add(getAuthors());
        contentElementsThatMightHaveNull.add(getMarketingTeams());
        contentElementsThatMightHaveNull.add(getRegions());
        contentElementsThatMightHaveNull.add(getSearchKeywords());
        contentElementsThatMightHaveNull.add(getSellingWindows());
        contentElementsThatMightHaveNull.add(getRenditions());
        contentElementsThatMightHaveNull.add(getFullTitle());

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
        for (int contentIter = 0; contentIter < marketingDocInfo.size(); contentIter++) {
            if (marketingDocInfo.get(contentIter) != null) {
                contentElements.add(marketingDocInfo.get(contentIter));
            }
        }
        return contentElements;
    }

    /***************************************************************************
     * Retrieves from the Concentra database the Marketing Keywords of the given
     * document
     * 
     * @return Xml Element with the document marketing keywords
     */
    protected Element getMarketingKeywords() {
        return getElementFromId("getMarketingKeywords", QUERY_GET_MARKETING_KEYWORDS,
                ELEMENT_MARKETING_KEYWORDS, ELEMENT_MARKETING_KEYWORD);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the selling windows of the given
     * document
     * 
     * @return Xml Element with the document selling windows
     */
    protected Element getSellingWindows() {
        return getElementFromId("getSellingWindows", QUERY_GET_SELLING_WINDOWS,
                ELEMENT_SELLING_WINDOWS, ELEMENT_SELLING_WINDOW);
    }

}
