package LemmaLearner;

import java.util.*;

import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		//Settings
		boolean shouldParseRealText = false;
		boolean shouldDisplayText = true;
		boolean shouldLoadSavedTexts = false;
		
		
		System.out.println("Start");
		
		CharStream testStream;
		TextDatabase textDatabase = new TextDatabase(shouldLoadSavedTexts, shouldDisplayText);
		GreedyLearner learner = new GreedyLearner(textDatabase);
		//memtest();
		//Thread.sleep(10000);
		
		if (shouldParseRealText) 
			textDatabase.addAllTextsInFolderToDatabase("Other texts");
		else {
			//File testFile = new File("Test texts/testTest.txt");
			//textDatabase.parseTextAndAddToDatabase(testFile);		
			//TestTool.parseString("The writing and artwork within are believed to be in the U.S. public domain, and Standard Ebooks releases this ebook edition under the terms in the CC0 1.0 Universal Public Domain Dedication", textDatabase);
			//TestTool.parseString("The Standard Ebooks CC0 1.0 Universal Public Domain Dedication", textDatabase);
			textDatabase.addAllTextsInFolderToDatabase("TextsToParse");		
		}
		learner.learnAllLemmas();
		
		/*
		String[] result = content.split("(\r\n|\\p{Punct})");
		*/
		System.out.println("End");
	}
	
    public static void memtest() {
        List<String> list = new ArrayList<String>();
        while (1<2){
            list.add("OutOfMemoryError soon");
        }

    }
}
