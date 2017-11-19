package sub.contentfeedback;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

   /**
    * 	/document/document_type = 'Agent Resource Center'  and /document/disclosure_level = 'For HP and Channel Partner Internal Use'  
    */
    public class Arc_content_feedback_201 {
    	
	public Arc_content_feedback_201() {		
	}

	public static void main(String[] args){
		
        QueryBuilder builder = QueryBuilder.start();
		
		DBObject and1 = new BasicDBObject("document.document_type","Agent Resource Center");
		DBObject and2 = new BasicDBObject("document.disclosure_level","For HP and Channel Partner Internal Use");
		
		builder.and(and1,and2).get();
		DBObject arc_content_feedback_201 = builder.get();
		System.out.println("id=arc_content_feedback_201");
		System.out.println("filter="+arc_content_feedback_201);
		System.out.println("hierarchyExpansions="+arc_content_feedback_201_hierarchy_exp());
	}

	
	     /**
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
	  	
	   public static DBObject arc_content_feedback_201_hierarchy_exp(){
			DBObject arc_content_feedback_201_hierarchy_exp = new BasicDBObject("document.product_types",false)
			.append("document.product_support_subcategories", false)
			.append("document.product_series", false)
			.append("document.product_names", false)
			.append("document.product_numbers", false)
			.append("document.product_lines", false)
			.append("document.product_support_categories", false)
			.append("document.product_big_series", false)
			.append("document.product_marketing_categories", false)
			.append("document.product_marketing_subcategories", false);
			return arc_content_feedback_201_hierarchy_exp;
		}
}
