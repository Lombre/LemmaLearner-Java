package TextDataStructures;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import Configurations.LearningConfigurations;
import LemmaLearner.TextDatabase;
import LemmaLearner.*;

public class Sentence implements Serializable, Comparable<Sentence>, ParagraphParent {
	
	private Set<Paragraph> originParagraphs;
	private List<Paragraph> subParagraphs;
	private final String rawSentence;
	private final short[] wordBeginningIndex;
	private final short[] wordLengthIndex;

	private Set<Lemma> lemmaSet;
	private Set<Conjugation> wordSet;

	public Sentence(String rawSentence, List<String> rawWords) {
		if (Short.MAX_VALUE <= rawSentence.length()) 
			throw new Error("A sentence that is to long has been parsed. Maximum allowed length is " + Short.MAX_VALUE + ". The sentence is: " + rawSentence);
		this.rawSentence = rawSentence;
		wordBeginningIndex = new short[rawWords.size()];
		wordLengthIndex = new short[rawWords.size()];
		setWordIndexes(rawSentence, rawWords);
		this.subParagraphs = new ArrayList<Paragraph>();
	}


	public Sentence(String rawText, List<String> rawWords, List<Paragraph> subParagraphs) {
		this(rawText, rawWords);
		this.subParagraphs = subParagraphs;
	}
	
	private void setWordIndexes(String rawSentence, List<String> rawWords) {
		String lowerCaseRawSentence = rawSentence.toLowerCase();
		int lastEndingIndex = 0;
		for (int i = 0; i < rawWords.size(); i++) {
			String currentRawWord = rawWords.get(i).toLowerCase();
			int indexBeginning = lowerCaseRawSentence.indexOf(currentRawWord.toLowerCase(), lastEndingIndex);
			if (indexBeginning == -1)
				throw new Error("Word \"" + currentRawWord + "\" not found in sentence: " + getRawSentence());
			wordBeginningIndex[i] = (short) indexBeginning;
			var length = (currentRawWord.codePointCount(0, currentRawWord.length()));
			wordLengthIndex[i] = (short) length;
			lastEndingIndex = wordBeginningIndex[i] + wordLengthIndex[i];
		}
		int k = 1;
	}
	
	public String getRawSentence() {
		return rawSentence;
	}

	public List<String> getRawWordList() {
		ArrayList<String> rawWords = new ArrayList<String>();
		for (int i = 0; i < wordBeginningIndex.length; i++) {
			int wordEndIndex = wordBeginningIndex[i] + wordLengthIndex[i];
			if (rawSentence.length() < wordEndIndex)
				System.out.println(rawSentence);
			String rawWord = rawSentence.toLowerCase().substring(wordBeginningIndex[i], wordEndIndex);
			rawWords.add(rawWord);
		}
		return rawWords;
	}	
	
	public String getLemmatizedRawSentence(TextDatabase database) {
		if (wordBeginningIndex.length == 0) 
			return getRawSentence();
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


	public int getNumberOfUnlearnedLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
		Set<Lemma> lemmas = this.getLemmaSet(database);
		int numberOfLemmasInSentence = lemmas.size();
		int numberOfLemmasInSentenceLearned = 0;
		for (var lemma : lemmas) {
			if (learnedLemmas.contains(lemma))
				numberOfLemmasInSentenceLearned++;
		}
		int numberOfUnlearnedLemmas = numberOfLemmasInSentence - numberOfLemmasInSentenceLearned;
		return numberOfUnlearnedLemmas;
	}
	
	public Set<Conjugation> getWordSet(TextDatabase database) {
		var wordList = getWordList(database);
		return new HashSet<Conjugation>(wordList);
	}

	public List<Lemma> getLemmaList(TextDatabase database) {
		return getWordList(database).stream()
									.map(word -> word.getLemma())
									.collect(Collectors.toList());
		
	}

	public Set<Lemma> getLemmaSet(TextDatabase database) {
		if (this.lemmaSet == null){
			var wordSet = getWordSet(database);
			var lemmaSet = new HashSet<Lemma>();
			for (var word : wordSet) {
				lemmaSet.add(word.getLemma());
			}
			// Saving a lemma set for each sentence takes a lot of space
			// so we only save it if we have enabled this in the config.
			if (false) { //TODO Add config for this
				return Collections.unmodifiableSet(lemmaSet);
			} else
				this.lemmaSet = lemmaSet; // Collections.unmodifiableSet(lemmaSet);
		}
		return this.lemmaSet;
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

	public boolean hasNoNewLemmas(Set<Lemma> learnedLemmas, TextDatabase database) {
		return getNumberOfUnlearnedLemmas(learnedLemmas, database) == 0;
	}

	public Paragraph getAParagraph() {
		if (originParagraphs == null || originParagraphs.size() == 0) {
			return null;
		} else {
			return (Paragraph) originParagraphs.toArray()[0];
		}
	}

	public boolean isUnended() {
		char lastChar = rawSentence.charAt(rawSentence.length() - 1);
		if (lastChar == '.' || lastChar == '!' || lastChar == '?' || lastChar == '"' )//|| lastChar == 'â€�') 
			return true;
		else return false;
	}

	public boolean startsWithLowerCase() {
		return Character.isLowerCase(rawSentence.charAt(0));
	}


	public Collection<Paragraph> getSubParagraphs() {
		return subParagraphs;
	}


	public Collection<Sentence> getSubSentencesMatchingCriteria(Paragraph originParagraph, List<Function<Sentence, Boolean>> filterCriterias){
		Collection<Sentence> filteredSentences = new ArrayList<Sentence>();
		//The sentence does not fit the criteria, but maybe it contains subparagraphs that do?
		for (Paragraph subParagraph : subParagraphs) {
			Sentence subParagraphSentence = subParagraph.asSentence(originParagraph);
			if (filterCriterias.stream().allMatch(criteria -> criteria.apply(subParagraphSentence))) {
				filteredSentences.add(subParagraphSentence);
			} else {
				//Reduce the paragraph:
				Paragraph filteredSubParagraph = subParagraph.getParagraphWithSentencesFilteredOnCriteria(filterCriterias);
				filteredSubParagraph.setOriginText(originParagraph.getOriginText());
				if (0 < filteredSubParagraph.getSentences().size())
					filteredSubParagraph.getSentences().forEach(subSentence -> filteredSentences.add(subSentence));
			}
		}
		return filteredSentences;
	}
	
	
	public boolean hasCorrectNumberOfWords(int minNumberOfWords, int maxNumberOfWords) {
		return minNumberOfWords <= this.getWordCount() && this.getWordCount() <= maxNumberOfWords;
	}

	
	public boolean hasCorrectNumberOfLetters(int minNumberOfLetters, int maxNumberOfLetters) {
		return minNumberOfLetters <= this.getRawSentence().length() && this.getRawSentence().length() <= maxNumberOfLetters;
	}


	public void updateLemmaSet(TextDatabase database) {
		lemmaSet = null;
		getLemmaSet(database); // this will update the lemmaset if lemmaSet=null
	}


	
}
