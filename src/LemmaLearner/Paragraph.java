package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Paragraph implements Serializable, Comparable<Paragraph> {

	private Text originText;
	private String paragraphID;
	private final String rawParagraph;
	private final Set<Sentence> sentences;
	
	public Paragraph(String rawParagraph, Collection<Sentence> sentences) {
		this.rawParagraph = rawParagraph;
		this.sentences = new ListSet<Sentence>(sentences);
		this.sentences.forEach(sentence -> sentence.setInitialOriginParagraph(this));
	}

	public String getRawParagraph() {		
		return rawParagraph;
	}
	


	public Set<Sentence> getSentences() {
		return sentences;
	}

	public List<Word> getAllWords() {
		List<Word> wordList = new ArrayList<Word>();
		for (Sentence sentence : sentences) {
			List<Word> sentenceWords = sentence.getWordList();
			wordList.addAll(sentenceWords);
		}
		return wordList;
	}
	
	public void setParagraphID(String id) {
		paragraphID = id;
	}

	public String getParagraphID() {
		return paragraphID;
	}
	
	public void addToDatabase(TextDatabase textDatabase) {
		if (textDatabase.allParagraphs.containsKey(paragraphID)) {
			throw new Error("The paragraph with the ID " + paragraphID + " aldready exists in the database.");
		} else {
			textDatabase.allParagraphs.put(paragraphID, this);
		}
	}
	
	@Override
	public int hashCode() {
		return getRawParagraph().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Paragraph)) return false;
		else return ((Paragraph) obj).getParagraphID().equals(this.paragraphID);
	}


	@Override
	public String toString() {
		return getRawParagraph();
	}

	public Text getOriginText() {
		return originText;
	}

	public void setOriginText(Text text) {
		this.originText = text;
	}

	@Override
	public int compareTo(Paragraph o) {
		return this.getRawParagraph().compareTo(o.getRawParagraph());
	}
}
