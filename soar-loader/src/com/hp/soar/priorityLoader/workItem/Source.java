package com.hp.soar.priorityLoader.workItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class Source {

	public abstract String getName();
	
	public abstract InputStream getInputStream() throws FileNotFoundException, IOException;
	
}
