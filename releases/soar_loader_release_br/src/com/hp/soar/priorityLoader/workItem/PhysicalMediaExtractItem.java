package com.hp.soar.priorityLoader.workItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.dom4j.Element;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.helper.SoarExtractionDBService;
import com.hp.soar.priorityLoader.ref.ReferenceLists;
import com.hp.soar.priorityLoader.utils.LoaderLog;

/**
 * PhysicalMedia extract item overrides addtributes and elements of a swItem that are specific to 
 * physical media metadata
 * @author dahlm
 *
 */
public class PhysicalMediaExtractItem extends SoarExtractItem {

	// ICM changes
	public PhysicalMediaExtractItem(SoarExtractionDBService dbService,
			String eventId, String objectId, boolean icmDriver, String icmRule, String chronicleId,
			String collectionId, String itemId, String itemState,
			boolean isSuspended, boolean collectionAccepted, Date lastExtractionTime) throws DfException, ProcessingException, IOException {
		
		super(dbService, eventId, objectId, icmDriver, icmRule, chronicleId, collectionId,
				itemId, itemState, isSuspended, collectionAccepted, lastExtractionTime);
	}

	protected void addOrderablePartId(Element itemElement) throws DfException {
        // RCD: FOR physical media collections, if we have to ignore new RCD changes till 
        // we release RCD data for which the item id will be generated and not be user input.
        // so as a work around if the ignore_rcd is true in the ini file of extrator it will make the orderable part num as item id
        // because for RCD new metadata properties requirement the item id for physical media has to be generated and the user input 
        // of unique orderable part num will be taken, which is old item id before RCD req.

        // checking if its physical meida type if yes then checking if its ignore RCD if yes then orderable part num as item id		
        if (ignoreRCD){ 
        	String tmpOrdPartID = dbObj.getString("orderable_part_id");
        	if(tmpOrdPartID!=null && !tmpOrdPartID.equals("")){
        		SoarExtractXMLBuilder.addXMLAttribute(itemElement, "item-ID", tmpOrdPartID, false);
        	}
        } else {
        	super.addOrderablePartId(itemElement);
        }
	}
	
	protected void addBOMElements(Element itemElement) throws DfException {
		 /*
		  * ***************************************************************************************************************************************
		  * @Vishnu Gowda Harish
		  * January 30th,2011
		  * SOAR11.1 RCD
		  * for bil of materials property
		  * will look something like this..
		  * <boms>
		  *      <bom>
		  *          <bom-value>qwse</bom-value>   -- this should be minoccurs=1 in xsd,bcoz every bom should have this value
		  *          <level oid ="1">Level1<level> -- this should be minoccurs=1 in xsd,bcoz every bom should have this value
		  *          <checksum-type oid="5">CRC</checksum-type> -- minoccurs=0, not a required property
		  *          <checksum-value>12sqsqw2</checksum-value> -- minoccurs=0, not a required property
		  *          <image-size>50</image-size> -- minoccurs=0, not a required property
		  *     </bom>
		  * </boms>.
		  * this property should be defined in all the xsd's required after customs-approved property, maintain the same sequence in xsd, as what you have
		  * written in the code
		  * BSC and WILDCAT partner not included for this property
		  * 
		  */

		 ArrayList<String> checkSumList = new ArrayList<String>();
		 String[] bomIds = getRepeatingAttr(dbObj,"bom_ids");
		 Element swBomsElement = null;
		 if(bomIds!=null && bomIds.length > 0){
			 for(String bomId : bomIds) {
				 if (bomId.equals("blank") || bomId.equals("")) {
					 continue;
				 }
				 IDfPersistentObject bomObj = dbService.getObjectByQualification("sw_bom where r_object_id='" + bomId + "'");
				 if (bomObj == null) {
					 LoaderLog.error("Failed to get bomObj for Id <" + bomId + ">");
					 continue;
				 }
				 if (swBomsElement == null) {
					 swBomsElement = SoarExtractXMLBuilder.addXMLElement(itemElement, "boms");
				 }
				 Element swBomElement=SoarExtractXMLBuilder.addXMLElement(swBomsElement,"bom");
				 String bomValue=bomObj.getString("bom_value");
				 if(bomValue != null && !bomValue.equals(""))
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"bom-value",bomValue);

				 String levelOid=bomObj.getString("level_oid");
				 if(levelOid!=null && !levelOid.equals("")) {
					 Element bomLevel=SoarExtractXMLBuilder.addXMLElement(swBomElement,"level",
							 ReferenceLists.getLabel(ReferenceLists.BOM_LEVELS_LIST, levelOid));
					 SoarExtractXMLBuilder.addXMLAttribute(bomLevel,"oid", levelOid, false);
				 }


				 String checksumTypeOid=bomObj.getString("checksum_type_oid");
				 if(checksumTypeOid!=null && !checksumTypeOid.equals("")){
					 Element bomChecksumType=SoarExtractXMLBuilder.addXMLElement(swBomElement,"checksum-type",
							 ReferenceLists.getLabel(ReferenceLists.BOM_CHECKSUMS_LIST, checksumTypeOid));
					 SoarExtractXMLBuilder.addXMLAttribute(bomChecksumType,"oid", checksumTypeOid, false);
				 }
				 
				 
				 String checksumValue=bomObj.getString("checksum_value");
				 if(checksumValue!=null && !checksumValue.equals("")) {
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"checksum-value",checksumValue);
					 checkSumList.add(checksumValue);
				 }

				 String imageSize=bomObj.getString("image_size");
				 if(imageSize!=null & !imageSize.equals(""))
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"image-size",imageSize);

				 // Code for adding bom new fields Sequence,quantity,Description need to check with vishnu

				 String bomSequence=bomObj.getString("bom_sequence");
				 if(bomSequence!=null & !bomSequence.equals(""))
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"bom-sequence",bomSequence);


				 String bomQuantity= bomObj.getString("bom_quantity");	
				 if(bomQuantity!=null & !bomQuantity.equals(""))
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"bom-quantity",bomQuantity);


				 String bomDescription= bomObj.getString("bom_description");
				 if(bomDescription!=null & !bomDescription.equals(""))
					 SoarExtractXMLBuilder.addXMLElement(swBomElement,"bom-description",bomDescription);							
			 }
		 }
	}
		
	protected void addOrderablePartIdElement(Element itemElement) throws DfException {
		/*@Vishnu Gowda Harish
		 * January 30th,2011
		 * SOAR11.1,RCD
		 * Below is the extraction for project name property, which exists in all submittal types.
		 *  will look something like this.
		 *  <orderable-part-ID> test123</orderable-part-ID> -- minoccurs as 0//as some partners will be looking to extract rcd and other types also
		 *  This element in the schema should be defined before project-name. maintain the sequence
		 * BSC and WILDCAT partner not included for this property
		 * 
		 */
		String partId = dbObj.getString("orderable_part_id"); 
		if( partId != null && !partId.equals("")){
			SoarExtractXMLBuilder.addXMLElement(itemElement, "orderable-part-ID", partId);
			if(dbObj.getValue("customs_approved")!=null){
				String customsAppvd = dbObj.getValue("customs_approved").asString().equals("F") ? "No" : "Yes";
				SoarExtractXMLBuilder.addXMLElement(itemElement, "customs-approved",customsAppvd);
			}
		}
	}


	protected void addOrderLinksElement(Element itemElement) throws DfException {
		String[] orderLinkOids = getRepeatingAttr(dbObj, "order_link_oids");
		if (orderLinkOids.length > 0) {
			Element ordLinkOidsElement = SoarExtractXMLBuilder.addXMLElement(itemElement,"order-links");
			for(String orderLinkOid : orderLinkOids) {
				Element ordLinkElement = SoarExtractXMLBuilder.addXMLElement(ordLinkOidsElement,"order-link",
						ReferenceLists.getLabel(ReferenceLists.ORDER_LINKS_LIST, orderLinkOid));
				SoarExtractXMLBuilder.addXMLAttribute(ordLinkElement, "oid", orderLinkOid, false);
			}
		}
	}
	
}
