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



class TestLemmatizing {
	

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
	public void testOnlineDictionaryAcceptsActualNoun() throws Exception {
		String actualWord = "king";
		OnlineDictionary dictionary = new OnlineDictionary("English");
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(actualWord, conjugation);
	}
	

	@Test
	public void testOnlineDictionaryAcceptsActualVerb() throws Exception {
		String actualWord = "kill";
		OnlineDictionary dictionary = new OnlineDictionary("English");
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(actualWord, conjugation);
	}

	@Test
	public void testOnlineDictionaryHandlesNames() throws Exception {
		String actualWord = "lucas";
		String nonConjugatedWord = TextDatabase.NOT_A_WORD_STRING;
		OnlineDictionary dictionary = new OnlineDictionary("English");
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(nonConjugatedWord, conjugation);
	}


	@Test
	public void testOnlineDictionaryHandlesNounConjugation() throws Exception {
		String actualWord = "kings";
		String nonConjugatedWord = "king";
		OnlineDictionary dictionary = new OnlineDictionary("English");
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(nonConjugatedWord, conjugation);
	}



	@Test
	public void testOnlineDictionaryRejectsNonWords() throws Exception {
		String actualWord = "thisisnotaword";
		OnlineDictionary dictionary = new OnlineDictionary("English");
		String conjugation = dictionary.getLemmaFromConjugation(actualWord);
		assertEquals(TextDatabase.NOT_A_WORD_STRING, conjugation);
	}	

}
