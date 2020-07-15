package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.junit.*;

import LemmaLearner.*;



class TestParsing {
	

	TextDatabase database;
	Text parsedText;
	
	@BeforeEach
	public void setUp() {
		database = new TextDatabase(false, false);
	}
	
	@Test
	void testCorrectTextName() throws IOException {
		parsedText = database.parse("Test texts/singleWordWithPunctuation.txt", false);
		assertEquals("singleWordWithPunctuation.txt", parsedText.getName());
	}

	@Test
	void testParseSingleWordSentence() throws IOException {
		String expectedWord = "Hello";
		String expectedParagraph = expectedWord + ".";
		
		parsedText = database.parse("Test texts/singleWordWithPunctuation.txt", false);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedParagraph);
		assertEquals(sentence.getWordList().size(), 1);
		
		Word word = sentence.getWordList().get(0);
		assertEquals(word.getRawWord(), expectedWord.toLowerCase());		
	}

	@Test
	void testParseMultiWordSentence() throws IOException {
		

		String expectedWord1 = "This";
		String expectedWord2 = "Hopefully";
		String expectedWord3 = "works";
		String expectedParagraph = expectedWord1 + " " + expectedWord2 + " " + expectedWord3 + ".";
				
		parsedText = database.parse("Test texts/multiWordSentence.txt", false);		
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedParagraph);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedParagraph);
		assertEquals(sentence.getWordList().size(), 3);
		
		Word word1 = sentence.getWordList().get(0);
		assertEquals(word1.getRawWord(), expectedWord1.toLowerCase());	

		Word word2 = sentence.getWordList().get(1);
		assertEquals(word2.getRawWord(), expectedWord2.toLowerCase());	

		Word word3 = sentence.getWordList().get(2);
		assertEquals(word3.getRawWord(), expectedWord3.toLowerCase());	
		
	}
	

	@Test
	void testParseMultiSentenceParagraph() throws IOException {		
		String expectedSentence1 = "First sentence.";
		String expectedSentence2 = "Second sentence!";
		String expectedSentence3 = "Third sentence?";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3;
				
		parsedText = database.parse("Test texts/multiSentenceParagraph.txt", false);		
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
		String expectedText = expectedParagraph1 + "\r\n\r\n" + expectedParagraph2 + "\r\n" + expectedParagraph3;
				
		parsedText = database.parse("Test texts/multiParagraphText.txt", false);		
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

		parsedText = database.parse("Test texts/singleQuotedSentence.txt", false);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getWordList().size(), 6);
	}
	

	@Test
	void testParseNestedQuotedSentence() throws IOException {	
		String expectedSentence = "“This. “Should. Hopefully! (Be...) One Sentence!””.";

		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getWordList().size(), 6);
		
		assertEquals("this", sentence.getWordList().get(0).getRawWord());
		assertEquals("should", sentence.getWordList().get(1).getRawWord());
		assertEquals("hopefully", sentence.getWordList().get(2).getRawWord());
		assertEquals("be", sentence.getWordList().get(3).getRawWord());
		assertEquals("one", sentence.getWordList().get(4).getRawWord());
		assertEquals("sentence", sentence.getWordList().get(5).getRawWord());
	}
	

	@Test
	void testParseSentenceWithInterruptedQuotes() throws IOException {	
		String expectedSentence = "This “Should.” Hopefully “Be...” “One Sentence”.";

		parsedText = TestTool.parseString(expectedSentence, database);
		assertEquals(parsedText.getParagraphs().size(), 1);
		assertEquals(parsedText.getRawText(), expectedSentence);
		
		Paragraph paragraph = (Paragraph) parsedText.getParagraphs().toArray()[0];
		assertEquals(paragraph.getRawParagraph(), expectedSentence);
		assertEquals(paragraph.getSentences().size(), 1);
		
		Sentence sentence = (Sentence) paragraph.getSentences().toArray()[0];
		assertEquals(sentence.getRawSentence(), expectedSentence);
		assertEquals(sentence.getWordList().size(), 6);
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
		assertEquals(sentence.getWordList().size(), 5);
		
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
		assertEquals(sentence1.getWordList().size(), 5);
		
		Sentence sentence2 = (Sentence) paragraph.getSentences().toArray()[1];
		assertEquals(sentence2.getRawSentence(), expectedSentence2);
		assertEquals(sentence2.getWordList().size(), 5);		
	}
	
	@Test
	void testIgnoreUnclosedSentences() throws Exception {
		String text1 = "This “Should hopefully be ignored as a sentece, but not as text.";
		String text2 = "So should” this.";

		assertEquals(TestTool.parseString(text1, database).getRawText(), text1);
		assertEquals(TestTool.parseString(text1, database).getParagraphs().size(), 0);

		assertEquals(TestTool.parseString(text2, database).getRawText(), text2);
		assertEquals(TestTool.parseString(text2, database).getParagraphs().size(), 0);		
	}
	

	
	
	

}
