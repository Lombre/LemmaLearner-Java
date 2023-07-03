package Lemmatization;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONString;

import LemmaLearner.SerilizationHelper;
import LemmaLearner.TextDatabase;
import TextDataStructures.Lemma;


//https://kaikki.org/dictionary/

//It should not be required to be serializable, but for some reason
//FST requires this.
public class WiktionaryDictionary implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6811924263820754119L;

	private Map<String, Set<String>> conjugationToLemma = new HashMap<String, Set<String>>();

	private Map<String, List<String>> lemmaToDefinition = new HashMap<String, List<String>>();
	
	private final String ALTERNATIVE_LEMMA_SAVE_FILE;

	private final String LANGUAGE;

	private final String SAVED_DICTIONARY_PATH;

	private final String DICTIONARY_FILE_LOCATION;

    private final String DICTIONARY_FOLDER = "dictionary-files/";

	private final String alternativeConjLemmaSeperator = "-->";

	private final String alternativeLemmaDefinitionSeperator = ">-->";

	private final String alternativeDefinitionDefinitionSeperator = "|->";

	private final String ALTERNATIVE_DICTIONARY_SAVE_FILE;

	public WiktionaryDictionary(String language) {
		this.LANGUAGE = language;
		this.SAVED_DICTIONARY_PATH = DICTIONARY_FOLDER + "/wiktionary_dictionary_" + this.LANGUAGE + ".saved";
		this.DICTIONARY_FILE_LOCATION = DICTIONARY_FOLDER + "/noninflected-words-" + this.LANGUAGE + ".json";
		this.ALTERNATIVE_LEMMA_SAVE_FILE = DICTIONARY_FOLDER + "lemmas_" + this.LANGUAGE + ".txt";
		this.ALTERNATIVE_DICTIONARY_SAVE_FILE = DICTIONARY_FOLDER + "definitions_" + this.LANGUAGE + ".txt";
	}

	public String getLemmaFromConjugation(String conjugation) {
		if (conjugationToLemma.containsKey(conjugation)) {
			var lemmas = conjugationToLemma.get(conjugation);

			//Sometimes there are more lemmas associated with one conjugation.
			//For example adverbs in english is sometimes categorized as a word by itself,
			//and as a conjugation of another word. Beautifully for example.
			boolean shouldOverGeneralizeLemmas = true;

			if (lemmas.contains(conjugation)) {
				if (lemmas.size() == 2 && shouldOverGeneralizeLemmas) {
					//Here we say that in the case that two lemmas are associated with a conjugation,
					//Take the one that does not match the conjugation. For "beautifully" it would be "beautiful".
					//This cuts down on the number of lemmas, but sometimes mistakes are made.
					//s is for example taken lemmatized to "?".
					var otherLemmas = new TreeSet<String>(lemmas);
					otherLemmas.remove(conjugation);
					return (String) otherLemmas.toArray()[0];
				} else
					return conjugation;
			} else
				return (String) lemmas.toArray()[0];
		} else
			return TextDatabase.NOT_A_WORD_STRING;
	}


	public boolean knowsConjugation(String rawConjugation) {
		return conjugationToLemma.containsKey(rawConjugation);
	}

	private void extractDictionaryFromWiktionaryJSONFile(String dictionaryFileLocation) {

		try {

			readAllStemFormAndAssociatedConjugations(dictionaryFileLocation, true);
			readAllStemFormAndAssociatedConjugations(dictionaryFileLocation, false);
			readAllDefinitions(dictionaryFileLocation);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Error in loading the dictionary.");
		}
	}


	private void readAllDefinitions(String dictionaryFileLocation) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(dictionaryFileLocation));

		while (in.ready()) {
			//The JSON file has one entry per line.
			String jsonString = in.readLine();
			addDefinitionToDictionary(jsonString);
		}

		in.close();
	}

	private void addDefinitionToDictionary(String jsonString) {

		JSONObject obj;
		try {
			obj = new JSONObject(jsonString);
		} catch (Exception e) {
			System.out.println("Errow when parsing the following JSON line: " + jsonString);
			return;
		}
		String word = obj.getString("word");
		var senses = obj.getJSONArray("senses");
		if (!senses.getJSONObject(0).has("glosses")) // No definition exists for the word.
			return;
		var jsonDefinitions = senses.getJSONObject(0).getJSONArray("glosses");
		ArrayList<String> definitions = new ArrayList<String>();
		for (int i = 0; i < jsonDefinitions.length(); i++) {
			definitions.add(jsonDefinitions.getString(i));
		}
		if (lemmaToDefinition.containsKey(word)) {
			lemmaToDefinition.get(word).addAll(definitions);
		} else
			lemmaToDefinition.put(word, definitions);
	}

	private void readAllStemFormAndAssociatedConjugations(String dictionaryFileLocation, boolean shouldOnlyReadStemForms) throws Exception {

		BufferedReader in = new BufferedReader(new FileReader(dictionaryFileLocation));

		while (in.ready()) {
			//The JSON file has one entry per line.
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
		JSONObject obj;
		try {
			obj = new JSONObject(jsonString);
		} catch (Exception e) {
			System.out.println("Errow when parsing the following JSON line: " + jsonString);
			return;
		}
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
			conjugationWord = ((JSONObject) conjugation).getString("form").toLowerCase();
			if (!conjugationWord.matches("\\p{L}+")) {
				continue;
			} else if (!conjugationToLemma.containsKey(conjugationWord)) {
				conjugationToLemma.put(conjugationWord, new TreeSet<String>() {{add(word);}});
			} else {
				var lemmas = conjugationToLemma.get(conjugationWord);
				lemmas.add(word);
			}
		}

	}



	private void addJSONRemainingFormsToDictionary(String jsonString) {

		JSONObject obj;
		try {
			obj = new JSONObject(jsonString);
		} catch (Exception e) {
			System.out.println("Errow when parsing the following JSON line: " + jsonString);
			return;
		}
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
			return;
		}
		else
			conjugationToLemma.put(word, new TreeSet<String>() {{add(word);}});

	}


	public void save() {
		//SerilizationHelper.save(conjugationToLemma, savedDictionaryPath);
		if (!new File(ALTERNATIVE_LEMMA_SAVE_FILE).exists()) {
			alternativeLemmaSave();
		}
		if (!new File(ALTERNATIVE_DICTIONARY_SAVE_FILE).exists()) {
			alternativeDictionarySave();
		}
	}

	private void alternativeDictionarySave() {
		File myFile = new File(ALTERNATIVE_DICTIONARY_SAVE_FILE);
		if (myFile.exists()) {
			myFile.delete();
		}
		try {
			myFile.createNewFile();
			FileWriter myWriter = new FileWriter(myFile.getAbsolutePath(), StandardCharsets.UTF_8);
			var lemmas = lemmaToDefinition.keySet();
			var lemmasSorted = new ArrayList<String>(lemmas);
			Collections.sort(lemmasSorted);
			for (String lemma : lemmasSorted) {
				String definitionString = lemma + " " + alternativeLemmaDefinitionSeperator + " ";
				var definitions = lemmaToDefinition.get(lemma).toArray();
				for(int i = 0; i < definitions.length - 1; i++) {
					var currentDefinition = definitions[i];
					definitionString += currentDefinition + alternativeDefinitionDefinitionSeperator;
				}
				definitionString += definitions[definitions.length - 1];
				definitionString = definitionString.replace("\n", "").replace("\r", "");
				myWriter.write(definitionString + "\n");
			}
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void alternativeLemmaSave() {
		File myFile = new File(ALTERNATIVE_LEMMA_SAVE_FILE);
		if (myFile.exists()) {
			myFile.delete();
		}
		try {
			myFile.createNewFile();
			FileWriter myWriter = new FileWriter(myFile.getAbsolutePath(), StandardCharsets.UTF_8);
			var conjugations = conjugationToLemma.keySet();
			var sortedConjugations = new ArrayList<String>(conjugations);
			Collections.sort(sortedConjugations);
			for (String conjugation : sortedConjugations) {
				String conjugationString = conjugation + " " + alternativeConjLemmaSeperator + " ";
				var lemmas = conjugationToLemma.get(conjugation).toArray();
				for(int i = 0; i < lemmas.length - 1; i++) {
					var currentLemma = lemmas[i];
					conjugationString += currentLemma + ";";
				}
				conjugationString += lemmas[lemmas.length - 1];
				myWriter.write(conjugationString + "\n");
			}
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}



	public void load() {
		boolean shouldLoadSavedDictionary = true;
		System.out.println("Start loading dictionary");
		//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.

		if (shouldLoadSavedDictionary && new File(ALTERNATIVE_LEMMA_SAVE_FILE).exists()) {
			try {
				conjugationToLemma.putAll(loadAlternativeSavedLemmas());
				lemmaToDefinition.putAll(loadAlternativeSavedDictionary());
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


	private Map<String, List<String>> loadAlternativeSavedDictionary() throws IOException {
		var dictionary = loadAlternativelySavedFile(ALTERNATIVE_DICTIONARY_SAVE_FILE, alternativeLemmaDefinitionSeperator, alternativeDefinitionDefinitionSeperator);
		return dictionary;
	}

	private Map<String, Set<String>> loadAlternativeSavedLemmas() throws IOException {

		var dictionary = loadAlternativelySavedFile(ALTERNATIVE_LEMMA_SAVE_FILE, alternativeConjLemmaSeperator, ";");
		Map<String, Set<String>> dictionaryConjugationToLemmas = new HashMap<String, Set<String>>();
		for (String conjugation : dictionary.keySet()){
			var lemmas = dictionary.get(conjugation);
			dictionaryConjugationToLemmas.put(conjugation, new TreeSet<String>(lemmas));
		}
		return dictionaryConjugationToLemmas;
	}


	private Map<String, List<String>> loadAlternativelySavedFile(String path, String keySeperator, String valueSeperator) throws IOException{

		Map<String, List<String>> dictionary = new HashMap<String, List<String>>();
		String rawDictionary = Files.readString(Path.of(path),  StandardCharsets.UTF_8);

		var dictionaryEntries = rawDictionary.split("\n");
		for (String dictionaryEntry : dictionaryEntries) {
			var splitEntry = dictionaryEntry.split(keySeperator);
			if (splitEntry.length != 2)
				throw new Error("Multiple keys on same line.");
			String key = splitEntry[0].trim().toLowerCase();
			var values = Arrays.asList(splitEntry[1].split(Pattern.quote(valueSeperator)));
			var trimmedValues = new ArrayList<String>();
			for (String value: values) {
				value = value.trim();
				trimmedValues.add(value);
			}
			dictionary.put(key, trimmedValues);
		}

		return dictionary;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Set<String>> loadSavedDictionary() throws Exception {
	    return (HashMap<String, Set<String>>) SerilizationHelper.load(SAVED_DICTIONARY_PATH);
	}

	public Set<String> getAllLemmasFromConjugation(String rawConjugation) {
		Set<String> potentialLemmas = conjugationToLemma.getOrDefault(rawConjugation, new TreeSet<String>(){{add(TextDatabase.NOT_A_WORD_STRING);}});
		return potentialLemmas;
	}

    public List<String> getDefinitions(Lemma lemma) {
        return lemmaToDefinition.getOrDefault(lemma.getRawLemma(), new ArrayList<String>());
    }

	
	
}
