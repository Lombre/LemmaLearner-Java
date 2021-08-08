package Tests;

import org.junit.jupiter.api.BeforeAll;
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

import org.junit.*;

import LemmaLearner.*;



class TestLemmatizing {
	

	TextDatabase database;
	Text parsedText;
	GreedyLearner learner;
	Configurations config;
	WiktionaryDictionary dictionary = setUpDictionary();
	
	
	public WiktionaryDictionary setUpDictionary() {
		var dictionary = new WiktionaryDictionary();
		dictionary.load();
		return dictionary;
	}
	
	@BeforeEach
	public void setUp() {
		config = new Configurations();
		database = new TextDatabase(config);
		learner = new GreedyLearner(database, config);
	}
	

	@Test
	public void testOnlineDictionaryAcceptsActualNoun() throws Exception {
		String actualWord = "king";
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(actualWord, conjugation);
	}
	

	@Test
	public void testOnlineDictionaryAcceptsActualVerb() throws Exception {
		String actualWord = "kill";
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(actualWord, conjugation);
	}

	@Test
	public void testOnlineDictionaryHandlesNames() throws Exception {
		String actualWord = "lucas";
		String nonConjugatedWord = TextDatabase.NOT_A_WORD_STRING;
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(nonConjugatedWord, conjugation);
	}


	@Test
	public void testOnlineDictionaryHandlesNounConjugation() throws Exception {
		String actualWord = "kings";
		String nonConjugatedWord = "king";
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(nonConjugatedWord, conjugation);
	}
	

	@Test
	public void testHandleWeirdWords() throws Exception {
		String word = "was";
		String conjugation = dictionary.getLemmaFromConjugation(word);
		assertEquals(conjugation, word);
	}


	@Test
	public void testOnlineDictionaryRejectsNonWords() throws Exception {
		String actualWord = "thisisnotaword";
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(TextDatabase.NOT_A_WORD_STRING, conjugation);
	}	

}
