package com.xsjiong.vedit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.OverScroller;
import com.xsjiong.vedit.scheme.VEditScheme;
import com.xsjiong.vedit.scheme.VEditSchemeLight;
import com.xsjiong.vlexer.VJavaLexer;
import com.xsjiong.vlexer.VLexer;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.xsjiong.vedit.G.T;

public class VEdit extends View {
	// --------------------
	// -----Constants------
	// --------------------

	public static final int DOUBLE_CLICK_INTERVAL = 200, MERGE_ACTIONS_INTERVAL = 250;
	public static final int LINENUM_SPLIT_WIDTH = 7;
	public static final int EXPAND_SIZE = 64;
	public static final int EMPTY_CHAR_WIDTH = 10;
	public static final int SCROLL_TO_CURSOR_EXTRA = 20;
	public static final short CHAR_SPACE = 32, CHAR_TAB = 9;
	public static final byte CURSOR_NONE = -1, CURSOR_NORMAL = 0, CURSOR_LEFT = 1, CURSOR_RIGHT = 2;
	public static final int EDIT_ACTION_STACK_SIZE = 64;


	// -----------------
	// -----Fields------
	// -----------------

	private TextPaint ContentPaint, LineNumberPaint;
	private Paint ColorPaint;
	private float YOffset;
	private float TextHeight;
	private int ContentHeight;
	private char[] S = new char[0];
	private int[] E = new int[257];
	private int _minFling, _touchSlop;
	private float _lastX, _lastY, _stX, _stY;
	private OverScroller Scroller;
	private VelocityTracker SpeedCalc;
	private boolean isDragging = false;
	private int TABSpaceCount = 4;
	private int _YScrollRange;
	private float LineNumberWidth;
	private int _maxOSX = 20, _maxOSY = 20;
	private int _SStart = -1, _SEnd;
	private int _CursorLine = 1, _CursorColumn = 0;
	private float _CursorWidth = 2;
	private float _LinePaddingTop = 5, _LinePaddingBottom = 5;
	private float _ContentLeftPadding = 7;
	private int _TextLength;
	private boolean _Editable = true;
	private VInputConnection _InputConnection;
	private boolean _ShowLineNumber = true;
	private float[] _CharWidths = new float[65536];
	private InputMethodManager _IMM;
	private long LastClickTime = 0, LastInsertCharTime = 0;
	private int _ComposingStart = -1;
	private VEditScheme _Scheme = new VEditSchemeLight();
	private VLexer _Lexer = new VJavaLexer();
	private GlassCursor CURSOR = new GlassCursor(this);
	private float _CursorHorizonOffset;
	private float _SStartHorizonOffset, _SEndHorizonOffset;
	private int _SStartLine, _SEndLine;
	private float LineHeight;
	private byte _DraggingCursor = CURSOR_NONE;
	private EditActionStack _EditActionStack = new EditActionStack(this, EDIT_ACTION_STACK_SIZE);

	// -----------------------
	// -----Constructors------
	// -----------------------

	public VEdit(Context cx) {
		this(cx, null, 0);
	}

	public VEdit(Context cx, AttributeSet attr) {
		this(cx, attr, 0);
	}

	public VEdit(Context cx, AttributeSet attr, int style) {
		super(cx, attr, style);
		Scroller = new OverScroller(getContext());
		SpeedCalc = VelocityTracker.obtain();
		ViewConfiguration config = ViewConfiguration.get(cx);
		_minFling = config.getScaledMinimumFlingVelocity();
		_touchSlop = config.getScaledTouchSlop();
		ContentPaint = new TextPaint();
		ContentPaint.setAntiAlias(true);
		LineNumberPaint = new TextPaint();
		LineNumberPaint.setAntiAlias(true);
		LineNumberPaint.setColor(Color.BLACK);
		LineNumberPaint.setTextAlign(Paint.Align.RIGHT);
		setTextSize(50);
		ContentPaint.setColor(Color.BLACK);
		ColorPaint = new Paint();
		ColorPaint.setAntiAlias(false);
		ColorPaint.setStyle(Paint.Style.FILL);
		ColorPaint.setDither(false);
		setFocusable(true);
		setFocusableInTouchMode(true);
		_IMM = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		E[0] = 2;
		E[1] = 0;
		E[2] = 1; // 来自我自己把全部删了之后的E信息
		_TextLength = 0;
	}


	// ------------------
	// -----Methods------
	// ------------------

	public void deleteSelecting() {
		int line = findLine(_SEnd);
		deleteChars(line, _SEnd - E[line], _SEnd - _SStart);
		finishSelecting();
	}

	public void setLexer(VLexer lexer) {
		if (lexer == null) {
			_Lexer = null;
			return;
		}
		if (_Lexer != null && _Lexer.getClass() == lexer.getClass()) return;
		_Lexer = lexer;
		_Lexer.setText(S);
	}

	public void loadURL(String url) throws IOException {
		loadURL(new URL(url));
	}

	public void loadURL(URL url) throws IOException {
		loadStream(url.openStream());
	}

	public void loadFile(String filepath) throws IOException {
		loadFile(new File(filepath));
	}

	public void loadFile(File file) throws IOException {
		loadStream(new FileInputStream(file));
	}

	public void loadStream(InputStream stream) throws IOException {
		loadStream(stream, StandardCharsets.UTF_8);
	}

	public void loadStream(InputStream stream, Charset charset) throws IOException {
		byte[] buf = new byte[1024];
		int read;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((read = stream.read(buf)) != -1) out.write(buf, 0, read);
		stream.close();
		out.close();
		setText(new String(out.toByteArray(), charset));
	}

	public void setTypeface(Typeface typeface) {
		ContentPaint.setTypeface(typeface);
		LineNumberPaint.setTypeface(typeface);
		onFontChange();
	}

	public void setShowLineNumber(boolean flag) {
		_ShowLineNumber = flag;
		invalidate();
	}

	public boolean isShowLineNumber() {
		return _ShowLineNumber;
	}

	public void setEditable(boolean editable) {
		_Editable = editable;
		if (_Editable && _IMM != null)
			_IMM.restartInput(this);
		else hideIME();
		invalidate();
	}

	public void setContentLeftPadding(float padding) {
		_ContentLeftPadding = padding;
		invalidate();
	}

	public float getContentLeftPadding() {
		return _ContentLeftPadding;
	}

	public int getLineNumber() {
		return E[0] - 1;
	}

	public int getLineStart(int line) {
		return E[line];
	}

	public int getLineEnd(int line) {
		return E[line + 1] - 1;
	}

	public char[] getLineChars(int line) {
		char[] ret = new char[E[line + 1] - E[line] - 1];
		System.arraycopy(S, E[line], ret, 0, ret.length);
		return ret;
	}

	public String getLineString(int line) {
		return new String(getLineChars(line));
	}

	public void setTextAntiAlias(boolean flag) {
		ContentPaint.setAntiAlias(flag);
		invalidate();
	}

	// Recommend using "setScale" since "setTextSize" will clear the text width cache, which makes drawing slower
	public void setTextSize(int unit, float size) {
		setTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));
	}

	public void setTextSize(float size) {
		ContentPaint.setTextSize(size);
		LineNumberPaint.setTextSize(size);
		onFontChange();
	}

	public void setColorScheme(VEditScheme scheme) {
		this._Scheme = scheme;
		CURSOR.setHeight(TextHeight);
		invalidate();
	}

	public VEditScheme getColorScheme() {
		return _Scheme;
	}

	public void setTABSpaceCount(int count) {
		TABSpaceCount = count;
		_CharWidths[CHAR_TAB] = TABSpaceCount * _CharWidths[CHAR_SPACE];
		invalidate();
	}

	public int getTABSpaceCount() {
		return TABSpaceCount;
	}

	public void setLinePadding(float top, float bottom) {
		_LinePaddingTop = top;
		_LinePaddingBottom = bottom;
		onFontChange();
		invalidate();
	}

	public float getLinePaddingTop() {
		return _LinePaddingTop;
	}

	public float getLinePaddingBottom() {
		return _LinePaddingBottom;
	}

	public void setText(String s) {
		setText(s == null ? new char[0] : s.toCharArray());
	}

	public void setText(char[] s) {
		if (s == null) s = new char[0];
		this.S = s;
		_TextLength = s.length;
		if (_Editable && _IMM != null)
			_IMM.restartInput(this);
		calculateEnters();
		_Lexer.setText(s);
		onLineChange();
		requestLayout();
		postInvalidate();
	}

	public int getSelectionStart() {
		return _SStart;
	}

	public int getSelectionEnd() {
		return _SEnd;
	}

	// return true if the start and the end of the selection has reserved
	public boolean setSelectionStart(int st) {
		boolean ret = false;
		if (st > _SEnd) {
			_SStart = _SEnd;
			_SEnd = st;
			ret = true;
		} else _SStart = st;
		onSelectionUpdate();
		invalidate();
		return ret;
	}

	public boolean setSelectionEnd(int en) {
		boolean ret = false;
		if (en < _SStart) {
			_SEnd = _SStart;
			_SStart = en;
			ret = true;
		} else _SEnd = en;
		onSelectionUpdate();
		invalidate();
		return ret;
	}

	public void setSelectionRange(int st, int en) {
		_SStart = st;
		_SEnd = en;
		onSelectionUpdate();
		invalidate();
	}

	public void moveCursor(int pos) {
		if (pos > _TextLength) pos = _TextLength;
		_CursorLine = findLine(pos);
		_CursorColumn = pos - E[_CursorLine];
		onSelectionUpdate();
		invalidate();
	}

	public void moveCursor(int line, int column) {
		if (line > E[0] - 1) {
			line = E[0] - 1;
			column = E[E[0]] - E[E[0] - 1] - 1;
		} else if (column > E[line + 1] - E[line] - 1)
			column = E[line + 1] - E[line] - 1;
		_CursorLine = line;
		_CursorColumn = column;
		onSelectionUpdate();
		invalidate();
	}

	public void setMaxOverScroll(int x, int y) {
		_maxOSX = x;
		_maxOSY = y;
	}

	public int getMaxOverScrollX() {
		return _maxOSX;
	}

	public int getMaxOverScrollY() {
		return _maxOSY;
	}

	private boolean _fixScroll = true;

	public void setFixScroll(boolean flag) {
		_fixScroll = flag;
	}

	public boolean isFixScroll() {
		return _fixScroll;
	}

	private boolean _dragDirection;
	private int _flingFactor = 1000;

	public void setFlingFactor(int factor) {
		_flingFactor = factor;
	}

	public void setCursorWidth(float width) {
		_CursorWidth = width;
		invalidate();
	}

	public float getCursorWidth() {
		return _CursorWidth;
	}

	public int getLineLength(int line) {
		return E[line + 1] - E[line] - 1;
	}

	public int getTextLength() {
		return _TextLength;
	}

	public void showIME() {
		if (_IMM != null)
			_IMM.showSoftInput(this, 0);
	}

	public void hideIME() {
		if (_IMM != null && _IMM.isActive(this))
			_IMM.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	public void deleteChar() {
		deleteChar(_CursorLine, _CursorColumn);
	}

	public void deleteChar(int line, int column) {
		if (line == 1 && column == 0) return;
		_EditActionStack.addAction(new EditAction.DeleteCharAction(line, column, S[E[line] + column - 1]));
	}

	public void _deleteChar() {
		int[] ret = _deleteChar(_CursorLine, _CursorColumn);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
		onSelectionUpdate();
	}

	public int[] _deleteChar(int line, int column) {
		if ((!_Editable) || (line == 1 && column == 0)) return new int[] {line, column};
		final int pos = E[line] + column;

		if (_TextLength > pos)
			System.arraycopy(S, pos, S, pos - 1, _TextLength - pos);
		if (column == 0) {
			column = E[line] - E[line - 1] - 1;
			System.arraycopy(E, line + 1, E, line, E[0] - line);
			E[0]--;
			for (int i = line; i <= E[0]; i++) E[i]--;
			line--;
			onLineChange();
		} else {
			for (int i = line + 1; i <= E[0]; i++) E[i]--;
			column--;
		}
		_TextLength--;
		_Lexer.onTextReferenceUpdate(S, _TextLength);
		_Lexer.onDeleteChars(pos, 1);
		postInvalidate();
		return new int[] {line, column};
	}

	public boolean isRangeSelecting() {
		return _SStart != -1;
	}

	public void insertChar(char c) {
		insertChar(_CursorLine, _CursorColumn, c);
	}

	public void insertChar(int line, int column, char c) {
		_EditActionStack.addAction(new EditAction.InsertCharAction(line, column, c));
	}

	public int[] _insertChar(int line, int column, char c) {
		if (!_Editable) return new int[] {line, column};
		final int pos = E[line] + column;

		if (S.length <= _TextLength + 1) {
			char[] ns = new char[S.length + EXPAND_SIZE];
			System.arraycopy(S, 0, ns, 0, pos);
			if (pos != _TextLength) System.arraycopy(S, pos, ns, pos + 1, _TextLength - pos);
			S = ns;
			S[pos] = c;
			ns = null;
			// TODO Should GC Here?
			System.gc();
		} else {
			// 没办法用System.arraycopy，因为考虑到顺序，可能会覆盖
			for (int i = _TextLength; i >= pos; i--) S[i + 1] = S[i];
			S[pos] = c;
		}
		if (c == '\n') {
			// 理由同上，注意这是>不是>=
			if (E[0] + 1 == E.length) expandEArray();
			for (int i = E[0]; i > line; i--) E[i + 1] = E[i] + 1;
			E[0]++;
			E[line + 1] = pos + 1;
			line++;
			column = 0;
			onLineChange();
		} else {
			for (int i = E[0]; i > line; i--) E[i]++;
			column++;
		}
		_TextLength++;
		_Lexer.onTextReferenceUpdate(S, _TextLength);
		_Lexer.onInsertChars(pos, 1);
		postInvalidate();
		return new int[] {line, column};
	}

	public String getText(int st, int en) {
		return new String(S, st, en - st);
	}

	public char[] getChars(int st, int en) {
		char[] ret = new char[en - st];
		System.arraycopy(S, st, ret, 0, ret.length);
		return ret;
	}

	public String getText() {
		return getText(0, _TextLength);
	}

	public char[] getChars() {
		return getChars(0, _TextLength);
	}

	public void makeLineVisible(int line) {
		float y = LineHeight * line - LineHeight;
		if (getScrollY() > y) {
			scrollTo(getScrollX(), (int) y);
			postInvalidate();
			return;
		}
		y += LineHeight - getHeight();
		if (getScrollY() < y) {
			scrollTo(getScrollX(), (int) Math.ceil(y));
			postInvalidate();
		}
	}

	public void makeCursorVisible(int line, int column) {
		int pos = E[line] + column;
		makeLineVisible(line);
		float sum = (_ShowLineNumber ? (LineNumberWidth + LINENUM_SPLIT_WIDTH) : 0) + _ContentLeftPadding;
		for (int i = E[line]; i < pos; i++)
			sum += _CharWidths[S[i]];
		if (sum - _CursorWidth / 2 < getScrollX()) {
			scrollTo((int) (sum - _CursorWidth / 2) - SCROLL_TO_CURSOR_EXTRA, getScrollY());
			postInvalidate();
		} else if (sum + _CursorWidth / 2 > getScrollX() + getWidth()) {
			scrollTo((int) Math.ceil(sum + _CursorWidth / 2 - getWidth()) + SCROLL_TO_CURSOR_EXTRA, getScrollY());
			postInvalidate();
		}
	}

	public void finishSelecting() {
		_SStart = -1;
		onSelectionUpdate();
		postInvalidate();
	}

	public int getPosition(int line, int column) {
		return E[line] + column;
	}

	public boolean isEmpty() {
		return E[0] == 1;
	}

	public int getCursorPosition() {
		if (isEmpty()) return 0;
		return E[_CursorLine] + _CursorColumn;
	}

	public void insertChars(char[] cs) {
		insertChars(_CursorLine, _CursorColumn, cs);
	}

	public void insertChars(int line, int column, char[] cs) {
		if (cs.length == 1) {
			insertChar(line, column, cs[0]);
			return;
		}
		_EditActionStack.addAction(new EditAction.InsertCharsAction(line, column, cs));
	}

	public void _insertChars(char[] cs) {
		int[] ret = _insertChars(_CursorLine, _CursorColumn, cs);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
		onSelectionUpdate();
	}

	public int[] _insertChars(int line, int column, char[] cs) {
		if (!_Editable) return new int[] {line, column};
		final int tl = cs.length;
		final int pos = E[line] + column;

		int nh = _TextLength + tl;
		if (nh > S.length) {
			char[] ns = new char[nh + EXPAND_SIZE];
			System.arraycopy(S, 0, ns, 0, pos);
			System.arraycopy(cs, 0, ns, pos, tl);
			if (pos != _TextLength) System.arraycopy(S, pos, ns, pos + tl, _TextLength - pos);
			S = ns;
			ns = null;
			System.gc();
		} else {
			for (int i = _TextLength - 1; i >= pos; i--) S[i + tl] = S[i];
			System.arraycopy(cs, 0, S, pos, tl);
		}
		_TextLength += tl;
		int tot = 0;
		int[] tmp = new int[EXPAND_SIZE];
		for (int i = 0; i < tl; i++)
			if (cs[i] == '\n') {
				if (++tot == tmp.length) {
					int[] tmp2 = new int[tmp.length + EXPAND_SIZE];
					System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
					tmp = tmp2;
					tmp2 = null;
				}
				tmp[tot] = i + pos + 1;
			}
		nh = E[0] + tot + 1;
		if (nh > E.length) {
			int[] ne = new int[nh];
			System.arraycopy(E, 0, ne, 0, line + 1);
			System.arraycopy(tmp, 1, ne, line + 1, tot);
			System.arraycopy(E, line + 1, ne, line + tot + 1, E[0] - line);
			ne[0] = E[0] + tot;
			for (int i = line + tot + 1; i <= ne[0]; i++) ne[i] += tl;
			E = ne;
			ne = null;
		} else {
			for (int i = E[0]; i > line; i--) E[i + tot] = E[i] + tl;
			System.arraycopy(tmp, 1, E, line + 1, tot);
			E[0] += tot;
		}
		if (tot != 0) onLineChange();
		line += tot;
		if (tot == 0)
			column += tl;
		else
			column = pos + tl - E[line];
		_Lexer.onTextReferenceUpdate(S, _TextLength);
		_Lexer.onInsertChars(pos, cs.length);
		postInvalidate();
		return new int[] {line, column};
	}

	public void deleteChars(int count) {
		deleteChars(_CursorLine, _CursorColumn, count);
	}

	public void deleteChars(int line, int column, int count) {
		int pos = E[line] + column;
		_EditActionStack.addAction(new EditAction.DeleteCharsAction(line, column, getChars(pos - count, pos)));
	}

	public void _deleteChars(int count) {
		int[] ret = _deleteChars(_CursorLine, _CursorColumn, count);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
		onSelectionUpdate();
	}

	public int[] _deleteChars(int line, int column, int count) {
		if ((!_Editable) || count == 0) return new int[] {line, column};
		if (count > _TextLength) {
			S = new char[0];
			E[0] = 2;
			E[1] = 0;
			E[2] = 1;
			_TextLength = 0;
		}
		int pos = E[line] + column;
		if (pos > _TextLength) pos = _TextLength;
		if (pos < count) count = pos;

		int tot = 0;
		for (int i = 1; i <= count; i++)
			if (S[pos - i] == '\n') tot++;
		if (_TextLength > pos)
			System.arraycopy(S, pos, S, pos - count, _TextLength - pos);
		_TextLength -= count;
		E[0] -= tot;
		for (int i = line - tot + 1; i <= E[0]; i++) E[i] = E[i + tot] - count;
		if (tot != 0) onLineChange();
		line -= tot;
		if (tot == 0)
			column -= count;
		else
			column = pos - count - E[line];
		_Lexer.onTextReferenceUpdate(S, _TextLength);
		_Lexer.onDeleteChars(pos, count);
		postInvalidate();
		return new int[] {line, column};
	}

	public void replace(int st, int en, char[] cs) {
		if (st > en) {
			int tmp = en;
			en = st;
			st = tmp;
		}
		int line = findLine(en);
		_EditActionStack.addAction(new EditAction.ReplaceAction(line, en - E[line], getChars(st, en), cs));
	}

	public void moveCursorRelative(int count) {
		count = Math.min(E[_CursorLine] + _CursorColumn + count, _TextLength);
		_CursorLine = findLine(count);
		_CursorColumn = count - E[_CursorLine];
		onSelectionUpdate();
	}

	public float getCharWidth(char c) {
		float ret = _CharWidths[c];
		if (ret == 0) {
			TMP2[0] = c;
			ret = ContentPaint.measureText(TMP2, 0, 1);
			if (ret < EMPTY_CHAR_WIDTH) ret = EMPTY_CHAR_WIDTH;
			_CharWidths[c] = ret;
		}
		return ret;
	}

	public void commitText(char[] cs) {
		_ComposingStart = -1;
		if (isRangeSelecting()) {
			int ss = _SStart;
			finishSelecting();
			replace(ss, _SEnd, cs);
		} else insertChars(cs);
	}

	public String getSelectedText() {
		if (!isRangeSelecting()) return null;
		return new String(S, _SStart, _SEnd - _SStart);
	}

	public void expandSelectionFrom(int pos) {
		if (pos == _TextLength - 1) return;
		int st, en;
		for (st = pos; st >= 0 && isSelectableChar(S[st]); st--) ;
		for (en = pos + 1; en < _TextLength && isSelectableChar(S[en]); en++) ;
		if (st + 1 == en) return;
		setSelectionRange(st + 1, en);
	}

	public int[] getCursorByPosition(float x, float y) {
		x -= _ContentLeftPadding;
		if (_ShowLineNumber) x -= (LineNumberWidth + LINENUM_SPLIT_WIDTH);
		int[] rret = new int[2];
		rret[0] = Math.min((int) Math.ceil(y / LineHeight), E[0] - 1);
		if (rret[0] < 1) rret[0] = 1;
		final int en = E[rret[0] + 1] - 1;
		int ret = E[rret[0]];
		for (float sum = -x; ret < en; ret++) {
			if ((sum += getCharWidth(S[ret])) >= 0) {
				if ((-(sum - getCharWidth(S[ret]))) > sum) // 是前面的更逼近一些
					ret++;
				break;
			}
		}
		rret[1] = ret - E[rret[0]];
		return rret;
	}

	public static boolean isSelectableChar(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '.' || Character.isJavaIdentifierPart(c);
	}

	public void clearEditActions() {
		_EditActionStack.clear();
	}

	public boolean redo() {
		return _EditActionStack.redo();
	}

	public boolean undo() {
		return _EditActionStack.undo();
	}


	// --------------------------
	// -----Override Methods-----
	// --------------------------

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		_DraggingCursor = CURSOR_NONE;
		CURSOR.recycle();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if ((!isRangeSelecting()) && h < oldh)
			makeLineVisible(_CursorLine);
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return ContentHeight;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		SpeedCalc.addMovement(event);
		boolean s = super.onTouchEvent(event);
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_stX = _lastX = event.getX();
				_stY = _lastY = event.getY();
				_DraggingCursor = getDraggingCursor(_stX + getScrollX(), _stY + getScrollY());
				if (_DraggingCursor != CURSOR_NONE) {
					_stX += getScrollX();
					_stY += getScrollY();
				}
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
				if (!isFocused())
					requestFocus();
				return true;
			case MotionEvent.ACTION_MOVE:
				float x = event.getX(), y = event.getY();
				if (_DraggingCursor != CURSOR_NONE) {
					int[] nc = getCursorByPosition(x + getScrollX() - _stX + _lastX, y + getScrollY() - _stY + _lastY);
					switch (_DraggingCursor) {
						case CURSOR_NORMAL: {
							moveCursor(nc[0], nc[1]);
							return true;
						}
						case CURSOR_LEFT: {
							if (setSelectionStart(E[nc[0]] + nc[1])) _DraggingCursor = CURSOR_RIGHT;
							makeCursorVisible(nc[0], nc[1]);
							return true;
						}
						case CURSOR_RIGHT: {
							if (setSelectionEnd(E[nc[0]] + nc[1])) _DraggingCursor = CURSOR_LEFT;
							makeCursorVisible(nc[0], nc[1]);
							return true;
						}
					}
				}
				if ((!isDragging) && (Math.abs(x - _stX) > _touchSlop || Math.abs(y - _stY) > _touchSlop)) {
					isDragging = true;
					if (_fixScroll)
						_dragDirection = Math.abs(x - _lastX) > Math.abs(y - _lastY);
				}
				if (isDragging) {
					int finalX = getScrollX(), finalY = getScrollY();
					if (_fixScroll) {
						if (_dragDirection) {
							finalX += (_lastX - x);
							// TODO 如果要改X边界记得这儿加上
							if (finalX < -_maxOSX) finalX = -_maxOSX;
						} else {
							finalY += (_lastY - y);
							if (finalY < -_maxOSY) finalY = -_maxOSY;
							else if (finalY > _YScrollRange + _maxOSY)
								finalY = _YScrollRange + _maxOSY;
						}
					} else {
						finalX += (_lastX - x);
						// TODO 如果要改X边界记得这儿加上
						if (finalX < -_maxOSX) finalX = -_maxOSX;
						finalY += (_lastY - y);
						if (finalY < -_maxOSY) finalY = -_maxOSY;
						else if (finalY > _YScrollRange + _maxOSY)
							finalY = _YScrollRange + _maxOSY;
					}
					scrollTo(finalX, finalY);
					postInvalidate();
				}
				_lastX = x;
				_lastY = y;
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (_DraggingCursor != CURSOR_NONE) {
					_DraggingCursor = CURSOR_NONE;
					return true;
				}
				SpeedCalc.computeCurrentVelocity(_flingFactor);
				if (!isDragging) {
					onClick(event.getX() + getScrollX(), event.getY() + getScrollY());
					performClick();
				} else {
					isDragging = false;
					int speedX = (int) SpeedCalc.getXVelocity();
					int speedY = (int) SpeedCalc.getYVelocity();
					if (Math.abs(speedX) <= _minFling) speedX = 0;
					if (Math.abs(speedY) <= _minFling) speedY = 0;
					if (_fixScroll) {
						if (_dragDirection) speedY = 0;
						else speedX = 0;
					}
					if (speedX != 0 || speedY != 0)
						Scroller.fling(getScrollX(), getScrollY(), -speedX, -speedY, -_maxOSX, Integer.MAX_VALUE, -_maxOSY, _YScrollRange + _maxOSY);
					else springBack();
					SpeedCalc.clear();
					invalidate();
				}
				return true;
		}
		return s;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		_YScrollRange = Math.max(ContentHeight - (bottom - top), 0);
	}

	@Override
	public void computeScroll() {
		if (Scroller.computeScrollOffset()) {
			int x = Scroller.getCurrX();
			int y = Scroller.getCurrY();
			scrollTo(x, y);
			postInvalidate();
		} else if (!isDragging && (getScrollX() < 0 || getScrollY() < 0 || getScrollY() > _YScrollRange)) { // TODO X边界还要改我
			springBack();
			postInvalidate();
		}
	}

	// 输入处理
	@Override
	public boolean onCheckIsTextEditor() {
		return true;
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		outAttrs.imeOptions = EditorInfo.IME_NULL
				| EditorInfo.IME_FLAG_NO_ENTER_ACTION
				| EditorInfo.IME_FLAG_NO_FULLSCREEN
				| EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION;
		outAttrs.inputType = EditorInfo.TYPE_MASK_CLASS
				| EditorInfo.TYPE_CLASS_TEXT
				| EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
				| EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE;
		outAttrs.initialSelStart = _SStart;
		outAttrs.initialSelEnd = isRangeSelecting() ? -1 : _SEnd;
		if (_InputConnection == null)
			_InputConnection = new VInputConnection(this);
		return _InputConnection;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) hideIME();
		super.setEnabled(enabled);
		if (enabled && _Editable && _IMM != null) _IMM.restartInput(this);
	}

	// 绘制函数
	@Override
	protected void onDraw(Canvas canvas) {
		// Marks
		long st = System.currentTimeMillis();
		final boolean showSelecting = isRangeSelecting();
		final boolean showCursor = (!showSelecting) && _Editable;
		final float bottom = getScrollY() + getHeight() + YOffset;
		final int right = getScrollX() + getWidth();
		final float xo = (_ShowLineNumber ? LineNumberWidth + LINENUM_SPLIT_WIDTH : 0) + _ContentLeftPadding;

		int line = Math.max((int) (getScrollY() / LineHeight) + 1, 1);
		float y = (line - 1) * LineHeight + YOffset + _LinePaddingTop;
		float XStart, wtmp, x;
		int i, en;
		int tot;
		if (_ShowLineNumber) {
			ColorPaint.setColor(_Scheme.getSplitLineColor());
			canvas.drawRect(LineNumberWidth, getScrollY(), LineNumberWidth + LINENUM_SPLIT_WIDTH, getScrollY() + getHeight(), ColorPaint);
		}
		int parseTot = _Lexer.findPart(E[line]);
		int parseTarget = _Lexer.getPartStart(parseTot);
		float SStartLineEnd = -1;
		LineDraw:
		for (; line < E[0]; line++) {
			if (_ShowLineNumber)
				canvas.drawText(Integer.toString(line), LineNumberWidth, y, LineNumberPaint);
			if (showCursor && _CursorLine == line) {
				ColorPaint.setColor(_Scheme.getSelectionColor());
				canvas.drawRect(xo - _ContentLeftPadding, y - YOffset - _LinePaddingTop, right, y + TextHeight - YOffset + _LinePaddingBottom, ColorPaint);
			}
			i = E[line];
			en = E[line + 1] - 1;
			XStart = xo;
			if (getScrollX() > XStart && i < _TextLength)
				while ((wtmp = XStart + getCharWidth(S[i])) < getScrollX()) {
					if (++i >= en) {
						if ((y += LineHeight) >= bottom) break LineDraw;
						continue LineDraw;
					}
					XStart = wtmp;
				}
			if (parseTot <= _Lexer.getPartCount()) {
				while (i >= parseTarget && parseTot <= _Lexer.getPartCount())
					parseTarget = _Lexer.getPartStart(++parseTot);
				ContentPaint.setColor(_Scheme.getTypeColor(_Lexer.getPartType(parseTot - 1)));
			}
			tot = 0;
			for (x = XStart; i < en && x <= right; i++) {
				if (i == parseTarget) {
					canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
					XStart = x;
					tot = 0;
					ContentPaint.setColor(_Scheme.getTypeColor(_Lexer.getPartType(parseTot)));
					++parseTot;
					if (parseTot <= _Lexer.getPartCount()) parseTarget = _Lexer.getPartStart(parseTot);
				}
				if ((TMP[tot] = S[i]) == '\t') {
					canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
					XStart = x;
					tot = 0;
					XStart += _CharWidths[CHAR_TAB];
					x += _CharWidths[CHAR_TAB];
				} else
					x += getCharWidth(TMP[tot++]);
			}
			canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
			if (showSelecting) {
				if (line == _SStartLine) SStartLineEnd = x;
				else if (line > _SStartLine && line < _SEndLine) {
					ColorPaint.setColor(_Scheme.getSelectionColor());
					canvas.drawRect(xo, y - YOffset - _LinePaddingTop, x, y - YOffset + TextHeight + _LinePaddingBottom, ColorPaint);
				}
			}
			if ((y += LineHeight) >= bottom) break;
		}
		if (showCursor) {
			ColorPaint.setColor(_Scheme.getCursorLineColor());
			ColorPaint.setStrokeWidth(_CursorWidth);
			float sty = LineHeight * _CursorLine;
			canvas.drawLine(xo + _CursorHorizonOffset, sty - LineHeight, xo + _CursorHorizonOffset, sty, ColorPaint);
			CURSOR.draw(canvas, xo + _CursorHorizonOffset, sty, CURSOR_NORMAL);
		} else if (showSelecting) {
			float sty = LineHeight * _SStartLine;
			if (_SStartLine == _SEndLine) {
				ColorPaint.setColor(_Scheme.getSelectionColor());
				canvas.drawRect(xo + _SStartHorizonOffset, sty - LineHeight, xo + _SEndHorizonOffset, sty, ColorPaint);
			} else {
				float eny = LineHeight * _SEndLine;
				ColorPaint.setColor(_Scheme.getSelectionColor());
				if (SStartLineEnd != -1)
					canvas.drawRect(xo + _SStartHorizonOffset, sty - LineHeight, SStartLineEnd, sty, ColorPaint);
				canvas.drawRect(xo, eny - LineHeight, xo + _SEndHorizonOffset, eny, ColorPaint);
			}
			CURSOR.draw(canvas, xo + _SStartHorizonOffset, sty, CURSOR_LEFT);
			CURSOR.draw(canvas, xo + _SEndHorizonOffset, LineHeight * _SEndLine, CURSOR_RIGHT);
		}
		if (G.LOG_TIME) {
			st = System.currentTimeMillis() - st;
			Log.i(T, "耗时3: " + st);
		}
	}

	// -------------------------
	// -----Private Methods-----
	// -------------------------

	private byte getDraggingCursor(float x, float y) {
		final float ori = x;
		x -= _ContentLeftPadding;
		if (_ShowLineNumber) x -= (LineNumberWidth + LINENUM_SPLIT_WIDTH);
		if (isRangeSelecting()) {
			if (CURSOR.isTouched(x - _SStartHorizonOffset, y - LineHeight * _SStartLine, CURSOR_LEFT)) {
				_lastX = ori;
				_lastY = LineHeight * _SStartLine - LineHeight * 0.5f;
				return CURSOR_LEFT;
			}
			if (CURSOR.isTouched(x - _SEndHorizonOffset, y - LineHeight * _SEndLine, CURSOR_RIGHT)) {
				_lastX = ori;
				_lastY = LineHeight * _SEndLine - LineHeight * 0.5f;
				return CURSOR_RIGHT;
			}
		} else if (CURSOR.isTouched(x - _CursorHorizonOffset, y - LineHeight * _CursorLine, CURSOR_NORMAL)) {
			_lastX = ori;
			_lastY = LineHeight * _CursorLine - LineHeight * 0.5f;
			return CURSOR_NORMAL;
		}
		return CURSOR_NONE;
	}

	private void deleteSurrounding(int beforeLength, int afterLength) {
		if (isRangeSelecting())
			deleteSelecting();
		else {
			int pos = getCursorPosition() + afterLength;
			int line = findLine(pos);
			deleteChars(line, pos - E[line], afterLength + beforeLength);
		}
	}

	private void calculateEnters() {
		E[E[0] = 1] = 0;
		for (int i = 0; i < _TextLength; i++) {
			if (S[i] == '\0') continue;
			if (S[i] == '\n') {
				if (++E[0] == E.length)
					expandEArray();
				E[E[0]] = i + 1;
			}
		}
		E[++E[0]] = _TextLength + 1;
	}

	private boolean processEvent(KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_UP) return false;
		if (event.isPrintingKey()) {
			insertChar((char) event.getUnicodeChar(event.getMetaState()));
			return true;
		}
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DEL:
				if (isRangeSelecting())
					deleteSelecting();
				else
					deleteChar();
				break;
			case KeyEvent.KEYCODE_ENTER:
				insertChar('\n');
				break;
			default:
				return false;
		}
		return true;
	}

	private void expandEArray() {
		// TODO Extract Constant
		int[] ne = new int[E.length + 256];
		System.arraycopy(E, 0, ne, 0, E.length);
		E = ne;
	}

	private int findLine(int pos) {
		int l = 1, r = E[0] - 1;
		int mid;
		while (l <= r) {
			mid = (l + r) >> 1;
			if (E[mid] <= pos)
				l = mid + 1;
			else
				r = mid - 1;
		}
		return r;
	}

	private void onClick(float x, float y) {
		if (!_Editable) return;
		long time = System.currentTimeMillis();
		boolean dc = time - LastClickTime <= DOUBLE_CLICK_INTERVAL;
		LastClickTime = time;
		int[] nc = getCursorByPosition(x, y);
		finishSelecting();
		if (dc) expandSelectionFrom(E[nc[0]] + nc[1]);
		_CursorLine = nc[0];
		_CursorColumn = nc[1];
		makeCursorVisible(_CursorLine, _CursorColumn);
		_ComposingStart = -1;
		if (_IMM != null) {
			_IMM.viewClicked(this);
			_IMM.showSoftInput(this, 0);
			onSelectionUpdate();
//			_IMM.restartInput(this);
		}
		postInvalidate();
	}

	private void onFontChange() {
		YOffset = -ContentPaint.ascent();
		TextHeight = ContentPaint.descent() + YOffset;
		LineHeight = TextHeight + _LinePaddingTop + _LinePaddingBottom;
		CURSOR.setHeight(TextHeight);
		clearCharWidthCache();
		onLineChange();
		requestLayout();
		postInvalidate();
	}

	private void onLineChange() {
		ContentHeight = (int) (LineHeight * (E[0] - 1));
		_YScrollRange = Math.max(ContentHeight - getHeight(), 0);
		LineNumberWidth = LineNumberPaint.measureText("9") * ((int) Math.log10(E[0] - 1) + 1);
	}

	private void clearCharWidthCache() {
		Arrays.fill(_CharWidths, 0);
		_CharWidths[CHAR_TAB] = (_CharWidths[CHAR_SPACE] = ContentPaint.measureText(" ")) * TABSpaceCount;
	}

	private void springBack() {
		Scroller.springBack(getScrollX(), getScrollY(), 0, Integer.MAX_VALUE, 0, _YScrollRange);
	}

	private ClipboardManager getClipboardManager() {
		return (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
	}

	private void onSelectionUpdate() {
		if (!isRangeSelecting()) {
			makeCursorVisible(_CursorLine, _CursorColumn);
			_CursorHorizonOffset = 0;
			int off = E[_CursorLine];
			int tar = off + _CursorColumn;
			for (; off < tar; off++) _CursorHorizonOffset += getCharWidth(S[off]);
		} else {
			_SStartHorizonOffset = 0;
			int off = E[_SStartLine = findLine(_SStart)];
			for (; off < _SStart; off++) _SStartHorizonOffset += getCharWidth(S[off]);
			_SEndHorizonOffset = 0;
			off = E[_SEndLine = findLine(_SEnd)];
			for (; off < _SEnd; off++) _SEndHorizonOffset += getCharWidth(S[off]);
		}
		if (_IMM != null) {
			int sst, sen;
			CursorAnchorInfo.Builder builder = new CursorAnchorInfo.Builder().setMatrix(null);
			if (isRangeSelecting()) {
				sst = _SStart;
				sen = _SEnd;
			} else {
				sst = sen = getCursorPosition();
				float x = 0;
				for (int i = E[_CursorLine]; i < sst; i++) x += getCharWidth(S[i]);
				float top = TextHeight * (_CursorLine - 1);
				builder.setInsertionMarkerLocation(x, top, top + YOffset, top + TextHeight, CursorAnchorInfo.FLAG_HAS_VISIBLE_REGION);
			}
			builder.setSelectionRange(sst, sen);
			_IMM.updateCursorAnchorInfo(this, builder.build());
			_IMM.updateSelection(this, sst, sen, -1, -1);
//			_IMM.restartInput(this);
		}
	}

	private void setComposingText(char[] cs) {
		if (_ComposingStart == -1) {
			_ComposingStart = getCursorPosition();
			insertChars(cs);
		} else
			replace(_ComposingStart, getCursorPosition(), cs);
		if (cs.length == 0)
			_ComposingStart = -1;
	}


	// --------------------------
	// -----Temporary Fields-----
	// --------------------------

	// TODO 还有512个字符都塞不满屏幕的情况！
	private char[] TMP = new char[512];
	private char[] TMP2 = new char[1];

	// -----------------------
	// -----Inner Classes-----
	// -----------------------

	private static class VInputConnection implements InputConnection {
		private VEdit Q;

		public VInputConnection(VEdit parent) {
			Q = parent;
		}

		@Override
		public CharSequence getTextBeforeCursor(int n, int flags) {
			int cursor = Q.isRangeSelecting() ? Q._SStart : Q.getCursorPosition();
			int st = Math.max(cursor - n, 0);
			return new String(Q.S, st, cursor - st);
		}

		@Override
		public CharSequence getTextAfterCursor(int n, int flags) {
			int cursor = Q.isRangeSelecting() ? Q._SStart : Q.getCursorPosition();
			return new String(Q.S, cursor, Math.min(cursor + n, Q._TextLength) - cursor);
		}

		@Override
		public CharSequence getSelectedText(int flags) {
			if (!Q.isRangeSelecting()) return "";
			return new String(Q.S, Q._SStart, Q._SEnd - Q._SStart);
		}

		@Override
		public int getCursorCapsMode(int reqModes) {
			// TODO Fix Me Maybe
			return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
		}

		@Override
		public boolean setComposingRegion(int start, int end) {
			if (start == end)
				Q._ComposingStart = -1;
			else
				Q._ComposingStart = start;
			return true;
		}

		@Override
		public boolean setComposingText(CharSequence text, int newCursorPosition) {
			char[] cs = new char[text.length()];
			for (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);
			Q.setComposingText(cs);
			return true;
		}

		@Override
		public boolean finishComposingText() {
			Q._ComposingStart = -1;
			return true;
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			char[] cs = new char[text.length()];
			for (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);
			Q.commitText(cs);
			return true;
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
			Q.processEvent(event);
			return true;
		}

		@Override
		public boolean setSelection(int start, int end) {
			if (start == end) {
				Q.finishSelecting();
				Q.moveCursor(start);
				return true;
			}
			Q._SStart = start;
			Q._SEnd = end;
			Q.postInvalidate();
			return true;
		}

		@Override
		public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
			// TODO Tough
			return null;
		}

		@Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength) {
			Q.deleteSurrounding(beforeLength, afterLength);
			return true;
		}

		@Override
		public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
			deleteSurroundingText(beforeLength, afterLength);
			return true;
		}

		@Override
		public boolean commitCompletion(CompletionInfo text) {
			// TODO Ha?
			return false;
		}

		@Override
		public boolean commitCorrection(CorrectionInfo correctionInfo) {
			// TODO Ha?
			return false;
		}

		@Override
		public boolean performEditorAction(int editorAction) {
			// TODO Ha?
			return false;
		}

		@Override
		public boolean performContextMenuAction(int id) {
			switch (id) {
				case android.R.id.copy: {
					if (Q.isRangeSelecting()) {
						ClipboardManager manager = Q.getClipboardManager();
						manager.setPrimaryClip(ClipData.newPlainText(null, Q.getSelectedText()));
						Q.finishSelecting();
					}
					break;
				}
				case android.R.id.cut:
					if (Q.isRangeSelecting()) {
						ClipboardManager manager = Q.getClipboardManager();
						manager.setPrimaryClip(ClipData.newPlainText(null, Q.getSelectedText()));
						int line = Q.findLine(Q._SEnd);
						Q.deleteChars(line, Q._SEnd - Q.E[line], Q._SEnd - Q._SStart);
						Q.finishSelecting();
					}
					break;
				case android.R.id.paste:
					ClipData data = Q.getClipboardManager().getPrimaryClip();
					if (data != null && data.getItemCount() > 0) {
						CharSequence s = data.getItemAt(0).coerceToText(Q.getContext());
						char[] cs = new char[s.length()];
						for (int i = 0; i < cs.length; i++) cs[i] = s.charAt(i);
						Q.commitText(cs);
					}
					break;
				case android.R.id.selectAll:
					Q.setSelectionRange(0, Q._TextLength);
					break;
			}
			return true;
		}

		@Override
		public boolean beginBatchEdit() {
			return false;
		}

		@Override
		public boolean endBatchEdit() {
			return false;
		}

		@Override
		public boolean clearMetaKeyStates(int states) {
			return false;
		}

		@Override
		public boolean reportFullscreenMode(boolean enabled) {
			return false;
		}

		@Override
		public boolean performPrivateCommand(String action, Bundle data) {
			return false;
		}

		@Override
		public boolean requestCursorUpdates(int cursorUpdateMode) {
			return false;
		}

		@Override
		public Handler getHandler() {
			return null;
		}

		@Override
		public void closeConnection() {
			finishComposingText();
		}

		@Override
		public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
			return false;
		}
	}

	private static class GlassCursor {
		private float h, radius;
		private Paint mp = new Paint();
		private VEdit parent;
		private Bitmap c0, c1, c2;

		public GlassCursor(VEdit parent) {
			this.parent = parent;
			mp.setStyle(Paint.Style.FILL);
			mp.setAntiAlias(true);
		}

		public void setHeight(float height) {
			h = height * 1.5f;
			radius = h * 0.4f;
			float yy = h - radius;
			float tmp = (float) Math.sqrt(yy * yy - radius * radius);
			float xx = tmp / yy * radius;
			yy = tmp / yy * tmp;
			float gradius = radius * 0.7f;
			Path path = new Path();
			path.moveTo(0, 0);
			path.lineTo(xx, yy);
			path.lineTo(-xx, yy);
			path.close();
			int ddd = (int) Math.ceil(radius * 2);
			c0 = Bitmap.createBitmap(ddd, (int) (h + 0.5), Bitmap.Config.ARGB_8888);
			c1 = Bitmap.createBitmap(ddd, ddd, Bitmap.Config.ARGB_8888);
			c2 = Bitmap.createBitmap(ddd, ddd, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(c0);
			canvas.translate(radius, 0);
			mp.setColor(parent._Scheme.getCursorColor());
			canvas.drawPath(path, mp);
			canvas.drawCircle(0, h - radius, radius, mp);
			mp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			canvas.drawCircle(0, h - radius, gradius, mp);
			mp.setXfermode(null);
			mp.setColor(parent._Scheme.getCursorGlassColor());
			canvas.drawCircle(0, h - radius, gradius, mp);

			canvas = new Canvas(c1);
			mp.setColor(parent._Scheme.getCursorColor());
			canvas.drawCircle(radius, radius, radius, mp);
			canvas.drawRect(radius, 0, radius * 2, radius, mp);
			mp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			canvas.drawCircle(radius, radius, gradius, mp);
			mp.setXfermode(null);
			mp.setColor(parent._Scheme.getCursorGlassColor());
			canvas.drawCircle(radius, radius, gradius, mp);

			canvas = new Canvas(c2);
			mp.setColor(parent._Scheme.getCursorColor());
			canvas.drawCircle(radius, radius, radius, mp);
			canvas.drawRect(0, 0, radius, radius, mp);
			mp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			canvas.drawCircle(radius, radius, gradius, mp);
			mp.setXfermode(null);
			mp.setColor(parent._Scheme.getCursorGlassColor());
			canvas.drawCircle(radius, radius, gradius, mp);
		}

		public void draw(Canvas canvas, float x, float y, byte type) {
			if (isRecycled()) setHeight(h / 1.5f);
			canvas.translate(x, y);
			switch (type) {
				case 0:
					canvas.drawBitmap(c0, -radius, 0, null);
					break;
				case 1:
					canvas.drawBitmap(c1, -radius * 2, 0, null);
					break;
				case 2:
					canvas.drawBitmap(c2, 0, 0, null);
					break;
			}
			canvas.translate(-x, -y);
		}

		public boolean isTouched(float x, float y, byte type) {
			if (isRecycled()) setHeight(h / 1.5f);
			Bitmap cur = null;
			switch (type) {
				case 0:
					cur = c0;
					x += radius;
					break;
				case 1:
					cur = c1;
					x += radius * 2;
					break;
				case 2:
					cur = c2;
					break;
			}
			if (cur == null) return false;
			if (x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight()) return false;
			return cur.getPixel((int) (x + 0.5), (int) (y + 0.5)) != Color.TRANSPARENT;
		}

		private static void tryRecycle(Bitmap bitmap) {
			if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
		}

		public boolean isRecycled() {
			return c0 == null;
		}

		public void recycle() {
			tryRecycle(c0);
			tryRecycle(c1);
			tryRecycle(c2);
			mp = null;
		}
	}

	public interface EditAction {
		void redo(VEdit edit);

		void undo(VEdit edit);

		void recycle();

		class MergedAction implements EditAction {
			public static final int MERGE_BUFFER = 16;

			private EditAction[] actions;
			private int pos;

			public static MergedAction obtain(EditAction ori, EditAction ac) {
				if (ori instanceof MergedAction) {
					MergedAction ret = (MergedAction) ori;
					ret.append(ac);
					return ret;
				} else return new MergedAction(new EditAction[] {ori, ac});
			}

			public MergedAction(EditAction[] actions) {
				this.actions = actions;
				this.pos = actions.length;
			}

			public void append(EditAction action) {
				if (pos == actions.length) {
					EditAction[] na = new EditAction[actions.length + MERGE_BUFFER];
					System.arraycopy(actions, 0, na, 0, actions.length);
					actions = na;
					na = null;
				}
				actions[pos++] = action;
			}

			@Override
			public void redo(VEdit edit) {
				for (int i = 0; i < pos; i++) actions[i].redo(edit);
			}

			@Override
			public void undo(VEdit edit) {
				for (int i = pos - 1; i >= 0; i--) actions[i].undo(edit);
			}

			@Override
			public void recycle() {
				actions = null;
			}
		}

		class ReplaceAction implements EditAction {
			private int line, column;
			private char[] origin;
			private char[] content;

			public ReplaceAction(int line, int column, char[] origin, char[] content) {
				this.line = line;
				this.column = column;
				this.origin = origin;
				this.content = content;
			}

			public void setOrigin(char[] cs) {
				this.origin = cs;
			}

			public void setChars(char[] cs) {
				this.content = cs;
			}

			@Override
			public void redo(VEdit edit) {
				int[] ret = edit._deleteChars(line, column, origin.length);
				ret = edit._insertChars(ret[0], ret[1], content);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void undo(VEdit edit) {
				int column = edit.E[this.line] + this.column - origin.length + content.length;
				int line = edit.findLine(column);
				column -= edit.E[line];
				int[] ret = edit._deleteChars(line, column, content.length);
				ret = edit._insertChars(ret[0], ret[1], origin);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void recycle() {
				origin = null;
				content = null;
			}
		}

		class DeleteCharAction implements EditAction {
			private int line, column;
			private char ch;

			public DeleteCharAction(int line, int column, char c) {
				this.line = line;
				this.column = column;
				this.ch = c;
			}

			public void setChar(char c) {
				this.ch = c;
			}

			@Override
			public void redo(VEdit edit) {
				ch = edit.S[edit.E[line] + column - 1];
				int[] ret = edit._deleteChar(line, column);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void undo(VEdit edit) {
				int column = edit.E[this.line] + this.column - 1;
				int line = edit.findLine(column);
				int[] ret = edit._insertChar(line, column - edit.E[line], ch);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void recycle() {
			}
		}

		class DeleteCharsAction implements EditAction {
			private int line, column;
			private char[] content;

			public DeleteCharsAction(int line, int column, char[] content) {
				this.line = line;
				this.column = column;
				this.content = content;
			}

			@Override
			public void redo(VEdit edit) {
				int[] ret = edit._deleteChars(line, column, content.length);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void undo(VEdit edit) {
				int column = edit.E[this.line] + this.column - content.length;
				int line = edit.findLine(column);
				int[] ret = edit._insertChars(line, column - edit.E[line], content);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void recycle() {
				content = null;
			}
		}

		class InsertCharAction implements EditAction {
			private int line, column;
			private char ch;

			public InsertCharAction(int line, int column, char ch) {
				this.line = line;
				this.column = column;
				this.ch = ch;
			}

			public void setChar(char ch) {
				this.ch = ch;
			}

			@Override
			public void redo(VEdit edit) {
				int[] ret = edit._insertChar(line, column, ch);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void undo(VEdit edit) {
				int nColumn = edit.E[line] + column + 1;
				int nLine = edit.findLine(nColumn);
				nColumn -= edit.E[nLine];
				int[] ret = edit._deleteChar(nLine, nColumn);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void recycle() {
			}
		}

		class InsertCharsAction implements EditAction {
			private int line, column;
			private char[] content;

			public InsertCharsAction(int line, int column, char[] content) {
				this.line = line;
				this.column = column;
				this.content = content;
			}

			public void appendText(char[] cs) {
				char[] n = new char[content.length + cs.length];
				System.arraycopy(content, 0, n, 0, content.length);
				System.arraycopy(cs, 0, n, content.length, cs.length);
				content = n;
				n = null;
			}

			public void setChars(char[] s) {
				content = s;
			}

			@Override
			public void redo(VEdit edit) {
				int[] ret = edit._insertChars(line, column, content);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void undo(VEdit edit) {
				int nColumn = edit.E[line] + column + content.length;
				int nLine = edit.findLine(nColumn);
				nColumn -= edit.E[nLine];
				int[] ret = edit._deleteChars(nLine, nColumn, content.length);
				edit.moveCursor(ret[0], ret[1]);
			}

			@Override
			public void recycle() {
				content = null;
			}
		}
	}

	public static class EditActionStack {
		private VEdit parent;
		private EditAction[] arr;
		private int _pos;
		private int _undoCount;
		private long LastActionTime = 0;

		public EditActionStack(VEdit parent) {
			this(parent, 64);
		}

		public EditActionStack(VEdit parent, int maxSize) {
			this.parent = parent;
			setMaxSize(maxSize);
		}

		public void setMaxSize(int size) {
			arr = new EditAction[size];
			_pos = _undoCount = 0;
		}

		public void clear() {
			_pos = _undoCount = 0;
		}

		public int getMaxSize() {
			return arr.length;
		}

		public void addAction(EditAction action) {
			long t = System.currentTimeMillis();
			long cur = t - LastActionTime;
			LastActionTime = t;
			if (cur <= MERGE_ACTIONS_INTERVAL) {
				EditAction lac = getLastAction();
				if (lac != null) {
					setLastAction(EditAction.MergedAction.obtain(lac, action));
					action.redo(parent);
					return;
				}
			}
			if (arr[_pos] != null) arr[_pos].recycle();
			arr[_pos++] = action;
			action.redo(parent);
			if (_pos == arr.length) _pos = 0;
		}

		public boolean undo() {
			if (_undoCount >= arr.length) return false;
			if (_pos == 0) _pos = arr.length;
			_pos--;
			if (arr[_pos] == null) {
				if (++_pos == arr.length) _pos = 0;
				return false;
			}
			arr[_pos].undo(parent);
			_undoCount++;
			return true;
		}

		public boolean redo() {
			if (_undoCount == 0) return false;
			arr[_pos].redo(parent);
			if (++_pos == arr.length) _pos = 0;
			_undoCount--;
			return true;
		}

		public EditAction getLastAction() {
			if (_undoCount >= arr.length) return null;
			int pp = _pos;
			if (pp == 0) pp = arr.length;
			return arr[pp - 1];
		}

		public void setLastAction(EditAction action) {
			if (_undoCount >= arr.length) return;
			int pp = _pos;
			if (pp == 0) pp = arr.length;
			arr[pp - 1] = action;
		}
	}
}