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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.cdsplus.conversion.JAXBContextManager;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.CgsDAO;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.contentservice.AbstractGenericService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;
import com.hp.concentra.bindings.output.schema.manual.ObjectFactory;
import com.hp.concentra.bindings.output.schema.manual.Content;
import com.hp.concentra.bindings.output.schema.manual.ContentGroups;
import com.hp.concentra.bindings.output.schema.manual.Document;
import com.hp.concentra.bindings.output.schema.manual.Projectref;
import com.hp.concentra.bindings.output.schema.manual.Ref;
import com.hp.concentra.bindings.output.schema.manual.Result;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import javax.ws.rs.WebApplicationException;

public class ManualServiceImpl extends AbstractGenericService {

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
				result.setBase(options.getBaseUri());
				result.setConsidered(new BigInteger("0"));
				result.setCount(new BigInteger("1"));
				org.dom4j.Document dom4jDoc = null;
				
				Marshaller marshaller;
				try {
					marshaller = JAXBContextManager.getInstance().getJAXBContext("com.hp.concentra.bindings.output.schema.manual.Result").createMarshaller();
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
		documentXml.setBase((String)options.getBaseUri());
		ObjectFactory factory=new ObjectFactory();
		ContentGroups contentgroups=null;
		DBObject contentGroup=null;
		Projectref ref=null;
		contentgroups=factory.createContentGroups();
		if(options.getSubscription()!=null && !("content").equals(options.getSubscription())){
			
			//Document documentNode=null;
			try {
				Options opt= new Options();
				opt.setContentType("cgs");
				opt.setDocid(options.getDocid());
				contentGroup=new CgsDAO().getDocumentMetadata(opt);
				if(contentGroup!=null){
					DBObject groups = getObject(contentGroup,
							"groups");
					BasicDBList cotentGroupList=getList(groups,"group");
					Iterator<?> it=cotentGroupList.iterator();	
					while(it.hasNext()){
						String contentGroupObj=(String) it.next();
						if(contentGroupObj!=null){
						contentgroups.getContentGroup().add(contentGroupObj);
						documentXml.setContentGroups(contentgroups);
						}
						}
				} 
			} catch (OptionsException oe) {
				throw new ApplicationException(oe.getMessage());
			} catch (MongoUtilsException mue) {
				//throw new ApplicationException(mue.getMessage());
				throw new WebApplicationException(mue, 500);
			} catch (MongoException e) {
				//throw new ApplicationException(e.getMessage());
				throw new WebApplicationException(e, 500);
			}
			}else{
				ref=factory.createProjectref();
				ref.setType("simple");
				ref.setHref("cgs/content/"+options.getDocid());
				contentgroups.getProjectref().add(ref);
				documentXml.setContentGroups(contentgroups);
			}
		//ods:apply changes
		if( options.getSubscription()!=null && 
				(options.getSubscription().startsWith("ods"))){

			removeHierachyExpOutsideProductsTag(documentXml);
			doc = convertObjectToXml(options, documentXml);
			//System.out.println("doc-->"+doc);
			document = updateExtraPropertyView(doc,options.getDocid());
			try {
				document =  changeProductView(document,"product",options);
			} catch (MongoUtilsException e) {
				throw new ApplicationException(e.getMessage());
			}catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
			
			return removeIndentation(changeDOMObjectToXml(document));
		}
		
		if (options.getSubscription()!=null && (options.getSubscription().equals("astro2") || options.getSubscription().equals("astro2_201"))) {
			
			removeHierachyExpOutsideProductsTag(documentXml);
			doc = convertObjectToXml(options, documentXml);
			// Remove Indentation
			doc = removeIndentation(doc);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			try {	
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					document =  docBuilder.parse(new InputSource(new StringReader(doc)));
					document.getDocumentElement().normalize();
					document = changeTagName(document, "product_support_subcategories", "support_subcategory",1);
					document = changeTagName(document, "product_names", "product_name",1);
					document = changeTagName(document, "product_types", "product_type",1);
					return removeIndentation(changeDOMObjectToXml(changeTagName(document, "product", "pm_oid",2)));
					
			} catch (ParserConfigurationException e) {
				throw new ApplicationException(e.getMessage());
			}catch (SAXException e) {
				throw new ApplicationException(e.getMessage());
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
		}
		doc = convertObjectToXml(options, documentXml);
//		if(options.getSubscription()!=null && !("content").equals(options.getSubscription())){
//			doc = changeDOMObjectToXml(updateExtraPropertyView(doc,options.getDocid()));	
//		}		
		// Remove Indentation
		doc = removeIndentation(doc);
		return doc;
	}
	
	private BasicDBList getList(DBObject dbObject, String key) {
		Object list = dbObject.get(key);
		if (list == null || list=="") {
			return new BasicDBList();
		}
		BasicDBList returnList = new BasicDBList();

		if (list instanceof BasicDBList) {
			return (BasicDBList) list;
		} else if (list instanceof DBObject) {
			returnList.add((DBObject) list);
		} else if (list instanceof String) {
			returnList.add((String) list);

		}
		return returnList;
	}
	private DBObject getObject(DBObject dbObject, String key) {
		DBObject returnObj = new BasicDBObject();
		Object obj = dbObject.get(key);
		if (obj == null || obj == "") {
			return null;
		} else if (obj instanceof DBObject) {
			returnObj = (DBObject) obj;

		} else if (obj instanceof String) {
			returnObj = (DBObject) obj;

		}
		return returnObj;
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
		
	private org.w3c.dom.Document changeTagName(org.w3c.dom.Document document, String fromTag, String toTag,int nestingLevel) {
		
		Element productsElement = (Element)document.getElementsByTagName("products").item(0);
		
		if(productsElement != null ) {
			
			if(nestingLevel==2){
				NodeList nodes = productsElement.getChildNodes();
				
				if(nodes!=null){
					for (int i = 0; i < nodes.getLength(); i++) {
						
						NodeList childNodeList =  nodes.item(i).getChildNodes();
						if(childNodeList != null) {
							
							for (int j = 0; j < childNodeList.getLength(); j++) {
								if (childNodeList.item(j) instanceof Element) {
									Element elem = (Element) childNodeList.item(j);
									if (!elem.hasAttributes())
										document.renameNode(elem, elem.getNamespaceURI(), toTag);
								}
							}
						}
					}		
				}
			}
			
			if(nestingLevel==1){
				Element elem = (Element)productsElement.getElementsByTagName(fromTag).item(0);
				document.renameNode(elem, elem.getNamespaceURI(), toTag);
			}
			
		}
		return document;
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
                  if(options.isReverse()){
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
                  
                  result.setConsidered(new BigInteger("0"));
                  
                  options.setDocid(null);
                  String str=convertObjectToXml("Result",result,options);          		
          		  return str;
           } catch (OptionsException oe) {
                  throw new ApplicationException(oe.getMessage());
           } catch (MongoUtilsException mue) {
                //throw new ApplicationException(mue.getMessage());
  				throw new WebApplicationException(mue, 500);
           } catch (MongoException e) {
                // throw new ApplicationException(e.getMessage());
      			throw new WebApplicationException(e, 500);
           } 
    }
	

	private org.w3c.dom.Document changeProductView(org.w3c.dom.Document document, String productsTag,Options options) throws MongoUtilsException, IOException {
		if(document != null) {
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(options.getContentType());
			GridFS gridFS = new GridFS(db, "ods_products");
			DBObject query = new BasicDBObject("filename",options.getDocid());
			GridFSDBFile gridFSDBFileMetadata = gridFS.findOne(query);
			if (gridFSDBFileMetadata != null){
				//System.out.println("Does not find ods transformation for this document.");
			

			InputStream inputStream = gridFSDBFileMetadata.getInputStream();

			String xmlDocument = IOUtils.toString(inputStream);
			BasicDBObject jsonObject=(BasicDBObject)JSON.parse(xmlDocument);

			BasicDBObject productsObj= (BasicDBObject)jsonObject.get("products");
			//BasicDBObject faq_productsObj= (BasicDBObject)jsonObject.get("faq_products");

			//System.out.println("productsObj->"+productsObj);
			//System.out.println("faq_productsObj->"+faq_productsObj);

			HashMap<String, String> productMap=(HashMap<String, String>)productsObj.toMap();
			//HashMap<String, String> faq_productMap=(HashMap<String, String>)faq_productsObj.toMap();

			Element productsElement = (Element)document.getElementsByTagName(SUB_DOC_PRODUCTS).item(0);
			if(productsElement != null) 
				removeProductNode(productsElement,"product");

			/*Element faqProductsElement = (Element)document.getElementsByTagName(SUB_DOC_FAQ_PRODUCTS).item(0);
			if(faqProductsElement != null) 
				removeProductNode(faqProductsElement,"product");*/

			NodeList productsNode = document.getElementsByTagName(productsTag);

			//if((!productMap.isEmpty() || !faq_productMap.isEmpty())){
			if(!productMap.isEmpty()){

				Set<String> productMapkeySet = productMap.keySet();
				Iterator<String> productMapkeySetIterator = productMapkeySet.iterator();

				//Set<String> faq_productMapkeySet = faq_productMap.keySet();
				//Iterator<String> faq_productMapkeySetIterator = faq_productMapkeySet.iterator();

				while(productMapkeySetIterator.hasNext()){
					String key= productMapkeySetIterator.next();
					Element productElement= (Element)productsElement.appendChild(document.createElement("product"));
					productElement.setTextContent(key);
					productElement.setAttribute("pm_col_name", productMap.get(key));
				}

//				while(faq_productMapkeySetIterator.hasNext()){
//					String key= faq_productMapkeySetIterator.next();
//					Element faqProductElement= (Element)faqProductsElement.appendChild(document.createElement("product"));
//					faqProductElement.setTextContent(key);
//					faqProductElement.setAttribute("pm_col_name", faq_productMap.get(key));
//				}
			}
		}
		}		
		return document;
	}
	
	private void removeProductNode(Node parent, String filter) {
		NodeList children = parent.getChildNodes();
		parent.setTextContent("");
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(filter)) {
				parent.removeChild(child);
			}
		}
}
}
