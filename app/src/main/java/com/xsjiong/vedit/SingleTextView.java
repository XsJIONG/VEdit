package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;

public class SingleTextView extends View {
	private static TextPaint GlobalPaint;
	private static float YOffset;

	private String S;

	public SingleTextView(Context cx) {
		super(cx);
		if (GlobalPaint == null) {
			GlobalPaint = new TextPaint();
			GlobalPaint.setColor(Color.BLACK);
			GlobalPaint.setAntiAlias(true);
			GlobalPaint.setTextSize(50);
			YOffset = -GlobalPaint.getFontMetrics().ascent;
		}
	}

	public void setText(String s) {
		this.S = s;
	}

	public String getText() {
		return S;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawText(S, 0, YOffset, GlobalPaint);
	}
}
