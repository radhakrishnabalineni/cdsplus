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
 *         &lt;element ref="{}biz_defined_property_name"/>
 *         &lt;element ref="{}biz_defined_property_values"/>
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
    "bizDefinedPropertyName",
    "bizDefinedPropertyValues"
})
@XmlRootElement(name = "biz_defined_property")
public class BizDefinedProperty {

    @XmlElement(name = "biz_defined_property_name", required = true)
    protected String bizDefinedPropertyName;
    @XmlElement(name = "biz_defined_property_values", required = true)
    protected BizDefinedPropertyValues bizDefinedPropertyValues;

    /**
     * Gets the value of the bizDefinedPropertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizDefinedPropertyName() {
        return bizDefinedPropertyName;
    }

    /**
     * Sets the value of the bizDefinedPropertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizDefinedPropertyName(String value) {
        this.bizDefinedPropertyName = value;
    }

    /**
     * Gets the value of the bizDefinedPropertyValues property.
     * 
     * @return
     *     possible object is
     *     {@link BizDefinedPropertyValues }
     *     
     */
    public BizDefinedPropertyValues getBizDefinedPropertyValues() {
        return bizDefinedPropertyValues;
    }

    /**
     * Sets the value of the bizDefinedPropertyValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link BizDefinedPropertyValues }
     *     
     */
    public void setBizDefinedPropertyValues(BizDefinedPropertyValues value) {
        this.bizDefinedPropertyValues = value;
    }

}
