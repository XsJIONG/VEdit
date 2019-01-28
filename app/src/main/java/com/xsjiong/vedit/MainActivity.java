package com.xsjiong.vedit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	private VEdit V;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		V = new VEdit(this);
		V.setBackgroundColor(Color.WHITE);
		V.setText("package com.xsjiong.vedit;\n" +
				"\n" +
				"import android.content.Context;\n" +
				"import android.graphics.Canvas;\n" +
				"import android.graphics.Color;\n" +
				"import android.graphics.Paint;\n" +
				"import android.graphics.Typeface;\n" +
				"import android.graphics.drawable.Drawable;\n" +
				"import android.text.TextPaint;\n" +
				"import android.util.AttributeSet;\n" +
				"import android.util.Log;\n" +
				"import android.view.SurfaceHolder;\n" +
				"import android.view.SurfaceView;\n" +
				"\n" +
				"public class VEdit extends SurfaceView implements SurfaceHolder.Callback {\n" +
				"\tprivate String S;\n" +
				"\tprivate TextPaint ContentPaint;\n" +
				"\tprivate Paint ViewPaint;\n" +
				"\tprivate SurfaceHolder H;\n" +
				"\tprivate float TextHeight;\n" +
				"\tprivate float YStart;\n" +
				"\tprivate int[] Enters = new int[257];\n" +
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
				"\t\tContentPaint = new TextPaint();\n" +
				"\t\tContentPaint.setAntiAlias(true);\n" +
				"\t\tContentPaint.setTextSize(50);\n" +
				"\t\tContentPaint.setTextAlign(Paint.Align.LEFT);\n" +
				"\t\tContentPaint.setColor(Color.BLACK);\n" +
				"\t\tContentPaint.setTypeface(Typeface.MONOSPACE);\n" +
				"\t\tViewPaint = new Paint();\n" +
				"\t\tViewPaint.setStyle(Paint.Style.FILL);\n" +
				"\t\tViewPaint.setColor(Color.WHITE);\n" +
				"\t\tH = getHolder();\n" +
				"\t\tH.addCallback(this);\n" +
				"\t\tsetFocusable(true);\n" +
				"\t\tsetFocusableInTouchMode(true);\n" +
				"\t\t_updateFontMetrics();\n" +
				"\t\tpostInvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\tpublic void setTypeface(Typeface tf) {\n" +
				"\t\tContentPaint.setTypeface(tf);\n" +
				"\t\tinvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\tprivate void _updateFontMetrics() {\n" +
				"\t\tPaint.FontMetrics m = ContentPaint.getFontMetrics();\n" +
				"\t\tTextHeight = m.descent - m.ascent;\n" +
				"\t\tYStart = -m.ascent;\n" +
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
				"\t\tEnters[++Enters[0]] = s.length();\n" +
				"\t\tinvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\tprivate boolean _Drawing = false;\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void surfaceCreated(SurfaceHolder holder) {\n" +
				"\t\t_Drawing = true;\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void surfaceDestroyed(SurfaceHolder holder) {\n" +
				"\t\t_Drawing = false;\n" +
				"\t}\n" +
				"\n" +
				"\tpublic void draw() {\n" +
				"\t\tif (!_Drawing) return;\n" +
				"\t\tCanvas C = null;\n" +
				"\t\ttry {\n" +
				"\t\t\tC = H.lockCanvas();\n" +
				"\t\t\t_draw(C);\n" +
				"\t\t\tH.unlockCanvasAndPost(C);\n" +
				"\t\t} catch (Throwable t) {\n" +
				"\t\t\tt.printStackTrace();\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void invalidate() {\n" +
				"\t\tsuper.invalidate();\n" +
				"\t\tdraw();\n" +
				"\t}\n" +
				"\n" +
				"\tprivate Drawable BK;\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void setBackground(Drawable background) {\n" +
				"\t\tBK = background;\n" +
				"\t\tinvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic void setBackgroundDrawable(Drawable background) {\n" +
				"\t\tBK = background;\n" +
				"\t\tinvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic Drawable getBackground() {\n" +
				"\t\treturn BK;\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tprotected void onLayout(boolean changed, int left, int top, int right, int bottom) {\n" +
				"\t\tsuper.onLayout(changed, left, top, right, bottom);\n" +
				"\t\tif (BK != null) BK.setBounds(left, top, right, bottom);\n" +
				"\t}\n" +
				"\n" +
				"\tprivate int StartLine = 1;\n" +
				"\tprivate int XPadding = 0;\n" +
				"\n" +
				"\tpublic void setStartLine(int st) {\n" +
				"\t\tStartLine = st;\n" +
				"\t\tinvalidate();\n" +
				"\t}\n" +
				"\n" +
				"\tprivate void _draw(Canvas canvas) {\n" +
				"\t\tlong st = System.currentTimeMillis();\n" +
				"\t\tif (BK != null) BK.draw(canvas);\n" +
				"\t\tfloat x, y = YStart;\n" +
				"\t\tfinal int step = 50;\n" +
				"\t\tint width = canvas.getWidth();\n" +
				"\t\tint en;\n" +
				"\t\tfor (int line = StartLine, i; line < Enters[0]; line++) {\n" +
				"\t\t\tx = XPadding;\n" +
				"\t\t\ten = Enters[line + 1] - 1;\n" +
				"\t\t\ti = Enters[line] + step;\n" +
				"\t\t\tif (i <= en) {\n" +
				"\t\t\t\tfor (; x <= width; i = Math.min(i + step, en)) {\n" +
				"\t\t\t\t\tcanvas.drawText(S, i - step, i, x, y, ContentPaint);\n" +
				"\t\t\t\t\tx += ContentPaint.measureText(S, i - step, i);\n" +
				"\t\t\t\t}\n" +
				"\t\t\t} else canvas.drawText(S, Enters[line], en, x, y, ContentPaint);\n" +
				"\t\t\ty += TextHeight;\n" +
				"\t\t}\n" +
				"\t\t//new StaticLayout(S, ContentPaint, canvas.getWidth(), Layout.Alignment.ALIGN_LEFT, 1, 0, true).draw(canvas);\n" +
				"\t\tst = System.currentTimeMillis() - st;\n" +
				"\t\tLog.i(\"VEdit\", \"耗时: \" + st);\n" +
				"\t}\n" +
				"}");
		V.setFitsSystemWindows(true);
		setContentView(V);
	}
}
