package LemmaLearner;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
//import org.json.*;

import Configurations.ParsingConfigurations;
import TextDataStructures.Paragraph;
import TextDataStructures.Sentence;
import TextDataStructures.Text;
import TextDataStructures.TranslatedParagraph;
import TextDataStructures.TranslatedText;


//https://kaikki.org/dictionary/
public class ManualParser {
	
	private ParsingConfigurations config;
	
	private final Map<Character, Character> openCharToCloseChar = new TreeMap<Character, Character>(){{
		put('\"', '\"'); 
		put('(',')');
		put('[', ']');
		put('“', '”');
		put('‘', '’');
		put('¿', '?');
		put('¡', '!');
	}};
	
	private final Set<Character> punctuationSet = new TreeSet<Character>(){{
		add('.'); add('?'); add('!');		
	}};

	private final Set<String> abbreviationSet;

	private final String newlines = "[\n\r]";
	
	public ManualParser(ParsingConfigurations configurations) {
		this.config = configurations;
		abbreviationSet = loadAbbreviations();
	}	
	


	private Set<String> loadAbbreviations() {
		List<String> rawAbbreviations;
		String filePath = "Abbreviations/" + config.getLanguage() + ".txt";
		try {
			rawAbbreviations = Files.readAllLines(Path.of(filePath));
		} catch (IOException e) {
			System.out.println("No relevant abbreviations file found at: " + filePath);
			return new TreeSet<String>();
		}
		
		Set<String> abbreviations = new TreeSet<String>();
		for (String abbreviation : rawAbbreviations) {
			if (abbreviation.charAt(abbreviation.length() - 1) != '.') {
				System.out.println("Abbreviations must end with a '.', see: " + abbreviation);
				continue;
			} else {
				//No matter what is written in the abbreviation file, 
				//we include different versions of the same abbreviation.
				abbreviations.add(abbreviation.toLowerCase());
				abbreviations.add(abbreviation.toUpperCase());
				abbreviations.add(abbreviation.toUpperCase().charAt(0) + abbreviation.substring(1).toLowerCase());
			}
		}
		
		return abbreviations;
	}

	
	public Text parseFile(final File file){
		String rawText;
		try {
			rawText = Files.readString(Path.of(file.getPath()),  StandardCharsets.UTF_8);
		} catch (IOException e) {			
			e.printStackTrace();
			throw new Error("Error when trying to parse Wikitionary file: " + file.toString());
		}	
		
		String textName = file.getName();
		return parseRawText(textName, rawText);
	}

	public Text parseRawText(final String textName, String rawText) {
		
		rawText = removeUnneccessaryChars(rawText);
		List<Paragraph> paragraphs = getParagraphsFromText(rawText, textName);		
		Text text = new Text(textName, rawText, paragraphs);
		//printOutput(paragraphs);
		return text;
	}

	private void printOutput(List<Paragraph> paragraphs) {
		System.out.println("No. paragraphs = " + paragraphs.size());
		int i = 0;
		for (var paragraph : paragraphs) {
			for (var sentence : paragraph.getSentences()) {
				System.out.println(i + " -> " + sentence);
			}
			System.out.println();
			i++;
		}
		
		TreeSet<String> allWords = new TreeSet<String>();
		paragraphs.forEach(paragraph -> 
						   paragraph.getSentences().forEach(sentence -> 
								   		sentence.getRawWordSet().forEach(word -> allWords.add(word.toLowerCase()))));
		System.out.println(allWords.size());
	}

	private List<Paragraph> getParagraphsFromText(String rawText, String textName) {
		List<String> rawParagraphs = getRawParagraphs(rawText);		
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		//List<List<List<String>>> paragraphs = new ArrayList<List<List<String>>>();
		int i = 0;
		for (String rawParagraph : rawParagraphs) {
			
			var paragraph = getParagraphFromRawParagraph(rawParagraph, textName + "_" + i);
			if (paragraph != null) {
				paragraphs.add(paragraph);				
				i++;
			}
		}
		return paragraphs;
	}

	private List<String> getRawParagraphs(String input) {
		List<String> rawParagraphs = new LinkedList<String>(Arrays.asList(input.split(newlines)));
		rawParagraphs.removeIf(paragraph -> paragraph.equals(""));
		return rawParagraphs;
	}

	private String removeUnneccessaryChars(String input) {
		input = input.replace("\t", "");
		input = input.replace("\\N", " ");
		return input;
	}
	
	private static int numberOfAbbreviations = 0;
	private Paragraph getParagraphFromRawParagraph(String rawParagraph, String textName) {
		
		int curPositionInSentence = 0;
		int curSentenceStartIndex = 0;
		if (rawParagraph.equals("�Transformaci�n!")) {//Transform!
			int j = 1;
		}
		
		
		List<Sentence> sentences = new ArrayList<Sentence>();		
		List<String> rawWords = new ArrayList<String>();
		List<Paragraph> subParagraphs = new ArrayList<Paragraph>();
				
		// Not the best code, it needs to be refractored, 
		// but I have unfortunately made it in such a way that it is not very easy
		
		// Parse the paragraph by walking over it from left to right, 
		// splitting it into sentences when they are found.
		for (int i = curPositionInSentence; i < rawParagraph.length(); i++) {
			
			char curChar = rawParagraph.charAt(i);
			
			if (isAtStartOfSubsentence(curChar)) {
				//Parse the subsentence. Like some in parenthesis.				
				
				i = extractSubSentences(rawParagraph, textName, curPositionInSentence, sentences, rawWords, subParagraphs, i);	
				if (i == -1) //It was unparsable
					return null;
				
				//The frame of reference now shifts to after the subsentence, as it has been parsed.		
				curPositionInSentence = i;
				if (isAtEndOfParagraph(rawParagraph, i)) {
					extractSentence(rawParagraph, curPositionInSentence, curSentenceStartIndex, sentences, rawWords, subParagraphs, i);					
				} 
				continue;
			} else if (isAtAbbreviation(i, rawParagraph)) {
				//Abbreviations should be ignored.
				numberOfAbbreviations++;
				//if (numberOfAbbreviations % 100 == 0)
				//	System.out.println("Nice! " + numberOfAbbreviations);
				continue;
			} else if (isAtEndOfParagraph(rawParagraph, i) || isAtPunctuation(rawParagraph, i, curChar)) {
				
				extractSentence(rawParagraph, curPositionInSentence, curSentenceStartIndex, sentences, rawWords, subParagraphs, i);
				
				//Clearing for new sentence.
				curPositionInSentence = i+1;
				curSentenceStartIndex = i+1;
				rawWords = new ArrayList<String>();
				subParagraphs = new ArrayList<Paragraph>();
				continue;
			}
		}
		Paragraph paragraph = new Paragraph(rawParagraph, sentences);
		paragraph.setParagraphID(textName);
		return paragraph;
	}



	private void extractSentence(String rawParagraph, int curPositionInSentence, int curSentenceStartIndex,
			List<Sentence> sentences, List<String> rawWords, List<Paragraph> subParagraphs, int i) {
		var rawEndOfSentence = rawParagraph.substring(curPositionInSentence, i+1);
		var endSentenceWords = getWordsInSentence(rawEndOfSentence);
		rawWords.addAll(endSentenceWords);
		String rawSentence = rawParagraph.substring(curSentenceStartIndex, i+1).trim();
		var currentSentence = new Sentence(rawSentence, rawWords, subParagraphs);
		sentences.add(currentSentence);
	}

	
	private boolean isAtAbbreviation(int index, String rawParagraph) {
		if (!isAtEndOfParagraph(rawParagraph, index) && isAtPunctuation(rawParagraph, index, rawParagraph.charAt(index))) {
			
			for (String abbreviation : abbreviationSet) {
				if (index - abbreviation.length() + 1 < 0) 
					continue;				
				else if (rawParagraph.startsWith(abbreviation, index - abbreviation.length() + 1) &&
						(index - abbreviation.length() < 0 || rawParagraph.charAt(index - abbreviation.length()) == ' '))
					return true;
			}
			
		}
		
		return false;
	}

	private int extractSubSentences(String rawParagraph, String textName, int curPositionInSentence,
			List<Sentence> sentences, List<String> rawWords, List<Paragraph> subParagraphs, int i) {
		int curEndSentenceIndex = getCorrespondingClosingCharPosition(rawParagraph, i);
		if (curEndSentenceIndex == -1)
			return -1;				
		
		//Add all the words before the subsentences
		var startRawSentence = rawParagraph.substring(curPositionInSentence, i);
		rawWords.addAll(getWordsInSentence(startRawSentence));
		var rawSubParagraph = rawParagraph.substring(i + 1, curEndSentenceIndex);
		
		//Now the actual subsentence.
		var subParagraph= getParagraphFromRawParagraph(rawSubParagraph, textName + "_" + sentences.size() + "_" + subParagraphs.size());
		if (subParagraph == null) //Something is unparsable in the subsentence.
			return -1;
		subParagraphs.add(subParagraph);
		for (var sentence: subParagraph.getSentences())
			rawWords.addAll(sentence.getRawWordList());
		return curEndSentenceIndex;
	}

	private int getCorrespondingClosingCharPosition(String rawParagraph, int indexOfOpeningChar) {
		
		var openingChar = rawParagraph.charAt(indexOfOpeningChar);
		var closingChar = openCharToCloseChar.get(openingChar);
			
		//If there are nested openings, for example ((())), we need to handle this.
		//This can be done by counting the openChars
		
		var currentPosition = indexOfOpeningChar;
		var openingCharCount = 1;
		
		while (0 < openingCharCount){
			int indexOfNextOpeningChar = rawParagraph.indexOf(openingChar, currentPosition+1);
			int indexOfNextClosingChar = rawParagraph.indexOf(closingChar, currentPosition+1);
			
			if (indexOfNextClosingChar == -1) 
				return -1;
			else if (indexOfNextOpeningChar < indexOfNextClosingChar && indexOfNextOpeningChar != -1) {
				openingCharCount++;
				currentPosition = indexOfNextOpeningChar;
			} else {
				openingCharCount--;
				currentPosition = indexOfNextClosingChar;
			}
		}		
		
		return currentPosition;
	}

	private boolean isAtStartOfSubsentence(char curChar) {
		return openCharToCloseChar.containsKey(curChar);
	}

	private static boolean isAtEndOfParagraph(String paragraph, int i) {
		return i + 1 == paragraph.length();
	}

	private boolean isAtPunctuation(String paragraph, int i, char curChar) {
		return punctuationSet.contains(curChar) && paragraph.charAt(i + 1) == ' ';
	}

	
	private static String regex = "(?U)[^\\p{Alpha}]+(\'[^\\\\p{Alpha}]+)?";
	private static Pattern pattern = Pattern.compile(regex);
	private static List<String> getWordsInSentence(String sentence) {
		var wordArray = pattern.split(sentence);
		var words = new ArrayList<String>(Arrays.asList(wordArray));
		words.removeIf(word -> word.equals(""));
		return words.stream().map(word -> word.toLowerCase()).collect(Collectors.toList());
	}

	

	private List<TranslatedParagraph> getParagraphsFromTranslatedText(String textName, List<String> rawUntranslatedParagraphs, List<String> rawTranslatedParagraphs) {	
		List<TranslatedParagraph> paragraphs = new ArrayList<TranslatedParagraph>();
		//List<List<List<String>>> paragraphs = new ArrayList<List<List<String>>>();
		
		for (int i = 0; i < rawUntranslatedParagraphs.size(); i++) {
			var rawUntranslatedParagraph = rawUntranslatedParagraphs.get(i);
			var rawTranslatedParagraph = rawTranslatedParagraphs.get(i);

			Paragraph untranslatedParagraph = getParagraphFromRawParagraph(rawUntranslatedParagraph, textName + "_" + i);
			Paragraph translatedParagraph = getParagraphFromRawParagraph(rawTranslatedParagraph, textName + "_" + i);
			if (untranslatedParagraph != null && translatedParagraph != null) {
				TranslatedParagraph combinedParagraph = new TranslatedParagraph(untranslatedParagraph, translatedParagraph);
				paragraphs.add(combinedParagraph);		
			}
		}
		return paragraphs;
	}


	public TranslatedText parseTwinTextFile(String textName, String rawEnglishText, String rawSpanishText, List<String> rawEnglishParagraphs, List<String> rawSpanishParagraphs) {
		
		List<TranslatedParagraph> combinedParagraphs = getParagraphsFromTranslatedText(textName, rawSpanishParagraphs, rawEnglishParagraphs);
		var combinedText = new TranslatedText(textName, rawSpanishText, rawEnglishText, combinedParagraphs);
		return combinedText;
	}

}

