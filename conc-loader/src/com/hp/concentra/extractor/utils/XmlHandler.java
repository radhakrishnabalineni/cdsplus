package com.hp.concentra.extractor.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import org.xml.sax.SAXException;


/**
 * Manipulates the given xml file
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class XmlHandler {
    private static final String INTERNAL_SUBSET = "";
    private static final String ORG_APACHE_XERCES_PARSERS_SAXPARSER = "org.apache.xerces.parsers.SAXParser";
    private String filename = null;
    private Document doc = null;

    /***************************************************************************
     * Class constructor that sets its file name and reads the file content
     * 
     * @param file
     *            Name of the file to be handled
     * @throws IOException 
     * @throws SAXException 
     * @throws DocumentException 
     */
    public XmlHandler(String file) throws IOException, DocumentException, SAXException {
        filename = file;
        readFile();
    }

    /***************************************************************************
     * Gets from the xml file the given element name
     * 
     * @param elementName
     *            Name of the element to be retrieved from the file
     * @return Element found on the xml
     */
    public Element getSingleElement(String elementName) {
        return doc.getRootElement();
    }

    /***************************************************************************
     * Reads the xml file and leaves it on a Document object
     * @throws IOException 
     * @throws DocumentException 
     * @throws SAXException 
     * @throws JDOMException 
     * 
     */
    public void readFile() throws IOException, DocumentException, SAXException {
      // Do NOT rely on the default parser - it does not find doctype declaration!
      SAXReader reader = new SAXReader(ORG_APACHE_XERCES_PARSERS_SAXPARSER); 
      doc = reader.read(new File(filename));
    }

    /***************************************************************************
     * Writes the Document object into a file
     * @throws IOException 
     * @throws XmlHandlerException
     *             If an error occures while writing the xml file, this
     *             exception is thrown.
     */
    public void writeFile() throws IOException {
      // TODO: find out what this is about:
//        DocumentType docType = doc.getDocType();
//        if (docType != null) {
//            docType.setsetInternalSubset(INTERNAL_SUBSET);
//        }

        OutputFormat fmt = new OutputFormat();
        fmt.setIndentSize(0);
        fmt.setNewlines(true);
        fmt.setTrimText(false);

        FileOutputStream out = new FileOutputStream(filename);
        XMLWriter writer = new XMLWriter(out, fmt);
        writer.write(doc);
        out.flush();
        out.close();
    }
}
