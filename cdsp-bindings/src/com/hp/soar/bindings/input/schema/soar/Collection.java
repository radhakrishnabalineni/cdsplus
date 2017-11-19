//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:02:59 AM IST 
//


package com.hp.soar.bindings.input.schema.soar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}company_info"/>
 *         &lt;element ref="{}collection-title"/>
 *         &lt;element ref="{}collection-description"/>
 *         &lt;element ref="{}update-types"/>
 *         &lt;element ref="{}update-priority"/>
 *         &lt;element ref="{}update-date"/>
 *         &lt;element ref="{}software-type"/>
 *         &lt;element ref="{}product-line"/>
 *         &lt;element ref="{}contacts"/>
 *         &lt;element ref="{}compression-utility"/>
 *         &lt;element ref="{}dms-documents"/>
 *         &lt;element ref="{}copyright"/>
 *         &lt;element ref="{}related-collections"/>
 *         &lt;element ref="{}registration-flag"/>
 *         &lt;element ref="{}notification-flag"/>
 *         &lt;element ref="{}flags"/>
 *         &lt;element ref="{}attachments"/>
 *         &lt;element ref="{}availability-schedules"/>
 *         &lt;element ref="{}software-items"/>
 *       &lt;/sequence>
 *       &lt;attribute name="collection-ID" use="required" type="{}string-length-1-20" />
 *       &lt;attribute name="partner-update-type" use="required" type="{}partner-update-type" />
 *       &lt;attribute name="soar-msg-ID" use="required" type="{}string-length-1-30" />
 *       &lt;attribute name="state" use="required" type="{}state" />
 *       &lt;attribute name="submittal-group-oid" use="required" type="{}string-length-1-32" />
 *       &lt;attribute name="submittal-type-oid" use="required" type="{}string-length-1-10" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "companyInfo",
    "collectionTitle",
    "collectionDescription",
    "updateTypes",
    "updatePriority",
    "updateDate",
    "softwareType",
    "productLine",
    "contacts",
    "compressionUtility",
    "dmsDocuments",
    "copyright",
    "relatedCollections",
    "registrationFlag",
    "notificationFlag",
    "flags",
    "attachments",
    "availabilitySchedules",
    "softwareItems"
})
@XmlRootElement(name = "collection")
public class Collection {

    @XmlElement(name = "company_info", required = true)
    protected String companyInfo;
    @XmlElement(name = "collection-title", required = true)
    protected String collectionTitle;
    @XmlElement(name = "collection-description", required = true)
    protected String collectionDescription;
    @XmlElement(name = "update-types", required = true)
    protected UpdateTypes updateTypes;
    @XmlElement(name = "update-priority", required = true)
    protected UpdatePriority updatePriority;
    @XmlElement(name = "update-date", required = true)
    protected XMLGregorianCalendar updateDate;
    @XmlElement(name = "software-type", required = true)
    protected SoftwareType softwareType;
    @XmlElement(name = "product-line", required = true)
    protected String productLine;
    @XmlElement(required = true)
    protected Contacts contacts;
    @XmlElement(name = "compression-utility", required = true)
    protected CompressionUtility compressionUtility;
    @XmlElement(name = "dms-documents", required = true)
    protected DmsDocuments dmsDocuments;
    @XmlElement(required = true)
    protected Copyright copyright;
    @XmlElement(name = "related-collections", required = true)
    protected RelatedCollections relatedCollections;
    @XmlElement(name = "registration-flag", required = true)
    protected StringValueYesNo registrationFlag;
    @XmlElement(name = "notification-flag", required = true)
    protected StringValueYesNo notificationFlag;
    @XmlElement(required = true, nillable = true)
    protected Flags flags;
    @XmlElement(required = true)
    protected Attachments attachments;
    @XmlElement(name = "availability-schedules", required = true)
    protected AvailabilitySchedules availabilitySchedules;
    @XmlElement(name = "software-items", required = true)
    protected SoftwareItems softwareItems;
    @XmlAttribute(name = "collection-ID", required = true)
    protected String collectionID;
    @XmlAttribute(name = "partner-update-type", required = true)
    protected PartnerUpdateType partnerUpdateType;
    @XmlAttribute(name = "soar-msg-ID", required = true)
    protected String soarMsgID;
    @XmlAttribute(required = true)
    protected State state;
    @XmlAttribute(name = "submittal-group-oid", required = true)
    protected String submittalGroupOid;
    @XmlAttribute(name = "submittal-type-oid", required = true)
    protected String submittalTypeOid;

    /**
     * Gets the value of the companyInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyInfo() {
        return companyInfo;
    }

    /**
     * Sets the value of the companyInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyInfo(String value) {
        this.companyInfo = value;
    }

    /**
     * Gets the value of the collectionTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionTitle() {
        return collectionTitle;
    }

    /**
     * Sets the value of the collectionTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionTitle(String value) {
        this.collectionTitle = value;
    }

    /**
     * Gets the value of the collectionDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionDescription() {
        return collectionDescription;
    }

    /**
     * Sets the value of the collectionDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionDescription(String value) {
        this.collectionDescription = value;
    }

    /**
     * Gets the value of the updateTypes property.
     * 
     * @return
     *     possible object is
     *     {@link UpdateTypes }
     *     
     */
    public UpdateTypes getUpdateTypes() {
        return updateTypes;
    }

    /**
     * Sets the value of the updateTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateTypes }
     *     
     */
    public void setUpdateTypes(UpdateTypes value) {
        this.updateTypes = value;
    }

    /**
     * Gets the value of the updatePriority property.
     * 
     * @return
     *     possible object is
     *     {@link UpdatePriority }
     *     
     */
    public UpdatePriority getUpdatePriority() {
        return updatePriority;
    }

    /**
     * Sets the value of the updatePriority property.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdatePriority }
     *     
     */
    public void setUpdatePriority(UpdatePriority value) {
        this.updatePriority = value;
    }

    /**
     * Gets the value of the updateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUpdateDate() {
        return updateDate;
    }

    /**
     * Sets the value of the updateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUpdateDate(XMLGregorianCalendar value) {
        this.updateDate = value;
    }

    /**
     * Gets the value of the softwareType property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwareType }
     *     
     */
    public SoftwareType getSoftwareType() {
        return softwareType;
    }

    /**
     * Sets the value of the softwareType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwareType }
     *     
     */
    public void setSoftwareType(SoftwareType value) {
        this.softwareType = value;
    }

    /**
     * Gets the value of the productLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductLine() {
        return productLine;
    }

    /**
     * Sets the value of the productLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductLine(String value) {
        this.productLine = value;
    }

    /**
     * Gets the value of the contacts property.
     * 
     * @return
     *     possible object is
     *     {@link Contacts }
     *     
     */
    public Contacts getContacts() {
        return contacts;
    }

    /**
     * Sets the value of the contacts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Contacts }
     *     
     */
    public void setContacts(Contacts value) {
        this.contacts = value;
    }

    /**
     * Gets the value of the compressionUtility property.
     * 
     * @return
     *     possible object is
     *     {@link CompressionUtility }
     *     
     */
    public CompressionUtility getCompressionUtility() {
        return compressionUtility;
    }

    /**
     * Sets the value of the compressionUtility property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompressionUtility }
     *     
     */
    public void setCompressionUtility(CompressionUtility value) {
        this.compressionUtility = value;
    }

    /**
     * Gets the value of the dmsDocuments property.
     * 
     * @return
     *     possible object is
     *     {@link DmsDocuments }
     *     
     */
    public DmsDocuments getDmsDocuments() {
        return dmsDocuments;
    }

    /**
     * Sets the value of the dmsDocuments property.
     * 
     * @param value
     *     allowed object is
     *     {@link DmsDocuments }
     *     
     */
    public void setDmsDocuments(DmsDocuments value) {
        this.dmsDocuments = value;
    }

    /**
     * Gets the value of the copyright property.
     * 
     * @return
     *     possible object is
     *     {@link Copyright }
     *     
     */
    public Copyright getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link Copyright }
     *     
     */
    public void setCopyright(Copyright value) {
        this.copyright = value;
    }

    /**
     * Gets the value of the relatedCollections property.
     * 
     * @return
     *     possible object is
     *     {@link RelatedCollections }
     *     
     */
    public RelatedCollections getRelatedCollections() {
        return relatedCollections;
    }

    /**
     * Sets the value of the relatedCollections property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelatedCollections }
     *     
     */
    public void setRelatedCollections(RelatedCollections value) {
        this.relatedCollections = value;
    }

    /**
     * Gets the value of the registrationFlag property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getRegistrationFlag() {
        return registrationFlag;
    }

    /**
     * Sets the value of the registrationFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setRegistrationFlag(StringValueYesNo value) {
        this.registrationFlag = value;
    }

    /**
     * Gets the value of the notificationFlag property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getNotificationFlag() {
        return notificationFlag;
    }

    /**
     * Sets the value of the notificationFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setNotificationFlag(StringValueYesNo value) {
        this.notificationFlag = value;
    }

    /**
     * Gets the value of the flags property.
     * 
     * @return
     *     possible object is
     *     {@link Flags }
     *     
     */
    public Flags getFlags() {
        return flags;
    }

    /**
     * Sets the value of the flags property.
     * 
     * @param value
     *     allowed object is
     *     {@link Flags }
     *     
     */
    public void setFlags(Flags value) {
        this.flags = value;
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link Attachments }
     *     
     */
    public Attachments getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Attachments }
     *     
     */
    public void setAttachments(Attachments value) {
        this.attachments = value;
    }

    /**
     * Gets the value of the availabilitySchedules property.
     * 
     * @return
     *     possible object is
     *     {@link AvailabilitySchedules }
     *     
     */
    public AvailabilitySchedules getAvailabilitySchedules() {
        return availabilitySchedules;
    }

    /**
     * Sets the value of the availabilitySchedules property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailabilitySchedules }
     *     
     */
    public void setAvailabilitySchedules(AvailabilitySchedules value) {
        this.availabilitySchedules = value;
    }

    /**
     * Gets the value of the softwareItems property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwareItems }
     *     
     */
    public SoftwareItems getSoftwareItems() {
        return softwareItems;
    }

    /**
     * Sets the value of the softwareItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwareItems }
     *     
     */
    public void setSoftwareItems(SoftwareItems value) {
        this.softwareItems = value;
    }

    /**
     * Gets the value of the collectionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionID() {
        return collectionID;
    }

    /**
     * Sets the value of the collectionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionID(String value) {
        this.collectionID = value;
    }

    /**
     * Gets the value of the partnerUpdateType property.
     * 
     * @return
     *     possible object is
     *     {@link PartnerUpdateType }
     *     
     */
    public PartnerUpdateType getPartnerUpdateType() {
        return partnerUpdateType;
    }

    /**
     * Sets the value of the partnerUpdateType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartnerUpdateType }
     *     
     */
    public void setPartnerUpdateType(PartnerUpdateType value) {
        this.partnerUpdateType = value;
    }

    /**
     * Gets the value of the soarMsgID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoarMsgID() {
        return soarMsgID;
    }

    /**
     * Sets the value of the soarMsgID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoarMsgID(String value) {
        this.soarMsgID = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link State }
     *     
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link State }
     *     
     */
    public void setState(State value) {
        this.state = value;
    }

    /**
     * Gets the value of the submittalGroupOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubmittalGroupOid() {
        return submittalGroupOid;
    }

    /**
     * Sets the value of the submittalGroupOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmittalGroupOid(String value) {
        this.submittalGroupOid = value;
    }

    /**
     * Gets the value of the submittalTypeOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubmittalTypeOid() {
        return submittalTypeOid;
    }

    /**
     * Sets the value of the submittalTypeOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmittalTypeOid(String value) {
        this.submittalTypeOid = value;
    }

}
