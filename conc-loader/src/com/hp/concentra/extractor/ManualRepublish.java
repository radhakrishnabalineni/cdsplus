package com.hp.concentra.extractor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.hp.cks.concentra.utils.DocbaseUtils;

public class ManualRepublish {
	private static int batchSize = 50;
	private static String propertyFileName = "manual_republish.properties";
	
	private String docbaseName = null;
	private String docbaseUser = null;
	private String docbasePassword = null;
	private String docIdsFileName = null;
	private String extractionEvent = null;

	private static Properties props = new Properties();
	static{
		try {
			props.load(new FileInputStream(propertyFileName));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load properties "+e.getMessage());
		}
	}
	
	public ManualRepublish(String fileName, int intPriority) {
		docIdsFileName = fileName;
		docbaseName = props.getProperty("DOCBASE_NAME");
		docbaseUser = props.getProperty("DOCBASE_USER");
		docbasePassword = props.getProperty("DOCBASE_PASSWORD");
		extractionEvent = props.getProperty("priority_"+intPriority);
		batchSize = Integer.parseInt(props.getProperty("BATCH_SIZE"));
		if(extractionEvent == null || docbaseName == null || docbaseUser == null || docbasePassword == null){
			throw new RuntimeException("Required properties are not loaded correctly");
		}
	}

	private void republishDocuments() throws DfException {
		List<String> docIds = getDocumentIdsFromFile();
		if (!docIds.isEmpty()) {
			IDfSession session = null;
			try {
				session = DocbaseUtils.connectToDocbase(docbaseName, docbaseUser, docbasePassword);
				if (session != null) {

					if (docIds != null) {
						if (docIds.size() > batchSize) {
							for (int i = 0; i < docIds.size();) {
								List<String> docIdsSubList = null;
								if (docIds.size() > (i + batchSize)) {
									docIdsSubList = docIds.subList(i, i + batchSize);
								} else {
									docIdsSubList = docIds.subList(i, docIds.size());
								}

								insertExtractionEvents(docIdsSubList, session, extractionEvent);

								i = i + batchSize;
							}
						} else {
							if (!docIds.isEmpty()) {
								insertExtractionEvents(docIds, session,extractionEvent);
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (session != null) {
					session.disconnect();
				}
			}
		} else {
			//System.out.println("There are no document Ids to republish");
		}
	}

	private void insertExtractionEvents(List<String> docIdsSubList,
			IDfSession session, String event) {
		IDfCollection results = null;
		try {

			String docIdsSqlInString = getDocIdsSqlInString(docIdsSubList);
			String dql = "insert into c_event_log "
					+ "(chronicle_id, object_id, object_name, object_version, event, event_date, user_name, description, attachment_id, user_comment)"
					+ " select doc.i_chronicle_id, doc.r_object_id, doc.object_name, r.r_version_label, '"
					+ event
					+ "' as event, date(now) as event_date, 'cdsplus' as user_name, 'Manual Republish' as description, '0000000000000000' as attachment_id, "
					+ "'Manual Republish' as user_comment from c_base_object (ALL) doc, dm_sysobject_r r  where object_name in ("
					+ docIdsSqlInString
					+ ") and any r_version_label='FINAL' "
					+ "and archived_flag=0 and r.r_object_id=doc.r_object_id and r.i_position = -1";
			//System.out.println("UPDATE Event Query :" + dql);
			results = DocbaseUtils.executeQuery(session, dql,DfQuery.EXEC_QUERY);
			int count = 0;
			if (results != null && results.next()) {
				count = results.getValueAt(0).asInteger();
                //System.out.println(count+" rows inserted for UPDATE event");
            }
			results.close();
			if(count < docIdsSubList.size()){
				dql = "insert into c_event_log "
						+ "(chronicle_id, object_id, object_name, object_version, event, event_date, user_name, description, attachment_id, user_comment)"
						+ " select doc.i_chronicle_id, doc.r_object_id, doc.object_name, r.r_version_label, 'UIDOCARCHD' as event, date(now) as event_date, "
						+ "'cdsplus' as user_name, 'Manual Republish' as description, '0000000000000000' as attachment_id, 'Manual Republish' as user_comment "
						+ "from c_base_object (ALL) doc, dm_sysobject_r r  where object_name in ( "
						+ docIdsSqlInString
						+ " ) and any r_version_label='FINAL' "
						+ "and archived_flag=1 and r.r_object_id=doc.r_object_id and r.i_position = -1";
				//System.out.println("DELETE Event Query :" + dql);
				results = DocbaseUtils.executeQuery(session, dql,DfQuery.EXEC_QUERY);
				if (results != null && results.next()) {
					count = results.getValueAt(0).asInteger();
					//System.out.println(count+" rows inserted for DELETE event");
	            }
			}
		} catch (DfException e) {
			e.printStackTrace();
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (DfException e) {
				}
			}
		}
	}

	private String getDocIdsSqlInString(List<String> docIdsSubList) {
		String inStr = "";
		for (String id : docIdsSubList) {
			inStr += "'" + id + "',";
		}
		if (!inStr.trim().equals("")) {
			inStr = inStr.substring(0, inStr.length() - 1);
		}
		return inStr;
	}

	private List<String> getDocumentIdsFromFile() {
		BufferedReader reader = null;
		List<String> docIds = new ArrayList<String>();
		try {
			File docIdsFile = new File(docIdsFileName);
			reader = new BufferedReader(new FileReader(docIdsFile));
			String docId = reader.readLine();

			while (docId != null) {
				docIds.add(docId.trim());
				docId = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return docIds;
	}

	public static void main(String[] args) throws DfException {
		if (args.length != 2) {
			System.err.println("Incorrecr Usage :");
			System.err
					.println("com.hp.concentra.extractor.ManualRepublish <doc_id_file> <int_priority>");
			System.exit(-1);
		}
		String fileName = args[0];
		String priority = args[1];
		int intPriority = 1;
		if (!new File(fileName).exists()) {
			System.err.println("File " + fileName + " does not exist");
			System.exit(-1);
		}
		try {
			intPriority = Integer.parseInt(priority);
			if (intPriority > 3) {
				intPriority = 3;
			}
		} catch (Exception e) {
			System.err.println("Invalid priority " + priority
					+ "; Should be 0,1,2 or 3");
			System.exit(-1);
		}

		ManualRepublish manRepublish = new ManualRepublish(fileName, intPriority);
		manRepublish.republishDocuments();
		//System.out.println("Manual Republish Trigger Completed");
	}

}
