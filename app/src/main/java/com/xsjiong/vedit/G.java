package com.xsjiong.vedit;

public interface G {
	String T = "VEdit";
	int[] REFRESH_COLORS = {0xFF2196F3, 0xFFFBC02D, 0xFFFF5722, 0xFFE91E63, 0xFF7E57C2};
	boolean LOG_TIME = false;

	String D = "package com.xsjiong.vedit;\n" +
			"\n" +
			"import android.content.Context;\n" +
			"import android.graphics.*;\n" +
			"import android.os.Bundle;\n" +
			"import android.os.Handler;\n" +
			"import android.text.InputType;\n" +
			"import android.text.TextPaint;\n" +
			"import android.util.AttributeSet;\n" +
			"import android.util.Log;\n" +
			"import android.util.TypedValue;\n" +
			"import android.view.*;\n" +
			"import android.view.inputmethod.*;\n" +
			"import android.widget.OverScroller;\n" +
			"\n" +
			"import java.util.Arrays;\n" +
			"\n" +
			"public class VEdit extends View {\n" +
			"\t// --------------------\n" +
			"\t// -----Constants------\n" +
			"\t// --------------------\n" +
			"\n" +
			"\tpublic static final int LINENUM_SPLIT_WIDTH = 7;\n" +
			"\tpublic static final int EXPAND_SIZE = 64;\n" +
			"\tpublic static final int EMPTY_CHAR_WIDTH = 10;\n" +
			"\tpublic static final int SCROLL_TO_CURSOR_EXTRA = 20;\n" +
			"\tpublic static final short CHAR_SPACE = 32, CHAR_TAB = 9;\n" +
			"\n" +
			"\n" +
			"\t// -----------------\n" +
			"\t// -----Fields------\n" +
			"\t// -----------------\n" +
			"\n" +
			"\tprivate TextPaint ContentPaint, LineNumberPaint;\n" +
			"\tprivate Paint ColorPaint;\n" +
			"\tprivate float YOffset;\n" +
			"\tprivate float TextHeight;\n" +
			"\tprivate int ContentHeight;\n" +
			"\tprivate char[] S;\n" +
			"\tprivate int[] E = new int[257];\n" +
			"\tprivate int _minFling, _touchSlop;\n" +
			"\tprivate float _lastX, _lastY, _stX, _stY;\n" +
			"\tprivate OverScroller Scroller;\n" +
			"\tprivate VelocityTracker SpeedCalc;\n" +
			"\tprivate boolean isDragging = false;\n" +
			"\tprivate int TABSpaceCount = 4;\n" +
			"\tprivate int _YScrollRange;\n" +
			"\tprivate float LineNumberWidth;\n" +
			"\tprivate int _maxOSX = 20, _maxOSY = 20;\n" +
			"\tprivate int _SStart = -1, _SEnd;\n" +
			"\tprivate int _CursorLine = 1, _CursorColumn = 0;\n" +
			"\tprivate int _ComposingStart = -1, _ComposingEnd;\n" +
			"\tprivate boolean _isComposing = false;\n" +
			"\tprivate float _CursorWidth = 2, _ComposingWidth = 3;\n" +
			"\tprivate float _LinePaddingTop = 5, _LinePaddingBottom = 5;\n" +
			"\tprivate int _ColorSplitLine = 0xFF2196F3;\n" +
			"\tprivate int _ColorSelectedLine = 0x3B2196F3;\n" +
			"\tprivate int _ColorCursor = 0xFFFF5722;\n" +
			"\tprivate int _ColorComposing = 0xFF222222;\n" +
			"\tprivate float _ContentLeftPadding = 7;\n" +
			"\tprivate int _TextLength;\n" +
			"\tprivate boolean _Editable = true;\n" +
			"\tprivate VInputConnection _InputConnection;\n" +
			"\tprivate InputMethodManager _IMM;\n" +
			"\tprivate boolean _ShowLineNumber = true;\n" +
			"\tprivate float[] _CharWidths = new float[Short.MAX_VALUE - Short.MIN_VALUE];\n" +
			"\n" +
			"\n" +
			"\t// -----------------------\n" +
			"\t// -----Constructors------\n" +
			"\t// -----------------------\n" +
			"\n" +
			"\tpublic VEdit(Context cx) {\n" +
			"\t\tthis(cx, null, 0);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic VEdit(Context cx, AttributeSet attr) {\n" +
			"\t\tthis(cx, attr, 0);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic VEdit(Context cx, AttributeSet attr, int style) {\n" +
			"\t\tsuper(cx, attr, style);\n" +
			"\t\tScroller = new OverScroller(getContext());\n" +
			"\t\tSpeedCalc = VelocityTracker.obtain();\n" +
			"\t\tViewConfiguration config = ViewConfiguration.get(cx);\n" +
			"\t\t_minFling = config.getScaledMinimumFlingVelocity();\n" +
			"\t\t_touchSlop = config.getScaledTouchSlop();\n" +
			"\t\tContentPaint = new TextPaint();\n" +
			"\t\tContentPaint.setAntiAlias(true);\n" +
			"\t\tLineNumberPaint = new TextPaint();\n" +
			"\t\tLineNumberPaint.setAntiAlias(true);\n" +
			"\t\tLineNumberPaint.setColor(Color.BLACK);\n" +
			"\t\tLineNumberPaint.setTextAlign(Paint.Align.RIGHT);\n" +
			"\t\tsetTextSize(50);\n" +
			"\t\tContentPaint.setColor(Color.BLACK);\n" +
			"\t\tColorPaint = new Paint();\n" +
			"\t\tColorPaint.setAntiAlias(false);\n" +
			"\t\tColorPaint.setStyle(Paint.Style.FILL);\n" +
			"\t\tColorPaint.setDither(false);\n" +
			"\t\t_IMM = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);\n" +
			"\t\tsetFocusable(true);\n" +
			"\t\tsetFocusableInTouchMode(true);\n" +
			"\t}\n" +
			"\n" +
			"\n" +
			"\t// ------------------\n" +
			"\t// -----Methods------\n" +
			"\t// ------------------\n" +
			"\n" +
			"\tpublic void setComposingWidth(float width) {\n" +
			"\t\t_ComposingWidth = width;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic float getComposingWidth() {\n" +
			"\t\treturn _ComposingWidth;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setComposingColor(int color) {\n" +
			"\t\t_ColorComposing = color;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getComposingColor() {\n" +
			"\t\treturn _ColorComposing;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTypeface(Typeface typeface) {\n" +
			"\t\tContentPaint.setTypeface(typeface);\n" +
			"\t\tLineNumberPaint.setTypeface(typeface);\n" +
			"\t\tonFontChange();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setShowLineNumber(boolean flag) {\n" +
			"\t\t_ShowLineNumber = flag;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic boolean isShowLineNumber() {\n" +
			"\t\treturn _ShowLineNumber;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setEditable(boolean editable) {\n" +
			"\t\t_Editable = editable;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setContentLeftPadding(float padding) {\n" +
			"\t\t_ContentLeftPadding = padding;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic float getContentLeftPadding() {\n" +
			"\t\treturn _ContentLeftPadding;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getLineNumber() {\n" +
			"\t\treturn E[0] - 1;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getLineStart(int line) {\n" +
			"\t\treturn E[line];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getLineEnd(int line) {\n" +
			"\t\treturn E[line + 1] - 1;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic char[] getLineChars(int line) {\n" +
			"\t\tchar[] ret = new char[E[line + 1] - E[line] - 1];\n" +
			"\t\tSystem.arraycopy(S, E[line], ret, 0, ret.length);\n" +
			"\t\treturn ret;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic String getLineString(int line) {\n" +
			"\t\treturn new String(getLineChars(line));\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextColor(int color) {\n" +
			"\t\tContentPaint.setColor(color);\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextAntiAlias(boolean flag) {\n" +
			"\t\tContentPaint.setAntiAlias(flag);\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\t// Recommend using \"setScale\" since \"setTextSize\" will clear the text width cache, which makes drawing slower\n" +
			"\tpublic void setTextSize(int unit, float size) {\n" +
			"\t\tsetTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextSize(float size) {\n" +
			"\t\tContentPaint.setTextSize(size);\n" +
			"\t\tLineNumberPaint.setTextSize(size);\n" +
			"\t\tonFontChange();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setSelectionColor(int color) {\n" +
			"\t\t_ColorSelectedLine = color;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setSplitLineColor(int color) {\n" +
			"\t\t_ColorSplitLine = color;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setCursorLineColor(int color) {\n" +
			"\t\t_ColorCursor = color;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int setSelectionColor() {\n" +
			"\t\treturn _ColorSelectedLine;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getSplitLineColor() {\n" +
			"\t\treturn _ColorSplitLine;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getCursorLineColor() {\n" +
			"\t\treturn _ColorCursor;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTABSpaceCount(int count) {\n" +
			"\t\tTABSpaceCount = count;\n" +
			"\t\t_CharWidths[CHAR_TAB] = TABSpaceCount * _CharWidths[CHAR_SPACE];\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getTABSpaceCount() {\n" +
			"\t\treturn TABSpaceCount;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setLinePadding(float top, float bottom) {\n" +
			"\t\t_LinePaddingTop = top;\n" +
			"\t\t_LinePaddingBottom = bottom;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic float getLinePaddingTop() {\n" +
			"\t\treturn _LinePaddingTop;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic float getLinePaddingBottom() {\n" +
			"\t\treturn _LinePaddingBottom;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setText(String s) {\n" +
			"\t\tsetText(s.toCharArray());\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setText(char[] s) {\n" +
			"\t\tthis.S = s;\n" +
			"\t\t_TextLength = s.length;\n" +
			"\t\tcalculateEnters();\n" +
			"\t\tonLineChange();\n" +
			"\t\trequestLayout();\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getSelectionStart() {\n" +
			"\t\treturn _SStart;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getSelectionEnd() {\n" +
			"\t\treturn _SEnd;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setSelectionStart(int st) {\n" +
			"\t\t_SStart = st;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setSelectionEnd(int en) {\n" +
			"\t\t_SEnd = en;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setSelectionRange(int st, int en) {\n" +
			"\t\t_SStart = st;\n" +
			"\t\t_SEnd = en;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void moveCursor(int pos) {\n" +
			"\t\tif (pos > _TextLength) pos = _TextLength;\n" +
			"\t\t_CursorLine = findLine(pos);\n" +
			"\t\t_CursorColumn = pos - E[_CursorLine];\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void moveCursor(int line, int column) {\n" +
			"\t\tif (line > E[0] - 1) {\n" +
			"\t\t\tline = E[0] - 1;\n" +
			"\t\t\tcolumn = E[E[0]] - E[E[0] - 1] - 1;\n" +
			"\t\t} else if (column > E[line + 1] - E[line] - 1)\n" +
			"\t\t\tcolumn = E[line + 1] - E[line] - 1;\n" +
			"\t\t_CursorLine = line;\n" +
			"\t\t_CursorColumn = column;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setMaxOverScroll(int x, int y) {\n" +
			"\t\t_maxOSX = x;\n" +
			"\t\t_maxOSY = y;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getMaxOverScrollX() {\n" +
			"\t\treturn _maxOSX;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getMaxOverScrollY() {\n" +
			"\t\treturn _maxOSY;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate boolean _fixScroll = true;\n" +
			"\n" +
			"\tpublic void setFixScroll(boolean flag) {\n" +
			"\t\t_fixScroll = flag;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic boolean isFixScroll() {\n" +
			"\t\treturn _fixScroll;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate boolean _dragDirection;\n" +
			"\tprivate int _flingFactor = 1000;\n" +
			"\n" +
			"\tpublic void setFlingFactor(int factor) {\n" +
			"\t\t_flingFactor = factor;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setCursorWidth(float width) {\n" +
			"\t\t_CursorWidth = width;\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic float getCursorWidth() {\n" +
			"\t\treturn _CursorWidth;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getLineLength(int line) {\n" +
			"\t\treturn E[line + 1] - E[line] - 1;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getTextLength() {\n" +
			"\t\treturn _TextLength;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic boolean isComposing() {\n" +
			"\t\treturn _isComposing;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void showIME() {\n" +
			"\t\t_IMM.showSoftInput(this, 0);\n" +
			"\t\t_IMM.restartInput(this);\n" +
			"\t\t_IMM.restartInput(this);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void hideIME() {\n" +
			"\t\t_IMM.hideSoftInputFromWindow(getWindowToken(), 0);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void deleteChar() {\n" +
			"\t\tint[] ret = deleteChar(_CursorLine, _CursorColumn);\n" +
			"\t\t_CursorLine = ret[0];\n" +
			"\t\t_CursorColumn = ret[1];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int[] deleteChar(int line, int column) {\n" +
			"\t\tif ((!_Editable) || (line == 1 && column == 0)) return new int[] {line, column};\n" +
			"\t\tint pos = E[line] + column;\n" +
			"\t\tif (_TextLength > pos)\n" +
			"\t\t\tSystem.arraycopy(S, pos, S, pos - 1, _TextLength - pos);\n" +
			"\t\tif (column == 0) {\n" +
			"\t\t\tcolumn = E[line] - E[line - 1] - 1;\n" +
			"\t\t\tSystem.arraycopy(E, line + 1, E, line, E[0] - line);\n" +
			"\t\t\tE[0]--;\n" +
			"\t\t\tfor (int i = line; i <= E[0]; i++) E[i]--;\n" +
			"\t\t\tline--;\n" +
			"\t\t\tonLineChange();\n" +
			"\t\t} else {\n" +
			"\t\t\tfor (int i = line + 1; i <= E[0]; i++) E[i]--;\n" +
			"\t\t\tcolumn--;\n" +
			"\t\t}\n" +
			"\t\t_TextLength--;\n" +
			"\t\tpostInvalidate();\n" +
			"\t\treturn new int[] {line, column};\n" +
			"\t}\n" +
			"\n" +
			"\tpublic boolean isRangeSelecting() {\n" +
			"\t\treturn _SStart != -1;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void insertChar(char c) {\n" +
			"\t\tint[] ret = insertChar(_CursorLine, _CursorColumn, c);\n" +
			"\t\t_CursorLine = ret[0];\n" +
			"\t\t_CursorColumn = ret[1];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int[] insertChar(int line, int column, char c) {\n" +
			"\t\tif (!_Editable) return new int[] {line, column};\n" +
			"\t\tint pos = E[line] + column;\n" +
			"\t\tif (S.length <= _TextLength + 1) {\n" +
			"\t\t\tchar[] ns = new char[S.length + EXPAND_SIZE];\n" +
			"\t\t\tSystem.arraycopy(S, 0, ns, 0, pos);\n" +
			"\t\t\tif (pos != _TextLength) System.arraycopy(S, pos, ns, pos + 1, _TextLength - pos);\n" +
			"\t\t\tS = ns;\n" +
			"\t\t\tS[pos] = c;\n" +
			"\t\t\tns = null;\n" +
			"\t\t\t// TODO Should GC Here?\n" +
			"\t\t\tSystem.gc();\n" +
			"\t\t} else {\n" +
			"\t\t\t// 没办法用System.arraycopy，因为考虑到顺序，可能会覆盖\n" +
			"\t\t\tfor (int i = _TextLength; i >= pos; i--) S[i + 1] = S[i];\n" +
			"\t\t\tS[pos] = c;\n" +
			"\t\t}\n" +
			"\t\tif (c == '\\n') {\n" +
			"\t\t\t// 理由同上，注意这是>不是>=\n" +
			"\t\t\tif (E[0] + 1 == E.length) expandEArray();\n" +
			"\t\t\tfor (int i = E[0]; i > line; i--) E[i + 1] = E[i] + 1;\n" +
			"\t\t\tE[0]++;\n" +
			"\t\t\tE[line + 1] = pos + 1;\n" +
			"\t\t\tline++;\n" +
			"\t\t\tcolumn = 0;\n" +
			"\t\t\tonLineChange();\n" +
			"\t\t} else {\n" +
			"\t\t\tfor (int i = E[0]; i > line; i--) E[i]++;\n" +
			"\t\t\tcolumn++;\n" +
			"\t\t}\n" +
			"\t\t_TextLength++;\n" +
			"\t\tpostInvalidate();\n" +
			"\t\treturn new int[] {line, column};\n" +
			"\t}\n" +
			"\n" +
			"\tpublic String getText(int st, int en) {\n" +
			"\t\treturn new String(S, st, en - st);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic char[] getChars(int st, int en) {\n" +
			"\t\tchar[] ret = new char[en - st];\n" +
			"\t\tSystem.arraycopy(S, st, ret, 0, ret.length);\n" +
			"\t\treturn ret;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic String getText() {\n" +
			"\t\treturn getText(0, _TextLength);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic char[] getChars() {\n" +
			"\t\treturn getChars(0, _TextLength);\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void makeLineVisible(int line) {\n" +
			"\t\tfinal float nh = TextHeight + _LinePaddingTop + _LinePaddingBottom;\n" +
			"\t\tfloat y = nh * line - getHeight();\n" +
			"\t\tif (getScrollY() < y) {\n" +
			"\t\t\tscrollTo(getScrollX(), (int) Math.ceil(y));\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void makeCursorVisible(int line, int column) {\n" +
			"\t\tint pos = E[line] + column;\n" +
			"\t\tmakeLineVisible(line);\n" +
			"\t\tfloat sum = (_ShowLineNumber ? (LineNumberWidth + LINENUM_SPLIT_WIDTH) : 0) + _ContentLeftPadding;\n" +
			"\t\tfor (int i = E[line]; i < pos; i++)\n" +
			"\t\t\tsum += _CharWidths[S[i]];\n" +
			"\t\tif (sum - _CursorWidth / 2 < getScrollX()) {\n" +
			"\t\t\tscrollTo((int) (sum - _CursorWidth / 2) - SCROLL_TO_CURSOR_EXTRA, getScrollY());\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t} else if (sum + _CursorWidth / 2 > getScrollX() + getWidth()) {\n" +
			"\t\t\tscrollTo((int) Math.ceil(sum + _CursorWidth / 2 - getWidth()) + SCROLL_TO_CURSOR_EXTRA, getScrollY());\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void finishComposing() {\n" +
			"\t\t_isComposing = false;\n" +
			"\t\tpostInvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void finishSelecting() {\n" +
			"\t\t_SStart = -1;\n" +
			"\t\tpostInvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getPosition(int line, int column) {\n" +
			"\t\treturn E[line] + column;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getCursorPosition() {\n" +
			"\t\treturn E[_CursorLine] + _CursorColumn;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void insertChars(char[] cs) {\n" +
			"\t\tint[] ret = insertChars(_CursorLine, _CursorColumn, cs);\n" +
			"\t\t_CursorLine = ret[0];\n" +
			"\t\t_CursorColumn = ret[1];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int[] insertChars(int line, int column, char[] cs) {\n" +
			"\t\tif (!_Editable) return new int[] {line, column};\n" +
			"\t\tfinal int tl = cs.length;\n" +
			"\n" +
			"\t\tint pos = E[line] + column;\n" +
			"\t\tint nh = _TextLength + tl;\n" +
			"\t\tif (nh > S.length) {\n" +
			"\t\t\tchar[] ns = new char[nh + EXPAND_SIZE];\n" +
			"\t\t\tSystem.arraycopy(S, 0, ns, 0, pos);\n" +
			"\t\t\tSystem.arraycopy(cs, 0, ns, pos, tl);\n" +
			"\t\t\tif (pos != _TextLength) System.arraycopy(S, pos, ns, pos + tl, _TextLength - pos);\n" +
			"\t\t\tS = ns;\n" +
			"\t\t\tns = null;\n" +
			"\t\t\tSystem.gc();\n" +
			"\t\t} else {\n" +
			"\t\t\tfor (int i = _TextLength - 1; i >= pos; i--) S[i + tl] = S[i];\n" +
			"\t\t\tSystem.arraycopy(cs, 0, S, pos, tl);\n" +
			"\t\t}\n" +
			"\t\t_TextLength += tl;\n" +
			"\t\tint tot = 0;\n" +
			"\t\tint[] tmp = new int[EXPAND_SIZE];\n" +
			"\t\tfor (int i = 0; i < tl; i++)\n" +
			"\t\t\tif (cs[i] == '\\n') {\n" +
			"\t\t\t\tif (++tot == tmp.length) {\n" +
			"\t\t\t\t\tint[] tmp2 = new int[tmp.length + EXPAND_SIZE];\n" +
			"\t\t\t\t\tSystem.arraycopy(tmp, 0, tmp2, 0, tmp.length);\n" +
			"\t\t\t\t\ttmp = tmp2;\n" +
			"\t\t\t\t\ttmp2 = null;\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\ttmp[tot] = i + pos + 1;\n" +
			"\t\t\t}\n" +
			"\t\tnh = E[0] + tot + 1;\n" +
			"\t\tif (nh > E.length) {\n" +
			"\t\t\tint[] ne = new int[nh];\n" +
			"\t\t\tSystem.arraycopy(E, 0, ne, 0, line + 1);\n" +
			"\t\t\tSystem.arraycopy(tmp, 1, ne, line + 1, tot);\n" +
			"\t\t\tSystem.arraycopy(E, line + 1, ne, line + tot + 1, E[0] - line);\n" +
			"\t\t\tne[0] = E[0] + tot;\n" +
			"\t\t\tfor (int i = line + tot + 1; i <= ne[0]; i++) ne[i] += tl;\n" +
			"\t\t\tE = ne;\n" +
			"\t\t\tne = null;\n" +
			"\t\t} else {\n" +
			"\t\t\tfor (int i = E[0]; i > line; i--) E[i + tot] = E[i] + tl;\n" +
			"\t\t\tSystem.arraycopy(tmp, 1, E, line + 1, tot);\n" +
			"\t\t\tE[0] += tot;\n" +
			"\t\t}\n" +
			"\t\tif (tot != 0) onLineChange();\n" +
			"\t\tline += tot;\n" +
			"\t\tif (tot == 0)\n" +
			"\t\t\tcolumn += tl;\n" +
			"\t\telse\n" +
			"\t\t\tcolumn = pos + tl - E[line];\n" +
			"\t\tpostInvalidate();\n" +
			"\t\treturn new int[] {line, column};\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void deleteChars(int count) {\n" +
			"\t\tint[] ret = deleteChars(_CursorLine, _CursorColumn, count);\n" +
			"\t\t_CursorLine = ret[0];\n" +
			"\t\t_CursorColumn = ret[1];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int[] deleteChars(int line, int column, int count) {\n" +
			"\t\tif ((!_Editable) || count == 0) return new int[] {line, column};\n" +
			"\t\tif (count > _TextLength) {\n" +
			"\t\t\tS = new char[0];\n" +
			"\t\t\tE[0] = 2;\n" +
			"\t\t\tE[1] = 0;\n" +
			"\t\t\tE[2] = 1;\n" +
			"\t\t\t_TextLength = 0;\n" +
			"\t\t}\n" +
			"\t\tfinal int pos = E[line] + column;\n" +
			"\t\tif (pos < count) count = pos;\n" +
			"\n" +
			"\t\tint tot = 0;\n" +
			"\t\tfor (int i = 1; i <= count; i++)\n" +
			"\t\t\tif (S[pos - i] == '\\n') tot++;\n" +
			"\t\tif (_TextLength > pos)\n" +
			"\t\t\tSystem.arraycopy(S, pos, S, pos - count, _TextLength - pos);\n" +
			"\t\t_TextLength -= count;\n" +
			"\t\tE[0] -= tot;\n" +
			"\t\tfor (int i = line - tot + 1; i <= E[0]; i++) E[i] = E[i + tot] - count;\n" +
			"\t\tif (tot != 0) onLineChange();\n" +
			"\t\tline -= tot;\n" +
			"\t\tif (tot == 0)\n" +
			"\t\t\tcolumn -= count;\n" +
			"\t\telse\n" +
			"\t\t\tcolumn = pos - count - E[line];\n" +
			"\t\tpostInvalidate();\n" +
			"\t\treturn new int[] {line, column};\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void replace(int st, int en, char[] cs) {\n" +
			"\t\tif (st > en) {\n" +
			"\t\t\tint tmp = en;\n" +
			"\t\t\ten = st;\n" +
			"\t\t\tst = tmp;\n" +
			"\t\t}\n" +
			"\t\tint line = findLine(en);\n" +
			"\t\tint[] ret = deleteChars(line, en - E[line], en - st);\n" +
			"\t\tret = insertChars(ret[0], ret[1], cs);\n" +
			"\t\tmoveCursor(ret[0], ret[1]);\n" +
			"\t\t// TODO Mark\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void moveCursorRelative(int count) {\n" +
			"\t\tcount = Math.min(E[_CursorLine] + _CursorColumn + count, _TextLength);\n" +
			"\t\t_CursorLine = findLine(count);\n" +
			"\t\t_CursorColumn = count - E[_CursorLine];\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setComposingText(char[] s) {\n" +
			"\t\tif (!_isComposing) {\n" +
			"\t\t\t_ComposingStart = _ComposingEnd = E[_CursorLine] + _CursorColumn;\n" +
			"\t\t\t_isComposing = true;\n" +
			"\t\t}\n" +
			"\t\t// TODO Make me 1\n" +
			"\t\treplace(_ComposingStart, _ComposingEnd, s);\n" +
			"\t\t_ComposingEnd = _ComposingStart + s.length;\n" +
			"//\t\tmoveCursor(_ComposingEnd);\n" +
			"\t}\n" +
			"\n" +
			"\n" +
			"\t// --------------------------\n" +
			"\t// -----Override Methods-----\n" +
			"\t// --------------------------\n" +
			"\n" +
			"\n" +
			"\t@Override\n" +
			"\tprotected void onSizeChanged(int w, int h, int oldw, int oldh) {\n" +
			"\t\tsuper.onSizeChanged(w, h, oldw, oldh);\n" +
			"\t\tif ((!isRangeSelecting()) && h < oldh)\n" +
			"\t\t\tmakeLineVisible(_CursorLine);\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tprotected int getSuggestedMinimumHeight() {\n" +
			"\t\treturn ContentHeight;\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic boolean onTouchEvent(MotionEvent event) {\n" +
			"\t\tSpeedCalc.addMovement(event);\n" +
			"\t\tswitch (event.getActionMasked()) {\n" +
			"\t\t\tcase MotionEvent.ACTION_DOWN:\n" +
			"\t\t\t\t_stX = _lastX = event.getX();\n" +
			"\t\t\t\t_stY = _lastY = event.getY();\n" +
			"\t\t\t\tif (!Scroller.isFinished())\n" +
			"\t\t\t\t\tScroller.abortAnimation();\n" +
			"\t\t\t\trequestFocus();\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t\tcase MotionEvent.ACTION_MOVE:\n" +
			"\t\t\t\tfloat x = event.getX(), y = event.getY();\n" +
			"\t\t\t\tif ((!isDragging) && (Math.abs(x - _stX) > _touchSlop || Math.abs(y - _stY) > _touchSlop)) {\n" +
			"\t\t\t\t\tisDragging = true;\n" +
			"\t\t\t\t\tif (_fixScroll)\n" +
			"\t\t\t\t\t\t_dragDirection = Math.abs(x - _lastX) > Math.abs(y - _lastY);\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tif (isDragging) {\n" +
			"\t\t\t\t\tint finalX = getScrollX(), finalY = getScrollY();\n" +
			"\t\t\t\t\tif (_fixScroll) {\n" +
			"\t\t\t\t\t\tif (_dragDirection) {\n" +
			"\t\t\t\t\t\t\tfinalX += (_lastX - x);\n" +
			"\t\t\t\t\t\t\t// TODO 如果要改X边界记得这儿加上\n" +
			"\t\t\t\t\t\t\tif (finalX < -_maxOSX) finalX = -_maxOSX;\n" +
			"\t\t\t\t\t\t} else {\n" +
			"\t\t\t\t\t\t\tfinalY += (_lastY - y);\n" +
			"\t\t\t\t\t\t\tif (finalY < -_maxOSY) finalY = -_maxOSY;\n" +
			"\t\t\t\t\t\t\telse if (finalY > _YScrollRange + _maxOSY)\n" +
			"\t\t\t\t\t\t\t\tfinalY = _YScrollRange + _maxOSY;\n" +
			"\t\t\t\t\t\t}\n" +
			"\t\t\t\t\t} else {\n" +
			"\t\t\t\t\t\tfinalX += (_lastX - x);\n" +
			"\t\t\t\t\t\t// TODO 如果要改X边界记得这儿加上\n" +
			"\t\t\t\t\t\tif (finalX < -_maxOSX) finalX = -_maxOSX;\n" +
			"\t\t\t\t\t\tfinalY += (_lastY - y);\n" +
			"\t\t\t\t\t\tif (finalY < -_maxOSY) finalY = -_maxOSY;\n" +
			"\t\t\t\t\t\telse if (finalY > _YScrollRange + _maxOSY)\n" +
			"\t\t\t\t\t\t\tfinalY = _YScrollRange + _maxOSY;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t\tscrollTo(finalX, finalY);\n" +
			"\t\t\t\t\tpostInvalidate();\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\t_lastX = x;\n" +
			"\t\t\t\t_lastY = y;\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t\tcase MotionEvent.ACTION_CANCEL:\n" +
			"\t\t\tcase MotionEvent.ACTION_UP:\n" +
			"\t\t\t\tSpeedCalc.computeCurrentVelocity(_flingFactor);\n" +
			"\t\t\t\tif (!isDragging)\n" +
			"\t\t\t\t\tonClick(event.getX() + getScrollX(), event.getY() + getScrollY());\n" +
			"\t\t\t\telse {\n" +
			"\t\t\t\t\tisDragging = false;\n" +
			"\t\t\t\t\tint speedX = (int) SpeedCalc.getXVelocity();\n" +
			"\t\t\t\t\tint speedY = (int) SpeedCalc.getYVelocity();\n" +
			"\t\t\t\t\tif (Math.abs(speedX) <= _minFling) speedX = 0;\n" +
			"\t\t\t\t\tif (Math.abs(speedY) <= _minFling) speedY = 0;\n" +
			"\t\t\t\t\tif (_fixScroll) {\n" +
			"\t\t\t\t\t\tif (_dragDirection) speedY = 0;\n" +
			"\t\t\t\t\t\telse speedX = 0;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t\tif (speedX != 0 || speedY != 0)\n" +
			"\t\t\t\t\t\tScroller.fling(getScrollX(), getScrollY(), -speedX, -speedY, -_maxOSX, Integer.MAX_VALUE, -_maxOSY, _YScrollRange + _maxOSY);\n" +
			"\t\t\t\t\telse springBack();\n" +
			"\t\t\t\t\tSpeedCalc.clear();\n" +
			"\t\t\t\t\tinvalidate();\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\t\treturn super.onTouchEvent(event);\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tprotected void onLayout(boolean changed, int left, int top, int right, int bottom) {\n" +
			"\t\tsuper.onLayout(changed, left, top, right, bottom);\n" +
			"\t\t_YScrollRange = Math.max(ContentHeight - (bottom - top), 0);\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic void computeScroll() {\n" +
			"\t\tif (Scroller.computeScrollOffset()) {\n" +
			"\t\t\tint x = Scroller.getCurrX();\n" +
			"\t\t\tint y = Scroller.getCurrY();\n" +
			"\t\t\tscrollTo(x, y);\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t} else if (!isDragging && (getScrollX() < 0 || getScrollY() < 0 || getScrollY() > _YScrollRange)) { // TODO X边界还要改我\n" +
			"\t\t\tspringBack();\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\t// 输入处理\n" +
			"\t@Override\n" +
			"\tpublic boolean onCheckIsTextEditor() {\n" +
			"\t\treturn true;\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic InputConnection onCreateInputConnection(EditorInfo outAttrs) {\n" +
			"\t\toutAttrs.imeOptions = EditorInfo.IME_NULL\n" +
			"\t\t\t\t| EditorInfo.IME_FLAG_NO_ENTER_ACTION\n" +
			"\t\t\t\t| EditorInfo.IME_FLAG_NO_FULLSCREEN\n" +
			"\t\t\t\t| EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION;\n" +
			"\t\toutAttrs.inputType = EditorInfo.TYPE_MASK_CLASS\n" +
			"\t\t\t\t| EditorInfo.TYPE_CLASS_TEXT\n" +
			"\t\t\t\t| EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE\n" +
			"\t\t\t\t| EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE;\n" +
			"\t\tif (_InputConnection == null)\n" +
			"\t\t\t_InputConnection = new VInputConnection(this);\n" +
			"\t\t_isComposing = false;\n" +
			"\t\treturn _InputConnection;\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tprotected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {\n" +
			"\t\tif (gainFocus) {\n" +
			"\t\t\tshowIME();\n" +
			"\t\t} else\n" +
			"\t\t\thideIME();\n" +
			"\t\tsuper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);\n" +
			"\t}\n" +
			"\n" +
			"\t// TODO 解封我, maybe\n" +
			"\t/*@Override\n" +
			"\tpublic boolean onKeyPreIme(int keyCode, KeyEvent event) {\n" +
			"\t\tif (processEvent(event)) return true;\n" +
			"\t\treturn super.onKeyPreIme(keyCode, event);\n" +
			"\t}*/\n" +
			"\n" +
			"\n" +
			"\t// 绘制函数\n" +
			"\t@Override\n" +
			"\tprotected void onDraw(Canvas canvas) {\n" +
			"\t\tlong st = System.currentTimeMillis();\n" +
			"\t\tfinal boolean showCursor = (!isRangeSelecting()) && _Editable;\n" +
			"\t\tfinal float nh = TextHeight + _LinePaddingBottom + _LinePaddingTop;\n" +
			"\t\tfinal float bottom = getScrollY() + getHeight() + YOffset;\n" +
			"\t\tfinal int right = getScrollX() + getWidth();\n" +
			"\t\tfinal float xo = (_ShowLineNumber ? LineNumberWidth + LINENUM_SPLIT_WIDTH : 0) + _ContentLeftPadding;\n" +
			"\t\tfinal int cursorPos = getCursorPosition();\n" +
			"\n" +
			"\t\tint line = Math.max((int) (getScrollY() / nh) + 1, 1);\n" +
			"\t\tfloat y = (line - 1) * nh + YOffset + _LinePaddingTop;\n" +
			"\t\tfloat XStart, wtmp, x;\n" +
			"\t\tint i, en;\n" +
			"\t\tint tot;\n" +
			"\t\tfloat composingStartX;\n" +
			"\t\tif (_ShowLineNumber) {\n" +
			"\t\t\tColorPaint.setColor(_ColorSplitLine);\n" +
			"\t\t\tcanvas.drawRect(LineNumberWidth, getScrollY(), LineNumberWidth + LINENUM_SPLIT_WIDTH, getScrollY() + getHeight(), ColorPaint);\n" +
			"\t\t}\n" +
			"\t\tLineDraw:\n" +
			"\t\tfor (; line < E[0]; line++) {\n" +
			"\t\t\tif (_ShowLineNumber)\n" +
			"\t\t\t\tcanvas.drawText(Integer.toString(line), LineNumberWidth, y, LineNumberPaint);\n" +
			"\t\t\t// TODO HighLight 应该不是showCursor来判断吧？\n" +
			"\t\t\tif (showCursor && _CursorLine == line) {\n" +
			"\t\t\t\tColorPaint.setColor(_ColorSelectedLine);\n" +
			"\t\t\t\tcanvas.drawRect(xo - _ContentLeftPadding, y - YOffset - _LinePaddingTop, right, y + TextHeight - YOffset + _LinePaddingBottom, ColorPaint);\n" +
			"\t\t\t}\n" +
			"\t\t\ti = E[line];\n" +
			"\t\t\ten = E[line + 1] - 1;\n" +
			"\t\t\tXStart = xo;\n" +
			"\t\t\tif (getScrollX() > XStart)\n" +
			"\t\t\t\twhile ((wtmp = XStart + _CharWidths[S[i]]) < getScrollX()) {\n" +
			"\t\t\t\t\tif (++i >= en) {\n" +
			"\t\t\t\t\t\tif ((y += nh) >= bottom) break LineDraw;\n" +
			"\t\t\t\t\t\tcontinue LineDraw;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t\tXStart = wtmp;\n" +
			"\t\t\t\t}\n" +
			"\t\t\tcomposingStartX = (i >= _ComposingStart && _isComposing) ? XStart : -1;\n" +
			"\t\t\ttot = 0;\n" +
			"\t\t\tfor (x = XStart; i < en && x <= right; i++) {\n" +
			"\t\t\t\tif (composingStartX == -1 && i == _ComposingStart && _isComposing)\n" +
			"\t\t\t\t\tcomposingStartX = x;\n" +
			"\t\t\t\tif (showCursor && i == cursorPos) {\n" +
			"\t\t\t\t\tColorPaint.setColor(_ColorCursor);\n" +
			"\t\t\t\t\tColorPaint.setStrokeWidth(_CursorWidth);\n" +
			"\t\t\t\t\tcanvas.drawLine(x, y - YOffset - _LinePaddingTop, x, y - YOffset + TextHeight + _LinePaddingBottom, ColorPaint);\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tif ((TMP[tot] = S[i]) == '\\t') {\n" +
			"\t\t\t\t\tXStart += _CharWidths[CHAR_TAB];\n" +
			"\t\t\t\t\tx += _CharWidths[CHAR_TAB];\n" +
			"\t\t\t\t} else\n" +
			"\t\t\t\t\tx += _CharWidths[TMP[tot++]];\n" +
			"\t\t\t\tif (_isComposing && i == _ComposingEnd - 1 && composingStartX != -1) {\n" +
			"\t\t\t\t\tColorPaint.setColor(_ColorComposing);\n" +
			"\t\t\t\t\tColorPaint.setStrokeWidth(_ComposingWidth);\n" +
			"\t\t\t\t\tcanvas.drawLine(composingStartX, y, x, y, ColorPaint);\n" +
			"\t\t\t\t}\n" +
			"\t\t\t}\n" +
			"\t\t\tif (showCursor && i == cursorPos) {\n" +
			"\t\t\t\tColorPaint.setColor(_ColorCursor);\n" +
			"\t\t\t\tColorPaint.setStrokeWidth(_CursorWidth);\n" +
			"\t\t\t\tcanvas.drawLine(x, y - YOffset - _LinePaddingTop, x, y - YOffset + TextHeight + _LinePaddingBottom, ColorPaint);\n" +
			"\t\t\t}\n" +
			"\t\t\tcanvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);\n" +
			"\t\t\tif ((y += nh) >= bottom) break;\n" +
			"\t\t}\n" +
			"\t\tif (G.LOG_TIME) {\n" +
			"\t\t\tst = System.currentTimeMillis() - st;\n" +
			"\t\t\tLog.i(G.T, \"耗时3: \" + st);\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic String toString() {\n" +
			"\t\treturn new String(S, 0, _TextLength);\n" +
			"\t}\n" +
			"\n" +
			"\t// -------------------------\n" +
			"\t// -----Private Methods-----\n" +
			"\t// -------------------------\n" +
			"\n" +
			"\tprivate void calculateEnters() {\n" +
			"\t\tE[E[0] = 1] = 0;\n" +
			"\t\tfor (int i = 0; i < _TextLength; i++) {\n" +
			"\t\t\tif (S[i] == '\\0') continue;\n" +
			"\t\t\tif (S[i] == '\\n') {\n" +
			"\t\t\t\tif (++E[0] == E.length)\n" +
			"\t\t\t\t\texpandEArray();\n" +
			"\t\t\t\tE[E[0]] = i + 1;\n" +
			"\t\t\t}\n" +
			"\t\t}\n" +
			"\t\tE[++E[0]] = _TextLength + 1;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate boolean processEvent(KeyEvent event) {\n" +
			"\t\tif (event.getAction() != KeyEvent.ACTION_UP) return false;\n" +
			"\t\tif (event.isPrintingKey()) {\n" +
			"\t\t\tinsertChar((char) event.getUnicodeChar(event.getMetaState()));\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\t\tswitch (event.getKeyCode()) {\n" +
			"\t\t\tcase KeyEvent.KEYCODE_DEL:\n" +
			"\t\t\t\tdeleteChar();\n" +
			"\t\t\t\tbreak;\n" +
			"\t\t\tcase KeyEvent.KEYCODE_ENTER:\n" +
			"\t\t\t\tinsertChar('\\n');\n" +
			"\t\t\t\tbreak;\n" +
			"\t\t\tdefault:\n" +
			"\t\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\t\treturn true;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void expandEArray() {\n" +
			"\t\t// TODO Extract Constant\n" +
			"\t\tint[] ne = new int[E.length + 256];\n" +
			"\t\tSystem.arraycopy(E, 0, ne, 0, E.length);\n" +
			"\t\tE = ne;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate int findLine(int pos) {\n" +
			"\t\tint l = 1, r = E[0] - 1;\n" +
			"\t\tint mid;\n" +
			"\t\twhile (l <= r) {\n" +
			"\t\t\tmid = (l + r) >> 1;\n" +
			"\t\t\tif (E[mid] <= pos)\n" +
			"\t\t\t\tl = mid + 1;\n" +
			"\t\t\telse\n" +
			"\t\t\t\tr = mid - 1;\n" +
			"\t\t}\n" +
			"\t\treturn r;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void onClick(float x, float y) {\n" +
			"\t\tx -= _ContentLeftPadding;\n" +
			"\t\tif (_ShowLineNumber)\n" +
			"\t\t\tx -= (LineNumberWidth + LINENUM_SPLIT_WIDTH);\n" +
			"\t\t_CursorLine = Math.min((int) Math.ceil(y / (TextHeight + _LinePaddingTop + _LinePaddingBottom)), E[0] - 1);\n" +
			"\t\tfinal int en = E[_CursorLine + 1] - 1;\n" +
			"\t\tint ret = E[_CursorLine];\n" +
			"\t\tfor (float sum = -x; ret < en; ret++) {\n" +
			"\t\t\tif ((sum += _CharWidths[S[ret]]) >= 0) {\n" +
			"\t\t\t\tif ((-(sum - _CharWidths[S[ret]])) > sum) // 是前面的更逼近一些\n" +
			"\t\t\t\t\tret++;\n" +
			"\t\t\t\tbreak;\n" +
			"\t\t\t}\n" +
			"\t\t}\n" +
			"\t\t_CursorColumn = ret - E[_CursorLine];\n" +
			"\t\tmakeCursorVisible(_CursorLine, _CursorColumn);\n" +
			"\t\tshowIME();\n" +
			"\t\tpostInvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void onFontChange() {\n" +
			"\t\tYOffset = -ContentPaint.ascent();\n" +
			"\t\tTextHeight = ContentPaint.descent() + YOffset;\n" +
			"\t\treloadCharWidthCache();\n" +
			"\t\tonLineChange();\n" +
			"\t\trequestLayout();\n" +
			"\t\tpostInvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void onLineChange() {\n" +
			"\t\tContentHeight = (int) ((TextHeight + _LinePaddingTop + _LinePaddingBottom) * (E[0] - 1));\n" +
			"\t\t_YScrollRange = Math.max(ContentHeight - getHeight(), 0);\n" +
			"\t\tLineNumberWidth = LineNumberPaint.measureText(\"9\") * ((int) Math.log10(E[0] - 1) + 1);\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void reloadCharWidthCache() {\n" +
			"\t\tchar[] tmp = new char[] {(char) CHAR_SPACE};\n" +
			"\t\t_CharWidths[CHAR_SPACE] = ContentPaint.measureText(tmp, 0, 1);\n" +
			"\t\t_CharWidths[CHAR_TAB] = _CharWidths[CHAR_SPACE] * TABSpaceCount;\n" +
			"\t\tfor (int i = 0; i < _CharWidths.length; i++) {\n" +
			"\t\t\tif (i == CHAR_TAB || i == CHAR_SPACE) continue;\n" +
			"\t\t\ttmp[0] = (char) i;\n" +
			"\t\t\t_CharWidths[i] = ContentPaint.measureText(tmp, 0, 1);\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\tprivate void springBack() {\n" +
			"\t\tScroller.springBack(getScrollX(), getScrollY(), 0, Integer.MAX_VALUE, 0, _YScrollRange);\n" +
			"\t}\n" +
			"\n" +
			"\n" +
			"\t// --------------------------\n" +
			"\t// -----Temporary Fields-----\n" +
			"\t// --------------------------\n" +
			"\n" +
			"\t// TODO 还有512个字符都塞不满屏幕的情况！\n" +
			"\tprivate char[] TMP = new char[512];\n" +
			"\n" +
			"\n" +
			"\t// -----------------------\n" +
			"\t// -----Inner Classes-----\n" +
			"\t// -----------------------\n" +
			"\n" +
			"\tprivate static class VInputConnection implements InputConnection {\n" +
			"\t\tprivate VEdit Q;\n" +
			"\n" +
			"\t\tpublic VInputConnection(VEdit parent) {\n" +
			"\t\t\t// super(parent, true);\n" +
			"\t\t\tQ = parent;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic CharSequence getTextBeforeCursor(int n, int flags) {\n" +
			"\t\t\tint cursor = Q.getCursorPosition();\n" +
			"\t\t\tint st = Math.max(cursor - n, 0);\n" +
			"\t\t\treturn new String(Q.S, st, cursor - st);\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic CharSequence getTextAfterCursor(int n, int flags) {\n" +
			"\t\t\tint cursor = Q.getCursorPosition();\n" +
			"\t\t\treturn new String(Q.S, cursor, Math.min(cursor + n, Q._TextLength) - cursor);\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic CharSequence getSelectedText(int flags) {\n" +
			"\t\t\tif (Q._SStart == -1) return null;\n" +
			"\t\t\treturn new String(Q.S, Q._SStart, Q._SEnd - Q._SStart);\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic int getCursorCapsMode(int reqModes) {\n" +
			"\t\t\t// TODO Fix Me Maybe\n" +
			"\t\t\treturn InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean setComposingRegion(int start, int end) {\n" +
			"\t\t\tQ._ComposingStart = start;\n" +
			"\t\t\tQ._ComposingEnd = end;\n" +
			"\t\t\tQ._isComposing = true;\n" +
			"\t\t\tQ.postInvalidate();\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean setComposingText(CharSequence text, int newCursorPosition) {\n" +
			"\t\t\tchar[] cs = new char[text.length()];\n" +
			"\t\t\tfor (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);\n" +
			"\t\t\tif (newCursorPosition > 0)\n" +
			"\t\t\t\tnewCursorPosition += Q._ComposingEnd - 1;\n" +
			"\t\t\telse\n" +
			"\t\t\t\tnewCursorPosition += Q._isComposing ? Q.getCursorPosition() : Q._ComposingStart;\n" +
			"\t\t\tif (newCursorPosition < 0) newCursorPosition = 0;\n" +
			"\t\t\tif (newCursorPosition > Q._TextLength)\n" +
			"\t\t\t\tnewCursorPosition = Q._TextLength;\n" +
			"\t\t\tQ.setComposingText(cs);\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean finishComposingText() {\n" +
			"\t\t\tQ.finishComposing();\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean commitText(CharSequence text, int newCursorPosition) {\n" +
			"\t\t\tchar[] cs = new char[text.length()];\n" +
			"\t\t\tfor (int i = 0; i < cs.length; i++) cs[i] = text.charAt(i);\n" +
			"\t\t\tif (Q.isComposing()) {\n" +
			"\t\t\t\tQ.setComposingText(cs);\n" +
			"\t\t\t\tif (newCursorPosition > 1)\n" +
			"\t\t\t\t\tQ.moveCursor(Q.getCursorPosition() + newCursorPosition - 1);\n" +
			"\t\t\t\telse if (newCursorPosition <= 0)\n" +
			"\t\t\t\t\tQ.moveCursor(Q.getCursorPosition() - text.length() - newCursorPosition);\n" +
			"\t\t\t\tQ.finishComposing();\n" +
			"\t\t\t} else\n" +
			"\t\t\t\tQ.insertChars(cs);\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean sendKeyEvent(KeyEvent event) {\n" +
			"\t\t\tQ.processEvent(event);\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean setSelection(int start, int end) {\n" +
			"\t\t\tQ._SStart = start;\n" +
			"\t\t\tQ._SEnd = end;\n" +
			"\t\t\tQ.postInvalidate();\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {\n" +
			"\t\t\t// TODO Tough\n" +
			"\t\t\treturn null;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean deleteSurroundingText(int beforeLength, int afterLength) {\n" +
			"\t\t\tint pos = Q.getCursorPosition() + afterLength;\n" +
			"\t\t\tint line = Q.findLine(pos);\n" +
			"\t\t\tint[] ret = Q.deleteChars(line, pos - Q.E[line], afterLength + beforeLength);\n" +
			"\t\t\tQ.moveCursor(ret[0], ret[1]);\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {\n" +
			"\t\t\t// TODO Ha?\n" +
			"\t\t\tdeleteSurroundingText(beforeLength, afterLength);\n" +
			"\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean commitCompletion(CompletionInfo text) {\n" +
			"\t\t\t// TODO Ha?\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean commitCorrection(CorrectionInfo correctionInfo) {\n" +
			"\t\t\t// TODO Ha?\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean performEditorAction(int editorAction) {\n" +
			"\t\t\t// TODO Ha?\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean performContextMenuAction(int id) {\n" +
			"\t\t\t// TODO ???\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean beginBatchEdit() {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean endBatchEdit() {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean clearMetaKeyStates(int states) {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean reportFullscreenMode(boolean enabled) {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean performPrivateCommand(String action, Bundle data) {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean requestCursorUpdates(int cursorUpdateMode) {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic Handler getHandler() {\n" +
			"\t\t\treturn null;\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic void closeConnection() {\n" +
			"\n" +
			"\t\t}\n" +
			"\n" +
			"\t\t@Override\n" +
			"\t\tpublic boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {\n" +
			"\t\t\treturn false;\n" +
			"\t\t}\n" +
			"\t}\n" +
			"}";
}
