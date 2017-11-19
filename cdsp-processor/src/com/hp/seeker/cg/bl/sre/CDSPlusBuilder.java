package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jdom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Build <code>MDProperties</code> from CDS+ metadata XML.
 * 
 * @author Kelly
 */
public class CDSPlusBuilder implements MDPropertiesBuilder {
		
	String NA_VALUE = "not applicable";
	@SuppressWarnings("unchecked")
    public MDProperties build(Element root) {
	    MDProperties props = new MDProperties();
	    
	    List<Element> l = root.getChildren();
	    for (Element e : l) {
	        String name = e.getName();
	        List<Element> cl = e.getChildren();
	        if (cl != null && ! cl.isEmpty()) {
	        	Collection<String> values = new ArrayList<String>();
	        	for (Element element : cl) {
	        		String s = element.getTextNormalize();
//	        		System.err.println(s);
	        		values.add((s == null || "".equals(s)) ? NA_VALUE : s);
	        		if (values.size() > 1) values.remove(NA_VALUE);      			
	        	}
	        	if (! values.isEmpty()) {
	        		put(props, name, new MultiValueProperty(values));
	        	}
	        } else {
	        	String s = e.getTextNormalize();
	        		put(props, name, new SingleValueProperty((s == null || "".equals(s)) ? NA_VALUE : s));
	        }
        }
	    return props;
	    
    }

	/**
	 * build from a CDS+ xml document instance
	 * @param xmlDoc
	 * @return
	 */
	public MDProperties build(Node xmlDoc) {
    MDProperties props = new MDProperties();

    NodeList nl = xmlDoc.getChildNodes();
    for (int i=0; i<nl.getLength(); i++) {
    	Node n = nl.item(i);
      String propName = n.getNodeName();
      MDProperty propValue = null;
      if (n.hasChildNodes()) {
      	Node child = n.getFirstChild();
      	if (child.getNodeType() == Node.TEXT_NODE) {
      		propValue = getMDProperty(child.getNodeValue());
      	} else {
      		propValue = getMDProperty(n.getChildNodes());
      	}
      } else {
      	propValue = getMDProperty(n.getNodeValue());
      }
      put(props, propName, propValue);
    }
    return props;
	}

	private MDProperty getMDProperty(NodeList nodeList) {
		HashSet<String> values = new HashSet<String>();
		values.add(NA_VALUE);
		for(int i=0; i<nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			String val = null;
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Node valNode = n.getFirstChild();
				if (valNode != null && (valNode.getNodeType() == Node.TEXT_NODE)) {
					val = valNode.getNodeValue();
				}
			} else if (n.getNodeType() == Node.TEXT_NODE) {
				val = n.getNodeValue();
			}
			if (val != null) {
				values.add(val);
			}
		}
		if (values.size() > 1) {
			values.remove(NA_VALUE);
		}
		return new MultiValueProperty(values);
	}
	
	/*
	 * getMDProperty with a string will return a SingleValueProperty 
	 */
	private MDProperty getMDProperty(String s) {
		return new SingleValueProperty((s == null || "".equals(s)) ? NA_VALUE : s);
	}
	
	private void put(MDProperties props, String name, MDProperty property) {
	    props.put(name, property);
	    
	    // add properties under alternative names:
	    if ("products".equals(name)) {
	    	props.put("target_oids", property);
	    }
	    
	    // also add common misspellings from the Mind Map...
	    if ("environment_details".equals(name)) {
	    	props.put("environments_details", property);
	    }
    }

}
