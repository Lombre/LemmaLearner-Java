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
	Configurations config;
	
	@BeforeEach
	public void setUp() {
		config = new Configurations();
		database = new TextDatabase(config);
		learner = new GreedyLearner(database, config);
	}
	

	@Test
	public void testLemmasByFrequencyOrderedCorrectly() throws Exception {
		String Lemma1 = "one";
		String Lemma2 = "two";
		String Lemma3 = "three";
		String expectedSentence1 = Lemma1 + " " + Lemma2 + " " + Lemma3 + ".";
		String expectedSentence2 = Lemma2 + " " + Lemma3 + ".";
		String expectedSentence3 = Lemma3 + ".";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3;
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedParagraph);
		assertSame(parsedText, returnedText);
		
		PriorityQueue<Lemma> LemmasByFrequency = learner.getLemmasByFrequency();
		Lemma hopefullyLemma3 = LemmasByFrequency.poll();
		Lemma hopefullyLemma2 = LemmasByFrequency.poll();
		Lemma hopefullyLemma1 = LemmasByFrequency.poll();
		assertNull(LemmasByFrequency.poll());
		
		assertSame(database.allLemmas.get(Lemma1), hopefullyLemma1);
		assertSame(database.allLemmas.get(Lemma2), hopefullyLemma2);
		assertSame(database.allLemmas.get(Lemma3), hopefullyLemma3);
	}
	
	@Test
	public void testInitialDirectlyLearnableLemmasByFrequencyOrderedCorrectly() throws Exception {
		String Lemma1 = "flour"; //Occurs once.
		String Lemma2 = "egg"; //Occurs twice.
		String Lemma3 = "milk"; //Occurs thrice.
		String expectedSentence1 = Lemma1 + ".";
		String expectedSentence3 = Lemma2 + ".";
		String expectedSentence2 = Lemma3 + ".";
		String expectedSentence4 = Lemma2 + " " + Lemma3 + ".";
		String expectedSentence5 = Lemma3 + " " + "cake" + ".";
		String expectedParagraph = expectedSentence1 + " " + expectedSentence2 + " " + expectedSentence3 + " " + expectedSentence4 + " " + expectedSentence5;
		

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database, false);
		
		assertEquals(1, database.allTexts.size());
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedParagraph, parsedText.getRawText());
		assertSame(parsedText, returnedText);
		
		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		var sentencesByUnlearnedLemmaFrequency = learner.getDirectlyLearnableSentencesByFrequency(learnedLemmas);
		
		assertEquals(expectedSentence2, sentencesByUnlearnedLemmaFrequency.poll().getRawSentence());
		assertEquals(expectedSentence3, sentencesByUnlearnedLemmaFrequency.poll().getRawSentence());
		assertEquals(expectedSentence1, sentencesByUnlearnedLemmaFrequency.poll().getRawSentence());
		assertNull(sentencesByUnlearnedLemmaFrequency.poll());
	}

	

	@Test
	public void testSentencesAreScoredCorrectlyNoConjugations() throws Exception {
		String rawLemma1 = "one";
		String rawLemma2 = "two";
		String rawLemma3 = "three";
		String expectedSentence = rawLemma1 + " " + rawLemma2 + " " + rawLemma3 + ".";
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database, false);
		Sentence learnedSentence = database.allSentences.get(expectedSentence);
		//Initalially the lemas should give a score of 1 each, as that are their frequencies.
		assertEquals(1+1+1, learnedSentence.getScore(database, config), 0.001);
		
		Lemma lemma1 = database.allLemmas.get(rawLemma1);
		Lemma lemma2 = database.allLemmas.get(rawLemma2);
		Lemma lemma3 = database.allLemmas.get(rawLemma3);
		
		lemma1.incrementTimesLearned();
		assertEquals(0.25+1+1, learnedSentence.getScore(database, config), 0.001);
		lemma2.incrementTimesLearned();
		assertEquals(0.25+0.25+1, learnedSentence.getScore(database, config), 0.001);
		lemma3.incrementTimesLearned();
		assertEquals(0.25+0.25+0.25, learnedSentence.getScore(database, config), 0.001);
		

		lemma2.incrementTimesLearned();
		assertEquals(0.25+(1.0/16)+0.25, learnedSentence.getScore(database, config), 0.001);
	}
	
	

	@Test
	public void testSentenceIsDirectlyLearnable() throws Exception {
		String Lemma1 = "one";
		String Lemma2 = "two";
		String Lemma3 = "three";
		String expectedSentence = Lemma1 + " " + Lemma2 + " " + Lemma3 + ".";
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database, false);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedSentence);
		assertSame(parsedText, returnedText);
		
		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		Sentence sentence = database.allSentences.get(expectedSentence);
		
		assertFalse(sentence.isDirectlyLearnable(learnedLemmas, database));
		
		learnedLemmas.add(database.allLemmas.get(Lemma1));
		assertFalse(sentence.isDirectlyLearnable(learnedLemmas, database));
		
		learnedLemmas.add(database.allLemmas.get(Lemma2));
		assertTrue(sentence.isDirectlyLearnable(learnedLemmas, database));
		
		learnedLemmas.add(database.allLemmas.get(Lemma3));
		assertFalse(sentence.isDirectlyLearnable(learnedLemmas, database));
		
	}
	

	@Test
	public void testLemmasAreLearnedGreedily() throws Exception {
		//If it is done greedily, the invariant that there is no Lemma w2 learned after another Lemma w1,
		//Such that w1.frequency < w2.frequency, unless w1 participates in the sentence used to learn w2,
		//or w2 is learnt without a sentence.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse);
		database.initializeLemmas();
		List<Pair<Lemma, Sentence>> learningOrder = learner.learnAllLemmas();
		for (int i = 0; i < learningOrder.size() - 1; i++) {
			var currentLemma = learningOrder.get(i).getFirst();
			var currentSentence = learningOrder.get(i).getSecond();
			var nextLemma = learningOrder.get(i+1).getFirst();
			var nextSentence = learningOrder.get(i+1).getSecond();

			//An initial sentence is learned, with all the Lemmas in it. 
			//The Lemmas learned from this sentence should thus be skipped.
			if (currentSentence.equals(learningOrder.get(0).getSecond())) {
				continue;
			}
			
			if (!nextSentence.getRawSentence().equals(learner.NOT_A_SENTENCE_STRING) && !nextSentence.getLemmaSet(database).contains(currentLemma)) {
				assertTrue("Greedy invariant broken: Lemma " + nextLemma + " has a higher frequency (" + nextLemma.getFrequency() +  ") than " + currentLemma + " (" + currentLemma.getFrequency() +  ") but is learnt after the Lemma, for no reason.", nextLemma.getFrequency() <= currentLemma.getFrequency());
			}
		}
	}
	
	


	@Test
	public void testLemmasAreLearnedOneByOne() throws Exception {
		//A lemma must be learned one at a time, for it to be a valid learning order.
		//Thus each new sentence must contain exactly one new lemma.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		TestTool.parseText(fileToParse, database);
		List<Pair<Lemma, Sentence>> learningOrder = learner.learnAllLemmas();
		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		//Is notaword lemma.
		learnedLemmas.add(learningOrder.get(0).getFirst());
		//Skip the first lemma, as this is notaword.
		for (int i = 1; i < learningOrder.size() - 1; i++) {
			Lemma learnedLemma = learningOrder.get(i).getFirst();
			Sentence currentSentence = learningOrder.get(i).getSecond();
			//An initial sentence is learned, with all the Lemmas in it. 
			//The Lemmas learned from this sentence should thus be skipped.
			if (currentSentence.equals(learningOrder.get(1).getSecond())) {
				learnedLemmas.add(learnedLemma);
				continue;
			}
			assertFalse(learnedLemmas.contains(learnedLemma));
			for (Lemma LemmaInSentence : currentSentence.getLemmaSet(database)) {
				if (LemmaInSentence == learnedLemma)
					assertFalse(learnedLemmas.contains(LemmaInSentence));
				else {
					assertTrue("Sentence \"" + currentSentence + "\" contains lemma " + LemmaInSentence + " which haven't been learned yet.", learnedLemmas.contains(LemmaInSentence));				
				}
			}
			learnedLemmas.add(learnedLemma);
		}
	}
	
	
	

}
