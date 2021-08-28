package TextDataStructures;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import LemmaLearner.*;

public class Paragraph implements Serializable, Comparable<Paragraph> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 565211008957338879L;
	
	private Text originText;
	private String paragraphID;
	private final String rawParagraph;
	private final ListSet<Sentence> sentences;
	
	public Paragraph(String rawParagraph, Collection<Sentence> sentences) {
		this.rawParagraph = rawParagraph;
		this.sentences = new ListSet<Sentence>(sentences);
		this.sentences.forEach(sentence -> sentence.setInitialOriginParagraph(this));
	}

	public String getRawParagraph() {		
		return rawParagraph;
	}
	


	public Set<Sentence> getSentences() {
		return sentences;
	}

	public List<String> getAllRawWords() {
		List<String> rawWordList = new ArrayList<String>();
		for (Sentence sentence : sentences) {
			List<String> sentenceWords = sentence.getRawWordList();
			rawWordList.addAll(sentenceWords);
		}
		return rawWordList;
	}
	
	public void setParagraphID(String id) {
		paragraphID = id;
	}

	public String getParagraphID() {
		return paragraphID;
	}
	
	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allParagraphs.containsKey(paragraphID)) {
			throw new Error("The paragraph with the ID " + paragraphID + " aldready exists in the database.");
		} else {
			textDatabase.allParagraphs.put(paragraphID, this);
		}
	}
	
	@Override
	public int hashCode() {
		return getRawParagraph().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Paragraph)) 
			return false;
		else 
			return ((Paragraph) obj).getParagraphID().equals(this.paragraphID);
	}


	@Override
	public String toString() {
		return getRawParagraph();
	}

	public Text getOriginText() {
		return originText;
	}

	public void setOriginText(Text text) {
		this.originText = text;
	}

	@Override
	public int compareTo(Paragraph o) {
		return this.getRawParagraph().compareTo(o.getRawParagraph());
	}

	public Sentence getNthSentence(int n) {
		throw new Error("Removed function."); 
		//return sentences.get(n);
	}

	public void setSentences(List<Sentence> newParagraphSentences) {
		sentences.clear();
		sentences.addAll(newParagraphSentences);
	}
}
