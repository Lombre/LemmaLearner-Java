package TextDataStructures;

import java.util.List;

public class TranslatedSentence extends Sentence {

	
	final String rawTranslatedSentence;
	
	public TranslatedSentence(String rawSentence, String translatedSentence, List<String> rawWords) {
		super(rawSentence, rawWords);
		this.rawTranslatedSentence = translatedSentence;
		// TODO Auto-generated constructor stub
	}
		
	
	@Override
	public String toString() {
		return getRawSentence() + " | " + rawTranslatedSentence;
	}

}
