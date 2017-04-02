package jp.co.my.myplatform.service.mysen;

import android.graphics.Color;
import android.graphics.Point;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.land.PLMSColorCover;
import jp.co.my.myplatform.service.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.land.PLMSRouteCover;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_X;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_Y;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MIN_XY;

public class PLMSAreaManager {

	private static final int DO_NOT_ENTER = -99;

	private PLMSArgument mArgument;
	private PLMSFieldView mField;

	private boolean isSlipMove; // すり抜け移動フラグ
	private MYArrayList<PLMSLandView> mBlockLandArray; // スキル進軍阻止対象の LandView
	private MYArrayList<PLMSLandView> mWarpLandArray; // スキルによりワープ可能な LandView

	private PLMSColorCover mAvailableAreaCover; // 操作可能ユニットの配置マス
	private PLMSColorCover mMoveAreaCover; // 移動可能マス
	private PLMSColorCover mAttackAreaCover; // 攻撃可能マス
	private PLMSRouteCover mRouteCover; // 初期位置と仮位置

	public PLMSAreaManager(PLMSArgument argument) {
		mArgument = argument;

		mBlockLandArray = new MYArrayList<>();
		mWarpLandArray = new MYArrayList<>();
		mAvailableAreaCover = new PLMSColorCover(0); // Army 毎に色をセット
		mMoveAreaCover = new PLMSColorCover(Color.argb(128, 0, 0, 255));
		mAttackAreaCover = new PLMSColorCover(Color.argb(128, 255, 0, 0));
		mRouteCover = new PLMSRouteCover();
	}

	public void hideAllAreaCover() {
		mMoveAreaCover.hideAllCoverViews();
		mAttackAreaCover.hideAllCoverViews();
		mRouteCover.hideAllCoverViews();
	}

	public boolean canAttackToLandView(PLMSLandView landView) {
		return (mAttackAreaCover.isShowingCover(landView) && landView.getUnitView() != null);
	}

	public void showAvailableArea(PLMSArmyStrategy armyStrategy) {
		MYArrayList<PLMSLandView> availableLandViewArray = new MYArrayList<>();
		for (PLMSUnitView unitView : armyStrategy.getUnitViewArray()) {
			availableLandViewArray.add(unitView.getLandView());
		}
		mAvailableAreaCover.changeColor(armyStrategy.getAvailableAreaColor());
		mAvailableAreaCover.showCoverViews(availableLandViewArray);
	}

	public void showMoveAndAttackArea(PLMSUnitView unitView) {
		MYArrayList<PLMSLandView> movableLandArray = getMovableLandArray(unitView);
		mMoveAreaCover.showCoverViews(movableLandArray);

		MYArrayList<PLMSLandView> attackableLandArray = getAttackableLandArray(movableLandArray, unitView);
		mAttackAreaCover.showCoverViews(attackableLandArray);
	}

	public MYArrayList<PLMSLandView> getMovableLandArray(PLMSUnitView unitView) {
		initLandArrayBySkill(unitView);
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		MYArrayList<PLMSLandView> movableLandArray = new MYArrayList<>();
		searchAdjacentMovableArea(unitView.getCurrentPoint(), unitView, movementForce, movableLandArray);

		movableLandArray.addAllOnlyNoContain(mWarpLandArray);

		return movableLandArray;
	}

	private MYArrayList<PLMSLandView> getAttackableLandArray(MYArrayList<PLMSLandView> movableLandArray, PLMSUnitView unitView) {
		MYArrayList<PLMSLandView> resultArray = new MYArrayList<>();

		// 現在地も追加
		movableLandArray.add(unitView.getLandView());

		int range = unitView.getUnitData().getWeapon().getAttackRange();
		for (PLMSLandView moveLandView : movableLandArray) {
			MYArrayList<PLMSLandView> rangeLandArray = getAroundLandArray(moveLandView.getPoint(), range);
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
										   int remainingMove, MYArrayList<PLMSLandView> movableLandArray) {
		MYArrayList<PLMSLandView> adjacentLandArray = getAroundLandArray(point, 1);
		for (PLMSLandView adjacentLandView : adjacentLandArray) {
			searchMovableArea(unitView, adjacentLandView, remainingMove, movableLandArray);
		}
	}

	private void searchMovableArea(PLMSUnitView unitView, PLMSLandView landView,
								   int remainingMove, MYArrayList<PLMSLandView> movableLandArray) {
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

	public PLMSLandRoute showRouteArea(PLMSUnitView unitView,
									   PLMSLandView targetLandView,
									   PLMSLandRoute prevRoute) {
		PLMSLandRoute landRoute = getRouteOfUnit(unitView, targetLandView, prevRoute);
		showRouteArea(landRoute);
		return landRoute;
	}

	public PLMSLandRoute showRouteArea(PLMSLandRoute landRoute) {
		mRouteCover.hideAllCoverViews();
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

		initLandArrayBySkill(unitView);
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		PLMSLandRoute baseRoute = new PLMSLandRoute(unitView.getLandView());
		MYArrayList<PLMSLandRoute> resultRouteArray = new MYArrayList<>();
		searchAdjacentRoute(unitView, targetLandView, unitView.getLandView(),
				movementForce, baseRoute, resultRouteArray);

		// ワープ移動先の追加
		for (PLMSLandView landView : mWarpLandArray) {
			PLMSLandRoute route = new PLMSLandRoute(landView);
			resultRouteArray.addIfNoContain(route);
		}

		// 前のルートと同じ経路に絞り込む
		MYArrayList<PLMSLandRoute> candidateRouteArray = resultRouteArray;
		if (prevRoute != null) {
			for (int i = 0; i < prevRoute.size(); i++) {
				MYArrayList<PLMSLandRoute> filteredArray =
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
									 MYArrayList<PLMSLandRoute> resultRouteArray) {
		MYArrayList<PLMSLandView> aroundLandArray = getAroundLandArray(focusLandView.getPoint(), 1);
		for (PLMSLandView aroundLand : aroundLandArray) {
			PLMSLandRoute copyFocusRoute = (PLMSLandRoute) focusRoute.clone();
			searchRoute(unitView, targetLandView, aroundLand, remainingMove, copyFocusRoute, resultRouteArray);
		}
	}

	private void searchRoute(PLMSUnitView unitView, PLMSLandView targetLandView,
							 PLMSLandView focusLandView,
							 int remainingMove, PLMSLandRoute focusRoute,
							 MYArrayList<PLMSLandRoute> resultRouteArray) {
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

	private MYArrayList<PLMSLandRoute> filterRouteArray(MYArrayList<PLMSLandRoute> baseRouteArray,
													  PLMSLandRoute needRoute,
													  int index) {
		MYArrayList<PLMSLandRoute> filteredArray = new MYArrayList<>();
		PLMSLandView needLandView = needRoute.get(index);
		for (PLMSLandRoute landRoute : baseRouteArray) {
			if (index < landRoute.size() && needLandView.equals(landRoute.get(index))) {
				filteredArray.add(landRoute);
			}
		}
		return filteredArray;
	}

	public MYArrayList<PLMSLandView> getAroundLandArray(Point point, int range) {
		MYArrayList<PLMSLandView> landArray = new MYArrayList<>();
		// 上右下左 の順番
		MYArrayList<Point> pointArray = new MYArrayList<>();
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

	// 移動不可の場合は負の値を返す
	private int getRemainingMoveCost(PLMSUnitView unitView, PLMSLandView landView, int remainingMove) {
		PLMSUnitView landUnitView = landView.getUnitView();
		if (unitView.equals(landUnitView) || (!isSlipMove && unitView.isEnemy(landUnitView))) {
			// 移動不可
			return DO_NOT_ENTER;
		}
		int nextRemainingMove = remainingMove - unitView.getUnitData().moveCost(landView.getLandData());
		if (nextRemainingMove < 0) {
			// 移動不可
			return DO_NOT_ENTER;
		}
		if (!isSlipMove && mBlockLandArray.contains(landView)) {
			// スキルによりそれ以上先に進ませない
			return 0;
		}
		return nextRemainingMove;
	}

	private void initLandArrayBySkill(PLMSUnitView moveUnitView) {
		isSlipMove = false;
		mBlockLandArray.clear();
		mWarpLandArray.clear();
		for (PLMSUnitView unitView : mArgument.getAllUnitViewArray()) {
			for (PLMSSkillData skillData : unitView.getUnitData().getPassiveSkillArray()) {
				skillData.executeMoveSkill(unitView, moveUnitView);
			}
		}
	}

	public void addBlockUnitView(PLMSUnitView unitView) {
		mBlockLandArray.addAll(getAroundLandArray(unitView.getLandView().getPoint(), 1));
	}

	public void addWarpUnitView(PLMSUnitView targetUnitView, PLMSUnitView moveUnitView) {
		if (targetUnitView.equals(moveUnitView)) {
			return;
		}

		MYArrayList<PLMSLandView> landViewArray = getAroundLandArray(targetUnitView.getLandView().getPoint(), 1);
		int moveCost = moveUnitView.getUnitData().getBranch().getMovementForce();
		for (PLMSLandView landView : landViewArray) {
			if (getRemainingMoveCost(moveUnitView, landView, moveCost) >= 0) {
				mWarpLandArray.add(landView);
			}
		}
	}

	// getter and setter
	public PLMSColorCover getAvailableAreaCover() {
		return mAvailableAreaCover;
	}

	public PLMSColorCover getMoveAreaCover() {
		return mMoveAreaCover;
	}

	public PLMSColorCover getAttackAreaCover() {
		return mAttackAreaCover;
	}

	public PLMSRouteCover getRouteCover() {
		return mRouteCover;
	}

	public void setField(PLMSFieldView field) {
		mField = field;
	}

	public void setSlipMove(boolean slipMove) {
		isSlipMove = slipMove;
	}
}
