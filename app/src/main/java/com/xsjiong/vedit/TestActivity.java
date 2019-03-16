package com.xsjiong.vedit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.xsjiong.vedit.scheme.VEditSchemeLight;
import com.xsjiong.vlexer.VJavaLexer;
import com.xsjiong.vlexer.VJavaScriptLexer;
import com.xsjiong.vlexer.VNullLexer;

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
		menu.add(0, 0, 2, "切换主题");
		menu.add(0, 0, 3, "隐藏行号");
		menu.add(0, 0, 4, "切换高亮");
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
			case 2:
				if (Content.getColorScheme() instanceof VEditSchemeLight)
					Content.setColorScheme(VEditSchemeDark.getInstance());
				else
					Content.setColorScheme(VEditSchemeLight.getInstance());
				break;
			case 3:
				if (Content.isShowLineNumber()) {
					Content.setShowLineNumber(false);
					item.setTitle("显示行号");
				} else {
					Content.setShowLineNumber(true);
					item.setTitle("隐藏行号");
				}
				break;
			case 4:
				new AlertDialog.Builder(this).setTitle("切换高亮").setItems(new String[] {"Java", "JavaScript", "无"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								Content.setLexer(new VJavaLexer());
								break;
							case 1:
								Content.setLexer(new VJavaScriptLexer());
								break;
							case 2:
								Content.setLexer(new VNullLexer());
								break;
						}
					}
				}).setCancelable(true).setPositiveButton("取消", null).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}