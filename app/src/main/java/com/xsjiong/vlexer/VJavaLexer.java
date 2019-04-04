package com.xsjiong.vlexer;

public class VJavaLexer extends VCommonLexer {
	// 这里应该使用Lazy Build，因为可能有子类继承，那时我们应该让子类来Build这个tree（只需要重写getKeywordTrie）
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do",
					"double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
					"int", "interface", "long", "native", "package", "private", "protected", "public", "return", "strictfp", "short",
					"static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
			);
		return KEYWORD_TRIE;
	}

	@Override
	protected boolean isIdentifierStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}

	@Override
	protected boolean isIdentifierPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}

	public VJavaLexer() {
	}

	public VJavaLexer(char[] cs) {
		super(cs);
	}

	@Override
	protected short getWordType(int st, int en) {
		if (isKeyword(S, st, P)) return TYPE_KEYWORD;
		else if (equals(st, P, "true") || equals(st, P, "false")) return TYPE_BOOLEAN;
		else if (equals(st, P, "null")) return TYPE_NULL;
		return TYPE_IDENTIFIER;
	}

	@Override
	protected boolean isWhitespace(char c) {
		return (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f' || c == '\uFFFF');
	}

	@Override
	public short processSymbol(char c) {
		// 我大Java的 >>> 和 >>>=
		if (c == '>') {
			if (P == L) return TYPE_OPERATOR;
			if (S[P] == c) {
				if (++P == L) return TYPE_OPERATOR; // >>
				if (S[P] == '=') ++P; // >>=
				else if (S[P] == '>') {
					if (++P == L) return TYPE_OPERATOR; // >>>
					if (S[P] == '=') { // >>>=
						++P;
						return TYPE_OPERATOR;
					}
				}
			} else if (S[P] == '=') ++P; // >=
			return TYPE_OPERATOR;
		}
		return super.processSymbol(c);
	}
}