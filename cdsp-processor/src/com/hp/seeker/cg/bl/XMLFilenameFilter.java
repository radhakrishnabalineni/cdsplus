package com.hp.seeker.cg.bl;

import java.io.File;
import java.io.FilenameFilter;

public class XMLFilenameFilter implements FilenameFilter {
	
	public boolean accept(File dir, String name) {
		return name.endsWith(".xml");
	}
	
}
