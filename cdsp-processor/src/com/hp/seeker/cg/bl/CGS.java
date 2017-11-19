package com.hp.seeker.cg.bl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.hp.seeker.cg.bl.sre.CDSPlusBuilder;
import com.hp.seeker.cg.bl.sre.MDProperties;
import com.hp.seeker.cg.bl.sre.RuleException;
import com.hp.seeker.cg.bl.sre.RuleSet;
import com.hp.seeker.cg.bl.sre.SREngine;

public class CGS {
	private static final Logger logger = LogManager.getLogger(CGS.class);
	private SREngine ruleEngine;
	private static CGS cgsInstance;
	
	private CGS() {
		ruleEngine = SREngine.getEngine();
	}
	
	public static final CGS getInstance(){
		if(cgsInstance == null){
			cgsInstance = new CGS();
		}
		return cgsInstance;
	}
	
	public boolean isRulesModified(String ruleSet) throws RuleException{
		boolean retVal = false;
		RuleSet rules = ruleEngine.getRules(ruleSet);		
		if ((rules == null) || rules.hasChanged()) {
			ruleEngine.loadRules(ruleSet);
			// load the rules
			retVal = (rules != null);
		}
		return retVal;
	}
	
	public void loadRules(String ruleSet) throws RuleException{
		ruleEngine.loadRules(ruleSet);
	}

	public void loadRules(String ruleSet, File file) throws RuleException{
		ruleEngine.loadRules(ruleSet, file);
	}
	/**
	 * this method will process the incoming file and populate a groups xml
	 *  and save it into outDir
	 * @param file - the file that needs to be processed
	 * @param outDir - location where the groups xml is saved
	 * @return Map
	 * 			returns a map containing the group list for the input file
	 * @throws RuleException
	 * 			in case of failures while applying rules on the xml this 
	 * would be thrown
	 * @throws IOException
	 * 			In case rule file is not found this exception is thrown.
	 */
	public Map<String, String> processFile(File file, File outDir,String ruleSet) throws RuleException, IOException {
		InputStream inputStream = new FileInputStream(file);
		MDProperties md = getMDProperties(inputStream);
		Map<String, String> groups = ruleEngine.evaluateRules(ruleSet, md);
		OutputUtil.saveFile(file.getName(), outDir, "support", groups);
		return groups;
	}
	
	/**
	 * 
	 * @param xmlDoc - xml document on which cgs rules needs to be applied
	 * @return
	 * @throws RuleException
	 * 			in case of failures while applying rules on the xml this 
	 * would be thrown
	 * @throws IOException
	 * 			In case rule file is not found this exception is thrown.
	 * @throws TransformerException
	 * 			
	 */
	
	public Map<String, String> process(Node xmlDoc,String ruleSet)throws RuleException, IOException, TransformerException {
		long start = System.currentTimeMillis();
		MDProperties md = getMDProperties(xmlDoc);
		Map<String, String> retVal = ruleEngine.evaluateRules(ruleSet, md);
		logger.info("Rules ProcessingTime: "+(System.currentTimeMillis() - start));
		return retVal;
	}
	
	
	/**
	 * 
	 * @param xmlDoc -loaded doc used to get the rules
	 * @return
	 * @throws RuleException
	 * 			in case of failures while applying rules on the xml this would be thrown
	 * @throws IOException
	 * 			In case rule file is not found this exception is thrown.
	 */
	private MDProperties getMDProperties(Node xmlDoc){
		CDSPlusBuilder mdbuilder = new CDSPlusBuilder();
		return mdbuilder.build(xmlDoc);
	}
	
	/**
	 * 
	 * @param inputStream -file will be transformed 
	 * 						to an input stream before applying rules
	 * @return
	 * @throws RuleException
	 * 			in case of failures while applying rules on the xml this 
	 * would be thrown
	 * @throws IOException
	 * 			In case rule file is not found this exception is thrown.
	 */
	private MDProperties getMDProperties(InputStream inputStream)
		throws IOException, RuleException {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(inputStream);			
				CDSPlusBuilder mdbuilder = new CDSPlusBuilder();
			return mdbuilder.build(doc.getRootElement());
			} catch (JDOMException e) {
				throw new RuleException(e);
			}
	}
	
	/**
	 * 
	 * @param inputStream -file will be transformed 
	 * 						to an input stream before applying rules
	 * @return
	 * @throws RuleException
	 * 			in case of failures while applying rules on the xml this 
	 * would be thrown
	 * @throws IOException
	 * 			In case rule file is not found this exception is thrown.
	 */
	private MDProperties getMDProperties(Element root)
		throws RuleException {
			CDSPlusBuilder mdbuilder = new CDSPlusBuilder();
			return mdbuilder.build(root);
	}
	
	/**
	 * 
	 * @param inDir - all files under this directory would be considered for applying rules
	 * @param outDir- all groups xml will be pushed to this directory
	 * @throws RuleException
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void processDir(File inDir, File outDir,String ruleSet) throws RuleException, JDOMException, IOException {
		for (File file : inDir.listFiles(new XMLFilenameFilter())) {
			processFile(file, outDir,ruleSet);
		}
	}
	
	public Map<String, String> process(String xml, String ruleSet)throws RuleException, IOException, TransformerException, JDOMException {

		StringReader reader = new StringReader(xml);
		InputSource source = new InputSource(reader);
		source.setEncoding("UTF-8");
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(source);
		
		MDProperties md = getMDProperties(doc.getRootElement());
		
		Map<String, String> retVal = ruleEngine.evaluateRules(ruleSet, md);
		
		return retVal;
	}

	public Map<String, String> process(Document xml, String ruleSet) throws RuleException {
		long start = System.currentTimeMillis();
		MDProperties md = getMDProperties(xml.getRootElement());
		Map<String, String> retVal = ruleEngine.evaluateRules(ruleSet, md);
		logger.info("Rules ProcessingTime: "+(System.currentTimeMillis() - start));
		return retVal;
	}

}
