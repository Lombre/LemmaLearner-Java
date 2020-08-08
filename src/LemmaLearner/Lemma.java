package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import LemmaLearner.*;

public class Lemma implements Serializable, Comparable<Lemma> {

	private final Set<Word> conjugations = new ListSet<Word>();
	private final String rawLemma;
	
	public Lemma(String rawWord) {
		this.rawLemma = rawWord.toLowerCase();
	}
	
	public String getRawLemma() {
		return rawLemma;
	}

	public int getFrequency() {
		return conjugations.stream().map(word -> word.getFrequency()).reduce(0, (freq1, freq2) -> freq1 + freq2);
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

	public void addConjugation(Word conjugation) {
		this.conjugations.add(conjugation);
		conjugation.setRawLemma(this);		
	}
}
