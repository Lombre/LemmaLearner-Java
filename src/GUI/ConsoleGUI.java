package GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Configurations.Configurations;
import Configurations.GuiConfigurations;
import Configurations.LearningConfigurations;
import LemmaLearner.ParsingProgressStruct;
import LemmaLearner.SortablePair;
import LemmaLearner.TextDatabase;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public class ConsoleGUI implements ProgressPrinter {

	private Mediator mediator;
	private int numberOfTexts = 0;
	
	public ConsoleGUI() {
		
	}

	@Override
	public void beginParsingTexts(int numberOfTexts, String folderLocation) {
		this.numberOfTexts = numberOfTexts;
		System.out.println("Beginning to parse the " + numberOfTexts + " texts in folder " + folderLocation + ".");
	}

	@Override
	public void printProgressInParsingTexts(File textFile, ParsingProgressStruct progressReporter) {
		System.out.println("Parsed text " + (progressReporter.parsedTextCounter + 1) + " of " + numberOfTexts + " - " + textFile.getName() + ".");
	}

	@Override
	public void printLearnedLemma(LearningConfigurations config, List<SortablePair<Lemma, Sentence>> orderOfLearnedLemmas, TextDatabase database) {
		if (!config.shouldPrintText())
			return;
		var newlyLearnedLemma = orderOfLearnedLemmas.get(orderOfLearnedLemmas.size() - 1).getFirst();
		var associatedSentence = orderOfLearnedLemmas.get(orderOfLearnedLemmas.size() - 1).getSecond();
		int lemmaNumber = orderOfLearnedLemmas.size();
		if (orderOfLearnedLemmas.size() % 100 == 0 || orderOfLearnedLemmas.size() < 1000) {
			System.out.println(lemmaNumber + ") " + newlyLearnedLemma + ", " + newlyLearnedLemma.getFrequency() + " -> " + associatedSentence);
			System.out.println("\t\t ->" + associatedSentence.getLemmatizedRawSentence(database));
		}
	}
	
	
	@Override
	public void beginAddTextsToDatabase(int size) {
		numberOfTexts = size;
		System.out.println("Began adding " + size  + " texts to the database.");
	}

	int textCounter = 0;
	@Override
	public void printAddedTextToDatabase() {
		textCounter++;
		System.out.println("Added text " + textCounter + " to the database.");
	}

	@Override
	public void printFinishedAddingTexts() {
		System.out.println("Finished adding text.");
	}

	@Override
	public void setMediator(Mediator mediator) {
		this.mediator = mediator;
	}

	public void runProgram() {
		
		mediator.loadFilesInLanguageSpecificFolder();
				
		mediator.startLearning();
			
		/*
		mediator.loadProgress();
		mediator.saveProgress();
		*/
	}

	@Override
	public void initialize(GuiConfigurations config) {
		this.runProgram();
	}



}
