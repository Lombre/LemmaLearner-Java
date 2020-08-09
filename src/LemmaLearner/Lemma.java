package LemmaLearner;
import java.awt.Component;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import LemmaLearner.*;

public class Lemma implements Serializable, Comparable<Lemma> {

	private final Set<Conjugation> conjugations = new ListSet<Conjugation>();
	private final String rawLemma;
	private int frequency = -1;
	private int timesLearned = 0;
	
	public Lemma(String rawWord) {
		this.rawLemma = rawWord.toLowerCase();
	}
	
	public String getRawLemma() {
		return rawLemma;
	}

	public int getFrequency() {
		if (frequency == -1) 
			frequency = conjugations.stream()
								    .map(word -> word.getFrequency())
								    .reduce(0, (freq1, freq2) -> freq1 + freq2);
		return frequency;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Lemma))
			return false;
		else return ((Lemma) obj).getRawLemma().equals(this.getRawLemma());
	}
	
	@Override
	public int hashCode() {
		return rawLemma.hashCode();
	}

	@Override
	public String toString() {
		return getRawLemma();
	}

	public Set<Sentence> getSentences() {
		return conjugations.stream().flatMap(conjugation -> conjugation.getSentences().stream()).collect(Collectors.toSet());
	}

	@Override
	public int compareTo(Lemma o) {
		return getRawLemma().compareTo(o.getRawLemma());
	}

	public void addConjugation(Conjugation conjugation) {
		this.conjugations.add(conjugation);
		conjugation.setRawLemma(this);		
	}

	public Set<Conjugation> getConjugations() {
		return conjugations;
	}

	public int getTimesLearned() {
		return timesLearned;
	}
	
	public void incrementTimesLearned() {
		timesLearned++;
	}
}
