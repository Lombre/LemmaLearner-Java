package LemmaLearner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import org.json.*;


//https://kaikki.org/dictionary/
public class ManualParser {
	
	private final static TreeMap<Character, Character> openCharToCloseChar = new TreeMap<Character, Character>(){{
		put('\"', '\"'); 
		put('(',')');
		put('[', ']');
		put('“', '”');
		put('‘', '’');
	}};
	
	private final static TreeSet<Character> punctuationSet = new TreeSet<Character>(){{
		add('.'); add('?'); add('!');		
	}};

	private final String newlines = "[\n\r]";
	
	
	public Text parseFile(final File file){
		String fileName = Path.of(file.getPath()).toString();
		String rawText;
		try {
			rawText = Files.readString(Path.of(file.getPath()),  StandardCharsets.UTF_8);
		} catch (IOException e) {			
			e.printStackTrace();
			return null;
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
		return input;
	}
	
	private Paragraph getParagraphFromRawParagraph(String rawParagraph, String textName) {
		
		int curPositionInSentence = 0;
		int curSentenceStartIndex = 0;
		
		
		
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
				//Parse the subsentence.
				
				
				int curEndSentenceIndex = getCorrespondingClosingCharPosition(rawParagraph, i);
				if (curEndSentenceIndex == -1) {
					//System.out.println("---------> " + rawParagraph);
					return null;
				}
				
				
				//Add all the words before the subsentences
				var startRawSentence = rawParagraph.substring(curPositionInSentence, i);
				rawWords.addAll(getWordsInSentence(startRawSentence));
				var rawSubParagraph = rawParagraph.substring(i + 1, curEndSentenceIndex);
				var subParagraph= getParagraphFromRawParagraph(rawSubParagraph, textName + "_" + sentences.size() + "_" + subParagraphs.size());
				if (subParagraph == null) 
					return null;
				subParagraphs.add(subParagraph);
				
				for (var sentence: subParagraph.getSentences())
					rawWords.addAll(sentence.getRawWordList());				
				i = curEndSentenceIndex;
				
				curPositionInSentence = i + 1;				
			} 
			
			
			if (isAtEndOfSentence(rawParagraph, i) || isAtPunctuation(rawParagraph, i, curChar)) {
				
				var rawEndOfSentence = rawParagraph.substring(curPositionInSentence, i+1);
				var endSentenceWords = getWordsInSentence(rawEndOfSentence);
				rawWords.addAll(endSentenceWords);
				String rawSentence = rawParagraph.substring(curSentenceStartIndex, i+1).trim();
				var currentSentence = new Sentence(rawSentence, rawWords, subParagraphs);
				sentences.add(currentSentence);
				
				//Clearing for new sentence.
				curPositionInSentence = i+1;
				curSentenceStartIndex = i+1;
				rawWords = new ArrayList<String>();
				subParagraphs = new ArrayList<Paragraph>();
			}
		}
		Paragraph paragraph = new Paragraph(rawParagraph, sentences);
		paragraph.setParagraphID(textName);
		return paragraph;
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

	private static boolean isAtStartOfSubsentence(char curChar) {
		return openCharToCloseChar.containsKey(curChar);
	}

	private static boolean isAtEndOfSentence(String paragraph, int i) {
		return i + 1 == paragraph.length();
	}

	private static boolean isAtPunctuation(String paragraph, int i, char curChar) {
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

}

