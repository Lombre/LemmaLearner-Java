package TextDataStructures;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import LemmaLearner.*;

public class Text implements Serializable, ParagraphParent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1010024096455553264L;
	protected final String name;
	protected final String rawText; 
	protected Set<Paragraph> paragraphs;
		
	protected Text(String textName, String rawText) {
		this.name = textName;
		this.rawText = rawText;
	}
	
	public Text(String textName, String rawText, List<Paragraph> paragraphs) {
		this.name = textName;
		this.rawText = rawText;
		this.paragraphs = new ListSet<Paragraph>(paragraphs);
		setOriginText(paragraphs);
	}

	
	private void setOriginText(Collection<Paragraph> paragraphs) {
		for (Paragraph paragraph : paragraphs) {
			paragraph.setOriginText(this);
			for (var sentence : paragraph.getSentences()) {
				if (sentence.getSubParagraphs() != null && 0 < sentence.getSubParagraphs().size()) {
					setOriginText(sentence.getSubParagraphs());
				}
			}
		}
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
		
		
		int initalNumberOfSentences = paragraphs.stream()
												.map(paragraph -> paragraph.getSentences().size())
												.reduce((a, b) -> a + b).get();
		
		

		List<Paragraph> originalParagraphs = new ArrayList<Paragraph>(paragraphs);
		paragraphs.clear();
		
		for (Paragraph paragraph : originalParagraphs) {
			Paragraph revisedParagraph = paragraph.getParagraphWithSentencesFilteredOnLength(minSentenceLength, maxSentenceLength);
			if (0 < revisedParagraph.getSentences().size()) 
				paragraphs.add(revisedParagraph);
		}		

		int laterNumberOfSentences = paragraphs.stream()
												.map(paragraph -> paragraph.getSentences().size())
												.reduce((a, b) -> a + b).get();
		
		
		//System.out.println("Initial number of sentences: " + initalNumberOfSentences);
		//System.out.println("Later number of sentences: " + laterNumberOfSentences);
		
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
