package com.hp.cdsplus.conversion;

import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.hp.cdsplus.conversion.exception.ConversionUtilsException;

public class JAXBContextManager {
	
	private ConcurrentHashMap<String, JAXBContext> jaxbContextCache;
	private static JAXBContextManager jaxbContextMgr;
	
	private JAXBContextManager(){
		jaxbContextCache = new ConcurrentHashMap<String, JAXBContext>();
	}
	
	public void createJAXBContext(String javaClassName) throws ConversionUtilsException{	
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Class.forName(javaClassName));	
			jaxbContextCache.put(javaClassName, jaxbContext);
		} catch (JAXBException e) {
			throw new ConversionUtilsException("Exception initializing JAXBContext object", e);
		} catch (ClassNotFoundException e) {
			throw new ConversionUtilsException("Exception initializing JAXBContext object", e);
		}
	}
	
	public JAXBContext getJAXBContext(String javaClassName) throws ConversionUtilsException{
		JAXBContext jc = jaxbContextCache.get(javaClassName);
		if(jc==null){
			createJAXBContext(javaClassName);
		}
		return jaxbContextCache.get(javaClassName);
	}
	
	public static JAXBContextManager getInstance(){
		if(jaxbContextMgr == null){
			jaxbContextMgr = new JAXBContextManager();
		}
		return jaxbContextMgr;
	}
}
