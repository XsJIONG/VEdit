package com.xsjiong.vedit;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.io.File;

public class ChooseFileDialog extends AlertDialog implements ChooseFileFragment.ChooseFileListener {
	private ChooseFileFragment frag;
	private ChooseFileListener listener;

	public ChooseFileDialog(Context cx, File f, ChooseFileListener listener) {
		this(cx, f, listener, false);
	}

	public ChooseFileDialog(Context cx, File f, ChooseFileListener listener, boolean chooseDir) {
		super(cx);
		this.listener = listener;
		frag = new ChooseFileFragment(cx, f, this, chooseDir);
		super.setView(frag.getView());
	}

	@Override
	public void onChoose(File f) {
		if (listener != null) listener.onChoose(f);
		dismiss();
	}

	public void setChooseFileListener(ChooseFileListener listener) {
		this.listener = listener;
	}

	public ChooseFileListener getChooseFileListener() {
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

	public interface ChooseFileListener {
		void onChoose(File f);
	}
}
