package jp.co.my.common.util;

import android.content.Context;
import android.os.Handler;


public class MYOtherUtil {

	private MYOtherUtil() {}

	// Activityクラスの同名メソッドのコピー
	public static void runOnUiThread(Context context, Handler handler, Runnable action) {
		if (Thread.currentThread().equals(context.getMainLooper().getThread())) {
			action.run();
		} else {
			handler.post(action);
		}
	}

	// 型チェックとキャスト
	// equals メソッド内ではワーニングを抑えるために、このメソッドを使わずに getClass() でクラス判定すること
	@SuppressWarnings("unchecked") // instanceof 比較ができないことで発生する型未チェック警告を抑制
	public static <T> T castObject(Object object, Class<T> klass) {
		if (object != null && (object.getClass() == klass)) {
			return (T) object;
		}
		return null;
	}
}
