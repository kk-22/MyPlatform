package jp.co.my.myplatform.mysen.userinterface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.mysen.PLMSArgument;
import jp.co.my.myplatform.mysen.PLMSLandView;
import jp.co.my.myplatform.mysen.PLMSUnitView;
import jp.co.my.myplatform.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.mysen.battle.PLMSBaseForecast;
import jp.co.my.myplatform.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.mysen.battle.PLMSSupportUnit;
import jp.co.my.myplatform.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.mysen.land.PLMSRouteArray;
import jp.co.my.myplatform.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.mysen.unit.PLMSUnitInterface;

public class PLMSComputerInterface extends PLMSWarInterface {

	private HashMap<PLMSLandView, MYArrayList<PLMSUnitView>> mEnemyRangeHashMap;

	public PLMSComputerInterface(PLMSArgument argument, PLMSArmyStrategy armyStrategy) {
		super(argument, armyStrategy);
	}

	@Override
	public void enableInterface() {
		super.enableInterface();
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
				// 補助スキル無い方が先
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
		super.disableInterface();
	}

	private void scanNextAction() {
		if (!mIsEnable) {
			// 既に終了済み
			return;
		}

		MYLogUtil.outputLog("scanNextAction");
		updateEnemyRange();
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
		PLMSBaseForecast bestForecast = null;
		int bestScore = Integer.MIN_VALUE;
		MYArrayList<PLMSUnitView> attackerUnitArray = new MYArrayList<>(); // 攻撃が可能なユニット
		for (PLMSUnitView actionUnitView : mTargetArmy.getAliveUnitViewArray()) {
			if (actionUnitView.isAlreadyAction()) {
				continue;
			}
			MYArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(actionUnitView, false);
			// 戦闘予測取得
			PLMSBaseForecast highestForecast = null;
			int highestScore = Integer.MIN_VALUE;
			MYArrayList<PLMSLandView> attackableLandArray = mAreaManager.getAttackableLandArray(movableLandArray, actionUnitView, false);
			for (PLMSLandView targetLandView : attackableLandArray) {
				PLMSUnitView targetUnitView = targetLandView.getUnitView();
				if (targetUnitView == null) {
					continue;
				}
				MYArrayList<PLMSLandView> attackLandArray = mAreaManager.getAttackLandArrayToTarget(targetUnitView, actionUnitView);
				MYArrayList<PLMSLandView> moveLandArray = movableLandArray.filterByArray(attackLandArray);
				for (PLMSLandView moveLandView : moveLandArray) {
					// バトルのスコア
					PLMSBattleForecast battleForecast = new PLMSBattleForecast(actionUnitView, moveLandView, targetUnitView, targetLandView);
					int score = calculateBattleScore(battleForecast);
					if (highestScore < score) {
						highestScore = score;
						highestForecast = battleForecast;
					}
				}
			}
			if (highestForecast != null) {
				attackerUnitArray.addIfNoContain(actionUnitView);
				if (highestScore > 0) {
					// TODO:回復補助スキル持ちなら、敵を倒せる場合以外は回復使用？
					// 撃破されず、一定値以上の与ダメがある戦闘を持つなら補助スキルを使用しない
					if (bestScore < highestScore) {
						bestScore = highestScore;
						bestForecast = highestForecast;
					}
					continue;
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
						// 補助のスコア
						PLMSSupportForecast supportForecast = new PLMSSupportForecast(actionUnitView, moveLandView, targetUnitView, targetLandView);
						int score = calculateSupportScore(supportForecast, attackerUnitArray);
						if (highestScore < score) {
							highestScore = score;
							highestForecast = supportForecast;
						}
					}
				}
			}
			if (bestScore < highestScore) {
				bestScore = highestScore;
				bestForecast = highestForecast;
			}
		}
		if (bestForecast != null) {
			moveUnitIfNeeded(bestForecast);
			return true;
		}
		// 戦闘・補助行動なし
		return false;
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
				int score = calculateBattleScore(battleForecast);
				if (highestScore < score) {
					highestScore = score;
					targetUnitView = enemyUnitView;
				}
			}
//			MYLogUtil.outputLog("move=" +moveUnitView.getUnitName() + " target=" +targetUnitView.getUnitName());

			// ルートを探す
			HashMap<PLMSLandView, PLMSRouteArray> routeArrayHashMap = mAreaManager.getAllRouteArrayHashMap(moveUnitView);
			PLMSLandView destinationLandView = filterDestinationLandView(moveUnitView, targetUnitView, routeArrayHashMap);
			if (destinationLandView == null) {
				// 移動不可
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
//		int selfDamage = battleUnit.getUnitData().getCurrentHP() - battleUnit.getRemainingHP();
		int enemyDamage = enemyUnit.getUnitData().getCurrentHP() - enemyUnit.getRemainingHP();
		int score = 100000000; // 補助より優先させるための初期値
		if (enemyUnit.getRemainingHP() <= 0) {
			// 倒せるなら優先
			score += 40000;
		} else if (battleUnit.getRemainingHP() <= 0) {
			// 倒されるのは避ける
			score += -40000;
		}

		if (enemyDamage <= 5) {
			// ダメージが低過ぎなら避ける
			score += -20000;
		}
		// 高ダメージを優先
		score += enemyDamage * 100;

		if (battleUnit.getLandView() != null) {
			// 移動を考慮した点数計算
			// 敵の攻撃範囲外を優先
			int enemyCount = enemyUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray().size();
			score += 10 * (enemyCount - getAttackedCountOfLand(battleUnit.getLandView()));

			// 移動しない地点を優先
			if (battleUnit.getLandView().equals(battleUnit.getUnitView().getLandView())) {
				score += 1;
			}
		}

		return score;
	}

	private int calculateSupportScore(PLMSSupportForecast supportForecast,
									  MYArrayList<PLMSUnitView> attackerUnitArray) {
		int score = 0;
		PLMSSupportUnit actionUnit = supportForecast.getLeftUnit();
		PLMSSupportUnit targetUnit = supportForecast.getRightUnit();
		PLMSLandView targetCurrentLandView = targetUnit.getUnitView().getLandView();
		boolean isAlreadyAction = targetUnit.getUnitView().isAlreadyAction(); // 対象が行動済みか
		boolean isAttacker = attackerUnitArray.contains(targetUnit.getUnitView()); // 対象が攻撃を行えるか

		PLMSSkillData supportSkill = actionUnit.getUnitData().getSupportSkillData();
		switch (supportSkill.getEffectType()) {
			case ONE_TURN_BUFF: {
				// 対象が攻撃予定か、移動済みかつ敵攻撃範囲内でなければ使用しない
				if (!isAttacker && (!isAlreadyAction || getAttackedCountOfLand(targetCurrentLandView) == 0)) {
					return Integer.MIN_VALUE;
				}
			}
			// 回復系
			case FLUCTUATE_HP:
			case SAINTS:
			case REVERSE:
			case DEDICATION: {
				// 回復値が大きいほど優先
				int healPoint = targetUnit.getRemainingHP() - targetUnit.getUnitView().getRemainingHP();
				score += healPoint * 100000;
				break;
			}
			case CHANGE_POSITION: {
				// TODO: 移動先の LandView が現状取得できない
				// TODO: この時点ではルート情報はないため、回り込みで狙う敵に近づけるか判定できない
				return Integer.MIN_VALUE;
				// 対象が移動済みでなければ使用しない
//				if (!isAlreadyAction) {
//					return Integer.MIN_VALUE;
//				}
//				// 被攻撃回数が減る場合にのみ使用
//				PLMSLandView targetPrevLand = targetUnit.getUnitView().getLandView();
//				PLMSLandView targetNextLand = targetUnit.getLandView();
//				int prevDefenceCount = mEnemyRangeHashMap.get(targetPrevLand).size();
//				int nextDefenceCount = mEnemyRangeHashMap.get(targetNextLand).size();
//				if (prevDefenceCount <= nextDefenceCount) {
//					return Integer.MIN_VALUE;
//				}
//				break;
			}
			case AGAIN_ACTION:
				break;
			case MUTUAL_ASSISTANCE:
			case IKKATU:
				break;
			default:
				MYLogUtil.showErrorToast("calculateSupportScore に未実装のスキル no=" +supportSkill.getEffectType().getInt() +" " +supportSkill.getSkillModel().getName());
				break;
		}
		score += targetUnit.getUnitData().getUnitScore() * 100;
		if (isAttacker) {
			// 攻撃よりも優先させる
			score += 200000000;
		}
		// 敵の攻撃範囲外を優先
		int enemyCount = actionUnit.getUnitData().getArmyStrategy().getEnemyArmy().getAliveUnitViewArray().size();
		score += 10 * (enemyCount - getAttackedCountOfLand(actionUnit.getLandView()));

		// 移動しない地点を優先
		if (actionUnit.getLandView().equals(actionUnit.getUnitView().getLandView())) {
			score += 1;
		}
//		MYLogUtil.outputLog(" unit=" +actionUnit.getUnitView().getUnitName() +" target=" +targetUnit.getUnitView().getUnitName() +" score=" +score);
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

					// 敵の攻撃範囲外を優先
					MYArrayList<PLMSUnitView> rangeUnitArray = mEnemyRangeHashMap.get(landView);
					score += -1000 * rangeUnitArray.size();

					// 斜め移動を優先
					Point landPoint = landView.getPoint();
					int diffX = MYMathUtil.difference(targetPoint.x, landPoint.x);
					int diffY = MYMathUtil.difference(targetPoint.y, landPoint.y);
					score += -10 * MYMathUtil.difference(diffX, diffY);

//					MYLogUtil.outputLog("move score=" +score +" " +landView.getTextPoint());
					if (score > highestScore) {
						highestScore = score;
						highestLandView = landView;
					}
				}
			}
		}
//		MYLogUtil.outputLog("move highestScore=" +highestScore+" " +highestLandView.getTextPoint());
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

	private void actionOfForecast(final PLMSBaseForecast forecast) {
		PLMSBattleForecast battleForecast = MYOtherUtil.castObject(forecast, PLMSBattleForecast.class);
		if (battleForecast != null) {
			mAnimationManager.addBattleAnimation(battleForecast);
			mAnimationManager.addAnimationCompletedRunnable(new Runnable() {
				@Override
				public void run() {
					PLMSUnitInterface leftUnit = forecast.getLeftUnit();
					leftUnit.getUnitView().didAction();
					scanNextActionIfNeeded();
				}
			});
			return;
		}
		PLMSSupportForecast supportForecast = MYOtherUtil.castObject(forecast, PLMSSupportForecast.class);
		PLMSSupportUnit supportUnit = supportForecast.getLeftUnit();
		PLMSSkillData supportSkill = supportUnit.getUnitData().getSupportSkillData();
		supportSkill.executeSupportSkill(supportForecast);
		mAnimationManager.sendTempAnimators();
		mAnimationManager.addAnimationCompletedRunnable(new Runnable() {
			@Override
			public void run() {
				PLMSUnitInterface leftUnit = forecast.getLeftUnit();
				leftUnit.getUnitView().didAction();
				scanNextActionIfNeeded();
			}
		});
	}

	private void updateEnemyRange() {
		MYArrayList<PLMSUnitView> enemyUnitArray = mTargetArmy.getEnemyArmy().getAliveUnitViewArray();
		int numberOfEnemy = enemyUnitArray.size();
		mEnemyRangeHashMap = new HashMap<>();
		for (PLMSLandView landView : mArgument.getFieldView().getLandViewArray()) {
			mEnemyRangeHashMap.put(landView, new MYArrayList<PLMSUnitView>(numberOfEnemy));
		}

		for (PLMSUnitView enemyUnitView : enemyUnitArray) {
			MYArrayList<PLMSLandView> attackableLandArray = mAreaManager.getAttackableLandArray(enemyUnitView);
			for (PLMSLandView landView : attackableLandArray) {
				MYArrayList<PLMSUnitView> rangeUnitArray = mEnemyRangeHashMap.get(landView);
				rangeUnitArray.add(enemyUnitView);
			}
		}
	}

	// 対象地点へ攻撃可能な敵数の取得
	private int getAttackedCountOfLand(PLMSLandView landView) {
		return mEnemyRangeHashMap.get(landView).size();
	}
}
