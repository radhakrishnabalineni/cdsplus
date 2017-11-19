package com.hp.concentra.extractor.documents;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.concentra.extractor.utils.ExtractorConstants;
import com.hp.concentra.extractor.workItem.ConcentraExtractElement;

/**
 * Class that retrieves all the needed information for the Library document
 * property file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class LibraryDoc extends com.hp.concentra.extractor.documents.ConcentraDoc {

    private static final String QUERY_GET_LIBRARY_DOC_INFO = "getLibraryDocInfo";
    private static final String QUERY_GET_ORIENTATION = "getOrientation";
    private static final String ELEMENT_ORIENTATION = "orientation";
    private static final String QUERY_GET_COLOR = "getColor";
    private static final String ELEMENT_COLOR = "color";

    public LibraryDoc(String objectId, String chronicleId, String token, String objectType, String colId, int priority,
            String activeFlag) {
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
        ArrayList libraryDocInfo = getLibraryDocInfo();
        ArrayList contentElementsThatMightHaveNull = new ArrayList();

        contentElementsThatMightHaveNull.add(getDocumentTypeDetails());
        contentElementsThatMightHaveNull.add(getProductLevels());
        contentElementsThatMightHaveNull.add(getAuthors());
        contentElementsThatMightHaveNull.add(getRegions());
        contentElementsThatMightHaveNull.add(getSearchKeywords());
        contentElementsThatMightHaveNull.add(getContentType());
        contentElementsThatMightHaveNull.add(getLanguageCode());
        contentElementsThatMightHaveNull.add(getPropertyUpdateDate());
        contentElementsThatMightHaveNull.add(getContentUpdateDate());
        contentElementsThatMightHaveNull.add(getOrientation());
        contentElementsThatMightHaveNull.add(getAcronym());
        contentElementsThatMightHaveNull.add(getFeedbackAddress());
        contentElementsThatMightHaveNull.add(getCollectionValidFlag());
        contentElementsThatMightHaveNull.add(getInformationSource());
        contentElementsThatMightHaveNull.add(getPlannedPublicDate());
        contentElementsThatMightHaveNull.add(getMasterObjectName());
        contentElementsThatMightHaveNull.add(getExtraProperties());
        contentElementsThatMightHaveNull.add(getComponents());
        contentElementsThatMightHaveNull.add(getFileName());
        contentElementsThatMightHaveNull.add(getColor());

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

        for (int contentIter = 0; contentIter < libraryDocInfo.size(); contentIter++) {
            if (libraryDocInfo.get(contentIter) != null) {
                contentElements.add(libraryDocInfo.get(contentIter));
            }
        }

        return contentElements;
    }

    private ArrayList getLibraryDocInfo() {
        return getGeneralInfo("getLibraryDocInfo", QUERY_GET_LIBRARY_DOC_INFO);
    }

    /***************************************************************************
     * Retrieves from the Concentra database the Orientation of the given
     * document
     * 
     * @return Xml Element with the Orientation
     */
    protected Element getOrientation() {
        return getSingleElementFromObjectTypeId("getOrientation", QUERY_GET_ORIENTATION,
                ELEMENT_ORIENTATION);
    }
    
    protected Element getColor(){
    	return getSingleElementFromObjectColumnId("getColor",QUERY_GET_COLOR,ELEMENT_COLOR);
    }

}
