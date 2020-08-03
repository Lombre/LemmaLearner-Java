package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import static org.junit.Assert.*;

import java.io.File;
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
	GreedyLearner learner;
	
	@BeforeEach
	public void setUp() {
		database = new TextDatabase(false, false);
		learner = new GreedyLearner(database);
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
		Word hopefullyWord3 = wordsByFrequency.poll();
		Word hopefullyWord2 = wordsByFrequency.poll();
		Word hopefullyWord1 = wordsByFrequency.poll();
		assertNull(wordsByFrequency.poll());
		
		assertSame(database.allWords.get(word1), hopefullyWord1);
		assertSame(database.allWords.get(word2), hopefullyWord2);
		assertSame(database.allWords.get(word3), hopefullyWord3);
	}
	
	@Test
	public void testInitialDirectlyLearnableWordsByFrequencyOrderedCorrectly() throws Exception {
		String word1 = "flour"; //Occurs once.
		String word2 = "egg"; //Occurs twice.
		String word3 = "milk"; //Occurs thrice.
		String expectedSentence1 = word1 + ".";
		String expectedSentence3 = word2 + ".";
		String expectedSentence2 = word3 + ".";
		String expectedSentence4 = word2 + " " + word3 + ".";
		String expectedSentence5 = word3 + " " + "cake" + ".";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3 + " " + expectedSentence4 + " " + expectedSentence5;
		

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(1, database.allTexts.size());
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedParagraph, parsedText.getRawText());
		assertSame(parsedText, returnedText);
		
		GreedyLearner learner = new GreedyLearner(database);
		Set<Word> learnedWords = new HashSet<Word>();
		var sentencesByUnlearnedWordFrequency = learner.getDirectlyLearnableWordsByFrequency(learnedWords);
		
		assertEquals(expectedSentence2, sentencesByUnlearnedWordFrequency.poll().getFirst().getRawSentence());
		assertEquals(expectedSentence3, sentencesByUnlearnedWordFrequency.poll().getFirst().getRawSentence());
		assertEquals(expectedSentence1, sentencesByUnlearnedWordFrequency.poll().getFirst().getRawSentence());
		assertNull(sentencesByUnlearnedWordFrequency.poll());
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
	

	@Test
	public void testLearnedLemmasIsDoneGreedily() throws Exception {
		//If it is done greedily, the invariant that there is no word w2 learned after another word w1,
		//Such that w1.frequency < w2.frequency, unless w1 participates in the sentence used to learn w2,
		//or w2 is learnt without a sentence.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse);
		List<Pair<Word, Sentence>> learningOrder = learner.learnAllLemmas();
		for (int i = 0; i < learningOrder.size() - 1; i++) {
			var currentWord = learningOrder.get(i).getFirst();
			var currentSentence = learningOrder.get(i).getSecond();
			var nextWord = learningOrder.get(i+1).getFirst();
			var nextSentence = learningOrder.get(i+1).getSecond();
			
			if (!nextSentence.getRawSentence().equals(learner.NOT_A_SENTENCE_STRING) && !nextSentence.getWordSet().contains(currentWord)) {
				if (!(nextWord.getFrequency() <= currentWord.getFrequency())) {
					int k = 1;
				}
				assertTrue("Greedy invariant broken: Word " + nextWord + " has a higher frequency than " + currentWord + " but is learnt after the word, for no reason.", nextWord.getFrequency() <= currentWord.getFrequency());
			}
		}
	}
	
	


	@Test
	public void testLemmasAreLearnedOneByOne() throws Exception {
		//A lemma must be learned one at a time, for it to be a valid learning order.
		//Thus each new sentence must contain exactly one new lemma.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse);
		List<Pair<Word, Sentence>> learningOrder = learner.learnAllLemmas();
		Set<Word> learnedWords = new HashSet<Word>();
		for (int i = 0; i < learningOrder.size() - 1; i++) {
			var learnedWord = learningOrder.get(i).getFirst();
			var currentSentence = learningOrder.get(i).getSecond();
			assertFalse(learnedWords.contains(learnedWord));
			for (Word wordInSentence : currentSentence.getWordsInDatabase(database)) {
				if (wordInSentence == learnedWord)
					assertFalse(learnedWords.contains(wordInSentence));
				else assertTrue(learnedWords.contains(wordInSentence));				
			}
			learnedWords.add(learnedWord);
		}
	}
	

}
