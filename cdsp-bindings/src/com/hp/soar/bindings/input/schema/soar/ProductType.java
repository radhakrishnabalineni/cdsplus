//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:02:59 AM IST 
//


package com.hp.soar.bindings.input.schema.soar;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for product-type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="product-type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PMSC"/>
 *     &lt;enumeration value="PMSF"/>
 *     &lt;enumeration value="PMS"/>
 *     &lt;enumeration value="PMM"/>
 *     &lt;enumeration value="PMN"/>
 *     &lt;enumeration value="PMPI"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum ProductType {

    PMM,
    PMN,
    PMPI,
    PMS,
    PMSC,
    PMSF;

    public String value() {
        return name();
    }

    public static ProductType fromValue(String v) {
        return valueOf(v);
    }

}
