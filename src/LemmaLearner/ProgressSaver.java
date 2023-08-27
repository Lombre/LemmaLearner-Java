package LemmaLearner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import Configurations.Configurations;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import TextDataStructures.Text;

public class ProgressSaver{

	private static final String PROGRESS_LEMMA_SENTENCE_SEPERATOR = ">-->";
    private final ManualParser parser;

    public ProgressSaver(Configurations config){
        parser = new ManualParser(config);
    }

	public static void saveProgressToFile(List<LearningElement> learnedLemmasAndSentences, String saveFilePath) {
		File savedProgressFile = new File(saveFilePath);
		try {
			savedProgressFile.createNewFile();
			OutputStream os = new FileOutputStream(savedProgressFile);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
			for (LearningElement element : learnedLemmasAndSentences) {
				writer.println(element.getRawLemmasString() + PROGRESS_LEMMA_SENTENCE_SEPERATOR + element.getSentenceLearnedFrom().getRawSentence());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public List<LearningElement> loadProgressFromFile(String saveFilePath, TextDatabase database) {
		Path savedProgressFile = Paths.get(saveFilePath);
		try {
			String rawProgressFile = Files.readString(savedProgressFile, StandardCharsets.UTF_8);
            var lines = Arrays.asList(rawProgressFile.split("\n"));
            var loadedProgress = new ArrayList<LearningElement>();
            for (String line : lines) {
                var splitLine = line.split(PROGRESS_LEMMA_SENTENCE_SEPERATOR);
                List<Lemma> learnedLemmasInSentence = Arrays.asList(splitLine[0].split(",")).stream()
                    .map(rawLemma -> database.allLemmas.get(rawLemma))
                    .collect(Collectors.toList());
                var sentenceAsParagraph = this.parser.getParagraphFromRawParagraph(splitLine[1], "saved_progress");
                var sentence = sentenceAsParagraph.getSentences().iterator().next();
                loadedProgress.add(new LearningElement(learnedLemmasInSentence, sentence));
            }
            return loadedProgress;
		} catch (IOException e) {
			e.printStackTrace();
            return null;
		}
	}

}
