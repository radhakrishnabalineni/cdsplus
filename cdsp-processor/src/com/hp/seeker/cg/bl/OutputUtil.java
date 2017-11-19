package com.hp.seeker.cg.bl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class OutputUtil {

	static Namespace nsProj = Namespace.getNamespace("proj", "http://www.hp.com/cdsplus");
	static Namespace nsXlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
	
	public static void saveFile(String name, File outDir,
			String contentClass, Map<String, String> groups) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(outDir, name));
		
		Document doc = new Document();
		doc.setBaseURI("http://cdsplus.austin.hp.com:80/cadence/app/");
		Element groupsElement = new Element("groups");
		groupsElement.addNamespaceDeclaration(Namespace.XML_NAMESPACE);
		groupsElement.addNamespaceDeclaration(nsXlink);
		groupsElement.addNamespaceDeclaration(nsProj);
		doc.setRootElement(groupsElement);
		
		for (String groupName : groups.keySet()) {
			Element groupElement = new Element("group");
			groupElement.addContent(groupName);
			
			groupsElement.addContent(groupElement);
		}
		
		Element link = new Element("ref", nsProj);
		link.setAttribute("type", "simple", nsXlink);
		link.setAttribute("href", contentClass + "/content/" + name.split("\\.")[0], nsXlink);
		groupsElement.addContent(link);
		new XMLOutputter(Format.getPrettyFormat()).output(doc, out);
	}

}
