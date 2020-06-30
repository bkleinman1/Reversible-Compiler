package com.bgk21.diss;

import java.util.ArrayList;

public class Lexer {

	int lineNumber;
	ArrayList<String> errors;

	public ArrayList<Token> lex(String input) {
		input += " ";
		ArrayList<Token> tokens = new ArrayList<Token>();
		errors = new ArrayList<String>();
		lineNumber = 0;

		for (int i = 0; i < input.length() - 1; i++) {

			boolean flag = false;
			char currentChar = input.charAt(i);
			char nextChar = input.charAt(i + 1);

			// KEY WORDS //

			if (Character.isLetter(currentChar)) {
				String word = "";
				word += currentChar;
				// Loop through the input until the end of the word/number (underscore included)
				while ((Character.isLetterOrDigit(nextChar) || nextChar == '_')
						&& i + word.length() + 1 < input.length()) {
					currentChar = nextChar;
					nextChar = input.charAt(i + word.length() + 1);
					word += currentChar;
				}
				switch (word) {
				case ("if"):
					tokens.add(new T_If());
					i += word.length() - 1;
					flag = true;
					break;
				case ("else"):
					tokens.add(new T_Else());
					i += word.length() - 1;
					flag = true;
					break;
				case ("for"):
					tokens.add(new T_For());
					i += word.length() - 1;
					flag = true;
					break;
				case ("while"):
					tokens.add(new T_While());
					i += word.length() - 1;
					flag = true;
					break;
				case ("print"):
					tokens.add(new T_Print());
					i += word.length() - 1;
					flag = true;
					break;
				case ("goto"):
					tokens.add(new T_Goto());
					i += word.length() - 1;
					flag = true;
					break;
				default:
					i += word.length() - 1;
					tokens.add(new T_Identifier(word));
					flag = true;
				}

			}

			if (!flag) {
				if (Character.isDigit(currentChar)) {
					String word = "";
					word += currentChar;
					while ((Character.isDigit(nextChar) || nextChar == '.') && i + word.length() + 1 < input.length()) {
						currentChar = nextChar;
						nextChar = input.charAt(i + word.length() + 1);
						word += currentChar;
					}

					tokens.add(new T_Double(Double.parseDouble(word)));
					i += word.length() - 1;
					flag = true;
				}
			}

			if (!flag) {
				switch (currentChar) {

				// SPECIAL NOTATION //
				case (';'):
					tokens.add(new T_Semicolon());
					flag = true;
					break;
				case ('('):
					tokens.add(new T_LeftBracket());
					flag = true;
					break;
				case (')'):
					tokens.add(new T_RightBracket());
					flag = true;
					break;
				case ('='):
					if (nextChar != '=') {
						tokens.add(new T_EqualDefines());
						flag = true;
					} else if (nextChar == '=') {
						tokens.add(new T_Equal());
						flag = true;
						i++;
					}
					break;
				case ('<'):
					if (nextChar != '=') {
						tokens.add(new T_LessThan());
						flag = true;
					} else if (nextChar == '=') {
						tokens.add(new T_LessEq());
						flag = true;
						i++;
					}
					break;
				case ('>'):
					if (nextChar != '=') {
						tokens.add(new T_GreaterThan());
						flag = true;
					} else if (nextChar == '=') {
						tokens.add(new T_GreaterEq());
						flag = true;
						i++;
					}
					break;
				case ('!'):
					if(nextChar == '=') {
						tokens.add(new T_NotEqual());
						flag = true;
						i++;
					}
					break;
				case ('{'):
					tokens.add(new T_LeftCurlyBracket());
					flag = true;
					break;
				case ('}'):
					tokens.add(new T_RightCurlyBracket());
					flag = true;
					break;
				case ('%'):
					tokens.add(new T_Mod());
					flag = true;
					break;
				case (':'):
					tokens.add(new T_Colon());
					flag = true;
					break;
				case ('+'):
					tokens.add(new T_Plus());
					flag = true;
					break;
				case ('-'):
					tokens.add(new T_Minus());
					flag = true;
					break;
				case ('/'):
					tokens.add(new T_Div());
					flag = true;
					break;
				case ('*'):
					tokens.add(new T_Times());
					flag = true;
					break;
				case (' '):
					flag = true;
					break;
				case ('\n'):
					tokens.add(new T_NewLine());
					flag = true;
					break;
				}
			}

			if (!flag) {
				errors.add("Error: Unknown character <" + currentChar + ">");
			}

		}
		return tokens;
	}
}
