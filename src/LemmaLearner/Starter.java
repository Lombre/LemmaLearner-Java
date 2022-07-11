package LemmaLearner;

import java.util.List;

import Configurations.Configurations;
import GUI.*;
import TextDataStructures.Lemma;
import TextDataStructures.Sentence;

//import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		
		ConsoleGUI gui;

		gui = new ConsoleGUI();
		var mediator = new Mediator(gui);
		gui.runProgram();		
	}
	
	
}
