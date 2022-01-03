package LemmaLearner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


import Configurations.Configurations;
import Configurations.DatabaseConfigurations;
import GUI.ProgressPrinter;
import Lemmatization.Lemmatizer;
import TextDataStructures.*;


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
	
	public void addAllTextsInFolderToDatabase(String folderLocation, ProgressPrinter progressPrinter) {
		
		List<File> textFilesInFolder = getTextFilesInFolder(folderLocation);
		
		
		if (config.shouldPrintText())
			progressPrinter.beginParsingTexts(textFilesInFolder.size(), folderLocation);
		
		
		ParsingProgressStruct progressStruct = new ParsingProgressStruct(textFilesInFolder);
		//We want to measure the time taken to parse all the texts.
		
		
		List<Text> parsedTexts = textFilesInFolder.stream()
						 			  			  .map(textFile -> parseTextFile(textFile, progressStruct, progressPrinter))
						 			  			  .collect(Collectors.toList());
		
		if (config.shouldPrintText()) 
			progressPrinter.beginAddTextsToDatabase(textFilesInFolder.size());
		
		parsedTexts.forEach(text -> {text.filterUnlearnableSentences(); 
									 addTextToDatabase(text, progressPrinter);});
			
		
		initializeLemmas();
		
		if (config.shouldPrintText()) {
			printAllTextsAddedToDatabaseInformation(progressStruct.absoluteStartTime);
			progressPrinter.printFinishedAddingTexts();
		}
		
	}
	

	public void addAllSubtitlesInFolderToDatabase(String folderLocation, ProgressPrinter gui) {
		File folder = new File(folderLocation);
		List<File> filesInFolder = Arrays.asList(folder.listFiles());
		List<File> subtitlesFilesInFolder = filesInFolder.stream().filter(file -> file.getName().toLowerCase().endsWith((".ass"))).collect(Collectors.toList());
		
		var englishSubtitles = new HashMap<String, File>();
		var spanishSubtitles = new HashMap<String, File>();
		
		for (File subtitleFile : subtitlesFilesInFolder) {
			String fileName = subtitleFile.getName();
			if (fileName.endsWith("-english.ass")) 
				englishSubtitles.put(fileName.substring(0, fileName.length() - "-english.ass".length()), subtitleFile);
			else if (fileName.endsWith("-spanish.ass")) 
				spanishSubtitles.put(fileName.substring(0, fileName.length() - "-spanish.ass".length()), subtitleFile);
		}
		
		Set<String> commonSubtitles = englishSubtitles.keySet().stream().collect(Collectors.toSet());
		commonSubtitles.retainAll(spanishSubtitles.keySet());
		for (String commonSubtitle : commonSubtitles) {
			TranslatedText text = parseTwinSubtitles(englishSubtitles.get(commonSubtitle), spanishSubtitles.get(commonSubtitle));
			addTextToDatabase(text, gui);
		}
		initializeLemmas();
		
	}


	private Text parseTextFile(File textFile, ParsingProgressStruct progressStruct, ProgressPrinter progressPrinter) {
		
		if (config.shouldPrintText())
			progressPrinter.printProgressInParsingTexts(textFile, progressStruct);
		progressStruct.accumulatedFileSpaceConsumption += textFile.length();
		Text text = parseTextFile(textFile);
		progressStruct.parsedTextCounter++;
		return text;
	}

	private List<File> getTextFilesInFolder(String folderLocation) {
		File folder = new File(folderLocation);
		List<File> filesInFolder = Arrays.asList(folder.listFiles());
		List<File> textFilesInFolder = filesInFolder.stream().filter(file -> isTextFile(file)).collect(Collectors.toList());
		return textFilesInFolder;
	}

	public void initializeLemmas() {
		Lemmatizer lemmatizer = new Lemmatizer((Configurations) config);
				
		//List<Conjugation> allConjugations = new ArrayList<Conjugation>(allWords.values());
				
		for (var conjugation : allWords.values()) {
			if (!conjugation.hasLemmaSet()) {
				addConjugationToDatabase(lemmatizer, conjugation);				
			}
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

	private void printAllTextsAddedToDatabaseInformation(long absoluteStartTime) {
		long absoluteEndTime = System.currentTimeMillis();
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000/60; //In minutes		
		System.out.println("Parsed all texts in " + absoluteTimeUsed + " minutes.");	

	}

	private void printProgressInAddingTextsToDatabase(File textFile, ParsingProgressStruct progressReporter) {
		
		float percentSpaceAnalyzed = ((((float) progressReporter.accumulatedFileSpaceConsumption)/progressReporter.totalFileSpaceConsumption) * 100);
		System.out.println("Parsed " +  String.format("%.2f", percentSpaceAnalyzed) + " % of all text, in terms of space.");
		System.out.println("Analysing text " + (progressReporter.parsedTextCounter+1) + ", " + textFile.getName());
	}

	@SuppressWarnings("IOException")
	public void parseTextAndAddToDatabase(File subfile) {
		Text parsedText = parseTextFile(subfile);		
		if (config.shouldSaveTexts())
			parsedText.save(getSavedTextFileName(subfile));
		//parsedText.combineAllParagraphs();
		parsedText.filterUnlearnableSentences();
		addTextToDatabase(parsedText, null);		
	}

	public void addTextToDatabase(Text parsedText, ProgressPrinter progressPrinter) {
		
		if (config.shouldPrintText())
			progressPrinter.printAddedTextToDatabase();
		
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

		ManualParser parser = new ManualParser((Configurations) config);
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
		final ManualParser parser = new ManualParser((Configurations) config);
		return parser.parseRawText(textName, rawText);
	}

	public void resetLearning() {
		allLemmas.values().forEach(x -> x.resetLearning());
		allWords.values().forEach(x -> x.resetLearning());
	}

	public Text loadAndInitializeProgressFile(String rawProgressFile, ProgressPrinter progressPrinter) {
		Text progressText = parseRawText("progress_file", rawProgressFile);
		progressText.filterUnlearnableSentences();
		addTextToDatabase(progressText, progressPrinter);
		initializeLemmas();
		return progressText;
	}

	public List<Triple<LocalTime, LocalTime, String>> parseSubtitles(String fileLocation) {
		List<String> rawSubtitleFileLines;
		try {
			rawSubtitleFileLines = Files.readAllLines(Paths.get(fileLocation), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
		List<String> subtitlesLines = rawSubtitleFileLines.stream().filter(line -> line.startsWith("Dialogue:")).collect(Collectors.toList());
		var subtitlesWithTimepoints = new ArrayList<Triple<LocalTime, LocalTime, String>>();
		for (String subtitleLine : subtitlesLines) {
			var splitSubtitleLine = subtitleLine.split(",");
			if (splitSubtitleLine.length < 9) 
				continue;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
			LocalTime startTime = LocalTime.from(formatter.parse("0"+ splitSubtitleLine[1]));
			LocalTime endTime = LocalTime.from(formatter.parse("0"+ splitSubtitleLine[2]));
			String text = Arrays.asList(splitSubtitleLine).subList(9, splitSubtitleLine.length)
														  .stream().reduce((a,b) -> a+","+b).get()
														  .replace("\\N", " ");

			subtitlesWithTimepoints.add(new Triple<LocalTime, LocalTime, String>(startTime, endTime, text));
		}
		
		return subtitlesWithTimepoints;
	}

	public TranslatedText parseTwinSubtitles(File englishFile, File spanishFile) {
		//95
		var englishSubtitles = parseSubtitles(englishFile.getAbsolutePath());
		var spanishSubtitles = parseSubtitles(spanishFile.getAbsolutePath());
		var spanishEnglishCorrespondance = new ArrayList<SortablePair<List<String>, List<String>>>();		
		int indexEnglish = 0;
		for (int i = 0; i < spanishSubtitles.size(); i++) {
			var currentSpanishSubtitle = spanishSubtitles.get(i);
			var rawSpanishSubtitle = currentSpanishSubtitle.getThird();
			var spanishStartTime = currentSpanishSubtitle.getFirst();
			var spanishEndTime = currentSpanishSubtitle.getSecond(); 
			
			List<String> correspondingEnglishSubtitles = new ArrayList<String>();
			for (; indexEnglish < englishSubtitles.size(); indexEnglish++) {
				var currentEnglishSubtitles = englishSubtitles.get(indexEnglish);
				var rawEnglishSubtitle = currentEnglishSubtitles.getThird();
				var englishStartTime = currentEnglishSubtitles.getFirst();
				var englishEndTime = currentEnglishSubtitles.getSecond();
				
				//No subtitles left behind!
				if (englishEndTime.isBefore(spanishStartTime)) {
					correspondingEnglishSubtitles.add(rawEnglishSubtitle);
					continue;
				}
				
				//Check for overlap in timeperiods:
				
				if (!(englishStartTime.isAfter(spanishEndTime) || spanishStartTime.isAfter(englishEndTime))) {
					correspondingEnglishSubtitles.add(rawEnglishSubtitle);
				} else {
					break;
				}			
				
			}
			if (correspondingEnglishSubtitles.isEmpty()) {
				if (!spanishEnglishCorrespondance.isEmpty())
					spanishEnglishCorrespondance.get(spanishEnglishCorrespondance.size() - 1).getFirst().add(rawSpanishSubtitle);
			} else {
				var entry = new SortablePair<List<String>, List<String>>(new ArrayList<String>() {{add(rawSpanishSubtitle);}}, correspondingEnglishSubtitles);
				spanishEnglishCorrespondance.add(entry);
			}			
		}
		
		List<String> rawEnglishParagraphs = spanishEnglishCorrespondance.stream()
															.map(englishSpanishPair -> englishSpanishPair.getSecond().stream().reduce((line1, line2) -> line1 + " " + line2).get())
															.collect(Collectors.toList());
		
		List<String> rawSpanishParagraphs = spanishEnglishCorrespondance.stream()
															.map(englishSpanishPair -> englishSpanishPair.getFirst().stream().reduce((line1, line2) -> line1 + " " + line2).get())
															.collect(Collectors.toList());
		
		String rawEnglishText = rawEnglishParagraphs.stream().reduce((line1, line2) -> line1 + "\n" + line2).get();
		String rawSpanishText = rawSpanishParagraphs.stream().reduce((line1, line2) -> line1 + "\n" + line2).get();
		
		
		ManualParser parser = new ManualParser((Configurations) config);
		TranslatedText parsedText = parser.parseTwinTextFile(spanishFile.getName(), rawEnglishText, rawSpanishText, rawEnglishParagraphs, rawSpanishParagraphs);	
		
		return parsedText;
		
	}

	
	
	
}
