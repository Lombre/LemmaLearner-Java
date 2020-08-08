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
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database, false);
		
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
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database, false);
		
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
		assertEquals(word1.toLowerCase(), database.allWords.get(word1.toLowerCase()).getRawConjugation());
		assertEquals(word2.toLowerCase(), database.allWords.get(word2.toLowerCase()).getRawConjugation());
		assertEquals(word3.toLowerCase(), database.allWords.get(word3.toLowerCase()).getRawConjugation());
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
		

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedText, database, false);
		
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
		for (Conjugation word : database.allWords.values()) {
			if (!word.getRawConjugation().equals("different")) TestTool.assertContainsSameObject(parsedSentence1, word.getSentences());
			if (!word.getRawConjugation().equals("repeated")) TestTool.assertContainsSameObject(parsedSentence2, word.getSentences());
		}
		
		
	}
	

	@Test
	public void testWordFrequencyIncreasedPerEncounter() throws Exception {
		String word1 = "one";
		String word2 = "two";
		String word3 = "three";
		String expectedSentence1 = word1 + " " + word2 + " " + word3 + ".";
		String expectedSentence2 = word2 + " " + word3 + ".";
		String expectedSentence3 = word3 + ".";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3;
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		assertSame(parsedText, returnedText);
		
		assertEquals(1, database.allParagraphs.size());
		assertEquals(3, database.allSentences.size());
		assertEquals(3, database.allWords.size());
		
		assertEquals(1, database.allWords.get(word1).getFrequency());
		assertEquals(2, database.allWords.get(word2).getFrequency());
		assertEquals(3, database.allWords.get(word3).getFrequency());
	}
	

	@Test
	public void testFrequencyOfWordsOnlyCountedOncePerSentence() throws Exception {
		String word1 = "unique";
		String word2 = "word";
		String word3 = "not";
		String word4 = "here";
		String expectedSentence1 = "Unique word.";
		String expectedSentence2 = "Not not here here.";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2;
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedParagraph, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		assertEquals(1, database.allParagraphs.size());
		assertEquals(2, database.allSentences.size());
		assertEquals(4, database.allWords.size());
		
		assertEquals(1, database.allWords.get(word1).getFrequency());
		assertEquals(1, database.allWords.get(word2).getFrequency());
		assertEquals(1, database.allWords.get(word3).getFrequency());
		assertEquals(1, database.allWords.get(word4).getFrequency());
	}
	
	
	
	
	

}
