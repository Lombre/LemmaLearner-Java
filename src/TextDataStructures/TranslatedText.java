package TextDataStructures;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import LemmaLearner.ListSet;

public class TranslatedText extends Text{

	final String rawTranslatedText;
	
	public TranslatedText(Text englishText, Text spanishText) {
		super(spanishText.getName(), spanishText.getRawText());
		this.rawTranslatedText = englishText.getRawText();			
		List<TranslatedParagraph> combinedParagraphs = new ArrayList<TranslatedParagraph>();
		var englishParagraphs = new ArrayList<Paragraph>(englishText.getParagraphs());
		var spanishParagraphs = new ArrayList<Paragraph>(spanishText.getParagraphs());
		if (englishParagraphs.size() != spanishParagraphs.size())
			throw new Error("Error: Mismatch between number of paragraphs in translated and untranslated text.");
		for (int i = 0; i < spanishParagraphs.size(); i++) {
			var combinedParagraph = new TranslatedParagraph(spanishParagraphs.get(i), englishParagraphs.get(i));
			combinedParagraphs.add(combinedParagraph);
		}
		//For some reason I need to do all this casting :/
		paragraphs = (Set<Paragraph>) ((Object) new ListSet<TranslatedParagraph>(combinedParagraphs));
	}

	public TranslatedText(String textName, String rawSpanishText, String rawEnglishText, List<TranslatedParagraph> combinedParagraphs) {
		super(textName, rawSpanishText);
		this.rawTranslatedText = rawEnglishText;		
		paragraphs = (Set<Paragraph>) ((Object) new ListSet<TranslatedParagraph>(combinedParagraphs));
	}
	
	
}
