//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:02:59 AM IST 
//


package com.hp.soar.bindings.input.schema.soar;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}server"/>
 *         &lt;element ref="{}directory" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="file-delivery-confirmation" use="required" type="{}string-value-Yes-No" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "server",
    "directory"
})
@XmlRootElement(name = "server-directory")
public class ServerDirectory {

    @XmlElement(required = true)
    protected Server server;
    @XmlElement(required = true)
    protected List<String> directory;
    @XmlAttribute(name = "file-delivery-confirmation", required = true)
    protected StringValueYesNo fileDeliveryConfirmation;

    /**
     * Gets the value of the server property.
     * 
     * @return
     *     possible object is
     *     {@link Server }
     *     
     */
    public Server getServer() {
        return server;
    }

    /**
     * Sets the value of the server property.
     * 
     * @param value
     *     allowed object is
     *     {@link Server }
     *     
     */
    public void setServer(Server value) {
        this.server = value;
    }

    /**
     * Gets the value of the directory property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the directory property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDirectory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDirectory() {
        if (directory == null) {
            directory = new ArrayList<String>();
        }
        return this.directory;
    }

    /**
     * Gets the value of the fileDeliveryConfirmation property.
     * 
     * @return
     *     possible object is
     *     {@link StringValueYesNo }
     *     
     */
    public StringValueYesNo getFileDeliveryConfirmation() {
        return fileDeliveryConfirmation;
    }

    /**
     * Sets the value of the fileDeliveryConfirmation property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringValueYesNo }
     *     
     */
    public void setFileDeliveryConfirmation(StringValueYesNo value) {
        this.fileDeliveryConfirmation = value;
    }

}
