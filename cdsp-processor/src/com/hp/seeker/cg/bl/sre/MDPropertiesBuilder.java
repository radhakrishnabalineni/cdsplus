/**
 * #(@) MDPropertiesBuilder.java Aug 14, 2007
 */
package com.hp.seeker.cg.bl.sre;

import org.jdom.Element;

/**
 * 
 *
 * @author  Kelly
 * @version $Revision$
 */
public interface MDPropertiesBuilder {
	
	/**
	 * Build metadata properties from a JDOM tree 
	 * @param root root of the JDOM
	 * @return metadata properties
	 */
	MDProperties build(Element root);
	
}
