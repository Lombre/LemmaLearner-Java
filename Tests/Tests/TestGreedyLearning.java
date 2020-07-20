package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.junit.*;

import LemmaLearner.*;



class TestGreedyLearning {
	

	TextDatabase database;
	Text parsedText;
	
	@BeforeEach
	public void setUp() {
		database = new TextDatabase(false, false);
	}
	

	@Test
	public void testWordsByFrequencyOrderedCorrectly() throws Exception {
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
		
		GreedyLearner learner = new GreedyLearner(database);
		PriorityQueue<Word> wordsByFrequency = learner.getWordsByFrequency();
		Word hopefullyWord1 = wordsByFrequency.poll();
		Word hopefullyWord2 = wordsByFrequency.poll();
		Word hopefullyWord3 = wordsByFrequency.poll();
		assertNull(wordsByFrequency.poll());
		
		assertSame(database.allWords.get(word1), hopefullyWord1);
		assertSame(database.allWords.get(word2), hopefullyWord2);
		assertSame(database.allWords.get(word3), hopefullyWord3);
	}
	
	@Test
	public void testSentencesByFrequencyOrderedCorrectly() throws Exception {
		String word1 = "one";
		String word2 = "two";
		String word3 = "three";
		String expectedSentence1 = word1 + ".";
		String expectedSentence2 = word2 + ".";
		String expectedSentence3 = word3 + ".";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3 + " " + expectedSentence3 + " " + expectedSentence3 + " " + expectedSentence2;
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		assertSame(parsedText, returnedText);
		
		GreedyLearner learner = new GreedyLearner(database);
		PriorityQueue<Sentence> wordsByFrequency = learner.getSentencesByWordFrequency();
		wordsByFrequency.add(database.allSentences.get(expectedSentence2));
		wordsByFrequency.add(database.allSentences.get(expectedSentence1));
		wordsByFrequency.add(database.allSentences.get(expectedSentence3));
		Sentence hopefullySentence3 = wordsByFrequency.poll();
		Sentence hopefullySentence2 = wordsByFrequency.poll();
		Sentence hopefullySentence1 = wordsByFrequency.poll();
		assertNull(wordsByFrequency.poll());
		
		assertSame(database.allSentences.get(expectedSentence1), hopefullySentence1);
		assertSame(database.allSentences.get(expectedSentence2), hopefullySentence2);
		assertSame(database.allSentences.get(expectedSentence3), hopefullySentence3);
	}
	

	@Test
	public void testSentenceIsDirectlyLearnable() throws Exception {
		String word1 = "one";
		String word2 = "two";
		String word3 = "three";
		String expectedSentence = word1 + " " + word2 + " " + word3 + ".";
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedSentence);
		assertSame(parsedText, returnedText);
		
		Set<Word> learnedWords = new HashSet<Word>();
		Sentence sentence = database.allSentences.get(expectedSentence);
		
		assertFalse(sentence.isDirectlyLearnable(learnedWords, database));
		
		learnedWords.add(database.allWords.get(word1));
		assertFalse(sentence.isDirectlyLearnable(learnedWords, database));
		
		learnedWords.add(database.allWords.get(word2));
		assertTrue(sentence.isDirectlyLearnable(learnedWords, database));
		
		learnedWords.add(database.allWords.get(word3));
		assertTrue(sentence.isDirectlyLearnable(learnedWords, database));
		
	}
	
	
	

}
