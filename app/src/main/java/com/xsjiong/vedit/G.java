package com.xsjiong.vedit;

public interface G {
	boolean LOG_TIME = true;

	String D = "package com.xsjiong.vedit;\n" +
			"\n" +
			"import android.content.Context;\n" +
			"import android.graphics.Canvas;\n" +
			"import android.graphics.Color;\n" +
			"import android.graphics.Paint;\n" +
			"import android.graphics.Typeface;\n" +
			"import android.text.TextPaint;\n" +
			"import android.util.AttributeSet;\n" +
			"import android.util.Log;\n" +
			"import android.view.MotionEvent;\n" +
			"import android.view.VelocityTracker;\n" +
			"import android.view.View;\n" +
			"import android.view.ViewConfiguration;\n" +
			"import android.widget.OverScroller;\n" +
			"\n" +
			"public class VEdit extends View {\n" +
			"\tpublic static final int MEASURE_STEP = 50;\n" +
			"\n" +
			"\tprivate String S;\n" +
			"\tprivate TextPaint ContentPaint;\n" +
			"\tprivate Paint ViewPaint;\n" +
			"\tprivate float TextHeight;\n" +
			"\tprivate float YPadding;\n" +
			"\tprivate int[] Enters = new int[257];\n" +
			"\n" +
			"\tprivate int _minFling, _touchSlop;\n" +
			"\tprivate float YOffset;\n" +
			"\t// 这是个负数！这是个负数！！这是个负数！！！\n" +
			"\tprivate int ContentHeight;\n" +
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
			"\t\tContentPaint = new TextPaint();\n" +
			"\t\tContentPaint.setAntiAlias(true);\n" +
			"\t\tContentPaint.setTextSize(50);\n" +
			"\t\tContentPaint.setTextAlign(Paint.Align.LEFT);\n" +
			"\t\tContentPaint.setColor(Color.BLACK);\n" +
			"\t\tViewPaint = new Paint();\n" +
			"\t\tViewPaint.setStyle(Paint.Style.FILL);\n" +
			"\t\tViewPaint.setColor(Color.WHITE);\n" +
			"\t\tsetFocusable(true);\n" +
			"\t\tsetFocusableInTouchMode(true);\n" +
			"\t\t_updateFontMetrics();\n" +
			"\t\tpostInvalidate();\n" +
			"\t\tViewConfiguration config = ViewConfiguration.get(cx);\n" +
			"\t\t_minFling = config.getScaledMinimumFlingVelocity();\n" +
			"\t\t_touchSlop = config.getScaledTouchSlop();\n" +
			"\t\tSpeedCalc = VelocityTracker.obtain();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTypeface(Typeface tf) {\n" +
			"\t\tContentPaint.setTypeface(tf);\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tprivate float TABReplaceWidth;\n" +
			"\n" +
			"\tprivate void _updateFontMetrics() {\n" +
			"\t\tPaint.FontMetrics m = ContentPaint.getFontMetrics();\n" +
			"\t\tTextHeight = m.descent - m.ascent;\n" +
			"\t\tYOffset = -m.ascent;\n" +
			"\t\tTABReplaceWidth = ContentPaint.measureText(TABReplace, 0, TABReplace.length);\n" +
			"\t\t// 实则是预留了5行！\n" +
			"\t\tContentHeight = (int) -(TextHeight * (Enters[0] + 4));\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextAntiAlias(boolean flag) {\n" +
			"\t\tContentPaint.setAntiAlias(flag);\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextSize(int size) {\n" +
			"\t\tContentPaint.setTextSize(size);\n" +
			"\t\t_updateFontMetrics();\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic TextPaint getContentPaint() {\n" +
			"\t\treturn ContentPaint;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTextColor(int color) {\n" +
			"\t\tContentPaint.setColor(color);\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setText(String s) {\n" +
			"\t\tthis.S = s;\n" +
			"\t\tEnters[Enters[0] = 1] = 0;\n" +
			"\t\tfor (int i = 0; i < s.length(); i++)\n" +
			"\t\t\tif (s.charAt(i) == '\\n') {\n" +
			"\t\t\t\tif (++Enters[0] == Enters.length) {\n" +
			"\t\t\t\t\tint[] newEnters = new int[Enters.length + 256];\n" +
			"\t\t\t\t\tSystem.arraycopy(Enters, 0, newEnters, 0, Enters.length);\n" +
			"\t\t\t\t\tEnters = newEnters;\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tEnters[Enters[0]] = i + 1;\n" +
			"\t\t\t}\n" +
			"\t\t/*if (Enters[Enters[0]] != s.length()) */\n" +
			"\t\tEnters[++Enters[0]] = s.length() + 1;\n" +
			"\t\tContentHeight = (int) -(TextHeight * (Enters[0] + 4));\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tprivate float XPadding = 0;\n" +
			"\tprivate char[] TABReplace = new char[] {' ', ' ', ' ', ' '};\n" +
			"\n" +
			"\tpublic void setTABReplace(String replace) {\n" +
			"\t\tsetTABReplace(replace.toCharArray());\n" +
			"\t}\n" +
			"\n" +
			"\tpublic void setTABReplace(char[] replace) {\n" +
			"\t\tTABReplace = replace;\n" +
			"\t\t_updateFontMetrics();\n" +
			"\t\tinvalidate();\n" +
			"\t}\n" +
			"\n" +
			"\tpublic int getLineNumber() {\n" +
			"\t\treturn Enters[0] - 1;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic char[] getTABReplace() {\n" +
			"\t\treturn TABReplace;\n" +
			"\t}\n" +
			"\n" +
			"\tprivate float _lastX, _lastY;\n" +
			"\tprivate OverScroller Scroller;\n" +
			"\tprivate VelocityTracker SpeedCalc;\n" +
			"\tprivate boolean isDragging;\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic boolean onTouchEvent(MotionEvent event) {\n" +
			"\t\tSpeedCalc.addMovement(event);\n" +
			"\t\tswitch (event.getActionMasked()) {\n" +
			"\t\t\tcase MotionEvent.ACTION_DOWN:\n" +
			"\t\t\t\t_lastX = event.getX();\n" +
			"\t\t\t\t_lastY = event.getY();\n" +
			"\t\t\t\tif (!Scroller.isFinished())\n" +
			"\t\t\t\t\tScroller.abortAnimation();\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t\tcase MotionEvent.ACTION_MOVE:\n" +
			"\t\t\t\tfloat deltaX = event.getX() - _lastX;\n" +
			"\t\t\t\tfloat deltaY = event.getY() - _lastY;\n" +
			"\t\t\t\tif (!isDragging) {\n" +
			"\t\t\t\t\tboolean xll = deltaX < 0;\n" +
			"\t\t\t\t\tboolean yll = deltaY < 0;\n" +
			"\t\t\t\t\tif (xll) deltaX = -deltaX;\n" +
			"\t\t\t\t\tif (yll) deltaY = -deltaY;\n" +
			"\t\t\t\t\tif (deltaX > _touchSlop) {\n" +
			"\t\t\t\t\t\tdeltaX -= _touchSlop;\n" +
			"\t\t\t\t\t\tisDragging = true;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t\tif (deltaY > _touchSlop) {\n" +
			"\t\t\t\t\t\tdeltaY -= _touchSlop;\n" +
			"\t\t\t\t\t\tisDragging = true;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t\tif (isDragging) {\n" +
			"\t\t\t\t\t\tif (xll) deltaX = -deltaX;\n" +
			"\t\t\t\t\t\tif (yll) deltaY = -deltaY;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tif (isDragging) {\n" +
			"\t\t\t\t\t//XPadding += deltaX;\n" +
			"\t\t\t\t\tYPadding += deltaY;\n" +
			"\t\t\t\t\t_lastX = event.getX();\n" +
			"\t\t\t\t\t_lastY = event.getY();\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tinvalidate();\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t\tcase MotionEvent.ACTION_CANCEL:\n" +
			"\t\t\tcase MotionEvent.ACTION_UP:\n" +
			"\t\t\t\tisDragging = false;\n" +
			"\t\t\t\tSpeedCalc.computeCurrentVelocity(1000);\n" +
			"\t\t\t\tint speedX = (int) SpeedCalc.getXVelocity();\n" +
			"\t\t\t\tint speedY = (int) SpeedCalc.getYVelocity();\n" +
			"\t\t\t\tif (Math.abs(speedX) <= _minFling) speedX = 0;\n" +
			"\t\t\t\tif (Math.abs(speedY) <= _minFling) speedY = 0;\n" +
			"\t\t\t\tif (speedX != 0 || speedY != 0) {\n" +
			"\t\t\t\t\tScroller.fling((int) XPadding, (int) YPadding, 0, speedY, Integer.MIN_VALUE, 0, ContentHeight + getHeight(), 0);\n" +
			"\t\t\t\t\tinvalidate();\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tSpeedCalc.clear();\n" +
			"\t\t\t\treturn true;\n" +
			"\t\t}\n" +
			"\t\treturn super.onTouchEvent(event);\n" +
			"\t}\n" +
			"\n" +
			"\t@Override\n" +
			"\tpublic void computeScroll() {\n" +
			"\t\tif (Scroller.computeScrollOffset()) {\n" +
			"\t\t\tXPadding = Scroller.getCurrX();\n" +
			"\t\t\tYPadding = Scroller.getCurrY();\n" +
			"\t\t\tpostInvalidate();\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\n" +
			"\t// TODO 我就不信！！还有100个字符都塞不满屏幕的情况！！\n" +
			"\t// TODO 好吧确实有，望修复\n" +
			"\tprivate char[] TMP = new char[256];\n" +
			"\n" +
			"\t@Override\n" +
			"\tprotected void onDraw(Canvas canvas) {\n" +
			"\t\tlong st = System.currentTimeMillis();\n" +
			"\t\tfloat x, y = -YPadding / TextHeight;\n" +
			"\t\tint StartLine = (int) y;\n" +
			"\t\ty = YPadding + StartLine * TextHeight + YOffset;\n" +
			"\t\tStartLine++;\n" +
			"\t\tint width = canvas.getWidth(), height = canvas.getHeight();\n" +
			"\t\tint en;\n" +
			"\t\tint tot;\n" +
			"\t\tfloat wtmp;\n" +
			"\t\tfloat XStart;\n" +
			"\t\tif (StartLine > 0)\n" +
			"\t\t\tLineDraw:for (int line = StartLine, i; line < Enters[0]; line++) {\n" +
			"\t\t\t\tXStart = XPadding;\n" +
			"\t\t\t\ten = Enters[line + 1];\n" +
			"\t\t\t\ti = Enters[line];\n" +
			"\t\t\t\tif (i == en) {\n" +
			"\t\t\t\t\tif ((y += TextHeight) > height) break;\n" +
			"\t\t\t\t\tcontinue;\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\ten--;\n" +
			"\t\t\t\tif (XStart < 0)\n" +
			"\t\t\t\t\t// TODO 这里需要判断TAB额外变出的长度\n" +
			"\t\t\t\t\twhile (true) {\n" +
			"\t\t\t\t\t\tif ((wtmp = (XStart + ContentPaint.measureText(S, i, Math.min(en, i + MEASURE_STEP)))) >= 0)\n" +
			"\t\t\t\t\t\t\tbreak;\n" +
			"\t\t\t\t\t\tif ((i += MEASURE_STEP) >= en) {\n" +
			"\t\t\t\t\t\t\tif ((y += TextHeight) > height) break LineDraw;\n" +
			"\t\t\t\t\t\t\tcontinue LineDraw;\n" +
			"\t\t\t\t\t\t}\n" +
			"\t\t\t\t\t\tXStart = wtmp;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\ttot = 0;\n" +
			"\t\t\t\tfor (x = XStart; i < en && x <= width; i++) {\n" +
			"\t\t\t\t\tif ((TMP[tot] = S.charAt(i)) == '\\t') {\n" +
			"\t\t\t\t\t\tXStart += TABReplaceWidth;\n" +
			"\t\t\t\t\t\tx += TABReplaceWidth;\n" +
			"\t\t\t\t\t} else if ((wtmp = ContentPaint.measureText(TMP, tot, 1)) != 0) {\n" +
			"\t\t\t\t\t\ttot++;\n" +
			"\t\t\t\t\t\tx += wtmp;\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tcanvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);\n" +
			"\t\t\t\t/*tot = i;\n" +
			"\t\t\t\tfor (x = XStart; i < en && x <= width; i++) {\n" +
			"\t\t\t\t\tif (S.charAt(i) == '\\t') {\n" +
			"\t\t\t\t\t\tif (tot != i)\n" +
			"\t\t\t\t\t\t\tcanvas.drawText(S, tot, i, XStart, y, ContentPaint);\n" +
			"\t\t\t\t\t\ttot = i + 1;\n" +
			"\t\t\t\t\t\tXStart += TABReplaceWidth;\n" +
			"\t\t\t\t\t\tx += TABReplaceWidth;\n" +
			"\t\t\t\t\t} else if ((wtmp = ContentPaint.measureText(S, i, i + 1)) != 0)\n" +
			"\t\t\t\t\t\tx += wtmp;\n" +
			"\t\t\t\t}\n" +
			"\t\t\t\tif (tot != en)\n" +
			"\t\t\t\t\tcanvas.drawText(S, tot, i, XStart, y, ContentPaint);*/\n" +
			"\t\t\t\t/*i = Enters[line] + MEASURE_STEP;\n" +
			"\t\t\t\tif (i <= en) {\n" +
			"\t\t\t\t\tfor (x = XStart; x <= width; i = Math.min(i + MEASURE_STEP, en)) {\n" +
			"\t\t\t\t\t\tcanvas.drawText(S, i - MEASURE_STEP, i, x, y, ContentPaint);\n" +
			"\t\t\t\t\t\tx += ContentPaint.measureText(S, i - MEASURE_STEP, i);\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t} else canvas.drawText(S, Enters[line], en, XStart, y, ContentPaint);*/\n" +
			"\t\t\t\t//canvas.drawText(S, i, en, XStart, y, ContentPaint);\n" +
			"\t\t\t\tif ((y += TextHeight) > height) break;\n" +
			"\t\t\t}\n" +
			"\t\tst = System.currentTimeMillis() - st;\n" +
			"\t\tLog.i(\"VEdit\", \"耗时: \" + st);\n" +
			"\t}\n" +
			"}";
}
