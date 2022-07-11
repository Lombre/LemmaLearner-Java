package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Configurations.Configurations;

import static org.junit.Assert.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.*;

import GUI.ConsoleGUI;
import LemmaLearner.*;
import TextDataStructures.Sentence;
import TextDataStructures.Text;


public class TestTool {
		
	public static final String testTextName = "test";

	public static Text parseString(String stringToParse, TextDatabase database) {
		return parseString(stringToParse, database, false);
	}
	
	public static Text parseString(String stringToParse, TextDatabase database, boolean shouldDisplayGUI) {
		return database.parseRawText(testTextName,  stringToParse);
	}
	
	
	public static Text parseStringAndAddToDatabase(String stringToParse, TextDatabase database) {
		Text parsedText = database.parseRawText(testTextName, stringToParse);
		database.addTextToDatabase(parsedText, new ConsoleGUI());
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
		database.parseTextAndAddToDatabase(fileToParse, new ConsoleGUI());		
		database.initializeLemmas();
	}
	
	public static void changeConfigField(Configurations config, String field, String value) {
		try {
			var map = (Map<String, String>) FieldUtils.readField(config, "configurationKeyToValue", true);
			map.put(field, value);
		} catch (IllegalAccessException e) {
			throw new Error();
		}
	}

}
