/**
 * #(@) RuleSet.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Set of rules.
 *
 * @author  Kelly
 * @version $Revision: 3$
 */
public class RuleSet {

	private String name;
	private Date date = new Date();
	private final List<Rule> rules = new ArrayList<Rule>();
	private URL resourceURL = null;
	
	/**
	 * Creates a new RuleSet.
	 * @param name name of the rule set
	 */
	public RuleSet(String name) {
		this.name = name;
	}

	/**
	 * Creates a new RuleSet.
	 * @param name name of the rule set
	 * @param rules rules
	 */
	public RuleSet(String name, List<Rule> rules) {
		this(name);
		this.rules.addAll(rules);
	}

	/**
	 * Getter: attribute name
	 * @return Returns the value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter: attribute name
	 * @param name new value of name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Compares the ruleset date with the last modified date of the resource
	 * (if the resource is a file) from the ruleset has been loaded. 
	 * @return true/false whether the resource from where the ruleset was loaded 
	 * has changed or not.
	 */
	public synchronized boolean hasChanged() {
		return (!getDate().equals(getLastModifiedDate()));
	}
	
	public URL getResourceURL() {
		return resourceURL;
	}

	public void setResourceURL(URL resourceURL) {
		this.resourceURL = resourceURL;
		setDate(getLastModifiedDate());
	}

	/**
	 * If the resource is a file, it returns the last modified date.
	 * @return The last modified date if the resource is a file and getDate() otherwise.
	 */
	private Date getLastModifiedDate() {
		Date result = getDate();

		if (this.resourceURL != null) {
	
			String filename = resourceURL.getFile();
			
			if (filename != null) {
				File f = new File(filename);
				if (f.exists()) {
					result = new Date(f.lastModified());
				}
			}
		}
		
		return result;
	}

	/**
	 * Getter: date
	 * @return the value of date
	 */
	public Date getDate() {
		return (date == null) ? null : (Date)date.clone();
	}

	/**
	 * Setter: date
	 * @param date new value of date
	 */
	public void setDate(Date date) {
		this.date = (date == null) ? null : (Date)date.clone();
	}

	/**
	 * Evaluate the rule in this set.
	 * @param md metadata
	 * @return matching rules.
	 * @throws RuleException failed to evaluate
	 */
	public Map<String, String> eval(MDProperties md) throws RuleException {
		Map<String, String> matches = new HashMap<String, String>();
		
		for (Rule rule : rules) {
			try {
				if (rule.eval(md)) {
					matches.put(rule.getName(), LDC.explain());
					if (rule.isFinal()) {
						break;
					}
				}
			}
			finally {
				LDC.remove();
			}
		}
		return matches;
	}
}
