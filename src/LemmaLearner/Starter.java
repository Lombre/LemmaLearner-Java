package LemmaLearner;

import GUI.*;

//import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		
		SwingGUI gui;

		gui = new SwingGUI();
		new Mediator(gui);
	}
	
	
}
