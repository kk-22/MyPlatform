package jp.co.my.myplatform.service.mysen.userinterface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.common.util.MYPointUtil;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.battle.PLMSBaseForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.service.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.land.PLMSRouteArray;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

public class PLMSComputerInterface extends PLMSWarInterface {

	public PLMSComputerInterface(PLMSArgument argument, PLMSArmyStrategy armyStrategy) {
		super(argument, armyStrategy);
	}

	@Override
	public void enableInterface() {
		// 移動順にソート
		Collections.sort(mTargetArmy.getAliveUnitViewArray(), new Comparator<PLMSUnitView>() {
			@Override
			public int compare(PLMSUnitView unitView1, PLMSUnitView unitView2) {
				// 近接ユニットは先
				int rangeDiff = unitView1.getUnitData().getBranch().getAttackRange()
						- unitView2.getUnitData().getBranch().getAttackRange();
				if (rangeDiff != 0) {
					return rangeDiff;
				}
				// 補助スキル無い方は先
				boolean isAvailable1 = unitView1.getUnitData().getSupportSkillData().isAvailable();
				boolean isAvailable2 = unitView2.getUnitData().getSupportSkillData().isAvailable();
				if (isAvailable1 == isAvailable2) {
					return 0;
				}
				return (isAvailable1) ? 1 : -1;
			}
		});

		scanNextAction();
	}

	@Override
	public void disableInterface() {
	}

	private void scanNextAction() {
		if (scanBattleAndSupport()) {
			return;
		} else if (scanMovement()) {
			return;
		}
		mArgument.getTurnManager().finishTurn();
	}

	private void scanNextActionIfNeeded() {
		if (!mArgument.getTurnManager().finishTurnIfNecessary()) {
			scanNextAction();
		}
	}

	private boolean scanBattleAndSupport() {
		MYArrayList<PLMSBattleForecast> allBattleForecast = new MYArrayList<>();
		MYArrayList<PLMSSupportForecast> allSupportForecast = new MYArrayList<>();
		for (PLMSUnitView actionUnitView : mTargetArmy.getAliveUnitViewArray()) {
			if (actionUnitView.isAlreadyAction()) {
				continue;
			}
			MYArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(actionUnitView, false);
			// 戦闘予測取得
			MYArrayList<PLMSLandView> attackableLandArray = mAreaManager.getAttackableLandArray(movableLandArray, actionUnitView, false);
			for (PLMSLandView targetLandView : attackableLandArray) {
				PLMSUnitView targetUnitView = targetLandView.getUnitView();
				if (targetUnitView == null) {
					continue;
				}
				MYArrayList<PLMSLandView> attackLandArray = mAreaManager.getAttackLandArrayToTarget(targetUnitView, actionUnitView);
				MYArrayList<PLMSLandView> moveLandArray = movableLandArray.filterByArray(attackLandArray);
				for (PLMSLandView moveLandView : moveLandArray) {
					allBattleForecast.add(new PLMSBattleForecast(actionUnitView, moveLandView, targetUnitView, targetLandView));
				}
			}

			// 補助予測取得
			MYArrayList<PLMSLandView> supportableLandArray = mAreaManager.getSupportableLandArray(movableLandArray, actionUnitView);
			if (supportableLandArray != null) {
				for (PLMSLandView targetLandView : supportableLandArray) {
					PLMSUnitView targetUnitView = targetLandView.getUnitView();
					MYArrayList<PLMSLandView> supportLandArray = mAreaManager.getSupportLandArrayToTarget(targetUnitView, actionUnitView);
					MYArrayList<PLMSLandView> moveLandArray = movableLandArray.filterByArray(supportLandArray);
					for (PLMSLandView moveLandView : moveLandArray) {
						allSupportForecast.add(new PLMSSupportForecast(actionUnitView, moveLandView, targetUnitView, targetLandView));
					}
				}
			}
		}
		if (allBattleForecast.size() == 0) {
			return false;
		}

		// 最適な行動の選択
		MYArrayList<PLMSBattleForecast> highestForecastArray = new MYArrayList<>();
		int highestScore = Integer.MIN_VALUE;
		for (PLMSBattleForecast battleForecast : allBattleForecast) {
			int score = calculateBattleScore(battleForecast);

			if (highestScore == score) {
				highestForecastArray.add(battleForecast);
			} else if (highestScore < score) {
				highestScore = score;
				highestForecastArray.clear();
				highestForecastArray.add(battleForecast);
			}
		}
		moveUnitIfNeeded(highestForecastArray.getFirst());
		return true;
	}

	private boolean scanMovement() {
		// TODO:近距離・補助スキルなしユニットを優先的に移動
		// 移動するユニットを探す
		for (PLMSUnitView moveUnitView : mTargetArmy.getAliveUnitViewArray()) {
			if (moveUnitView.isAlreadyAction()) {
				continue;
			}
			// 狙う対象を探す
			PLMSUnitView targetUnitView = null;
			int highestScore = Integer.MIN_VALUE;
			for (PLMSUnitView enemyUnitView : mTargetArmy.getEnemyArmy().getAliveUnitViewArray()) {
				PLMSBattleForecast battleForecast = new PLMSBattleForecast(moveUnitView, null, enemyUnitView, null);
				int score = calculateBattleScore(battleForecast);
				if (highestScore < score) {
					highestScore = score;
					targetUnitView = enemyUnitView;
				}
			}

			// ルートを探す
			HashMap<PLMSLandView, PLMSRouteArray> routeArrayHashMap = mAreaManager.getAllRouteArrayHashMap(moveUnitView);
			PLMSLandView destinationLandView = filterDestinationLandView(moveUnitView, targetUnitView, routeArrayHashMap);
			if (destinationLandView == null) {
				// TODO:エラーではないため、動作に問題がなければトーストを消す
				MYLogUtil.showErrorToast("destination is null. unit=" +moveUnitView +" target=" +targetUnitView);
				continue;
			}
			// 移動
			moveUnit(moveUnitView, destinationLandView);
			return true;
		}
		return false;
	}

	private int calculateBattleScore(PLMSBattleForecast battleForecast) {
		PLMSBattleUnit battleUnit = battleForecast.getLeftUnit();
		PLMSBattleUnit enemyUnit = battleForecast.getRightUnit();
		int selfDamage = battleUnit.getUnitData().getCurrentHP() - battleUnit.getRemainingHP();
		int enemyDamage = enemyUnit.getUnitData().getCurrentHP() - enemyUnit.getRemainingHP();
		int score = enemyDamage - selfDamage;
		if (enemyUnit.getRemainingHP() <= 0) {
			score += 10000;
		} else if (battleUnit.getRemainingHP() <= 0) {
			score -= 10000;
		}
		return score;
	}

	// ルート情報から移動先の PLMSLandView を絞り込む
	private PLMSLandView filterDestinationLandView(PLMSUnitView movingUnitView,
												   PLMSUnitView targetUnitView,
												   HashMap<PLMSLandView, PLMSRouteArray> routeArrayHashMap) {
		MYArrayList<PLMSLandView> attackLandArray = mAreaManager.getAttackLandArrayToTarget(targetUnitView, movingUnitView);
		MYArrayList<PLMSRouteArray> shortestRouteArrays = new MYArrayList<>(attackLandArray.size());
		int shortestNum = Integer.MAX_VALUE;
		for (PLMSLandView attackLandView : attackLandArray) {
			PLMSRouteArray routeArray = routeArrayHashMap.get(attackLandView);
			PLMSLandRoute landRoute = routeArray.getFirst();
			if (landRoute == null || landRoute.size() > shortestNum) {
				continue;
			}
			if (landRoute.size() < shortestNum) {
				shortestNum = landRoute.size();
				shortestRouteArrays.clear();
			}
			shortestRouteArrays.add(routeArray);
		}

		PLMSLandView targetLandView = targetUnitView.getLandView();
		Point targetPoint = targetLandView.getPoint();
		PLMSLandView highestLandView = null;
		int highestScore = Integer.MIN_VALUE;
		for (PLMSRouteArray routeArray : shortestRouteArrays) {
			for (PLMSLandRoute landRoute : routeArray) {
				int oneTurnIndex = landRoute.getOneTurnIndex();
				for (int i = 1; i <= oneTurnIndex; i++) {
					PLMSLandView landView = landRoute.get(i);
					PLMSUnitView landUnitView = landView.getUnitView();
					if (landUnitView != null) {
						if (landUnitView.isEnemy(movingUnitView)) {
							// TODO: すり抜け判定
							// 敵ならそこで移動停止
							break;
						} else {
							// 味方なら通過
							continue;
						}
					}
					// 1ターン目での移動先から離れている程マイナス
					int score = -10000 * (oneTurnIndex - i);
					if (MYPointUtil.isEqualOneSide(targetPoint, landView.getPoint())) {
						score += 100;
					}
					// TODO:敵の攻撃範囲外を優先
					if (score > highestScore) {
						highestScore = score;
						highestLandView = landView;
					}
				}
			}
		}
		return highestLandView;
	}

	private void moveUnit(final PLMSUnitView moveUnitView, final PLMSLandView moveLandView) {
		Animator animator = mAnimationManager.getMovementAnimation(moveUnitView, moveUnitView.getLandView(), moveLandView);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				moveUnitView.moveToLand(moveLandView);
				scanNextActionIfNeeded();
			}
		});
		mAnimationManager.addAnimator(animator);

		moveUnitView.standby();
	}

	private void moveUnitIfNeeded(final PLMSBaseForecast forecast) {
		PLMSUnitInterface forecastUnit = forecast.getLeftUnit();
		final PLMSUnitView moveUnitView = forecastUnit.getUnitView();
		PLMSLandView currentLandView = moveUnitView.getLandView();
		final PLMSLandView moveLandView = forecastUnit.getLandView();
		if (moveLandView.equals(currentLandView)) {
			actionOfForecast(forecast);
			return;
		}
		Animator animator = mAnimationManager.getMovementAnimation(moveUnitView, currentLandView, moveLandView);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				moveUnitView.moveToLand(moveLandView);
				actionOfForecast(forecast);
			}
		});
		mAnimationManager.addAnimator(animator);
	}

	private void actionOfForecast(PLMSBaseForecast forecast) {
		final PLMSBattleForecast battleForecast = MYOtherUtil.castObject(forecast, PLMSBattleForecast.class);
		mAnimationManager.addBattleAnimation(battleForecast);
		mAnimationManager.addAnimationCompletedRunnable(new Runnable() {
			@Override
			public void run() {
				PLMSUnitInterface leftUnit = battleForecast.getLeftUnit();
				leftUnit.getUnitView().didAction();
				scanNextActionIfNeeded();
			}
		});
	}
}
