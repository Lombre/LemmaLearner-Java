
package LemmaLearner;

import java.util.*;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import Configurations.LearningConfigurations;
import GUI.ProgressPrinter;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public abstract class BasicLearner implements Learner {

	protected TextDatabase database;
	protected List<LearningElement> orderOfLearnedLemmas;
	protected Set<Lemma> learnedLemmas = new HashSet<Lemma>();
	protected Set<Sentence> seenSentences;
	protected PriorityQueue<Lemma> lemmasByFrequency;
	protected TreePriorityQueue<Sentence> directlyLearnableSentencesByScore;
	public static final String NOT_A_SENTENCE_STRING = TextDatabase.NOT_A_WORD_STRING + ".";
	protected final LearningConfigurations config;
    protected final SentenceScorer scorer;
	public ProgressPrinter progressPrinter;

	public HashMap<List<Lemma>, TreePriorityQueue<Sentence>> sentencesWithRequiredLemmas;
	public HashMap<Lemma, ListSet<List<Lemma>>> lemmaToListSet = new  HashMap<Lemma, ListSet<List<Lemma>>>();

	public BasicLearner(TextDatabase database, LearningConfigurations config, SentenceScorer scorer) {
		this.database = database;
		this.config = config;
        this.scorer = scorer;
	}

	public List<LearningElement> learnAllLemmas() {

		long absoluteStartTime = System.currentTimeMillis();

		initializeForLearning();

		//Learns lemmas one by one.
		while (!hasFinishedLearningLemmas()) {
			learnNextLemma();
			LearningProgressPrinter.printFractionOfLemmasLearned(config, database, orderOfLearnedLemmas);
		}

		if (config.shouldPrintText())
			LearningProgressPrinter.printFinishedLearningLemmasInformation(absoluteStartTime, orderOfLearnedLemmas, learnedLemmas, database);

		return orderOfLearnedLemmas;
	}

	public void learnNextLemma(){
		if (directlyLearnableSentencesByScore.isEmpty())
			learnLemmaWithoutSentence();
		else
			learnLemmasInSentence(getBestScoringDirectlyLearnableSentence());
	}

    private Lemma learnLemmaWithoutSentence() {
		Lemma lemmaToLearn = lemmasByFrequency.poll();
		lemmaToLearn.incrementTimesLearned();
		learnLemma(new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>()), lemmaToLearn);
		return lemmaToLearn;
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
		seenSentences = new HashSet<Sentence>();
		orderOfLearnedLemmas = new ArrayList<LearningElement>();
		learnedLemmas = new HashSet<Lemma>();
		lemmasByFrequency = getLemmasByFrequency();
		directlyLearnableSentencesByScore = getDirectlyLearnableSentencesByFrequency(learnedLemmas);
	}

	protected boolean hasFinishedLearningLemmas() {
		return lemmasByFrequency.isEmpty() || !(learnedLemmas.size() < config.getMaxNumberOfSentencesToLearn());
	}


	private void learnNotAWordLemma() {
		Lemma notAWordLemma = database.allLemmas.get(TextDatabase.NOT_A_WORD_STRING);
		//We say that we have learned it 100 times, to discount the value it adds to sentences:
		for (int i = 0; i < config.getMaxTimesLemmaShouldBeLearned() - 1; i++)
			notAWordLemma.incrementTimesLearned();
		Sentence notAWordSentence = new Sentence(NOT_A_SENTENCE_STRING, new ArrayList<String>(){{add(TextDatabase.NOT_A_WORD_STRING);}});
		var notAWordWord = new Conjugation(notAWordSentence, TextDatabase.NOT_A_WORD_STRING);
		database.allWords.put(TextDatabase.NOT_A_WORD_STRING, notAWordWord);
		notAWordLemma.addConjugation(notAWordWord);

		learnLemmasInSentence(notAWordSentence);

	}

	public List<Pair<Sentence, Double>> getNBestScoringSentencesWithPutBack(int n) {
		var alternatives = getNBestScoringSentences(n);
		alternatives.forEach(sentence -> addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByScore));
		var alternativesWithScore = alternatives.stream()
			.map(sentence -> new Pair<Sentence, Double>(sentence, getScore(sentence)))
			.collect(Collectors.toList());
		return alternativesWithScore;
	}

	private ArrayList<Sentence> getNBestScoringSentences(int n) {
		//Give up to n alternative sentences:
		int numAlternatives = Math.min(n, directlyLearnableSentencesByScore.size());
		var alternatives = new ArrayList<Sentence>();

		for (int i = 0; i < numAlternatives; i++) {
			Sentence currentAlternative = getBestScoringDirectlyLearnableSentence();
			alternatives.add(currentAlternative);
		}
		return alternatives;
	}

	protected abstract Sentence getBestScoringDirectlyLearnableSentence();

	private void learnInitialSentence() {

		//Find the sentence in the database with the max score
		int bestSentenceScore = -1;
		Sentence bestScoringSentence = null;

		//getNBestInitialSentences();

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

        // Update which lemmas have been learned.
        var lemmasToLearn = sentence.getUnlearnedLemmas(learnedLemmas, database);
		for (Lemma lemmaToLearn : lemmasToLearn) {
			learnedLemmas.add(lemmaToLearn);
			lemmasByFrequency.remove(lemmaToLearn); //It is not possible to learn the lemma again.
		}

		// This must be done afterwards.
		for (Lemma lemmaToLearn : lemmasToLearn) {
			updateDirectlyLearnableSentences(lemmaToLearn);
		}
		orderOfLearnedLemmas.add(new LearningElement(lemmasToLearn, sentence));
		progressPrinter.printLearnedLemmas(config, orderOfLearnedLemmas, database);
		updateSentencesAssociatedWithLemmasInSentence(sentence);
	}

	private void updateDirectlyLearnableSentences(Lemma newlyLearnedLemma) {
		//Only sentences which contain the newly learned lemma, will actually be affected.
		//If there are no longer any new lemmas in them, they should be removed,
		//but if there is a single new lemma in them, they are a potential candidate for learning that lemma.

		for (Sentence sentence : newlyLearnedLemma.getSentences()) {
			if (directlyLearnableSentencesByScore.contains(sentence) && sentence.hasNoNewLemmas(learnedLemmas, database)) {
				directlyLearnableSentencesByScore.remove(sentence);
			} else if (!seenSentences.contains(sentence) && isDirectlyLearnable(sentence)) {
				addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByScore);
				seenSentences.add(sentence);
			}
		}
	}

    public abstract boolean isDirectlyLearnable(Sentence sentence);

    private double getScore(Sentence sentence){
        return scorer.getScore(sentence);
    }

	protected boolean addSentenceToDirectlyLearnableSentencesQueue(Sentence sentence, TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency) {
		return directlyLearnableSentencesByFrequency.add(sentence, getScore(sentence));
	}

	protected void updateSentencesAssociatedWithLemmasInSentence(Sentence directlyLearnableSentence) {
		//As the lemmas in the sentences have been learned one more time,
		//the score associated with sentences containing one ore more of these lemmas might have changed.
		//Any sentence that contains one of these lemmas, and which is directly learnable, might therefore need to have their score updated.

		Set<Sentence> sentencesToUpdate = new HashSet<Sentence>();
		for (var lemma : directlyLearnableSentence.getLemmaSet(database)) {
			if (lemma.getTimesLearned() <= config.getMaxTimesLemmaShouldBeLearned()) {
				sentencesToUpdate.addAll(lemma.getSentences());
			}
		}

		for (var conjugation : directlyLearnableSentence.getWordSet(database)) {
			if (conjugation.getTimesLearned() <= config.getMaxTimesLemmaShouldBeLearned()) {
				sentencesToUpdate.addAll(conjugation.getSentences());
			}
		}

		for (var sentence : sentencesToUpdate){
			if (directlyLearnableSentencesByScore.contains(sentence)) {
				directlyLearnableSentencesByScore.update(sentence, getScore(sentence));
			}
		}
			// if (lemma.getTimesLearned() <= config.getMaxTimesLemmaShouldBeLearned()) {
			// 	for (Sentence sentence : lemma.getSentences()) {
			// 		if (directlyLearnableSentencesByScore.contains(sentence)) {
			// 			directlyLearnableSentencesByScore.update(sentence, getScore(sentence));
			// 		}
			// 	}
			// }
	}

	protected void learnLemma(Sentence directlyLearnableSentence, Lemma lemmaToLearn) {
		learnedLemmas.add(lemmaToLearn);

		for (Lemma lemma : directlyLearnableSentence.getLemmaSet(database))
			lemma.incrementTimesLearned();

		for (Conjugation conjugation : directlyLearnableSentence.getWordSet(database))
			conjugation.incrementTimesLearned();

		lemmasByFrequency.remove(lemmaToLearn); //It is not possible to learn the lemma again.
                                                //
		orderOfLearnedLemmas.add(new LearningElement(new ArrayList<Lemma>(){{add(lemmaToLearn);}}, directlyLearnableSentence));
		progressPrinter.printLearnedLemmas(config, orderOfLearnedLemmas, database);

		updateDirectlyLearnableSentences(lemmaToLearn);
	}

	public abstract TreePriorityQueue<Sentence> getDirectlyLearnableSentencesByFrequency(Set<Lemma> learnedLemmas);

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

	public List<LearningElement> getLearningList() {
		return orderOfLearnedLemmas;
	}

	public void reloadSentencesAssociatedWithLemma(Lemma lemma) {
		var sentences = lemma.getSentences();
		for (Sentence sentence : sentences) {
			if (directlyLearnableSentencesByScore.contains(sentence))
				directlyLearnableSentencesByScore.remove(sentence);
			if (isDirectlyLearnable(sentence)){
				addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByScore);
			}
		}
	}

	public void updateLearningWithNewLemmatization(Lemma oldLemma, Lemma newLemma) {
		reloadSentencesAssociatedWithLemma(oldLemma);
		reloadSentencesAssociatedWithLemma(newLemma);
		if (lemmasByFrequency.contains(oldLemma)){
			lemmasByFrequency.remove(oldLemma);
			lemmasByFrequency.add(oldLemma);
		}
		if (lemmasByFrequency.contains(newLemma)){
			lemmasByFrequency.remove(newLemma);
			lemmasByFrequency.add(newLemma);
		}
	}

	@Override
	public void setProgressPrinter(ProgressPrinter progressPrinter) {
		this.progressPrinter = progressPrinter;
	}

}
