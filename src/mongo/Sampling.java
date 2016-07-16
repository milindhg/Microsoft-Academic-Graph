/**
 * @author Milind Gokhale and Renuka Deshmukh
 * This class primarily does the work of sampling.
 * We started with a random sample of 1,000 papers and 
 * fetched corresponding data for these papers from other tables. 
 * 
 * Date : April 15, 2016
 * 
 */
package mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Sampling {

	// forwarding ports
	private static final String LOCAL_HOST = "localhost";
	private static final String REMOTE_HOST = "tanner.ils.indiana.edu";
	private static final Integer LOCAL_PORT = 8989;
	private static final Integer REMOTE_PORT = 27087; // Default mongodb port

	// ssh connection info
	private static final String SSH_USER = "********";
	private static final String SSH_PASSWORD = "****************";
	private static final String SSH_HOST = "tanner.ils.indiana.edu";
	private static final Integer SSH_PORT = 22;

	private static Session SSH_SESSION;

	private static final int BATCH_WRITE_SIZE = 500;
	private static final int TOTAL_RECORDS_SIZE = 1000000;

	public static void main(String[] args) throws JSchException {

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		SSH_SESSION = null;
		SSH_SESSION = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
		SSH_SESSION.setPassword(SSH_PASSWORD);
		SSH_SESSION.setConfig(config);
		SSH_SESSION.connect();
		SSH_SESSION.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);

		// String dbURI =
		// "mongodb://mgokhale:Nbaonline1!MHG>Going>To>IUB@tanner.ils.indiana.edu:27087";
		// MongoClient mc = new MongoClient(new MongoClientURI(dbURI));

		getLastDocument("testFinalMSDB", "PaperCollection");

		CreatePaperDocument();

	}

	private static void getLastDocument(String string, String string2) {
		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		MongoDatabase finalDB = mc.getDatabase("testFinalMSDB");
		MongoCollection<Document> finalColl = finalDB
				.getCollection("PaperCollection");
		MongoCursor<Document> finalPapersCursor = finalColl.find()
				.sort(new Document("_id", -1)).limit(1).iterator();
		Document last = finalPapersCursor.next();
		System.out.println("Last record: " + last);
		finalPapersCursor.close();
		mc.close();
	}

	/**
	 * 
	 * This is the main function for creating the sample from the complete
	 * dataset and store its entries in a new table. This sampling is done in an
	 * incremental manner.
	 * 
	 */
	public static void CreatePaperDocument() {
		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		MongoDatabase db = mc.getDatabase("microsoftdb");

		mc.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

		MongoCollection<Document> papersColl = db.getCollection("Papers");
		MongoCollection<Document> keywdColl = db.getCollection("PaperKeywords");
		MongoCollection<Document> paperRefsColl = db.getCollection("PaperRefs");
		MongoCollection<Document> pauthColl = db
				.getCollection("PaperAuthorAffiliations");

		MongoDatabase finalDB = mc.getDatabase("testFinalMSDB");
		MongoCollection<Document> finalColl = finalDB
				.getCollection("PaperCollection");

		// commented to make it incremental inserts
		// finalColl.drop();

		// commented first simple find iterator to get the last record
		// MongoCursor<Document> papersCursor = papersColl.find().iterator();
		Document lastRecordFinalPaperColl = finalColl.find()
				.sort(new Document("_id", -1)).limit(1).iterator().next();
		Document lastRecord_idFromPapers = papersColl
				.find(new Document("PId", lastRecordFinalPaperColl.get("PId")))
				.iterator().next();
		ObjectId _idOfLastRecord = lastRecord_idFromPapers.getObjectId("_id");
		MongoCursor<Document> papersCursor = papersColl.find(
				new Document("_id", new Document("$gt", _idOfLastRecord)))
				.iterator();

		ArrayList<Paper> papers = new ArrayList<>();
		int currDoc = 0;
		int totalRecordsCount = 0;

		while (papersCursor.hasNext() && totalRecordsCount < TOTAL_RECORDS_SIZE) {
			Document doc = papersCursor.next();

			Paper paper = new Paper();
			paper.setPaperId(String.valueOf(doc.get("PId")));
			paper.setPublishYear((int) doc.get("PublYear"));
			paper.setPaperRank((int) doc.get("PRank"));

			String bval = String.valueOf(doc.get("ConfId_to_VenueName"));
			if (bval != null && !bval.isEmpty())
				paper.setInConference(true);

			bval = String.valueOf(doc.get("JId_to_VenueName"));
			if (bval != null && !bval.isEmpty())
				paper.setInJournal(true);

			Document kwquery = new Document("PId", paper.getPaperId());
			MongoCursor<Document> kwCursor = keywdColl.find(kwquery).iterator();

			StringBuffer kwsb = new StringBuffer();
			StringBuffer fossb = new StringBuffer();
			while (kwCursor.hasNext()) {
				Document kwdoc = kwCursor.next();
				kwsb.append(String.valueOf(kwdoc.get("KWName")));
				kwsb.append("|");
				fossb.append(String.valueOf(kwdoc.get("FOSId_to_KW")));
				fossb.append("|");
			}
			kwCursor.close();
			paper.setFieldOfStudy(fossb.toString());
			paper.setKeywords(kwsb.toString());

			Document refquery = new Document("PId", paper.getPaperId());
			MongoCursor<Document> refCursor = paperRefsColl.find(refquery)
					.iterator();
			StringBuffer prefsb = new StringBuffer();

			while (refCursor.hasNext()) {
				Document refdoc = refCursor.next();
				prefsb.append(String.valueOf(refdoc.get("PRefId")));
				prefsb.append("|");
			}
			paper.setPaperRefIds(prefsb.toString());
			refCursor.close();

			Document authquery = new Document("PId", paper.getPaperId());
			MongoCursor<Document> authCursor = pauthColl.find(authquery)
					.iterator();
			StringBuffer authsb = new StringBuffer();

			while (authCursor.hasNext()) {
				Document authdoc = authCursor.next();
				authsb.append(String.valueOf(authdoc.get("AuthId")));
				authsb.append("|");
			}
			paper.setAuthors(authsb.toString());
			authCursor.close();

			papers.add(paper);

			// System.out.println(doc);
			currDoc++;
			totalRecordsCount++;

			// Batch write condition to commit currently held records
			if (currDoc == BATCH_WRITE_SIZE) {
				ArrayList<Document> docPapers = new ArrayList<Document>();
				for (Paper currPaper : papers) {
					docPapers.add(currPaper.getBsonDoc());
				}

				finalColl.insertMany(docPapers);
				System.out.println(totalRecordsCount
						+ " total records committed");

				papers = new ArrayList<>();
				currDoc = 0;
				ObjectId _idLast = doc.getObjectId("_id");
				System.out.println("Last Obj Id was _id=" + _idLast);
				papersCursor.close();
				papersCursor = papersColl.find(
						new Document("_id", new Document("$gt", _idLast)))
						.iterator();
			}

		}
		papersCursor.close();
		mc.close();
	}

	public static HashMap<Integer, Integer> YearWiseTrend(String keyword) {
		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		MongoDatabase db = mc.getDatabase("microsoftdb");

		mc.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

		MongoCollection<Document> papersColl = db.getCollection("Papers");
		MongoCollection<Document> keywdColl = db.getCollection("PaperKeywords");
		MongoCursor<Document> keywdCollCursor = keywdColl.find(
				new Document("KWName", keyword)).iterator();

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		while (keywdCollCursor.hasNext()) {
			Document doc = keywdCollCursor.next();
			String paperId = String.valueOf(doc.get("PId"));
			MongoCursor<Document> paperCursor = papersColl.find(
					new Document("PId", paperId)).iterator();
			if (paperCursor.hasNext() && Utilities.isInteger(paperId)) {
				Document paper = paperCursor.next();
				int paperYear = paper.getInteger("PublYear", 0);
				if (paperYear != 0) {
					if (map.containsKey(paperYear)) {
						map.put(paperYear, map.get(paperYear));
					} else {
						map.put(paperYear, 1);
					}
				}
				paperCursor.close();
				System.out.println("Processed Paper: " + paperId);
			}
		}
		mc.close();
		return map;
	}
}
