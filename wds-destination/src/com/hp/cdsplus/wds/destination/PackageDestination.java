package com.hp.cdsplus.wds.destination;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.cdsplus.util.xml.XMLUtils;
import com.hp.cdsplus.wds.exception.DestinationException;
import com.hp.cdsplus.wds.exception.NonRetryException;
import com.hp.loader.utils.ThreadLog;

@SuppressWarnings("deprecation")
public class PackageDestination implements IDestination {

	private long max_zip_size = 0;
	private long max_zip_limit = 0;
	private long stageTotalSize = 0;
	private String subscription_name;

	private static long lastTime = 0;

	private ConcurrentHashMap<String, byte[]> stageMap = new ConcurrentHashMap<String, byte[]>();
	private File outputDir;
	private String serverName;

	public void init(Element root, File workingDir) {

		subscription_name = XMLUtils.nullCheck("subscription Name",
				XMLUtils.getAttributeValue(root, "subName"));
		ThreadLog.info("subscription name - " + subscription_name);
		String outDirString = XMLUtils.nullCheck("output directory",
				XMLUtils.getAttributeValue(root, "outputDir"));
		ThreadLog.info("output dir - " + outDirString);
		outputDir = new File(outDirString);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		String max_size = XMLUtils.getAttributeValue(root, "maxSize");
		if (max_size != null && max_size != "")
			max_zip_size = Long.parseLong(max_size);

		max_zip_size = (max_zip_size == 0) ? (1024 * 1024) : max_zip_size
				* (1024 * 1024);
		ThreadLog.info("Max Package size set to - " + max_zip_size);

		String max_count = XMLUtils.getAttributeValue(root, "maxCount");
		if (max_count != null && max_count != "")
			max_zip_limit = Long.parseLong(max_count);
		max_zip_limit = (max_zip_limit == 0) ? 1000 : max_zip_limit;
		ThreadLog.info("Max Package count set to - " + max_zip_limit);
		serverName = System.getProperty("spacedog_cdsp_server_url");

		// Since Server name is mandatory, setting to CDSP Houston URL by
		// default
		if (serverName == null) {
			serverName = "http://cdsplus.houston.hp.com/cadence/app/";
		}
	}

	/**
	 * 
	 */
	public boolean put(String location, byte[] bytes)
			throws DestinationException {
		// boolean returnVal = false;
		stageTotalSize += bytes.length;
		stageMap.put(location, bytes);
		// ThreadLog.info("adding to map - "+location);
		return (stageMap.size() >= max_zip_limit || stageTotalSize > max_zip_size);
	}

	public synchronized boolean finalizeDest(boolean force)
			throws DestinationException {
		if ((force && !stageMap.isEmpty())
				|| (stageMap.size() >= max_zip_limit || stageTotalSize > max_zip_size)) {
			// has either reached the last event (force==true) and there is
			// something to be sent or reached either of the zip file size
			// limits
			return finishTar();
		} else {
			// return true if force and there isn't anything to save, otherwise
			// false
			return force ? true : false;
		}
	}

	public synchronized boolean finishTar() throws DestinationException {
		long tempName = getLastTime();
		int pkgSize = stageMap.size();
		File zipFile = new File(outputDir, subscription_name + "_" + tempName);
		ZipOutputStream zos;
		ZipEntry ze = null;

		Iterator<String> itr = null;

		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));
			itr = stageMap.keySet().iterator();
			while (itr.hasNext()) {
				String name = itr.next();
				byte[] content = stageMap.remove(name);
				ze = new ZipEntry(name);
				ze.setSize(content.length);
				zos.putNextEntry(ze);
				zos.write(content);
				zos.closeEntry();
			}
			zos.finish();
			zos.close();
			zipFile.renameTo(new File(outputDir, subscription_name + "_"
					+ tempName + ".zip"));
			ThreadLog.debug("Packaged " + pkgSize + " files into "
					+ zipFile.getName());

			// reset the stageTotalSize as it just wrote out the file
			stageTotalSize = 0;
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new NonRetryException("File not found - " + zipFile, e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NonRetryException(e);
		}
	}

	/**
	 * @throws RecoverableException
	 * @throws NonRecoverableException
	 * @throws
	 * 
	 */
	public boolean remove(String location) throws DestinationException {
		// boolean returnVal = false;
		OutputFormat format1;
		ByteArrayOutputStream bos = null;
		Document doc = XMLUtils.newDocument();
		doc = getDeleteDocument(doc);
		byte insBytes[]=null;
		try{
			bos = new ByteArrayOutputStream();
			  XMLSerializer serializer = new XMLSerializer();
			  serializer.setOutputByteStream(bos);
			  serializer.asDOMSerializer();
			  serializer.serialize(doc);
			  insBytes = bos.toByteArray();
		  } catch (IOException e) {
			  throw new NonRetryException(e);
		}
		  finally {
			  if(bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					throw new NonRetryException(e);
				}
		  }
		Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			format1 = new OutputFormat(doc);
			format1.setLineSeparator("\r\n");
			format1.setIndenting(false);
			format1.setLineWidth(0);
			format1.setPreserveSpace(true);

			//DOMSource source = new DOMSource(doc);
			StreamSource source = new StreamSource(new ByteArrayInputStream(insBytes));
			DOMResult result = new DOMResult();
			xformer.transform(source, result);
			Document doc1 = (Document) result.getNode();
			
			bos = new ByteArrayOutputStream();
			XMLSerializer serializer = new XMLSerializer(bos, format1);
			serializer.asDOMSerializer();
			serializer.serialize(doc1);
			stageMap.put(location, bos.toByteArray());
		} catch (TransformerConfigurationException e) {
			throw new NonRetryException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new NonRetryException(e);
		} catch (TransformerException e) {
			throw new NonRetryException(e);
		}

		catch (IOException e) {
			throw new NonRetryException(e);
		} finally {
			try {
				if (bos != null)
					bos.close();

			} catch (IOException e) {
				throw new NonRetryException(e);
			}
		}
		return (stageMap.size() >= max_zip_limit || stageTotalSize > max_zip_size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.cdsplus.wds.destination.IDestination#remove(java.lang.String,
	 * javax.xml.transform.Templates)
	 */
	public boolean remove(String location, Templates sTemplates)
			throws DestinationException {
		Document doc = XMLUtils.newDocument();
		OutputFormat format1;
		ByteArrayOutputStream bos=null;
		Transformer transformer;
		doc = getDeleteDocument(doc);
		byte insBytes[]=null;
		try{
			bos = new ByteArrayOutputStream();
			  XMLSerializer serializer = new XMLSerializer();
			  serializer.setOutputByteStream(bos);
			  serializer.asDOMSerializer();
			  serializer.serialize(doc);
			  insBytes = bos.toByteArray();
		  } catch (IOException e) {
			  throw new NonRetryException(e);
		}
		  finally {
			  if(bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					throw new NonRetryException(e);
				}
		  }
		try {
			format1 = new OutputFormat(doc);
			format1.setLineSeparator("\r\n");
			format1.setIndenting(false);
			format1.setLineWidth(0);
			format1.setPreserveSpace(true);

			DOMResult result = new DOMResult();
			//DOMSource source = new DOMSource(doc);
			StreamSource source = new StreamSource(new ByteArrayInputStream(insBytes));
			transformer = sTemplates.newTransformer();
			transformer.transform(source, result);
			
			Document doc1 = (Document) result.getNode();
			bos = new ByteArrayOutputStream();
			XMLSerializer serializer = new XMLSerializer(bos, format1);
			serializer.asDOMSerializer();
			serializer.serialize(doc1);

			stageMap.put(location, bos.toByteArray());
		} catch (TransformerConfigurationException e) {
			throw new NonRetryException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new NonRetryException(e);
		} catch (TransformerException e) {
			throw new NonRetryException(e);
		} catch (IOException e) {
			throw new NonRetryException(e);
		} finally {
			try {
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				throw new NonRetryException(e);
			}
		}

		return (stageMap.size() >= max_zip_limit || stageTotalSize > max_zip_size);
	}

	/**
	 * @return
	 */
	public synchronized static long getLastTime() {
		long x = System.currentTimeMillis();
		if (x <= lastTime) {
			x = lastTime + 1;
		}
		lastTime = x;
		return lastTime;
	}

	/**
	 * set needStoreSync
	 */
	public boolean needStoreSync() {
		return true;
	}

	/**
	 * This method prepares document with delete action
	 * 
	 * @param doc
	 * @return
	 */
	private Document getDeleteDocument(Document doc) {
		Element root = doc.createElement("document");
		root.setAttribute("xml:base", serverName);
		root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		root.setAttribute("xmlns:proj", "http://www.hp.com/cdsplus");
		Element childAction = doc.createElement("action");
		org.w3c.dom.Text text = doc.createTextNode("delete");
		childAction.appendChild(text);
		root.appendChild(childAction);
		doc.appendChild(root);
		return doc;
	}
}
