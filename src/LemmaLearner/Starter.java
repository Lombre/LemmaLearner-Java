package LemmaLearner;

import GUI.*;

//import org.antlr.v4.runtime.CharStream;


public class Starter {
	
	public static void main(String[] args) throws Exception {
		var gui = new SwingGUI();
		// var gui = new ConsoleGUI();
		new Mediator(gui);
	}
}
