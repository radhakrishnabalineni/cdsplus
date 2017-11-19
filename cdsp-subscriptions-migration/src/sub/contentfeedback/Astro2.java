package sub.contentfeedback;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class Astro2 {

    public Astro2() {		
	}

	public static void main(String[] args){
		
		System.out.println("id=astro2");
		System.out.println("hierarchyExpansions="+astro2_hierarchy_exp());
	}
	
	
	 /**
	  <xsl:variable name="deliver_original_products" select="true()"/>
	  <xsl:variable name="deliver_product_type" select="false()"/>
	  <xsl:variable name="deliver_product_support_subcategories" select="false()"/>
	  <xsl:variable name="deliver_product_series" select="false()"/>
	  <xsl:variable name="deliver_product_name" select="false()"/>
	  <xsl:variable name="deliver_product_numbers" select="false()"/>
	  <xsl:variable name="deliver_product_lines" select="false()"/>
	  <xsl:variable name="deliver_product_support_categories" select="false()"/>
	  <xsl:variable name="deliver_product_big_series" select="false()"/>
	  <xsl:variable name="deliver_product_marketing_categories" select="false()"/>
	  <xsl:variable name="deliver_product_marketing_subcategories" select="false()"/>
     */
	
     public static DBObject astro2_hierarchy_exp(){
		DBObject astro2_hierarchy_exp = new BasicDBObject("document.original_products",true)
		.append("document.product_types", false)
		.append("document.product_support_subcategories", false)
		.append("document.product_series", false)
		.append("document.product_names", false)
		.append("document.product_numbers", false)
		.append("document.product_lines", false)
		.append("document.product_support_categories", false)
		.append("document.product_big_series", false)
		.append("document.product_marketing_categories", false)
		.append("document.product_marketing_subcategories", false);
		return astro2_hierarchy_exp;
	 }

}
