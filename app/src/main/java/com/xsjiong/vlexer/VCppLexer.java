package com.xsjiong.vlexer;

public class VCppLexer extends VCLexer {
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"asm", "do", "if", "return", "typedef", "auto", "double", "inline", "short", "typeid", "bool", "dynamic_cast", "int", "signed", "typename", "break", "else", "long", "sizeof", "union", "case", "enum", "mutable", "static", "unsigned", "catch", "explicit",
					"namespace", "static_cast", "using", "char", "export", "new", "struct", "virtual", "class", "extern", "operator", "switch", "void", "const", "false", "private", "template", "volatile", "const_cast", "float", "protected", "this", "wchar_t", "continue",
					"for", "public", "throw", "while", "default", "friend", "register", "true", "delete", "goto", "reinterpret_cast", "try"
			);
		return KEYWORD_TRIE;
	}

	public VCppLexer() {
	}
}
