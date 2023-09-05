package LemmaLearner;

import java.util.Set;

import Configurations.LearningConfigurations;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;


public interface SentenceScorer {


    public double getScore(Sentence sentence);

}
