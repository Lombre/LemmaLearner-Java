package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.junit.*;

import LemmaLearner.*;



class TestDatabaseInsertion {
	

	TextDatabase database;
	Text parsedText;
	
	@BeforeEach
	public void setUp() {
		database = new TextDatabase(false, false);
	}
	
	@Test
	public void testEmptyTextInsertion_NoAddedParagraphsSentencesOrWords() throws Exception {
		String expectedSentence = "";
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedSentence, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		assertEquals(database.allParagraphs.size(), 0);
		assertEquals(database.allSentences.size(), 0);
		assertEquals(database.allWords.size(), 0);
	}
	
	
	@Test
	public void testSingleSentenceInsertion() throws Exception {
		String word1 = "This";
		String word2 = "hopefully";
		String word3 = "works";
		String expectedSentence = word1 + " " + word2 + " " + word3;
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get("test");
		assertEquals(expectedSentence, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		assertEquals(database.allParagraphs.size(), 1);
		Paragraph parsedParagraph = database.allParagraphs.get("test0");
		assertEquals(expectedSentence, parsedParagraph.getRawParagraph());
		assertSame(parsedParagraph, parsedText.getParagraphs().toArray()[0]);
		
		assertEquals(database.allSentences.size(), 1);
		Sentence parsedSentence = database.allSentences.get(expectedSentence);
		assertEquals(expectedSentence, parsedSentence.getRawSentence());
		assertSame(parsedSentence, parsedParagraph.getSentences().toArray()[0]);
		
		assertEquals(database.allWords.size(), 3);
		assertEquals(word1.toLowerCase(), database.allWords.get(word1.toLowerCase()).getRawWord());
		assertEquals(word2.toLowerCase(), database.allWords.get(word2.toLowerCase()).getRawWord());
		assertEquals(word3.toLowerCase(), database.allWords.get(word3.toLowerCase()).getRawWord());
	}
	

	//@Test
	//public void testTwoTextsInsertion() throws Exception {
	//	fail();
	//}
	

	@Test
	public void testSameSentenceSameTextInsertion() throws Exception {
		String repeatedSentence = "This is a repeated sentence.";
		String nonRepeatedSentence = "This is a different sentence.";
		String paragraph2 = nonRepeatedSentence + " " + repeatedSentence; 
		String expectedText = repeatedSentence + "\r\n" + paragraph2;
		

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedText, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get("test");
		assertEquals(expectedText, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		//The paragraphs should be different, but be part of the same text.
		assertEquals(database.allParagraphs.size(), 2);
		Paragraph parsedParagraph1 = database.allParagraphs.get("test0");
		Paragraph parsedParagraph2 = database.allParagraphs.get("test1");
		Paragraph textParagraph1 = (Paragraph) parsedText.getParagraphs().toArray()[0];
		Paragraph textParagraph2 = (Paragraph) parsedText.getParagraphs().toArray()[1];
		assertEquals(repeatedSentence, parsedParagraph1.getRawParagraph());
		assertEquals(paragraph2, parsedParagraph2.getRawParagraph());
		assertSame(parsedParagraph1, textParagraph1);
		assertSame(parsedParagraph2, textParagraph2);
		assertSame(parsedText, parsedParagraph1.getOriginText());
		assertSame(parsedText, parsedParagraph2.getOriginText());
		assertNotSame(parsedParagraph1, parsedParagraph2);
		
		assertEquals(database.allSentences.size(), 2);
		
		Sentence parsedSentence1 = database.allSentences.get(repeatedSentence);		
		assertEquals(repeatedSentence, parsedSentence1.getRawSentence());
		assertSame(parsedSentence1, parsedParagraph1.getSentences().toArray()[0]);
		assertSame(parsedSentence1, textParagraph1.getSentences().toArray()[0]);
		assertSame(parsedSentence1, parsedParagraph2.getSentences().toArray()[1]);
		assertSame(parsedSentence1, textParagraph2.getSentences().toArray()[1]);

		Sentence parsedSentence2 = database.allSentences.get(nonRepeatedSentence);
		assertEquals(nonRepeatedSentence, parsedSentence2.getRawSentence());
		assertSame(parsedSentence2, parsedParagraph2.getSentences().toArray()[0]);
		assertSame(parsedSentence2, textParagraph2.getSentences().toArray()[0]);
		
		assertNotSame(parsedSentence1, parsedSentence2);
		
		assertEquals(database.allWords.size(), 6);
		for (Word word : database.allWords.values()) {
			if (!word.getRawWord().equals("different")) TestTool.assertContainsSameObject(parsedSentence1, word.getSentences());
			if (!word.getRawWord().equals("repeated")) TestTool.assertContainsSameObject(parsedSentence2, word.getSentences());
		}
		
		
	}
	
	
	
	
	

}
