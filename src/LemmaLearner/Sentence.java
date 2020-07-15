package LemmaLearner;
import java.io.Serializable;
import java.util.*;

public class Sentence implements Serializable {
	
	private Set<Paragraph> originParagraphs;
	private final String rawSentence;
	private final short[] wordBeginningIndex;
	private final short[] wordLengthIndex;
	
	public Sentence(String rawSentence, List<Word> words) {
		this.rawSentence = rawSentence;
		if (Short.MAX_VALUE <= rawSentence.length()) throw new Error("A sentence that is to long has been parsed. Maximum allowed length is " + Short.MAX_VALUE + ". The sentence is: " + rawSentence);
		wordBeginningIndex = new short[words.size()];
		wordLengthIndex = new short[words.size()];
		String lowerCaseRawSentence = rawSentence.toLowerCase();
		for (int i = 0; i < words.size(); i++) {
			Word currentWord = words.get(i);
			int indexBeginning = lowerCaseRawSentence.toLowerCase().indexOf(currentWord.getRawWord());
			wordBeginningIndex[i] = (short) indexBeginning;
			wordLengthIndex[i] = (short) (currentWord.getRawWord().length());
		}
	}
	
	public String getRawSentence() {
		return rawSentence;
	}

	public List<Word> getWordList() {
		ArrayList<Word> words = new ArrayList<Word>();
		for (int i = 0; i < wordBeginningIndex.length; i++) {
			String rawWord = rawSentence.substring(wordBeginningIndex[i], wordBeginningIndex[i] + wordLengthIndex[i]).toLowerCase();
			words.add(new Word(this, rawWord));
		}
		return words;
	}	
	

	/**
	 * OBS. Note that the words are not synchronized with the database. For this, use getSynchronizedWordSet(textDatabase)
	 * @return
	 */
	public Set<Word> getWordSet() {
		return new HashSet<>(getWordList());
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Sentence)) return false;
		else return ((Sentence) obj).getRawSentence().equals(this.getRawSentence());
	}
	
	@Override
	public int hashCode() {		
		return rawSentence.hashCode();
	}

	@Override
	public String toString() {
		return getRawSentence();
	}

	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allSentences.containsKey(getRawSentence())) {
			Sentence sentenceInDatabase = textDatabase.allSentences.get(this.getRawSentence());
			for (Paragraph paragraph : originParagraphs) {
				sentenceInDatabase.originParagraphs.add(paragraph);
				paragraph.getSentences().remove(this);
				paragraph.getSentences().add(sentenceInDatabase);
			}
			//The words do not need to be updated, as since a sentence S on the same form is already in the database,
			//the words of S must already be in the database, and point to S.
		} else {
			textDatabase.allSentences.put(getRawSentence(), this);
		}
	}

	public void setInitialOriginParagraph(Paragraph paragraph) {
		originParagraphs = new HashSet<Paragraph>();
		originParagraphs.add(paragraph);
	}
}
