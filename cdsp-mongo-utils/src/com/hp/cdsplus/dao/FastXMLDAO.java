package com.hp.cdsplus.dao;

import java.util.ArrayList;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author pamujulv
 *
 *enhancements to code to retrieve data for new elements
 *description, projectname, publicationdestinations, maincomponent, maincomponentdetails, symptom, symptomdetails, faqregions,
 *faqcountries, publicationdestinations,originaldocid, minorcomponent1, minorcomponent1details, minorcomponent2, minorcomponent2details
 * failurecode, productdivision, productcategory, contenttopic, contenttopicdetail, projectname, usertask, usertaskdetail
 */

public class FastXMLDAO {

	Set<String> documentKeyset;
	private static final String VALUE = "#";

	public Set<String> getDocumentKeyset(DBObject document) {
		documentKeyset= document.keySet();
		return documentKeyset;
	}
	
	public String getCount(DBObject document, String element,  String subelement ){
		Object object= document.get(element);
		Integer count=0;
		if(object!=null && !object.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)object;
			BasicDBList list= getList(dbo,subelement);
			count= list.size();
		}
		return count.toString();
	}

	public String getProductCount(DBObject document){
		return getCount(document,"products","product");
	}
	public String getFAQRegionCount(DBObject document){
		return getCount(document,"faq_regions","faq_region");
	}
	public String value(DBObject document, String element){
		return (document.get(element)== null ? "":document.get(element).toString());
	}

	public String url(DBObject document){
		return value(document,"url");
	}

	public String getFaqProductCount(DBObject document){
		return getCount(document,"faq_products","product");
	}

	public String getTotalProductCount(DBObject document){
		Integer count= Integer.valueOf(getProductCount(document))+ Integer.valueOf(getFaqProductCount(document));
		return count.toString();
	}

	public String getCharSet(DBObject document){
		return "utf-8";
	}

	public String getRObjectID(DBObject document){
		return value(document,"r_object_id");
	}

	//SMO : getting company information from document
	public String getCompany_info(DBObject document){
		return value(document,"company_info");
	}
	
	public String getPriority(DBObject document){
		return value(document,"priority");
	}

	public String getProductList(DBObject document){
		StringBuffer products= new StringBuffer();
		if(Integer.valueOf(getProductCount(document))>0){
			BasicDBObject productdbo = (BasicDBObject)document.get("products");
			BasicDBList productsList= getList(productdbo,"product");
			for (Object object : productsList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject product=(BasicDBObject)object;
					products.append((String)product.get(VALUE)).append(";");
				}
			}
		}
		return products.toString();
	}

	public String getFaqProductList(DBObject document){
		StringBuffer products= new StringBuffer();
		if(Integer.valueOf(getFaqProductCount(document))>0){
			BasicDBObject productdbo = (BasicDBObject)document.get("faq_products");
			BasicDBList productsList= getList(productdbo,"product");
			for (Object object : productsList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject product=(BasicDBObject)object;
					products.append((String)product.get(VALUE)).append(";");
				}
			}
		}
		return products.toString();
	}
	
	public String listValues(DBObject document, String element, String subelement){
		StringBuffer listValues= new StringBuffer();
		Object listValuesObject= document.get(element);
		if(listValuesObject!=null && !listValuesObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)listValuesObject;
			BasicDBList listValuesList= getList(dbo,subelement);
			for (Object object : listValuesList) {
				if(object!=null && !object.toString().equals("")){
					listValues.append(object).append(";");
				}
			}
		}
		return listValues.toString();
	}

	public String maincomponentdetails(DBObject document){
		return listValues(document, "main_component_details", "main_component_detail");
	}

	public String minorcomponent1(DBObject document){
		return value(document,"minor_component1");
	}

	public String productfunction(DBObject document){
		return value(document,"product_function");
	}

	public String productdivision(DBObject document){
		return value(document,"product_division");
	}

	public String productcategory(DBObject document){
		return value(document,"product_category");
	}	

	public String contenttopic(DBObject document){
		return value(document,"content_topic");
	}	

	public String productfunctiondetails(DBObject document){
		return listValues(document, "product_function_details", "product_function_detail");
	}

	public String failurecode(DBObject document){
		return value(document,"failure_code");
	}

	public String getProductLines(DBObject document){
		return listValues(document, "product_lines", "product");
	}

	// Getting product hiearchy
	public String getProductType(DBObject document){
		return listValues(document, "product_types", "product");
	}

	public String getMarketingCategory(DBObject document){
		return listValues(document, "product_marketing_categories", "product");
	}

	public String getMarketingSubCategory(DBObject document){
		return listValues(document, "product_marketing_subcategories", "product");
	}

	public String getProductBigSeries(DBObject document){
		return listValues(document, "product_big_series", "product");
	}

	public String getProductSeries(DBObject document){
		return listValues(document, "product_series", "product");
	}


	public String getProductName(DBObject document){
		return listValues(document, "product_names", "product");
	}

	public String getProductNumber(DBObject document){
		return listValues(document, "product_numbers", "product");
	}

	public String getProductSupportCategories(DBObject document){
		return listValues(document, "product_support_categories", "product");
	}

	public String getProductSupportSubCategories(DBObject document){
		return listValues(document, "product_support_subcategories", "product");
	}


	//get faq product hierarchy
	// Getting product hiearchy
	public String getfaqProductType(DBObject document){
		return listValues(document, "faq_product_types", "product");
	}

	public String getfaqMarketingCategory(DBObject document){
		return listValues(document, "faq_product_marketing_categories", "product");
	}

	public String getfaqMarketingSubCategory(DBObject document){
		return listValues(document, "faq_product_marketing_subcategories", "product");
	}

	public String getfaqProductBigSeries(DBObject document){
		return listValues(document, "faq_product_big_series", "product");
	}

	public String getfaqProductSeries(DBObject document){
		return listValues(document, "faq_product_series", "product");
	}


	public String getfaqProductName(DBObject document){
		return listValues(document, "faq_product_names", "product");
	}

	public String getfaqProductNumber(DBObject document){
		return listValues(document, "faq_product_numbers", "product");
	}

	public String getfaqProductSupportCategories(DBObject document){
		return listValues(document, "faq_product_support_categories", "product");
	}

	public String getfaqProductSupportSubCategories(DBObject document){
		return listValues(document, "faq_product_support_subcategories", "product");
	}


	public String getMSProductHierarchy(DBObject document){
		StringBuffer productsHiearchy= new StringBuffer();
		productsHiearchy.append(getProductType(document)).append(getMarketingCategory(document)).append(getMarketingSubCategory(document)).append(getProductBigSeries(document)).append(getProductSeries(document)).append(getProductName(document)).append(getProductNumber(document));
		return productsHiearchy.toString().replaceAll(",", ";");
	}

	public String getSupportProductHierarchy(DBObject document){
		StringBuffer productsHiearchy= new StringBuffer();
		productsHiearchy.append(getProductType(document)).append(getMarketingCategory(document)).append(getMarketingSubCategory(document)).append(getProductSupportCategories(document)).append(getProductSupportSubCategories(document)).append(getProductBigSeries(document)).append(getProductSeries(document)).append(getProductName(document)).append(getProductNumber(document));
		return productsHiearchy.toString().replaceAll(",", ";");
	}

	public String getFaqProductHierarchy(DBObject document){
		StringBuffer faqproductsHiearchy= new StringBuffer();
		faqproductsHiearchy.append(getfaqProductType(document)).append(getfaqMarketingCategory(document)).append(getfaqMarketingSubCategory(document)).append(getfaqProductSupportCategories(document)).append(getfaqProductSupportSubCategories(document)).append(getfaqProductBigSeries(document)).append(getfaqProductSeries(document)).append(getfaqProductName(document)).append(getfaqProductNumber(document));
		return faqproductsHiearchy.toString().replaceAll(",", ";");
	}

	public String document_type(DBObject document){
		return value(document,"document_type");
	}

	public String version_label(DBObject document){
		return value(document,"version_label");
	}

	public String eds_document(DBObject document){
		return value(document,"eds_document");
	}

	public String eds_client_type(DBObject document){
		return value(document,"eds_client_type");
	}

	public String eds_client_quot(DBObject document){
		return value(document,"eds_client_quot");
	}

	public String eds_gold_standard_flag(DBObject document){
		return value(document,"eds_gold_standard_flag");
	}

	public String addr_country_code(DBObject document){
		return value(document,"addr_country_code");
	}

	public String business_group(DBObject document){
		return value(document,"business_group");
	}

	public String cmg_name(DBObject document){
		return value(document,"cmg_name");
	}

	public String col_master_id(DBObject document){
		return value(document,"col_master_id");
	}

	public String collection_ref_update_date(DBObject document){
		return validateDate(value(document,"collection_ref_update_date"));
	}

	private String validateDate(String result) {
		if(result!=null && !result.equals("")){
			result= result+"Z";
		}
		return result;
	}

	public String collection_update_date(DBObject document){
		return validateDate(value(document,"collection_update_date"));
	}

	public String collection_update_user(DBObject document){
		return value(document,"collection_update_user");
	}

	public String collection_valid_flag(DBObject document){
		return value(document,"collection_valid_flag");
	}

	public String content_class(DBObject document){
		return value(document,"content_class");
	}

	public String future_disclosure_date(DBObject document){
		return validateDate(value(document,"future_disclosure_date"));
	}

	public String i_chronicle_id(DBObject document){
		return value(document,"i_chronicle_id");
	}

	public String inventory_management(DBObject document){
		return value(document,"inventory_management");
	}

	public String lifecycle_state_update_date(DBObject document){
		return validateDate(value(document,"lifecycle_state_update_date"));
	}
	public String lifecycle_state_update_user(DBObject document){
		return value(document,"lifecycle_state_update_user");
	}
	public String object_create_date(DBObject document){
		return validateDate(value(document,"object_create_date"));
	}
	public String owner_name(DBObject document){
		return value(document,"owner_name");
	}
	public String project_id(DBObject document){
		return value(document,"project_id");
	}

	public String project_name(DBObject document){
		return value(document,"project_name");
	}

	public String pub_media(DBObject document){
		return value(document,"pub_media");
	}

	public String pub_ref_update_date(DBObject document){
		return validateDate(value(document,"pub_ref_update_date"));
	}

	public String pub_page_size(DBObject document){
		return value(document,"pub_page_size");
	}

	public String content_url(DBObject document){
		return value(document,"content_url");
	}

	public String originaldocid(DBObject document){
		return value(document,"original_docid");
	}

	public String edsclientquote(DBObject document){
		return value(document,"eds_client_quote");
	}

	public String property_update_user(DBObject document){
		return value(document,"property_update_user");
	}
	public String r_object_type(DBObject document){
		return value(document,"r_object_type");
	}

	public String r_version_labels(DBObject document){
		return listValues(document,"r_version_labels","r_version_label");
	}

	public String eds_business_topics(DBObject document){
		return listValues(document,"eds_business_topics","eds_business_topic");
	}

	public BasicDBList regions(DBObject document){
		BasicDBList regionsList=new BasicDBList();
		Object regionsObject= document.get("regions");
		if(regionsObject!=null && !regionsObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)regionsObject;
			regionsList= getList(dbo,"region");
		}
		return regionsList;
	}

	public String regionslabel(DBObject document){
		return listValues(document,"regions","region");
	}

	public String countries(DBObject document){
		return listValues(document,"countries","country");
	}

	public String authors(DBObject document){
		return listValues(document,"authors","author");
	}

	public String search_keywords(DBObject document){
		return listValues(document,"search_keywords","search_keyword");
	}

	public String crm_asset_code(DBObject document){
		return value(document,"crm_asset_code");
	}
	public String cmg_acronym(DBObject document){
		return value(document,"cmg_acronym");
	}
	public String cmg_feedback_address(DBObject document){
		return value(document,"cmg_feedback_address");
	}
	public String private_document_flag(DBObject document){
		return value(document,"private_document_flag");
	}
	public String master_object_name(DBObject document){
		return value(document,"master_object_name");
	}
	public String language_label(DBObject document){
		return value(document,"language_label");
	}

	public String full_title(DBObject document){
		Object dbo= document.get("full_title");
		if(dbo!=null && !dbo.toString().equals("")){
			if(dbo instanceof DBObject)
				return ((BasicDBObject)dbo).get(VALUE).toString();
		}
		return "";
	}
	public String collection_full_title(DBObject document){
		return value(document,"collection_full_title");
	}
	public String property_update_date(DBObject document){
		return validateDate(value(document,"property_update_date"));
	}
	public String content_update_date(DBObject document){
		return validateDate(value(document,"content_update_date"));
	}

	public String collection_search_keywords(DBObject document){
		return listValues(document,"collection_search_keywords","collection_search_keyword");
	}

	public String product_levels(DBObject document){
		return listValues(document,"product_levels","product_level");
	}

	public BasicDBList audiences(DBObject document){
		BasicDBList audiencesList=new BasicDBList();
		Object regionslabelObject= document.get("audiences");
		if(regionslabelObject!=null && !regionslabelObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)regionslabelObject;
			audiencesList= getList(dbo,"audience");
		}
		return audiencesList;
	}

	public String extra_properties(DBObject document){
		StringBuffer extra_properties=new StringBuffer();
		Object extra_propertiesObject= document.get("extra_properties");
		if(extra_propertiesObject!=null && !extra_propertiesObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)extra_propertiesObject;
			BasicDBList extra_propertiesList= getList(dbo,"extra_property");
			for (Object object : extra_propertiesList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject extra_property=(BasicDBObject)object;
					extra_properties.append(extra_property.get("extra_property_name")).append(":");
					BasicDBObject dboject=((BasicDBObject)extra_property.get("extra_property_values"));
					if(dboject!=null)
						extra_properties.append(dboject.get("extra_property_value"));
				}
				extra_properties.append(";");
			}
		}
		return extra_properties.toString();
	}

	public String biz_defined_properties(DBObject document){
		StringBuffer biz_defined_properties=new StringBuffer();
		Object biz_defined_propertiesObject= document.get("biz_defined_properties");
		if(biz_defined_propertiesObject!=null && !biz_defined_propertiesObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)biz_defined_propertiesObject;
			BasicDBList biz_defined_propertiesList= getList(dbo,"biz_defined_property");
			for (Object object : biz_defined_propertiesList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject biz_defined_property=(BasicDBObject)object;
					biz_defined_properties.append(biz_defined_property.get("biz_defined_property_name")).append(":");
					BasicDBObject dboject=((BasicDBObject)biz_defined_property.get("biz_defined_property_values"));
					if(dboject!=null)
						biz_defined_properties.append(dboject.get("biz_defined_property_value"));
				}
				biz_defined_properties.append(";");
			}
		}
		return biz_defined_properties.toString();
	}


	public String subElementValue(DBObject document, String element, String subelement){
		StringBuffer subElementValue= new StringBuffer();
		Object object= document.get(element);
		if(object!=null && !object.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)object;
			String values=dbo.get(subelement).toString();
			values=values.replace('[', ' ');
			values=values.replace(']', ' ');
			values=values.replace('"',' ');
			String[] valuesArray = values.split(",");
			for (int i = 0; i < valuesArray.length; i++) {
				subElementValue.append(valuesArray[i].toString().trim()).append(";");
			}
		}
		return subElementValue.toString();
	}

	public String pub_controller(DBObject document){
		return subElementValue(document,"pub_controller","user_name");
	}

	public String printmanagementprograms(DBObject document){
		return subElementValue(document,"print_management_programs","print_management_program");
	}

	public String pub_manager(DBObject document){
		return subElementValue(document,"pub_manager","user_name");
	}

	public String pub_manager_backup(DBObject document){
		return subElementValue(document,"pub_manager_backup","user_name");
	}

	public String business_units(DBObject document){
		return subElementValue(document,"business_units","business_unit");
	}

	public String organization(DBObject document){
		return value(document,"organization");
	}

	public String smartflow_content_types(DBObject document){
		return subElementValue(document,"smartflow_content_types","smartflow_content_type");
	}

	public String pub_vendor_details(DBObject document){
		StringBuffer pub_vendor_details=new StringBuffer();
		Object pub_vendor_detailsObject= document.get("pub_vendor_details");
		if(pub_vendor_detailsObject!=null && !pub_vendor_detailsObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)pub_vendor_detailsObject;
			BasicDBList pub_vendor_detailList= getList(dbo,"pub_vendor_detail");
			for (Object object : pub_vendor_detailList) {
				if(object!=null && !object.toString().equals("")){
					if(object instanceof String){
						pub_vendor_details.append(object);
					}else if(object instanceof BasicDBObject ){
						BasicDBObject pub_vendor_detail=(BasicDBObject)object;
						pub_vendor_details.append("pubv_vendor").append(":").append(pub_vendor_detail.get("pubv_vendor")).append(";");
						pub_vendor_details.append("pubv_inventory_policy").append(":").append(pub_vendor_detail.get("pubv_inventory_policy")).append(";");
						pub_vendor_details.append("pubv_order_limit").append(":").append(pub_vendor_detail.get("pubv_order_limit")).append(";");
						pub_vendor_details.append("pubv_restriction").append(":").append(pub_vendor_detail.get("pubv_restriction")).append(";");
						pub_vendor_details.append("pubv_strict_hold_flag").append(":").append(pub_vendor_detail.get("pubv_strict_hold_flag")).append(";");
						pub_vendor_details.append("pubv_unit_code").append(":").append(pub_vendor_detail.get("pubv_unit_code")).append(";");
						pub_vendor_details.append("pubv_initial_air_quantity").append(":").append(pub_vendor_detail.get("pubv_initial_air_quantity")).append(";");
						pub_vendor_details.append("pubv_reorder_quantity").append(":").append(pub_vendor_detail.get("pubv_reorder_quantity")).append(";");
					}
				}
			}
		}
		return pub_vendor_details.toString();
	}

	public String renditions(DBObject document){
		StringBuffer renditions= new StringBuffer();
		Object renditionsObject= document.get("renditions");
		if(renditionsObject!=null && !renditionsObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)renditionsObject;
			BasicDBList renditionList= getList(dbo,"rendition");
			for (Object object : renditionList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject rendition=(BasicDBObject)object;
					renditions.append((String)rendition.get(VALUE)).append(";");
				}
			}
		}
		return renditions.toString();
	}

	public String col_id(DBObject document){
		return value(document,"col_id");
	}
	public String contenturl(DBObject document){
		return value(document,"contenturl");
	}
	public String content_version_date(DBObject document){
		return validateDate(value(document,"content_version_date"));
	}
	public String description(DBObject document){
		return value(document,"description");
	}
	public String disclosure_level(DBObject document){
		return value(document,"disclosure_level");
	}
	public String content_version(DBObject document){
		return value(document,"content_version");
	}
	public String feedback_address(DBObject document){
		return value(document,"feedback_address");
	}
	public String document_class(DBObject document){
		return value(document,"document_class");
	}
	public String language_code(DBObject document){
		return value(document,"language_code");
	}
	public String concentra_internal_id(DBObject document){
		return value(document,"concentra_internal_id");
	}
	public String creation_date(DBObject document){
		return validateDate(value(document,"creation_date"));
	}
	public String object_name(DBObject document){
		return value(document,"object_name");
	}
	public String original_filename(DBObject document){
		return value(document,"original_filename");
	}
	public String pub_air_program_flag(DBObject document){
		return value(document,"pub_air_program_flag");
	}
	public String pub_flag(DBObject document){
		return value(document,"pub_flag");
	}
	public String pub_inventory_scrap_count(DBObject document){
		return value(document,"pub_inventory_scrap_count");
	}
	public String pub_page_count(DBObject document){
		return value(document,"pub_page_count");
	}
	public String review_date(DBObject document){
		return validateDate(value(document,"review_date"));
	}
	public String collection_description(DBObject document){
		return value(document,"collection_description");
	}
	public String corporate_content_flag(DBObject document){
		return value(document,"corporate_content_flag");
	}
	public String information_source(DBObject document){
		return value(document,"information_source");
	}

	public String customer_segment_codes(DBObject document){
		return subElementValue(document, "customer_segment_codes", "customer_segment_code");
	}

	public String business_function_codes(DBObject document){
		return subElementValue(document, "business_function_codes", "business_function_code");
	}

	public String buyer_role_codes(DBObject document){
		return subElementValue(document, "buyer_role_codes", "buyer_role_code");
	}

	public String customer_codes(DBObject document){
		return subElementValue(document, "customer_codes", "customer_code");
	}

	public String employee_count_codes(DBObject document){
		return subElementValue(document, "employee_count_codes", "employee_count_code");
	}

	public String industry_segment_codes(DBObject document){
		return subElementValue(document, "industry_segment_codes", "industry_segment_code");
	}

	public String industry_vertical_codes(DBObject document){
		return subElementValue(document, "industry_vertical_codes", "industry_vertical_code");
	}

	public String it_budget_codes(DBObject document){
		return subElementValue(document, "it_budget_codes", "it_budget_code");
	}

	public String job_responsibility_codes(DBObject document){
		return subElementValue(document, "job_responsibility_codes", "job_responsibility_code");
	}

	public String marketing_program_codes(DBObject document){
		return subElementValue(document, "marketing_program_codes", "marketing_program_code");
	}

	public String office_count_codes(DBObject document){
		return subElementValue(document, "office_count_codes", "office_count_code");
	}

	public String operating_system_codes(DBObject document){
		return subElementValue(document, "operating_system_codes", "operating_system_code");
	}

	public String partner_codes(DBObject document){
		return subElementValue(document, "partner_codes", "partner_code");
	}

	public String technicality_level_codes(DBObject document){
		return subElementValue(document, "technicality_level_codes", "technicality_level_code");
	}

	public String web_newsletter_codes(DBObject document){
		return subElementValue(document, "web_newsletter_codes", "web_newsletter_code");
	}

	public String partner_segment_codes(DBObject document){
		return subElementValue(document, "partner_segment_codes", "partner_segment_code");
	}

	public String active_flag(DBObject document){
		return value(document,"active_flag");
	}

	public String content_update_user(DBObject document){
		return value(document,"content_update_user");
	}

	public String is_translation_flag(DBObject document){
		return value(document,"is_translation_flag");
	}

	public String product_announcement_date(DBObject document){
		return validateDate(value(document,"product_announcement_date"));
	}

	public String product_release_date(DBObject document){
		return validateDate(value(document,"product_release_date"));
	}

	public String web_release_date(DBObject document){
		return validateDate(value(document,"web_release_date"));
	}

	public String content_topic_details(DBObject document){
		return subElementValue(document, "content_topic_details", "content_topic_detail");
	}

	public String document_type_details(DBObject document){
		return subElementValue(document, "document_type_details", "document_type_detail");
	}

	public String environments(DBObject document){
		return subElementValue(document, "environments", "environment");
	}

	public String content_type(DBObject document){
		return value(document,"content_type");
	}

	public String action_required(DBObject document){
		return value(document,"action_required");
	}

	public String publication_code(DBObject document){
		return value(document,"publication_code");
	}

	public String planned_public_date(DBObject document){
		return validateDate(value(document,"planned_public_date"));
	}

	public String custom_product_groups(DBObject document){
		return subElementValue(document, "custom_product_groups", "custom_product_group");
	}

	public String faq_custom_product_groups(DBObject document){
		return subElementValue(document, "faq_custom_product_groups", "faq_custom_product_group");
	}

	public String joint_product_collections(DBObject document){
		return subElementValue(document, "joint_product_collections", "joint_product_collection");
	}

	public String failure_code(DBObject document){
		return value(document,"failure_code");
	}

	public String has_valid_products(DBObject document){
		return value(document,"has_valid_products");
	}

	public String valid_flag(DBObject document){
		return value(document,"valid_flag");
	}

	public String clean_content_flag(DBObject document){
		return value(document,"clean_content_flag");
	}

	public String dpi_resolution(DBObject document){
		return value(document,"dpi_resolution");
	}

	public String pixel_height(DBObject document){
		return value(document,"pixel_height");
	}

	public String pixel_width(DBObject document){
		return value(document,"pixel_width");
	}

	public String publication_priority(DBObject document){
		return value(document,"publication_priority");
	}

	public String top_issue_expiry(DBObject document){
		return value(document,"top_issue_expiry");
	}

	public String top_issue_expiry_date(DBObject document){
		return validateDate(value(document,"top_issue_expiry_date"));
	}

	public String file_bytes(DBObject document){
		return value(document,"file_bytes");
	}

	public String cost_per_issue(DBObject document){
		return value(document,"cost_per_issue");
	}
	public String cost_per_unit(DBObject document){
		return value(document,"cost_per_unit");
	}
	public String labor_hours_paid(DBObject document){
		return value(document,"labor_hours_paid");
	}
	public String labor_minutes_paid(DBObject document){
		return value(document,"labor_minutes_paid");
	}
	public String labor_rate_per_hour(DBObject document){
		return value(document,"labor_rate_per_hour");
	}

	public String material_cost_per_unit(DBObject document){
		return value(document,"material_cost_per_unit");
	}

	public String action_expiry_date(DBObject document){
		return validateDate(value(document,"action_expiry_date"));
	}
	public String action_start_date(DBObject document){
		return validateDate(value(document,"action_start_date"));
	}
	public String effectivity_date(DBObject document){
		return validateDate(value(document,"effectivity_date"));
	}


	public String lastModified(DBObject document){
		return value(document,"lastModified");
	}
	

	public String components(DBObject document){
		StringBuffer components= new StringBuffer();
		Object listValuesObject= document.get("components");
		if(listValuesObject!=null && !listValuesObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)listValuesObject;
			BasicDBList listValuesList= getList(dbo,"component");
			for (Object object : listValuesList) {
				if(object!=null && !object.toString().equals("") && !object.toString().contains("\n")){
					BasicDBObject componentObject=(BasicDBObject)object;
					Object filename=componentObject.get("file_name");
					if(filename!=null && !filename.equals("")){
						components.append(((BasicDBObject)filename).get(VALUE)).append(";");
					}
				}
			}
		}
		return components.toString();
	}


	public String contentgroups(DBObject cgsdbObject){
		StringBuffer contentgroups= new StringBuffer();
		if(cgsdbObject!=null){
			DBObject groups = (DBObject)cgsdbObject.get("groups");
			ArrayList list  = (ArrayList) groups.get("group");
			for(int i =0 ; i < list.size(); i++){
				contentgroups.append(list.get(i)).append(";");
			}
		}
		return contentgroups.toString();
	}



	public boolean existsProducts(DBObject document){
		if(getDocumentKeyset(document).contains("products"))
			return true;
		return false;
	}

	public boolean existsFaqProducts(DBObject document){
		if(getDocumentKeyset(document).contains("faq_products"))
			return true;
		return false;
	}

	private BasicDBList getList(DBObject document, String key) {
		Object list = document.get(key);
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
	
	//For Taxonomy Changes
	public String taxonomy_categorizations(DBObject document){
		StringBuffer taxonomy_categorizations=new StringBuffer();
		Object taxonomy_categorizationsObject= document.get("taxonomy_categorizations");
		if(taxonomy_categorizationsObject!=null && !taxonomy_categorizationsObject.toString().equals("")){
			BasicDBObject dbo = (BasicDBObject)taxonomy_categorizationsObject;
			BasicDBList taxonomy_categorizationsList= getList(dbo,"taxonomy_categorization");
			for (Object object : taxonomy_categorizationsList) {
				if(object!=null && !object.toString().equals("")){
					BasicDBObject taxonomy_categorization=(BasicDBObject)object;
					taxonomy_categorizations.append(taxonomy_categorization.get("categorization_key")).append("=");
					taxonomy_categorizations.append(taxonomy_categorization.get("categorization_value"));
				}
				taxonomy_categorizations.append("||");
			}
		}
		return taxonomy_categorizations.toString();
	}
	
	public String projectName(DBObject document){
		return value(document, "project_name");
	}
		
	public String publicationDestinations(DBObject document){
		String destinations = listValues( document,  "publication_destinations", "publication_destination");
		return destinations;
	}
	
	
	
	public String mainComponent(DBObject document){
		return value(document, "main_component");
	}
	
	public String symptom(DBObject document){
		return value(document, "symptom");
	}
	
	public String symptomDetails(DBObject document){
		String details = listValues( document,  "symptom_details", "symptom_detail");
		return details;
	}
	
	public String faqRegions(DBObject document){

		//Change to retrieval of FAQ regions
		//String regions = listValues( document,  "faq_regions", "faq_region");
		//Retrieve the value at "# key" for document faq_region json
		StringBuffer regions= new StringBuffer();
		if(Integer.valueOf(getFAQRegionCount(document))>0){
			BasicDBObject regiondbo = (BasicDBObject)document.get("faq_regions");
			BasicDBList regionList= getList(regiondbo,"faq_region");
			for (Object object : regionList) {
				if(object!=null){
					BasicDBObject region=(BasicDBObject)object;
					String regionStr= (String)region.get(VALUE);
					if(!regionStr.equals(""))
					regions.append((String)region.get(VALUE)).append(";");
				}
			}
		}
		return regions.toString();
		//return regions;
	
	}
	
	public String faqCountries(DBObject document){
		String destinations = listValues( document,  "faq_countries", "faq_country");
		return destinations;
	}

	public String originalDocid(DBObject document){
		return value(document, "original_docid");
	}
	
	public String minorComponent1(DBObject document){
		return value(document, "minor_component1");
	}
	
	public String minorComponent1Details(DBObject document){
		String details = listValues( document,  "minor_component1_details", "minor_component1_detail");
		return details;
	}
	
	public String minorComponent2(DBObject document){
		return value(document, "minor_component2");
	}
	
	public String minorComponent2Details(DBObject document){
		String details = listValues( document,  "minor_component2_details", "minor_component2_detail");
		return details;
	}
		
	public String failureCode(DBObject document){
		return value(document, "failure_code");
	}
	
	public String productDivision(DBObject document){
		return value(document, "product_division");
	}
	
	public String productCategory(DBObject document){
		return value(document, "product_category");
	}
	
	public String contentTopic(DBObject document){
		return value(document, "content_topic");
	}
	
	public String contentTopicDetails(DBObject document){
		String details = listValues( document,  "content_topic_details", "content_topic_detail");
		return details;
	}
	
	public String userTask(DBObject document){
		return value(document, "user_task");
	}
	
	public String userTaskDetails(DBObject document){
		String details = listValues( document,  "user_task_details", "user_task_detail");
		return details;
	}
	
	public String originalSystem(DBObject document){
		return value(document, "original_system");
	}
	
}
