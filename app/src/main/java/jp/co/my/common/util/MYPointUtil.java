package jp.co.my.common.util;

import android.graphics.Point;

public class MYPointUtil {

	public enum Direction {
		NONE, TOP, RIGHT, BOTTOM, LEFT,
	}

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

	// rootPoint から見た targetPoint の位置を返す
	// 左上を(0,0)とした座標にのみ使用可能
	public static Direction getDirection(Point rootPoint, Point targetPoint) {
		if (rootPoint.y < targetPoint.y) {
			return Direction.BOTTOM;
		} else if (rootPoint.y > targetPoint.y) {
			return Direction.TOP;
		} else if (rootPoint.x < targetPoint.x) {
			return Direction.RIGHT;
		} else if (rootPoint.x > targetPoint.x) {
			return Direction.LEFT;
		}
		return Direction.NONE;
	}

	// 縦横いずれかの軸が同じ
	public static boolean isEqualOneSide(Point point1, Point point2) {
		return  (point1.x == point2.x || point1.y == point2.y);
	}

	public static Point createWithDiff(Point basePoint, int diffX, int diffY) {
		return new Point(basePoint.x + diffX, basePoint.y + diffY);
	}

	public static Point createWithDirection(Point basePoint, Direction direction) {
		return createWithDirection(basePoint, direction, 1);
	}

	public static Point createWithDirection(Point basePoint, Direction direction, int diff) {
		switch (direction) {
			default:
			case NONE: return new Point(basePoint);
			case TOP: return createWithDiff(basePoint, 0, -diff);
			case RIGHT: return createWithDiff(basePoint, diff, 0);
			case BOTTOM: return createWithDiff(basePoint, 0, diff);
			case LEFT: return createWithDiff(basePoint, -diff, 0);
		}
	}
}
