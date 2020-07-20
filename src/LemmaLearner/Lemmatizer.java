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

}
