package com.bgk21.diss;

import java.util.ArrayList;

class Block {
	public ArrayList<Exp> exps;

	public Block(ArrayList<Exp> exps) {
		assert (exps.size() > 0);
		this.exps = exps;
	}

	public String toString() {
		String s = "";
		int i = 0;
		for (Exp exp : exps) {
			s += i + ": " + exp + "\n";
			i++;
		}
		return s;
	}
}

class Exp {
	int lineNumber;

	public Exp(int lineNo) {
		this.lineNumber = lineNo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
	
}

class IntLiteral extends Exp {
	public int n;

	IntLiteral(int n, int lineNumber) {
		super(lineNumber);
		this.n = n;
	}

	public String toString() {
		return "IntLiteral(" + n + ")";
	}
}

class DoubleLiteral extends Exp {
	public double n;
	
	DoubleLiteral(double n, int lineNumber) {
		super(lineNumber);
		this.n = n;
	}
	public String toString() {
		return "Double(" + n + ")";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(n);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoubleLiteral other = (DoubleLiteral) obj;
		if (Double.doubleToLongBits(n) != Double.doubleToLongBits(other.n))
			return false;
		return true;
	}
	
	
}

class BlockExp extends Exp {
	public Block b;

	public BlockExp(Block b, int lineNumber) {
		super(lineNumber);
		this.b = b;
	}
}

class VarExp extends Exp {
	String name;

	public VarExp(String name, int lineNumber) {
		super(lineNumber);
		this.name = name;
	}

	public String toString() {
		return "VarExp(" + "\"" + name + "\"" + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarExp other = (VarExp) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}

class AssignExp extends Exp {
	VarExp left;
	Exp right;

	public AssignExp(VarExp left, Exp right, int lineNumber) {
		super(lineNumber);
		this.left = left;
		this.right = right;
	}

	public String toString() {
		return "AssignExp(" + left + ", " + right + ")";
	}
}

class BinopExp extends Exp {
	Exp left;
	Binop op;
	Exp right;

	public BinopExp(Exp left, Binop op, Exp right, int lineNumber) {
		super(lineNumber);
		this.left = left;
		this.op = op;
		this.right = right;
	}

	public String toString() {
		return "BinopExp(" + left + ", " + op + ", " + right + ")";
	}
}

class CompExp extends Exp {
	Exp left;
	Comp comp;
	Exp right;

	public CompExp(Exp left, Comp comp, Exp right, int lineNumber) {
		super(lineNumber);
		this.left = left;
		this.comp = comp;
		this.right = right;
	}

	public String toString() {
		return "CompExp(" + left + ", " + comp + ", " + right + ")";
	}
}

class IfExp extends Exp {
	CompExp condition;
	ArrayList<String> vars;
	int endOfBody;
	int endOfIf;

	public IfExp(CompExp condition, ArrayList<String> vars, int endOfBody, int endOfIf, int lineNumber) {
		super(lineNumber);
		this.condition = condition;
		this.vars = vars;
		this.endOfBody = endOfBody;
		this.endOfIf = endOfIf;
	}

	public String toString() {
		return "IfExp(" + condition + ")";
	}
}

class EndIfExp extends Exp {
	IfExp exp;

	public EndIfExp(IfExp exp, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
	}

	public String toString() {
		return "EndIfExp(" + exp.condition + ")";
	}
}

class ForExp extends Exp {
	Exp itt;
	Exp body;
	int endOfFor;

	public ForExp(Exp ittExp, BlockExp body, int endOfFor, int lineNumber) {
		super(lineNumber);
		this.itt = ittExp;
		this.body = body;
		this.endOfFor = endOfFor;
	}
	
	public String toString() {
		return "For(" + itt + ")";
	}
}

class EndForExp extends Exp {
	Exp itt;
	int startOfFor; 
	public EndForExp(Exp ittExp, int startOfFor, int lineNumber) {
		super(lineNumber);
		this.itt = ittExp;
		this.startOfFor = startOfFor;
	}
	public String toString() {
		return "EndFor(" + itt + ")";
	}
}

class IttExp extends Exp {
	VarExp itterator;
	Exp from;
	Exp to;

	public IttExp(VarExp itterator, Exp from, Exp to, int lineNumber) {
		super(lineNumber);
		this.itterator = itterator;
		this.from = from;
		this.to = to;
	}
	public String toString() {
		return itterator + " = " + from + ":" + to;
	}
}

class WhileExp extends Exp {
	CompExp condition;
	int endOfWhile;
	VarExp counter;

	public WhileExp(CompExp condition, VarExp counter, int endOfWhile, int lineNumber) {
		super(lineNumber);
		this.condition = condition;
		this.endOfWhile = endOfWhile;
		this.counter = counter;
	}

	public String toString() {
		return "WhileExp(" + condition + ")";
	}
}

class EndWhileExp extends Exp {
	CompExp comp;
	int startOfWhile;

	public EndWhileExp(VarExp counter, int startOfWhile, int lineNumber) {
		super(lineNumber);
		this.comp = new CompExp(counter, new Greater(), new DoubleLiteral(0, lineNumber), lineNumber);
		this.startOfWhile = startOfWhile;
	}

	public String toString() {
		return "EndWhileExp(" + comp + ")";
	}
}

class PrintExp extends Exp {
	Exp message;

	public PrintExp(Exp msg, int lineNumber) {
		super(lineNumber);
		this.message = msg;
	}

	public String toString() {
		return "PrintExp(" + message + ")";
	}
}

class GotoExp extends Exp {
	int statement;

	public GotoExp(int statement, int lineNumber) {
		super(lineNumber);
		this.statement = statement;
	}

	public String toString() {
		return "GotoExp(" + statement + ")";
	}
}

class ComeFromExp extends Exp {
	int statement;

	public ComeFromExp(int statement, int lineNumber) {
		super(lineNumber);
		this.statement = statement;
	}

	public String toString() {
		return "ComeFromExp(" + statement + ")";
	}
}

//A block of expressions which is executed in one step
class CompilerExp extends Exp {
	public Block b;

	public CompilerExp(Block b, int lineNumber) {
		super(lineNumber);
		this.b = b;
	}

	public CompilerExp(Exp e, int lineNumber) {
		super(lineNumber);
		ArrayList<Exp> exps = new ArrayList<Exp>();
		exps.add(e);
		this.b = new Block(exps);
	}

	public String toString() {
		String s = "CompilerExp {\n";
		for (Exp e : b.exps) {
			s += "\t" + e + "\n";
		}
		return s + "}";
	}
}

class Binop {
}

class Plus extends Binop {
	public Plus() {
	}
}

class Minus extends Binop {
	public Minus() {
	}
}

class Times extends Binop {
	public Times() {
	}
}

class Div extends Binop {
	public Div() {
	}
}

class Mod extends Binop {
	public Mod() {
	}
}

class LeftBracket extends Binop {
}
class RightBracket extends Binop {
}


class Comp {
	public String toString() {
		return this.getClass().getSimpleName();
	}
}

class Less extends Comp {
	public Less() {
	}
}

class LessEq extends Comp {
	public LessEq() {
	}
}

class Equals extends Comp {
	public Equals() {
	}
}

class NotEquals extends Comp {
	public NotEquals() {
	}
}

class Greater extends Comp {
	public Greater() {
	}
}

class GreaterEq extends Comp {
	public GreaterEq() {
	}
}
