package com.xsjiong.vedit;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import com.xsjiong.vedit.scheme.VEditScheme;
import com.xsjiong.vedit.scheme.VEditSchemeLight;
import com.xsjiong.vlexer.VJavaLexer;
import com.xsjiong.vlexer.VLexer;

import java.io.File;
import java.io.FileInputStream;

public class MultiContentManager extends LinearLayoutCompat implements View.OnClickListener {
	public static final String UNTITLED = "untitled";

	private static final int EXPAND_SIZE = 8;
	private static int MAX_HEIGHT = -1;
	private static int BUTTON_PADDING = -1;

	private HorizontalScrollView ButtonScroller;
	private LinearLayoutCompat ButtonLayout;
	private int size;
	private EditData[] data = new EditData[EXPAND_SIZE];
	private int ind;
	private VEdit Content;
	private EditDataClickListener _ClickListener;

	public MultiContentManager(Context cx) {
		super(cx);
		if (MAX_HEIGHT == -1) {
			MAX_HEIGHT = UI.dp2px(30);
			BUTTON_PADDING = UI.dp2px(5);
		}
		setOrientation(LinearLayoutCompat.VERTICAL);
		ButtonLayout = new LinearLayoutCompat(cx);
		ButtonLayout.setOrientation(LinearLayoutCompat.HORIZONTAL);
		ButtonLayout.setDividerPadding(UI.dp2px(5));
		ButtonScroller = new HorizontalScrollView(cx);
		ButtonScroller.setFillViewport(true);
		ButtonScroller.addView(ButtonLayout, -1, MAX_HEIGHT);
		Content = new VEdit(cx);
		setColorScheme(VEditSchemeLight.getInstance());
		addView(ButtonScroller, -1, MAX_HEIGHT);
		addView(Content, -1, -1);
		size = 0;
		ind = -1;
		onTabSizeUpdated();
	}

	public EditData getCurrentEditData() {
		return data[ind];
	}

	public int getSize() {
		return size;
	}

	public int getIndex() {
		return ind;
	}

	public EditData getEditData(int index) {
		return data[index];
	}

	private void onTabSizeUpdated() {
		if (size == 0) {
			ind = -1;
			addTab();
		}
	}

	public void setEditDataClickListener(EditDataClickListener listener) {
		_ClickListener = listener;
	}

	public EditDataClickListener getEditDataClickListener() {
		return _ClickListener;
	}

	public VEdit getContent() {
		return Content;
	}

	public void setColorScheme(VEditScheme scheme) {
		ButtonLayout.setBackgroundColor(scheme.getBackgroundColor());
		Content.setColorScheme(scheme);
		for (int i = 0; i < size; i++)
			selectButton(i, i == ind);
	}

	public VEditScheme getColorScheme() {
		return Content.getColorScheme();
	}

	public void closeExist(File f) {
		for (int i = 0; i < size; i++)
			if (f.equals(data[i].file)) {
				deleteTab(i);
				return;
			}
	}

	private void ensureCapture() {
		int olen = data.length;
		if (size > olen) {
			olen += EXPAND_SIZE;
			EditData[] dst = new EditData[olen];
			System.arraycopy(data, 0, dst, 0, data.length);
			data = dst;
			dst = null;
		}
	}

	public void addTab(File f) {
		for (int i = 0; i < size; i++)
			if (f.equals(data[i].file)) {
				setIndex(i);
				return;
			}
		try {
			addTab(new String(IO.Read(new FileInputStream(f))).toCharArray(), f);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void deleteTab(int pos) {
		if (pos < 0) pos = 0;
		if (pos > size) pos = size;
		size--;
		for (int i = pos; i < size; i++) {
			data[i] = data[i + 1];
			data[i].index = i;
		}
		data[size] = null;
		ButtonLayout.removeViewAt(pos);
		if (ind == pos) {
			ind = -1;
			if (pos == size)
				setIndex(size - 1);
			else setIndex(pos);
		} else if (ind > pos) {
			pos = ind - 1;
			ind = -1;
			setIndex(pos);
		}
		onTabSizeUpdated();
	}

	public void addTab() {
		addTab(size, null);
	}

	public void addTab(char[] cs) {
		addTab(size, cs);
	}

	public void addTab(char[] cs, File file) {
		addTab(size, cs, file);
	}

	public void addTab(int pos, char[] cs) {
		addTab(pos, cs, null);
	}

	public void addTab(int pos, char[] cs, File file) {
		if (pos < 0) pos = 0;
		if (pos > size) pos = size;
		size++;
		ensureCapture();
		for (int i = size - 1; i > pos; i--)
			(data[i] = data[i - 1]).index = i;
		data[pos] = new EditData(pos);
		data[pos].file = file;
		if (file != null) data[pos].saved = true;
		data[pos].setText(cs);
		AppCompatButton button = new AppCompatButton(getContext());
		button.setTextColor(Content.getColorScheme().getLineNumberColor());
		button.setBackgroundColor(Content.getColorScheme().getBackgroundColor());
		button.setEllipsize(TextUtils.TruncateAt.END);
		button.setText(data[pos].getDisplay());
		button.setPadding(0, 0, 0, 0);
		button.setTextAlignment(TEXT_ALIGNMENT_CENTER);
		button.setAllCaps(false);
		button.setTag(data[pos]);
		button.setOnClickListener(this);
		button.setPadding(BUTTON_PADDING, 0, BUTTON_PADDING, 0);
		ButtonLayout.addView(button, pos);
		setIndex(pos);
		onTabSizeUpdated();
	}

	public void onEditDataUpdated(int ind) {
		getButton(ind).setText(data[ind].getDisplay());
	}

	@Override
	public void onClick(View v) {
		EditData data = (EditData) v.getTag();
		if (_ClickListener != null)
			_ClickListener.onEditDataClick(data);
		if (data.index != ind) setIndex(data.index);
	}

	public void setIndex(int pos) {
		if (size == 0) return;
		if (pos < 0) pos = 0;
		if (pos > size) pos = size;
		if (pos == ind) return;
		Log.i("VEdit", "pos " + pos);
		if (ind != -1) {
			selectButton(ind, false);
			data[ind].loadFrom(Content);
		}
		selectButton(ind = pos, true);
		final AppCompatButton button = getButton(ind);
		button.post(new Runnable() {
			@Override
			public void run() {
				int left = button.getLeft();
				int right = button.getRight() - ButtonScroller.getWidth();
				if (right > ButtonScroller.getScrollX())
					ButtonScroller.smoothScrollTo(right, 0);
				else if (left < ButtonScroller.getScrollX())
					ButtonScroller.smoothScrollTo(left, 0);
			}
		});
		data[ind].applyTo(Content);
	}

	public void selectButton(int ind, boolean selected) {
		if (ind < 0) return;
		if (ind >= ButtonLayout.getChildCount()) return;
		AppCompatButton button = getButton(ind);
		if (selected) {
			button.setBackgroundColor(Content.getColorScheme().getLineNumberColor());
			button.setTextColor(Content.getColorScheme().getBackgroundColor());
		} else {
			button.setBackgroundColor(Content.getColorScheme().getBackgroundColor());
			button.setTextColor(Content.getColorScheme().getLineNumberColor());
		}
	}

	public AppCompatButton getButton(int ind) {
		return (AppCompatButton) ButtonLayout.getChildAt(ind);
	}

	public interface EditDataClickListener {
		void onEditDataClick(EditData data);
	}

	public static class EditData {
		int scrollX, scrollY;
		int position;
		int index;
		int length;
		char[] cs;
		boolean saved;
		File file;
		Class<? extends VLexer> lexer;

		public EditData(int ind) {
			scrollX = scrollY = position = 0;
			saved = false;
			file = null;
			lexer = VJavaLexer.class;
			index = ind;
		}

		public EditData(int ind, VEdit edit) {
			this(ind);
			loadFrom(edit);
		}

		public void setText(char[] cs) {
			if (cs == null) return;
			setText(cs, cs.length);
		}

		public void setText(char[] cs, int length) {
			this.cs = cs;
			this.length = length;
		}

		public void loadFrom(VEdit edit) {
			scrollX = edit.getScrollX();
			scrollY = edit.getScrollY();
			position = edit.getCursorPosition();
			lexer = edit.getLexer().getClass();
			length = edit.getTextLength();
			cs = edit.getRawChars();
		}

		public void applyTo(VEdit edit) {
			edit.finishSelecting();
			edit.setText(cs, length);
			try {
				edit.setLexer(lexer.newInstance());
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
			edit.moveCursor(position);
			edit.setScrollX(scrollX);
			edit.setScrollY(scrollY);
		}

		public String getDisplay() {
			String ret = file == null ? UNTITLED : file.getName();
			if (saved) return ret;
			return "*" + ret;
		}
	}
}