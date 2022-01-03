package TextDataStructures;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TranslatedParagraph extends Paragraph{

	private final String rawTranslatedParagraph;
	public TranslatedParagraph(Paragraph spanishParagraph, Paragraph englishParagraph) {
		super(spanishParagraph.getRawParagraph(), (Collection<Sentence>) (Object) getTranslatedSentences(spanishParagraph, englishParagraph), spanishParagraph.getParagraphID());
		rawTranslatedParagraph = englishParagraph.getRawParagraph();
	}

	private static Collection<TranslatedSentence> getTranslatedSentences(Paragraph spanishParagraph, Paragraph englishParagraph) {
		
		var spanishSentences = new ArrayList<Sentence>(spanishParagraph.getSentences());
		var englishSentence = new ArrayList<Sentence>(englishParagraph.getSentences()).get(0);
		
		var translatedSentences = new ArrayList<TranslatedSentence>();
		
		
		
		
		
		for (int i = 0; i < spanishSentences.size(); i++) {
			var currentSpanishSentence = spanishSentences.get(i);
			var translatedSentence = new TranslatedSentence(currentSpanishSentence.getRawSentence(), englishSentence.getRawSentence(), currentSpanishSentence.getRawWordList());
			translatedSentences.add(translatedSentence);
		}
		
		return translatedSentences;
	}
	
	

	@Override
	public String toString() {
		return getRawParagraph() + " | " + rawTranslatedParagraph;
	}
	
	

}
