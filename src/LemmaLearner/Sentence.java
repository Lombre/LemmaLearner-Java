package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Sentence implements Serializable, Comparable<Sentence> {
	
	private Set<Paragraph> originParagraphs;
	private final String rawSentence;
	private final short[] wordBeginningIndex;
	private final short[] wordLengthIndex;
	
	public Sentence(String rawSentence, List<String> rawWords) {
		if (Short.MAX_VALUE <= rawSentence.length()) throw new Error("A sentence that is to long has been parsed. Maximum allowed length is " + Short.MAX_VALUE + ". The sentence is: " + rawSentence);
		this.rawSentence = rawSentence;
		wordBeginningIndex = new short[rawWords.size()];
		wordLengthIndex = new short[rawWords.size()];
		setWordIndexes(rawSentence, rawWords);
	}

	private void setWordIndexes(String rawSentence, List<String> rawWords) {
		String lowerCaseRawSentence = rawSentence.toLowerCase();
		for (int i = 0; i < rawWords.size(); i++) {
			String currentRawWord = rawWords.get(i);
			int indexBeginning = lowerCaseRawSentence.indexOf(currentRawWord.toLowerCase());
			if (indexBeginning == -1) 
				throw new Error("Word \"" + currentRawWord + "\" not found in sentence: " + getRawSentence());
			wordBeginningIndex[i] = (short) indexBeginning;
			wordLengthIndex[i] = (short) (currentRawWord.length());
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
	

	/**
	 * OBS. Note that the words are not synchronized with the database. For this, use getSynchronizedWordSet(textDatabase)
	 * @return
	 */
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
		return numberOfUnlearnedLemmas <= 1;
	}

	private int getNumberOfUnlearnedLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
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

	public double getScore(TextDatabase database) {
		double score = 0;
		var lemmas = getLemmaSet(database);
		for (Lemma lemma : lemmas) {
			if (lemma.getTimesLearned() == 0) {
				//The primary basis for the score is the frequency of the unlearned lemma.
				score += lemma.getFrequency();
			} else {
				double extraScore = 1.0/( Math.pow(2, lemma.getTimesLearned()));
				score += extraScore;
			}
		}
		return score;
	}

	public boolean hasNoNewLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
		return getNumberOfUnlearnedLemmas(learnedLemmas, database) == 0;
	}


	
}
