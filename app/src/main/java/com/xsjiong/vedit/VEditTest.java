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
	public static final int MEASURE_STEP = 50;

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
	private boolean isDragging = false;

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
		setTextSize(50);
		ContentPaint.setColor(Color.BLACK);
	}

	public void setTextSize(int unit, float size) {
		setTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));
	}

	private int TABSpaceCount = 4;
	private float TABWidth;

	public void setTextSize(float size) {
		ContentPaint.setTextSize(size);
		YOffset = -ContentPaint.ascent();
		TextHeight = ContentPaint.descent() + YOffset;
		TABWidth = TABSpaceCount * ContentPaint.measureText(" ");
		_updateBounds();
		requestLayout();
		invalidate();
	}

	public void setTABSpaceCount(int count) {
		TABSpaceCount = count;
		TABWidth = TABSpaceCount * ContentPaint.measureText(" ");
		invalidate();
	}

	public int getTABSpaceCount() {
		return TABSpaceCount;
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
		_updateBounds();
		requestLayout();
		invalidate();
	}

	/*@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure();
		setMeasuredDimension(
				MeasureSpec.getSize(widthMeasureSpec),
				ContentHeight
		);
	}*/

	@Override
	protected int getSuggestedMinimumHeight() {
		return ContentHeight;
	}

	private int _YScrollRange;

	private void _updateBounds() {
		ContentHeight = (int) (TextHeight * (E[0] - 1));
	}

	private int _maxOSX = 20, _maxOSY = 20;

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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		SpeedCalc.addMovement(event);
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				_stX = _lastX = event.getX();
				_stY = _lastY = event.getY();
				if (!Scroller.isFinished())
					Scroller.abortAnimation();
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
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		_YScrollRange = ContentHeight - (bottom - top);
	}

	private void springBack() {
		Scroller.springBack(getScrollX(), getScrollY(), 0, Integer.MAX_VALUE, 0, _YScrollRange);
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

	// TODO 还有512个字符都塞不满屏幕的情况！
	private char[] TMP = new char[512];
	private char[] TMP2 = new char[MEASURE_STEP];

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
		int line = Math.max((int) (getScrollY() / TextHeight) + 1, 1);
		float y = (line - 1) * TextHeight + YOffset;
		float bottom = getScrollY() + getHeight() + YOffset;
		float right = getScrollX() + getWidth();
		float XStart, wtmp, x;
		int i, en;
		int tot;
		LineDraw:
		for (; line < E[0]; line++) {
			if (false) {
				canvas.drawText(S, E[line], E[line + 1] - E[line] - 1, 0, y, ContentPaint);
			} else {
				i = E[line];
				en = E[line + 1] - 1;
				XStart = 0;
				if (getScrollX() > 0)
					// TODO 这里需要判断TAB额外变出的长度
					while (true) {
						System.arraycopy(S, i, TMP2, 0, tot = Math.min(en - i, MEASURE_STEP));
						if ((wtmp = (XStart + ContentPaint.measureText(TMP2, 0, tot))) >= getScrollX())
							break;
						if ((i += MEASURE_STEP) >= en) {
							if ((y += TextHeight) > bottom) break LineDraw;
							continue LineDraw;
						}
						XStart = wtmp;
					}
				tot = 0;
				for (x = XStart; i < en && x <= right; i++) {
					if ((TMP[tot] = S[i]) == '\t') {
						XStart += TABWidth;
						x += TABWidth;
					} else x += Math.max(10, ContentPaint.measureText(TMP, tot++, 1));
				}
				canvas.drawText(TMP, 0, tot, XStart, y, ContentPaint);
			}
			if ((y += TextHeight) >= bottom) break;
		}
		st = System.currentTimeMillis() - st;
		Log.i("VEdit", "耗时: " + st);
	}
}