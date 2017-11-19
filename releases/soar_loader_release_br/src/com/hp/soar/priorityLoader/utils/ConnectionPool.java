/**
 * 
 */
package com.hp.soar.priorityLoader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.hp.loader.utils.ConfigurationReader;

/**
 * @author dahlm
 *
 */
public class ConnectionPool {

	private static final String MAXSESSIONREUSE = "max_session_reuse";
	public static final String DOCBASE_FILE = "docbase_properties_file";
	public static final String DOCBASE_OWNER = "DOCBASE_OWNER";
	public static final String DOCBASE_PASSWORD = "DOCBASE_PASSWORD";
	public static final String DMS_DOCBASE_NAME = "DOCBASE";
	public static final String DOCBASE_PROPERTY = "docbase_property";

	private static final String ERRORMSG = " is not specified in config file";
	private static int sessionId = 0;

	private static int MAX_SESSION_REUSE = 100;
	private static String docBasename = null;
	private static String docbaseOwner = null;
	private static String docbasePassword = null;

	private static ConnectionPool sessionPool = new ConnectionPool();

	private Map<IDfSession, SessionStats> usedPool = new HashMap<IDfSession, SessionStats>();
	private LinkedList <SessionStats> freePool = new LinkedList<SessionStats>();

	private StringBuffer sb = new StringBuffer();
	private boolean exiting = false;
	
	public static synchronized void init(ConfigurationReader config) throws IllegalArgumentException {

		String docBaseFile = config.getAttribute(DOCBASE_FILE);
		File f = new File(docBaseFile);
		if (!f.canRead()) {
			throw new IllegalArgumentException("Can't read docbase config file.");
		}

		String docBaseProperty = config.getAttribute(DOCBASE_PROPERTY);
		if (docBaseProperty == null || docBaseProperty.length() == 0) {
			throw new IllegalArgumentException(DOCBASE_PROPERTY+" not specified in "+docBaseFile);
		}

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(f));
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Failed to read docbase config file",ioe);
		}

		// set the docbaseName/userName/password for the sessions
		String tmp = config.getAttribute(MAXSESSIONREUSE);
		if ((tmp != null) && (tmp.length() > 0)) {
			try {
				MAX_SESSION_REUSE = Integer.parseInt(tmp);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(MAXSESSIONREUSE+" is not a number");
			}
		}

		docBasename = props.getProperty(docBaseProperty);
		if ((docBasename == null) || (docBasename.length() == 0)) {
			throw new IllegalArgumentException(DMS_DOCBASE_NAME+ERRORMSG);
		}

		docbaseOwner = props.getProperty(DOCBASE_OWNER);
		if ((docbaseOwner == null) || (docbaseOwner.length() == 0)) {
			throw new IllegalArgumentException(DOCBASE_OWNER+ERRORMSG);
		} 

		docbasePassword = props.getProperty(DOCBASE_PASSWORD);
		if ((docbasePassword== null) || (docbasePassword.length() == 0)) {
			throw new IllegalArgumentException(DOCBASE_PASSWORD+ERRORMSG);
		} 

		//  Verify that the config is good. 
		//  Find the dfc.properties file and get the docbase server out of it
		Properties dfcProps = new Properties();
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			InputStream is = cl.getResourceAsStream("dfc.properties"); 
			dfcProps.load(is);
		} catch (Exception ioe) {
			throw new IllegalArgumentException("Failed to load dfc.properties file.", ioe);
		}

		String docBroker = dfcProps.getProperty("dfc.docbroker.host[0]");

		// Get configured docbroker
		String sourceBroker = config.getAttribute("docbroker_host");
		if (docBroker == null || (!docBroker.equals(sourceBroker))) {
			throw new IllegalArgumentException("Configuration Error: dfc.docbroker.host[0] in dfc.properties <"+docBroker+"> does not match expected docbroker <"+sourceBroker+">");
		}

	}

	/**
	 * createSession creates a connection to the docbase using the docbaseOwner and password
	 * @return
	 * @throws DfException if session creation fails
	 */
	private synchronized IDfSession createSession() throws DfException {
		long delayInSeconds = 2;
		IDfLoginInfo li = new DfLoginInfo();
		li.setUser(docbaseOwner);
		li.setPassword(docbasePassword);
		li.setDomain("");
		IDfSession session = null; 
		IDfClient dfc = DfClient.getLocalClient();
		while (session == null) {
			try {
				session = dfc.newSession(docBasename, li);
			} catch (DfException dfe) {
				if (exiting) {
					LoaderLog.error(LoaderLog.getExceptionMsgForLog("Failed to get docbase session. ", dfe));
					// bail out we are exiting
					throw dfe;
				}
				LoaderLog.error(LoaderLog.getExceptionMsgForLog("Failed to get docbase session. Retry in "+delayInSeconds+" sec. ", dfe));
				try {
					Thread.sleep(delayInSeconds*1000);
				} catch (InterruptedException ie) {
				}
				delayInSeconds *= 2;
				// max delay is 5 minutes
				delayInSeconds = delayInSeconds > 300 ? delayInSeconds : 300;
			}
		}
		
		SessionStats stats = new SessionStats(session);
		usedPool.put(session, stats);
		return session;
	}

	/**
	 * getSessionStats returns a string with stat information for the session pool.  The format is
	 * FreePool: <cnt> UsedPool: <cnt> indexes  [<indexes on free list>]{indexes on used pool}
	 * @return
	 */
	private synchronized String sessionStats() {
		sb.setLength(0);
		sb.append("FreePool: ").append(freePool.size()).append(" UsedPool: ").append(usedPool.size());
		sb.append(" indexes  [");
		for(SessionStats s: freePool) {
			sb.append(s.getSessionStats()).append(", ");
		}
		if (freePool.size() > 0) {
			sb.setLength(sb.length()-2);
		}
		sb.append("]{");
		for(IDfSession id : usedPool.keySet()) {
			SessionStats s = usedPool.get(id);
			sb.append(s.getSessionStats()).append(", ");
		}
		if (usedPool.size() > 0) {
			sb.setLength(sb.length()-2);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * getSession returns the next free session from the free list, if no free sessions are available
	 * @return
	 * @throws DfException 
	 */
	private synchronized IDfSession getSession() throws DfException {
		if (freePool.size() > 0) {
			SessionStats stats = freePool.removeFirst();
			usedPool.put(stats.session, stats);
			//				System.out.println("Free - : "+stats.getSessionId()+sessionStats());
			return stats.session;
		} else {
			return createSession();
		}
	}

	/**
	 * releaseSession takes the session passed in and has it release itself.  If
	 * the session had been used less than MAX_SESSION_REUSE, it will be put on the free list
	 * otherwise it will be released
	 * @param session
	 * @throws DfException 
	 */
	private synchronized void releaseSession(IDfSession session) throws DfException {
		// don't release a none acquired session
		if (session == null) {
			return;
		}
		SessionStats stats = usedPool.remove(session);
		if (stats != null) {
			stats.release();
		} else {
			// Houston we have a problem
			LoaderLog.error("ExtractorSessionPool: Attempt to release session not from the pool: "+session);
			// release the session as it was requested.
			session.disconnect();
		}
	}

	/**
	 * releaseEverySession causes all free sessions to be released
	 */
	private synchronized void releaseEverySession() {
		while(freePool.size() > 0) {
			SessionStats ss = freePool.removeFirst();
			ss.releaseSession();
		}
		if (usedPool.size() > 0) {
			LoaderLog.info("ExtractorSessionPool: "+usedPool.size()+" sessions are still in use!");
		}
	}

	/**
	 * exiting sets the exiting flag to break out of a connection attempt
	 */
	static public void exiting() {
		sessionPool.exiting = true;
	}
	
	/**
	 * @return - connections from session pool if there FREE connections available in pool. 
	 * Else creates a new connection to docbase which will be released to docbase as soon 
	 * as its activites completed.
	 * @throws DfException 
	 */
	static public synchronized IDfSession getDocbaseSession() throws DfException {
		return sessionPool.getSession();
	}

	/**
	 * Releases the session to session pool. If the connection was created as an extra connection, 
	 * it is disconnected to docbase else its status is set to FREE and releases to session pool. 
	 * @param docbaseSession - ConcentraAdminSession
	 * @throws DfException 
	 */
	static public synchronized void releaseDocbaseSession(IDfSession docbaseSession) throws DfException{
		sessionPool.releaseSession(docbaseSession);
	}

	/**
	 * Disconnects all open collections from docbase.
	 * This method will be called at the shutown of the servlet
	 */
	public static void releaseAllSessions(){
		sessionPool.releaseEverySession();
	}

	public static String getSessionStats() {
		return sessionPool.sessionStats();
	}

	/**
	 * SessionStats keeps the adminSession and the count of the number of times the session
	 * has been used
	 * @author dahlm
	 *
	 */
	private class SessionStats {

		IDfSession session;
		int				 count;
		int				 id;

		private SessionStats(IDfSession session) {
			this.session = session;
			count = 0;
			this.id = sessionId++;
		}


		/**
		 * @return the sessionId
		 */
		public String getSessionStats() {
			return id+":"+count;
		}


		/**
		 * release session increments the usage counter and releases the session after 100 uses.
		 * @throws DfException 
		 */
		private void release() throws DfException {
			if (++count < MAX_SESSION_REUSE) {
				freePool.addLast(this);
				//					System.out.println("Free + : "+id+sessionStats());
			} else {
				session.disconnect();
				//					out.println("Released : "+id+sessionStats());
			}
		}

		/**
		 * releaseSession releases the admin session being held.
		 */
		private void releaseSession() {
			if (session != null) {
				try {
					System.out.println("Disconnecting from docbase");
					session.disconnect();
				} catch (DfException e) {
				}
			}
		}
	}

	private class SessionRunner implements Runnable {
		IDfSession session;
		long sleepTime;
		/**
		 * testRunner class.  Simulates an actual method by getting a connection and
		 * holding it a random time between 0 and 60 seconds.
		 * @param runIdx
		 */
		private SessionRunner(int runIdx) {
			Thread t = new Thread(this, "Runner-"+runIdx);
			t.start();
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			while(true) {
				try {
					session = ConnectionPool.getDocbaseSession();
					// process 0-20 seconds
					sleepTime = System.currentTimeMillis() % 20000;
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {
				} catch (DfException e) {
					System.out.println("Docbase connection failed");
					e.printStackTrace();
				} finally {
					try {
						ConnectionPool.releaseDocbaseSession(session);
					} catch (DfException e) {
						System.out.println("Docbase release failed");
						e.printStackTrace();
					}
				}

				// time to wait between requests
				try {
					// between 0-2 seconds
					sleepTime = System.currentTimeMillis() % 2000;
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {}
			}
		}
	}

	public static void main(String[] argv) {
		LinkedList<IDfSession> sessionList = new LinkedList<IDfSession>();
		try {
			for(int i=0;i<8;i++) {
				sessionPool.new SessionRunner(i);
			}
			while(true) {
				System.out.println(sessionPool.getSessionStats());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
