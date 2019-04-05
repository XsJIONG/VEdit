package com.xsjiong.vedit;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

public class MainApplication extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		G.Initialize(base);
		//Logs.setLogFile(icon_create File(Environment.getExternalStorageDirectory(), "VEditLog.txt"));
	}
}
