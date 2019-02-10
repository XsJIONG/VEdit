package com.xsjiong.vlexer;

import java.lang.reflect.Field;

public abstract class VLexer {
	public static final int EXPAND_SIZE = 128;
	public static final short TYPE_COUNT = 22;
	public static final short UNRESOLVED_TYPE = 21, TYPE_EOF = 20, TYPE_IDENTIFIER = 0, TYPE_KEYWORD = 1, TYPE_NUMBER = 2, TYPE_COMMENT = 3, TYPE_STRING = 4, TYPE_CHAR = 5, TYPE_OPERATOR = 6, TYPE_BOOLEAN = 7, TYPE_ASSIGNMENT = 8,
			TYPE_NULL = 9, TYPE_LEFT_PARENTHESIS = 10, TYPE_RIGHT_PARENTHESIS = 11, TYPE_LEFT_SQUARE_BRACKET = 12, TYPE_RIGHT_SQUARE_BRACKET = 13, TYPE_LEFT_BRACE = 14, TYPE_RIGHT_BRACE = 15, TYPE_SEMICOLON = 16,
			TYPE_COLON = 17, TYPE_PERIOD = 18, TYPE_COMMA = 19;

	protected char[] S;
	protected int P, ST;
	protected short[] D = new short[EXPAND_SIZE + 1];
	protected int[] DS = new int[EXPAND_SIZE + 1];
	protected int[] DE = new int[EXPAND_SIZE + 1];
	protected int L;

	public VLexer() {
	}

	// Notice that we don't copy the array here for higher speed
	public VLexer(char[] s) {
		setText(s);
	}

	public abstract Trie getKeywordTrie();

	protected abstract boolean isWhitespace(char c);

	protected abstract boolean isIdentifierStart(char c);

	protected abstract boolean isIdentifierPart(char c);

	protected abstract short ProcessSymbol(char c);

	public final void copyFrom(VLexer a) {
		this.S = a.S;
		this.P = a.P;
		this.ST = a.ST;
		this.D = a.D;
		this.DS = a.DS;
		this.DE = a.DE;
		this.L = a.L;
	}

	public final void setTextLength(int len) {
		this.L = len;
	}

	// 传入的是修改前光标的位置
	public final void onInsertChars(int pos, int len) {
		if (len == 0) return;
		int part = findPart(pos);
		this.P = DS[part];
		int en = DE[part] + len;
		//if (DS[0] != part && DS[part + 1] == DE[part]) // 前后两个part相连
		//	en = DE[part + 1] + len;
		int afterLen = DS[0] - part;
		short[] afterD = new short[afterLen];
		int[] afterDS = new int[afterLen];
		int[] afterDE = new int[afterLen];
		if (afterLen != 0) {
			System.arraycopy(D, part + 1, afterD, 0, afterLen);
			for (int i = 0; i < afterLen; i++) {
				afterDS[i] = DS[part + i + 1] + len;
				afterDE[i] = DE[part + i + 1] + len;
			}
		}
		DS[0] = part - 1;
		short type;
		while (this.P < en) {
			type = getNext();
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
			DE[DS[0]] = P;
		}
		if (afterLen != 0) {
			int nl = DS[0] + afterLen + 1;
			while (D.length < nl) expandDArray();
			System.arraycopy(afterD, 0, D, DS[0] + 1, afterLen);
			System.arraycopy(afterDS, 0, DS, DS[0] + 1, afterLen);
			System.arraycopy(afterDE, 0, DE, DS[0] + 1, afterLen);
			DS[0] += afterLen;
		}
	}

	public final void onDeleteChars(int pos, int len) {
		if (len > pos) len = pos;
		int part2 = findPart(pos);
		int en = DE[part2] - len;
		pos -= len;
		int part1 = findPart(pos);
		if (part1 != 1) part1--;
		this.P = DS[part1];
		int afterLen = DS[0] - part2;
		short[] afterD = new short[afterLen];
		int[] afterDS = new int[afterLen];
		int[] afterDE = new int[afterLen];
		if (afterLen != 0) {
			System.arraycopy(D, part2 + 1, afterD, 0, afterLen);
			for (int i = 0; i < afterLen; i++) {
				afterDS[i] = DS[part2 + i + 1] - len;
				afterDE[i] = DE[part2 + i + 1] - len;
			}
		}
		DS[0] = part1 - 1;
		short type;
		while (this.P < en) {
			type = getNext();
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
			DE[DS[0]] = P;
		}
		if (afterLen != 0) {
			int nl = DS[0] + afterLen + 1;
			while (D.length < nl) expandDArray();
			System.arraycopy(afterD, 0, D, DS[0] + 1, afterLen);
			System.arraycopy(afterDS, 0, DS, DS[0] + 1, afterLen);
			System.arraycopy(afterDE, 0, DE, DS[0] + 1, afterLen);
			DS[0] += afterLen;
		}
	}

	public final void onTextReferenceUpdate(char[] cs, int len) {
		this.S = cs;
		this.L = len;
	}

	public final int getPartCount() {
		return DS[0];
	}

	public final int findPart(int pos) {
		int l = 1, r = DS[0];
		int mid;
		while (l <= r) {
			mid = (l + r) >> 1;
			if (DS[mid] <= pos)
				l = mid + 1;
			else
				r = mid - 1;
		}
		return r;
	}

	// Start With 1!!!!!!!!!!!
	public final short getPartType(int ind) {
		return D[ind];
	}

	public final int getPartStart(int ind) {
		return DS[ind];
	}

	public final int getPartEnd(int ind) {
		return DE[ind];
	}

	public final String getPartText(int ind) {
		return getText(DS[ind], DE[ind]);
	}

	public final char[] getPartChars(int ind) {
		return getChars(DS[ind], DE[ind]);
	}

	public final short[] getParts() {
		return D;
	}

	public final int[] getPartStarts() {
		return DS;
	}

	public final int[] getPartEnds() {
		return DE;
	}

	public final String getText() {
		return new String(S);
	}

	public final char[] getChars() {
		return S;
	}

	public final String getText(int st, int en) {
		return new String(S, st, en - st);
	}

	public final char[] getChars(int st, int en) {
		char[] ret = new char[en - st];
		System.arraycopy(S, st, ret, 0, ret.length);
		return ret;
	}

	public final void clearCache() {
		this.P = this.DS[0] = 0;
	}

	private void expandDArray() {
		short[] nd = new short[D.length + EXPAND_SIZE];
		System.arraycopy(D, 0, nd, 0, D.length);
		D = nd;
		nd = null;
		int[] nd2 = new int[D.length];
		System.arraycopy(DS, 0, nd2, 0, DS.length);
		DS = nd2;
		nd2 = new int[D.length];
		System.arraycopy(DE, 0, nd2, 0, DE.length);
		DE = nd2;
		nd2 = null;
		System.gc();
	}

	public final void setText(char[] cs) {
		this.S = cs;
		this.P = this.DS[0] = 0;
		short type;
		while ((type = getNext()) != TYPE_EOF) {
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
			DE[DS[0]] = P;
		}
	}

	private short getNext() {
		ReadSpaces();
		if (P == L) return TYPE_EOF;
		ST = P;
		if (isIdentifierStart(S[P])) {
			int st = P;
			do {
				++P;
			} while (P != L && isIdentifierPart(S[P]));
			if (isKeyword(S, st, P)) return TYPE_KEYWORD;
			else if (equals(st, P, "true") || equals(st, P, "false")) return TYPE_BOOLEAN;
			else if (equals(st, P, "null")) return TYPE_NULL;
			return TYPE_IDENTIFIER;
		}
		if (Character.isDigit(S[P]) || S[P] == '.') {
			boolean hex = false;
			do {
				if (++P == L) break;
				if (S[P] == 'x' && S[P - 1] == '0' && P - 1 == ST) {
					hex = true;
					continue;
				}
			}
			while (Character.isDigit(S[P]) || S[P] == '.' || S[P] == 'e' || (hex && Character.isLetter(S[P])) || ((S[P] == '-' || S[P] == '+') && S[P - 1] == 'e'));
			if (P != L && P == ST + 1 && S[ST] == '.') return TYPE_PERIOD;
			return TYPE_NUMBER;
		}
		return ProcessSymbol(S[P++]);
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
		if (en - st != s.length()) return false;
		for (int i = st; i < en; i++)
			if (S[i] != s.charAt(i - st)) return false;
		return true;
	}

	public final String getLastString() {
		return new String(S, ST, P - ST);
	}

	protected final void ReadSpaces() {
		while (P != L && isWhitespace(S[P])) ++P;
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
