package LemmaLearner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import Configurations.LearningConfigurations;
import GUI.ProgressPrinter;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public class GreedyLearner extends BasicLearner {

	final int maxLemmasPerSentence;

	public GreedyLearner(TextDatabase database, LearningConfigurations config, int maxLemmasPerSentence) {
		super(database, config, new BasicScorer(database, config));
		this.maxLemmasPerSentence = maxLemmasPerSentence;
	}

    public boolean isDirectlyLearnable(Sentence sentence){
		int numberOfUnlearnedLemmas = sentence.getNumberOfUnlearnedLemmas(learnedLemmas, database);
		return 0 < numberOfUnlearnedLemmas && numberOfUnlearnedLemmas <= maxLemmasPerSentence;
    }

	@Override
	protected Sentence getBestScoringDirectlyLearnableSentence() {
		return directlyLearnableSentencesByScore.poll();
	}

	// @Override
	// public Lemma learnLemmaFromDirectlyLearnableSentence(Sentence directlyLearnableSentence) {
	// 	Lemma lemmaToLearn = getSingleUnlearnedLemma(directlyLearnableSentence);
	// 	learnLemma(directlyLearnableSentence, lemmaToLearn);
	// 	updateSentencesAssociatedWithLemmasInSentence(directlyLearnableSentence);
	// 	return lemmaToLearn;
	// }

	// private Lemma getSingleUnlearnedLemma(Sentence directlyLearnableSentence) {
	// 	var unlearnedLemmas = directlyLearnableSentence.getUnlearnedLemmas(learnedLemmas, database);
	// 	if (unlearnedLemmas.size() != 1)
	// 		throw new Error("Error: The sentence \"" + directlyLearnableSentence + "\" contained more or less than a single unlearned lemma. Unlearned lemmas: " + unlearnedLemmas);
	// 	Lemma lemmaToLearn = unlearnedLemmas.get(0);
	// 	return lemmaToLearn;
	// }

	@Override
    public TreePriorityQueue<Sentence> getDirectlyLearnableSentencesByFrequency(Set<Lemma> learnedLemmas) {
		TreePriorityQueue<Sentence> directlyLearnableSentencesByFrequency = getSentencePriorityQueue();
		for (Sentence sentence : database.allSentences.values()) {
			if (isDirectlyLearnable(sentence) && 0 < sentence.getRawWordList().size()) {
				addSentenceToDirectlyLearnableSentencesQueue(sentence, directlyLearnableSentencesByFrequency);
			}
		}
		seenSentences.addAll(directlyLearnableSentencesByFrequency.toList());
		return directlyLearnableSentencesByFrequency;
    }
}
