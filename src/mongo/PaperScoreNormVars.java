/**
 * @author Milind Gokhale
 * This class primarily contains static variables for saving the Normalization variables. 
 * These variables helped in normalizing the scores of the Author Popularity, Paper rank, 
 * Keyword match and Paper year scores. 
 * 
 * Date : April 30, 2016
 * 
 */
package mongo;

public class PaperScoreNormVars {
	public static int AUTHORPOPULARITY_MIN = Integer.MAX_VALUE;
	public static int AUTHORPOPULARITY_MAX = Integer.MIN_VALUE;
	public static int PAPERRANK_MIN = Integer.MAX_VALUE;
	public static int PAPERRANK_MAX = Integer.MIN_VALUE;
	public static int KEYWORDMATCH_MIN = Integer.MAX_VALUE;
	public static int KEYWORDMATCH_MAX = Integer.MIN_VALUE;
	public static int PAPERYEAR_MIN = Integer.MAX_VALUE;
	public static int PAPERYEAR_MAX = Integer.MIN_VALUE;
}
