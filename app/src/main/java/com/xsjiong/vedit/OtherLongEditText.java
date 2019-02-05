package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class OtherLongEditText extends ListView {
	private LongTextViewAdapter A;

	public OtherLongEditText(Context cx) {
		this(cx, null, 0);
	}

	public OtherLongEditText(Context cx, AttributeSet attr) {
		this(cx, attr, 0);
	}

	public OtherLongEditText(Context cx, AttributeSet attr, int style) {
		super(cx, attr, style);
		setDivider(null);
		setAdapter(A = new LongTextViewAdapter());
	}

	public void setText(String text) {
		A.setText(text);
		A.notifyDataSetChanged();
	}

	public void setTextSize(float size) {
		A.setTextSize(size);
		A.notifyDataSetChanged();
	}

	public void setTextSize(int unit, float size) {
		setTextSize(TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics()));
	}

	public int getLineCount() {
		return A.C.length;
	}

	public String getLineContent(int ind) {
		return A.C[ind];
	}

	public void setAntiAlias(boolean flag) {
		A.setAntiAlias(flag);
		invalidate();
	}

	public void setTextColor(int color) {
		A.setTextColor(color);
		invalidate();
	}

	private static class LongTextViewAdapter extends BaseAdapter {
		String[] C;
		TextPaint P;
		private float YOffset;
		private int TextHeight;

		public LongTextViewAdapter() {
			this(null);
		}

		public LongTextViewAdapter(String text) {
			setText(text);
			P = new TextPaint();
			setAntiAlias(true);
			setTextColor(Color.BLACK);
			setTextSize(50);
		}

		public void setAntiAlias(boolean flag) {
			P.setAntiAlias(flag);
		}

		public void setTextColor(int color) {
			P.setColor(color);
		}

		public void setText(String text) {
			if (text == null) this.C = new String[0];
			else this.C = text.split("\n");
		}

		public void setTextSize(float size) {
			P.setTextSize(size);
			YOffset = -P.ascent();
			TextHeight = (int) Math.ceil(P.descent() + YOffset);
		}

		@Override
		public int getCount() {
			return C.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SingleTextView ret = (SingleTextView) convertView;
			if (ret == null)
				ret = new SingleTextView(parent.getContext(), this);
			ret.setText(C[position]);
			return ret;
		}

		private static class SingleTextView extends View {
			private LongTextViewAdapter Q;
			private String S;

			public SingleTextView(Context cx, LongTextViewAdapter parent) {
				super(cx);
				this.Q = parent;
			}

			public void setText(String s) {
				this.S = s;
			}

			public String getText() {
				return S;
			}

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Q.TextHeight);
			}

			@Override
			protected void onDraw(Canvas canvas) {
				canvas.drawText(S, 0, Q.YOffset, Q.P);
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		long st = System.currentTimeMillis();
		super.draw(canvas);
		if (G.LOG_TIME) {
			st = System.currentTimeMillis() - st;
			Log.i(G.T, "耗时2: " + st);
		}
	}
}
