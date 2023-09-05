package LemmaLearner;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import Configurations.Configurations;
import Configurations.DatabaseConfigurations;
import GUI.ProgressPrinter;
import Lemmatization.Lemmatizer;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Paragraph;
import TextDataStructures.Sentence;
import TextDataStructures.Text;
import TextDataStructures.TranslatedText;
import GUI.Mediator;


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

	private Lemmatizer lemmatizer;
			
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
		
		
		parsedTexts.forEach(text -> {filterText(text);
									 addTextToDatabase(text, progressPrinter);});
			
		
		initializeLemmas();

		filterOnRequiredCount(parsedTexts);


		if (config.shouldPrintText()) {
			printAllTextsAddedToDatabaseInformation(progressStruct.absoluteStartTime);
			progressPrinter.printFinishedAddingTexts();
		}

	}

    private void filterOnRequiredCount(List<Text> parsedTexts) {
        var lemmaToBookCount = new HashMap<Lemma, Integer>();
		for (Text text : parsedTexts) {
			Set<Lemma> lemmasInText = text.getAllLemmasInText(this);
			for (Lemma lemma : lemmasInText) {
				lemmaToBookCount.computeIfPresent(lemma, (key, val) -> val + 1);
				lemmaToBookCount.computeIfAbsent(lemma, (key) -> 1);
			}
		}

		System.out.println("Initial lemma count: " + lemmaToBookCount.size());

		double requiredFraction = 0.0;

		var filteredLemmas = new TreeSet<Lemma>();

		for (Lemma lemma : lemmaToBookCount.keySet()) {
			int lemmaCount = lemmaToBookCount.get(lemma);
			if (requiredFraction*parsedTexts.size() <= lemmaCount) {
				filteredLemmas.add(lemma);
			}
		}

		System.out.println("After filtering with lemmas required to bein in at least " + requiredFraction + " fractions of the texts: " + filteredLemmas.size());
    }

	public void filterText(Text text) {
		var criterias = Arrays.asList(filterSentencesOnNumberOfWords(),
									  filterSentencesOnNumberOfLetters());
		text.filterSentencesBasedOnCriteria(criterias);
	}

	private Function<Sentence, Boolean> filterSentencesOnNumberOfLetters() {
		return (Sentence s) -> s.hasCorrectNumberOfLetters(config.getMinSentenceLengthInLetters(), config.getMaxSentenceLengthInLetters());
	}

	private Function<Sentence, Boolean> filterSentencesOnNumberOfWords() {
		return (Sentence s) -> s.hasCorrectNumberOfWords(config.getMinSentenceLengthInWords(), config.getMaxSentenceLengthInWords());
	}

	private void printTestRemovalSummary() {
		var lemmasSorted = new ArrayList<Lemma>(allLemmas.values());
		Collections.sort(lemmasSorted, (Lemma lemma1, Lemma lemma2) -> {return lemma2.getFrequency() - lemma1.getFrequency();});
		
		for (int i = 0; i < lemmasSorted.size(); i++) {
			var lemma = lemmasSorted.get(i);
			if (i % 500 == 0) {
				System.out.println(i + ") "+ lemma + ": " + lemma.getFrequency());	
			}
		}
		
		var wordPairsCount = new HashMap<Pair<String, String>, Integer>();
		var wordCount = new HashMap<String, Integer>();
		for (var lemma: allLemmas.values()) {
			wordCount.put(lemma.getRawLemma(), 0);
		}
		
		int goodSentences = 0;
		for (Sentence sentence: allSentences.values()) {
			var lemmas = new ArrayList<Lemma>(sentence.getLemmaSet(this));
			boolean hasGoodPair = false;
			for (int i = 0; i < lemmas.size() - 1; i++) {
				for (int j = i + 1; j < lemmas.size(); j++) {
					var lemma1 = lemmas.get(i);
					var lemma2 = lemmas.get(j);
					var wordPair = new Pair<String, String>(lemma1.getRawLemma(), lemma2.getRawLemma());
					if (wordPairsCount.containsKey(wordPair)) {
						wordPairsCount.put(wordPair, wordPairsCount.get(wordPair) + 1);
					} else {
						wordPairsCount.put(wordPair, 1);						
					}
					
					if ((wordCount.get(lemma1.getRawLemma()) < 10 || wordCount.get(lemma2.getRawLemma()) < 5) || 
							(wordPairsCount.get(wordPair) <= 3 && lemma1.getFrequency() < 500 && lemma2.getFrequency() < 500)) {
						hasGoodPair = true;
					}
				}
			}
			if (hasGoodPair) {
				for (var lemma: sentence.getLemmaSet(this)) {
					wordCount.put(lemma.getRawLemma(), wordCount.get(lemma.getRawLemma()) + 1);
				}
				goodSentences++;
			}
		}
		
		System.out.println(wordPairsCount.size());
		System.out.println(goodSentences);
		System.out.println(allSentences.size());
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
		if (!folder.exists())
			throw new Error("Text folder " + folder.getAbsolutePath() + " does not exist.");
		List<File> filesInFolder = Arrays.asList(folder.listFiles());
		if (filesInFolder.size() == 0)
			throw new Error("Text folder " + folder.getAbsolutePath() + " is empty");
		List<File> textFilesInFolder = filesInFolder.stream().filter(file -> isTextFile(file)).collect(Collectors.toList());
		return textFilesInFolder;
	}

	public void initializeLemmas() {
		if (lemmatizer == null)
			lemmatizer = new Lemmatizer((Configurations) config);
				

		for (var conjugation : allWords.values()) {
			if (!conjugation.hasLemmaSet()) {
				String rawLemma = lemmatizer.getRawLemma(conjugation);
				addConjugationToDatabase(rawLemma, conjugation);
			}
		}

		for (var lemma : allLemmas.values()) {
			addDefinitionToLemma(lemma);
		}
		
		lemmatizer.save();	
		
		if (config.shouldPrintText()) {
			
			System.out.println("Total number of sentences: " + allSentences.size());
			System.out.println("Total number of words: " + allSentences.values().stream().map(x -> x.getLemmaSet(this).size()).reduce(0, (x, y) -> x + y));
			int numberOfConjugations = (allLemmas.containsKey(NOT_A_WORD_STRING))? (allWords.size() - allLemmas.get(NOT_A_WORD_STRING).getConjugations().size()): 0;
			System.out.println("A total of " + allWords.size() + " words, a total of " + numberOfConjugations + " unique conjugations and " + allLemmas.size() + " lemmas are found in all the texts combined.");		
		}					
		
	}
	
	private void addDefinitionToLemma(Lemma lemma) {
		var definitions = lemmatizer.getDefinitions(lemma);
		lemma.addDefinitions(definitions);
	}

	private void addConjugationToDatabase(String rawLemma, Conjugation currentConjugation) {
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
	public void parseTextAndAddToDatabase(File subfile, ProgressPrinter progressPrinter) {
		Text parsedText = parseTextFile(subfile);		
		if (config.shouldSaveTexts())
			parsedText.save(getSavedTextFileName(subfile));
		//parsedText.combineAllParagraphs();
		filterText(parsedText);
		addTextToDatabase(parsedText, progressPrinter);		
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

		var lines = Arrays.asList(rawProgressFile.split("\n"));
		var isolatedLines = lines.stream().map(sentence -> sentence.split(Mediator.PROGRESS_LEMMA_SENTENCE_SEPERATOR)[1]).collect(Collectors.toList());
		String isolatedSentences = isolatedLines.stream().reduce((x, y) -> x + "\n" + y).get();
		final String progess_file_name = "progress_file";
		if (allTexts.containsKey(progess_file_name)){
			removeTextFromDatabase(progess_file_name);
		}
 		Text progressText = parseRawText(progess_file_name, isolatedSentences);
		// No filtering is done

		addTextToDatabase(progressText, progressPrinter);
		initializeLemmas();
		return progressText;
	}

	private void removeTextFromDatabase(String textName) {
		Text text = allTexts.get(textName);
		allTexts.remove(textName);

		var textParagraphs = text.getParagraphs();
		textParagraphs.stream().forEach(parapgrah -> allParagraphs.remove(parapgrah.getParagraphID()));

		var textSentences = textParagraphs.stream().flatMap(parapgraph -> parapgraph.getSentences().stream());
		textSentences.forEach(sentence -> allSentences.remove(sentence.getRawSentence()));

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

	public Set<String> getPotentialLemmatiations(String rawConjugation) {
		return lemmatizer.getAllRawLemmas(rawConjugation);
	}

	public void changeLemmatization(String rawConjugation, String rawLemma) {
		lemmatizer.changeLemmatization(rawConjugation, rawLemma);

		Conjugation conjugation = allWords.get(rawConjugation);
		Lemma oldLemma = conjugation.getLemma();
		Lemma newLemma = allLemmas.getOrDefault(rawLemma, new Lemma(rawLemma));
		allLemmas.put(rawLemma, newLemma);

		oldLemma.getFrequency();

		conjugation.setNewLemma(newLemma);
		newLemma.addNewConjugation(conjugation);
		oldLemma.removeConjugation(conjugation);

		for (Sentence sentence : conjugation.getSentences()) {
			sentence.updateLemmaSet(this);
		}
	}

	
	
	
}
