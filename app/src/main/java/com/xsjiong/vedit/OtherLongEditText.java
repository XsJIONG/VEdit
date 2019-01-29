package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Created by Vlad on 15.07.2016.
 */
public class OtherLongEditText extends ListView {
	private static final int DEFAULT_COLOR = Color.BLACK;
	private static final int DEFAULT_TEXT_SIZE = 15;
	private static final int DEFAULT_GRAVITY = Gravity.LEFT | Gravity.TOP;
	private static final int DEFAULT_LINES_PER_ITEM = Gravity.LEFT | Gravity.TOP;
	private int textColor;
	private float textSize;
	private int gravity;
	private int maxLines;

	public OtherLongEditText(Context context) {
		super(context);
		init(null);
	}

	public OtherLongEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public OtherLongEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		setDivider(null);
		int textColor = DEFAULT_COLOR;
		float textSize = DEFAULT_TEXT_SIZE;
		int gravity = DEFAULT_GRAVITY;
		int maxLines = DEFAULT_LINES_PER_ITEM;

		setTextColor(textColor);
		setTextSize(textSize);
		setGravity(gravity);
		setMaxLines(maxLines);

	}

	public void setText(String text) {
		setAdapter(new LongTextViewAdapter(getContext(), text));
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		checkAndNotify();
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
		checkAndNotify();
	}

	public void setGravity(int gravity) {
		this.gravity = gravity;
		checkAndNotify();
	}

	public void setMaxLines(int maxLines) {
		this.maxLines = maxLines;
		checkAndNotify();
	}

	private void checkAndNotify() {
		if (getAdapter() instanceof BaseAdapter) {
			((BaseAdapter) getAdapter()).notifyDataSetChanged();
		}
	}

	/**
	 * Adapter, which handles the text wrapping into multiple list items
	 */
	private class LongTextViewAdapter extends BaseAdapter {

		private final Context context;
		private String[] content;
		private final SingleTextView textView;


		/**
		 * @param context - Context to be used across adapter
		 * @param text    - source text to be wrapped
		 */
		public LongTextViewAdapter(Context context, String text) {
			this.context = context;
			this.content = text.split("\n");

			textView = new SingleTextView(context);
//			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//			textView.setTextColor(textColor);
//			textView.setGravity(gravity);
		}

		@Override
		public int getCount() {
			Log.i("ContentLength", content.length + "");
			return content.length;
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
			Log.i("GetView", position + " " + content[position]);
			SingleTextView textView = (SingleTextView) convertView;
			if (textView == null) {
				textView = new SingleTextView(context);
			}
			textView.setText(content[position]);
			return textView;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		long st = System.currentTimeMillis();
		super.draw(canvas);
		st = System.currentTimeMillis() - st;
		Log.i("VEdit", "耗时2: " + st);
	}
}
