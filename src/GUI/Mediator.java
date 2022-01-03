package GUI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import Configurations.Configurations;
import LemmaLearner.*;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import TextDataStructures.Text;

public class Mediator {

	private Configurations config;
	private ProgressPrinter gui;
	private TextDatabase database;
	private GreedyLearner learner;
	
	private final int numberOfAlternatives = 15;
	
	public Mediator(ProgressPrinter progressPrinter) {
		this.config = new Configurations();
		this.database = new TextDatabase(config);
		this.learner = new GreedyLearner(database, config);
		this.gui = progressPrinter;
		learner.progressPrinter = progressPrinter;
		this.gui.setMediator(this);
	}
	
	
	public void loadFilesInGivenFolder(String folderLocation) {
		System.out.println("Loading");
		database.addAllTextsInFolderToDatabase(folderLocation, gui);
		
	}


	public void loadSubtitesFilesInGivenFolder(String folderLocation) {
		System.out.println("Loading subtitles");
		database.addAllSubtitlesInFolderToDatabase(folderLocation, gui);
		
	}

	public void startLearning() {
		var learningList = learner.learnAllLemmas();
		System.out.println("Done!");
	}
	
	public void initializeLearning() {
		learner.initializeForLearning();
		var alternatives = learner.getNBestScoringSentencesWithPutBack(numberOfAlternatives);
		gui.displayAlternatives(alternatives, learner.getLearnedLemmas(), config, database);
	}


	public void learnNextLemma() {
		learner.learnNextLemma();
		var alternatives = learner.getNBestScoringSentencesWithPutBack(numberOfAlternatives);
		gui.displayAlternatives(alternatives, learner.getLearnedLemmas(), config, database);
		//After learning the sentence we need to display the new learnable sentences.
	}
	
	public void learnLemmaInSentence(Sentence sentence) {
		learner.learnLemmaFromDirectlyLearnableSentence(sentence);
		var alternatives = learner.getNBestScoringSentencesWithPutBack(numberOfAlternatives);
		gui.displayAlternatives(alternatives, learner.getLearnedLemmas(), config, database);
	}
	
	public void saveProgress() {
		var learnedLemmasAndSentences = learner.getLearningList();
		File savedProgressFile = new File("saved_progress.txt");
		try {
			savedProgressFile.createNewFile();
			OutputStream os = new FileOutputStream(savedProgressFile);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
			for (SortablePair<Lemma, Sentence> lemmaSentencePair : learnedLemmasAndSentences) {
				var lemma = lemmaSentencePair.getFirst();
				var sentence = lemmaSentencePair.getSecond();
				writer.println(lemma.getRawLemma() + "|" + sentence.getRawSentence());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void loadProgress() {
		Path savedProgressFile = Paths.get("saved_progress.txt");
		try {
			String rawProgressFile = Files.readString(savedProgressFile, StandardCharsets.UTF_8);
			Text progresText = database.loadAndInitializeProgressFile(rawProgressFile, gui);
			List<Sentence> sentences = progresText.getParagraphs().stream().flatMap(paragraph -> paragraph.getSentences().stream())
																		   .collect(Collectors.toList());
			learner.initializeDataStructures();
			for (Sentence sentence : sentences) {
				learner.learnLemmasInSentence(sentence);
			}
			int j = 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
