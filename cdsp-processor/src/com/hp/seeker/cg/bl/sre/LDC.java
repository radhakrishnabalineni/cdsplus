/**
 * NDC Aug 14, 2008
 */
package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * List diagnostic context to explain rule execution.
 * 
 * @author Kelly
 * @version $Revision: 2$
 */
public class LDC {
	private final static String NOT_ACTIVE = "LDC Not Active";
	static Map<Thread, List<DiagnosticContext>> map = new Hashtable<Thread, List<DiagnosticContext>>();
	
	static int addCounter = 0;
	static int addThreshold = 100;
	static boolean active = false;
	
	// no instance allowed
	private LDC() {
	}

	public static void setLDCActive(boolean tf) {
		active = tf;
	}
	
	/**
	 * Get LDC list for current thread.
	 * 
	 * @return list for current thread.
	 */
	private static List<DiagnosticContext> getCurrentList() {
		if (map != null) {
			return map.get(Thread.currentThread());
		}
		return null;
	}
	
	public static String explain() {
		if (!active) {
			return NOT_ACTIVE;
		}
		StringBuilder sb = new StringBuilder();
		List<DiagnosticContext> list = getCurrentList();
		
		if (sb != null) {
			for (DiagnosticContext dc : list) {
				sb.append(dc.message);
				sb.append(System.getProperty("line.separator"));
			}
		}
		
		return sb.toString();
	}

	public static void remove() {
		if (!active) {
			// not currently working 
			return;
		}
		
		map.remove(Thread.currentThread());
		
		if (addCounter < addThreshold) {
			return;
		}
		
		// remove dead threads from the mapping, if any...
		Vector<Thread> deadThreads = new Vector<Thread>();
		for (Thread t : map.keySet()) {
			if (! t.isAlive()) {
				deadThreads.add(t);
			}
		}
		for (Thread t : deadThreads) {
			map.remove(t);
		}
		
		addCounter = 0;
	}
	
	/**
	 * Add a diagnostic message to the list
	 * @param depth recursion depth for indentation
	 * @param result condition result
	 * @param condition condition name
	 * @param msg message
	 */
	public static void add(int depth, String condition, String id, String msg) {
		
		if (!active) {
			return;
		}
		
		addCounter++;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append(" ");
		}
		sb.append(condition);
		sb.append(" id='");
		sb.append(id);
		sb.append("'");
		if (msg != null) {
			sb.append(": ");
			sb.append(msg);
		}
		
		List<DiagnosticContext> list = getCurrentList();
		if (list == null) {
			list = new ArrayList<DiagnosticContext>();
			Thread key = Thread.currentThread();
			map.put(key, list);
		}
		
		list.add(new DiagnosticContext(sb.toString()));
	}
	
	
	/**
	 * Diagnostic Context
	 */
	private static class DiagnosticContext {
		String message;
		DiagnosticContext(String message) {
			this.message = message;
		}
	}

}
