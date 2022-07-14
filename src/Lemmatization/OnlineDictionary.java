package Lemmatization;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import LemmaLearner.*;



//Will need to refractor this class, as honestly, the code is quite messy :/

public class OnlineDictionary {
	
	private final String language;
	private final String dictionaryURL;
	private final HashMap<String, String> conjugationToLemma = new HashMap<String, String>();
	private final String SAVED_DICTIONARY_PATH = "OnlineDictionary.saved";
	private boolean shouldLoadSavedDictionary = true;
	private final String startSecondaryLanguage;
	private final String startPrimaryLanguage;
	
	private final String startWordHTMLMarker = "' class=\"babQuickResult\">";
	private final String endWordHTMLMarker = "</a>";
	
	private final String startWordTypeHTMLMarker = "<span class=\"suffix\">{";
	private final String endWordTypeHTMLMarker = "}";
	

	private final String startConjugationMarker;
	private final String endConjugationMarker = "\"";
	
	
	public OnlineDictionary(String language) {
		this.language = language;
		this.startConjugationMarker =  "/conjugation/" + language.toLowerCase() + "/";
		if (this.language.toLowerCase().equals("english")) {
			dictionaryURL =  "https://en.bab.la/dictionary/english-spanish/";
			startSecondaryLanguage = "\" in English</h2>";
			startPrimaryLanguage = "\" in Spanish</h2>";
		} 
		else {
			dictionaryURL = "https://en.bab.la/dictionary/english-"+ this.language.toLowerCase() + "/";
			startSecondaryLanguage = "\" in " + this.language + "</h2>";
			startPrimaryLanguage = "\" in English</h2>";
		}
	}


	public void load() {
		//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.
		if (Files.exists(Paths.get(SAVED_DICTIONARY_PATH)) && shouldLoadSavedDictionary) {
			
			try {					
				conjugationToLemma.putAll(loadSavedDictionary());		
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new Error();
			}
			
		}
	}
	
	
	public void save() {
		try {
			FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
			FileOutputStream fileOutputStream = new FileOutputStream(SAVED_DICTIONARY_PATH);
			FSTObjectOutput out = conf.getObjectOutput(fileOutputStream);
		    out.writeObject(conjugationToLemma);
		    // DON'T out.close() when using factory method;
		    out.flush();
		    fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Saving file \"" + SAVED_DICTIONARY_PATH + "\" failed.");
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> loadSavedDictionary() throws Exception {
		FileInputStream fileInputStream = new FileInputStream(SAVED_DICTIONARY_PATH);
	    FSTObjectInput in = new FSTObjectInput(fileInputStream);
	    HashMap<String, String> result = (HashMap<String, String>) in.readObject();
	    in.close();
	    return result;
	}


	public String getLemmaFromConjugation(String actualWord) throws IOException, InterruptedException {
		
		
		if (knowsConjugation(actualWord) && shouldLoadSavedDictionary) {
			return conjugationToLemma.get(actualWord);
		}
		
		if (actualWord.equals("in*"))
			return actualWord;

		
		String primaryLanguageWebpageContent = getPrimaryLanguageWebpageContent(actualWord);         
        List<SortablePair<String, String>> dictionaryWords = getWordsAndWordTypesFromWebsiteResponse(actualWord, primaryLanguageWebpageContent);        
        removeWordsWithIllegealWordTypes(dictionaryWords);
        
        if (dictionaryWords.size() == 0) {
        	conjugationToLemma.put(actualWord, TextDatabase.NOT_A_WORD_STRING);
		} else {
			//Verbs like run will be on the form "to run". We only need the last part.
			String foundWord = dictionaryWords.get(0).getFirst();
			foundWord = foundWord.split(" ")[foundWord.split(" ").length - 1];       
			conjugationToLemma.put(actualWord, foundWord);			
		}
        
		return conjugationToLemma.get(actualWord);
	}


	private void removeWordsWithIllegealWordTypes(List<SortablePair<String, String>> dictionaryWords) {
		//Remove things likes names, because we don't really care about learning those types of words
		dictionaryWords.removeIf(pair -> pair.getSecond().equals("pl") || pair.getSecond().equals("pr.n.") || pair.getSecond().equals("m"));
	}


	private List<SortablePair<String, String>> getWordsAndWordTypesFromWebsiteResponse(String actualWord, String response) throws IOException, InterruptedException, Error {
		List<SortablePair<String, String>> dictionaryWords = new ArrayList<SortablePair<String, String>>(); 
        while (response.indexOf(startWordHTMLMarker) != -1) {
        	int startWordIndex = response.indexOf(startWordHTMLMarker) + startWordHTMLMarker.length();
        	int endWordIndex = response.indexOf(endWordHTMLMarker, startWordIndex);
        	String foundWord = response.substring(startWordIndex, endWordIndex).toLowerCase();		

        	int startWordTypeIndex = response.indexOf(startWordTypeHTMLMarker, endWordIndex) + startWordTypeHTMLMarker.length();
        	int endWordTypeIndex = response.indexOf(endWordTypeHTMLMarker, startWordTypeIndex);
        	int divIndex = response.indexOf("</div>", endWordIndex);
        	if (divIndex < startWordTypeIndex || endWordTypeIndex == -1) {
        		response = response.substring(endWordIndex);
				continue;
			} else {
				String foundWordType = response.substring(startWordTypeIndex, endWordTypeIndex).toLowerCase();	
        		response = response.substring(endWordTypeIndex);        		
        		dictionaryWords.add(new SortablePair<String, String>(foundWord, foundWordType));
			}
		}
        
        
        String wordWebpage = getWebpage(actualWord);
        //System.out.println(wordWebpage);
        //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (isConjugation(wordWebpage)) {
        	var conjugationPair = new ArrayList<SortablePair<String,String>>(){};
    		int startConjugationIndex = wordWebpage.indexOf(startConjugationMarker) + startConjugationMarker.length();
    		int endConjugationIndex = wordWebpage.indexOf(endConjugationMarker, startConjugationIndex);
        	String conjugation = wordWebpage.substring(startConjugationIndex, endConjugationIndex).toLowerCase();
        	conjugationPair.add(new SortablePair<String, String>(conjugation, "vb"));
			return conjugationPair;
		}
		
		
		return dictionaryWords;
	}


	private boolean isConjugation(String response) {
		int startConjugationIndex = response.indexOf(startConjugationMarker);
		return startConjugationIndex != -1;
	}


	private String getPrimaryLanguageWebpageContent(String actualWord)
			throws IOException, InterruptedException, Error {
		String response = getWebpage(actualWord);
        
        int startSecondaryLanguageTranslationIndex = response.indexOf(startSecondaryLanguage);
        int startPrimaryLanguageTranslationIndex = response.indexOf(startPrimaryLanguage);
        
        if (noPrimaryLanguageDefinitionFound(startPrimaryLanguageTranslationIndex) || noDefinitionFound(response)) {
        	conjugationToLemma.put(actualWord, TextDatabase.NOT_A_WORD_STRING);
        	response = "";
        }
        else if (startSecondaryLanguageTranslationIndex != -1) //We only want to look at the english translation.
        	response = response.substring(startPrimaryLanguageTranslationIndex, startSecondaryLanguageTranslationIndex); 
        else
			response = response.substring(startPrimaryLanguageTranslationIndex);
		return response;
	}



	private String getWebpage(String actualWord) throws IOException, InterruptedException, Error {
		String fileDestination = "Websites/" + language.toLowerCase() + "_" + actualWord + ".txt";
		File savedDictionaryData = new File(fileDestination);
		
		if (savedDictionaryData.exists() && true) {
			return Files.readString(savedDictionaryData.toPath(), StandardCharsets.UTF_8);
		} else {
			String response = getDictionaryWebpage(actualWord);
			
			
			try (PrintWriter out = new PrintWriter(fileDestination)) {
				out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("The file for the word " + actualWord + " could not be saved to disk.");
				//throw new Error("The file for the word " + actualWord + " could not be saved to disk.");
			}
			System.out.println("Saved file for word \"" + actualWord + "\"");
			//To avoid spaming the website.
			int sleepTime = 133 + new Random().nextInt(357);
			Thread.sleep(sleepTime);
			//System.out.println(sleepTime);
			return response;			
		}
		
	}



	private boolean noPrimaryLanguageDefinitionFound(int startEnglishTranslationIndex) {
		return startEnglishTranslationIndex == -1;
	}



	private boolean noDefinitionFound(String response) {
		return response.contains("Our team was informed that the translation for \"");
	}
	
	

	private String getDictionaryWebpage(String actualWord) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(dictionaryURL + actualWord))
                .GET() // GET is default
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
		return response.body();
	}


	public boolean knowsConjugation(String actualWord) {
		return conjugationToLemma.containsKey(actualWord);
	}

}
