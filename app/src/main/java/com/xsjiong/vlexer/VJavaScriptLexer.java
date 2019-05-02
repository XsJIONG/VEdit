package com.xsjiong.vlexer;

public class VJavaScriptLexer extends VJavaLexer {
	// 为什么这里要Lazy Build？参看我的父亲
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"break", "case", "continue", "default", "delete", "do", "else", "for", "function", "if", "in", "let", "new", "return", "switch",
					"this", "typeof", "var", "void", "while", "with", "yield", "catch", "const", "debugger", "finally", "instanceof", "throw", "try"
			);
		return KEYWORD_TRIE;
	}

	public VJavaScriptLexer() {
	}

	@Override
	public short processSymbol(char c) {
		if (c == '\'') {
			// JS的世界里没有char
			boolean z = false;
			do {
				if (S[P] == '\n') return TYPE_STRING;
				if (P == L) return TYPE_STRING;
				if (S[P] == '\\')
					z = !z;
				else if (S[P] == '\'' && !z) {
					++P;
					return TYPE_STRING;
				} else if (z) z = false;
				++P;
			} while (true);
		}
		if (c == '=' && S[P] == '>') // =>
			return TYPE_OPERATOR;
		return super.processSymbol(c);
	}
}