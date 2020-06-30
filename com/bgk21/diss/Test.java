package com.bgk21.diss;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class Test {

	// LEXER TESTS

	@org.junit.jupiter.api.Test
	void testIdentifierLex() {
		Lexer lexer = new Lexer();
		String input = "var_1";
		Token actual = lexer.lex(input).get(0);
		Token expected = new T_Identifier("var_1");
		assertEquals(actual, expected);
	}

	@org.junit.jupiter.api.Test
	void testDoubleLex() {
		Lexer lexer = new Lexer();
		String input = "30.5";
		Token actual = lexer.lex(input).get(0);
		Token expected = new T_Double(30.5);
		assertEquals(actual, expected);
	}

	@org.junit.jupiter.api.Test
	void testKeywordsLex() {
		Lexer lexer = new Lexer();
		String input = "if else for while print";
		ArrayList<Token> actual = lexer.lex(input);
		ArrayList<Token> expected = new ArrayList<Token>();
		expected.add(new T_If());
		expected.add(new T_Else());
		expected.add(new T_For());
		expected.add(new T_While());
		expected.add(new T_Print());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(actual.get(i), expected.get(i));
		}

	}

	@org.junit.jupiter.api.Test
	void testSpecialCharacters() {
		Lexer lexer = new Lexer();
		String input = ";(){}:-+=/*%==!=";
		ArrayList<Token> actual = lexer.lex(input);
		ArrayList<Token> expected = new ArrayList<Token>();
		expected.add(new T_Semicolon());
		expected.add(new T_LeftBracket());
		expected.add(new T_RightBracket());
		expected.add(new T_LeftCurlyBracket());
		expected.add(new T_RightCurlyBracket());
		expected.add(new T_Colon());
		expected.add(new T_Minus());
		expected.add(new T_Plus());
		expected.add(new T_EqualDefines());
		expected.add(new T_Div());
		expected.add(new T_Times());
		expected.add(new T_Mod());
		expected.add(new T_Equal());
		expected.add(new T_NotEqual());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(actual.get(i), expected.get(i));
		}
	}

	@org.junit.jupiter.api.Test
	void testLongestMatchLex() {
		Lexer lexer = new Lexer();
		String input = "=== forex whileident ";
		ArrayList<Token> actual = lexer.lex(input);
		ArrayList<Token> expected = new ArrayList<Token>();
		expected.add(new T_Equal());
		expected.add(new T_EqualDefines());
		expected.add(new T_Identifier("forex"));
		expected.add(new T_Identifier("whileident"));
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(actual.get(i), expected.get(i));
		}
	}

	@org.junit.jupiter.api.Test
	void testIncorrectStrings() {
		Lexer lexer = new Lexer();
		String input = ".?@€£#$^";
		lexer.lex(input);
		assertTrue(lexer.errors.size() == input.length());
	}
	
	
	//PARSER TESTS
	
	@org.junit.jupiter.api.Test
	void testVariableParse() {
		String input = "{x;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new VarExp("x", 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMultilineParse() {
		String input = "{x;y;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new VarExp("x", 0));
			assertEquals(p.AST.exps.get(1), new VarExp("y", 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testForParse() {
		String input = "{for(x = 1:2){x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new AssignExp(null, null, 0));
			assertEquals(p.AST.exps.get(1), new ForExp(null, null, 0, 0));
			assertEquals(p.AST.exps.get(2), new ComeFromExp(0, 0));
			assertEquals(p.AST.exps.get(3), new VarExp("x", 0));
			assertEquals(p.AST.exps.get(4), new AssignExp(null, null, 0));
			assertEquals(p.AST.exps.get(5), new GotoExp(0, 0));
			assertEquals(p.AST.exps.get(6), new EndForExp(null, 0, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testWhileParse() {
		String input = "{while(x<1){x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new AssignExp(null, null, 0));
			assertEquals(p.AST.exps.get(1), new WhileExp(null, null, 0, 0));
			assertEquals(p.AST.exps.get(2), new ComeFromExp(0, 0));
			assertEquals(p.AST.exps.get(3), new VarExp("x", 0));
			assertEquals(p.AST.exps.get(4), new AssignExp(null, null, 0));
			assertEquals(p.AST.exps.get(5), new GotoExp(0, 0));
			assertEquals(p.AST.exps.get(6), new EndWhileExp(null, 0, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIfParse() {
		String input = "{if(x<1){x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new CompilerExp(new Exp(0), 0));
			assertEquals(p.AST.exps.get(1), new IfExp(null, null, 0, 0, 0));
			assertEquals(p.AST.exps.get(2), new VarExp("x", 0));
			assertEquals(p.AST.exps.get(3), new GotoExp(0, 0));
			assertEquals(p.AST.exps.get(4), new ComeFromExp(0, 0));
			assertEquals(p.AST.exps.get(5), new EndIfExp(null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIfElseParse() {
		String input = "{if(x<1){x;} else {y;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new CompilerExp(new Exp(0), 0));
			assertEquals(p.AST.exps.get(1), new IfExp(null, null, 0, 0, 0));
			assertEquals(p.AST.exps.get(2), new VarExp("x", 0));
			assertEquals(p.AST.exps.get(3), new GotoExp(0, 0));
			assertEquals(p.AST.exps.get(4), new ComeFromExp(0, 0));
			assertEquals(p.AST.exps.get(5), new VarExp("y", 0));
			assertEquals(p.AST.exps.get(6), new EndIfExp(null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	
	@org.junit.jupiter.api.Test
	void testPrintParse() {
		String input = "{print(5);}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new PrintExp(null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}

	@org.junit.jupiter.api.Test
	void testCompParse() {
		String input = "{x<5;x>5;x==5;x!=5;x<=5;x>=5;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new CompExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(1), new CompExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(2), new CompExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(3), new CompExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(4), new CompExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(5), new CompExp(null, null, null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testBinopParse() {
		String input = "{x+5;x-5;x*5;x/5;x%5;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new BinopExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(1), new BinopExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(2), new BinopExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(3), new BinopExp(null, null, null, 0));
			assertEquals(p.AST.exps.get(4), new BinopExp(null, null, null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testEqualDefineParse() {
		String input = "{x=5;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			assertEquals(p.AST.exps.get(0), new AssignExp(null, null, 0));
		} catch (SyntaxException e) {
			fail("Parse Failed");
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIncorrectCurlyBracketParse() {
		String input = "{";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Input not in form \"{ <ene> }\""));
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMissingSemicolonParse() {
		String input = "{x}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Missing semicolon"));
		}
	}

	@org.junit.jupiter.api.Test
	void testIncorrectIfParse() {
		String input = "{if(x<5{x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Incorrect Brackets"));
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIncorrectWhileParse() {
		String input = "{while(x<5{x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Incorrect Brackets"));
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIncorrectForParse() {
		String input = "{for(x<5){x;};}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Error parsing for"));
		}
	}
	
	@org.junit.jupiter.api.Test
	void testEmptyPrintParse() {
		String input = "{print();}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Token list size is 0"));
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIncorrectAssignParse() {
		String input = "{5=5;}";
		Program p = new Program(input, true);
		try {
			p.lexAndParse();
			fail("Parsed an incorrect grammar");
		} catch (SyntaxException e) {
			assertTrue(e.msg.equals("Error with token list"));
		}
	}
	
	//RUN-TIME TESTS
	
	@org.junit.jupiter.api.Test
	void testAssignment() {
		String input = "{x = 5;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testLessthan() {
		String input = "{x = 5; if(x<10){x=10;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 10);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMorethan() {
		String input = "{x = 5; if(x>1){x=10;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 10);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testLessEqual() {
		String input = "{x = 10; if(x<=10){x=20;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 20);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMoreEqual() {
		String input = "{x = 10; if(x>=10){x=20;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 20);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testNotEqual() {
		String input = "{x = 10; if(x!=5){x=20;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 20);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testAddition() {
		String input = "{x = 5 + 3;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 8);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testSubtraction() {
		String input = "{x = 5 - 3;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 2);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMultiplication() {
		String input = "{x = 5 * 3;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 15);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testDivision() {
		String input = "{x = 5 / 2;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 2.5);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testModulo() {
		String input = "{x = 5 % 2;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 1);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	
	@org.junit.jupiter.api.Test
	void testCondition() {
		String input = "{x = 5; if(x==5){x=10;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 10);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testBranching() {
		String input = "{x = 5; if(x==1){x=10;}else{x=15;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 15);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testFor() {
		String input = "{for(x = 1:5){x;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testWhile() {
		String input = "{x = 1; while(x < 5){x = x + 1;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	
	//REVERSE EXECUTION TESTS
	
	@org.junit.jupiter.api.Test
	void testAssignReverse() {
		String input = "{x = 5;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIfReverse() {
		String input = "{x = 5; if(x == 5) {x = 10;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 10);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testIfElseReverse() {
		String input = "{x = 5; if(x == 10) {x = 10;} else {x = 20;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 20);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testForReverse() {
		String input = "{for(x = 1:5){x;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testWhileReverse() {
		String input = "{x = 0; while(x < 5) {x = x + 1;};}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testPlusReverse() {
		String input = "{x = 5; x = x + 5;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 10);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMinusReverse() {
		String input = "{x = 10; x = x - 5;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 5);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testMultiplyReverse() {
		String input = "{x = 5; x = x * 10;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 50);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testDivideReverse() {
		String input = "{x = 5; x = x / 5;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 1);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
	@org.junit.jupiter.api.Test
	void testModReverse() {
		String input = "{x = 10; x = x % 3;}";
		Emulator e;
		try {
			e = new Emulator(input, true);
			e.p.lexAndParse();
			e.execute(true);
			assertTrue(Operation.sum(e.register.get("x"), true) == 1);
			e.execute(false);
			assertTrue(Operation.sum(e.register.get("x"), true) == 0);
		} catch (RuntimeException | SyntaxException exception) {
			exception.printStackTrace();
		}
	}
	
}
