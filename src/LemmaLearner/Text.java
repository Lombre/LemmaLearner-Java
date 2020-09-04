package LemmaLearner;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;


public class Text implements Serializable{
	
	private final String name;
	private final String rawText; 
	private final Set<Paragraph> paragraphs;
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	
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

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(savedTextPath);
			FSTObjectOutput out = conf.getObjectOutput(fileOutputStream);
		    out.writeObject( this);
		    // DON'T out.close() when using factory method;
		    out.flush();
		    fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Saving file \"" + savedTextPath + "\" failed.");
		}
		
	}
	
	public static Text load(String savedTextPath) throws ClassNotFoundException, IOException {

	    FileInputStream fileInputStream = new FileInputStream(savedTextPath);
	    FSTObjectInput in = new FSTObjectInput(fileInputStream);
	    Text result = (Text) in.readObject();
	    in.close();
	    return result;
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
		filterSentencesBasedOnLength();
		
	}


	private void filterSentencesBasedOnLength() {
		int minSentenceLength = 4;
		int maxSentenceLength = 16;
		List<Paragraph> originalParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		for (Paragraph paragraph : originalParagraphs) {
			List<Sentence> originalParagraphSentences = new ArrayList<Sentence>(paragraph.getSentences());
			for (Sentence sentence : originalParagraphSentences) {
				
				if (sentence.getWordCount() < minSentenceLength){
					//It should not be necessary to remove the pointers from the sentence itself
					paragraph.getSentences().remove(sentence);
				} else if (maxSentenceLength < sentence.getWordCount()) {
					//If the sentence is to long, we can replace it with its subsentences, if it has any.
					if (sentence.getSubSentences().size() != 0) {
						List<Sentence> sentencesWithCorrectLength = sentence.getSubSentences().stream()
																						 	  .filter(subSentence -> minSentenceLength <= subSentence.getWordCount() && subSentence.getWordCount() <= maxSentenceLength)
																						 	  .collect(Collectors.toList());
						paragraph.getSentences().replaceWith(sentence, sentencesWithCorrectLength);
					}
					else {
						paragraph.getSentences().remove(sentence);						
					}
				}
			}
			if (paragraph.getSentences().size() == 0) {
				this.paragraphs.remove(paragraph);
			}
		}
	}
	
}
