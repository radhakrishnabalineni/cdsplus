/**
 * 
 */
package com.hp.soar.priorityLoader.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * ProductNames is one per collection.  Keeping anything that was loaded for the collection so it doesn't have to reprocess everytime. 
 * @author dahlm
 *
 */
public class ProductNames {

	private HashMap<String, ArrayList<ProductNameBean>> productMap =
			new HashMap<String, ArrayList<ProductNameBean>>();

	// nothing needs to be built in the constructor
	public ProductNames() {
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<ProductNameBean> loadProductNames(IDfSession session, String productGroupOid) throws DfException{
		StringBuffer dqlQuery = new StringBuffer();
		String nameName;
		String extraName;
		String nameOID;
		String nameUpdate;
		IDfCollection results = null;
		ArrayList<ProductNameBean> nameBeans = new ArrayList<ProductNameBean>();

		// formulate the dqlQuery query to find the product names.
		dqlQuery.append("SELECT pgn.group_id, pgn.group_name, pgn.group_extra_name, pgn.update_flag ");
		dqlQuery.append("FROM dm_dbo.c_product_group_names pgn, dm_dbo.soar_item_products ip ");
		dqlQuery.append("WHERE ip.group_id = pgn.group_id AND ip.product_group_oid = '");
		dqlQuery.append(productGroupOid).append("' ORDER BY pgn.group_id");

		// execute the query
		/*
		 * SOAR 13.2 release,BR678222: Review SOAR Loader Logging for Disk Optimization -- COnverting the logger from info to debug to 
		 * avoid DQL statements in the log
		 */
		//LoaderLog.info("loadProductNames query: " + dqlQuery);
		LoaderLog.debug("loadProductNames query: " + dqlQuery);

		try {
			results = DocbaseUtils.executeQuery(session, dqlQuery.toString(), IDfQuery.DF_READ_QUERY,
					"ProductNames: loadProductNames : query to find the product names");

			while (results.next()) {
				nameOID = results.getString("group_id");
				nameName = results.getString("group_name");
				extraName = results.getString("group_extra_name");
				nameUpdate = results.getString("update_flag");
				nameBeans.add(new ProductNameBean(productGroupOid, nameOID, nameName, extraName, nameUpdate));
			}
		} finally {
			DocbaseUtils.closeResults(results);
		}

		Collections.sort(nameBeans);
		return nameBeans;
	}
  
	/**
	 * getNameBeans returns the cached copy of the beans for a product group in a collection.
	 * Once a collection is done, the cache is emptied and reloaded for the next one.
	 * @param session
	 * @param productGroupOid
	 * @return
	 * @throws DfException 
	 */
	public ArrayList<ProductNameBean> getNameBeans(IDfSession session, String productGroupOid) throws DfException {

		ArrayList<ProductNameBean> productList = productMap.get(productGroupOid);
		if (productList == null) {
			productList = loadProductNames(session, productGroupOid);
			productMap.put(productGroupOid, productList);
		}
		return productList;
	}
}
