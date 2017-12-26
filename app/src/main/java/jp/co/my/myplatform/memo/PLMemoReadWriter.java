package jp.co.my.myplatform.memo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.core.PLApplication;

public class PLMemoReadWriter {

	private static final String KEY_LAST_NAME = "KEY_LAST_NAME";

	private EditText mEditText;
	private String mCurrentName;

	public PLMemoReadWriter(EditText editText) {
		mEditText = editText;
	}

	public File[] memoFiles() {
		String path = getDirectoryPath();
		File directory = new File(path);
		return directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				String suffix = MYStringUtil.getSuffix(name);
				return (suffix != null && suffix.equals("txt"));
			}
		});
	}

	public void loadFirstMemo() {
		// TODO:async?
		SharedPreferences pref = MYLogUtil.getPreference();
		String lastName = pref.getString(KEY_LAST_NAME, null);
		if (lastName != null && loadFromFile(lastName)) {
			loadedMemo(lastName, false);
		} else {
			loadedMemo(null, true);
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

			// PCから閲覧できるようにする
			Uri textUri = Uri.fromFile(textFile);
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, textUri);
			PLApplication.getContext().sendBroadcast(mediaScanIntent);
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public boolean loadFromFile(String name) {
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

	public void loadedMemo(String name, boolean savePreferences) {
		if (name == null) {
			// 新規ファイルの読み込み
			name = newFileName();
		}
		mCurrentName = name;
		if (savePreferences) {
			SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
			editor.putString(KEY_LAST_NAME, name);
			editor.commit();
		}
	}

	public boolean deleteCurrentFile() {
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.remove(KEY_LAST_NAME);
		editor.commit();

		File file = getCurrentFile();
		return file.delete();
	}

	public boolean renameCurrentFile(String title) {
		// 未作成ファイルの可能性があるため保存
		saveToFile();

		String nextName = title + ".txt";
		File nextFile = new File(getDirectoryPath(), nextName);
		File currentFile = getCurrentFile();
		loadedMemo(nextName, true);
		return currentFile.renameTo(nextFile);
	}

	private String newFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return format.format(Calendar.getInstance().getTime()) + ".txt";
	}

	private String getDirectoryPath() {
		return PLApplication.appRootPath() + "memo/";
	}

	public String getCurrentName() {
		return mCurrentName;
	}

	private File getCurrentFile() {
		return new File(getDirectoryPath(), mCurrentName);
	}
}
