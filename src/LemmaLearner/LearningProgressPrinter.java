package LemmaLearner;

import java.util.*;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;


public class LearningProgressPrinter {

	public static void printLemmasWithHighNumberOfConjugations(TextDatabase database) {
		if (false) {
			
			for (Lemma lemma : database.allLemmas.values()) {
				if (10 <= lemma.getConjugations().size()) {
					System.out.println("Lemma \"" + lemma + "\" has " + lemma.getConjugations().size() + " conjugations.");
					for (Conjugation conjugation : lemma.getConjugations()) {
						System.out.println(lemma + " -> " + conjugation);
					}
					System.out.println();
				}
			}			
		}
	}

	public static void printNumberOfTimesConjugationsHaveBeenLearned(Set<Lemma> learnedLemmas) {
		List<Conjugation> learnedConjugations = learnedLemmas.stream().flatMap(lemma -> lemma.getConjugations().stream()).collect(Collectors.toList());
		System.out.println("There are a total of " + learnedConjugations.size() + " conjugations associated with the learned lemmas.");
		HashMap<Integer, List<Conjugation>> timesConjugationsHaveBeenLearned = new HashMap<Integer, List<Conjugation>>();
		//NotAWord is not included in this
		for (Conjugation conjugation : learnedConjugations) {
			if (conjugation.getLemma().getRawLemma().equals(TextDatabase.NOT_A_WORD_STRING.toLowerCase())) {
				continue;
			} else if (timesConjugationsHaveBeenLearned.containsKey(conjugation.getTimesLearned())) {
				timesConjugationsHaveBeenLearned.get(conjugation.getTimesLearned()).add(conjugation);
			} else {
				var listLemma = new ArrayList<Conjugation>();
				listLemma.add(conjugation);
				timesConjugationsHaveBeenLearned.put(conjugation.getTimesLearned(), listLemma);
			}
		}
		
		int i = 0;
		int sumNumberOfLemmas = 0;
		for (; i <= 10; i++) {
			if (timesConjugationsHaveBeenLearned.containsKey(i)) {
				System.out.println("Number of conjugations that have been learned " + i + " times: " + timesConjugationsHaveBeenLearned.get(i).size());	
				sumNumberOfLemmas += timesConjugationsHaveBeenLearned.get(i).size();
			}
		}
				
		System.out.println("Number of conjugations that have been learned more than " + (i-1) + " times: " + (learnedConjugations.size() - sumNumberOfLemmas));
	}

	static void printNumberOfTimesLemmasHaveBeenLearned(TextDatabase database) {
		HashMap<Integer, List<Lemma>> timesLemmasHaveBeenLearned = new HashMap<Integer, List<Lemma>>();
		for (Lemma lemma : database.allLemmas.values()) {
			if (timesLemmasHaveBeenLearned.containsKey(lemma.getTimesLearned())) {
				timesLemmasHaveBeenLearned.get(lemma.getTimesLearned()).add(lemma);
			} else {
				var listLemma = new ArrayList<Lemma>();
				listLemma.add(lemma);
				timesLemmasHaveBeenLearned.put(lemma.getTimesLearned(), listLemma);
			}
		}
		
		int i = 0;
		int sumNumberOfLemmas = 0;
		for (; i <= 10; i++) {
			if (timesLemmasHaveBeenLearned.containsKey(i)) {
				System.out.println("Number of lemmas that have been learned " + i + " times: " + timesLemmasHaveBeenLearned.get(i).size());	
				sumNumberOfLemmas += timesLemmasHaveBeenLearned.get(i).size();
			}
		}
				
		System.out.println("Number of lemmas that have been learned more than " + (i-1) + " times: " + (database.allLemmas.size() - sumNumberOfLemmas));
		System.out.println();
	}

	public static void printFinishedLearningLemmasInformation(long absoluteStartTime, List<Pair<Lemma, Sentence>> learningOrder, Set<Lemma> learnedLemmas, TextDatabase database) {
		
		List<Lemma> lemmasLearnedFromSentences = learningOrder.stream()
															.filter(pair -> !pair.getSecond().getRawSentence().equals(GreedyLearner.NOT_A_SENTENCE_STRING))
															.map(pair -> pair.getFirst())
															.collect(Collectors.toList());
		
		
		System.out.println("Number of lemmas learned from sentences: " + lemmasLearnedFromSentences.size() + " of " + database.allLemmas.size());
	
		long absoluteEndTime = System.currentTimeMillis();	
		float absoluteTimeUsed = ((float) (absoluteEndTime - absoluteStartTime))/1000; //In minutes		
		System.out.println("Learned all words in " + absoluteTimeUsed + " seconds.");	
		
		printNumberOfTimesLemmasHaveBeenLearned(database);
		printNumberOfTimesConjugationsHaveBeenLearned(learnedLemmas);				
		printerNumberOfIgnorableSentences(database);		
		//printerNumberOfTimesLemmasHaveBeenLearned(learningOrder);
	}

	private static void printerNumberOfTimesLemmasHaveBeenLearned(List<Pair<Lemma, Sentence>> learningOrder) {
		for (int i = 0; i < learningOrder.size(); i++) {
			Lemma lemma = learningOrder.get(i).getFirst();
			System.out.println((i+1) + ") " + lemma + " -> " + lemma.getTimesLearned());
		}
	}

	private static void printerNumberOfIgnorableSentences(TextDatabase database) {
		//A test of methods to filter out redundant sentences, to minimize computation time.
		
		HashSet<Pair<Lemma, Lemma>> seenLemmaPairs = new HashSet<Pair<Lemma, Lemma>>();
		HashMap<Lemma, Integer> timesLemmaSeen = new HashMap<Lemma, Integer>();
		database.allLemmas.values().stream().forEach(lemma -> timesLemmaSeen.put(lemma, 0));
		int ignorableSentences = 0;
		List<Sentence> ignoredSentences = new ArrayList<Sentence>();
		
		int minTimesToLearn = 20;
		int lowFrequency = 100;
		for (Sentence sentence : database.allSentences.values()) {
			List<Lemma> sentenceLemmas = new ArrayList<Lemma>(sentence.getLemmaSet(database));
			var uncommonLemmas = sentenceLemmas.stream().filter(lemma -> lemma.getFrequency() < lowFrequency).collect(Collectors.toList());
			
			
			boolean hasNonRedundantPair = !containOnlyRedundantLemmaPairs(seenLemmaPairs, sentenceLemmas);
			boolean containLemmaSeenFewTimes = sentenceLemmas.stream().anyMatch(x -> timesLemmaSeen.get(x) < minTimesToLearn);
			boolean containsLowFrequencyNonRedundantPair = !containOnlyRedundantLemmaPairs(seenLemmaPairs, uncommonLemmas);
			
			//System.out.println(sentence);
			if ((hasNonRedundantPair && containLemmaSeenFewTimes) || containsLowFrequencyNonRedundantPair) {
				addPairsOfLemmasToSeenLemmaPairs(seenLemmaPairs, sentenceLemmas);
				sentenceLemmas.forEach(lemma -> timesLemmaSeen.put(lemma, timesLemmaSeen.get(lemma) + 1));
			} else {
				ignorableSentences++;
				ignoredSentences.add(sentence);
			}
		}
		System.out.println("Ignorable sentences: " + ignorableSentences + " out of " + database.allSentences.size() + " (" + (100.0f*ignorableSentences/database.allSentences.size()) + "%).");
			
	}
	


	private static boolean containOnlyRedundantLemmaPairs(HashSet<Pair<Lemma, Lemma>> seenLemmaPairs, List<Lemma> sentenceLemmas) {
		for (int i = 0; i < sentenceLemmas.size(); i++) {
			for (int j = i + 1; j < sentenceLemmas.size(); j++) {
				var lemmaPair1 = new Pair<Lemma, Lemma>(sentenceLemmas.get(i), sentenceLemmas.get(j));
				//var lemmaPair2 = new Pair<Lemma, Lemma>(sentenceLemmas.get(j), sentenceLemmas.get(i));
				if (!seenLemmaPairs.contains(lemmaPair1)) {
					return false;
				}
			}
		}
		return true;
	}
	

	private static void addPairsOfLemmasToSeenLemmaPairs(HashSet<Pair<Lemma, Lemma>> seenLemmaPairs, List<Lemma> sentenceLemmas) {
		for (int i = 0; i < sentenceLemmas.size(); i++) {
			for (int j = i + 1; j < sentenceLemmas.size(); j++) {
				var lemmaPair1 = new Pair<Lemma, Lemma>(sentenceLemmas.get(i), sentenceLemmas.get(j));
				var lemmaPair2 = new Pair<Lemma, Lemma>(sentenceLemmas.get(j), sentenceLemmas.get(i));
				if (!seenLemmaPairs.contains(lemmaPair1)) {
					seenLemmaPairs.add(lemmaPair1);
					seenLemmaPairs.add(lemmaPair2);
				}
			}
		}
	}


	public static void printLearnedInformation(List<Pair<Lemma, Sentence>> learningOrder, TextDatabase database) {
		var learnedWordSentencePair = learningOrder.get(learningOrder.size() - 1);		
		if (learningOrder.size() <= 1000 || (learningOrder.size()) % 1 == 0) {
			Paragraph paragraph = learnedWordSentencePair.getSecond().getAParagraph();
			String rawParagraph;
			if (paragraph != null) {
				rawParagraph = paragraph.getRawParagraph();
			} else {
				rawParagraph = "No paragraph.";
			}
			System.out.println((learningOrder.size()) + ", " +  
								learnedWordSentencePair.getFirst() + ", " + 
								learnedWordSentencePair.getFirst().getFrequency() + ": " + 
								learnedWordSentencePair.getSecond());
			System.out.println(learnedWordSentencePair.getSecond().getLemmatizedRawSentence(database));
			//System.out.println(rawParagraph);
			System.out.println();
			
		}
	}
	
	
}
