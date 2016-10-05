package jp.co.my.myplatform.service.memo;

import android.content.SharedPreferences;
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
import jp.co.my.myplatform.service.core.PLApplication;

public class PLMemoReadWriter {

	private static final String KEY_LAST_NAME = "KEY_LAST_NAME";

	private EditText mEditText;
	private String mCurrentName;

	public PLMemoReadWriter(EditText editText) {
		mEditText = editText;
	}

	public void loadFirstMemo() {
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

	public void saveToFile() {
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

	private String getDirectoryPath() {
		return PLApplication.appRootPath() + "memo/";
	}
}
