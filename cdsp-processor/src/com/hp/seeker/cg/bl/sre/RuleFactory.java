/**
 * #(@) RuleFactory.java Aug 15, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.hp.ods.service.ODS;

/**
 * Factory class for rules.
 *
 * @author  Kelly
 * @version $Revision: 7$
 */
public class RuleFactory {
	private static final Logger logger = LogManager.getLogger(RuleFactory.class);
	private static ODS ods;
	
	static final Map<String, Concept> CONCEPTS = new HashMap<String, Concept>();
	static final Map<String, And> AND_CONDITIONS = new HashMap<String, And>();
	static final Map<String, Or> OR_CONDITIONS = new HashMap<String, Or>();

	/**
	 * Build rule set from XML.
	 * @param doc XML document
	 * @param name name of the rule set
	 * @return the rule set
	 */
	public static RuleSet buildRules(Document doc, String name) {
		return buildRules(doc, name, null);
	}
	
	/**
	 * Build rule set from XML.
	 * @param doc XML document
	 * @param name name of the rule set
	 * @param resourceURL the resource URL where Document has been loaded.
	 * @return the rule set
	 */
	@SuppressWarnings("unchecked")
	public static RuleSet buildRules(Document doc, String name, URL resourceURL) {
		List<Rule> rules = new ArrayList<Rule>();
		Element root = doc.getRootElement();
		
		// build global concepts
		List<Element> globalConcepts = root.getChildren("concept");
		if (globalConcepts != null) {
			for (Element gc : globalConcepts) {
				Concept concept = (Concept) buildCondition(gc);
				CONCEPTS.put(concept.getId(), concept);
			}
		}

		// declares global conditions
		processGlobal(root);

		// build the rules
		List<Element> jRules = root.getChildren("rule");
		for (Element jrule : jRules) {
			buildRule(rules, jrule);
		}
		
		RuleSet rs = new RuleSet(name, rules);
		rs.setResourceURL(resourceURL);
		
		return rs;
	}

	@SuppressWarnings("unchecked")
	private static void processGlobal(Element root) {
		
		Element globalElt = root.getChild("global");
		
		if ((globalElt == null) || (globalElt.getChildren().isEmpty())) {
			return;
		}
		
		// build global AND conditions
		List<Element> globalAndConditions = globalElt.getChildren("and");
		if (globalAndConditions != null) {
			for (Element gc : globalAndConditions) {
				And and = (And) buildCondition(gc);
				AND_CONDITIONS.put(and.getId(), and);
			}
		}

		// build global OR conditions
		List<Element> globalOrConditions = globalElt.getChildren("or");
		if (globalAndConditions != null) {
			for (Element gc : globalOrConditions) {
				Or or = (Or) buildCondition(gc);
				OR_CONDITIONS.put(or.getId(), or);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void buildRule(List<Rule> rules, Element jrule) {
		if (jrule == null) {
			throw new IllegalArgumentException("Parameter jrule is not nullable");
		}
		Rule rule = new Rule(jrule.getChildText("value"));
		
		if (jrule.getAttribute("final") != null) {
			String isFinal = jrule.getAttributeValue("final");
			if ((isFinal != null) && (isFinal.equals("true"))) {
				rule.setFinal(true);	
			}
		}

		if (jrule.getAttribute("id") != null) {
			String id = jrule.getAttributeValue("id");
			if (id != null) {
				rule.setId(id);	
			}
		}

		Element c1 = jrule.getChild("condition");
		if (c1 == null) {
			logger.warn("Rule has no condition: " + rule.getName());
			rule.setCondition(new False(null));
		} else {
			List<Element> sub = c1.getChildren();
			rule.setCondition(buildCondition(sub.get(0)));
		}
		
		if (! RuleChecker.checkRule(rule)) {
			logger.warn("Rule failed validation: " + rule.getName());
		} else {
			rules.add(rule);
		}
	}

	@SuppressWarnings("unchecked")
	private static Condition buildCondition(Element condition) {
		String name = condition.getName();
		
		if ("or".equalsIgnoreCase(name)) {
			return new Or(condition, buildConditions(condition.getChildren()));
		} else if ("and".equalsIgnoreCase(name)) {
			return new And(condition, buildConditions(condition.getChildren()));
		} else if ("not".equalsIgnoreCase(name)) {
			return new Not(condition, buildCondition(((List<Element>) condition.getChildren()).get(0)));
		} else if ("name_value_pair".equalsIgnoreCase(name) || "equals".equalsIgnoreCase(name)) {
			return new Equals(condition);
		} else if ("true".equalsIgnoreCase(name)) {
			return new True(condition);
		} else if ("false".equalsIgnoreCase(name)) {
			return new False(condition);
		} else if ("present".equalsIgnoreCase(name)) {
			return new Present(condition);
		} else if ("descendant_of".equalsIgnoreCase(name)) {
			DescendantOf x = new DescendantOf(condition, getODS());
			return x;
		} else if ("concept".equalsIgnoreCase(name)) {
			Concept x = new Concept(condition, getODS());
			return x;
		} else if ("concept_ref".equalsIgnoreCase(name)) {
			ConceptRef x = new ConceptRef(condition);
			return x;
		} 
		if ("and_ref".equalsIgnoreCase(name)) {
			AndRef x = new AndRef(condition);
			return x;
		} 
		if ("or_ref".equalsIgnoreCase(name)) {
			OrRef x = new OrRef(condition);
			return x;
		}
		
		// last resort
		return new False(null);
	}
	
	private static List<Condition> buildConditions(List<Element> children) {
		List<Condition> c = new ArrayList<Condition>();
		for (Element element : children) {
			c.add(buildCondition(element));
		}
		return c;
	}
	
	private static ODS getODS() {
		if (ods == null) {
			// lazy init...
			try {
				InitialContext ctx = new InitialContext();
				ods = (ODS) ctx.lookup("ods.3.0.ODS#com.hp.ods.service.ODS"); 
			} 
			catch (Exception e) {
				logger.warn("Failed to obtain ODS. " +
						"This is OK if running out side of the container but some conditions " +
						"will throw null pointers.");
				logger.error(e);
			}
		}
		return ods;
	}

}
