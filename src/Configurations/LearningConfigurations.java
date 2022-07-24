package Configurations;

public interface LearningConfigurations extends BaseTestable {
	
	public double getScoreExponent();
	
	public double getConjugationScoreFactor();
	
	public int getMaxTimesLemmaShouldBeLearned();

	public int getMaxNumberOfSentencesToLearn();	

	public boolean shouldConjugationsBeScored();

	public boolean shouldNegativelyScoreNonWords();

	public boolean enableSpaceSavingMode();
}
