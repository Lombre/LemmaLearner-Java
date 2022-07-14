package Configurations;

public interface DatabaseConfigurations extends BaseTestable{

	boolean shouldLoadSavedTexts();

	boolean shouldSaveTexts();
	
	String getLanguage();

	int getMaxSentenceLengthInWords();
	
	int getMinSentenceLengthInWords();

	int getMaxSentenceLengthInLetters();
	
	int getMinSentenceLengthInLetters();

}