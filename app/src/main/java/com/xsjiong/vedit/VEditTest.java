package com.xsjiong.vedit;

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

import java.util.Arrays;

public class VEditTest extends View {
	// --------------------
	// -----Constants------
	// --------------------

	private static final String T = "VEdit";
	public static final int LINENUM_SPLIT_WIDTH = 7;
	public static final int EXPAND_SIZE = 64;
	public static final int EMPTY_CHAR_WIDTH = 10;
	public static final int SCROLL_TO_CURSOR_EXTRA = 20;


	// -----------------
	// -----Fields------
	// -----------------

	private TextPaint ContentPaint, LineNumberPaint;
	private Paint ColorPaint;
	private float YOffset;
	private float TextHeight;
	private int ContentHeight;
	private char[] S;
	private int[] E = new int[257];
	private int _minFling, _touchSlop;
	private float _lastX, _lastY, _stX, _stY;
	private OverScroller Scroller;
	private VelocityTracker SpeedCalc;
	private boolean isDragging = false;
	private int TABSpaceCount = 4;
	private float TABWidth;
	private int _YScrollRange;
	private float LineNumberWidth;
	private int _maxOSX = 20, _maxOSY = 20;
	private int _SStart = -1, _SEnd;
	private int _CursorLine = 1, _CursorColumn = 0;
	private int _ComposingStart = -1, _ComposingEnd;
	private boolean _isComposing = false;
	private float _CursorWidth = 2, _ComposingWidth = 3;
	private float _LinePaddingTop = 5, _LinePaddingBottom = 5;
	private int _ColorSplitLine = 0xFF2196F3;
	private int _ColorSelectedLine = 0x3B2196F3;
	private int _ColorCursor = 0xFFFF5722;
	private int _ColorComposing = 0xFF222222;
	private float _ContentLeftPadding = 7;
	private int _TextLength;
	private boolean _Editable = true;
	private VInputConnection _InputConnection;
	private InputMethodManager _IMM;
	private boolean _ShowLineNumber = true;
	private float[] _CharWidths = new float[Short.MAX_VALUE - Short.MIN_VALUE];


	// -----------------------
	// -----Constructors------
	// -----------------------

	public VEditTest(Context cx) {
		this(cx, null, 0);
	}

	public VEditTest(Context cx, AttributeSet attr) {
		this(cx, attr, 0);
	}

	public VEditTest(Context cx, AttributeSet attr, int style) {
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
		_IMM = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}


	// ------------------
	// -----Methods------
	// ------------------

	public void setComposingWidth(float width) {
		_ComposingWidth = width;
		invalidate();
	}

	public float getComposingWidth() {
		return _ComposingWidth;
	}

	public void setComposingColor(int color) {
		_ColorComposing = color;
		invalidate();
	}

	public int getComposingColor() {
		return _ColorComposing;
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

	public void setTextColor(int color) {
		ContentPaint.setColor(color);
		invalidate();
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

	public void getSelectedLineColor(int color) {
		_ColorSelectedLine = color;
		invalidate();
	}

	public void setSplitLineColor(int color) {
		_ColorSplitLine = color;
		invalidate();
	}

	public void setCursorColor(int color) {
		_ColorCursor = color;
		invalidate();
	}

	public int getSelectedLineColor() {
		return _ColorSelectedLine;
	}

	public int getSplitLineColor() {
		return _ColorSplitLine;
	}

	public int getCursorColor() {
		return _ColorCursor;
	}

	public void setTABSpaceCount(int count) {
		TABSpaceCount = count;
		TABWidth = TABSpaceCount * LineNumberPaint.measureText(" ");
		invalidate();
	}

	public int getTABSpaceCount() {
		return TABSpaceCount;
	}

	public void setLinePadding(float top, float bottom) {
		_LinePaddingTop = top;
		_LinePaddingBottom = bottom;
		invalidate();
	}

	public float getLinePaddingTop() {
		return _LinePaddingTop;
	}

	public float getLinePaddingBottom() {
		return _LinePaddingBottom;
	}

	public void setText(String s) {
		setText(s.toCharArray());
	}

	public void setText(char[] s) {
		this.S = s;
		_TextLength = s.length;
		calculateEnters();
		onLineChange();
		requestLayout();
		invalidate();
	}

	public int getSelectionStart() {
		return _SStart;
	}

	public int getSelectionEnd() {
		return _SEnd;
	}

	public void setSelectionStart(int st) {
		_SStart = st;
		invalidate();
	}

	public void setSelectionEnd(int en) {
		_SEnd = en;
		invalidate();
	}

	public void setSelectionRange(int st, int en) {
		_SStart = st;
		_SEnd = en;
		invalidate();
	}

	public void moveCursor(int pos) {
		if (pos > _TextLength) pos = _TextLength;
		_CursorLine = findLine(pos);
		_CursorColumn = pos - E[_CursorLine];
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

	public boolean isComposing() {
		return _isComposing;
	}

	public void showIME() {
		_IMM.showSoftInput(this, 0);
		_IMM.restartInput(this);
	}

	public void hideIME() {
		_IMM.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	public void deleteChar() {
		int[] ret = deleteChar(_CursorLine, _CursorColumn);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
	}

	public int[] deleteChar(int line, int column) {
		if ((!_Editable) || (line == 1 && column == 0)) return new int[] {line, column};
		int pos = E[line] + column;
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
		postInvalidate();
		return new int[] {line, column};
	}

	public boolean isRangeSelecting() {
		return _SStart != -1;
	}

	public void insertChar(char c) {
		int[] ret = insertChar(_CursorLine, _CursorColumn, c);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
	}

	public int[] insertChar(int line, int column, char c) {
		if (!_Editable) return new int[] {line, column};
		int pos = E[line] + column;
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
		final float nh = TextHeight + _LinePaddingTop + _LinePaddingBottom;
		float y = nh * line - getHeight();
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
			sum += getCharWidth(S[i]);
		if (sum - _CursorWidth / 2 < getScrollX()) {
			scrollTo((int) (sum - _CursorWidth / 2) - SCROLL_TO_CURSOR_EXTRA, getScrollY());
			postInvalidate();
		} else if (sum + _CursorWidth / 2 > getScrollX() + getWidth()) {
			scrollTo((int) Math.ceil(sum + _CursorWidth / 2 - getWidth()) + SCROLL_TO_CURSOR_EXTRA, getScrollY());
			postInvalidate();
		}
	}

	public void finishComposing() {
		_isComposing = false;
		postInvalidate();
	}

	public void finishSelection() {
		_SStart = -1;
		postInvalidate();
	}

	public int getPosition(int line, int column) {
		return E[line] + column;
	}

	public int getCursorPosition() {
		return E[_CursorLine] + _CursorColumn;
	}

	public void insertChars(char[] cs) {
		int[] ret = insertChars(_CursorLine, _CursorColumn, cs);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
	}

	public int[] insertChars(int line, int column, char[] cs) {
		if (!_Editable) return new int[] {line, column};
		final int tl = cs.length;

		int pos = E[line] + column;
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
		postInvalidate();
		return new int[] {line, column};
	}

	public void deleteChars(int count) {
		int[] ret = deleteChars(_CursorLine, _CursorColumn, count);
		_CursorLine = ret[0];
		_CursorColumn = ret[1];
	}

	public int[] deleteChars(int line, int column, int count) {
		if ((!_Editable) || count == 0) return new int[] {line, column};
		if (count > _TextLength) {
			S = new char[0];
			E[0] = 2;
			E[1] = 0;
			E[2] = 1;
			_TextLength = 0;
		}
		final int pos = E[line] + column;
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
		int[] ret = deleteChars(line, en - E[line], en - st);
		ret = insertChars(ret[0], ret[1], cs);
		moveCursor(ret[0], ret[1]);
		// TODO Mark
	}

	public void moveCursorRelative(int count) {
		count = Math.min(E[_CursorLine] + _CursorColumn + count, _TextLength);
		_CursorLine = findLine(count);
		_CursorColumn = count - E[_CursorLine];
	}

	public void setComposingText(char[] s) {
		if (!_isComposing) {
			_ComposingStart = _ComposingEnd = E[_CursorLine] + _CursorColumn;
			_isComposing = true;
		}
		// TODO Make me 1
		replace(_ComposingStart, _ComposingEnd, s);
		_ComposingEnd = _ComposingStart + s.length;
//		moveCursor(_ComposingEnd);
	}


	// --------------------------
	// -----Override Methods-----
	// --------------------------


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
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_stX = _lastX = event.getX();
				_stY = _lastY = event.getY();
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
				requestFocus();
				return true;
			case MotionEvent.ACTION_MOVE:
				float x = event.getX(), y = event.getY();
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
				SpeedCalc.computeCurrentVelocity(_flingFactor);
				if (!isDragging)
					onClick(event.getX() + getScrollX(), event.getY() + getScrollY());
				else {
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
		return super.onTouchEvent(event);
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
		if (_InputConnection == null)
			_InputConnection = new VInputConnection(this);
		_isComposing = false;
		return _InputConnection;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if (gainFocus) {
			showIME();
		} else
			hideIME();
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	// TODO 解封我, maybe
	/*@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (processEvent(event)) return true;
		return super.onKeyPreIme(keyCode, event);
	}*/


	// 绘制函数
	@Override
	protected void onDraw(Canvas canvas) {
		long st = System.currentTimeMillis();
		final boolean showCursor = (!isRangeSelecting()) && _Editable;
		final float nh = TextHeight + _LinePaddingBottom + _LinePaddingTop;
		final float bottom = getScrollY() + getHeight() + YOffset;
		final int right = getScrollX() + getWidth();
		final float xo = (_ShowLineNumber ? LineNumberWidth + LINENUM_SPLIT_WIDTH : 0) + _ContentLeftPadding;
		final int cursorPos = getCursorPosition();

		int line = Math.max((int) (getScrollY() / nh) + 1, 1);
		float y = (line - 1) * nh + YOffset + _LinePaddingTop;
		float XStart, wtmp, x;
		int i, en;
		int tot;
		float composingStartX;
		if (_ShowLineNumber) {
			ColorPaint.setColor(_ColorSplitLine);
			canvas.drawRect(LineNumberWidth, getScrollY(), LineNumberWidth + LINENUM_SPLIT_WIDTH, getScrollY() + getHeight(), ColorPaint);
		}
		LineDraw:
		for (; line < E[0]; line++) {
			if (_ShowLineNumber)
				canvas.drawText(Integer.toString(line), LineNumberWidth, y, LineNumberPaint);
			// TODO HighLight 应该不是showCursor来判断吧？
			if (showCursor && _CursorLine == line) {
				ColorPaint.setColor(_ColorSelectedLine);
				canvas.drawRect(xo - _ContentLeftPadding, y - YOffset - _LinePaddingTop, right, y + TextHeight - YOffset + _LinePaddingBottom, ColorPaint);
			}
			i = E[line];
			en = E[line + 1] - 1;
			XStart = xo;
			if (getScrollX() > XStart)
				while ((wtmp = XStart + getCharWidth(S[i])) < getScrollX()) {
					if (++i >= en) {
						if ((y += nh) >= bottom) break LineDraw;
						continue LineDraw;
					}
					XStart = wtmp;
				}
			composingStartX = (i >= _ComposingStart && _isComposing) ? XStart : -1;
			tot = 0;
			for (x = XStart; i < en && x <= right; i++) {
				if (composingStartX == -1 && i == _ComposingStart && _isComposing)
					composingStartX = x;
				if (showCursor && i == cursorPos) {
					ColorPaint.setColor(_ColorCursor);
					ColorPaint.setStrokeWidth(_CursorWidth);
					canvas.drawLine(x, y - YOffset - _LinePaddingTop, x, y - YOffset + TextHeight + _LinePaddingBottom, ColorPaint);
				}
				if ((TMP[tot] = S[i]) == '\t') {
					XStart += TABWidth;
					x += TABWidth;
				} else
					x += getCharWidth(TMP[tot++]);
				if (_isComposing && i == _ComposingEnd - 1 && composingStartX != -1) {
					ColorPaint.setColor(_ColorComposing);
					ColorPaint.setStrokeWidth(_ComposingWidth);
					canvas.drawLine(composingStartX, y, x, y, ColorPaint);
				}
			}
			if (showCursor && i == cursorPos) {
				ColorPaint.setColor(_ColorCursor);
				ColorPaint.setStrokeWidth(_CursorWidth);
				canvas.drawLine(x, y - YOffset - _LinePaddingTop, x, y - YOffset + TextHeight + _LinePaddingBottom, ColorPaint);
			}
			canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
			if ((y += nh) >= bottom) break;
		}
		if (G.LOG_TIME) {
			st = System.currentTimeMillis() - st;
			Log.i(T, "耗时3: " + st);
		}
	}

	@Override
	public String toString() {
		return new String(S, 0, _TextLength);
	}

	// -------------------------
	// -----Private Methods-----
	// -------------------------

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
		x -= _ContentLeftPadding;
		if (_ShowLineNumber)
			x -= (LineNumberWidth + LINENUM_SPLIT_WIDTH);
		_CursorLine = Math.min((int) Math.ceil(y / (TextHeight + _LinePaddingTop + _LinePaddingBottom)), E[0] - 1);
		final int en = E[_CursorLine + 1] - 1;
		int ret = E[_CursorLine];
		for (float sum = -x; ret < en; ret++) {
			if ((sum += getCharWidth(S[ret])) >= 0) {
				if ((-(sum - getCharWidth(S[ret]))) > sum) // 是前面的更逼近一些
					ret++;
				break;
			}
		}
		_CursorColumn = ret - E[_CursorLine];
		makeCursorVisible(_CursorLine, _CursorColumn);
		showIME();
		postInvalidate();
	}

	private void onFontChange() {
		YOffset = -ContentPaint.ascent();
		TextHeight = ContentPaint.descent() + YOffset;
		TABWidth = TABSpaceCount * LineNumberPaint.measureText(" ");
		clearCharWidthCache();
		onLineChange();
		requestLayout();
		postInvalidate();
	}

	private void onLineChange() {
		ContentHeight = (int) ((TextHeight + _LinePaddingTop + _LinePaddingBottom) * (E[0] - 1));
		_YScrollRange = Math.max(ContentHeight - getHeight(), 0);
		LineNumberWidth = LineNumberPaint.measureText("9") * ((int) Math.log10(E[0] - 1) + 1);
	}

	private float getCharWidth(char c) {
		if (c == '\t') return TABWidth;
		int ind = (int) ((short) c) - Short.MIN_VALUE;
		if (_CharWidths[ind] == 0) {
			TMP2[0] = c;
			if ((_CharWidths[ind] = ContentPaint.measureText(TMP2, 0, 1)) == 0)
				_CharWidths[ind] = EMPTY_CHAR_WIDTH;
			return _CharWidths[ind];
		} else return _CharWidths[ind];
	}

	private void clearCharWidthCache() {
		Arrays.fill(_CharWidths, 0);
	}

	private void springBack() {
		Scroller.springBack(getScrollX(), getScrollY(), 0, Integer.MAX_VALUE, 0, _YScrollRange);
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
		private VEditTest Q;

		public VInputConnection(VEditTest parent) {
			// super(parent, true);
			Q = parent;
		}

		@Override
		public CharSequence getTextBeforeCursor(int n, int flags) {
			int cursor = Q.getCursorPosition();
			int st = Math.max(cursor - n, 0);
			return new String(Q.S, st, cursor - st);
		}

		@Override
		public CharSequence getTextAfterCursor(int n, int flags) {
			int cursor = Q.getCursorPosition();
			return new String(Q.S, cursor, Math.min(cursor + n, Q._TextLength) - cursor);
		}

		@Override
		public CharSequence getSelectedText(int flags) {
			if (Q._SStart == -1) return null;
			return new String(Q.S, Q._SStart, Q._SEnd - Q._SStart);
		}

		@Override
		public int getCursorCapsMode(int reqModes) {
			// TODO Fix Me Maybe
			return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
		}

		@Override
		public boolean setComposingRegion(int start, int end) {
			Q._ComposingStart = start;
			Q._ComposingEnd = end;
			Q._isComposing = true;
			Q.postInvalidate();
			return true;
		}

		@Override
		public boolean setComposingText(CharSequence text, int newCursorPosition) {
			char[] cs = new char[text.length()];
			for (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);
			if (newCursorPosition > 0)
				newCursorPosition += Q._ComposingEnd - 1;
			else
				newCursorPosition += Q._isComposing ? Q.getCursorPosition() : Q._ComposingStart;
			if (newCursorPosition < 0) newCursorPosition = 0;
			if (newCursorPosition > Q._TextLength)
				newCursorPosition = Q._TextLength;
			Q.setComposingText(cs);
			return true;
		}

		@Override
		public boolean finishComposingText() {
			Q.finishComposing();
			return true;
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			char[] cs = new char[text.length()];
			for (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);
			if (Q.isComposing()) {
				Q.setComposingText(cs);
				if (newCursorPosition > 1)
					Q.moveCursor(Q.getCursorPosition() + newCursorPosition - 1);
				else if (newCursorPosition <= 0)
					Q.moveCursor(Q.getCursorPosition() - text.length() - newCursorPosition);
				Q.finishComposing();
			} else
				Q.insertChars(cs);
			return true;
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
			Q.processEvent(event);
			return true;
		}

		@Override
		public boolean setSelection(int start, int end) {
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
			int pos = Q.getCursorPosition() + afterLength;
			int line = Q.findLine(pos);
			int[] ret = Q.deleteChars(line, pos - Q.E[line], afterLength + beforeLength);
			Q.moveCursor(ret[0], ret[1]);
			return true;
		}

		@Override
		public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
			// TODO Ha?
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
			// TODO ???
			return false;
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

		}

		@Override
		public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
			return false;
		}
	}
}