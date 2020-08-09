package LemmaLearner;

import java.util.*;
import java.util.stream.Collectors;



public class GreedyLearner {
	
	private TextDatabase database;
	private List<Pair<Lemma, Sentence>> learningOrder = new ArrayList<Pair<Lemma, Sentence>>();
	private Set<Lemma> learnedLemmas = new HashSet<Lemma>();	
	private Set<Sentence> seenSentences = new HashSet<Sentence>();
	private PriorityQueue<Lemma> lemmasByFrequency;
	private Set<Sentence> directlyLearnableSentencesByFrequency;
	public static final String NOT_A_SENTENCE_STRING = "No sentence found.";

	public GreedyLearner(TextDatabase database) {
		this.database = database;
	}
	
	
	public List<Pair<Lemma, Sentence>> learnAllLemmas() {	
		
		lemmasByFrequency = getLemmasByFrequency();
		directlyLearnableSentencesByFrequency = getDirectlyLearnableLemmasByFrequency(learnedLemmas);
		long absoluteStartTime = System.currentTimeMillis();
		
		learnInitialSentence();		

		while (!lemmasByFrequency.isEmpty()) {
			Lemma newlyLearnedLemma;
			if (directlyLearnableSentencesByFrequency.isEmpty()) {
				newlyLearnedLemma = learnLemmaWithoutSentence();
				updateDirectlyLearnableSentences(newlyLearnedLemma);
			} else {
				
				Sentence directlyLearnableSentence = getBestSentence();	
				if (directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database).isEmpty())
					continue;
				else {
					newlyLearnedLemma = learnLemmaFromSentence(directlyLearnableSentence);	
					updateDirectlyLearnableSentences(newlyLearnedLemma);				
				}
			}			
		}	
		
		List<Lemma> lemmasLearnedFromSentences = learningOrder.stream()
															.filter(pair -> !pair.getSecond().getRawSentence().equals(NOT_A_SENTENCE_STRING))
															.map(pair -> pair.getFirst())
															.collect(Collectors.toList());
		
		System.out.println("Number of lemmas learned from sentences: " + lemmasLearnedFromSentences.size() + " of " + database.allLemmas.size());

		long absoluteEndTime = System.currentTimeMillis();	
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000; //In minutes		
		System.out.println("Learned all words in " + absoluteTimeUsed + " seconds.");	
		
		HashMap<Integer, List<Lemma>> timesLemmasHaveBeenLearned = new HashMap<Integer, List<Lemma>>();
		for (Lemma lemma : database.allLemmas.values()) {
			if (timesLemmasHaveBeenLearned.containsKey(lemma.getFrequency())) {
				timesLemmasHaveBeenLearned.get(lemma.getFrequency()).add(lemma);
			} else {
				var listLemma = new ArrayList<Lemma>();
				listLemma.add(lemma);
				timesLemmasHaveBeenLearned.put(lemma.getFrequency(), listLemma);
			}
		}
		int i = 0;
		int sumNumberOfLemmas = 0;
		for (; i <= 10; i++) {
			if (timesLemmasHaveBeenLearned.containsKey(i)) {
				System.out.println("Number of lemmas that have been learned " + i + " times: " + timesLemmasHaveBeenLearned.get(i).size());	
				sumNumberOfLemmas += timesLemmasHaveBeenLearned.get(i).size();
			}
		}
		System.out.println("Number of lemmas that have been learned more than " + (i-1) + " times: " + (database.allLemmas.size() - sumNumberOfLemmas));
		
		
		return learningOrder;
	}


	private Sentence getBestSentence() {
		double bestScore = -1;
		Sentence bestSentence = null;
		for (Sentence sentence : directlyLearnableSentencesByFrequency) {
			double sentenceScore = sentence.getScore(database);
			if (bestScore < sentenceScore) {
				bestScore = sentenceScore;
				bestSentence = sentence;
			}
		}
		return bestSentence;
	}


	private void learnInitialSentence() {
		
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
		seenSentences.add(bestScoringSentence);
		for (Lemma lemma : bestScoringSentence.getLemmaSet(database)) {
			learnLemma(bestScoringSentence, lemma);
			updateDirectlyLearnableSentences(lemma);	
		}
	}


	private void updateDirectlyLearnableSentences( Lemma newlyLearnedLemma) {
		for (Sentence sentence : newlyLearnedLemma.getSentences()) {			
			if (directlyLearnableSentencesByFrequency.contains(sentence) && sentence.hasNoNewLemmas(learnedLemmas, database)) {
				directlyLearnableSentencesByFrequency.remove(sentence);				
			} else if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedLemmas, database)) {
				directlyLearnableSentencesByFrequency.add(sentence);
				seenSentences.add(sentence);
			}
		}
		System.out.println("Priority queue size: " + directlyLearnableSentencesByFrequency.size());
	}


	private Lemma learnLemmaFromSentence(Sentence directlyLearnableSentence) {
		Lemma lemmaToLearn = directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database).get(0);
		learnLemma(directlyLearnableSentence, lemmaToLearn);
		return lemmaToLearn;
	}


	private void learnLemma(Sentence directlyLearnableSentence, Lemma lemmaToLearn) {
		lemmaToLearn.incrementTimesLearned();
		learnedLemmas.add(lemmaToLearn);
		lemmasByFrequency.remove(lemmaToLearn);
		learningOrder.add(new Pair<Lemma, Sentence>(lemmaToLearn, directlyLearnableSentence));
		printLearnedInformation(learningOrder);
	}


	private Lemma learnLemmaWithoutSentence() {
		Lemma lemmaToLearn = lemmasByFrequency.poll();
		lemmaToLearn.incrementTimesLearned();
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


	public Set<Sentence> getDirectlyLearnableLemmasByFrequency(Set<Lemma> learnedLemmas) {
		Set<Sentence> directlyLearnableSentencesByFrequency = new HashSet<Sentence>();
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedLemmas, database) && 0 < sentence.getRawWordList().size()) {
				directlyLearnableSentencesByFrequency.add(sentence);				
				seenSentences.add(sentence);
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


