package com.bgk21.diss;

import java.util.ArrayList;
import java.util.List;

public class ParserOLD {

	Program p;

	static int lineNumber;

	int currentStatement = 0;

	int insideIf;
	int insideWhile;
	int insideFor;
	int offset;
	boolean comparison;
	ArrayList<ArrayList<String>> initialiseVars;

	public ParserOLD(Program p) {
		this.p = p;
		lineNumber = 1;
		insideIf = 0;
		insideWhile = 0;
		insideFor = 0;
		offset = 0;
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
		if (list.get(0) instanceof T_Integer && list.size() == 1) {
			ArrayList<Exp> temp = new ArrayList<Exp>();
			temp.add(new IntLiteral(((T_Integer) list.get(0)).number, lineNumber));
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
			return parse_binop(list, pos);
		}

		// If the input doesnt match any of these forms, throw syntax exception
		p.errors.add("Error on line " + lineNumber + "\nToken did not parse");
		throw new SyntaxException("Error");
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
		throw new SyntaxException("");
	}

	public ArrayList<Exp> parse_binop(List<Token> list, int pos) throws SyntaxException {

		ArrayList<Exp> temp = new ArrayList<Exp>();
		Exp left = parse_e(list.subList(0, pos)).get(0);
		Exp right = parse_e(list.subList(pos + 1, list.size())).get(0);
		Binop op = null;
		if (list.get(pos) instanceof T_Minus)
			op = new Minus();
		if (list.get(pos) instanceof T_Plus)
			op = new Plus();
		if (list.get(pos) instanceof T_Div)
			op = new Div();
		if (list.get(pos) instanceof T_Times)
			op = new Times();
		temp.add(new BinopExp(left, op, right, lineNumber));
		return temp;

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

//	public ArrayList<Exp> parse_if(List<Token> list) throws SyntaxException {
//		int tempLineNo = lineNumber;
//		int compPos;
//		if (list.get(1) instanceof T_LeftBracket) {
//			int rightBracket = containsInstance(list, T_RightBracket.class);
//			if (rightBracket == -1) {
//				p.errors.add("Error on line " + lineNumber + "\nError with token ')'");
//				throw new SyntaxException("Error");
//			}
//			compPos = containsInstance(list.subList(0, rightBracket), T_Comp.class);
//			if (compPos == -1) {
//				p.errors.add("Error on line " + lineNumber + "\nError with operator");
//				throw new SyntaxException("Error");
//			}
//
//			ArrayList<Exp> temp = new ArrayList<Exp>();
//			Exp left = parse_e(list.subList(2, compPos)).get(0);
//			Exp right = parse_e(list.subList(compPos + 1, rightBracket)).get(0);
//
//			Comp comp = null;
//			if (list.get(compPos) instanceof T_Equal)
//				comp = new Equals();
//			if (list.get(compPos) instanceof T_LessThan)
//				comp = new Less();
//			if (list.get(compPos) instanceof T_GreaterThan)
//				comp = new Greater();
//			if (list.get(compPos) instanceof T_LessEq)
//				comp = new LessEq();
//			if (list.get(compPos) instanceof T_GreaterEq)
//				comp = new GreaterEq();
//
//			if (list.get(rightBracket + 1) instanceof T_LeftCurlyBracket) {
//				int startOfThenBody = rightBracket + 1;
//				int endOfThenBody = getPosOfClosingCurlyBracket(list, startOfThenBody);
//				BlockExp thenBody = new BlockExp(parse(list.subList(startOfThenBody, endOfThenBody + 1)), lineNumber);
//				BlockExp elseBody = null;
//
//				if (endOfThenBody + 1 < list.size()) {
//					if (list.get(endOfThenBody + 1) instanceof T_Else) {
//						int startOfElseBody = endOfThenBody + 2;
//						int endOfElseBody = getPosOfClosingCurlyBracket(list, startOfElseBody);
//						elseBody = new BlockExp(parse(list.subList(startOfElseBody, endOfElseBody + 1)), lineNumber);
//					}
//				}
//
//				temp.add(new IfExp(new CompExp(left, comp, right, lineNumber), thenBody, elseBody, id, tempLineNo));
//				id++;
//				return temp;
//			}
//
//		}
//		p.errors.add("Error on line " + lineNumber + "\nIf-Statement does not follow if(x) { X } else { Y };");
//		throw new SyntaxException("Error");
//	}

	public ArrayList<Exp> parse_if(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement + offset;
		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);

			offset++;
			
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

				System.out.println("IF: Start: " + startStatement + ", end: " + endOfIf);

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
		throw new SyntaxException("Error");
	}

	public ArrayList<Exp> parse_for(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement + insideIf;

		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);

			IttExp ittExp = parse_itt(list.subList(2, rightBracket));
			
			insideFor++;

			int startOfBody = rightBracket + 1;
			int endOfBody = getPosOfClosingCurlyBracket(list, startOfBody);
			
			currentStatement += 2;
			BlockExp body = new BlockExp(parse(list.subList(startOfBody, endOfBody + 1)), lineNumber);
			body.b.exps.add(new AssignExp(ittExp.itterator, new BinopExp(ittExp.itterator, new Plus(), new IntLiteral(1, lineNumber), lineNumber), lineNumber));
			body.b.exps.add(new GotoExp(startStatement, lineNumber));
			
			int endOfFor = startStatement + body.b.exps.size() + 1;
			body.b.exps.add(0, new ComeFromExp(endOfFor+2, tempLineNo));
			endOfFor++;

			System.out.println("WHILE: Start: " + startStatement + ", end: " + endOfFor);
			
			ArrayList<Exp> temp = new ArrayList<Exp>();
			temp.add(new AssignExp(ittExp.itterator, ittExp.from, tempLineNo));
			temp.add(new ForExp(ittExp, body, endOfFor, tempLineNo));
			temp.addAll(body.b.exps);
			temp.add(new EndForExp(new IttExp(ittExp.itterator, ittExp.to, ittExp.from, lineNumber), startStatement, lineNumber));
			currentStatement++;
			insideFor--;
			return temp;
		}
		p.errors.add("Error on line " + lineNumber + "\nFor-Statement does not follow form for(<var> = i : j) { X };");
		throw new SyntaxException("Bracks incorrect");
	}

//	public ArrayList<Exp> parse_for(List<Token> list) throws SyntaxException {
//		int tempLineNo = lineNumber;
//		if (list.get(1) instanceof T_LeftBracket) {
//			int rightBracket = getPosOfClosingBracket(list, 1);
//
//			IttExp ittExp = parse_itt(list.subList(2, rightBracket));
//			
//			int startOfBody = rightBracket + 1;
//			int endOfBody = getPosOfClosingCurlyBracket(list, startOfBody);
//			
//			BlockExp body = new BlockExp(parse(list.subList(startOfBody, endOfBody + 1)), lineNumber);
//			
//			ArrayList<Exp> temp = new ArrayList<Exp>();
//			temp.add(new ForExp(ittExp, body, id, tempLineNo));
//			id++;
//			return temp;
//		}
//		throw new SyntaxException("");
//	}

//	public ArrayList<Exp> parse_while(List<Token> list) throws SyntaxException {
//		int tempLineNo = lineNumber;
//		int startStatement = currentStatement + insideIf;
//		if (list.get(1) instanceof T_LeftBracket) {
//			int rightBracket = getPosOfClosingBracket(list, 1);
//
//			initialiseVars.add(new ArrayList<String>());
//
//			comparison = true;
//			Exp condition = parse_e(list.subList(2, rightBracket)).get(0);
//			comparison = false;
//			
//			insideIf++;
//			insideWhile++;
//			
//			if (list.get(rightBracket + 1) instanceof T_LeftCurlyBracket) {
//				int startOfThenBody = rightBracket + 1;
//				int endOfThenBody = getPosOfClosingCurlyBracket(list, startOfThenBody);
//				currentStatement+=2;
//				BlockExp thenBody = new BlockExp(parse(list.subList(startOfThenBody, endOfThenBody + 1)), lineNumber);
//				
//				VarExp counter = new VarExp("COUNTER" + insideIf, lineNumber);
//				
//				ArrayList<Exp> compExps = new ArrayList<Exp>();
//				for(String s : initialiseVars.get(insideIf - 1)) {
//					compExps.add(new AssignExp(new VarExp(s, lineNumber), new VarExp(Parser.getRealVarName(s), lineNumber), lineNumber));
//				}
//				thenBody.b.exps.add(new CompilerExp(new Block(compExps), lineNumber));
//				
//				thenBody.b.exps.add(new AssignExp(counter, new BinopExp(counter, new Plus(), new IntLiteral(1, lineNumber), lineNumber), lineNumber));
//				thenBody.b.exps.add(new GotoExp(startStatement, lineNumber));
//				thenBody.b.exps.add(0, new UpdateCounter(counter, tempLineNo));
//				
//				int endOfWhile = startStatement + thenBody.b.exps.size() + 1;
//				
//				thenBody.b.exps.add(0, new ComeFromExp(endOfWhile+2, tempLineNo));
//				endOfWhile++;
//				
//				System.out.println("WHILE: Start: " + startStatement + ", end: " + endOfWhile);
//				
//				ArrayList<Exp> compEx = new ArrayList<Exp>(compExps);
//				compEx.add(new AssignExp(counter, new IntLiteral(0, lineNumber), tempLineNo));
//				
//				ArrayList<Exp> temp = new ArrayList<Exp>();
//				WhileExp e = new WhileExp((CompExp) condition, initialiseVars.get(insideIf - 1), counter, endOfWhile, tempLineNo);
//				temp.add(new CompilerExp(new Block(compEx), tempLineNo));
//				temp.add(e);
//				temp.addAll(thenBody.b.exps);
//				temp.add(new EndWhileExp(counter, startStatement, lineNumber));
//				currentStatement++;
//
//				insideIf--;
//				insideWhile--;
//				return temp;
//			}
//
//		}
//		p.errors.add("Error on line " + lineNumber + "\nIf-Statement does not follow if(x) { X } else { Y };");
//		throw new SyntaxException("Error");
//	}

	// TRY MAKE ANOTHER WHILE PARSE WHICH DOESNT USE _TEMP VARIABLES?
	public ArrayList<Exp> parse_while(List<Token> list) throws SyntaxException {
		int tempLineNo = lineNumber;
		int startStatement = currentStatement + insideIf;
		if (list.get(1) instanceof T_LeftBracket) {
			int rightBracket = getPosOfClosingBracket(list, 1);
			Exp condition = parse_e(list.subList(2, rightBracket)).get(0);
			if (condition instanceof CompExp) {
				if (list.get(rightBracket + 1) instanceof T_LeftCurlyBracket) {
					insideWhile++;
					int startOfThenBody = rightBracket + 1;
					int endOfThenBody = getPosOfClosingCurlyBracket(list, startOfThenBody);
					currentStatement += 2;
					BlockExp thenBody = new BlockExp(parse(list.subList(startOfThenBody, endOfThenBody + 1)), lineNumber);

					VarExp counter = new VarExp("COUNTER" + insideWhile, lineNumber);

					thenBody.b.exps.add(new AssignExp(counter, new BinopExp(counter, new Plus(), new IntLiteral(1, lineNumber), lineNumber), lineNumber));
					thenBody.b.exps.add(new GotoExp(startStatement, lineNumber));
					int endOfWhile = startStatement + thenBody.b.exps.size() + 1;
					thenBody.b.exps.add(0, new ComeFromExp(endOfWhile + 2, tempLineNo));
					endOfWhile++;

					System.out.println("WHILE: Start: " + startStatement + ", end: " + endOfWhile);

					ArrayList<Exp> temp = new ArrayList<Exp>();
					WhileExp e = new WhileExp((CompExp) condition, counter, endOfWhile, tempLineNo);
					temp.add(new AssignExp(counter, new IntLiteral(0, tempLineNo), tempLineNo));
					temp.add(e);
					temp.addAll(thenBody.b.exps);
					temp.add(new EndWhileExp(counter, startStatement, lineNumber));
					currentStatement++;

					insideWhile--;
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
			int rightBracket = containsInstance(list, T_RightBracket.class);
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
		throw new SyntaxException("");
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
		throw new SyntaxException("Incorrect Curly Brackets");
	}

	public static String getRealVarName(String name) {
		while (name.endsWith("_TEMP")) {
			name = name.substring(0, name.length() - 5);
		}
		return name;
	}

}

//class SyntaxException extends Exception {
//	private static final long serialVersionUID = 1L;
//	public String msg;
//
//	public SyntaxException(String _msg) {
//		msg = _msg;
//	}
//}
