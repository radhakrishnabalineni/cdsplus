package com.hp.loader.utils;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;

/**
 * Class is used to handle the reading of the configuration file for loaders
 * 
 * @author      GADSC IPG-IT CR
 * @version     %I%, %G%
 * @since       1.0
 */
public class ConfigurationReader {
    
    // Constants
    private static final String ELEMENTS_SEARCH_PREFIX_EXPR = "//";
    private static final String ATTRIBUTE_SEARCH_PREFIX_EXPR = "@";
    private static final String ELEMENT_TEXT_SEARCH_EXPR = "/text()";
    
    // Attributes

    private String fileName;
    private Document document;

    // Constructors

    public ConfigurationReader(String fileName) throws ParserConfigurationException, 
    																									SAXException, 
    																									IOException {
        this.fileName = fileName;
        DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
        docBuilderFact.setNamespaceAware(true);
        
        DocumentBuilder documentBuilder = docBuilderFact.newDocumentBuilder();
        // Parsing the XML document and storing it in memory
        document = documentBuilder.parse(fileName);
    }

    // Methods
    
    /**
     * Gets the specified attribute as a string
     * @param atributeName The name of the attribute of the root element    
     * @return The value associated to the attribute if found; otherwise null.
     */
    public String getAttribute(String attributeName) {
        String result = null;
        
        if (document != null && document.getDocumentElement() != null)
            result = this.document.getDocumentElement().getAttribute(attributeName);
        
        return result;
    }

    /**
     * Gets the specified Element from the XML file 
     * @param elementName The name of the element of the root element 
     * @return The element if found; otherwise null.
     */
    public Element getElement(String elementName) {
        NodeList nodes = null;
        Element result = null;

        // Gets all the elements with the same tag name, we assume there
        // is only going to be one element
        nodes = document.getElementsByTagName(elementName);
        
        //if more than one element is found, none of the nodes are returned
        if (nodes !=  null && nodes.getLength() == 1){
            result = (Element) nodes.item(0);
        }
        
        return result;
    }

    /**
     * Gets the elements from the XML file according to a specific name 
     * @param elementName The name of the elements to look for
     * @return The element if found; otherwise null.
     */
    public NodeList getElements(String elementName) {
    	XPathFactory xPathFactory = null;
    	XPath xpath = null;
    	XPathExpression xPathExpression = null;
    	NodeList result = null;

    	// Gets all the elements with the tag name specified by the
    	// elementName parameter
    	xPathFactory = XPathFactory.newInstance();
    	xpath = xPathFactory.newXPath();
    	try {
				xPathExpression = xpath.compile(ELEMENTS_SEARCH_PREFIX_EXPR.concat(elementName));
				result = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// if we have a problem with the xpath, we return null
			}
    	return result;
    }

    /**
     * Gets the value of a specific element 
     * @param parentNode  Parent node of the element
     * @param elementName The name of the elements to look for
     * @return The value of the element if found; otherwise null.
     */
    public String getElementValue(Node parentNode, String elementName) {
        XPathFactory xPathFactory = null;
        XPath xpath = null;
        XPathExpression xPathExpression = null;
        String result = null;

        try {
            // Gets the value of the element with the tag name specified by the
            // elementName parameter
            xPathFactory = XPathFactory.newInstance();
            xpath = xPathFactory.newXPath();
            xPathExpression = xpath.compile(elementName.concat(ELEMENT_TEXT_SEARCH_EXPR));
            result = (String) xPathExpression.evaluate(parentNode, XPathConstants.STRING);
        } catch (XPathExpressionException e ) {
        	  // problem with the element 
        }

        return result;
    }
    
    /**
     * Gets the value of a specific node attribute
     * @param Node The node which the attribute belongs to
     * @param attributeName The name of the attribute to look for
     * @return The value of the attribute if found; otherwise null.
     */
    public String getAttributeValue(Node node, String attributeName) {
        String result = null;
        // This gets all the elements with the tag name specified by the
        // elementName parameter
        Element element = (Element) node;
        if (element != null) {
        	result = element.getAttribute(attributeName);
        }
        return result;
    }
}
