package LemmaLearner;

import java.util.*;



public class GreedyLearner {
	
	private TextDatabase database;
	private List<Pair<Lemma, Sentence>> orderOfLearnedLemmas = new ArrayList<Pair<Lemma, Sentence>>();
	private Set<Lemma> learnedLemmas = new HashSet<Lemma>();	
	private Set<Sentence> seenSentences = new HashSet<Sentence>();
	private PriorityQueue<Lemma> lemmasByFrequency;
	private TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency;
	public static final String NOT_A_SENTENCE_STRING = "No sentence found.";
	private final LearningConfigations config;
	

	public GreedyLearner(TextDatabase database, LearningConfigations config) {
		this.database = database;
		this.config = config;
	}
	

	private void initialize() {
		LearningProgressPrinter.printLemmasWithHighNumberOfConjugations(database);				
		lemmasByFrequency = getLemmasByFrequency();
		directlyLearnableSentencesByFrequency = getDirectlyLearnableSentencesByFrequency(learnedLemmas);
	}

	public List<Pair<Lemma, Sentence>> learnAllLemmas() {	
				
		long absoluteStartTime = System.currentTimeMillis();
		initialize();
		
		//NotAWordLemma includes things like names, and should be ignored for the actual learning.
		//This is done by learning it at the start.
		learnNotAWordLemma();
		
		//A baseline of lemmas is required for learning the lemmas in the database.
		//To get this, all the lemmas of a sentence is learned.
		learnInitialSentence();		
		
		//Learns lemmas one by one.
		while (!hasFinishedLearningLemmas()) {
			if (directlyLearnableSentencesByFrequency.isEmpty())
				learnLemmaWithoutSentence();
			else 						
				learnLemmaFromDirectlyLearnableSentence(getBestScoringDirectlyLearnableSentenceWithNChoises(0));			
		}	
		
		LearningProgressPrinter.printFinishedLearningLemmasInformation(absoluteStartTime, orderOfLearnedLemmas, learnedLemmas, database);
				
		return orderOfLearnedLemmas;
	}



	private boolean hasFinishedLearningLemmas() {
		return lemmasByFrequency.isEmpty() || !(learnedLemmas.size() < config.getMaxNumberOfSentencesToLearn());
	}


	private void learnNotAWordLemma() {
		Lemma notAWordLemma = database.allLemmas.get(TextDatabase.NOT_A_WORD_STRING);
		notAWordLemma.incrementTimesLearned();
		learnLemma(new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>()), notAWordLemma);
	}

	
	private Sentence getBestScoringDirectlyLearnableSentence() {	
		return directlyLearnableSentencesByFrequency.poll();
	}

	Scanner in = new Scanner(System.in); 
	private Sentence getBestScoringDirectlyLearnableSentenceWithNChoises(int n) {	
		
		if (n == 0 || n == 1) {
			return getBestScoringDirectlyLearnableSentence();
		}
		
		//Give up to n alternative sentences:
		int numAlternatives = Math.min(n, directlyLearnableSentencesByFrequency.size());
		var alternatives = new ArrayList<Sentence>();
		
		System.out.println("Alternatives: ");
		
		for (int i = 0; i < numAlternatives; i++) {
			Sentence currentAlternative = directlyLearnableSentencesByFrequency.poll();
			alternatives.add(currentAlternative);
			System.out.println((i+1) + ") " + currentAlternative);
			System.out.println(" - " + currentAlternative.getLemmatizedRawSentence(database));
		}
		
		//TODO (*) Der er sikkert en bug hvis alle alternativerne bliver fjernet.
		
		//Let the user choose the desired sentence, and put all the remaining sentences back in the priority queue.
		String input = in.nextLine();
		if (input.matches("\\d+")) { //A sentence is chosen
			int choiseIndex = Integer.valueOf(input) - 1; 
			Sentence choise = alternatives.remove(choiseIndex);
			alternatives.forEach(sentence -> directlyLearnableSentencesByFrequency.add(sentence, sentence.getScore(database, config)));
			return choise;
		} else { //A sentence needs to be removed as a choise.
			var inputParts = input.split(" ");
			System.out.println("Removed: " + alternatives.get(Integer.valueOf(inputParts[1]) - 1));
			alternatives.remove(Integer.valueOf(inputParts[1]) - 1);
			alternatives.forEach(sentence -> directlyLearnableSentencesByFrequency.add(sentence, sentence.getScore(database, config)));
			return getBestScoringDirectlyLearnableSentenceWithNChoises(n);
		}
		
	}


	private void learnInitialSentence() {
		
		//Find the sentence in the database with the max score
		int bestSentenceScore = -1;
		Sentence bestScoringSentence = null;
		for (Sentence sentence : database.allSentences.values()) {
			//Sum of word frequencies in sentence
			int currentSentenceScore = sentence.getUnlearnedLemmas(learnedLemmas, database).stream()
															.map(lemma -> lemma.getFrequency())
															.reduce(0, (frequency1, frequency2) -> frequency1 + frequency2);
			if (bestSentenceScore < currentSentenceScore) {
				bestSentenceScore = currentSentenceScore;
				bestScoringSentence = sentence;
			}
		}		
		
		System.out.println("Learn initial sentence with total frequency score " + bestSentenceScore + ": " + bestScoringSentence.getRawSentence());
		seenSentences.add(bestScoringSentence);
		
		//Learn the lemmas in the sentence
		
		for (Lemma lemma : bestScoringSentence.getLemmaSet(database)) {
			learnLemma(bestScoringSentence, lemma);
			updateDirectlyLearnableSentences(lemma);	
		}
	}


	private void updateDirectlyLearnableSentences( Lemma newlyLearnedLemma) {
		//Only sentences which contain the newly learned lemma, will actually be affected.
		//If there are no longer any new lemmas in them, they should be removed,
		//but if there is a single new lemma in them, they are a potential candidate for learning that lemma.
		for (Sentence sentence : newlyLearnedLemma.getSentences()) {
			if (directlyLearnableSentencesByFrequency.contains(sentence) && sentence.hasNoNewLemmas(learnedLemmas, database)) {
				directlyLearnableSentencesByFrequency.remove(sentence);
			} else if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedLemmas, database)) {
				directlyLearnableSentencesByFrequency.add(sentence, sentence.getScore(database, config));
				seenSentences.add(sentence);
			}
		}
	}


	private Lemma learnLemmaFromDirectlyLearnableSentence(Sentence directlyLearnableSentence) {
		Lemma lemmaToLearn = directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database).get(0);
		learnLemma(directlyLearnableSentence, lemmaToLearn);		
		updateSentencesAssociatedWithLemmasInSentence(directlyLearnableSentence);		
		return lemmaToLearn;
	}


	private void updateSentencesAssociatedWithLemmasInSentence(Sentence directlyLearnableSentence) {
		//As the lemmas in the sentences have been learned one more time,
		//the score associated with sentences containing one ore more of these lemmas might have changed.
		//Any sentence that contains one of these lemmas, and which is directly learnable, might therefore need to have their score updated.
		for (Lemma lemma : directlyLearnableSentence.getLemmaSet(database)) {
			if (lemma.getTimesLearned() < config.getMaxTimesLemmaShouldBeLearned()) {
				for (Sentence sentence : lemma.getSentences()) {
					if (directlyLearnableSentencesByFrequency.contains(sentence)) {
						directlyLearnableSentencesByFrequency.update(sentence, sentence.getScore(database, config));
					}
				}
			}
		}
	}


	private void learnLemma(Sentence directlyLearnableSentence, Lemma lemmaToLearn) {
		learnedLemmas.add(lemmaToLearn);
		for (Lemma lemma : directlyLearnableSentence.getLemmaSet(database)) {
			lemma.incrementTimesLearned();
		}
		for (Conjugation conjugation : directlyLearnableSentence.getWordSet(database)) {
			conjugation.incrementTimesLearned();
		}
		lemmasByFrequency.remove(lemmaToLearn);
		orderOfLearnedLemmas.add(new Pair<Lemma, Sentence>(lemmaToLearn, directlyLearnableSentence));
		LearningProgressPrinter.printLearnedInformation(orderOfLearnedLemmas, database);
		updateDirectlyLearnableSentences(lemmaToLearn);			
	}


	private Lemma learnLemmaWithoutSentence() {
		Lemma lemmaToLearn = lemmasByFrequency.poll();
		lemmaToLearn.incrementTimesLearned();		
		learnLemma(new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>()), lemmaToLearn);
		return lemmaToLearn;
	}


	public TreePriorityQueue<Sentence> getDirectlyLearnableSentencesByFrequency(Set<Lemma> learnedLemmas) {
		TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getSentencesByUnlearnedWordFrequency();
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedLemmas, database) && 0 < sentence.getRawWordList().size()) {
				directlyLearnableSentencesByFrequency.add(sentence, sentence.getScore(database, config));	
			}
		}
		seenSentences.addAll(directlyLearnableSentencesByFrequency.toList());
		return directlyLearnableSentencesByFrequency;
	}


	public TreePriorityQueue<Sentence> getSentencesByUnlearnedWordFrequency() {
		TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency = new TreePriorityQueue<Sentence>((sentenceScore1, sentenceScore2) -> Double.compare(-sentenceScore1, -sentenceScore2));
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Lemma> getLemmasByFrequency() {
		PriorityQueue<Lemma> lemmasByFrequency = new PriorityQueue<Lemma>((lemma1, lemma2) -> Integer.compare(lemma2.getFrequency(), lemma1.getFrequency()));
		lemmasByFrequency.addAll(database.allLemmas.values());
		return lemmasByFrequency;
	}
	
	
	
}


