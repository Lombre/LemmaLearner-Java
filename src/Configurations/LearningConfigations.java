package Configurations;

public interface LearningConfigations extends BaseTestable {
	
	public double getScoreExponent();
	
	public int getMaxTimesLemmaShouldBeLearned();

	public int getMaxNumberOfSentencesToLearn();	

	public boolean shouldConjugationsBeScored();

}