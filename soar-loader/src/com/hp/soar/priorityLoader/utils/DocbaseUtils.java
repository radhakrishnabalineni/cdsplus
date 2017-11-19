package com.hp.soar.priorityLoader.utils;



import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * This class encapsulates the various common docbase operations
 */
public class DocbaseUtils {

    public static IDfSession connectToDocbase(String docBasename, String userName, String password) {
        System.out.println("create new DfLoginInfo for change state");
        IDfLoginInfo li = new DfLoginInfo();
        li.setUser(userName);
        li.setPassword(password);
        li.setDomain("");
        try {
            IDfClient dfc = DfClient.getLocalClient();
            return dfc.newSession(docBasename, li);
        } catch (DfException e) {
            System.out.println("Unable to connect to docbase: " + e.toString());
        }
        return null;
    }

 /*
    public static String convertFileToUTF8(String filename, String encoding) throws IOException, DmRepositoryException {
        String returnFilename = filename;
        Vector vecEncoding;
        boolean bConvert = true;
        String validFormat = "ASCII|UTF-8|UTF-16";

        // check for BOM and remove it (CZ: 20-02-2004)
        removeBOMcharacters(filename);

        if (encoding == null || encoding.equals("")) {
            vecEncoding = getFileEncoding(filename, false);

            if (vecEncoding.size() == 0) {
                throw new DmRepositoryException("SOAR was unable to determine the encoding of this file");
            }
            if (vecEncoding.size() == 1) {
                if (validFormat.indexOf((String) vecEncoding.get(0)) != -1)
                    bConvert = false;
            }
            encoding = (String) vecEncoding.get(0);
        } else {
            if (validFormat.indexOf(encoding) != -1)
                bConvert = false;
        }
        SessionLog.log("filename: " + filename, SessionLog.LOG_DBG, null, "DocbaseUtils");
        SessionLog.log("encoding: " + encoding, SessionLog.LOG_DBG, null, "DocbaseUtils");
        SessionLog.log("bConvert: " + bConvert, SessionLog.LOG_DBG, null, "DocbaseUtils");

        if (bConvert) {
            String strContent = convertFileToUTF8AsString(filename, encoding);

            if (strContent != null) {
                File outputFilename = new File(filename);

                FileOutputStream outputFile = new FileOutputStream(outputFilename);

                OutputStreamWriter osw = new OutputStreamWriter(outputFile, "UTF-8");
                osw.write(strContent);
                osw.flush();
                osw.close();

                outputFile.close();
            }
        }

        return returnFilename;
    }

    public static String convertFileToUTF8(String filename) throws Exception {
        String returnFilename;

        returnFilename = convertFileToUTF8(filename, null);

        return returnFilename;
    }

    public static String convertFileToUTF8AsString(String filename, String encoding) throws IOException {
        String fileContent = null;
        String result;
        int red;

        removeBOMcharacters(filename);

        InputStreamReader isr;
        isr = new InputStreamReader(new FileInputStream(filename), encoding);

        char[] buf = new char[1024];

        while ((red = isr.read(buf, 0, 1024)) != -1) {
            if (fileContent == null) {
                fileContent = new String(buf, 0, red);
            } else {
                fileContent += new String(buf, 0, red);
            }
        }
        isr.close();

        result = fileContent;

        return result;
    }
*/
 
 
    /**
     * Method for executing a DQL query - new version which accepts description
     * and includes timing information
     * 
     * @param session
     * @param queryString :
     *            the dql query string to be executed
     * @param queryType :
     *            the type of query, e.g. Read or Execute
     * @param queryDescription :
     *            user defined description to be written to the log file
     * @return
     * @throws DfException 
     */
    public static IDfCollection executeQuery(IDfSession session, String queryString, int queryType,
            String queryDescription) throws DfException  {
 
      IDfCollection col = null; //For the result

      // set default query type
      if (queryType < 0) {
        queryType = IDfQuery.READ_QUERY;
      }
      IDfCollection aud = null;
      long start = System.currentTimeMillis();

      IDfQuery q = new DfQuery(); // Create query object
      q.setDQL(queryString); // Give it the query
      col = q.execute(session, queryType); // execute and return

      long queryTime = System.currentTimeMillis() - start; // in milliseconds

      if (col == null) {
      	String msg = "executeQuery() -- Query failed - Null Results -- check query : [" + queryString + "]";
      	LoaderLog.error(msg);
      	throw new DfException(msg);
      }
      if (col.getState() != IDfCollection.DF_READY_STATE) {
      	String msg = "executeQuery() -- Query failed - SYSTEM EXCEPTION -- collection not in ready state " + 
      							 col.getState() + " Qid:" + col.getObjectId().toString() + " query : [" + queryString + "]";
      	LoaderLog.error(msg);
      	throw new DfException(msg);
      }

      return col;

    }

    /**
     * Write a message to the standard log file
     * 
     * @param message
     *            the string to write to the log file
     private static String classLog(IDfSession session, String message) {
        return LoaderLog.log(message, LoaderLog.LOG_INF, session, "DocbaseUtils");
    }
    */

    public static void closeResults(IDfCollection results) {
        if (results != null) {
            try {
                results.close();
                results = null;
            } catch (Exception e) {

            }
        }
    }
    
}// end class

