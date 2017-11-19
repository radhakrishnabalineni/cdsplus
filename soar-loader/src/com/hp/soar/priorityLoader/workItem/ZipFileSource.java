package com.hp.soar.priorityLoader.workItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileSource extends Source {
	ZipFile	 zf;
	ZipEntry ze;
	
	public ZipFileSource(ZipFile zf, ZipEntry ze) {
		super();
		this.zf = zf;
		this.ze = ze;
	}

	@Override
	public String getName() {
		// 
		return ze.getName();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is = zf.getInputStream(ze);
		if (is == null) {
			throw new FileNotFoundException("No zip entry for file: "+ze.getName());
		}
		return zf.getInputStream(ze);
	}

}
