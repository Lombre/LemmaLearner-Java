package LemmaLearner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import antlrGrammar.TextParsingGrammarBaseVisitor;
import antlrGrammar.TextParsingGrammarParser;
import antlrGrammar.TextParsingGrammarParser.DanglingSentenceContext;
import antlrGrammar.TextParsingGrammarParser.EndParagraphSentenceContext;
import antlrGrammar.TextParsingGrammarParser.MidParagraphSentenceContext;
import antlrGrammar.TextParsingGrammarParser.NonWordContext;
import antlrGrammar.TextParsingGrammarParser.NormalWordContext;
import antlrGrammar.TextParsingGrammarParser.ParagraphContext;
import antlrGrammar.TextParsingGrammarParser.QuotedSentenceContext;
import antlrGrammar.TextParsingGrammarParser.StartContext;
import antlrGrammar.TextParsingGrammarParser.TextContext;
import antlrGrammar.TextParsingGrammarParser.WordContext;

public class ANTLRvisitor {

	public static class StartVisitor extends TextParsingGrammarBaseVisitor<Text>{
		
		private final String textName;
		
		public StartVisitor(String textName) {
			this.textName = textName;
		}
		
		@Override
		public Text visitStart(StartContext ctx) {			
			var visitor = new TextVisitor(textName);
			if (ctx.txt != null)
				return visitor.visit(ctx.txt);
			else return new Text(textName, "", new ArrayList<>());
		}
	}
	
	public static class TextVisitor extends TextParsingGrammarBaseVisitor<Text>{
		
		private final String textName;
		
		public TextVisitor(String textName) {
			this.textName = textName;
		}
		
		@Override
		public Text visitText(TextContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			var visitor = new ParagraphVisitor();
			int paragraphCount = 0;
			//Maybe change to a string builder
			for (int i = 0; i < ctx.children.size(); i++) {
				var child = ctx.children.get(i);				
				if (child instanceof TextParsingGrammarParser.ParagraphContext)	{
					Paragraph paragraph = visitor.visit(child);
					paragraph.setParagraphID(textName + paragraphCount);
					paragraphs.add(paragraph);
					if (paragraphCount % 1000 == 0) {
						//System.out.println("Visisted paragraph " + paragraphCount);						
					}					
					paragraphCount++;
				}
				else if (child instanceof TerminalNodeImpl)
					continue;
				else if (child instanceof TextParsingGrammarParser.SkipLineContext)
					continue;
				else 
					throw new Error("Unhandeled context type: " + child.getClass());
			}
			return new Text(textName, rawText, paragraphs);
		}
	}
	
	public static class ParagraphVisitor extends TextParsingGrammarBaseVisitor<Paragraph>{

		@Override
		public Paragraph visitParagraph(ParagraphContext ctx) {
			var visitor = new SentenceVisitor();
			List<Sentence> sentences = new ArrayList<Sentence>();
			String rawText = getRawTextFromContext(ctx);
			//System.out.println(rawText);
			
			
			for (int i = 0; i < ctx.children.size(); i++) {
				var child = ctx.children.get(i);				
				if (child instanceof MidParagraphSentenceContext || 
					child instanceof EndParagraphSentenceContext)
					sentences.add(visitor.visit(child));
				else if (child instanceof TerminalNodeImpl)					
					continue;
				else throw new Error("Unhandeled context type: " + child.getClass());
			}
			return new Paragraph(rawText, sentences);
		}
	}
	

	public static class SentenceVisitor extends TextParsingGrammarBaseVisitor<Sentence>{

		@Override
		public Sentence visitMidParagraphSentence(MidParagraphSentenceContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			SentenceVisitor danglingSentenceVisitor = new SentenceVisitor();
			Sentence danglingSentence = danglingSentenceVisitor.visit(ctx.sentence);			
			return new Sentence(rawText, danglingSentence.getWordList());
		}		

		
		@Override
		public Sentence visitEndParagraphSentence(EndParagraphSentenceContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			SentenceVisitor danglingSentenceVisitor = new SentenceVisitor();
			Sentence danglingSentence = danglingSentenceVisitor.visit(ctx.sentence);			
			return new Sentence(rawText, danglingSentence.getWordList());
		}		

		@Override
		public Sentence visitDanglingSentence(DanglingSentenceContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			WordVisitor wordVisitor = new WordVisitor();
			QuotedSentenceVisitor quotedSentenceVisitor = new QuotedSentenceVisitor();
			List<Word> words = new ArrayList<Word>();

			for (int i = 0; i < ctx.children.size(); i++) {
				var child = ctx.children.get(i);				
				if (child instanceof WordContext) {
					List<Word> wordsToAdd = wordVisitor.visit(child);
					words.addAll(wordsToAdd);	
				}
				else if (child instanceof QuotedSentenceContext) {
					List<Word> wordsToAdd = quotedSentenceVisitor.visit(child).getAllWords();
					words.addAll(wordsToAdd);	
					
				}
				else if (child instanceof TerminalNodeImpl)	
					continue;
				else 
					throw new Error("Unhandeled context type: " + child.getClass());
			}
			
			return new Sentence(rawText, words);
		}
	}
	
	

	

	public static class QuotedSentenceVisitor extends TextParsingGrammarBaseVisitor<Paragraph>{

		@Override
		public Paragraph visitQuotedSentence(QuotedSentenceContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			Paragraph midParagraph = new ParagraphVisitor().visit(ctx.midParagraph);
			return new Paragraph(rawText, midParagraph.getSentences());
		}
	}
	

	

	public static class WordVisitor extends TextParsingGrammarBaseVisitor<List<Word>>{

		@Override
		public List<Word> visitWord(WordContext ctx) {
			var child = ctx.children.get(0);
			NormalWordVisitor visitor = new NormalWordVisitor();
			if (child instanceof NormalWordContext )
				return visitor.visit(child);
			else if (child instanceof NonWordContext)					
				return new ArrayList<Word>();
			//	return new ArrayList<Word>() {{add(new Word(TextDatabase.notAWordString));}};
			else throw new Error("Unhandeled context type: " + child.getClass());
		}
	}	
	

	public static class NormalWordVisitor extends TextParsingGrammarBaseVisitor<List<Word>>{

		@Override
		public List<Word> visitNormalWord(NormalWordContext ctx) {
			String rawText = getRawTextFromContext(ctx);
			List<String> splitText = Arrays.asList(rawText.split("(’|,)"));
			
			return splitText.stream()
					        .filter(wordPart -> !wordPart.equals(""))
							.map(wordPart -> new Word(wordPart))
							.collect(Collectors.toList());
		}
	}	

	public static String getRawTextFromContext(ParserRuleContext ctx) {
		int startIndex = ctx.start.getStartIndex();
		int endIndex = ctx.stop.getStopIndex();
		Interval interval = new Interval(startIndex, endIndex);
		return ctx.start.getInputStream().getText(interval);		
	}
	
}
