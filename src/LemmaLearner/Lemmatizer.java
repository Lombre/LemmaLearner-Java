package LemmaLearner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Lemmatizer {
	
	final Path lemmaFilePath = Paths.get("lemma.en.txt");
	HashMap<String, List<String>> conjugationToLemmas = new HashMap<String, List<String>>();
	OnlineDictionary onlineDictionary = new OnlineDictionary();
	
	
	public Lemmatizer() {
		
		try (Stream<String> lines = Files.lines(lemmaFilePath, Charset.defaultCharset())) {
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
		        String kageString = "kage";
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		int k = 1;
	}


	public String getRawLemma(Word conjugation) {
		return getRawLemma(conjugation.getRawWord());
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
