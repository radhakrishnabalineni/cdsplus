package com.hp.seeker.cg.bl.sre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Element;

/**
 * 
 *
 * @author  Kelly
 */
public class CPFBuilder implements MDPropertiesBuilder {
	
	@SuppressWarnings("unchecked")
	public MDProperties build(Element root) {
		MDProperties mdp = new MDProperties();
		
		Element props = root.getChild("properties");
		List<Element> list = props.getChildren();
		for (Element element : list) {
			String name = element.getName();
			String s = element.getAttributeValue("repeating");
			if (s == null) {
				continue;
			}
			boolean rep = Boolean.parseBoolean(s);
			if (rep) {
				Collection<String> vcol = new ArrayList<String>();
				List<Element> vals = element.getChildren("val");
				for (Element e2 : vals) {
					vcol.add(e2.getTextTrim());
				}
				mdp.put(name, new MultiValueProperty(vcol));
			} else {
				mdp.put(name, new SingleValueProperty(element.getTextTrim()));
			}
		}
		
		return mdp;
	}
}
