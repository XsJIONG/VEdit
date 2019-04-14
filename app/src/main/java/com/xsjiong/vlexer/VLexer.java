package com.xsjiong.vlexer;

import java.lang.reflect.Field;

public abstract class VLexer {
	public static final int EXPAND_SIZE = 128;
	public static final int TOTAL_COUNT = 26;
	public static final short TYPE_TAG_END = 25, TYPE_TAG_START = 24, TYPE_SIMPLE_TAG = 23, TYPE_PREPROCESSOR_COMMAND = 22, UNRESOLVED_TYPE = 21, TYPE_EOF = 20, TYPE_IDENTIFIER = 0, TYPE_KEYWORD = 1, TYPE_NUMBER = 2, TYPE_COMMENT = 3, TYPE_STRING = 4, TYPE_CHAR = 5, TYPE_OPERATOR = 6, TYPE_BOOLEAN = 7, TYPE_ASSIGNMENT = 8,
			TYPE_NULL = 9, TYPE_LEFT_PARENTHESIS = 10, TYPE_RIGHT_PARENTHESIS = 11, TYPE_LEFT_SQUARE_BRACKET = 12, TYPE_RIGHT_SQUARE_BRACKET = 13, TYPE_LEFT_BRACE = 14, TYPE_RIGHT_BRACE = 15, TYPE_SEMICOLON = 16,
			TYPE_COLON = 17, TYPE_PERIOD = 18, TYPE_COMMA = 19, FAILED = -1;

	public char[] S;
	public int P, ST;
	public short[] D = new short[EXPAND_SIZE + 1];
	public int[] DS = new int[EXPAND_SIZE + 1];
	public int L;

	public VLexer() {
	}

	// Notice that we don't copy the array here for higher speed
	public VLexer(char[] s) {
		setText(s);
	}

	public final void copyFrom(VLexer a) {
		this.S = a.S;
		this.P = a.P;
		this.ST = a.ST;
		this.D = a.D;
		this.DS = a.DS;
		this.L = a.L;
	}

	public final void readString(char type) {
		boolean z = false;
		do {
			if (S[P] == '\n') return;
			if (P == L) return;
			if (S[P] == '\\')
				z = !z;
			else if (S[P] == type && !z) {
				++P;
				return;
			} else if (z) z = false;
			++P;
		} while (true);
	}

	public final void setTextLength(int len) {
		this.L = len;
	}

	protected final void ReadSpaces() {
		while (P != L && isWhitespace(S[P])) ++P;
	}

	// 传入的是修改前光标的位置
	public final void onInsertChars(int pos, int len) {
		if (len == 0) return;
		if (L - len == 0) {
			parseAll();
			return;
		}
		int part = findPart(pos);
		if (pos == DS[part]) part--;
		if (part == 0) return;
		this.P = Math.min(DS[part], pos);
//		int en = DE[part] + len;
		int afterLen = DS[0] - part;
		short[] afterD = new short[afterLen];
		int[] afterDS = new int[afterLen];
		int[] afterDE = new int[afterLen];
		if (afterLen != 0) {
			System.arraycopy(D, part + 1, afterD, 0, afterLen);
			for (int i = 0; i < afterLen; i++)
				afterDS[i] = DS[part + i + 1] + len;
		}
		DS[0] = Math.max(part - 1, 0);
		short type;
		int i = 0;
//		while (this.P < en) {
		while (true) {
			type = getNext();
			if (type == TYPE_EOF) break;
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
			if (P == L) return;
			while (i != afterLen && P > afterDE[i]) i++;
			if (i != afterLen && ST == afterDS[i] && P == afterDE[i] && type == afterD[i]) break;
		}
		if (afterLen != 0) {
			int cplen = afterLen - i;
			int nl = DS[0] + cplen - 1;
			while (D.length < nl) expandDArray();
			System.arraycopy(afterD, i, D, DS[0], cplen);
			System.arraycopy(afterDS, i, DS, DS[0], cplen);
			DS[0] = nl;
		}
	}

	public final void onDeleteChars(int pos, int len) {
		if (len > pos) len = pos;
		int part2 = findPart(pos);
//		int en = DE[part2] - len;
		pos -= len;
		int part1 = findPart(pos);
		if (pos == DS[part1]) part1--;
		if (part1 == 0) return; // MARK HERE
		this.P = Math.min(DS[part1], pos);
		int afterLen = DS[0] - part2;
		short[] afterD = new short[afterLen];
		int[] afterDS = new int[afterLen];
		int[] afterDE = new int[afterLen];
		if (afterLen != 0) {
			System.arraycopy(D, part2 + 1, afterD, 0, afterLen);
			for (int i = 0; i < afterLen; i++)
				afterDS[i] = DS[part2 + i + 1] - len;
		}
		DS[0] = part1 - 1;
		int i = 0;
		short type;
//		while (this.P < en) {
		while (true) {
			type = getNext();
			if (type == TYPE_EOF) break;
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
			if (P == L) return;
			while (i != afterLen && P > afterDE[i]) i++;
			if (i != afterLen && ST == afterDS[i] && P == afterDE[i] && type == afterD[i]) break;
		}
		if (afterLen != 0) {
			int cplen = afterLen - i;
			int nl = DS[0] + cplen - 1;
			while (D.length < nl) expandDArray();
			System.arraycopy(afterD, i, D, DS[0], cplen);
			System.arraycopy(afterDS, i, DS, DS[0], cplen);
			DS[0] = nl;
		}
	}

	public final void onTextReferenceUpdate(char[] cs, int len) {
		this.S = cs;
		this.L = len;
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

	/*public final int getPartStart(int ind) {
		if (ind > DS[0]) return Integer.MAX_VALUE;
		return DS[ind];
	}*/

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
		nd2 = null;
		System.gc();
	}

	public final void setText(char[] cs) {
		this.S = cs;
		this.L = cs.length;
		parseAll();
	}

	private void parseAll() {
		this.P = this.DS[0] = 0;
		short type;
		while ((type = getNext()) != TYPE_EOF) {
			if (++DS[0] == D.length)
				expandDArray();
			D[DS[0]] = type;
			DS[DS[0]] = ST;
		}
	}

	protected abstract short getNext();

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

	protected abstract boolean isWhitespace(char c);

	public final boolean isStartOfLine(int pos) {
		if (--pos < 0) return true;
		while (pos >= 0) {
			if (S[pos] == '\n') return true;
			if (!isWhitespace(S[pos])) return false;
			pos--;
		}
		return true;
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