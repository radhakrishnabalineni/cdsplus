/**
 * Concept Jul 15, 2008
 */
package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;

import com.hp.ods.service.ODS;
import com.hp.ods.util.PMRelations;

/**
 * ...
 * 
 * @author Kelly
 * @version $Revision: 4$
 */
public class Concept extends DescendantOf {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Concept.class);
	boolean matchConceptOIDs = true;
	
	/**
	 * Constructor.
	 * @param condition JDOM element that defines the condition
	 * @param taxo Taxo SF to interact with the DB
	 */
	public Concept(Element condition, ODS ods) {
		super(condition, ods);
		
		try {
			ruleOIDs.addAll(computeConceptOIDs(condition));
		} 
		catch (Exception e) {
			logger.error("Failed to compute concept OIDs.");
			logger.error(e);
			// throw new RuleException("Failed to compute concept OIDs.");
		}
		
		String s = condition.getAttributeValue("matchConceptOIDs");
		if (s != null) {
			matchConceptOIDs = Boolean.getBoolean(s);
		}
	}


	private Collection<Integer> computeConceptOIDs(Element condition) {
		String query = condition.getAttributeValue("contains");
		List<Integer> levels = null;
		
		String slevels = condition.getAttributeValue("levels");
		if (slevels != null) {
			levels = new ArrayList<Integer>();
			StringTokenizer st = new StringTokenizer(slevels, ",");
			while (st.hasMoreTokens()) {
				levels.add(new Integer(st.nextToken()));
			}
		}
		
		if (query == null || query.length() == 0) {
			return new ArrayList<Integer>(); // return empty collection.
		}
		
		logger.debug("Computing concept OIDs for: >" + query 
				+ "< levels: " + levels);
		
		Collection<Integer> oids = ods.readOIDs(query, levels);
		logger.debug("Concept OIDs are: " + oids.toString());
		
		return oids;
	}


	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		boolean result = false;
		List<Integer> oids = getTargetOIDs(md);
		
		for (Integer oid : oids) {
			if (matchConceptOIDs && ruleOIDs.contains(oid)) {
				logger.debug("Concept matched at concept OID: " + id);
				result = true;
				
				LDC.add(depth, "Concept", id, "One or more target OIDs match concept "+ getId() +" => " + result);
				return result;
			}
		}
		
		
		try {
			Collection<Integer> ancestors = getPathOIDs(md); 
			if (ancestors == null || ancestors.isEmpty()) {
				ancestors = ods.readAncestorOids(oids, PMRelations.PM_ALL);
				storePathOIDs(md, ancestors);
			}
			
			long t1 = System.currentTimeMillis();
			for (Integer oid : ruleOIDs) {
				if (ancestors.contains(oid)) {
					result = true;
					LDC.add(depth, "Concept", id, "One or more target OIDs match concept "+ getId() +" => " + result);
					break;
				}
			}
			long t = System.currentTimeMillis() - t1;
			logger.debug("Ancestor check time - " + id + ": " + t);
		} 
		catch (Exception e) {
			throw new RuleException("Failed to look up ancestors: " + e.getMessage());
		}
		
		return result;
	}
	
	
}
