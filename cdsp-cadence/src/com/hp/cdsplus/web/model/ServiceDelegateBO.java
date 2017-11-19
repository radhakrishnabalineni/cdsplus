package com.hp.cdsplus.web.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.hp.cdsplus.dao.Options;

@XmlRootElement(name = "ServiceDelegateBO") 

public class ServiceDelegateBO {

	private WebOptions webOptions;
	private Options options;
	public enum RequestMethod {
		GET,
		PUT,
		POST
	};
	/**
	 * @return the webOptions
	 */
	public WebOptions getWebOptions() {
		return webOptions;
	}

	/**
	 * @param set webOptions
	 */
	public void setWebOptions(WebOptions webOptions) {
		this.webOptions = webOptions;
	}

	/**
	 * @return the options
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * @param set webOptions
	 */
	public void setOptions(Options options) {
		this.options = options;
	}
}
