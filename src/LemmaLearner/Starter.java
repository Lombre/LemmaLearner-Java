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



public class Starter {
	
	public static void main(String[] args) throws Exception {
		//Settings
		boolean shouldParseRealText = true;
		boolean shouldDisplayText = true;
		boolean shouldLoadSavedTexts = true;
		
		
		System.out.println("Start");
		
		CharStream testStream;
		TextDatabase textDatabase = new TextDatabase(shouldLoadSavedTexts, shouldDisplayText);
		//memtest();

		if (shouldParseRealText)
			//testStream = CharStreams.fromFileName("Texts/Wheel of Time, The - Robert Jordan & Brandon Sanderson.txt");
			textDatabase.addAllTextsInFolderToDatabase("Other texts");
		else {
			File testFile = new File("Test texts/testTest.txt");
			textDatabase.parseTextAndAddToDatabase(testFile);				
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
