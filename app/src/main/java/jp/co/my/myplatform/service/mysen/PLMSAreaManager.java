package jp.co.my.myplatform.service.mysen;

import android.graphics.Color;
import android.graphics.Point;

import java.util.ArrayList;

import jp.co.my.myplatform.service.mysen.Land.PLMSColorCover;
import jp.co.my.myplatform.service.mysen.Land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.Land.PLMSRouteCover;

import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_X;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_Y;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MIN_XY;

public class PLMSAreaManager {

	private static final int DO_NOT_ENTER = -99;

	private PLMSFieldView mField;
	private ArrayList<PLMSUnitView> mUnitArray;

	private PLMSColorCover mMoveAreaCover;                    // 移動可能マス
	private PLMSColorCover mAttackAreaCover;                // 攻撃可能マス
	private PLMSRouteCover mRouteCover;                        // 初期位置と仮位置

	public PLMSAreaManager(PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mField = field;
		mUnitArray = unitArray;

		mMoveAreaCover = new PLMSColorCover(Color.argb(128, 0, 0, 255));
		mAttackAreaCover = new PLMSColorCover(Color.argb(128, 255, 0, 0));
		mRouteCover = new PLMSRouteCover();
	}

	public void showMoveAndAttackArea(PLMSUnitView unitView) {
		ArrayList<PLMSLandView> movableLandArray = getMovableLandArray(unitView);
		mMoveAreaCover.showCoverViews(movableLandArray);

		ArrayList<PLMSLandView> attackableLandArray = getAttackableLandArray(movableLandArray, unitView);
		mAttackAreaCover.showCoverViews(attackableLandArray);
	}

	public ArrayList<PLMSLandView> getMovableLandArray(PLMSUnitView unitView) {
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		ArrayList<PLMSLandView> movableLandArray = new ArrayList<>();
		searchAdjacentMovableArea(unitView.getCurrentPoint(), unitView, movementForce, movableLandArray);
		return movableLandArray;
	}

	public void hideAllAreaCover() {
		mMoveAreaCover.hideCoverViews();
		mAttackAreaCover.hideCoverViews();
		mRouteCover.hideCoverViews();
	}

	private ArrayList<PLMSLandView> getAttackableLandArray(ArrayList<PLMSLandView> movableLandArray, PLMSUnitView unitView) {
		ArrayList<PLMSLandView> resultArray = new ArrayList<>();

		// 現在地も追加
		movableLandArray.add(unitView.getLandView());

		int range = unitView.getUnitData().getWeapon().getAttackRange();
		for (PLMSLandView moveLandView : movableLandArray) {
			ArrayList<PLMSLandView> rangeLandArray = getAroundLandArray(moveLandView.getPoint(), range);
			for (PLMSLandView rangeLandView : rangeLandArray) {
				PLMSUnitView rangeUnitView = rangeLandView.getUnitView();
				if (movableLandArray.contains(rangeLandView) || unitView.equals(rangeUnitView)) {
					// 攻撃不可
					continue;
				}
				if (resultArray.contains(rangeLandView)) {
					// 登録済み
					continue;
				}
				if (rangeUnitView == null || unitView.isEnemy(rangeUnitView)) {
					resultArray.add(rangeLandView);
				}
			}
		}
		return resultArray;
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
		int nextRemainingMove = getRemainingMoveCost(unitView, landView, remainingMove);
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		if (landView.getUnitView() == null && !movableLandArray.contains(landView)) {
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

	public PLMSLandRoute showRouteArea(PLMSUnitView unitView,
									   PLMSLandView targetLandView,
									   PLMSLandRoute prevRoute) {
		PLMSLandRoute landRoute = getRouteOfUnit(unitView, targetLandView, prevRoute);
		showRouteArea(landRoute);
		return landRoute;
	}

	public PLMSLandRoute showRouteArea(PLMSLandRoute landRoute) {
		mRouteCover.hideCoverViews();
		if (landRoute != null) {
			mRouteCover.showCoverViews(landRoute);
		}
		return landRoute;
	}

	private PLMSLandRoute getRouteOfUnit(PLMSUnitView unitView,
										 PLMSLandView targetLandView,
										 PLMSLandRoute prevRoute) {
		if (unitView.getLandView().equals(targetLandView)) {
			// 移動の必要なし
			return new PLMSLandRoute(targetLandView);
		}

		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		PLMSLandRoute baseRoute = new PLMSLandRoute(unitView.getLandView());
		ArrayList<PLMSLandRoute> resultRouteArray = new ArrayList<>();
		searchAdjacentRoute(unitView, targetLandView, unitView.getLandView(),
				movementForce, baseRoute, resultRouteArray);

		// 前のルートと同じ経路に絞り込む
		ArrayList<PLMSLandRoute> candidateRouteArray = resultRouteArray;
		if (prevRoute != null) {
			for (int i = 0; i < prevRoute.size(); i++) {
				ArrayList<PLMSLandRoute> filteredArray =
						filterRouteArray(candidateRouteArray, prevRoute, i);
				if (filteredArray.size() == 0) {
					break;
				}
				candidateRouteArray = filteredArray;
			}
		}

		// 最短ルートに絞り込む
		int shortestSize = 99;
		PLMSLandRoute shortestRoute = null;
		for (PLMSLandRoute landRoute : candidateRouteArray) {
			int size = landRoute.size();
			if (size < shortestSize) {
				shortestSize = size;
				shortestRoute = landRoute;
			}
		}
		return shortestRoute;
	}

	private void searchAdjacentRoute(PLMSUnitView unitView, PLMSLandView targetLandView,
									 PLMSLandView focusLandView,
									 int remainingMove, PLMSLandRoute focusRoute,
									 ArrayList<PLMSLandRoute> resultRouteArray) {
		ArrayList<PLMSLandView> aroundLandArray = getAroundLandArray(focusLandView.getPoint(), 1);
		for (PLMSLandView aroundLand : aroundLandArray) {
			PLMSLandRoute copyFocusRoute = (PLMSLandRoute) focusRoute.clone();
			searchRoute(unitView, targetLandView, aroundLand, remainingMove, copyFocusRoute, resultRouteArray);
		}
	}

	private void searchRoute(PLMSUnitView unitView, PLMSLandView targetLandView,
							 PLMSLandView focusLandView,
							 int remainingMove, PLMSLandRoute focusRoute,
							 ArrayList<PLMSLandRoute> resultRouteArray) {
		if (focusRoute.contains(focusLandView)) {
			// 直前のLand
			return;
		}
		int nextRemainingMove = getRemainingMoveCost(unitView, focusLandView, remainingMove);
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		focusRoute.add(focusLandView);
		if (focusLandView.equals(targetLandView)) {
			// 目標に到達
			resultRouteArray.add(focusRoute);
			return;
		}
		searchAdjacentRoute(unitView, targetLandView, focusLandView, nextRemainingMove, focusRoute, resultRouteArray);
	}

	private ArrayList<PLMSLandRoute> filterRouteArray(ArrayList<PLMSLandRoute> baseRouteArray,
													  PLMSLandRoute needRoute,
													  int index) {
		ArrayList<PLMSLandRoute> filteredArray = new ArrayList<>();
		PLMSLandView needLandView = needRoute.get(index);
		for (PLMSLandRoute landRoute : baseRouteArray) {
			if (index < landRoute.size() && needLandView.equals(landRoute.get(index))) {
				filteredArray.add(landRoute);
			}
		}
		return filteredArray;
	}

	public boolean canAttackToLandView(PLMSLandView landView) {
		return (mAttackAreaCover.isShowingCover(landView) && landView.getUnitView() != null);
	}

	// getter and setter
	public PLMSColorCover getMoveAreaCover() {
		return mMoveAreaCover;
	}

	public PLMSColorCover getAttackAreaCover() {
		return mAttackAreaCover;
	}

	public PLMSRouteCover getRouteCover() {
		return mRouteCover;
	}
}
