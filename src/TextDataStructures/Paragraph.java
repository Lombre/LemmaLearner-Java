package TextDataStructures;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import LemmaLearner.*;

public class Paragraph implements Serializable, Comparable<Paragraph> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 565211008957338879L;
	
	private ParagraphParent parent;
	private String paragraphID;
	protected final String rawParagraph;
	protected final ListSet<Sentence> sentences;
	

	
	public Paragraph(String rawParagraph, Collection<Sentence> sentences) {
		this.rawParagraph = rawParagraph;
		this.sentences = new ListSet<Sentence>(sentences);
		this.sentences.forEach(sentence -> sentence.setInitialOriginParagraph(this));
	}
	
	public Paragraph(String rawParagraph, Collection<Sentence> sentences, String paragraphID) {
		this(rawParagraph, sentences);
		setParagraphID(paragraphID);
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
		if (paragraphID == null) {
			throw new Error("No paragraphID set.");
		} else if (textDatabase.allParagraphs.containsKey(paragraphID)) {
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
		if (parent instanceof Text) {
			return (Text) parent;			
		} else throw new Error("The current paragraph: \"" + rawParagraph + "\" does not have a text as a parent.");
	}

	public void setOriginText(Text text) {
		this.parent = (ParagraphParent) text;
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

	public Paragraph getParagraphWithSentencesFilteredOnCriteria(Function<Sentence, Boolean> filterCriteria) {
		var filteredSentences = new ArrayList<Sentence>();
		for (Sentence sentence : sentences) {
			if (filterCriteria.apply(sentence)) {
				filteredSentences.add(sentence);
			} else {
				Collection<Sentence> subSentences = sentence.getSubSentencesMatchingCriteria(this, filterCriteria);
				if (0 < subSentences.size())
					filteredSentences.addAll(subSentences);				
			}
		}
		var returnParagraph =  new Paragraph(rawParagraph, filteredSentences, paragraphID);
		returnParagraph.setOriginText(this.getOriginText());
		return returnParagraph;
	}

	public Sentence asSentence(Paragraph originParagraph) {
		Sentence sentenceForm = new Sentence(this.rawParagraph, getAllRawWords());
		sentenceForm.setInitialOriginParagraph(originParagraph);
		return sentenceForm;
	}

	public Set<Lemma> getAllLemmas(TextDatabase textDatabase) {
		var allLemmas = new HashSet<Lemma>();
		for (Sentence sentence : getSentences()) {
			allLemmas.addAll(sentence.getLemmaSet(textDatabase));
		}
		return allLemmas;
	}
}
