package jp.co.my.myplatform.service.mysen.unit;

import android.animation.Animator;
import android.graphics.Point;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.common.util.MYPointUtil;
import jp.co.my.myplatform.service.mysen.PLMSAnimationManager;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSBranchData;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.service.mysen.battle.PLMSForecastUnit;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportUnit;

public class PLMSSkillData {

	private PLMSArgument mArgument;
	private PLMSSkillModel mSkillModel;
	private TimingType mTimingType;
	private RequirementType mRequirementType;
	private TargetType mTargetType;
	private EffectType mEffectType;

	public PLMSSkillData(PLMSArgument argument, PLMSSkillModel skillModel) {
		mArgument = argument;
		mSkillModel = skillModel;

		if (skillModel == null) {
			return;
		}
		mTimingType = TimingType.getType(skillModel.getTimingType());
		mRequirementType = RequirementType.getType(skillModel.getRequirementType());
		mTargetType = TargetType.getType(skillModel.getTargetType());
		mEffectType = EffectType.getType(skillModel.getEffectType());
	}

	public void executeStartTurnSkill(PLMSUnitInterface skillUnit, int numberOfTurn) {
		if (mTimingType != TimingType.START_TURN) {
			return;
		}
		if (!canExecuteSkill(skillUnit)) {
			return;
		}
		if (mRequirementType == RequirementType.NUMBER_OF_TURN
				&& numberOfTurn % mSkillModel.getRequirementValue() != 1) {
			return;
		}

		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(skillUnit, null);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case FLUCTUATE_HP: {
				for (PLMSUnitInterface targetUnit : targetArray) {
					fluctuateHP(targetUnit, mSkillModel.getEffectValue());
				}
				break;
			}
			default:
				MYLogUtil.showErrorToast("executeStartTurnSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
	}

	public void executeStartBattleSkill(PLMSUnitInterface skillUnit,
										PLMSBattleUnit battleUnit, PLMSBattleForecast battleForecast) {
		if (mTimingType == null) {
			return;
		}
		boolean isAttacker = battleForecast.getLeftUnit().equals(battleUnit);
		boolean isBattleUnit = battleUnit.getUnitView().equals(skillUnit);
		switch (mTimingType) {
			case START_BATTLE: if (!isBattleUnit) {return;} break;
			case ATTACK_TO_ENEMY: if (!isBattleUnit || !isAttacker) {return;} break;
			case ATTACK_TO_ME: if (!isBattleUnit || isAttacker) {return;} break;
			case START_TEAM_BATTLE: if (isBattleUnit) {return;} break;
			default: return;
		}

		PLMSBattleUnit enemyUnit = battleUnit.getAnotherUnit();
		if (!canExecuteSkill(skillUnit)) {
			return;
		}
		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(skillUnit, battleForecast);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case BATTLE_BUFF: {
				if (!targetArray.contains(battleUnit.getUnitView())) {
					// 紋章の範囲外ならなにもしない
					break;
				}
				for (Integer paramNumber : getBuffParamNumberArray()) {
					battleUnit.addBattleBuffOfNo(paramNumber, mSkillModel.getEffectValue());
				}
				break;
			}
			case ALL_TURN_BATTLE_BUFF: {
				if (!targetArray.contains(battleUnit.getUnitView())) {
					break;
				}
				int battleBuff = 0;
				for (int turnBuff : battleUnit.getUnitData().getBuffParams()) {
					battleBuff += turnBuff;
				}
				for (Integer paramNumber : getBuffParamNumberArray()) {
					battleUnit.addBattleBuffOfNo(paramNumber, battleBuff);
				}
				break;
			}
			case CHASE_ATTACK:
				battleUnit.incrementChasePoint();
				break;
			case DISABLE_CHASE_ATTACK:
				battleUnit.decrementChasePoint();
				enemyUnit.decrementChasePoint();
				break;
			case KILL_WEAPON:
				battleUnit.incrementChasePoint();
				enemyUnit.decrementChasePoint();
				break;
			case THREE_WAY_INTENSIFICATION:
				battleForecast.setThreeWayRatio(mSkillModel.getEffectValue());
				break;
			case CHASE_ATTACK_IF_HAS_COUNTER:
			case CONTINUOUSLY_CHASE_ATTACK:
			case PREEMPTIVE_ATTACK:
			case ALL_RANGE_COUNTER:
				battleUnit.addSkill(this);
				break;
			case CONTINUOUSLY_ATTACK:
				battleUnit.setNumberOfConsecutiveAttack(mSkillModel.getEffectValue());
				break;
			default:
				MYLogUtil.showErrorToast("executeStartBattleSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
	}

	public void executeFinishBattleSkill(PLMSBattleUnit battleUnit, PLMSBattleForecast battleForecast) {
		boolean isAttacker = battleForecast.getLeftUnit().equals(battleUnit);
		if (mTimingType != TimingType.FINISH_BATTLE
				&& !(mTimingType == TimingType.FINISH_ATTACK_BATTLE && isAttacker)
				&& !(mTimingType == TimingType.FINISH_DEFENCE_BATTLE && !isAttacker)) {
			return;
		}

		PLMSUnitView unitView = battleUnit.getUnitView();
		if (!canExecuteSkill(unitView) || !battleUnit.isAlive()) {
			return;
		}
		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(battleUnit, battleForecast);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case FLUCTUATE_HP: {
				for (PLMSUnitInterface targetUnit : targetArray) {
					fluctuateHP(targetUnit, mSkillModel.getEffectValue());
				}
				break;
			}
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case CHANGE_POSITION: {
				moveUnit(battleUnit);
				break;
			}
			default:
				MYLogUtil.showErrorToast("executeFinishBattleSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
	}

	public void executeMovementSkill(PLMSUnitInterface skillUnit, PLMSUnitInterface moveUnit) {
		if (!(mTimingType == TimingType.MY_MOVEMENT && moveUnit == skillUnit)
				&& !(mTimingType == TimingType.ENEMY_MOVEMENT && skillUnit.getUnitView().isEnemy(moveUnit))) {
			return;
		}
		if (!canExecuteSkill(skillUnit)) {
			return;
		}

		switch (mEffectType) {
			case SLIP_MOVE:
				mArgument.getAreaManager().setSlipMove(true);
				break;
			case WARP_TO_TEAM:
				for (PLMSUnitView unitView : moveUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					mArgument.getAreaManager().addWarpUnitView(unitView, moveUnit.getUnitView());
				}
				break;
			case WARP_TO_TEAM_OF_LESS_HP:
				for (PLMSUnitView unitView : moveUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					if (getRemainingHPRatio(unitView) <= mSkillModel.getEffectValue()) {
						mArgument.getAreaManager().addWarpUnitView(unitView, moveUnit.getUnitView());
					}
				}
				break;
			case BLOCK_ENEMY_MOVE:
				mArgument.getAreaManager().addBlockUnitView(skillUnit.getUnitView());
				break;
			default:
				MYLogUtil.showErrorToast("executeMovementSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
	}

	// HP変動補助スキルの実行
	public void updateRemainingHPOfSupportUnit(PLMSSupportForecast forecast) {
		PLMSSupportUnit supportUnit = forecast.getLeftUnit();
		PLMSSupportUnit targetUnit = forecast.getRightUnit();
		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(supportUnit, null);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case FLUCTUATE_HP: {
				for (PLMSUnitInterface unitInterface : targetArray) {
					PLMSSupportUnit unit = MYOtherUtil.castObject(unitInterface, PLMSSupportUnit.class);
					unit.setDiffHP(mSkillModel.getEffectValue());
				}
				break;
			}
			case SAINTS: {
				int damage = supportUnit.getUnitData().getMaxHP() - supportUnit.getRemainingHP();
				supportUnit.setDiffHP(damage / 2);
				targetUnit.setDiffHP(damage + 7);
				break;
			}
			case REVERSE: {
				int maxHP = targetUnit.getUnitData().getMaxHP();
				int point = Math.max(0, (maxHP - targetUnit.getRemainingHP() * 2));
				targetUnit.setDiffHP(point + 7);
				break;
			}
			case DEDICATION:
				break;
			case MUTUAL_ASSISTANCE:
				break;
			default:
				// HP補助スキル以外
				break;
		}
		for (PLMSSkillData skillData : supportUnit.getUnitData().getPassiveSkillArray()) {
			if (skillData.mTimingType != TimingType.HEAL_BY_STAFF) {
				continue;
			}
			switch (skillData.mEffectType) {
				case HEAL_ME_TOO: {
					if (supportUnit.getDiffHP() != 0) {
						// 補助スキルにより自身も回復しているためスキルは発動しない
						break;
					}
					int targetDiffHP = targetUnit.getDiffHP();
					supportUnit.setDiffHP(targetDiffHP * skillData.getSkillModel().getEffectValue() / 100);
					break;
				}
				default:
					MYLogUtil.showErrorToast("updateRemainingHPOfSupportUnit に未実装のスキル no="
							+skillData.mEffectType.getInt() +" " +skillData.mSkillModel.getName());
					break;
			}
		}
	}

	//
	public void executeSupportSkill(PLMSSupportForecast forecast) {
		PLMSSupportUnit supportUnit = forecast.getLeftUnit();
		PLMSSupportUnit targetUnit = forecast.getRightUnit();
		if (supportUnit.getDiffHP() != 00 || targetUnit.getDiffHP() != 0) {
			// HP変動補助スキル
			int supportDiffHP = supportUnit.getDiffHP();
			int targetDiffHP = targetUnit.getDiffHP();
			if (supportDiffHP != 0) {
				fluctuateHP(supportUnit, supportDiffHP);
			}
			if (targetDiffHP != 0) {
				fluctuateHP(targetUnit, targetDiffHP);
			}
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(new MYArrayList<PLMSUnitInterface>(targetUnit));
				break;
			}
			case CHANGE_POSITION:
				moveUnit(supportUnit);
				break;
			case AGAIN_ACTION: {
				PLMSUnitView targetUnitView = targetUnit.getUnitView();
				targetUnitView.againAction();
				mArgument.getAreaManager().getAvailableAreaCover().showCoverView(targetUnit.getLandView());

				Animator animator = targetUnitView.makeCommonAnimator();
				mArgument.getAnimationManager().addTempAnimator(animator, PLMSAnimationManager.ANIMATOR_BUFF);
				break;
			}
			case IKKATU:
				break;
			default:
				MYLogUtil.showErrorToast("executeSupportSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
	}

	public boolean canExecuteSupportSkill(PLMSUnitView skillUnitView, PLMSLandView skillLandView,
										  PLMSUnitView targetUnitView) {
		PLMSUnitData targetData = targetUnitView.getUnitData();
		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				int value = mSkillModel.getEffectValue();
				for (Integer paramNumber : getBuffParamNumberArray()) {
					if (targetData.getBuffParameterOfNo(paramNumber) < value) {
						return true;
					}
				}
				return false;
			}
			case FLUCTUATE_HP:
			case SAINTS:
			case REVERSE:
			case DEDICATION:
				return (targetUnitView.getRemainingHP() < targetData.getMaxHP());
			case CHANGE_POSITION: {
				PLMSSupportUnit skillUnit = new PLMSSupportUnit(skillUnitView, skillLandView);
				PLMSLandView skillMoveLandView = getMoveLandView(skillUnit, targetUnitView, mSkillModel.getEffectValue());
				PLMSLandView targetMoveLandView = getMoveLandView(targetUnitView, skillUnit, mSkillModel.getEffectSubValue());
				return  ((mSkillModel.getEffectValue() == 0 || skillMoveLandView != null)
						&& (mSkillModel.getEffectSubValue() == 0 || targetMoveLandView != null));
			}
			case AGAIN_ACTION: {
				return  (targetUnitView.isAlreadyAction()
						&& targetData.getSupportSkillData().getEffectType() != EffectType.AGAIN_ACTION);
			}
			case MUTUAL_ASSISTANCE:
				break;
			case IKKATU:
				break;
			default:
				MYLogUtil.showErrorToast("canExecuteSupportSkill に未実装のスキル no=" +mEffectType.getInt() +" " +mSkillModel.getName());
				break;
		}
		return false;
	}

	private boolean canExecuteSkill(PLMSUnitInterface skillUnit) {
		if (mSkillModel == null) {
			return false;
		}
		switch (mRequirementType) {
			case NONE:
				return true;
			case LESS_THAN_MY_HP: {
				return getRemainingHPRatio(skillUnit) <= mSkillModel.getRequirementValue();
			}
			case GREATER_THAN_MY_HP: {
				return getRemainingHPRatio(skillUnit) >= mSkillModel.getRequirementValue();
			}
			default:
				// 各 execute メソッドで判定
				return true;
		}
	}

	private float getRemainingHPRatio(PLMSUnitInterface unitView) {
		PLMSUnitData unitData = unitView.getUnitData();
		return unitData.getCurrentHP() / unitData.getMaxHP() * 100;
	}

	// battleUnit : 戦闘を行った自身もしくは味方ユニット
	private MYArrayList<PLMSUnitInterface> getTargetUnitViewArray(PLMSUnitInterface skillUnit, PLMSBattleForecast battleForecast) {
		MYArrayList<PLMSUnitInterface> resultArray = new MYArrayList<>();
		switch (mTargetType) {
			case NONE:
				break;
			case SELF:
				resultArray.add(skillUnit);
				break;
			case TEAM_IN_RANGE: {
				PLMSArmyStrategy army = skillUnit.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(skillUnit, battleForecast, army.getAliveUnitViewArray());
				break;
			}
			case TARGET: {
				resultArray.add(skillUnit.getAnotherUnit());
				break;
			}
			case TARGET_AND_SELF: {
				resultArray.add(skillUnit);
				resultArray.add(skillUnit.getAnotherUnit());
				break;
			}
			case TEAM_OTHER_THAN_ME:
				break;
			case ENEMY: {
				PLMSBattleUnit enemyUnit = battleForecast.getBattleUnitOfUnitTeam(skillUnit).getAnotherUnit();
				if (!enemyUnit.isAlive()) {
					break;
				}
				resultArray.add(enemyUnit.getUnitView());
				break;
			}
			case ENEMY_IN_ENEMY_RANGE: {
				PLMSBattleUnit enemy = battleForecast.getBattleUnitOfUnitTeam(skillUnit).getAnotherUnit();
				PLMSArmyStrategy army = enemy.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(enemy, battleForecast, army.getAliveUnitViewArray());
				break;
			}
			case ENEMY_IN_MY_RANGE: {
				PLMSArmyStrategy army = skillUnit.getUnitData().getArmyStrategy().getEnemyArmy();
				resultArray = getInRangeUnitArray(skillUnit, battleForecast, army.getAliveUnitViewArray());
				break;
			}
		}
		int targetBranch = mSkillModel.getTargetBranch();
		if (targetBranch == 0) {
			return resultArray;
		}

		MYArrayList<PLMSUnitInterface> filteredArray = new MYArrayList<>();
		for (PLMSUnitInterface unitView : resultArray) {
			PLMSUnitData unitData = unitView.getUnitData();
			if ((unitData.getBranch().getNo() & targetBranch) != 0) {
				filteredArray.add(unitView);
			}
		}
		return filteredArray;
	}

	private MYArrayList<PLMSUnitInterface> getInRangeUnitArray(PLMSUnitInterface centerUnit,
															   PLMSBattleForecast battleForecast,
															   MYArrayList<PLMSUnitView> baseUnitArray) {
		Point centerPoint;
		if (battleForecast == null) {
			centerPoint = centerUnit.getLandView().getPoint();
		} else {
			// 戦闘時の UnitView と BattleUnit の位置の違いを吸収
			centerPoint= battleForecast.getUnitOfBattle(centerUnit).getLandView().getPoint();
		}

		MYArrayList<PLMSUnitInterface> resultArray = new MYArrayList<>(baseUnitArray.size());
		int range = mSkillModel.getTargetRange();
		for (PLMSUnitView unitView : baseUnitArray) {
			PLMSLandView unitLandView;
			if (battleForecast == null) {
				unitLandView = unitView.getLandView();
			} else {
				unitLandView = battleForecast.getUnitOfBattle(unitView).getLandView();
			}
			int difference = MYMathUtil.difference(centerPoint, unitLandView.getPoint());
			// 同じ位置は除く
			if (difference != 0 && difference <= range) {
				resultArray.add(unitView);
			}
		}
		return resultArray;
	}

	// ユニットの移動。片方のユニットしか移動しない場合は Point が null
	private void moveUnit(PLMSForecastUnit skillUnit) {
		PLMSForecastUnit targetUnit = skillUnit.getAnotherUnit();
		PLMSLandView skillLandView = getMoveLandView(skillUnit, targetUnit, mSkillModel.getEffectValue());
		PLMSLandView targetLandView = getMoveLandView(targetUnit, skillUnit, mSkillModel.getEffectSubValue());
		if ((skillLandView == null && mSkillModel.getEffectValue() != 0 && skillUnit.isAlive())
				|| (targetLandView == null && mSkillModel.getEffectSubValue() != 0 && targetUnit.isAlive())) {
			// 移動不可
			return;
		}

		PLMSAnimationManager animationManager = mArgument.getAnimationManager();
		MYArrayList<Animator> animatorArray = new MYArrayList<>();
		if (skillLandView != null) {
			PLMSUnitView skillUnitView = skillUnit.getUnitView();
			animatorArray.add(animationManager.getMovementAnimation(skillUnitView, skillUnit.getLandView(), skillLandView));
			skillUnitView.moveToLand(skillLandView);
		}
		if (targetLandView != null) {
			PLMSUnitView targetUnitView = targetUnit.getUnitView();
			mArgument.getAreaManager().getAvailableAreaCover().changeCover(targetUnitView.getLandView(), targetLandView);

			animatorArray.add(animationManager.getMovementAnimation(targetUnitView, targetUnit.getLandView(), targetLandView));
			targetUnitView.moveToLand(targetLandView);
		}
		animationManager.addTogetherAnimatorArray(animatorArray);
	}

	// スキルよる移動先の LandView を取得
	// ignoreUnit : 移動時に位置を無視する UnitView
	private PLMSLandView getMoveLandView(PLMSUnitInterface moveUnit, PLMSUnitInterface ignoreUnit, int moveValue) {
		do {
			PLMSLandView landView = getMoveLandViewOfValue(moveUnit, ignoreUnit, moveValue);
			if (landView != null) {
				return landView;
			}
			// 相手から離れる方向へ2マス以上移動するスキルの場合、1マス移動まで繰り返す
			moveValue++;
		} while (moveValue <= -1);
		return null;
	}

	// getMoveLandView メソッドからのみ呼ばれるメソッド
	private PLMSLandView getMoveLandViewOfValue(PLMSUnitInterface moveUnit, PLMSUnitInterface ignoreUnit, int moveValue) {
		if (moveValue == 0 || !moveUnit.isAlive()) {
			return null;
		}
		Point ignoreLandPoint = ignoreUnit.getLandView().getPoint();
		Point currentLandPoint = moveUnit.getLandView().getPoint();
		Point targetPoint = MYPointUtil.getMovePoint(ignoreLandPoint, currentLandPoint, moveValue);
		PLMSLandView targetLandView = mArgument.getFieldView().getLandViewForPoint(targetPoint);
		if (targetLandView == null) {
			// フィールド範囲外
			return null;
		}
		PLMSUnitData unitData = moveUnit.getUnitData();
		if (unitData.moveCost(targetLandView.getLandData()) >= 9) {
			// 侵入不可の地形
			return null;
		}
		PLMSUnitView landUnitView = targetLandView.getUnitView();
		PLMSUnitView ignoreUnitView = ignoreUnit.getUnitView();
		if (landUnitView != null && !ignoreUnitView.equals(landUnitView)) {
			// 他のユニットが移動先にいる
			return null;
		}
		PLMSLandView currentLandView = moveUnit.getLandView();
		MYArrayList<Point> halfwayPointArray = MYPointUtil.getHalfwayPointArray(
				currentLandView.getPoint(), targetLandView.getPoint());
		for (Point point : halfwayPointArray) {
			PLMSLandView landView = mArgument.getFieldView().getLandViewForPoint(point);
			PLMSUnitView unitView = landView.getUnitView();
			if (moveUnit.getUnitView().isEnemy(unitView) && !ignoreUnitView.equals(unitView)) {
				// 移動経路の途中に敵ユニット
				return null;
			}
		}
		return targetLandView;
	}

	private void setBuffToUnitArray(MYArrayList<PLMSUnitInterface> targetUnitArray) {
		MYArrayList<Integer> buffParamNumberArray = getBuffParamNumberArray();
		int value = mSkillModel.getEffectValue();
		for (PLMSUnitInterface targetUnit : targetUnitArray) {
			PLMSUnitData unitData = targetUnit.getUnitData();
			for (Integer paramNumber : buffParamNumberArray) {
				unitData.setBuffOfNo(paramNumber, value);
			}
			Animator animator = targetUnit.getUnitView().makeBuffAnimator(value);
			mArgument.getAnimationManager().addTempAnimator(animator, PLMSAnimationManager.ANIMATOR_BUFF);
		}
	}

	private MYArrayList<Integer> getBuffParamNumberArray() {
		MYArrayList<Integer> resultArray = new MYArrayList<>(PLMSUnitData.PARAMETER_NUMBER);
		int statusType = mSkillModel.getStatusType();
		if ((statusType & SKILL_ATTACK) != 0) {
			resultArray.add(PLMSUnitData.PARAMETER_ATTACK);
		}
		if ((statusType & SKILL_SPEED) != 0) {
			resultArray.add(PLMSUnitData.PARAMETER_SPEED);
		}
		if ((statusType & SKILL_DEFENSE) != 0) {
			resultArray.add(PLMSUnitData.PARAMETER_DEFENSE);
		}
		if ((statusType & SKILL_MAGIC_DEFENSE) != 0) {
			resultArray.add(PLMSUnitData.PARAMETER_MAGIC_DEFENSE);
		}
		return resultArray;
	}

	public void fluctuateHP(PLMSUnitInterface unit, int diffHP) {
		int currentHP = unit.getRemainingHP();
		int remainingHP = unit.getUnitData().calculateSkillRemainingHP(currentHP, diffHP);
		PLMSAnimationManager animationManager = mArgument.getAnimationManager();
		Animator animator = animationManager.getFluctuateHPAnimation(unit.getUnitView(), remainingHP, diffHP);
		animationManager.addTempAnimator(animator, PLMSAnimationManager.ANIMATOR_HP);
	}

	public boolean isWeaknessBranch(PLMSBattleUnit enemyUnit) {
		if (mSkillModel == null) {
			return false;
		}
		PLMSBranchData enemyBranch = enemyUnit.getUnitData().getBranch();
		return  ((mSkillModel.getWeaknessBranch() & enemyBranch.getNo()) != 0);
	}

	// getter
	public PLMSSkillModel getSkillModel() {
		return mSkillModel;
	}

	public EffectType getEffectType() {
		return mEffectType;
	}

	public boolean isAvailable() {
		return (mSkillModel != null);
	}

	private static final int SKILL_ATTACK = 0x0001;
	private static final int SKILL_SPEED = 0x0002;
	private static final int SKILL_DEFENSE = 0x0004;
	private static final int SKILL_MAGIC_DEFENSE = 0x0008;
	private static final int SKILL_HP = 0x0010;

	private enum TimingType {
		NONE(0),
		START_TURN(1),
		START_BATTLE(11), ATTACK_TO_ENEMY(12), ATTACK_TO_ME(13), START_TEAM_BATTLE(14),
		FINISH_BATTLE(16), FINISH_ATTACK_BATTLE(17), FINISH_DEFENCE_BATTLE(18),
		MY_MOVEMENT(21), ENEMY_MOVEMENT(22),
		SUPPORT_TIMING(31), HEAL_BY_STAFF(32);

		final int id;
		TimingType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private TimingType getType(int no) {
			TimingType[] types = TimingType.values();
			for (TimingType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の TimingType=" +no);
			return START_TURN;
		}
	}

	private enum RequirementType {
		NONE(0),
		LESS_THAN_MY_HP(1), GREATER_THAN_MY_HP(2),
		NUMBER_OF_TURN(11);

		final int id;
		RequirementType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private RequirementType getType(int no) {
			RequirementType[] types = RequirementType.values();
			for (RequirementType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の RequirementType=" +no);
			return NONE;
		}
	}

	private enum TargetType {
		NONE(0),
		SELF(1), TEAM_IN_RANGE(2), TEAM_OTHER_THAN_ME(3),
		TARGET(6), TARGET_AND_SELF(7),
		ENEMY(11), ENEMY_IN_ENEMY_RANGE(12), ENEMY_IN_MY_RANGE(13);

		final int id;
		TargetType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private TargetType getType(int no) {
			TargetType[] types = TargetType.values();
			for (TargetType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の TargetType=" +no);
			return NONE;
		}
	}

	public enum EffectType {
		NONE(0),
		ONE_TURN_BUFF(1), BATTLE_BUFF(2), ALL_TURN_BATTLE_BUFF(3),
		FLUCTUATE_HP(11), HEAL_ME_TOO(12),
		CHASE_ATTACK_IF_HAS_COUNTER(21), CHASE_ATTACK(22), CONTINUOUSLY_CHASE_ATTACK(23),
		DISABLE_CHASE_ATTACK(26), KILL_WEAPON(27), PREEMPTIVE_ATTACK(28), KAZANAGI(29),
		SLIP_MOVE(31),WARP_TO_TEAM(32), WARP_TO_TEAM_OF_LESS_HP(33), BLOCK_ENEMY_MOVE(36),
		THREE_WAY_INTENSIFICATION(41), ADD_ADVANTAGE_COLOR(42), ALL_RANGE_COUNTER(43), PROTECT_WEAKNESS_ATTACK(44),
		FLUCTUATE_SECRET_DAMAGE(46), CONTINUOUSLY_ATTACK(47),
		CHANGE_POSITION(51), AGAIN_ACTION(56),
		SAINTS(61), REVERSE(62), DEDICATION(63), MUTUAL_ASSISTANCE(64),
		IKKATU(71), PANIC(72), CHANGE_MOVE_POWER(73);

		final int id;
		EffectType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private EffectType getType(int no) {
			EffectType[] types = EffectType.values();
			for (EffectType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の EffectType=" +no);
			return ONE_TURN_BUFF;
		}
	}
}
