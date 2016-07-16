/**
 * @author Renuka Deshmukh
 * This is a data class for holding Paper objects in the various tasks and other files.
 * 
 * Date : April 15, 2016
 * 
 */
package mongo;

import java.util.ArrayList;
import java.util.Set;

import org.bson.Document;

public class Paper extends Document {
	private String PaperId;
	private int PublishYear;
	private int PaperRank;
	private boolean InConference;
	private boolean InJournal;
	private String FieldOfStudy;
	private String Keywords;
	private String PaperRefIds;
	private String Authors;
	private Set<String> KeywordsList;
	private ArrayList<String> AuthorsList;

	public Document getBsonDoc() {
		Document doc = new Document();
		doc.append("PId", PaperId).append("PublYear", PublishYear)
				.append("PRank", PaperRank).append("IsConf", InConference)
				.append("IsJrnl", InJournal).append("FOSIds", FieldOfStudy)
				.append("KW", Keywords).append("PRefIds", PaperRefIds)
				.append("AuthIds", Authors);
		return doc;

	}

	public String getPaperId() {
		return PaperId;
	}

	public void setPaperId(String paperId) {
		PaperId = paperId;
	}

	public int getPublishYear() {
		return PublishYear;
	}

	public void setPublishYear(int publishYear) {
		PublishYear = publishYear;
	}

	public int getPaperRank() {
		return PaperRank;
	}

	public void setPaperRank(int paperRank) {
		PaperRank = paperRank;
	}

	public boolean isInConference() {
		return InConference;
	}

	public void setInConference(boolean inConference) {
		InConference = inConference;
	}

	public boolean isInJournal() {
		return InJournal;
	}

	public void setInJournal(boolean inJournal) {
		InJournal = inJournal;
	}

	public String getFieldOfStudy() {
		return FieldOfStudy;
	}

	public void setFieldOfStudy(String fieldOfStudy) {
		FieldOfStudy = fieldOfStudy;
	}

	public String getKeywords() {
		return Keywords;
	}

	public void setKeywords(String keywords) {
		Keywords = keywords;
	}

	public String getPaperRefIds() {
		return PaperRefIds;
	}

	public void setPaperRefIds(String paperRefIds) {
		PaperRefIds = paperRefIds;
	}

	public String getAuthors() {
		return Authors;
	}

	public void setAuthors(String authors) {
		Authors = authors;
	}

	public Set<String> getKeywordsList() {
		return KeywordsList;
	}

	public void setKeywordsList(Set<String> keywordsList) {
		KeywordsList = keywordsList;
	}

	public ArrayList<String> getAuthorsList() {
		return AuthorsList;
	}

	public void setAuthorsList(ArrayList<String> authorsList) {
		AuthorsList = authorsList;
	}

}
