package com.hp.concentra.extractor.utils;


import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.loader.workItem.WorkItem;


/**
 * This class is used to handle the mapping of the Concentra events into
 * Extractor events and priorities
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class MappingHandler {

	public static final int NO_PRIORITY = -1;
	
	private static final String EXTRACTOR_EVENT_ELEMENT_NAME = "extractor_event";
	private static final String PRIORITY_ELEMENT_NAME = "priority";
	private static final String CONCENTRA_EVENT_ELEMENT_NAME = "concentra_event";

	// Constants
	private static final String MAPPING_ELEMENT_NAME = "mapping";

	// Attributes

	// Mapping of the Concentra events into Priorities and Extractor events
	Hashtable<String, MappingNode> concentraEventsTable = null;

	// Constructors

	/**
	 * Constructs a newly allocated MappingHandler object
	 */
	public MappingHandler(String fileName) {
		File f = new File(fileName);
		if (!f.exists() || !f.canRead()) {
			throw new IllegalArgumentException("Event mapping file "+fileName+" is missing or not readable.");
		}
		// Loads the mapping file into the internal structures
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document mappingDocument = null;
		NodeList mappingNodesList = null;
		Node mappingNodeInfo = null;
		Element mappingElementNode = null;
		String concentraEvent = null;
		String extractorEvent = null;
		Integer priority;

		// Reads the xml mapping file
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("Failed to create docBuilder.",e);
		}
		// Parses the mapping xml file
		try {
			mappingDocument = docBuilder.parse(f);
		} catch (SAXException e) {
			throw new IllegalArgumentException("Parse exception in MappingHandler",e);
		} catch (IOException e) {
			throw new IllegalArgumentException("IO exception in MappingHandler",e);
		}
		
		// normalize text representation
		mappingDocument.getDocumentElement().normalize();
		// Gets the mapping nodes
		mappingNodesList = mappingDocument.getElementsByTagName(MAPPING_ELEMENT_NAME);
		if (mappingNodesList != null && mappingNodesList.getLength() > 0) {

			concentraEventsTable = new Hashtable<String, MappingNode>();

			// Processes the mapping nodes
			for (int nodeIndex = 0; nodeIndex < mappingNodesList.getLength(); nodeIndex++) {

				mappingNodeInfo = mappingNodesList.item(nodeIndex);
				if (mappingNodeInfo.getNodeType() == Node.ELEMENT_NODE) {
					mappingElementNode = (Element) mappingNodeInfo;

					// Gets the mapping node information
					concentraEvent = getElementValue(mappingElementNode, CONCENTRA_EVENT_ELEMENT_NAME).trim().toUpperCase();
					String pVal = getElementValue(mappingElementNode, PRIORITY_ELEMENT_NAME).trim();
					try {
						priority = Integer.valueOf(pVal);
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Bad priority value <"+pVal+"> in event mapping file");
					}
					extractorEvent = getElementValue(mappingElementNode, EXTRACTOR_EVENT_ELEMENT_NAME).trim().toLowerCase();

					concentraEventsTable.put(concentraEvent, new MappingNode(extractorEvent,priority));
				}
			}
		}
		mappingDocument = null;
		docBuilder = null;
		docBuilderFactory = null;
	}

	/**
	 * Gets the Extractor event associated to a Concentra event
	 * 
	 * @param concentraEvent
	 *            the Concentra event
	 * @return the Extractor event
	 */
	public String getExtractorEvent(String concentraEvent) {
		String result = null;

		if (concentraEventsTable != null) {
			MappingNode node = (MappingNode) concentraEventsTable.get(concentraEvent); 
			if ( node != null) {
				result = node.getExtractorEvent();
			}
		}
		return result;
	}

	/**
	 * Gets the priority associated to a Concentra event
	 * 
	 * @param concentraEvent
	 *            the Concentra event
	 * @return the priority
	 */
	public int getPriority(String concentraEvent) {
		int result = NO_PRIORITY;

		// these need checking as ITG gets bad data that is not in the events table
		if (concentraEventsTable != null) {
			MappingNode node = concentraEventsTable.get(concentraEvent); 
			if ( node != null) {
				result = node.getPriority();
			}
		}
		return result;
	}

	/**
	 * getEventType gets the constant that represents the event
	 * @param event
	 * @return
	 */
	public String getEventType(String event) {
      if (event != null && event.equalsIgnoreCase(WorkItem.EVENT_UPDATE)) {
        return WorkItem.EVENT_UPDATE;
      } else if (event != null && event.equalsIgnoreCase(WorkItem.EVENT_DELETE)) {
        return WorkItem.EVENT_DELETE;
      } else if (event != null && event.equalsIgnoreCase(WorkItem.EVENT_TOUCH)) {
        return WorkItem.EVENT_TOUCH;
      }
      return event;
	}
	
	/**
	 * Gets the value of an element node
	 * 
	 * @param parentElement
	 *            the parent element node
	 * @param tagName
	 *            the tag name of the searched element
	 * @return the value of the searched element
	 */
	private String getElementValue(Element parentNode, String tagName) {
		NodeList elementList = null;
		String elementValue = null;

		elementList = parentNode.getElementsByTagName(tagName);
		elementValue = ((Node) (((Element) elementList.item(0)).getChildNodes().item(0))).getNodeValue().trim();
		elementList = null;
 
		return getEventType(elementValue);
	}

	public class MappingNode {

		private String extractorEvent;
		private Integer priority;

		/**
		 * Constructs a newly allocated MappingNode object
		 * 
		 * @param extractorEvent
		 *            Extractor event type
		 * @param priority
		 *            Priority assigned to the event type
		 */
		MappingNode(String extractorEvent, Integer priority) {
			// set the event to be the constant from WorkItem
			if (extractorEvent.equals(WorkItem.EVENT_TOUCH)) {
				this.extractorEvent = WorkItem.EVENT_TOUCH;
			} else if (extractorEvent.equals(WorkItem.EVENT_DELETE)) {
				this.extractorEvent = WorkItem.EVENT_DELETE;
			} else {
				this.extractorEvent = WorkItem.EVENT_UPDATE;
			}
			this.priority = priority;
		}

		/**
		 * Gets the Extractor event type
		 * 
		 * @return the registered Extractor event type
		 */
		String getExtractorEvent() {
			return extractorEvent;
		}

		/**
		 * Gets the priority assigned to a Concentra event type
		 * 
		 * @return the registered priority
		 */
		Integer getPriority() {
			return priority;
		}

	}
}
