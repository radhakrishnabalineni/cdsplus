import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.mongo.utils.MongoAPIUtils;
import com.mongodb.BasicDBObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class SaveAttachmentsToMongo
{
  private static MongoAPIUtils mongoapi = new MongoAPIUtils();
  static DefaultHttpClient httpClientL = new DefaultHttpClient();
  static DefaultHttpClient httpClientN = new DefaultHttpClient();
  static HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler()
  {
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
    {
      if (executionCount >= 2000) {
        return false;
      }
      if ((exception instanceof InterruptedIOException)) {
        return false;
      }
      if ((exception instanceof UnknownHostException)) {
        return false;
      }
      if ((exception instanceof ConnectTimeoutException)) {
        return false;
      }
      if ((exception instanceof SSLException)) {
        return false;
      }
      HttpClientContext clientContext = HttpClientContext.adapt(context);
      HttpRequest request = clientContext.getRequest();
      boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
      if (idempotent) {
        return true;
      }
      return false;
    }
  };
  
  static
  {
    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, 100000);
    httpClientL.setParams(httpParams);
    httpClientN.setParams(httpParams);
    httpClientL.setHttpRequestRetryHandler(myRetryHandler);
    httpClientN.setHttpRequestRetryHandler(myRetryHandler);
  }
  
  public static void main(String[] args)
    throws MongoUtilsException
  {
    System.setProperty("mongo.configuration", "/opt/ais/app/applications/soar_attachment_import/mongo.properties");
    String idListFile = "/opt/ais/app/applications/soar_attachment_import/docidList.txt";
    BufferedReader br = null;
    String line = "";
    Process p = null;
    try
    {
      File updateLogFile = new File("/opt/ais/app/applications/soar_attachment_import/update.log");
      updateLogFile.delete();
      updateLogFile = new File("/opt/ais/app/applications/soar_attachment_import/update.log");
      updateLogFile.createNewFile();
      br = new BufferedReader(new FileReader(idListFile));
      while ((line = br.readLine()) != null) {
        saveAttachmentForDocid(line.trim());
    	p = Runtime.getRuntime().exec("sed -i '1d' docidList.txt");
        p.waitFor();
        p.destroy();
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    } catch (InterruptedException e) {
		e.printStackTrace();
	}
  }
  
  public static void saveAttachmentForDocid(String DOC_ID)
  {
    System.out.println("Current:" + DOC_ID);
    HttpGet getRequestL = new HttpGet("http://cdsplus.austin.hp.com/cadence/app/soar/content/" + DOC_ID + "/*");
    HttpGet getRequestN = new HttpGet("http://cdsplus.houston.hp.com/cadence/app/soar/content/" + DOC_ID + "/*");
    String dbName = "soar";
    try
    {
      HttpResponse responseL = httpClientL.execute(getRequestL);
      HttpResponse responseN = httpClientN.execute(getRequestN);
      SAXReader reader = new SAXReader();
      Document documentL = null;Document documentN = null;
      if (responseL.getStatusLine().getStatusCode() == 200)
      {
        InputStream inL = responseL.getEntity().getContent();
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentL = reader.read(inL);
      }
      else
      {
        throw new IOException();
      }
      if (responseN.getStatusLine().getStatusCode() == 200)
      {
        InputStream inN = responseN.getEntity().getContent();
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentN = reader.read(inN);
      }
      else
      {
        throw new IOException();
      }
      if ((documentL.selectNodes("/result/proj:ref").size() > 0) && (documentN.selectNodes("/result/proj:ref").size() < documentL.selectNodes("/result/proj:ref").size()))
      {
        List<Node> nodes = documentL.selectNodes("//proj:ref/@xlink:href");
        Iterator<Node> iterator = nodes.iterator();
        FileWriter updateLogFile = new FileWriter("/opt/ais/app/applications/soar_attachment_import/update.log", true);
        String newLine = System.getProperty("line.separator");
        while (iterator.hasNext())
        {
          String path = ((Node)iterator.next()).getStringValue();
          if (documentN.selectNodes("//proj:ref[@xlink:href='" + path + "']").size() != 1)
          {
            getRequestN = new HttpGet("http://cdsplus.austin.hp.com/cadence/app/" + path);
            responseN = httpClientN.execute(getRequestN);
            if (responseN.getStatusLine().getStatusCode() == 200)
            {
              InputStream inN = responseN.getEntity().getContent();
              String[] pathParts = path.split("/");
              File f = new File("/opt/ais/app/applications/soar_attachment_import/" + pathParts[2]);
              f.mkdirs();
              FileOutputStream fileOutputStream = new FileOutputStream(new File("/opt/ais/app/applications/soar_attachment_import/" + pathParts[2] + File.separator + pathParts[3]));
              int read = 0;
              byte[] bytes = new byte[1024];
              while ((read = inN.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
              }
              fileOutputStream.close();
              
              updateLogFile.write(dbName + "\t" + pathParts[2] + "\t" + pathParts[3] + newLine);
              updateLogFile.flush();
              writeContent(dbName, pathParts[2], pathParts[3], "text/plain", "/opt/ais/app/applications/soar_attachment_import/" + pathParts[2] + File.separator + pathParts[3], 
                "update", Integer.valueOf(0), false);
              System.out.println("Successfully Saved File " + pathParts[2] + File.separator + pathParts[3]);
            }
            else
            {
              throw new IOException();
            }
          }
        }
        updateLogFile.close();
      }
    }
    catch (ClientProtocolException e1)
    {
      e1.printStackTrace();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
      try
      {
        Thread.sleep(10000L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
    catch (DocumentException e)
    {
      e.printStackTrace();
    }
    catch (MongoUtilsException e)
    {
      e.printStackTrace();
    }
  }
  
  public static void writeContent(String dbName, String id, String gridFSFileName, String mimeType, String filePath, String updateType, Integer priority, boolean deleteFile)
    throws MongoUtilsException
  {
    BasicDBObject document = new BasicDBObject();
    document.put("mime", mimeType);
    document.put("eventType", updateType);
    document.put("priority", priority);
    mongoapi.writeContent(dbName, id, gridFSFileName, filePath, 
      document, deleteFile);
  }
}
