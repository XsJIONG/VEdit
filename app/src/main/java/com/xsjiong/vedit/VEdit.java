package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.OverScroller;

public class VEdit extends SurfaceView implements SurfaceHolder.Callback {
	private String S;
	private TextPaint ContentPaint;
	private Paint ViewPaint;
	private SurfaceHolder H;
	private float TextHeight;
	private float YStart;
	private int[] Enters = new int[257];
	private int _minFling;

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
		ContentPaint.setTextAlign(Paint.Align.LEFT);
		ContentPaint.setColor(Color.BLACK);
		ContentPaint.setTypeface(Typeface.MONOSPACE);
		ViewPaint = new Paint();
		ViewPaint.setStyle(Paint.Style.FILL);
		ViewPaint.setColor(Color.WHITE);
		H = getHolder();
		H.addCallback(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		_updateFontMetrics();
		postInvalidate();
		ViewConfiguration config = ViewConfiguration.get(cx);
		_minFling = config.getScaledMinimumFlingVelocity();
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
		YStart = -m.ascent;
		TABReplaceWidth = ContentPaint.measureText(TABReplace, 0, TABReplace.length);
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
		Enters[++Enters[0]] = s.length();
		invalidate();
	}

	private boolean _Drawing = false;

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_Drawing = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		_Drawing = false;
	}

	public void draw() {
		if (!_Drawing) return;
		Canvas C = null;
		try {
			C = H.lockCanvas();
			_draw(C);
			H.unlockCanvasAndPost(C);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		draw();
	}

	private Drawable BK;

	@Override
	public void setBackground(Drawable background) {
		BK = background;
		invalidate();
	}

	@Override
	public void setBackgroundDrawable(Drawable background) {
		BK = background;
		invalidate();
	}

	@Override
	public Drawable getBackground() {
		return BK;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (BK != null) BK.setBounds(left, top, right, bottom);
	}

	private int StartLine = 1;
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

	public char[] getTABReplace() {
		return TABReplace;
	}

	public void setStartLine(int st) {
		StartLine = st;
		invalidate();
	}

	private float _touchStartX;
	private OverScroller Scroller;
	private VelocityTracker SpeedCalc;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		SpeedCalc.addMovement(event);
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_touchStartX = XPadding - event.getX();
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
				return true;
			case MotionEvent.ACTION_MOVE:
				XPadding = _touchStartX + event.getX();
				draw();
				return true;
			case MotionEvent.ACTION_UP:
				XPadding = _touchStartX + event.getX();
				SpeedCalc.computeCurrentVelocity(1000);
				int speedX = (int) SpeedCalc.getXVelocity();
				// TODO speedY
				if (Math.abs(speedX) > _minFling) {
					Scroller.fling((int) XPadding, 0, speedX, -0, Integer.MIN_VALUE, 0, 0, 0);
					invalidate();
				}
				SpeedCalc.clear();
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		if (Scroller.computeScrollOffset()) {
			XPadding = Scroller.getCurrX();
			postInvalidate();
		}
	}

	private void _draw(Canvas canvas) {
		long st = System.currentTimeMillis();
		if (BK != null) BK.draw(canvas);
		float x, y = YStart;
		final int step = 50;
		int width = canvas.getWidth(), height = canvas.getHeight();
		int en;
		// TODO 我就不信！！还有100个字符都塞不满屏幕的情况！！
		// TODO 好吧确实有，望修复
		char[] tmp = new char[100];
		int tot;
		for (int line = StartLine, i; line < Enters[0]; line++) {
			x = XPadding;
			en = Enters[line + 1] - 1;
			tot = 0;
			for (i = Enters[line]; i <= en && x <= width; i++) {
				if ((tmp[tot] = S.charAt(i)) == '\t') {
					System.arraycopy(TABReplace, 0, tmp, tot, TABReplace.length);
					tot += TABReplace.length;
					x += TABReplaceWidth;
				} else
					x += ContentPaint.measureText(tmp, tot++, 1);
			}
			canvas.drawText(tmp, 0, tot, XPadding, y, ContentPaint);
			/*i = Enters[line] + step;
			if (i <= en) {
				for (; x <= width; i = Math.min(i + step, en)) {
					canvas.drawText(S, i - step, i, x, y, ContentPaint);
					x += ContentPaint.measureText(S, i - step, i);
				}
			} else canvas.drawText(S, Enters[line], en, x, y, ContentPaint);*/
			if ((y += TextHeight) > height) break;
		}
		//new StaticLayout(S, ContentPaint, canvas.getWidth(), Layout.Alignment.ALIGN_LEFT, 1, 0, true).draw(canvas);
		st = System.currentTimeMillis() - st;
		Log.i("VEdit", "耗时: " + st);
	}
}