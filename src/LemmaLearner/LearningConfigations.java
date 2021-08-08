package LemmaLearner;

public interface LearningConfigations {

	

	public void setScoreExponent(double newExponent);
	
	public double getScoreExponent();
	
	public int getMaxTimesLemmaShouldBeLearned();

	public int getMaxNumberOfSentencesToLearn();	

	boolean shouldPrintText();

	public boolean shouldConjugationsBeScored();

}