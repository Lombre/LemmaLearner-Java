package Lemmatization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Configurations.LemmatizationConfigurations;
import LemmaLearner.TextDatabase;
import TextDataStructures.Conjugation;

public class Lemmatizer {
	
	private HashMap<String, List<String>> conjugationToLemmas = new HashMap<String, List<String>>();
	//private OnlineDictionary onlineDictionary;
	private WiktionaryDictionary dictionary;
	private SimpleDictionaryLemmatizer simpleDictionaryLematizer;
	private final LemmatizationConfigurations config;
	
	
	public Lemmatizer(LemmatizationConfigurations config) {	
		this.config = config;
		//initializeStandardLemmatizer(lemmaFilePath);
		dictionary = new WiktionaryDictionary(this.config.getLanguage());
		dictionary.load();
		simpleDictionaryLematizer = new SimpleDictionaryLemmatizer(this.config.getLanguage());
	}
	
	public void save() {
		dictionary.save();
		simpleDictionaryLematizer.save();
	}



	public String getRawLemma(Conjugation conjugation) {
		return getRawLemma(conjugation.getRawConjugation());
	}
	
	public String getRawLemma(String rawConjugation) {
		if (simpleDictionaryLematizer.hasLemmaForConjugation(rawConjugation))
			return simpleDictionaryLematizer.getLemma(rawConjugation);

		String tempLemma = rawConjugation;
		if (conjugationToLemmas.containsKey(rawConjugation)) {
			tempLemma = conjugationToLemmas.get(tempLemma).get(0);
		}
		String actualLemma;
		try {
			actualLemma = dictionary.getLemmaFromConjugation(tempLemma);
		} catch (Exception e) {
			e.printStackTrace();
			actualLemma =  TextDatabase.NOT_A_WORD_STRING;
		}
		return actualLemma;
	}

	public boolean knowsConjugation(String rawConjugation) {
		boolean knows =  conjugationToLemmas.containsKey(rawConjugation) || dictionary.knowsConjugation(rawConjugation);
		if (!knows) 
			knows = false;
		knows =  conjugationToLemmas.containsKey(rawConjugation) || dictionary.knowsConjugation(rawConjugation);
		return conjugationToLemmas.containsKey(rawConjugation) || dictionary.knowsConjugation(rawConjugation);
	}

	public Set<String> getAllRawLemmas(String rawConjugation) {
		return dictionary.getAllLemmasFromConjugation(rawConjugation);
	}

	public void changeLemmatization(String rawConjugation, String rawLemma) {
		var rawLemmas = conjugationToLemmas.getOrDefault(rawConjugation, new ArrayList<String>());
		rawLemmas.add(0, rawLemma);
		simpleDictionaryLematizer.changeLemmatization(rawConjugation, rawLemma);
		simpleDictionaryLematizer.save();
	}

}
