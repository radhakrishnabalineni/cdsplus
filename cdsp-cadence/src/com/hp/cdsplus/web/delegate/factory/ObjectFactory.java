package com.hp.cdsplus.web.delegate.factory;

import com.hp.cdsplus.web.contentservice.ContentService;
import com.hp.cdsplus.web.exception.ApplicationException;
import com.hp.cdsplus.web.util.ServiceConstants;

public class ObjectFactory {

	 	//use getServiceInstance method to get object of type ContentService 
	public ContentService getServiceInstance(String contentType) {
	      
	   String classFullPath = "com.hp.cdsplus.web.contentservice.impl";
      
      final StringBuilder str = new StringBuilder(contentType.length());
      str.append(Character.toUpperCase(contentType.charAt(0))).append(contentType.substring(1));
      
      String customClassName = str.toString() + "ServiceImpl";
      
      classFullPath = classFullPath + "." + customClassName;
      
      Class<?> c = null;
      try
      {
          c = Class.forName(classFullPath);
          return (ContentService) c.newInstance();
      }
      catch(ClassNotFoundException cnfEx)
      {
    	  throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " " + contentType
							+ " " + ServiceConstants.errorMsg_NotFound);
      }
      catch(SecurityException se)
      {
    	  throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " " + contentType
							+ " " + ServiceConstants.errorMsg_NotFound);
      }
      catch(InstantiationException ise)
      {
    	  throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " " + contentType
							+ " " + ServiceConstants.errorMsg_NotFound);
      }
      catch(IllegalAccessException ie)
      {
    	  throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " " + contentType
							+ " " + ServiceConstants.errorMsg_NotFound);
      }
      catch(IllegalArgumentException iae)
      {
    	  throw new ApplicationException(
					ServiceConstants.errorMsg_The_Entry + " " + contentType
							+ " " + ServiceConstants.errorMsg_NotFound);
      }
   }
}
