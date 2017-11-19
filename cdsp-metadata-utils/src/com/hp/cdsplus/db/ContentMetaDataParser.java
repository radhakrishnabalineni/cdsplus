package com.hp.cdsplus.db;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ContentMetaDataParser {
	
	static Logger LOG = Logger.getLogger(ContentMetaDataParser.class);
	
	DocumentBean docBean = null;
	boolean cursorStg = false;
	
	public DocumentBean parseDocument(String docId, DBCursor cursor, boolean cursorStg) {
		this.cursorStg = cursorStg;
		return parseDocument(docId, cursor); 
	}
	
	public DocumentBean parseDocument(String docId, DBCursor cursor) {
		
    	docBean = new DocumentBean();
    	docBean.setId(docId);
		if (cursor.hasNext()) {
			DBObject  obj = (DBObject)cursor.next();
			DBObject document =(DBObject) obj.get("document");
			if (document != null) {
    			DBObject products;
				try {
					products = (DBObject)document.get("products");
					if (products == null) {
						if(!cursorStg ) {
		    				docBean.setProductNull(true);
		    				docBean.setErrMsg(" Null Products");
		    				return docBean;
						} else {
	    					docBean.setParseFailed(true);
		    				docBean.setErrMsg(" Null Products");
		    				return docBean;
						}
					} 
					BasicDBList product = (BasicDBList)products.get("product");
	    			if (product != null) {
						if (!cursorStg) {
							for (Iterator<?> iterator = product.iterator(); iterator.hasNext();) {
								String tmp = iterator.next().toString();
								docBean.getPmoids().add(tmp.substring((tmp.indexOf("\"", 8)+1),tmp.lastIndexOf("\"")));
								//System.out.println(tmp.substring((tmp.indexOf("\"", 8)+1),tmp.lastIndexOf("\"")));
							}
						} else {
							for (Iterator<?> iterator01 = product.iterator(); iterator01.hasNext();) {
								String temp[] = iterator01.next().toString().split("_");
								if(temp.length>1) {
									docBean.getPmoids().add(temp[1]);
								}
							}							
						}
	    			} else {
    					docBean.setParseFailed(true);
	    				docBean.setErrMsg(" Null Product List");
	    				return docBean;
	    			}
				} catch (Exception e) {
					docBean.setParseFailed(true);
					docBean.setErrMsg(" Unknown Error");
					return docBean;
					//LOG.error("error while processing products for docid: " + docId);
				}
				if (document.get("document_type")!=null) {
					docBean.setDocType(document.get("document_type").toString()); 
					//LOG.info(document.get("document_type"));
				} else {
					docBean.setDocType("");
				}
				if (document.get("content_topic")!=null) {
					docBean.setContentTopic(document.get("content_topic").toString()); 
					//LOG.info(document.get("content_topic"));
				} else {
					docBean.setContentTopic("");
				}
				DBObject contentTopicDetails =(DBObject) document.get("content_topic_details");
				if (contentTopicDetails!= null && contentTopicDetails.get("content_topic_detail")!=null) {
					docBean.setContentTopicDetail(contentTopicDetails.get("content_topic_detail").toString());
					//LOG.info(contentTopicDetails.get("content_topic_detail"));
				} else {
					docBean.setContentTopicDetail("");
				}
			} else {
				docBean.setParseFailed(true);
				docBean.setErrMsg(" Null Document Obj");
				//LOG.error("document is null for docid: " + docId);
			}
		} else {
			docBean.setParseFailed(true);
			docBean.setErrMsg(" Cursor Error");
		}
		return docBean;	
	}
}
