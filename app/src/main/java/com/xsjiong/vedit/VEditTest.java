package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

public class VEditTest extends View {
	private TextPaint ContentPaint;
	private float YOffset;
	private float TextHeight;
	private int ContentHeight;
	private char[] S;
	private int[] E = new int[257];
	private int _minFling, _touchSlop;
	private float _lastX, _lastY, _stX, _stY;
	private OverScroller Scroller;
	private VelocityTracker SpeedCalc;
	private boolean isDragging;

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
		setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
		ContentPaint.setColor(Color.BLACK);
	}

	public void setTextSize(int unit, float size) {
		setTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));
	}

	public void setTextSize(float size) {
		ContentPaint.setTextSize(size);
		YOffset = -ContentPaint.ascent();
		TextHeight = ContentPaint.descent() + YOffset;
		ContentHeight = (int) (TextHeight * (E[0] - 1));
		requestLayout();
		invalidate();
	}

	public void setText(String s) {
		setText(s.toCharArray());
	}

	public void setText(char[] s) {
		this.S = s;
		E[E[0] = 1] = 0;
		for (int i = 0; i < s.length; i++)
			if (s[i] == '\n') {
				if (++E[0] == E.length) {
					int[] ne = new int[E.length + 256];
					System.arraycopy(E, 0, ne, 0, E.length);
					E = ne;
				}
				E[E[0]] = i + 1;
			}
		E[++E[0]] = s.length + 1;
		ContentHeight = (int) (TextHeight * (E[0] - 1));
		requestLayout();
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(
				MeasureSpec.getSize(widthMeasureSpec),
				ContentHeight
		);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float y = YOffset;
		for (int line = 1; line < E[0]; line++) {
			canvas.drawText(S, E[line], E[line + 1] - E[line] - 1, 0, y, ContentPaint);
			y += TextHeight;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		SpeedCalc.addMovement(event);
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_stX = _lastX = event.getX();
				_stY = _lastY = event.getY();
				isDragging = false;
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
				return true;
			case MotionEvent.ACTION_MOVE:
				float x = event.getX(), y = event.getY();
				if ((!isDragging) && Math.abs(x - _stX) > _touchSlop || Math.abs(y - _stY) > _touchSlop)
					isDragging = true;
				if (isDragging) {
					int deltaX = (int) (_lastX - x), deltaY = (int) (_lastY - y);
					if (Math.abs(deltaX) > Math.abs(deltaY)) deltaY = 0;
					else deltaX = 0;
					overScrollBy(deltaX, deltaY, getScrollX(), getScrollY(), Integer.MAX_VALUE - 20, ContentHeight, 20, 20, true);
					invalidate();
				}
				_lastX = x;
				_lastY = y;
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				SpeedCalc.computeCurrentVelocity(1000);
				int speedX = (int) SpeedCalc.getXVelocity();
				int speedY = (int) SpeedCalc.getYVelocity();
				if (Math.abs(speedX) <= _minFling) speedX = 0;
				if (Math.abs(speedY) <= _minFling) speedY = 0;
				if (speedX != 0 || speedY != 0) {
					Scroller.fling(getScrollX(), getScrollY(), -speedX, -speedY, -10, Integer.MAX_VALUE, -10, ContentHeight);
					invalidate();
				}
				SpeedCalc.clear();
				invalidate();
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		if (Scroller.isFinished()) super.scrollTo(scrollX, scrollY);
		else {
			int oldX = getScrollX();
			int oldY = getScrollY();
			if (clampedX || clampedY) {
				Log.i("SpringBack", scrollX + " " + scrollY);
				Scroller.springBack(scrollX, scrollY, 0, Integer.MAX_VALUE, 0, ContentHeight);
				invalidate();
			}
		}
	}

	@Override
	public void computeScroll() {
		if (Scroller.computeScrollOffset()) {
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = Scroller.getCurrX();
			int y = Scroller.getCurrY();
			if (oldX != x || oldY != y)
				// TODO Extract MaxOverScroll
				overScrollBy(x - oldX, y - oldY, oldX, oldY, Integer.MAX_VALUE - 20, ContentHeight, 20, 20, false);
			postInvalidate();
		}
		/*if (Scroller.computeScrollOffset()) {
			scrollTo(Scroller.getCurrX(), Scroller.getCurrY());
			postInvalidate();
		}*/
	}
}