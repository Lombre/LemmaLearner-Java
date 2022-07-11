package LemmaLearner;

import java.util.*;

import Configurations.LearningConfigations;
import GUI.ProgressPrinter;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public class GreedyLearner {
	
	private TextDatabase database;
	private List<SortablePair<Lemma, Sentence>> orderOfLearnedLemmas;
	private Set<Lemma> learnedLemmas;
	private Set<Sentence> seenSentences = new HashSet<Sentence>();
	private PriorityQueue<Lemma> lemmasByFrequency;
	private TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency;
	public static final String NOT_A_SENTENCE_STRING = "No sentence found.";
	private final LearningConfigations config;
	public ProgressPrinter progressPrinter;
	
	public HashMap<List<Lemma>, TreePriorityQueue<Sentence>> sentencesWithRequiredLemmas;
	public HashMap<Lemma, ListSet<List<Lemma>>> lemmaToListSet = new  HashMap<Lemma, ListSet<List<Lemma>>>();

	public GreedyLearner(TextDatabase database, LearningConfigations config) {
		this.database = database;
		this.config = config;
	}
	


	public List<SortablePair<Lemma, Sentence>> learnAllLemmas() {	
				
		long absoluteStartTime = System.currentTimeMillis();
		
		initializeForLearning();		
		
		//Learns lemmas one by one.
		while (!hasFinishedLearningLemmas()) {			
			learnNextLemma();		
			

			if (config.shouldPrintText()) {
				if (orderOfLearnedLemmas.size() % 100 == 0) {
					var learnedLemmas = orderOfLearnedLemmas.stream().map(pair -> pair.getFirst());
					int totalNumberOfOccurencesOfLearnedLemmas = learnedLemmas.map(lemma -> lemma.getFrequency()).reduce(0, (a,b) -> a+b);
					int totalNumberOfLemmaOccurences = database.allLemmas.values().stream().map(lemma -> lemma.getFrequency()).reduce(0, (a,b) -> a+b);
					System.out.println("Learned lemmas " + totalNumberOfOccurencesOfLearnedLemmas + 
							" of " + totalNumberOfLemmaOccurences + 
							" fraction " + 1.0*totalNumberOfOccurencesOfLearnedLemmas/totalNumberOfLemmaOccurences +
							" or 1 out of " + 1/(1 - 1.0*totalNumberOfOccurencesOfLearnedLemmas/totalNumberOfLemmaOccurences));
				}				
			}
			
		}	
		
		if (config.shouldPrintText())
			LearningProgressPrinter.printFinishedLearningLemmasInformation(absoluteStartTime, orderOfLearnedLemmas, learnedLemmas, database);
				
		return orderOfLearnedLemmas;
	}



	public void learnNextLemma() {
		if (directlyLearnableSentencesByFrequency.isEmpty())
			learnLemmaWithoutSentence();
		else 						
			learnLemmaFromDirectlyLearnableSentence(getBestScoringDirectlyLearnableSentenceWithNChoises(0));
	}



	public void initializeForLearning() {
		//All the fundamental data structures containing things like sentences needs to be set up.
		initializeDataStructures();
		
		//NotAWordLemma includes things like names, and should be ignored for the actual learning.
		//This is done by learning it at the start.
		learnNotAWordLemma();
		
		//A baseline of lemmas is required for learning the lemmas in the database.
		//To get this, all the lemmas of a sentence is learned.
		learnInitialSentence();
	}
	
	

	public void initializeDataStructures() {
		
		orderOfLearnedLemmas = new ArrayList<SortablePair<Lemma, Sentence>>();
		learnedLemmas = new HashSet<Lemma>();	
		lemmasByFrequency = getLemmasByFrequency();
		directlyLearnableSentencesByFrequency = getDirectlyLearnableSentencesByFrequency(learnedLemmas);		
	}



	private boolean hasFinishedLearningLemmas() {
		return lemmasByFrequency.isEmpty() || !(learnedLemmas.size() < config.getMaxNumberOfSentencesToLearn());
	}


	private void learnNotAWordLemma() {
		Lemma notAWordLemma = database.allLemmas.get(TextDatabase.NOT_A_WORD_STRING);
		//We say that we have learned it 100 times, to discount the value it adds to sentences:
		for (int i = 0; i < 100; i++)
			notAWordLemma.incrementTimesLearned();
		learnLemma(new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>()), notAWordLemma);
	}

	
	private Sentence getBestScoringDirectlyLearnableSentence() {	
		return directlyLearnableSentencesByFrequency.poll();
	}

	
	Scanner inScanner = new Scanner(System.in); 
	private Sentence getBestScoringDirectlyLearnableSentenceWithNChoises(int n) {	
		
		if (n == 0 || n == 1) {
			return getBestScoringDirectlyLearnableSentence();
		}
		
		var alternatives = getNBestScoringSentences(n);
		
		
		//TODO (*) Der er sikkert en bug hvis alle alternativerne bliver fjernet.
		
		//Let the user choose the desired sentence, and put all the remaining sentences back in the priority queue.
		String input = inScanner.nextLine();
		if (input.matches("\\d+")) { //A sentence is chosen
			int choiseIndex = Integer.valueOf(input) - 1; 
			Sentence choise = alternatives.remove(choiseIndex);
			alternatives.forEach(sentence -> addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency));
			return choise;
		} else { //A sentence needs to be removed as a choise.
			var inputParts = input.split(" ");
			System.out.println("Removed: " + alternatives.get(Integer.valueOf(inputParts[1]) - 1));
			alternatives.remove(Integer.valueOf(inputParts[1]) - 1);
			alternatives.forEach(sentence -> addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency));
			return getBestScoringDirectlyLearnableSentenceWithNChoises(n);
		}
		
	}


	public ArrayList<Sentence> getNBestScoringSentencesWithPutBack(int n) {
		var alternatives = getNBestScoringSentences(n);
		alternatives.forEach(sentence -> addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency));
		return alternatives;
	}


	private ArrayList<Sentence> getNBestScoringSentences(int n) {
		//Give up to n alternative sentences:
		int numAlternatives = Math.min(n, directlyLearnableSentencesByFrequency.size());
		var alternatives = new ArrayList<Sentence>();
				
		for (int i = 0; i < numAlternatives; i++) {
			Sentence currentAlternative = directlyLearnableSentencesByFrequency.poll();
			alternatives.add(currentAlternative);
		}
		return alternatives;
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
		
		if (config.shouldPrintText())
			System.out.println("Learn initial sentence with total frequency score " + bestSentenceScore + ": " + bestScoringSentence.getRawSentence());
		
		learnLemmasInSentence(bestScoringSentence);
	}



	public void learnLemmasInSentence(Sentence sentence) {
		seenSentences.add(sentence);

		for (Lemma lemma : sentence.getLemmaSet(database)) 
			lemma.incrementTimesLearned();		
		for (Conjugation conjugation : sentence.getWordSet(database)) 
			conjugation.incrementTimesLearned();	

		var lemmasToLearn = sentence.getUnlearnedLemmas(learnedLemmas, database);
		for (Lemma lemmaToLearn : lemmasToLearn) {
			//learnLemma(bestScoringSentence, lemma);
			//Should not be necessary:
			//updateDirectlyLearnableSentences(lemma);	
			

			learnedLemmas.add(lemmaToLearn);			
			lemmasByFrequency.remove(lemmaToLearn); //It is not possible to learn the lemma again.
			updateDirectlyLearnableSentences(lemmaToLearn);						
			orderOfLearnedLemmas.add(new SortablePair<Lemma, Sentence>(lemmaToLearn, sentence));			
			if (config.shouldPrintText())
				progressPrinter.printLearnedLemma(orderOfLearnedLemmas, database);
		}
		
		updateSentencesAssociatedWithLemmasInSentence(sentence);
		
	}


	private void updateDirectlyLearnableSentences(Lemma newlyLearnedLemma) {
		//Only sentences which contain the newly learned lemma, will actually be affected.
		//If there are no longer any new lemmas in them, they should be removed,
		//but if there is a single new lemma in them, they are a potential candidate for learning that lemma.
		
		for (Sentence sentence : newlyLearnedLemma.getSentences()) {
			if (directlyLearnableSentencesByFrequency.contains(sentence) && sentence.hasNoNewLemmas(learnedLemmas, database)) {
				directlyLearnableSentencesByFrequency.remove(sentence);
			} else if (!seenSentences.contains(sentence) && sentence.isDirectlyLearnable(learnedLemmas, database)) {
				addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency);
				seenSentences.add(sentence);
			}
		}
	}



	private boolean addSentenceToDirectlyLearnableSentencesQueue(Sentence sentence, TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency) {
		return directlyLearnableSentencesByFrequency.add(sentence, sentence.getScore(database, config));
	}


	public Lemma learnLemmaFromDirectlyLearnableSentence(Sentence directlyLearnableSentence) {
		Lemma lemmaToLearn = getSingleUnlearnedLemma(directlyLearnableSentence);
		learnLemma(directlyLearnableSentence, lemmaToLearn);		
		updateSentencesAssociatedWithLemmasInSentence(directlyLearnableSentence);		
		return lemmaToLearn;
	}



	private Lemma getSingleUnlearnedLemma(Sentence directlyLearnableSentence) {
		var unlearnedLemmas = directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database); 
		if (unlearnedLemmas.size() != 1) 
			throw new Error("Error: The sentence \"" + directlyLearnableSentence + "\" contained more or less than a single unlearned lemma. Unlearned lemmas: " + unlearnedLemmas);
		Lemma lemmaToLearn = unlearnedLemmas.get(0);
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
		
		for (Lemma lemma : directlyLearnableSentence.getLemmaSet(database)) 
			lemma.incrementTimesLearned();		
		for (Conjugation conjugation : directlyLearnableSentence.getWordSet(database)) 
			conjugation.incrementTimesLearned();		
		
		lemmasByFrequency.remove(lemmaToLearn); //It is not possible to learn the lemma again.
		
		orderOfLearnedLemmas.add(new SortablePair<Lemma, Sentence>(lemmaToLearn, directlyLearnableSentence));
		if (config.shouldPrintText())
			progressPrinter.printLearnedLemma(orderOfLearnedLemmas, database);
		
		updateDirectlyLearnableSentences(lemmaToLearn);			
	}


	private Lemma learnLemmaWithoutSentence() {
		Lemma lemmaToLearn = lemmasByFrequency.poll();
		lemmaToLearn.incrementTimesLearned();		
		learnLemma(new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>()), lemmaToLearn);
		return lemmaToLearn;
	}


	public TreePriorityQueue<Sentence> getDirectlyLearnableSentencesByFrequency(Set<Lemma> learnedLemmas) {
		TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getSentencePriorityQueue();
		for (Sentence sentence : database.allSentences.values()) {
			if (sentence.isDirectlyLearnable(learnedLemmas, database) && 0 < sentence.getRawWordList().size()) {
				addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency);
			}
		}
		seenSentences.addAll(directlyLearnableSentencesByFrequency.toList());
		return directlyLearnableSentencesByFrequency;
	}


	public TreePriorityQueue<Sentence> getSentencePriorityQueue() {
		TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency = new TreePriorityQueue<Sentence>((sentenceScore1, sentenceScore2) -> Double.compare(-sentenceScore1, -sentenceScore2));
		return directlyLearnableSentencesByFrequency;
	}


	public PriorityQueue<Lemma> getLemmasByFrequency() {
		PriorityQueue<Lemma> lemmasByFrequency = new PriorityQueue<Lemma>((lemma1, lemma2) -> Integer.compare(lemma2.getFrequency(), lemma1.getFrequency()));
		lemmasByFrequency.addAll(database.allLemmas.values());
		return lemmasByFrequency;
	}


	public void resetLearning() {
		database.resetLearning();		
	}



	public Set<Lemma> getLearnedLemmas() {
		return learnedLemmas;
	}



	public List<SortablePair<Lemma, Sentence>> getLearningList() {
		return orderOfLearnedLemmas;
	}
	
	
	
}


