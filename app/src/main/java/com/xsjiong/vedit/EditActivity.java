package com.xsjiong.vedit;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.HorizontalScrollView;
import com.xsjiong.vedit.scheme.VEditScheme;
import com.xsjiong.vedit.scheme.VEditSchemeDark;
import com.xsjiong.vedit.scheme.VEditSchemeLight;
import com.xsjiong.vlexer.VJavaLexer;
import com.xsjiong.vlexer.VLexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class EditActivity extends BaseActivity implements VEdit.EditListener, MultiContentManager.EditDataClickListener {
	private LinearLayoutCompat Container;
	private MultiContentManager ContentManager;
	private VEdit Content;
	private Toolbar Title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Title = new Toolbar(this);
		Title.setTitle(R.string.app_name);
		ViewCompat.setElevation(Title, UI.dp2px(5));
		Title.setBackgroundColor(UI.ThemeColor);
		Title.setTitleTextColor(UI.AccentColor);
		setSupportActionBar(Title);
		Container = new LinearLayoutCompat(this);
		Container.setOrientation(LinearLayoutCompat.VERTICAL);
		Container.addView(Title);
		ContentManager = new MultiContentManager(this);
		ContentManager.setColorScheme(VEditSchemeDark.getInstance());
		ContentManager.setEditDataClickListener(this);
		Content = ContentManager.getContent();
		Content.setTypeface(Typeface.createFromAsset(getAssets(), "FiraCode-Medium.ttf"));
		Content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		Content.setEditListener(this);
		Container.addView(ContentManager, -1, -1);
		setContentView(Container);
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

	private static final String[] _STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
	private static final String[] _STORAGE_DESC = {"我们需要存储权限", "我们需要读取权限"};

	private final boolean requestStoragePermissions() {
		if (isAllPermissionsGranted(_STORAGE_PERMISSIONS)) return false;
		requestPermissions(_STORAGE_PERMISSIONS, _STORAGE_DESC);
		return true;
	}

	@Override
	public boolean onEdit(VEdit.EditAction action) {
		MultiContentManager.EditData data = ContentManager.getCurrentEditData();
		if (data.saved) {
			data.saved = false;
			ContentManager.onEditDataUpdated(ContentManager.getIndex());
		}
		return false;
	}

	@Override
	public void onEditDataClick(final MultiContentManager.EditData data) {
		if (data.index == ContentManager.getIndex()) {
			if (!data.saved) {
				new AlertDialog.Builder(this).setTitle("提示").setMessage("你还没有保存，确定要关闭吗？").setPositiveButton("保存并关闭", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SaveTab(data, new Runnable() {
							@Override
							public void run() {
								ContentManager.deleteTab(data.index);
							}
						});
					}
				}).setNeutralButton("关闭", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ContentManager.deleteTab(data.index);
					}
				}).setCancelable(true).show();
			} else {
				ContentManager.deleteTab(data.index);
			}
		}
	}

	private void SaveTab(final MultiContentManager.EditData data, final Runnable action) {
		if (requestStoragePermissions()) return;
		if (data.file == null) {
			new CreateFileDialog(this, Environment.getExternalStorageDirectory(), new CreateFileDialog.CreateFileListener() {
				@Override
				public void onChoose(File f) {
					data.file = f;
					ContentManager.onEditDataUpdated(data.index);
					SaveTab(data, action);
				}
			}).show();
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(data.file);
			out.write(Content.getText().getBytes());
			out.close();
			data.saved = true;
			ContentManager.onEditDataUpdated(data.index);
			if (action != null) action.run();
		} catch (Throwable t) {
			UI.showError(this, t);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final int flag = MenuItem.SHOW_AS_ACTION_ALWAYS;
		View v = new View(this);
		v.setBackgroundColor(Color.RED);
		SubMenu sm = menu.addSubMenu(0, 0, 0, "文件").setIcon(UI.tintDrawable(this, R.mipmap.icon_directory, UI.AccentColor));
		sm.getItem().setShowAsActionFlags(flag);
		sm.add(0, 1, 0, "新建");
		sm.add(0, 2, 0, "打开");
		sm.add(0, 3, 0, "保存");
		sm.add(0, 4, 0, "另存为");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				ContentManager.addTab();
				break;
			case 2: {
				if (requestStoragePermissions()) break;
				new ChooseFileDialog(this, Environment.getExternalStorageDirectory(), new ChooseFileDialog.ChooseFileListener() {
					@Override
					public void onChoose(File f) {
						ContentManager.addTab(f);
					}
				}, false).show();
				break;
			}
			case 3: {
				SaveTab(ContentManager.getCurrentEditData(), null);
				break;
			}
			case 4: {
				if (requestStoragePermissions()) break;
				new CreateFileDialog(this, Environment.getExternalStorageDirectory(), new CreateFileDialog.CreateFileListener() {
					@Override
					public void onChoose(File f) {
						MultiContentManager.EditData data = ContentManager.getCurrentEditData();
						ContentManager.closeExist(f);
						data.file = f;
						ContentManager.onEditDataUpdated(data.index);
						SaveTab(data, null);
					}
				}).show();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/*@Override
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
				ContentManager.undo();
				break;
			case 1:
				ContentManager.redo();
				break;
			case 2:
				if (ContentManager.getColorScheme() instanceof VEditSchemeLight)
					ContentManager.setColorScheme(VEditSchemeDark.getInstance());
				else
					ContentManager.setColorScheme(VEditSchemeLight.getInstance());
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
				icon_create AlertDialog.Builder(this).setTitle("切换高亮").setSingleChoiceItems(G.LEXER_NAMES, G.getLexerIndex(ContentManager.getLexer()), icon_create DialogInterface.OnClickListener() {
					@Override
					public void onEditDataClick(DialogInterface dialog, int which) {
						Content.setLexer(G.newLexer(which));
					}
				}).setCancelable(true).setPositiveButton("取消", null).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}*/


}