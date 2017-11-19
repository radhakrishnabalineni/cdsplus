package com.hp.soar.priorityLoader.workItem;

import java.io.File;

import org.dom4j.Element;

import com.documentum.fc.common.DfException;
import com.hp.cdspDestination.ProjContent;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.LoaderLog;

public class SoarExtractAttachment extends SoarExtractElement {

	// attachment to be stored
	private ProjContent projContent = null;
	
	// flag indicating this attachment has been done
	private boolean storeCompleted = false;
	
	public SoarExtractAttachment(SoarExtractionDBService dbService,
			String objectId, String updateType, String eventId, String collectionId, String itemId,
			StringBuffer cdspPath) throws DfException, ProcessingException {
		
		super(dbService, objectId, updateType);
		
		dbObj = dbService.getDocumentByQualification("sw_descriptive_content where r_object_id='" + objectId + "'");
		if (dbObj == null) {
			throw new IllegalArgumentException("No DB object for Attachment "+objectId);
		}
		  
		String contentName = dbObj.getString("object_name"); 
		if (!contentName.equals(FIRST_VERSION_ANTECEDENT_ID) && (updateType != ITEM_UPTODATE)) {
			// create ProjContent object (contentName is lower cased on creation);
			projContent = new ProjContent(SoarExtractElement.getContent_type(), contentName, null, true);
			
			if ((updateType != ITEM_DELETED) && (updateType != ITEM_IMMEDIATE_DELETED)) {
				// Item needs to be extracted
				StringBuffer localContentFile = new StringBuffer(SoarExtractElement.getContentPath().getAbsolutePath());

				localContentFile.append(FILE_SEP).append(eventId).append(FILE_SEP).append(collectionId);
				if (itemId != null) {
					localContentFile.append(FILE_SEP).append(itemId);
				}
				
				// create the checkout location and get the file 
				File contentFile = new File(localContentFile.toString().toLowerCase());
				contentFile.mkdirs();
				File contentDestPath = new File(contentFile, contentName.toLowerCase());
				if (!(contentDestPath.exists() && (contentDestPath.length() == dbObj.getContentSize()))) {
					// if we haven't already gotten this one, get it
					dbObj.getFile(contentDestPath.getAbsolutePath());
				}
				
				// make sure path ends in /
				String srcPath = contentFile.getAbsolutePath();
				if (!srcPath.endsWith(FILE_SEP)) {
					srcPath += FILE_SEP;
				}
				// set the src path for a load to cds+
				projContent.setContentPath(srcPath);
				
			}
			
			// create the destination path for the content
			// The item/filename are normalized to item_filename 
			StringBuffer itemPath = new StringBuffer();
			if (itemId != null) {
				itemPath.append(itemId).append(SoarExtractElement.file_separator);
			}
			itemPath.append(contentName);
			cdspPath.append(ProjContent.normalizeContentNameForUrl(itemPath.toString()));
			projContent.setPath(cdspPath.toString());
				
		}
	}

	protected void addAttachmentElement(Element atmtsElement, StringBuffer objPath) throws DfException, ProcessingException {

		Element attElement = SoarExtractXMLBuilder.addXMLElement(atmtsElement, "attachment");		

		SoarExtractXMLBuilder.addXMLElement(attElement, "attachment-title", dbObj.getString("title"));

		String[] authors = getRepeatingAttr(dbObj, "authors");
		Element authElement = SoarExtractXMLBuilder.addXMLElement(attElement, "authors", "");
		boolean authorFound = false;
		for (String author : authors) {
			if (author != null && !(author.trim().equals(""))) {
				Element authorEle = SoarExtractXMLBuilder.addXMLElement(authElement, "author", author.toString());
				SoarExtractXMLBuilder.addXMLAttribute(authorEle, "nil", "false", true);
				authorFound = true;
			}
		}

		if (!authorFound) {
			Element authorEle = SoarExtractXMLBuilder.addXMLElement(authElement, "author", "");
			SoarExtractXMLBuilder.addXMLAttribute(authorEle, "nil", "true", true);
		}

		String[] flagOids = getRepeatingAttr(dbObj, "flag_oids");
		String reviewFlag = hasStringValue(flagOids, "3");
		SoarExtractXMLBuilder.addXMLElement(attElement, "review-flag", reviewFlag);

		addFlagOidsElementForComponent(attElement, flagOids, "DESCRIPTIVE_CONTENT");

		String disLvlOid = dbObj.getString("disclosure_level_oid");
		Element disclosureElement = SoarExtractXMLBuilder.addXMLElement(attElement, "disclosure-level",
				ReferenceLists.getLabel(ReferenceLists.DISCLOSURE_LEVELS_LIST, disLvlOid));

		SoarExtractXMLBuilder.addXMLAttribute(disclosureElement, "oid", disLvlOid, false);

		// Add document-type
		// -----------------
		String[] docTypeIds = getRepeatingAttr(dbObj, "document_type_oids");
		Element docTypeOidsElement = SoarExtractXMLBuilder.addXMLElement(attElement, "document-types", "");
		for(String docType : docTypeIds) {
			Element docTypeEle = SoarExtractXMLBuilder.addXMLElement(docTypeOidsElement, "document-type",
					ReferenceLists.getLabel(ReferenceLists.DOCUMENT_TYPES_LIST, docType));

			SoarExtractXMLBuilder.addXMLAttribute(docTypeEle, "oid", docType, false);
		}

		addLanguageOidsElement(attElement, dbObj);

		String attSrc = dbObj.getString("source");
		SoarExtractXMLBuilder.addNullbaleElement(attElement, "attachment-source", attSrc);
		
		SoarExtractXMLBuilder.addXMLElement(attElement, "format", dbObj.getString("a_content_type"));

		String contentName = dbObj.getString("object_name");
		if (contentName.equals("")) {
			contentName = dbObj.getString("i_contents_id");
		}
		SoarExtractXMLBuilder.addXMLElement(attElement, "filename", contentName);
		
		objPath.append(contentName);
		
		// add the linked-content element
		Element linkedContentElement = SoarExtractXMLBuilder.addXMLElement(attElement, "linked-content",
				//objPath.toString().toLowerCase());
				objPath.toString());
		StringBuffer soarDto = new StringBuffer(",");
		for (String docType : docTypeIds) {
			soarDto.append(docType).append(",");
		}
		SoarExtractXMLBuilder.addXMLAttribute(linkedContentElement,"soar-dto", soarDto.toString(), false);

	}
	
	/**
	 * doStore puts the projConent into cds+
	 * @throws ProcessingException
	 */
	public void doStore(Integer priority) throws ProcessingException  {
		if (!storeCompleted) {
			try {
				storePC(projContent, priority);
				storeCompleted = true;
			} catch (ProcessingException e) {
				try {
					LoaderLog.error("Attachment "+dbObj.getString("object_name")+" update failed "+e.getMessage());
				} catch (DfException e1) {
					LoaderLog.error("Attachment <noName> update failed "+e.getMessage());
				}
				throw e;
			}
		}
	}
	
	@Override
	protected String getIdentifier() {
		return null;
	}

}
