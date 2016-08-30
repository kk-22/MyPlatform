package jp.co.my.myplatform.service.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import jp.co.my.common.util.MYLogUtil;

public class PLDeviceSetting {

	private static final String KEY_DEFAULT_SCREEN_BRIGHTNESS = "DefaultScreenBrightness";
	// スクリーン明るさ
	public static final int MIN_SCREEN_BRIGHTNESS = 1;

	public static void revertAllSetting() {
		revertScreenBrightness();
	}

	// 画面明度最小に設定し、前の値を保存
	public static void setMinScreenBrightness() {
		int prevBrightness = setScreenBrightness(MIN_SCREEN_BRIGHTNESS);
		if (prevBrightness == MIN_SCREEN_BRIGHTNESS) {
			return;
		}
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putInt(KEY_DEFAULT_SCREEN_BRIGHTNESS, prevBrightness);
		editor.commit();
	}

	// 画面明度を元に戻す
	public static  void revertScreenBrightness() {
		Context context = PLApplication.getContext();
		int currentBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
		if (currentBrightness != MIN_SCREEN_BRIGHTNESS) {
			return;
		}
		SharedPreferences pref = MYLogUtil.getPreference();
		int defaultBrightness = pref.getInt(KEY_DEFAULT_SCREEN_BRIGHTNESS, MIN_SCREEN_BRIGHTNESS);
		setScreenBrightness(defaultBrightness);
	}

	// 画面明度を設定し、変更前の値を返す
	private static int setScreenBrightness(int brightness) {
		Context context = PLApplication.getContext();
		int prevBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
		if (prevBrightness != brightness) {
			// 自動明度設定をマニュアルに変更
			Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
		}
		return prevBrightness;
	}
}
