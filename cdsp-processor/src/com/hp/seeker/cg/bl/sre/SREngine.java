/**
 * #(@) Engine.java Aug 13, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Simple rule engine (SRE).
 *
 * @author  Kelly
 */
public class SREngine {
	private static final Logger logger = LogManager.getLogger(SREngine.class);
	private static SREngine instance = null;
	
	private Map<String, RuleSet> ruleSets = new HashMap<String, RuleSet>(); // Map of names and rules
	private boolean allowSingleValueSubAndOr = true;
	
	/**
     * Getter for allowSingleValueSubAndOr.
     * @return the allowSingleValueSubAndOr
     */
    public boolean isAllowSingleValueSubAndOr() {
    	return allowSingleValueSubAndOr;
    }

	/**
     * Setter for allowSingleValueSubAndOr.
     * @param allowSingleValueSubAndOr the allowSingleValueSubAndOr
     */
    public void setAllowSingleValueSubAndOr(boolean allowSingleValueSubAndOr) {
    	this.allowSingleValueSubAndOr = allowSingleValueSubAndOr;
    }

	/**
	 * Creates a new instance of Engine.
	 */
	protected SREngine() {
	}
	
	
	/**
	 * Get the rule engine.
	 * @return
	 */
	public static synchronized SREngine getEngine() {
		if (instance == null) {
			instance = new SREngine();
		}
		return instance;
	}
	
	
	/**
	 * Add a rule set.
	 * @param name name of the rule set
	 * @param rules rules
	 */
	public void addRuleSet(String name, List<Rule> rules) {
		addRuleSet(new RuleSet(name, rules));
	}

	
	/**
	 * Add a rule set.
	 * @param name name of the rule set
	 * @param rules rules
	 */
	public void addRuleSet(RuleSet rules) {
		ruleSets.put(rules.getName(), rules);
	}

	
	/**
	 * Evaluate a rule set.
	 * @param name name of the rule set
	 * @param md meta data
	 * @return Map of groups and their reasoning
	 * @throws SeekerUnsupportedRuleSetException
	 * @throws RuleException
	 */
	public Map<String, String> evaluateRules(String name, MDProperties md) 
			throws RuleException {
		// get rule set
		RuleSet rules = getRules(name);
		if (rules == null) {
			throw new RuleException("Unsupported rule set: " +  name);
		}	
		Map<String, String> groups = rules.eval(md);
		return groups;
	}
	
	/**
	 * loadRules requests that a new set of rules be loaded 
	 */
	public synchronized void loadRules(String name, File file) throws RuleException {
		logger.info("Loading Rules");
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			URL resourceURL = file.toURI().toURL();
			// removes the rule set that will be reloaded further
			RuleSet old_rule_set = ruleSets.remove(name);
			ruleSets.put("cgs_old", old_rule_set);
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(is);
			RuleSet ruleSet = RuleFactory.buildRules(doc, name, resourceURL);
			
			addRuleSet(ruleSet);
		}
		catch (IOException e) {
			logger.error("Failed to read rules from inout stream : ");
			throw new RuleException("IOException Occurred while trying to read rules from : "+file.getAbsolutePath());
		} 
		catch (JDOMException e) {
			logger.error("Failed to parse rules from: ");
			throw new RuleException("JDOMException Occurred while trying to parse rules from : "+file.getAbsolutePath());
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} 
				catch (IOException e) {
					logger.error("IOException : Failed to close File input stream for : "+file.getAbsolutePath());
				}
			}
		}
	}
	/**
	 * loadRules requests that a new set of rules be loaded 
	 */
	public synchronized void loadRules(String name) throws RuleException {
		// load them if not
		String resourceName = "CGSRules";
		InputStream is = null;
		logger.info("Loading Rules");
		try {
			// removes the rule set that will be reloaded further
			RuleSet old_rule_set = ruleSets.remove(name);
			ruleSets.put("cgs_old", old_rule_set);

			// read CGSRules file 
			String home = System.getProperty("SSDATA_HOME");
			if (home == null || home == ""){
				logger.error("SSDATA_HOME variable not set.Cannot find the CGSRules file.");
				throw new RuleException("Failed to read rules from: " + resourceName+". " +
						"The Environment variable SSDATA_HOME is not set.");
			}
			if (!home.endsWith(File.separator)){
				home = home + File.separator;
			}
			File file = new File(home+"system/cgs/CGSRules");
			
			URL resourceURL = file.toURI().toURL();
			resourceName = resourceURL.toString();
			logger.info("file loaded: " + resourceURL.getFile());
			is = resourceURL.openStream();
			
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(is);
			RuleSet ruleSet = RuleFactory.buildRules(doc, name, resourceURL);
			
			addRuleSet(ruleSet);
		}
		catch (IOException e) {
			logger.error("Failed to read rules from: " + resourceName);
			throw new RuleException("IOException Occurred while trying to read rules from: " + resourceName);
		} 
		catch (JDOMException e) {
			logger.error("Failed to parse rules from: " + resourceName);
			throw new RuleException("JDOMException Occurred while trying to parse rules from: " + resourceName);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} 
				catch (IOException e) {
					logger.error("IOException : Failed to close stream for: " + resourceName);
				}
			}
		}
	}
	
	/**
	 * Locate and load the rules for a given name. Rules are 
	 * @param name name of the rule set
	 * @return the rule set for the given name
	 * @throws RuleException If the rules can't be loaded.
	 */
	public RuleSet getRules(String name) {
		return ruleSets.get(name);
	}
}
