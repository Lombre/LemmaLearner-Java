package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.junit.*;

import LemmaLearner.*;



class TestTool {
		
	public static final String testTextName = "test";

	public static Text parseString(String stringToParse, TextDatabase database) {
		return database.parse(testTextName, CharStreams.fromString(stringToParse), false);
	}
	
	
	public static Text parseStringAndAddToDatabase(String stringToParse, TextDatabase database) {
		Text parsedText = database.parse(testTextName, CharStreams.fromString(stringToParse), false);
		database.addTextToDatabase(parsedText);
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
	

}
