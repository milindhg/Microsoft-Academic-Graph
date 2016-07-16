/**
 * @author Milind Gokhale
 * This class holds the utilty functions for calculating the various scores like 
 * Paper Rank, Paper Year, Author Popularity and Keyword match
 * 
 * Date : April 15, 2016
 * 
 */
package mongo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Utilities {
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

	public static boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	public static double calcAuthPopNormScore(ArrayList<String> authorList,
			MongoClient mc) {

		// Get the author written paper count for each paper in the author list.
		MongoDatabase DB = mc.getDatabase("microsoftdb");
		MongoCollection<Document> PaperAuthAffColl = DB
				.getCollection("PaperAuthorAffiliations");
		ArrayList<Document> aggregateInput = new ArrayList<Document>();
		aggregateInput.add(new Document("$match", new Document("AuthId",
				new Document("$in", authorList))));
		aggregateInput.add(new Document("$group", new Document("_id",
				new Document("AuthId", "$AuthId")).append("count",
				new Document("$sum", 1))));
		MongoCursor<Document> foundAuthorsPapersCursor = PaperAuthAffColl
				.aggregate(aggregateInput).iterator();

		int maxCount = Integer.MIN_VALUE;
		while (foundAuthorsPapersCursor.hasNext()) {
			Document doc = foundAuthorsPapersCursor.next();
			System.out.println(doc);
			int currAuthPaperCount = doc.getInteger("count", 1);
			if (currAuthPaperCount > maxCount) {
				maxCount = currAuthPaperCount;
			}
		}
		foundAuthorsPapersCursor.close();

		// Calculate the normalized author popularity score
		double normScore = (double) (maxCount - PaperScoreNormVars.AUTHORPOPULARITY_MIN)
				/ (PaperScoreNormVars.AUTHORPOPULARITY_MAX - PaperScoreNormVars.AUTHORPOPULARITY_MIN);
		return normScore;
	}

	public static double calcYearNormScore(int myPaperYear, int currPaperYear) {
		// int diff = myPaperYear - currPaperYear;
		double normScore = (double) (currPaperYear - PaperScoreNormVars.PAPERYEAR_MIN)
				/ (PaperScoreNormVars.PAPERYEAR_MAX - PaperScoreNormVars.PAPERYEAR_MIN);
		return normScore;
	}

	public static double calcKWMatchNormScore(Set<String> myPaperKeywordList,
			Set<String> currPaperKeywordList) {
		Set<String> intersection = new HashSet<String>(myPaperKeywordList);
		intersection.retainAll(currPaperKeywordList);

		// to avoid divide by zero error
		if (PaperScoreNormVars.KEYWORDMATCH_MAX
				- PaperScoreNormVars.KEYWORDMATCH_MIN == 0) {
			return 1;
		}
		double normScore = (double) (intersection.size() - PaperScoreNormVars.KEYWORDMATCH_MIN)
				/ (PaperScoreNormVars.KEYWORDMATCH_MAX - PaperScoreNormVars.KEYWORDMATCH_MIN);
		return normScore;
	}

	public static double calcPaperRankNormScore(int paperRank) {
		double normScore = (double) (paperRank - PaperScoreNormVars.PAPERRANK_MIN)
				/ (PaperScoreNormVars.PAPERRANK_MAX - PaperScoreNormVars.PAPERRANK_MIN);
		return normScore;
	}

	public static double calcFinalScore(PaperScore obj) {
		double finalScore = PaperScoreWeights.AUTHORPOPULARITY.getWeight()
				* obj.getAuthorPopularityWtScore()
				+ PaperScoreWeights.KEYWORDMATCH.getWeight()
				* obj.getKeywordMatchWtScore()
				+ PaperScoreWeights.PAPERRANK.getWeight()
				* obj.getPaperRankWtScore()
				+ PaperScoreWeights.YEAR.getWeight() * obj.getYearWtScore();
		return finalScore;
	}

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

		ArrayList<String> authorList = new ArrayList<String>();
		authorList.add("18BF35FC");
		authorList.add("47C741C7");
		authorList.add("6F11A8E3");
		authorList.add("5755B049");
		authorList.add("5E00B27C");

		MongoClient mc = new MongoClient(LOCAL_HOST, LOCAL_PORT);
		calcAuthPopNormScore(authorList, mc);
		mc.close();
	}
}
