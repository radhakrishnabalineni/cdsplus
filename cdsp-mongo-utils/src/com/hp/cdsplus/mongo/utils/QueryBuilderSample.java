package com.hp.cdsplus.mongo.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class QueryBuilderSample {
	
	QueryBuilder builder ;
	public QueryBuilderSample() {
		builder = QueryBuilder.start();
	}
	
	public QueryBuilder resetQueryBuilder(){
		builder = null;
		builder = QueryBuilder.start();
		return builder;
	}
	public void atlas_201(){
QueryBuilder builder = QueryBuilder.start();
		
		builder.put("document.document_type").is("web standard graphic");
		
		BasicDBList dtd_filter = new BasicDBList();
		dtd_filter.add("product image campaign");
		dtd_filter.add("product image - not as shown campaign");
		dtd_filter.add("product image campaign with output sample");
		dtd_filter.add("product image - not as shown campaign with output output");
		
		builder.and("document.document_type_details").notIn(dtd_filter);
		
		DBObject subQuery1 = builder.get();
		//System.out.println(subQuery1);
		
		builder = null;
		builder = QueryBuilder.start();
		
		builder.put("document.cmg_acronym").is("cmg115");
		DBObject and1 = builder.get();
		
		//System.out.println("and1 - "+and1);
		
		builder = null;
		builder = QueryBuilder.start();
		BasicDBList product_list = new BasicDBList();
		product_list.add("321957");
		builder.put("document.product_types.product").notIn(product_list);
		
		DBObject and3 = builder.get();
		//System.out.println("and3 - "+and3);
		
		builder = null;
		builder = QueryBuilder.start();
		
		DBObject or1 = new BasicDBObject("document.pixel_height" , new BasicDBObject("$gte" , "1501"));
		DBObject or2 = new BasicDBObject("document.pixel_width" , new BasicDBObject("$gte" , "1501"));
		builder.or(or1,or2).and(new BasicDBObject("document.dpi_resolution","300"));
		
		DBObject or_c1 = builder.get();
		
		DBObject or_c2 = new BasicDBObject("document.content_type", "png");
		
		builder = null;
		builder= QueryBuilder.start();
		builder.put("document.content_type").is("gif");
		builder.and(new BasicDBObject("document.pixel_height","400").append("document.pixel_width", "400"));
		
		DBObject or_c3 = builder.get();
		
		
		builder = null;
		builder = QueryBuilder.start();
		builder.put("document.content_type").is("jpg");
		
		DBObject q1=new BasicDBObject("document.pixel_width","400").append("document.pixel_height","400");
		DBObject q2=new BasicDBObject("document.pixel_width","170").append("document.pixel_height","190");
		DBObject q3=new BasicDBObject("document.pixel_width","100").append("document.pixel_height","70");
		
		builder.or(q1,q2,q3);	
		DBObject or_c4 = builder.get();
		
		builder = null;
		builder = QueryBuilder.start();
		
		DBObject and2 = builder.or(or_c1,or_c2,or_c3,or_c4).get();
		
		//System.out.println("and2 - "+and2);
		builder = null;
		builder = QueryBuilder.start();
		DBObject subQuery2_or1 = builder.and(and1,and2,and3).get();
		
		builder = null;
		builder = QueryBuilder.start();
		builder.put("document.cmg_acronym").is("cmg572");
		builder.and("document.product_types.product").in(product_list);
		
		DBObject subQuery2_or2 = builder.get();
		
		builder = null;
		builder = QueryBuilder.start();
		builder.or(subQuery2_or1,subQuery2_or2);
		DBObject subQuery2 = builder.get();
		
		//System.out.println(subQuery2);
		builder = null;
		
		builder = QueryBuilder.start();
		builder.and(subQuery1,subQuery2);
		DBObject cap_201 = builder.get();
		System.out.println("cap_201");
		System.out.println(cap_201);
	}
	
	public static void main(String[] args){
		

	}
}
