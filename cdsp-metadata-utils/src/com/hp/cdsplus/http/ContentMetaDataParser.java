package com.hp.cdsplus.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ContentMetaDataParser extends DefaultHandler {
	
	static Logger LOG = Logger.getLogger(ContentMetaDataParser.class);
	
	DocumentBean docBean = null;
	String tmpValue = null;
	
    SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser parser = null;
	InputStream inputStream = null;

	public DocumentBean parseXML (String docId, byte [] xmlBytes) {
		
        try {
        	docBean = new DocumentBean();
        	docBean.setId(docId);
            parser = factory.newSAXParser();
			if (xmlBytes.length > 0) {
				inputStream = new ByteArrayInputStream(xmlBytes);
				//LOG.info("Parsing: " + URL_STRING+docId);
				parser.parse(inputStream, this);
			} else {
				docBean.setParseFailed(true);
				docBean.setErrMsg(" Unknown Error");
			}
        } catch (ParserConfigurationException e) {
        	docBean.setParseFailed(true);
			docBean.setErrMsg(" Parser Configuration Error");
        } catch (SAXException e) {
        	docBean.setParseFailed(true);
			docBean.setErrMsg(" SAX Error");
        } catch (IOException e) {
        	docBean.setParseFailed(true);
			docBean.setErrMsg(" IO Error");
		}
		return docBean;
	}

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        //clear tmpValue on start of element
    	tmpValue = null;
    }
    
    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
    	
        // if end of book element add to list
        if (element.equals("product")) {
        	docBean.getPmoids().add(tmpValue);
        } else if (element.equals("master_object_name")) {
        	docBean.setId(tmpValue);
        } else if (element.equals("document_type")) {
        	docBean.setDocType(tmpValue);
        } else if (element.equals("content_topic")) {
        	docBean.setContentTopic(tmpValue);
        } else if (element.equals("content_topic_detail")) {
        	docBean.setContentTopicDetail(tmpValue);
        }
    }
    @Override
    public void characters(char[] ac, int i, int j) throws SAXException {
        tmpValue = new String(ac, i, j);
    }
}
