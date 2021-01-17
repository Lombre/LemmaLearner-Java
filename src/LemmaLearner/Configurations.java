package LemmaLearner;

public class Configurations implements LearningConfigations, DatabaseConfigurations {

	private int maxTimesLemmaShouldBeLearned = 10;

	private int maxNumberOfSentencesToLearn = 12000;
	
	private boolean shouldConjugationsBeScored = true;
	
	private final boolean shouldLoadSavedTexts = true;
	
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

	
	
	
	
	
	
	
}
