package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.junit.*;

import LemmaLearner.*;
import antlrGrammar.TextParsingGrammarParser;
import antlrGrammar.TextParsingGrammarParser.StartContext;



public class TestTool {
		
	public static final String testTextName = "test";

	public static Text parseString(String stringToParse, TextDatabase database) {
		return parseString(stringToParse, database, false);
	}
	
	public static Text parseString(String stringToParse, TextDatabase database, boolean shouldDisplayGUI) {
		return database.parse(testTextName, CharStreams.fromString(stringToParse), shouldDisplayGUI);
	}
	
	
	public static Text parseStringAndAddToDatabase(String stringToParse, TextDatabase database, boolean shouldDisplayGUI) {
		Text parsedText = database.parse(testTextName, CharStreams.fromString(stringToParse), shouldDisplayGUI);
		database.addTextToDatabase(parsedText);
		database.initializeLemmas();
		return parsedText;
	}
	
	public static void assertSameObjectInList(List<Object> listOfObjects) {
		if (listOfObjects.size() < 2) return;
		
		Object firstObject = listOfObjects.get(0);
		//Sameness is transetive, so it is only necessary to compare the objects to the first object.
		for (Object object : listOfObjects) {
			assertSame(firstObject, object);
		}
	}
	

	public static void assertContainsSameObject(Object expected, Set<Sentence> set) {		
		boolean hasSameObject = false;
		for (Object object : set) {
			if (object == expected)
				hasSameObject = true;
		}
		assertTrue(hasSameObject);
	}

	public static void parseText(File fileToParse, TextDatabase database) {
		database.parseTextAndAddToDatabase(fileToParse);		
		database.initializeLemmas();
	}

	public static void displayParserTree(TextParsingGrammarParser parser, StartContext startContext) {
			JFrame frame = new JFrame("Antlr AST");
			JPanel panel = new JPanel();
			frame.setLayout(new BorderLayout());
			frame.add(BorderLayout.CENTER, new JScrollPane(panel));
			frame.setLocationRelativeTo(null);
			
			//JScrollPane scrollPane = new JScrollPane();
			//scrollPane.setPreferredSize(new Dimension( 800,300));
			panel.setAutoscrolls(true);
			TreeViewer viewer = new TreeViewer(Arrays.asList(
					parser.getRuleNames()),startContext);
			viewer.setScale(1); // Scale a little
			viewer.setAutoscrolls(true);
			panel.add(viewer);			
			//scrollPane.add(viewer);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(new Dimension( 800,300));	
			frame.pack();
			frame.setVisible(true);
	}
	

}
