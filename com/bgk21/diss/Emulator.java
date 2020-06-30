package com.bgk21.diss;

import java.util.HashMap;
import java.util.Stack;

public class Emulator {

	HashMap<String, Stack<Operation>> register;
	Program p;
	int currentStatement;

	public Emulator(String fileURL) throws RuntimeException {
		System.out.println(fileURL);
		p = new Program(fileURL);
		register = new HashMap<String, Stack<Operation>>();
		currentStatement = 0;
	}

	//ONLY USED FOR JUNIT TESTING
	public Emulator(String text, boolean TEST) throws RuntimeException {
		p = new Program(text, TEST);
		register = new HashMap<String, Stack<Operation>>();
		currentStatement = 0;
	}
	
	public void execute(boolean forwards) throws RuntimeException {
		while (true) {
			if (executeNextStatement(forwards)) {
				// nothing
			} else {
				break;
			}
		}
	}

	// returns true if statement was successfully executed, else returns false
	public boolean executeNextStatement(boolean forwards) throws RuntimeException {
		System.out.println(p.AST.exps.get(currentStatement));
		if (forwards) {
			if (currentStatement < p.AST.exps.size()) {
				executeExp(p.AST.exps.get(currentStatement), forwards);
				currentStatement++;
				if (currentStatement < p.AST.exps.size()) {
					if (p.AST.exps.get(currentStatement) instanceof CompilerExp && p.AST.exps.get(currentStatement - 1) instanceof CompilerExp) {
						executeNextStatement(forwards);
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			if (currentStatement > 0) {
				currentStatement--;
				executeExp(p.AST.exps.get(currentStatement), forwards);
				if (currentStatement - 1 > 0) {
					if (p.AST.exps.get(currentStatement - 1) instanceof CompilerExp && p.AST.exps.get(currentStatement) instanceof CompilerExp) {
						executeNextStatement(forwards);
					}
				}
				return true;
			} else {
				return false;
			}
		}

	}

	public double executeExp(Exp exp, boolean forwards) throws RuntimeException {
		if (exp instanceof AssignExp) {
			return executeAssign((AssignExp) exp, forwards);
		} else if (exp instanceof BinopExp) {
			return executeBinop((BinopExp) exp, forwards);
		} else if (exp instanceof VarExp) {
			return executeVar((VarExp) exp, forwards);
		} else if (exp instanceof DoubleLiteral) {
			return ((DoubleLiteral) exp).n;
		} else if (exp instanceof IfExp) {
			return executeIf((IfExp) exp, forwards);
		} else if (exp instanceof PrintExp) {
			return executePrint((PrintExp) exp, forwards);
		} else if (exp instanceof WhileExp) {
			return executeWhile((WhileExp) exp, forwards);
		} else if (exp instanceof GotoExp) {
			if (forwards)
				return (currentStatement = ((GotoExp) exp).statement - 1);
			return -1;
		} else if (exp instanceof ComeFromExp) {
			if (!forwards)
				return (currentStatement = ((ComeFromExp) exp).statement);
			return -1;
		} else if (exp instanceof CompExp) {
			return executeComp((CompExp) exp);
		} else if (exp instanceof EndIfExp) {
			return executeEndIf((EndIfExp) exp, forwards);
		} else if (exp instanceof CompilerExp) {
			return executeCompilerExp((CompilerExp) exp, forwards);
		} else if (exp instanceof EndWhileExp) {
			return executeEndWhile((EndWhileExp) exp, forwards);
		} else if (exp instanceof ForExp) {
			return executeFor((ForExp) exp, forwards);
		} else if (exp instanceof EndForExp) {
			return executeEndFor((EndForExp) exp, forwards);
		}
		throw new RuntimeException(exp + " was not recognised as an expression");
	}

	public double executeCompilerExp(CompilerExp exp, boolean forwards) throws RuntimeException {
		if (forwards) {
			for (Exp e : exp.b.exps) {
				executeExp(e, forwards);
			}
		} else {
			for (int i = exp.b.exps.size() - 1; i >= 0; i--) {
				executeExp(exp.b.exps.get(i), forwards);
			}
		}

		return 0;
	}

	public double executePrint(PrintExp exp, boolean forwards) throws RuntimeException {
		if (forwards) {
			Display.consoleArea.addRow(Double.toString(executeExp(exp.message, true)));
		} else {
			Display.consoleArea.removeRow(Display.consoleArea.getRows().get(Display.consoleArea.getRows().size() - 1));
		}
		return 0;
	}

	// returns 1 if true, else returns 0
	public double executeComp(CompExp exp) throws RuntimeException {
		double left = executeExp(exp.left, true);
		double right = executeExp(exp.right, true);

		if (exp.comp instanceof Equals)
			if (left == right)
				return 1;
		if (exp.comp instanceof Less)
			if (left < right)
				return 1;
		if (exp.comp instanceof Greater)
			if (left > right)
				return 1;
		if (exp.comp instanceof GreaterEq)
			if (left >= right)
				return 1;
		if (exp.comp instanceof LessEq)
			if (left <= right)
				return 1;
		if (exp.comp instanceof NotEquals)
			if (left != right)
				return 1;
		return 0;
	}

	public double executeFor(ForExp exp, boolean forwards) throws RuntimeException {
		if(forwards) {
			double itt = executeExp(((IttExp)exp.itt).itterator, forwards);
			double to = executeExp(((IttExp)exp.itt).to, forwards);
			
			if(itt == to)
				executeExp(new GotoExp(exp.endOfFor+1, 0), true);
		}
		
		return 0;
	}

	public double executeEndFor(EndForExp exp, boolean forwards) throws RuntimeException {
		double itt = executeExp(((IttExp)exp.itt).itterator, true);
		double to = executeExp(((IttExp)exp.itt).to, true);
		
		if(itt == to) {
			executeExp(new GotoExp(exp.startOfFor+1, 0), true);
		}
		
		return 0;
	}

	public double executeWhile(WhileExp exp, boolean forwards) throws RuntimeException {
		if (forwards) {

			double condition = executeExp(exp.condition, forwards);
			if (condition == 0) {
				executeExp(new GotoExp(exp.endOfWhile, 0), true);
			}
		}

		return 0;
	}

	public double executeEndWhile(EndWhileExp exp, boolean forwards) throws RuntimeException {
		if (!forwards) {
			double condition = executeExp(exp.comp, forwards);
			if (condition == 0) {
				executeExp(new GotoExp(exp.startOfWhile + 1, 0), true);
			}
		}
		return 0;
	}

	public double executeIf(IfExp exp, boolean forwards) throws RuntimeException {
		if (forwards) {
			double condition = executeExp(exp.condition, forwards);
			if (condition == 0) { // if condition false
				if (exp.endOfIf - exp.endOfBody != 1) {
					executeExp(new GotoExp(exp.endOfBody + 1, 0), true);
				} else {
					executeExp(new GotoExp(exp.endOfIf, 0), true);
				}
			}

		}
		return 0;
	}

	public double executeEndIf(EndIfExp exp, boolean forwards) throws RuntimeException {
		if (!forwards) {
			double condition = executeExp(exp.exp.condition, forwards);

			if (condition == 1) {
				executeExp(new GotoExp(exp.exp.endOfBody + 1, 0), true);
			}
		}
		return 0;
	}

	public double executeAssign(AssignExp exp, boolean forwards) throws RuntimeException {
		executeVar(exp.left, forwards);
		double eval = executeExp(exp.right, forwards);
		double difference = eval - Operation.sum(register.get(exp.left.name), true);
		Stack<Operation> stack = register.get(exp.left.name);
		if (forwards) {
			stack.push(new Inc(difference));
		} else {
			if (!stack.isEmpty())
				stack.pop();
		}
		return Operation.sum(register.get(exp.left.name), forwards);
	}

	public double executeBinop(BinopExp exp, boolean forwards) throws RuntimeException {
		double left = executeExp(exp.left, forwards);
		double right = executeExp(exp.right, forwards);

		if (forwards) {
			if (exp.op instanceof Plus)
				return (left + right);
			if (exp.op instanceof Minus)
				return (left - right);
			if (exp.op instanceof Div)
				return (left / right);
			if (exp.op instanceof Times)
				return (left * right);
			if (exp.op instanceof Mod)
				return (left % right);
		} else {
			if (exp.op instanceof Plus)
				return (left - right);
			if (exp.op instanceof Minus)
				return (left + right);
			if (exp.op instanceof Div)
				return (left * right);
			if (exp.op instanceof Times)
				return (left / right);
			if(exp.op instanceof Mod)
				return (left % right);
		}

		throw new RuntimeException("Error: Operator is incorrect");
	}

	public double executeVar(VarExp exp, boolean forwards) {
		if (register.containsKey(exp.name)) {
			if (exp.name.equalsIgnoreCase("counter2")) {
				System.out.println(register.get(exp.name));
			}
			return Operation.sum(register.get(exp.name), forwards);
		} else {
			Stack<Operation> stack = new Stack<Operation>();
			stack.push(new Inc(0));
			register.put(exp.name, new Stack<Operation>());
			return 0;
		}
	}
}

class RuntimeException extends Exception {
	private static final long serialVersionUID = 1L;
	public String msg;

	public RuntimeException(String _msg) {
		msg = _msg;
	}
}
