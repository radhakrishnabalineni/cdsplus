//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:09 AM IST 
//


package com.hp.concentra.bindings.output.schema.support;

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
 *         &lt;element ref="{}categorization_key"/>
 *         &lt;element ref="{}categorization_value"/>
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
    "categorizationKey",
    "categorizationValue"
})
@XmlRootElement(name = "taxonomy_categorization")
public class TaxonomyCategorization {

    @XmlElement(name = "categorization_key", required = true)
    protected String categorizationKey;
    @XmlElement(name = "categorization_value", required = true)
    protected String categorizationValue;

    /**
     * Gets the value of the categorizationKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategorizationKey() {
        return categorizationKey;
    }

    /**
     * Sets the value of the categorizationKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategorizationKey(String value) {
        this.categorizationKey = value;
    }

    /**
     * Gets the value of the categorizationValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategorizationValue() {
        return categorizationValue;
    }

    /**
     * Sets the value of the categorizationValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategorizationValue(String value) {
        this.categorizationValue = value;
    }

}
