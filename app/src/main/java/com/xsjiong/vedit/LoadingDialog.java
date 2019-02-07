package com.xsjiong.vedit;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class LoadingDialog extends FullScreenDialog {
	public LoadingDialog(Context cx) {
		super(cx);
		setCanceledOnTouchOutside(false);
		setCancelable(false);
		Initialize();
	}

	private LinearLayout Root;
	private SmoothProgressBar Content;
	private TextView Message;

	private void Initialize() {
		Root = new LinearLayout(getContext());
		Root.setGravity(Gravity.CENTER);
		Root.setBackground(null);
		Root.setOrientation(LinearLayout.VERTICAL);
		Content = new SmoothProgressBar(getContext());
		Content.setSmoothProgressDrawableColors(G.REFRESH_COLORS);
		Root.addView(Content);
		Message = new TextView(getContext());
		Message.setTextColor(Color.WHITE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
		params.topMargin = UI.dp2px(10);
		Root.addView(Message, params);
		Message.setVisibility(View.GONE);
		setContentView(Root);
	}

	public LoadingDialog setMessage(CharSequence cs) {
		Message.setText(cs);
		Message.setVisibility(cs == null ? View.GONE : View.VISIBLE);
		return this;
	}
}
