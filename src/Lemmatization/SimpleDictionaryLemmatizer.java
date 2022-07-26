package Lemmatization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SimpleDictionaryLemmatizer {

    private String language;
    private final String KEY_VALUE_DELIMITER = "->";
    private final Map<String, String> conjugationToLemmaMap;
    final String dict_path;

	public SimpleDictionaryLemmatizer(String language){
        this.language = language;
        dict_path = "private_lemma_dict_" + language + ".txt";
        this.conjugationToLemmaMap = loadConfigs(dict_path);
    }



	private Map<String, String> loadConfigs(String fileLocation) {
		String rawDictText = loadRawDict(fileLocation);

		List<String> dictLines = getTextLines(rawDictText);

		final var conjugationToLemmaMap = new TreeMap<String, String>();

		for (String dictLine : dictLines) {
			addDictLinesToMap(conjugationToLemmaMap, dictLine);
		}

		return conjugationToLemmaMap;
	}

    public void save(){
        save(dict_path);
    }

	private void save(String fileLocation) {
		String rawConfigurationToWrite = "";
		for (String key : conjugationToLemmaMap.keySet()) {
			String configurationValuevalue = conjugationToLemmaMap.get(key);
			rawConfigurationToWrite += key + " " + KEY_VALUE_DELIMITER + " " + configurationValuevalue + "\n\n";
		}
		try {
			Files.writeString(Path.of(dict_path), rawConfigurationToWrite);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private String loadRawDict(String fileLocation) {

		String rawDict;
		try {
            if (Files.exists(Path.of(fileLocation))){
                rawDict = Files.readString(Path.of(fileLocation),  StandardCharsets.UTF_8);
            } else
                rawDict = "";
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Error when trying to load the dict file. It should be located at: " + Path.of(fileLocation).toAbsolutePath().toString());
		}
        return rawDict;
	}


	private void addDictLinesToMap(final Map<String, String> conjugationToLemmaMap, String dictLine) throws Error {
		String[] dictEntryPair = dictLine.split(KEY_VALUE_DELIMITER);
		if (dictEntryPair.length != 2)
			throw new Error("Exactly one equal sign is required for each line in the dict file. See the line: " + dictLine);
		String rawKey = dictEntryPair[0].trim();
		String rawValue = dictEntryPair[1].trim();
		if (conjugationToLemmaMap.containsKey(rawKey))
			throw new Error("They dict file is not allowed to contain the same key twice. Second occurence in line: " + dictLine);
		conjugationToLemmaMap.put(rawKey, rawValue);
	}



	private List<String> getTextLines(String rawText) {
		List<String> textLines = new ArrayList<String>(Arrays.asList(rawText.split("\n")));

		final char commentLineDenoter = '#';
		textLines = textLines.stream().map(line -> line.strip()).collect(Collectors.toList());
		//All empty lines and comments should be removed.
		textLines.removeIf(line -> line.length() == 0 || line.charAt(0) == commentLineDenoter);
		return textLines;
	}



	public void changeLemmatization(String rawConjugation, String rawLemma) {
        conjugationToLemmaMap.put(rawConjugation, rawLemma);
	}



    public boolean hasLemmaForConjugation(String rawConjugation) {
        return conjugationToLemmaMap.containsKey(rawConjugation);
    }



    public String getLemma(String rawConjugation) {
        return conjugationToLemmaMap.get(rawConjugation);
    }


}
