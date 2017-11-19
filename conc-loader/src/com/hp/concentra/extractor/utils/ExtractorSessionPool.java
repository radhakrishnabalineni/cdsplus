package com.hp.concentra.extractor.utils;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.hp.cks.concentra.core.session.ConcentraAdminSession;
import com.hp.cks.concentra.core.session.ConcentraSessionManager;
import com.hp.cks.concentra.core.session.SessionException;
import com.hp.cks.concentra.utils.StringOps;
import com.hp.loader.utils.ConfigurationReader;


/**
 * Singleton class which acts as a pool of docbase sessions opened to XDMS docbase. 
 * Has methods for 
 * 1. retrieving session from session pool
 * 2. releasing session to session pool
 * 3. Retrieving new connection to docbase if there are no free connections
 *  
 * @author mariswam
 */
public class ExtractorSessionPool {

  private static final String MAXSESSIONREUSE = "max_session_reuse";
  
  private static int sessionId = 0;

  private static int MAX_SESSION_REUSE = 100;

  private static ExtractorSessionPool sessionPool = new ExtractorSessionPool();

  private Map<ConcentraAdminSession, SessionStats> usedPool = new HashMap<ConcentraAdminSession, SessionStats>();
  private LinkedList <SessionStats> freePool = new LinkedList<SessionStats>();

  private StringBuffer sb = new StringBuffer();

  private synchronized ConcentraAdminSession createSession() throws SessionException {
    ConcentraAdminSession adminSession = ConcentraSessionManager.getAdminSession();
    SessionStats stats = new SessionStats(adminSession);
    usedPool.put(adminSession, stats);
    LoaderLog.info(Thread.currentThread().getName()+" SESSION Create session: "+stats.getSessionStats()+" "+sessionStats());
    return adminSession;
  }

  /**
   * getSessionStats returns a string with stat information for the session pool.  The format is
   * FreePool: <cnt> UsedPool: <cnt> indexes  [<indexes on free list>]{indexes on used pool}
   * @return
   */
  private synchronized String sessionStats() {
    sb.setLength(0);
    sb.append(" FreePool: ").append(freePool.size()).append(" UsedPool: ").append(usedPool.size());
    sb.append(" indexes  [");
    for(SessionStats s: freePool) {
      sb.append(s.getSessionStats()).append(", ");
    }
    if (freePool.size() > 0) {
      sb.setLength(sb.length()-2);
    }
    sb.append("]{");
    for(ConcentraAdminSession id : usedPool.keySet()) {
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
   * @throws SessionException 
   */
  private synchronized ConcentraAdminSession getSession() throws SessionException {
    if (freePool.size() > 0) {
      SessionStats stats = freePool.removeFirst();
      usedPool.put(stats.adminSession, stats);
//      LoaderLog.debug(Thread.currentThread().getName()+" SESSION Free to Used "+stats.getSessionStats()  +"      "+sessionStats());
      return stats.adminSession;
    } else {
      return createSession();
    }
  }

  /**
   * releaseSession takes the session passed in and has it release itself.  If
   * the session had been used less than MAX_SESSION_REUSE, it will be put on the free list
   * otherwise it will be released
   * @param session
   */
  private synchronized void releaseSession(ConcentraAdminSession session) {
    // don't release a none acquired session
    if (session == null) {
      LoaderLog.info(Thread.currentThread().getName()+" SESSION Attempt to release null session");
      return;
    }
    SessionStats stats = usedPool.remove(session);
    if (stats != null) {
      stats.release();
    } else {
      // Houston we have a problem
      LoaderLog.error(Thread.currentThread().getName()+" SESSION ExtractorSessionPool: Attempt to release session not from the pool: "+session);
      // release the session as it was requested.
      session.release();
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
      LoaderLog.info(Thread.currentThread().getName()+" SESSION ExtractorSessionPool: "+usedPool.size()+" sessions are still in use!");
    }
  }

  static public synchronized void init(ConfigurationReader config) {
    String maxSessionReuse = config.getAttribute(MAXSESSIONREUSE);
    if (maxSessionReuse != null) {
      try {
        MAX_SESSION_REUSE = Integer.parseInt(maxSessionReuse);
      } catch (NumberFormatException nfe) {
      }
    } else {
      LoaderLog.warn(MAXSESSIONREUSE+" field is missing or not a number in configuration file.  Using default of "+MAX_SESSION_REUSE);
    }
    LoaderLog.info(MAXSESSIONREUSE+" field is "+MAX_SESSION_REUSE);
  }
  
  /**
   * @return - connections from session pool if there FREE connections available in pool. 
   * Else creates a new connection to docbase which will be released to docbase as soon 
   * as its activites completed.
   * @throws SessionException 
   */
  static public synchronized ConcentraAdminSession getDocbaseSession() throws SessionException {
    return sessionPool.getSession();
  }

  /**
   * Releases the session to session pool. If the connection was created as an extra connection, 
   * it is disconnected to docbase else its status is set to FREE and releases to session pool. 
   * @param docbaseSession - ConcentraAdminSession
   */
  static public synchronized void releaseDocbaseSession(ConcentraAdminSession docbaseSession){
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

    ConcentraAdminSession adminSession;
    int										count;
    int										id;

    private SessionStats(ConcentraAdminSession adminSession) {
      this.adminSession = adminSession;
      count = 0;
      this.id = sessionId++;
    }


    private int getSessionId() {
      return id;
    }
    
    /**
     * @return the sessionId
     */
    public String getSessionStats() {
      return id+":"+count;
    }


    /**
     * release session increments the usage counter and releases the session after 100 uses.
     */
    private void release() {
      if (++count < MAX_SESSION_REUSE) {
        freePool.addLast(this);
//        LoaderLog.debug(Thread.currentThread().getName()+" SESSION Used to Free "+id+"      "+sessionStats());
      } else {
        adminSession.release();
//        LoaderLog.debug(Thread.currentThread().getName()+" SESSION Released     "+id+"      "+sessionStats());
      }
    }

    /**
     * releaseSession releases the admin session being held.
     */
    private void releaseSession() {
      adminSession.release();
      LoaderLog.info(Thread.currentThread().getName()+" SESSION ReleaseSession session :   "+id+sessionStats());
    }
  }

  private class SessionRunner implements Runnable {
    ConcentraAdminSession session;
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
          session = ExtractorSessionPool.getDocbaseSession();
          // process 0-20 seconds
          sleepTime = System.currentTimeMillis() % 20000;
          Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
        } catch (SessionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          ExtractorSessionPool.releaseDocbaseSession(session);
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
    LinkedList<ConcentraAdminSession> sessionList = new LinkedList<ConcentraAdminSession>();
    try {
      for(int i=0;i<8;i++) {
        sessionPool.new SessionRunner(i);
      }
      while(true) {
        //System.out.println(sessionPool.getSessionStats());
        try {
          Thread.sleep(10000);
        } catch (InterruptedException ie) {}
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
