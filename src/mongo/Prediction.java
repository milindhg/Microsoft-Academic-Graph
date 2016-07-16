/**
 * @author Milind Gokhale
 * This class primarily works on the predicting the citation recommendations 
 * for various papers using Recommendation class.
 * This class gives output in the steps of 20 from 20 predictions to 100 predictions.
 * 
 * Date : April 25, 2016
 * 
 */
package mongo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Prediction {

	public static File file_pred_20 = new File("F:/Pred_20.csv");
	public static File file_pred_40 = new File("F:/Pred_40.csv");
	public static File file_pred_60 = new File("F:/Pred_60.csv");
	public static File file_pred_80 = new File("F:/Pred_80.csv");
	public static File file_pred_100 = new File("F:/Pred_100.csv");
	public static File file_pred_60_withscore = new File(
			"F:/Pred_15_WithScore.csv");

	public static void main(String[] args) {
		ArrayList<String> inputPaperIds = new ArrayList<String>();
		// inputPaperIds.add("0404A0F1");
		inputPaperIds.add("58FDA5BF");
		inputPaperIds.add("592DE9D2");
		inputPaperIds.add("5D33ABBE");
		inputPaperIds.add("5D9D02CD");
		inputPaperIds.add("5DD6439E");
		inputPaperIds.add("5FA5178A");
		inputPaperIds.add("6D8436DF");
		inputPaperIds.add("09F3625D");
		inputPaperIds.add("5F8BC29B");
		inputPaperIds.add("5FE5C811");
		inputPaperIds.add("5B78E9DE");
		inputPaperIds.add("6DF65C47");
		inputPaperIds.add("2DE433FA");
		inputPaperIds.add("5B78E9DE");
		inputPaperIds.add("704F5E20");
		inputPaperIds.add("6B9E13C5");
		inputPaperIds.add("6062053E");
		inputPaperIds.add("645493B3");
		inputPaperIds.add("12E5DA8F");
		Recommendation.initDBConn();
		for (String currPaperId : inputPaperIds) {
			Paper currPaper = Recommendation.getPaperDetails(currPaperId);
			ArrayList<PaperScore> recoPapers = Recommendation
					.getPaperRecommendations(currPaper);
			Recommendation.calcNormVars(currPaper, recoPapers);
			Recommendation.applyRanking(currPaper, recoPapers);

			// sort as per the score in descending order
			Collections.sort(recoPapers, new Comparator<PaperScore>() {
				@Override
				public int compare(PaperScore arg0, PaperScore arg1) {
					return Double.compare(arg1.getFinalScore(),
							arg0.getFinalScore());
				}
			});

			// Top 15 recommendations
			ArrayList<String> top_100 = new ArrayList<String>();
			System.out.println("Top 15 recommendations...");
			for (int i = 0; i < 15; i++) {
				top_100.add(recoPapers.get(i).getPaperId());
				System.out.println(recoPapers.get(i).getPaperId() + "="
						+ recoPapers.get(i).getFinalScore());
				try {
					// Write the predictions to files
					if (i == 19) {
						writeToFile(file_pred_20, top_100, currPaperId);
					}
					if (i == 39) {
						writeToFile(file_pred_40, top_100, currPaperId);
					}
					if (i == 59) {
						writeToFile(file_pred_60, top_100, currPaperId);
					}
					if (i == 79) {
						writeToFile(file_pred_80, top_100, currPaperId);
					}
					if (i == 99) {
						writeToFile(file_pred_100, top_100, currPaperId);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		Recommendation.closeDBConn();

		System.exit(0);
	}

	/**
	 * function to do the file write operation
	 * 
	 * @param file
	 * @param predictions
	 * @param paperId
	 * @throws IOException
	 */
	public static void writeToFile(File file, ArrayList<String> predictions,
			String paperId) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		String line = "";
		for (int i = 0; i < predictions.size() - 1; i++) {
			line += predictions.get(i) + ",";
		}
		line += predictions.get(predictions.size() - 1);
		fw.write(paperId + ":" + line + "\n");
		fw.close();
	}

}
