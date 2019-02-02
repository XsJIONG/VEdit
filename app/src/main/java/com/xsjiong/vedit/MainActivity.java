package com.xsjiong.vedit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.TextView;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {
	private LinearLayoutCompat C;
	private VEdit V1;
	private OtherLongEditText V2;
	private VEditTest V3;
	private TextView V4;
	private TextEditor V5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String T = null;
		if (true)
			T = G.D;
		else
			try {
				T = new String(IO.Read(new FileInputStream("/sdcard/AppProjects/MNIST.nn")));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		C = new LinearLayoutCompat(this);
		C.setOrientation(LinearLayoutCompat.HORIZONTAL);
		LinearLayoutCompat.LayoutParams P = new LinearLayoutCompat.LayoutParams(0, -1);
		P.weight = 1;
		/*V1 = new VEdit(this);
		V1.setBackgroundColor(Color.WHITE);
		V1.setTextSize(50);
		V1.setText(T);
		C.addView(V1, P);*/
		/*V2 = new OtherLongEditText(this);
		V2.setBackgroundColor(Color.WHITE);
		V2.setText(T);
		C.addView(V2, P);*/
		V3 = new VEditTest(this);
		V3.setBackgroundColor(Color.WHITE);
		V3.setText(T);
		C.addView(V3, P);
		/*V4 = new TextView(this) {
			@Override
			public void draw(Canvas canvas) {
				long st = System.currentTimeMillis();
				super.draw(canvas);
				st = System.currentTimeMillis() - st;
				Log.i("VEdit", "耗时4: " + st);
			}
		};
		V4.setTextColor(Color.BLACK);
		V4.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
		V4.setMovementMethod(new ScrollingMovementMethod());
		V4.setVerticalScrollBarEnabled(true);
		V4.setScroller(new Scroller(this));
		V4.setHorizontallyScrolling(true);
		V4.setMaxLines(Integer.MAX_VALUE);
		V4.setText(T);
		C.addView(V4, P);*/
		/*V5 = new TextEditor(this);
		V5.setText(T);
		C.addView(V5, P);*/
		setContentView(C);
	}

	@Override
	protected void onPause() {
		V3.hideIME();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		V3.hideIME();
		super.onDestroy();
	}
}
