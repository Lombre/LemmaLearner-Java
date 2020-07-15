package LemmaLearner;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class Text implements Serializable{
	
	private final String name;
	private final String rawText; 
	private final Set<Paragraph> paragraphs;
	
	public Text(String textName, String rawText, List<Paragraph> paragraphs) {
		this.name = textName;
		this.rawText = rawText;
		this.paragraphs = new LinkedHashSet<Paragraph>(paragraphs);
		this.paragraphs.forEach(paragraph->paragraph.setOriginText(this));
	}

	
	public List<Word> getAllWords(){
		List<Word> words = paragraphs.stream()
				  						.flatMap(paragraph -> paragraph.getSentences().stream())
										.flatMap(sentence -> sentence.getWordList().stream())
										.collect(Collectors.toList());
		Set<Word> nonDuplicatedWords = new HashSet<Word>(words);
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
	
	public void save(String savedTextPath) throws IOException {
	    FileOutputStream fileOutputStream
	      = new FileOutputStream(savedTextPath);
	    ObjectOutputStream objectOutputStream 
	      = new ObjectOutputStream(fileOutputStream);
	    objectOutputStream.writeObject(this);
	    objectOutputStream.flush();
	    objectOutputStream.close();		
	}
	
	public static Text load(String savedTextPath) throws ClassNotFoundException, IOException {

	    FileInputStream fileInputStream
	      = new FileInputStream(savedTextPath);
	    ObjectInputStream objectInputStream
	      = new ObjectInputStream(fileInputStream);
	    Text loadedText = (Text) objectInputStream.readObject();
	    objectInputStream.close(); 
	    return loadedText;		
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
	
}
