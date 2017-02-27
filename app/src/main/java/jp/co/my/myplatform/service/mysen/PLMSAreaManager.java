package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import java.util.ArrayList;

import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_X;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_Y;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MIN_XY;

public class PLMSAreaManager {

	private static final int DO_NOT_ENTER = -99;

	private PLMSFieldView mField;
	private ArrayList<PLMSUnitView> mUnitArray;

	public PLMSAreaManager(PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mField = field;
		mUnitArray = unitArray;
	}

	public void showMoveAndAttackArea(PLMSUnitView unitView) {
		ArrayList<PLMSLandView> movableLandArray = getMovableLandArray(unitView);

		for (PLMSLandView landView : movableLandArray) {
			landView.getMoveAreaCover().showCoverView();
		}
		showAttackArea(movableLandArray, unitView);
	}

	public ArrayList<PLMSLandView> getMovableLandArray(PLMSUnitView unitView) {
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		ArrayList<PLMSLandView> movableLandArray = new ArrayList<>();
		searchAdjacentMovableArea(unitView.getCurrentPoint(), unitView, movementForce, movableLandArray);
		return movableLandArray;
	}

	public void hideAllMoveAndAttackArea() {
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.getMoveAreaCover().hideCoverView();
			landView.getAttackAreaCover().hideCoverView();
		}
	}

	private void showAttackArea(ArrayList<PLMSLandView> movableLandArray, PLMSUnitView unitView) {
		// 現在地も追加
		movableLandArray.add(unitView.getLandView());

		int range = unitView.getUnitData().getBranch().getAttackRange();
		for (PLMSLandView moveLandView : movableLandArray) {
			ArrayList<PLMSLandView> rangeLandArray = getAroundLandArray(moveLandView.getPoint(), range);
			for (PLMSLandView rangeLandView : rangeLandArray) {
				if (rangeLandView.getMoveAreaCover().isShowingCover()
						|| unitView.equals(rangeLandView.getUnitView())) {
					// 攻撃不可
					continue;
				}
				PLMSUnitView rangeUnitView = rangeLandView.getUnitView();
				if (rangeUnitView == null || unitView.isEnemy(rangeUnitView)) {
					rangeLandView.getAttackAreaCover().showCoverView();
				}
			}
		}
	}

	private void searchAdjacentMovableArea(Point point, PLMSUnitView unitView,
										   int remainingMove, ArrayList<PLMSLandView> movableLandArray) {
		ArrayList<PLMSLandView> adjacentLandArray = getAroundLandArray(point, 1);
		for (PLMSLandView adjacentLandView : adjacentLandArray) {
			searchMovableArea(unitView, adjacentLandView, remainingMove, movableLandArray);
		}
	}

	private void searchMovableArea(PLMSUnitView unitView, PLMSLandView landView,
								   int remainingMove, ArrayList<PLMSLandView> movableLandArray) {
		if (movableLandArray.contains(landView)) {
			// 直前のLand
			return;
		}
		int nextRemainingMove = getRemainingMoveCost(unitView, landView, remainingMove);
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		if (landView.getUnitView() == null) {
			movableLandArray.add(landView);
		}
		searchAdjacentMovableArea(landView.getPoint(), unitView, nextRemainingMove, movableLandArray);
	}

	// 移動不可の場合は負の値を返す
	private int getRemainingMoveCost(PLMSUnitView unitView, PLMSLandView landView, int remainingMove) {
		PLMSUnitView landUnitView = landView.getUnitView();
		if (landUnitView != null &&
				(landUnitView.equals(unitView) || landUnitView.isEnemy(unitView))) {
			// 移動不可
			return DO_NOT_ENTER;
		}
		int nextRemainingMove = remainingMove - unitView.getUnitData().moveCost(landView.getLandData());
		if (nextRemainingMove < 0) {
			// 移動不可
			return DO_NOT_ENTER;
		}
		return nextRemainingMove;
	}

	public ArrayList<PLMSLandView> getAroundLandArray(Point point, int range) {
		ArrayList<PLMSLandView> landArray = new ArrayList<>();
		// 上右下左 の順番
		ArrayList<Point> pointArray = new ArrayList<>();
		for (int i = -range; i <= range; i++) {
			int y = range - Math.abs(i);
			pointArray.add(new Point(point.x + i, point.y + y));
			if (y != 0) {
				pointArray.add(new Point(point.x + i, point.y + y * -1));
			}
		}
		for (Point targetPoint : pointArray) {
			int x = targetPoint.x;
			int y = targetPoint.y;
			if (MIN_XY <= x && x < MAX_X && MIN_XY <= y && y < MAX_Y) {
				PLMSLandView landView = mField.getLandViewForPoint(new Point(x, y));
				landArray.add(landView);
			}
		}
		return landArray;
	}

	public ArrayList<PLMSLandView> getRouteLandArray(PLMSUnitView unitView,
													 PLMSLandView targetLandView,
													 ArrayList<PLMSLandView> prevRouteLandArray) {
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		ArrayList<PLMSLandView> baseRouteLandArray = new ArrayList<>();
		baseRouteLandArray.add(unitView.getLandView());
		ArrayList<ArrayList<PLMSLandView>> resultLandArrays = new ArrayList<>();

		searchAdjacentRoute(unitView, targetLandView, unitView.getLandView(),
				movementForce, baseRouteLandArray, resultLandArrays);

		// TODO: 各ルートの長さとprevRouteLandArrayを使ってルート絞り込み
		return resultLandArrays.get(0);
	}

	private void searchAdjacentRoute(PLMSUnitView unitView, PLMSLandView targetLandView,
									 PLMSLandView focusLandView,
									 int remainingMove, ArrayList<PLMSLandView> focusRouteLandArray,
									 ArrayList<ArrayList<PLMSLandView>> resultLandArrays) {
		ArrayList<PLMSLandView> aroundLandArray = getAroundLandArray(focusLandView.getPoint(), 1);
		for (PLMSLandView aroundLand : aroundLandArray) {
			ArrayList<PLMSLandView> copyFocusRouteLandArray = new ArrayList<>(focusRouteLandArray);
			searchRoute(unitView, targetLandView, aroundLand, remainingMove, copyFocusRouteLandArray, resultLandArrays);
		}
	}

	private void searchRoute(PLMSUnitView unitView, PLMSLandView targetLandView,
							 PLMSLandView focusLandView,
							 int remainingMove, ArrayList<PLMSLandView> focusRouteLandArray,
							 ArrayList<ArrayList<PLMSLandView>> resultLandArrays) {
		if (focusRouteLandArray.contains(focusLandView)) {
			// 直前のLand
			return;
		}
		int nextRemainingMove = getRemainingMoveCost(unitView, focusLandView, remainingMove);
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		focusRouteLandArray.add(focusLandView);
		if (focusLandView.equals(targetLandView)) {
			// 目標に到達
			resultLandArrays.add(focusRouteLandArray);
			return;
		}
		searchAdjacentRoute(unitView, targetLandView, focusLandView, nextRemainingMove, focusRouteLandArray, resultLandArrays);
	}
}
