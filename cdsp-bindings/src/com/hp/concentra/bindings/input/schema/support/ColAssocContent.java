//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:04 AM IST 
//


package com.hp.concentra.bindings.input.schema.support;

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
 *         &lt;element ref="{}col_assoc_content_name"/>
 *         &lt;element ref="{}col_assoc_content_values"/>
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
    "colAssocContentName",
    "colAssocContentValues"
})
@XmlRootElement(name = "col_assoc_content")
public class ColAssocContent {

    @XmlElement(name = "col_assoc_content_name", required = true)
    protected String colAssocContentName;
    @XmlElement(name = "col_assoc_content_values", required = true)
    protected ColAssocContentValues colAssocContentValues;

    /**
     * Gets the value of the colAssocContentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColAssocContentName() {
        return colAssocContentName;
    }

    /**
     * Sets the value of the colAssocContentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColAssocContentName(String value) {
        this.colAssocContentName = value;
    }

    /**
     * Gets the value of the colAssocContentValues property.
     * 
     * @return
     *     possible object is
     *     {@link ColAssocContentValues }
     *     
     */
    public ColAssocContentValues getColAssocContentValues() {
        return colAssocContentValues;
    }

    /**
     * Sets the value of the colAssocContentValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColAssocContentValues }
     *     
     */
    public void setColAssocContentValues(ColAssocContentValues value) {
        this.colAssocContentValues = value;
    }

}