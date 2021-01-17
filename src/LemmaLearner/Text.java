package LemmaLearner;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;


public class Text implements Serializable{
	
	private final String name;
	private final String rawText; 
	private final Set<Paragraph> paragraphs;
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	
	public Text(String textName, String rawText, List<Paragraph> paragraphs) {
		this.name = textName;
		this.rawText = rawText;
		this.paragraphs = new ListSet<Paragraph>(paragraphs);
		this.paragraphs.forEach(paragraph->paragraph.setOriginText(this));
	}

	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public void save(String savedTextPath) {		
		/*
		StringBuilder stringBuilder = new StringBuilder();
		try {
			stringBuilder.append("|>>>");
			stringBuilder.append(name);
			stringBuilder.append("|||");
			stringBuilder.append(rawText);
			stringBuilder.append(System.lineSeparator()); //Maybe remove this.			
			stringBuilder.append("<<<|");
			for (Paragraph paragraph : paragraphs) {
				stringBuilder.append(paragraph.getRawParagraph());
				stringBuilder.append(System.lineSeparator());
				stringBuilder.append(System.lineSeparator());
				for (Sentence sentence : paragraph.getSentences()) {
					stringBuilder.append(sentence.getRawSentence());
					stringBuilder.append(System.lineSeparator());
					for (String word : sentence.getRawWordList()) {
						stringBuilder.append(word + " ");
					}
					stringBuilder.append(System.lineSeparator());
					stringBuilder.append(System.lineSeparator());
				}
				stringBuilder.append(System.lineSeparator());
			}
			PrintWriter out = new PrintWriter("testTest.txt");
			out.print(stringBuilder);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		int k = 1;
		*/
		
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(savedTextPath);
			FSTObjectOutput out = conf.getObjectOutput(fileOutputStream);
		    out.writeObject( this);
		    // DON'T out.close() when using factory method;
		    out.flush();
		    fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Saving file \"" + savedTextPath + "\" failed.");
		}
		
		
	}
	
	public static Text load(String savedTextPath) throws ClassNotFoundException, IOException {
		/*
		
		String content = Files.readString(Path.of("testTest.txt"));
		int indexStartName = content.indexOf("|>>>");
		int indexEndNameStartText = content.indexOf("|||");
		int indexEndTextStartParagraphs = content.indexOf("<<<|");
		
		String textName = content.substring(indexStartName+ "|>>>".length(), indexEndNameStartText);
		String rawText = content.substring(indexEndNameStartText + "|||".length(), indexEndTextStartParagraphs);
		String contentWithParagraph = content.substring(indexEndTextStartParagraphs + "<<<|".length());
		
		String wordSeperator = " ";
		String wordMarker = System.lineSeparator();
		String sentenceMarker = System.lineSeparator() + System.lineSeparator();
		String paragraphMarker = System.lineSeparator() + System.lineSeparator() + System.lineSeparator();
		
		int currentIndex = 0;
		int indexBeginNextParagraph = 0;
		while (indexBeginNextParagraph != -1 && contentWithParagraph.length() != 0) {
			indexBeginNextParagraph = contentWithParagraph.indexOf(paragraphMarker, currentIndex) + paragraphMarker.length();
			int indexEndCurrentParagraph = contentWithParagraph.indexOf(sentenceMarker, currentIndex);
			String rawParagraph = contentWithParagraph.substring(currentIndex, indexEndCurrentParagraph);
			
			currentIndex = indexEndCurrentParagraph + sentenceMarker.length();
			
			int indexNextSentence = currentIndex;
			int indexEndSentence = contentWithParagraph.indexOf(wordMarker, currentIndex);
			while (indexNextSentence < indexBeginNextParagraph - paragraphMarker.length() && indexEndSentence != -1) {		
				//String rawSentence = contentWithParagraph.substring(currentIndex, indexEndSentence);
				
				currentIndex = indexEndSentence + wordMarker.length();
				
				int indexEndWordSentence =  contentWithParagraph.indexOf(wordMarker, currentIndex);
				//String wordSentence = contentWithParagraph.substring(currentIndex, indexEndWordSentence);
				
				
				indexNextSentence = contentWithParagraph.indexOf(sentenceMarker, currentIndex) + sentenceMarker.length();
				indexEndSentence = contentWithParagraph.indexOf(wordMarker, indexNextSentence);
				
				currentIndex = indexNextSentence;
				//String fromCurrent = contentWithParagraph.substring(currentIndex);
				int k = 1;
			}
			
			currentIndex = indexBeginNextParagraph;
		}
		
		int j = 1;
		
		
		System.out.println("Meh");
		
		
		String[] paragraphSections = contentWithParagraph.split(System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		for (String paragraphSection : paragraphSections) {
			String sentenceSections[] = paragraphSection.split(System.lineSeparator() + System.lineSeparator());	
			for (String sentenceSection : sentenceSections) {
				int kage = 1;
			}
		}
		*/
		
		
		
	    FileInputStream fileInputStream = new FileInputStream(savedTextPath);
	    FSTObjectInput in = new FSTObjectInput(fileInputStream);
	    Text result = (Text) in.readObject();
	    in.close();
	    return result;
	    
		
	}


	public Set<Paragraph> getParagraphs() {
		return paragraphs;
	}


	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allTexts.containsKey(name)) {
			throw new Error("The text \"" + name + "\" is already in the database.");
		} else {
			textDatabase.allTexts.put(name, this);
		}
	}


	public String getRawText() {
		return rawText;
	}

	

	public void filterUnlearnableSentences() {
		filterSentencesBasedOnLength();
	}


	private void filterSentencesBasedOnLength() {
		int minSentenceLength = 4;
		int maxSentenceLength = 16;
		List<Paragraph> originalParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		for (Paragraph paragraph : originalParagraphs) {
			List<Sentence> originalParagraphSentences = new ArrayList<Sentence>(paragraph.getSentences());
			for (Sentence sentence : originalParagraphSentences) {
				
				if (sentence.getWordCount() < minSentenceLength){
					//It should not be necessary to remove the pointers from the sentence itself
					paragraph.getSentences().remove(sentence);
				} else if (maxSentenceLength < sentence.getWordCount()) {
					//If the sentence is to long, we can replace it with its subsentences, if it has any.
					if (sentence.getSubSentences().size() != 0) {
						List<Sentence> sentencesWithCorrectLength = sentence.getSubSentences().stream()
																						 	  .filter(subSentence -> minSentenceLength <= subSentence.getWordCount() && subSentence.getWordCount() <= maxSentenceLength)
																						 	  .collect(Collectors.toList());
						paragraph.getSentences().replaceWith(sentence, sentencesWithCorrectLength);
					}
					else {
						paragraph.getSentences().remove(sentence);						
					}
				}
			}
			if (paragraph.getSentences().size() == 0) {
				this.paragraphs.remove(paragraph);
			}
		}
	}


	public void combineAllParagraphs() {
		
		var combinedParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		paragraphs.clear();
		
		for (int i = 0; i < combinedParagraphs.size() - 1; i++) {
			var currentParagraph = combinedParagraphs.get(i);
			var nextParagraph = combinedParagraphs.get(i+1);
			
			var currentParagraphLastSentence = currentParagraph.getNthSentence(currentParagraph.getSentences().size()-1);
			var nextParapgrahFirstSentence = nextParagraph.getNthSentence(0);
			if (currentParagraphLastSentence.isUnended() && 
				nextParapgrahFirstSentence.startsWithLowerCase()) {
				
				Paragraph combinedParapgrah = combineParagraphs(currentParagraph, nextParagraph);
				paragraphs.add(combinedParapgrah);
				i++;
			} else {
				paragraphs.add(currentParagraph);
			}
		}
				
		// TODO Auto-generated method stub
		
	}


	private Paragraph combineParagraphs(Paragraph currentParagraph, Paragraph nextParagraph) {
		var combinedSentences = currentParagraph.getSentences();
		combinedSentences.addAll(nextParagraph.getSentences());
		Paragraph combinedParagraph = new Paragraph(currentParagraph.getRawParagraph() + " " + nextParagraph.getRawParagraph(), combinedSentences);
		// TODO Auto-generated method stub
		return combinedParagraph;
	}
	
}
