package LemmaLearner;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Word implements Serializable {

	private final Set<Sentence> sentences = new HashSet<Sentence>();
	private final String rawWord;
	
	public Word(Sentence originSentence, String rawWord) {
		this(rawWord);
		sentences.add(originSentence);
	}
	
	public Word(String rawWord) {
		this.rawWord = rawWord.toLowerCase();
	}
	
	public String getRawWord() {
		return rawWord;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Word))
			return false;
		else return ((Word) obj).getRawWord().equals(this.getRawWord());
	}
	
	@Override
	public int hashCode() {
		return rawWord.hashCode();
	}

	@Override
	public String toString() {
		return getRawWord();
	}

	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allWords.containsKey(this.getRawWord())) {
			//The sentences have already been added, 
			//so the word in the database simply also needs to refer to those sentences.
			textDatabase.allWords.get(this.getRawWord()).sentences.addAll(sentences);			
		} else {
			textDatabase.allWords.put(getRawWord(), this);
		}
	}

	public Set<Sentence> getSentences() {
		return sentences;
	}
}
