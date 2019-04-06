package com.xsjiong.vedit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;

import java.io.File;
import java.io.FileFilter;

public class ChooseFileActivity extends BaseActivity implements ChooseFileFragment.ChooseFileListener {
	public static final String TAG_RESULT = "result";

	private static final String TAG_CHOOSE_DIR = "choose_dir";
	private static final String TAG_CREATE_FILE = "create_file";

	private ChooseFileFragment F;
	private Intent Result;
	private boolean _CREATE;
	private boolean _DIRECTORY;
	private File ChosenFile;
	private AlertDialog InputDialog, OverrideDialog;
	private AppCompatEditText NameInput;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Result = new Intent();
		setResult(RESULT_OK, Result);
		Intent intent = getIntent();
		if (!(_CREATE = intent.getBooleanExtra(TAG_CREATE_FILE, false)))
			_DIRECTORY = intent.getBooleanExtra(TAG_CHOOSE_DIR, false);
		if (_CREATE) {
			F = new ChooseFileFragment(this, G._HOME_DIR, this, true, new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return true;
				}
			});
			NameInput = new AppCompatEditText(this);
			NameInput.setHint("文件名");
			InputDialog = new AlertDialog.Builder(this).setTitle("文件名字").setView(NameInput).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = NameInput.getText().toString();
					if (name.length() == 0) {
						NameInput.setError("文件名不能为空");
						UI.preventDismiss(InputDialog);
						return;
					}
					File ret = null;
					try {
						ret = new File(ChosenFile, name);
					} catch (Throwable t) {
						NameInput.setError("含有非法字符");
						UI.preventDismiss(InputDialog);
						return;
					}
					if (ret.exists()) {
						NameInput.setError("文件已存在");
						UI.preventDismiss(InputDialog);
						return;
					}
					try {
						ret.createNewFile();
					} catch (Throwable t) {
						NameInput.setError("创建文件失败");
						UI.preventDismiss(InputDialog);
						return;
					}
					UI.forceDismiss(InputDialog);
					returnFile(ret);
				}
			}).setNegativeButton("取消", null).setCancelable(true).create();
			OverrideDialog = new AlertDialog.Builder(this).setTitle("文件已存在").setMessage("你确定要覆盖此文件吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ChosenFile.delete();
					UI.forceDismiss(OverrideDialog);
					returnFile(ChosenFile);
				}
			}).setNegativeButton("取消", null).setCancelable(true).create();
		} else
			F = new ChooseFileFragment(this, G._HOME_DIR, this, _DIRECTORY);
		setContentView(F.getView());
	}

	@Override
	public void onChoose(File f) {
		if (_CREATE) {
			ChosenFile = f;
			if (f.isDirectory()) {
				NameInput.setError(null);
				NameInput.getText().clear();
				InputDialog.show();
				return;
			}
			OverrideDialog.show();
		} else returnFile(f);
	}

	private void returnFile(File f) {
		Result.putExtra(TAG_RESULT, f.getAbsolutePath());
		finish();
	}

	public static void chooseFile(Activity cx, int requestCode) {
		Intent intent = new Intent(cx, ChooseFileActivity.class);
		intent.putExtra(TAG_CHOOSE_DIR, false);
		cx.startActivityForResult(intent, requestCode);
	}

	public static void chooseDirectory(Activity cx, int requestCode) {
		Intent intent = new Intent(cx, ChooseFileActivity.class);
		intent.putExtra(TAG_CHOOSE_DIR, true);
		cx.startActivityForResult(intent, requestCode);
	}

	public static void createFile(Activity cx, int requestCode) {
		Intent intent = new Intent(cx, ChooseFileActivity.class);
		intent.putExtra(TAG_CREATE_FILE, true);
		cx.startActivityForResult(intent, requestCode);
	}
}
