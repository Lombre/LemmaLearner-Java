package LemmaLearner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import org.antlr.v4.*;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import Tests.TestTool;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		//Settings
		boolean shouldParseRealText = false;
		boolean shouldDisplayText = true;
		boolean shouldLoadSavedTexts = true;
		
		
		System.out.println("Start");
		
		CharStream testStream;
		TextDatabase textDatabase = new TextDatabase(shouldLoadSavedTexts, shouldDisplayText);
		GreedyLearner learner = new GreedyLearner(textDatabase);
		//memtest();
		
		if (shouldParseRealText)
			textDatabase.addAllTextsInFolderToDatabase("Other texts");
		else {
			//File testFile = new File("Test texts/testTest.txt");
			//textDatabase.parseTextAndAddToDatabase(testFile);		
			//TestTool.parseString("The writing and artwork within are believed to be in the U.S. public domain, and Standard Ebooks releases this ebook edition under the terms in the CC0 1.0 Universal Public Domain Dedication", textDatabase);
			//TestTool.parseString("The Standard Ebooks CC0 1.0 Universal Public Domain Dedication", textDatabase);
			textDatabase.addAllTextsInFolderToDatabase("TextsToParse");		
			learner.learnAllLemmas();
		}
		
		/*
		String content = Files.readString(Path.of("Wheel of Time, The - Robert Jordan & Brandon Sanderson.txt"));
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
