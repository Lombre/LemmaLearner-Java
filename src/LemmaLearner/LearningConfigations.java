package LemmaLearner;

public interface LearningConfigations {

	int getMaxTimesLemmaShouldBeLearned();

	int getMaxNumberOfSentencesToLearn();

	boolean shouldConjugationsBeScored();

}