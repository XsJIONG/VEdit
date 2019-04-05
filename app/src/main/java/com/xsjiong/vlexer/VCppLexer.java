package com.xsjiong.vlexer;

public class VCppLexer extends VCLexer {
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"asm", "do", "if", "return", "typedef", "auto", "double", "inline", "short", "typeid", "bool", "int", "signed", "typename", "break", "else", "long", "sizeof", "union", "case", "enum", "mutable", "static", "unsigned", "catch", "explicit",
					"namespace", "using", "char", "export", "struct", "virtual", "class", "extern", "operator", "switch", "void", "const", "false", "private", "template", "volatile", "float", "protected", "this", "continue",
					"for", "public", "throw", "while", "default", "friend", "register", "true", "delete", "goto", "try"
			);
		return KEYWORD_TRIE;
	}

	public VCppLexer() {
	}

	public VCppLexer(char[] cs) {
		super(cs);
	}
}
