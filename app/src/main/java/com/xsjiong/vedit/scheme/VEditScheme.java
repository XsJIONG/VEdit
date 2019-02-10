package com.xsjiong.vedit.scheme;

import com.xsjiong.vlexer.VLexer;

public abstract class VEditScheme {
	protected int[] C = new int[VLexer.TYPE_COUNT];
	protected int _SplitLine, _Selection, _Cursor;

	public void setTypeColor(short type, int color) {
		C[type] = color;
	}

	public int getTypeColor(short type) {
		return C[type];
	}

	public void setSplitLineColor(int color) {
		_SplitLine = color;
	}

	public int getSplitLineColor() {
		return _SplitLine;
	}

	public void setSelectionColor(int color) {
		_Selection = color;
	}

	public int getSelectionColor() {
		return _Selection;
	}

	public void setCursorColor(int color) {
		_Cursor = color;
	}

	public int getCursorColor() {
		return _Cursor;
	}
}