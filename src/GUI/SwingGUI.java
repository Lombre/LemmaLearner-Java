package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Configurations.Configurations;
import Configurations.GuiConfigurations;
import LemmaLearner.ParsingProgressStruct;
import LemmaLearner.SortablePair;
import LemmaLearner.TextDatabase;
import TextDataStructures.Conjugation;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;
import net.miginfocom.swing.MigLayout;

public class SwingGUI implements ProgressPrinter {

	private Mediator mediator;
	private JFrame frame;
	private JPanel panel;
	private JTextField folderField;
	private JLabel progressLabel;
	private JTextField conjugationField;
	private JTextField lemmaField;
	private JProgressBar progressBar;
	private JList<String> learnedJList;
	private JList<String> sentenceChoisesJList;
	private JScrollPane scrollLearnedJList;
	private JScrollPane scrollSentenceChoisesJList;
	private JButton learnUntilStopButton;
	private MyCellRenderer renderer1;
	private MyCellRenderer renderer2;
	private MigLayout layout;
	ReentrantLock actionLock = new ReentrantLock(true);

	private JButton startLearningButton;

	private GuiConfigurations config;
    private AbstractButton learnNextLemmaButton;
    private JButton loadFolderButton;

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
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize(GuiConfigurations config) {
		this.config = config;
		if (config == null)
			throw new Error("Config must be given to the GUI before it is initialized.");
		
		frame = new JFrame("LemmaLearner");
		layout = new MigLayout("", "");
        panel = new JPanel(layout);
        
        setupLoadText(config);

        setupLearningButtons();

        setupLearnedListAndSentenceChoises();

        setupLemmatizationOptions();

        setupProgressBar();

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}

    private void setupLemmatizationOptions() {
        String conjugationText = "Conjugation:";
        JLabel jLabel = new JLabel(conjugationText);
        panel.add(jLabel, "split 3, sg conjugation");
        panel.add(new JLabel("Lemmatizations:"),"pushx, growx, sg lemmatization");
        panel.add(new JLabel(""), "wrap, sg changelemma");

        conjugationField = new JTextField("");
        conjugationField.setColumns(20);
        panel.add(conjugationField, "split 3, sg conjugation");

        lemmaField = new JTextField("");
        panel.add(lemmaField, "pushx, growx, sg lemmatization");

        var changeLemmatizations = new JButton("Change lemmatizations");
        changeLemmatizations.addActionListener(event -> changeLemmatization());
        //changeLemmatizations.addActionListener(event -> loadFilesInFolder());
        panel.add(changeLemmatizations, "span, wrap, sg changelemma");
    }

    private void changeLemmatization() {
        String rawConjugation = conjugationField.getText();
        String lemmaTextToParse = lemmaField.getText();
        String rawLemma = lemmaTextToParse.split(" ")[0];
        mediator.changeLemmatization(rawConjugation, rawLemma);
        displayAlternatives();
        updatePanelView();
	}

	private void setupProgressBar() {
        progressLabel = new JLabel("Not started yet.");
        panel.add(progressLabel, "center, wrap");

        progressBar = new JProgressBar();
        panel.add(progressBar, "growx");
    }

    private void setupLearnedListAndSentenceChoises() {
        panel.add(new JLabel("Learned sentences"), "split2, growx");
        panel.add(new JLabel("Sentence choises"), "growx, wrap");

        learnedJList = new JList<String>(new DefaultListModel<String>());
        renderer1 = new MyCellRenderer(160);
        learnedJList.setCellRenderer(renderer1);
        //learnedJList.setCellRenderer(new MyCellRenderer());
        //learnedJList.setBounds(100,100, 750,750);

        scrollLearnedJList = new JScrollPane(learnedJList);
        panel.add(scrollLearnedJList, "split2, pushy, grow, sg learningpanes");

        sentenceChoisesJList = new JList<String>(new DefaultListModel<String>());
        sentenceChoisesJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e){
                    int indexOfSelectedItem = sentenceChoisesJList.getSelectedIndex();
                    if (indexOfSelectedItem == -1) //Nothing is selected, for example seen if items are removed from the list
                        return;
                    Sentence sentenceSelected = sentenceAlternatives.get(indexOfSelectedItem); //e.getFirstIndex());
                    displayLemmatizationChoiseForSentence(sentenceSelected);
                }

				private void displayLemmatizationChoiseForSentence(Sentence sentenceSelected) {
					Conjugation conjugation = mediator.getUnlearnedConjugation(sentenceSelected);
                    Set<String> potentialLemmatizations = mediator.getPotentialLemmatizations(conjugation.getRawConjugation());
                    String potentialLemmatizationsString = "(" +  potentialLemmatizations.stream().reduce((x,y) -> x + ", " + y).get() + ")";
                    conjugationField.setText(conjugation.getRawConjugation());
                    lemmaField.setText(conjugation.getLemma() + " " + potentialLemmatizationsString);
				}
            });
        renderer2 = new MyCellRenderer(160);
        sentenceChoisesJList.setCellRenderer(renderer2);
        //sentenceChoisesJList.setCellRenderer(new MyCellRenderer());
        //sentenceChoisesJList.setBounds(100,100, 750,750);

        scrollSentenceChoisesJList = new JScrollPane(sentenceChoisesJList);
        panel.add(scrollSentenceChoisesJList,  "pushy, grow, sg learningpanes, wrap");
    }

    private void setupLearningButtons() {
        startLearningButton = new JButton("Start learning");
        startLearningButton.addActionListener(event -> startLearning());
        panel.add(startLearningButton, "center, span, wrap");
        startLearningButton.setEnabled(false);

        learnNextLemmaButton = new JButton("Learn next lemma");
        learnNextLemmaButton.addActionListener(event -> learnSelectedSentence());
        panel.add(learnNextLemmaButton, "center, span, wrap");
        learnNextLemmaButton.setEnabled(false);

        learnUntilStopButton = new JButton("Learn until stop");
        learnUntilStopButton.addActionListener(event -> learnUntilStop());
        panel.add(learnUntilStopButton, "center, span, wrap");
        learnUntilStopButton.setEnabled(false);
    }

    private void setupLoadText(GuiConfigurations config) {
        panel.add(new JLabel("Text folder:"), "split3");
        folderField = new JTextField("Texts/" + config.getLanguage()); // + "english/");
        panel.add(folderField, "pushx, growx");
        loadFolderButton = new JButton("Load files");

        loadFolderButton.addActionListener(event -> loadFilesInFolder());
        panel.add(loadFolderButton, "span, wrap");
    }

	private AtomicBoolean isCurrentlyLearning = new AtomicBoolean(false);
	private void learnUntilStop() {
		//The code is a little weird because i need to ensure that the lock works correctly.
		if (isCurrentlyLearning.compareAndSet(false, true)) {
			learnUntilStopButton.setText("Stop learning sentences");
			new Thread(() -> {
				actionLock.lock();
                learnNextLemmaButton.setEnabled(false);
				while (isCurrentlyLearning.get()) {
					learnActualSentence();
				}
                learnNextLemmaButton.setEnabled(true);
				actionLock.unlock();
			}).start();
		} else {
			isCurrentlyLearning.getAndSet(false);
			learnUntilStopButton.setText("Learn until stop");
		}
	}

	private void startLearning() {
		new Thread(() -> {
			actionLock.lock();
			startLearningButton.setEnabled(false);
			progressLabel.setText("Learning initial sentence." );
			panel.repaint();
			mediator.initializeLearning();
			displayAlternatives();
			frame.repaint();
            learnNextLemmaButton.setEnabled(true);
            learnUntilStopButton.setEnabled(true);
			actionLock.unlock();
		}).start();
	}

	private void loadFilesInFolder() {
		new Thread(() -> {
			actionLock.lock();
            loadFolderButton.setEnabled(false);
			progressLabel.setText("Learning new sentence." );
			panel.repaint();
			mediator.loadFilesInGivenFolder(folderField.getText());
            startLearningButton.setEnabled(true);
			progressLabel.setText("Finished learning new sentence." );
			startLearningButton.setEnabled(true);
			frame.repaint();
			actionLock.unlock();
		}).start();		
	}
	
	public void learnSelectedSentence() {
		new Thread(() -> {
			actionLock.lock();
            learnUntilStopButton.setEnabled(false);
            learnActualSentence();
			displayAlternatives();

            learnUntilStopButton.setEnabled(true);
			actionLock.unlock();
		}).start();
	}

    private void updatePanelView() {
        panel.revalidate();
        panel.repaint();
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
        updatePanelView();
	}

	// Must be temporarily stored so that they can be accessed in the list.
	List<Sentence> sentenceAlternatives;
	public void displayAlternatives() {
		var sentenceDescriptions = mediator.getAlternernativeSentencesDescription();
        sentenceAlternatives = sentenceDescriptions.stream().map(x -> x.sentence).collect(Collectors.toList());

		var displayList = ((DefaultListModel<String>) sentenceChoisesJList.getModel());
        displayList.clear();
        for (SentenceDescription description : sentenceDescriptions) {
            String element = description.getGUIDescription();
            displayList.addElement(element);
        }
        updatePanelView();
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
		progressLabel.setText("Finished adding texts to the database." );
		progressBar.setValue(0);
		panel.repaint();
	}

	@Override
	public void setMediator(Mediator mediator) {
		this.mediator = mediator;
	}


}

class MyCellRenderer extends DefaultListCellRenderer {
	   public static final String HTML_1 = "<html>";
	   public static final String HTML_2 = "";
	   public static final String HTML_3 = "</html>";
	   private int width;

	   public MyCellRenderer(int width) {
	      this.width = width;
	   }

	   @Override
	   public Component getListCellRendererComponent(JList list, Object value,
	         int index, boolean isSelected, boolean cellHasFocus) {
	      String text = HTML_1 + HTML_2 + value.toString()
	            + HTML_3;
	      return super.getListCellRendererComponent(list, text, index, isSelected,
	            cellHasFocus);
	   }
	   
	   public void setWidth(int width) {
		   this.width = width;
	   }

	}

