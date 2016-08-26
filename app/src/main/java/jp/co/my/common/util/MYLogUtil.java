package jp.co.my.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.my.mysupport.model.SUDeviceSetting;

/**
 * Created by kazuki on 2015/08/12.
 */
public class MYLogUtil {
	private static final String LOG_FILE_NAME = "MyLog";
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
			FileOutputStream fileOutputstream = sContext.openFileOutput(LOG_FILE_NAME, Context.MODE_PRIVATE | Context.MODE_APPEND);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputstream));
			writer.write(entryStr);
			writer.close();
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public static void displayLogDialog(Activity activity) {
		FileInputStream fileInputStream = null;
		BufferedReader reader = null;
		try {
			fileInputStream = sContext.openFileInput(LOG_FILE_NAME);
			reader= new BufferedReader(new InputStreamReader(fileInputStream,"UTF-8"));
			StringBuffer strBuffer = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				strBuffer.append(line);
				strBuffer.append("\n");
			}
			reader.close();

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
			alertDialogBuilder.setMessage(strBuffer.toString());
			alertDialogBuilder.create().show();
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public static void deleteLogFile() {
		sContext.deleteFile(LOG_FILE_NAME);
		SUDeviceSetting.clearStartCalendar();
	}
}
