package com.bgk21.diss;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {

	Program p;

	static int lineNumber;

	int currentStatement = 0;

	int insideIf;
	int whileCount;
	boolean comparison;
	ArrayList<ArrayList<String>> initialiseVars;

	public Parser(Program p) {
		this.p = p;
		lineNumber = 1;
		insideIf = 0;
		whileCount = 0;
		initialiseVars = new ArrayList<ArrayList<String>>();
		comparison = false;
	}

	public Block parse(List<Token> input) throws SyntaxException {
		return new Block(parse_block(input));
	}

	public ArrayList<Exp> parse_block(List<Token> list) throws SyntaxException {
		// If input in the form "{ <ene> }"
		if ((list.get(0) instanceof T_LeftCurlyBracket) && (list.get(list.size() - 1) instanceof T_RightCurlyBracket)) {
			return parse_ene(list.subList(1, list.size() - 1)); // Parse <ene>
		}
		p.errors.add("Error on line " + lineNumber + "\nInput not in form \\\"{ <ene> }\\\"");
		throw new SyntaxException("Input not in form \"{ <ene> }\"");
	}

	public ArrayList<Exp> parse_ene(List<Token> list) throws SyntaxException {
		if (list.get(0) instanceof T_NewLine) {
			lineNumber++;
			list = list.subList(1, list.size());
		}

		// if right curly bracket before semicolon, look for next right curly bracket,
		// and parse_e up to semicolon after bracket

		int endOfLine = containsInstance(list, T_Semicolon.class);
		int nextLeftCurlyBracket = containsInstance(list, T_LeftCurlyBracket.class);
		if (endOfLine != -1) {
			if (nextLeftCurlyBracket != -1 && nextLeftCurlyBracket < endOfLine) {
				int i = nextLeftCurlyBracket;
				int openBrackets = 0;
				while (i < list.size()) {
					if (list.get(i) instanceof T_LeftCurlyBracket)
						openBrackets++;
					else if (list.get(i) instanceof T_RightCurlyBracket)
						openBrackets--;

					if (openBrackets == 0) {
						if (i + 1 < list.size()) {
							if (list.get(i + 1) instanceof T_Else) {
								i += 1;
							} else {
								ArrayList<Exp> temp = new ArrayList<Exp>();
								temp.addAll(parse_e(list.subList(0, i + 1)));
								if (!list.subList(i + 2, list.size()).isEmpty()) {
									if (!(list.subList(i + 2, list.size()).get(0) instanceof T_NewLine && list.subList(i + 2, list.size()).size() == 1)) {
										temp.addAll(parse_ene(list.subList(i + 2, list.size())));
									} else {
										lineNumber++;
									}
								}
								return temp;
							}
						}
					}
					i++;
				}
				p.errors.add("Error on line " + lineNumber + "\nIncorrect curly brackets");
				throw new SyntaxException("Incorrect curly brackets");
			} else {
				ArrayList<Exp> temp = new ArrayList<Exp>();
				temp.addAll(parse_e(list.subList(0, endOfLine)));
				if (!list.subList(endOfLine + 1, list.size()).isEmpty()) {
					if (!(list.subList(endOfLine + 1, list.size()).get(0) instanceof T_NewLine && list.subList(endOfLine + 1, list.size()).size() == 1)) {
						temp.addAll(parse_ene(list.subList(endOfLine + 1, list.size())));
					} else {
						lineNumber++;
					}
				}
				return temp;
			}
		} else {
			p.errors.add("Error on line " + lineNumber + "\nMissing semi-colon");
			throw new SyntaxException("Missing semicolon");
		}
	}

	public ArrayList<Exp> parse_e(List<Token> list) throws SyntaxException {
		int pos;
		if (list.size() == 0) {
			p.errors.add("Error on line " + lineNumber + "\nMissing arguments");
			throw new SyntaxException("Token list size is 0");
		}
		if (list.get(0) instanceof T_Double && list.size() == 1) {
			ArrayList<Exp> temp = new ArrayList<Exp>();
			temp.add(new DoubleLiteral(((T_Double) list.get(0)).number, lineNumber));
			return temp;
		}
		if (list.get(0) instanceof T_Identifier) {
			if (list.size() == 1) {
				ArrayList<Exp> temp = new ArrayList<Exp>();
				String name = ((T_Identifier) list.get(0)).name;
				if (comparison) {
					for (int i = 0; i < insideIf + 1; i++) {
						name += "_TEMP";
					}
					if (!initialiseVars.get(insideIf).contains(name)) {
						initialiseVars.get(insideIf).add(name);
					}
				}
				temp.add(new VarExp(name, lineNumber));
				return temp;
			} else if (list.get(1) instanceof T_EqualDefines) {
				currentStatement++;
				return parse_equal_defines(list);
			}
		}
		if (list.get(0) instanceof T_If) {
			currentStatement++;
			return parse_if(list);
		}
		if (list.get(0) instanceof T_For) {
			currentStatement++;
			return parse_for(list);
		}
		if (list.get(0) instanceof T_Print) {
			currentStatement++;
			return parse_print(list);
		}
		if (list.get(0) instanceof T_While) {
			currentStatement++;
			return parse_while(list);
		}
		if ((pos = containsInstance(list, T_Comp.class)) != -1) {
			return parse_comp(list, pos);
		}
		if ((pos = containsInstance(list, T_Binop.class)) != -1) {
			return parse_binop(list);
		}

		// If the input doesnt match any of these forms, throw syntax exception
		p.errors.add("Error on line " + lineNumber + "\nToken did not parse");
		throw new SyntaxException("Error with token list");
	}

	public ArrayList<Exp> parse_equal_defines(List<Token> list) throws SyntaxException {
		ArrayList<Exp> temp = new ArrayList<Exp>();
		List<Token> l = new ArrayList<Token>();
		l.add(list.get(0));
		VarExp left = (VarExp) parse_e(l).get(0);
		Exp right = parse_e(list.subList(2, list.size())).get(0);
		temp.add(new AssignExp(left, right, lineNumber));
		return temp;
	}

	public IttExp parse_itt(List<Token> list) throws SyntaxException {
		if (list.get(0) instanceof T_Identifier) {
			VarExp var = (VarExp) parse_e(list.subList(0, 1)).get(0);
			if (list.get(1) instanceof T_EqualDefines) {
				if (list.get(3) instanceof T_Colon) {
					Exp from = parse_e(list.subList(2, 3)).get(0);
					Exp to = parse_e(list.subList(4, 5)).get(0);

					return new IttExp(var, from, to, lineNumber);
				}
			}
		}
		throw new SyntaxException("Error parsing for");
	}

	public ArrayList<Exp> parse_binop(List<Token> list) throws SyntaxException {
		Stack<Binop> operatorStack = new Stack<>();
		Stack<Exp> operandStack = new Stack<>();
		for(Token token : list) {
			if(token instanceof T_Identifier) {
				String name = ((T_Identifier) token).name;
				if (comparison) {
					for (int i = 0; i < insideIf + 1; i++) {
						name += "_TEMP";
					}
					if (!initialiseVars.get(insideIf).contains(name)) {
						initialiseVars.get(insideIf).add(name);
					}
				}
				operandStack.push(new VarExp(name, lineNumber));
				
			} else if (token instanceof T_Double) {
				operandStack.push(new DoubleLiteral(((T_Double) token).number, lineNumber));
			} else if(token instanceof T_LeftBracket || operatorStack.isEmpty()) {
				operatorStack.push(getBinop(token));
			} else if (operatorOrder(getBinop(token)) > operatorOrder(operatorStack.lastElement())) {
				operatorStack.push(getBinop(token));
			} else if (token instanceof T_RightBracket) {
				while(!(operatorStack.lastElement() instanceof LeftBracket)) {
					Binop operator = operatorStack.pop();
					Exp rightOperand = operandStack.pop();
					Exp leftOperand = operandStack.pop();
					BinopExp binopExp = new BinopExp(leftOperand, operator, rightOperand, lineNumber);
					operandStack.push(binopExp);
				}
				operatorStack.pop();
			} else if (operatorOrder(getBinop(token)) <= operatorOrder(operatorStack.lastElement())) {
				while(!operatorStack.isEmpty() && operatorOrder(getBinop(token)) <= operatorOrder(operatorStack.lastElement())) {
					Binop operator = operatorStack.pop();
					Exp rightOperand = operandStack.pop();
					Exp leftOperand = operandStack.pop();
					BinopExp binopExp = new BinopExp(leftOperand, operator, rightOperand, lineNumber);
					operandStack.push(binopExp);
				}
				operatorStack.push(getBinop(token));
			}
		}
		while(!operatorStack.isEmpty()) {
			Binop operator = operatorStack.pop();
			Exp rightOperand = operandStack.pop();
			Exp leftOperand = operandStack.pop();
			BinopExp binopExp = new BinopExp(leftOperand, operator, rightOperand, lineNumber);
			operandStack.push(binopExp);
		}
		
		ArrayList<Exp> out = new ArrayList<Exp>();
		out.add(operandStack.lastElement());
		return out;
	}
	
	private Binop getBinop(Token t) {
		Binop op = null;
		if (t instanceof T_Minus)
			op = new Minus();
		if (t instanceof T_Plus)
			op = new Plus();
		if (t instanceof T_Div)
			op = new Div();
		if (t instanceof T_Times)
			op = new Times();
		if (t instanceof T_Mod)
			op = new Mod();
		if(t instanceof T_RightBracket)
			op = new RightBracket();
		if(t instanceof T_LeftBracket) {
			op = new LeftBracket();
		}
		return op;
	}

	private int operatorOrder(Binop binop) throws SyntaxException {
		// BIDMAS
		if(binop instanceof LeftBracket || binop instanceof RightBracket) {
			return 0;
		} else if (binop instanceof Div) {
			return 5;
		} else if (binop instanceof Mod) {
			return 4;
		} else if (binop instanceof Times) {
			return 3;
		} else if (binop instanceof Plus) {
			return 2;
		} else if (binop instanceof Minus) {
			return 1;
		} else {
			throw new SyntaxException("");
		}
	}

	public ArrayList<Exp> parse_comp(List<Token> list, int pos) throws SyntaxException {
		ArrayList<Exp> temp = new ArrayList<Exp>();
		Exp left = parse_e(list.subList(0, pos)).get(0);
		Exp right = parse_e(list.subList(pos + 1, list.size())).get(0);

		Comp comp = null;
		if (list.get(pos) instanceof T_Equal)
			comp = new Equals();
		if (list.get(pos) instanceof T_LessThan)
			comp = new Less();
		if (list.get(pos) instanceof T_GreaterThan)
			comp = new Greater();
		if (list.get(pos) instanceof T_LessEq)
			comp = new LessEq();
		if (list.get(pos) instanceof T_GreaterEq)
			comp = new GreaterEq();
		if (list.get(pos) instanceof T_NotEqual)
			comp = new NotEquals();

		temp.add(new CompExp(left, comp, right, lineNumber));
		return temp;
	}

	public ArrayList<Exp> parse_if(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement;
		currentStatement++;
		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);

			initialiseVars.add(new ArrayList<String>());

			comparison = true;
			Exp condition = parse_e(list.subList(2, rightBracket)).get(0);
			comparison = false;

			insideIf++;

			if (list.get(rightBracket + 1) instanceof T_LeftCurlyBracket) {
				int startOfThenBody = rightBracket + 1;
				int endOfThenBody = getPosOfClosingCurlyBracket(list, startOfThenBody);
				BlockExp thenBody = new BlockExp(parse(list.subList(startOfThenBody, endOfThenBody + 1)), lineNumber);
				currentStatement += 2;
				BlockExp elseBody = null;

				if (endOfThenBody + 1 < list.size()) {
					if (list.get(endOfThenBody + 1) instanceof T_Else) {
						int startOfElseBody = endOfThenBody + 2;
						int endOfElseBody = getPosOfClosingCurlyBracket(list, startOfElseBody);
						elseBody = new BlockExp(parse(list.subList(startOfElseBody, endOfElseBody + 1)), lineNumber);
					}
				}

				int endOfBody = startStatement + thenBody.b.exps.size() + 2;
				int endOfIf = startStatement + thenBody.b.exps.size() + 3;
				if (elseBody != null)
					endOfIf += elseBody.b.exps.size();

				ArrayList<Exp> compExps = new ArrayList<Exp>();
				for (String s : initialiseVars.get(insideIf - 1)) {
					compExps.add(new AssignExp(new VarExp(s, lineNumber), new VarExp(Parser.getRealVarName(s), lineNumber), lineNumber));
				}

				ArrayList<Exp> temp = new ArrayList<Exp>();
				IfExp e = new IfExp((CompExp) condition, initialiseVars.get(insideIf - 1), endOfBody, endOfIf, tempLineNo);
				temp.add(new CompilerExp(new Block(compExps), tempLineNo));
				temp.add(e);
				temp.addAll(thenBody.b.exps);
				temp.add(new GotoExp(endOfIf, lineNumber)); // go to the end of if
				temp.add(new ComeFromExp(startStatement, lineNumber)); // Come from the start of if
				if (elseBody != null)
					temp.addAll(elseBody.b.exps);
				temp.add(new EndIfExp(e, lineNumber));
				currentStatement++;

				insideIf--;
				return temp;
			}

		}
		p.errors.add("Error on line " + lineNumber + "\nIf-Statement does not follow if(<condition>) { X } else { Y };");
		throw new SyntaxException("Error parsing if");
	}

	public ArrayList<Exp> parse_for(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement;
		currentStatement++;

		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);

			IttExp ittExp = parse_itt(list.subList(2, rightBracket));

			int startOfBody = rightBracket + 1;
			int endOfBody = getPosOfClosingCurlyBracket(list, startOfBody);

			currentStatement++;
			BlockExp body = new BlockExp(parse(list.subList(startOfBody, endOfBody + 1)), lineNumber);
			currentStatement += 2;

			body.b.exps.add(new AssignExp(ittExp.itterator, new BinopExp(ittExp.itterator, new Plus(), new DoubleLiteral(1, lineNumber), lineNumber), lineNumber));
			body.b.exps.add(new GotoExp(startStatement, lineNumber));

			int endOfFor = startStatement + body.b.exps.size() + 1;
			body.b.exps.add(0, new ComeFromExp(endOfFor + 2, tempLineNo));
			endOfFor++;

			ArrayList<Exp> temp = new ArrayList<Exp>();
			temp.add(new AssignExp(ittExp.itterator, ittExp.from, tempLineNo));
			temp.add(new ForExp(ittExp, body, endOfFor, tempLineNo));
			temp.addAll(body.b.exps);
			temp.add(new EndForExp(new IttExp(ittExp.itterator, ittExp.to, ittExp.from, lineNumber), startStatement, lineNumber));
			currentStatement++;
			return temp;
		}
		p.errors.add("Error on line " + lineNumber + "\nFor-Statement does not follow form for(<var> = i : j) { X };");
		throw new SyntaxException("Error parsing for");
	}

	
	public ArrayList<Exp> parse_while(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement;
		currentStatement++;
		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);
			Exp condition = parse_e(list.subList(2, rightBracket)).get(0);
			if (condition instanceof CompExp) {
				if (list.get(rightBracket + 1) instanceof T_LeftCurlyBracket) {
					whileCount++;
					int startOfThenBody = rightBracket + 1;
					int endOfThenBody = getPosOfClosingCurlyBracket(list, startOfThenBody);

					VarExp counter = new VarExp("_COUNTER" + whileCount, lineNumber);

					currentStatement++;
					BlockExp thenBody = new BlockExp(parse(list.subList(startOfThenBody, endOfThenBody + 1)), lineNumber);
					currentStatement += 2;

					thenBody.b.exps.add(new AssignExp(counter, new BinopExp(counter, new Plus(), new DoubleLiteral(1, lineNumber), lineNumber), lineNumber));
					thenBody.b.exps.add(new GotoExp(startStatement, lineNumber));
					int endOfWhile = startStatement + thenBody.b.exps.size() + 1;
					thenBody.b.exps.add(0, new ComeFromExp(endOfWhile + 2, tempLineNo));
					endOfWhile++;

					ArrayList<Exp> temp = new ArrayList<Exp>();
					WhileExp e = new WhileExp((CompExp) condition, counter, endOfWhile, tempLineNo);
					temp.add(new AssignExp(counter, new DoubleLiteral(0, tempLineNo), tempLineNo));
					temp.add(e);
					temp.addAll(thenBody.b.exps);
					temp.add(new EndWhileExp(counter, startStatement, lineNumber));
					currentStatement++;

					return temp;
				}
			}
		}
		p.errors.add("Error on line " + lineNumber + "\nWhile-Statement does not follow while(<condition>) { X; };");
		throw new SyntaxException("Error with while loop");
	}

	public ArrayList<Exp> parse_print(List<Token> list) throws SyntaxException {
		if (list.get(1) instanceof T_LeftBracket) {
			int leftBracket = 1;
			int rightBracket = getPosOfClosingBracket(list, 1);
			if (rightBracket == -1) {
				p.errors.add("Error on line " + lineNumber + "\nError with token ')'");
				throw new SyntaxException("Right bracket incorrect");
			}

			Exp content = (Exp) parse_e(list.subList(leftBracket + 1, rightBracket)).get(0);

			ArrayList<Exp> temp = new ArrayList<Exp>();
			temp.add(new PrintExp(content, lineNumber));

			return temp;

		}
		p.errors.add("Error on line " + lineNumber + "\nPrint statement does not follow the form print(x)");
		throw new SyntaxException("Error parsing print");
	}

	public <E> int containsInstance(List<E> list, Class<?> class1) {
		int i = 0;
		for (E e : list) {
			if (class1.isInstance(e)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public int getPosOfClosingCurlyBracket(List<Token> list, int startingBracket) throws SyntaxException {
		int i = startingBracket;
		int openBrackets = 0;
		while (i < list.size()) {
			if (list.get(i) instanceof T_LeftCurlyBracket)
				openBrackets++;
			else if (list.get(i) instanceof T_RightCurlyBracket)
				openBrackets--;

			if (openBrackets == 0) {
				return i;
			}
			i++;
		}
		p.errors.add("Error on line " + lineNumber + "\nError with curly brackets");
		throw new SyntaxException("Incorrect Curly Brackets");
	}

	public int getPosOfClosingBracket(List<Token> list, int startingBracket) throws SyntaxException {
		int i = startingBracket;
		int openBrackets = 0;
		while (i < list.size()) {
			if (list.get(i) instanceof T_LeftBracket)
				openBrackets++;
			else if (list.get(i) instanceof T_RightBracket)
				openBrackets--;

			if (openBrackets == 0) {
				return i;
			}
			i++;
		}
		p.errors.add("Error on line " + lineNumber + "\nError with curly brackets");
		throw new SyntaxException("Incorrect Brackets");
	}

	public static String getRealVarName(String name) {
		while (name.endsWith("_TEMP")) {
			name = name.substring(0, name.length() - 5);
		}
		return name;
	}

}

class SyntaxException extends Exception {
	private static final long serialVersionUID = 1L;
	public String msg;

	public SyntaxException(String _msg) {
		msg = _msg;
	}
}
