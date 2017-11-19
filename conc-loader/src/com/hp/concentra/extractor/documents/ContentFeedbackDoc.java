package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;

import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Content Feedback
 * document property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class ContentFeedbackDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

    private static final String GET_CONTENT_FEEDBACK_INFO_QUERY = "getContentFeedbackInfo";
    private static final String ELEMENT_FEEDBACK_PRODUCT_LINE = "feedback_product_line";
    private static final String ELEMENT_FEEDBACK_PRODUCT_LINES = "feedback_product_lines";
    private static final String GET_FEEDBACK_PRODUCT_LINES_QUERY = "getFeedbackProductLines";

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
    public ContentFeedbackDoc(String objectId, String chronicleId, String token, String objectType, String colId,
            int priority, String activeFlag) {
        super(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
    }
    
    /***************************************************************************
     * Retrieves for this specific type of document, all the necessary
     * information
     * 
     * @param productGroups
     *            that need to be check to determine if the element has valid
     *            products needed to finish the creation of the property.xml
     *            file
     */
    protected ArrayList getSpecificDocInfo(Element productGroups) {
        LoaderLog.info("ContentfeedbackDoc getSpecificDocInfo");
        ArrayList contentElements = new ArrayList();
        ArrayList contentElementsThatMightHaveNull = new ArrayList();

        contentElementsThatMightHaveNull.add(getFeedbackProductLines());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getRegions());
        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        contentElementsThatMightHaveNull.add(getLanguageLabel());
        contentElementsThatMightHaveNull.add(getInformationSource());
        contentElementsThatMightHaveNull.add(getFullTitle());
        contentElementsThatMightHaveNull.add(getFileName());
        ArrayList contentFeedbackInfo = getContentFeedbackInfo();

        for (int contentIter = 0; contentIter < contentElementsThatMightHaveNull.size(); contentIter++) {
            if (contentElementsThatMightHaveNull.get(contentIter) != null) {
                contentElements.add(contentElementsThatMightHaveNull.get(contentIter));
            }
        }
        for (int contentIter = 0; contentIter < contentFeedbackInfo.size(); contentIter++) {
            if (contentFeedbackInfo.get(contentIter) != null) {
                contentElements.add(contentFeedbackInfo.get(contentIter));
            }
        }
        return contentElements;
    }

    /***************************************************************************
     * Retrieves from Concentra database the feedback product lines of this
     * document
     * 
     * @return Xml element with all the feedback product lines
     */
    private Element getFeedbackProductLines() {
        return getElementFromObjectTypeId("getFeedbackProductLines",
                GET_FEEDBACK_PRODUCT_LINES_QUERY, ELEMENT_FEEDBACK_PRODUCT_LINES, ELEMENT_FEEDBACK_PRODUCT_LINE);
    }

    /***************************************************************************
     * Retrieves from Concentra database the content feedback general properties
     * 
     * @return ArrayList with the Elements containing the content feedback
     *         document general properties
     */
    private ArrayList getContentFeedbackInfo() {
        return getGeneralInfo("getContentFeedbackInfo", GET_CONTENT_FEEDBACK_INFO_QUERY);
    }

}
