/**
 * @author Milind Gokhale and Renuka Deshmukh
 * This is an ENUM to hold the weight given to the various scores in the Paper scores calculation.
 * 
 * Date : April 30, 2016
 * 
 */
package mongo;

public enum PaperScoreWeights {
	AUTHORPOPULARITY(0.2), KEYWORDMATCH(0.5), YEAR(0.2), PAPERRANK(0.1);

	private double value;

	PaperScoreWeights(double value) {
		this.value = value;
	}

	public double getWeight() {
		return value;
	}

	public static void main(String[] args) {
		System.out.println(PaperScoreWeights.KEYWORDMATCH.getWeight());
	}
}
