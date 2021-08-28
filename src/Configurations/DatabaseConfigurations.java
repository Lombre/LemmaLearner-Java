package Configurations;

public interface DatabaseConfigurations extends BaseTestable{

	boolean shouldLoadSavedTexts();

	boolean shouldSaveTexts();
	
	String getLanguage();

}