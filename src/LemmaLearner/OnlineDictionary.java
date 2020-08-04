package LemmaLearner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class OnlineDictionary {
	
	private final String DICTIONARY_WEBSITE = "https://en.bab.la/dictionary/english-danish/";
	private String startWordHTMLMarker = "' class=\"babQuickResult\">";
	private String endWordHTMLMarker = "</a>";
	
	public String getLemmaFromConjugation(String actualWord) throws IOException, InterruptedException {
		
		String response = getResponse(actualWord);
        
        int startDanishTranslationIndex = response.indexOf("\" in English</h2>");
        int startEnglishTranslationIndex = response.indexOf("\" in Danish</h2>");
        
        if (noEnglishDefinitionFound(startEnglishTranslationIndex) || noDefinitionFound(response))
        	return TextDatabase.NOT_A_WORD_STRING;
        else //We only want to look at the english translation.
        	response = response.substring(startEnglishTranslationIndex); 
        
        int startWordIndex = response.indexOf(startWordHTMLMarker) + startWordHTMLMarker.length();
        int endWordIndex = response.indexOf(endWordHTMLMarker, startWordIndex);
        String foundWord = response.substring(startWordIndex, endWordIndex);
        
        //Verbs like run will be on the form "to run". We only need the last part.
        foundWord = foundWord.split(" ")[foundWord.split(" ").length - 1];
        System.out.println("Word \"" + actualWord + "\" has lemma \"" + foundWord + "\".");
        
		return foundWord;
	}



	private String getResponse(String actualWord) throws IOException, InterruptedException, Error {
		File savedDictionaryData = new File("Websites/" + actualWord + ".txt");
		
		if (savedDictionaryData.exists()) {
			return Files.readString(savedDictionaryData.toPath());
		} else {
			String response = getDictionaryWebpage(actualWord);
			
	        //To avoid spaming the website.
	        Thread.sleep(2000);
			
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
