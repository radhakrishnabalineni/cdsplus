package com.hp.cdsplus.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class LinkGroomer {
	
	String contentType = null;
	ContentDAO contentDAO = null;
	JSONArray jsonArray = null;
	HashMap<String,String> subscriptionGroomMap = null;
	/*
	static{
	   initialize();	
	}
	
	public static void initialize(){
		BufferedReader br = null;
		jsonArray = new JSONArray();
		try {
			String currentLine;
 			br = new BufferedReader(new FileReader("C:/Users/jaiswaln/workspace11_12_2013/cadence-latest/config/grooms/supportcontent/"+null+".json"));
 			while ((currentLine = br.readLine()) != null) {
 					System.out.println(currentLine);
					jsonArray.put(new JSONObject(currentLine));
			}
 			
 			String contentType = null;
 			
 			jsonArrayMap.put(contentType, jsonArray);
 		}catch (JSONException e) {
			e.printStackTrace();
		} 		
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
	*/
	
	public LinkGroomer(String contentType){
		this.contentType = contentType;
		contentDAO = new ContentDAO();		
		subscriptionGroomMap = new HashMap<String,String>();		
		refreshGrooms();
	}
		
	public void refreshGrooms(){
		jsonArray = new JSONArray();
		try {
			DBCursor cursor = contentDAO.getAllGrooms(contentType);
			DBObject dbObject = null;
			while(cursor.hasNext()){
				dbObject = cursor.next();
				jsonArray.put(new JSONObject(dbObject.toString()));
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, 500);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		subscriptionGroomMap.clear();		
	}
	public String applyGrooms(String originalString,String subscription){
		JSONObject jsonObject = null;
		String transformedString = originalString;
		try {
			boolean isMatched = false;
			for(int i=0;i<jsonArray.length();i++){
				jsonObject = (JSONObject)jsonArray.get(i);
				if(jsonObject.has("groomName") && ((String)jsonObject.get("groomName")).equalsIgnoreCase(getGroomNameForSubscription(subscription))){
					if(jsonObject!=null && jsonObject.has("searchpattern")){
						String[] response = RegexProcessor.replaceAll(transformedString, (String)jsonObject.get("searchpattern"), (String)jsonObject.get("regex"), (String)jsonObject.get("replacement"));
						transformedString = response[0];
						if(response[1]!=null && response[1].equals("matched")){
							isMatched = true;
							break;
						}
					}
					else if(jsonObject!=null && jsonObject.has("regex")){
						String[] response = RegexProcessor.replaceAll(transformedString, (String)jsonObject.get("regex"), (String)jsonObject.get("replacement"));
						transformedString = response[0];
						if(response[1]!=null && response[1].equals("matched")){
							isMatched = true;
							break;
						}
					}	
				}						
			}
			
			if(!isMatched)			
			for(int i=0;i<jsonArray.length();i++){
				jsonObject = (JSONObject)jsonArray.get(i);
				if(jsonObject.has("groomName") && ((String)jsonObject.get("groomName")).equalsIgnoreCase(getGroomNameForSubscription(subscription))){
				 if(jsonObject!=null && jsonObject.has("negativeregex")){
						String[] response = RegexProcessor.replaceAllNotMatching(transformedString, (String)jsonObject.get("negativeregex"), (String)jsonObject.get("replacement"));
						transformedString = response[0];
						if(response[1]!=null && response[1].equals("matched")){
							break;
						}
					}	
				}
			}						
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return transformedString;
	}
	
	
	public String applyGroomsToAttributes(InputStream in,Options options,Document productDocument) throws SAXException, DocumentException{
		refreshGrooms();
		String subscription = options.getSubscription();
		Document document = null;
	
			SAXReader reader = new SAXReader();
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			document = reader.read(in);
			if(options.getSubscription()==null || (!options.getSubscription().equals("hpgtxml") && !options.getSubscription().equals("xml") && !options.getSubscription().equals("xml_hpconnected")))
				document.getRootElement().addNamespace("proj", "http://www.hp.com/cdsplus");
			if(options.getSubscription()==null || (!options.getSubscription().equals("hpgtxml") && !options.getSubscription().equals("xml") && !options.getSubscription().equals("xml_hpconnected")))
				document.getRootElement().addNamespace("xlink", "http://www.w3.org/1999/xlink");
			if(document.processingInstruction("dctm")!=null)
				document.remove(document.processingInstruction("dctm"));
			document.setDocType(null);
			if(options.getBaseUri()!=null && (options.getSubscription()==null ||(!options.getSubscription().equals("hpgtxml") && !options.getSubscription().equals("xml") && !options.getSubscription().equals("xml_hpconnected"))))				
				document.getRootElement().addAttribute("xml:base", options.getBaseUri());
			List<Node> nodesList = document.selectNodes("//link|//url");
			Iterator<Node> nodesIterator = nodesList.iterator();
			while(nodesIterator.hasNext()){
				Element element = (Element)nodesIterator.next();
				element.addAttribute("address", applyGrooms(element.attributeValue("address"),subscription));
				//element.remove(element.attribute("show"));
			}
			if(productDocument!=null){
				List content = document.getRootElement().content();
				content.add(0,productDocument.getRootElement());				
			}
			
		if( this.contentType.equalsIgnoreCase("supportcontent") && subscription!= null && 
			(subscription.equalsIgnoreCase("bschtml") ||
		    subscription.equalsIgnoreCase("blossom1") ||
		    subscription.equalsIgnoreCase("html"))){
			
			TransformerFactory tfactory = TransformerFactory.newInstance();
			try{
				String path = "/opt/ais/app/applications/cadence/config/groomsxslt/"+subscription+".xsl";
				//Transformer transformer = tfactory.newTransformer(new StreamSource("/config/"+subscription+".xsl"));
				Transformer transformer = tfactory.newTransformer(new StreamSource(path));
				DocumentSource source = new DocumentSource(document);
				DocumentResult result = new DocumentResult();
				transformer.transform(source, result);				
				return toPretyHtml(result.getDocument());
				
			} catch (TransformerConfigurationException e) {
				throw new ApplicationException(e.getMessage());				
			}
			catch (TransformerException e) {
				throw new ApplicationException(e.getMessage());
			} catch (Exception e) {
				throw new ApplicationException(e.getMessage());
			}			
		}
		String documentString = document.asXML();
		//if(this.contentType.equalsIgnoreCase("supportcontent") && subscription!=null && subscription.equalsIgnoreCase("xml_supportcontent_properties"))
		documentString = documentString.replaceAll("<\\!\\-\\-MMR to EMR promotion transform V[0-9]\\.[0-9]\\-\\->", "");
		documentString = documentString.replaceAll("[<][?]xm[-][^>]*[?][>]","");
		documentString = documentString.replaceAll("\r*\n*<[?]xml[-]stylesheet[^?>]*[?]>", "");
		documentString = documentString.replaceAll("<para hidden=\"no\"> *\r*\n* *</para>", "<para hidden=\"no\"/>");
		return documentString;
	}
	
	public String getGroomNameForSubscription(String subscription){
		String groomName = null;		
		try {
			if(!subscriptionGroomMap.containsKey(subscription)){
				subscriptionGroomMap.put(subscription, contentDAO.getGroomName("supportcontent", subscription));
			}
			groomName = subscriptionGroomMap.get(subscription);
		} catch (MongoUtilsException e) {
				e.printStackTrace();
				throw new WebApplicationException(e, 500);
		}
		return groomName;
	}
	public String toPretyHtml(Document document) throws Exception {   
        
        StringWriter sw = new StringWriter();   
        OutputFormat format = OutputFormat.createPrettyPrint();   
        HTMLWriter writer;   
        try {   
            writer = new HTMLWriter(sw, format);   
            Document doc = DocumentHelper.parseText(document.asXML());   
            writer.write(doc);   
            writer.flush();  
            String html = sw.toString();
            html = html.replaceAll(" *\r?\n? *<\\?javax\\.xml\\.transform\\.disable-output-escaping \\?> *(\r?\n?)* *", "")
            		.replaceAll(" *\r?\n? *<\\?javax\\.xml\\.transform\\.enable-output-escaping \\?> *(\r?\n?)* *", "").replaceAll("&amp;nbsp;", "&nbsp;");   
            html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"+html;
            return html;
            
        } catch (UnsupportedEncodingException e) {   
            e.printStackTrace();   
        } catch (IOException e) {   
            e.printStackTrace();   
        }   
        return null;
}   

}
