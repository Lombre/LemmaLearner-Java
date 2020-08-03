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
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = getDirectlyLearnableWordsByFrequency(learnedWords);
		Set<Sentence> seenSentences = new HashSet<Sentence>();
		directlyLearnableSentencesByFrequency.stream().forEach(sentenceScorePair -> seenSentences.add(sentenceScorePair.getFirst()));;
		long absoluteStartTime = System.currentTimeMillis();
		
		List<Pair<Sentence, Integer>> polledPairs = new ArrayList<Pair<Sentence, Integer>>();

		while (!wordsByFrequency.isEmpty()) {
			Word newlyLearnedWord;
			if (directlyLearnableSentencesByFrequency.isEmpty()) {
				newlyLearnedWord = learnWordWithoutSentence(wordsByFrequency, learnedWords, learningOrder);
				updateDirectlyLearnableSentences(newlyLearnedWord, learnedWords, directlyLearnableSentencesByFrequency, seenSentences);
			} else {
				int k = 1;
				//polledPairs.clear();
				//while (!directlyLearnableSentencesByFrequency.isEmpty()) polledPairs.add(directlyLearnableSentencesByFrequency.poll());
				//directlyLearnableSentencesByFrequency.addAll(polledPairs);
				var sentenceScorePair = directlyLearnableSentencesByFrequency.poll();
				Sentence directlyLearnableSentence = sentenceScorePair.getFirst();	
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
												   PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByUnlearnedWordFrequency, Set<Sentence> seenSentences) {
		for (Sentence sentence : newlyLearnedWord.getSentences()) {
			if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedWords, database)) {
				var unlearnedWords = sentence.getUnlearnedWords(learnedWords, database);
				Integer maxUnlearnedWordFrequency = unlearnedWords.stream().map(word -> word.getFrequency()).max((x, y) -> x.compareTo(y)).get();
				directlyLearnableSentencesByUnlearnedWordFrequency.add(new Pair<Sentence, Integer>(sentence, -maxUnlearnedWordFrequency));
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


	public PriorityQueue<Pair<Sentence, Integer>> getDirectlyLearnableWordsByFrequency(Set<Word> learnedWords) {
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = getSentencesByUnlearnedWordFrequency(learnedWords);
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedWords, database)) {
				Pair<Sentence, Integer> sentenceScorePair = new Pair<Sentence, Integer>(sentence, -sentence.getHighestFrequency(database));
				directlyLearnableSentencesByFrequency.add(sentenceScorePair);				
			}
		}
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Pair<Sentence, Integer>> getSentencesByUnlearnedWordFrequency(Set<Word> learnedWords) {
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = new PriorityQueue<Pair<Sentence, Integer>>();
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Word> getWordsByFrequency() {
		PriorityQueue<Word> wordsByFrequency = new PriorityQueue<Word>((word1, word2) -> Integer.compare(word2.getFrequency(), word1.getFrequency()));
		wordsByFrequency.addAll(database.allWords.values());
		return wordsByFrequency;
	}
	
	
	
}


