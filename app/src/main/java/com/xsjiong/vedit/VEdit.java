package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.lang.ref.SoftReference;

public class VEdit extends View {
	public static final int MEASURE_STEP = 50;

	private String S;
	private TextPaint ContentPaint;
	private Paint ViewPaint;
	private float TextHeight;
	private float YPadding;
	private int[] Enters = new int[257];

	private int _minFling, _touchSlop;
	private float YOffset;
	// 这是个负数！这是个负数！！这是个负数！！！
	private int ContentHeight;

	public VEdit(Context cx) {
		this(cx, null, 0);
	}

	public VEdit(Context cx, AttributeSet attr) {
		this(cx, attr, 0);
	}

	public VEdit(Context cx, AttributeSet attr, int style) {
		super(cx, attr, style);
		Scroller = new OverScroller(getContext());
		ContentPaint = new TextPaint();
		ContentPaint.setAntiAlias(true);
		ContentPaint.setTextSize(50);
		ContentPaint.setColor(Color.BLACK);
		ViewPaint = new Paint();
		ViewPaint.setStyle(Paint.Style.FILL);
		ViewPaint.setColor(Color.WHITE);
		_updateFontMetrics();
		ViewConfiguration config = ViewConfiguration.get(cx);
		_minFling = config.getScaledMinimumFlingVelocity();
		_touchSlop = config.getScaledTouchSlop();
		SpeedCalc = VelocityTracker.obtain();
	}

	public void setTypeface(Typeface tf) {
		ContentPaint.setTypeface(tf);
		invalidate();
	}

	private float TABReplaceWidth;

	private void _updateFontMetrics() {
		Paint.FontMetrics m = ContentPaint.getFontMetrics();
		TextHeight = m.descent - m.ascent;
		YOffset = -m.ascent;
		TABReplaceWidth = ContentPaint.measureText(TABReplace, 0, TABReplace.length);
		ContentHeight = (int) -(TextHeight * (Enters[0] - 1));
	}

	public void setTextAntiAlias(boolean flag) {
		ContentPaint.setAntiAlias(flag);
		invalidate();
	}

	public void setTextSize(int size) {
		ContentPaint.setTextSize(size);
		_updateFontMetrics();
		invalidate();
	}

	public TextPaint getContentPaint() {
		return ContentPaint;
	}

	public void setTextColor(int color) {
		ContentPaint.setColor(color);
		invalidate();
	}

	public void setText(String s) {
		this.S = s;
		Enters[Enters[0] = 1] = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\n') {
				if (++Enters[0] == Enters.length) {
					int[] newEnters = new int[Enters.length + 256];
					System.arraycopy(Enters, 0, newEnters, 0, Enters.length);
					Enters = newEnters;
				}
				Enters[Enters[0]] = i + 1;
			}
		/*if (Enters[Enters[0]] != s.length()) */
		Enters[++Enters[0]] = s.length() + 1;
		LINE_IMAGES = new SoftReference[Enters[0]];
		ContentHeight = (int) -(TextHeight * (Enters[0] - 1));
		invalidate();
	}

	private float XPadding = 0;
	private char[] TABReplace = new char[] {' ', ' ', ' ', ' '};

	public void setTABReplace(String replace) {
		setTABReplace(replace.toCharArray());
	}

	public void setTABReplace(char[] replace) {
		TABReplace = replace;
		_updateFontMetrics();
		invalidate();
	}

	public int getLineNumber() {
		return Enters[0] - 1;
	}

	public char[] getTABReplace() {
		return TABReplace;
	}

	private float _lastX, _lastY;
	private OverScroller Scroller;
	private VelocityTracker SpeedCalc;
	private boolean isDragging;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		SpeedCalc.addMovement(event);
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_lastX = event.getX();
				_lastY = event.getY();
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
				return true;
			case MotionEvent.ACTION_MOVE:
				float deltaX = event.getX() - _lastX;
				float deltaY = event.getY() - _lastY;
				if (!isDragging) {
					boolean xll = deltaX < 0;
					boolean yll = deltaY < 0;
					if (xll) deltaX = -deltaX;
					if (yll) deltaY = -deltaY;
					if (deltaX > _touchSlop) {
						deltaX -= _touchSlop;
						isDragging = true;
					}
					if (deltaY > _touchSlop) {
						deltaY -= _touchSlop;
						isDragging = true;
					}
					if (isDragging) {
						if (xll) deltaX = -deltaX;
						if (yll) deltaY = -deltaY;
					}
				}
				if (isDragging) {
					//XPadding += deltaX;
					YPadding += deltaY;
					_lastX = event.getX();
					_lastY = event.getY();
				}
				invalidate();
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				isDragging = false;
				SpeedCalc.computeCurrentVelocity(1000);
				int speedX = (int) SpeedCalc.getXVelocity();
				int speedY = (int) SpeedCalc.getYVelocity();
				if (Math.abs(speedX) <= _minFling) speedX = 0;
				if (Math.abs(speedY) <= _minFling) speedY = 0;
				if (speedX != 0 || speedY != 0) {
					Scroller.fling((int) XPadding, (int) YPadding, 0, speedY, Integer.MIN_VALUE, 0, ContentHeight + getHeight(), 0);
					invalidate();
				}
				SpeedCalc.clear();
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), -ContentHeight);
	}

	@Override
	public void computeScroll() {
		if (Scroller.computeScrollOffset()) {
			XPadding = Scroller.getCurrX();
			YPadding = Scroller.getCurrY();
			postInvalidate();
		}
	}

	// TODO 我就不信！！还有100个字符都塞不满屏幕的情况！！
	// TODO 好吧确实有，望修复
	private char[] TMP = new char[256];

	private SoftReference<Bitmap>[] LINE_IMAGES;

	@Override
	protected void onDraw(Canvas canvas) {
		long st = System.currentTimeMillis();
		float x, y = -YPadding / TextHeight;
		int StartLine = (int) y;
		y = YPadding + StartLine * TextHeight + YOffset;
		StartLine++;
		int width = canvas.getWidth(), height = canvas.getHeight();
		int en;
		int tot;
		float wtmp;
		float XStart;
		if (StartLine > 0)
			LineDraw:for (int line = StartLine, i; line < Enters[0]; line++) {
				XStart = XPadding;
				en = Enters[line + 1];
				i = Enters[line];
				if (i == en) {
					if ((y += TextHeight) > height) break;
					continue;
				}
				en--;
				/*if (XStart < 0)
					// TODO 这里需要判断TAB额外变出的长度
					while (true) {
						if ((wtmp = (XStart + ContentPaint.measureText(S, i, Math.min(en, i + MEASURE_STEP)))) >= 0)
							break;
						if ((i += MEASURE_STEP) >= en) {
							if ((y += TextHeight) > height) break LineDraw;
							continue LineDraw;
						}
						XStart = wtmp;
					}
				tot = 0;
				for (x = XStart; i < en && x <= width; i++) {
					if ((TMP[tot] = S.charAt(i)) == '\t') {
						XStart += TABReplaceWidth;
						x += TABReplaceWidth;
					} else if ((wtmp = ContentPaint.measureText(TMP, tot, 1)) != 0) {
						tot++;
						x += wtmp;
					}
				}
				canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);*/
				/*tot = i;
				for (x = XStart; i < en && x <= width; i++) {
					if (S.charAt(i) == '\t') {
						if (tot != i)
							canvas.drawText(S, tot, i, XStart, y, ContentPaint);
						tot = i + 1;
						XStart += TABReplaceWidth;
						x += TABReplaceWidth;
					} else if ((wtmp = ContentPaint.measureText(S, i, i + 1)) != 0)
						x += wtmp;
				}
				if (tot != en)
					canvas.drawText(S, tot, i, XStart, y, ContentPaint);*/
				/*i = Enters[line] + MEASURE_STEP;
				if (i <= en) {
					for (x = XStart; x <= width; i = Math.min(i + MEASURE_STEP, en)) {
						canvas.drawText(S, i - MEASURE_STEP, i, x, y, ContentPaint);
						x += ContentPaint.measureText(S, i - MEASURE_STEP, i);
					}
				} else canvas.drawText(S, Enters[line], en, XStart, y, ContentPaint);*/
//				canvas.drawText(S, i, en, XStart, y, ContentPaint);
				Bitmap tmp = null;
				if (LINE_IMAGES[line] != null)
					tmp = LINE_IMAGES[line].get();
				if (tmp == null) {
					tmp = Bitmap.createBitmap((int) ContentPaint.measureText(S, i, en) + 1, (int) TextHeight + 1, Bitmap.Config.ARGB_4444);
					new Canvas(tmp).drawText(S, i, en, 0, YOffset, ContentPaint);
					LINE_IMAGES[line] = new SoftReference<>(tmp);
				}
				/*Bitmap tmp = LINE_IMAGES[line];
				if (tmp == null) {
					tmp = Bitmap.createBitmap((int) ContentPaint.measureText(S, i, en) + 1, (int) TextHeight + 1, Bitmap.Config.ARGB_4444);
					new Canvas(tmp).drawText(S, i, en, 0, YOffset, ContentPaint);
					LINE_IMAGES[line] = tmp;
				}*/
				canvas.drawBitmap(tmp, 0, y, ContentPaint);
				if ((y += TextHeight) > height) break;
			}
		st = System.currentTimeMillis() - st;
		Log.i("VEdit", "耗时1: " + st);
	}
}