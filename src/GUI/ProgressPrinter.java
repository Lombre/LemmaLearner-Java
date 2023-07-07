package GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Configurations.Configurations;
import Configurations.GuiConfigurations;
import Configurations.LearningConfigurations;
import LemmaLearner.LearningElement;
import LemmaLearner.ParsingProgressStruct;
import LemmaLearner.SortablePair;
import LemmaLearner.TextDatabase;
import TextDataStructures.*;

public interface ProgressPrinter {
	
	public void beginParsingTexts(int numberOfTexts, String folderLocation);
	
	public void printProgressInParsingTexts(File textFile, ParsingProgressStruct progressReporter);
		
	public void printLearnedLemmas(LearningConfigurations config, List<LearningElement> orderOfLearnedLemmas, TextDatabase database);

	public void beginAddTextsToDatabase(int size);

	public void printAddedTextToDatabase();

	public void printFinishedAddingTexts();

	public void setMediator(Mediator mediator);

	public void initialize(GuiConfigurations config);
	
}
