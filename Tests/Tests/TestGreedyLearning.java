package Tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Configurations.Configurations;
import GUI.ConsoleGUI;
import LemmaLearner.BasicScorer;
import LemmaLearner.GreedyLearner;
import LemmaLearner.Learner;
import LemmaLearner.LearningElement;
import LemmaLearner.SentenceScorer;
import LemmaLearner.SortablePair;
import LemmaLearner.TextDatabase;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import TextDataStructures.Text;


class TestGreedyLearning {


	TextDatabase database;
	Text parsedText;
	GreedyLearner learner;
	Configurations config;

	@BeforeEach
	public void setUp() {
		config = new Configurations("Tests/test_config.txt");
		database = new TextDatabase(config);
		learner = new GreedyLearner(database, config, 1);
		ConsoleGUI gui = new ConsoleGUI();
		learner.progressPrinter = gui;
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

		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database);

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


		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedParagraph, database);

		assertEquals(1, database.allTexts.size());
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(expectedParagraph, parsedText.getRawText());
		assertSame(parsedText, returnedText);

		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		learner.initializeDataStructures();
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

		// We need to say that the config should not take conjugations into account
		TestTool.changeConfigField(config, "ShouldConjugationsBeScored", "false");
		TestTool.changeConfigField(config, "ShouldNegativelyScoreNonWords", "false");

		TestTool.parseStringAndAddToDatabase(expectedSentence, database);
		Sentence learnedSentence = database.allSentences.get(expectedSentence);
		//Initially the lemmas should give a score of 1 each, as that are their frequencies.
		SentenceScorer scorer = new BasicScorer(database, config);
		assertEquals((1+1+1)*(1+1+1)/Math.pow(4, 2), scorer.getScore(learnedSentence), 0.001);
		
		Lemma lemma1 = database.allLemmas.get(rawLemma1);
		Lemma lemma2 = database.allLemmas.get(rawLemma2);
		Lemma lemma3 = database.allLemmas.get(rawLemma3);
		
		lemma1.incrementTimesLearned();
		assertEquals((1+1)*(1+1+0.5)/Math.pow(4,1), scorer.getScore(learnedSentence), 0.001);
		lemma2.incrementTimesLearned();
		assertEquals((1)*(1 + 0.5 + 0.5)/Math.pow(4, 0), scorer.getScore(learnedSentence), 0.001);
		lemma2.incrementTimesLearned();
		assertEquals((1)*(1 + 0.25 + 0.5)/Math.pow(4, 0), scorer.getScore(learnedSentence), 0.001);

		lemma3.incrementTimesLearned();
		assertEquals(0, scorer.getScore(learnedSentence), 0.001);
	}
	
	

	@Test
	public void testSentenceIsDirectlyLearnable() throws Exception {
		String Lemma1 = "one";
		String Lemma2 = "two";
		String Lemma3 = "three";
		String expectedSentence = Lemma1 + " " + Lemma2 + " " + Lemma3 + ".";
		
		Text returnedText = TestTool.parseStringAndAddToDatabase(expectedSentence, database);
		
		assertEquals(database.allTexts.size(), 1);
		Text parsedText = database.allTexts.get(TestTool.testTextName);
		assertEquals(parsedText.getRawText(), expectedSentence);
		assertSame(parsedText, returnedText);

		var learner = new GreedyLearner(database, config, 2);
		Set<Lemma> learnedLemmas = learner.getLearnedLemmas();
		Sentence sentence = database.allSentences.get(expectedSentence);


		assertFalse(learner.isDirectlyLearnable(sentence));

		learnedLemmas.add(database.allLemmas.get(Lemma1));
		assertTrue(learner.isDirectlyLearnable(sentence));
		
		learnedLemmas.add(database.allLemmas.get(Lemma2));
		assertTrue(learner.isDirectlyLearnable(sentence));
		
		learnedLemmas.add(database.allLemmas.get(Lemma3));
		assertFalse(learner.isDirectlyLearnable(sentence));
		
	}
	

	@Test
	public void testLemmasAreLearnedGreedily() throws Exception {
		
		//This assumes no extra scoring for conjugations or learning lemmas again
		TestTool.changeConfigField(config, "ShouldConjugationsBeScored", "False");
		TestTool.changeConfigField(config, "MaxTimesLemmaShouldBeLearned", "1");
		TestTool.changeConfigField(config, "ShouldNegativelyScoreNonWords", "False");
		
		//If it is done greedily, the invariant that there is no Lemma w2 learned after another Lemma w1,
		//Such that w1.frequency < w2.frequency, unless w1 participates in the sentence used to learn w2,
		//or w2 is learnt without a sentence.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse, new ConsoleGUI());
		database.initializeLemmas();
		System.out.println(config);
		List<LearningElement> learningOrder = learner.learnAllLemmas();
		for (int i = 0; i < learningOrder.size() - 1; i++) {
			var currentLemma = learningOrder.get(i).getLemmasLearned().get(0);
			var currentSentence = learningOrder.get(i).getSentenceLearnedFrom();
			var nextLemma = learningOrder.get(i+1).getLemmasLearned().get(0);
			var nextSentence = learningOrder.get(i+1).getSentenceLearnedFrom();

			//An initial sentence is learned, with all the Lemmas in it. 
			//The Lemmas learned from this sentence should thus be skipped.
			if (currentSentence.equals(learningOrder.get(1).getSentenceLearnedFrom())) {
				continue;
			}
			
			if (!nextSentence.getRawSentence().equals(GreedyLearner.NOT_A_SENTENCE_STRING) && !nextSentence.getLemmaSet(database).contains(currentLemma)) {
				assertTrue("Greedy invariant broken: Lemma number " + i + " \"" + nextLemma + "\" has a higher frequency (" + nextLemma.getFrequency() +  ") than \"" + currentLemma + "\" (" + currentLemma.getFrequency() +  ") but is learnt after the Lemma, for no reason.", nextLemma.getFrequency() <= currentLemma.getFrequency());
			}
		}
	}
	

	@Test
	public void testLemmasAreLearnedOneByOne() throws Exception {
		//A lemma must be learned one at a time, for it to be a valid learning order.
		//Thus each new sentence must contain exactly one new lemma.
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse, new ConsoleGUI());
		database.initializeLemmas();
		List<LearningElement> learningOrder = learner.learnAllLemmas();
		assertTrue(100 < learningOrder.size());
		Set<Lemma> learnedLemmas = new HashSet<Lemma>();
		System.out.println(config);
		//Is notaword lemma.
		learnedLemmas.addAll(learningOrder.get(0).getLemmasLearned());
		//Skip the first lemma, as this is notaword.
		for (int i = 1; i < learningOrder.size() - 1; i++) {
			List<Lemma> currentLemmas = learningOrder.get(i).getLemmasLearned();
			Sentence currentSentence = learningOrder.get(i).getSentenceLearnedFrom();
			assertFalse(learnedLemmas.contains(currentLemmas));
			//An initial sentence is learned, with all the Lemmas in it.
			//The Lemmas learned from this sentence should thus be skipped.
			if (currentSentence.equals(learningOrder.get(1).getSentenceLearnedFrom())) {
				learnedLemmas.addAll(currentLemmas);
				continue;
			}
			for (Lemma lemmaInSentence : currentSentence.getLemmaSet(database)) {
				if (currentLemmas.contains(lemmaInSentence))
					assertFalse(learnedLemmas.contains(lemmaInSentence));
				else {
					assertTrue("Sentence \"" + currentSentence + "\" contains lemma " + lemmaInSentence + " which haven't been learned yet.", learnedLemmas.contains(lemmaInSentence));
				}
			}
			learnedLemmas.addAll(currentLemmas);
		}
	}
	

	@Test
	public void  testSentenceAlternativesAreInCorrectOrder() throws Exception {
		//A lemma must be learned one at a time, for it to be a valid learning order.
		//Thus each new sentence must contain exactly one new lemma.
		GreedyLearner newLearner = new GreedyLearner(database, config, 2);
		newLearner.progressPrinter = learner.progressPrinter;
		File fileToParse = new File("Test texts/Adventures of Sherlock Holmes, The - Arthur Conan Doyle.txt");
		database.parseTextAndAddToDatabase(fileToParse, new ConsoleGUI());
		database.initializeLemmas();
		newLearner.initializeForLearning();
		//newLearner.getNBestScoringSentencesWithPutBack(10000000);
		for (int i = 0; i < 100; i++) {
			var alternatives = newLearner.getNBestScoringSentencesWithPutBack(1000);
			int k = 1;
			for (int j = 0; j < Math.max(alternatives.size() - 1, 10); j++){
				assertTrue("At i=" + i + " alternative " + j + ": "  + alternatives.get(j) + " has a smaller score than alterntative " + (j+1) + ": " + alternatives.get(j+1), alternatives.get(j).getSecondValue() >= alternatives.get(j + 1).getSecondValue());
			}
			newLearner.learnNextLemma();
		}
	}
	
	
	

}
