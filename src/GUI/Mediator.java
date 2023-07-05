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
import java.util.Set;
import java.util.stream.Collectors;

import Configurations.Configurations;
import LemmaLearner.*;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import TextDataStructures.Text;

public class Mediator {

	private Configurations config;
	private ProgressPrinter gui;
	private TextDatabase database;
	private GreedyLearner learner;
	
	private static final int numberOfAlternatives = 15;
	private final String PROGRESS_SAVE_FILE;
	public static final String PROGRESS_LEMMA_SENTENCE_SEPERATOR = ">-->";
	
	public Mediator(ProgressPrinter progressPrinter) {
		this.config = new Configurations();
		this.database = new TextDatabase(config);
		this.learner = new GreedyLearner(database, config);
		this.PROGRESS_SAVE_FILE = "saved_progress_" + config.getLanguage()  + ".txt";
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
		File savedProgressFile = new File(PROGRESS_SAVE_FILE);
		try {
			savedProgressFile.createNewFile();
			OutputStream os = new FileOutputStream(savedProgressFile);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
			for (SortablePair<Lemma, Sentence> lemmaSentencePair : learnedLemmasAndSentences) {
				var lemma = lemmaSentencePair.getFirst();
				var sentence = lemmaSentencePair.getSecond();
				writer.println(lemma.getRawLemma() + PROGRESS_LEMMA_SENTENCE_SEPERATOR + sentence.getRawSentence());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void loadProgress() {
		Path savedProgressFile = Paths.get(PROGRESS_SAVE_FILE);
		try {
			String rawProgressFile = Files.readString(savedProgressFile, StandardCharsets.UTF_8);
			Text progresText = database.loadAndInitializeProgressFile(rawProgressFile, gui);
			List<Sentence> sentences = progresText.getParagraphs().stream().flatMap(paragraph -> paragraph.getSentences().stream())
																		   .collect(Collectors.toList());
			learner.resetLearning();
			learner.initializeDataStructures();
			for (Sentence sentence : sentences) {
				learner.learnLemmasInSentence(sentence);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public List<SentenceDescription> getAlternernativeSentencesDescription() {
		
		var alternativeSentences = learner.getNBestScoringSentencesWithPutBack(10);
		var alternativeSentencesDescription = new ArrayList<SentenceDescription>();
		var learnedLemmas = learner.getLearnedLemmas();
		for (Sentence sentence : alternativeSentences) {
			alternativeSentencesDescription.add(new SentenceDescription(sentence, database, config, learnedLemmas));
		}
		return alternativeSentencesDescription;
	}


	public Conjugation getUnlearnedConjugation(Sentence sentence) {
		var words = sentence.getWordSet(database);
		Lemma unlearnedLemma = sentence.getUnlearnedLemmas(learner.getLearnedLemmas(), database).get(0);
		var unlearnedConjugation = words.stream().filter(word -> word.getLemma().equals(unlearnedLemma)).findFirst().get();
		return unlearnedConjugation;
	}


	public Set<String> getPotentialLemmatizations(String rawConjugation) {
		return database.getPotentialLemmatiations(rawConjugation);
	}


	public void changeLemmatization(String rawConjugation, String rawLemma) {
		Conjugation conjugation = database.allWords.get(rawConjugation);
		Lemma oldLemma = conjugation.getLemma();
		database.changeLemmatization(rawConjugation, rawLemma);
		Lemma newLemma = conjugation.getLemma();
		learner.updateLearningWithNewLemmatization(oldLemma, newLemma);
	}


	public String getSentenceLemmatization(Sentence selectedSentence) {
		return selectedSentence.getLemmatizedRawSentence(database);
	}


	public void alreadyKnowLemmaInSentence(Sentence sentence) {
		var unlearnedLemmas = sentence.getUnlearnedLemmas(learner.getLearnedLemmas(), database);
		var lemma = unlearnedLemmas.get(0);
		String rawFakeSentence = "";
		var rawConjugations = lemma.getConjugations().stream().map(conjugation -> conjugation.getRawConjugation()).collect(Collectors.toList());
		for (var conjugation : rawConjugations){
			rawFakeSentence += conjugation + " ";
		}
		rawFakeSentence = rawFakeSentence.trim() + ".";
		Sentence fakeSentence = new Sentence(rawFakeSentence, rawConjugations);
		learner.learnLemmasInSentence(fakeSentence);

	}

}


class SentenceDescription{
	Configurations config;
	TextDatabase database;
	Sentence sentence;
	Set<Lemma> learnedLemmas;

	public SentenceDescription(Sentence sentence, TextDatabase database, Configurations config, Set<Lemma> learnedLemmas){
		this.sentence = sentence;
		this.database = database;
		this.config = config;
		this.learnedLemmas = learnedLemmas;
	}

	public String getGUIDescription() {
		var learnedLemma = sentence.getUnlearnedLemmas(learnedLemmas, database).get(0);
		return learnedLemma.getRawLemma() + ", " + String.format("%.2f", sentence.getScore(database, config)) + ": " + sentence.getRawSentence()
			;//+ "<br> ---- " + sentence.getLemmatizedRawSentence(database) + "<br>";
	}
}
