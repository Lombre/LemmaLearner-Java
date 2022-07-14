package GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import Configurations.Configurations;
import LemmaLearner.ParsingProgressStruct;
import LemmaLearner.SortablePair;
import LemmaLearner.TextDatabase;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import net.miginfocom.swing.MigLayout;

public class SwingGUI implements ProgressPrinter {
	
	private Mediator mediator;
	private JFrame frame;
	private JPanel panel;
	private JTextField folderField;
	private JLabel progressLabel;
	private JProgressBar progressBar;
	private JList<String> learnedJList;
	private JList<String> sentenceChoisesJList;
	private JButton learnUntilStopButton;
	
	ReentrantLock actionLock = new ReentrantLock(true);
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//new Mediator();
	}

	/**
	 * Create the application.
	 */
	public SwingGUI() {
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("LemmaLearner");
		
        panel = new JPanel(new MigLayout("", ""));
        
        panel.add(new JLabel("TextFolder:"), "split3");
        folderField = new JTextField("Texts/" + "english/");
        panel.add(folderField, "pushx, growx");
        var loadFolderButton = new JButton("Load files");
        Thread thread = new Thread(() -> System.out.println("kage"));
        thread.run();
        loadFolderButton.addActionListener(event -> loadFilesInFolder());
        panel.add(loadFolderButton, "span, wrap");
        
        var startLearningButton = new JButton("Start learning");
        startLearningButton.addActionListener(event -> startLearning());
        panel.add(startLearningButton, "center, span, wrap");
        
        var learnNextLemmaButton = new JButton("Learn next lemma");
        learnNextLemmaButton.addActionListener(event -> learnSelectedSentence());
        panel.add(learnNextLemmaButton, "center, span, wrap");
        

        learnUntilStopButton = new JButton("Learn until stop");
        learnUntilStopButton.addActionListener(event -> learnUntilStop());
        panel.add(learnUntilStopButton, "center, span, wrap");
        
        panel.add(new JLabel("Learned sentences"), "split2, growx");
        panel.add(new JLabel("Sentence choises"), "growx, wrap");
        
        
        
        learnedJList = new JList<String>(new DefaultListModel<String>());
        learnedJList.setBounds(100,100, 75,75);
        
        panel.add(new JScrollPane(learnedJList), "split2, growx, pushy, growy");
        

        sentenceChoisesJList = new JList<String>(new DefaultListModel<String>());
        sentenceChoisesJList.setBounds(100,100, 75,75);
        
        panel.add(new JScrollPane(sentenceChoisesJList), "growx, growy, wrap");
        
        
        progressLabel = new JLabel("Kage er godt");
        panel.add(progressLabel, "center, wrap");
        
        progressBar = new JProgressBar();
        panel.add(progressBar, "growx");        
        
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}

	private AtomicBoolean isCurrentlyLearning = new AtomicBoolean(false);
	private void learnUntilStop() {
		//The code is a little weird because i need to ensure that the lock works correctly.
		if (!isCurrentlyLearning.get()) {
			isCurrentlyLearning.set(true);
			learnUntilStopButton.setText("Stop learning sentences");
			new Thread(() -> {
				actionLock.lock();
				while (isCurrentlyLearning.get()) {
					learnActualSentence();
				}
				actionLock.unlock();
			}).start();
		} else {
			isCurrentlyLearning.set(false);
			learnUntilStopButton.setText("Learn until stop");
		}
	}

	private void startLearning() {
		new Thread(() -> {
			actionLock.lock();
			mediator.initializeLearning();
			actionLock.unlock();
		}).start();
	}

	private void loadFilesInFolder() {
		new Thread(() -> {
			actionLock.lock();
			mediator.loadFilesInGivenFolder(folderField.getText()); 
			actionLock.unlock();
		}).start();		
	}
	
	
	public void learnSelectedSentence() {
		new Thread(() -> {
			actionLock.lock();
			learnActualSentence();
			actionLock.unlock();
		}).start();
	}

	private void learnActualSentence() {
		int selectedItemIndex = sentenceChoisesJList.getSelectedIndex();
		if (selectedItemIndex != -1) {
			mediator.learnLemmaInSentence(sentenceAlternatives.get(selectedItemIndex));
		} else {
			mediator.learnNextLemma();			
		}
	}

	@Override
	public void beginParsingTexts(int numberOfTexts, String folderLocation) {
		new Thread(() -> progressLabel.setText("Begin loading " + numberOfTexts + " texts in folder")).start();
		progressBar.setMaximum(numberOfTexts);
		panel.repaint();
	}

	@Override
	public void printProgressInParsingTexts(File textFile, ParsingProgressStruct progressReporter) {
		incrementProgressBar();
	}

	@Override
	public void printLearnedLemma(List<SortablePair<Lemma, Sentence>> orderOfLearnedLemmas, TextDatabase database) {
		var learnedPair = orderOfLearnedLemmas.get(orderOfLearnedLemmas.size() - 1);
		Lemma learnedLemma = learnedPair.getFirst();
		Sentence learnedSentence = learnedPair.getSecond();
		var list = ((DefaultListModel<String>) learnedJList.getModel());
		list.addElement((list.size() + 1) + ", " + learnedLemma.getRawLemma() + " -> " + learnedSentence.getRawSentence());
	}

	ArrayList<Sentence> sentenceAlternatives;
	@Override
	public void displayAlternatives(ArrayList<Sentence> alternatives, Set<Lemma> learnedLemmas, Configurations config, TextDatabase database) {
		this.sentenceAlternatives = alternatives;
		var displayList = ((DefaultListModel<String>) sentenceChoisesJList.getModel());
		displayList.clear();
		for (Sentence sentence : alternatives) {
			displayList.addElement(sentence.getUnlearnedLemmas(learnedLemmas, database) + ", " + String.format("%.2f", sentence.getScore(database, config)) + " -> " + sentence.getLemmatizedRawSentence(database));
		}
	}

	@Override
	public void beginAddTextsToDatabase(int numberOfTexts) {
		progressLabel.setText("Begin adding " + numberOfTexts + " texts to database");
		progressBar.setMaximum(numberOfTexts);
		progressBar.setValue(0);
		panel.repaint();
	}

	@Override
	public void printAddedTextToDatabase() {
		incrementProgressBar();
		
	}
	
	private void incrementProgressBar() {
		new Thread(() -> {progressBar.setValue(progressBar.getValue() + 1); 
						  panel.repaint();}).start();
	}

	@Override
	public void printFinishedAddingTexts() {
		progressLabel.setText("Finished adding texts to database." );
		progressBar.setValue(0);
		panel.repaint();
	}

	@Override
	public void setMediator(Mediator mediator) {
		this.mediator = mediator;
	}

}
