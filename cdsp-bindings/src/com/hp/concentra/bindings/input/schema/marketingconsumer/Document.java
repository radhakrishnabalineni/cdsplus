//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:02 AM IST 
//


package com.hp.concentra.bindings.input.schema.marketingconsumer;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{}r_object_id"/>
 *         &lt;element ref="{}priority"/>
 *         &lt;element ref="{}products"/>
 *         &lt;element ref="{}document_type"/>
 *         &lt;element ref="{}version_label"/>
 *         &lt;element ref="{}cmg_feedback_address"/>
 *         &lt;element ref="{}collection_valid_flag"/>
 *         &lt;element ref="{}information_source"/>
 *         &lt;element ref="{}content_type"/>
 *         &lt;element ref="{}content_update_date"/>
 *         &lt;element ref="{}master_object_name"/>
 *         &lt;element ref="{}property_update_date"/>
 *         &lt;element ref="{}document_type_details"/>
 *         &lt;element ref="{}marketing_keywords"/>
 *         &lt;element ref="{}language_label"/>
 *         &lt;element ref="{}audiences"/>
 *         &lt;element ref="{}extra_properties"/>
 *         &lt;element ref="{}accounts"/>
 *         &lt;element ref="{}authors"/>
 *         &lt;element ref="{}marketing_teams"/>
 *         &lt;element ref="{}regions"/>
 *         &lt;element ref="{}search_keywords"/>
 *         &lt;element ref="{}selling_windows"/>
 *         &lt;element ref="{}renditions"/>
 *         &lt;element ref="{}full_title"/>
 *         &lt;element ref="{}has_valid_products"/>
 *         &lt;element ref="{}col_id"/>
 *         &lt;element ref="{}confidential_comment"/>
 *         &lt;element ref="{}content_history"/>
 *         &lt;element ref="{}description"/>
 *         &lt;element ref="{}disclosure_level"/>
 *         &lt;element ref="{}content_version"/>
 *         &lt;element ref="{}language_code"/>
 *         &lt;element ref="{}miscellaneous_comment"/>
 *         &lt;element ref="{}creation_date"/>
 *         &lt;element ref="{}object_name"/>
 *         &lt;element ref="{}original_docid"/>
 *         &lt;element ref="{}original_filename"/>
 *         &lt;element ref="{}original_system"/>
 *         &lt;element ref="{}document_class"/>
 *         &lt;element ref="{}review_date"/>
 *         &lt;element ref="{}show_as_new_duration"/>
 *         &lt;element ref="{}valid_flag"/>
 *       &lt;/sequence>
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
    "rObjectId",
    "priority",
    "products",
    "documentType",
    "versionLabel",
    "cmgFeedbackAddress",
    "collectionValidFlag",
    "informationSource",
    "contentType",
    "contentUpdateDate",
    "masterObjectName",
    "propertyUpdateDate",
    "documentTypeDetails",
    "marketingKeywords",
    "languageLabel",
    "audiences",
    "extraProperties",
    "accounts",
    "authors",
    "marketingTeams",
    "regions",
    "searchKeywords",
    "sellingWindows",
    "renditions",
    "fullTitle",
    "hasValidProducts",
    "colId",
    "confidentialComment",
    "contentHistory",
    "description",
    "disclosureLevel",
    "contentVersion",
    "languageCode",
    "miscellaneousComment",
    "creationDate",
    "objectName",
    "originalDocid",
    "originalFilename",
    "originalSystem",
    "documentClass",
    "reviewDate",
    "showAsNewDuration",
    "validFlag"
})
@XmlRootElement(name = "document")
public class Document {

    @XmlElement(name = "company_info", required = true)
    protected String companyInfo;
    @XmlElement(name = "r_object_id", required = true)
    protected String rObjectId;
    protected byte priority;
    @XmlElement(required = true)
    protected Products products;
    @XmlElement(name = "document_type", required = true)
    protected String documentType;
    @XmlElement(name = "version_label", required = true)
    protected BigDecimal versionLabel;
    @XmlElement(name = "cmg_feedback_address", required = true)
    protected String cmgFeedbackAddress;
    @XmlElement(name = "collection_valid_flag")
    protected boolean collectionValidFlag;
    @XmlElement(name = "information_source", required = true)
    protected String informationSource;
    @XmlElement(name = "content_type", required = true)
    protected String contentType;
    @XmlElement(name = "content_update_date", required = true)
    protected XMLGregorianCalendar contentUpdateDate;
    @XmlElement(name = "master_object_name", required = true)
    protected String masterObjectName;
    @XmlElement(name = "property_update_date", required = true)
    protected XMLGregorianCalendar propertyUpdateDate;
    @XmlElement(name = "document_type_details", required = true)
    protected DocumentTypeDetails documentTypeDetails;
    @XmlElement(name = "marketing_keywords", required = true)
    protected MarketingKeywords marketingKeywords;
    @XmlElement(name = "language_label", required = true)
    protected String languageLabel;
    @XmlElement(required = true)
    protected Audiences audiences;
    @XmlElement(name = "extra_properties", required = true)
    protected ExtraProperties extraProperties;
    @XmlElement(required = true)
    protected Accounts accounts;
    @XmlElement(required = true)
    protected Authors authors;
    @XmlElement(name = "marketing_teams", required = true)
    protected MarketingTeams marketingTeams;
    @XmlElement(required = true)
    protected Regions regions;
    @XmlElement(name = "search_keywords", required = true)
    protected SearchKeywords searchKeywords;
    @XmlElement(name = "selling_windows", required = true)
    protected SellingWindows sellingWindows;
    @XmlElement(required = true)
    protected Renditions renditions;
    @XmlElement(name = "full_title", required = true)
    protected FullTitle fullTitle;
    @XmlElement(name = "has_valid_products")
    protected boolean hasValidProducts;
    @XmlElement(name = "col_id", required = true)
    protected String colId;
    @XmlElement(name = "confidential_comment", required = true)
    protected String confidentialComment;
    @XmlElement(name = "content_history", required = true)
    protected String contentHistory;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(name = "disclosure_level", required = true)
    protected String disclosureLevel;
    @XmlElement(name = "content_version", required = true)
    protected BigDecimal contentVersion;
    @XmlElement(name = "language_code", required = true)
    protected String languageCode;
    @XmlElement(name = "miscellaneous_comment", required = true)
    protected String miscellaneousComment;
    @XmlElement(name = "creation_date", required = true)
    protected XMLGregorianCalendar creationDate;
    @XmlElement(name = "object_name", required = true)
    protected String objectName;
    @XmlElement(name = "original_docid", required = true)
    protected String originalDocid;
    @XmlElement(name = "original_filename", required = true)
    protected String originalFilename;
    @XmlElement(name = "original_system", required = true)
    protected String originalSystem;
    @XmlElement(name = "document_class", required = true)
    protected String documentClass;
    @XmlElement(name = "review_date", required = true)
    protected XMLGregorianCalendar reviewDate;
    @XmlElement(name = "show_as_new_duration", required = true)
    protected String showAsNewDuration;
    @XmlElement(name = "valid_flag")
    protected boolean validFlag;

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
     * Gets the value of the rObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRObjectId() {
        return rObjectId;
    }

    /**
     * Sets the value of the rObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRObjectId(String value) {
        this.rObjectId = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(byte value) {
        this.priority = value;
    }

    /**
     * Gets the value of the products property.
     * 
     * @return
     *     possible object is
     *     {@link Products }
     *     
     */
    public Products getProducts() {
        return products;
    }

    /**
     * Sets the value of the products property.
     * 
     * @param value
     *     allowed object is
     *     {@link Products }
     *     
     */
    public void setProducts(Products value) {
        this.products = value;
    }

    /**
     * Gets the value of the documentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Sets the value of the documentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentType(String value) {
        this.documentType = value;
    }

    /**
     * Gets the value of the versionLabel property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getVersionLabel() {
        return versionLabel;
    }

    /**
     * Sets the value of the versionLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setVersionLabel(BigDecimal value) {
        this.versionLabel = value;
    }

    /**
     * Gets the value of the cmgFeedbackAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmgFeedbackAddress() {
        return cmgFeedbackAddress;
    }

    /**
     * Sets the value of the cmgFeedbackAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmgFeedbackAddress(String value) {
        this.cmgFeedbackAddress = value;
    }

    /**
     * Gets the value of the collectionValidFlag property.
     * 
     */
    public boolean isCollectionValidFlag() {
        return collectionValidFlag;
    }

    /**
     * Sets the value of the collectionValidFlag property.
     * 
     */
    public void setCollectionValidFlag(boolean value) {
        this.collectionValidFlag = value;
    }

    /**
     * Gets the value of the informationSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInformationSource() {
        return informationSource;
    }

    /**
     * Sets the value of the informationSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInformationSource(String value) {
        this.informationSource = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the contentUpdateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getContentUpdateDate() {
        return contentUpdateDate;
    }

    /**
     * Sets the value of the contentUpdateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setContentUpdateDate(XMLGregorianCalendar value) {
        this.contentUpdateDate = value;
    }

    /**
     * Gets the value of the masterObjectName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMasterObjectName() {
        return masterObjectName;
    }

    /**
     * Sets the value of the masterObjectName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMasterObjectName(String value) {
        this.masterObjectName = value;
    }

    /**
     * Gets the value of the propertyUpdateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPropertyUpdateDate() {
        return propertyUpdateDate;
    }

    /**
     * Sets the value of the propertyUpdateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPropertyUpdateDate(XMLGregorianCalendar value) {
        this.propertyUpdateDate = value;
    }

    /**
     * Gets the value of the documentTypeDetails property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentTypeDetails }
     *     
     */
    public DocumentTypeDetails getDocumentTypeDetails() {
        return documentTypeDetails;
    }

    /**
     * Sets the value of the documentTypeDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentTypeDetails }
     *     
     */
    public void setDocumentTypeDetails(DocumentTypeDetails value) {
        this.documentTypeDetails = value;
    }

    /**
     * Gets the value of the marketingKeywords property.
     * 
     * @return
     *     possible object is
     *     {@link MarketingKeywords }
     *     
     */
    public MarketingKeywords getMarketingKeywords() {
        return marketingKeywords;
    }

    /**
     * Sets the value of the marketingKeywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarketingKeywords }
     *     
     */
    public void setMarketingKeywords(MarketingKeywords value) {
        this.marketingKeywords = value;
    }

    /**
     * Gets the value of the languageLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguageLabel() {
        return languageLabel;
    }

    /**
     * Sets the value of the languageLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguageLabel(String value) {
        this.languageLabel = value;
    }

    /**
     * Gets the value of the audiences property.
     * 
     * @return
     *     possible object is
     *     {@link Audiences }
     *     
     */
    public Audiences getAudiences() {
        return audiences;
    }

    /**
     * Sets the value of the audiences property.
     * 
     * @param value
     *     allowed object is
     *     {@link Audiences }
     *     
     */
    public void setAudiences(Audiences value) {
        this.audiences = value;
    }

    /**
     * Gets the value of the extraProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ExtraProperties }
     *     
     */
    public ExtraProperties getExtraProperties() {
        return extraProperties;
    }

    /**
     * Sets the value of the extraProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtraProperties }
     *     
     */
    public void setExtraProperties(ExtraProperties value) {
        this.extraProperties = value;
    }

    /**
     * Gets the value of the accounts property.
     * 
     * @return
     *     possible object is
     *     {@link Accounts }
     *     
     */
    public Accounts getAccounts() {
        return accounts;
    }

    /**
     * Sets the value of the accounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Accounts }
     *     
     */
    public void setAccounts(Accounts value) {
        this.accounts = value;
    }

    /**
     * Gets the value of the authors property.
     * 
     * @return
     *     possible object is
     *     {@link Authors }
     *     
     */
    public Authors getAuthors() {
        return authors;
    }

    /**
     * Sets the value of the authors property.
     * 
     * @param value
     *     allowed object is
     *     {@link Authors }
     *     
     */
    public void setAuthors(Authors value) {
        this.authors = value;
    }

    /**
     * Gets the value of the marketingTeams property.
     * 
     * @return
     *     possible object is
     *     {@link MarketingTeams }
     *     
     */
    public MarketingTeams getMarketingTeams() {
        return marketingTeams;
    }

    /**
     * Sets the value of the marketingTeams property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarketingTeams }
     *     
     */
    public void setMarketingTeams(MarketingTeams value) {
        this.marketingTeams = value;
    }

    /**
     * Gets the value of the regions property.
     * 
     * @return
     *     possible object is
     *     {@link Regions }
     *     
     */
    public Regions getRegions() {
        return regions;
    }

    /**
     * Sets the value of the regions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Regions }
     *     
     */
    public void setRegions(Regions value) {
        this.regions = value;
    }

    /**
     * Gets the value of the searchKeywords property.
     * 
     * @return
     *     possible object is
     *     {@link SearchKeywords }
     *     
     */
    public SearchKeywords getSearchKeywords() {
        return searchKeywords;
    }

    /**
     * Sets the value of the searchKeywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchKeywords }
     *     
     */
    public void setSearchKeywords(SearchKeywords value) {
        this.searchKeywords = value;
    }

    /**
     * Gets the value of the sellingWindows property.
     * 
     * @return
     *     possible object is
     *     {@link SellingWindows }
     *     
     */
    public SellingWindows getSellingWindows() {
        return sellingWindows;
    }

    /**
     * Sets the value of the sellingWindows property.
     * 
     * @param value
     *     allowed object is
     *     {@link SellingWindows }
     *     
     */
    public void setSellingWindows(SellingWindows value) {
        this.sellingWindows = value;
    }

    /**
     * Gets the value of the renditions property.
     * 
     * @return
     *     possible object is
     *     {@link Renditions }
     *     
     */
    public Renditions getRenditions() {
        return renditions;
    }

    /**
     * Sets the value of the renditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Renditions }
     *     
     */
    public void setRenditions(Renditions value) {
        this.renditions = value;
    }

    /**
     * Gets the value of the fullTitle property.
     * 
     * @return
     *     possible object is
     *     {@link FullTitle }
     *     
     */
    public FullTitle getFullTitle() {
        return fullTitle;
    }

    /**
     * Sets the value of the fullTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullTitle }
     *     
     */
    public void setFullTitle(FullTitle value) {
        this.fullTitle = value;
    }

    /**
     * Gets the value of the hasValidProducts property.
     * 
     */
    public boolean isHasValidProducts() {
        return hasValidProducts;
    }

    /**
     * Sets the value of the hasValidProducts property.
     * 
     */
    public void setHasValidProducts(boolean value) {
        this.hasValidProducts = value;
    }

    /**
     * Gets the value of the colId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColId() {
        return colId;
    }

    /**
     * Sets the value of the colId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColId(String value) {
        this.colId = value;
    }

    /**
     * Gets the value of the confidentialComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfidentialComment() {
        return confidentialComment;
    }

    /**
     * Sets the value of the confidentialComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfidentialComment(String value) {
        this.confidentialComment = value;
    }

    /**
     * Gets the value of the contentHistory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentHistory() {
        return contentHistory;
    }

    /**
     * Sets the value of the contentHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentHistory(String value) {
        this.contentHistory = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the disclosureLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisclosureLevel() {
        return disclosureLevel;
    }

    /**
     * Sets the value of the disclosureLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisclosureLevel(String value) {
        this.disclosureLevel = value;
    }

    /**
     * Gets the value of the contentVersion property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getContentVersion() {
        return contentVersion;
    }

    /**
     * Sets the value of the contentVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setContentVersion(BigDecimal value) {
        this.contentVersion = value;
    }

    /**
     * Gets the value of the languageCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the value of the languageCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguageCode(String value) {
        this.languageCode = value;
    }

    /**
     * Gets the value of the miscellaneousComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiscellaneousComment() {
        return miscellaneousComment;
    }

    /**
     * Sets the value of the miscellaneousComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiscellaneousComment(String value) {
        this.miscellaneousComment = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreationDate(XMLGregorianCalendar value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the objectName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Sets the value of the objectName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectName(String value) {
        this.objectName = value;
    }

    /**
     * Gets the value of the originalDocid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalDocid() {
        return originalDocid;
    }

    /**
     * Sets the value of the originalDocid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalDocid(String value) {
        this.originalDocid = value;
    }

    /**
     * Gets the value of the originalFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the value of the originalFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalFilename(String value) {
        this.originalFilename = value;
    }

    /**
     * Gets the value of the originalSystem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalSystem() {
        return originalSystem;
    }

    /**
     * Sets the value of the originalSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalSystem(String value) {
        this.originalSystem = value;
    }

    /**
     * Gets the value of the documentClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentClass() {
        return documentClass;
    }

    /**
     * Sets the value of the documentClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentClass(String value) {
        this.documentClass = value;
    }

    /**
     * Gets the value of the reviewDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReviewDate() {
        return reviewDate;
    }

    /**
     * Sets the value of the reviewDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReviewDate(XMLGregorianCalendar value) {
        this.reviewDate = value;
    }

    /**
     * Gets the value of the showAsNewDuration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowAsNewDuration() {
        return showAsNewDuration;
    }

    /**
     * Sets the value of the showAsNewDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowAsNewDuration(String value) {
        this.showAsNewDuration = value;
    }

    /**
     * Gets the value of the validFlag property.
     * 
     */
    public boolean isValidFlag() {
        return validFlag;
    }

    /**
     * Sets the value of the validFlag property.
     * 
     */
    public void setValidFlag(boolean value) {
        this.validFlag = value;
    }

}