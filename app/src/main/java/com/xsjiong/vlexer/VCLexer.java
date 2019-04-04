package com.xsjiong.vlexer;

public class VCLexer extends VCommonLexer {
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "restrict",
					"register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "inline"
			);
		return KEYWORD_TRIE;
	}

	public VCLexer() {
	}

	public VCLexer(char[] cs) {
		super(cs);
	}

	@Override
	protected short getWordType(int st, int en) {
		if (isKeyword(S, st, P)) return TYPE_KEYWORD;
		// No bool in C!!!!
		return TYPE_IDENTIFIER;
	}

	@Override
	protected boolean isWhitespace(char c) {
		return (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f' || c == '\uFFFF');
	}

	@Override
	protected boolean isIdentifierStart(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
	}

	@Override
	protected boolean isIdentifierPart(char c) {
		return isIdentifierStart(c);
	}

	@Override
	public short specialJudge() {
		if (S[P] == '#' && isStartOfLine(P)) {
			do {
				++P;
			} while (P != L && S[P] != '\n');
			return TYPE_PREPROCESSOR_COMMAND;
		}
		return super.specialJudge();
	}
}