package LemmaLearner;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Paragraph implements Serializable {

	private Text originText;
	private String paragraphID;
	private final String rawParagraph;
	private final Set<Sentence> sentences;
	
	public Paragraph(String rawParagraph, Collection<Sentence> sentences) {
		this.rawParagraph = rawParagraph;
		this.sentences = new LinkedHashSet<Sentence>(sentences);
		this.sentences.forEach(sentence -> sentence.setInitialOriginParagraph(this));
	}

	public String getRawParagraph() {		
		return rawParagraph;
	}
	


	public Set<Sentence> getSentences() {
		return sentences;
	}

	public List<Word> getAllWords() {
		return sentences.stream()
						.flatMap(sentence -> sentence.getWordList().stream())
						.collect(Collectors.toList());
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
}
