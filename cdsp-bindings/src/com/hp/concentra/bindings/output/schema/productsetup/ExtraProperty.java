//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:11 AM IST 
//


package com.hp.concentra.bindings.output.schema.productsetup;

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
 *         &lt;element ref="{}extra_property_name"/>
 *         &lt;element ref="{}extra_property_values"/>
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
    "extraPropertyName",
    "extraPropertyValues"
})
@XmlRootElement(name = "extra_property")
public class ExtraProperty {

    @XmlElement(name = "extra_property_name", required = true)
    protected String extraPropertyName;
    @XmlElement(name = "extra_property_values", required = true)
    protected ExtraPropertyValues extraPropertyValues;

    /**
     * Gets the value of the extraPropertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtraPropertyName() {
        return extraPropertyName;
    }

    /**
     * Sets the value of the extraPropertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtraPropertyName(String value) {
        this.extraPropertyName = value;
    }

    /**
     * Gets the value of the extraPropertyValues property.
     * 
     * @return
     *     possible object is
     *     {@link ExtraPropertyValues }
     *     
     */
    public ExtraPropertyValues getExtraPropertyValues() {
        return extraPropertyValues;
    }

    /**
     * Sets the value of the extraPropertyValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtraPropertyValues }
     *     
     */
    public void setExtraPropertyValues(ExtraPropertyValues value) {
        this.extraPropertyValues = value;
    }

}
