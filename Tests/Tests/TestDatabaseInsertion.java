package Tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Configurations.Configurations;
import GUI.ConsoleGUI;
import LemmaLearner.TextDatabase;
import TextDataStructures.Conjugation;
import TextDataStructures.Paragraph;
import TextDataStructures.Sentence;
import TextDataStructures.Text;



class TestDatabaseInsertion {
	

	TextDatabase database;
	Text parsedText;
	Configurations config;
	
	@BeforeEach
	public void setUp() {
		config = new Configurations("Tests/test_config.txt");
		database = new TextDatabase(config);
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
		Paragraph parsedParagraph = database.allParagraphs.get("test_0");
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
		

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedText, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get("test");
		assertEquals(expectedText, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		//The paragraphs should be different, but be part of the same text.
		assertEquals(database.allParagraphs.size(), 2);
		Paragraph parsedParagraph1 = database.allParagraphs.get("test_0");
		Paragraph parsedParagraph2 = database.allParagraphs.get("test_1");
		Paragraph textParagraph1 = (Paragraph) parsedText.getParagraphs().toArray()[0];
		Paragraph textParagraph2 = (Paragraph) parsedText.getParagraphs().toArray()[1];
		assertEquals(repeatedSentence, parsedParagraph1.getRawParagraph());
		assertEquals(paragraph2, parsedParagraph2.getRawParagraph());
		assertSame(parsedParagraph1, textParagraph1);
		assertSame(parsedParagraph2, textParagraph2);
		assertSame(parsedText, parsedParagraph1.getOriginText());
		assertSame(parsedText, parsedParagraph2.getOriginText());
		assertNotSame(parsedParagraph1, parsedParagraph2);
		
		//The sentences are repeated, and thus only one should be saved.
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
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database);
		
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
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedParagraph, parsedText.getRawText());
		assertSame(returnedText, parsedText);
		
		assertEquals(1, database.allParagraphs.size());
		System.out.println(config.getMinSentenceLengthInLetters());
		assertEquals(database.allSentences.toString(), 2, database.allSentences.size());
		assertEquals(4, database.allWords.size());
		
		assertEquals(1, database.allWords.get(word1).getFrequency());
		assertEquals(1, database.allWords.get(word2).getFrequency());
		assertEquals(1, database.allWords.get(word3).getFrequency());
		assertEquals(1, database.allWords.get(word4).getFrequency());
	}
	
	@Test
	public void testAddFilesInFolderToDatabase_texts_are_added() {
		database.addAllTextsInFolderToDatabase("Test texts/MultiFileFolder/", new ConsoleGUI());
		assertEquals(2, database.allTexts.size());
		assertTrue(database.allTexts.keySet().contains("test1.txt"));
		assertTrue(database.allTexts.keySet().contains("test2.txt"));
	}
	
	@Test
	public void testAddFilesInFolderToDatabase_sentences_are_added() {
		database.addAllTextsInFolderToDatabase("Test texts/MultiFileFolder/", new ConsoleGUI());
		
		assertEquals(2, database.allSentences.size());
		assertTrue(database.allSentences.keySet().contains("This is a test - 1."));
		assertTrue(database.allSentences.keySet().contains("This is also a test - 2."));
	}

	@Test
	public void testAddFilesInFolderToDatabase_words_are_added() {
		database.addAllTextsInFolderToDatabase("Test texts/MultiFileFolder/", new ConsoleGUI());
		
		assertEquals(5, database.allWords.size());
		System.out.println(database.allWords.keySet());
		assertTrue(database.allWords.keySet().contains("this"));
		assertTrue(database.allWords.keySet().contains("is"));
		assertTrue(database.allWords.keySet().contains("also"));
		assertTrue(database.allWords.keySet().contains("a"));
		assertTrue(database.allWords.keySet().contains("test"));
	}


	@Test
	public void testAddFilesInFolderToDatabase_lemmas_are_added() {
		database.addAllTextsInFolderToDatabase("Test texts/MultiFileFolder/", new ConsoleGUI());
		
		assertEquals(5, database.allLemmas.size());
		System.out.println(database.allLemmas.keySet());
		assertTrue(database.allLemmas.keySet().contains("this"));
		assertTrue(database.allLemmas.keySet().contains("be"));
		assertTrue(database.allLemmas.keySet().contains("also"));
		assertTrue(database.allLemmas.keySet().contains("a"));
		assertTrue(database.allLemmas.keySet().contains("test"));
	}
	

	@Test
	public void testFilteringOfSentencesOnNumberOfWords_toFewWords() {
		TestTool.changeConfigField(config, "MinSentenceLengthInWords", "10");
		TestTool.changeConfigField(config, "MaxSentenceLengthInWords", "20");
		TestTool.parseStringAndAddToDatabase("Yes. Yes no. No yes yes cake. Not enough words in this sentence.", database);
		assertEquals(0, database.allSentences.size());
	}
	


	@Test
	public void testFilteringOfSentencesOnNumberOfWords_enoughWordsInSentence() {
		TestTool.changeConfigField(config, "MinSentenceLengthInWords", "10");
		TestTool.changeConfigField(config, "MaxSentenceLengthInWords", "20");
		TestTool.parseStringAndAddToDatabase("There are enough words in this sentence to be included in the database. There are also enough words in this sentence to be included in the database.", database);
		assertEquals(2, database.allSentences.size());
	}
	

	@Test
	public void testFilteringOfSentencesOnNumberOfWords_toManyWords() {
		TestTool.changeConfigField(config, "MinSentenceLengthInWords", "10");
		TestTool.changeConfigField(config, "MaxSentenceLengthInWords", "20");
		TestTool.parseStringAndAddToDatabase("There are far far to many words in this sentence for it to reasonably be included in the text database seen here.", database);
		assertEquals(0, database.allSentences.size());
	}
	
	

	@Test
	public void testFilteringOfSentencesOnNumberOfWords_toManyWords_butWithSubsentence() {
		TestTool.changeConfigField(config, "MinSentenceLengthInWords", "5");
		TestTool.changeConfigField(config, "MaxSentenceLengthInWords", "20");
		String subSentence = "but not in terms of this subsentence";
		TestTool.parseStringAndAddToDatabase("There are far far to many words in this sentence (" + subSentence + ") for it to reasonably be included in the text database seen here.", database);
		assertEquals(1, database.allSentences.size());
		assertTrue(database.allSentences.containsKey(subSentence));
	}
	


	@Test
	public void testFilteringOfSentencesOnNumberOfLetters_toFewLetters() {
		TestTool.changeConfigField(config, "MinSentenceLengthInLetters", "20");
		TestTool.changeConfigField(config, "MaxSentenceLengthInLetters", "100");
		TestTool.parseStringAndAddToDatabase("Blah. Cake is good. Not enogh letters.", database);
		assertEquals(0, database.allSentences.size());
	}

	@Test
	public void testFilteringOfSentencesOnNumberOfLetters_enoughLetters() {
		TestTool.changeConfigField(config, "MinSentenceLengthInLetters", "20");
		TestTool.changeConfigField(config, "MaxSentenceLengthInLetters", "100");
		TestTool.parseStringAndAddToDatabase("There are enogugh letters in this sentence. It is also the case in this sentence.", database);
		assertEquals(2, database.allSentences.size());
	}

	@Test
	public void testFilteringOfSentencesOnNumberOfLetters_toManyLetters() {
		TestTool.changeConfigField(config, "MinSentenceLengthInLetters", "20");
		TestTool.changeConfigField(config, "MaxSentenceLengthInLetters", "50");
		TestTool.parseStringAndAddToDatabase("There are definetely to many letters in this sentnence for it to actually be included in the database, hahahahahahahahahahahahahahahaha.", database);
		assertEquals(0, database.allSentences.size());
	}

	@Test
	public void testFilteringOfSentencesOnNumberOfLetters_toManyLetters_withSubsentence() {
		TestTool.changeConfigField(config, "MinSentenceLengthInLetters", "20");
		TestTool.changeConfigField(config, "MaxSentenceLengthInLetters", "50");
		String subSentence = "but not in terms of this subsentence";
		TestTool.parseStringAndAddToDatabase("There are definetely to many letters in this sentnence for it to actually be included in the database (" + subSentence + "), hahahahahahahahahahahahahahahaha.", database);
		assertEquals(1, database.allSentences.size());
		assertTrue(database.allSentences.containsKey(subSentence));
	}
	

	@Test
	public void testFilteringOfSentencesOnNumberOfWordsAndLetters() {
		TestTool.changeConfigField(config, "MinSentenceLengthInWords", "4");
		TestTool.changeConfigField(config, "MaxSentenceLengthInWords", "30");
		TestTool.changeConfigField(config, "MinSentenceLengthInLetters", "10");
		TestTool.changeConfigField(config, "MaxSentenceLengthInLetters", "80");
		var testString = "“Then, good night, your Majesty, and I trust that we shall soon have some good news for you. And good night, Watson,” he added, as the wheels of the royal brougham rolled down the street. “If you will be good enough to call tomorrow afternoon at three o’clock I should like to chat this little matter over with you.”";
		TestTool.parseStringAndAddToDatabase(testString, database);
		assertEquals(0, database.allSentences.size());
			
	}
	
	
	
	

}
