package jp.co.my.myplatform.service.mysen.userinterface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
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

	private boolean scanBattleAndSupport() {
		MYArrayList<PLMSBattleForecast> allBattleForecast = new MYArrayList<>();
		MYArrayList<PLMSSupportForecast> allSupportForecast = new MYArrayList<>();
		for (PLMSUnitView actionUnitView : mTargetArmy.getAliveUnitViewArray()) {
			if (actionUnitView.isAlreadyAction()) {
				continue;
			}
			MYArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(actionUnitView);
			// 戦闘予測取得
			MYArrayList<PLMSLandView> attackableLandArray = mAreaManager.getAttackableLandArray(movableLandArray, actionUnitView);
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
			int score = calculateScore(battleForecast);

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
				int score = calculateScore(battleForecast);
				if (highestScore < score) {
					highestScore = score;
					targetUnitView = enemyUnitView;
				}
			}

			// ルートを探す
			PLMSRouteArray[][] allRouteArrays = mAreaManager.getAllRouteArrays(moveUnitView);
			MYArrayList<PLMSLandView> attackLandArray = mAreaManager.getAttackLandArrayToTarget(targetUnitView, moveUnitView);
			PLMSLandRoute bestRoute = null;
//			for (PLMSLandView attackLandView : attackLandArray) {
//				// TODO: 他の敵はすり抜けるようにルートを取得
//				// TODO: movementForceは使用しない
//				// TODO: 検索がループしないようにする
//				if (bestRoute == null || route.size() < bestRoute.size()) {
//					/*
//					TODO: ルートにより優先度
//					優先度上げる：敵の上を通らない、斜めに移動
//					優先度下げる：敵の攻撃範囲に入る
//					 */
//					bestRoute = route;
//				}
//			}
			// TODO: delete debug code
			bestRoute = allRouteArrays[5][3].getFirst();

			if (bestRoute == null) {
				MYLogUtil.showErrorToast("bestRoute is null");
				continue;
			}
			int movementForce = moveUnitView.getUnitData().getBranch().getMovementForce();
			PLMSLandView moveLandView = null; // このターンでの移動先
			for (int i = 1; i <= movementForce && i < bestRoute.size(); i++) {
				// TODO:移動コスト計算していない。allRouteArraysのLandRouteのnumberOfTurnが使えないか？
				PLMSLandView routeLandView = bestRoute.get(i);
				PLMSUnitView landUnitView = routeLandView.getUnitView();
				if (landUnitView == null) {
					moveLandView = routeLandView;
				} else if (moveUnitView.isEnemy(landUnitView)) {
					// TODO: すり抜け持ちならreturnしない
					// 敵がいるためその先に移動不可
					break;
				}
			}
			if (moveLandView == null) {
				// 移動不可
				continue;
			}
			// 移動
			moveUnit(moveUnitView, moveLandView);
			return true;
		}
		return false;
	}

	private int calculateScore(PLMSBattleForecast battleForecast) {
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

	private void moveUnit(final PLMSUnitView moveUnitView, final PLMSLandView moveLandView) {
		Animator animator = mAnimationManager.getMovementAnimation(moveUnitView, moveUnitView.getLandView(), moveLandView);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				moveUnitView.moveToLand(moveLandView);
				scanNextAction();
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
				scanNextAction();
			}
		});
	}
}
