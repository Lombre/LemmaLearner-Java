package LemmaLearner;

import java.util.*;
import java.util.stream.Collectors;



public class GreedyLearner {
	
	TextDatabase database;
	public static final String NOT_A_SENTENCE_STRING = "No sentence found.";

	public GreedyLearner(TextDatabase database) {
		this.database = database;
	}
	
	
	public List<Pair<Word, Sentence>> learnAllLemmas() {	
		List<Pair<Word, Sentence>> learningOrder = new ArrayList<Pair<Word, Sentence>>();
		Set<Word> learnedWords = new HashSet<Word>();
		PriorityQueue<Word> wordsByFrequency = getWordsByFrequency();
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getDirectlyLearnableWordsByFrequency(learnedWords);
		Set<Sentence> seenSentences = new HashSet<Sentence>(directlyLearnableSentencesByFrequency);
		long absoluteStartTime = System.currentTimeMillis();		

		while (!wordsByFrequency.isEmpty()) {
			Word newlyLearnedWord;
			if (directlyLearnableSentencesByFrequency.isEmpty()) {
				newlyLearnedWord = learnWordWithoutSentence(wordsByFrequency, learnedWords, learningOrder);
				updateDirectlyLearnableSentences(newlyLearnedWord, learnedWords, directlyLearnableSentencesByFrequency, seenSentences);
			} else {
				Sentence directlyLearnableSentence = directlyLearnableSentencesByFrequency.poll();	
				if (directlyLearnableSentence.getUnlearnedWords(learnedWords, database).isEmpty())
					continue;
				else {
					newlyLearnedWord = learnWordFromSentence(directlyLearnableSentence, learnedWords, learningOrder, wordsByFrequency);	
					updateDirectlyLearnableSentences(newlyLearnedWord, learnedWords, directlyLearnableSentencesByFrequency, seenSentences);				
				}
			}			
		}	
		
		List<Word> wordsLearnedFromSentences = learningOrder.stream()
															.filter(pair -> !pair.getSecond().equals(NOT_A_SENTENCE_STRING))
															.map(pair -> pair.getFirst())
															.collect(Collectors.toList());
		System.out.println("Number of words learned from sentences: " + wordsLearnedFromSentences.size() + " of " + database.allWords.size());

		long absoluteEndTime = System.currentTimeMillis();	
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000; //In minutes		
		System.out.println("Learned all words in " + absoluteTimeUsed + " seconds.");				
		
		return learningOrder;
	}


	private void updateDirectlyLearnableSentences( Word newlyLearnedWord, Set<Word> learnedWords, 
												   PriorityQueue<Sentence> directlyLearnableSentencesByUnlearnedWordFrequency, Set<Sentence> seenSentences) {
		for (Sentence sentence : newlyLearnedWord.getSentences()) {
			if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedWords, database)) {
				directlyLearnableSentencesByUnlearnedWordFrequency.add(sentence);
				seenSentences.add(sentence);
			}
		}
	}


	private Word learnWordFromSentence(Sentence directlyLearnableSentence, Set<Word> learnedWords, List<Pair<Word, Sentence>> learningOrder, PriorityQueue<Word> wordsByFrequency) {
		Word wordToLearn = directlyLearnableSentence.getUnlearnedWords(learnedWords, database).get(0);
		learnedWords.add(wordToLearn);
		wordsByFrequency.remove(wordToLearn);
		learningOrder.add(new Pair<Word, Sentence>(wordToLearn, directlyLearnableSentence));
		printLearnedInformation(learningOrder);
		return wordToLearn;
	}


	private Word learnWordWithoutSentence(PriorityQueue<Word> wordsByFrequency, Set<Word> learnedWords, List<Pair<Word, Sentence>> learningOrder) {
		Word wordToLearn = wordsByFrequency.poll();
		learnedWords.add(wordToLearn);
		learningOrder.add(new Pair<Word, Sentence>(wordToLearn, new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<Word>())));
		printLearnedInformation(learningOrder);
		return wordToLearn;
	}


	private void printLearnedInformation(List<Pair<Word, Sentence>> learningOrder) {
		var learnedWordSentencePair = learningOrder.get(learningOrder.size() - 1);		
		if (learningOrder.size() <= 1000 || (learningOrder.size()) % 100 == 0) {
			System.out.println((learningOrder.size()) + ", " +  learnedWordSentencePair.getFirst() + ", " + learnedWordSentencePair.getFirst().getFrequency() + ": " + learnedWordSentencePair.getSecond());
			
		}
	}


	public PriorityQueue<Sentence> getDirectlyLearnableWordsByFrequency(Set<Word> learnedWords) {
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getSentencesByUnlearnedWordFrequency(learnedWords);
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedWords, database)) 
				directlyLearnableSentencesByFrequency.add(sentence);
		}
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Sentence> getSentencesByUnlearnedWordFrequency(Set<Word> learnedWords) {
		Comparator<Sentence> sentencesByUnlearnedWordFrequency = new Comparator<Sentence>() {			
			@Override
			public int compare(Sentence sentence1, Sentence sentence2) {
				int maxFrequencyInSentence1;
				int maxFrequencyInSentence2;
				
				if (sentence1.getUnlearnedWords(learnedWords, database).isEmpty())
					maxFrequencyInSentence1 = 0;
				else maxFrequencyInSentence1 = sentence1.getUnlearnedWords(learnedWords, database).stream().max((word1, word2) -> Integer.compare(word2.getFrequency(), word1.getFrequency()))
																										   .get().getFrequency();
												
				if (sentence2.getUnlearnedWords(learnedWords, database).isEmpty())
					maxFrequencyInSentence2 = 0;
				else maxFrequencyInSentence2 = sentence2.getUnlearnedWords(learnedWords, database).stream().max((word1, word2) -> Integer.compare(word2.getFrequency(), word1.getFrequency()))
																										   .get().getFrequency();
				
				return maxFrequencyInSentence2 - maxFrequencyInSentence1;
			}
		};
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = new PriorityQueue<Sentence>(sentencesByUnlearnedWordFrequency);
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Word> getWordsByFrequency() {
		PriorityQueue<Word> wordsByFrequency = new PriorityQueue<Word>((word1, word2) -> Integer.compare(word2.getFrequency(), word1.getFrequency()));
		wordsByFrequency.addAll(database.allWords.values());
		return wordsByFrequency;
	}
	
	
	
}


