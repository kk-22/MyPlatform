package jp.co.my.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.my.myplatform.core.PLApplication;

public class MYLogUtil {
	private static final String LOG_FILE_NAME = "my_log.txt";
	private static final String TOAST_TAG = "toastLog";
	private static Context sContext;
	private static boolean sIsWriteLog;
	private static Handler sUiHandler;

	private MYLogUtil() {}

	public static void initLogUtil(Context context, boolean isWriteLog) {
		MYLogUtil.sContext = context;
		MYLogUtil.sIsWriteLog = isWriteLog;
		sUiHandler = new Handler();
	}

	/*
	Toast表示
	 */
	public static void showToast(String text) {
		MYLogUtil.showShortToast(text);
	}

	public static void showLongToast(String text) {
		outputLog(text);
		showToastOnUIThread(text, Toast.LENGTH_LONG);
	}

	public static void showShortToast(String text) {
		outputLog(text);
		showToastOnUIThread(text, Toast.LENGTH_SHORT);
	}

	public static void showErrorToast(String text) {
		text = "Error:" + text;
		outputErrorLog(text);
		showToastOnUIThread(text, Toast.LENGTH_LONG);
	}

	public static void showExceptionToast(Exception exception) {
		exception.printStackTrace();
		String text = exception.getMessage();
		if (text == null) {
			text = "no error message in exception";
		}
		outputErrorLog(text);
		showToastOnUIThread(text, Toast.LENGTH_LONG);
	}

	private static void showToastOnUIThread(final String text, final int duration) {
		MYOtherUtil.runOnUiThread(sContext, sUiHandler, new Runnable() {
			@Override
			public void run() {
				Toast.makeText(sContext, text, duration).show();
			}
		});
	}

	/*
	ログ出力
	 */
	public static void outputLog(String text) {
		Log.i(TOAST_TAG, text);
		writeLogFile(text);
	}

	public static void outputErrorLog(String text) {
		Log.e(TOAST_TAG, text);
		writeLogFile(text);
	}

	// デバッグ用
	public static void dummyLine() {
		outputLog("dummy line");
	}

	/*
	ログファイル操作
	 */
	private static void writeLogFile(String text) {
		if (!sIsWriteLog) {
			return;
		}
		SimpleDateFormat dataFormat = new SimpleDateFormat("MM/dd HH:mm:ss ");
		String entryStr = dataFormat.format(new Date()) + text +"\n";
		try {
			File file = getLogFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file, true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"Shift-JIS");
			PrintWriter writer = new PrintWriter(new BufferedWriter(outputStreamWriter));
			writer.write(entryStr);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public static boolean openLogFile() {
		File textFile = getLogFile();
		if (!textFile.exists()) {
			MYLogUtil.showToast("ログファイルなし");
			return false;
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(textFile));
			StringBuilder stringBuilder = new StringBuilder();
			String lineStr;
			while((lineStr = reader.readLine()) != null){
				stringBuilder.append(lineStr).append("\n");
			}
			reader.close();

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
			sContext.startActivity(intent);
			return true;
		} catch (Exception e) {
			showExceptionToast(e);
			return false;
		}
	}

	private static File getLogFile() {
		return new File(PLApplication.appRootPath(), LOG_FILE_NAME);
	}

	public static void deleteLogFile() {
		getLogFile().delete();
	}

	public static SharedPreferences getPreference() {
		return sContext.getSharedPreferences("preference", Context.MODE_PRIVATE);
	}

	public static SharedPreferences.Editor getPreferenceEditor() {
		return getPreference().edit();
	}


	public static SharedPreferences.Editor saveObject(String key, Object object) {
		return saveObject(key, object, false);
	}

	public static SharedPreferences.Editor saveObject(String key, Object object, boolean willApply) {
		Gson gson = new Gson();
		String json = gson.toJson(object);
		SharedPreferences.Editor editor = getPreferenceEditor();
		editor.putString(key, json);
		if (willApply) {
			editor.apply();
		}
		return editor;
	}

	public static <T> T loadObject(String key, Class<T> klass) {
		Gson gson = new Gson();
		String userSettingString = getPreference().getString(key, "");
		return gson.fromJson(userSettingString, klass);
	}

	public static <T> MYArrayList<T> loadArrayList(String key) {
		Type collectionType = new TypeToken<MYArrayList<T>>(){}.getType();
		String json = getPreference().getString(key, "");
		Gson gson = new Gson();
		return gson.fromJson(json, collectionType);
	}
}
