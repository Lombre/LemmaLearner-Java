package LemmaLearner;

import java.util.*;
import java.util.stream.Collectors;



public class GreedyLearner {
	
	TextDatabase database;
	public static final String NOT_A_SENTENCE_STRING = "No sentence found.";

	public GreedyLearner(TextDatabase database) {
		this.database = database;
	}
	
	
	public List<Pair<Lemma, Sentence>> learnAllLemmas() {	
		List<Pair<Lemma, Sentence>> learningOrder = new ArrayList<Pair<Lemma, Sentence>>();
		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		PriorityQueue<Lemma> lemmasByFrequency = getLemmasByFrequency();
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = getDirectlyLearnableLemmasByFrequency(learnedLemmas);
		Set<Sentence> seenSentences = new HashSet<Sentence>();
		directlyLearnableSentencesByFrequency.stream().forEach(sentenceScorePair -> seenSentences.add(sentenceScorePair.getFirst()));;
		long absoluteStartTime = System.currentTimeMillis();
		
		List<Pair<Sentence, Integer>> polledPairs = new ArrayList<Pair<Sentence, Integer>>();
		
		learnInitialSentence(learningOrder, learnedLemmas, lemmasByFrequency, directlyLearnableSentencesByFrequency, seenSentences);		

		while (!lemmasByFrequency.isEmpty()) {
			Lemma newlyLearnedLemma;
			if (directlyLearnableSentencesByFrequency.isEmpty()) {
				newlyLearnedLemma = learnLemmaWithoutSentence(lemmasByFrequency, learnedLemmas, learningOrder);
				updateDirectlyLearnableSentences(newlyLearnedLemma, learnedLemmas, directlyLearnableSentencesByFrequency, seenSentences);
			} else {
				int k = 1;
				//polledPairs.clear();
				//while (!directlyLearnableSentencesByFrequency.isEmpty()) polledPairs.add(directlyLearnableSentencesByFrequency.poll());
				//directlyLearnableSentencesByFrequency.addAll(polledPairs);
				var sentenceScorePair = directlyLearnableSentencesByFrequency.poll();
				Sentence directlyLearnableSentence = sentenceScorePair.getFirst();	
				if (directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database).isEmpty())
					continue;
				else {
					newlyLearnedLemma = learnLemmaFromSentence(directlyLearnableSentence, learnedLemmas, learningOrder, lemmasByFrequency);	
					updateDirectlyLearnableSentences(newlyLearnedLemma, learnedLemmas, directlyLearnableSentencesByFrequency, seenSentences);				
				}
			}			
		}	
		
		List<Lemma> lemmasLearnedFromSentences = learningOrder.stream()
															.filter(pair -> !pair.getSecond().equals(NOT_A_SENTENCE_STRING))
															.map(pair -> pair.getFirst())
															.collect(Collectors.toList());
		System.out.println("Number of words learned from sentences: " + lemmasLearnedFromSentences.size() + " of " + database.allWords.size());

		long absoluteEndTime = System.currentTimeMillis();	
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000; //In minutes		
		System.out.println("Learned all words in " + absoluteTimeUsed + " seconds.");				
		
		return learningOrder;
	}


	private void learnInitialSentence(List<Pair<Lemma, Sentence>> learningOrder, Set<Lemma> learnedLemmas, PriorityQueue<Lemma> lemmasByFrequency,
			PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency, Set<Sentence> seenSentences) {
		
		int bestSentenceScore = -1;
		Sentence bestScoringSentence = null;
		for (Sentence sentence : database.allSentences.values()) {
			//Sum of word frequencies in sentence
			int currentSentenceScore = sentence.getLemmaSet(database).stream()
															.map(lemma -> lemma.getFrequency())
															.reduce(0, (frequency1, frequency2) -> frequency1 + frequency2);
			if (bestSentenceScore < currentSentenceScore) {
				bestSentenceScore = currentSentenceScore;
				bestScoringSentence = sentence;
			}
		}
		System.out.println("Learn initial sentence with total frequency score " + bestSentenceScore + ": " + bestScoringSentence.getRawSentence());
		for (Lemma lemma : bestScoringSentence.getLemmaSet(database)) {
			learnLemma(bestScoringSentence, learnedLemmas, learningOrder, lemmasByFrequency, lemma);
			updateDirectlyLearnableSentences(lemma, learnedLemmas, directlyLearnableSentencesByFrequency, seenSentences);	
		}
	}


	private void updateDirectlyLearnableSentences( Lemma newlyLearnedLemma, Set<Lemma> learnedLemmas, 
												   PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByUnlearnedWordFrequency, Set<Sentence> seenSentences) {
		for (Sentence sentence : newlyLearnedLemma.getSentences()) {
			if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedLemmas, database)) {
				var unlearnedLemmas = sentence.getUnlearnedLemmas(learnedLemmas, database);
				Integer maxUnlearnedLemmaFrequency = unlearnedLemmas.stream().map(lemma -> lemma.getFrequency()).max((x, y) -> x.compareTo(y)).get();
				directlyLearnableSentencesByUnlearnedWordFrequency.add(new Pair<Sentence, Integer>(sentence, -maxUnlearnedLemmaFrequency));
				seenSentences.add(sentence);
			}
		}
	}


	private Lemma learnLemmaFromSentence(Sentence directlyLearnableSentence, Set<Lemma> learnedLemmas, List<Pair<Lemma, Sentence>> learningOrder, PriorityQueue<Lemma> lemmasByFrequency) {
		Lemma lemmaToLearn = directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database).get(0);
		learnLemma(directlyLearnableSentence, learnedLemmas, learningOrder, lemmasByFrequency, lemmaToLearn);
		return lemmaToLearn;
	}


	private void learnLemma(Sentence directlyLearnableSentence, Set<Lemma> learnedLemmas,
			List<Pair<Lemma, Sentence>> learningOrder, PriorityQueue<Lemma> lemmasByFrequency, Lemma lemmaToLearn) {
		learnedLemmas.add(lemmaToLearn);
		lemmasByFrequency.remove(lemmaToLearn);
		learningOrder.add(new Pair<Lemma, Sentence>(lemmaToLearn, directlyLearnableSentence));
		printLearnedInformation(learningOrder);
	}


	private Lemma learnLemmaWithoutSentence(PriorityQueue<Lemma> lemmasByFrequency, Set<Lemma> learnedLemmas, List<Pair<Lemma, Sentence>> learningOrder) {
		Lemma lemmaToLearn = lemmasByFrequency.poll();
		learnedLemmas.add(lemmaToLearn);
		learningOrder.add(new Pair<Lemma, Sentence>(lemmaToLearn, new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>())));
		printLearnedInformation(learningOrder);
		return lemmaToLearn;
	}


	private void printLearnedInformation(List<Pair<Lemma, Sentence>> learningOrder) {
		var learnedWordSentencePair = learningOrder.get(learningOrder.size() - 1);		
		if (learningOrder.size() <= 1000 || (learningOrder.size()) % 100 == 0) {
			System.out.println((learningOrder.size()) + ", " +  learnedWordSentencePair.getFirst() + ", " + learnedWordSentencePair.getFirst().getFrequency() + ": " + learnedWordSentencePair.getSecond());
			
		}
	}


	public PriorityQueue<Pair<Sentence, Integer>> getDirectlyLearnableLemmasByFrequency(Set<Lemma> learnedLemmas) {
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = getSentencesByUnlearnedWordFrequency(learnedLemmas);
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedLemmas, database) && 0 < sentence.getRawWordList().size()) {
				Pair<Sentence, Integer> sentenceScorePair = new Pair<Sentence, Integer>(sentence, -sentence.getHighestFrequency(database));
				directlyLearnableSentencesByFrequency.add(sentenceScorePair);				
			}
		}
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Pair<Sentence, Integer>> getSentencesByUnlearnedWordFrequency(Set<Lemma> learnedLemmas) {
		PriorityQueue<Pair<Sentence, Integer>> directlyLearnableSentencesByFrequency = new PriorityQueue<Pair<Sentence, Integer>>();
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Lemma> getLemmasByFrequency() {
		PriorityQueue<Lemma> lemmasByFrequency = new PriorityQueue<Lemma>((lemma1, lemma2) -> Integer.compare(lemma2.getFrequency(), lemma1.getFrequency()));
		lemmasByFrequency.addAll(database.allLemmas.values());
		return lemmasByFrequency;
	}
	
	
	
}


