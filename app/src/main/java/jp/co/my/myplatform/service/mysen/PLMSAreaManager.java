package jp.co.my.myplatform.service.mysen;

import android.graphics.Color;
import android.graphics.Point;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.land.PLMSColorCover;
import jp.co.my.myplatform.service.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.land.PLMSRouteArray;
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
	private PLMSColorCover mSupportAreaCover; // 補助スキル対象マス
	private PLMSRouteCover mRouteCover; // 初期位置と仮位置

	public PLMSAreaManager(PLMSArgument argument) {
		mArgument = argument;

		mBlockLandArray = new MYArrayList<>();
		mWarpLandArray = new MYArrayList<>();
		mAvailableAreaCover = new PLMSColorCover(0); // Army 毎に色をセット
		mMoveAreaCover = new PLMSColorCover(Color.argb(128, 0, 0, 255));
		mAttackAreaCover = new PLMSColorCover(Color.argb(128, 255, 0, 0));
		mSupportAreaCover = new PLMSColorCover(Color.argb(255, 0, 255, 0));
		mRouteCover = new PLMSRouteCover();
	}

	public void hideAllAreaCover() {
		mMoveAreaCover.hideAllCoverViews();
		mAttackAreaCover.hideAllCoverViews();
		mSupportAreaCover.hideAllCoverViews();
		mRouteCover.hideAllCoverViews();
	}

	public boolean canAttackToLandView(PLMSLandView landView) {
		return (mAttackAreaCover.isShowingCover(landView) && landView.getUnitView() != null);
	}

	public void showAvailableArea(PLMSArmyStrategy armyStrategy) {
		MYArrayList<PLMSLandView> availableLandViewArray = new MYArrayList<>();
		for (PLMSUnitView unitView : armyStrategy.getAliveUnitViewArray()) {
			if (!unitView.isAlreadyAction()) {
				availableLandViewArray.add(unitView.getLandView());
			}
		}
		mAvailableAreaCover.changeColor(armyStrategy.getAvailableAreaColor());
		mAvailableAreaCover.showCoverViews(availableLandViewArray);
	}

	public void showActionArea(PLMSUnitView unitView) {
		MYArrayList<PLMSLandView> movableLandArray = getMovableLandArray(unitView);
		mMoveAreaCover.showCoverViews(movableLandArray);

		MYArrayList<PLMSLandView> attackableLandArray = getAttackableLandArray(movableLandArray, unitView);
		mAttackAreaCover.showCoverViews(attackableLandArray);

		MYArrayList<PLMSLandView> supportableLandArray = getSupportableLandArray(movableLandArray, unitView);
		mSupportAreaCover.showCoverViews(supportableLandArray);
	}

	public MYArrayList<PLMSLandView> getMovableLandArray(PLMSUnitView unitView) {
		initLandArrayBySkill(unitView);
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		MYArrayList<PLMSLandView> movableLandArray = new MYArrayList<>();
		searchAdjacentMovableArea(unitView.getCurrentPoint(), unitView, movementForce, movableLandArray);

		movableLandArray.addAllOnlyNoContain(mWarpLandArray);

		return movableLandArray;
	}

	// 移動範囲と味方ユニットがいる地点を除く攻撃範囲の取得
	public MYArrayList<PLMSLandView> getAttackableLandArray(MYArrayList<PLMSLandView> movableLandArray, PLMSUnitView unitView) {
		MYArrayList<PLMSLandView> resultArray = new MYArrayList<>();

		// 現在地も追加
		movableLandArray.add(unitView.getLandView());

		int range = unitView.getUnitData().getBranch().getAttackRange();
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

	public MYArrayList<PLMSLandView> getSupportableLandArray(MYArrayList<PLMSLandView> movableLandArray, PLMSUnitView moveUnitView) {
		PLMSUnitData unitData = moveUnitView.getUnitData();
		PLMSSkillData supportSkill = unitData.getSupportSkillData();
		if (!supportSkill.isAvailable()) {
			return null;
		}

		int range = supportSkill.getSkillModel().getTargetRange();
		MYArrayList<PLMSLandView> resultArray = new MYArrayList<>();
		movableLandArray.add(moveUnitView.getLandView()); // 現在地も追加
		for (PLMSUnitView teamUnitView : unitData.getArmyStrategy().getAliveUnitViewArray(moveUnitView)) {
			PLMSLandView teamLandView = teamUnitView.getLandView();
			Point teamPoint = teamLandView.getPoint();
			MYArrayList<PLMSLandView> rangeLandArray = getAroundLandArray(teamPoint, range);
			for (PLMSLandView rangeLandView : rangeLandArray) {
				if (!movableLandArray.contains(rangeLandView)) {
					// 移動不可マス
					continue;
				}
				if (supportSkill.canExecuteSupportSkill(moveUnitView, rangeLandView, teamUnitView)) {
					resultArray.addIfNoContain(teamLandView);
				}
			}
		}
		return resultArray;
	}

	// 全攻撃地点の取得
	public MYArrayList<PLMSLandView> getAttackLandArrayToTarget(PLMSUnitView targetUnitView,
																PLMSUnitView attackerUnitView) {
		int range = attackerUnitView.getUnitData().getBranch().getAttackRange();
		Point targetPoint = targetUnitView.getLandView().getPoint();
		return getAroundLandArray(targetPoint, range);
	}

	// スキルが発動可能な地点の取得
	public MYArrayList<PLMSLandView> getSupportLandArrayToTarget(PLMSUnitView targetUnitView,
																 PLMSUnitView skillUnitView) {
		MYArrayList<PLMSLandView> resultArray = new MYArrayList<>();
		PLMSLandView targetLandView = targetUnitView.getLandView();
		PLMSSkillData supportSkill = skillUnitView.getUnitData().getSupportSkillData();
		int range = supportSkill.getSkillModel().getTargetRange();

		for (PLMSLandView rangeLandView : getAroundLandArray(targetLandView.getPoint(), range)) {
			if (supportSkill.canExecuteSupportSkill(skillUnitView, rangeLandView, targetUnitView)) {
				resultArray.add(rangeLandView);
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

	public PLMSLandRoute getRouteOfUnit(PLMSUnitView unitView,
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

		if (resultRouteArray.size() == 0 && mWarpLandArray.contains(targetLandView)) {
			// ワープによる移動
			return new PLMSLandRoute(targetLandView);
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

	public PLMSRouteArray[][] getAllRouteArrays(PLMSUnitView movingUnitView) {
		MYLogUtil.outputLog("start");
		initLandArrayBySkill(movingUnitView);
		isSlipMove = true; // 敵を通過するルートも含む

		PLMSRouteArray[][] allRouteArrays = new PLMSRouteArray[MAX_X][MAX_Y];
		for (int i = 0; i < MAX_X; i++) {
			for (int j = 0; j < MAX_Y; j++) {
				allRouteArrays[i][j] = new PLMSRouteArray();
			}
		}

		PLMSLandView currentLandView = movingUnitView.getLandView();
		Point currentPoint = currentLandView.getPoint();
		PLMSRouteArray currentRoute = allRouteArrays[currentPoint.x][currentPoint.y];
		currentRoute.add(new PLMSLandRoute(currentLandView));
		currentRoute.didSearch();
		// TODO:ワープ＋移動によるルートも初期位置と同様の形で追加（ただしターン数の初期値は2）
		// TODO:ワープ先の個数＋１をsearchedCountにセット

		int range = 1;
		int searchedCount = 1; // 現在位置、ワープ先地点分を初期値に設定
		int maxSearchedCount = MAX_Y * MAX_X;
		while (searchedCount < maxSearchedCount) {
			MYLogUtil.outputLog("loop start range="+ range +" searchedCount=" +searchedCount);
			// 現在位置から広がるように順に検索
			MYArrayList<PLMSLandView> rangeLandArray = getAroundLandArray(currentPoint, range);
			for (PLMSLandView focusLandView : rangeLandArray) {
				Point focusPoint = focusLandView.getPoint();
				PLMSRouteArray focusRouteArray = allRouteArrays[focusPoint.x][focusPoint.y];

				// フォーカス位置に移動するための出発地点を探す
				MYArrayList<PLMSLandView> aroundLandArray = getAroundLandArray(focusPoint, 1);
				for (PLMSLandView aroundLandView : aroundLandArray) {
					Point aroundPoint = aroundLandView.getPoint();
					PLMSRouteArray aroundRouteArray = allRouteArrays[aroundPoint.x][aroundPoint.y];
					if (aroundRouteArray.size() == 0) {
						// ルートが無い地点からは出発できない
						continue;
					}
					searchAllRoute(movingUnitView, focusLandView, focusRouteArray, aroundRouteArray, allRouteArrays);
				}
				MYLogUtil.outputLog(" searched count=" +searchedCount);
				focusRouteArray.didSearch();
				searchedCount++;
//				if (searchedCount >= maxSearchedCount) {
//					break;
//				}
			}
			MYLogUtil.outputLog("loop end range="+ range +" searchedCount=" +searchedCount);
			range++;
		}
		MYLogUtil.outputLog("end");
		return allRouteArrays;
	}

	// TODO:旧サーチ処理と共通化できないか
	private void searchAllRoute(PLMSUnitView movingUnitView,
								PLMSLandView focusLandView,
								PLMSRouteArray focusRouteArray,
								PLMSRouteArray prevRouteArray,
								PLMSRouteArray[][] allRouteArrays) {
		MYLogUtil.outputLog(" searchAllRoute start");
		// コスト判定
		PLMSLandRoute prevRoute = prevRouteArray.getFirst();
		int numberOfTurn = prevRoute.getNumberOfTurn(); // この位置に移動するのに必要なターン数
		int remainingPower = prevRoute.getRemainingMovementPower();
		int nextRemainingMove = getRemainingMoveCost(movingUnitView, focusLandView, remainingPower);
		if (nextRemainingMove < 0) {
			int movementForce = movingUnitView.getUnitData().getBranch().getMovementForce();
			nextRemainingMove = getRemainingMoveCost(movingUnitView, focusLandView, movementForce);
			if (nextRemainingMove < 0) {
				// 移動不可の地形
				return;
			}
			numberOfTurn++;
		}

		// 探索済みルート判定
		PLMSLandRoute oldRoute = focusRouteArray.getFirst();
		if (oldRoute != null) {
			if (oldRoute.getNumberOfTurn() < numberOfTurn) {
				// 最短ルートではない
				return;
			} else if (oldRoute.getNumberOfTurn() > numberOfTurn) {
				// 最短ルートが出たため既存のルートを破棄
				focusRouteArray.clear();
			}
		}
		for (PLMSLandRoute landRoute : prevRouteArray) {
			PLMSLandRoute copyRoute = (PLMSLandRoute) landRoute.clone();
			copyRoute.add(focusLandView);
			copyRoute.setRemainingMovementPower(remainingPower);
			copyRoute.setNumberOfTurn(numberOfTurn);
			focusRouteArray.add(copyRoute);
		}
		searchAdjacentAllRoute(movingUnitView, focusLandView, focusRouteArray, allRouteArrays);
		MYLogUtil.outputLog(" searchAllRoute end");
	}

	private void searchAdjacentAllRoute(PLMSUnitView movingUnitView,
										PLMSLandView focusLandView,
										PLMSRouteArray focusRouteArray,
										PLMSRouteArray[][] allRouteArrays) {
		MYLogUtil.outputLog(" searchAdjacentAllRoute start");
		MYArrayList<PLMSLandView> aroundLandArray = getAroundLandArray(focusLandView.getPoint(), 1);
		for (PLMSLandView aroundLand : aroundLandArray) {
			Point focusPoint = focusLandView.getPoint();
			PLMSRouteArray routeArray = allRouteArrays[focusPoint.x][focusPoint.y];
			if (!routeArray.isAlreadySearched()) {
				// 未探索地点を追い続けることで起きる無駄な迂回ルートの大量生成を避ける
				continue;
			}
			if (routeArray == focusRouteArray) {
				// 直前の 位置
				continue;
			}
			searchAllRoute(movingUnitView, aroundLand, routeArray, focusRouteArray, allRouteArrays);
		}
		MYLogUtil.outputLog(" searchAdjacentAllRoute end");
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
		//  TODO: 生存ユニットのみスキル発動
		for (PLMSUnitView unitView : mArgument.getAllUnitViewArray()) {
			for (PLMSSkillData skillData : unitView.getUnitData().getPassiveSkillArray()) {
				skillData.executeMovementSkill(unitView, moveUnitView);
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

	public PLMSColorCover getSupportAreaCover() {
		return mSupportAreaCover;
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
