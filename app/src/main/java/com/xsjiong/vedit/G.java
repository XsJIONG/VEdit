package com.xsjiong.vedit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.xsjiong.vlexer.*;

public final class G {
	public static final String T = "VEdit";
	public static final int[] REFRESH_COLORS = {0xFF2196F3, 0xFFFBC02D, 0xFFFF5722, 0xFFE91E63, 0xFF7E57C2};
	public static final boolean LOG_TIME = false;
	public static final Class<? extends VLexer>[] LEXERS = (Class<? extends VLexer>[]) new Class<?>[] {VJavaLexer.class, VJavaScriptLexer.class, VCLexer.class, VCppLexer.class, VNullLexer.class};
	public static final String[] LEXER_NAMES = {"Java", "JavaScript", "C", "C++", "无"};

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