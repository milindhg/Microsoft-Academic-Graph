/**
 * @author Milind Gokhale
 * This class primarily converts the json input file to separate csv files 
 * as per the tables so that they can be imported as nodes and relationships 
 * in neo4j database.
 * 
 * Date : April 20, 2016
 * 
 */
package mongo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class JsonToNeo4J {
	// forwarding ports
	private static final String LOCAL_HOST = "localhost";
	private static final String REMOTE_HOST = "tanner.ils.indiana.edu";
	private static final Integer LOCAL_PORT = 8988;
	private static final Integer REMOTE_PORT = 27087; // Default mongodb port

	// ssh connection info
	private static final String SSH_USER = "********";
	private static final String SSH_PASSWORD = "****************";
	private static final String SSH_HOST = "tanner.ils.indiana.edu";
	private static final Integer SSH_PORT = 22;

	private static Session SSH_SESSION;

	private static final int BATCH_WRITE_SIZE = 500;
	private static final int TOTAL_RECORDS_SIZE = 50000;
	private static final char separator = '$';
	private static final String separatorString = "$";
	public static File file_paper = new File("F:/Paper.csv");
	public static File file_papernames = new File("F:/PaperNames.csv");
	public static File file_paper_author = new File("F:/Paper_Author.csv");
	public static File file_paper_FOS = new File("F:/Paper_FOS.csv");
	public static File file_paper_KW = new File("F:/Paper_KW.csv");
	public static File file_paper_References = new File(
			"F:/Paper_References.csv");

	public static void main(String[] args) throws JSchException, IOException {
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		SSH_SESSION = null;
		SSH_SESSION = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
		SSH_SESSION.setPassword(SSH_PASSWORD);
		SSH_SESSION.setConfig(config);
		SSH_SESSION.connect();
		SSH_SESSION.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);

		// CreatePaperDocument();
		getPaperNames();
	}

	/**
	 * This is the main function that contains the main logic of fetching the
	 * data from the sampled table and prepares separate csv files to facilitate
	 * import in neo4j
	 * 
	 * @throws IOException
	 */
	public static void CreatePaperDocument() throws IOException {
		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		MongoDatabase db = mc.getDatabase("testFinalMSDB");

		mc.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

		MongoCollection<Document> papersColl = db
				.getCollection("PaperCollection");

		MongoCursor<Document> papersCursor = papersColl.find().iterator();
		int currDoc = 0;
		// File handling to create csv files

		// Iterate over papersCollection and for each record, fetch the FOS,
		// AuthID, RefID write it in their respective files.
		while (papersCursor.hasNext()) {

			Document doc = papersCursor.next();
			currDoc++;
			Paper paper = new Paper();
			paper.setPaperId(String.valueOf(doc.get("PId")));
			paper.setPublishYear((int) doc.get("PublYear"));
			paper.setPaperRank((int) doc.get("PRank"));
			paper.setInConference((boolean) doc.get("IsConf"));
			paper.setInJournal((boolean) doc.get("IsJrnl"));

			writeToFile(
					file_paper,
					paper.getPaperId() + "," + paper.getPublishYear() + ","
							+ paper.getPaperRank() + ","
							+ paper.isInConference() + ","
							+ paper.isInJournal());

			String[] FOSIds = String.valueOf(doc.get("FOSIds"))
					.replace(' ', '_').replace('|', ' ').split(" ");
			String[] KWs = String.valueOf(doc.get("KW")).replace(' ', '_')
					.replace('|', ' ').split(" ");
			String[] AuthIds = String.valueOf(doc.get("AuthIds"))
					.replace(' ', '_').replace('|', ' ').split(" ");

			String[] PRefIds = String.valueOf(doc.get("PRefIds"))
					.replace(' ', '_').replace('|', ' ').split(" ");
			writeToFile(file_paper_FOS, FOSIds, paper.getPaperId());
			writeToFile(file_paper_author, AuthIds, paper.getPaperId());
			writeToFile(file_paper_KW, KWs, paper.getPaperId());
			writeToFile(file_paper_References, PRefIds, paper.getPaperId());

			if (currDoc % BATCH_WRITE_SIZE == 0) {
				System.out.println(currDoc + " total records processed");

				// get last id and get further records cursor. THIS IS to
				// avoid
				// the cursor not found exception
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

	/**
	 * gets paper names from the papers and attaches it to the records to be put
	 * in csv
	 * 
	 * @throws IOException
	 */
	public static void getPaperNames() throws IOException {
		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		MongoDatabase db1 = mc.getDatabase("testFinalMSDB");
		MongoDatabase db2 = mc.getDatabase("microsoftdb");

		mc.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);

		MongoCollection<Document> papersColl = db1
				.getCollection("PaperCollection");
		MongoCollection<Document> papersCollMS = db2.getCollection("Papers");

		MongoCursor<Document> papersCursor = papersColl.find().iterator();
		int currDoc = 0;
		// File handling to create csv files

		// Iterate over papersCollection and for each record, fetch the FOS,
		// AuthID, RefID write it in their respective files.
		while (papersCursor.hasNext()) {

			Document doc = papersCursor.next();
			currDoc++;
			Paper paper = new Paper();
			paper.setPaperId(String.valueOf(doc.get("PId")));

			// get paper entry from microsoft db and fetch name.
			MongoCursor<Document> currPaperCursor = papersCollMS.find(
					new Document("PId", paper.getPaperId())).iterator();
			if (currPaperCursor.hasNext()) {
				Document currPaper = currPaperCursor.next();
				String paperName = currPaper.getString("NormTitle");
				writeToFile(file_papernames, paper.getPaperId() + ","
						+ paperName);
			}
			currPaperCursor.close();

			if (currDoc % BATCH_WRITE_SIZE == 0) {
				System.out.println(currDoc + " total records processed");

				// get last id and get further records cursor. THIS IS to
				// avoid
				// the cursor not found exception
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

	/**
	 * 
	 * function to do the file write operation
	 * 
	 * @param file
	 * @param line
	 * @throws IOException
	 */
	public static void writeToFile(File file, String line) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		fw.write(line + "\n");
		fw.close();
	}

	/**
	 * function to do the file write operation
	 * 
	 * @param file
	 * @param lines
	 * @param paperId
	 * @throws IOException
	 */
	public static void writeToFile(File file, String[] lines, String paperId)
			throws IOException {
		FileWriter fw = new FileWriter(file, true);
		for (String string : lines) {
			if (!string.equals("")) {
				fw.write(paperId + "," + string + "\n");
			}
		}
		fw.close();
	}

}
