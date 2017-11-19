package com.hp.cdsplus.web.util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.DisclosureLevelWhitelist;
import com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.HierarchyExpansions;
import com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.ImportedStyles;
import com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.ProjNamespaceTypes;
import com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.Stylesheet;
import com.hp.cdsplus.conversion.ConversionUtils;
import com.hp.cdsplus.conversion.exception.ConversionUtilsException;
import com.hp.cdsplus.dao.ConfigDAO;
import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import javax.ws.rs.WebApplicationException;

public class StylesheetSubscriptionUtil {
	
	protected ContentDAO contentDAO = new ContentDAO();
	protected ConfigDAO configDAO = new ConfigDAO();
	protected ConversionUtils conversion = new ConversionUtils();
	private String bindingName = "com.hp.cdsplus.bindings.output.schema.stylesheetsubscription.Stylesheet";
	
	
	public Object getStylesheetXMLDocument(Options option){
		
		Object stylesheetObject = null;
		String convertObjectToXml = null;
		
		try {
			
			if(!validateSubcsription(option))
					throw new ApplicationException(ServiceConstants.errorMsg_The_Entry +" "+ option.getDocid()
											+ "  "+ServiceConstants.errorMsg_doesnt_exist);
			
			/*if(checkForHtmlStylesheet(option)){
				stylesheetObject =  getHtmlStylesheetObject(option);
				
			}else if(checkForGroomerSubscription(option)){
				stylesheetObject =  getGroomerStylesheetObject(option);
				
			}else{*/
				stylesheetObject = getGeneralStylesheetObject(option);
			//}
			
			if(stylesheetObject == null)
					throw new ApplicationException(ServiceConstants.errorMsg_The_Entry + " "+ option.getDocid() + " "
									+ ServiceConstants.errorMsg_doesnt_exist);
			
			convertObjectToXml = conversion.convertObjecttoXML(stylesheetObject, bindingName);
			
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			//throw new ApplicationException(e.getMessage());
			throw new WebApplicationException(e, 500);
		} catch (OptionsException e) {
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		}catch (ConversionUtilsException e) {
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		}
		return convertObjectToXml;
		
	}
	
	private boolean validateSubcsription(Options options) throws MongoUtilsException, OptionsException{
		
		if(ConfigurationManager.getInstance().isValidSubscription(options.getContentType(), options.getDocid()) ){ 
				 //|| checkForGroomerSubscription(options) ||  checkForHtmlStylesheet(options)){
			return true;
		}
		return false;
	}
	
	 public Object getGeneralStylesheetObject(Options option){
	    	
		 	Stylesheet generalSub = null;
	    	String dbObject = null;
	    	
	    		    	
	    	try {
	    		
	    		dbObject = configDAO.getSubscription(option.getContentType(),option.getDocid());
	    		
				if(dbObject != null){
					
					BasicDBObject jsonObject=(BasicDBObject)JSON.parse(dbObject);					
					BasicDBObject stylesheetDBObject = new BasicDBObject().append("stylesheet", jsonObject.get("stylesheet"));
					
					if(stylesheetDBObject != null) {
					
						generalSub = (Stylesheet) conversion.convertJSONtoObject(stylesheetDBObject.toString(),bindingName);
						generalSub.setBase(option.getBaseUri());
						
						if(jsonObject.get("_id") != null)
							generalSub.setSubscriptionName(jsonObject.get("_id").toString());
						
						if(jsonObject.get(ServiceConstants.GENERAL_FILTER) != null)
							generalSub.setFilter(jsonObject.get(ServiceConstants.GENERAL_FILTER).toString());
						
						if(jsonObject.get(ServiceConstants.GENERAL_HIERARCHY_EXPANSIONS) != null)
							generalSub.setHierarchyExpansions(getHierarchyExpansionsObject(jsonObject));
											
						if(jsonObject.get(ServiceConstants.GENERAL_SMART_FOLDER) != null)
							generalSub.setSmartfolder(jsonObject.get(ServiceConstants.GENERAL_SMART_FOLDER).toString());
					}
					
				}
						
			} catch (MongoUtilsException e) {
				e.printStackTrace();
				//throw new ApplicationException(e.getMessage());
				throw new WebApplicationException(e, 500);
			}catch (ConversionUtilsException e) {
				e.printStackTrace();
				throw new ApplicationException(e.getMessage());
			}
	    	return generalSub;
	    	
	 }

	private HierarchyExpansions getHierarchyExpansionsObject(DBObject jsonObject) {
		
		HierarchyExpansions hierExp = null;
		String hierExpStr = jsonObject.get(ServiceConstants.GENERAL_HIERARCHY_EXPANSIONS).toString();
		hierExpStr = hierExpStr.replace("{", "").replace("}", "").trim();
				
		try{				
			if(!hierExpStr.isEmpty()){
				
				hierExp = new HierarchyExpansions();
				String[] listOfHE = hierExpStr.split(",");
				for(String keyvlaue : listOfHE){
					
					String[] key = keyvlaue.split(":");
					String reqStr = checkForRequiredStr(key[0]);
					if(reqStr != null) {
					
						HierarchyExpansionParams heParam= HierarchyExpansionParams.valueOf(reqStr);
						switch(heParam){		
						
							case product_names:hierExp.setProductNames(key[1]); break;
							case product_types:hierExp.setProductTypes(key[1]); break;
							case product_numbers:hierExp.setProductNumbers(key[1]); break;
							case product_number_names:hierExp.setProductNumberNames(key[1]); break;
							case product_lines:hierExp.setProductLines(key[1]); break;
							case product_big_series:hierExp.setProductBigSeries(key[1]); break;
							case product_series:hierExp.setProductSeries(key[1]); break;
							case product_marketing_categories:hierExp.setProductMarketingCategories(key[1]); break;
							case product_marketing_subcategories:hierExp.setProductMarketingSubcategories(key[1]); break;
							case product_support_categories:hierExp.setProductSupportCategories(key[1]); break;
							case product_support_subcategories:hierExp.setProductSupportSubcategories(key[1]); break;
							case support_name_oids:hierExp.setSupportNameOids(key[1]); break;	
							
							case faq_product_names:hierExp.setFaqProductNames(key[1]); break;
							case faq_product_types:hierExp.setFaqProductTypes(key[1]); break;
							case faq_product_numbers:hierExp.setFaqProductNumbers(key[1]); break;
							case faq_product_number_names:hierExp.setFaqProductNumberNames(key[1]); break;
							case faq_product_lines:hierExp.setFaqProductLines(key[1]); break;
							case faq_product_big_series:hierExp.setFaqProductBigSeries(key[1]); break;
							case faq_product_series:hierExp.setFaqProductSeries(key[1]); break;
							case faq_product_marketing_categories:hierExp.setFaqProductMarketingCategories(key[1]); break;
							case faq_product_marketing_subcategories:hierExp.setFaqProductMarketingSubcategories(key[1]); break;
							case faq_product_support_categories:hierExp.setFaqProductSupportCategories(key[1]); break;
							case faq_product_support_subcategories:hierExp.setFaqProductSupportSubcategories(key[1]); break;
							
							case concentra_internal_id:hierExp.setConcentraInternalId(	key[1]); break;
							case original_products:hierExp.setOriginalProducts(key[1]); break;	
							case product_name_proj_ref:hierExp.setProductNameProjRef(key[1]); break;
							case proj_is_smartfolder: hierExp.setProjIsSmartfolder(key[1]); break;
							case product_number_list:hierExp.setProductNumberList(key[1]); break;
							
							default: break;
						}
					}
				}
			}
		}catch(Exception e ){
			//Need to add in xsd and enum
			//throw new ApplicationException(heKey + "sub field has to add in HierarchyExpansion Object");
			throw new ApplicationException(e.getMessage());
		}
		return hierExp;
	}
	
	private String checkForRequiredStr(String check){
				
		Set<String> listOfHier = getListOfHier();
		for(String str : listOfHier){
			if(check.contains(str))
				return str;
		}
		return null;
	}
 public enum HierarchyExpansionParams {
		 
		 product_names,
		 product_types,
		 product_numbers,
		 product_number_names,
		 product_lines,
		 product_series,
		 product_big_series,
		 product_support_categories,
		 product_support_subcategories,
		 product_marketing_categories,
		 product_marketing_subcategories,
		 support_name_oids,
		 		 
		//For FAQ_Products
		 faq_product_names,
		 faq_product_types,
		 faq_product_numbers,
		 faq_product_number_names,
		 faq_product_lines,
		 faq_product_series,
		 faq_product_big_series,
		 faq_product_support_categories,
		 faq_product_support_subcategories,
		 faq_product_marketing_categories,
		 faq_product_marketing_subcategories,
		 
		 product_name_proj_ref,
		 product_number_list,
		 proj_is_smartfolder,
		 concentra_internal_id,
		 original_products;
	 }
	 private Set<String> getListOfHier(){
			
		 Set<String> set = new HashSet<String>();
		
		 	set.add("product_names");
			set.add("product_types");
			set.add("product_numbers");
			set.add("product_number_names");
			set.add("product_lines");
			set.add("product_big_series");
			set.add("product_series");
			set.add("product_marketing_categories");
			set.add("product_marketing_subcategories");
			set.add("product_support_categories");
			set.add("product_support_subcategories");
			
			set.add("support_name_oids");
			
			set.add("faq_product_names");
			set.add("faq_product_types");
			set.add("faq_product_numbers");
			set.add("faq_product_number_names");
			set.add("faq_product_lines");
			set.add("faq_product_big_series");
			set.add("faq_product_series");
			set.add("faq_product_marketing_categories");
			set.add("faq_product_marketing_subcategories");
			set.add("faq_product_support_categories");
			set.add("faq_product_support_subcategories");
			
			set.add("concentra_internal_id");
			set.add("original_products");
			set.add("product_name_proj_ref");
			set.add("product_number_list");
			set.add("proj_is_smartfolder");
			 
			return set;
		}
	 /*private boolean checkForGroomerSubscription(Options option){
 	
		try {
			DBCursor cursor = contentDAO.getAllGroomMappings();
			 if(cursor != null && cursor.length() != 0){
				Iterator<DBObject> iterator = cursor.iterator();
				while(iterator.hasNext()){
					DBObject dbObject  = (DBObject)iterator.next();
					if( dbObject.get("contentType") != null && dbObject.get("contentType").toString().equals(option.getContentType())
						&& dbObject.get("groomName") != null && dbObject.get("groomName").toString().equals(option.getDocid()))
						return true;
				}
			}
		} catch (MongoUtilsException e) {
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		} 
		return false;
	}*/
	
	/*private boolean checkForHtmlStylesheet(Options options) {
		
		if(options.getContentType().equals("supportcontent")){		
			File file = new File("/opt/ais/app/applications/cadence/config/groomsxslt/"+options.getDocid()+".xsl");
			if(file.exists()) return true;
		}
		return false;
	}*/
	
	 /*public Object getGroomerStylesheetObject(Options option){
	    	
	    	Stylesheet groomSub = null;
	    	
	    	try {
				
	    		DBCursor cursor = contentDAO.getAllGrooms(option.getContentType());
				if(cursor != null && cursor.length() != 0){
					
					Iterator<DBObject> iterator = cursor.iterator();
					groomSub = new Stylesheet();
					groomSub.setBase(option.getBaseUri());
			    	groomSub.setSubscriptionName(option.getDocid());
			    	groomSub.setType("groomer");
			    	List<Groomer> groomerList = groomSub.getGroomer();
			    	
					while(iterator.hasNext()){
						DBObject dbObject = (DBObject)iterator.next();
						if(dbObject.get("groomName").equals(option.getDocid())){
							Groomer groomer = new Groomer();
							if(dbObject.get(ServiceConstants.GROOMER_REGEX) != null)
								groomer.setRegex(dbObject.get(ServiceConstants.GROOMER_REGEX).toString());
							if(dbObject.get(ServiceConstants.GROOMER_REPLACEMENT) != null)
								groomer.setReplacement(dbObject.get(ServiceConstants.GROOMER_REPLACEMENT).toString());
							if(dbObject.get(ServiceConstants.GROOMER_SEARCH_PATTERN) != null)
								groomer.setSearchpattern(dbObject.get(ServiceConstants.GROOMER_SEARCH_PATTERN).toString());
							groomerList.add(groomer);
						}
					}
					if(groomerList!= null)
						groomSub.getGroomer().addAll(groomerList);
				}
			} catch (MongoUtilsException e) {
				e.printStackTrace();
				throw new ApplicationException(e.getMessage());
			}
	    	return groomSub;
	 }*/
	 /*public Object getHtmlStylesheetObject(Options option){
	    	
		 Stylesheet htmlSub =new Stylesheet();
		 htmlSub.setBase(option.getBaseUri());
		 htmlSub.setSubscriptionName(option.getDocid());
		 String xsltStylesheet = getXSLTStylesheet(option);
		 //System.out.println(xsltStylesheet);
		 Document stylesheetDOMObject = getDocumentObject(xsltStylesheet);
		 updatedHTMLSubObject(stylesheetDOMObject, htmlSub);
		 
		 return htmlSub;
     }*/
	 
	/* private void updatedHTMLSubObject(Document stylesheetDOMObject,Stylesheet htmlSub) {
		 
		 //For Import
		 NodeList importNodeList = stylesheetDOMObject.getElementsByTagName("xsl:import");
		 ImportedStyles imporedStyle = new ImportedStyles();
		 if(importNodeList != null){
			 for(int i=0; i<importNodeList.getLength();i++){
				 Element importElement = (Element) importNodeList.item(i);
				 imporedStyle.getImported().add(importElement.getAttribute("href").replace(".xsl", ""));
			 }
			 htmlSub.setImportedStyles(imporedStyle);
		 }
		 
		 //For Translations Object
		 Element  translationsElement= (Element)stylesheetDOMObject.getElementsByTagName("hptrans:translations").item(0);
		 if(translationsElement != null){
			 NodeList localeNodeList = translationsElement.getElementsByTagName("locale");
			 Translations translations = new Translations();
			 List<Locale> listOfLocale = new ArrayList<Locale>();
			 //List<Translation> listOfTranslation = new ArrayList<Translation>();
			 
			if(localeNodeList != null) {
				for(int i=0; i<localeNodeList.getLength();i++){
					Element localeElement = (Element) localeNodeList.item(i);
					Locale locale = new Locale();
					locale.setCc(localeElement.getAttribute("cc"));
					locale.setDocLocale(localeElement.getAttribute("doc_locale"));
					locale.setLc(localeElement.getAttribute("lc"));
					
					NodeList translationNodeList = localeElement.getElementsByTagName("translation");
					if(translationNodeList != null) {
							for(int j=0; j<translationNodeList.getLength();j++){
								Element translationElement = (Element)translationNodeList.item(j);
								locale.getTranslation().add(translationElement.getAttribute("name")+" - "+translationElement.getTextContent());			 
							}
					 }
					 listOfLocale.add(locale); 
				}
				translations.getLocale().addAll(listOfLocale);
			}
			htmlSub.setTranslations(translations);
		 }
	 }
	 */
	 /*private String getXSLTStylesheet(Options options) {
	    	
		 	String subName = options.getDocid().replace("stylesheet", "");
	    	String path = "/opt/ais/app/applications/cadence/config/groomsxslt/"+subName+".xsl";
	    	InputStream inputStream = null;
	    	BufferedReader br = null;
	    	
	        try {
	        	
	        	FileInputStream fis = new FileInputStream(path);
	            StringWriter writer = new StringWriter();
	            String encoding = "UTF-8";
	            IOUtils.copy(fis, writer, encoding);
	            System.out.println(writer.toString());
	        	
	        	inputStream = new FileInputStream(path);	
	        	br = new BufferedReader(new InputStreamReader(inputStream));
	            StringBuilder sb = new StringBuilder();
	            String line = br.readLine();

	            while (line != null) {
	                sb.append(line);
	                sb.append("\n");
	                line = br.readLine();
	            }
	            return sb.toString();
	            
	        } catch (FileNotFoundException file) {
	        	throw new ApplicationException(file.getMessage());
			}catch (IOException e) {
	        	throw new ApplicationException(e.getMessage());
			}finally {
	            try {
					br.close();
				} catch (IOException e) {
					throw new ApplicationException(e.getMessage());
				}
	        }
	 }*/
	 
	
	 
	/* private org.w3c.dom.Document getDocumentObject(String doc) {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document document = null;
			try {	
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					document =  docBuilder.parse(new InputSource(new StringReader(doc)));
					document.getDocumentElement().normalize();
					
			} catch (ParserConfigurationException e) {
				throw new ApplicationException(e.getMessage());
			}catch (SAXException e) {
				throw new ApplicationException(e.getMessage());
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
			return document;
	}*/
	 
	 public static void main(String[] args) throws UnknownHostException {
		 
		    Options option = new Options();
		    option.setContentType("supportcontent");
		    option.setDocid("html");
		    StylesheetSubscriptionUtil test = new StylesheetSubscriptionUtil();
			//Object checkHE = test.getHtmlStylesheetObject(option);
		  
	 }
				
}
