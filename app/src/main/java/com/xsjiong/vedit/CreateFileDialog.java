package com.xsjiong.vedit;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import java.io.File;
import java.io.FileFilter;

public class CreateFileDialog extends AlertDialog implements ChooseFileFragment.ChooseFileListener {
	private ChooseFileFragment frag;
	private CreateFileListener listener;
	private File ChosenFile;
	private AlertDialog InputDialog, OverrideDialog;
	private AppCompatEditText NameInput;

	public CreateFileDialog(Context cx, File f, final CreateFileListener listener) {
		super(cx);
		this.listener = listener;
		frag = new ChooseFileFragment(cx, f, this, true, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return true;
			}
		});
		NameInput = new AppCompatEditText(cx);
		NameInput.setHint("文件名");
		InputDialog = new AlertDialog.Builder(cx).setTitle("文件名字").setView(NameInput).setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
				if (listener != null) listener.onChoose(ret);
				dismiss();
			}
		}).setNegativeButton("取消", null).setCancelable(true).create();
		OverrideDialog = new AlertDialog.Builder(cx).setTitle("文件已存在").setMessage("你确定要覆盖此文件吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ChosenFile.delete();
				UI.forceDismiss(OverrideDialog);
				if (listener != null) listener.onChoose(ChosenFile);
				dismiss();
			}
		}).setNegativeButton("取消", null).setCancelable(true).create();
		super.setView(frag.getView());
	}

	@Override
	public void onChoose(File f) {
		ChosenFile = f;
		if (f.isDirectory()) {
			NameInput.setError(null);
			NameInput.getText().clear();
			InputDialog.show();
			return;
		}
		OverrideDialog.show();
	}

	public void setChooseFileListener(CreateFileListener listener) {
		this.listener = listener;
	}

	public CreateFileListener getChooseFileListener() {
		return this.listener;
	}

	public void setLastText(CharSequence cs) {
		frag.setLastText(cs);
	}


	public void setChooseDirText(CharSequence cs) {
		frag.setChooseDirText(cs);
	}

	public CharSequence getLastText() {
		return frag.getLastText();
	}

	public CharSequence getChooseDirText() {
		return frag.getChooseDirText();
	}

	public ChooseFileFragment getFragment() {
		return frag;
	}

	public void setNow(File f) {
		frag.setNow(f);
	}

	public File getNow() {
		return frag.getNow();
	}

	@Override
	public void setView(View v) {
		throw new RuntimeException("Stub!");
	}

	public interface CreateFileListener {
		void onChoose(File f);
	}
}
