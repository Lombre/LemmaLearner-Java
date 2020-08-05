package LemmaLearner;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.nustaq.serialization.FSTConfiguration;

import antlrGrammar.*;



public class TextDatabase{
	
	
	public static final String NOT_A_WORD_STRING = "NotAWord";

	//All texts are assumed to be unique, with no duplicates. Uses text.name.
	public HashMap<String, Text> allTexts = new HashMap<String, Text>(); 
	
	//All paragraphs, in relation to their texts, are assumed to be unique. Uses paragraph.paragraphID.
	public HashMap<String, Paragraph> allParagraphs = new HashMap<String, Paragraph>(); 
	
	//Sentences are not assumed to be unique. Uses sentence.rawSentence.
	public HashMap<String, Sentence> allSentences = new HashMap<String, Sentence>();
	
	//Words are not assumed to be unique. Uses word.rawWord.
	public HashMap<String, Word> allWords = new HashMap<String, Word>();
	
	final boolean shouldLoadSavedTexts;
	final boolean shouldPrintText;
	
	
	public TextDatabase(boolean shouldLoadSavedTexts, boolean shouldPrintText) {
		this.shouldLoadSavedTexts = shouldLoadSavedTexts;
		this.shouldPrintText = shouldPrintText;
	}
	
	public void addAllTextsInFolderToDatabase(String folderLocation) {
		File folder = new File(folderLocation);
		List<File> filesInFolder = Arrays.asList(folder.listFiles());
		List<File> textFilesInFolder = filesInFolder.stream().filter(file -> isTextFile(file)).collect(Collectors.toList());
		List<Text> parsedTexts = new ArrayList<Text>();
		
		//We want to measure the time taken to parse all the texts.
		long absoluteStartTime = System.currentTimeMillis();		
		long totalFileSpaceConsumption = textFilesInFolder.stream()
											  .map(file -> file.length())
											  .reduce(0L, (subtotal, element) -> subtotal + element);
		long accumulatedFileSpaceConsumption = 0L;		
		
		for (int i = 0; i < textFilesInFolder.size(); i++) {
			File subfile = textFilesInFolder.get(i);
			
			if (shouldPrintText)
				printProgressInAddingTextsToDatabase(textFilesInFolder, totalFileSpaceConsumption,
						accumulatedFileSpaceConsumption, i, subfile);
			accumulatedFileSpaceConsumption += subfile.length();
			
			parseTextAndAddToDatabase(subfile);
		}
		
		
		if (shouldPrintText)
			printAllTextsAddedToDatabaseInformation(absoluteStartTime);
		
		
	}

	private void printAllTextsAddedToDatabaseInformation(long absoluteStartTime) {
		long absoluteEndTime = System.currentTimeMillis();
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000/60; //In minutes		
		System.out.println("Parsed all texts in " + absoluteTimeUsed + " minutes.");				
		Lemmatizer lemmatizer = new Lemmatizer();
		List<String> allConjugations = allWords.values().stream().map(word -> word.getRawWord()).collect(Collectors.toList());
		HashSet<String> allLemmas = new HashSet<String>();
		
		for (int i = 0; i < allConjugations.size(); i++) {
			String conjugation = allConjugations.get(i);
			System.out.println("Looking at word " + i + " of " + allConjugations.size() + " \"" + conjugation + "\".");
			allLemmas.add(lemmatizer.getLemma(conjugation));
			System.out.println();
		}
		
		System.out.println("A total of " + allWords.size() + " unique conjugations and " + allLemmas.size() + " lemmas are found in all the texts combined.");		
		int k = 1;
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
		parsedText.save(getSavedTextFileName(subfile));
		parsedText.filterUnlearnableSentences();
		addTextToDatabase(parsedText);		
	}

	public void addTextToDatabase(Text parsedText) {
		if (parsedText != null) {
			//The could be made more simple, probably a function for each operation.
			//However, it needs to be done sequentially to avoid concurrency errors.
			parsedText.addToDatabase(this);
			
			List<Paragraph> parsedParagraphs = parsedText.getParagraphs().stream().collect(Collectors.toList());
			parsedParagraphs.forEach(paragraph -> paragraph.addToDatabase(this));
			
			List<Sentence> parsedSentences = parsedParagraphs.stream().flatMap(paragraph -> paragraph.getSentences().stream())
																	  .collect(Collectors.toList());
			parsedSentences.forEach(sentence -> sentence.addToDatabase(this));
			
			List<Word> parsedWords = parsedSentences.stream().flatMap(sentence -> sentence.getWordSet().stream())
					  										 .collect(Collectors.toList());
			parsedWords.forEach(word -> word.addToDatabase(this));
		}
	}

	private Text parseTextFile(File subfile) {
		File possibleSavedFile = new File(getSavedTextFileName(subfile));
		try {					
			//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.
			if (possibleSavedFile.exists() && shouldLoadSavedTexts) {
				return Text.load(possibleSavedFile.getAbsolutePath());													
			} 			
		} catch (Exception e) {
			System.out.println("The saved text: " + getSavedTextFileName(subfile) + " could not be loaded.");
			e.printStackTrace();
		}
		//If the loading failed, or if the text shouldn't be loaded, simply read it:

		try {
			Text parsedText = parse(subfile.getPath(), false);
			return parsedText;
		} catch (IOException e) {
			e.printStackTrace();
			//Signaling that the text could not be read or saved.
			return null;
		}				
	}

	private String getSavedTextFileName(File subfile) {
		String absolutePath = subfile.getAbsolutePath();
		String absolutePathWithoutExtension = absolutePath.substring(0, absolutePath.lastIndexOf('.'));
		return absolutePathWithoutExtension + ".saved";
	}

	private boolean isTextFile(File subfile) {
		return subfile.isFile() && subfile.getName().toLowerCase().endsWith((".txt"));
	}
		
	public Text parse(String textLocation,  boolean shouldDisplayGUITree) throws IOException {
		return parse(new File(textLocation).getName(), CharStreams.fromFileName(textLocation), shouldDisplayGUITree);
	}
	
	public Text parse(String textName, CharStream input, boolean shouldDisplayGUITree) {
		
		final Lexer lexer = new TextParsingGrammarLexer(input);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final TextParsingGrammarParser parser = new TextParsingGrammarParser(tokens);
		final TextParsingGrammarParser.StartContext startContext = parser.start();
		final ANTLRvisitor.StartVisitor visitor = new ANTLRvisitor.StartVisitor(textName);		

		if (shouldDisplayGUITree)	
			displayParserTree(parser, startContext);		
		
		final Text resultText = visitor.visit(startContext);
		return resultText;
	}


	private static void displayParserTree(final TextParsingGrammarParser parser,
			final TextParsingGrammarParser.StartContext startContext) {
		JFrame frame = new JFrame("Antlr AST");
		JPanel panel = new JPanel();
		frame.setLayout(new BorderLayout());
		frame.add(BorderLayout.CENTER, new JScrollPane(panel));
		frame.setLocationRelativeTo(null);
		
		//JScrollPane scrollPane = new JScrollPane();
		//scrollPane.setPreferredSize(new Dimension( 800,300));
		panel.setAutoscrolls(true);
		TreeViewer viewer = new TreeViewer(Arrays.asList(
				parser.getRuleNames()),startContext);
		viewer.setScale(1); // Scale a little
		viewer.setAutoscrolls(true);
		panel.add(viewer);			
		//scrollPane.add(viewer);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension( 800,300));	
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
}
