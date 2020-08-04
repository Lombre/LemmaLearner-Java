package LemmaLearner;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;


public class Text implements Serializable, Comparable<Text>{
	
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

	
	public List<Word> getAllWords(){
		List<Word> words = paragraphs.stream()
				  						.flatMap(paragraph -> paragraph.getSentences().stream())
										.flatMap(sentence -> sentence.getWordList().stream())
										.collect(Collectors.toList());
		Set<Word> nonDuplicatedWords = new ListSet<Word>(words);
		List<Word> wordList = new ArrayList<Word>(nonDuplicatedWords);
		wordList.sort((a, b) -> a.getRawWord().compareTo(b.getRawWord()));
		return wordList;
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
		
		/*
		
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(savedTextPath);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(this);
			objectOutputStream.flush();
			objectOutputStream.close();		
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Saving file \"" + savedTextPath + "\" failed.");
		}
		*/
	}
	
	public static Text load(String savedTextPath) throws ClassNotFoundException, IOException {

	    FileInputStream fileInputStream = new FileInputStream(savedTextPath);
	    FSTObjectInput in = new FSTObjectInput(fileInputStream);
	    Text result = (Text) in.readObject();
	    in.close();
	    return result;
	    
		/*
	    FileInputStream fileInputStream = new FileInputStream(savedTextPath);
	    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	    Text loadedText = (Text) objectInputStream.readObject();
	    objectInputStream.close(); 
	    return loadedText;	
	    */	
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


	@Override
	public int compareTo(Text o) {
		return this.getName().compareTo(o.getName());
	}

	

	public void filterUnlearnableSentences() {
		int minSentenceLength = 4;
		int maxSentenceLength = 16;
		List<Paragraph> originalParagraphs = new ArrayList<Paragraph>(paragraphs);
		
		for (Paragraph paragraph : originalParagraphs) {
			List<Sentence> originalParagraphSentences = new ArrayList<Sentence>(paragraph.getSentences());
			for (Sentence sentence : originalParagraphSentences) {
				if (!(minSentenceLength <= sentence.getWordCount() && sentence.getWordCount() <= maxSentenceLength)) {
					//It should not be necessary to remove the pointers from the sentence itself
					paragraph.getSentences().remove(sentence);
				}
			}
			if (paragraph.getSentences().size() == 0) {
				this.paragraphs.remove(paragraph);
			}
		}
		
		// TODO Auto-generated method stub
		
	}
	
}
