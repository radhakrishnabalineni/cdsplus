//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:03:00 AM IST 
//


package com.hp.soar.bindings.output.schema.soar;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for item-availability.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="item-availability">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Orderable"/>
 *     &lt;enumeration value="Unorderable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum ItemAvailability {

    @XmlEnumValue("Orderable")
    ORDERABLE("Orderable"),
    @XmlEnumValue("Unorderable")
    UNORDERABLE("Unorderable");
    private final String value;

    ItemAvailability(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ItemAvailability fromValue(String v) {
        for (ItemAvailability c: ItemAvailability.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
