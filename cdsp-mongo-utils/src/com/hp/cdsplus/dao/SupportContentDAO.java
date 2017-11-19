package com.hp.cdsplus.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hp.cdsplus.mongo.config.ConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.hp.cdsplus.mongo.exception.MongoUtilsException;

/**
 * @author milind
 * This class is specifically written for generating contentbody, embeddedlinks
 * and componentreuse elements for fastxml requirements. The data for these elements comes from supportcontent.
 * A sample file from supportcontent would look like the example below :
 * 
 * <c_support_doc xmlns:dctm="http://www.documentum.com" dctm:obj_status="Read-Only" dctm:obj_id="0900a5a5891f7606" 
                             dctm:version_label="CURRENT" c_support_doc.description="Looking for......">
	 <title>HP Pavilion 500-536d Desktop PC product specifications</title>
  
	<generic>
		<section reuse="yes" component.title="Will Turner" is-link="yes" dctm:obj_status="Read-Only" dctm:obj_id="0900a5a588e4b1e9" dctm:version_label="2.2" reuse.name="c03742268">
			<para hidden="no">
				<graphic reuse="yes">
					<image src.type="component" dctm:link_version_label="3.0" src="c03742269.jpg"/>
					<alternate.description>
						<para hidden="no">Product image</para>
					</alternate.description>
				</graphic>
			</para>
		</section>
		<section is-link="yes" reuse="no" usage="expandable">
		.
		.
		.
		.
		.

	</generic>
	</c_support_doc>
 * 
 * 
 */
public class SupportContentDAO {

	//currently hard coding the ELEMENT NAMES can be put in config files
	private static final String SUPPORT_CONTENT_DB_NAME = "supportcontent";
	private static final String CONTENT_FILES_COLLECTION_NAME = "content";
	private static final String CONTENT_FILE_NAME = "filename";
	private static final String UTF_8 = "UTF-8";

	private static final String PERIOD_STRING = ". ";
	private static final String SEMI_COLON_STRING = ";";
	private static final String XML_FILE_EXTENSION = ".xml";

	private static final String TITLE_ELEMENT_NAME = "title";
	private static final String URL_ELEMENT_NAME = "url";
	private static final String HEADING_ELEMENT_NAME = "heading";
	private static final String SECTION_ELEMENT_NAME = "section";
	private static final String ADDRESS_ATTRIBUTE_NAME = "address";

	/**
	 * This method fetches the supportcontent data from gridfs files. To prevent client code to
	 * make too many db calls this method is private.
	 * @param documentID
	 * @return
	 * @throws MongoUtilsException
	 * @throws IOException
	 */
	private Document getContentData(String documentID) throws MongoUtilsException, IOException {
		StringWriter writer = new StringWriter();
		Document doc = null;
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated(SUPPORT_CONTENT_DB_NAME);
		if(db != null){
			GridFS gfs = new GridFS(db, CONTENT_FILES_COLLECTION_NAME);
			if(gfs != null){
				String contentFileName = documentID + XML_FILE_EXTENSION;

				BasicDBObject query = new BasicDBObject().append( CONTENT_FILE_NAME, contentFileName);
				List<GridFSDBFile> gridFSDBFileList = gfs.find(query);
				if(gridFSDBFileList.size() > 0 ) {
					GridFSDBFile gridFSDBFile = gridFSDBFileList.get(0);
					InputStream inputStream = gridFSDBFile.getInputStream();
					IOUtils.copy(inputStream, writer, UTF_8);
					if(! writer.toString().isEmpty()){
						doc = Jsoup.parse(writer.toString());
					}
				}
			}
		}

		return doc;
	}

	/**
	 * Used to extract the title element within the root c_support_doc. This method
	 * is being used by the body method internally as this data from this element is used as part of body content.
	 * @param args
	 */
	private String title(Document document){
		StringBuffer buffer = new StringBuffer();
		if(document != null){
			Elements titles =  document.select(TITLE_ELEMENT_NAME);
			if(titles != null && ! titles.isEmpty()){
				for(Element contentTitle : titles){
					buffer.append(contentTitle.text());

				}
			}
		}
		return buffer.toString();
	}

	/**
	 * iterated all elemnts of a document looking for elements containing url
	 * Concatenate all url's in a single string with ; as delimiter
	 * @param document
	 * @return
	 */
	
	private String urls(Document document){
		StringBuffer buffer = new StringBuffer();
		if(document != null ){
			Elements elements = document.select(URL_ELEMENT_NAME);
			if(elements != null && ! elements.isEmpty()){
				for(Element url : elements){
					buffer.append(url.attr(ADDRESS_ATTRIBUTE_NAME));
					buffer.append(SEMI_COLON_STRING);
				}
			}
		}

		return buffer.toString();
	}

	/**
	 * Iterates through all elements looking for title element
	 * and first occurrence of heading element (to match the data produced by austin instance)
	 * and the body of all "section" elements is extracted and sent as a string
	 * added ". " between elements for better usage.
	 * Though there can be n number of headings in a given document but referring to
	 * data from austin server and from fast team we add data only from the first occurance
	 * of heading (might change)
	 * @param document
	 * @return
	 */
	
	private String body(Document document){
		StringBuffer buffer = new StringBuffer();
		if(document != null){
			String title = title(document);
			if( title != null && ! "".equals(title)){
				buffer.append(title).append(PERIOD_STRING);
			}

			Elements headings = document.select(HEADING_ELEMENT_NAME);
			if(headings != null && headings.size() > 0){
				buffer.append(headings.get(0).text()).append(PERIOD_STRING);
			}
			Elements sections = document.select(SECTION_ELEMENT_NAME);

			if(sections != null && sections.size() > 0){

				for(Element section : sections){
					buffer.append(section.text());
					buffer.append(PERIOD_STRING);
				}
			}

		}
		return buffer.toString();
	}

	/***
	 * iterate the section elements. Look for elements that have attribute reuse="yes"
	 * if yes then select the "reuse.name" + ".xml" and "component.title". Separated by : and 
	 * each pair seperated by ";". There can be situations where the string might be ":;:;"
	 * @param document
	 * @return
	 */
	
	private String componentReuse(Document document){
		StringBuffer buffer = new StringBuffer();
		if( document != null) {
			Elements sections = document.select("section");
			if(sections != null && ! sections.isEmpty()){
				for(Element section : sections){
					String reuse = section.attr("reuse");
					if(reuse != null && reuse.equalsIgnoreCase("yes")) {
						String reuseName = section.attr("reuse.name");
						String componentTitle = section.attr("component.title");
						if(reuseName != null && ! "".equals(reuseName)){
							buffer.append(reuseName);
							buffer.append(".xml");
						}

						buffer.append(":");

						if(componentTitle != null && ! "".equals(componentTitle)){
							buffer.append(componentTitle);
						}

						buffer.append(";");
					}
				}
			}
		}

		return buffer.toString();
	}
	
	/**
	 * As calls to retrieve a file from gridfs can be expansive we are forcing all the above methods to
	 * be clubbed together and made private so that the client application can use only this method for
	 * parsing. That way we ensure that one call to gridfs gives all the required data
	 *  
	 * @param documentID
	 * @return
	 * @throws MongoUtilsException
	 * @throws IOException
	 */

	public Map<String, String> retrieveSupportContentData(String documentID) throws MongoUtilsException, IOException {
		Map<String, String> supportContentData = new HashMap<String, String>();
		Document document = getContentData(documentID); 

		supportContentData.put("content_body",body(document));
		supportContentData.put("embedded_links", urls(document));
		supportContentData.put("component_reuse", componentReuse(document));

		return supportContentData;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws MongoUtilsException, IOException {
		// TODO Auto-generated method stub
		DB db = ConfigurationManager.getInstance().getMongoDBAuthenticated("supportcontent");
		SupportContentDAO dao = new SupportContentDAO();
		Options options = new Options();
		//options.setDocid("c50341800");
		options.setDocid("c04532123");

		/*dao.content_body(options);
		dao.embedded_link(options);
		dao.component_reuse(options);*/
		Map<String, String>contentData = dao.retrieveSupportContentData(options.getDocid());
		for(String key : contentData.keySet()){
			System.out.println("Key : " + key);
			System.out.println(contentData.get(key));
		}

	}

}
