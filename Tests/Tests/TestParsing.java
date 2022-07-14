package Tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Configurations.Configurations;
import LemmaLearner.TextDatabase;
import TextDataStructures.Paragraph;
import TextDataStructures.Sentence;
import TextDataStructures.Text;



class TestParsing {
	

	TextDatabase database;
	Text parsedText;
	Configurations config;
	
	@BeforeEach
	public void setUp() {
		config = new Configurations();
		database = new TextDatabase(config);
	}
	
	@Test
	void testCorrectTextName() throws IOException {
		parsedText = database.parseRawText("singleWordWithPunctuation.txt", "Test texts/singleWordWithPunctuation.txt");
		assertEquals("singleWordWithPunctuation.txt", parsedText.getName());
	}
	

	@Test
	void testWordSplitCorrectlySingleQuoteCase() throws Exception {
		String wordPart1 = "Hej";
		String wordPart2 = "med";
		String wordPart3 = "dig";
		String expectedSentence = "'" + wordPart1 + "'" + wordPart2 + "'" + wordPart3 + "'";
		
		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);

		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);

		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(3, sentence.getRawWordList().size());
		
		assertEquals(wordPart1.toLowerCase(), sentence.getRawWordList().get(0));
		assertEquals(wordPart2.toLowerCase(), sentence.getRawWordList().get(1));
		assertEquals(wordPart3.toLowerCase(), sentence.getRawWordList().get(2));
	}
	


	@Test
	void testWordSplitCorrectlySingleQuoteCommaCase() throws Exception {
		String word = "Hej";
		String expectedSentence = word + "\',";
		
		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);

		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);

		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(1, sentence.getRawWordList().size());
		
		assertEquals(word.toLowerCase(), sentence.getRawWordList().get(0));
	}

	@Test
	void testParseSingleWordSentence() throws IOException {
		String expectedWord = "Hello";
		String expectedParagraph = expectedWord + ".";
		
		parsedText = TestTool.parseString(expectedParagraph, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedParagraph);
		assertEquals(sentence.getRawWordList().size(), 1);
		
		String rawWord = sentence.getRawWordList().get(0);
		assertEquals(rawWord, expectedWord.toLowerCase());		
	}

	@Test
	void testParseMultiWordSentence() throws IOException {
		

		String expectedWord1 = "This";
		String expectedWord2 = "Hopefully";
		String expectedWord3 = "works";
		String expectedParagraph = expectedWord1 + " " + expectedWord2 + " " + expectedWord3 + ".";
				
		parsedText = database.parseTextFile("Test texts/multiWordSentence.txt");		
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedParagraph);
		assertEquals(sentence.getRawWordList().size(), 3);
		
		String rawWord1 = sentence.getRawWordList().get(0);
		assertEquals(rawWord1, expectedWord1.toLowerCase());	

		String rawWord2 = sentence.getRawWordList().get(1);
		assertEquals(rawWord2, expectedWord2.toLowerCase());	

		String rawWord3 = sentence.getRawWordList().get(2);
		assertEquals(rawWord3, expectedWord3.toLowerCase());	
		
	}
	

	@Test
	void testParseMultiSentenceParagraph() throws IOException {		
		String expectedSentence1 = "First sentence.";
		String expectedSentence2 = "Second sentence!";
		String expectedSentence3 = "Third sentence?";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3;
				
		parsedText = database.parseTextFile("Test texts/multiSentenceParagraph.txt");		
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 3);
						
		Sentence sentence1 = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence1.getRawSentence(), expectedSentence1);
		Sentence sentence2 = (Sentence) paragraph.getSentences().toArray()[1];
		assertEquals(sentence2.getRawSentence(), expectedSentence2);		
		Sentence sentence3 = (Sentence) paragraph.getSentences().toArray()[2];
		assertEquals(sentence3.getRawSentence(), expectedSentence3);
		
	}
		

	@Test
	void testParseMultipleParagraphs() throws IOException {		
		String expectedParagraph1 = "First paragraph. This is cool.";
		String expectedParagraph2 = "Second paragraph. This is also cool.";
		String expectedParagraph3 = "Third paragraph. This is even cooler!";
		String expectedText = expectedParagraph1 + "\n\n" + expectedParagraph2 + "\n" + expectedParagraph3;
				
		parsedText = database.parseTextFile("Test texts/multiParagraphText.txt");		
		assertEquals(3, parsedText.getParagraphs().size());
		assertEquals(expectedText, parsedText.getRawText());
		
		Paragraph paragraph1 = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(expectedParagraph1, paragraph1.getRawParagraph());

		Paragraph paragraph2 = (Paragraph) parsedText.getParagraphs().toArray()[1];
		assertEquals(expectedParagraph2, paragraph2.getRawParagraph());

		Paragraph paragraph3 = (Paragraph) parsedText.getParagraphs().toArray()[2];
		assertEquals(expectedParagraph3, paragraph3.getRawParagraph());
		
	}
	
	@Test
	void testParseSingleQuotedSentence() throws IOException {	
		String expectedSentence = "“This. Should. Hopefully! Be... One Sentence!”.";

		parsedText = database.parseTextFile("Test texts/singleQuotedSentence.txt");
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getRawWordList().size(), 6);
	}
	

	@Test
	void testParseNestedQuotedSentence() throws IOException {	
		String expectedSentence = "“This.  “Should. Hopefully! (Be...) One Sentence””.";
		
		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(1, parsedText.getParagraphs().size());
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getRawWordList().size(), 6);
		
		List<String> rawWordList = sentence.getRawWordList();
		assertEquals("this", rawWordList.get(0));
		assertEquals("should", rawWordList.get(1));
		assertEquals("hopefully", rawWordList.get(2));
		assertEquals("be", rawWordList.get(3));
		assertEquals("one", rawWordList.get(4));
		assertEquals("sentence", rawWordList.get(5));
	}
	

	@Test
	void testParseSentenceWithInterruptedQuotes() throws IOException {	
		String expectedSentence = "This (Should.) Hopefully 'Be...' \"One Sentence\".";

		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getRawWordList().size(), 6);
	}
	

	@Test
	void testParseSingleDanglingSentence() throws Exception {
		String expectedSentence = "This is a dangling Sentence";	
		
		
		Text parsedText = TestTool.parseString(expectedSentence, database);
		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getRawWordList().size(), 5);
		
	}	


	@Test
	void testParseParagraphEndingInDanglingSentence() throws Exception {
		String expectedSentence1 = "This is a normal sentence.";	
		String expectedSentence2 = "This is a dangling Sentence";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2;		
		
		parsedText = TestTool.parseString(expectedParagraph, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 2);
		
		Sentence sentence1 = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence1.getRawSentence(), expectedSentence1);
		assertEquals(sentence1.getRawWordList().size(), 5);
		
		Sentence sentence2 = (Sentence) paragraph.getSentences().toArray()[1];
		assertEquals(sentence2.getRawSentence(), expectedSentence2);
		assertEquals(sentence2.getRawWordList().size(), 5);		
	}
	
	@Test
	void testIgnoreUnclosedSentences() throws Exception {
		String text1 = "This \"Should hopefully be ignored as a sentece, but not as text.";
		String text2 = "So should\" this.";

		assertEquals(TestTool.parseString(text1, database).getRawText(), text1);
		assertEquals(TestTool.parseString(text1, database).getParagraphs().size(), 0);

		assertEquals(TestTool.parseString(text2, database).getRawText(), text2);
		assertEquals(TestTool.parseString(text2, database).getParagraphs().size(), 0);		
	}
	

	
	
	

}
