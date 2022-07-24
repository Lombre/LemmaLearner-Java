package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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

import Configurations.Configurations;
import Configurations.GuiConfigurations;
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
	private JScrollPane scrollLearnedJList;
	private JScrollPane scrollSentenceChoisesJList;
	private JButton learnUntilStopButton;
	private MyCellRenderer renderer1;
	private MyCellRenderer renderer2;
	private MigLayout layout;
	ReentrantLock actionLock = new ReentrantLock(true);
	
	private JButton startLearningButton;
	
	private GuiConfigurations config;

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
        
        panel.add(new JLabel("Text folder:"), "split3");
        folderField = new JTextField("Texts/" + config.getLanguage()); // + "english/");
        panel.add(folderField, "pushx, growx");
        var loadFolderButton = new JButton("Load files");
        
        loadFolderButton.addActionListener(event -> loadFilesInFolder());
        panel.add(loadFolderButton, "span, wrap");
        
        startLearningButton = new JButton("Start learning");
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
        renderer1 = new MyCellRenderer(160);
        learnedJList.setCellRenderer(renderer1);
        //learnedJList.setCellRenderer(new MyCellRenderer());
        //learnedJList.setBounds(100,100, 750,750);
        
        scrollLearnedJList = new JScrollPane(learnedJList);
        panel.add(scrollLearnedJList, "split2, pushy, grow");
        
        sentenceChoisesJList = new JList<String>(new DefaultListModel<String>());
        renderer2 = new MyCellRenderer(160);
        sentenceChoisesJList.setCellRenderer(renderer2);
        //sentenceChoisesJList.setCellRenderer(new MyCellRenderer());
        //sentenceChoisesJList.setBounds(100,100, 750,750);
        
        scrollSentenceChoisesJList = new JScrollPane(sentenceChoisesJList);
        panel.add(scrollSentenceChoisesJList,  "grow, wrap");
        
        
        progressLabel = new JLabel("");
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
		if (isCurrentlyLearning.compareAndSet(false, true)) {
			learnUntilStopButton.setText("Stop learning sentences");
			new Thread(() -> {
				actionLock.lock();
				while (isCurrentlyLearning.get()) {
					learnActualSentence();
				}
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
			actionLock.unlock();
		}).start();
	}

	private void loadFilesInFolder() {
		new Thread(() -> {
			actionLock.lock();
			progressLabel.setText("Learning new sentence." );
			panel.repaint();
			mediator.loadFilesInGivenFolder(folderField.getText()); 
			progressLabel.setText("Finished learning new sentence." );
			
			frame.repaint();
			actionLock.unlock();
		}).start();		
	}
	
	public void learnSelectedSentence() {
		new Thread(() -> {
			actionLock.lock();
			learnActualSentence();
			displayAlternatives();
			
			int newWidths = (int) ((scrollLearnedJList.getWidth() + scrollLearnedJList.getWidth())/2.3);
			System.out.println(newWidths);
			renderer1.setWidth(newWidths);
			renderer2.setWidth(newWidths);
			
			scrollLearnedJList.getViewport().revalidate();
			scrollSentenceChoisesJList.getViewport().revalidate();
			panel.revalidate();
			scrollLearnedJList.getViewport().revalidate();
			scrollSentenceChoisesJList.getViewport().revalidate();
			panel.revalidate();
			panel.repaint();
			
			actionLock.unlock();
			System.out.println(frame.getWidth());
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

	// Must be temporarily stored so that they can be accessed in the list.
	ArrayList<Sentence> sentenceAlternatives;
	public void displayAlternatives() {
		var sentencePair = mediator.getAlternativeSentencesWithDescription();
		sentenceAlternatives = sentencePair.getKey();
		var descriptions = sentencePair.getValue();
		var displayList = ((DefaultListModel<String>) sentenceChoisesJList.getModel());
		displayList.clear();
		for (int i = 0; i < descriptions.size(); i++) {
			var sentenceDescription = descriptions.get(i);
			var sentence = sentenceAlternatives.get(i);
			displayList.addElement(sentenceDescription + "<br> ------ " + sentence.getRawSentence());
			
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
	   public static final String HTML_1 = "<html><body style='width: ";
	   public static final String HTML_2 = "px'>";
	   public static final String HTML_3 = "</html>";
	   private int width;

	   public MyCellRenderer(int width) {
	      this.width = width;
	   }

	   @Override
	   public Component getListCellRendererComponent(JList list, Object value,
	         int index, boolean isSelected, boolean cellHasFocus) {
	      String text = HTML_1 + String.valueOf(width) + HTML_2 + value.toString()
	            + HTML_3;
	      return super.getListCellRendererComponent(list, text, index, isSelected,
	            cellHasFocus);
	   }
	   
	   public void setWidth(int width) {
		   this.width = width;
	   }

	}

