package LemmaLearner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class OnlineDictionary {
	
	private final String DICTIONARY_WEBSITE = "https://en.bab.la/dictionary/english-spanish/";
	private String startWordHTMLMarker = "' class=\"babQuickResult\">";
	private String endWordHTMLMarker = "</a>";
	private HashMap<String, String> conjugationToLemma = new HashMap<String, String>();
	private final String SAVED_DICTIONARY_PATH = "OnlineDictionary.saved";
	private boolean shouldLoadSavedDictionary = true;
	
	public OnlineDictionary() {
		
	}


	public void load() {
		File possibleSavedFile = new File(SAVED_DICTIONARY_PATH);
		try {					
			//If the file have already been parsed and save, simply load that, as this is faster than parsing it again.
			if (possibleSavedFile.exists() && shouldLoadSavedDictionary) 
				conjugationToLemma = loadSavedDictionary();
			else 
				conjugationToLemma = new HashMap<String, String>();				
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Error();
			// TODO: handle exception
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
	    HashMap<String, String> result = ( HashMap<String, String>) in.readObject();
	    in.close();
	    return result;
	}


	public String getLemmaFromConjugation(String actualWord) throws IOException, InterruptedException {
		if (actualWord.equals("con") || actualWord.equals("aux") || actualWord.equals("in*")) {
			return actualWord;
		}
		
		if (conjugationToLemma.containsKey(actualWord)) {
			return conjugationToLemma.get(actualWord);
		}
		
		String response = getResponse(actualWord);
        
        int startSpanishTranslationIndex = response.indexOf("\" in English</h2>");
        int startEnglishTranslationIndex = response.indexOf("\" in Spanish</h2>");
        
        if (noEnglishDefinitionFound(startEnglishTranslationIndex) || noDefinitionFound(response)) {
        	conjugationToLemma.put(actualWord, TextDatabase.NOT_A_WORD_STRING);
        	return conjugationToLemma.get(actualWord);
        }
        else //We only want to look at the english translation.
        	response = response.substring(startEnglishTranslationIndex); 
        
        int startWordIndex = response.indexOf(startWordHTMLMarker) + startWordHTMLMarker.length();
        int endWordIndex = response.indexOf(endWordHTMLMarker, startWordIndex);
        String foundWord = response.substring(startWordIndex, endWordIndex).toLowerCase();
        
        //Verbs like run will be on the form "to run". We only need the last part.
        foundWord = foundWord.split(" ")[foundWord.split(" ").length - 1];       
        conjugationToLemma.put(actualWord, foundWord);
		return conjugationToLemma.get(actualWord);
	}



	private String getResponse(String actualWord) throws IOException, InterruptedException, Error {
		File savedDictionaryData = new File("Websites/" + actualWord + ".txt");
		
		if (savedDictionaryData.exists() && true) {
			return Files.readString(savedDictionaryData.toPath());
		} else {
			String response = getDictionaryWebpage(actualWord);
			
	        //To avoid spaming the website.
	        Thread.sleep(1000);
			
			try (PrintWriter out = new PrintWriter("Websites/" + actualWord + ".txt")) {
				out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Error("The file for the word " + actualWord + " could not be saved to disk.");
			}
			System.out.println("Saved file for word \"" + actualWord + "\"");
			return response;			
		}
		
	}



	private boolean noEnglishDefinitionFound(int startEnglishTranslationIndex) {
		return startEnglishTranslationIndex == -1;
	}



	private boolean noDefinitionFound(String response) {
		return response.contains("Our team was informed that the translation for \"");
	}
	
	

	private String getDictionaryWebpage(String actualWord) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DICTIONARY_WEBSITE + actualWord))
                .GET() // GET is default
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

}
