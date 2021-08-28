package TextDataStructures;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import LemmaLearner.*;

public class Text implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1010024096455553264L;
	private final String name;
	private final String rawText; 
	private final Set<Paragraph> paragraphs;
	
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
		SerilizationHelper.save(this, savedTextPath);
	}
	
	public static Text load(String savedTextPath) {
	    return (Text) SerilizationHelper.load(savedTextPath);		
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
		filterSentencesBasedOnLength(4, 16);
	}


	private void filterSentencesBasedOnLength(int minSentenceLength, int maxSentenceLength) {
		
		//List<Paragraph> originalParagraphs = new ArrayList<Paragraph>(paragraphs);
		List<Paragraph> remainingParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		for (Paragraph paragraph : paragraphs) {
			
			List<Sentence> originalParagraphSentences = new ArrayList<Sentence>(paragraph.getSentences());
			//This will be the new sentences in the paragraph, when all the bad sentences have been removed.
			List<Sentence> newParagraphSentences = new ArrayList<Sentence>();
			
			
			for (Sentence sentence : originalParagraphSentences) {
				
				if (sentence.getWordCount() < minSentenceLength){
					//It should not be necessary to remove the pointers from the sentence itself
				} else if (maxSentenceLength < sentence.getWordCount()) {
					//If the sentence is to long, we can replace it with its subsentences, if it has any.
					
					for (Paragraph subParagraph : sentence.getSubParagraphs()) {
						
					}
				} else {
					newParagraphSentences.add(sentence);
				}
			}
			
			paragraph.setSentences(newParagraphSentences);
			
			if (0 < paragraph.getSentences().size()) {
				remainingParagraphs.add(paragraph);
			}
		}
		
		paragraphs.clear();
		paragraphs.addAll(remainingParagraphs);
		
	}


	public void combineAllParagraphs() {
		
		throw new Error("Removed function");
		/*
		var combinedParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		paragraphs.clear();
		
		for (int i = 0; i < combinedParagraphs.size() - 1; i++) {
			var currentParagraph = combinedParagraphs.get(i);
			var nextParagraph = combinedParagraphs.get(i+1);

			throw new Error();
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
		*/
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
