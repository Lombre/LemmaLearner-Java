package LemmaLearner;

import static org.junit.Assert.assertTrue;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.*;

import org.nustaq.serialization.FSTConfiguration;

import Tests.TestTool;


public class TextDatabase{
	
	
	public static final String NOT_A_WORD_STRING = "notaword";

	//All texts are assumed to be unique, with no duplicates. Uses text.name.
	public HashMap<String, Text> allTexts = new HashMap<String, Text>(); 
		
	public HashMap<String, Paragraph> allParagraphs = new HashMap<String, Paragraph>(); 
	
	//Sentences are not assumed to be unique. Uses sentence.rawSentence.
	public HashMap<String, Sentence> allSentences = new HashMap<String, Sentence>();
	
	//Words are not assumed to be unique. Uses word.rawWord as the key.
	//The word MUST be lowercase.
	public HashMap<String, Conjugation> allWords = new HashMap<String, Conjugation>();

	//The lemma MUST be lowercase.
	public HashMap<String, Lemma> allLemmas = new HashMap<String, Lemma>();
	
	private final DatabaseConfigurations config;
	
	
	
	public TextDatabase(DatabaseConfigurations config) {
		this.config = config;
	}
	
	public void addAllTextsInFolderToDatabase(String folderLocation) {
		
		List<File> textFilesInFolder = getTextFilesInFolder(folderLocation);
		
		//We want to measure the time taken to parse all the texts.
		long absoluteStartTime = System.currentTimeMillis();		
		long totalFileSpaceConsumption = textFilesInFolder.stream()
											  .map(file -> file.length())
											  .reduce(0L, (subtotal, element) -> subtotal + element);
		long accumulatedFileSpaceConsumption = 0L;		
		
		for (int i = 0; i < textFilesInFolder.size(); i++) {
			File subfile = textFilesInFolder.get(i);
			
			if (config.shouldPrintText())
				printProgressInAddingTextsToDatabase(textFilesInFolder, totalFileSpaceConsumption, accumulatedFileSpaceConsumption, i, subfile);
			accumulatedFileSpaceConsumption += subfile.length();
			
			parseTextAndAddToDatabase(subfile);
		}
		
		initializeLemmas();
		
		if (config.shouldPrintText())
			printAllTextsAddedToDatabaseInformation(absoluteStartTime);
		
	}

	private List<File> getTextFilesInFolder(String folderLocation) {
		File folder = new File(folderLocation);
		List<File> filesInFolder = Arrays.asList(folder.listFiles());
		List<File> textFilesInFolder = filesInFolder.stream().filter(file -> isTextFile(file)).collect(Collectors.toList());
		return textFilesInFolder;
	}

	public void initializeLemmas() {
		Lemmatizer lemmatizer = new Lemmatizer("English");
				
		//List<Conjugation> allConjugations = new ArrayList<Conjugation>(allWords.values());
				
		for (var conjugation : allWords.values()) {
			addConjugationToDatabase(lemmatizer, conjugation);
		}
		
		lemmatizer.save();	
		
		if (config.shouldPrintText()) {
			System.out.println("Total number of sentences: " + allSentences.size());
			System.out.println("Total number of words: " + allSentences.values().stream().map(x -> x.getLemmaSet(this).size()).reduce(0, (x, y) -> x + y));
			int numberOfConjugations = (allLemmas.containsKey(NOT_A_WORD_STRING))? (allWords.size() - allLemmas.get(NOT_A_WORD_STRING).getConjugations().size()): 0;
			System.out.println("A total of " + allWords.size() + " words, a total of " + numberOfConjugations + " unique conjugations and " + allLemmas.size() + " lemmas are found in all the texts combined.");		
		}					
		
	}
	
	private void addConjugationToDatabase(Lemmatizer lemmatizer, Conjugation currentConjugation) {
		String rawLemma = lemmatizer.getRawLemma(currentConjugation);
		Lemma currentLemma;
		if (allLemmas.containsKey(rawLemma))
			currentLemma = allLemmas.get(rawLemma);
		else {
			currentLemma = new Lemma(rawLemma);
			allLemmas.put(rawLemma, currentLemma);				
		}
		currentLemma.addConjugation(currentConjugation);
	}          
	

	private void printLemmatizationProgress(int numberOfConjugations, int i, Conjugation currentConjugation, String rawLemma) {
		if ((i % 100 == 0 || i < 1000) && config.shouldPrintText()) {
			System.out.println("Looking at word " + i + " of " + numberOfConjugations + " \"" + currentConjugation.getRawConjugation() + "\".");		
		    System.out.println("Word \"" + currentConjugation.getRawConjugation() + "\" has lemma \"" + rawLemma + "\".");
		    System.out.println();
		}
	}

	private void printAllTextsAddedToDatabaseInformation(long absoluteStartTime) {
		long absoluteEndTime = System.currentTimeMillis();
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000/60; //In minutes		
		System.out.println("Parsed all texts in " + absoluteTimeUsed + " minutes.");	

	}

	private void printProgressInAddingTextsToDatabase(List<File> textFilesInFolder, long totalFileSpaceConsumption,
			long accumulatedFileSpaceConsumption, int i, File subfile) {
		
		float percentSpaceAnalyzed = ((((float) accumulatedFileSpaceConsumption)/totalFileSpaceConsumption) * 100);
		System.out.println("Parsed " +  String.format("%.2f", percentSpaceAnalyzed) + " % of all text, in terms of space.");
		System.out.println("Analysing text " + (i+1) + " of " + textFilesInFolder.size() + ", " + subfile.getName());
	}

	@SuppressWarnings("IOException")
	public void parseTextAndAddToDatabase(File subfile) {
		Text parsedText = parseTextFile(subfile);		
		if (config.shouldSaveTexts())
			parsedText.save(getSavedTextFileName(subfile));
		//parsedText.combineAllParagraphs();
		parsedText.filterUnlearnableSentences();
		addTextToDatabase(parsedText);		
	}

	public void addTextToDatabase(Text parsedText) {
		if (parsedText != null) {
			
			//The could be made more simple, probably a function for each operation.
			//However, it needs to be done sequentially to avoid concurrency errors.
			
			parsedText.addToDatabase(this);
			
			var parsedParagraphs = parsedText.getParagraphs();
			parsedParagraphs.forEach(paragraph -> paragraph.addToDatabase(this));
			
			
			List<Sentence> parsedSentences = parsedParagraphs.stream().flatMap(paragraph -> paragraph.getSentences().stream())
																	  .collect(Collectors.toList());
			parsedSentences.forEach(sentence -> sentence.addToDatabase(this));
			
			
			List<Conjugation> parsedWords = parsedSentences.stream().flatMap(sentence -> sentence.getRawWordSet().stream().map(rawWord -> new Conjugation(sentence, rawWord)))
					  										 		.collect(Collectors.toList());
			parsedWords.forEach(word -> word.addToDatabase(this));
			
			
		}
	}
	
	public Text parseTextFile(String fileName) {	
		return parseTextFile(new File(fileName));
	}

	private Text parseTextFile(File subfile) {
		File possibleSavedFile = new File(getSavedTextFileName(subfile));
		try {					
			//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.
			if (possibleSavedFile.exists() && config.shouldLoadSavedTexts()) {
				return Text.load(possibleSavedFile.getAbsolutePath());													
			} 			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("The saved text: " + getSavedTextFileName(subfile) + " could not be loaded.");
			System.out.println("Will try to read the original file instead.");
		}
		//If the loading failed, or if the text shouldn't be loaded, simply read it:

		ManualParser parser = new ManualParser();
		Text parsedText = parser.parseFile(subfile);	
		return parsedText;				
	}

	private String getSavedTextFileName(File subfile) {
		String absolutePath = subfile.getAbsolutePath();
		String absolutePathWithoutExtension = absolutePath.substring(0, absolutePath.lastIndexOf('.'));
		return absolutePathWithoutExtension + ".saved";
	}

	private boolean isTextFile(File subfile) {
		return subfile.isFile() && subfile.getName().toLowerCase().endsWith((".txt"));
	}
		
	
	public Text parseRawText(String textName, String rawText) {
		final ManualParser parser = new ManualParser();
		return parser.parseRawText(textName, rawText);
	}

	public void resetLearning() {
		allLemmas.values().forEach(x -> x.resetLearning());
		allWords.values().forEach(x -> x.resetLearning());
	}

	
	
	
}
