//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:02 AM IST 
//


package com.hp.concentra.bindings.input.schema.contentfeedback;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.hp.concentra.bindings.input.schema.contentfeedback package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FeedbackContentName_QNAME = new QName("", "feedback_content_name");
    private final static QName _VersionLabel_QNAME = new QName("", "version_label");
    private final static QName _ActionRequested_QNAME = new QName("", "action_requested");
    private final static QName _MiscellaneousComment_QNAME = new QName("", "miscellaneous_comment");
    private final static QName _RObjectId_QNAME = new QName("", "r_object_id");
    private final static QName _Product_QNAME = new QName("", "product");
    private final static QName _CompanyInfo_QNAME = new QName("", "company_info");
    private final static QName _ActionTaken_QNAME = new QName("", "action_taken");
    private final static QName _FeedbackPriority_QNAME = new QName("", "feedback_priority");
    private final static QName _OriginalDocid_QNAME = new QName("", "original_docid");
    private final static QName _FileBytes_QNAME = new QName("", "file_bytes");
    private final static QName _Priority_QNAME = new QName("", "priority");
    private final static QName _InformationSource_QNAME = new QName("", "information_source");
    private final static QName _OriginalSystem_QNAME = new QName("", "original_system");
    private final static QName _FeedbackProductLine_QNAME = new QName("", "feedback_product_line");
    private final static QName _LanguageCode_QNAME = new QName("", "language_code");
    private final static QName _DocumentClass_QNAME = new QName("", "document_class");
    private final static QName _ContentType_QNAME = new QName("", "content_type");
    private final static QName _LanguageLabel_QNAME = new QName("", "language_label");
    private final static QName _ConfidentialComment_QNAME = new QName("", "confidential_comment");
    private final static QName _ObjectName_QNAME = new QName("", "object_name");
    private final static QName _Region_QNAME = new QName("", "region");
    private final static QName _DocumentType_QNAME = new QName("", "document_type");
    private final static QName _PropertyUpdateDate_QNAME = new QName("", "property_update_date");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.hp.concentra.bindings.input.schema.contentfeedback
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Products }
     * 
     */
    public Products createProducts() {
        return new Products();
    }

    /**
     * Create an instance of {@link FeedbackProductLines }
     * 
     */
    public FeedbackProductLines createFeedbackProductLines() {
        return new FeedbackProductLines();
    }

    /**
     * Create an instance of {@link Regions }
     * 
     */
    public Regions createRegions() {
        return new Regions();
    }

    /**
     * Create an instance of {@link FullTitle }
     * 
     */
    public FullTitle createFullTitle() {
        return new FullTitle();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "feedback_content_name")
    public JAXBElement<String> createFeedbackContentName(String value) {
        return new JAXBElement<String>(_FeedbackContentName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "version_label")
    public JAXBElement<BigDecimal> createVersionLabel(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_VersionLabel_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "action_requested")
    public JAXBElement<String> createActionRequested(String value) {
        return new JAXBElement<String>(_ActionRequested_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "miscellaneous_comment")
    public JAXBElement<String> createMiscellaneousComment(String value) {
        return new JAXBElement<String>(_MiscellaneousComment_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "r_object_id")
    public JAXBElement<String> createRObjectId(String value) {
        return new JAXBElement<String>(_RObjectId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "product")
    public JAXBElement<String> createProduct(String value) {
        return new JAXBElement<String>(_Product_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "company_info")
    public JAXBElement<String> createCompanyInfo(String value) {
        return new JAXBElement<String>(_CompanyInfo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "action_taken")
    public JAXBElement<String> createActionTaken(String value) {
        return new JAXBElement<String>(_ActionTaken_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "feedback_priority")
    public JAXBElement<String> createFeedbackPriority(String value) {
        return new JAXBElement<String>(_FeedbackPriority_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "original_docid")
    public JAXBElement<String> createOriginalDocid(String value) {
        return new JAXBElement<String>(_OriginalDocid_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "file_bytes")
    public JAXBElement<Long> createFileBytes(Long value) {
        return new JAXBElement<Long>(_FileBytes_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "priority")
    public JAXBElement<Byte> createPriority(Byte value) {
        return new JAXBElement<Byte>(_Priority_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "information_source")
    public JAXBElement<String> createInformationSource(String value) {
        return new JAXBElement<String>(_InformationSource_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "original_system")
    public JAXBElement<String> createOriginalSystem(String value) {
        return new JAXBElement<String>(_OriginalSystem_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "feedback_product_line")
    public JAXBElement<String> createFeedbackProductLine(String value) {
        return new JAXBElement<String>(_FeedbackProductLine_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "language_code")
    public JAXBElement<String> createLanguageCode(String value) {
        return new JAXBElement<String>(_LanguageCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "document_class")
    public JAXBElement<String> createDocumentClass(String value) {
        return new JAXBElement<String>(_DocumentClass_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "content_type")
    public JAXBElement<String> createContentType(String value) {
        return new JAXBElement<String>(_ContentType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "language_label")
    public JAXBElement<String> createLanguageLabel(String value) {
        return new JAXBElement<String>(_LanguageLabel_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "confidential_comment")
    public JAXBElement<String> createConfidentialComment(String value) {
        return new JAXBElement<String>(_ConfidentialComment_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "object_name")
    public JAXBElement<String> createObjectName(String value) {
        return new JAXBElement<String>(_ObjectName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "region")
    public JAXBElement<String> createRegion(String value) {
        return new JAXBElement<String>(_Region_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "document_type")
    public JAXBElement<String> createDocumentType(String value) {
        return new JAXBElement<String>(_DocumentType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "property_update_date")
    public JAXBElement<XMLGregorianCalendar> createPropertyUpdateDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_PropertyUpdateDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

}
