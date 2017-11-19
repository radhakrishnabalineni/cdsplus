package com.hp.cdsplus.dao;





import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public enum ProductLevel {
	PRODUCT_TYPE, SUPPORT_CATEGORY, MARKETING_CATEGORY, MARKETING_SUBCATEGORY, PRODUCT_BIGSERIES, PRODUCT_SERIES, PRODUCT_NAME, PRODUCT_NUMBER, SUPPORT_SUBCATEGORY;
	
	public static final String PRODUCT_SERIES_OID_FIELD ="PRODUCT_SERIES_OID";
	public static final String PRODUCT_BIGSERIES_OID_FIELD = "PRODUCT_BIGSERIES_OID";
	public static final String PRODUCT_NUMBER_NAME_FIELD = "PRODUCT_NUMBER_NAME";
	public static final String PRODUCT_LINE_CODE_FIELD = "PRODUCT_LINE_CODE";
	public static final String PRODUCT_NAME_OID_FIELD ="PRODUCT_NAME_OID";
	public static final String SUPPORT_CATEGORY_OID_FIELD = "SUPPORT_CATEGORY_OID";
	public static final String SUPPORT_SUBCATEGORY_OID_FIELD="SUPPORT_SUBCATEGORY_OID";
	public static final String MARKETING_CATEGORY_OID_FIELD="MARKETING_CATEGORY_OID";
	public static final String MARKETING_SUBCATEGORY_OID_FIELD="MARKETING_SUBCATEGORY_OID";
	public static final String PRODUCT_TYPE_OID_FIELD = "PRODUCT_TYPE_OID";
	public static final String PRODUCT_NUMBER_OID_FIELD = "PRODUCT_NUMBER_OID";
	public static final String SUPPORT_NAME_OID_FIELD = "SUPPORT_NAME_OID";
	public  DBObject getChildLevelsFields(){
		
		DBObject dispFields = new BasicDBObject();
		
		switch(this){
		
		case PRODUCT_TYPE:
			dispFields.put(SUPPORT_CATEGORY_OID_FIELD,1);
			dispFields.put(MARKETING_CATEGORY_OID_FIELD,1);
			break;
		case SUPPORT_CATEGORY:
			dispFields.put(SUPPORT_SUBCATEGORY_OID_FIELD,1);
			break;
		case SUPPORT_SUBCATEGORY:
			dispFields.put(PRODUCT_BIGSERIES_OID_FIELD,1);
			break;
		case MARKETING_CATEGORY:
		dispFields.put(MARKETING_SUBCATEGORY_OID_FIELD,1);
			break;
		case MARKETING_SUBCATEGORY:
			dispFields.put(PRODUCT_BIGSERIES_OID_FIELD,1);
			break;
		case PRODUCT_BIGSERIES:
			dispFields.put(PRODUCT_SERIES_OID_FIELD,1);
			break;
		case PRODUCT_SERIES:
			dispFields.put(PRODUCT_NAME_OID_FIELD,1);
			break;
		case PRODUCT_NAME:
			dispFields.put(PRODUCT_NUMBER_OID_FIELD,1);
			break;
		case PRODUCT_NUMBER:
		default:
			//dispFields=null;
			break;
		}
		return dispFields;
	}
	
	public DBObject getParentLevelFields(){
		
		DBObject dispFields = new BasicDBObject();
		
		switch(this){
		
		
		case SUPPORT_CATEGORY:
			dispFields.put(PRODUCT_TYPE_OID_FIELD,1);
			break;
		case SUPPORT_SUBCATEGORY:
			dispFields.put(SUPPORT_CATEGORY_OID_FIELD,1);
			break;
		case MARKETING_CATEGORY:
			dispFields.put(PRODUCT_TYPE_OID_FIELD,1);
			break;
		case MARKETING_SUBCATEGORY:
			dispFields.put(MARKETING_CATEGORY_OID_FIELD,1);
			break;
		case PRODUCT_BIGSERIES:
			dispFields.put(SUPPORT_SUBCATEGORY_OID_FIELD,1);
			dispFields.put(MARKETING_SUBCATEGORY_OID_FIELD,1);
			break;
		case PRODUCT_SERIES:
			dispFields.put(PRODUCT_BIGSERIES_OID_FIELD,1);
			break;
		case PRODUCT_NAME:
			dispFields.put(PRODUCT_SERIES_OID_FIELD,1);
			break;
		case PRODUCT_NUMBER:
			dispFields.put(PRODUCT_NAME_OID_FIELD,1);
			break;
		case PRODUCT_TYPE:
		default:
			//dispFields=null;
			break;
		}
		return dispFields;
	}
}