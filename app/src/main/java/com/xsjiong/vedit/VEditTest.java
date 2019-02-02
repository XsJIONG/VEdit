package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.OverScroller;

public class VEditTest extends View {
	// --------------------
	// -----Constants------
	// --------------------

	public static final int MEASURE_STEP = 50;
	public static final int LINENUM_SPLIT_WIDTH = 7;
	public static final boolean USE_WHOLE_LINE_DRAWING = true;


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
	private TextRegion _Selection = new TextRegion();
	private TextRegion _Composing = new TextRegion(-1, 0);
	private float _CursorWidth = 2;
	private float _LinePaddingTop = 5, _LinePaddingBottom = 5;
	private int _ColorSplitLine = 0xFF2196F3;
	private int _ColorSelectedLine = 0x3B2196F3;
	private int _ColorCursor = 0xFFFF5722;
	private float _ContentLeftPadding = 7;
	private int _TextLength;
	private boolean _Editable = true;
	private VInputConnection _InputConnection;
	private InputMethodManager _IMM;


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
		ColorPaint.setDither(false);
		_IMM = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}


	// ------------------
	// -----Methods------
	// ------------------

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

	public void setTextSize(int unit, float size) {
		setTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));
	}

	public void setTextSize(float size) {
		ContentPaint.setTextSize(size);
		LineNumberPaint.setTextSize(size);
		YOffset = -ContentPaint.ascent();
		TextHeight = ContentPaint.descent() + YOffset;
		TABWidth = TABSpaceCount * ContentPaint.measureText(" ");
		onLineChange();
		requestLayout();
		invalidate();
	}

	public int getSelctionStartLine() {
		return _Selection.StartLine;
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
		TABWidth = TABSpaceCount * ContentPaint.measureText(" ");
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
		E[E[0] = 1] = 0;
		for (int i = 0; i < s.length; i++) {
			if (s[i] == '\0') continue;
			if (s[i] == '\n') {
				if (++E[0] == E.length) {
					int[] ne = new int[E.length + 256];
					System.arraycopy(E, 0, ne, 0, E.length);
					E = ne;
				}
				E[E[0]] = i + 1;
			}
		}
		E[++E[0]] = s.length + 1;
		_TextLength = s.length;
		onLineChange();
		requestLayout();
		invalidate();
	}

	public int getSelectionStart() {
		return E[_Selection.StartLine] + _Selection.StartColumn;
	}

	public int getSelectionEnd() {
		return E[_Selection.EndLine] + _Selection.EndColumn;
	}

	public void setSelection(int pos) {
		int line = findLine(pos);
		int column = pos - E[line];
		_Selection.StartLine = _Selection.EndLine = line;
		_Selection.StartColumn = _Selection.EndColumn = column;
		invalidate();
	}

	public void setSelectionStart(int st) {
		_Selection.StartLine = findLine(st);
		_Selection.StartColumn = st - E[_Selection.StartLine];
		invalidate();
	}

	public void setSelectionEnd(int en) {
		_Selection.EndLine = findLine(en);
		_Selection.EndColumn = en - E[_Selection.EndLine];
		invalidate();
	}

	public void setSelectionRange(int st, int en) {
		_Selection.StartLine = findLine(st);
		_Selection.StartColumn = st - E[_Selection.StartLine];
		_Selection.EndLine = findLine(en);
		_Selection.EndColumn = en - E[_Selection.EndLine];
		invalidate();
	}

	public void setSelectionStartLine(int line) {
		_Selection.StartLine = line;
		_Selection.StartColumn = Math.min(_Selection.StartColumn, E[line + 1] - E[line] - 1);
		invalidate();
	}

	public void setSelectionEndLine(int line) {
		_Selection.EndLine = line;
		_Selection.EndColumn = Math.min(_Selection.EndColumn, E[line + 1] - E[line] - 1);
		invalidate();
	}

	public void setSelectionLine(int line) {
		_Selection.StartLine = _Selection.EndLine = line;
		_Selection.StartColumn = _Selection.EndColumn = 0;
		invalidate();
	}

	public void setSelectionStart(int line, int column) {
		_Selection.StartLine = line;
		_Selection.StartColumn = column;
		invalidate();
	}

	public void setSelectionEnd(int line, int column) {
		_Selection.EndLine = line;
		_Selection.EndColumn = column;
		invalidate();
	}

	public void setSelection(int line, int column) {
		_Selection.StartLine = _Selection.EndLine = line;
		_Selection.StartColumn = _Selection.EndColumn = column;
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
	private int _flingFactor = 1200;

	public void setFlingFactor(float factor) {
		_flingFactor = (int) (1000 * factor);
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
		return _Composing.StartLine != -1;
	}

	public void showIME() {
		_IMM.showSoftInput(this, InputMethodManager.RESULT_SHOWN);
		_IMM.restartInput(this);
	}

	public void hideIME() {
		_IMM.hideSoftInputFromWindow(getWindowToken(), 0);
	}


	// --------------------------
	// -----Override Methods-----
	// --------------------------

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
					invalidate();
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
		_YScrollRange = ContentHeight - (bottom - top);
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
		// TODO 在切换出输入法后切换到SelectedLine
		outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
		if (_InputConnection == null)
			_InputConnection = new VInputConnection(this);
		_Composing.StartLine = -1;
		return _InputConnection;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
		Log.i("VEdit", "FocusChange: " + gainFocus);
		if (!gainFocus)
			// TODO FLAG
			_IMM.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	// 绘制函数
	@Override
	protected void onDraw(Canvas canvas) {
		long st = System.currentTimeMillis();
		/*int StartLine = (int) (getScrollY() / TextHeight);
		float y = StartLine * TextHeight + -getScrollY() + YOffset;
		StartLine++;
		for (int line = StartLine; line < E[0]; line++) {
			canvas.drawText(S, E[line], E[line + 1] - E[line] - 1, 0, y, ContentPaint);
			y += TextHeight;
		}*/
		final boolean isSingleSelection = _Selection.StartLine == _Selection.EndLine && _Selection.StartColumn == _Selection.EndColumn && _Editable;
		final float nh = TextHeight + _LinePaddingBottom + _LinePaddingTop;
		final float bottom = getScrollY() + getHeight() + YOffset;
		final float right = getScrollX() + getWidth();
		final float xo = LineNumberWidth + LINENUM_SPLIT_WIDTH + _ContentLeftPadding;

		int line = Math.max((int) (getScrollY() / nh) + 1, 1);
		float y = (line - 1) * nh + YOffset + _LinePaddingTop;
		float XStart, wtmp, x;
		int i, en;
		int tot;
		ColorPaint.setColor(_ColorSplitLine);
		canvas.drawRect(LineNumberWidth, getScrollY(), LineNumberWidth + LINENUM_SPLIT_WIDTH, getScrollY() + getHeight(), ColorPaint);
		LineDraw:
		for (; line < E[0]; line++) {
			if (isSingleSelection && _Selection.StartLine == line) {
				ColorPaint.setColor(_ColorSelectedLine);
				canvas.drawRect(xo - _ContentLeftPadding, y - YOffset - _LinePaddingTop, right, y + TextHeight - YOffset + _LinePaddingBottom, ColorPaint);
			}
			canvas.drawText(Integer.toString(line), LineNumberWidth, y, LineNumberPaint);
			if (USE_WHOLE_LINE_DRAWING) {
				System.arraycopy(S, E[line], TMP, 0, tot = (E[line + 1] - E[line] - 1));
				canvas.drawText(TMP, 0, tot, xo, y, ContentPaint);
			} else {
				i = E[line];
				en = E[line + 1] - 1;
				XStart = xo;
				if (getScrollX() > 0)
					// TODO 这里需要判断TAB额外变出的长度
					while (true) {
						System.arraycopy(S, i, TMP2, 0, tot = Math.min(en - i, MEASURE_STEP));
						if ((wtmp = (XStart + ContentPaint.measureText(TMP2, 0, tot))) >= getScrollX())
							break;
						if ((i += MEASURE_STEP) >= en) {
							if ((y += nh) > bottom) break LineDraw;
							continue LineDraw;
						}
						XStart = wtmp;
					}
				tot = 0;
				for (x = XStart; i < en && x <= right; i++) {
					// Mark
					if ((TMP[tot] = S[i]) == '\t') {
						XStart += TABWidth;
						x += TABWidth;
					} else {
						// You See? 这个可怜的TMP3就是拿来测单个字符的
						TMP3[0] = TMP[tot++];
						// TODO 改成getTextWidths，效率++++
						x += Math.max(10, ContentPaint.measureText(TMP3, 0, 1));
					}
				}
				canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
			}
			if ((y += nh) >= bottom) break;
		}
		if (USE_WHOLE_LINE_DRAWING) {
			if (isSingleSelection) {
				System.arraycopy(S, E[_Selection.StartLine], TMP, 0, _Selection.StartColumn);
				float cursorStart = ContentPaint.measureText(TMP, 0, _Selection.StartColumn) + xo;
				ColorPaint.setColor(_ColorCursor);
				float yst = (_Selection.StartLine - 1) * nh;
				// TODO Width Control
				canvas.drawRect(cursorStart - _CursorWidth / 2, yst, cursorStart + _CursorWidth / 2, yst + nh, ColorPaint);
			}
		} else {
			// TODO Complete Me!
		}
		st = System.currentTimeMillis() - st;
		Log.i("VEdit", "耗时3: " + st);
	}


	// -------------------------
	// -----Private Methods-----
	// -------------------------

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
		x -= (LineNumberWidth + LINENUM_SPLIT_WIDTH + _ContentLeftPadding);
		_Selection.StartLine = _Selection.EndLine = (int) Math.ceil(y / (TextHeight + _LinePaddingTop + _LinePaddingBottom));
		char[] lc = getLineChars(_Selection.StartLine);
		float[] widths = new float[lc.length];
		ContentPaint.getTextWidths(lc, 0, lc.length, widths);
		lc = null;
		int ret = 0;
		for (float sum = -x; ret < widths.length; ret++) {
			if ((sum += widths[ret]) >= 0) {
				if ((-(sum - widths[ret])) > sum) // 是前面的更逼近一些
					ret++;
				break;
			}
		}
		_Selection.StartColumn = _Selection.EndColumn = ret;
		showIME();
		invalidate();
	}

	private void onLineChange() {
		ContentHeight = (int) ((TextHeight + _LinePaddingTop + _LinePaddingBottom) * (E[0] - 1));
		_YScrollRange = ContentHeight - getHeight();
		LineNumberWidth = LineNumberPaint.measureText("9") * ((int) Math.log10(E[0] - 1) + 1);
	}

	public void deleteChar() {
		if (!_Editable) return;
		if (_Selection.StartLine != _Selection.EndLine || _Selection.StartColumn != _Selection.EndColumn) return;
		if (_Selection.StartLine == 1 && _Selection.StartColumn == 0) return;
		int st = E[_Selection.StartLine] + _Selection.StartColumn;
		System.arraycopy(S, st, S, st - 1, _TextLength - st);
		if (_Selection.StartColumn == 0) {
			_Selection.StartColumn = _Selection.EndColumn = E[_Selection.StartLine] - E[_Selection.StartLine - 1] - 1;
			System.arraycopy(E, _Selection.StartLine + 1, E, _Selection.StartLine, E[0] - _Selection.StartLine);
			E[0]--;
			for (int i = _Selection.StartLine; i <= E[0]; i++) E[i]--;
			_Selection.StartLine = --_Selection.EndLine;
		} else {
			for (int i = _Selection.StartLine + 1; i <= E[0]; i++) E[i]--;
			_Selection.StartColumn = --_Selection.EndColumn;
		}
		_TextLength--;
		invalidate();
	}

	private void springBack() {
		Scroller.springBack(getScrollX(), getScrollY(), 0, Integer.MAX_VALUE, 0, _YScrollRange);
	}


	// --------------------------
	// -----Temporary Fields-----
	// --------------------------

	// TODO 还有512个字符都塞不满屏幕的情况！
	private char[] TMP = new char[512];
	private char[] TMP2 = new char[MEASURE_STEP];
	private char[] TMP3 = new char[1];


	// -----------------------
	// -----Inner Classes-----
	// -----------------------

	private static class TextRegion {
		int StartLine, StartColumn;
		int EndLine, EndColumn;

		public TextRegion() {
			StartLine = EndLine = 1;
			StartColumn = EndColumn = 0;
		}

		public TextRegion(int line, int column) {
			StartLine = EndLine = line;
			StartColumn = EndColumn = column;
		}

		public TextRegion(int stLine, int stColumn, int enLine, int enColumn) {
			this.StartLine = stLine;
			this.StartColumn = stColumn;
			this.EndLine = enLine;
			this.EndColumn = enColumn;
		}
	}

	private static class VInputConnection extends BaseInputConnection {
		private VEditTest Q;

		public VInputConnection(VEditTest parent) {
			super(parent, true);
			Q = parent;
		}

		@Override
		public CharSequence getTextBeforeCursor(int n, int flags) {
			// TODO 这里需要判断单点选择吗？
			int cursor = Q.getSelectionStart();
			int start = Math.max(cursor - n, 0);
			return new String(Q.S, start, cursor - start);
		}

		@Override
		public CharSequence getTextAfterCursor(int n, int flags) {
			int docLength = Q._TextLength;
			int cursor = Q.getSelectionStart();
			if ((cursor + n) >= docLength)
				return new String(Q.S, cursor, docLength - cursor - 1);
			return new String(Q.S, cursor, n);
		}

		@Override
		public CharSequence getSelectedText(int flags) {
			int cursor = Q.getSelectionStart();
			return new String(Q.S, cursor, Q.getSelectionEnd() - cursor);
		}

		@Override
		public int getCursorCapsMode(int reqModes) {
			// TODO Fix Me Maybe
			return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
		}

		@Override
		public boolean setComposingRegion(int start, int end) {
			TextRegion com = Q._Composing;
			com.StartLine = Q.findLine(start);
			com.StartColumn = start - Q.E[com.StartLine];
			com.EndLine = Q.findLine(end);
			com.EndColumn = end - Q.E[com.EndLine];
			Q.invalidate();
			// TODO 这里返回值什么鬼？
			return true;
		}

		@Override
		public boolean setComposingText(CharSequence text, int newCursorPosition) {
			return super.setComposingText(text, newCursorPosition);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
			if (event.getAction() != KeyEvent.ACTION_UP) return super.sendKeyEvent(event);
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DEL:
					Mark // 检测我的时间！
					Q.deleteChar();
					break;
				default:
					return super.sendKeyEvent(event);
			}
			return true;
		}
	}
}