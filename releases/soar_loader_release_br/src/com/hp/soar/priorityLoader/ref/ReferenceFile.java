/**
 * 
 */
package com.hp.soar.priorityLoader.ref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hp.cdspDestination.MongoDestination;
import com.hp.cdspDestination.ProjContent;
import com.hp.cdspDestination.Put;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;
import com.hp.loader.workItem.WorkItem;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.workItem.SoarExtractElement;

/**
 * @author dahlm
 *
 */
public class ReferenceFile {

  public static final String SCHEMA = "schema";
  public static final String FILEELEMENT = "fileElemName";
  private static final String XMLBASEATTR = "xml:base";
  private static final String SCHEMAATTR = "xsi:noNamespaceSchemaLocation";
  private static final String DATEATTR = "date";
  private static final String XMLNSXSIATTR = "xmlns:xsi"; 
  private static final String XMLNSXSIVALUE = "http://www.w3.org/2001/XMLSchema-instance"; 
  private static final String XMLNSLINKATTR = "xmlns:xlink"; 
  private static final String XMLNSLINGVALUE = "http://www.w3.org/1999/xlink";  
  private static final String XMLNSXMLATTR = "xmlns:xml"; 
  private static final String XMLNSXMLVALUE = "http://www.w3.org/XML/1998/namespace";
  private static final String XMLNSPROJATTR = "xmlns:proj"; 
  private static final String XMLNSPROJVALUE = "http://www.hp.com/cdsplus"; 


  protected static final String REFLIST_DISK_PATH = "reflist_disk_path";
  protected static final String REFLIST_CONTENT_TYPE = "reflist_content_type";

  protected static String baseUrl;

  protected static String diskLocation;
  protected static String cdspLocation;
  protected static Put put;
  protected static boolean initialize;

  protected boolean updated = false;

  // name of the enclosing element for this reference file
  protected String elementName;
  // schema for this reference file
  protected String schema;

  // List for keeping the order of output of the reference table
  private ArrayList<String> refTableOrder = new ArrayList<String>();
  
  private static boolean legacy= false;
  private static  MongoDestination mongoDest = null;

  /**
   * @param elementName
   * @param schema TODO
   */
  public ReferenceFile(String elementName, String schema) {
    super();
    this.elementName = elementName;
    this.schema = schema;
  }

  public void addTable(String tableName) {
    refTableOrder.add(tableName);
  }

  protected Document getDocument(HashMap<String, Date> updateDateMap) throws ProcessingException {
    // create a new document to hold this referenceFile
    // Create a new XML document
    Document doc = DocumentHelper.createDocument();
    doc.addElement(elementName);
    Element rootElement = doc.getRootElement();
    //		rootElement.addAttribute(XMLBASEATTR, baseUrl);
    rootElement.addAttribute(XMLNSXSIATTR, XMLNSXSIVALUE);
    //		rootElement.addAttribute(XMLNSLINKATTR, XMLNSLINGVALUE);
    //		rootElement.addAttribute(XMLNSXMLATTR, XMLNSXMLVALUE);
    //		rootElement.addAttribute(XMLNSPROJATTR, XMLNSPROJVALUE);
    rootElement.addAttribute(DATEATTR, ReferenceTable.formatDateValue(true));
    rootElement.addAttribute(SCHEMAATTR, schema);

    // populate the document with each reference table
    for(String tableName : refTableOrder) {
      ReferenceTable refTable = ReferenceLists.getRefTable(tableName);
      refTable.addTable(rootElement, updateDateMap);
    }
    return doc;
  }

  /**
   * @param updated the updated to set
   */
  public void setUpdated(boolean updated) {
    this.updated = updated;
  }

  public void store(HashMap<String, Date> updateDateMap) throws IOException, ProcessingException {
    if (!updated) {
      // nothing has changed so skip this file
      return;
    }

    Document doc = getDocument(updateDateMap);
    byte[] docBytes = SoarExtractElement.getBytes(doc);

    // write the file to disk
    FileOutputStream fos = new FileOutputStream(diskLocation+elementName+".xml");
    fos.write(docBytes);
    fos.close();

    // write the file to cds+
    String fileName = elementName+".xml";
    System.out.println("XML file name in Ref File:"+fileName);
    ProjContent pc = new ProjContent(cdspLocation, fileName, docBytes);
    if (!cdspLocation.endsWith("/")) {
      pc.setPath(cdspLocation + "/content/" + fileName);
    } else {
      pc.setPath(cdspLocation + "content/" + fileName);
    }
    boolean done = false;
    int numTries = 0;
    while (!done) {
    	try {
    		if(legacy){
    			String retVal = pc.put(put, WorkItem.EVENT_UPDATE, 1);
    			done = true;
    		}else{
    			String retVal = pc.processUpdate(mongoDest, WorkItem.EVENT_UPDATE, 1, false);
    			done = true;
    		}
    	} catch (ProcessingException pe) {
    		if (!pe.shouldRetry() && (numTries++ > 3)) {
        		LoaderLog.error("Failed to put referenceTable: "+fileName+" to cds+. "+pe.getMessage());
        		throw pe;
    		}
    		LoaderLog.error("Failed to put referenceTable: "+fileName+" to cds+. Retrying ");
    	}catch (MongoUtilsException cnfe) {
			throw new ProcessingException("Store Failed", true);
		}
    }

    updated = false;
  }

  public static void init(ConfigurationReader config) {
    baseUrl = config.getAttribute( SoarExtractElement.PROJECT_SERVER ); 
    put = new Put( baseUrl );
    legacy= Boolean.parseBoolean(config.getAttribute("legacy"));
	//System.out.println("in Ref File legacy:->"+legacy);
	mongoDest = new MongoDestination();

    diskLocation = config.getAttribute(REFLIST_DISK_PATH);
    if (diskLocation == null) {
      throw new IllegalArgumentException(REFLIST_DISK_PATH+" is not in config file.");
    }
    // make sure it ends with /
    if (!diskLocation.endsWith(File.separator)) {
      diskLocation += File.separator;
    }

    // add connent to content_type path
    cdspLocation = config.getAttribute(REFLIST_CONTENT_TYPE);
    if (cdspLocation == null) {
      throw new IllegalArgumentException(REFLIST_CONTENT_TYPE+" is not in config file.");
    }
  }

}
