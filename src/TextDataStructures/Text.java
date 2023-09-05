package TextDataStructures;
import java.io.*;
import java.util.*;
import java.util.function.Function;

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

	

	public void filterSentencesBasedOnCriteria(List<Function<Sentence, Boolean>> filterCriterias) {
		
		Set<Paragraph> filteredParagraphs = new ListSet<Paragraph>();
		
		for (Paragraph paragraph : paragraphs) {
			Paragraph revisedParagraph = paragraph.getParagraphWithSentencesFilteredOnCriteria(filterCriterias);
			if (0 < revisedParagraph.getSentences().size()) 
				filteredParagraphs.add(revisedParagraph);
		}		
		paragraphs = filteredParagraphs;
	}


	private Paragraph combineParagraphs(Paragraph currentParagraph, Paragraph nextParagraph) {
		var combinedSentences = currentParagraph.getSentences();
		combinedSentences.addAll(nextParagraph.getSentences());
		Paragraph combinedParagraph = new Paragraph(currentParagraph.getRawParagraph() + " " + nextParagraph.getRawParagraph(), combinedSentences);
		// TODO Auto-generated method stub
		return combinedParagraph;
	}

	public Set<Lemma> getAllLemmasInText(TextDatabase textDatabase) {
		var allLemmas = new HashSet<Lemma>();
		for (Paragraph paragraph : paragraphs) {
			allLemmas.addAll(paragraph.getAllLemmas(textDatabase));
		}
		return allLemmas;
	}
	
}
