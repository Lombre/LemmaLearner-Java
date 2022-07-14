package Lemmatization;

import java.util.HashMap;
import java.util.List;

import Configurations.LemmatizationConfigurations;
import LemmaLearner.TextDatabase;
import TextDataStructures.Conjugation;

public class Lemmatizer {
	
	private HashMap<String, List<String>> conjugationToLemmas = new HashMap<String, List<String>>();
	//private OnlineDictionary onlineDictionary;
	private WiktionaryDictionary dictionary;
	private final LemmatizationConfigurations config;
	
	
	public Lemmatizer(LemmatizationConfigurations config) {	
		this.config = config;
		//initializeStandardLemmatizer(lemmaFilePath);
		dictionary = new WiktionaryDictionary(this.config.getLanguage());
		dictionary.load();
	}
	
	public void save() {
		dictionary.save();
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

}
