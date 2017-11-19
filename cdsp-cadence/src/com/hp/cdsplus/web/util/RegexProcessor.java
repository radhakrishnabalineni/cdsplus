package com.hp.cdsplus.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexProcessor {
	
//	public static String replaceAll(String originalString,String regex,String replacement){
//		
//		return originalString.replaceAll(regex, replacement);		
//		
//	}
//	
	public static String[] replaceAll(String originalString,String regex,String replacement){
		regex = "^"+regex+"$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(originalString);
		String response[] = new String[2];
		
		if(matcher.find()){
			response[0]=matcher.replaceAll(replacement);;
			response[1]="matched";
			return response;
		}
		response[0]=originalString;
		response[1]=null;
		return response;
		
	}
	
	public static String[] replaceAllNotMatching(String originalString,String regex,String replacement){
		if(originalString.endsWith("\\")){
			originalString = originalString+"\\";
		}
		regex = "^"+regex+"$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(originalString);
		String response[] = new String[2];
		if(!matcher.matches()){
			originalString = originalString.replaceAll("\\$", "\\\\\\$");
			response[0]=replacement.replaceAll("\\$1", originalString);
			response[1]="matched";
			return response;			 
		}		
		response[0]=originalString;
		response[1]=null;
		return response;			
	}
	
	public static String[] replaceAll(String originalString,String searchPattern,String regex,String replacement){
		regex = "^"+regex+"$";
		searchPattern = "^"+searchPattern+"$";
		Pattern pattern = Pattern.compile(searchPattern);
		Matcher matcher = pattern.matcher(originalString);
		String response[] = new String[2];
		if(matcher.find()){
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(originalString);
			response[0]=matcher.replaceAll(replacement);
			response[1]="matched";
			return response;	
			
		}
		response[0]=originalString;
		response[1]=null;
		return response;
		
	}	

}
