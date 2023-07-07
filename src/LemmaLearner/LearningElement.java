package LemmaLearner;

import java.util.List;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

public class LearningElement{

    private final List<Lemma> lemmasLearned; //Typically 1
    private final Sentence sentenceLearnedFrom;

    public LearningElement(List<Lemma> lemmasLearned, Sentence sentenceLearnedFrom){
        this.lemmasLearned = lemmasLearned;
        this.sentenceLearnedFrom = sentenceLearnedFrom;
    }

	public List<Lemma> getLemmasLearned() {
		return lemmasLearned;
	}

	public Sentence getSentenceLearnedFrom() {
		return sentenceLearnedFrom;
	}

    public String getRawLemmasString(){
        return lemmasLearned.stream().map(lemma -> lemma.getRawLemma()).reduce((x,y) -> x + ", " + y).get();
    }


}
