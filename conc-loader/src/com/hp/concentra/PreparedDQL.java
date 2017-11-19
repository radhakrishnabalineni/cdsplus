package com.hp.concentra;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.core.session.SessionException;
import com.hp.cks.concentra.utils.DmRepositoryException;
import com.hp.cks.concentra.utils.DocbaseUtils;
import com.hp.concentra.extractor.concentraSource.ConcentraSource;
import com.hp.concentra.extractor.utils.LoaderLog;
import com.hp.concentra.extractor.utils.Result;
import com.hp.loader.priorityLoader.ProcessingException;
import com.hp.loader.utils.ConfigurationReader;

/**
 * This class is used to create an object that represent a DLQ statement to be
 * executed.
 * 
 * It is also the singleton for the queries that are stored
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class PreparedDQL {
  private static final String EXTRACTION_DQL = "Extraction dql.";
  private static final String XML_QUERIES_FILE = "/ExtractorQueries.xml";

  // List of queries loaded
  private static HashMap<String, String> listQueries;

  // Attributes

  /**
   * Attribute that represents an extract session to Concentra.
   * 
   * @see ConcentraAdminSession
   */
  private ConcentraAdminSession docbaseSession = null;

  /**
   * Attribute that represents the DQL statement to be executed. The
   * parameters must be defined by using the '?' character as IN parameter
   * placeholders.
   */
  private String query;

  /**
   * Attribute that represents the table used to store the respective
   * parameters that will resolve the '?' parameter placeholders in the DQL
   * statements in a ordered way.
   */
  private Hashtable parameters;

  // Constructors

  /**
   * Constructs a newly allocated PreparedDQL object.
   */
  public PreparedDQL() {
    this.parameters = new Hashtable();
  }

  /**
   * Constructs a newly allocated PreparedDQL object.
   * 
   * @param session
   *            the extract session to Concentra object
   * @param query
   *            an DQL query that may contain one or more '?' characters (IN
   *            parameter placeholders)
   * @throws SessionException
   * @see ConcentraAdminSession
   */
  public PreparedDQL(ConcentraAdminSession docbaseSession, String query) {
    this();
    this.docbaseSession = docbaseSession;
    this.query = query;
  }

  // Methods

  /**
   * Sets a parameter to be used in the query.
   * 
   * @param paramIndex
   *            Index of the parameter according to the '?' characters order
   *            defined in the query
   * @param paramValue
   *            Value to be assigned to the parameter
   */
  public void setParameter(int paramIndex, String paramValue) {
    // Add the parameter to the parameters table
    this.parameters.put(Integer.toString(paramIndex), new DQLParameter(paramValue));
  }

  /**
   * Sets a parameter to be used in the query.
   * 
   * @param paramIndex
   *            Index of the parameter according to the '?' characters order
   *            defined in the query
   * @param paramValue
   *            Value to be assigned to the parameter
   * @param paramType
   *            Type to be assigned to the parameter:
   *            <ul>
   *            <li>DQLParameter.DQL_STRING
   *            <li>DQLParameter.DQL_LABEL
   *            <li>DQLParameter.DQL_INTEGER
   *            </ul>
   * @see DQLParameter
   */
  public void setParameter(int paramIndex, String paramValue, short paramType) {
    // Add the parameter to the parameters table
    this.parameters.put(Integer.toString(paramIndex), new DQLParameter(paramValue, paramType));
  }

  /**
   * Resolves the query ? characters according to the defined parameters.
   * 
   * @return the DQL query with the parameter placeholders resolved ('?'
   *         characters replaced with the respective values)
   */
  private String resolveParameters() {
    int index = 0, fromIndex = 0;
    String queryResolved = null;

    // Replace the ? character with the respective parameter
    // according to the index of occurrence
    try {
      queryResolved = query;
      while ((fromIndex = queryResolved.indexOf('?', fromIndex)) != -1) {
        if (parameters.containsKey(Integer.toString(index)))
          queryResolved = queryResolved.replaceFirst("\\?", ((DQLParameter) parameters.get(Integer
              .toString(index))).toDQLQueryString());
        index++;
      }
    } catch (Exception e) {
      queryResolved = null;
      LoaderLog.error("There was a problem trying to resolve the raw query and replacing (?) with his original value");
    }
    return queryResolved;
  }

  /**
   * Executes the DQL in this prepared object and returns the Result object
   * generated by the query.
   * 
   * @return a Result object that contains the data produced by the query;
   * 
   * @see Result
   */
  public Result execute() {
    String queryResolved = null;
    IDfCollection concentraResult = null;
    Result result = null;

    LoaderLog.info("PreparedDQL execute");

    if (this.query != null && !this.query.equals("")) {
      // if the query can be resolved with the defined parameters
      // then execute the query
      if ((queryResolved = resolveParameters()) != null) {

        // perform the query and store results

        try {
          LoaderLog.debug(queryResolved);

          concentraResult = DocbaseUtils.executeQuery(docbaseSession, queryResolved, IDfQuery.DF_EXEC_QUERY,
              EXTRACTION_DQL);
          result = new Result(concentraResult);
        } catch (DmRepositoryException e) {
          LoaderLog.error(e.getMessage());
        } finally {
          // Always have to close the collection, because there is a
          // limited number of available
          // collections for each session. If collection remains open,
          // other queries can't execute
          // producing a recurrent error caused for resources
          // limitation.
          if (concentraResult != null) {
            try {
              concentraResult.close();
              concentraResult = null;
            } catch (DfException e) {
              LoaderLog.error(e.getMessage());
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * init reads the queries from the file and setups up the lookup table for them
   * @param config
   * @throws ProcessingException 
   */
  static public void init(ConfigurationReader config) throws ProcessingException  {
    try {
      long start = System.currentTimeMillis();
      InputStream is = ConcentraSource.class.getResourceAsStream(XML_QUERIES_FILE);
      if (is != null) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(is);
        // normalize text representation
        doc.getDocumentElement().normalize();
        
        listQueries = new HashMap<String,String>();
        
        NodeList listOfQueries = doc.getElementsByTagName("query");
        for (int nodesIndex = 0; nodesIndex < listOfQueries.getLength(); nodesIndex++) {
          Node queryNode = listOfQueries.item(nodesIndex);
          if (queryNode.getNodeType() == Node.ELEMENT_NODE) {
            Element firstRuleElement = (Element) queryNode;
            String queryName = firstRuleElement.getAttribute("name");
            String dql =  getElementContent(firstRuleElement, "dql");
            if (listQueries.containsKey(queryName)) {
              throw new ProcessingException("Multiple queries with the same name "+queryName+" in "+XML_QUERIES_FILE);
            }
            listQueries.put(queryName, dql);
          }
        }
      } else {
          throw new ProcessingException("Unable to find queries file: "+XML_QUERIES_FILE+" in classpath.");
      }
    } catch (Exception e) {
      throw new ProcessingException(e);
    }
  }
  
  /**
   * get the query requested
   * 
   * @param queryName
   * @return A String Object with the query
   */
  static private String getQuery(String queryName) {
    return listQueries.get(queryName);
  }

  /**
   * Gets a new instance of PreparedDQL
   * 
   * @param queryName
   *            String with a query name
   * @return A new instance of PrepareDQL
   * @see PreparedDQL
   */
  static public PreparedDQL getPrepareDQL(String queryName, ConcentraAdminSession docbaseSession) {
      return new PreparedDQL(docbaseSession, getQuery(queryName));
  }

  /**
   * The main function of this private method is to return the content value
   * from a specific element
   * 
   * @param Element
   *            Element from the Doc xml needed to extract the content
   * @param String
   *            Use to pass the tag name you want to retrieve
   * 
   * @return String Use to return the content value from the specific element
   * @see Element
   */
  static private String getElementContent(Element firstRuleElement, String tagName) {
      NodeList firstNameList = firstRuleElement.getElementsByTagName(tagName);
      Element firstNameElement = (Element) firstNameList.item(0);
      NodeList textFNList = firstNameElement.getChildNodes();
      return ((Node) textFNList.item(0)).getNodeValue().trim();
  }

}
