package com.xsjiong.vedit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;

public class MainActivity extends AppCompatActivity {
	private LinearLayoutCompat C;
	private VEdit V1;
	private OtherLongEditText V2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		V1 = new VEdit(this);
		V1.setBackgroundColor(Color.WHITE);
		V1.setTextSize(50);
		V1.setText(G.D);
		V2 = new OtherLongEditText(this);
		V2.setBackgroundColor(Color.WHITE);
		V2.setTextSize(50);
		V2.setText(G.D);
		C = new LinearLayoutCompat(this);
		C.setOrientation(LinearLayoutCompat.HORIZONTAL);
		LinearLayoutCompat.LayoutParams P = new LinearLayoutCompat.LayoutParams(0, -1);
		P.weight = 1;
		C.addView(V1, P);
		//C.addView(V2, P);
		setContentView(C);
	}
}
