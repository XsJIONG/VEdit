package com.xsjiong.vedit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class UI {
	public static int ThemeColor = 0xFF2196F3;
	public static int AccentColor = 0xFFFFFFFF;

	public static void tintStatusBar(Activity activity, int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				Window window = activity.getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(color);
			} catch (Throwable t) {
				Logs.e("tintStatusBar -> " + activity, t);
			}
		}
	}

	public static final int dp2px(int dp) {
		return (int) (Resources.getSystem().getDisplayMetrics().density * dp + 0.5f);
	}

	public static void onUI(Runnable action) {
		if (Looper.getMainLooper() == Looper.myLooper()) action.run();
		else new Handler(Looper.getMainLooper()).post(action);
	}

	public static void preventDismiss(AlertDialog dialog) {
		try {
			Field field = Dialog.class.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, false);
		} catch (Throwable e) {
			Logs.wtf(e);
		}
	}

	public static void forceDismiss(AlertDialog dialog) {
		try {
			Field field = Dialog.class.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		} catch (Throwable e) {
			Logs.wtf(e);
		}
	}

	public static Drawable tintDrawable(Drawable d, int color) {
		d = d.mutate();
		d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		return d;
	}

	public static void postShowError(final Context cx, final Throwable t) {
		onUI(new Runnable() {
			@Override
			public void run() {
				showError(cx, t);
			}
		});
	}

	public static AlertDialog showError(final Context cx, Throwable t) {
		final String msg = Logs.getStackTraceString(t);
		AlertDialog ret = new AlertDialog.Builder(cx).setTitle("Oops").setMessage(msg).setCancelable(true).setNegativeButton("复制", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ClipboardManager manager = (ClipboardManager) cx.getSystemService(Context.CLIPBOARD_SERVICE);
				manager.setPrimaryClip(ClipData.newPlainText("Error", msg));
			}
		}).setPositiveButton("确定", null).create();
		ret.show();
		return ret;
	}
}