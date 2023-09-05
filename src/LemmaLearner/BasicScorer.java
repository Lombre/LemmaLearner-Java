package LemmaLearner;

import java.util.Set;

import Configurations.LearningConfigurations;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public class BasicScorer implements SentenceScorer {

    private final TextDatabase database;
    private final LearningConfigurations config;

    public BasicScorer(TextDatabase database, LearningConfigurations config){
        this.database = database;
        this.config = config;
    }

    @Override
    public double getScore(Sentence sentence) {
		double unlearnedLemmaScore = getUnlearnedLemmaFrequencyScore(sentence, database, config.getMaxTimesLemmaShouldBeLearned());
		double lemmaScore = getLemmaScore(sentence, database, config.getMaxTimesLemmaShouldBeLearned(), config.getScoreExponent());
		double conjugationScore = (config.shouldConjugationsBeScored())? getConjugationScore(sentence, database, config.getMaxTimesLemmaShouldBeLearned(), config.getScoreExponent(), config.getConjugationScoreFactor()): 0;
		double notawordModifier = (config.shouldNegativelyScoreNonWords())? getNotAWordModifier(sentence, database): 1;
		double score = unlearnedLemmaScore*(lemmaScore + conjugationScore)*notawordModifier;//unlearnedLemmaScore + lemmaScore + conjugationScore;//unlearnedLemmaScore*(lemmaScore + conjugationScore);
		return score;
    }



	private double getNotAWordModifier(Sentence sentence, TextDatabase database) {
		double modifier = 1;
		var lemmaList = sentence.getLemmaList(database);
		for (var lemma : lemmaList) {
			if (lemma.getRawLemma().equals(TextDatabase.NOT_A_WORD_STRING)) {
				modifier *= 0.85;
			}
		}
		return modifier;
	}


	private double getUnlearnedLemmaFrequencyScore(Sentence sentence, TextDatabase database, int numberOfTimesCounted) {
		double score = 0;
		var lemmas = sentence.getLemmaSet(database);
		int unlearnedLemmasCount = 0;
		for (Lemma lemma : lemmas) {
			if (lemma.getTimesLearned() == 0) {
				//The primary basis for the score is the frequency of the unlearned lemma.
				score += lemma.getFrequency();
				unlearnedLemmasCount++;
			}
		}
        double penalty = 1/Math.pow(4, unlearnedLemmasCount - 1);
        score = score*penalty;
		return score;
	}

	private double getLemmaScore(Sentence sentence, TextDatabase database, int numberOfTimesCounted, double scoreExponent) {
		double score = 0;
		var lemmas = sentence.getLemmaSet(database);
		for (Lemma lemma : lemmas) {
			if (lemma.getTimesLearned() < numberOfTimesCounted){
				double extraScore = 1.0/( Math.pow(scoreExponent, lemma.getTimesLearned()));
				score += extraScore;
			}
		}
		return score;
	}

	// Score factor is how relatively less conjugations should be score relative to lemmas
	private double getConjugationScore(Sentence sentence, TextDatabase database, int numberOfTimesCounted, double scoreExponent, double scoreFactor) {
		double score = 0;
		var conjugations = sentence.getWordSet(database);
		for (Conjugation conjugation : conjugations) {
			if (conjugation.getTimesLearned() < numberOfTimesCounted){
				double extraScore = 1.0/( Math.pow(scoreExponent, conjugation.getTimesLearned())* scoreFactor);
				score += extraScore;
			}
		}
		return score;
	}



}
