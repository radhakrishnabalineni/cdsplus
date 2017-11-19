package com.hp.cdsplus.processor.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BasicBSONEncoder;

import com.hp.cdsplus.dao.ContentDAO;
import com.hp.cdsplus.dao.Options;
import com.hp.cdsplus.dao.exception.OptionsException;
import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;
import com.hp.cdsplus.processor.Processor;
import com.hp.cdsplus.processor.exception.AdapterException;
import com.hp.cdsplus.processor.exception.ProcessException;
import com.hp.cdsplus.processor.item.ContentItem;
import com.hp.cdsplus.processor.item.Item;
import com.hp.cdsplus.processor.item.WorkItem;
import com.hp.cdsplus.processor.queue.QueueManager;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

/**
 */
public class MetadataTransformationAdapter implements Adapter {
	private ContentDAO contentDao;
	private boolean hasAttachments =false;
	
	private static final Logger logger = LogManager.getLogger(MetadataTransformationAdapter.class);
	//SMO : UserStory#7553 
	public static final String company_content = "content";
	public static final String HPI="HPI";
	private static final ThreadLocal<String> company_info = new ThreadLocal<String>();
	private static final String COMPANY_INFO = "company_info";
	
	public static final String TEMP_COLLECTION_NAME = "metadataTempCollection";
	public static final String PRODUCT_MASTER_DB_NAME = "productmaster";
	public static final String REGION_DB_NAME = "region";
	public static final String MANUAL_DB_NAME = "manual";
	public static final String SUPPORT_DB_NAME = "support";
	public static final String SOAR_DB_NAME = "soar";
	public static final String REGION_COLLECTION_NAME = "metadataLiveCollection";

	private static final String ID_FIELD = "_id";
	private static final String DOCUMENT_DOC = "document";
	private static final String PRODUCTS_DOC = "products";
	private static final String FAQ_PRODUCTS_DOC = "faq_products";
	private static final String PRODUCT_DOC = "product";
	private static final String RENDITIONS_DOC = "renditions";
	private static final String RENDITION_DOC = "rendition";
	private static final String FILE_NAME_DOC = "file_name";
	private static final String REGIONS_DOC = "regions";
	private static final String ESSO_REGIONS_DOC = "esso_regions";
	private static final String FAQ_REGIONS_DOC = "faq_regions";
	private static final String REGION_DOC = "region";
	private static final String ESSO_REGION_DOC = "esso_region";
	private static final String FAQ_REGION_DOC = "faq_region";
	private static final String REGION_NAME_FILED = "region.name";
	private static final String COUNTRIES_DOC = "countries";
	private static final String COUNTRY_DOC = "country";
	private static final String ESSO_COUNTRY_DOC = "esso_country";
	private static final String FAQ_COUNTRY_DOC = "faq_country";
	private static final String HAS_ATTACHMENT_FIELD = "hasAttachments";

	// Constants for sub documents to be updated in document
	private static final String SUB_DOC_PRODUCTS = "document.products";
	private static final String SUB_DOC_FAQ_PRODUCTS = "document.faq_products";
	private static final String SUB_DOC_RENDITIONS = "document.renditions";
	private static final String SUB_DOC_FILE_NAME = "document.file_name";
	private static final String SUB_DOC_COMPONENTS_FILE_NAME = "document.components.component";
	private static final String SUB_DOC_COUNTRIES = "document.countries";
	private static final String SUB_DOC_ESSO_COUNTRIES = "document.esso_countries";
	private static final String SUB_DOC_FAQ_COUNTRIES = "document.faq_countries";
	private static final String SUB_DOC_CGS = "document.content_groups";
	
	private static final String SUB_DOC_FAQ_REGIONS = "document.faq_regions";
	private static final String SUB_DOC_ESSO_REGIONS = "document.esso_regions";
	private static final String SUB_DOC_REGIONS = "document.regions";
	
	// proj:ref Specific Constants
	private static final String XLINK_TYPE_KEY = "@xlink/type";
	private static final String XLINK_TYPE_VALUE = "simple";
	private static final String XLINK_HREF_KEY = "@xlink/href";
	
	private static final String PROJECTREF = "projectref";
	private static final String VALUE = "#";
	private static final String SLASH = "/";
	private static final String DOT = ".";
	private static final String UNDERSCORE = "_";

	// Constants for SOAR
	private static final String SOAR_SOFTWARE_FEED_DOC = "soar-software-feed";
	private static final String SOAR_COLLECTION_DOC = "collection";
	private static final String SOAR_ATTACHMENTS_DOC = "attachments";
	private static final String SOAR_ATTACHMENT_DOC = "attachment";
	private static final String SOAR_FILENAME_DOC = "filename";
	private static final String SOAR_SOFTWARE_ITEMS_DOC = "software-items";
	private static final String SOAR_SOFTWARE_ITEM_DOC = "software-item";
	private static final String SOAR_PRODUCTS_SUPPORTED_DOC = "products-supported";
	private static final String SOAR_OID_ATTR = "@projectoid";
	private static final String SOAR_ITEM_ID="@item-ID";
	
	//Constants for CGS
	private static final String CGS="cgs";
	private boolean isPartitioned = false;
	private int tempDocumentSize = 0;
	private int maxHierarchyDocSize= 16252928;
	
	//SMO User story#7553 Changes : methods added for setting and getting companyinfo in a threadlocal variable.
	private static void setCompany_info(String info)
	{
		company_info.set(info);
	}
	
	
	private static String getCompany_info()
	{
		return company_info.get();
	}
	/**
	 * Method evaluate.
	 * 
	 * This is the entry point of the Metadata Transformation adapter. This
	 * method will be called by the Processor to apply transformation on the
	 * document metadata. This method calls method transformMetadata() to apply
	 * various transformation.
	 * 
	 * @param item
	 *            Item
	 * @throws OptionsException
	 * @throws MongoUtilsException
	 * @see com.hp.cdsplus.processor.adapters.Adapter#evaluate(Item)
	 */
	@Override
	public void evaluate(Item item) throws AdapterException,
			MongoUtilsException, OptionsException {
		WorkItem wItem = (WorkItem) item;
		contentDao = new ContentDAO();
		//reset hasAttachment 
		//this.setHasAttachments(false);
		String maxSize = System.getProperty("maxHierarchyDocSize");
		
		if(maxSize!=null && maxSize!=""){
			maxHierarchyDocSize = Integer.parseInt(maxSize);
		}
		DBObject gridFSUpdateDocument = new BasicDBObject();
		
		DBObject update = transformMetadata(wItem, gridFSUpdateDocument);
		if(this.isPartitioned){
			GridFSDBFile gridFSDBFile = null;
			InputStream inputStream = null;
			StringWriter writer = new StringWriter();
			String gridFSData = null;
			
			logger.info(wItem.getId()+ " IsPartitioned : "+isPartitioned+" Total Doc Size : "+this.tempDocumentSize);
			
			DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(item.getContentType());
			GridFS gfs = new GridFS(db, "product_content");
		
			GridFSInputFile gfsFile = gfs.createFile(gridFSUpdateDocument.toString().getBytes());
			gfsFile.setFilename(wItem.getId());
			gfsFile.setContentType(wItem.getContentType());
				
			List<GridFSDBFile> gridFSDBFileList=gfs.find(new BasicDBObject().append("filename",wItem.getId()));
			// If gridFS filelist is empty , save metadata info into gridFS
			if(gridFSDBFileList.size()==0){
				gfsFile.save();
			}else if (gridFSDBFileList.size()==1){
				if(gridFSDBFileList.iterator().hasNext()){
					gridFSDBFile = gridFSDBFileList.iterator().next();
					inputStream = gridFSDBFile.getInputStream();  
					try {
						IOUtils.copy(inputStream, writer, "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
					}
					gridFSData = writer.toString();
					// Check if it is old record only for metadata or it is hierarchy information
					if(gridFSData.contains(SUB_DOC_PRODUCTS) || gridFSData.contains(SUB_DOC_FAQ_PRODUCTS)){
						gfs.remove(new BasicDBObject().append("filename",wItem.getId()));
						gfsFile.save();
					}
					// Grid FS file is hierarchy information so append metadata information to that
					else{
						gfs.remove(new BasicDBObject().append("filename",wItem.getId()));
						DBObject dbo = (DBObject) JSON.parse(gridFSData);
						dbo.putAll(gridFSUpdateDocument);
						gfsFile = gfs.createFile(dbo.toString().getBytes());
						gfsFile.setFilename(wItem.getId());
						gfsFile.setContentType(wItem.getContentType());
						gfsFile.save();
					}
				}
				
					
			}
		}
		if (update != null) {
			Options options = new Options();
			options.setContentType(item.getContentType());
			options.setDocid(wItem.getId());

			options.setMetadataDocument(update);

			contentDao.writeMetadataToTemp(options);
		}

	}

	/**
	 * Transform the Metadata of WorkItem: wItem
	 * 
	 * @param wItem
	 * @return Transformed document to update
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 * @throws AdapterException
	 */
	public DBObject transformMetadata(WorkItem wItem, DBObject gridFSUpdateDocument)
			throws MongoUtilsException, OptionsException, AdapterException {

		BasicDBObject updateQuery = new BasicDBObject();
		BasicDBObject updateDocument = new BasicDBObject();
		DBObject transformedProduct = new BasicDBObject();
		DBObject transformedFAQProduct = new BasicDBObject();
		DBObject transformedRegion = new BasicDBObject();
		DBObject transformedFAQRegion = new BasicDBObject();
		DBObject transformedEssoRegion = new BasicDBObject();
		
		Options options = new Options();
		options.setContentType(wItem.getContentType());
		options.setDocid(wItem.getId());

		DBObject result = contentDao.getTempMetadata(options);

		if (result == null) {
			logger.error("Can't find Document object. Skipping processing of document id - " + wItem.getId());
			return null;
		}
		this.tempDocumentSize += new BasicBSONEncoder().encode(result).length;
		
		/*if((MANUAL_DB_NAME).equals(wItem.getContentType()) || (SUPPORT_DB_NAME).equals(wItem.getContentType())){
			updateQuery.append(SUB_DOC_CGS,
					getCGSTransformation(wItem));
		}*/
		

		DBObject document = (DBObject) result.get(DOCUMENT_DOC);
		

		if (document != null) {
			//SMO : 7553
			//CR NO:10483
			String companyType = (String)document.get(COMPANY_INFO);
			if(StringUtils.isEmpty(companyType))
			{
//				logger.debug("Company_info "+ companyType +" is an invalid value");
//				return null;""
				companyType = "";
			}
			//else
				setCompany_info(companyType);
			
			if (getObject(document, PRODUCTS_DOC) != null) {
				// Product Transformation
				DBObject products_sub_doc = getObject(document, PRODUCTS_DOC);
				if (products_sub_doc != null) {
					BasicDBList productList = getList(products_sub_doc,
							PRODUCT_DOC);
					if (productList != null) {
						transformedProduct = getProductTransformation(productList);
									updateQuery.append(SUB_DOC_PRODUCTS,transformedProduct);
						
					}
				}
			}
			
			//faq products expansion
			if (getObject(document, FAQ_PRODUCTS_DOC) != null) {
				// Product Transformation
				DBObject products_sub_doc = getObject(document, FAQ_PRODUCTS_DOC);
				if (products_sub_doc != null) {
					BasicDBList productList = getList(products_sub_doc,
							PRODUCT_DOC);
					if (productList != null) {
						transformedFAQProduct = getProductTransformation(productList);
							updateQuery.append(SUB_DOC_FAQ_PRODUCTS,transformedFAQProduct);
					}
				}
			}
			
			
			//faq regions expansion
			if (getObject(document, FAQ_REGIONS_DOC) != null) {
				// Product Transformation
				DBObject faq_regions_sub_doc = getObject(document, FAQ_REGIONS_DOC);
				if (faq_regions_sub_doc != null) {
					BasicDBList faqRegionsList = getList(faq_regions_sub_doc,FAQ_REGION_DOC);
					if (faqRegionsList != null) {
						BasicDBObject faqregion_sub_doc = new BasicDBObject();
						transformedFAQRegion = faqregion_sub_doc.append(FAQ_REGION_DOC, getRegionMetadataTransformation(faqRegionsList));
						
							updateQuery.append(SUB_DOC_FAQ_REGIONS,transformedFAQRegion);
					}
				}
			}
			
			//esso regions expansion
			if (getObject(document, ESSO_REGIONS_DOC) != null) {
				// Product Transformation
				DBObject esso_regions_sub_doc = getObject(document, ESSO_REGIONS_DOC);
				if (esso_regions_sub_doc != null) {
					BasicDBList essoRegionsList = getList(esso_regions_sub_doc,ESSO_REGION_DOC);
					if (esso_regions_sub_doc != null) {
						BasicDBObject essoregion_sub_doc = new BasicDBObject();
						transformedEssoRegion = essoregion_sub_doc.append(ESSO_REGION_DOC, getRegionMetadataTransformation(essoRegionsList));
						
							updateQuery.append(SUB_DOC_ESSO_REGIONS,transformedEssoRegion);
					}
				}
			}
			
			// Commenting as it will get applied to aal content classes. Need a output binding change to all content classes
			//regions expansion
			/*if (getObject(document, REGIONS_DOC) != null) {
				// Product Transformation
				DBObject regions_sub_doc = getObject(document, REGIONS_DOC);
				if (regions_sub_doc != null) {
					BasicDBList regionsList = getList(regions_sub_doc, REGION_DOC);
					if (regions_sub_doc != null) {
						BasicDBObject region_sub_doc = new BasicDBObject();
						transformedRegion = region_sub_doc.append(REGION_DOC, getRegionMetadataTransformation(regionsList));
						
							updateQuery.append(SUB_DOC_REGIONS,transformedRegion);
					}
				}
			}*/			
			
			// renditions Transformation
			DBObject renditions = getObject(document, RENDITIONS_DOC);

			if (renditions != null) {
				BasicDBList renditionList = getList(renditions, RENDITION_DOC);
				if (renditionList != null) {
					//this.setHasAttachments(true);
					updateQuery.append(SUB_DOC_RENDITIONS,
							getRenditionTransformation(wItem, renditionList));
				}
			}
			// fileName Transformation
			String file_name = (String) document.get(FILE_NAME_DOC);

			if (file_name != "" && file_name != null) {
				//this.setHasAttachments(true);			
				updateQuery.append(SUB_DOC_FILE_NAME,
						getFilenameTransformation(wItem, file_name));
			}
			// region Transformation
			DBObject regions = getObject(document, REGIONS_DOC);
			if (regions != null) {
				BasicDBList regionList = getList(regions, REGION_DOC);
				if (regionList != null) {
					updateQuery.append(SUB_DOC_COUNTRIES,
							getRegionTransformation(regionList,COUNTRY_DOC));

				}
			}
			
			// esso region Transformation
			DBObject essoregions = getObject(document, ESSO_REGIONS_DOC);
			if (essoregions != null) {
				BasicDBList regionList = getList(essoregions, ESSO_REGION_DOC);
				if (regionList != null) {
					updateQuery.append(SUB_DOC_ESSO_COUNTRIES,
							getRegionTransformation(regionList, ESSO_COUNTRY_DOC));
				}
			}
			
			// faq region Transformation
			DBObject faqregions = getObject(document, FAQ_REGIONS_DOC);
			if (faqregions != null) {
				BasicDBList regionList = getList(faqregions, FAQ_REGION_DOC);
				if (regionList != null) {
					updateQuery.append(SUB_DOC_FAQ_COUNTRIES,
							getRegionTransformation(regionList, FAQ_COUNTRY_DOC));
					}
			}
			
			
			// component attachment fileName Transformation
			DBObject components = getObject(document, "components");
			if(components!=null){
				BasicDBList componentList = getList(components, "component");
				StringBuilder buildQuery;
				int icomponentIndex = 0;

				Iterator<?> componentss_itr = componentList.iterator();

				while(componentss_itr.hasNext()) {
					Object object =  componentss_itr.next();
					if(object!=null && !object.equals("") && object instanceof BasicDBObject){
						String components_file_name= (String)((BasicDBObject)object).get("file_name");
						if (components_file_name!=null && !components_file_name.equals("")) {
							buildQuery = new StringBuilder();
							buildQuery.append("document.components.component.");
							if (componentList.size() > 1) {
								buildQuery.append(icomponentIndex + DOT);
							}
							buildQuery.append("file_name");
							updateQuery.append(buildQuery.toString(),
									getComponentsFilenameTransformation(wItem, components_file_name.toString()));
						}
						DBObject com_regions_sub_doc = getObject((BasicDBObject)object, REGIONS_DOC);
						if (com_regions_sub_doc != null) {
							buildQuery = new StringBuilder();
							buildQuery.append("document.components.component.");
							
								BasicDBList compRegionsList = getList(com_regions_sub_doc,REGION_DOC);
								if (compRegionsList != null) {
									if (componentList.size() > 1) {
										buildQuery.append(icomponentIndex + DOT);
									}
									buildQuery.append(REGIONS_DOC+DOT+REGION_DOC);
									updateQuery.append(buildQuery.toString(),getRegionMetadataTransformation(compRegionsList));
							}
						}
						icomponentIndex++;
					}
				}
				

			}

			
		}
		// Metadata Transformation for SOAR
		else if (getObject(result, SOAR_SOFTWARE_FEED_DOC) != null) {

			DBObject soarsoftwarefeed = getObject(result,
					SOAR_SOFTWARE_FEED_DOC);

			if (soarsoftwarefeed != null) {
				DBObject collection = getObject(soarsoftwarefeed,
						SOAR_COLLECTION_DOC);
				if (collection != null) {
				//SMO : 7553
					//CR NO:10483
					String companyType = (String)collection.get(COMPANY_INFO);
					if(StringUtils.isEmpty(companyType))
					{
						//logger.debug("Company_info "+ companyType +" is an invalid value");
						//return null;
					companyType="";
					}
					//else
						setCompany_info(companyType);
					transformSAORMetadata(collection, wItem, updateQuery);
				}
			}
		} else {
			logger.debug("Sub ducument for " + wItem.getId() + "is null");
			//return null;
		}
		// append hasAttachment
		
		//Fix for CR 520 -  hasAttachment should be true if content.files has entry for docid
		//updateQuery.append(HAS_ATTACHMENT_FIELD, this.isHasAttachments());
		
		updateQuery.append(HAS_ATTACHMENT_FIELD, this.getHasAttachments(options));		
		
		/**
		 * Check for Document size after transformation. If it exceeds 16 MB, 
		 * remove products and faq_products from document and save it to gridFS 
		 * */
		this.tempDocumentSize += new BasicBSONEncoder().encode(updateQuery).length;
		//System.out.println("Document Size  : "+tempDocumentSize);
		
		if(this.tempDocumentSize > maxHierarchyDocSize){
			logger.info("Document "+wItem.getId()+ "Exceeding 16 MB size");
			this.isPartitioned=  true;
			updateQuery.append("isPartitioned", isPartitioned);
			
			updateQuery.removeField(SUB_DOC_FAQ_PRODUCTS);
			updateQuery.removeField(SUB_DOC_PRODUCTS);
			
			gridFSUpdateDocument.put(PRODUCTS_DOC, transformedProduct);
			gridFSUpdateDocument.put(FAQ_PRODUCTS_DOC, transformedFAQProduct);
			
			DBObject unsetObject = new BasicDBObject("$unset",new BasicDBObject(SUB_DOC_FAQ_PRODUCTS,1).append(SUB_DOC_PRODUCTS, 1));
			
			Options tempoptions = new Options();
			tempoptions.setContentType(wItem.getContentType());
			tempoptions.setDocid(wItem.getId());

			tempoptions.setMetadataDocument(unsetObject);

			contentDao.writeMetadataToTemp(tempoptions);
		}
		updateDocument.append("$set", updateQuery);
		//logger.debug(updateDocument);
		
		return updateDocument;

	}
	//SMO : UserStory#7553 
	//Description: new method added for deciding the content part of the url (hpicontent/hpecontent)
	private String findContentValue() {
		String companyType = getCompany_info();
		String content = null;
		content= "/"+companyType.toLowerCase()+company_content+"/";
		/*Not required as content will never be null
		 * if(StringUtils.isEmpty(content))
		{
			logger.debug("Content  "+ content +" is found to be invalid");
			return null;
		}*/ 
		return content;
		
	}

	/**
	 * Transform SOAR Metadata
	 * 
	 * @param collection
	 * @param wItem
	 * @param updateQuery
	 */
	public void transformSAORMetadata(DBObject collection, WorkItem wItem,
			BasicDBObject updateQuery) {

		StringBuilder buildQuery;

		DBObject col_attachments = getObject(collection, SOAR_ATTACHMENTS_DOC);
		if (col_attachments != null) {
			BasicDBList col_attachment_list = getList(col_attachments,
					SOAR_ATTACHMENT_DOC);
			int arrayIndex = 0;

			Iterator<?> col_attachments_itr = col_attachment_list.iterator();
			while (col_attachments_itr.hasNext()) {
				DBObject col_attachment = (DBObject) col_attachments_itr.next();
				// fileName Transformation
				String filename = (String) col_attachment
						.get(SOAR_FILENAME_DOC);

				if (filename != "" && filename != null) {
					//this.setHasAttachments(true);
					buildQuery = new StringBuilder();
					buildQuery.append(SOAR_SOFTWARE_FEED_DOC + DOT
							+ SOAR_COLLECTION_DOC + DOT + SOAR_ATTACHMENTS_DOC
							+ DOT + SOAR_ATTACHMENT_DOC + DOT);
					if (col_attachment_list.size() > 1) {
						buildQuery.append(arrayIndex + DOT);
					}
					buildQuery.append(SOAR_FILENAME_DOC);
					updateQuery.append(buildQuery.toString(),
							getFilenameTransformation(wItem, filename));
				}
				arrayIndex++;
			}
		}
		DBObject software_items = getObject(collection, SOAR_SOFTWARE_ITEMS_DOC);
		if (software_items != null) {
			BasicDBList software_item_list = getList(software_items,
					SOAR_SOFTWARE_ITEM_DOC);
			int itemIndex = 0;
			Iterator<?> itr = software_item_list.iterator();
			while (itr.hasNext()) {
				DBObject software_item = (DBObject) itr.next();
				
				String item_id= (String)software_item.get(SOAR_ITEM_ID);
				
				// Transform filename for item attachments
				DBObject item_attachments = getObject(software_item,
						SOAR_ATTACHMENTS_DOC);
				if (item_attachments != null) {
					BasicDBList item_attachment_list = getList(
							item_attachments, SOAR_ATTACHMENT_DOC);

					int itemAttachIndex = 0;

					Iterator<?> item_attachments_itr = item_attachment_list
							.iterator();

					while (item_attachments_itr.hasNext()) {
						DBObject item_attachment = (DBObject) item_attachments_itr
								.next();
						// fileName Transformation
						String filename = (String) item_attachment
								.get(SOAR_FILENAME_DOC);

						if ( filename !="" && filename != null) {
							//this.setHasAttachments(true);
								buildQuery = new StringBuilder();
								buildQuery.append(SOAR_SOFTWARE_FEED_DOC + DOT
										+ SOAR_COLLECTION_DOC + DOT
										+ SOAR_SOFTWARE_ITEMS_DOC + DOT
										+ SOAR_SOFTWARE_ITEM_DOC + DOT);
								if (software_item_list.size() > 1) {
									buildQuery.append(itemIndex + DOT);
								}
								buildQuery.append(SOAR_ATTACHMENTS_DOC + DOT
										+ SOAR_ATTACHMENT_DOC + DOT);
								if (item_attachment_list.size() > 1) {
									buildQuery.append(itemAttachIndex + DOT);
								}
								buildQuery.append(SOAR_FILENAME_DOC);
								updateQuery.append(
										buildQuery.toString(),
										getSOARFilenameTransformation(wItem,
												filename,item_id));
						}
						itemAttachIndex++;
					}
				}
				DBObject products_supported = getObject(software_item,
						SOAR_PRODUCTS_SUPPORTED_DOC);
				if (products_supported != null) {
					BasicDBList product_list = getList(products_supported,
							PRODUCT_DOC);
					int productArrayIndex = 0;
					Iterator<?> product_itr = product_list.iterator();
					while (product_itr.hasNext()) {
						DBObject product = (DBObject) product_itr.next();

						String productOid = (String) product.get(SOAR_OID_ATTR);
						if(productOid!="" && productOid!=null){
						//SMO : 7553	
						String company_content = findContentValue();
							buildQuery = new StringBuilder();
							
							buildQuery.append(SOAR_SOFTWARE_FEED_DOC + DOT
									+ SOAR_COLLECTION_DOC + DOT
									+ SOAR_SOFTWARE_ITEMS_DOC + DOT
									+ SOAR_SOFTWARE_ITEM_DOC + DOT);
							// for each software-item
							if (software_item_list.size() > 1) {
								buildQuery.append(itemIndex + DOT);
							}
							buildQuery.append(SOAR_PRODUCTS_SUPPORTED_DOC + DOT
									+ PRODUCT_DOC + DOT);
							// for each product
							//if (product_list.size() > 1) {
								buildQuery.append(productArrayIndex + DOT);
							//}
							buildQuery.append(SOAR_OID_ATTR);
							//SMO : 7553
							updateQuery.append(buildQuery.toString(),
									PRODUCT_MASTER_DB_NAME + company_content + productOid.toLowerCase());
						}
						productArrayIndex++;
					}
				}
				itemIndex++;
			}
			// the software item object must be a list to be able to transform it into right format
			// hence replacing the soar-item object with a list equivalent
			software_items.put(SOAR_SOFTWARE_ITEM_DOC,software_item_list);
		}
	}

	/**
	 * Method to check if the attachments exists for given document or not
	 * 
	 * @param wItem
	 * @return
	 * @throws MongoUtilsException
	 */
	private boolean getHasAttachments(Options options)
			throws MongoUtilsException {
		// Retrieve attachment list
		ArrayList<DBObject> attachmentList = contentDao
				.getAttachmentList(options);
		if (attachmentList.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @return the contentDao
	 */
	public ContentDAO getContentDao() {
		return contentDao;
	}

	/**
	 * @param contentDao
	 *            the contentDao to set
	 */
	public void setContentDao(ContentDAO contentDao) {
		this.contentDao = contentDao;
	}

	/**
	 * Transform Products
	 * 
	 * @param productList
	 * @return
	 */
	public DBObject getProductTransformation(BasicDBList productList) {
		BasicDBObject product = null;
		BasicDBList productsList = new BasicDBList();
		BasicDBObject projectref_sub_doc = null;
		BasicDBObject product_sub_doc = new BasicDBObject();
		//SMO : 7553
		String company_content = findContentValue();
		Iterator<?> itr = productList.iterator();
		while (itr.hasNext()) {
			Object productOid = itr.next();
			// logger.debug(productOid);
			if (productOid != null && productOid != ""
					&& !(productOid instanceof DBObject)) {

				product = new BasicDBObject();
				projectref_sub_doc = new BasicDBObject();

				projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
				//SMO : 7553
				projectref_sub_doc.put(XLINK_HREF_KEY, PRODUCT_MASTER_DB_NAME
						+ company_content + productOid.toString().toLowerCase());

				product.put(PROJECTREF, projectref_sub_doc);
				product.put(VALUE, productOid);
				productsList.add(product);
			} else {
				productsList.add((DBObject) productOid);
			}
		}
		product_sub_doc.append(PRODUCT_DOC, productsList);
		return product_sub_doc;

	}

	/**
	 * Transform Regions
	 * 
	 * @param regionList
	 * @return
	 * @throws MongoUtilsException 
	 * @throws OptionsException 
	 */
	public DBObject getRegionMetadataTransformation(BasicDBList sourceRegionsList) throws OptionsException, MongoUtilsException {
		
		BasicDBObject regionWithProjref = null;
		BasicDBList regionsList = new BasicDBList();
		BasicDBObject projectref_sub_doc = null;
		
		/*DBObject query = new BasicDBObject();
		query.put(REGION_NAME_FILED, new BasicDBObject("$in", sourceRegionsList));

		Options options = new Options();
		options.setContentType(REGION_DB_NAME);
		options.setQuery(query);
		
		String regionCollection = ConfigurationManager.getInstance()
				.getMappingValue(REGION_DB_NAME, REGION_COLLECTION_NAME);
		ArrayList<DBObject> regions = contentDao.getAllMetadata(
				regionCollection, options);
		 */
		String company_content = findContentValue();
		Iterator<?> itr = sourceRegionsList.iterator();
		
		while (itr.hasNext()) {
			String region = (String)itr.next();
			// logger.debug(productOid);
				regionWithProjref = new BasicDBObject();
				projectref_sub_doc = new BasicDBObject();

				projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
				
				//for (DBObject regionDocument : regions) {
					//DBObject regionObject = (DBObject) regionDocument.get(REGION_DOC);
					//if(regionObject!=null && regionObject.get("name")!=null && regionObject.get("name").toString().equalsIgnoreCase(region)){
						projectref_sub_doc.put(XLINK_HREF_KEY, REGION_DB_NAME
								+ company_content + Processor.regionListCache.get(region));
                        
						regionWithProjref.put(PROJECTREF, projectref_sub_doc);
						regionWithProjref.put(VALUE, region);
						regionsList.add(regionWithProjref);
					//}
				//}			
		}
		
		return regionsList;
	}
	
	/**
	 * Transform Renditions
	 * 
	 * @param wItem
	 * @param renditionList
	 * @return Transformed Renditions Object
	 */
	public DBObject getRenditionTransformation(WorkItem wItem,
			BasicDBList renditionList) {
		BasicDBObject renditions_doc = new BasicDBObject();
		BasicDBList renditonsList = new BasicDBList();
		DBObject rendition_sub_doc =null;
		DBObject projectref_sub_doc=null;
		String company_content = findContentValue();
		Iterator<?> itr = renditionList.iterator();
		while (itr.hasNext()) {
			rendition_sub_doc = (BasicDBObject) itr.next();
			
			projectref_sub_doc = new BasicDBObject();
			
			String value = (String) rendition_sub_doc.get(VALUE);
			// null check for value
			if (value != "" && value != null) {
				// Remove # field to add at the end
				
				rendition_sub_doc.removeField(VALUE);

				projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
				projectref_sub_doc
						.put(XLINK_HREF_KEY, wItem.getContentType() + company_content
								+ wItem.getId() + SLASH + value.toLowerCase());
				rendition_sub_doc.put(PROJECTREF, projectref_sub_doc);
				// Add # field
				rendition_sub_doc.put(VALUE, value);
			}
			renditonsList.add(rendition_sub_doc);
			
		}
		renditions_doc.append(RENDITION_DOC, renditionList);
		return renditions_doc;
	}

	/**
	 * File Name Transformation
	 * 
	 * @param wItem
	 * @param file_name
	 * @return transformed file_name sub document
	 */
	public DBObject getFilenameTransformation(WorkItem wItem, String file_name) {
		DBObject file_name_sub_doc = new BasicDBObject();
		DBObject projectref_sub_doc = new BasicDBObject();
		String company_content = findContentValue();
		projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
		if(!wItem.getContentType().equalsIgnoreCase(SOAR_DB_NAME) && file_name.endsWith(".xml")){
			projectref_sub_doc.put(XLINK_HREF_KEY, wItem.getContentType()+"content" + company_content
				+ wItem.getId());
		}else{
			projectref_sub_doc.put(XLINK_HREF_KEY, wItem.getContentType()+ company_content
					+ wItem.getId() + SLASH + file_name.toLowerCase());
		}
		file_name_sub_doc.put(PROJECTREF, projectref_sub_doc);
		file_name_sub_doc.put(VALUE, file_name);

		return file_name_sub_doc;

	}
	
	public DBObject getComponentsFilenameTransformation(WorkItem wItem, String file_name) {
		DBObject file_name_sub_doc = new BasicDBObject();
		DBObject projectref_sub_doc = new BasicDBObject();
		String company_content = findContentValue();
		projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
		projectref_sub_doc.put(XLINK_HREF_KEY, wItem.getContentType()+"content"+ company_content
				+ wItem.getId() + SLASH + file_name.toLowerCase());
		file_name_sub_doc.put(PROJECTREF, projectref_sub_doc);
		file_name_sub_doc.put(VALUE, file_name);

		return file_name_sub_doc;

	}
	/**
	 * File Name Transformation for SOAR Items
	 * 
	 * @param wItem
	 * @param file_name
	 * @return transformed file_name sub document
	 */
	public DBObject getSOARFilenameTransformation(WorkItem wItem, String file_name, String item_id) {
		DBObject file_name_sub_doc = new BasicDBObject();
		DBObject projectref_sub_doc = new BasicDBObject();
		String company_content = findContentValue();
		projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
		projectref_sub_doc.put(XLINK_HREF_KEY, wItem.getContentType() + company_content
				+ wItem.getId() + SLASH + item_id.toLowerCase()+UNDERSCORE+file_name.toLowerCase());

		file_name_sub_doc.put(PROJECTREF, projectref_sub_doc);
		file_name_sub_doc.put(VALUE, file_name);

		return file_name_sub_doc;

	}

	/**
	 * Region Transformation
	 * 
	 * @param result
	 * @return list of Countries
	 * @throws MongoUtilsException
	 * @throws OptionsException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DBObject getRegionTransformation(BasicDBList regionList, String subElement)
			throws OptionsException, MongoUtilsException {
		BasicDBList countryList = null;
		TreeSet country = new TreeSet();

		DBObject query = new BasicDBObject();
		query.put(REGION_NAME_FILED, new BasicDBObject("$in", regionList));

		Options options = new Options();
		options.setContentType(REGION_DB_NAME);
		options.setQuery(query);
		// logger.debug("query - "+query);

		String regionCollection = ConfigurationManager.getInstance()
				.getMappingValue(REGION_DB_NAME, REGION_COLLECTION_NAME);
		ArrayList<DBObject> regions = contentDao.getAllMetadata(
				regionCollection, options);

		for (DBObject regionDocument : regions) {
			DBObject region = (DBObject) regionDocument.get(REGION_DOC);

			if (region == null) {
				logger.debug("Region Information is not available for region : "
						+ regionDocument.get(ID_FIELD));
				return null;
			} else {
				DBObject countries_sub_doc = getObject(region, COUNTRIES_DOC);
				if (countries_sub_doc != null) {
					countryList = getList(countries_sub_doc, COUNTRY_DOC);
					country.addAll(countryList);
				}
			}
		}
		return new BasicDBObject(subElement, country);
	}

	/**
	 * @param dbObject
	 * @param key
	 * @return
	 */
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

	/**
	 * @param dbObject
	 * @param key
	 * @return list of DB Object
	 */
	private BasicDBList getList(DBObject dbObject, String key) {
		Object list = dbObject.get(key);
		if (list == null) {
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

	/**
	 * @returns the content_groups url
	 */
	
	/*public DBObject getCGSTransformation(WorkItem wItem) {
		DBObject cgs_sub_doc = new BasicDBObject();
		DBObject projectref_sub_doc = new BasicDBObject();
        	 projectref_sub_doc.put(XLINK_TYPE_KEY, XLINK_TYPE_VALUE);
        	 projectref_sub_doc.put(XLINK_HREF_KEY, CGS+ SLASH +"content"+ SLASH + wItem.getId());
		cgs_sub_doc.put(PROJECTREF, projectref_sub_doc);
		return cgs_sub_doc;
	}*/
	
	/**
	 * @return the hasAttachments
	 */
	public boolean isHasAttachments() {
		return hasAttachments;
	}

	/**
	 * @param hasAttachments the hasAttachments to set
	 */
	public void setHasAttachments(boolean hasAttachments) {
		this.hasAttachments = hasAttachments;
	}

	public static void main(String[] args) throws UnknownHostException,
			ProcessException, MongoUtilsException, OptionsException {
		System.setProperty("mongo.configuration", "config/mongo.properties");
		Processor.populateRegions();
		 //Mongo mongo = new Mongo("g2t1888c.austin.hp.com");
		Mongo mongo = new Mongo("localhost");
		DB db = mongo.getDB("generalpurpose");
		// db.authenticate("cdspdb", "cdspdb".toCharArray());
		DBCollection metadata_temp = db.getCollection("metadata_temp");
		DBObject obj = new BasicDBObject();
		obj.put("_id", "c50282886");
		obj.put("priority", 1);
		obj.put("eventType", "update");
		// CR NO:10483
		obj.put("lastModified" ,1384182852237L);
//		obj.put("_id", "col25498");
//		obj.put("priority", 0);	
//		obj.put("eventType", "update");
//		obj.put("lastModified" , 1428995919682L);
		QueueManager queMgr= new QueueManager();
//		queMgr.push(new ContentItem("library",0L, queMgr));
//		WorkItem item = new WorkItem("library", obj,queMgr);
		queMgr.push(new ContentItem("generalpurpose",0L, queMgr));
		WorkItem item = new WorkItem("generalpurpose", obj, queMgr);	
		//ContentItem item = new ContentItem();
		//item.load();
	//	System.out.println(metadata_temp.findOne(new BasicDBObject("_id",
	//			"c01977769")));
		MetadataTransformationAdapter metaAdp = new MetadataTransformationAdapter();
		metaAdp.evaluate(item);
		DBObject cursor =  metadata_temp.findOne(new BasicDBObject("_id",
				"c50282886"));
		File file = new File("C:\\FOLDER------D\\repository\\c50282886.json");
		FileOutputStream fop = null;
				try {
					fop = new FileOutputStream(file);

					fop.write(cursor.toString().getBytes());

					fop.write("\n".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	 

	
}
