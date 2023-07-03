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
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultStyledDocument;

import Configurations.Configurations;
import Configurations.GuiConfigurations;
import Configurations.LearningConfigurations;
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
    private AbstractButton alreadyKnowLemmaButton;
    private JButton loadFolderButton;
    private JButton loadProgressButton;
    private JButton saveProgressButton;
    private JLabel sentenceContextLabel;
    private JLabel lemmaDefinitionLabel;
    private JLabel sentenceLemmatization;
    private JEditorPane sentenceContextArea;
    private JEditorPane lemmaDefinitionArea;

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

        setupDefinitionTextField();

        setupContextTextField();

        setupLemmatizationOptions();

        setupProgressBar();

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}

    private void setupDefinitionTextField() {
        lemmaDefinitionLabel = new JLabel("Lemma definition:");
        panel.add(lemmaDefinitionLabel, "wrap");
        lemmaDefinitionArea = new JTextPane();
        lemmaDefinitionArea.setEditable(false);
        lemmaDefinitionArea.setContentType("text/html");
        lemmaDefinitionArea.setText("<html>No lemma selected.</html>");
        lemmaDefinitionArea.updateUI();
        panel.add(lemmaDefinitionArea, "growx 50, wrap"); // A lower priority for the growth is needed.
    }

    private void setupContextTextField() {
        sentenceContextLabel = new JLabel("Sentence context:");
        panel.add(sentenceContextLabel, "wrap");
        sentenceContextArea = new JTextPane();
        sentenceContextArea.setEditable(false);
        sentenceContextArea.setContentType("text/html");
        sentenceContextArea.setText("<html>No sentence selected.</html>");
        sentenceContextArea.updateUI();
        panel.add(sentenceContextArea, "growx 50, wrap"); // A lower priority for the growth is needed.
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
        //updatePanelView();
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
        learnedJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e){
                    int indexOfSelectedItem = learnedJList.getSelectedIndex();
                    if (indexOfSelectedItem == -1) //Nothing is selected, for example seen if items are removed from the list
                        return;
                    var selectedLemma    = orderOfLearnedLemmas.get(indexOfSelectedItem).getFirst();
                    var selectedSentence = orderOfLearnedLemmas.get(indexOfSelectedItem).getSecond();
                    //displayLemmatizationChoiseForSentence(selectedSentence);
                    displaySentenceContext(selectedSentence);
                    displayLemmaDefinition(selectedLemma);
                    displaySentenceLemmatization(selectedSentence);
                }
            });
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
                    Sentence selectedSentence = sentenceAlternatives.get(indexOfSelectedItem); //e.getFirstIndex());
                    displayLemmatizationChoiseForSentence(selectedSentence);
                    displaySentenceContext(selectedSentence);
                    displayLemmaDefinition(mediator.getUnlearnedConjugation(selectedSentence).getLemma());
                    displaySentenceLemmatization(selectedSentence);
                }


            });
        renderer2 = new MyCellRenderer(160);
        sentenceChoisesJList.setCellRenderer(renderer2);
        scrollSentenceChoisesJList = new JScrollPane(sentenceChoisesJList);
        panel.add(scrollSentenceChoisesJList,  "pushy, grow, sg learningpanes, wrap");


        sentenceLemmatization = new JLabel("No sentence chosen.");
        panel.add(sentenceLemmatization, "center, wrap");
    }

    private void displaySentenceLemmatization(Sentence selectedSentence) {
        sentenceLemmatization.setText(mediator.getSentenceLemmatization(selectedSentence));
    }

    private void displayLemmatizationChoiseForSentence(Sentence sentenceSelected) {
		Conjugation conjugation = mediator.getUnlearnedConjugation(sentenceSelected);
        Set<String> potentialLemmatizations = mediator.getPotentialLemmatizations(conjugation.getRawConjugation());
        String potentialLemmatizationsString = "(" +  potentialLemmatizations.stream().reduce((x,y) -> x + ", " + y).get() + ")";
        conjugationField.setText(conjugation.getRawConjugation());
        lemmaField.setText(conjugation.getLemma() + " " + potentialLemmatizationsString);
	}

    private void displayLemmaDefinition(Lemma unlearnedLemma) {
        String textToSet = "";
        var definitions = unlearnedLemma.getDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            textToSet += (i+1) + ") " + definitions.get(i) + "\n";
        }
        if (1500 < textToSet.length()){
            textToSet = "Paragraph to long (" + textToSet.length() + " charachters).";
        }
        lemmaDefinitionArea.setText(textToSet);
        lemmaDefinitionLabel.setText(unlearnedLemma.getRawLemma());
    }


    private void displaySentenceContext(Sentence selectedSentence) {
        var originatingParagraph = selectedSentence.getAParagraph();
        var paragraphText = originatingParagraph.getRawParagraph();
        String textToSet = originatingParagraph.getRawParagraph();
        int indexOfSentence = paragraphText.toLowerCase().indexOf(selectedSentence.getRawSentence().toLowerCase());
        if (1500 < textToSet.length()){
            textToSet = "Paragraph to long (" + textToSet.length() + " charachters).";
        } else if (indexOfSentence != -1){
            String startParagraph = "<html>" + paragraphText.substring(0, indexOfSentence);
            int endOfSentence = indexOfSentence + selectedSentence.getRawSentence().length();
            String highlightedSentence = "<span style=\"color: red\">" + paragraphText.substring(indexOfSentence, endOfSentence) + "</span>";
            String endParagraph = paragraphText.substring(endOfSentence) + "</html>";
            textToSet = startParagraph + highlightedSentence + endParagraph;
        }
        sentenceContextArea.setText(textToSet);
        sentenceContextLabel.setText("From text \"" + originatingParagraph.getOriginText().getName() + "\"");
    }

    private void setupLearningButtons() {

        loadProgressButton = new JButton("Load progress");
        loadProgressButton.addActionListener(event -> loadProgress());
        panel.add(loadProgressButton, "split2, center, sg learningbuttons");
        loadProgressButton.setEnabled(false);

        saveProgressButton = new JButton("Save progress");
        saveProgressButton.addActionListener(event -> saveProgress());
        panel.add(saveProgressButton, "center, sg learningbuttons, wrap");
        saveProgressButton.setEnabled(false);

        startLearningButton = new JButton("Start learning");
        startLearningButton.addActionListener(event -> startLearning());
        panel.add(startLearningButton, "split4, center, sg learningbuttons");
        startLearningButton.setEnabled(false);

        learnNextLemmaButton = new JButton("Learn next lemma");
        learnNextLemmaButton.addActionListener(event -> learnSelectedSentence());
        panel.add(learnNextLemmaButton, "sg learningbuttons");
        learnNextLemmaButton.setEnabled(false);


        alreadyKnowLemmaButton = new JButton("Already know lemma");
        alreadyKnowLemmaButton.addActionListener(event -> alreadyKnowLemma());
        panel.add(alreadyKnowLemmaButton, "sg learningbuttons");
        alreadyKnowLemmaButton.setEnabled(false);


        learnUntilStopButton = new JButton("Learn until stop");
        learnUntilStopButton.addActionListener(event -> learnUntilStop());
        panel.add(learnUntilStopButton, "sg learningbuttons, wrap");
        learnUntilStopButton.setEnabled(false);
    }

    private void alreadyKnowLemma() {
		int selectedItemIndex = sentenceChoisesJList.getSelectedIndex();
		if (selectedItemIndex != -1) {
            mediator.alreadyKnowLemmaInSentence(sentenceAlternatives.get(selectedItemIndex));
            displayAlternatives();
		}
    }

    private void saveProgress() {
        mediator.saveProgress();
    }

    private void loadProgress() {
        progressLabel.setText("Loading progress from file.");
        ((DefaultListModel<String>) learnedJList.getModel()).clear();
        mediator.loadProgress();

        displayAlternatives();
        SwingUtilities.updateComponentTreeUI(frame);
        progressLabel.setText("Finished loading progress from file.");
        //loadProgressButton.setEnabled(false);
        startLearningButton.setEnabled(false);
        learnNextLemmaButton.setEnabled(true);
        alreadyKnowLemmaButton.setEnabled(true);
        learnUntilStopButton.setEnabled(true);
        saveProgressButton.setEnabled(true);

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
                alreadyKnowLemmaButton.setEnabled(false);
				while (isCurrentlyLearning.get()) {
					learnActualSentence();
				}
                learnNextLemmaButton.setEnabled(true);
                alreadyKnowLemmaButton.setEnabled(true);
				actionLock.unlock();
			}).start();
		} else {
			isCurrentlyLearning.getAndSet(false);
            displayAlternatives();
            SwingUtilities.updateComponentTreeUI(frame);
			learnUntilStopButton.setText("Learn until stop");
		}
	}

	private void startLearning() {
		var thread = new Thread(() -> {
			actionLock.lock();
			startLearningButton.setEnabled(false);
            loadProgressButton.setEnabled(false);
			progressLabel.setText("Learning initial sentence." );
			//panel.repaint();
			mediator.initializeLearning();
			displayAlternatives();
			//frame.repaint();
            saveProgressButton.setEnabled(true);
            learnNextLemmaButton.setEnabled(true);
            learnUntilStopButton.setEnabled(true);
            alreadyKnowLemmaButton.setEnabled(true);
            actionLock.unlock();
		});

        try {

            System.out.println("Started");
            thread.start();
            thread.join();
            System.out.println("Returned");
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

	}

	private void loadFilesInFolder() {
		new Thread(() -> {
			actionLock.lock();
            loadFolderButton.setEnabled(false);
			progressLabel.setText("Load files in folder." );
			//panel.repaint();
			mediator.loadFilesInGivenFolder(folderField.getText());
			progressLabel.setText("Finished loading files in folder." );
			startLearningButton.setEnabled(true);
            loadProgressButton.setEnabled(true);
			//frame.repaint();
			actionLock.unlock();
		}).start();
	}

	public void learnSelectedSentence() {
        learnUntilStopButton.setEnabled(false);
        learnActualSentence();
        displayAlternatives();
        learnUntilStopButton.setEnabled(true);
        //SwingUtilities.updateComponentTreeUI(frame);
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
		//panel.repaint();
	}

	@Override
	public void printProgressInParsingTexts(File textFile, ParsingProgressStruct progressReporter) {
		incrementProgressBar();
	}

    List<SortablePair<Lemma, Sentence>> orderOfLearnedLemmas = new ArrayList<SortablePair<Lemma, Sentence>>();
	@Override
	public void printLearnedLemma(LearningConfigurations config, List<SortablePair<Lemma, Sentence>> orderOfLearnedLemmas, TextDatabase database) {
		var learnedPair = orderOfLearnedLemmas.get(orderOfLearnedLemmas.size() - 1);
        this.orderOfLearnedLemmas.clear();
        this.orderOfLearnedLemmas.addAll(orderOfLearnedLemmas);
		Lemma learnedLemma = learnedPair.getFirst();
		Sentence learnedSentence = learnedPair.getSecond();
		var list = ((DefaultListModel<String>) learnedJList.getModel());
		list.addElement((list.size() + 1) + ", " + learnedLemma.getRawLemma() + " -> " + learnedSentence.getRawSentence());
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

        System.out.println("Display alternatives");
        //sentenceChoisesJList.updateUI();
        //updatePanelView();
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

