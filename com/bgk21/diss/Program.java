package com.bgk21.diss;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Program {

	String fileURL;
	public ArrayList<Token> tokenList;
	public Block AST;
	public String text;
	ArrayList<String> errors;

	public Program(String fileURL) {
		errors = new ArrayList<String>();
		this.fileURL = fileURL;
		try {
			text = getInput(fileURL);
		} catch (Exception e) {
			text = "";
			e.printStackTrace();
		}
	}
	
	//This method is only used for JUnit testing, removing the need for a file location
	public Program(String text, boolean TESTING) {
		this.text = text;
		errors = new ArrayList<String>();
	}

	public String getInput(String fileLocation) throws Exception {
		Scanner scanner = new Scanner(new FileReader(fileLocation));
		String text = "";
		while (scanner.hasNext()) {
			text += scanner.next() + " ";
		}
		scanner.close();
		text = formatCode(text);
		return text;
	}

	public void lexAndParse() throws SyntaxException {
		Lexer lex = new Lexer();
		tokenList = lex.lex(text);
		this.errors.addAll(lex.errors);

		Parser parse = new Parser(this);
		AST = parse.parse(tokenList);
	}

	public void outputTokenList(ArrayList<Token> tokens) {
		for (Token t : tokens) {
			if (t instanceof T_Identifier) {
				T_Identifier id = (T_Identifier) t;
				System.out.println(t.getClass().getName() + "( \"" + id.name + "\" )");
			} else if (t instanceof T_Integer) {
				T_Integer num = (T_Integer) t;
				System.out.println(t.getClass().getName() + "( " + num.number + " )");
			} else {
				System.out.println(t.getClass().getName());
			}
		}
	}

	public String formatCode(String text) {
		String output = "";
		String indent = "";
		int cnt = 0;
		for (char c : text.toCharArray()) {
			if (c == ';') {
				output += c + "\n";
				if (nextChar(text, cnt + 1) == '}')
					indent = indent.substring(3);
				output += indent;
			} else if (c == '{') {
				indent += "   ";
				output += c + "\n" + indent;
			} else {
				output += c;
			}
			cnt++;
		}
		return output;
	}

	private char nextChar(String text, int index)  {
		while (index < text.length()) {
			if (Character.isWhitespace(text.toCharArray()[index])) {
				index++;
			} else {
				return text.toCharArray()[index];
			}
		}
		return ' ';
	}

}
