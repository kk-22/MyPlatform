package jp.co.my.common.util;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MYViewUtil {

	private MYViewUtil() {}

	public static Point getDisplaySize(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point displaySize = new Point();
		windowManager.getDefaultDisplay().getSize(displaySize);
		return displaySize;
	}

	public static boolean removeFromSuperView(View view) {
		ViewGroup parentView = (ViewGroup) view.getParent();
		if (parentView == null) {
			return false;
		}
		parentView.removeView(view);
		return true;
	}
}
