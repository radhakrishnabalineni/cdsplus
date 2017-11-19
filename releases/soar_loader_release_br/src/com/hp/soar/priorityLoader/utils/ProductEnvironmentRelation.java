package com.hp.soar.priorityLoader.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.dom4j.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;
import com.hp.soar.priorityLoader.helper.SoarExtractXMLBuilder;
import com.hp.soar.priorityLoader.ref.ReferenceLists;

public class ProductEnvironmentRelation {

	// ID of this group <groupType_groupNumber>
	private String groupId;
	private String groupName;
	
	private String groupNumber;
	private String groupType;
	
	// environments for this product group
	private ArrayList<String> envDetailOids = new ArrayList<String>();
	
	/**
	 * Create a new ProductEnvironmentRelation
	 * @param result
	 * @param validEnvs
	 * @throws DfException
	 */
	public ProductEnvironmentRelation(IDfCollection result, HashSet<String>validEnvs) throws DfException {
		groupId = result.getString("group_id");
		groupName = result.getString("group_name");
		
		int idx = groupId.indexOf('_');
		if (idx != -1) { 
			groupNumber = groupId.substring(idx+1);
			groupType = groupId.substring(0,idx);
		}
		
		addEnvironment(result, validEnvs);
	}
	
	
	/**
	 * addEnvironment adds an environment oid to the list if it is valid
	 * @param result
	 * @param validEnvs
	 * @throws DfException
	 */
	public void addEnvironment(IDfCollection result, HashSet<String>validEnvs) throws DfException {
		String envOid = result.getString("environment_detail_oid");
		if (validEnvs.contains(envOid)) {
			envDetailOids.add(envOid);
		}
	}


	public String getGroupId() {
		return groupId;
	}


	public String getGroupNumber() {
		return groupNumber;
	}


	public String getGroupType() {
		return groupType;
	}

	/**
	 * addElement creates a complete Product Environment Relation in the xml element
	 * @param parent
	 * @param envMsrMap environmentOid -> Min Service Release Map Oid
	 * @param envLabels  Parameter to collect the unique environments for the item containing this PER
	 */
	public void addElement(Element parent, HashMap<String, String> envMsrMap, ArrayList<String>envLabels) {
		// create the holding element
		Element prdEnvisRelElemnt = SoarExtractXMLBuilder.addXMLElement(parent, "product-environments-relation");
		
		// add the group oid info if it isn't null
		if (groupNumber != null) {
			Element prdIdElement = SoarExtractXMLBuilder.addXMLElement(prdEnvisRelElemnt, "product-oid", groupNumber);
			SoarExtractXMLBuilder.addXMLAttribute(prdIdElement, "type", groupType, false);
		}
		
		// Add the environments element
		Element envisMainElement = SoarExtractXMLBuilder.addXMLElement(prdEnvisRelElemnt, "environments");

		for (String envOid : envDetailOids) {
            Element enviMsrElement = SoarExtractXMLBuilder.addXMLElement(envisMainElement, "environment-msr");
            // environment_detail is the content of this element
            String envLabel = ReferenceLists.getLabel(ReferenceLists.ENVIRONMENTS_DETAILS_LIST, envOid); 
            Element enviElement = SoarExtractXMLBuilder.addXMLElement(enviMsrElement, "environment", envLabel); 

            // save the environment label if it isn't there already
            if (!envLabels.contains(envLabel)) {
            	envLabels.add(envLabel);
            }
            
            SoarExtractXMLBuilder.addXMLAttribute(enviElement, "oid", envOid, false);
            
            // environment is the base attribute
            envLabel = ReferenceLists.getLabel(ReferenceLists.ENVIRONMENTS_LIST, envOid); 
            SoarExtractXMLBuilder.addXMLAttribute(enviElement, "environment-base", envLabel, false);

            if (envMsrMap.containsKey(envOid)) {
            	String minService = ReferenceLists.getLabel(ReferenceLists.MIN_SERVICE_RELEASES_LIST, envMsrMap.get(envOid));
            	if (minService != null) {
            		Element minServiceRelElement = SoarExtractXMLBuilder.addXMLElement(enviMsrElement, "minimum-service-release", minService);
            		SoarExtractXMLBuilder.addXMLAttribute( minServiceRelElement, "oid", envMsrMap.get(envOid), false);
            	}
            }
		}
	}
}
