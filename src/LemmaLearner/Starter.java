package LemmaLearner;

import java.util.*;

import Configurations.Configurations;
import GUI.*;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

//import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		
		/*
		  
		//Settings
		boolean shouldParseRealText = false;
				
		Configurations config = new Configurations();
		
		TextDatabase textDatabase = new TextDatabase(config);
		GreedyLearner learner = new GreedyLearner(textDatabase, config);
				
		System.out.println("Start");
		
		if (shouldParseRealText) 
			textDatabase.addAllTextsInFolderToDatabase("Texts/" + config.getLanguage());
		else 
			textDatabase.addAllTextsInFolderToDatabase("Texts/TestTexts/" + config.getLanguage());		
				
		List<SortablePair<Lemma, Sentence>> learningList = learner.learnAllLemmas();	
		
		
		System.out.println("End");
		*/
		
		
		ConsoleGUI gui;
		if (true) 
			gui = new ConsoleGUI();

		gui = new ConsoleGUI();
		Mediator mediator = new Mediator(gui);
		
	}
	
	
}
