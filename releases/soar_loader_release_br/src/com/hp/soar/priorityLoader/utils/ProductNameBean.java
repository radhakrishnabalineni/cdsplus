package com.hp.soar.priorityLoader.utils;

import java.io.Serializable;
import java.util.HashMap;



// use this bean to store the pending file details. Load before the organize page and manipulate in
// the form validtion, need to do this to allow users to change file names and not lose them during
// the validation.

public final class ProductNameBean implements Serializable, Comparable {

	private static HashMap<String, Integer> hierarchyIdx = null;
	
	static {
		// set up the indexes for ordering of a map
		hierarchyIdx = new HashMap<String, Integer>();
		int i=0;
		hierarchyIdx.put("Platform ID", new Integer(i++));
		hierarchyIdx.put("Product Number", new Integer(i++));
		hierarchyIdx.put("Product Name", new Integer(i++));
		hierarchyIdx.put("Product Series", new Integer(i++));
		hierarchyIdx.put("Support Subcategory", new Integer(i++));
		hierarchyIdx.put("Support Category", new Integer(i++));
		hierarchyIdx.put("Marketing Subcategory", new Integer(i++));
		hierarchyIdx.put("Marketing Category", new Integer(i++));
		hierarchyIdx.put("Marketing Product Type", new Integer(i++));
	}
	
	
	private String productGroupOID;
	private String productNameName;
	private String productName;
	private String productNameOID;
	private String productMasterOID;
	private String productUpdate;
	private String productType;
	private String productHierarchyType;
	private String productImage;

	// private IDfSession session;

	public ProductNameBean(String groupOID, String nameOID, String nameName, String extraName, String nameUpdate) {
		this.productGroupOID = groupOID;
		this.productNameOID = nameOID;
		this.productName = nameName;
		this.productNameName = nameName;
		if (!extraName.equals("")) {
			this.productNameName += " (" + extraName + ")";
		}
		this.productUpdate = nameUpdate;

		int nPos = productNameOID.indexOf("_");
		if (nPos != -1) {
			this.productMasterOID = productNameOID.substring(nPos + 1);

			String hierarchy = productNameOID.substring(0, nPos);

			if (hierarchy.equals("PMSC")) {
				this.productType = "Support Category";
				this.productImage = "images/product_support_category.gif";
			} else if (hierarchy.equals("PMSF")) {
				this.productType = "Support Subcategory";
				this.productImage = "images/product_support_sub_category.gif";
			} else if (hierarchy.equals("PMMT")) {
				this.productType = "Marketing Product Type";
				this.productImage = "images/product_marketing_type.gif";
			} else if (hierarchy.equals("PMMC")) {
				this.productType = "Marketing Category";
				this.productImage = "images/product_support_category.gif";
			} else if (hierarchy.equals("PMMF")) {
				this.productType = "Marketing Subcategory";
				this.productImage = "images/product_support_sub_category.gif";
			} else if (hierarchy.equals("PMS")) {
				this.productType = "Product Series";
				this.productImage = "images/product_series.gif";
			} else if (hierarchy.equals("PMM")) {
				this.productType = "Product Name";
				this.productImage = "images/product_name.gif";
			} else if (hierarchy.equals("PMN")) {
				this.productType = "Product Number";
				this.productImage = "images/product_number.gif";
			} else if (hierarchy.equals("PMPI")) {
				this.productType = "Platform ID";
				this.productImage = "images/product_platform_id.gif";
			}

			this.productHierarchyType = hierarchy;
		}

	}

	public String getProductGroupOID() {
		return this.productGroupOID;
	}

	public String getProductNameOID() {
		return this.productNameOID;
	}

	public void setProductNameOID(String s) {
		this.productNameOID = s;
	}

	public String getProductMasterOID() {
		return this.productMasterOID;
	}

	public void setProductMasterOID(String s) {
		this.productMasterOID = s;
	}

	public String getProductName() {
		return this.productName;
	}

	public void setProductName(String s) {
		this.productName = s;
	}

	public String getProductNameName() {
		return this.productNameName;
	}

	public void setProductNameName(String s) {
		this.productNameName = s;
	}

	public String getProductUpdate() {
		return this.productUpdate;
	}

	public void setProductUpdate(String s) {
		this.productUpdate = s;
	}

	public String getProductType() {
		return this.productType;
	}

	public void setProductType(String s) {
		this.productType = s;
	}

	public String getProductHierarchyType() {
		return this.productHierarchyType;
	}

	public void setProductHierarchyType(String s) {
		this.productHierarchyType = s;
	}

	public String getProductImage() {
		return this.productImage;
	}

	public void setProductImage(String s) {
		this.productImage = s;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if ( !(o instanceof ProductNameBean) ) {
			return -1;
		}
		ProductNameBean yourBean = (ProductNameBean)o;
    int returnValue = 0;

    Integer myHierarchy = hierarchyIdx.get(productType);
    Integer yourHierarchy = hierarchyIdx.get(yourBean.productType);
    
    if (myHierarchy == yourHierarchy) {
        returnValue = productName.compareTo(yourBean.productName);
    } else if (myHierarchy < yourHierarchy) {
        returnValue = 1;
    } else {
        returnValue = -1;
    }

    return returnValue;
	}

}// end class

