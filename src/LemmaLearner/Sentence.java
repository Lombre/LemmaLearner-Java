package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


public class Sentence implements Serializable, Comparable<Sentence> {
	
	private Set<Paragraph> originParagraphs;
	private List<Sentence> subSentences;
	private final String rawSentence;
	private final short[] wordBeginningIndex;
	private final short[] wordLengthIndex;
	private final double scoreExponent = 4;
	
	public Sentence(String rawSentence, List<String> rawWords) {
		if (Short.MAX_VALUE <= rawSentence.length()) throw new Error("A sentence that is to long has been parsed. Maximum allowed length is " + Short.MAX_VALUE + ". The sentence is: " + rawSentence);
		this.rawSentence = rawSentence;
		wordBeginningIndex = new short[rawWords.size()];
		wordLengthIndex = new short[rawWords.size()];
		setWordIndexes(rawSentence, rawWords);
	}

	public Sentence(String rawText, List<String> rawWords, List<Sentence> subSentences) {
		this(rawText, rawWords);
		this.subSentences = subSentences;
	}

	private void setWordIndexes(String rawSentence, List<String> rawWords) {
		String lowerCaseRawSentence = rawSentence.toLowerCase();
		int lastEndingIndex = 0;
		for (int i = 0; i < rawWords.size(); i++) {
			String currentRawWord = rawWords.get(i);
			int indexBeginning = lowerCaseRawSentence.indexOf(currentRawWord.toLowerCase(), lastEndingIndex);
			if (indexBeginning == -1) 
				throw new Error("Word \"" + currentRawWord + "\" not found in sentence: " + getRawSentence());
			wordBeginningIndex[i] = (short) indexBeginning;
			wordLengthIndex[i] = (short) (currentRawWord.length());
			lastEndingIndex = wordBeginningIndex[i] + wordLengthIndex[i];
		}
	}
	
	public String getRawSentence() {
		return rawSentence;
	}

	public List<String> getRawWordList() {
		ArrayList<String> rawWords = new ArrayList<String>();
		for (int i = 0; i < wordBeginningIndex.length; i++) {
			String rawWord = rawSentence.substring(wordBeginningIndex[i], wordBeginningIndex[i] + wordLengthIndex[i]).toLowerCase();
			rawWords.add(rawWord);
		}
		return rawWords;
	}	
	
	public String getLemmatizedRawSentence(TextDatabase database) {
		if (wordBeginningIndex.length == 0) {
			return getRawSentence();
		}
		List<Conjugation> conjugations = getWordList(database);
		String lemmatizedRawSentence = "";
		lemmatizedRawSentence += rawSentence.substring(0, wordBeginningIndex[0]);
		for (int i = 0; i < wordBeginningIndex.length -1; i++) {
			Conjugation currentConjugation = conjugations.get(i);
			lemmatizedRawSentence += currentConjugation + "(" + currentConjugation.getLemma() + ", " + currentConjugation.getLemma().getTimesLearned() + ", " + currentConjugation.getTimesLearned() + ")";
			lemmatizedRawSentence += rawSentence.substring(wordBeginningIndex[i] + wordLengthIndex[i], wordBeginningIndex[i+1]);				
		}
		Conjugation lastConjugation = conjugations.get(conjugations.size()-1);
		lemmatizedRawSentence += lastConjugation + "(" + lastConjugation.getLemma() + ", " + lastConjugation.getLemma().getTimesLearned() + ", " + lastConjugation.getTimesLearned() + ")";
		lemmatizedRawSentence += rawSentence.substring(wordBeginningIndex[wordBeginningIndex.length-1] + wordLengthIndex[wordBeginningIndex.length-1], rawSentence.length());
		return lemmatizedRawSentence;
	}

	public Set<String> getRawWordSet() {
		return new HashSet<>(getRawWordList());
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Sentence)) return false;
		else return ((Sentence) obj).getRawSentence().equals(this.getRawSentence());
	}
	
	@Override
	public int hashCode() {		
		return rawSentence.hashCode();
	}

	@Override
	public String toString() {
		return getRawSentence();
	}

	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allSentences.containsKey(getRawSentence())) {
			Sentence sentenceInDatabase = textDatabase.allSentences.get(this.getRawSentence());
			for (Paragraph paragraph : originParagraphs) {
				sentenceInDatabase.originParagraphs.add(paragraph);
				paragraph.getSentences().remove(this);
				paragraph.getSentences().add(sentenceInDatabase);
			}
			//The words do not need to be updated, as since a sentence S on the same form is already in the database,
			//the words of S must already be in the database, and point to S.
		} else {
			textDatabase.allSentences.put(getRawSentence(), this);
		}
	}

	public void setInitialOriginParagraph(Paragraph paragraph) {
		originParagraphs = new ListSet<Paragraph>();
		originParagraphs.add(paragraph);
	}

	public boolean isDirectlyLearnable(Set<Lemma> learnedLemmas, TextDatabase database) {
		int numberOfUnlearnedLemmas = getNumberOfUnlearnedLemmas(learnedLemmas, database);
		return numberOfUnlearnedLemmas == 1;
	}

	public int getNumberOfUnlearnedLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
		Set<Lemma> lemmas = this.getLemmaSet(database);
		int lemmasInSentence = lemmas.size();
		lemmas.retainAll(learnedLemmas);
		int lemmasInSentenceLearned = lemmas.size();
		int numberOfUnlearnedLemmas = lemmasInSentence - lemmasInSentenceLearned;
		return numberOfUnlearnedLemmas;
	}
	
	public Set<Conjugation> getWordSet(TextDatabase database) {
		return new HashSet<Conjugation>(getWordList(database));
	}

	private List<Lemma> getLemmaList(TextDatabase database) {
		return getWordList(database).stream()
									.map(word -> word.getLemma())
									.collect(Collectors.toList());
		
	}

	public Set<Lemma> getLemmaSet(TextDatabase database) {
		return getWordSet(database).stream()
									.map(word -> word.getLemma())
									.collect(Collectors.toCollection(HashSet::new));
		
	}

	public List<Conjugation> getWordList(TextDatabase database) {
		List<String> rawWordsInSentence = getRawWordList();
		List<Conjugation> wordsInDatabase = new ArrayList<Conjugation>();
		for (String rawWord : rawWordsInSentence) {
			wordsInDatabase.add(database.allWords.get(rawWord));
		}
		return wordsInDatabase;
	}
	
	public List<Lemma> getUnlearnedLemmas(Set<Lemma> learnedLemmas, TextDatabase database){
		Set<Lemma> lemmasInSentence = this.getLemmaSet(database);
		List<Lemma> unlearnedLemmas = lemmasInSentence.stream().filter(lemma -> !learnedLemmas.contains(lemma)).collect(Collectors.toList());
		return unlearnedLemmas;
	}

	@Override
	public int compareTo(Sentence o) {
		return rawSentence.compareTo(o.rawSentence);
	}

	public Integer getHighestFrequency(TextDatabase database) {
		Set<Conjugation> wordsInDatabase = getWordSet(database);
		return wordsInDatabase.stream().map(word -> word.getFrequency()).max((x, y) -> x.compareTo(y)).get();
	}

	public int getWordCount() {
		return wordBeginningIndex.length;
	}

	public double getScore(TextDatabase database, int numberOfTimesCounted) {
		double lemmaScore = getLemmaScore(database, numberOfTimesCounted);
		double conjugationScore = getConjugationScore(database, numberOfTimesCounted);
		double score = lemmaScore + conjugationScore;
		return score;
	}

	private double getLemmaScore(TextDatabase database, int numberOfTimesCounted) {
		double score = 0;
		var lemmas = getLemmaSet(database);
		for (Lemma lemma : lemmas) {
			if (lemma.getTimesLearned() == 0) {
				//The primary basis for the score is the frequency of the unlearned lemma.
				score += lemma.getFrequency();
			} else if (lemma.getTimesLearned() < numberOfTimesCounted){
				double extraScore = 1.0/( Math.pow(scoreExponent, lemma.getTimesLearned()));
				score += extraScore;
			} 
		}
		return score;
	}
	

	private double getConjugationScore(TextDatabase database, int numberOfTimesCounted) {
		double score = 0;
		var conjugations = getWordSet(database);
		for (Conjugation conjugation : conjugations) {
			if (conjugation.getTimesLearned() < numberOfTimesCounted){
				double extraScore = 1.0/( Math.pow(scoreExponent, conjugation.getTimesLearned()+2));
				score += extraScore;
			} 
		}
		return score;
	}

	public boolean hasNoNewLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
		return getNumberOfUnlearnedLemmas(learnedLemmas, database) == 0;
	}

	public List<Sentence> getSubSentences() {
		return subSentences;
	}

	public Paragraph getAParagraph() {
		if (originParagraphs == null || originParagraphs.size() == 0) {
			return null;
		} else {
			return (Paragraph) originParagraphs.toArray()[0];
		}
	}


	
}
