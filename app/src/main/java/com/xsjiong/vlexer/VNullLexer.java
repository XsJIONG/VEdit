package com.xsjiong.vlexer;

public class VNullLexer extends VLexer {
	@Override
	protected short getNext() {
		if (P == L) return VLexer.TYPE_EOF;
		P = L;
		return VLexer.UNRESOLVED_TYPE;
	}

	@Override
	public Trie getKeywordTrie() {
		return null;
	}

	@Override
	protected boolean isWhitespace(char c) {
		return false;
	}

	@Override
	protected boolean isIdentifierStart(char c) {
		return false;
	}

	@Override
	protected boolean isIdentifierPart(char c) {
		return false;
	}

	@Override
	protected short ProcessSymbol(char c) {
		return 0;
	}
}
