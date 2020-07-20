package LemmaLearner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.parse.ANTLRParser.parserRule_return;


public class GreedyLearner {
	
	TextDatabase database;

	public GreedyLearner(TextDatabase database) {
		this.database = database;
	}
	
	
	public void learnAllLemmas() {	
		List<Pair<Word, String>> learningOrder = new ArrayList<Pair<Word, String>>();
		Set<Word> learnedWords = new HashSet<Word>();
		PriorityQueue<Word> wordsByFrequency = getWordsByFrequency();
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getDirectlyLearnableWordsByFrequency(learnedWords);
		Set<Sentence> seenSentences = new HashSet<Sentence>(directlyLearnableSentencesByFrequency);

		while (!wordsByFrequency.isEmpty()) {
			Word learnedWord;
			if (directlyLearnableSentencesByFrequency.isEmpty()) {
				learnedWord = learnWordWithoutSentence(wordsByFrequency, learnedWords, learningOrder);
				updateDirectlyLearnableSentences(learnedWords, directlyLearnableSentencesByFrequency, seenSentences, learnedWord);
			} else {
				Sentence directlyLearnableSentence = directlyLearnableSentencesByFrequency.poll();				
				if (directlyLearnableSentence.getUnlearnedWords(learnedWords, database).isEmpty())
					continue;
				else {
					learnedWord = learnWordFromSentence(directlyLearnableSentence, learningOrder, learnedWords, wordsByFrequency);	
					updateDirectlyLearnableSentences(learnedWords, directlyLearnableSentencesByFrequency, seenSentences, learnedWord);				
				}
			}			
		}	
		
		List<Word> wordsLearnedFromSentences = learningOrder.stream()
															.filter(pair -> !pair.getSecond().equals("No sentence found."))
															.map(pair -> pair.getFirst())
															.collect(Collectors.toList());
		System.out.println("Number of words learned from sentences: " + wordsLearnedFromSentences.size() + " of " + database.allWords.size());
		
		
	}


	private void updateDirectlyLearnableSentences(Set<Word> learnedWords,
			PriorityQueue<Sentence> directlyLearnableSentencesByFrequency, Set<Sentence> seenSentences,
			Word learnedWord) {
		for (Sentence sentence : learnedWord.getSentences()) {
			if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedWords, database)) {
				directlyLearnableSentencesByFrequency.add(sentence);
				seenSentences.add(sentence);
			}
		}
	}


	private Word learnWordFromSentence(Sentence directlyLearnableSentence, List<Pair<Word, String>> learningOrder, Set<Word> learnedWords, PriorityQueue<Word> wordsByFrequency) {
		Word wordToLearn = directlyLearnableSentence.getUnlearnedWords(learnedWords, database).get(0);
		learnedWords.add(wordToLearn);
		wordsByFrequency.remove(wordToLearn);
		learningOrder.add(new Pair<Word, String>(wordToLearn, directlyLearnableSentence.getRawSentence()));
		printLearnedInformation(learningOrder);
		return wordToLearn;
	}


	private Word learnWordWithoutSentence(PriorityQueue<Word> wordsByFrequency, Set<Word> learnedWords, List<Pair<Word, String>> learningOrder) {
		Word wordToLearn = wordsByFrequency.poll();
		learnedWords.add(wordToLearn);
		learningOrder.add(new Pair<Word, String>(wordToLearn, "No sentence found."));
		printLearnedInformation(learningOrder);
		return wordToLearn;
	}


	private void printLearnedInformation(List<Pair<Word, String>> learningOrder) {
		var learnedWordSentencePair = learningOrder.get(learningOrder.size() - 1);				
		System.out.println(learnedWordSentencePair.getFirst().getFrequency() + ", " + learnedWordSentencePair.getFirst() + ": " + learnedWordSentencePair.getSecond());
	}


	private PriorityQueue<Sentence> getDirectlyLearnableWordsByFrequency(Set<Word> learnedWords) {
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getSentencesByWordFrequency();
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedWords, database)) 
				directlyLearnableSentencesByFrequency.add(sentence);
		}
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Sentence> getSentencesByWordFrequency() {
		Comparator<Sentence> sentencesByWordFrequency = new Comparator<Sentence>() {			
			TextDatabase database = GreedyLearner.this.database;
			
			@Override
			public int compare(Sentence sentence1, Sentence sentence2) {
				int maxFrequencyInSentence1 = sentence1.getWordsInDatabase(database).stream().max((word1, word2) -> word1.getFrequency() - word2.getFrequency()).get().getFrequency();
				int maxFrequencyInSentence2 = sentence2.getWordsInDatabase(database).stream().max((word1, word2) -> word1.getFrequency() - word2.getFrequency()).get().getFrequency();
				return maxFrequencyInSentence2 - maxFrequencyInSentence1;
			}
		};
		PriorityQueue<Sentence> directlyLearnableSentencesByFrequency = new PriorityQueue<Sentence>(sentencesByWordFrequency);
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Word> getWordsByFrequency() {
		PriorityQueue<Word> wordsByFrequency = new PriorityQueue<Word>((word1, word2) -> word2.getFrequency() - word1.getFrequency());
		wordsByFrequency.addAll(database.allWords.values());
		return wordsByFrequency;
	}
	
	public class Pair<A, B> {
	    private A first;
	    private B second;

	    public Pair(A first, B second) {
	        super();
	        this.first = first;
	        this.second = second;
	    }

	    public int hashCode() {
	        int hashFirst = first != null ? first.hashCode() : 0;
	        int hashSecond = second != null ? second.hashCode() : 0;

	        return (hashFirst + hashSecond) * hashSecond + hashFirst;
	    }

	    public boolean equals(Object other) {
	        if (other instanceof Pair) {
	            Pair otherPair = (Pair) other;
	            return 
	            ((  this.first == otherPair.first ||
	                ( this.first != null && otherPair.first != null &&
	                  this.first.equals(otherPair.first))) &&
	             (  this.second == otherPair.second ||
	                ( this.second != null && otherPair.second != null &&
	                  this.second.equals(otherPair.second))) );
	        }

	        return false;
	    }

	    public String toString()
	    { 
	           return "(" + first + ", " + second + ")"; 
	    }

	    public A getFirst() {
	        return first;
	    }

	    public void setFirst(A first) {
	        this.first = first;
	    }

	    public B getSecond() {
	        return second;
	    }

	    public void setSecond(B second) {
	        this.second = second;
	    }
	}
	
}


