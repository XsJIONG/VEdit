package com.xsjiong.vlexer;

public class VNullLexer extends VLexer {
	@Override
	protected short getNext() {
		if (P == L) return VLexer.TYPE_EOF;
		P = L;
		return VLexer.UNRESOLVED_TYPE;
	}

	@Override
	protected boolean isWhitespace(char c) {
		return false;
	}

	public VNullLexer() {
	}

	public VNullLexer(char[] cs) {
		super(cs);
	}
}
