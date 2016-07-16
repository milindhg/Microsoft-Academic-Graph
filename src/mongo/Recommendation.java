/**
 * @author Milind Gokhale
 * This class primarily does the work of ranking the citation recommendations.
 * The scores are calculated, final score is calculated and the recommendations 
 * are sorted in descending order of the final score. 
 * 
 * Date : April 25, 2016
 * 
 */
package mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.MongoClient;
import com.mongodb.MongoCursorNotFoundException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Recommendation {

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

	private static MongoClient mc;
	private static MongoDatabase db;

	private static final int BATCH_WRITE_SIZE = 500;
	private static final int TOTAL_RECORDS_SIZE = 50000;

	public static void initDBConn() {
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		try {
			JSch jsch = new JSch();
			SSH_SESSION = null;
			SSH_SESSION = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
			SSH_SESSION.setPassword(SSH_PASSWORD);
			SSH_SESSION.setConfig(config);
			SSH_SESSION.connect();
			SSH_SESSION
					.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
		} catch (JSchException e) {
			// e.printStackTrace();
			System.out
					.println("error establishing connection. Please check whether mongo server is UP!");
			System.exit(0);
		}

		mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		db = mc.getDatabase("microsoftdb");
	}

	public static void closeDBConn() {
		mc.close();
	}

	public static void main(String[] args) throws JSchException {

		initDBConn();

		String inputPaperId = "0404A0F1";
		Paper inputPaper = getPaperDetails(inputPaperId);
		ArrayList<PaperScore> recoPapers = getPaperRecommendations(inputPaper);
		calcNormVars(inputPaper, recoPapers);
		applyRanking(inputPaper, recoPapers);

		// sort as per the score in descending order
		Collections.sort(recoPapers, new Comparator<PaperScore>() {
			@Override
			public int compare(PaperScore arg0, PaperScore arg1) {
				return Double.compare(arg1.getFinalScore(),
						arg0.getFinalScore());
			}
		});

		// Top 15 recommendations
		System.out.println("Top 15 recommendations...");
		for (int i = 0; i < 15; i++) {
			System.out.println(recoPapers.get(i).getFinalScore());
		}
		closeDBConn();
		System.exit(0);

	}

	/**
	 * get paper details and populate it in the paper object
	 * 
	 * @param paperId
	 * @return
	 */
	public static Paper getPaperDetails(String paperId) {
		MongoCollection paperColl = db.getCollection("Papers");
		MongoCursor<Document> paperCursor = paperColl.find(
				new Document("PId", paperId)).iterator();
		Paper paper = null;
		if (paperCursor.hasNext()) {
			paper = new Paper();
			Document currPaper = paperCursor.next();
			paper.setPaperId(paperId);
			paper.setPaperRank(currPaper.getInteger("PRank", 0));
			paper.setPublishYear(currPaper.getInteger("PublYear", -1));

			paperCursor.close();
			// prepare author list and set in the paper object
			MongoCollection<Document> paperAuthAffColl = db
					.getCollection("PaperAuthorAffiliations");
			MongoCursor<Document> paperAuthAffCursor = paperAuthAffColl.find(
					new Document("PId", paper.getPaperId())).iterator();

			ArrayList<String> authorsList = new ArrayList<String>();
			while (paperAuthAffCursor.hasNext()) {
				Document currAuthor = paperAuthAffCursor.next();
				authorsList.add(currAuthor.getString("AuthId"));
			}
			paperAuthAffCursor.close();
			paper.setAuthorsList(authorsList);

			// prepare keyword set and set in the paper object
			MongoCollection<Document> paperKeywordsColl = db
					.getCollection("PaperKeywords");
			MongoCursor<Document> paperKeywordsCursor = paperKeywordsColl.find(
					new Document("PId", paper.getPaperId())).iterator();

			Set<String> keywordList = new HashSet<String>();
			while (paperKeywordsCursor.hasNext()) {
				Document currKeywordEntry = paperKeywordsCursor.next();
				keywordList.add(currKeywordEntry.getString("KWName"));
			}
			paperKeywordsCursor.close();
			paper.setKeywordsList(keywordList);

		}

		return paper;

	}

	/**
	 * get the citation recommendations for the input paper.
	 * 
	 * @param inputPaper
	 * @return
	 */
	public static ArrayList<PaperScore> getPaperRecommendations(Paper inputPaper) {

		// Get all the papers of all the keywords of the inputPaper
		Set<String> keywordSet = inputPaper.getKeywordsList();
		MongoCollection paperKeywordsColl = db.getCollection("PaperKeywords");
		MongoCollection paperColl = db.getCollection("Papers");
		ArrayList<PaperScore> finalAllKWPapersList = new ArrayList<PaperScore>();

		// Get all the papers which are older than inputPaper year for each of
		// the keywords from the keywordSet
		for (String currKeyword : keywordSet) {
			MongoCursor<Document> paperKeywordCursor = paperKeywordsColl.find(
					new Document("KWName", currKeyword)).iterator();
			Set<String> currKWPaperList = new HashSet<String>();
			while (paperKeywordCursor.hasNext()) {
				Document currKWPaper = paperKeywordCursor.next();
				try {
					currKWPaperList.add(currKWPaper.getString("PId"));
				} catch (ClassCastException e) {
					System.out.println("Couldn't Cast this PId: "
							+ currKWPaper.get("PId"));
				}
			}
			paperKeywordCursor.close();

			// From all the papers of the current keyword, iterate and get the
			// papers which are older than the inputPaper year.
			MongoCursor<Document> currKWOlderPapersCursor = paperColl.find(
					new Document("PId", new Document("$in", currKWPaperList))
							.append("PublYear",
									new Document("$lt", inputPaper
											.getPublishYear()))).iterator();
			try {
				while (currKWOlderPapersCursor.hasNext()) {
					Document currKWOlderPaper = currKWOlderPapersCursor.next();
					try {
						Paper currFinalPaper = getPaperDetails(currKWOlderPaper
								.getString("PId"));
						PaperScore currFinalPaperScore = new PaperScore(
								currFinalPaper);
						finalAllKWPapersList.add(currFinalPaperScore);
					} catch (ClassCastException e) {
						System.out.println("Couldn't Cast this PId: "
								+ currKWOlderPaper.get("PId"));
					}
				}
			} catch (MongoCursorNotFoundException e) {
				System.out.println("MongoCursorNotFoundException!!");
			} finally {
				currKWOlderPapersCursor.close();
			}
		}

		return finalAllKWPapersList;
	}

	public static Set<String> getAllAuthorsList(
			ArrayList<PaperScore> paperScoreList) {
		Set<String> authorsList = new HashSet<String>();
		for (PaperScore paperScore : paperScoreList) {
			authorsList.addAll(paperScore.getAuthorList());
		}
		return authorsList;
	}

	/**
	 * Calculate the Author popularity normalization variables
	 * 
	 * @param authorsList
	 */
	public static void calcNormAuthPopMinMax(Set<String> authorsList) {
		MongoCollection<Document> PaperAuthAffColl = db
				.getCollection("PaperAuthorAffiliations");
		ArrayList<Document> aggregateInput = new ArrayList<Document>();
		aggregateInput.add(new Document("$match", new Document("AuthId",
				new Document("$in", authorsList))));
		aggregateInput.add(new Document("$group", new Document("_id",
				new Document("AuthId", "$AuthId")).append("count",
				new Document("$sum", 1))));
		MongoCursor<Document> foundAuthorsPapersCursor = PaperAuthAffColl
				.aggregate(aggregateInput).iterator();

		while (foundAuthorsPapersCursor.hasNext()) {
			Document doc = foundAuthorsPapersCursor.next();
			System.out.println(doc);
			int currAuthPaperCount = doc.getInteger("count", 1);
			if (currAuthPaperCount < PaperScoreNormVars.AUTHORPOPULARITY_MIN)
				PaperScoreNormVars.AUTHORPOPULARITY_MIN = currAuthPaperCount;
			if (currAuthPaperCount > PaperScoreNormVars.AUTHORPOPULARITY_MAX)
				PaperScoreNormVars.AUTHORPOPULARITY_MAX = currAuthPaperCount;
		}
		foundAuthorsPapersCursor.close();

	}

	/**
	 * Calculate normalization variables min and max
	 * 
	 * @param inputPaper
	 * @param paperScoreList
	 */
	public static void calcNormVars(Paper inputPaper,
			ArrayList<PaperScore> paperScoreList) {
		for (PaperScore currPaperScore : paperScoreList) {
			// Update Author Popularity min max
			Set<String> authorsList = getAllAuthorsList(paperScoreList);
			calcNormAuthPopMinMax(authorsList);

			// Update Paper year min max
			if (currPaperScore.getPublishYear() < PaperScoreNormVars.PAPERYEAR_MIN)
				PaperScoreNormVars.PAPERYEAR_MIN = currPaperScore
						.getPublishYear();
			if (currPaperScore.getPublishYear() > PaperScoreNormVars.PAPERYEAR_MAX)
				PaperScoreNormVars.PAPERYEAR_MAX = currPaperScore
						.getPublishYear();

			// Update Paper Rank min max
			if (currPaperScore.getPaperRank() < PaperScoreNormVars.PAPERRANK_MIN)
				PaperScoreNormVars.PAPERRANK_MIN = currPaperScore
						.getPaperRank();
			if (currPaperScore.getPaperRank() > PaperScoreNormVars.PAPERRANK_MAX)
				PaperScoreNormVars.PAPERRANK_MAX = currPaperScore
						.getPaperRank();

			// Update Paper keyword match min max
			Set<String> intersection = new HashSet<String>(
					inputPaper.getKeywordsList());
			intersection.retainAll(currPaperScore.getKeywordsList());
			if (intersection.size() < PaperScoreNormVars.KEYWORDMATCH_MIN)
				PaperScoreNormVars.KEYWORDMATCH_MIN = intersection.size();
			if (intersection.size() > PaperScoreNormVars.KEYWORDMATCH_MAX)
				PaperScoreNormVars.KEYWORDMATCH_MAX = intersection.size();
		}
	}

	public static void applyRanking(Paper inputPaper,
			ArrayList<PaperScore> paperScoreList) {
		for (PaperScore currPaperScore : paperScoreList) {
			currPaperScore.setAuthorPopularityWtScore(Utilities
					.calcAuthPopNormScore(currPaperScore.getAuthorList(), mc));
			currPaperScore.setKeywordMatchWtScore(Utilities
					.calcKWMatchNormScore(inputPaper.getKeywordsList(),
							currPaperScore.getKeywordsList()));
			currPaperScore.setPaperRankWtScore(Utilities
					.calcPaperRankNormScore(currPaperScore.getPaperRank()));
			currPaperScore.setYearWtScore(Utilities.calcYearNormScore(
					inputPaper.getPublishYear(),
					currPaperScore.getPublishYear()));
			currPaperScore.setFinalScore(Utilities
					.calcFinalScore(currPaperScore));
		}
	}
}
