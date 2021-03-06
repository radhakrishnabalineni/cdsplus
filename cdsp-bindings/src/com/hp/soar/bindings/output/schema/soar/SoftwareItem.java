//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:00 AM IST 
//


package com.hp.soar.bindings.output.schema.soar;

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
 *         &lt;element ref="{}version"/>
 *         &lt;element ref="{}orderable-part-ID" minOccurs="0"/>
 *         &lt;element ref="{}customs-approved" minOccurs="0"/>
 *         &lt;element ref="{}boms" minOccurs="0"/>
 *         &lt;element ref="{}install-formats"/>
 *         &lt;element ref="{}replaces-item-ID"/>
 *         &lt;element ref="{}supersede-info"/>
 *         &lt;element ref="{}disclosure-level"/>
 *         &lt;element ref="{}media-types"/>
 *         &lt;element ref="{}update-types"/>
 *         &lt;element ref="{}severity"/>
 *         &lt;element ref="{}update-date"/>
 *         &lt;element ref="{}customer-available-date"/>
 *         &lt;element ref="{}obsolete-date"/>
 *         &lt;element ref="{}products-supported"/>
 *         &lt;element ref="{}products-environments-supported"/>
 *         &lt;element ref="{}taxonomy-categorizations"/>
 *         &lt;element ref="{}item-suspended"/>
 *         &lt;element ref="{}flags"/>
 *         &lt;element ref="{}languages"/>
 *         &lt;element ref="{}distribution-regions" minOccurs="0"/>
 *         &lt;element ref="{}pricing"/>
 *         &lt;element ref="{}software-password"/>
 *         &lt;element ref="{}installation-filename"/>
 *         &lt;element ref="{}physical-fulfillment" minOccurs="0"/>
 *         &lt;element ref="{}bundled-items"/>
 *         &lt;element ref="{}cd-checkvalues" minOccurs="0"/>
 *         &lt;element ref="{}order-links" minOccurs="0"/>
 *         &lt;element ref="{}item-url"/>
 *         &lt;element ref="{}partner-feedback"/>
 *         &lt;element ref="{}iadd-properties"/>
 *         &lt;element ref="{}software-files"/>
 *         &lt;element ref="{}attachments"/>
 *       &lt;/sequence>
 *       &lt;attribute name="chronicle-ID" use="required" type="{}string-length-1-16" />
 *       &lt;attribute name="from-toad" type="{}string-value-Yes-No" />
 *       &lt;attribute name="item-ID" use="required" type="{}string-length-1-30" />
 *       &lt;attribute name="languages-at-file-level" type="{}string-value-Yes-No" />
 *       &lt;attribute name="partner-update-type" use="required" type="{}partner-update-type" />
 *       &lt;attribute name="product-level-sensitive" type="{}string-value-Yes-No" />
 *       &lt;attribute name="soar-description-overridden" type="{}string-value-Yes-No" />
 *       &lt;attribute name="soar-distinct-environments" type="{}string-length-1-n" />
 *       &lt;attribute name="state" use="required" type="{}state" />
 *       &lt;attribute name="toad-OID" type="{}string-length-1-11" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "version",
    "orderablePartID",
    "customsApproved",
    "boms",
    "installFormats",
    "replacesItemID",
    "supersedeInfo",
    "disclosureLevel",
    "mediaTypes",
    "updateTypes",
    "severity",
    "updateDate",
    "customerAvailableDate",
    "obsoleteDate",
    "productsSupported",
    "productsEnvironmentsSupported",
    "taxonomyCategorizations",
    "itemSuspended",
    "flags",
    "languages",
    "distributionRegions",
    "pricing",
    "softwarePassword",
    "installationFilename",
    "physicalFulfillment",
    "bundledItems",
    "cdCheckvalues",
    "orderLinks",
    "itemUrl",
    "partnerFeedback",
    "iaddProperties",
    "softwareFiles",
    "attachments"
})
@XmlRootElement(name = "software-item")
public class SoftwareItem {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(name = "orderable-part-ID")
    protected String orderablePartID;
    @XmlElement(name = "customs-approved")
    protected StringValueYesNo customsApproved;
    protected Boms boms;
    @XmlElement(name = "install-formats", required = true, nillable = true)
    protected InstallFormats installFormats;
    @XmlElement(name = "replaces-item-ID", required = true, nillable = true)
    protected String replacesItemID;
    @XmlElement(name = "supersede-info", required = true)
    protected SupersedeInfo supersedeInfo;
    @XmlElement(name = "disclosure-level", required = true)
    protected DisclosureLevel disclosureLevel;
    @XmlElement(name = "media-types", required = true)
    protected MediaTypes mediaTypes;
    @XmlElement(name = "update-types", required = true)
    protected UpdateTypes updateTypes;
    @XmlElement(required = true)
    protected Severity severity;
    @XmlElement(name = "update-date", required = true)
    protected XMLGregorianCalendar updateDate;
    @XmlElement(name = "customer-available-date", required = true)
    protected XMLGregorianCalendar customerAvailableDate;
    @XmlElement(name = "obsolete-date", required = true, nillable = true)
    protected XMLGregorianCalendar obsoleteDate;
    @XmlElement(name = "products-supported", required = true)
    protected ProductsSupported productsSupported;
    @XmlElement(name = "products-environments-supported", required = true)
    protected ProductsEnvironmentsSupported productsEnvironmentsSupported;
    @XmlElement(name = "taxonomy-categorizations", required = true)
    protected TaxonomyCategorizations taxonomyCategorizations;
    @XmlElement(name = "item-suspended", required = true)
    protected ItemSuspended itemSuspended;
    @XmlElement(required = true, nillable = true)
    protected Flags flags;
    @XmlElement(required = true)
    protected Languages languages;
    @XmlElement(name = "distribution-regions")
    protected DistributionRegions distributionRegions;
    @XmlElement(required = true)
    protected Pricing pricing;
    @XmlElement(name = "software-password", required = true, nillable = true)
    protected String softwarePassword;
    @XmlElement(name = "installation-filename", required = true, nillable = true)
    protected String installationFilename;
    @XmlElement(name = "physical-fulfillment")
    protected PhysicalFulfillment physicalFulfillment;
    @XmlElement(name = "bundled-items", required = true)
    protected BundledItems bundledItems;
    @XmlElement(name = "cd-checkvalues")
    protected CdCheckvalues cdCheckvalues;
    @XmlElement(name = "order-links")
    protected OrderLinks orderLinks;
    @XmlElement(name = "item-url", required = true, nillable = true)
    protected String itemUrl;
    @XmlElement(name = "partner-feedback", required = true)
    protected PartnerFeedback partnerFeedback;
    @XmlElement(name = "iadd-properties", required = true, nillable = true)
    protected IaddProperties iaddProperties;
    @XmlElement(name = "software-files", required = true)
    protected SoftwareFiles softwareFiles;
    @XmlElement(required = true)
    protected Attachments attachments;
    @XmlAttribute(name = "chronicle-ID", required = true)
    protected String chronicleID;
    @XmlAttribute(name = "from-toad")
    protected StringValueYesNo fromToad;
    @XmlAttribute(name = "item-ID", required = true)
    protected String itemID;
    @XmlAttribute(name = "languages-at-file-level")
    protected StringValueYesNo languagesAtFileLevel;
    @XmlAttribute(name = "partner-update-type", required = true)
    protected PartnerUpdateType partnerUpdateType;
    @XmlAttribute(name = "product-level-sensitive")
    protected StringValueYesNo productLevelSensitive;
    @XmlAttribute(name = "soar-description-overridden")
    protected StringValueYesNo soarDescriptionOverridden;
    @XmlAttribute(name = "soar-distinct-environments")
    protected String soarDistinctEnvironments;
    @XmlAttribute(required = true)
    protected State state;
    @XmlAttribute(name = "toad-OID")
    protected String toadOID;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the orderablePartID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderablePartID() {
        return orderablePartID;
    }

    /**
     * Sets the value of the orderablePartID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderablePartID(String value) {
        this.orderablePartID = value;
    }

    /**
     * Gets the value of the customsApproved property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getCustomsApproved() {
        return customsApproved;
    }

    /**
     * Sets the value of the customsApproved property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setCustomsApproved(StringValueYesNo value) {
        this.customsApproved = value;
    }

    /**
     * Gets the value of the boms property.
     * 
     * @return
     *     possible object is
     *     {@link Boms }
     *     
     */
    public Boms getBoms() {
        return boms;
    }

    /**
     * Sets the value of the boms property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boms }
     *     
     */
    public void setBoms(Boms value) {
        this.boms = value;
    }

    /**
     * Gets the value of the installFormats property.
     * 
     * @return
     *     possible object is
     *     {@link InstallFormats }
     *     
     */
    public InstallFormats getInstallFormats() {
        return installFormats;
    }

    /**
     * Sets the value of the installFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link InstallFormats }
     *     
     */
    public void setInstallFormats(InstallFormats value) {
        this.installFormats = value;
    }

    /**
     * Gets the value of the replacesItemID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplacesItemID() {
        return replacesItemID;
    }

    /**
     * Sets the value of the replacesItemID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplacesItemID(String value) {
        this.replacesItemID = value;
    }

    /**
     * Gets the value of the supersedeInfo property.
     * 
     * @return
     *     possible object is
     *     {@link SupersedeInfo }
     *     
     */
    public SupersedeInfo getSupersedeInfo() {
        return supersedeInfo;
    }

    /**
     * Sets the value of the supersedeInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupersedeInfo }
     *     
     */
    public void setSupersedeInfo(SupersedeInfo value) {
        this.supersedeInfo = value;
    }

    /**
     * Gets the value of the disclosureLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DisclosureLevel }
     *     
     */
    public DisclosureLevel getDisclosureLevel() {
        return disclosureLevel;
    }

    /**
     * Sets the value of the disclosureLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisclosureLevel }
     *     
     */
    public void setDisclosureLevel(DisclosureLevel value) {
        this.disclosureLevel = value;
    }

    /**
     * Gets the value of the mediaTypes property.
     * 
     * @return
     *     possible object is
     *     {@link MediaTypes }
     *     
     */
    public MediaTypes getMediaTypes() {
        return mediaTypes;
    }

    /**
     * Sets the value of the mediaTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link MediaTypes }
     *     
     */
    public void setMediaTypes(MediaTypes value) {
        this.mediaTypes = value;
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
     * Gets the value of the severity property.
     * 
     * @return
     *     possible object is
     *     {@link Severity }
     *     
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the value of the severity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Severity }
     *     
     */
    public void setSeverity(Severity value) {
        this.severity = value;
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
     * Gets the value of the customerAvailableDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCustomerAvailableDate() {
        return customerAvailableDate;
    }

    /**
     * Sets the value of the customerAvailableDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCustomerAvailableDate(XMLGregorianCalendar value) {
        this.customerAvailableDate = value;
    }

    /**
     * Gets the value of the obsoleteDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getObsoleteDate() {
        return obsoleteDate;
    }

    /**
     * Sets the value of the obsoleteDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setObsoleteDate(XMLGregorianCalendar value) {
        this.obsoleteDate = value;
    }

    /**
     * Gets the value of the productsSupported property.
     * 
     * @return
     *     possible object is
     *     {@link ProductsSupported }
     *     
     */
    public ProductsSupported getProductsSupported() {
        return productsSupported;
    }

    /**
     * Sets the value of the productsSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductsSupported }
     *     
     */
    public void setProductsSupported(ProductsSupported value) {
        this.productsSupported = value;
    }

    /**
     * Gets the value of the productsEnvironmentsSupported property.
     * 
     * @return
     *     possible object is
     *     {@link ProductsEnvironmentsSupported }
     *     
     */
    public ProductsEnvironmentsSupported getProductsEnvironmentsSupported() {
        return productsEnvironmentsSupported;
    }

    /**
     * Sets the value of the productsEnvironmentsSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductsEnvironmentsSupported }
     *     
     */
    public void setProductsEnvironmentsSupported(ProductsEnvironmentsSupported value) {
        this.productsEnvironmentsSupported = value;
    }

    /**
     * Gets the value of the taxonomyCategorizations property.
     * 
     * @return
     *     possible object is
     *     {@link TaxonomyCategorizations }
     *     
     */
    public TaxonomyCategorizations getTaxonomyCategorizations() {
        return taxonomyCategorizations;
    }

    /**
     * Sets the value of the taxonomyCategorizations property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxonomyCategorizations }
     *     
     */
    public void setTaxonomyCategorizations(TaxonomyCategorizations value) {
        this.taxonomyCategorizations = value;
    }

    /**
     * Gets the value of the itemSuspended property.
     * 
     * @return
     *     possible object is
     *     {@link ItemSuspended }
     *     
     */
    public ItemSuspended getItemSuspended() {
        return itemSuspended;
    }

    /**
     * Sets the value of the itemSuspended property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemSuspended }
     *     
     */
    public void setItemSuspended(ItemSuspended value) {
        this.itemSuspended = value;
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
     * Gets the value of the languages property.
     * 
     * @return
     *     possible object is
     *     {@link Languages }
     *     
     */
    public Languages getLanguages() {
        return languages;
    }

    /**
     * Sets the value of the languages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Languages }
     *     
     */
    public void setLanguages(Languages value) {
        this.languages = value;
    }

    /**
     * Gets the value of the distributionRegions property.
     * 
     * @return
     *     possible object is
     *     {@link DistributionRegions }
     *     
     */
    public DistributionRegions getDistributionRegions() {
        return distributionRegions;
    }

    /**
     * Sets the value of the distributionRegions property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistributionRegions }
     *     
     */
    public void setDistributionRegions(DistributionRegions value) {
        this.distributionRegions = value;
    }

    /**
     * Gets the value of the pricing property.
     * 
     * @return
     *     possible object is
     *     {@link Pricing }
     *     
     */
    public Pricing getPricing() {
        return pricing;
    }

    /**
     * Sets the value of the pricing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pricing }
     *     
     */
    public void setPricing(Pricing value) {
        this.pricing = value;
    }

    /**
     * Gets the value of the softwarePassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoftwarePassword() {
        return softwarePassword;
    }

    /**
     * Sets the value of the softwarePassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoftwarePassword(String value) {
        this.softwarePassword = value;
    }

    /**
     * Gets the value of the installationFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstallationFilename() {
        return installationFilename;
    }

    /**
     * Sets the value of the installationFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstallationFilename(String value) {
        this.installationFilename = value;
    }

    /**
     * Gets the value of the physicalFulfillment property.
     * 
     * @return
     *     possible object is
     *     {@link PhysicalFulfillment }
     *     
     */
    public PhysicalFulfillment getPhysicalFulfillment() {
        return physicalFulfillment;
    }

    /**
     * Sets the value of the physicalFulfillment property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhysicalFulfillment }
     *     
     */
    public void setPhysicalFulfillment(PhysicalFulfillment value) {
        this.physicalFulfillment = value;
    }

    /**
     * Gets the value of the bundledItems property.
     * 
     * @return
     *     possible object is
     *     {@link BundledItems }
     *     
     */
    public BundledItems getBundledItems() {
        return bundledItems;
    }

    /**
     * Sets the value of the bundledItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link BundledItems }
     *     
     */
    public void setBundledItems(BundledItems value) {
        this.bundledItems = value;
    }

    /**
     * Gets the value of the cdCheckvalues property.
     * 
     * @return
     *     possible object is
     *     {@link CdCheckvalues }
     *     
     */
    public CdCheckvalues getCdCheckvalues() {
        return cdCheckvalues;
    }

    /**
     * Sets the value of the cdCheckvalues property.
     * 
     * @param value
     *     allowed object is
     *     {@link CdCheckvalues }
     *     
     */
    public void setCdCheckvalues(CdCheckvalues value) {
        this.cdCheckvalues = value;
    }

    /**
     * Gets the value of the orderLinks property.
     * 
     * @return
     *     possible object is
     *     {@link OrderLinks }
     *     
     */
    public OrderLinks getOrderLinks() {
        return orderLinks;
    }

    /**
     * Sets the value of the orderLinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderLinks }
     *     
     */
    public void setOrderLinks(OrderLinks value) {
        this.orderLinks = value;
    }

    /**
     * Gets the value of the itemUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getItemUrl() {
        return itemUrl;
    }

    /**
     * Sets the value of the itemUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setItemUrl(String value) {
        this.itemUrl = value;
    }

    /**
     * Gets the value of the partnerFeedback property.
     * 
     * @return
     *     possible object is
     *     {@link PartnerFeedback }
     *     
     */
    public PartnerFeedback getPartnerFeedback() {
        return partnerFeedback;
    }

    /**
     * Sets the value of the partnerFeedback property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartnerFeedback }
     *     
     */
    public void setPartnerFeedback(PartnerFeedback value) {
        this.partnerFeedback = value;
    }

    /**
     * Gets the value of the iaddProperties property.
     * 
     * @return
     *     possible object is
     *     {@link IaddProperties }
     *     
     */
    public IaddProperties getIaddProperties() {
        return iaddProperties;
    }

    /**
     * Sets the value of the iaddProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link IaddProperties }
     *     
     */
    public void setIaddProperties(IaddProperties value) {
        this.iaddProperties = value;
    }

    /**
     * Gets the value of the softwareFiles property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwareFiles }
     *     
     */
    public SoftwareFiles getSoftwareFiles() {
        return softwareFiles;
    }

    /**
     * Sets the value of the softwareFiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwareFiles }
     *     
     */
    public void setSoftwareFiles(SoftwareFiles value) {
        this.softwareFiles = value;
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
     * Gets the value of the chronicleID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChronicleID() {
        return chronicleID;
    }

    /**
     * Sets the value of the chronicleID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChronicleID(String value) {
        this.chronicleID = value;
    }

    /**
     * Gets the value of the fromToad property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getFromToad() {
        return fromToad;
    }

    /**
     * Sets the value of the fromToad property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setFromToad(StringValueYesNo value) {
        this.fromToad = value;
    }

    /**
     * Gets the value of the itemID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Sets the value of the itemID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setItemID(String value) {
        this.itemID = value;
    }

    /**
     * Gets the value of the languagesAtFileLevel property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getLanguagesAtFileLevel() {
        return languagesAtFileLevel;
    }

    /**
     * Sets the value of the languagesAtFileLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setLanguagesAtFileLevel(StringValueYesNo value) {
        this.languagesAtFileLevel = value;
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
     * Gets the value of the productLevelSensitive property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getProductLevelSensitive() {
        return productLevelSensitive;
    }

    /**
     * Sets the value of the productLevelSensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setProductLevelSensitive(StringValueYesNo value) {
        this.productLevelSensitive = value;
    }

    /**
     * Gets the value of the soarDescriptionOverridden property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getSoarDescriptionOverridden() {
        return soarDescriptionOverridden;
    }

    /**
     * Sets the value of the soarDescriptionOverridden property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setSoarDescriptionOverridden(StringValueYesNo value) {
        this.soarDescriptionOverridden = value;
    }

    /**
     * Gets the value of the soarDistinctEnvironments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoarDistinctEnvironments() {
        return soarDistinctEnvironments;
    }

    /**
     * Sets the value of the soarDistinctEnvironments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoarDistinctEnvironments(String value) {
        this.soarDistinctEnvironments = value;
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
     * Gets the value of the toadOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToadOID() {
        return toadOID;
    }

    /**
     * Sets the value of the toadOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToadOID(String value) {
        this.toadOID = value;
    }

}
