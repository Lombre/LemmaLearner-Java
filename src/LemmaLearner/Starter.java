package LemmaLearner;

import java.util.*;

//import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		
		//Settings
		boolean shouldParseRealText = true;
		
		Configurations config = new Configurations();
		
		TextDatabase textDatabase = new TextDatabase(config);
		GreedyLearner learner = new GreedyLearner(textDatabase, config);
				
		System.out.println("Start");
		
		if (shouldParseRealText) 
			textDatabase.addAllTextsInFolderToDatabase("Texts");
		else 
			textDatabase.addAllTextsInFolderToDatabase("TextsToParse");		
		
		
		List<Pair<Lemma, Sentence>> learningList = learner.learnAllLemmas();	
		
		System.out.println("End");
		
	}
	
}
