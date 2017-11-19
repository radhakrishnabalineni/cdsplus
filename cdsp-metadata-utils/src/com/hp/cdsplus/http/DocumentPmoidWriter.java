/**
 * 
 */
package com.hp.cdsplus.http;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * @author VinodPothuru
 * Date 03/18/2014
 *
 */
public class DocumentPmoidWriter extends Thread{

	static Logger LOG = Logger.getLogger(DocumentPmoidWriter.class);
	
	static final String DOC_DETAIL = "content_doc_detail";
	static final String DOC_PMOID = "content_doc_pmoid";
	static final String PROCESSED_DOCS = "processed_docs.txt";
	static final String INVALID_DOCS = "invalid_docs.txt";
	static final String FILE_EXTN = ".txt";
	static final String FILED_SEPARATOR = "|";
	static final int fileChangeCount = 100;
	
	static boolean parsingData = true;
	String lineSeparator = System.getProperty("line.separator");
	
    File docDetailFile = null; //new File(filePath + baseDocOutputFile + fileNamePartII + outputFileExtn);
    FileWriter docDetailFileWriter = null;

    File docPmoidFile = null; 
    FileWriter docPmoidFileWriter = null;
    
    File processedDocsFile = null;
    FileWriter processedDocsFileWriter = null;

    File invalidDocsFile = null;
    FileWriter invalidDocsFileWriter = null;

    static final String docFileHeader = "master_object_name" + FILED_SEPARATOR +
	                                    "document_type" + FILED_SEPARATOR +
	                                    "content_topic" + FILED_SEPARATOR +
	                                    "content_topic_detail";
	static final String docPmoidHeader = "master_object_name" + FILED_SEPARATOR +
									     "pmoid";
	
	private String filePath = null;
	private int fileNamePart2 = 0;
	Queue<DocumentBean> pmoidQueue = null;
	Queue<String> invalidDocIdQueue = null;
		
	int objCount = 0; 
	int pmoidSize = 0;

	public DocumentPmoidWriter(String filePath, Queue<DocumentBean> pmoidQueue, Queue<String> invalidDocQ) {
		this.filePath = filePath;
		this.pmoidQueue = pmoidQueue;
		this.invalidDocIdQueue = invalidDocQ;
	}
	
	public void run() {
		
        DocumentBean dBean = null;
        
        openFiles();
        processedDocsFile = new File(filePath+PROCESSED_DOCS);
        try {
        	processedDocsFileWriter = new FileWriter(processedDocsFile);
        } catch(IOException e) {
        	LOG.error(e);
        }
        
		invalidDocsFile = new File(filePath+INVALID_DOCS);
        try {
        	invalidDocsFileWriter = new FileWriter(invalidDocsFile);
        } catch(IOException e) {
        	LOG.error(e);
        }

        synchronized (pmoidQueue){
        	pmoidSize = pmoidQueue.size();
        }

        while (pmoidSize > 0 || parsingData) {
        	
        	if (pmoidSize > 0) {
				synchronized(pmoidQueue) {
					dBean = pmoidQueue.poll();
					StatusReporter.docIdsProcessed++;
				}
				
				if (dBean != null) {
					writeDoc(dBean);
					objCount++;
				}
				if (objCount >= fileChangeCount) {
					closeFiles();
					openFiles();
					objCount = 0;
				} 
        	} else  {
	        	try {
					//LOG.info("DocumentPmoidWriter paused");
					sleep(10000);
				} catch (InterruptedException e) {
					LOG.error(e);
				}
	        }
			synchronized(pmoidQueue) {
				pmoidSize = pmoidQueue.size();
				//LOG.info("pmoidSize after poll: " + pmoidSize);
			}
			
			synchronized(invalidDocIdQueue){
				while (invalidDocIdQueue.size() > 0) {
    				try{
    					invalidDocsFileWriter.write(invalidDocIdQueue.poll());
    				} catch (IOException e) {
    					LOG.error(e);
    				}
				}
				try{
					invalidDocsFileWriter.flush();
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
        
        closeFiles();
        try {
        	processedDocsFileWriter.flush();
			processedDocsFileWriter.close();
		} catch (IOException e) {
			LOG.error("Error while closing processedDocsFileWriter",e);
		}
        try {
        	invalidDocsFileWriter.flush();
			invalidDocsFileWriter.close();
		} catch (IOException e) {
			LOG.error("Error while closing invalidDocsFileWriter", e);
		}
        //LOG.info("files closed");
        ContentMetaDataProcessorHTTP.writingData = false;
        StatusReporter.reportingTime = System.currentTimeMillis();
	}
	
	private String createFileName(String baseFN) {
		return (filePath + baseFN + "_" + String.format("%04d",fileNamePart2) + FILE_EXTN);
	}

	private void openFiles() {
		
        //create new set of files
		fileNamePart2++;
        docDetailFile = new File(createFileName(DOC_DETAIL));
        docPmoidFile = new File(createFileName(DOC_PMOID));
        
        try{
        	docDetailFileWriter = new FileWriter(docDetailFile);
        	docPmoidFileWriter = new FileWriter(docPmoidFile);
        	
        	docDetailFileWriter.write(docFileHeader + lineSeparator);
        	docPmoidFileWriter.write(docPmoidHeader + lineSeparator);
        	
        } catch(IOException e) {
        	LOG.error(e);
        }
	}
	
	private void closeFiles() {
		
        try {
        	docDetailFileWriter.flush();
        	docDetailFileWriter.close();
        	docPmoidFileWriter.flush();
        	docPmoidFileWriter.close();
        } catch (IOException e) {
        	LOG.error(e);
        }
	}
	
	private void writeDoc(DocumentBean docBean) {
		
		try {
			docDetailFileWriter.write(docBean.getId());
			docDetailFileWriter.write(FILED_SEPARATOR);
			docDetailFileWriter.write(docBean.getDocType());
			docDetailFileWriter.write(FILED_SEPARATOR);
			docDetailFileWriter.write(docBean.getContentTopic());
			docDetailFileWriter.write(FILED_SEPARATOR);
			docDetailFileWriter.write(docBean.getContentTopicDetail());
			docDetailFileWriter.write(lineSeparator);
			docDetailFileWriter.flush();
			
			for (int i=0; i<docBean.getPmoids().size(); i++){
				docPmoidFileWriter.write(docBean.getId());
				docPmoidFileWriter.write(FILED_SEPARATOR);
				docPmoidFileWriter.write(docBean.getPmoids().get(i));
				docPmoidFileWriter.write(lineSeparator);
			}
			docPmoidFileWriter.flush();
			processedDocsFileWriter.write(docBean.getId()+lineSeparator);
			//LOG.info(docBean.getId() + "done, objCount: " + objCount);
			
		} catch (IOException e) {
			LOG.error("docid: " + docBean.getId(), e);
        } catch (Exception e) {
			LOG.error("docid: " + docBean.getId(), e);
        }
	}
}
