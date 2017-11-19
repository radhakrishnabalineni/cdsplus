package com.hp.cdsplus.web.resource;

public class ProcessorInstance {

	String _id;
	String servername;
	String installationlocation;
	String isrunning;
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getServername() {
		return servername;
	}
	public void setServername(String servername) {
		this.servername = servername;
	}
	public String getInstallationlocation() {
		return installationlocation;
	}
	public void setInstallationlocation(String installationlocation) {
		this.installationlocation = installationlocation;
	}
	public String getIsrunning() {
		return isrunning;
	}
	public void setIsrunning(String isrunning) {
		this.isrunning = isrunning;
	}
	
}

