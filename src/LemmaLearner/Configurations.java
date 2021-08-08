package LemmaLearner;

public class Configurations implements LearningConfigations, DatabaseConfigurations {

	private int maxTimesLemmaShouldBeLearned = 10;

	private int maxNumberOfSentencesToLearn = 120000;	

	private double SCORE_EXPONENT = 2;
	
	private boolean shouldConjugationsBeScored = true;
	
	private final boolean shouldLoadSavedTexts = false;
	
	private final boolean shouldSaveTexts = false;
	
	private final boolean shouldPrintText = true;
	

	@Override
	public boolean shouldLoadSavedTexts() {
		return shouldLoadSavedTexts;
	}

	@Override
	public boolean shouldPrintText() {
		return shouldPrintText;
	}


	@Override
	public double getScoreExponent() {
		return SCORE_EXPONENT;
	}

	
	@Override
	public int getMaxTimesLemmaShouldBeLearned() {
		return maxTimesLemmaShouldBeLearned;
	}

	@Override
	public int getMaxNumberOfSentencesToLearn() {
		return maxNumberOfSentencesToLearn;
	}

	@Override
	public boolean shouldConjugationsBeScored() {
		return shouldConjugationsBeScored;
	}

	@Override
	public void setScoreExponent(double newExponent) {
		this.SCORE_EXPONENT = newExponent;
	}

	@Override
	public boolean shouldSaveTexts() {
		return shouldSaveTexts;
	}
	
}
