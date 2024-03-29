package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Configurations.Configurations;

import static org.junit.Assert.*;

import java.util.*;


import LemmaLearner.*;
import TextDataStructures.Sentence;
import TextDataStructures.Text;



class TestNewPriorityQueue {
	

	TextDatabase database;
	Text parsedText;
	GreedyLearner learner;
	TreePriorityQueue<Sentence> queue;
	Configurations config;
	
	@BeforeEach
	public void setUp() {
		config = new Configurations();
		database = new TextDatabase(config);
		learner = new GreedyLearner(database, config, 1);
		queue = learner.getSentencePriorityQueue();
		
	}
	

	@Test
	public void testAddSingleToEmptyQueue() throws Exception {
		Sentence testSentence = new Sentence("This is a test.", new ArrayList<String>());
		queue.add(testSentence, 10.0);
		assertEquals(1, queue.size());
		Sentence returnedSentence = queue.poll();
		assertSame(testSentence, returnedSentence);
		assertEquals(0, queue.size());
	}
	

	@Test
	public void testDequeElementsInSortedOrder() throws Exception {
		Sentence testSentence1 = new Sentence("This is a test1.", new ArrayList<String>());
		Sentence testSentence2 = new Sentence("This is a test2.", new ArrayList<String>());
		Sentence testSentence3 = new Sentence("This is a test3.", new ArrayList<String>());
		queue.add(testSentence1, 10.0);
		queue.add(testSentence2, 0.0);
		queue.add(testSentence3, 10.5);
		assertEquals(3, queue.size());
		Sentence returnedSentence1 = queue.poll();
		Sentence returnedSentence2 = queue.poll();
		Sentence returnedSentence3 = queue.poll();
		assertSame(testSentence3, returnedSentence1);
		assertSame(testSentence1, returnedSentence2);
		assertSame(testSentence2, returnedSentence3);
		assertEquals(0, queue.size());
	}
	

	@Test
	public void testHandlesElementsWithSamePriority() throws Exception {
		Sentence testSentence1 = new Sentence("This is a test1.", new ArrayList<String>());
		Sentence testSentence2 = new Sentence("This is a test2.", new ArrayList<String>());
		Sentence testSentence3 = new Sentence("This is a test3.", new ArrayList<String>());
		queue.add(testSentence1, 10.0);
		queue.add(testSentence2, 10.0);
		queue.add(testSentence3, 10.0);
		assertEquals(3, queue.size());
		Sentence returnedSentence1 = queue.poll();
		Sentence returnedSentence2 = queue.poll();
		Sentence returnedSentence3 = queue.poll();
		assertSame(testSentence1, returnedSentence1);
		assertSame(testSentence2, returnedSentence2);
		assertSame(testSentence3, returnedSentence3);
		assertEquals(0, queue.size());
	}
	
	
	
	

}
