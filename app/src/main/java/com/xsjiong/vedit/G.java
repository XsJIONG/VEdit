package com.xsjiong.vedit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import com.xsjiong.vlexer.*;

import java.io.File;

public final class G {
	public static final String T = "VEdit";
	public static final int[] REFRESH_COLORS = {0xFF2196F3, 0xFFFBC02D, 0xFFFF5722, 0xFFE91E63, 0xFF7E57C2};
	public static final boolean LOG_TIME = false;
	public static final Class<? extends VLexer>[] LEXERS = (Class<? extends VLexer>[]) new Class<?>[] {VJavaLexer.class, VJavaScriptLexer.class, VCLexer.class, VCppLexer.class, VNullLexer.class};
	public static final String[] LEXER_NAMES = {"Java", "JavaScript", "C", "C++", "无"};
	private static SharedPreferences S;
	public static int _LEXER_ID;
	public static int _TEXT_SIZE;
	public static File _HOME_DIR;

	static final void Initialize(Context cx) {
		S = cx.getSharedPreferences("editor_config", Context.MODE_PRIVATE);
		String str = S.getString("lexer_name", LEXER_NAMES[0]);
		for (int i = 0; i < LEXER_NAMES.length; i++)
			if (LEXER_NAMES[i].equals(str)) {
				_LEXER_ID = i;
				break;
			}
		_TEXT_SIZE = S.getInt("text_size", 14);
		_HOME_DIR = new File(S.getString("home_dir", Environment.getExternalStorageDirectory().getAbsolutePath()));
	}

	public static void setLexerId(int id) {
		S.edit().putString("lexer_name", LEXER_NAMES[_LEXER_ID = id]).apply();
	}

	public static void setTextSize(int size) {
		S.edit().putInt("text_size", _TEXT_SIZE = size).apply();
	}

	public static void setHomeDir(File dir) {
		S.edit().putString("home_dir", (_HOME_DIR = dir).getAbsolutePath()).apply();
	}

	public static final int getLexerIndex(VLexer lexer) {
		Class<? extends VLexer> cl = lexer.getClass();
		for (int i = 0; i < LEXERS.length; i++)
			if (LEXERS[i] == cl) return i;
		return -1;
	}

	public static final VLexer newLexer(int index) {
		try {
			return LEXERS[index].newInstance();
		} catch (Throwable t) {
			return null;
		}
	}
}