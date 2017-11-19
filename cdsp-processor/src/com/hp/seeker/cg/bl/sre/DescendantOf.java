/**
 * DescendantOf Dec 24, 2007
 */
package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;

import com.hp.ods.service.ODS;
import com.hp.ods.util.PMRelations;

/**
 * Property target OIDs has a descendant of the given OIDs.
 * 
 * @author Kelly
 * @version $Revision: 6$
 */
public class DescendantOf extends Condition {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	//private static final Logger logger = Logger.getLogger(DescendantOf.class);
	final List<Integer> ruleOIDs = new ArrayList<Integer>();
	ODS ods;

	/**
	 * Constructor.
	 */
	public DescendantOf(Element condition, ODS ods) {
		super(condition);
		this.ods = ods;
		
		String s = condition.getAttributeValue("oids");
		if (s == null || s.length() == 0) {
			return;
		}
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			ruleOIDs.add(Integer.parseInt(st.nextToken().trim()));
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.seeker.cg.bl.sre.Condition#eval(com.hp.seeker.cg.bl.sre.MDProperties)
	 */
	@Override
	boolean eval(MDProperties md, int depth) throws RuleException {
		boolean result = false;
		List<Integer> oids = getTargetOIDs(md);
		
		try {
			Collection<Integer> ancestors = ods.readAncestorOids(oids, PMRelations.PM_ALL);
			for (Integer oid : ruleOIDs) {
				if (ancestors.contains(oid)) {
					result = true;
					LDC.add(depth, "DescendantOf", id, "Target OID '" + oid 
							+ "' is a descendant of rule OID => " + result);
					break;
				}
			}
		} 
		catch (Exception e) {
			throw new RuleException("Failed to look up ancestors: " + e.getMessage());
		}
		
		return result;
	}
	
	List<Integer> getTargetOIDs(MDProperties md) throws RuleException {
		return getOIDs(md, "target_oids");
	}
	
	List<Integer> getPathOIDs(MDProperties md) throws RuleException {
		return getOIDs(md, "path_oids");
	}
	
	List<Integer> getOIDs(MDProperties md, String name) throws RuleException {
		MDProperty p = md.get(name);
		
		if (p == null) {
			return null;
		}
		
		// convert target OID strings to Integers
		List<Integer> oids = new ArrayList<Integer>();
		String[] vals = p.getValues();
		if (vals != null) {
			for (String val : vals) {
				try {
					oids.add(Integer.parseInt(val));
				} 
				catch (NumberFormatException e) {
					throw new RuleException("Target OID value is not a number: " + val);
				}
			}
		}
		return oids;
	}

	void storePathOIDs(MDProperties md, Collection<Integer> oids) {
		if (oids == null || oids.isEmpty()) {
			return;
		}
		ArrayList<String> values = new ArrayList<String>();
		for (Integer oid : oids) {
	        values.add(oid.toString());
        }
		
		MultiValueProperty p = new MultiValueProperty(values);
		md.put("path_oids", p);
	}
}
