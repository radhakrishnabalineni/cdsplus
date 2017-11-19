package com.hp.concentra.extractor.documents;

import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.utils.LoaderLog;

/**
 * Factory class that, given a object type, returns the most appropriate
 * Concentra Document object
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */

public class ConcentraDocFactory {
    private static final String STANDARD_MARKETING_DOC = "c_standard_marketing_doc";
    private static final String PRODUCT_SETUP_DOC = "c_product_setup_doc";
    private static final String MARKETING_DOC = "c_marketing_doc";
    private static final String GENERAL_PURPOSE_DOC = "c_general_purpose_doc";
    private static final String CONTENT_FEEDBACK_DOC = "c_content_feedback_doc";
    private static final String LIBRARY_DOC = "c_library_doc";

    /***************************************************************************
     * Method that, given an object type, returns the most appropriate Concentra
     * Document object
     * 
     * @param objectType
     *            Indicates the object type to be retrieved from Concentra
     * @param objectId
     *            Unique identifier of the object to be retrieved from Concentra
     * @param chronicleId
     *            Chronicle identifier of the object to be retrieved from
     *            Concentra
     * @return Concentra Document
     */
    public static synchronized ConcentraDoc getDocumentInstance(String objectType, String objectId, String chronicleId,
            String token, String colId, int priority, String activeFlag) {
        ConcentraDoc doc = null;

        if (objectType.equals(CONTENT_FEEDBACK_DOC)) {
            doc = new ContentFeedbackDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(GENERAL_PURPOSE_DOC)) {
            doc = new GeneralPurposeDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(ExtractorConstants.HHO_MARKETING_DOC)) {
            doc = new HhoMarketingDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(LIBRARY_DOC)) {
            doc = new LibraryDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(ExtractorConstants.MANUAL_DOC)) {
            doc = new ManualDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(MARKETING_DOC)) {
            doc = new MarketingDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(STANDARD_MARKETING_DOC)) {
            doc = new StandardMarketingDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(PRODUCT_SETUP_DOC)) {
            doc = new ProductSetupDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else if (objectType.equals(ExtractorConstants.SUPPORT_DOC)) {
            doc = new SupportDoc(objectId, chronicleId, token, objectType, colId, priority, activeFlag);
        } else {
            LoaderLog.fatal("The document object type "+objectType+" is not valid.");
        }
        return doc;
    }

    /***************************************************************************
     * Method that, given an object type, returns the most appropriate Concentra
     * Document object.  This is used to get doc type for delete/touch events
     * 
     * @param objectType
     *            Indicates the object type to be retrieved from Concentra
     * @return Concentra Document
     */
    public static synchronized ConcentraDoc getDocumentInstance(String objectType) {
      return getDocumentInstance(objectType, null, null, null, null, 1, null);
    }

}
