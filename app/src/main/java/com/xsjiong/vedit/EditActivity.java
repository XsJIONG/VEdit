package com.xsjiong.vedit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.xsjiong.vedit.theme.VEditThemeDark;
import com.xsjiong.vedit.theme.VEditThemeLight;
import com.xsjiong.vedit.ui.UI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class EditActivity extends BaseActivity implements VEdit.EditListener, MultiContentManager.EditDataClickListener, C {
	public static final int REQUEST_CODE_SETTING = 1;
	public static final int REQUEST_CODE_CHOOSE_FILE = 2;

	private LinearLayoutCompat Container;
	private MultiContentManager ContentManager;
	private VEdit Content;
	private Toolbar Title;
	private LoadingDialog Loading;

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
		ContentManager.setTheme(VEditThemeDark.getInstance());
		ContentManager.setEditDataClickListener(this);
		Content = ContentManager.getContent();
		Content.setTypeface(Typeface.createFromAsset(getAssets(), "FiraCode-Medium.ttf"));
		Content.setAutoParse(false);
		Content.setEditListener(this);
		onSettingChanged();
		Container.addView(ContentManager, -1, -1);
		setContentView(Container);

		Loading = new LoadingDialog(this);
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
		getPermissions(_STORAGE_PERMISSIONS, _STORAGE_DESC, false);
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
		if (data.getFile() == null) {
			setChooseFileListener(new ChooseFileListener() {
				@Override
				public void onChoose(File f) {
					data.setFile(f);
					SaveTab(data, action);
					//ContentManager.onEditDataUpdated(data.index);
				}
			});
			ChooseFileActivity.createFile(this, REQUEST_CODE_CHOOSE_FILE);
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(data.getFile());
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
		sm.add(0, 6, 0, "Debug");
		menu.add(0, 5, 0, R.string.title_settings).setIcon(UI.tintDrawable(this, R.mipmap.icon_settings, UI.AccentColor)).setShowAsActionFlags(flag);
		return true;
	}

	private void showLoading(final String msg) {
		UI.onUI(new Runnable() {
			@Override
			public void run() {
				Loading.setMessage(msg);
				if (!Loading.isShowing()) Loading.show();
			}
		});
	}

	private void dismissLoading() {
		UI.onUI(new Runnable() {
			@Override
			public void run() {
				Loading.dismiss();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				ContentManager.addTab();
				break;
			case 2: {
				if (requestStoragePermissions()) break;
				setChooseFileListener(new ChooseFileListener() {
					@Override
					public void onChoose(final File f) {
						showLoading("加载文件中...");
						new Thread() {
							@Override
							public void run() {
								try {
									final char[] s = new String(IO.Read(new FileInputStream(f))).toCharArray();
									UI.onUI(new Runnable() {
										@Override
										public void run() {
											ContentManager.addTab(s, f);
											// 好很好！三重嵌套nmsl
											new Thread() {
												@Override
												public void run() {
													Content.parseAll();
													dismissLoading();
												}
											}.start();
										}
									});
								} catch (Throwable t) {
									dismissLoading();
									UI.showError(EditActivity.this, t);
								}
							}
						}.start();
					}
				});
				ChooseFileActivity.chooseFile(this, REQUEST_CODE_CHOOSE_FILE);
				break;
			}
			case 3: {
				SaveTab(ContentManager.getCurrentEditData(), null);
				break;
			}
			case 4: {
				if (requestStoragePermissions()) break;
				setChooseFileListener(new ChooseFileListener() {
					@Override
					public void onChoose(File f) {
						MultiContentManager.EditData data = ContentManager.getCurrentEditData();
						ContentManager.closeExist(f);
						data.setFile(f);
						ContentManager.onEditDataUpdated(data.index);
						SaveTab(data, null);
					}
				});
				ChooseFileActivity.createFile(this, REQUEST_CODE_CHOOSE_FILE);
				break;
			}
			case 5: {
				startActivityForResult(new Intent(this, SettingActivity.class), REQUEST_CODE_SETTING);
				break;
			}
			case 6:{
				new AlertDialog.Builder(this).setTitle("Debug").setMessage(Content.getLexer().getStateString()).setPositiveButton("确定", null).setCancelable(true).show();
			}
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private ChooseFileListener _ChooseFileListener;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_CODE_SETTING:
					if (data.getBooleanExtra(SettingActivity.CONFIG_CHANGED, false)) onSettingChanged();
					break;
				case REQUEST_CODE_CHOOSE_FILE:
					String s = data.getStringExtra(ChooseFileActivity.TAG_RESULT);
					if (s == null) break;
					if (_ChooseFileListener != null) _ChooseFileListener.onChoose(new File(s));
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void onSettingChanged() {
		Content.setTextSize(TypedValue.COMPLEX_UNIT_SP, G._TEXT_SIZE);
		Content.setShowLineNumber(G._SHOW_LINE_NUMBER);
		ContentManager.setTheme(G._NIGHT_THEME ? VEditThemeDark.getInstance() : VEditThemeLight.getInstance());
		ContentManager.onEditDataUpdated(ContentManager.getIndex());
	}

	private synchronized void setChooseFileListener(ChooseFileListener listener) {
		this._ChooseFileListener = listener;
	}

	private interface ChooseFileListener {
		void onChoose(File f);
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
				if (ContentManager.getColorScheme() instanceof VEditThemeLight)
					ContentManager.setTheme(VEditThemeDark.getInstance());
				else
					ContentManager.setTheme(VEditThemeLight.getInstance());
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
				new AlertDialog.Builder(this).setTitle("切换高亮").setSingleChoiceItems(G.LEXER_NAMES, G.getLexerIndex(ContentManager.getLexer()), icon_create DialogInterface.OnClickListener() {
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