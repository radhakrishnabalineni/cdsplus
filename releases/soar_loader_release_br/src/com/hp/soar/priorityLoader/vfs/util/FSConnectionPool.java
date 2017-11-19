package com.hp.soar.priorityLoader.vfs.util;

import java.util.Stack;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

import com.hp.soar.priorityLoader.utils.LoaderLog;

/**
 * 
 * @author Vijaya Bhaskar
 * @version 1.0
 */
public class FSConnectionPool {

	private static FSConnectionPool pool = null;
	private static Stack<StandardFileSystemManager> sfsMgrPool = new Stack<StandardFileSystemManager>();
	private static Object mutex = new Object();

	private static int currConnCount = 0;

	private static int numWorkerThreads = 0;

	private FSConnectionPool() {
	}

	/**
	 * Synchronized method to return the Instance of the
	 * <code>FSConnectionPool</code>
	 * 
	 * @return
	 * @throws FileSystemException
	 */
	public static synchronized FSConnectionPool getInstance(
			RemoteConfiguration remtConfig) throws FileSystemException {

		if (LoaderLog.isDebugEnabled()) {
			LoaderLog.debug("Entering the getInstance method");
		}

		if (pool == null) {

			numWorkerThreads = remtConfig.getNumWorkerThreads();

			if (LoaderLog.isDebugEnabled()) {
				LoaderLog.debug("Number of worker threads are :"
						+ numWorkerThreads);
				LoaderLog
						.debug("================ Creating Pool ================");
			}

			synchronized (mutex) {
				pool = new FSConnectionPool();
			}

			if (LoaderLog.isDebugEnabled()) {
				LoaderLog.debug("Done Creating Pool.");
			}
		}

		while (currConnCount < numWorkerThreads) {

			try {
				StandardFileSystemManager fsMgr = new StandardFileSystemManager();

				fsMgr.init();
				sfsMgrPool.push(fsMgr);
				currConnCount++;
				if (LoaderLog.isDebugEnabled()) {
					LoaderLog.debug("Exiting the getInstance method");
				}

			} catch (FileSystemException e) {
				currConnCount--;
				LoaderLog.logStackTrace(e);
				throw e;
			}

		}

		if (LoaderLog.isDebugEnabled()) {
			LoaderLog.debug("Exiting the getInstance method");
		}

		return pool;
	}

	/**
	 * Gets <code>StandardFileSystemManager</code> instance from
	 * <code>FSConnectionPool</code>
	 * 
	 * @return <code>StandardFileSystemManager</code> instance
	 * @throws FileSystemException
	 *             in case of exception
	 */
	public StandardFileSystemManager getFSManager() throws FileSystemException {

		if (LoaderLog.isDebugEnabled()) {
			LoaderLog.debug("Entering the getFSManager method.");
		}

		StandardFileSystemManager fsMgr = null;

		synchronized (this) {

			while (sfsMgrPool.isEmpty()) {
				try {
					if (LoaderLog.isDebugEnabled()) {
						LoaderLog
								.info("Wait for other threads to complete, or 1 sec. This will be invoked after returning the unused FS Manager to the pool.");
					}

					this.wait();

				} catch (InterruptedException e) {
					LoaderLog.logStackTrace(e);
				}
			}

			if (LoaderLog.isDebugEnabled()) {
				LoaderLog.debug("StandardFileSystemManager pool size now : "
						+ sfsMgrPool.size());
			}

			fsMgr = sfsMgrPool.pop();

		}
		return fsMgr;

	}

	/**
	 * Returns the <code>FileSystemManager</code> instance to the pool
	 * 
	 * @param fsMgr
	 *            <code>FileSystemManager</code>
	 */
	public void returnSFSManager(FileSystemManager fsMgr) {

		if (LoaderLog.isDebugEnabled()) {
			LoaderLog.debug("Entering the returnSFSManager method");
		}

		try {
			if (fsMgr instanceof StandardFileSystemManager) {

				StandardFileSystemManager sfsMgr = (StandardFileSystemManager) fsMgr;

				sfsMgr.freeUnusedResources();

				// sfsMgr.close();

				sfsMgrPool.push(sfsMgr);
			} else {
				LoaderLog
						.error("couldnt return the object to the pool as its null or not type of StandardFileSystemManager.");
			}
		} catch (Exception e) {
			// In case of error, the resource can't be returned to the pool.
			currConnCount--;
			String errMsg = LoaderLog
					.getExceptionMsgForLog(
							"Error occured while returning the object to the pool. ",
							e);
			LoaderLog.error(errMsg);
		} finally {
			try {
				synchronized (this) {
					this.notifyAll();
				}
			} catch (Exception e) {
				String errMsg = LoaderLog.getExceptionMsgForLog(
						"Error occured while notifying all threads.", e);
				LoaderLog.error(errMsg);
			}
		}

		if (LoaderLog.isDebugEnabled()) {
			LoaderLog.debug("Exiting the returnSFSManager method");
		}

	}

}
