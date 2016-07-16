/**
 * @author Milind Gokhale
 * This is a data class for holding the paper scores of the papers 
 * and for storing the calculated final score.
 * 
 * Date : April 30, 2016
 * 
 */
package mongo;

import java.util.ArrayList;
import java.util.Set;

public class PaperScore {
	private String paperId;
	private Set<String> keywordsList;
	private int publishYear;
	private ArrayList<String> authorList;
	private int paperRank;
	private double authorPopularityWtScore;
	private double keywordMatchWtScore;
	private double yearWtScore;
	private double paperRankWtScore;
	private double finalScore;

	public PaperScore() {

	}

	public PaperScore(Paper paper) {
		this.paperId = paper.getPaperId();
		this.authorList = paper.getAuthorsList();
		this.keywordsList = paper.getKeywordsList();
		this.paperRank = paper.getPaperRank();
		this.publishYear = paper.getPublishYear();
	}

	public String getPaperId() {
		return paperId;
	}

	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}

	public Set<String> getKeywordsList() {
		return keywordsList;
	}

	public void setKeywordsList(Set<String> keywordsList) {
		this.keywordsList = keywordsList;
	}

	public int getPublishYear() {
		return publishYear;
	}

	public void setPublishYear(int publishYear) {
		this.publishYear = publishYear;
	}

	public ArrayList<String> getAuthorList() {
		return authorList;
	}

	public void setAuthorList(ArrayList<String> authorList) {
		this.authorList = authorList;
	}

	public int getPaperRank() {
		return paperRank;
	}

	public void setPaperRank(int paperRank) {
		this.paperRank = paperRank;
	}

	public double getAuthorPopularityWtScore() {
		return authorPopularityWtScore;
	}

	public void setAuthorPopularityWtScore(double authorPopularityWtScore) {
		this.authorPopularityWtScore = authorPopularityWtScore;
	}

	public double getKeywordMatchWtScore() {
		return keywordMatchWtScore;
	}

	public void setKeywordMatchWtScore(double keywordMatchWtScore) {
		this.keywordMatchWtScore = keywordMatchWtScore;
	}

	public double getYearWtScore() {
		return yearWtScore;
	}

	public void setYearWtScore(double yearWtScore) {
		this.yearWtScore = yearWtScore;
	}

	public double getPaperRankWtScore() {
		return paperRankWtScore;
	}

	public void setPaperRankWtScore(double paperRankWtScore) {
		this.paperRankWtScore = paperRankWtScore;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

}
