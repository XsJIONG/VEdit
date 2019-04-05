package com.xsjiong.vedit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;

public class SettingActivity extends BaseActivity {
	public static final String CONFIG_CHANGED = "ConfigChanged";

	private SettingFragment Q;
	private Intent ResultIntent;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ResultIntent = new Intent();
		setResult(RESULT_OK, ResultIntent);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		Q = new SettingFragment(this);
		Q.addGroup("编辑器");
		Q.addSimpleItem("设置字体大小", "设定编辑器的字体大小").setOnClickListener(new View.OnClickListener() {
			private AlertDialog Dialog;

			@Override
			public void onClick(View v) {
				final AppCompatEditText edit = new AppCompatEditText(SettingActivity.this);
				edit.setHint("字体大小");
				edit.setText(Integer.toString(G._TEXT_SIZE));
				edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				edit.setSelection(edit.getText().length());
				Dialog = new AlertDialog.Builder(SettingActivity.this).setTitle("设置字体大小").setView(edit).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UI.preventDismiss(Dialog);
						String str = edit.getText().toString();
						if (str.length() == 0) {
							edit.setError("不能为空");
							return;
						}
						int size;
						try {
							size = Integer.parseInt(str);
						} catch (Throwable t) {
							edit.setError("含有非法字符");
							return;
						}
						UI.forceDismiss(Dialog);
						if (size == G._TEXT_SIZE) return;
						G.setTextSize(size);
						onConfigChanged();
					}
				}).setCancelable(true).setNegativeButton("取消", null).create();
				Dialog.show();
			}
		});
		Q.addSimpleItem("切换高亮", "切换编辑器所使用的高亮方式").setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingActivity.this).setTitle("切换高亮").setSingleChoiceItems(G.LEXER_NAMES, G._LEXER_ID, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == G._LEXER_ID) return;
						G.setLexerId(which);
						onConfigChanged();
					}
				}).setCancelable(true).setPositiveButton("取消", null).show();
			}
		});
		setContentView(Q.getView());
	}

	private void onConfigChanged() {
		ResultIntent.putExtra(CONFIG_CHANGED, true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
