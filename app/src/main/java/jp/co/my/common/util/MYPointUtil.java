package jp.co.my.common.util;

import android.graphics.Point;

public class MYPointUtil {

	private MYPointUtil() {}

	// 直線を示す2つの Point 間の Point を返す
	public static MYArrayList<Point> getHalfwayPointArray(Point point1, Point point2) {
		MYArrayList<Point> resultArray = new MYArrayList<>();
		if (point1.x != point2.x) {
			int begin = Math.min(point1.x, point2.x);
			int end = Math.max(point1.x, point2.x);
			for (int i = begin + 1; i < end; i++) {
				resultArray.add(new Point(i, point1.y));
			}
		} else {
			int begin = Math.min(point1.y, point2.y);
			int end = Math.max(point1.y, point2.y);
			for (int i = begin + 1; i < end; i++) {
				resultArray.add(new Point(point1.x, i));
			}
		}
		return resultArray;
	}
}
