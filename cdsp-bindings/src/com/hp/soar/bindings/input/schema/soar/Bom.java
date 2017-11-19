//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:02:59 AM IST 
//


package com.hp.soar.bindings.input.schema.soar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}bom-value"/>
 *         &lt;element ref="{}level"/>
 *         &lt;element ref="{}checksum-type" minOccurs="0"/>
 *         &lt;element ref="{}checksum-value" minOccurs="0"/>
 *         &lt;element ref="{}image-size" minOccurs="0"/>
 *         &lt;element ref="{}bom-sequence" minOccurs="0"/>
 *         &lt;element ref="{}bom-quantity" minOccurs="0"/>
 *         &lt;element ref="{}bom-description" minOccurs="0"/>
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
    "bomValue",
    "level",
    "checksumType",
    "checksumValue",
    "imageSize",
    "bomSequence",
    "bomQuantity",
    "bomDescription"
})
@XmlRootElement(name = "bom")
public class Bom {

    @XmlElement(name = "bom-value", required = true)
    protected String bomValue;
    @XmlElement(required = true)
    protected Level level;
    @XmlElement(name = "checksum-type")
    protected ChecksumType checksumType;
    @XmlElement(name = "checksum-value")
    protected String checksumValue;
    @XmlElement(name = "image-size")
    protected String imageSize;
    @XmlElement(name = "bom-sequence")
    protected String bomSequence;
    @XmlElement(name = "bom-quantity")
    protected String bomQuantity;
    @XmlElement(name = "bom-description")
    protected String bomDescription;

    /**
     * Gets the value of the bomValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBomValue() {
        return bomValue;
    }

    /**
     * Sets the value of the bomValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBomValue(String value) {
        this.bomValue = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link Level }
     *     
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link Level }
     *     
     */
    public void setLevel(Level value) {
        this.level = value;
    }

    /**
     * Gets the value of the checksumType property.
     * 
     * @return
     *     possible object is
     *     {@link ChecksumType }
     *     
     */
    public ChecksumType getChecksumType() {
        return checksumType;
    }

    /**
     * Sets the value of the checksumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChecksumType }
     *     
     */
    public void setChecksumType(ChecksumType value) {
        this.checksumType = value;
    }

    /**
     * Gets the value of the checksumValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecksumValue() {
        return checksumValue;
    }

    /**
     * Sets the value of the checksumValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecksumValue(String value) {
        this.checksumValue = value;
    }

    /**
     * Gets the value of the imageSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageSize() {
        return imageSize;
    }

    /**
     * Sets the value of the imageSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageSize(String value) {
        this.imageSize = value;
    }

    /**
     * Gets the value of the bomSequence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBomSequence() {
        return bomSequence;
    }

    /**
     * Sets the value of the bomSequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBomSequence(String value) {
        this.bomSequence = value;
    }

    /**
     * Gets the value of the bomQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBomQuantity() {
        return bomQuantity;
    }

    /**
     * Sets the value of the bomQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBomQuantity(String value) {
        this.bomQuantity = value;
    }

    /**
     * Gets the value of the bomDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBomDescription() {
        return bomDescription;
    }

    /**
     * Sets the value of the bomDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBomDescription(String value) {
        this.bomDescription = value;
    }

}
