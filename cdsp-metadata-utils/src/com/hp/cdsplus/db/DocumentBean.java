/**
 * 
 */
package com.hp.cdsplus.db;

import java.util.ArrayList;

/**
 * @author VinodPothuru
 * Date 03/18/2014
 *
 */
public class DocumentBean {

	private String id = "";
	private String docType = "";
	private String contentTopic = "";
	private String contentTopicDetail = "";
	private ArrayList<String> pmoids = new ArrayList<String>();
	private boolean parseFailed = false;
	private boolean productNull = false;
	private String errMsg = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getContentTopic() {
		return contentTopic;
	}
	public void setContentTopic(String contentTopic) {
		this.contentTopic = contentTopic;
	}
	public String getContentTopicDetail() {
		return contentTopicDetail;
	}
	public void setContentTopicDetail(String contentTopicDetail) {
		this.contentTopicDetail = contentTopicDetail;
	}
	public boolean isParseFailed() {
		return parseFailed;
	}
	public void setParseFailed(boolean parseFailed) {
		this.parseFailed = parseFailed;
	}
	public boolean isProductNull() {
		return productNull;
	}
	public void setProductNull(boolean productNull) {
		this.productNull = productNull;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	public ArrayList<String> getPmoids() {
		return pmoids;
	}
}
