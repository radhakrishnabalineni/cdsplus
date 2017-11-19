package com.hp.cdsplus.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;

import com.hp.cdsplus.conversion.exception.ConversionUtilsException;


/**
 * This class provides methods to convert XML to JSON and vice versa.<br>
Make sure the JAXB binding classes are in class path before using this utility class methods.<br>

<b><i>Other Dependancies:</i></b><br>
jaxb1-impl.jar<br>
jaxb1-impl-javadoc.jar<br>
jaxb-api.jar<br>
jaxb-impl.jar<br>
jaxb-impl-javadoc.jar<br>
jaxb-xjc.jar<br>
jaxb-xjc-javadoc.jar<br>
jettison-1.3.3.jar<br>
jsr173_1.0_api.jar<br>
jsr173_1.0_api-1.0.jar<br>
stax-api-1.0-2.jar<br>

 *
 */
public class ConversionUtils {
	public String convertObjecttoXML(Object obj, String bindingClassName)throws ConversionUtilsException {
		try {
			Marshaller marshaller = getMarshaller(bindingClassName);
			
			if(bindingClassName.equalsIgnoreCase("com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed")) 
                marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,"soar-software-feed-vE4.xsd");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			Writer writer = new OutputStreamWriter(baos);
			
			marshaller.marshal(obj, writer);
			
			String convertedXML = baos.toString();
			
			convertedXML = convertedXML.replace("projectref", "proj:ref").replace("projectoid", "proj:oid");
			
			return convertedXML;
		} catch (JAXBException e) {
			throw new ConversionUtilsException(
					"Exception while converting JSON to XML", e);
		}
	}



	/**
	 * This method used to convert JSON to Object.<br>
	Converts the jsonString(String- json format) bind to JAXB bindingClassName(String) and the converted Object is returned.<br>

	 * @param jsonString
	 * @param bindingClassName
	 * @return java.lang.String 
	 * @throws ClassNotFoundException
	 * @throws JSONException
	 * @throws JAXBException
	 * @throws XMLStreamException
	 */
	public Object convertJSONtoObject(String jsonString, String bindingClassName) throws ConversionUtilsException{

		try{
			
			jsonString = jsonString.replace("xlink/href", "xlink.href").replace("xlink/type", "xlink.type");
			JSONObject jsonObj = new JSONObject(jsonString);
			Configuration config = new Configuration();

			Map<String,String> xmlToJsonNamespaces= new HashMap<String,String>();
			xmlToJsonNamespaces.put("http://www.w3.org/1999/xlink", "xlink");
			config.setXmlToJsonNamespaces(xmlToJsonNamespaces);

			MappedNamespaceConvention con = new MappedNamespaceConvention(config);
			CustomMappedXMLStreamReader xmlStreamReader = new CustomMappedXMLStreamReader(jsonObj, con);

			Unmarshaller unmarshaller = getUnmarshaller(bindingClassName);
			Object obj = (Object) unmarshaller.unmarshal(xmlStreamReader);
			Object boundObject= getBindingClass(bindingClassName).cast(obj);
			
			return boundObject;
		
		}
		catch(JAXBException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}
		catch(JSONException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}
		catch(XMLStreamException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}
	}




	/**
	 * This method used to convert XML to JSON.<br>
	Converts the xmlString bind to JAXB bindingClassName(String) and the converted JSON is returned as a String.<br>

	 * @param xmlString
	 * @param bindingClassName
	 * @return java.lang.String 
	 * @throws ClassNotFoundException
	 * @throws JAXBException
	 * @throws XMLStreamException 
	 */
	public String convertXMLtoJSON(String xmlString, String bindingClassName ) throws ConversionUtilsException{
		return convertXMLtoJSON(xmlString.getBytes(),bindingClassName);
	}


	/**
	 * This method used to convert XML to JSON.<br>
	Converts the xml byte array bind to JAXB bindingClassName(String) and the converted JSON is returned as a String.<br>

	 * @param content
	 * @param bindingClassName
	 * @return java.lang.String 
	 * @throws ClassNotFoundException
	 * @throws JAXBException
	 * @throws XMLStreamException 
	 */
	public String convertXMLtoJSON(byte[] content, String bindingClassName ) throws ConversionUtilsException{
		try{

			Unmarshaller unmarshaller = getUnmarshaller(bindingClassName);
			
			ByteArrayInputStream inputStream = new ByteArrayInputStream(replaceInvalidCharacters(content).getBytes());
			
			InputStreamReader reader= new InputStreamReader(inputStream);
			
			Object obj = (Object) unmarshaller.unmarshal(reader);

			getBindingClass(bindingClassName).cast(obj);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(baos);
			
			Configuration config = new Configuration();

			Map<String,String> xmlToJsonNamespaces= new HashMap<String,String>();
			xmlToJsonNamespaces.put("http://www.w3.org/1999/xlink", "xlink");
			config.setXmlToJsonNamespaces(xmlToJsonNamespaces);
			CustomConverter customConv = new CustomConverter();
			config.setTypeConverter(customConv);
			
			MappedNamespaceConvention con = new MappedNamespaceConvention(config);

			CustomMappedXMLStreamWriter xmlStreamWriter = new CustomMappedXMLStreamWriter(con, writer);

			Marshaller marshaller = getMarshaller(bindingClassName);

			marshaller.marshal(obj, xmlStreamWriter);

			return baos.toString();
		}catch(JAXBException e){
			throw new ConversionUtilsException("Exception while converting XML to JSON", e);
		}

	}



	private String replaceInvalidCharacters(byte[] content) {
		String xmlString= new String(content);
		Pattern pattern = Pattern.compile("&#[0-9]*;");
		Matcher m = pattern.matcher(xmlString);
		xmlString= m.replaceAll(" ");
		return xmlString;
	}

	/**
	 * This method used to convert JSON to XML.<br>
	Converts the jsonString(String- json format) bind to JAXB bindingClassName(String) and the converted XML is returned as a String.<br>

	 * @param jsonString
	 * @param bindingClassName
	 * @return java.lang.String 
	 * @throws ClassNotFoundException
	 * @throws JSONException
	 * @throws JAXBException
	 * @throws XMLStreamException
	 */
	public String convertJSONtoXML(String jsonString, String bindingClassName) throws ConversionUtilsException{
		try{
			jsonString = jsonString.replace("xlink/href", "xlink.href").replace("xlink/type", "xlink.type");

			JSONObject jsonObj = new JSONObject(jsonString);
			
			Configuration config = new Configuration();

			Map<String,String> xmlToJsonNamespaces= new HashMap<String,String>();
			xmlToJsonNamespaces.put("http://www.w3.org/1999/xlink", "xlink");
			config.setXmlToJsonNamespaces(xmlToJsonNamespaces);

			MappedNamespaceConvention con = new MappedNamespaceConvention(config);
			
			CustomMappedXMLStreamReader xmlStreamReader = new CustomMappedXMLStreamReader(jsonObj, con);
			
			Unmarshaller unmarshaller = getUnmarshaller(bindingClassName);
			Object obj = (Object) unmarshaller.unmarshal(xmlStreamReader);
			Object boundObj = getBindingClass(bindingClassName).cast(obj);

			Marshaller marshaller = getMarshaller(bindingClassName);
			if(bindingClassName.equalsIgnoreCase("com.hp.soar.bindings.output.schema.soar.SoarSoftwareFeed")) 
                marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,"soar-software-feed-vE4.xsd");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(baos);

			marshaller.marshal(boundObj, writer);
			
			String convertedXML= baos.toString();
			
			convertedXML = convertedXML.replace("projectref", "proj:ref").replace("projectoid", "proj:oid");
						
			return convertedXML;
		}
		catch(JAXBException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}
		catch(JSONException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}
		catch(XMLStreamException e){
			throw new ConversionUtilsException("Exception while converting JSON to XML", e);
		}

	}

	private Class<?> getBindingClass(String className) throws ConversionUtilsException{

			try{
				return Class.forName(className);  
			}catch(ClassNotFoundException e){
				throw new ConversionUtilsException("Exception while binding the classname to Class!", e);
			}
	}
	
	private Marshaller getMarshaller(String className) throws ConversionUtilsException{
			try {
				Marshaller marshaller= JAXBContextManager.getInstance().getJAXBContext(className).createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				return marshaller;
			} catch (JAXBException e) {
				throw new ConversionUtilsException("Exception while creating Marshaller for the class "+className, e);
			}
	}
	
	private Unmarshaller getUnmarshaller(String className) throws ConversionUtilsException{
		try {
			return JAXBContextManager.getInstance().getJAXBContext(className).createUnmarshaller();
		} catch (JAXBException e) {
			throw new ConversionUtilsException("Exception while creating Unmarshaller for the class "+className, e);
		}
	}
	

}
