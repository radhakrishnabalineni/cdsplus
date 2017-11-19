package com.hp.cdsplus.wds.utils;

public interface IDestinationConstants {
	  //project.spacedog.wds.Shell
	  public static final String FROM_ADDR	= "from_addr";
	  public static final String TO_ADDR 	= "to_addr";
	  public static final String SMTP_HOST 	= "smtp_host";
	  public static final String SMTP_PORT 	= "smtp_port";
	  
	  //project.spacedog.wds.RemoteDestination
	  public static final String NAMED_CONFIG_ELE_NAME 	= "namedDestination";
	  public static final String CONFIG_ELE_NAME 		= "remoteDestination";
	  
	  //project.spacedog.wds.Selection
	  public static final String SELECTION_CONFIG_ELE_NAME = "selection";
	  
	  //project.spacedog.wds.Manifest
	  public static final String POST_SERVER_PATH 	= "/cadence/app/";
	  
	  //project.spacedog.wds.PackageDestination
	  public static final String PAC_NAME_SEPERATOR = "_";
	  public static final String TMP_EXTENSION 		= ".temp";
	  public static final String ZIP_EXTENSION 		= ".zip";
	  public static final String TEMP_ZIP_FILENAME 	= "zippackage" + TMP_EXTENSION;
	  public static final String NAME_ATTR 			= "name";
	  public static final String MAX_SIZE_ATTR 		= "maxSize";
	  public static final String TEMP_DIR_ATTR 		= "tempDir";
	  public static final String MAX_COUNT_ATTR 	= "maxCount";
	  public static final String OUTPUT_DIR_ATTR 	= "outputDir";
	  public static final int IO_CHUNK_SIZE 		= 1024 * 8; //8k
	  
	  //project.spacedog.wds.ReleaseManagerDestination
	  public static final String WEBLIST_HEADER = 	"# Weblist file\n"
		  	+"#\n# Weblist file\n" 
		  	+"#\n# TYPE\t\tDescription\n"
		  	+"#\n# CGI\t\tCGI Script (everything in cgi-bin)\n"
			+"# HTML\t\tAn HTML Document (.html or .htm)\n"
			+"# TEXT\t\tA text Document (.txt or .text)\n"
			+"# MAP\t\tImagemap coordibate file using NEW imagemap utility\n"
			+"# MAP1\t\tImagemap coordibate file using old imagemap utility\n"
			+"# ASIS\t\tLeave this file as is. (.gif, .jpg, .exe, .bin, ...etc.)\n"
			+"# OBS\t\tObsolete File\n"
			+"# DEV\t\tWork in progress\n"
			+"# \n";

	  public static final int MAX_PACKAGE_SIZE = 100 * 1024 * 1024;
	  
	  //project.spacedog.wds.Subscription
	  public static final String SUBSCRIPTION_CONFIG_ELE_NAME = "sub";
	  public static final String SUBSCRIPTION_ATTR_NAME = "subscription";
	  public static final int SUBSCRIPTION_OUTPUT_BLOCK_SIZE = 4096;
}
