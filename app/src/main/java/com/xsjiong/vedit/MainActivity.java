package com.xsjiong.vedit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ActionMode;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileInputStream;

public class MainActivity extends Activity {
	private LinearLayout C;
	private VEdit Content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String T = null;
		if (true)
			T = G.D;
		else
			try {
				//T = new String(IO.Read(new FileInputStream("/sdcard/AppProjects/MNIST.nn")));
				T = new String(IO.Read(new FileInputStream("/sdcard/Download/JsVActivity.java")));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		C = new LinearLayout(this);
		C.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams P = new LinearLayout.LayoutParams(0, -1);
		P.weight = 1;
		Content = new VEdit(this);
		Content.setTypeface(Typeface.createFromAsset(getAssets(), "FiraCode-Medium.ttf"));
		Content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		Content.setBackgroundColor(Color.WHITE);
		Content.setText(T);
		C.addView(Content, P);
		EditText ed = new EditText(this);
		ed.setText(G.D);
		ed.setTextColor(Color.BLACK);
		C.addView(ed, P);
		setContentView(C);
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
