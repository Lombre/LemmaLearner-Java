package LemmaLearner;

import java.util.*;

import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		
		//Settings
		boolean shouldParseRealText = true;
		boolean shouldDisplayText = true;
		boolean shouldLoadSavedTexts = true;
		
		TextDatabase textDatabase = new TextDatabase(shouldLoadSavedTexts, shouldDisplayText);
		GreedyLearner learner = new GreedyLearner(textDatabase);
		
		
		System.out.println("Start");
		
		
		if (shouldParseRealText) 
			textDatabase.addAllTextsInFolderToDatabase("Other texts");
		else 
			textDatabase.addAllTextsInFolderToDatabase("Texts");		
		
		
		learner.learnAllLemmas();		
		System.out.println("End");
		
	}
	
}
