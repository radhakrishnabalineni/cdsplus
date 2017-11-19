package com.hp.soar.priorityLoader.vfs.util;

import org.w3c.dom.Element;

import com.hp.loader.utils.ConfigurationReader;
import com.hp.soar.priorityLoader.utils.LoaderLog;
import com.hp.soar.priorityLoader.workItem.SFTPDestination;

/**
 * 
 * @author Vijaya Bhaskar
 * @version 1.0
 */
public class RemoteConfigUtil {

	private static final String REMOTEDESTINATION = "remoteDestination";

	private static final String RDCLASSNAME = "class";
	private static final String SSHKEYFILE = "sshkey";
	private static final String SERVER = "server";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String ZEROBYTEDELETE = "zeroByteDelete";
	private static final String PASSPHRASE = "passphrase";
	private static final String SERVEROID = "serverOid";
	private static final String PREFIX = "prefix";

	private static final String SOAR_ELEMENT = "soar";
	private static final String NUM_WORKER_THREADS = "num_worker_threads";

	/**
	 * Parses the configurations from soarConfig.xml and stores the values in <code>RemoteConfiguration</code> object
	 * 
	 * @param config
	 *            input <code>ConfigurationReader</code> instance
	 * @return <code>RemoteConfiguration</code> instance
	 */
	public static synchronized RemoteConfiguration praseConfig(
			ConfigurationReader config) {

		RemoteConfiguration rc = null;

		Element rdElement = config.getElement(REMOTEDESTINATION);
		if (rdElement == null) {
			LoaderLog.error(REMOTEDESTINATION
					+ " not specified in config file.");
			throw new IllegalArgumentException(REMOTEDESTINATION
					+ " not specified in config file.");
		}

		rc = new RemoteConfiguration();

		String className = rdElement.getAttribute(RDCLASSNAME);

		if (className == null
				|| !className.equals(SFTPDestination.class.getName())) {
			LoaderLog.error(className
					+ " is not specified ic config file or is not supported");
			throw new IllegalArgumentException(className
					+ " is not specified ic config file or is not supported");
		} else {
			rc.setClassName(className);
		}

		String prefix = checkRequiredEntry(PREFIX,
				rdElement.getAttribute(PREFIX));

		rc.setPrefix(prefix);

		String serverOid = checkRequiredEntry(SERVEROID,
				rdElement.getAttribute(SERVEROID));

		rc.setServerOid(serverOid);

		String zbd = rdElement.getAttribute(ZEROBYTEDELETE);

		rc.setZbd(zbd);

		String userName = checkRequiredEntry(USERNAME,
				rdElement.getAttribute(USERNAME));

		rc.setUserName(userName);

		String serverUrl = checkRequiredEntry(SERVER,
				rdElement.getAttribute(SERVER));

		rc.setServerUrl(serverUrl);

		String password = rdElement.getAttribute(PASSWORD);
		String sshKey = rdElement.getAttribute(SSHKEYFILE);
		String passphrase = rdElement.getAttribute(PASSPHRASE);

		rc.setPassword(password);
		rc.setSshKey(sshKey);
		rc.setPassphrase(passphrase);

		Element soar = config.getElement(SOAR_ELEMENT);

		String numWorkerThreads = soar.getAttribute(NUM_WORKER_THREADS);

		try {
			int numThreads = Integer.parseInt(numWorkerThreads);
			rc.setNumWorkerThreads(numThreads);
		} catch (Exception e) {
			// set the default to 4
			rc.setNumWorkerThreads(4);
			String errMsg = LoaderLog
					.getExceptionMsgForLog(
							"Couldnt read/parse the soar-->num_worker_threads from the config : ",
							e);
			LoaderLog.error(errMsg);
		}

		return rc;
	}

	private static String checkRequiredEntry(String field, String x) {
		if (x == null || x.equals("")) {
			LoaderLog.error(" config file field: <" + field
					+ "> must have a value");
			throw new IllegalArgumentException(" config file field: <" + field
					+ "> must have a value");
		}
		return x;
	}

}
