package jp.co.my.common.util;

import android.graphics.Point;

public class MYMathUtil {

	private MYMathUtil() {}

	public static int difference(int number1, int number2) {
		int abs1 = Math.abs(number1);
		int abs2 = Math.abs(number2);
		return Math.max(abs1, abs2) - Math.min(abs1, abs2);
	}

	public static int difference(Point point1, Point point2) {
		return difference(point1.x, point2.x) + difference(point1.y, point2.y);
	}
}
