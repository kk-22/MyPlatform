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

	// rootPoint から見て number 分動かした位置を取得
	public static Point getMovePoint(Point rootPoint, Point movePoint, int number) {
		if (number == 0) {
			return null;
		}
		int direction = getDirection(rootPoint, movePoint);
		switch (direction) {
			case 1: return new Point(movePoint.x, movePoint.y - number);
			case 2: return new Point(movePoint.x - number, movePoint.y);
			case 3: return new Point(movePoint.x, movePoint.y + number);
			case 4: return new Point(movePoint.x + number, movePoint.y);
			default: return null;
		}
	}

	// rootPoint から見た targetPoint の位置を返す
	// 1：上　2：右　3：下　4：左
	public static int getDirection(Point rootPoint, Point targetPoint) {
		if (rootPoint.y < targetPoint.y) {
			return 1;
		} else if (rootPoint.y > targetPoint.y) {
			return 3;
		} else if (rootPoint.x < targetPoint.x) {
			return 2;
		} else if (rootPoint.x > targetPoint.x) {
			return 4;
		}
		return 0;
	}
}
