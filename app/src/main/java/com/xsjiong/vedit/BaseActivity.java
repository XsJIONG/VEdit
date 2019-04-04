package com.xsjiong.vedit;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
	protected static final int REQUEST_PERMISSION_CODE = 1926;
	private boolean REQUESTING_PERMISSION = false;
	private String[] PERMISSION_DESCRIPTIONS = null;

	protected synchronized void requestPermissions(String[] permissions, String[] desc) {
		if (REQUESTING_PERMISSION) return;
		REQUESTING_PERMISSION = true;
		PERMISSION_DESCRIPTIONS = desc;
		ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
	}

	public final boolean isAllPermissionsGranted(String[] permissions) {
		for (int i = 0; i < permissions.length; i++)
			if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
				return false;
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION_CODE) {
			REQUESTING_PERMISSION = false;
			for (int i = 0; i < grantResults.length; i++)
				if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
					new AlertDialog.Builder(this).setTitle("权限需求").setMessage(PERMISSION_DESCRIPTIONS[i]).setCancelable(true).setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							requestPermissions(permissions, PERMISSION_DESCRIPTIONS);
						}
					}).setNeutralButton("取消", null).show();
					break;
				}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
