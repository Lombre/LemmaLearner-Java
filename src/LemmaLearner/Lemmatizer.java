package LemmaLearner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Lemmatizer {
	
	private final String language;
	private final Path lemmaFilePath;
	private HashMap<String, List<String>> conjugationToLemmas = new HashMap<String, List<String>>();
	private OnlineDictionary onlineDictionary;
	
	
	public Lemmatizer(String language) {	
		this.language = language;
		lemmaFilePath = Paths.get(this.language.toLowerCase() + "_lemma.txt");
		//initializeStandardLemmatizer(lemmaFilePath);
		onlineDictionary = new OnlineDictionary(this.language);
		onlineDictionary.load();
	}
	
	void save() {
		onlineDictionary.save();
	}

	private void initializeStandardLemmatizer(Path path) {
		if (Files.exists(path)) {
			try (Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
				for (String line : (Iterable<String>) lines::iterator)
				{
					String[] splitLine = line.split("->");
					String lemma = splitLine[0].split("/")[0].strip();
					String[] conjugations = splitLine[1].strip().split(",");
					for (String conjugation : conjugations) {
						if (conjugationToLemmas.containsKey(conjugation)) {
							List<String> lemmaList = conjugationToLemmas.get(conjugation);
							lemmaList.add(lemma);
						}
						else {
							List<String> lemmaList = new ArrayList() {{add(lemma);}};
							conjugationToLemmas.put(conjugation.strip(), lemmaList);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}


	public String getRawLemma(Conjugation conjugation) {
		return getRawLemma(conjugation.getRawConjugation());
	}
	
	public String getRawLemma(String rawConjugation) {
		//First use the normal lemmatization, and then perform the online check.
		String tempLemma = rawConjugation;
		if (conjugationToLemmas.containsKey(rawConjugation)) {
			tempLemma = conjugationToLemmas.get(tempLemma).get(0);
		}
		String actualLemma;
		try {
			actualLemma = onlineDictionary.getLemmaFromConjugation(tempLemma);
		} catch (Exception e) {
			e.printStackTrace();
			actualLemma =  TextDatabase.NOT_A_WORD_STRING;
		}
		return actualLemma;
	}

}
