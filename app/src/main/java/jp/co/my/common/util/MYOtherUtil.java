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
	public static <T> T castObject(Object object, Class<T> klass) {
		if ((object.getClass() == klass)) {
			return (T) object;
		}
		return null;
	}
}
