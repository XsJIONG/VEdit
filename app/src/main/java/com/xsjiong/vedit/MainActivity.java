package com.xsjiong.vedit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {
	private LinearLayoutCompat C;
	private VEdit V1;
	private OtherLongEditText V2;
	private VEditTest V3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		C = new LinearLayoutCompat(this);
		C.setOrientation(LinearLayoutCompat.HORIZONTAL);
		LinearLayoutCompat.LayoutParams P = new LinearLayoutCompat.LayoutParams(0, -1);
		P.weight = 1;
		/*V1 = new VEdit(this);
		V1.setBackgroundColor(Color.WHITE);
		V1.setTextSize(50);
		V1.setText(G.D);
		C.addView(V1, P);*/
		/*V2 = new OtherLongEditText(this);
		V2.setBackgroundColor(Color.WHITE);
		V2.setText(G.D);
		C.addView(V2, P);*/
		V3 = new VEditTest(this);
		V3.setBackgroundColor(Color.WHITE);
//		V3.setText(G.D);
		try {
			V3.setText(new String(IO.Read(new FileInputStream("/sdcard/AppProjects/MNIST.nn"))));
//			V3.setText(G.D);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		C.addView(V3, P);
		setContentView(C);
	}
}
