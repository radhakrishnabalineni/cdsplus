package com.hp.cdsplus.web.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.hp.cdsplus.web.model.ServiceDelegateBO.RequestMethod;


/**
 * @author reddypm
 *
 */
@XmlRootElement(name = "WebOptions")
public class WebOptions {



	/**
	 * 
	 */

	private String base;
	private int maxLevel;
	private String baseUri;
	private String wildCard;
	private RequestMethod requestMethod;

	/**
	 * @return the wildCard
	 */
	public String getWildCard() {
		return wildCard;
	}

	/**
	 * @param wildCard the wildCard to set
	 */
	public void setWildCard(String wildCard) {
		this.wildCard = wildCard;
	}

	/**
	 * @return the base
	 */
	public String getBase() {
		return base;
	}

	/**
	 * @param base
	 *            the base to set
	 */
	public void setBase(String base) {
		this.base = base;
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel
	 *            the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * @return the baseUri
	 */
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * @param baseUri the baseUri to set
	 */
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}
	
}
