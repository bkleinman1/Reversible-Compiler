package com.bgk21.diss;

import java.util.Stack;

public class Operation {
	public static double sum(Stack<Operation> stack, boolean forwards) {
		double total = 0;
		if (stack != null) {
			if (stack.isEmpty())
				return 0;

			for (Operation op : stack) {
				if (op instanceof Inc) {
					if (forwards)
						total += ((Inc) op).n;
					else
						total -= ((Inc) op).n;
				}
			}
		}
		return total;
	}
}

class Inc extends Operation {
	double n;

	public Inc(double n) {
		this.n = n;
	}

	public String toString() {
		return "Inc(" + n + ")";
	}

}