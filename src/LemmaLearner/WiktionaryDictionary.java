package LemmaLearner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONObject;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;


//https://kaikki.org/dictionary/
//It should not have to be serializable, but for some reason
//FST requires this.
public class WiktionaryDictionary implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6811924263820754119L;

	private TreeMap<String, Set<String>> conjugationToLemma = new TreeMap<String, Set<String>>();
	
	private final String LANGUAGE = "english";
	
	private final String SAVED_DICTIONARY_PATH = "wiktionary_dictionary_" + LANGUAGE + ".saved";

	private final String DICTIONARY_FILE_LOCATION = "wiktionary files/noninflected-words-" + LANGUAGE + ".json";
	
	public WiktionaryDictionary() {
	}
	
	public String getLemmaFromConjugation(String conjugation) {
		if (conjugationToLemma.containsKey(conjugation)) {
			var lemmas = conjugationToLemma.get(conjugation);
			
			if (lemmas.contains(conjugation)) {
				if (lemmas.size() == 2) {
					var otherLemmas = new TreeSet(lemmas);
					otherLemmas.remove(conjugation);
					return (String) otherLemmas.toArray()[0];					
				} else {
					return conjugation;
				}
			} else 
				return (String) lemmas.toArray()[0];
		} else return TextDatabase.NOT_A_WORD_STRING;
	}
	

	public boolean knowsConjugation(String rawConjugation) {
		return conjugationToLemma.containsKey(rawConjugation);
	}

	private void extractDictionaryFromWiktionaryJSONFile(String dictionaryFileLocation) {
		
		try {
			
			readAllStemFormAndAssociatedConjugations(dictionaryFileLocation, true);		
			readAllStemFormAndAssociatedConjugations(dictionaryFileLocation, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Error in loading the dictionary.");
		}
	}


	private void readAllStemFormAndAssociatedConjugations(String dictionaryFileLocation, boolean shouldOnlyReadStemForms) throws Exception {
		
		BufferedReader in = new BufferedReader(new FileReader(dictionaryFileLocation));
		
		int numberOfEntries = 0;
		
		while (in.ready()) {
			//The JSON file has one entry per line.
			numberOfEntries++;
			String jsonString = in.readLine();
			if (shouldOnlyReadStemForms)
				addJSONStemFormSegmentToDictionary(jsonString);		
			else 
				addJSONRemainingFormsToDictionary(jsonString);
		}		
		
		in.close();
	}

	private void addJSONStemFormSegmentToDictionary(String jsonString) {
		// This ignores entries corresponding to conjugations
		
		// The idea is that if a word is a conjugated form of a lemma, 
		// the lemma also has that word registered as a conjugation.
		
		// They can then be added there.
		
		//There will be some exceptions, but they are handled later
		
		JSONObject obj = new JSONObject(jsonString);
		String word = obj.getString("word"); //No .toLowerCase(). This removes lemmas like names.
		final String conjugationField = "forms";
		
		if (!word.matches("\\p{L}+"))
			return;
		
		if (!obj.has(conjugationField)  || word.chars().anyMatch(x -> Character.isUpperCase(x)))
			return;
	
		//The word must then be a stem/lemma form, as it has conjugations:
		
		if (!conjugationToLemma.containsKey(word)) {
			conjugationToLemma.put(word, new TreeSet<String>() {{add(word);}});
		} else {
			conjugationToLemma.get(word).add(word);
		}
		
		var conjugations = obj.getJSONArray(conjugationField);		
		
		for (var conjugation : conjugations) {
			String conjugationWord;
			try {
				conjugationWord = ((JSONObject) conjugation).getString("form").toLowerCase();
				
			} catch (Exception e) {
				throw new Error();
				// TODO: handle exception
			}
			if (!conjugationToLemma.containsKey(conjugationWord)) {
				conjugationToLemma.put(conjugationWord, new TreeSet<String>() {{add(word);}});
			} else {
				var lemmas = conjugationToLemma.get(conjugationWord);
				lemmas.add(word);
			}
		}
		
		
	
	}
	

	
	private void addJSONRemainingFormsToDictionary(String jsonString) {

		JSONObject obj = new JSONObject(jsonString);
		String word = obj.getString("word"); //No .toLowerCase(). This removings things like names.
		final String conjugationField = "forms";
		if (!word.matches("\\p{L}+"))
			return;
		
		//word.contains(" ") || word.contains("-") || 
		if (obj.has(conjugationField) || Character.isUpperCase(word.charAt(0)))
			return;
	
		//All the lemmas with conjugations have already been added.
		//So we only need to add this entry, if it does not exist in the dictionary.
		//The word must then be a stem/lemma form, as it has conjugations:
		
		if (conjugationToLemma.containsKey(word)) {
			//This can happen if there are two entries for a word.
			//Like if one is the word as a verb, and another as a noun.
			var lemmas = conjugationToLemma.get(word);
			return;
		}
		else 
			conjugationToLemma.put(word, new TreeSet<String>() {{add(word);}});
		
	}

	
	public void save() {
		SerilizationHelper.save(conjugationToLemma, SAVED_DICTIONARY_PATH);
	}
	
	

	public void load() {
		boolean shouldLoadSavedDictionary = true;
		System.out.println("Start loading dictionary");
		//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.
		if (Files.exists(Paths.get(SAVED_DICTIONARY_PATH)) && shouldLoadSavedDictionary) {
			try {					
				conjugationToLemma.putAll(loadSavedDictionary());		
			}
			catch (Exception e) {
				e.printStackTrace();
				extractDictionaryFromWiktionaryJSONFile(DICTIONARY_FILE_LOCATION);				
			}
			
		} else {
			extractDictionaryFromWiktionaryJSONFile(DICTIONARY_FILE_LOCATION);
		}
		System.out.println("Finished loading dictionary");
	}
	

	@SuppressWarnings("unchecked")
	private TreeMap<String, Set<String>> loadSavedDictionary() throws Exception {
	    return (TreeMap<String, Set<String>>) SerilizationHelper.load(SAVED_DICTIONARY_PATH);
	}

	
	
}
