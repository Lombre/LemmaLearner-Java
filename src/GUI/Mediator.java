package GUI;

import java.io.File;
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
		this.gui.initialize(config);
	}
	
	
	public void loadFilesInGivenFolder(String folderLocation) {
		System.out.println("Loading texts");
		database.addAllTextsInFolderToDatabase(folderLocation, gui);
		
	}

	public void loadFilesInLanguageSpecificFolder() {
		this.loadFilesInGivenFolder("Texts/" + config.getLanguage() + "/");
	}


	public void loadSubtitesFilesInGivenFolder(String folderLocation) {
		System.out.println("Loading subtitles");
		database.addAllSubtitlesInFolderToDatabase(folderLocation, gui);
		
	}

	public void startLearning() {
		learner.learnAllLemmas();
		System.out.println("Done!");
	}
	
	public void initializeLearning() {
		learner.initializeForLearning();
	}


	public void learnNextLemma() {
		learner.learnNextLemma();
	}
	
	public void learnLemmaInSentence(Sentence sentence) {
		learner.learnLemmaFromDirectlyLearnableSentence(sentence);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public Pair<ArrayList<Sentence>, ArrayList<String>> getAlternativeSentencesWithDescription() {
		
		var alternativeSentences = learner.getNBestScoringSentencesWithPutBack(10);
		var alternativeSentencesDescription = new ArrayList<String>();
		var learnedLemmas = learner.getLearnedLemmas();
		for (Sentence sentence : alternativeSentences) {
			alternativeSentencesDescription.add(sentence.getUnlearnedLemmas(learnedLemmas, database) + ", " + String.format("%.2f", sentence.getScore(database, config)) + " -> " + sentence.getLemmatizedRawSentence(database));
		}
		return new Pair<ArrayList<Sentence>, ArrayList<String>>(alternativeSentences, alternativeSentencesDescription);
	}


	
	
}
