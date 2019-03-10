package com.xsjiong.vedit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import com.xsjiong.vedit.scheme.VEditSchemeDark;

public class TestActivity extends Activity {
	private LinearLayout Container;
	private VEdit Content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Container = new LinearLayout(this);
		Container.setOrientation(LinearLayout.HORIZONTAL);
		Content = new VEdit(this);
		Content.setColorScheme(VEditSchemeDark.getInstance());
		Content.setTypeface(Typeface.createFromAsset(getAssets(), "FiraCode-Medium.ttf"));
		Content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		Container.addView(Content, -1, -1);
		setContentView(Container);
		String T = "Load Failed";
		try {
			T = new String(IO.Read(getAssets().open("ActivityManager.java")));
		} catch (Throwable t) {
			T = Log.getStackTraceString(t);
		}
		Content.setText(T);
	}

	@Override
	protected void onPause() {
		Content.hideIME();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Content.hideIME();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "撤销");
		menu.add(0, 0, 1, "重做");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getOrder()) {
			case 0:
				Content.undo();
				break;
			case 1:
				Content.redo();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}