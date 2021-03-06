//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.13 at 11:02:59 AM IST 
//


package com.hp.soar.bindings.input.schema.soar;

import java.math.BigInteger;
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
 *         &lt;element ref="{}file-type"/>
 *         &lt;element ref="{}software-file" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="total-number-of-files" use="required" type="{}integer-min-value-1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fileType",
    "softwareFile"
})
@XmlRootElement(name = "software-set")
public class SoftwareSet {

    @XmlElement(name = "file-type", required = true)
    protected FileType fileType;
    @XmlElement(name = "software-file", required = true)
    protected List<SoftwareFile> softwareFile;
    @XmlAttribute(name = "total-number-of-files", required = true)
    protected BigInteger totalNumberOfFiles;

    /**
     * Gets the value of the fileType property.
     * 
     * @return
     *     possible object is
     *     {@link FileType }
     *     
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * Sets the value of the fileType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileType }
     *     
     */
    public void setFileType(FileType value) {
        this.fileType = value;
    }

    /**
     * Gets the value of the softwareFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the softwareFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoftwareFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SoftwareFile }
     * 
     * 
     */
    public List<SoftwareFile> getSoftwareFile() {
        if (softwareFile == null) {
            softwareFile = new ArrayList<SoftwareFile>();
        }
        return this.softwareFile;
    }

    /**
     * Gets the value of the totalNumberOfFiles property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalNumberOfFiles() {
        return totalNumberOfFiles;
    }

    /**
     * Sets the value of the totalNumberOfFiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalNumberOfFiles(BigInteger value) {
        this.totalNumberOfFiles = value;
    }

}
