package jp.co.my.common.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

import java.util.Calendar;

/**
 * Created by kazuki on 2016/05/04.
 */
public class MYViewUtil {

	private MYViewUtil() {}

	public static Point getDisplaySize(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point displaySize = new Point();
		windowManager.getDefaultDisplay().getSize(displaySize);
		return displaySize;
	}
}
