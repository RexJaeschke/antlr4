package org.antlr.v4.test.rt.java;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestLexerErrors extends BaseTest {

	@Test
	public void testInvalidCharAtStart() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'a' 'b' ;";
		String found = execLexer("L.g4", grammar, "L", "x");
		assertEquals("[@0,1:0='<EOF>',<-1>,1:1]\n", found);
		assertEquals("line 1:0 token recognition error at: 'x'\n", this.stderrDuringParse);
	}

	@Test
	public void testStringsEmbeddedInActions_1() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "ACTION2 : '[' (STRING | ~'\"')*? ']';\n" +
	                  "STRING : '\"' ('\\\"' | .)*? '\"';\n" +
	                  "WS : [ \\t\\r\\n]+ -> skip;";
		String found = execLexer("L.g4", grammar, "L", "[\"foo\"]");
		assertEquals("[@0,0:6='[\"foo\"]',<1>,1:0]\n" + 
	              "[@1,7:6='<EOF>',<-1>,1:7]\n", found);
		assertNull(this.stderrDuringParse);
	}

	@Test
	public void testStringsEmbeddedInActions_2() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "ACTION2 : '[' (STRING | ~'\"')*? ']';\n" +
	                  "STRING : '\"' ('\\\"' | .)*? '\"';\n" +
	                  "WS : [ \\t\\r\\n]+ -> skip;";
		String found = execLexer("L.g4", grammar, "L", "[\"foo]");
		assertEquals("[@0,6:5='<EOF>',<-1>,1:6]\n", found);
		assertEquals("line 1:0 token recognition error at: '[\"foo]'\n", this.stderrDuringParse);
	}

	@Test
	public void testEnforcedGreedyNestedBrances_1() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "ACTION : '{' (ACTION | ~[{}])* '}';\n" +
	                  "WS : [ \\r\\n\\t]+ -> skip;";
		String found = execLexer("L.g4", grammar, "L", "{ { } }");
		assertEquals("[@0,0:6='{ { } }',<1>,1:0]\n" + 
	              "[@1,7:6='<EOF>',<-1>,1:7]\n", found);
		assertNull(this.stderrDuringParse);
	}

	@Test
	public void testEnforcedGreedyNestedBrances_2() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "ACTION : '{' (ACTION | ~[{}])* '}';\n" +
	                  "WS : [ \\r\\n\\t]+ -> skip;";
		String found = execLexer("L.g4", grammar, "L", "{ { }");
		assertEquals("[@0,5:4='<EOF>',<-1>,1:5]\n", found);
		assertEquals("line 1:0 token recognition error at: '{ { }'\n", this.stderrDuringParse);
	}

	@Test
	public void testInvalidCharAtStartAfterDFACache() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'a' 'b' ;";
		String found = execLexer("L.g4", grammar, "L", "abx");
		assertEquals("[@0,0:1='ab',<1>,1:0]\n" + 
	              "[@1,3:2='<EOF>',<-1>,1:3]\n", found);
		assertEquals("line 1:2 token recognition error at: 'x'\n", this.stderrDuringParse);
	}

	@Test
	public void testInvalidCharInToken() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'a' 'b' ;";
		String found = execLexer("L.g4", grammar, "L", "ax");
		assertEquals("[@0,2:1='<EOF>',<-1>,1:2]\n", found);
		assertEquals("line 1:0 token recognition error at: 'ax'\n", this.stderrDuringParse);
	}

	@Test
	public void testInvalidCharInTokenAfterDFACache() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'a' 'b' ;";
		String found = execLexer("L.g4", grammar, "L", "abax");
		assertEquals("[@0,0:1='ab',<1>,1:0]\n" + 
	              "[@1,4:3='<EOF>',<-1>,1:4]\n", found);
		assertEquals("line 1:2 token recognition error at: 'ax'\n", this.stderrDuringParse);
	}

	@Test
	public void testDFAToATNThatFailsBackToDFA() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'ab' ;\n" +
	                  "B : 'abc' ;";
		String found = execLexer("L.g4", grammar, "L", "ababx");
		assertEquals("[@0,0:1='ab',<1>,1:0]\n" + 
	              "[@1,2:3='ab',<1>,1:2]\n" + 
	              "[@2,5:4='<EOF>',<-1>,1:5]\n", found);
		assertEquals("line 1:4 token recognition error at: 'x'\n", this.stderrDuringParse);
	}

	@Test
	public void testDFAToATNThatMatchesThenFailsInATN() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'ab' ;\n" +
	                  "B : 'abc' ;\n" +
	                  "C : 'abcd' ;";
		String found = execLexer("L.g4", grammar, "L", "ababcx");
		assertEquals("[@0,0:1='ab',<1>,1:0]\n" + 
	              "[@1,2:4='abc',<2>,1:2]\n" + 
	              "[@2,6:5='<EOF>',<-1>,1:6]\n", found);
		assertEquals("line 1:5 token recognition error at: 'x'\n", this.stderrDuringParse);
	}

	@Test
	public void testErrorInMiddle() throws Exception {
		String grammar = "lexer grammar L;\n" +
	                  "A : 'abc' ;";
		String found = execLexer("L.g4", grammar, "L", "abx");
		assertEquals("[@0,3:2='<EOF>',<-1>,1:3]\n", found);
		assertEquals("line 1:0 token recognition error at: 'abx'\n", this.stderrDuringParse);
	}

	@Test
	public void testLexerExecDFA() throws Exception {
		String grammar = "grammar L;\n" +
	                  "start : ID ':' expr;\n" +
	                  "expr : primary expr? {} | expr '->' ID;\n" +
	                  "primary : ID;\n" +
	                  "ID : [a-z]+;";
		String found = execLexer("L.g4", grammar, "LLexer", "x : x");
		assertEquals("[@0,0:0='x',<3>,1:0]\n" + 
	              "[@1,2:2=':',<1>,1:2]\n" + 
	              "[@2,4:4='x',<3>,1:4]\n" + 
	              "[@3,5:4='<EOF>',<-1>,1:5]\n", found);
		assertEquals("line 1:1 token recognition error at: ' '\nline 1:3 token recognition error at: ' '\n", this.stderrDuringParse);
	}


}