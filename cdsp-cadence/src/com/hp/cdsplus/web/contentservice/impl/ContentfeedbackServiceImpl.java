package com.hp.cdsplus.web.contentservice.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.cdsplus.conversion.JAXBContextManager;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.concentra.bindings.output.schema.contentfeedback.Content;
import com.hp.concentra.bindings.output.schema.contentfeedback.Document;
import com.hp.concentra.bindings.output.schema.contentfeedback.Ref;
import com.hp.concentra.bindings.output.schema.contentfeedback.Result;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import javax.ws.rs.WebApplicationException;

public class ContentfeedbackServiceImpl extends AbstractGenericService {
	
	ContentDAO contentDAO = new ContentDAO();
	
	@Override
	public Object getExpandDetails(Options options) {
		
		DBObject document=null;
		
		if (options.getExpand() != null) {
			
			if (options.getExpand().equalsIgnoreCase("true")) {
				
				Result result = new Result();
				Ref ref = new Ref();
				Content content = new Content();
				document=getDocument(options);
				if(document == null){
					throw new ApplicationException(ServiceConstants.errorMsg_The_Entry +" "+ options.getDocid()
                            + "  "+ServiceConstants.errorMsg_doesnt_exist);
				}
				Long lastModified=(Long)document.get("lastModified");
				Integer priority=(Integer) document.get("priority");
				
				String subscription = options.getSubscription() == null ? "content": options.getSubscription();
				
				String href= options.getContentType()+ "/"+subscription+"/"+document.get("_id");
				
				if((String) document.get("eventType")!=null)
					ref.setEventType((String) document.get("eventType"));
				
				if((Boolean) document.get("hasAttachments")!=null)
					ref.setHasAttachments((Boolean)document.get("hasAttachments"));
				
				if(lastModified!=null)
					ref.setLastModified(BigInteger.valueOf(lastModified));
				
				if(priority!=null)
					ref.setPriority(BigInteger.valueOf(priority));
				
				if(href!=null)
					ref.setHref(href);
				
				ref.setType(ServiceConstants.xmlElementType);
				
				content.setDocument(null);
				ref.setContent(content);
				
				result.getRef().add(ref);
				result.setConsidered(new BigInteger("0"));
				result.setCount(new BigInteger("1"));
				
				org.dom4j.Document dom4jDoc = null;
				
				Marshaller marshaller;
				try {
					marshaller = JAXBContextManager.getInstance().getJAXBContext("com.hp.concentra.bindings.output.schema.contentfeedback.Result").createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					Writer writer = new OutputStreamWriter(baos);
					marshaller.marshal(result, writer);
					String convertedXML = baos.toString();
					
					dom4jDoc = DocumentHelper.parseText(convertedXML);
					
					org.dom4j.Element contentElement = (org.dom4j.Element)dom4jDoc.selectSingleNode("/result/proj:ref/content");
					
					InputStream is = new ByteArrayInputStream(getDocumentMetaData(options).toString().getBytes("UTF-8"));
					SAXReader reader = new SAXReader();
					reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
					org.dom4j.Document metadata = reader.read(is);					
					metadata.getRootElement().attributes().removeAll(metadata.getRootElement().attributes());
					contentElement.add(metadata.getRootElement());
					
				} catch (JAXBException e) {
					e.printStackTrace();
				} catch (ConversionUtilsException e) {
					e.printStackTrace();
				} catch (DocumentException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (ApplicationException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				return dom4jDoc.asXML() ;
			}
		}		
		return getDocumentExpandDetails(options);
	}
	
	public String getStringFromDoc(org.w3c.dom.Document doc)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(doc);   
	}

	@Override
	public Object getDocumentMetaData(Options options)
			throws ApplicationException, WebApplicationException {
		
		if(options.getSubscription()!=null && options.getSubscription().equals(ServiceConstants.stylesheetSub)){
			 return stylesheetUtil.getStylesheetXMLDocument(options);
		}
		
		String doc = null;
		org.w3c.dom.Document document = null;
		Document documentXml = (Document) super.getDocumentMetaData(options);
		documentXml.setBase(options.getBaseUri());
		
		if (options.getSubscription()!=null && (options.getSubscription().equals("astro2"))) {
			
			removeHierachyExpOutsideProductsTag(documentXml);
			doc = convertObjectToXml(options, documentXml);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			try {	
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					document =  docBuilder.parse(new InputSource(new StringReader(doc)));
					document.getDocumentElement().normalize();
					doc = changeTagName(document, "product", "pm_oid");
					org.w3c.dom.Document domDocument = updateExtraPropertyView(doc,options.getDocid());
					doc = changeDOMObjectToXml(domDocument);
					// Remove Indentation
					doc = removeIndentation(doc);
					return doc;
					
			} catch (ParserConfigurationException e) {
				throw new ApplicationException(e.getMessage());
			}catch (SAXException e) {
				throw new ApplicationException(e.getMessage());
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
		}
		doc=convertObjectToXml(options, documentXml);
//		if(options.getSubscription()!=null && !("content").equals(options.getSubscription())){
//			org.w3c.dom.Document domDocument = updateExtraPropertyView(doc,options.getDocid());
//			doc = changeDOMObjectToXml(domDocument);
//		}	
		// Remove Indentation
		doc = removeIndentation(doc);
		return doc;
	}
	
	private void removeHierachyExpOutsideProductsTag(Document documentXml) {
		documentXml.setProductNames(null);
		documentXml.setProductLines(null);
		documentXml.setProductTypes(null);
		documentXml.setProductNumbers(null);
		documentXml.setProductNumberNames(null);
		documentXml.setProductMarketingCategories(null);
		documentXml.setProductMarketingSubcategories(null);
		documentXml.setProductSupportCategories(null);
		documentXml.setProductSupportSubcategories(null);
		documentXml.setProductBigSeries(null);
		documentXml.setProductSeries(null);
		documentXml.setSupportNameOids(null);
	}
	private String changeTagName(org.w3c.dom.Document doc, String fromTag, String toTag) {
		
		StringWriter sw = null;
		StreamResult result=null;
		
		NodeList nodes = doc.getElementsByTagName("products").item(0).getChildNodes();
		Integer listSize = nodes.getLength();
		
		if(nodes!=null){
			for (int i = 0; i < listSize; i++) {
				if (nodes.item(i) instanceof Element) {
					Element elem = (Element) nodes.item(i);
					
					if (!elem.hasAttributes())	{					
						if(elem.getElementsByTagName("proj:ref")!=null){
							
							if(elem.getFirstChild()!=null)
							elem.removeChild(elem.getFirstChild());
							
							elem.removeChild(elem.getElementsByTagName("proj:ref").item(0));
						}
						doc.renameNode(elem, elem.getNamespaceURI(), toTag);
					}
				}
			}		
			try {
				
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				sw = new StringWriter();
				result = new StreamResult(sw);
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);
				
			} catch (TransformerException e) {
				throw new ApplicationException(e.getMessage());
			}
		}
		return sw.toString();
	}
	
	@Override
    public Object getExpandedDocumentList(Options options) {
           DBCursor docList = null;          
           Ref ref = null;
           ArrayList<Ref> refs = new ArrayList<Ref>();
           Result result = new Result();
           try {
        	      if(options.getLimit()>10000)
        	    	 options.setLimit(10000); 
        	      else if(options.getLimit()==0)
                     options.setLimit(20);
                  
                  String subscription = options.getSubscription() == null ? "content"
                               : options.getSubscription();
                  String urlLink = options.getContentType() + "/" + subscription
                               + "/";

                  if (!("content".equals(subscription)) && !validateSubcsription(options, subscription) && !validateFastxmlSubscription(options, subscription)){

                        throw new ApplicationException(
                                      ServiceConstants.errorMsg_The_Entry +" "+ subscription
                                      + "  "+ServiceConstants.errorMsg_doesnt_exist);
                  }else{
                        docList = contentDAO.getLiveDocumentList(options);
                  }

                  validateDocumentCount(docList,options);
                  if(options.isReverse() && docList!=null){
                        docList.sort(new BasicDBObject("lastModified",1));
                  }

                  result.setBase(options.getBaseUri());

                  if (docList != null && !(docList.size() == 0)) {
                        for (DBObject docObject : docList) {
                               ref = new Ref();
                               ref.setEventType(docObject.get(ServiceConstants.eventType)==null ? "update":docObject.get(ServiceConstants.eventType).toString());
                               ref.setHasAttachments(docObject.get(ServiceConstants.hasAttachments)==null ? null:Boolean.valueOf(docObject.get(ServiceConstants.hasAttachments).toString()));
                               ref.setLastModified(docObject.get(ServiceConstants.lastModified)==null ? null:BigInteger.valueOf(Long.valueOf(docObject.get(ServiceConstants.lastModified).toString())));
                               ref.setPriority(docObject.get(ServiceConstants.priority)==null ? BigInteger.valueOf(4l):BigInteger.valueOf(Long.valueOf(docObject.get(ServiceConstants.priority).toString())));
                               /*
                               if(ref.getEventType()!=null){
                                      if(ref.getEventType().equalsIgnoreCase("delete"))
                                             ref.setStatus(docObject.get(ServiceConstants.eventType).toString());
                               }*/
                               ref.setType(ServiceConstants.xmlElementType);
                               ref.setHref(urlLink + docObject.get(ServiceConstants.id));
                               Content content = new Content();
                               options.setDocid(docObject.get(ServiceConstants.id).toString());
                               content.setDocument((Document) super.getDocumentMetaData(options));
                               ref.setContent(content);
                               refs.add(ref);
                        }
                        result.getRef().addAll(refs);
                        result.setCount(BigInteger.valueOf(Long.valueOf(String.valueOf(docList.size()))));
                  }else
                	  result.setCount(new BigInteger("0"));
                  
                  result.setBase(options.getBaseUri());
                  result.setConsidered(new BigInteger("0"));
                  return result;
           } catch (OptionsException oe) {
                  throw new ApplicationException(oe.getMessage());
           } catch (MongoUtilsException mue) {
                  //throw new ApplicationException(mue.getMessage());
   			throw new WebApplicationException(mue, 500);
           } catch (MongoException e) {
                //throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } 
    }
	
}