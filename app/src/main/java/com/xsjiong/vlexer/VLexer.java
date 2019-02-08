package com.xsjiong.vlexer;

import java.lang.reflect.Field;

public abstract class VLexer {
	public static final short UNRESOLVED_TYPE = -2, TYPE_EOF = -1, TYPE_IDENTIFIER = 0, TYPE_KEYWORD = 1, TYPE_NUMBER = 2, TYPE_COMMENT = 3, TYPE_STRING = 4, TYPE_CHAR = 5, TYPE_OPERATOR = 6, TYPE_BOOLEAN = 7, TYPE_ASSIGNMENT = 8,
			TYPE_NULL = 9, TYPE_LEFT_PARENTHESIS = 10, TYPE_RIGHT_PARENTHESIS = 11, TYPE_LEFT_SQUARE_BRACKET = 12, TYPE_RIGHT_SQUARE_BRACKET = 13, TYPE_LEFT_BRACE = 14, TYPE_RIGHT_BRACE = 15, TYPE_SEMICOLON = 16,
			TYPE_COLON = 17, TYPE_PERIOD = 18, TYPE_COMMA = 19;

	protected char[] S;
	protected int P, ST;

	public VLexer() {
		this(new char[0]);
	}

	public VLexer(String s) {
		this(s == null ? new char[0] : s.toCharArray());
	}

	// Notice that we don't copy the array here for higher speed
	public VLexer(char[] s) {
		this.S = s;
		this.P = this.ST = 0;
	}

	public abstract Trie getKeywordTrie();

	protected abstract boolean isWhitespace(char c);

	protected abstract boolean isIdentifierStart(char c);

	protected abstract boolean isIdentifierPart(char c);

	protected abstract short ProcessSymbol(char c);

	public final void setText(String s) {
		setText(s == null ? new char[0] : s.toCharArray());
	}

	public final void setText(char[] cs) {
		this.S = cs;
		this.P = this.ST = 0;
	}

	public final short getNext() {
		ReadSpaces();
		if (P == S.length) return TYPE_EOF;
		ST = P;
		if (isIdentifierStart(S[P])) {
			int st = P;
			do {
				++P;
			} while (P != S.length && isIdentifierPart(S[P]));
			if (isKeyword(S, st, P)) return TYPE_KEYWORD;
			else if (equals(st, P, "true") || equals(st, P, "false")) return TYPE_BOOLEAN;
			else if (equals(st, P, "null")) return TYPE_NULL;
			return TYPE_IDENTIFIER;
		}
		if (Character.isDigit(S[P]) || S[P] == '.') {
			boolean hex = false;
			do {
				if (++P == S.length) break;
				if (S[P] == 'x' && S[P - 1] == '0' && P - 1 == ST) {
					hex = true;
					continue;
				}
			}
			while (Character.isDigit(S[P]) || S[P] == '.' || S[P] == 'e' || (hex && Character.isLetter(S[P])) || ((S[P] == '-' || S[P] == '+') && S[P - 1] == 'e'));
			if (P != S.length && P == ST + 1 && S[ST] == '.') return TYPE_PERIOD;
			return TYPE_NUMBER;
		}
		return ProcessSymbol(S[P++]);
	}

	public final void setPosition(int pos) {
		this.ST = this.P = pos;
	}

	public final boolean isKeyword(char[] cs, int st, int en) {
		return getKeywordTrie().hasWord(cs, st, en);
	}

	public String getTypeName(short type) {
		try {
			for (Field one : this.getClass().getFields())
				if (one.getType() == short.class && one.getShort(null) == type) return one.getName();
			return null;
		} catch (Throwable t) {
			return null;
		}
	}

	protected final boolean equals(int st, int en, String s) {
		for (int i = st; i < en; i++)
			if (S[i] != s.charAt(i - st)) return false;
		return true;
	}

	public final String getLastString() {
		return new String(S, ST, P - ST);
	}

	protected final void ReadSpaces() {
		while (P != S.length && isWhitespace(S[P])) ++P;
	}

	public static class Trie {
		private final short[][] C;
		private final boolean[] L;

		public Trie(int len) {
			C = new short[len][26];
			L = new boolean[len];
		}

		public static Trie BuildTrie(String... ks) {
			int len = 1;
			int i;
			for (i = 0; i < ks.length; i++) len += ks[i].length();
			Trie t = new Trie(len);
			short tot = 0;
			int cur;
			byte c;
			for (String one : ks) {
				for (cur = i = 0; i < one.length(); i++) {
					c = (byte) (one.charAt(i) - 'a');
					if (t.C[cur][c] == 0)
						t.C[cur][c] = ++tot;
					cur = t.C[cur][c];
				}
				t.L[cur] = true;
			}
			return t;
		}

		public boolean hasWord(char[] cs, int st, int en) {
			byte c;
			int cur = 0;
			for (int i = st; i < en; i++) {
				c = (byte) (cs[i] - 'a');
				if (c < 0 || c >= 26) return false;
				if ((cur = C[cur][c]) == 0) return false;
			}
			return L[cur];
		}
	}
}
