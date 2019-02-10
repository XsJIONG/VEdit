package com.xsjiong.vedit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	private LinearLayout Container;
	private VEdit Content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Container = new LinearLayout(this);
		Container.setOrientation(LinearLayout.HORIZONTAL);
		Content = new VEdit(this);
		Content.setTypeface(Typeface.createFromAsset(getAssets(), "FiraCode-Medium.ttf"));
		Content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		Content.setBackgroundColor(Color.WHITE);
		Container.addView(Content, -1, -1);
		setContentView(Container);
		Content.setText(G.D);
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
}