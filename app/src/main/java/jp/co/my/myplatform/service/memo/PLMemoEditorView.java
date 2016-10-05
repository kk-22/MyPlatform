package jp.co.my.myplatform.service.memo;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLApplication;

public class PLMemoEditorView extends PLContentView {

	private static final String KEY_LAST_NAME = "KEY_LAST_NAME";
	private static final int EDIT_MODE_HEIGHT = 1150;				// 入力モード中のEditTextの高さ

	private EditText mEditText;
	private String mCurrentName;

	public PLMemoEditorView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mEditText = (EditText) findViewById(R.id.memo_edit);

		initEditTextEvent();
		initButtonEvent();

		// TODO:async?
		SharedPreferences pref = MYLogUtil.getPreference();
		String lastName = pref.getString(KEY_LAST_NAME, null);
		if (lastName != null && loadFromFile(lastName)) {
			loadedMemo(lastName, false);
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String name = format.format(Calendar.getInstance().getTime()) + ".txt";
			loadedMemo(name, true);
		}
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		saveToFile();
	}

	@Override
	public boolean onBackKey() {
		changeFullMode();
		return super.onBackKey();
	}

	private void saveToFile() {
		// TODO: if text size is 0, don't save.
		// TODO: 文字があったテキストが0文字になったら削除
		// TODO: 変更フラグを持って、未変更ならスキップ
		// TODO: use async?
		String path = getDirectoryPath();
		File directory = new File(path);
		if (!directory.exists() && directory.mkdirs()) {
			MYLogUtil.showErrorToast("Can't create directory:" +directory.getPath());
			return;
		}

		File textFile = new File(directory, mCurrentName);
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(textFile)));
			writer.write(mEditText.getText().toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	private boolean loadFromFile(String name) {
		String path = getDirectoryPath();
		// TODO:Use title of argument
		File textFile = new File(path + name);
		if (!textFile.exists()) {
			MYLogUtil.showErrorToast("no exist file:" +textFile.getName());
			return false;
		}

		StringBuilder stringBuilder = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(textFile));
			String lineStr;
			while((lineStr = reader.readLine()) != null){
				stringBuilder.append(lineStr).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
		mEditText.setText(stringBuilder.toString());
		return true;
	}

	private void loadedMemo(String name, boolean savePreferences) {
		mCurrentName = name;
		if (savePreferences) {
			SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
			editor.putString(KEY_LAST_NAME, name);
			editor.commit();
		}
	}

	private void initEditTextEvent() {
		mEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					return onBackKey();
				} else if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}

//				String replaceText = "\nテスト";
//				int start = mEditText.getSelectionStart();
//				int end = mEditText.getSelectionEnd();
//				Editable editable = mEditText.getText();
//				editable.replace(Math.min( start, end ), Math.max( start, end ), replaceText );

				MYLogUtil.outputLog("changed");
//				PLMemoEditor.this.set

				return false;
			}
		});
		mEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.outputLog("onClick");
				changeEditMode();
			}
		});
		mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					MYLogUtil.outputLog("onFocusChange");
					changeEditMode();
				}
			}
		});
		mEditText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				int rootHeight = PLMemoEditorView.this.getHeight();
				mEditText.setHeight(rootHeight - 200);

				// ちらつき防止
				findViewById(R.id.button_linear).setVisibility(View.VISIBLE);
			}
		});
	}

	private void changeEditMode() {
		MYLogUtil.outputLog("changeEditMode");
		mEditText.setHeight(EDIT_MODE_HEIGHT);
	}

	private void changeFullMode() {
		MYLogUtil.outputLog("changeFullMode");
		int rootHeight = PLMemoEditorView.this.getHeight();
		mEditText.setHeight(rootHeight - 200);
	}

	private void initButtonEvent() {
		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveToFile();
				MYLogUtil.showToast("Save to file");
			}
		});
		findViewById(R.id.change_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEditText.getHeight() == EDIT_MODE_HEIGHT) {
					InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethod.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
					changeFullMode();
				} else {
					mEditText.setHeight(EDIT_MODE_HEIGHT);
				}
			}
		});
	}

	private String getDirectoryPath() {
		return PLApplication.appRootPath() + "memo/";
	}
}
