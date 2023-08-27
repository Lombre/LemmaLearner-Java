package LemmaLearner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import Configurations.LearningConfigurations;
import GUI.ProgressPrinter;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public interface Learner {

	public List<LearningElement> learnAllLemmas();

	public void learnNextLemma();

	public void initializeForLearning();

	public void initializeDataStructures();

	public ArrayList<Sentence> getNBestScoringSentencesWithPutBack(int n);

	public void learnLemmasInSentence(Sentence sentence);

	public Lemma learnLemmaFromDirectlyLearnableSentence(Sentence directlyLearnableSentence);

	public void resetLearning();

	public Set<Lemma> getLearnedLemmas();

	public List<LearningElement> getLearningList();

	public void reloadSentencesAssociatedWithLemma(Lemma lemma);

	public void updateLearningWithNewLemmatization(Lemma oldLemma, Lemma newLemma);

    public void setProgressPrinter(ProgressPrinter progressPrinter);

}
