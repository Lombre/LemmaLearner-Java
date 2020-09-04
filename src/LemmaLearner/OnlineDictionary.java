package LemmaLearner;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

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
	
	
	public OnlineDictionary(String language) {
		this.language = language;
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
	
	private HashMap<String, String> loadSavedDictionary() throws Exception {
		FileInputStream fileInputStream = new FileInputStream(SAVED_DICTIONARY_PATH);
	    FSTObjectInput in = new FSTObjectInput(fileInputStream);
	    HashMap<String, String> result = (HashMap<String, String>) in.readObject();
	    in.close();
	    return result;
	}


	public String getLemmaFromConjugation(String actualWord) throws IOException, InterruptedException {
		
		
		if (conjugationToLemma.containsKey(actualWord) && shouldLoadSavedDictionary) {
			return conjugationToLemma.get(actualWord);
		}
		
		if (actualWord.equals("in*"))
			return actualWord;
				
		String primaryLanguageWebpageContent = getPrimaryLanguageWebpageContent(actualWord);         
        List<Pair<String, String>> dictionaryWords = getWordsAndWordTypesFromWebsiteResponse(primaryLanguageWebpageContent);        
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


	private void removeWordsWithIllegealWordTypes(List<Pair<String, String>> dictionaryWords) {
		dictionaryWords.removeIf(pair -> pair.getSecond().equals("pl") || pair.getSecond().equals("pr.n.") || pair.getSecond().equals("m"));
	}


	private List<Pair<String, String>> getWordsAndWordTypesFromWebsiteResponse(String response) {
		List<Pair<String, String>> dictionaryWords = new ArrayList<Pair<String, String>>(); 
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
        		dictionaryWords.add(new Pair<String, String>(foundWord, foundWordType));
			}
		}
		return dictionaryWords;
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
			return Files.readString(savedDictionaryData.toPath());
		} else {
			String response = getDictionaryWebpage(actualWord);
			
	        //To avoid spaming the website.
			
			try (PrintWriter out = new PrintWriter(fileDestination)) {
				out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Error("The file for the word " + actualWord + " could not be saved to disk.");
			}
			System.out.println("Saved file for word \"" + actualWord + "\"");
			Thread.sleep(1000);
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

}
