package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import LemmaLearner.*;

public class Conjugation implements Serializable, Comparable<Conjugation> {

	private Set<Sentence> sentences;
	private final String rawConjugation;
	private int frequency;
	private Lemma lemma;
	private int timesLearned;
	
	
	public Conjugation(Sentence originSentence, String rawConjugation) {
		this(rawConjugation);
		sentences = new ListSet<Sentence>();
		sentences.add(originSentence);
	}
	
	public Conjugation(String rawConjugation) {
		this.rawConjugation = rawConjugation.toLowerCase();
	}
	
	public String getRawConjugation() {
		return rawConjugation;
	}
	
	public Lemma getLemma() {
		if (lemma == null)
			throw new NullPointerException();
		return lemma;
	}

	public int getFrequency() {
		return frequency;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Conjugation))
			return false;
		else return ((Conjugation) obj).getRawConjugation().equals(this.getRawConjugation());
	}
	
	@Override
	public int hashCode() {
		return rawConjugation.hashCode();
	}

	@Override
	public String toString() {
		return getRawConjugation();
	}

	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allWords.containsKey(this.getRawConjugation())) {
			//The sentences have already been added, 
			//so the word in the database simply also needs to refer to those sentences.
			Conjugation databaseWord = textDatabase.allWords.get(this.getRawConjugation());
			databaseWord.sentences.addAll(sentences);		
			databaseWord.frequency++;			
		} else {
			textDatabase.allWords.put(getRawConjugation(), this);
			frequency = 1;
		}
	}

	public Set<Sentence> getSentences() {
		return sentences;
	}

	@Override
	public int compareTo(Conjugation o) {
		return getRawConjugation().compareTo(o.getRawConjugation());
	}

	public void setRawLemma(Lemma lemma) {
		if (this.lemma != null) 
			throw new Error("The lemma for a conjugation is immutable.");
		this.lemma = lemma;
	}


	public void incrementTimesLearned() {
		timesLearned++;
	}
	
	public Integer getTimesLearned() {
		return timesLearned;
	}
}
