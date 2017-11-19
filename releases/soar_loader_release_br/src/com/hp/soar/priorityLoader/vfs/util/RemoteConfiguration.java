/**
 * 
 */
package com.hp.soar.priorityLoader.vfs.util;

/**
 * 
 * @author Vijaya Bhaskar
 * @version 1.0
 */
public class RemoteConfiguration {

	private String className = null;
	private String prefix = null;
	private String serverOid = null;
	private String zbd = null;
	private String userName = null;
	private String serverUrl = null;
	private String password = null;
	private String sshKey = null;
	private String passphrase = null;
	private int numWorkerThreads = 0;

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the serverOid
	 */
	public String getServerOid() {
		return serverOid;
	}

	/**
	 * @param serverOid
	 *            the serverOid to set
	 */
	public void setServerOid(String serverOid) {
		this.serverOid = serverOid;
	}

	/**
	 * @return the zbd
	 */
	public String getZbd() {
		return zbd;
	}

	/**
	 * @param zbd
	 *            the zbd to set
	 */
	public void setZbd(String zbd) {
		this.zbd = zbd;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the serverUrl
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @param serverUrl
	 *            the serverUrl to set
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the sshKey
	 */
	public String getSshKey() {
		return sshKey;
	}

	/**
	 * @param sshKey
	 *            the sshKey to set
	 */
	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}

	/**
	 * @return the passphrase
	 */
	public String getPassphrase() {
		return passphrase;
	}

	/**
	 * @param passphrase
	 *            the passphrase to set
	 */
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	/**
	 * @return the numWorkerThreads
	 */
	public int getNumWorkerThreads() {
		return numWorkerThreads;
	}

	/**
	 * @param numWorkerThreads
	 *            the numWorkerThreads to set
	 */
	public void setNumWorkerThreads(int numWorkerThreads) {
		this.numWorkerThreads = numWorkerThreads;
	}

}
